<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">托盘管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增托盘</el-button>
    </div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:150px;" @change="loadData">
            <el-option label="闲置" value="AVAILABLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="破损" value="DAMAGED" />
            <el-option label="丢失" value="LOST" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="托盘编码/名称" clearable style="width:200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="hover">
      <el-table :data="filteredList" stripe border>
        <el-table-column prop="palletCode" label="托盘编码" width="120" />
        <el-table-column prop="palletName" label="托盘名称" />
        <el-table-column prop="specification" label="规格" />
        <el-table-column prop="depositAmount" label="押金(元)" width="110" align="right">
          <template #default="{row}">¥{{ Number(row.depositAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :class="statusClass(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑托盘' : '新增托盘'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="托盘编码" prop="palletCode">
          <el-input v-model="form.palletCode" placeholder="请输入托盘编码" />
        </el-form-item>
        <el-form-item label="托盘名称" prop="palletName">
          <el-input v-model="form.palletName" placeholder="请输入托盘名称" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="例如：1200x1000x150mm" />
        </el-form-item>
        <el-form-item label="押金金额" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :step="10" />
          <span style="margin-left:8px;color:#909399;">元</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="闲置" value="AVAILABLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="破损" value="DAMAGED" />
            <el-option label="丢失" value="LOST" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listPallets, createPallet, updatePallet, deletePallet } from '../../api/pallet'

const query = reactive({ status: '', keyword: '' })
const list = ref([])
const filteredList = computed(() => {
  return list.value.filter(item => {
    if (query.keyword && !item.palletCode.includes(query.keyword) && !(item.palletName || '').includes(query.keyword)) return false
    return true
  })
})
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ id: null, palletCode: '', palletName: '', specification: '', depositAmount: 100, status: 'AVAILABLE', remark: '' })
const rules = {
  palletCode: [{ required: true, message: '请输入托盘编码', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入押金金额', trigger: 'blur' }]
}

const statusText = (s) => ({AVAILABLE:'闲置',IN_USE:'使用中',DAMAGED:'破损',LOST:'丢失'}[s] || s)
const statusClass = (s) => ({
  AVAILABLE:'status-tag status-available',
  IN_USE:'status-tag status-in-use',
  DAMAGED:'status-tag status-damaged',
  LOST:'status-tag status-lost'
}[s] || 'status-tag')

const resetQuery = () => { query.status = ''; query.keyword = ''; loadData() }
const loadData = async () => { list.value = await listPallets(query.status) || [] }

const handleAdd = () => {
  Object.assign(form, { id: null, palletCode: '', palletName: '', specification: '', depositAmount: 100, status: 'AVAILABLE', remark: '' })
  dialogVisible.value = true
}
const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.id) await updatePallet(form.id, form)
    else await createPallet(form)
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除托盘[${row.palletCode}]吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deletePallet(row.id)
      ElMessage.success('删除成功')
      loadData()
    }).catch(() => {})
}

onMounted(loadData)
</script>
