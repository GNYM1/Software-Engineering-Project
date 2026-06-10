<template>
  <section>
    <div class="page-title">
      <div>
        <h2>我的预约</h2>
        <p class="muted">查看预约记录，并完成取消、签到、签退操作。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadReservations">刷新</el-button>
    </div>

    <el-table class="panel" :data="reservations" v-loading="loading" row-key="reservationId">
      <el-table-column prop="reservationId" label="编号" width="90" />
      <el-table-column prop="seatId" label="座位ID" width="100" />
      <el-table-column prop="reserveDate" label="日期" width="120" />
      <el-table-column prop="timeSlot" label="时段" width="100" />
      <el-table-column label="开始时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :disabled="!isReserved(row)" @click="operate(row, 'cancel')">取消</el-button>
          <el-button size="small" type="primary" :disabled="!isReserved(row)" @click="operate(row, 'checkin')">签到</el-button>
          <el-button size="small" type="success" :disabled="!isUsing(row)" @click="operate(row, 'checkout')">签退</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const reservations = ref([])

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

function statusType(status = '') {
  if (status.includes('完成')) return 'success'
  if (status.includes('使用')) return 'primary'
  if (status.includes('取消') || status.includes('违约')) return 'danger'
  return 'warning'
}

function isReserved(row) {
  return String(row.status || '').includes('预约')
}

function isUsing(row) {
  return String(row.status || '').includes('使用')
}

async function loadReservations() {
  loading.value = true
  try {
    const res = await request.get('/reservations/my')
    reservations.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function operate(row, action) {
  const map = {
    cancel: { text: '取消预约', method: 'delete', url: `/reservations/${row.reservationId}` },
    checkin: { text: '签到', method: 'post', url: `/reservations/${row.reservationId}/checkin` },
    checkout: { text: '签退', method: 'post', url: `/reservations/${row.reservationId}/checkout` }
  }
  const item = map[action]
  await ElMessageBox.confirm(`确认执行“${item.text}”？`, '操作确认', { type: 'warning' })
  await request[item.method](item.url)
  ElMessage.success(`${item.text}成功`)
  await loadReservations()
}

onMounted(loadReservations)
</script>
