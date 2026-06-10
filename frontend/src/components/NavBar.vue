<template>
  <header class="navbar">
    <div class="brand">
      <div class="brand-mark">R</div>
      <div>
        <strong>自习室座位预约系统</strong>
        <span>{{ roleLabel }}</span>
      </div>
    </div>

    <nav class="nav-menu">
      <button
        v-for="item in menus"
        :key="item.key"
        :class="{ active: activeView === item.key }"
        type="button"
        @click="$emit('navigate', item.key)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="user-box">
      <span>{{ user.username }}</span>
      <el-button size="small" @click="$emit('logout')">退出</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: { type: Object, required: true },
  activeView: { type: String, required: true }
})

defineEmits(['navigate', 'logout'])

const studentMenus = [
  { key: 'rooms', label: '自习室' },
  { key: 'myReservations', label: '我的预约' }
]

const adminMenus = [
  { key: 'roomManage', label: '自习室管理' },
  { key: 'seatManage', label: '座位管理' },
  { key: 'statistics', label: '数据统计' }
]

const menus = computed(() => props.user.role === 'admin' ? adminMenus : studentMenus)
const roleLabel = computed(() => props.user.role === 'admin' ? '管理员端' : '学生端')
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: minmax(220px, auto) 1fr auto;
  align-items: center;
  gap: 18px;
  min-height: 68px;
  padding: 0 24px;
  border-bottom: 1px solid #d9e1ea;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: #1d4ed8;
  font-weight: 800;
}

.brand strong,
.brand span {
  display: block;
}

.brand strong {
  font-size: 16px;
}

.brand span {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
}

.nav-menu {
  display: flex;
  gap: 6px;
}

.nav-menu button {
  min-width: 86px;
  height: 36px;
  border: 0;
  border-radius: 6px;
  color: #475569;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
}

.nav-menu button.active {
  color: #1d4ed8;
  background: #e8f0ff;
  font-weight: 700;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
}

@media (max-width: 760px) {
  .navbar {
    grid-template-columns: 1fr;
    padding: 12px 16px;
  }

  .nav-menu {
    overflow-x: auto;
  }

  .user-box {
    justify-content: space-between;
  }
}
</style>
