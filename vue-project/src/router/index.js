import { createRouter, createWebHistory } from 'vue-router'
import MapView from '@/views/MapView.vue'
import HomeView from '@/views/HomeView.vue'
import MyPageView from '@/views/MyPageView.vue'
import AccountView from '@/views/AccountView.vue'
import ChatView from '@/views/ChatView.vue'
import IndoorMapView from '@/views/ExhibitionHall.vue'
import UserLikeCourseListView from '@/views/UserLikeCourseListView.vue'
import UserLikeCourseDetailView from '@/views/UserLikeCourseDetailView.vue'
import PlaceDetailsView from '@/views/PlaceDetailsView.vue'
import PlaceListView from '@/views/PlaceListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/home',
      name: 'Home',
      component: HomeView,
    },
    {
      path: '/map',
      name: 'Map',
      component: MapView,
    },
    {
      path: '/list',
      name: 'PlaceList',
      component: PlaceListView,
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
      path: '/aitutor',
      name: 'AiTutor',
      component: ChatView,
    },
    {
      path: '/usercourselist',
      name: 'UserLikeCourseList',
      component: UserLikeCourseListView,
    },
    {
      path: '/indoormap',
      name: 'IndoorMapView',
      component: IndoorMapView,
    },
    {
      path: '/coursedetail/:courseId',
      name: 'UserLikeCourseDetail',
      component: UserLikeCourseDetailView,
    },
    // 과학전시
    {
      path: '/exhibition/:id',
      name: 'ExhibitionDetail',
      component: PlaceDetailsView,
    },
    // 답사장소
    {
      path: '/place/:id',
      name: 'PlaceDetail',
      component: PlaceDetailsView, // 같은 뷰 재사용
    },
  ],
})

export default router
