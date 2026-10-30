<template>
  <div class="app-container">
    <el-form :inline="true">
      <el-form-item label="品种">
        <el-input v-model="query.variety" placeholder="全部" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="检测时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="load">统计</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12">
      <el-col :xs="12" :sm="8" :md="4" v-for="c in cards" :key="c.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ c.label }}</div>
          <div class="stat-value">{{ c.value }}<span class="stat-unit">{{ c.unit }}</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :md="12">
        <el-card shadow="never" header="等级分布"><div ref="gradeChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="12">
        <el-card shadow="never" header="中心糖度分布（°Brix）"><div ref="sugarChart" class="chart" /></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :md="12">
        <el-card shadow="never" header="月度平均糖度"><div ref="trendChart" class="chart" /></el-card>
      </el-col>
      <el-col :md="12">
        <el-card shadow="never" header="品种汇总">
          <el-table :data="stats.varietySummary || []" size="small" height="300">
            <el-table-column label="品种" prop="variety" />
            <el-table-column label="样本数" prop="total" width="80" />
            <el-table-column label="平均糖度" prop="avgSugar" width="90" />
            <el-table-column label="平均单果重" prop="avgWeight" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" header="批次品控汇总" style="margin-top: 12px">
      <el-table :data="stats.batchSummary || []" size="small">
        <el-table-column label="溯源码" prop="batchCode" />
        <el-table-column label="品种" prop="variety" />
        <el-table-column label="样本数" prop="total" />
        <el-table-column label="平均糖度" prop="avgSugar" />
        <el-table-column label="平均单果重" prop="avgWeight" />
        <el-table-column label="优质果(特等+一等)" prop="premiumCount" />
        <el-table-column label="优质率">
          <template #default="scope">{{ percent(scope.row.premiumCount, scope.row.total) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="TraceStats">
import * as echarts from "echarts"
import { getStats } from "@/api/trace"

const stats = ref({})
const query = ref({ variety: undefined })
const dateRange = ref([])
const gradeChart = ref(null)
const sugarChart = ref(null)
const trendChart = ref(null)
let charts = []

const cards = computed(() => {
  const o = stats.value.overview || {}
  return [
    { label: "种植批次", value: o.batchCount ?? 0, unit: "批" },
    { label: "农事记录", value: o.farmCount ?? 0, unit: "条" },
    { label: "品控样本", value: o.qcCount ?? 0, unit: "个" },
    { label: "平均中心糖", value: o.avgSugar ?? 0, unit: "°Bx" },
    { label: "优质率", value: percent(o.premiumCount, o.qcCount), unit: "" },
    { label: "累计扫码", value: o.scanCount ?? 0, unit: "次" }
  ]
})

function percent(a, b) {
  if (!b) return "0%"
  return ((Number(a || 0) / Number(b)) * 100).toFixed(1) + "%"
}

function load() {
  const params = { variety: query.value.variety }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  getStats(params).then(res => {
    stats.value = res.data
    nextTick(render)
  })
}

function render() {
  charts.forEach(c => c.dispose())
  const s = stats.value
  const g = echarts.init(gradeChart.value, "macarons")
  g.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { bottom: 0 },
    series: [{ type: "pie", radius: ["40%", "68%"], data: (s.gradeDistribution || []).map(d => ({ name: d.gradeName, value: d.total })) }]
  })
  const su = echarts.init(sugarChart.value, "macarons")
  su.setOption({
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: (s.sugarDistribution || []).map(d => d.bucket + "-" + (Number(d.bucket) + 1)) },
    yAxis: { type: "value", name: "样本数", minInterval: 1 },
    series: [{ type: "bar", data: (s.sugarDistribution || []).map(d => d.total), barMaxWidth: 40 }]
  })
  const t = echarts.init(trendChart.value, "macarons")
  t.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["中心糖", "边糖"] },
    xAxis: { type: "category", data: (s.monthlyTrend || []).map(d => d.month) },
    yAxis: { type: "value", name: "°Brix", scale: true },
    series: [
      { name: "中心糖", type: "line", smooth: true, data: (s.monthlyTrend || []).map(d => d.avgSugar) },
      { name: "边糖", type: "line", smooth: true, data: (s.monthlyTrend || []).map(d => d.avgEdge) }
    ]
  })
  charts = [g, su, t]
}

function resize() {
  charts.forEach(c => c.resize())
}

onMounted(() => {
  load()
  window.addEventListener("resize", resize)
})
onBeforeUnmount(() => {
  window.removeEventListener("resize", resize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.stat-card { margin-bottom: 12px; }
.stat-label { color: #909399; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: bold; margin-top: 6px; }
.stat-unit { font-size: 13px; font-weight: normal; margin-left: 4px; color: #909399; }
.chart { height: 300px; }
</style>
