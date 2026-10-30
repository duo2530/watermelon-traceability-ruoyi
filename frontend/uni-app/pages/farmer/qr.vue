<template>
  <view class="page">
    <view class="card" v-if="batch">
      <view class="brand">{{ batch.brandName || '西瓜溯源' }}</view>
      <view class="variety">{{ batch.variety }} · {{ batch.plotName || '' }}</view>
      <image class="qr" :src="qrUrl" mode="aspectFit" show-menu-by-longpress @click="preview" />
      <view class="code">{{ batch.batchCode }}</view>
      <view class="hint">扫码查看种植全过程 · 已被扫 {{ batch.scanCount || 0 }} 次</view>
      <view class="content">{{ qrContent }}</view>
      <view class="btns">
        <button size="mini" @click="save">保存到相册</button>
        <button size="mini" @click="regen">重新生成</button>
        <button size="mini" type="primary" @click="viewTrace">消费者视角</button>
      </view>
    </view>
    <view class="actions" v-if="batch && batch.status !== '2'">
      <button class="act" @click="go('/pages/farmer/farm-record?batchId=' + batch.batchId)">录入农事</button>
      <button class="act" @click="go('/pages/farmer/qc-submit?batchId=' + batch.batchId)">填报糖度</button>
    </view>
  </view>
</template>

<script setup>
  import { ref, computed } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import config from '@/config'
  import { getBatch, genQrcode } from '@/api/trace'

  const batch = ref(null)
  const qrContent = ref('')
  const stamp = ref(Date.now())
  const qrUrl = computed(() => (batch.value && batch.value.qrPath ? config.baseUrl + batch.value.qrPath + '?t=' + stamp.value : ''))

  let id = null

  function load() {
    return getBatch(id).then(res => {
      batch.value = res.data
      qrContent.value = res.qrContent
      if (!res.data.qrPath) return regen()
    })
  }

  function regen() {
    return genQrcode(id).then(res => {
      batch.value = res.data
      qrContent.value = res.qrContent
      stamp.value = Date.now()
    })
  }

  function preview() {
    uni.previewImage({ urls: [qrUrl.value] })
  }

  function save() {
    // #ifdef H5
    const a = document.createElement('a')
    a.href = qrUrl.value
    a.download = batch.value.batchCode + '.png'
    a.click()
    // #endif
    // #ifndef H5
    uni.downloadFile({
      url: qrUrl.value,
      success: r => uni.saveImageToPhotosAlbum({
        filePath: r.tempFilePath,
        success: () => uni.showToast({ title: '已保存' }),
        fail: () => uni.showToast({ icon: 'none', title: '保存失败，请检查相册权限' })
      })
    })
    // #endif
  }

  function viewTrace() {
    go('/pages/consumer/trace?code=' + batch.value.batchCode)
  }

  function go(url) {
    uni.navigateTo({ url })
  }

  onLoad(opt => {
    id = opt.id
    load()
  })
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding: 32rpx; box-sizing: border-box; }
  .card { background: #fff; border-radius: 24rpx; padding: 40rpx 32rpx; text-align: center; }
  .brand { font-size: 40rpx; font-weight: bold; color: #2e7d32; }
  .variety { font-size: 26rpx; color: #999; margin-top: 8rpx; }
  .qr { width: 460rpx; height: 460rpx; margin: 32rpx auto 16rpx; display: block; }
  .code { font-size: 36rpx; font-weight: bold; letter-spacing: 2rpx; }
  .hint { font-size: 24rpx; color: #999; margin-top: 8rpx; }
  .content { font-size: 20rpx; color: #bbb; margin-top: 12rpx; word-break: break-all; }
  .btns { display: flex; justify-content: center; gap: 16rpx; margin-top: 32rpx; }
  .actions { display: flex; gap: 24rpx; margin-top: 32rpx; }
  .act { flex: 1; background: #fff; color: #2e7d32; border-radius: 48rpx; font-size: 28rpx; }
</style>
