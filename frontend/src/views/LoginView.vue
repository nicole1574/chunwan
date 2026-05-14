<template>
  <div class="login-wrap">
    <el-card class="box">
      <template #header>登录</template>
      <el-form @submit.prevent>
        <el-form-item label="用户名"><el-input v-model="username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="password" type="password" /></el-form-item>
        <el-button type="primary" @click="login">登录</el-button>
        <div class="tip">管理员：admin/admin123；用户：user/user123</div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const username = ref('admin')
const password = ref('admin123')

const login = async () => {
  try {
    const { data } = await api.post('/api/auth/login', { username: username.value, password: password.value })
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    router.push('/graph')
  } catch {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login-wrap { display: flex; justify-content: center; margin-top: 120px; }
.box { width: 420px; }
.tip { margin-top: 10px; color: #909399; font-size: 12px; }
</style>
