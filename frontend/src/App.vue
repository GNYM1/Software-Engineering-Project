<template>
  <div class="app-shell">
    <Login
      v-if="!user"
      @login-success="handleLogin"
    />

    <template v-else>
      <NavBar
        :user="user"
        :active-view="activeView"
        @navigate="activeView = $event"
        @logout="logout"
      />

      <main class="page">
        <Rooms
          v-if="activeView === 'rooms'"
          @select-room="openSeatMap"
        />
        <SeatMap
          v-else-if="activeView === 'seatMap'"
          :room="selectedRoom"
          @back="activeView = 'rooms'"
        />
        <MyReservations v-else-if="activeView === 'myReservations'" />
        <RoomManage v-else-if="activeView === 'roomManage'" />
        <SeatManage v-else-if="activeView === 'seatManage'" />
        <Statistics v-else-if="activeView === 'statistics'" />
      </main>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Login from './views/Login.vue'
import NavBar from './components/NavBar.vue'
import Rooms from './views/student/Rooms.vue'
import SeatMap from './views/student/SeatMap.vue'
import MyReservations from './views/student/MyReservations.vue'
import RoomManage from './views/admin/RoomManage.vue'
import SeatManage from './views/admin/SeatManage.vue'
import Statistics from './views/admin/Statistics.vue'

const storedUser = localStorage.getItem('user')
const user = ref(storedUser ? JSON.parse(storedUser) : null)
const activeView = ref(user.value?.role === 'admin' ? 'roomManage' : 'rooms')
const selectedRoom = ref(null)

function handleLogin(payload) {
  user.value = payload.user
  localStorage.setItem('token', payload.token)
  localStorage.setItem('user', JSON.stringify(payload.user))
  activeView.value = payload.user.role === 'admin' ? 'roomManage' : 'rooms'
}

function openSeatMap(room) {
  selectedRoom.value = room
  activeView.value = 'seatMap'
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  user.value = null
  selectedRoom.value = null
  activeView.value = 'rooms'
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  min-width: 320px;
  color: #1f2937;
  background: #f4f6f8;
  font-family: Inter, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

.app-shell {
  min-height: 100vh;
}

.page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.muted {
  color: #6b7280;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.panel {
  border: 1px solid #d9e1ea;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.panel-pad {
  padding: 18px;
}

@media (max-width: 720px) {
  .page {
    padding: 16px;
  }

  .page-title {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
