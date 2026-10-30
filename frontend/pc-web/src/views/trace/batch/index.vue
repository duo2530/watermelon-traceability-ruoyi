<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="溯源码" prop="batchCode">
        <el-input v-model="queryParams.batchCode" placeholder="WM2026..." clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="品种" prop="variety">
        <el-input v-model="queryParams.variety" placeholder="如：8424" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="品牌" prop="brandName">
        <el-input v-model="queryParams.brandName" placeholder="品牌名称" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option v-for="dict in wm_batch_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="定植日期">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trace:batch:add']">新增批次</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Box" :disabled="multiple" @click="handleArchive" v-hasPermi="['trace:batch:archive']">归档</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trace:batch:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trace:batch:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="溯源码" align="center" prop="batchCode" width="150">
        <template #default="scope">
          <a class="link-type" style="cursor:pointer" @click="handlePreview(scope.row)">{{ scope.row.batchCode }}</a>
        </template>
      </el-table-column>
      <el-table-column label="品种" align="center" prop="variety" width="90" />
      <el-table-column label="地块" align="center" prop="plotName" :show-overflow-tooltip="true" />
      <el-table-column label="农户" align="center" prop="farmerName" width="90" />
      <el-table-column label="品牌" align="center" prop="brandName" :show-overflow-tooltip="true" />
      <el-table-column label="定植" align="center" prop="plantDate" width="105" />
      <el-table-column label="采收" align="center" prop="harvestDate" width="105" />
      <el-table-column label="扫码" align="center" prop="scanCount" width="65" />
      <el-table-column label="状态" align="center" prop="status" width="85">
        <template #default="scope">
          <dict-tag :options="wm_batch_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Picture" @click="handleQr(scope.row)" v-hasPermi="['trace:batch:qrcode']">二维码</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trace:batch:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trace:batch:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="batchRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="地块" prop="plotId">
              <el-select v-model="form.plotId" placeholder="选择地块" clearable filterable style="width: 100%">
                <el-option v-for="p in plotOptions" :key="p.plotId" :label="p.plotName" :value="p.plotId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="variety">
              <el-input v-model="form.variety" placeholder="如：8424、麒麟" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定植日期" prop="plantDate">
              <el-date-picker v-model="form.plantDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采收日期" prop="harvestDate">
              <el-date-picker v-model="form.harvestDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计产量" prop="expectedYieldKg">
              <el-input-number v-model="form.expectedYieldKg" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" v-if="form.batchId">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="dict in wm_batch_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌名称" prop="brandName">
              <el-input v-model="form.brandName" placeholder="如：东台西瓜" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌LOGO">
              <image-upload v-model="form.brandLogo" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="品牌介绍" prop="brandDesc">
              <el-input v-model="form.brandDesc" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图">
              <image-upload v-model="form.coverImage" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 二维码 -->
    <el-dialog title="批次溯源二维码" v-model="qrOpen" width="420px" append-to-body>
      <div style="text-align: center">
        <img v-if="qr.qrPath" :src="baseApi + qr.qrPath + '?t=' + qrStamp" style="width: 260px; height: 260px" />
        <div style="margin-top: 8px; font-size: 16px; font-weight: bold">{{ qr.batchCode }}</div>
        <div style="margin-top: 6px; color: #909399; word-break: break-all; font-size: 12px">{{ qr.qrContent }}</div>
      </div>
      <template #footer>
        <el-button type="primary" icon="Download" @click="downloadQr">下载图片</el-button>
        <el-button icon="Refresh" @click="regenerateQr">重新生成</el-button>
      </template>
    </el-dialog>

    <!-- 消费者视角预览 -->
    <el-drawer v-model="previewOpen" title="消费者扫码预览" size="480px">
      <div v-if="preview">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="溯源码">{{ preview.batch.batchCode }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ preview.batch.variety }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ preview.brand.name }}</el-descriptions-item>
          <el-descriptions-item label="产地">{{ preview.plot ? preview.plot.address : '-' }}</el-descriptions-item>
          <el-descriptions-item label="平均糖度">{{ preview.quality.avgSugar || '-' }} °Brix</el-descriptions-item>
          <el-descriptions-item label="主等级">{{ preview.quality.mainGrade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="扫码次数">{{ preview.batch.scanCount }}</el-descriptions-item>
        </el-descriptions>
        <el-timeline style="margin-top: 16px">
          <el-timeline-item v-for="(t, i) in preview.timeline" :key="i" :timestamp="t.recordTime">
            <b>{{ t.recordTypeLabel }}</b> {{ t.content }}
            <span v-if="t.inputName">（{{ t.inputName }} {{ t.inputAmount }}）</span>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="TraceBatch">
import { batchApi, plotApi, getPublicTrace } from "@/api/trace"

const { proxy } = getCurrentInstance()
const { wm_batch_status } = useDict("wm_batch_status")
const baseApi = import.meta.env.VITE_APP_BASE_API

const list = ref([])
const plotOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const qrOpen = ref(false)
const qr = ref({})
const qrStamp = ref(0)
const previewOpen = ref(false)
const preview = ref(null)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, batchCode: undefined, variety: undefined, brandName: undefined, status: undefined },
  rules: {
    variety: [{ required: true, message: "品种不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  batchApi.list(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function loadPlots() {
  plotApi.options().then(res => { plotOptions.value = res.data })
}

function reset() {
  form.value = { batchId: undefined, plotId: undefined, variety: undefined, plantDate: undefined, harvestDate: undefined, expectedYieldKg: undefined, brandName: undefined, brandLogo: undefined, brandDesc: undefined, coverImage: undefined, status: "0", remark: undefined }
  proxy.resetForm("batchRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.batchId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  loadPlots()
  open.value = true
  title.value = "新增批次（保存后自动生成溯源码和二维码）"
}

function handleUpdate(row) {
  reset()
  loadPlots()
  batchApi.get(row.batchId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改批次 " + res.data.batchCode
  })
}

function submitForm() {
  proxy.$refs["batchRef"].validate(valid => {
    if (!valid) return
    if (form.value.batchId != undefined) {
      batchApi.update(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      batchApi.add(form.value).then(res => {
        proxy.$modal.msgSuccess("新增成功，溯源码 " + res.data.batchCode)
        open.value = false
        getList()
        handleQr(res.data)
      })
    }
  })
}

function handleDelete(row) {
  const batchIds = row.batchId || ids.value
  proxy.$modal.confirm('是否确认删除批次编号为"' + batchIds + '"的数据项？').then(() => batchApi.del(batchIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleArchive() {
  proxy.$modal.confirm("归档后农户将不能再录入或修改该批次数据，是否继续？").then(() => batchApi.archive(ids.value)).then(() => {
    getList()
    proxy.$modal.msgSuccess("归档成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("trace/batch/export", { ...queryParams.value }, `batch_${new Date().getTime()}.xlsx`)
}

function handleQr(row) {
  batchApi.get(row.batchId).then(res => {
    if (!res.data.qrPath) {
      return regenerateQr(row.batchId)
    }
    qr.value = { ...res.data, qrContent: res.qrContent }
    qrStamp.value = Date.now()
    qrOpen.value = true
  })
}

function regenerateQr(batchId) {
  const id = typeof batchId === "number" ? batchId : qr.value.batchId
  return batchApi.qrcode(id).then(res => {
    qr.value = { ...res.data, qrContent: res.qrContent }
    qrStamp.value = Date.now()
    qrOpen.value = true
  })
}

function downloadQr() {
  const a = document.createElement("a")
  a.href = baseApi + qr.value.qrPath
  a.download = qr.value.batchCode + ".png"
  a.click()
}

function handlePreview(row) {
  getPublicTrace(row.batchCode).then(res => {
    preview.value = res.data
    previewOpen.value = true
  })
}

getList()
</script>
