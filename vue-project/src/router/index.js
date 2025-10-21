import { createRouter, createWebHistory } from 'vue-router'
import MapView from '@/views/MapView.vue'
import HomeView from '@/views/HomeView.vue'
import MyPageView from '@/views/MyPageView.vue'
import AccountView from '@/views/AccountView.vue'
import ChatView from '@/views/ChatView.vue'
import UserLikeCourse from '@/views/UserLikeCourse.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/map',
      name: 'Map',
      component: MapView,
    },
    {
      path: '/home',
      name: 'Home',
      component: HomeView,
    },
    {
      path: '/mypage',
      name: 'Mypage',
      component: MyPageView,
    },
    {
      path: '/account',
      name: 'AccountView',
      component: AccountView,
    },
    {
      path: '/chat',
      name: 'ChatView',
      component: ChatView,
    },
    {
      path: '/userLikeCourse',
      name: 'UserLikeCourse',
      component: UserLikeCourse,
    },
  ],
})

export default router
