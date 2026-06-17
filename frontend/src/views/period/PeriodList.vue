<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">账期管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增账期</el-button>
    </div>
    <el-card shadow="hover">
      <el-table :data="list" stripe border>
        <el-table-column prop="periodCode" label="账期编码" width="130" />
        <el-table-column prop="periodName" label="账期名称" />
        <el-table-column prop="startDate" label="开始日期" width="130" />
        <el-table-column prop="endDate" label="结束日期" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status==='OPEN'" type="primary" size="small">开启中</el-tag>
            <el-tag v-else type="info" size="small">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="closeTime" label="关闭时间" width="170" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='OPEN'" type="warning" link size="small" @click="handleClose(row)">关账</el-button>
            <el-button v-if="row.status==='CLOSED'" type="success" link size="small" @click="handleReopen(row)">重开</el-button>
            <el-button v-if="row.status==='OPEN'" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增账期" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="账期编码" prop="periodCode">
          <el-input v-model="form.periodCode" placeholder="如：2026-07" />
        </el-form-item>
        <el-form-item label="账期名称" prop="periodName">
          <el-input v-model="form.periodName" placeholder="如：2026年7月" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width:100%;" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width:100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listAccountPeriods, createAccountPeriod,
  closePeriod, reopenPeriod, deletePeriod
} from '../../api/accountPeriod'

const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')

const list = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({
  periodCode: '', periodName: '',
  startDate: '', endDate: '', remark: ''
})
const rules = {
  periodCode: [{ required: true, message: '请输入账期编码', trigger: 'blur' }],
  periodName: [{ required: true, message: '请输入账期名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const loadData = async () => { list.value = await listAccountPeriods() || [] }

const handleAdd = () => {
  const now = new Date()
  Object.assign(form, {
    periodCode: `${now.getFullYear()}-${String(now.getMonth()+2).padStart(2,'0')}`,
    periodName: `${now.getFullYear()}年${now.getMonth()+2}月`,
    startDate: '', endDate: '', remark: ''
  })
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (form.startDate && form.endDate && new Date(form.startDate) > new Date(form.endDate)) {
    return ElMessage.warning('开始日期不能晚于结束日期')
  }
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createAccountPeriod(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const handleClose = (row) => {
  ElMessageBox.confirm(
    `确认关闭账期[${row.periodName}]吗？\n关闭后，该账期内的所有领用、归还、扣款记录都不能修改或删除。`,
    '关账确认', { type: 'warning', confirmButtonText: '确认关闭' }
  ).then(async () => {
    await closePeriod(row.id, currentUser.userId || 1)
    ElMessage.success('账期已关闭')
    loadData()
  }).catch(() => {})
}
const handleReopen = (row) => {
  ElMessageBox.confirm(`确认重开账期[${row.periodName}]吗？重开后该账期的记录将可以修改。`, '提示', { type: 'warning' })
    .then(async () => { await reopenPeriod(row.id); ElMessage.success('账期已重开'); loadData() })
    .catch(() => {})
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除账期[${row.periodName}]吗？`, '提示', { type: 'warning' })
    .then(async () => { await deletePeriod(row.id); ElMessage.success('删除成功'); loadData() })
    .catch(() => {})
}

onMounted(loadData)
</script>
