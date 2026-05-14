<template>
  <el-card>
    <template #header>春晚知识图谱</template>
    <div ref="chartRef" style="height: 680px"></div>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import api from '../api'

const chartRef = ref(null)

const loadGraph = async () => {
  const { data } = await api.get('/api/graph')
  const chart = echarts.init(chartRef.value)
  const nodes = data.nodes.map((n) => ({
    id: String(n.vid),
    name: n.props.name || n.props.year || n.labels[0],
    category: n.labels[0],
    value: n.props
  }))
  const links = data.links.map((l) => ({
    source: String(l.source),
    target: String(l.target),
    value: l.type
  }))

  chart.setOption({
    tooltip: { formatter: (p) => (p.dataType === 'edge' ? p.data.value : p.data.name) },
    legend: [{ data: ['Person', 'Program', 'Year', 'Role', 'Category'] }],
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      force: { repulsion: 220, edgeLength: [80, 180] },
      label: { show: true },
      edgeLabel: { show: true, formatter: '{c}' },
      data: nodes,
      links
    }]
  })
}

onMounted(async () => {
  await nextTick()
  await loadGraph()
})
</script>
