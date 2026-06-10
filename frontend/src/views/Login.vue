<template>
  <div class="login-page">
    <section class="login-info">
      <p class="eyebrow">Study Room Reservation</p>
      <h1>自习室座位预约系统</h1>
      <p>学生可查看开放自习室、按座位图预约、签到签退；管理员可维护自习室和座位，并查看预约统计。</p>
    </section>

    <section class="login-card">
      <el-segmented v-model="mode" :options="modeOptions" class="mode-switch" />
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">
          {{ mode === 'login' ? '登录' : '注册并登录' }}
        </el-button>
      </el-form>

      <div class="demo-tip">
        <span>测试账号：admin / admin123</span>
        <span>学生账号：zhangsan / 123456</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const emit = defineEmits(['login-success'])

const mode = ref('login')
const loading = ref(false)
const modeOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]

const form = reactive({
  username: '',
  password: '',
  phone: ''
})

async function submit() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    if (mode.value === 'register') {
      await request.post('/user/register', {
        username: form.username.trim(),
        password: form.password,
        phone: form.phone.trim()
      })
      ElMessage.success('注册成功')
    }
    const res = await request.post('/user/login', {
      username: form.username.trim(),
      password: form.password
    })
    emit('login-success', res.data)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) 420px;
  align-items: center;
  gap: 56px;
  min-height: 100vh;
  padding: 56px max(28px, calc((100vw - 1120px) / 2));
  background:
    linear-gradient(120deg, rgba(29, 78, 216, 0.9), rgba(8, 145, 178, 0.78)),
    url("https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=1600&q=80") center / cover;
}

.login-info {
  color: #fff;
}

.eyebrow {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.login-info h1 {
  margin: 0 0 18px;
  font-size: 44px;
  line-height: 1.15;
}

.login-info p:last-child {
  max-width: 560px;
  margin: 0;
  font-size: 17px;
  line-height: 1.8;
}

.login-card {
  padding: 28px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.24);
}

.mode-switch {
  width: 100%;
  margin-bottom: 22px;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}

.demo-tip {
  display: grid;
  gap: 6px;
  margin-top: 18px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    gap: 26px;
  }

  .login-info h1 {
    font-size: 34px;
  }
}
</style>
