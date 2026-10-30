<template>
  <view class="page">
    <view class="batch" v-for="b in list" :key="b.batchId" @click="open(b)">
      <view class="main">
        <text class="code">{{ b.batchCode }}</text>
        <text class="meta">{{ b.variety }} · {{ b.plotName || '未指定地块' }} · 扫码 {{ b.scanCount || 0 }} 次</text>
        <text class="meta">定植 {{ b.plantDate || '-' }}　品牌 {{ b.brandName || '-' }}</text>
      </view>
      <view class="ops">
        <text class="op" @click.stop="go('/pages/farmer/farm-record?batchId=' + b.batchId)" v-if="b.status !== '2'">农事</text>
        <text class="op" @click.stop="go('/pages/farmer/qc-submit?batchId=' + b.batchId)" v-if="b.status !== '2'">品控</text>
        <text class="op qr">二维码 ›</text>
      </view>
    </view>
    <uni-load-more :status="loadStatus" />
    <view class="fab" @click="go('/pages/farmer/batch-create')">
      <uni-icons type="plusempty" size="28" color="#fff"></uni-icons>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
  import { listBatch } from '@/api/trace'

  const list = ref([])
  const pageNum = ref(1)
  const total = ref(0)
  const loadStatus = ref('more')

  function load(reset) {
    if (reset) {
      pageNum.value = 1
      list.value = []
    }
    loadStatus.value = 'loading'
    return listBatch({ pageNum: pageNum.value, pageSize: 10 }).then(res => {
      list.value = list.value.concat(res.rows)
      total.value = res.total
      loadStatus.value = list.value.length >= total.value ? 'noMore' : 'more'
    }).catch(() => { loadStatus.value = 'more' })
  }

  function open(b) {
    go('/pages/farmer/qr?id=' + b.batchId)
  }

  function go(url) {
    uni.navigateTo({ url })
  }

  onShow(() => load(true))
  onReachBottom(() => {
    if (loadStatus.value === 'more') {
      pageNum.value++
      load()
    }
  })
  onPullDownRefresh(() => load(true).finally(() => uni.stopPullDownRefresh()))
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding: 24rpx; box-sizing: border-box; }
  .batch { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
  .main { display: flex; flex-direction: column; }
  .code { font-size: 32rpx; font-weight: bold; color: #333; }
  .meta { font-size: 24rpx; color: #999; margin-top: 8rpx; }
  .ops { display: flex; justify-content: flex-end; margin-top: 16rpx; border-top: 1px solid #f0f0f0; padding-top: 16rpx; }
  .op { font-size: 26rpx; color: #2e7d32; margin-left: 32rpx; }
  .op.qr { color: #7e57c2; }
  .fab { position: fixed; right: 40rpx; bottom: 80rpx; width: 100rpx; height: 100rpx; border-radius: 50%; background: #2e7d32; display: flex; align-items: center; justify-content: center; box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.2); }
</style>
