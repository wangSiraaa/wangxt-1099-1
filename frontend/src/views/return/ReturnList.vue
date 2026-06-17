<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">托盘归还</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增归还</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="承运商">
          <el-select v-model="query.carrierId" clearable placeholder="全部" style="width:200px;" @change="loadData">
            <el-option v-for="c in carriers" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货方">
          <el-select v-model="query.shipperId" clearable placeholder="全部" style="width:200px;" @change="loadData">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
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
        <el-table-column prop="returnNo" label="归还单号" width="170" />
        <el-table-column prop="carrierName" label="承运商" />
        <el-table-column prop="shipperName" label="发货方" />
        <el-table-column label="归还情况" width="360">
          <template #default="{row}">
            <el-tag type="success" size="small">正常{{ row.normalCount }}</el-tag>
            <el-tag v-if="row.missingPartCount" type="warning" size="small" style="margin-left:4px;">缺件{{ row.missingPartCount }}</el-tag>
            <el-tag v-if="row.stainCount" type="warning" size="small" style="margin-left:4px;">污损{{ row.stainCount }}</el-tag>
            <el-tag v-if="row.scrappedCount" type="danger" size="small" style="margin-left:4px;">报废{{ row.scrappedCount }}</el-tag>
            <el-tag v-if="row.lostCount" type="danger" size="small" style="margin-left:4px;">丢失{{ row.lostCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deductionAmount" label="扣款(元)" align="right" width="110">
          <template #default="{row}">
            <span v-if="row.deductionAmount > 0" class="amount-negative">¥{{ fmt(row.deductionAmount) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="returnDate" label="归还日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag type="success" size="small">已确认</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="viewDetails(row)">明细</el-button>
            <el-button type="success" link size="small" @click="viewLifecycle(row)">追踪</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">修改</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改托盘归还' : '新增托盘归还'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="承运商" prop="carrierId">
          <el-select v-model="form.carrierId" placeholder="选择承运商" filterable style="width:100%;">
            <el-option v-for="c in carriers" :key="c.id" :label="c.userName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货方" prop="shipperId">
          <el-select v-model="form.shipperId" placeholder="选择发货方" filterable style="width:100%;" @change="loadUnreturnedPallets">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归还日期" prop="returnDate">
          <el-date-picker v-model="form.returnDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="归还明细" prop="details">
          <div style="width:100%;">
            <div style="margin-bottom:8px;">
              <el-button type="primary" size="small" :icon="Plus" :disabled="!form.shipperId" @click="addDetail">添加托盘</el-button>
              <span style="margin-left:12px;color:#909399;">可归还：{{ unreturnedPallets.length }} 个</span>
              <span style="margin-left:20px;">合计扣款：<b class="amount-negative">¥{{ fmt(totalDeduction) }}</b></span>
            </div>
            <el-table :data="form.details" border size="small" style="width:100%;">
              <el-table-column label="托盘" width="260">
                <template #default="{row,$index}">
                  <el-select v-model="row.palletId" placeholder="选择托盘" filterable style="width:100%;" @change="() => onPalletSelChange($index)">
                    <el-option v-for="p in getSelectablePallets($index)" :key="p.id" :label="p.palletCode + ' (押金¥' + p.depositAmount + ')'" :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="归还状态" width="140">
                <template #default="{row}">
                  <el-select v-model="row.returnStatus" style="width:100%;" size="small" @change="() => onReturnStatusChange(row)">
                    <el-option label="正常归还" value="NORMAL" />
                    <el-option label="缺件" value="MISSING_PART" />
                    <el-option label="污损" value="STAIN" />
                    <el-option label="整托报废" value="SCRAPPED" />
                    <el-option label="丢失" value="LOST" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="破损类型" width="120" v-if="showDamageTypeColumn">
                <template #default="{row}">
                  <el-select v-model="row.damageType" style="width:100%;" size="small">
                    <el-option label="缺件" value="MISSING_PART" />
                    <el-option label="污损" value="STAIN" />
                    <el-option label="整托报废" value="SCRAPPED" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="扣款(元)" width="110" align="right">
                <template #default="{row,$index}">¥{{ fmt(calcDeduction($index)) }}</template>
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存修改' : '确认归还' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="归还明细" width="720px">
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="归还单号">{{ currentReturn.returnNo }}</el-descriptions-item>
        <el-descriptions-item label="承运商">{{ currentReturn.carrierName }}</el-descriptions-item>
        <el-descriptions-item label="发货方">{{ currentReturn.shipperName }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ currentReturn.returnDate }}</el-descriptions-item>
        <el-descriptions-item label="归还数量">
          正常{{ currentReturn.normalCount }}
          <span v-if="currentReturn.missingPartCount"> / 缺件{{ currentReturn.missingPartCount }}</span>
          <span v-if="currentReturn.stainCount"> / 污损{{ currentReturn.stainCount }}</span>
          <span v-if="currentReturn.scrappedCount"> / 报废{{ currentReturn.scrappedCount }}</span>
          <span v-if="currentReturn.lostCount"> / 丢失{{ currentReturn.lostCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="扣款金额"><span class="amount-negative">¥{{ fmt(currentReturn.deductionAmount) }}</span></el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentDetails" border stripe size="small">
        <el-table-column prop="palletCode" label="托盘编码" />
        <el-table-column prop="returnStatus" label="归还状态">
          <template #default="{row}">
            <el-tag v-if="row.returnStatus==='NORMAL'" type="success" size="small">正常</el-tag>
            <el-tag v-else-if="row.returnStatus==='MISSING_PART'" type="warning" size="small">缺件</el-tag>
            <el-tag v-else-if="row.returnStatus==='STAIN'" type="warning" size="small">污损</el-tag>
            <el-tag v-else-if="row.returnStatus==='SCRAPPED'" type="danger" size="small">整托报废</el-tag>
            <el-tag v-else-if="row.returnStatus==='LOST'" type="danger" size="small">丢失</el-tag>
            <el-tag v-else size="small">{{ row.returnStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="damageType" label="破损类型" width="100">
          <template #default="{row}">
            <span v-if="row.damageType==='MISSING_PART'">缺件</span>
            <span v-else-if="row.damageType==='STAIN'">污损</span>
            <span v-else-if="row.damageType==='SCRAPPED'">整托报废</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="depositAmount" label="押金(元)" align="right">
          <template #default="{row}">¥{{ fmt(row.depositAmount) }}</template>
        </el-table-column>
        <el-table-column prop="deductionAmount" label="扣款(元)" align="right">
          <template #default="{row}">
            <span class="amount-negative">¥{{ fmt(row.deductionAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="lifecycleVisible" title="押金生命周期追踪" width="900px">
      <div v-for="(life, idx) in currentLifecycles" :key="idx" style="margin-bottom:20px;padding-bottom:20px;border-bottom:1px solid #ebeef5;">
        <el-descriptions :column="3" border size="small" style="margin-bottom:12px;">
          <el-descriptions-item label="托盘编码">{{ life.palletCode }}</el-descriptions-item>
          <el-descriptions-item label="初始押金">¥{{ fmt(life.initialDepositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="当前押金">¥{{ fmt(life.currentDepositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="累计扣款">¥{{ fmt(life.totalDeductedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag size="small">{{ life.currentStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最新时间">{{ life.latestEventTime }}</el-descriptions-item>
        </el-descriptions>
        <el-timeline>
          <el-timeline-item
            v-for="(event, eIdx) in life.events"
            :key="eIdx"
            :timestamp="event.eventTime"
            :type="getEventTypeColor(event.eventType)"
            size="large">
            <el-card shadow="hover" size="small">
              <h4 style="margin:0 0 8px 0;">{{ event.eventTypeName }}</h4>
              <p style="margin:4px 0;">
                单据号：{{ event.eventNo }}
              </p>
              <p style="margin:4px 0;" v-if="event.fromHolderName">
                持有人变更：{{ event.fromHolderName }} → {{ event.toHolderName }}
              </p>
              <p style="margin:4px 0;" v-if="event.toHolderName && !event.fromHolderName">
                持有人：{{ event.toHolderName }}
              </p>
              <p style="margin:4px 0;" v-if="event.depositBearerName">
                押金承担方：{{ event.depositBearerName }}
              </p>
              <p style="margin:4px 0;" v-if="event.depositAmount">
                押金金额：<b>¥{{ fmt(event.depositAmount) }}</b>
              </p>
              <p style="margin:4px 0;" v-if="event.deductionAmount">
                扣款金额：<b class="amount-negative">¥{{ fmt(event.deductionAmount) }}</b>
              </p>
              <p style="margin:4px 0;" v-if="event.returnStatusName">
                归还状态：{{ event.returnStatusName }}
              </p>
              <p style="margin:4px 0;" v-if="event.damageTypeName">
                破损类型：{{ event.damageTypeName }}
              </p>
              <p style="margin:4px 0;color:#909399;" v-if="event.remark">
                备注：{{ event.remark }}
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listShippers, listCarriers } from '../../api/auth'
import { getUnreturnedDetails } from '../../api/palletPickup'
import { listReturns, getReturnDetails, createReturn, updateReturn, deleteReturn } from '../../api/palletReturn'
import { getLifecyclesByPickupId } from '../../api/depositLifecycle'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')
const MISSING_PART_RATE = 0.3
const STAIN_RATE = 0.2
const SCRAPPED_RATE = 1.0
const LOST_RATE = 1.0

const query = reactive({ carrierId: null, shipperId: null })
const list = ref([])
const carriers = ref([])
const shippers = ref([])
const unreturnedPallets = ref([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const lifecycleVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const submitting = ref(false)
const currentReturn = ref({})
const currentDetails = ref([])
const currentLifecycles = ref([])
const form = reactive({ carrierId: null, shipperId: null, returnDate: new Date().toISOString().slice(0,10), details: [], remark: '' })
const rules = {
  carrierId: [{ required: true, message: '请选择承运商', trigger: 'change' }],
  shipperId: [{ required: true, message: '请选择发货方', trigger: 'change' }],
  returnDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const showDamageTypeColumn = computed(() => 
  form.details.some(d => ['MISSING_PART', 'STAIN', 'SCRAPPED'].includes(d.returnStatus))
)

const totalDeduction = computed(() => form.details.reduce((sum, _, i) => sum + calcDeduction(i), 0))

const onReturnStatusChange = (row) => {
  if (['MISSING_PART', 'STAIN', 'SCRAPPED'].includes(row.returnStatus)) {
    row.damageType = row.returnStatus
  } else {
    row.damageType = null
  }
}

const calcDeduction = (idx) => {
  const d = form.details[idx]
  const p = unreturnedPallets.value.find(x => x.id === d.palletId)
  const dep = p?.depositAmount || d.depositAmount || 0
  if (d.returnStatus === 'MISSING_PART') return Number((dep * MISSING_PART_RATE).toFixed(2))
  if (d.returnStatus === 'STAIN') return Number((dep * STAIN_RATE).toFixed(2))
  if (d.returnStatus === 'SCRAPPED') return Number((dep * SCRAPPED_RATE).toFixed(2))
  if (d.returnStatus === 'LOST') return Number((dep * LOST_RATE).toFixed(2))
  return 0
}
const resetQuery = () => { query.carrierId = null; query.shipperId = null; loadData() }
const loadData = async () => {
  let cid = query.carrierId, sid = query.shipperId
  if (currentUser.roleType === 'CARRIER') cid = cid || currentUser.userId
  list.value = await listReturns(cid, sid) || []
}
const loadUnreturnedPallets = async () => {
  unreturnedPallets.value = await getUnreturnedDetails(form.shipperId) || []
}
const getSelectablePallets = (idx) => {
  const selected = form.details.map((d, i) => i !== idx ? d.palletId : null).filter(Boolean)
  return unreturnedPallets.value.filter(p => !selected.includes(p.palletId))
}
const onPalletSelChange = (idx) => {
  const p = unreturnedPallets.value.find(x => x.id === form.details[idx].palletId)
  if (p) { form.details[idx].palletCode = p.palletCode; form.details[idx].pickupDetailId = p.id }
}
const addDetail = () => form.details.push({ palletId: null, palletCode: '', returnStatus: 'NORMAL', damageType: null, remark: '', depositAmount: 0, pickupDetailId: null })
const removeDetail = (idx) => form.details.splice(idx, 1)

const resetForm = () => {
  Object.assign(form, {
    carrierId: currentUser.roleType === 'CARRIER' ? currentUser.userId : (carriers.value[0]?.id || null),
    shipperId: shippers.value[0]?.id || null,
    returnDate: new Date().toISOString().slice(0,10),
    details: [], remark: ''
  })
  isEdit.value = false
  editId.value = null
  unreturnedPallets.value = []
  addDetail()
  if (form.shipperId) loadUnreturnedPallets()
}
const handleAdd = async () => {
  resetForm()
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true
  editId.value = row.id
  const details = await getReturnDetails(row.id) || []
  Object.assign(form, {
    carrierId: row.carrierId,
    shipperId: row.shipperId,
    returnDate: row.returnDate,
    remark: row.remark,
    details: details.map(d => ({
      palletId: d.palletId, palletCode: d.palletCode, returnStatus: d.returnStatus,
      remark: d.remark, depositAmount: d.depositAmount, pickupDetailId: d.pickupId
    }))
  })
  await loadUnreturnedPallets()
  for (const d of form.details) {
    const p = details.find(x => x.palletId === d.palletId)
    if (p && !unreturnedPallets.value.find(x => x.id === d.palletId)) {
      unreturnedPallets.value.push({ id: d.palletId, palletCode: d.palletCode, depositAmount: d.depositAmount })
    }
  }
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!form.details.length) return ElMessage.warning('请添加归还明细')
  if (form.details.some(d => !d.palletId)) return ElMessage.warning('请选择所有托盘')
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) await updateReturn(editId.value, form)
    else await createReturn(form)
    ElMessage.success(isEdit.value ? '修改成功' : '归还成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除归还单[${row.returnNo}]吗？删除后将恢复托盘状态和押金余额。`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteReturn(row.id)
      ElMessage.success('删除成功')
      loadData()
    }).catch(() => {})
}
const viewDetails = async (row) => {
  currentReturn.value = row
  currentDetails.value = await getReturnDetails(row.id) || []
  detailVisible.value = true
}
const viewLifecycle = async (row) => {
  const details = await getReturnDetails(row.id) || []
  const pickupIds = [...new Set(details.map(d => d.pickupId).filter(Boolean))]
  const allLifecycles = []
  for (const pid of pickupIds) {
    const lifecycles = await getLifecyclesByPickupId(pid) || []
    for (const life of lifecycles) {
      if (details.some(d => d.palletId === life.palletId)) {
        allLifecycles.push(life)
      }
    }
  }
  currentLifecycles.value = allLifecycles
  lifecycleVisible.value = true
}
const getEventTypeColor = (type) => {
  const colors = {
    'PICKUP': 'primary',
    'TRANSFER': 'warning',
    'RETURN': 'success',
    'DEDUCTION': 'danger',
    'SETTLEMENT': 'info'
  }
  return colors[type] || ''
}

onMounted(async () => {
  carriers.value = await listCarriers() || []
  shippers.value = await listShippers() || []
  if (currentUser.roleType === 'CARRIER') query.carrierId = currentUser.userId
  loadData()
})
</script>
