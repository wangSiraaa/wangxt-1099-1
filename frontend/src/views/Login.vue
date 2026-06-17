<template>
  <div style="height:100vh;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);display:flex;align-items:center;justify-content:center;">
    <el-card style="width:420px;box-shadow:0 12px 40px rgba(0,0,0,.15);">
      <div style="text-align:center;margin-bottom:30px;">
        <h2 style="margin:0 0 8px 0;color:#303133;">物流托盘押金系统</h2>
        <p style="margin:0;color:#909399;font-size:13px;">托盘押金循环归还管理平台</p>
      </div>
      <el-form ref="loginFormRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="userCode">
          <el-input v-model="form.userCode" placeholder="请输入用户编码" size="large" :prefix-icon="User">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%;" :loading="loading" @click="handleLogin">登 录</el-button>
      </el-form>
      <div style="margin-top:16px;padding:12px;background:#f5f7fa;border-radius:4px;font-size:12px;color:#606266;">
        <div style="font-weight:600;margin-bottom:6px;">测试账号：</div>
        <div>发货方：SHIPPER001 / 123456</div>
        <div>承运商：CARRIER001 / 123456</div>
        <div>财务：FINANCE001 / 123456</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({ userCode: '', password: '' })
const rules = {
  userCode: [{ required: true, message: '请输入用户编码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await loginFormRef.value?.validate()
  try {
    loading.value = true
    const res = await login(form)
    localStorage.setItem('pallet_user', JSON.stringify(res))
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
