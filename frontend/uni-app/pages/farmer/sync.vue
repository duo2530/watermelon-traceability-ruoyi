<template>
  <view class="page">
    <view class="summary">
      <view class="num">{{ pending.length }}</view>
      <view class="desc">条待同步</view>
      <view class="net" :class="online ? 'on' : 'off'">{{ online ? '网络正常' : '无网络' }}</view>
      <button class="sync-btn" :loading="syncing" :disabled="!pending.length || !online" @click="syncNow">立即同步</button>
      <view class="last" v-if="lastResult">{{ lastResult }}</view>
    </view>

    <uni-section title="待同步" type="line" v-if="pending.length"></uni-section>
    <view class="item" v-for="i in pending" :key="i.clientId">
      <view class="row">
        <uni-tag :text="i.type === 'farm' ? '农事' : '品控'" size="small" :type="i.type === 'farm' ? 'success' : 'warning'" />
        <text class="title">{{ describe(i) }}</text>
      </view>
      <view class="meta">{{ time(i.createdAt) }} · 重试 {{ i.retry || 0 }} 次<text v-if="i.localImages && i.localImages.length"> · {{ i.localImages.length }} 张图片待上传</text></view>
      <view class="ops"><text class="del" @click="remove(i)">删除</text></view>
    </view>

    <uni-section title="被服务端拒绝（需处理）" type="line" v-if="rejected.length"></uni-section>
    <view class="item rejected" v-for="i in rejected" :key="i.clientId">
      <view class="row">
        <uni-tag :text="i.type === 'farm' ? '农事' : '品控'" size="small" type="error" />
        <text class="title">{{ describe(i) }}</text>
      </view>
      <view class="reason">原因：{{ i.message }}</view>
      <view class="ops">
        <text class="retry" @click="retry(i)">重新提交</text>
        <text class="del" @click="remove(i)">删除</text>
      </view>
    </view>

    <view class="empty" v-if="!queue.length">
      <uni-icons type="checkbox-filled" size="60" color="#4caf50"></uni-icons>
      <view>本机没有待同步的记录</view>
    </view>

    <view class="note">
      说明：田间无网络时，农事和品控记录会先保存在本机；联网后打开小程序或网络恢复时会自动同步。每条记录有唯一编号，重复提交不会产生重复数据。
    </view>
  </view>
</template>

<script setup>
  import { ref, computed } from 'vue'
  import { onShow, onUnload } from '@dcloudio/uni-app'
  import { getQueue, flush, removeItem, retryItem, isOnline, onQueueChange, cachedBatchOptions } from '@/utils/offlineQueue'

  const queue = ref(getQueue())
  const online = ref(true)
  const syncing = ref(false)
  const lastResult = ref('')
  const pending = computed(() => queue.value.filter(i => i.status !== 'rejected'))
  const rejected = computed(() => queue.value.filter(i => i.status === 'rejected'))
  const batchMap = {}
  cachedBatchOptions().forEach(b => { batchMap[b.batchId] = b.batchCode })

  const off = onQueueChange(list => { queue.value = list })
  onUnload(off)

  const typeLabel = { TILL: '整地', PLANT: '育苗定植', FERTILIZE: '施肥', IRRIGATE: '灌溉', PESTICIDE: '植保用药', POLLINATE: '授粉', PRUNE: '整枝压蔓', HARVEST: '采收', OTHER: '其他' }

  function describe(i) {
    const b = batchMap[i.payload.batchId] || ('批次#' + i.payload.batchId)
    if (i.type === 'farm') return `${b} ${typeLabel[i.payload.recordType] || i.payload.recordType} ${i.payload.content || ''}`
    return `${b} 中心糖 ${i.payload.sugarCenter}°Bx${i.payload.weightKg ? ' · ' + i.payload.weightKg + 'kg' : ''}`
  }

  function time(ts) {
    const d = new Date(ts)
    const p = n => (n < 10 ? '0' + n : n)
    return `${d.getMonth() + 1}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
  }

  async function syncNow() {
    syncing.value = true
    try {
      const s = await flush()
      if (s.skipped) {
        lastResult.value = '暂时无法同步（无网络或正在同步中）'
      } else {
        lastResult.value = `成功 ${s.accepted} 条，重复 ${s.duplicate} 条，拒绝 ${s.rejected} 条` + (s.failed ? `，失败 ${s.failed} 条（稍后重试）` : '')
      }
    } finally {
      syncing.value = false
      queue.value = getQueue()
    }
  }

  function remove(i) {
    uni.showModal({
      title: '删除记录',
      content: '删除后该记录不会再同步到服务器，确定删除？',
      success: r => r.confirm && removeItem(i.clientId)
    })
  }

  function retry(i) {
    retryItem(i.clientId)
    syncNow()
  }

  onShow(async () => {
    queue.value = getQueue()
    online.value = await isOnline()
  })
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding: 24rpx; box-sizing: border-box; }
  .summary { background: #fff; border-radius: 16rpx; padding: 40rpx; text-align: center; margin-bottom: 24rpx; }
  .num { font-size: 80rpx; font-weight: bold; color: #ff9800; }
  .desc { color: #666; font-size: 26rpx; }
  .net { display: inline-block; margin-top: 16rpx; font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
  .net.on { background: #e8f5e9; color: #2e7d32; }
  .net.off { background: #fdecea; color: #e53935; }
  .sync-btn { margin-top: 24rpx; background: #2e7d32; color: #fff; border-radius: 48rpx; }
  .last { margin-top: 16rpx; font-size: 24rpx; color: #666; }
  .item { background: #fff; border-radius: 12rpx; padding: 20rpx 24rpx; margin-bottom: 16rpx; }
  .row { display: flex; align-items: center; }
  .title { margin-left: 12rpx; font-size: 28rpx; color: #333; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .meta { font-size: 22rpx; color: #999; margin-top: 8rpx; }
  .reason { font-size: 24rpx; color: #e53935; margin-top: 8rpx; }
  .ops { display: flex; justify-content: flex-end; margin-top: 8rpx; }
  .del { color: #999; font-size: 24rpx; margin-left: 32rpx; }
  .retry { color: #2e7d32; font-size: 24rpx; }
  .empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
  .note { font-size: 22rpx; color: #999; line-height: 1.6; padding: 24rpx 8rpx; }
</style>
