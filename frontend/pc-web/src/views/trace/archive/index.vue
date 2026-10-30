<template>
  <div class="app-container">
    <el-card shadow="never" header="数据导出归档">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px"
        title="导出为一个 Excel 文件，包含“种植批次 / 农事记录 / 品控记录”三个工作表，可作为季末归档材料。归档批次后，农户不能再录入或修改该批次数据。" />
      <el-form :model="form" label-width="100px" style="max-width: 640px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="mode">
            <el-radio value="all">按条件导出</el-radio>
            <el-radio value="one">单个批次</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="mode === 'all'">
          <el-form-item label="批次状态">
            <el-select v-model="form.status" placeholder="全部" clearable style="width: 200px">
              <el-option v-for="dict in wm_batch_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="定植日期">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" />
          </el-form-item>
        </template>
        <el-form-item label="批次" v-else>
          <el-select v-model="form.batchId" filterable placeholder="选择批次" style="width: 320px">
            <el-option v-for="b in batchOptions" :key="b.batchId" :label="b.batchCode + ' ' + b.variety" :value="b.batchId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['trace:archive:export']">导出 Excel</el-button>
          <el-button type="warning" icon="Box" :disabled="mode !== 'one' || !form.batchId" @click="handleArchive" v-hasPermi="['trace:batch:archive']">归档该批次</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="TraceArchive">
import { batchApi } from "@/api/trace"

const { proxy } = getCurrentInstance()
const { wm_batch_status } = useDict("wm_batch_status")

const mode = ref("all")
const form = ref({ status: undefined, batchId: undefined })
const dateRange = ref([])
const batchOptions = ref([])

function handleExport() {
  let params = {}
  if (mode.value === "one") {
    if (!form.value.batchId) {
      proxy.$modal.msgWarning("请选择批次")
      return
    }
    params.batchId = form.value.batchId
  } else {
    params.status = form.value.status
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
  }
  proxy.download("trace/archive/export", params, `trace_archive_${new Date().getTime()}.xlsx`)
}

function handleArchive() {
  proxy.$modal.confirm("归档后农户将不能再录入或修改该批次数据，是否继续？").then(() => batchApi.archive(form.value.batchId)).then(() => {
    proxy.$modal.msgSuccess("归档成功")
    loadBatches()
  }).catch(() => {})
}

function loadBatches() {
  batchApi.list({ pageNum: 1, pageSize: 500 }).then(res => { batchOptions.value = res.rows })
}

loadBatches()
</script>
