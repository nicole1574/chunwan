<template>
  <el-card>
    <template #header>演职人员管理</template>
    <el-form :inline="true">
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="性别"><el-input v-model="form.gender" /></el-form-item>
      <el-form-item label="民族"><el-input v-model="form.ethnicity" /></el-form-item>
      <el-form-item label="地区"><el-input v-model="form.region" /></el-form-item>
      <el-form-item label="个人/团体">
        <el-select v-model="form.type" style="width: 120px">
          <el-option label="个人" value="个人" />
          <el-option label="团体" value="团体" />
        </el-select>
      </el-form-item>
      <el-form-item label="境外"><el-switch v-model="form.overseas" /></el-form-item>
      <el-form-item label="百科链接"><el-input v-model="form.baikeUrl" style="width: 260px" /></el-form-item>
      <el-form-item label="简介"><el-input v-model="form.bio" style="width: 300px" /></el-form-item>
      <el-form-item>
        <el-button @click="crawl">百科抓取</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="persons" style="margin-top: 12px">
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" />
      <el-table-column prop="ethnicity" label="民族" />
      <el-table-column prop="region" label="地区" />
      <el-table-column prop="type" label="类型" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button text @click="edit(scope.row)">编辑</el-button>
          <el-button text type="danger" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const persons = ref([])
const form = ref({ name: '', gender: '', ethnicity: '', region: '', type: '个人', overseas: false, baikeUrl: '', bio: '' })
const editingId = ref('')

const load = async () => {
  const { data } = await api.get('/api/persons')
  persons.value = data
}

const save = async () => {
  try {
    if (!form.value.name) return ElMessage.warning('请填写姓名')
    if (editingId.value) await api.put(`/api/admin/persons/${editingId.value}`, form.value)
    else await api.post('/api/admin/persons', form.value)
    reset()
    await load()
  } catch {
    ElMessage.error('保存失败，可能重复')
  }
}

const crawl = async () => {
  if (!form.value.name) return ElMessage.warning('请先输入姓名')
  const { data } = await api.post('/api/admin/persons/crawl', null, { params: { name: form.value.name } })
  form.value = { ...form.value, ...data }
}

const edit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
}

const del = async (id) => {
  await api.delete(`/api/admin/persons/${id}`)
  await load()
}

const reset = () => {
  editingId.value = ''
  form.value = { name: '', gender: '', ethnicity: '', region: '', type: '个人', overseas: false, baikeUrl: '', bio: '' }
}

onMounted(load)
</script>
