<template>
  <view class="page">
    <uni-forms ref="formRef" :model="form" :rules="rules" label-width="90px" class="card">
      <uni-forms-item label="地块" name="plotId">
        <view class="row">
          <picker class="flex1" :range="plots" range-key="plotName" :value="plotIndex" @change="e => (plotIndex = Number(e.detail.value))">
            <view class="picker">{{ plotIndex > -1 ? plots[plotIndex].plotName : '请选择地块' }}</view>
          </picker>
          <text class="link" @click="showPlot = true">+新地块</text>
        </view>
      </uni-forms-item>
      <uni-forms-item label="品种" name="variety" required>
        <uni-easyinput v-model="form.variety" placeholder="如：8424、麒麟" />
      </uni-forms-item>
      <uni-forms-item label="定植日期" name="plantDate">
        <uni-datetime-picker type="date" v-model="form.plantDate" />
      </uni-forms-item>
      <uni-forms-item label="预计采收" name="harvestDate">
        <uni-datetime-picker type="date" v-model="form.harvestDate" />
      </uni-forms-item>
      <uni-forms-item label="品牌名称" name="brandName">
        <uni-easyinput v-model="form.brandName" placeholder="如：东台西瓜" />
      </uni-forms-item>
      <uni-forms-item label="品牌介绍" name="brandDesc">
        <uni-easyinput type="textarea" v-model="form.brandDesc" placeholder="产地、种植方式、认证等" maxlength="1000" />
      </uni-forms-item>
      <uni-forms-item label="封面图">
        <wm-images v-model="cover" :max="1" />
      </uni-forms-item>
    </uni-forms>
    <button class="submit" type="primary" :loading="saving" @click="submit">创建批次并生成二维码</button>

    <uni-popup ref="plotPopup" type="dialog" v-if="showPlot">
      <view class="plot-dialog">
        <view class="title">新增地块</view>
        <uni-easyinput v-model="plot.plotName" placeholder="地块名称" />
        <view style="height: 16rpx" />
        <uni-easyinput v-model="plot.address" placeholder="地址（可选）" />
        <view style="height: 16rpx" />
        <uni-easyinput type="digit" v-model="plot.areaMu" placeholder="面积（亩，可选）" />
        <view class="btns">
          <button size="mini" @click="showPlot = false">取消</button>
          <button size="mini" type="primary" @click="savePlot">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
  import { ref, watch, nextTick } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import { plotOptions, addPlot, addBatch } from '@/api/trace'

  const formRef = ref(null)
  const plotPopup = ref(null)
  const saving = ref(false)
  const plots = ref([])
  const plotIndex = ref(-1)
  const showPlot = ref(false)
  const plot = ref({ plotName: '', address: '', areaMu: '' })
  const cover = ref({ uploaded: [], local: [] })
  const form = ref({ variety: '', plantDate: '', harvestDate: '', brandName: '', brandDesc: '' })
  const rules = { variety: { rules: [{ required: true, errorMessage: '请填写品种' }] } }

  watch(showPlot, v => {
    if (v) nextTick(() => plotPopup.value && plotPopup.value.open())
  })

  function loadPlots(selectId) {
    return plotOptions().then(res => {
      plots.value = res.data
      plotIndex.value = selectId ? plots.value.findIndex(p => p.plotId === selectId) : (plots.value.length === 1 ? 0 : -1)
    })
  }

  function savePlot() {
    if (!plot.value.plotName) {
      uni.showToast({ icon: 'none', title: '请填写地块名称' })
      return
    }
    const data = { ...plot.value, areaMu: plot.value.areaMu === '' ? undefined : Number(plot.value.areaMu) }
    addPlot(data).then(() => {
      showPlot.value = false
      plot.value = { plotName: '', address: '', areaMu: '' }
      return plotOptions()
    }).then(res => {
      plots.value = res.data
      plotIndex.value = 0
    })
  }

  async function submit() {
    try {
      await formRef.value.validate()
    } catch (e) {
      return
    }
    if (cover.value.local.length) {
      uni.showToast({ icon: 'none', title: '封面图未上传成功，请联网后重试' })
      return
    }
    saving.value = true
    const data = {
      ...form.value,
      plantDate: form.value.plantDate || undefined,
      harvestDate: form.value.harvestDate || undefined,
      plotId: plotIndex.value > -1 ? plots.value[plotIndex.value].plotId : undefined,
      coverImage: cover.value.uploaded[0] || ''
    }
    addBatch(data).then(res => {
      uni.showToast({ icon: 'success', title: '批次已创建' })
      setTimeout(() => uni.redirectTo({ url: '/pages/farmer/qr?id=' + res.data.batchId }), 600)
    }).finally(() => {
      saving.value = false
    })
  }

  onLoad(() => loadPlots())
</script>

<style lang="scss" scoped>
  @import '@/static/scss/wm-form.scss';
  .row { display: flex; align-items: center; }
  .flex1 { flex: 1; }
  .link { color: #2e7d32; font-size: 26rpx; margin-left: 16rpx; }
  .plot-dialog { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 32rpx; }
  .title { font-size: 32rpx; font-weight: bold; margin-bottom: 24rpx; }
  .btns { display: flex; justify-content: flex-end; margin-top: 24rpx; gap: 16rpx; }
</style>
