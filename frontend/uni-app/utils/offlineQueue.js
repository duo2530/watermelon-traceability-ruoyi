/**
 * 离线录入队列
 *
 * 田间弱网时，农事/品控记录先写入本地队列，联网后批量提交 /trace/sync/batch。
 * 每条记录带客户端生成的 clientId，服务端按 clientId 去重，重复提交不会产生重复数据。
 *
 * 队列项: { clientId, type: 'farm'|'qc', payload, localImages: [], createdAt, retry, status: 'pending'|'rejected', message }
 */
import { syncBatch, uploadImage } from '@/api/trace'
import { getToken } from '@/utils/auth'

const QUEUE_KEY = 'wm_offline_queue'
const BATCH_CACHE_KEY = 'wm_batch_options'
const SYNC_SIZE = 50

let flushing = false
const listeners = []

function read() {
  return uni.getStorageSync(QUEUE_KEY) || []
}

function write(list) {
  uni.setStorageSync(QUEUE_KEY, list)
  listeners.forEach(fn => {
    try { fn(list) } catch (e) { console.error(e) }
  })
}

export function uuid() {
  return 'c' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10) + Math.random().toString(36).slice(2, 6)
}

export function getQueue() {
  return read()
}

export function pendingCount() {
  return read().filter(i => i.status !== 'rejected').length
}

/** 订阅队列变化，返回取消函数 */
export function onQueueChange(fn) {
  listeners.push(fn)
  return () => {
    const i = listeners.indexOf(fn)
    i > -1 && listeners.splice(i, 1)
  }
}

/**
 * 入队
 * @param type farm | qc
 * @param payload 业务数据
 * @param localImages 尚未上传的本地图片路径
 */
export function enqueue(type, payload, localImages = []) {
  const item = { clientId: uuid(), type, payload, localImages, createdAt: Date.now(), retry: 0, status: 'pending', message: '' }
  const list = read()
  list.push(item)
  write(list)
  return item
}

export function removeItem(clientId) {
  write(read().filter(i => i.clientId !== clientId))
}

/** 把被拒绝的条目重新置为待同步 */
export function retryItem(clientId) {
  write(read().map(i => (i.clientId === clientId ? { ...i, status: 'pending', message: '' } : i)))
}

export function isOnline() {
  return new Promise(resolve => {
    uni.getNetworkType({
      success: res => resolve(res.networkType !== 'none'),
      fail: () => resolve(true)
    })
  })
}

/**
 * 本地图片持久化（小程序/App 的临时文件可能被清理）；H5 不支持 saveFile 时原样返回
 */
export function persistImage(tempPath) {
  return new Promise(resolve => {
    // #ifdef H5
    resolve(tempPath)
    // #endif
    // #ifndef H5
    uni.saveFile({
      tempFilePath: tempPath,
      success: res => resolve(res.savedFilePath),
      fail: () => resolve(tempPath)
    })
    // #endif
  })
}

async function uploadLocalImages(item) {
  if (!item.localImages || !item.localImages.length) return item
  const uploaded = []
  for (const p of item.localImages) {
    const res = await uploadImage(p)
    uploaded.push(res.fileName)
  }
  const exist = item.payload.images ? item.payload.images.split(',').filter(Boolean) : []
  return { ...item, localImages: [], payload: { ...item.payload, images: exist.concat(uploaded).join(',') } }
}

/**
 * 同步队列。返回 { accepted, duplicate, rejected, failed, skipped }
 */
export async function flush() {
  const summary = { accepted: 0, duplicate: 0, rejected: 0, failed: 0, skipped: false }
  if (flushing || !getToken()) {
    summary.skipped = true
    return summary
  }
  if (!(await isOnline())) {
    summary.skipped = true
    return summary
  }
  flushing = true
  try {
    let pending = read().filter(i => i.status !== 'rejected')
    // 先上传离线时拍的图片；单条失败保留在队列，下次再试
    const ready = []
    for (const item of pending) {
      try {
        const done = await uploadLocalImages(item)
        if (done !== item) {
          write(read().map(i => (i.clientId === done.clientId ? done : i)))
        }
        ready.push(done)
      } catch (e) {
        summary.failed++
      }
    }
    for (let i = 0; i < ready.length; i += SYNC_SIZE) {
      const chunk = ready.slice(i, i + SYNC_SIZE)
      let results
      try {
        const res = await syncBatch(chunk.map(c => ({ clientId: c.clientId, type: c.type, payload: c.payload, createdAt: c.createdAt })))
        results = res.data || []
      } catch (e) {
        summary.failed += chunk.length
        write(read().map(it => (chunk.find(c => c.clientId === it.clientId) ? { ...it, retry: (it.retry || 0) + 1 } : it)))
        break
      }
      const byId = {}
      results.forEach(r => { byId[r.clientId] = r })
      const next = []
      read().forEach(it => {
        const r = byId[it.clientId]
        if (!r) {
          next.push(it)
        } else if (r.result === 'accepted' || r.result === 'duplicate') {
          summary[r.result]++
        } else {
          summary.rejected++
          next.push({ ...it, status: 'rejected', message: r.message })
        }
      })
      write(next)
    }
  } finally {
    flushing = false
  }
  return summary
}

let autoInstalled = false

/** 网络恢复时自动同步（在 App.vue onLaunch 调用一次） */
export function installAutoSync() {
  if (autoInstalled) return
  autoInstalled = true
  uni.onNetworkStatusChange(res => {
    if (res.isConnected && pendingCount() > 0) {
      flush().then(s => {
        if (s.accepted > 0) {
          uni.showToast({ icon: 'none', title: `已自动同步 ${s.accepted} 条离线记录` })
        }
      })
    }
  })
}

/** 缓存批次下拉，离线时也能选择批次 */
export function cacheBatchOptions(list) {
  uni.setStorageSync(BATCH_CACHE_KEY, list || [])
}

export function cachedBatchOptions() {
  return uni.getStorageSync(BATCH_CACHE_KEY) || []
}

/**
 * 录入统一入口：先入队，再尝试立即同步。在线和离线走同一条幂等通道。
 * @returns { status: 'synced' | 'queued' | 'rejected', message }
 */
export async function submitRecord(type, payload, images) {
  const p = { ...payload, images: (images.uploaded || []).join(',') }
  const item = enqueue(type, p, images.local || [])
  await flush()
  const left = read().find(i => i.clientId === item.clientId)
  if (!left) return { status: 'synced', message: '' }
  if (left.status === 'rejected') {
    // 当场被拒绝的记录不留在队列里，表单内容保留给用户修改后重新提交
    removeItem(item.clientId)
    return { status: 'rejected', message: left.message }
  }
  return { status: 'queued', message: '' }
}
