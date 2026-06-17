import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('pallet_user') || 'null')
    if (user && user.token) {
      config.headers.Authorization = `Bearer ${user.token}`
      config.headers['X-User-Id'] = user.userId
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    ElMessage.error(res.message || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('pallet_user')
      router.push('/login')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
