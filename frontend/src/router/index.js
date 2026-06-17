import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'pallets', name: 'PalletList', component: () => import('../views/pallet/PalletList.vue'), meta: { title: '托盘管理' } },
      { path: 'deposits', name: 'DepositList', component: () => import('../views/deposit/DepositList.vue'), meta: { title: '押金管理' } },
      { path: 'pickups', name: 'PickupList', component: () => import('../views/pickup/PickupList.vue'), meta: { title: '托盘领用' } },
      { path: 'returns', name: 'ReturnList', component: () => import('../views/return/ReturnList.vue'), meta: { title: '托盘归还' } },
      { path: 'deductions', name: 'DeductionList', component: () => import('../views/deduction/DeductionList.vue'), meta: { title: '扣款管理' } },
      { path: 'periods', name: 'PeriodList', component: () => import('../views/period/PeriodList.vue'), meta: { title: '账期管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('pallet_user') || 'null')
  if (to.path !== '/login' && !user) {
    next('/login')
  } else {
    next()
  }
})

export default router
