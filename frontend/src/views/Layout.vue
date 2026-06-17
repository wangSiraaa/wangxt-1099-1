<template>
  <el-container style="height:100vh;">
    <el-aside width="230px" style="background:#1f2d3d;">
      <div style="height:60px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:16px;font-weight:600;border-bottom:1px solid #2d3e53;">
        <el-icon style="margin-right:8px;font-size:22px;"><Box /></el-icon>
        托盘押金系统
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        style="border-right:none;"
      >
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>工作台</span></el-menu-item>
        <el-menu-item index="/pallets" v-if="isFinance"><el-icon><Goods /></el-icon><span>托盘管理</span></el-menu-item>
        <el-menu-item index="/deposits"><el-icon><Wallet /></el-icon><span>押金管理</span></el-menu-item>
        <el-menu-item index="/pickups" v-if="isShipper || isFinance"><el-icon><TakeawayBox /></el-icon><span>托盘领用</span></el-menu-item>
        <el-menu-item index="/returns" v-if="isCarrier || isFinance"><el-icon><RefreshRight /></el-icon><span>托盘归还</span></el-menu-item>
        <el-menu-item index="/deductions" v-if="isFinance"><el-icon><Money /></el-icon><span>扣款管理</span></el-menu-item>
        <el-menu-item index="/periods" v-if="isFinance"><el-icon><Calendar /></el-icon><span>账期管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #e6e6e6;display:flex;align-items:center;justify-content:space-between;padding:0 24px;">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div style="display:flex;align-items:center;gap:16px;">
          <el-tag :type="roleTagType" size="small">{{ currentUser.roleName }}</el-tag>
          <span style="font-size:14px;color:#606266;">{{ currentUser.userName }}</span>
          <el-button type="text" @click="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-button>
        </div>
      </el-header>
      <el-main style="padding:20px;background:#f0f2f5;">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Box, DataAnalysis, Goods, Wallet, TakeawayBox, RefreshRight, Money, Calendar, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const currentUser = computed(() => {
  const u = JSON.parse(localStorage.getItem('pallet_user') || 'null')
  return u || {}
})
const activeMenu = computed(() => route.path)
const isShipper = computed(() => currentUser.value.roleType === 'SHIPPER')
const isCarrier = computed(() => currentUser.value.roleType === 'CARRIER')
const isFinance = computed(() => currentUser.value.roleType === 'FINANCE')
const roleTagType = computed(() => {
  switch (currentUser.value.roleType) {
    case 'SHIPPER': return ''
    case 'CARRIER': return 'warning'
    case 'FINANCE': return 'success'
    default: return 'info'
  }
})

const logout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' }).then(() => {
    localStorage.removeItem('pallet_user')
    ElMessage.success('已退出')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
