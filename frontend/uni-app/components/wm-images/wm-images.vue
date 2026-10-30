<template>
  <view class="wm-images">
    <view class="img-item" v-for="(img, i) in items" :key="i">
      <image :src="img.preview" mode="aspectFill" class="img" @click="previewAt(i)" />
      <view class="badge" v-if="img.local">待上传</view>
      <view class="del" @click="remove(i)">×</view>
    </view>
    <view class="img-item add" v-if="items.length < max" @click="choose">
      <uni-icons type="camera" size="28" color="#999"></uni-icons>
    </view>
  </view>
</template>

<script setup>
  /**
   * 图文材料选择：在线时立即上传（得到 /profile/... 路径）；离线时保留本地路径，等同步时上传
   * v-model: { uploaded: ['/profile/upload/..'], local: ['wxfile://...'] }
   */
  import { computed } from 'vue'
  import config from '@/config'
  import { uploadImage } from '@/api/trace'
  import { isOnline, persistImage } from '@/utils/offlineQueue'

  const props = defineProps({
    modelValue: { type: Object, default: () => ({ uploaded: [], local: [] }) },
    max: { type: Number, default: 6 }
  })
  const emit = defineEmits(['update:modelValue'])

  const items = computed(() => [
    ...props.modelValue.uploaded.map(p => ({ preview: config.baseUrl + p, path: p, local: false })),
    ...props.modelValue.local.map(p => ({ preview: p, path: p, local: true }))
  ])

  function update(uploaded, local) {
    emit('update:modelValue', { uploaded, local })
  }

  async function choose() {
    const remain = props.max - items.value.length
    uni.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      success: async res => {
        const online = await isOnline()
        const uploaded = [...props.modelValue.uploaded]
        const local = [...props.modelValue.local]
        for (const p of res.tempFilePaths) {
          if (online) {
            try {
              uni.showLoading({ title: '上传中' })
              const r = await uploadImage(p)
              uploaded.push(r.fileName)
              continue
            } catch (e) {
              // 上传失败转为离线保存
            } finally {
              uni.hideLoading()
            }
          }
          local.push(await persistImage(p))
        }
        update(uploaded, local)
      }
    })
  }

  function remove(i) {
    const n = props.modelValue.uploaded.length
    const uploaded = [...props.modelValue.uploaded]
    const local = [...props.modelValue.local]
    if (i < n) uploaded.splice(i, 1)
    else local.splice(i - n, 1)
    update(uploaded, local)
  }

  function previewAt(i) {
    uni.previewImage({ current: i, urls: items.value.map(x => x.preview) })
  }
</script>

<style lang="scss" scoped>
  .wm-images {
    display: flex;
    flex-wrap: wrap;
  }
  .img-item {
    position: relative;
    width: 150rpx;
    height: 150rpx;
    margin: 0 16rpx 16rpx 0;
    border-radius: 8rpx;
    overflow: hidden;
    background: #f5f5f5;
  }
  .img {
    width: 100%;
    height: 100%;
  }
  .add {
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px dashed #ccc;
  }
  .badge {
    position: absolute;
    left: 0;
    bottom: 0;
    right: 0;
    background: rgba(230, 162, 60, 0.85);
    color: #fff;
    font-size: 20rpx;
    text-align: center;
  }
  .del {
    position: absolute;
    top: 0;
    right: 0;
    width: 40rpx;
    height: 40rpx;
    line-height: 36rpx;
    text-align: center;
    color: #fff;
    background: rgba(0, 0, 0, 0.5);
    border-bottom-left-radius: 8rpx;
  }
</style>
