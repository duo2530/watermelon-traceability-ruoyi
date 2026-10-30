<template>
  <view class="page">
    <view v-if="error" class="error">
      <uni-icons type="info" size="60" color="#e53935"></uni-icons>
      <view class="err-text">{{ error }}</view>
      <button size="mini" @click="back">返回扫码</button>
    </view>

    <view v-else-if="data">
      <!-- 头部：品牌 + 等级 -->
      <view class="hero" :style="cover ? `background-image: linear-gradient(rgba(0,0,0,.35), rgba(0,0,0,.55)), url(${cover})` : ''">
        <view class="verified"><uni-icons type="checkbox-filled" size="16" color="#fff"></uni-icons> 正品溯源 · 第 {{ data.batch.scanCount }} 次查询</view>
        <view class="brand-row">
          <image v-if="data.brand.logo" class="logo" :src="img(data.brand.logo)" mode="aspectFill" />
          <view>
            <view class="brand">{{ data.brand.name || '西瓜' }}</view>
            <view class="variety">品种：{{ data.batch.variety }}</view>
          </view>
        </view>
        <view class="code">溯源码 {{ data.batch.batchCode }}</view>
      </view>

      <!-- 品质 -->
      <view class="card quality" v-if="data.quality.sampleCount">
        <view class="grade-box">
          <view class="grade">{{ data.quality.mainGrade }}</view>
          <view class="label">果品等级</view>
        </view>
        <view class="metric">
          <view class="v">{{ data.quality.avgSugar }}<text class="u">°Brix</text></view>
          <view class="label">平均中心糖度</view>
        </view>
        <view class="metric">
          <view class="v">{{ data.quality.minSugar }}~{{ data.quality.maxSugar }}</view>
          <view class="label">糖度范围</view>
        </view>
      </view>
      <view class="card" v-if="data.quality.sampleCount">
        <view class="card-title">抽检明细（{{ data.quality.sampleCount }} 个样本）</view>
        <view class="sample" v-for="(s, i) in data.quality.samples" :key="i">
          <text class="s-grade">{{ s.gradeName }}</text>
          <text>中心糖 {{ s.sugarCenter }}</text>
          <text v-if="s.sugarEdge">边糖 {{ s.sugarEdge }}</text>
          <text v-if="s.weightKg">{{ s.weightKg }}kg</text>
          <text>{{ s.fruitShape }}</text>
        </view>
      </view>

      <!-- 产地 -->
      <view class="card">
        <view class="card-title">产地信息</view>
        <view class="kv"><text class="k">种植基地</text><text class="v2">{{ data.plot ? data.plot.plotName : '-' }}</text></view>
        <view class="kv"><text class="k">产地</text><text class="v2">{{ data.plot && data.plot.address ? data.plot.address : '-' }}</text></view>
        <view class="kv" v-if="data.plot && data.plot.soilType"><text class="k">土壤</text><text class="v2">{{ data.plot.soilType }}</text></view>
        <view class="kv"><text class="k">种植户</text><text class="v2">{{ data.batch.farmerName || '-' }}</text></view>
        <view class="kv"><text class="k">定植日期</text><text class="v2">{{ data.batch.plantDate || '-' }}</text></view>
        <view class="kv"><text class="k">采收日期</text><text class="v2">{{ data.batch.harvestDate || '-' }}</text></view>
      </view>

      <!-- 种植全过程 -->
      <view class="card">
        <view class="card-title">种植全过程</view>
        <view v-if="!data.timeline.length" class="muted">暂无农事记录</view>
        <view class="tl" v-for="(t, i) in data.timeline" :key="i">
          <view class="dot" />
          <view class="tl-body">
            <view class="tl-head"><text class="tl-type">{{ t.recordTypeLabel || t.recordType }}</text><text class="tl-time">{{ (t.recordTime || '').slice(0, 16) }}</text></view>
            <view class="tl-content" v-if="t.content">{{ t.content }}</view>
            <view class="tl-input" v-if="t.inputName">投入品：{{ t.inputName }} {{ t.inputAmount }}</view>
            <view class="tl-imgs" v-if="t.images.length">
              <image v-for="(p, j) in t.images" :key="j" :src="img(p)" mode="aspectFill" class="tl-img" @click="previewImgs(t.images, j)" />
            </view>
          </view>
        </view>
      </view>

      <!-- 品牌 -->
      <view class="card" v-if="data.brand.desc">
        <view class="card-title">品牌故事</view>
        <view class="desc">{{ data.brand.desc }}</view>
      </view>
      <view class="footer">数据由种植户实时记录，经平台存证 · 西瓜溯源平台</view>
    </view>
  </view>
</template>

