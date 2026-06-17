<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">押金生命周期追踪</h2>
    </div>
    <el-card shadow="hover" style="margin-bottom:16px;">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="查询类型">
          <el-select v-model="query.searchType" style="width:160px;">
            <el-option label="发货方查询" value="shipper" />
            <el-option label="托盘编码查询" value="pallet" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货方" v-if="query.searchType==='shipper'">
          <el-select v-model="query.shipperId" placeholder="请选择发货方" clearable style="width:180px;">
            <el-option v-for="s in shippers" :key="s.id" :label="s.userName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="托盘编码" v-if="query.searchType==='pallet'">
          <el-input v-model="query.palletCode" placeholder="请输入托盘编码" clearable style="width:180px;" />
        </el-form-item>
        <el-form-item label="显示未归还" v-if="query.searchType==='shipper'">
          <el-switch v-model="query.onlyUnreturned" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-empty v-if="!lifecycles.length" description="暂无数据，请选择查询条件后查询" />

      <div v-for="(life, idx) in lifecycles" :key="idx" style="margin-bottom:24px;padding-bottom:24px;border-bottom:1px solid #ebeef5;">
        <el-descriptions :column="4" border size="small" style="margin-bottom:12px;">
          <el-descriptions-item label="托盘编码">
            <span style="font-weight:600;">{{ life.palletCode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="初始押金">¥{{ fmt(life.initialDepositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="当前押金">
            <span :class="life.currentDepositAmount > 0 ? 'amount-positive' : ''">
              ¥{{ fmt(life.currentDepositAmount) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="累计扣款">
            <span class="amount-negative">¥{{ fmt(life.totalDeductedAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="life.currentStatus==='已归还' ? 'success' : life.currentStatus==='未归还' ? 'warning' : 'info'" size="small">
              {{ life.currentStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前持有人">{{ life.currentHolderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="押金承担方">{{ life.depositBearerName || '-' }}</el-descriptions-item>
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
              <h4 style="margin:0 0 8px 0;">
                <el-tag :type="getEventTypeColor(event.eventType)" size="small" style="margin-right:8px;">
                  {{ event.eventTypeName }}
                </el-tag>
                <span style="font-size:14px;">{{ event.eventNo }}</span>
              </h4>
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:4px 24px;">
                <p style="margin:4px 0;" v-if="event.fromHolderName">
                  持有人变更：{{ event.fromHolderName }} → <b>{{ event.toHolderName }}</b>
                </p>
                <p style="margin:4px 0;" v-if="event.toHolderName && !event.fromHolderName">
                  持有人：<b>{{ event.toHolderName }}</b>
                </p>
                <p style="margin:4px 0;" v-if="event.depositBearerName">
                  押金承担方：<b>{{ event.depositBearerName }}</b>
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
                <p style="margin:4px 0;" v-if="event.carrierName">
                  承运商：{{ event.carrierName }}
                </p>
                <p style="margin:4px 0;" v-if="event.destStoreName">
                  目的门店：{{ event.destStoreName }}
                </p>
                <p style="margin:4px 0;" v-if="event.palletCount">
                  托盘数量：{{ event.palletCount }}
                </p>
              </div>
              <p style="margin:4px 0;color:#909399;" v-if="event.remark">
                备注：{{ event.remark }}
              </p>
              <p style="margin:4px 0;color:#909399;font-size:12px;" v-if="event.operatorName">
                操作人：{{ event.operatorName }} · {{ event.eventTime }}
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listShippers, listCarriers } from '../../api/auth'
import {
  getLifecyclesByShipperId,
  getLifecycleByPalletCode
} from '../../api/depositLifecycle'

const fmt = (v) => v ? Number(v).toFixed(2) : '0.00'
const currentUser = JSON.parse(localStorage.getItem('pallet_user') || '{}')

const query = reactive({
  searchType: 'shipper',
  shipperId: null,
  palletCode: '',
  onlyUnreturned: true
})
const shippers = ref([])
const carriers = ref([])
const lifecycles = ref([])
const loading = ref(false)

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

const resetQuery = () => {
  query.searchType = 'shipper'
  query.shipperId = null
  query.palletCode = ''
  query.onlyUnreturned = true
  lifecycles.value = []
}

const loadData = async () => {
  loading.value = true
  try {
    if (query.searchType === 'shipper') {
      const sid = query.shipperId || (currentUser.roleType === 'SHIPPER' ? currentUser.userId : null)
      if (!sid) {
        ElMessage.warning('请选择发货方')
        return
      }
      lifecycles.value = await getLifecyclesByShipperId(sid, query.onlyUnreturned) || []
    } else {
      if (!query.palletCode?.trim()) {
        ElMessage.warning('请输入托盘编码')
        return
      }
      const data = await getLifecycleByPalletCode(query.palletCode.trim())
      lifecycles.value = data ? [data] : []
    }
    if (!lifecycles.value.length) {
      ElMessage.info('未查询到相关数据')
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  shippers.value = await listShippers() || []
  carriers.value = await listCarriers() || []
  if (currentUser.roleType === 'SHIPPER') {
    query.shipperId = currentUser.userId
  }
})
</script>

<style scoped>
.amount-positive { color: #67c23a; font-weight: 600; }
.amount-negative { color: #f56c6c; font-weight: 600; }
</style>
