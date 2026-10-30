<template>
  <view class="page">
    <view class="banner">
      <view class="title">西瓜溯源</view>
      <view class="sub">一批一码 · 种植全过程可查</view>
    </view>
    <view class="entry" @click="go('/pages/consumer/scan')">
      <uni-icons type="scan" size="40" color="#2e7d32"></uni-icons>
      <view class="entry-text">
        <view class="entry-title">扫码验真</view>
        <view class="entry-sub">消费者 / 采购商查看种植过程与果品等级</view>
      </view>
      <uni-icons type="right" size="18" color="#ccc"></uni-icons>
    </view>
    <view class="entry" @click="go('/pages/farmer/index')" v-if="isFarmer">
      <uni-icons type="compose" size="40" color="#ff7043"></uni-icons>
      <view class="entry-text">
        <view class="entry-title">农户工作台</view>
        <view class="entry-sub">农事录入 · 糖度果型 · 批次二维码 · 离线同步</view>
      </view>
      <uni-icons type="right" size="18" color="#ccc"></uni-icons>
    </view>
    <view class="tip" v-if="!isFarmer">当前账号没有录入权限。如需录入农事，请联系管理员分配“农户”角色。</view>
  </view>
</template>

<script setup>
  import { computed } from 'vue'
  import { checkPermi } from '@/utils/permission'

  const isFarmer = computed(() => checkPermi(['trace:sync:submit', 'trace:farm:add']))

  function go(url) {
    uni.navigateTo({ url })
  }
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding: 24rpx; box-sizing: border-box; }
  .banner { background: linear-gradient(135deg, #1b5e20, #66bb6a); border-radius: 24rpx; color: #fff; padding: 60rpx 40rpx; margin-bottom: 24rpx; }
  .title { font-size: 48rpx; font-weight: bold; }
  .sub { font-size: 26rpx; opacity: 0.9; margin-top: 12rpx; }
  .entry { display: flex; align-items: center; background: #fff; border-radius: 16rpx; padding: 32rpx 28rpx; margin-bottom: 20rpx; }
  .entry-text { flex: 1; margin-left: 24rpx; }
  .entry-title { font-size: 32rpx; font-weight: bold; color: #333; }
  .entry-sub { font-size: 24rpx; color: #999; margin-top: 6rpx; }
  .tip { font-size: 24rpx; color: #999; padding: 16rpx 8rpx; }
</style>
