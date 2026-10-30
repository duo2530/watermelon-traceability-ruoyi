<template>
  <view class="page">
    <view class="hero">
      <uni-icons type="scan" size="80" color="#2e7d32"></uni-icons>
      <view class="title">西瓜溯源 · 扫码验真</view>
      <view class="sub">扫描瓜贴/包装上的二维码，查看种植全过程</view>
    </view>
    <!-- #ifndef H5 -->
    <button class="scan-btn" @click="scan">扫一扫</button>
    <!-- #endif -->
    <view class="manual">
      <uni-easyinput v-model="code" placeholder="或输入溯源码，如 WM202610310001" :maxlength="64" @confirm="query" />
      <button class="query-btn" size="mini" @click="query">查询</button>
    </view>
    <view class="history" v-if="history.length">
      <view class="h-title">最近查询</view>
      <view class="h-item" v-for="h in history" :key="h" @click="open(h)">{{ h }}</view>
    </view>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { parseTraceCode } from '@/utils/traceCode'

  const HISTORY_KEY = 'wm_trace_history'
  const code = ref('')
  const history = ref(uni.getStorageSync(HISTORY_KEY) || [])

  function open(c) {
    const h = [c, ...history.value.filter(x => x !== c)].slice(0, 5)
    history.value = h
    uni.setStorageSync(HISTORY_KEY, h)
    uni.navigateTo({ url: '/pages/consumer/trace?code=' + c })
  }

  function query() {
    const c = parseTraceCode(code.value)
    if (!c) {
      uni.showToast({ icon: 'none', title: '溯源码格式不正确' })
      return
    }
    open(c)
  }

  function scan() {
    uni.scanCode({
      scanType: ['qrCode'],
      success: res => {
        const c = parseTraceCode(res.result || res.path || '')
        if (!c) {
          uni.showToast({ icon: 'none', title: '不是本平台的溯源码' })
          return
        }
        open(c)
      }
    })
  }
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding: 40rpx 32rpx; box-sizing: border-box; }
  .hero { text-align: center; padding: 60rpx 0 40rpx; }
  .title { font-size: 40rpx; font-weight: bold; margin-top: 16rpx; color: #333; }
  .sub { font-size: 26rpx; color: #999; margin-top: 12rpx; }
  .scan-btn { background: #2e7d32; color: #fff; border-radius: 48rpx; margin-bottom: 32rpx; }
  .manual { display: flex; align-items: center; gap: 16rpx; background: #fff; padding: 20rpx; border-radius: 16rpx; }
  .query-btn { background: #2e7d32; color: #fff; }
  .history { margin-top: 32rpx; }
  .h-title { font-size: 26rpx; color: #999; margin-bottom: 12rpx; }
  .h-item { background: #fff; padding: 20rpx 24rpx; border-radius: 12rpx; margin-bottom: 12rpx; font-size: 28rpx; color: #2e7d32; }
</style>
