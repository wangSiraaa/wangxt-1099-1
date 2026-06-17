<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">托盘领用</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增领用</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="发货方">
          <el-select v-model="query.shipperId" clearable placeholder="全部" style="width:200px;" @change="loadData">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:150px;" @change="loadData">
            <el-option label="使用中" value="IN_USE" />
            <el-option label="部分归还" value="PARTIAL_RETURNED" />
            <el-option label="全部归还" value="FULLY_RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="hover">
      <el-table :data="list" stripe border>
        <el-table-column prop="pickupNo" label="领用单号" width="170" />
        <el-table-column prop="shipperName" label="发货方" />
        <el-table-column prop="palletCount" label="数量" width="80" align="center" />
        <el-table-column prop="totalDeposit" label="押金(元)" width="120" align="right">
          <template #default="{row}">¥{{ fmt(row.totalDeposit) }}</template>
        </el-table-column>
        <el-table-column prop="pickupDate" label="领用日期" width="120" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{row}">
            <el-tag v-if="row.status==='IN_USE'" type="warning" size="small">使用中</el-tag>
            <el-tag v-else-if="row.status==='PARTIAL_RETURNED'" size="small">部分归还</el-tag>
            <el-tag v-else type="success" size="small">全部归还</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="viewDetails(row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增领用托盘" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="发货方" prop="shipperId">
          <el-select v-model="form.shipperId" placeholder="请选择发货方" filterable style="width:100%;" @change="onShipperChange">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.shipperId" label="可用押金">
          <el-tag type="success">¥{{ fmt(shipperBalance.availableAmount) }}</el-tag>
          <el-tag v-if="shipperBalance.availableAmount < totalDepositAmount" type="danger" style="margin-left:8px;">
            押金不足！需 ¥{{ fmt(totalDepositAmount) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="领用日期" prop="pickupDate">
          <el-date-picker v-model="form.pickupDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="托盘明细" prop="details">
          <div style="width:100%;">
            <div style="margin-bottom:8px;">
              <el-button type="primary" size="small" :icon="Plus" :disabled="!availablePallets.length" @click="addDetail">添加托盘</el-button>
              <span style="margin-left:12px;color:#909399;">可领取托盘：{{ availablePallets.length }} 个</span>
              <span style="margin-left:20px;">合计押金：<b class="amount-positive">¥{{ fmt(totalDepositAmount) }}</b></span>
            </div>
            <el-table :data="form.details" border size="small" style="width:100%;">
              <el-table-column label="托盘" width="260">
                <template #default="{row,$index}">
                  <el-select v-model="row.palletId" placeholder="选择托盘" filterable style="width:100%;" @change="() => onPalletChange($index)">
                    <el-option v-for="p in getSelectablePallets($index)" :key="p.id" :label="p.palletCode + ' - ¥' + p.depositAmount" :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="押金(元)" align="right" width="120">
                <template #default="{row}">¥{{ fmt(getPalletDeposit(row.palletId)) }}</template>
              </el-table-column>
              <el-table-column label="备注">
                <template #default="{row}">
                  <el-input v-model="row.remark" placeholder="备注" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{$index}">
                  <el-button type="danger" link size="small" @click="removeDetail($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定领用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="领用明细" width="700px">
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="领用单号">{{ currentPickup.pickupNo }}</el-descriptions-item>
        <el-descriptions-item label="发货方">{{ currentPickup.shipperName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentPickup.palletCount }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ fmt(currentPickup.totalDeposit) }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ currentPickup.pickupDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ pickupStatusText(currentPickup.status) }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentDetails" border stripe size="small">
        <el-table-column prop="palletCode" label="托盘编码" />
        <el-table-column prop="depositAmount" label="押金(元)" align="right">
          <template #default="{row}">¥{{ fmt(row.depositAmount) }}</template>
        </el-table-column>
        <el-table-column prop="returnStatus" label="归还状态">
          <template #default="{row}">
            <el-tag v-if="row.returnStatus==='NOT_RETURNED'" type="warning" size="small">未归还</el-tag>
            <el-tag v-else-if="row.returnStatus==='NORMAL'" type="success" size="small">正常归还</el-tag>
            <el-tag v-else-if="row.returnStatus==='DAMAGED'" size="small">破损</el-tag>
            <el-tag v-else type="danger" size="small">丢失</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listShippers } from '../../api/auth'
