<template>
  <view class="page">
    <view class="offline-tip" v-if="!online">当前无网络，记录将保存在本机，联网后自动同步并判定等级</view>
    <uni-forms ref="formRef" :model="form" :rules="rules" label-width="90px" class="card">
      <uni-forms-item label="批次" name="batchId" required>
        <picker :range="batches" range-key="label" :value="batchIndex" @change="onBatch">
          <view class="picker">{{ batchIndex > -1 ? batches[batchIndex].label : '请选择批次' }}</view>
        </picker>
      </uni-forms-item>
      <uni-forms-item label="样品编号" name="sampleNo">
        <uni-easyinput v-model="form.sampleNo" placeholder="如：S01（可选）" />
      </uni-forms-item>
      <uni-forms-item label="中心糖度" name="sugarCenter" required>
        <uni-easyinput type="digit" v-model="form.sugarCenter" placeholder="°Brix，如 12.5" @blur="evaluate" />
      </uni-forms-item>
      <uni-forms-item label="边糖" name="sugarEdge">
        <uni-easyinput type="digit" v-model="form.sugarEdge" placeholder="°Brix（可选）" @blur="evaluate" />
      </uni-forms-item>
      <uni-forms-item label="果型" name="fruitShape">
        <view class="chips">
          <view v-for="s in shapes" :key="s.value" :class="['chip', form.fruitShape === s.value ? 'on' : '']" @click="form.fruitShape = s.value; evaluate()">{{ s.label }}</view>
        </view>
      </uni-forms-item>
      <uni-forms-item label="单果重" name="weightKg">
        <uni-easyinput type="digit" v-model="form.weightKg" placeholder="kg，如 6.2" @blur="evaluate" />
      </uni-forms-item>
      <uni-forms-item label="预判等级">
        <view class="grade" :class="grade.gradeCode">{{ grade.gradeName || (online ? '填写糖度后自动判定' : '离线，同步后判定') }}</view>
      </uni-forms-item>
      <uni-forms-item label="检测时间" name="checkTime">
        <uni-datetime-picker type="datetime" v-model="form.checkTime" :clear-icon="false" />
      </uni-forms-item>
      <uni-forms-item label="图片">
        <wm-images v-model="images" />
      </uni-forms-item>
    </uni-forms>
    <button class="submit" type="primary" :loading="saving" @click="submit">保存品控记录</button>
  </view>
</template>

<script setup>
  import { ref } from 'vue'
  import { onLoad } from '@dcloudio/uni-app'
  import { evaluateGrade } from '@/api/trace'
  import { submitRecord } from '@/utils/offlineQueue'
  import { useBatchPicker, formatNow } from '@/utils/batchPicker'

  // 字典 wm_fruit_shape 的离线兜底副本
  const shapes = [{ value: '1', label: '端正' }, { value: '2', label: '基本端正' }, { value: '3', label: '畸形' }]

  const formRef = ref(null)
  const saving = ref(false)
  const grade = ref({})
  const images = ref({ uploaded: [], local: [] })
  const form = ref(emptyForm())
  const { batches, batchIndex, online, loadBatches, onBatch } = useBatchPicker(form)

  // 输入框给的是字符串，uni-forms 的 format:'number' 会判失败，这里自定义区间校验；空值交给 required 处理
  function rangeRule(min, max, msg) {
    return {
      validateFunction: (rule, value, data, callback) => {
        if (value === '' || value === null || value === undefined) return true
        const n = Number(value)
        if (isNaN(n) || n < min || n > max) {
          callback(msg)
          return false
        }
        return true
      }
    }
  }
  const sugarRule = rangeRule(0, 30, '糖度应在 0-30 之间')
  const rules = {
    batchId: { rules: [{ required: true, errorMessage: '请选择批次' }] },
    sugarCenter: { rules: [{ required: true, errorMessage: '请填写中心糖度' }, sugarRule] },
    sugarEdge: { rules: [sugarRule] },
    weightKg: { rules: [rangeRule(0, 50, '单果重应在 0-50kg 之间')] }
  }

  function emptyForm() {
    return { batchId: null, sampleNo: '', sugarCenter: '', sugarEdge: '', fruitShape: '1', weightKg: '', checkTime: formatNow() }
  }

  function num(v) {
    return v === '' || v === null || v === undefined ? undefined : Number(v)
  }

  function payload() {
    const f = form.value
    return { ...f, sugarCenter: num(f.sugarCenter), sugarEdge: num(f.sugarEdge), weightKg: num(f.weightKg) }
  }

  function evaluate() {
    const p = payload()
    if (!online.value || p.sugarCenter === undefined || isNaN(p.sugarCenter)) {
      grade.value = {}
      return
    }
    const q = { batchId: p.batchId, sugarCenter: p.sugarCenter, sugarEdge: p.sugarEdge, fruitShape: p.fruitShape, weightKg: p.weightKg }
    Object.keys(q).forEach(k => (q[k] === undefined || q[k] === null) && delete q[k])
    evaluateGrade(q).then(res => { grade.value = res }).catch(() => {})
  }

  onLoad(opt => loadBatches(opt && opt.batchId))

  async function submit() {
    try {
      await formRef.value.validate()
    } catch (e) {
      const first = Array.isArray(e) && e.length ? e[0].errorMessage : (e && e.message)
      console.warn('qc validate failed', e)
      uni.showToast({ icon: 'none', title: first || '请检查表单' })
      return
    }
    saving.value = true
    try {
      const r = await submitRecord('qc', payload(), images.value)
      if (r.status === 'rejected') {
        uni.showModal({ title: '提交失败', content: r.message, showCancel: false })
        return
      }
      uni.showToast({ icon: r.status === 'synced' ? 'success' : 'none', title: r.status === 'synced' ? '已保存' : '已离线保存，联网后同步' })
      const keep = form.value.batchId
      form.value = { ...emptyForm(), batchId: keep }
      images.value = { uploaded: [], local: [] }
      grade.value = {}
    } finally {
      saving.value = false
    }
  }
</script>

<style lang="scss" scoped>
  @import '@/static/scss/wm-form.scss';
  .grade { font-size: 30rpx; font-weight: bold; color: #999; line-height: 72rpx; }
  .grade.SPECIAL { color: #e53935; }
  .grade.FIRST { color: #2e7d32; }
  .grade.SECOND { color: #1e88e5; }
</style>
