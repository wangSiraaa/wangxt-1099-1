<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">扣款管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">人工扣款</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="发货方">
          <el-select v-model="query.shipperId" clearable placeholder="全部" style="width:200px;" @change="loadData">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.deductionType" clearable placeholder="全部" style="width:150px;" @change="loadData">
            <el-option label="破损扣款" value="DAMAGED" />
            <el-option label="丢失扣款" value="LOST" />
            <el-option label="其他扣款" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="hover">
      <el-table :data="list" stripe border :summary-method="getSummaries" show-summary>
        <el-table-column prop="deductionNo" label="扣款单号" width="180" />
        <el-table-column prop="shipperName" label="发货方" />
        <el-table-column prop="deductionType" label="类型" width="110">
          <template #default="{row}">
            <el-tag v-if="row.deductionType==='DAMAGED'" type="warning" size="small">破损</el-tag>
            <el-tag v-else-if="row.deductionType==='LOST'" type="danger" size="small">丢失</el-tag>
            <el-tag v-else size="small">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="palletCount" label="托盘数" width="80" align="center" />
        <el-table-column prop="deductionAmount" label="扣款金额(元)" width="140" align="right">
          <template #default="{row}"><span class="amount-negative">¥{{ fmt(row.deductionAmount) }}</span></template>
        </el-table-column>
        <el-table-column prop="deductionDate" label="扣款日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag type="success" size="small">已确认</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100" v-if="isFinance">
          <template #default="{row}">
            <el-button v-if="!row.returnId" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="人工扣款" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="发货方" prop="shipperId">
          <el-select v-model="form.shipperId" placeholder="选择发货方" filterable style="width:100%;" @change="onShipperChange">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="扣款类型" prop="deductionType">
          <el-select v-model="form.deductionType" placeholder="选择类型" style="width:100%;">
            <el-option label="破损扣款" value="DAMAGED" />
            <el-option label="丢失扣款" value="LOST" />
            <el-option label="其他扣款" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="扣款金额" prop="deductionAmount">
          <el-input-number v-model="form.deductionAmount" :min="0.01" :precision="2" :step="10" />
          <span style="margin-left:8px;color:#909399;">元</span>
          <el-tag v-if="form.shipperId && form.deductionAmount > balance.availableAmount" type="danger" style="margin-left:8px;">
            余额不足！当前 ¥{{ fmt(balance.availableAmount) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="扣款日期" prop="deductionDate">
          <el-date-picker v-model="form.deductionDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入扣款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认扣款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listShippers } from '../../api/auth'
import { listDeductions, createManualDeduction, deleteDeduction } from '../../api/deduction'
import { getDepositBalance } from '../../api/deposit'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')
const isFinance = computed(() => currentUser.roleType === 'FINANCE')

const query = reactive({ shipperId: null, deductionType: '' })
const list = ref([])
const shippers = ref([])
const balance = ref({ availableAmount: 0 })

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({
  shipperId: null, shipperName: '', deductionType: 'OTHER',
  deductionAmount: 100, deductionDate: new Date().toISOString().slice(0,10), remark: ''
})
const rules = {
  shipperId: [{ required: true, message: '请选择发货方', trigger: 'change' }],
  deductionType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  deductionAmount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  deductionDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  remark: [{ required: true, message: '请输入扣款原因', trigger: 'blur' }]
}

const getSummaries = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) { sums[index] = '合计'; return }
    if (column.label === '扣款金额(元)') {
      const total = data.reduce((sum, row) => sum + Number(row.deductionAmount || 0), 0)
      sums[index] = `¥${fmt(total)}`
      return
    }
    sums[index] = ''
  })
  return sums
}

const resetQuery = () => { query.shipperId = null; query.deductionType = ''; loadData() }
const loadData = async () => { list.value = await listDeductions(query.shipperId, query.deductionType) || [] }
const onShipperChange = async () => {
  const shipper = shippers.value.find(s => s.id === form.shipperId)
  form.shipperName = shipper?.userName || ''
  balance.value = (await getDepositBalance(form.shipperId)) || { availableAmount: 0 }
}
const handleAdd = () => {
  Object.assign(form, {
    shipperId: shippers.value[0]?.id || null,
    shipperName: shippers.value[0]?.userName || '',
    deductionType: 'OTHER', deductionAmount: 100,
    deductionDate: new Date().toISOString().slice(0,10), remark: ''
  })
  if (form.shipperId) onShipperChange()
  dialogVisible.value = true
}
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createManualDeduction({
      shipperId: form.shipperId, shipperName: form.shipperName,
      deductionType: form.deductionType, deductionAmount: form.deductionAmount,
      deductionDate: form.deductionDate, remark: form.remark
    })
    ElMessage.success('扣款成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除扣款记录[${row.deductionNo}]吗？`, '提示', { type: 'warning' })
    .then(async () => { await deleteDeduction(row.id); ElMessage.success('删除成功'); loadData() })
    .catch(() => {})
}

onMounted(async () => { shippers.value = await listShippers() || []; loadData() })
</script>
