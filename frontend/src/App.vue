<template>
  <el-container style="height: 100vh">
    <el-header class="header">
      <div>春晚知识图谱管理系统</div>
      <div>
        <el-button v-if="token" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" v-if="token">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/graph">知识图谱</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/persons">演职人员管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/programs">节目管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/relations">关系管理</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const token = ref('')
const isAdmin = ref(false)

const syncAuthState = () => {
  token.value = localStorage.getItem('token') || ''
  isAdmin.value = localStorage.getItem('role') === 'ROLE_ADMIN'
}

onMounted(syncAuthState)
watch(() => route.fullPath, syncAuthState)

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  syncAuthState()
  router.push('/login')
}
</script>

<style>
body { margin: 0; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
}
</style>
