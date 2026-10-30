<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="批次" prop="batchId">
        <el-select v-model="queryParams.batchId" placeholder="全部批次" clearable filterable style="width: 200px">
          <el-option v-for="b in batchOptions" :key="b.batchId" :label="b.batchCode + ' ' + b.variety" :value="b.batchId" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级" prop="gradeCode">
        <el-select v-model="queryParams.gradeCode" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="g in gradeOptions" :key="g.gradeCode" :label="g.gradeName" :value="g.gradeCode" />
          <el-option label="等外" value="NONE" />
        </el-select>
      </el-form-item>
      <el-form-item label="果型" prop="fruitShape">
        <el-select v-model="queryParams.fruitShape" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="dict in wm_fruit_shape" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="检测时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trace:qc:add']">填报品控</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trace:qc:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trace:qc:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="批次" align="center" prop="batchCode" width="150" />
      <el-table-column label="品种" align="center" prop="variety" width="80" />
      <el-table-column label="样品号" align="center" prop="sampleNo" width="90" />
      <el-table-column label="中心糖" align="center" prop="sugarCenter" width="80" />
      <el-table-column label="边糖" align="center" prop="sugarEdge" width="70" />
      <el-table-column label="果型" align="center" prop="fruitShape" width="90">
        <template #default="scope">
          <dict-tag :options="wm_fruit_shape" :value="scope.row.fruitShape" />
        </template>
      </el-table-column>
      <el-table-column label="单果重(kg)" align="center" prop="weightKg" width="95" />
      <el-table-column label="等级" align="center" prop="gradeName" width="85">
        <template #default="scope">
          <el-tag :type="gradeTag(scope.row.gradeCode)">{{ scope.row.gradeName || '等外' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="检测时间" align="center" prop="checkTime" width="160" />
      <el-table-column label="检测人" align="center" prop="inspector" width="90" />
      <el-table-column label="图片" align="center" width="80">
        <template #default="scope">
          <image-preview v-if="scope.row.images" :src="scope.row.images" :width="40" :height="40" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trace:qc:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trace:qc:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="qcRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="批次" prop="batchId">
              <el-select v-model="form.batchId" placeholder="选择批次" filterable style="width: 100%">
                <el-option v-for="b in batchOptions" :key="b.batchId" :label="b.batchCode + ' ' + b.variety" :value="b.batchId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="样品编号" prop="sampleNo">
              <el-input v-model="form.sampleNo" placeholder="如：S01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心糖度" prop="sugarCenter">
              <el-input-number v-model="form.sugarCenter" :min="0" :max="30" :step="0.1" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="边糖" prop="sugarEdge">
              <el-input-number v-model="form.sugarEdge" :min="0" :max="30" :step="0.1" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="果型" prop="fruitShape">
              <el-radio-group v-model="form.fruitShape">
                <el-radio v-for="dict in wm_fruit_shape" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单果重(kg)" prop="weightKg">
              <el-input-number v-model="form.weightKg" :min="0" :max="50" :step="0.1" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预判等级">
              <el-tag :type="gradeTag(preGrade.gradeCode)" size="large">{{ preGrade.gradeName || '-' }}</el-tag>
              <span style="margin-left: 8px; color: #909399; font-size: 12px">按当前分级标准自动判定，保存时以服务端结果为准</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测时间" prop="checkTime">
              <el-date-picker v-model="form.checkTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图片">
              <image-upload v-model="form.images" :limit="6" />
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
  </div>
</template>

<script setup name="TraceQc">
import { qcApi, batchApi, gradeApi } from "@/api/trace"

const { proxy } = getCurrentInstance()
const { wm_fruit_shape } = useDict("wm_fruit_shape")

const list = ref([])
const batchOptions = ref([])
const gradeOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const preGrade = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, batchId: undefined, gradeCode: undefined, fruitShape: undefined },
  rules: {
    batchId: [{ required: true, message: "请选择批次", trigger: "change" }],
    sugarCenter: [{ required: true, message: "中心糖度不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function gradeTag(code) {
  return { SPECIAL: "danger", FIRST: "success", SECOND: "primary" }[code] || "info"
}

function getList() {
  loading.value = true
  qcApi.list(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

// 填表时实时预判等级
let timer = null
watch(() => [form.value.batchId, form.value.sugarCenter, form.value.sugarEdge, form.value.fruitShape, form.value.weightKg], () => {
  if (!open.value || form.value.sugarCenter == null) {
    preGrade.value = {}
    return
  }
  clearTimeout(timer)
  timer = setTimeout(() => {
    const p = { batchId: form.value.batchId, sugarCenter: form.value.sugarCenter, sugarEdge: form.value.sugarEdge, fruitShape: form.value.fruitShape, weightKg: form.value.weightKg }
    Object.keys(p).forEach(k => p[k] == null && delete p[k])
    qcApi.evaluate(p).then(res => { preGrade.value = res })
  }, 300)
})

function reset() {
  form.value = { qcId: undefined, batchId: queryParams.value.batchId, sampleNo: undefined, sugarCenter: undefined, sugarEdge: undefined, fruitShape: "1", weightKg: undefined, checkTime: proxy.parseTime(new Date()), images: undefined, remark: undefined }
  preGrade.value = {}
  proxy.resetForm("qcRef")
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
  ids.value = selection.map(item => item.qcId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "填报糖度 / 果型"
}

function handleUpdate(row) {
  reset()
  qcApi.get(row.qcId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改品控记录"
  })
}

function submitForm() {
  proxy.$refs["qcRef"].validate(valid => {
    if (!valid) return
    if (form.value.qcId != undefined) {
      qcApi.update(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      qcApi.add(form.value).then(res => {
        proxy.$modal.msgSuccess("保存成功，判定等级：" + res.data.gradeName)
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const qcIds = row.qcId || ids.value
  proxy.$modal.confirm('是否确认删除品控记录"' + qcIds + '"？').then(() => qcApi.del(qcIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("trace/qc/export", { ...queryParams.value }, `qc_${new Date().getTime()}.xlsx`)
}

batchApi.options().then(res => { batchOptions.value = res.data })
gradeApi.list({ pageNum: 1, pageSize: 100 }).then(res => { gradeOptions.value = res.rows })
getList()
</script>
