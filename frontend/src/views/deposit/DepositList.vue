<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">押金管理</h2>
      <el-button type="primary" :icon="Plus" @click="handlePay">缴纳押金</el-button>
    </div>

    <el-card shadow="hover" style="margin-bottom:20px;">
      <template #header><span style="font-weight:600;">押金余额汇总</span></template>
      <el-table :data="balances" stripe border>
        <el-table-column prop="shipperName" label="发货方" />
        <el-table-column prop="totalPaid" label="累计缴纳" align="right">
          <template #default="{row}"><span class="amount-positive">¥{{ fmt(row.totalPaid) }}</span></template>
        </el-table-column>
        <el-table-column prop="totalDeducted" label="累计扣款" align="right">
          <template #default="{row}"><span class="amount-negative">¥{{ fmt(row.totalDeducted) }}</span></template>
        </el-table-column>
        <el-table-column prop="totalRefunded" label="累计退还" align="right">
          <template #default="{row}">¥{{ fmt(row.totalRefunded) }}</template>
        </el-table-column>
        <el-table-column prop="currentBalance" label="当前余额" align="right">
          <template #default="{row}"><b>¥{{ fmt(row.currentBalance) }}</b></template>
        </el-table-column>
        <el-table-column prop="frozenAmount" label="冻结金额" align="right">
          <template #default="{row}"><el-tag type="warning" size="small">¥{{ fmt(row.frozenAmount) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="availableAmount" label="可用金额" align="right">
          <template #default="{row}"><el-tag type="success" size="small">¥{{ fmt(row.availableAmount) }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="发货方">
          <el-select v-model="query.shipperId" clearable placeholder="全部" style="width:200px;" @change="loadPayments">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPayments">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="hover">
      <template #header><span style="font-weight:600;">押金缴纳记录</span></template>
      <el-table :data="payments" stripe border>
        <el-table-column prop="paymentNo" label="单据编号" width="180" />
        <el-table-column prop="shipperName" label="发货方" />
        <el-table-column prop="paymentAmount" label="缴纳金额" align="right" width="130">
          <template #default="{row}"><span class="amount-positive">¥{{ fmt(row.paymentAmount) }}</span></template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="缴纳日期" width="120" />
        <el-table-column prop="paymentMethod" label="支付方式" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag size="small" type="success">已确认</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="缴纳押金" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="发货方" prop="shipperId">
          <el-select v-model="form.shipperId" placeholder="请选择发货方" filterable style="width:100%;">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="缴纳金额" prop="paymentAmount">
          <el-input-number v-model="form.paymentAmount" :min="0.01" :precision="2" :step="100" />
          <span style="margin-left:8px;color:#909399;">元</span>
        </el-form-item>
        <el-form-item label="缴纳日期" prop="paymentDate">
          <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="form.paymentMethod" placeholder="请选择" style="width:100%;">
            <el-option label="银行转账" value="银行转账" />
            <el-option label="现金" value="现金" />
            <el-option label="支票" value="支票" />
            <el-option label="其他" value="其他" />
          </el-select>
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
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listShippers } from '../../api/auth'
import { listDepositBalances, listDepositPayments, payDeposit } from '../../api/deposit'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const query = reactive({ shipperId: null })
const balances = ref([])
const payments = ref([])
const shippers = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ shipperId: null, paymentAmount: 1000, paymentDate: new Date().toISOString().slice(0,10), paymentMethod: '银行转账', remark: '' })
const rules = {
  shipperId: [{ required: true, message: '请选择发货方', trigger: 'change' }],
  paymentAmount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const loadData = async () => {
  shippers.value = await listShippers() || []
  balances.value = await listDepositBalances() || []
}
const loadPayments = async () => {
  payments.value = await listDepositPayments(query.shipperId) || []
}
const handlePay = () => {
  Object.assign(form, { shipperId: shippers.value[0]?.id || null, paymentAmount: 1000, paymentDate: new Date().toISOString().slice(0,10), paymentMethod: '银行转账', remark: '' })
  dialogVisible.value = true
}
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await payDeposit(form)
    ElMessage.success('押金缴纳成功')
    dialogVisible.value = false
    loadData()
    loadPayments()
  } finally { submitting.value = false }
}

onMounted(async () => { await loadData(); loadPayments() })
</script>