import { listAvailablePallets } from '../../api/pallet'
import { getDepositBalance } from '../../api/deposit'
import { listPickups, getPickupDetails, createPickup } from '../../api/palletPickup'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')

const query = reactive({ shipperId: null, status: '' })
const list = ref([])
const shippers = ref([])
const availablePallets = ref([])
const shipperBalance = ref({ availableAmount: 0 })

const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const currentPickup = ref({})
const currentDetails = ref([])
const form = reactive({ shipperId: null, pickupDate: new Date().toISOString().slice(0,10), details: [], remark: '' })
const rules = {
  shipperId: [{ required: true, message: '请选择发货方', trigger: 'change' }],
  pickupDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const totalDepositAmount = computed(() =>
  form.details.reduce((sum, d) => sum + Number(getPalletDeposit(d.palletId) || 0), 0)
)
const pickupStatusText = (s) => ({IN_USE:'使用中',PARTIAL_RETURNED:'部分归还',FULLY_RETURNED:'全部归还'}[s] || s)

const resetQuery = () => { query.shipperId = null; query.status = ''; loadData() }
const loadData = async () => {
  let sid = query.shipperId
  if (currentUser.roleType === 'SHIPPER') sid = sid || currentUser.userId
  list.value = await listPickups(sid, query.status) || []
}
const onShipperChange = async () => {
  shipperBalance.value = (await getDepositBalance(form.shipperId)) || { availableAmount: 0 }
  availablePallets.value = await listAvailablePallets() || []
}
const getPalletDeposit = (id) => availablePallets.value.find(p => p.id === id)?.depositAmount || form.details.find(d => d.palletId === id)?.depositAmount || 0
const getSelectablePallets = (idx) => {
  const selectedIds = form.details.map((d, i) => i !== idx ? d.palletId : null).filter(Boolean)
  return availablePallets.value.filter(p => !selectedIds.includes(p.id))
}
const onPalletChange = (idx) => {
  const p = availablePallets.value.find(x => x.id === form.details[idx].palletId)
  if (p) { form.details[idx].palletCode = p.palletCode; form.details[idx].depositAmount = p.depositAmount }
}
const addDetail = () => form.details.push({ palletId: null, palletCode: '', remark: '', depositAmount: 0 })
const removeDetail = (idx) => form.details.splice(idx, 1)

const handleAdd = async () => {
  Object.assign(form, { shipperId: currentUser.roleType === 'SHIPPER' ? currentUser.userId : (shippers.value[0]?.id || null), pickupDate: new Date().toISOString().slice(0,10), details: [], remark: '' })
  if (form.details.length === 0) addDetail()
  if (form.shipperId) await onShipperChange()
  else availablePallets.value = await listAvailablePallets() || []
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!form.details.length) return ElMessage.warning('请添加托盘明细')
  if (form.details.some(d => !d.palletId)) return ElMessage.warning('请选择所有托盘')
  if (shipperBalance.value.availableAmount < totalDepositAmount.value) {
    return ElMessage.warning(`可用押金不足！需 ¥${fmt(totalDepositAmount.value)}，当前 ¥${fmt(shipperBalance.value.availableAmount)}`)
  }
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createPickup(form)
    ElMessage.success('领用成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const viewDetails = async (row) => {
  currentPickup.value = row
  currentDetails.value = await getPickupDetails(row.id) || []
  detailVisible.value = true
}

onMounted(async () => {
  shippers.value = await listShippers() || []
  if (currentUser.roleType === 'SHIPPER') query.shipperId = currentUser.userId
  loadData()
})
</script>
