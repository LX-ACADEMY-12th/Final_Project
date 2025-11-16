<template>
  <div class="mypage-root d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">

    <div class="mypage-header d-flex justify-content-between align-items-center">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold"
        style="flex: 1; text-align: center; font-size: 1rem; font-weight: 700; color: #111827;">
        마이페이지
      </div>
      <div class="header-right d-flex justify-content-end" style="flex: 1;"></div>
    </div>

    <div class="mypage-scroll flex-grow-1">

      <section class="mypage-section mypage-section--profile">
        <div class="mypage-profile-card">
          <div class="profile-avatar-wrapper">
            <div class="profile-avatar">
              <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필" class="profile-image" />
              <div v-else class="profile-avatar-fallback">
                <i class="bi bi-person-fill" style="font-size: 2rem;"></i>
              </div>
            </div>
            <button class="profile-settings-badge" @click="goToAccountView">
              <i class="bi bi-gear-fill"></i>
            </button>
          </div>
          <div class="profile-info">
            <div class="profile-name">{{ user?.name || '로그인 해주세요' }}</div>
            <div class="profile-greeting">{{ user?.email || ' ' }}</div>
          </div>
        </div>
      </section>

      <section class="mypage-section">
        <div class="mypage-section-header">
          <h5 class="mypage-section-title">나의 과학 여행</h5>
        </div>
        <div class="mypage-card-grid">

          <button type="button" class="mypage-card mypage-card--stamp" @click="goToStampPage">
            <div class="card-icon-wrap">
              <i class="bi bi-crosshair"></i>
            </div>
            <div class="card-text-wrap">
              <div class="card-title">스탬프 투어</div>
              <div class="card-sub">
                방문 인증 스탬프로<br> 과학 여행 기록하기
              </div>
            </div>
          </button>

          <button type="button" class="mypage-card mypage-card--route" @click="goToUserLikeCouseList">
            <div class="card-icon-wrap">
              <i class="bi bi-bookmark-plus-fill"></i>
            </div>
            <div class="card-text-wrap">
              <div class="card-title">저장된 추천 경로</div>
              <div class="card-sub">
                마음에 든 과학 체험 코스를 <br>한눈에 보기
              </div>
            </div>
          </button>
        </div>
      </section>

      <section class="mypage-section">
        <div class="mypage-section-header">
          <h5 class="mypage-section-title">내 활동</h5>
        </div>
        <ul class="mypage-list">
          <li class="mypage-list-item" @click="goToLikePlace">
            <div class="list-left">
              <span class="list-icon heart">
                <i class="bi bi-heart-fill"></i>
              </span>
              <span class="list-text-main">관심 목록</span>
            </div>
            <i class="bi bi-chevron-right list-chevron"></i>
          </li>
          <li class="mypage-list-item" @click="goToNotice">
            <div class="list-left">
              <span class="list-icon bell">
                <i class="bi bi-bell-fill"></i>
              </span>
              <span class="list-text-main">공지사항</span>
            </div>
            <i class="bi bi-chevron-right list-chevron"></i>
          </li>
        </ul>
      </section>
      <section class="mypage-section">
        <div class="mypage-section-header">
          <h5 class="mypage-section-title">계정</h5>
        </div>
        <ul class="mypage-list">
          <li class="mypage-list-item" @click="showSettingsModal">
            <div class="list-left">
              <span class="list-icon gear">
                <i class="bi bi-gear-fill"></i>
              </span>
              <span class="list-text-main">
                {{ isLoggedIn ? '로그아웃 / 탈퇴' : '로그인 / 탈퇴' }}
              </span>
            </div>
            <i class="bi bi-chevron-right list-chevron"></i>
          </li>
        </ul>
      </section>
    </div>

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" style="flex-shrink: 0;" />

    <SettingsModal :show="isSettingsModalOpen" :isLoggedIn="isLoggedIn" @close="isSettingsModalOpen = false"
      @logout="handleLogout" @withdraw="handleWithdraw" @login="goToLoginView" />
  </div>
