/**
 * 农户录入页共用的批次选择：在线拉取并缓存，离线时用缓存
 */
import { ref } from 'vue'
import { batchOptions } from '@/api/trace'
import { isOnline, cacheBatchOptions, cachedBatchOptions } from '@/utils/offlineQueue'

export function formatNow() {
  const d = new Date()
  const p = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

export function useBatchPicker(form) {
  const batches = ref([])
  const batchIndex = ref(-1)
  const online = ref(true)

  function apply(list, presetId) {
    batches.value = list.map(b => ({ ...b, label: b.batchCode + ' ' + (b.variety || '') }))
    const id = presetId ? Number(presetId) : form.value.batchId
    batchIndex.value = batches.value.findIndex(b => b.batchId === id)
    if (batchIndex.value === -1 && batches.value.length === 1) {
      batchIndex.value = 0
    }
    form.value.batchId = batchIndex.value > -1 ? batches.value[batchIndex.value].batchId : null
  }

  async function loadBatches(presetId) {
    online.value = await isOnline()
    if (online.value) {
      try {
        const res = await batchOptions()
        cacheBatchOptions(res.data)
        apply(res.data, presetId)
        return
      } catch (e) {
        // 接口失败时退回缓存
      }
    }
    apply(cachedBatchOptions(), presetId)
  }

  function onBatch(e) {
    batchIndex.value = Number(e.detail.value)
    form.value.batchId = batches.value[batchIndex.value].batchId
  }

  return { batches, batchIndex, online, loadBatches, onBatch }
}
