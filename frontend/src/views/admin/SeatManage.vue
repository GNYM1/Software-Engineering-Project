<template>
  <section>
    <div class="page-title">
      <div>
        <h2>座位管理</h2>
        <p class="muted">维护座位编号、类型、状态和座位图行列位置。</p>
      </div>
      <el-button type="primary" :disabled="!selectedRoomId" @click="openDialog()">新增座位</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="selectedRoomId" placeholder="选择自习室" class="room-select" @change="loadSeats">
        <el-option v-for="room in rooms" :key="room.roomId" :label="room.roomName" :value="room.roomId" />
      </el-select>
      <el-button :loading="loading" @click="loadSeats">刷新</el-button>
    </div>

    <el-table class="panel" :data="seats" v-loading="loading" row-key="seatId">
      <el-table-column prop="seatNumber" label="座位编号" width="120" />
      <el-table-column prop="seatType" label="类型" width="120" />
      <el-table-column label="位置" width="110">
        <template #default="{ row }">{{ row.rowNum }} 行 {{ row.colNum }} 列</template>
      </el-table-column>
      <el-table-column prop="qrCode" label="二维码标识" min-width="150" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeSeat(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.seatId ? '编辑座位' : '新增座位'" width="500px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="座位编号"><el-input v-model="form.seatNumber" /></el-form-item>
        <el-form-item label="座位类型">
          <el-select v-model="form.seatType">
            <el-option label="普通" value="普通" />
            <el-option label="靠窗" value="靠窗" />
            <el-option label="插座" value="插座" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="空闲" value="空闲" />
            <el-option label="已预约" value="已预约" />
            <el-option label="使用中" value="使用中" />
            <el-option label="维护中" value="维护中" />
          </el-select>
        </el-form-item>
        <el-form-item label="行号"><el-input-number v-model="form.rowNum" :min="1" /></el-form-item>
        <el-form-item label="列号"><el-input-number v-model="form.colNum" :min="1" /></el-form-item>
        <el-form-item label="二维码"><el-input v-model="form.qrCode" placeholder="选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSeat">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const rooms = ref([])
const seats = ref([])
const selectedRoomId = ref(null)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const form = reactive(emptyForm())

function emptyForm() {
  return {
    seatId: null,
    seatNumber: '',
    seatType: '普通',
    status: '空闲',
    rowNum: 1,
    colNum: 1,
    qrCode: ''
  }
}

function resetForm(row = null) {
  Object.assign(form, emptyForm(), row || {})
}

function openDialog(row = null) {
  resetForm(row)
  dialogVisible.value = true
}

async function loadRooms() {
  const res = await request.get('/rooms')
  rooms.value = res.data || []
  if (!selectedRoomId.value && rooms.value.length) {
    selectedRoomId.value = rooms.value[0].roomId
  }
}

async function loadSeats() {
  if (!selectedRoomId.value) return
  loading.value = true
  try {
    const res = await request.get(`/rooms/${selectedRoomId.value}/seats`)
    seats.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function saveSeat() {
  saving.value = true
  try {
    const payload = { ...form }
    if (form.seatId) {
      await request.put(`/seats/${form.seatId}`, payload)
    } else {
      await request.post(`/rooms/${selectedRoomId.value}/seats`, payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadSeats()
  } finally {
    saving.value = false
  }
}

async function removeSeat(row) {
  await ElMessageBox.confirm(`确认删除座位“${row.seatNumber}”？`, '删除确认', { type: 'warning' })
  await request.delete(`/seats/${row.seatId}`)
  ElMessage.success('删除成功')
  await loadSeats()
}

onMounted(async () => {
  await loadRooms()
  await loadSeats()
})
</script>

<style scoped>
.room-select {
  width: 240px;
}
</style>
