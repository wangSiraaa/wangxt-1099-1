<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div style="color:#909399;font-size:13px;">托盘总数</div>
              <div style="font-size:28px;font-weight:600;color:#303133;margin-top:8px;">{{ stats.totalPallets }}</div>
            </div>
            <el-icon style="font-size:40px;color:#409eff;"><Goods /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div style="color:#909399;font-size:13px;">可用托盘</div>
              <div style="font-size:28px;font-weight:600;color:#67c23a;margin-top:8px;">{{ stats.availablePallets }}</div>
            </div>
            <el-icon style="font-size:40px;color:#67c23a;"><CircleCheck /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div style="color:#909399;font-size:13px;">使用中</div>
              <div style="font-size:28px;font-weight:600;color:#e6a23c;margin-top:8px;">{{ stats.inUsePallets }}</div>
            </div>
            <el-icon style="font-size:40px;color:#e6a23c;"><Loading /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="margin-bottom:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div style="color:#909399;font-size:13px;">破损/丢失</div>
              <div style="font-size:28px;font-weight:600;color:#f56c6c;margin-top:8px;">{{ stats.abnormalPallets }}</div>
            </div>
            <el-icon style="font-size:40px;color:#f56c6c;"><Warning /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:600;">押金概览</span>
            </div>
          </template>
          <el-table :data="depositBalances" size="small" stripe>
            <el-table-column prop="shipperName" label="发货方" />
            <el-table-column prop="totalPaid" label="已缴纳" align="right">
              <template #default="{row}"><span class="amount-positive">¥{{ formatAmount(row.totalPaid) }}</span></template>
            </el-table-column>
            <el-table-column prop="totalDeducted" label="已扣款" align="right">
              <template #default="{row}"><span class="amount-negative">¥{{ formatAmount(row.totalDeducted) }}</span></template>
            </el-table-column>
            <el-table-column prop="currentBalance" label="当前余额" align="right">
              <template #default="{row}"><b>¥{{ formatAmount(row.currentBalance) }}</b></template>
            </el-table-column>
            <el-table-column prop="availableAmount" label="可用金额" align="right">
              <template #default="{row}"><el-tag type="success" size="small">¥{{ formatAmount(row.availableAmount) }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-weight:600;">最近操作记录</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item v-for="(item,idx) in recentRecords" :key="idx"
              :timestamp="item.time" placement="top" :type="item.type" :hollow="true">
              <div style="font-weight:500;">{{ item.title }}</div>
              <div style="color:#909399;font-size:12px;">{{ item.desc }}</div>
            </el-timeline-item>
            <el-timeline-item v-if="!recentRecords.length" type="info">暂无记录</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Goods, CircleCheck, Loading, Warning } from '@element-plus/icons-vue'
import { listPallets } from '../api/pallet'
import { listDepositBalances } from '../api/deposit'
import { listPickups } from '../api/palletPickup'
import { listReturns } from '../api/palletReturn'

const stats = ref({ totalPallets: 0, availablePallets: 0, inUsePallets: 0, abnormalPallets: 0 })
const depositBalances = ref([])
const recentRecords = ref([])
const formatAmount = (v) => v ? Number(v).toFixed(2) : '0.00'

const loadData = async () => {
  try {
    const pallets = await listPallets() || []
    stats.value.totalPallets = pallets.length
    stats.value.availablePallets = pallets.filter(p => p.status === 'AVAILABLE').length
    stats.value.inUsePallets = pallets.filter(p => p.status === 'IN_USE').length
    stats.value.abnormalPallets = pallets.filter(p => ['DAMAGED','LOST'].includes(p.status)).length

    depositBalances.value = await listDepositBalances() || []

    const pickups = await listPickups() || []
    const returns = await listReturns() || []
    const records = []
    returns.slice(0, 3).forEach(r => records.push({
      time: r.returnDate, type: 'success',
      title: `托盘归还 - ${r.carrierName}`,
      desc: `归还${r.returnCount}个，扣款¥${formatAmount(r.deductionAmount)}`
    }))
    pickups.slice(0, 2).forEach(p => records.push({
      time: p.pickupDate, type: 'primary',
      title: `托盘领用 - ${p.shipperName}`,
      desc: `领用${p.palletCount}个，押金¥${formatAmount(p.totalDeposit)}`
    }))
    records.sort((a,b) => new Date(b.time) - new Date(a.time))
    recentRecords.value = records.slice(0, 5)
  } catch (e) {}
}

onMounted(loadData)
</script>