<script setup>
  import { ref, computed } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import config from '@/config'
  import { publicTrace } from '@/api/trace'
  import { parseTraceCode } from '@/utils/traceCode'

  const data = ref(null)
  const error = ref('')
  const cover = computed(() => (data.value && data.value.batch.coverImage ? img(data.value.batch.coverImage) : ''))

  function img(p) {
    if (!p) return ''
    return /^https?:\/\//.test(p) ? p : config.baseUrl + p
  }

  function previewImgs(list, i) {
    uni.previewImage({ current: i, urls: list.map(img) })
  }

  function back() {
    uni.redirectTo({ url: '/pages/consumer/scan' })
  }

  onLoad(opt => {
    // 小程序码进入时参数在 scene 中
    const code = parseTraceCode(opt.code || (opt.scene ? decodeURIComponent(opt.scene) : ''))
    if (!code) {
      error.value = '溯源码无效'
      return
    }
    uni.showLoading({ title: '查询中' })
    publicTrace(code).then(res => {
      data.value = res.data
      uni.setNavigationBarTitle({ title: (res.data.brand.name || '西瓜') + ' · 溯源' })
    }).catch(() => {
      error.value = '未查询到该溯源码，请确认是否为正品'
    }).finally(() => uni.hideLoading())
  })
</script>

<style lang="scss" scoped>
  .page { background: #f5f6f7; min-height: 100vh; padding-bottom: 40rpx; }
  .error { text-align: center; padding: 160rpx 40rpx; }
  .err-text { margin: 24rpx 0 40rpx; color: #666; font-size: 28rpx; }
  .hero { background: linear-gradient(135deg, #1b5e20, #43a047); background-size: cover; background-position: center; color: #fff; padding: 40rpx 32rpx 48rpx; }
  .verified { display: inline-flex; align-items: center; gap: 6rpx; background: rgba(255, 255, 255, 0.2); border-radius: 24rpx; padding: 6rpx 18rpx; font-size: 22rpx; }
  .brand-row { display: flex; align-items: center; margin-top: 28rpx; }
  .logo { width: 110rpx; height: 110rpx; border-radius: 16rpx; margin-right: 24rpx; background: #fff; }
  .brand { font-size: 48rpx; font-weight: bold; }
  .variety { font-size: 26rpx; opacity: 0.9; margin-top: 6rpx; }
  .code { font-size: 24rpx; opacity: 0.8; margin-top: 20rpx; }
  .card { background: #fff; margin: 24rpx; border-radius: 16rpx; padding: 28rpx; }
  .quality { display: flex; margin-top: -24rpx; position: relative; }
  .grade-box, .metric { flex: 1; text-align: center; }
  .grade { font-size: 44rpx; font-weight: bold; color: #e53935; }
  .v { font-size: 40rpx; font-weight: bold; color: #2e7d32; }
  .u { font-size: 22rpx; margin-left: 4rpx; }
  .label { font-size: 22rpx; color: #999; margin-top: 6rpx; }
  .card-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; color: #333; }
  .sample { display: flex; flex-wrap: wrap; gap: 20rpx; font-size: 24rpx; color: #666; padding: 12rpx 0; border-bottom: 1px solid #f5f5f5; }
  .s-grade { color: #e53935; font-weight: bold; }
  .kv { display: flex; font-size: 26rpx; padding: 10rpx 0; }
  .k { width: 160rpx; color: #999; }
  .v2 { flex: 1; color: #333; }
  .muted { color: #999; font-size: 26rpx; }
  .tl { display: flex; position: relative; padding-bottom: 28rpx; }
  .tl::before { content: ''; position: absolute; left: 11rpx; top: 28rpx; bottom: 0; width: 2rpx; background: #e0e0e0; }
  .tl:last-child::before { display: none; }
  .dot { width: 24rpx; height: 24rpx; border-radius: 50%; background: #43a047; margin-top: 6rpx; flex-shrink: 0; }
  .tl-body { flex: 1; margin-left: 20rpx; }
  .tl-head { display: flex; justify-content: space-between; }
  .tl-type { font-size: 28rpx; font-weight: bold; color: #2e7d32; }
  .tl-time { font-size: 22rpx; color: #999; }
  .tl-content { font-size: 26rpx; color: #333; margin-top: 8rpx; }
  .tl-input { font-size: 24rpx; color: #888; margin-top: 6rpx; }
  .tl-imgs { display: flex; flex-wrap: wrap; margin-top: 12rpx; }
  .tl-img { width: 150rpx; height: 150rpx; border-radius: 8rpx; margin: 0 12rpx 12rpx 0; }
  .desc { font-size: 26rpx; color: #555; line-height: 1.7; white-space: pre-wrap; }
  .footer { text-align: center; color: #bbb; font-size: 22rpx; padding: 24rpx; }
</style>
