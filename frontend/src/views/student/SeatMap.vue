<template>
  <section>
    <div class="page-title">
      <div>
        <h2>{{ room?.roomName || '座位图' }}</h2>
        <p class="muted">{{ room?.location || '请选择自习室' }}</p>
      </div>
      <el-button @click="$emit('back')">返回自习室</el-button>
    </div>

    <div class="panel panel-pad">
      <div class="toolbar">
        <el-date-picker
          v-model="reserveDate"
          type="date"
          value-format="YYYY-MM-DD"
          :disabled-date="disablePastDate"
          placeholder="预约日期"
        />
        <el-select v-model="timeSlot" class="slot-select" placeholder="预约时段">
          <el-option label="上午 08:00-12:00" value="上午" />
          <el-option label="下午 14:00-18:00" value="下午" />
          <el-option label="晚上 18:30-21:30" value="晚上" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadSeats">刷新座位</el-button>
      </div>

      <div class="legend">
        <span><i class="free"></i>空闲</span>
        <span><i class="reserved"></i>已预约</span>
        <span><i class="using"></i>使用中</span>
        <span><i class="maintaining"></i>维护中</span>
      </div>

      <el-empty v-if="!loading && seats.length === 0" description="暂无座位数据" />
      <div
        v-else
        class="seat-grid"
        :style="{ gridTemplateColumns: `repeat(${maxCol || 1}, minmax(68px, 1fr))` }"
      >
        <button
          v-for="cell in seatCells"
          :key="cell.key"
          type="button"
          class="seat"
          :class="statusClass(cell.seat?.status)"
          :disabled="!cell.seat || !canReserve(cell.seat)"
          @click="openReserve(cell.seat)"
        >
          <span class="seat-no">{{ cell.seat?.seatNumber || '' }}</span>
          <small>{{ cell.seat?.seatType || '' }}</small>
        </button>
      </div>
    </div>

    <el-dialog v-model="reserveDialog" title="确认预约" width="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="座位">{{ selectedSeat?.seatNumber }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ selectedSeat?.seatType }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ reserveDate }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ timeSlotText }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="reserveDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReserve">确认预约</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const props = defineProps({
  room: { type: Object, default: null }
})

defineEmits(['back'])

const loading = ref(false)
const saving = ref(false)
const seats = ref([])
const reserveDialog = ref(false)
const selectedSeat = ref(null)
const reserveDate = ref(new Date().toISOString().slice(0, 10))
const timeSlot = ref('上午')

const slotTimes = {
  '上午': ['08:00:00', '12:00:00'],
  '下午': ['14:00:00', '18:00:00'],
  '晚上': ['18:30:00', '21:30:00']
}

const maxRow = computed(() => Math.max(0, ...seats.value.map(item => item.rowNum || 1)))
const maxCol = computed(() => Math.max(0, ...seats.value.map(item => item.colNum || 1)))
const timeSlotText = computed(() => {
  const times = slotTimes[timeSlot.value]
  return `${timeSlot.value} ${times[0].slice(0, 5)}-${times[1].slice(0, 5)}`
})

const seatCells = computed(() => {
  const map = new Map(seats.value.map(seat => [`${seat.rowNum || 1}-${seat.colNum || 1}`, seat]))
  const cells = []
  for (let row = 1; row <= maxRow.value; row += 1) {
    for (let col = 1; col <= maxCol.value; col += 1) {
      cells.push({ key: `${row}-${col}`, seat: map.get(`${row}-${col}`) })
    }
  }
  return cells
})

function disablePastDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

function statusClass(status = '') {
  if (status.includes('维护')) return 'maintaining'
  if (status.includes('使用')) return 'using'
  if (status.includes('预约')) return 'reserved'
  return 'free'
}

function canReserve(seat) {
  return statusClass(seat.status) === 'free'
}

function openReserve(seat) {
  selectedSeat.value = seat
  reserveDialog.value = true
}

async function loadSeats() {
  if (!props.room?.roomId) return
  loading.value = true
  try {
    const res = await request.get(`/rooms/${props.room.roomId}/seats`)
    seats.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function submitReserve() {
  if (!selectedSeat.value) return
  const [start, end] = slotTimes[timeSlot.value]
  saving.value = true
  try {
    await request.post('/reservations', {
      seatId: selectedSeat.value.seatId,
      reserveDate: reserveDate.value,
      timeSlot: timeSlot.value,
      startTime: `${reserveDate.value}T${start}`,
      endTime: `${reserveDate.value}T${end}`
    })
    ElMessage.success('预约成功')
    reserveDialog.value = false
    await loadSeats()
  } finally {
    saving.value = false
  }
}

watch(() => props.room?.roomId, loadSeats)
onMounted(loadSeats)
</script>

<style scoped>
.slot-select {
  width: 180px;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 18px;
  color: #475569;
  font-size: 14px;
}

.legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend i {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.seat-grid {
  display: grid;
  gap: 12px;
  max-width: 780px;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}

.seat {
  display: grid;
  place-items: center;
  gap: 2px;
  min-height: 68px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.seat:enabled:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.12);
}

.seat:disabled {
  cursor: not-allowed;
}

.seat-no {
  font-size: 16px;
  font-weight: 800;
}

.seat small {
  font-size: 12px;
}

.free {
  color: #166534;
  border-color: #86efac;
  background: #dcfce7;
}

.reserved {
  color: #92400e;
  border-color: #facc15;
  background: #fef3c7;
}

.using {
  color: #991b1b;
  border-color: #fca5a5;
  background: #fee2e2;
}

.maintaining {
  color: #475569;
  border-color: #cbd5e1;
  background: #e2e8f0;
}

@media (max-width: 620px) {
  .seat-grid {
    gap: 8px;
    padding: 10px;
  }

  .seat {
    min-height: 56px;
  }
}
</style>
