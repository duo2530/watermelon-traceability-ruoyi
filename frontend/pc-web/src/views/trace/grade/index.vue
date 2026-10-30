<template>
  <div class="app-container">
    <el-alert type="info" :closable="false" style="margin-bottom: 12px"
      title="判定规则：按“优先级”从小到大依次匹配，第一个满足全部条件的启用标准即为该果的等级；指定品种的标准优先于通用标准；都不满足判为“等外”。空条件表示不限制。" />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trace:grade:add']">新增标准</el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="优先级" align="center" prop="sort" width="80" />
      <el-table-column label="等级编码" align="center" prop="gradeCode" width="110" />
      <el-table-column label="等级名称" align="center" prop="gradeName" width="100" />
      <el-table-column label="适用品种" align="center" prop="variety" width="100">
        <template #default="scope">{{ scope.row.variety || '全部' }}</template>
      </el-table-column>
      <el-table-column label="中心糖 ≥" align="center" prop="sugarCenterMin" width="90" />
      <el-table-column label="边糖 ≥" align="center" prop="sugarEdgeMin" width="80" />
      <el-table-column label="单果重(kg)" align="center" width="120">
        <template #default="scope">{{ range(scope.row.weightMin, scope.row.weightMax) }}</template>
      </el-table-column>
      <el-table-column label="允许果型" align="center" width="160">
        <template #default="scope">{{ shapeLabels(scope.row.shapeAllow) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="说明" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trace:grade:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trace:grade:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="gradeRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="等级编码" prop="gradeCode">
              <el-input v-model="form.gradeCode" placeholder="如：SPECIAL" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级名称" prop="gradeName">
              <el-input v-model="form.gradeName" placeholder="如：特等果" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用品种" prop="variety">
              <el-input v-model="form.variety" placeholder="留空=全部品种" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="sort">
              <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心糖下限" prop="sugarCenterMin">
              <el-input-number v-model="form.sugarCenterMin" :min="0" :max="30" :step="0.5" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="边糖下限" prop="sugarEdgeMin">
              <el-input-number v-model="form.sugarEdgeMin" :min="0" :max="30" :step="0.5" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单果重下限" prop="weightMin">
              <el-input-number v-model="form.weightMin" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单果重上限" prop="weightMax">
              <el-input-number v-model="form.weightMax" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="允许果型">
              <el-checkbox-group v-model="shapes">
                <el-checkbox v-for="dict in wm_fruit_shape" :key="dict.value" :value="dict.value">{{ dict.label }}</el-checkbox>
              </el-checkbox-group>
              <span style="margin-left: 8px; color: #909399; font-size: 12px">不勾选=不限</span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="说明" prop="remark">
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

<script setup name="TraceGrade">
import { gradeApi } from "@/api/trace"

const { proxy } = getCurrentInstance()
const { sys_normal_disable, wm_fruit_shape } = useDict("sys_normal_disable", "wm_fruit_shape")

const list = ref([])
const open = ref(false)
const loading = ref(true)
const title = ref("")
const shapes = ref([])

const data = reactive({
  form: {},
  rules: {
    gradeCode: [{ required: true, message: "等级编码不能为空", trigger: "blur" }],
    gradeName: [{ required: true, message: "等级名称不能为空", trigger: "blur" }]
  }
})
const { form, rules } = toRefs(data)

function range(min, max) {
  if (min == null && max == null) return "不限"
  return (min ?? "") + " ~ " + (max ?? "")
}

function shapeLabels(v) {
  if (!v) return "不限"
  return v.split(",").map(s => (wm_fruit_shape.value.find(d => d.value === s) || {}).label || s).join("、")
}

function getList() {
  loading.value = true
  gradeApi.list({ pageNum: 1, pageSize: 100 }).then(res => {
    list.value = res.rows
    loading.value = false
  })
}

function reset() {
  form.value = { standardId: undefined, gradeCode: undefined, gradeName: undefined, variety: "", sugarCenterMin: undefined, sugarEdgeMin: undefined, weightMin: undefined, weightMax: undefined, shapeAllow: "", sort: list.value.length + 1, status: "0", remark: undefined }
  shapes.value = []
  proxy.resetForm("gradeRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增分级标准"
}

function handleUpdate(row) {
  reset()
  gradeApi.get(row.standardId).then(res => {
    form.value = res.data
    shapes.value = res.data.shapeAllow ? res.data.shapeAllow.split(",") : []
    open.value = true
    title.value = "修改分级标准"
  })
}

function submitForm() {
  proxy.$refs["gradeRef"].validate(valid => {
    if (!valid) return
    form.value.shapeAllow = shapes.value.join(",")
    const req = form.value.standardId != undefined ? gradeApi.update(form.value) : gradeApi.add(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess("保存成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除分级标准"' + row.gradeName + '"？').then(() => gradeApi.del(row.standardId)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
