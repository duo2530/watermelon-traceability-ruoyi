<template>
  <view class="page">
    <view class="header">
      <view class="hello">{{ name }}，您好</view>
      <view class="sub">西瓜种植溯源 · 农户工作台</view>
      <view class="sync-bar" @click="go('/pages/farmer/sync')">
        <uni-icons :type="pending > 0 ? 'cloud-upload' : 'checkbox'" size="18" color="#fff"></uni-icons>
        <text class="sync-text">{{ pending > 0 ? `${pending} 条离线记录待同步` : '数据已全部同步' }}</text>
        <text class="sync-link">查看 ›</text>
      </view>
    </view>

    <view class="grid">
      <view class="cell" v-for="m in menus" :key="m.url" @click="go(m.url)">
        <view class="icon" :style="{ background: m.color }">
          <uni-icons :type="m.icon" size="30" color="#fff"></uni-icons>
        </view>
        <text class="label">{{ m.label }}</text>
      </view>
    </view>

    <uni-section title="我的批次" type="line" sub-title="点击查看二维码"></uni-section>
    <view class="batch" v-for="b in batches" :key="b.batchId" @click="go('/pages/farmer/qr?id=' + b.batchId)">
      <view class="batch-main">
        <text class="code">{{ b.batchCode }}</text>
        <text class="meta">{{ b.variety }} · {{ b.plotName || '未指定地块' }}</text>
      </view>
      <uni-tag :text="statusText(b.status)" size="small" :type="b.status === '0' ? 'primary' : 'default'" />
    </view>
    <view class="empty" v-if="!batches.length">还没有批次，先去“新建批次”吧</view>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
  import { useUserStore } from '@/store'
  import { listBatch } from '@/api/trace'
  import { pendingCount, flush } from '@/utils/offlineQueue'

  const name = ref(useUserStore().name || '农户')
  const pending = ref(0)
  const batches = ref([])
  const menus = [
    { label: '农事录入', icon: 'compose', color: '#4caf50', url: '/pages/farmer/farm-record' },
    { label: '糖度/果型', icon: 'medal', color: '#ff7043', url: '/pages/farmer/qc-submit' },
    { label: '新建批次', icon: 'plusempty', color: '#42a5f5', url: '/pages/farmer/batch-create' },
    { label: '溯源二维码', icon: 'scan', color: '#7e57c2', url: '/pages/farmer/batch-list' },
    { label: '离线同步', icon: 'cloud-upload', color: '#ffa726', url: '/pages/farmer/sync' },
    { label: '扫码验真', icon: 'search', color: '#26a69a', url: '/pages/consumer/scan' }
  ]

  function statusText(s) {
    return { '0': '种植中', '1': '已采收', '2': '已归档' }[s] || s
  }

  function go(url) {
    uni.navigateTo({ url })
  }

  function load() {
    pending.value = pendingCount()
    return listBatch({ pageNum: 1, pageSize: 5 }).then(res => {
      batches.value = res.rows
    }).catch(() => {})
  }

  onShow(() => {
    load()
    // 回到工作台时顺带尝试同步离线数据
    if (pendingCount() > 0) {
      flush().then(() => { pending.value = pendingCount() })
    }
  })

  onPullDownRefresh(() => {
    load().finally(() => uni.stopPullDownRefresh())
  })
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding-bottom: 40rpx; }
  .header { background: linear-gradient(135deg, #2e7d32, #66bb6a); padding: 40rpx 32rpx 32rpx; color: #fff; }
  .hello { font-size: 40rpx; font-weight: bold; }
  .sub { font-size: 26rpx; opacity: 0.85; margin-top: 8rpx; }
  .sync-bar { margin-top: 28rpx; background: rgba(255, 255, 255, 0.18); border-radius: 12rpx; padding: 18rpx 20rpx; display: flex; align-items: center; }
  .sync-text { flex: 1; margin-left: 12rpx; font-size: 26rpx; }
  .sync-link { font-size: 24rpx; opacity: 0.9; }
  .grid { display: flex; flex-wrap: wrap; background: #fff; margin: 24rpx; border-radius: 16rpx; padding: 16rpx 0; }
  .cell { width: 33.33%; display: flex; flex-direction: column; align-items: center; padding: 24rpx 0; }
  .icon { width: 96rpx; height: 96rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; }
  .label { margin-top: 12rpx; font-size: 26rpx; color: #333; }
  .batch { background: #fff; margin: 0 24rpx 16rpx; border-radius: 12rpx; padding: 24rpx; display: flex; align-items: center; }
  .batch-main { flex: 1; display: flex; flex-direction: column; }
  .code { font-size: 30rpx; font-weight: bold; color: #333; }
  .meta { font-size: 24rpx; color: #999; margin-top: 6rpx; }
  .empty { text-align: center; color: #999; font-size: 26rpx; padding: 40rpx; }
</style>
