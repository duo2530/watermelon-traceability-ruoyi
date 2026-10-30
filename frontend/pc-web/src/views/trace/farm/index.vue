<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="批次" prop="batchId">
        <el-select v-model="queryParams.batchId" placeholder="全部批次" clearable filterable style="width: 200px">
          <el-option v-for="b in batchOptions" :key="b.batchId" :label="b.batchCode + ' ' + b.variety" :value="b.batchId" />
        </el-select>
      </el-form-item>
      <el-form-item label="农事类型" prop="recordType">
        <el-select v-model="queryParams.recordType" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="dict in wm_farm_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="作业时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trace:farm:add']">录入农事</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trace:farm:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trace:farm:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="批次" align="center" prop="batchCode" width="150" />
      <el-table-column label="类型" align="center" prop="recordType" width="100">
        <template #default="scope">
          <dict-tag :options="wm_farm_type" :value="scope.row.recordType" />
        </template>
      </el-table-column>
      <el-table-column label="作业内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="投入品" align="center" width="160">
        <template #default="scope">{{ scope.row.inputName }} {{ scope.row.inputAmount }}</template>
      </el-table-column>
      <el-table-column label="作业时间" align="center" prop="recordTime" width="160" />
      <el-table-column label="天气" align="center" prop="weather" width="70" />
      <el-table-column label="图片" align="center" width="90">
        <template #default="scope">
          <image-preview v-if="scope.row.images" :src="scope.row.images" :width="50" :height="50" />
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operator" width="90" />
      <el-table-column label="来源" align="center" width="70">
        <template #default="scope">
          <el-tag v-if="scope.row.clientId" type="warning" size="small">离线</el-tag>
          <el-tag v-else size="small">在线</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trace:farm:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trace:farm:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="farmRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="批次" prop="batchId">
              <el-select v-model="form.batchId" placeholder="选择批次" filterable style="width: 100%">
                <el-option v-for="b in batchOptions" :key="b.batchId" :label="b.batchCode + ' ' + b.variety" :value="b.batchId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="农事类型" prop="recordType">
              <el-select v-model="form.recordType" style="width: 100%">
                <el-option v-for="dict in wm_farm_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业时间" prop="recordTime">
              <el-date-picker v-model="form.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="天气" prop="weather">
              <el-select v-model="form.weather" allow-create filterable clearable style="width: 100%">
                <el-option v-for="w in weathers" :key="w" :label="w" :value="w" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投入品" prop="inputName">
              <el-input v-model="form.inputName" placeholder="肥料/农药名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用量" prop="inputAmount">
              <el-input v-model="form.inputAmount" placeholder="如：50kg/亩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="作业内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="现场图片">
              <image-upload v-model="form.images" :limit="6" />
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

<script setup name="TraceFarm">
import { farmApi, batchApi } from "@/api/trace"

const { proxy } = getCurrentInstance()
const { wm_farm_type } = useDict("wm_farm_type")
const weathers = ["晴", "多云", "阴", "小雨", "中雨", "大雨"]

const list = ref([])
const batchOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, batchId: undefined, recordType: undefined },
  rules: {
    batchId: [{ required: true, message: "请选择批次", trigger: "change" }],
    recordType: [{ required: true, message: "请选择农事类型", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  farmApi.list(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = { recordId: undefined, batchId: queryParams.value.batchId, recordType: undefined, content: undefined, inputName: undefined, inputAmount: undefined, recordTime: proxy.parseTime(new Date()), weather: undefined, images: undefined }
  proxy.resetForm("farmRef")
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
  ids.value = selection.map(item => item.recordId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "录入农事记录"
}

function handleUpdate(row) {
  reset()
  farmApi.get(row.recordId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改农事记录"
  })
}

function submitForm() {
  proxy.$refs["farmRef"].validate(valid => {
    if (!valid) return
    const req = form.value.recordId != undefined ? farmApi.update(form.value) : farmApi.add(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const recordIds = row.recordId || ids.value
  proxy.$modal.confirm('是否确认删除农事记录"' + recordIds + '"？').then(() => farmApi.del(recordIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("trace/farm/export", { ...queryParams.value }, `farm_${new Date().getTime()}.xlsx`)
}

batchApi.options().then(res => { batchOptions.value = res.data })
getList()
</script>