</template>

<script>
// (Script 태그 내용은 변경 사항 없음)
import SettingsModal from '@/components/modal/SettingsModal.vue';
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import eventBus from '@/utils/eventBus';
import BottomNavbar from '@/components/BottomNavbar.vue';

export default {
  name: 'MyPageView',
  components: {
    SettingsModal,
    BottomNavbar
  },
  setup() {
    const authStore = useAuthStore();
    const { user, isLoggedIn } = storeToRefs(authStore);
    return {
      authStore,
      user,
      isLoggedIn
    };
  },
  data() {
    return {
      isSettingsModalOpen: false,
      selectedNavItem: '마이페이지'
    }
  },
  methods: {
    // 🟢 [수정] goBack() 메서드 복구
    goBack() {
      this.$router.back();
    },

    handleNavigation(navItemName) {
      console.log(navItemName, '클릭됨.');
      this.selectedNavItem = navItemName;
      if (navItemName === '코스관리' && !this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
      if (navItemName === '홈') {
        this.$router.push('/home');
      } else if (navItemName === '목록') {
        this.$router.push('/list');
      } else if (navItemName === '지도') {
        this.$router.push('/map');
      } else if (navItemName === '코스관리') {
        this.$router.push('/usercourselist');
      } else if (navItemName === '마이페이지') {
        // 현재 페이지
      }
    },
    // ( ... 나머지 메서드들은 변경 없음 ... )
    goToAccountView() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
      this.$router.push({ name: 'AccountView' })
    },
    goToStampPage() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
      this.$router.push({ name: 'stamp' })
    },
    showSettingsModal() {
      this.isSettingsModalOpen = true;
    },
    goToLoginView() {
      this.$router.push({ name: 'login' });
      this.isSettingsModalOpen = false;
    },
    goToLikePlace() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
      this.$router.push({ name: 'LikePlace' })
    },
    goToUserLikeCouseList() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
      this.$router.push({ name: 'UserLikeCourseList' })
    },
    goToNotice() {
      this.$router.push({ name: 'Notice' });
    },
    handleLogout() {
      console.log('MyPageView에서 로그아웃 로직 실행');
      this.authStore.logout();
      this.isSettingsModalOpen = false;
      this.$router.replace({ name: 'Home' });
      console.log('로그아웃 완료 및 페이지 이동');
    },
    async handleWithdraw() {
      if (!confirm('정말로 회원 탈퇴하시겠습니까? 탈퇴하시면 모든 정보가 삭제되며 복구할 수 없습니다.')) {
        return;
      }
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        this.isSettingsModalOpen = false;
        this.$router.push({ name: 'login' });
        return;
      }
      try {
        const response = await axios.delete(`/user/withdraw`);
        if (response.status === 204) {
          eventBus.emit('show-global-alert', {
            message: '회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다',
            type: 'error'
          });
          this.handleLogout();
        }
      } catch (error) {
        console.error('회원 탈퇴 실패:', error);
      }
    }
  }
}
</script>

<style scoped>
/* ( ... @font-face ... ) */
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-style: normal;
}

/* ( ... .mypage-root, .mypage-header ... 변경 없음 ) */
.mypage-root {
  width: 100%;
  margin: 0 auto;
  height: 100%;
  overflow: hidden;
}

.mypage-header {
  display: flex;
  height: 63px;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1020;
  background-color: #FFFFFF;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.mypage-header-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #111827;
}

/* ( ... .mypage-scroll, .mypage-section ... 변경 없음 ) */
.mypage-scroll {
  flex: 1 1 auto;
  overflow-y: auto;
  min-height: 0;
  padding: 12px 16px 80px;
  -webkit-overflow-scrolling: touch;
  background-color: #FFFFFF;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.mypage-scroll::-webkit-scrollbar {
  display: none;
}

.mypage-section {
  margin-bottom: 24px;
}

