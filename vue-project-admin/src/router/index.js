import { createRouter, createWebHistory } from 'vue-router'
// 1. 기존 HomeView 대신 AdminPanel.vue를 import 합니다.
import AdminLayout from '@/views/AdminLayout.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
   
    {
      // [신규] 로그인 페이지
      path: '/',
      name: 'Login',
      component: LoginView
    },
    { 
      path: '/admin',
      name: 'admin', 
      component: AdminLayout,
    },
    {
      // [신규] 회원가입 페이지
      path: '/register',
      name: 'Register',
      component: RegisterView
    }
  ],
})

export default router