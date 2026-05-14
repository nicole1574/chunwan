<template>
  <el-card>
    <template #header>节目管理</template>
    <el-form :inline="true">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类型"><el-input v-model="form.type" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="form.content" style="width: 280px" /></el-form-item>
      <el-form-item label="年份">
        <el-select v-model="selectedYear" style="width: 130px" placeholder="选择年份">
          <el-option v-for="y in years" :key="y" :label="y" :value="y" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
      <el-form-item>
        <el-upload
          accept=".pdf"
          :show-file-list="false"
          :http-request="uploadPdf"
        >
          <el-button>导入节目单 PDF</el-button>
        </el-upload>
      </el-form-item>
    </el-form>

    <el-table :data="programs" style="margin-top: 12px">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="content" label="内容" />
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

const programs = ref([])
const form = ref({ name: '', type: '', content: '' })
const editingId = ref('')
const years = ref(Array.from({ length: 2026 - 1983 + 1 }, (_, i) => i + 1983))
const selectedYear = ref(null)

const ensureYear = async () => {
  if (!selectedYear.value) return null
  const allYears = (await api.get('/api/years')).data
  let node = allYears.find((x) => x.year === selectedYear.value)
  if (!node) node = (await api.post(`/api/admin/years/${selectedYear.value}`)).data
  return node
}

const load = async () => {
  const { data } = await api.get('/api/programs')
  programs.value = data
}

const save = async () => {
  try {
    if (!form.value.name) return ElMessage.warning('请填写节目名称')
    let program
    if (editingId.value) program = (await api.put(`/api/admin/programs/${editingId.value}`, form.value)).data
    else program = (await api.post('/api/admin/programs', form.value)).data

    const yearNode = await ensureYear()
    if (yearNode) {
      await api.post(`/api/admin/programs/${program.id}/year/${yearNode.id}`)
    }

    reset()
    await load()
  } catch {
    ElMessage.error('保存失败，可能重复')
  }
}

const uploadPdf = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const { data } = await api.post('/api/admin/programs/import/pdf', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(`导入完成：年份${data.yearsCreated}，节目${data.programsCreated}，人员${data.personsCreated}，关系${data.relationsCreated}`)
    await load()
    options.onSuccess?.(data)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'PDF 导入失败')
    options.onError?.(e)
  }
}

const edit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
}

const del = async (id) => {
  await api.delete(`/api/admin/programs/${id}`)
  await load()
}

const reset = () => {
  editingId.value = ''
  selectedYear.value = null
  form.value = { name: '', type: '', content: '' }
}

onMounted(load)
</script>
