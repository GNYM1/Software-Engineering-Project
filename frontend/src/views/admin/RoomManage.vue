<template>
  <section>
    <div class="page-title">
      <div>
        <h2>自习室管理</h2>
        <p class="muted">维护自习室基础信息和开放状态。</p>
      </div>
      <el-button type="primary" @click="openDialog()">新增自习室</el-button>
    </div>

    <el-table class="panel" :data="rooms" v-loading="loading" row-key="roomId">
      <el-table-column prop="roomName" label="名称" min-width="130" />
      <el-table-column prop="location" label="位置" min-width="170" />
      <el-table-column prop="totalSeats" label="座位数" width="90" />
      <el-table-column label="开放时间" width="150">
        <template #default="{ row }">{{ time(row.openTime) }}-{{ time(row.closeTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '开放' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">切换状态</el-button>
          <el-button size="small" type="danger" @click="removeRoom(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.roomId ? '编辑自习室' : '新增自习室'" width="520px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="名称"><el-input v-model="form.roomName" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="座位数"><el-input-number v-model="form.totalSeats" :min="0" /></el-form-item>
        <el-form-item label="开放时间"><el-time-picker v-model="form.openTime" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="关闭时间"><el-time-picker v-model="form.closeTime" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="开放" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRoom">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rooms = ref([])

const form = reactive(emptyForm())

function emptyForm() {
  return {
    roomId: null,
    roomName: '',
    location: '',
    totalSeats: 0,
    openTime: '07:00:00',
    closeTime: '22:00:00',
    status: 1,
    description: ''
  }
}

function resetForm(data = null) {
  Object.assign(form, emptyForm(), data || {})
}

function time(value) {
  return value ? String(value).slice(0, 5) : '--:--'
}

function openDialog(row = null) {
  resetForm(row)
  dialogVisible.value = true
}

async function loadRooms() {
  loading.value = true
  try {
    const res = await request.get('/rooms')
    rooms.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function saveRoom() {
  saving.value = true
  try {
    const payload = { ...form }
    if (form.roomId) {
      await request.put(`/rooms/${form.roomId}`, payload)
    } else {
      await request.post('/rooms', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadRooms()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  await request.put(`/rooms/${row.roomId}/status`)
  ElMessage.success('状态已更新')
  await loadRooms()
}

async function removeRoom(row) {
  await ElMessageBox.confirm(`确认删除“${row.roomName}”？`, '删除确认', { type: 'warning' })
  await request.delete(`/rooms/${row.roomId}`)
  ElMessage.success('删除成功')
  await loadRooms()
}

onMounted(loadRooms)
</script>
