<template>
  <view class="page">
    <view class="offline-tip" v-if="!online">当前无网络，记录将保存在本机，联网后自动同步</view>
    <uni-forms ref="formRef" :model="form" :rules="rules" label-width="90px" class="card">
      <uni-forms-item label="批次" name="batchId" required>
        <picker :range="batches" range-key="label" :value="batchIndex" @change="onBatch">
          <view class="picker">{{ batchIndex > -1 ? batches[batchIndex].label : '请选择批次' }}</view>
        </picker>
      </uni-forms-item>
      <uni-forms-item label="农事类型" name="recordType" required>
        <view class="chips">
          <view v-for="t in types" :key="t.value" :class="['chip', form.recordType === t.value ? 'on' : '']" @click="form.recordType = t.value">{{ t.label }}</view>
        </view>
      </uni-forms-item>
      <uni-forms-item label="作业时间" name="recordTime">
        <uni-datetime-picker type="datetime" v-model="form.recordTime" :clear-icon="false" />
      </uni-forms-item>
      <uni-forms-item label="天气" name="weather">
        <view class="chips">
          <view v-for="w in weathers" :key="w" :class="['chip', form.weather === w ? 'on' : '']" @click="form.weather = w">{{ w }}</view>
        </view>
      </uni-forms-item>
      <uni-forms-item label="投入品" name="inputName">
        <uni-easyinput v-model="form.inputName" placeholder="肥料/农药名称（可选）" />
      </uni-forms-item>
      <uni-forms-item label="用量" name="inputAmount">
        <uni-easyinput v-model="form.inputAmount" placeholder="如：50kg/亩" />
      </uni-forms-item>
      <uni-forms-item label="作业内容" name="content">
        <uni-easyinput type="textarea" v-model="form.content" placeholder="描述本次作业" maxlength="1000" />
      </uni-forms-item>
      <uni-forms-item label="现场图片">
        <wm-images v-model="images" />
      </uni-forms-item>
    </uni-forms>
    <button class="submit" type="primary" :loading="saving" @click="submit">保存记录</button>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import { submitRecord } from '@/utils/offlineQueue'
  import { useBatchPicker, formatNow } from '@/utils/batchPicker'

  // 字典 wm_farm_type 的离线兜底副本（离线时取不到字典接口）
  const types = [
    { value: 'TILL', label: '整地' }, { value: 'PLANT', label: '育苗定植' }, { value: 'FERTILIZE', label: '施肥' },
    { value: 'IRRIGATE', label: '灌溉' }, { value: 'PESTICIDE', label: '植保用药' }, { value: 'POLLINATE', label: '授粉' },
    { value: 'PRUNE', label: '整枝压蔓' }, { value: 'HARVEST', label: '采收' }, { value: 'OTHER', label: '其他' }
  ]
  const weathers = ['晴', '多云', '阴', '小雨', '大雨']

  const formRef = ref(null)
  const saving = ref(false)
  const images = ref({ uploaded: [], local: [] })
  const form = ref(emptyForm())
  const { batches, batchIndex, online, loadBatches, onBatch } = useBatchPicker(form)

  const rules = {
    batchId: { rules: [{ required: true, errorMessage: '请选择批次' }] },
    recordType: { rules: [{ required: true, errorMessage: '请选择农事类型' }] }
  }

  function emptyForm() {
    return { batchId: null, recordType: 'FERTILIZE', recordTime: formatNow(), weather: '晴', inputName: '', inputAmount: '', content: '' }
  }

  onLoad(opt => loadBatches(opt && opt.batchId))

  async function submit() {
    try {
      await formRef.value.validate()
    } catch (e) {
      return
    }
    saving.value = true
    try {
      const r = await submitRecord('farm', form.value, images.value)
      if (r.status === 'rejected') {
        uni.showModal({ title: '提交失败', content: r.message, showCancel: false })
        return
      }
      uni.showToast({ icon: r.status === 'synced' ? 'success' : 'none', title: r.status === 'synced' ? '已保存' : '已离线保存，联网后同步' })
      const keep = form.value.batchId
      form.value = { ...emptyForm(), batchId: keep }
      images.value = { uploaded: [], local: [] }
    } finally {
      saving.value = false
    }
  }
</script>

<style lang="scss" scoped>
  @import '@/static/scss/wm-form.scss';
</style>
