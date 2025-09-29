import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import PostView from '@/views/PostView.vue'
import MyPageView from '@/views/MyPageView.vue'
import ProfileEditView from '@/views/ProfileEditView.vue'
import UserInfoModify from '@/views/UserInfoModify.vue'
import SettingView from '@/views/SettingView.vue'
import FriendList from '@/views/FriendList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
    },
    {
      path: '/post',
      name: 'post',
      component: PostView,
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
    },
    {
      path: '/profileEdit',
      name: 'profileEdit',
      component: ProfileEditView,
    },
    {
      path: '/userInfoModify',
      name: 'userInfoModify',
      component: UserInfoModify,
    },
    {
      path: '/setting',
      name: 'setting',
      component: SettingView,
    },
    {
      path: '/friendList',
      name: 'friendList',
      component: FriendList,
    },
  ],
})

export default router
