<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">托盘转运</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增转运</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="转出承运商">
          <el-select v-model="query.fromCarrierId" clearable placeholder="全部" style="width:180px;" @change="loadData">
            <el-option v-for="c in carriers" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入承运商">
          <el-select v-model="query.toCarrierId" clearable placeholder="全部" style="width:180px;" @change="loadData">
            <el-option v-for="c in carriers" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:150px;" @change="loadData">
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已取消" value="CANCELLED" />
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
        <el-table-column prop="transferNo" label="转运单号" width="170" />
        <el-table-column prop="fromCarrierName" label="转出承运商" />
        <el-table-column prop="toCarrierName" label="转入承运商" />
        <el-table-column prop="depositBearerName" label="押金承担方" />
        <el-table-column prop="palletCount" label="托盘数量" width="100" align="center" />
        <el-table-column prop="totalDeposit" label="押金(元)" width="120" align="right">
          <template #default="{row}">¥{{ fmt(row.totalDeposit) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status==='CONFIRMED'" type="success" size="small">已确认</el-tag>
            <el-tag v-else type="info" size="small">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="viewDetails(row)">明细</el-button>
            <el-button 
              type="danger" 
              link 
              size="small" 
              @click="handleCancel(row)"
              :disabled="row.status !== 'CONFIRMED'">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增转运" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="转出承运商" prop="fromCarrierId">
          <el-select v-model="form.fromCarrierId" placeholder="请选择转出承运商" filterable style="width:100%;" @change="onFromCarrierChange">
            <el-option v-for="c in carriers" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入承运商" prop="toCarrierId">
          <el-select v-model="form.toCarrierId" placeholder="请选择转入承运商" filterable style="width:100%;">
            <el-option v-for="c in carriers.filter(c => c.id !== form.fromCarrierId)" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="押金承担方" prop="depositBearerId">
          <el-select v-model="form.depositBearerId" placeholder="默认与转出承运商一致" filterable style="width:100%;">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转运明细" prop="details">
          <div style="width:100%;">
            <div style="margin-bottom:8px;">
              <el-button type="primary" size="small" :icon="Plus" :disabled="!availablePallets.length" @click="addDetail">添加托盘</el-button>
              <span style="margin-left:12px;color:#909399;">可转运托盘：{{ availablePallets.length }} 个</span>
              <span style="margin-left:20px;">合计押金：<b class="amount-positive">¥{{ fmt(totalDepositAmount) }}</b></span>
            </div>
            <el-table :data="form.details" border size="small" style="width:100%;">
              <el-table-column label="托盘" width="260">
                <template #default="{row,$index}">
                  <el-select v-model="row.pickupDetailId" placeholder="选择托盘" filterable style="width:100%;" @change="() => onPalletChange($index)">
                    <el-option 
                      v-for="p in getSelectablePallets($index)" 
                      :key="p.id" 
                      :label="p.palletCode + ' - ¥' + p.depositAmount + ' (' + p.currentHolderName + ')'" 
                      :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="当前持有人" width="140">
                <template #default="{row}">{{ getPalletInfo(row.pickupDetailId)?.currentHolderName }}</template>
              </el-table-column>
              <el-table-column label="押金(元)" align="right" width="120">
                <template #default="{row}">¥{{ fmt(getPalletInfo(row.pickupDetailId)?.depositAmount) }}</template>
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认转运</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="转运明细" width="700px">
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="转运单号">{{ currentTransfer.transferNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="currentTransfer.status === 'CONFIRMED' ? 'success' : 'info'">
            {{ currentTransfer.status === 'CONFIRMED' ? '已确认' : '已取消' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="转出承运商">{{ currentTransfer.fromCarrierName }}</el-descriptions-item>
        <el-descriptions-item label="转入承运商">{{ currentTransfer.toCarrierName }}</el-descriptions-item>
        <el-descriptions-item label="押金承担方">{{ currentTransfer.depositBearerName }}</el-descriptions-item>
        <el-descriptions-item label="托盘数量">{{ currentTransfer.palletCount }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ fmt(currentTransfer.totalDeposit) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTransfer.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentDetails" border stripe size="small">
        <el-table-column prop="palletCode" label="托盘编码" />
        <el-table-column prop="depositAmount" label="押金(元)" align="right">
          <template #default="{row}">¥{{ fmt(row.depositAmount) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listShippers, listCarriers } from '../../api/auth'
import { listTransfers, getTransferById, createTransfer, cancelTransfer, getTransfersByPickupDetailId } from '../../api/palletTransfer'
import { getLifecycleByPickupDetailId } from '../../api/depositLifecycle'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')

const query = reactive({ fromCarrierId: null, toCarrierId: null, status: '' })
const list = ref([])
const carriers = ref([])
const shippers = ref([])
const availablePallets = ref([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const currentTransfer = ref({})
const currentDetails = ref([])
const form = reactive({ 
  fromCarrierId: null, 
  toCarrierId: null, 
  depositBearerId: null, 
  details: [], 
  remark: '' 
})
const rules = {
  fromCarrierId: [{ required: true, message: '请选择转出承运商', trigger: 'change' }],
  toCarrierId: [{ required: true, message: '请选择转入承运商', trigger: 'change' }]
}

const totalDepositAmount = computed(() =>
  form.details.reduce((sum, d) => sum + Number(getPalletInfo(d.pickupDetailId)?.depositAmount || 0), 0)
)

const resetQuery = () => { 
  query.fromCarrierId = null
  query.toCarrierId = null
  query.status = ''
  loadData() 
}
const loadData = async () => {
  list.value = await listTransfers(query) || []
}
const onFromCarrierChange = async () => {
  form.toCarrierId = null
  form.depositBearerId = null
  form.details = []
  if (form.fromCarrierId) {
    availablePallets.value = await loadAvailablePallets(form.fromCarrierId) || []
  } else {
    availablePallets.value = []
  }
}
const loadAvailablePallets = async (carrierId) => {
  const lifecycles = await getLifecyclesByShipperIdForTransfer(carrierId) || []
  return lifecycles.filter(l => l.currentStatus === '使用中' && l.currentHolderId === carrierId)
}
const getPalletInfo = (id) => availablePallets.value.find(p => p.pickupDetailId === id)
const getSelectablePallets = (idx) => {
  const selectedIds = form.details.map((d, i) => i !== idx ? d.pickupDetailId : null).filter(Boolean)
  return availablePallets.value.filter(p => !selectedIds.includes(p.pickupDetailId))
}
const onPalletChange = (idx) => {
  const p = getPalletInfo(form.details[idx].pickupDetailId)
  if (p) { 
    form.details[idx].palletCode = p.palletCode
    form.details[idx].depositAmount = p.initialDepositAmount 
  }
}
const addDetail = () => form.details.push({ pickupDetailId: null, palletCode: '', remark: '', depositAmount: 0 })
const removeDetail = (idx) => form.details.splice(idx, 1)

const getLifecyclesByShipperIdForTransfer = async (carrierId) => {
  try {
    const shippersList = shippers.value
    const allPallets = []
    for (const shipper of shippersList) {
      const lifecycles = await getLifecycleByPickupDetailId ? [] : (await getLifecyclesByShipperId(shipper.id, true)) || []
      for (const life of lifecycles) {
        if (life.currentHolderId === carrierId) {
          allPallets.push(life)
        }
      }
    }
    return allPallets
  } catch (e) {
    return []
  }
}

const handleAdd = async () => {
  Object.assign(form, { 
    fromCarrierId: currentUser.roleType === 'CARRIER' ? currentUser.userId : (carriers.value[0]?.id || null), 
    toCarrierId: null, 
    depositBearerId: null, 
    details: [], 
    remark: '' 
  })
  if (form.details.length === 0) addDetail()
  if (form.fromCarrierId) await onFromCarrierChange()
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!form.details.length) return ElMessage.warning('请添加托盘明细')
  if (form.details.some(d => !d.pickupDetailId)) return ElMessage.warning('请选择所有托盘')
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createTransfer({ ...form, userId: currentUser.userId })
    ElMessage.success('转运成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const viewDetails = async (row) => {
  currentTransfer.value = row
  currentDetails.value = (await getTransfersByPickupDetailId(row.id)) || []
  detailVisible.value = true
}
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该转运记录吗？取消后将恢复原持有人。', '确认取消', {
      type: 'warning'
    })
    await cancelTransfer(row.id, currentUser.userId)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消失败')
  }
}

onMounted(async () => {
  carriers.value = await listCarriers() || []
  shippers.value = await listShippers() || []
  if (currentUser.roleType === 'CARRIER') query.fromCarrierId = currentUser.userId
  loadData()
})
</script>

<style scoped>
.amount-positive {
  color: #67c23a;
}
</style>
