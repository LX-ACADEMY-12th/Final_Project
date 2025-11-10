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
import ReviewPhotoGridView from '@/views/ReviewPhotoGridView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import UserLikePlaceListView from '@/views/UserLikePlaceListView.vue'
import NoticeView from '@/views/NoticeView.vue'
import NoticeDetailView from '@/views/NoticeDetailView.vue'
import StampView from '@/views/StampView.vue'
import TourView from '@/views/TourView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/home',
      name: 'Home',
      component: HomeView,
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
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
    // 장소/전시의 모든 사진을 모아보는 페이지
    {
      path: '/photos/:targetType/:targetId',
      name: 'AllPhotos',
      component: ReviewPhotoGridView,
      props: true,
    },
    // 사용자 관심 목록(전시, 장소)
    {
      path: '/likePlace',
      name: 'LikePlace',
      component: UserLikePlaceListView,
      props: true,
    },
    {
      path: '/notice',
      name: 'Notice',
      component: NoticeView,
    },
    //  공지사항 상세 페이지
    {
      path: '/notice/:id',
      name: 'NoticeDetail',
      component: NoticeDetailView,
    },
    // 스탬프 화면
    {
      path: '/stamp',
      name: 'stamp',
      component: StampView,
    },
    {
      path: '/virtualTour',
      name: 'virtualTour',
      component: TourView,
    },
  ],
})

export default router