.mypage-section--profile {
  margin-top: 4px;
}

.mypage-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.mypage-section-title {
  margin: 0;
  color: #111827;
  font-weight: 700;
  font-size: 0.95rem;
}


/* 프로필 카드 (흰색 배경 + 그림자 스타일)
*/
.mypage-profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #FFFFFF;
  color: #111827;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
  padding: 16px;
}

.profile-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.profile-avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  background-color: #F3F4F6;
  border-radius: 999px;
  overflow: hidden;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  font-size: 1.5rem;
}

/* ( ... 뱃지 스타일 변경 없음 ... ) */
.profile-settings-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background-color: #F9FAFB;
  border: 1px solid #E5E7EB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  color: #4B5563;
  position: absolute;
  bottom: 0;
  right: 0;
  cursor: pointer;
  padding: 0;
}

.profile-settings-badge i {
  font-size: 14px;
}

.profile-settings-badge:active {
  filter: brightness(0.95);
}

.profile-info {
  flex: 1 1 auto;
}

.profile-greeting {
  font-size: 0.85rem;
  color: #4B5563;
  opacity: 1;
}

.profile-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
}


/* ( ... '나의 과학 여행' 카드 스타일 ... 변경 없음 ) */
.mypage-card-grid {
  display: grid;
  grid-template-columns: 1fr;

  gap: 12px;
}

/* 🟢 [반응형 코드]
  420px 이상일 때 2열로 변경
*/
@media (min-width: 420px) {
  .mypage-card-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.mypage-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  padding: 14px 12px;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  background-color: #F9FAFB;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.15s ease, background-color 0.12s ease;
  min-height: 96px;
}

.mypage-card--stamp {
  background: #4A7CEC;
  color: #FFFFFF;
  border-color: rgba(37, 99, 235, 0.4);
}

.mypage-card--route {
  background-color: #FFFFFF;
}

.mypage-card:active {
  transform: translateY(1px);
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.15);
}

.card-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #4A7CEC;
  color: white;
  flex-shrink: 0;
}

.mypage-card--stamp .card-icon-wrap {
  background-color: rgba(15, 23, 42, 0.18);
}

.card-icon-wrap i {
  font-size: 1.1rem;
}

.card-text-wrap {
  margin-top: 0;
  text-align: center;
}

.card-title {
  font-size: 0.95rem;
  font-weight: 700;
}

.card-sub {
  margin-top: 2px;
  font-size: 0.8rem;
  color: #6B7280;
}

.mypage-card--stamp .card-sub {
  color: rgba(243, 244, 246, 0.9);
}

.card-chevron {
  position: absolute;
  bottom: 10px;
  right: 10px;
  font-size: 0.9rem;
  color: rgba(31, 41, 55, 0.6);
}

.mypage-card--stamp .card-chevron {
  color: rgba(249, 250, 251, 0.9);
}


/* ( ... .mypage-list (리스트 스타일) ... 변경 없음 ) */
.mypage-list {
  list-style: none;
  padding: 0;
  margin: 0;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  overflow: hidden;
  background-color: #FFFFFF;
}

.mypage-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  cursor: pointer;
  transition: background-color 0.12s ease;
}

.mypage-list-item+.mypage-list-item {
  border-top: 1px solid rgba(229, 231, 235, 0.9);
}

.mypage-list-item:hover {
  background-color: #F9FAFB;
}

.mypage-list-item:active {
  background-color: #F3F4F6;
}

.list-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-icon {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
}

.list-icon.heart {
  color: #EF4444;
  background-color: #FEE2E2;
}

.list-icon.bell {
  color: #F59E0B;
  background-color: #FEF3C7;
}

.list-icon.gear {
  color: #4B5563;
  background-color: #E5E7EB;
}

.list-text-main {
  font-size: 0.9rem;
  color: #111827;
}

.list-chevron {
  font-size: 0.9rem;
  color: #9CA3AF;
}
</style>
