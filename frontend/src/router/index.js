import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AdminPersons from '../views/AdminPersons.vue'
import AdminPrograms from '../views/AdminPrograms.vue'
import AdminRelations from '../views/AdminRelations.vue'
import GraphView from '../views/GraphView.vue'

const routes = [
  { path: '/login', component: LoginView },
  { path: '/admin/persons', component: AdminPersons, meta: { admin: true } },
  { path: '/admin/programs', component: AdminPrograms, meta: { admin: true } },
  { path: '/admin/relations', component: AdminRelations, meta: { admin: true } },
  { path: '/graph', component: GraphView },
  { path: '/', redirect: '/graph' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _, next) => {
  if (to.path === '/login') return next()
  const token = localStorage.getItem('token')
  if (!token) return next('/login')
  const role = localStorage.getItem('role') || ''
  if (to.meta.admin && role !== 'ROLE_ADMIN') return next('/graph')
  next()
})

export default router
