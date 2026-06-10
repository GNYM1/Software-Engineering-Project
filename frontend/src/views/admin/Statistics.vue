<template>
  <section>
    <div class="page-title">
      <div>
        <h2>数据统计</h2>
        <p class="muted">查看座位使用、用户活跃、时段热度和违约情况。</p>
      </div>
      <el-button type="warning" @click="judgeViolations">执行违约判定</el-button>
    </div>

    <div class="toolbar">
      <el-date-picker
        v-model="range"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary" :loading="loading" @click="loadStats">查询</el-button>
    </div>

    <div class="metric-grid">
      <article class="metric panel">
        <span>使用率</span>
        <strong>{{ usage.usageRate ?? 0 }}%</strong>
        <small>完成/使用预约：{{ usage.finishedReservations || 0 }}</small>
      </article>
      <article class="metric panel">
        <span>预约数</span>
        <strong>{{ usage.totalReservations || 0 }}</strong>
        <small>座位预约率：{{ usage.seatBookingRate ?? 0 }}%</small>
      </article>
      <article class="metric panel">
        <span>活跃用户</span>
        <strong>{{ active.activeUserCount || 0 }}</strong>
        <small>总预约记录：{{ active.reservationCount || 0 }}</small>
      </article>
      <article class="metric panel">
        <span>违约率</span>
        <strong>{{ violations.violationRate ?? 0 }}%</strong>
        <small>违约数：{{ violations.violationCount || 0 }}</small>
      </article>
    </div>

    <div class="stats-grid">
      <section class="panel panel-pad">
        <h3>热门时段</h3>
        <div v-if="slotBars.length" class="bars">
          <div v-for="item in slotBars" :key="item.label" class="bar-row">
            <span>{{ item.label }}</span>
            <div class="bar-track"><i :style="{ width: `${item.percent}%` }"></i></div>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
        <el-empty v-else description="暂无时段数据" />
      </section>

      <section class="panel panel-pad">
        <h3>活跃用户 Top 10</h3>
        <el-table :data="active.topUsers || []" size="small">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="reservationCount" label="预约次数" width="110" />
        </el-table>
      </section>

      <section class="panel panel-pad wide">
        <h3>违约类型</h3>
        <div v-if="violationTypeBars.length" class="bars">
          <div v-for="item in violationTypeBars" :key="item.label" class="bar-row">
            <span>{{ item.label }}</span>
            <div class="bar-track danger"><i :style="{ width: `${item.percent}%` }"></i></div>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
        <el-empty v-else description="暂无违约数据" />
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const loading = ref(false)
const today = new Date()
const sevenDaysAgo = new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000)
const range = ref([sevenDaysAgo.toISOString().slice(0, 10), today.toISOString().slice(0, 10)])
const usage = ref({})
const active = ref({})
const peak = ref({})
const violations = ref({})

function params() {
  return {
    start: range.value?.[0],
    end: range.value?.[1]
  }
}

function makeBars(data = {}) {
  const entries = Object.entries(data)
  const max = Math.max(1, ...entries.map(([, value]) => Number(value)))
  return entries.map(([label, value]) => ({
    label,
    value,
    percent: Math.max(6, Math.round((Number(value) / max) * 100))
  }))
}

const slotBars = computed(() => makeBars(peak.value.timeSlots))
const violationTypeBars = computed(() => makeBars(violations.value.types))

async function loadStats() {
  loading.value = true
  try {
    const [usageRes, activeRes, peakRes, violationRes] = await Promise.all([
      request.get('/statistics/usage', { params: params() }),
      request.get('/statistics/active-users', { params: params() }),
      request.get('/statistics/peak-hours', { params: params() }),
      request.get('/statistics/violations', { params: params() })
    ])
    usage.value = usageRes.data || {}
    active.value = activeRes.data || {}
    peak.value = peakRes.data || {}
    violations.value = violationRes.data || {}
  } finally {
    loading.value = false
  }
}

async function judgeViolations() {
  const res = await request.post('/reservations/violations/judge')
  ElMessage.success(`已判定 ${res.data || 0} 条违约记录`)
  await loadStats()
}

onMounted(loadStats)
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.metric {
  display: grid;
  gap: 8px;
  padding: 18px;
}

.metric span,
.metric small {
  color: #64748b;
}

.metric strong {
  color: #0f172a;
  font-size: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.wide {
  grid-column: 1 / -1;
}

h3 {
  margin: 0 0 14px;
  font-size: 17px;
}

.bars {
  display: grid;
  gap: 14px;
}

.bar-row {
  display: grid;
  grid-template-columns: 100px 1fr 48px;
  align-items: center;
  gap: 12px;
}

.bar-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar-track {
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #e2e8f0;
}

.bar-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #1d4ed8;
}

.bar-track.danger i {
  background: #dc2626;
}

@media (max-width: 880px) {
  .metric-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
