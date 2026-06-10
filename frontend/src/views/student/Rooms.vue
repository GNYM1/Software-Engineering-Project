<template>
  <section>
    <div class="page-title">
      <div>
        <h2>自习室列表</h2>
        <p class="muted">选择开放中的自习室进入座位图。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadRooms">刷新</el-button>
    </div>

    <el-empty v-if="!loading && rooms.length === 0" description="暂无开放自习室" />
    <div v-else class="room-grid">
      <article v-for="room in rooms" :key="room.roomId" class="room-card panel">
        <div class="room-head">
          <h3>{{ room.roomName }}</h3>
          <el-tag :type="room.status === 1 ? 'success' : 'info'">
            {{ room.status === 1 ? '开放' : '关闭' }}
          </el-tag>
        </div>
        <p class="location">{{ room.location }}</p>
        <div class="room-meta">
          <span>{{ room.totalSeats || 0 }} 个座位</span>
          <span>{{ formatTime(room.openTime) }} - {{ formatTime(room.closeTime) }}</span>
        </div>
        <p class="desc">{{ room.description || '暂无说明' }}</p>
        <el-button
          type="primary"
          :disabled="room.status !== 1"
          @click="$emit('select-room', room)"
        >
          查看座位
        </el-button>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../../api/request'

defineEmits(['select-room'])

const rooms = ref([])
const loading = ref(false)

function formatTime(value) {
  return value ? String(value).slice(0, 5) : '--:--'
}

async function loadRooms() {
  loading.value = true
  try {
    const res = await request.get('/rooms', { params: { status: 1 } })
    rooms.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadRooms)
</script>

<style scoped>
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.room-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 240px;
  padding: 18px;
}

.room-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.room-head h3 {
  margin: 0;
  font-size: 18px;
}

.location,
.desc {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.room-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.room-meta span {
  padding: 6px 10px;
  border-radius: 6px;
  color: #334155;
  background: #eef3f8;
  font-size: 13px;
}

.room-card .el-button {
  margin-top: auto;
}
</style>
