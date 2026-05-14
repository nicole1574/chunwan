<template>
  <el-card>
    <template #header>关系管理</template>
    <el-form :inline="true">
      <el-form-item label="演职人员">
        <el-autocomplete v-model="personName" :fetch-suggestions="queryPerson" placeholder="输入姓名" @select="onPersonSelect" />
      </el-form-item>
      <el-form-item label="节目">
        <el-autocomplete v-model="programName" :fetch-suggestions="queryProgram" placeholder="输入节目" @select="onProgramSelect" />
      </el-form-item>
      <el-form-item label="关系">
        <el-select v-model="relationType" style="width: 120px">
          <el-option label="参演" value="参演" />
          <el-option label="导演" value="导演" />
          <el-option label="主持" value="主持" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份">
        <el-select v-model="roleId" clearable style="width: 160px">
          <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="categoryId" clearable style="width: 160px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="createRelation">新增关系</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-form :inline="true">
      <el-form-item label="新增身份"><el-input v-model="newRole" /></el-form-item>
      <el-form-item><el-button @click="createRole">添加</el-button></el-form-item>
      <el-form-item label="新增关系分类"><el-input v-model="newCategory" /></el-form-item>
      <el-form-item><el-button @click="createCategory">添加</el-button></el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const personName = ref('')
const programName = ref('')
const personId = ref('')
const programId = ref('')
const relationType = ref('参演')
const roleId = ref('')
const categoryId = ref('')
const roles = ref([])
const categories = ref([])
const newRole = ref('')
const newCategory = ref('')

const queryPerson = async (query, cb) => {
  const { data } = await api.get('/api/suggest/persons', { params: { q: query } })
  cb(data.map((x) => ({ value: x.name, id: x.id })))
}
const queryProgram = async (query, cb) => {
  const { data } = await api.get('/api/suggest/programs', { params: { q: query } })
  cb(data.map((x) => ({ value: x.name, id: x.id })))
}
const onPersonSelect = (item) => (personId.value = item.id)
const onProgramSelect = (item) => (programId.value = item.id)

const loadMeta = async () => {
  roles.value = (await api.get('/api/roles')).data
  categories.value = (await api.get('/api/categories')).data
}

const createRelation = async () => {
  if (!personId.value || !programId.value) return ElMessage.warning('请选择演职人员与节目')
  await api.post('/api/admin/relations', {
    personId: personId.value,
    programId: programId.value,
    relationType: relationType.value,
    roleId: roleId.value || null,
    categoryId: categoryId.value || null
  })
  ElMessage.success('关系创建成功')
}

const createRole = async () => {
  if (!newRole.value) return
  await api.post('/api/admin/roles', { name: newRole.value })
  newRole.value = ''
  await loadMeta()
}

const createCategory = async () => {
  if (!newCategory.value) return
  await api.post('/api/admin/categories', { name: newCategory.value })
  newCategory.value = ''
  await loadMeta()
}

onMounted(loadMeta)
</script>
