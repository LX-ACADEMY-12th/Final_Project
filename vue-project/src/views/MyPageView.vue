<template>
  <div id="my-page" class="container px-4 py-4">
    <!-- 헤더 영역 -->
    <div class="header-section d-flex align-items-center justify-content-between pb-4 border-bottom">
      <button class="btn p-0 me-3 border-0" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <h2 class="h5 mb-0 fw-bold text-dark">마이페이지</h2>
      <div class="btn p-0 me-3 border-0" style="visibility: hidden;">
        <i class="bi bi-arrow-left fs-4"></i>
      </div>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="content-wrapper py-4">
      <div class=" d-flex align-items-center mb-4">
        <div class="position-relative me-3">
          <!--
          기존 emoji 아이콘을 v-if/v-else로 감싸고,
          user.profileImageUrl이 있으면 <img>를 표시합니다.
          기존 'profile-pic' 클래스(60x60px)와 스타일을 유지합니다.
        -->
          <div
            class="profile-pic rounded-circle d-flex align-items-center justify-content-center bg-body-secondary text-secondary"
            style="overflow: hidden;"> <!-- 👈 이미지가 원을 벗어나지 않도록 overflow: hidden 추가 -->

            <!-- 스토어에 이미지가 있으면 <img> 표시 -->
            <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필"
              style="width: 100%; height: 100%; object-fit: cover;">
            <!-- 스토어에 이미지가 없으면(v-else) 기존 이모지 아이콘 표시 -->
            <i v-else class="bi bi-emoji-smile" style="font-size: 2.5rem;"></i>
          </div>
          <button
            class="btn btn-primary rounded-circle p-0 position-absolute profile-badge d-flex align-items-center justify-content-center"
            @click="goToAccountView">
            <i class="bi bi-gear-fill"></i>
          </button>
        </div>
        <div>
          <div class="fw-bold text-dark">{{ user?.name || '로그인 해주세요' }}</div>
          <div class="small text-dark">{{ user?.email || ' ' }}</div>
        </div>
      </div>


      <button type="button" class="p-4 mb-4 text-white custom-rounded stamp-card w-100 text-start">
        <div class="d-flex align-items-center mb-2">
          <i class="bi bi-crosshair me-2 fs-5"></i>
          <span class="fw-bold">스탬프 투어</span>
        </div>
      </button>
      <button
        class="btn btn-primary w-100 p-3 mb-4 custom-rounded text-start d-flex align-items-center justify-content-between saved-route-btn"
        @click="goToUserLikeCouseList">
        <div class="d-flex align-items-center">
          <i class="bi bi-bookmark-plus-fill me-2 fs-5"></i>
          <span class="fw-bold">저장된 추천 경로</span>
        </div>
        <!-- <i class="bi bi-plus-lg fs-5"></i> -->
      </button>
      <ul class="list-group list-group-flush">
        <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0"
          @click="goToLikePlace()">
          <div class="d-flex align-items-center">
            <i class="bi bi-heart-fill me-3 fs-5 text-heart-red"></i>
            <span>관심 목록</span>
          </div>
          <i class="bi bi-chevron-right text-muted"></i>
        </li>

        <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0"
          @click="showSettingsModal">
          <div class=" d-flex align-items-center">
            <i class="bi bi-gear-fill me-3 fs-5 text-secondary"></i>
            <!-- 🟢 [수정] Pinia 스토어의 isLoggedIn을 사용합니다. -->
            <span>{{ isLoggedIn ? '로그아웃/탈퇴' : '로그인/탈퇴' }}</span>
          </div>
          <i class="bi bi-chevron-right text-muted"></i>
        </li>
        <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0"
          @click="goToNotice">
          <div class="d-flex align-items-center">
            <i class="bi bi-bell-fill me-3 fs-5 text-warning"></i>
            <span>공지사항</span>
          </div>
          <i class="bi bi-chevron-right text-muted"></i>
        </li>
      </ul>
    </div>

    <SettingsModal :show="isSettingsModalOpen" :isLoggedIn="isLoggedIn" @close="isSettingsModalOpen = false"
      @logout="handleLogout" @withdraw="handleWithdraw" @login="goToLoginView" />
  </div>
</template>

<script>
import SettingsModal from '@/components/modal/SettingsModal.vue';
import axios from '@/api/axiosSetup'; // axios import
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import eventBus from '@/utils/eventBus';

export default {
  name: 'MyPageView',
  components: {
    SettingsModal
  },
  setup() {
    const authStore = useAuthStore();

    // storeToRefs를 사용해 user, isLoggedIn을 반응형(reactive)으로 가져옵니다.
    // 이 컴포넌트의 data, computed, methods에서 this.user, this.isLoggedIn으로 접근 가능
    const { user, isLoggedIn } = storeToRefs(authStore);

    // 🟢 authStore.logout() 등 스토어의 액션을 호출하기 위해 authStore 자체도 반환합니다.
    return {
      authStore,
      user,        // template에서 user.name, user.email을 사용하기 위해
      isLoggedIn   // template과 methods에서 로그인 상태를 확인하기 위해
    };
  },
  // 1. 상태(Data) 정의
  data() {
    return {
      isSettingsModalOpen: false,
    }
  },

  // 4. 메서드(Methods)
  methods: {
    // 뒤로가기 함수
    goBack() {
      this.$router.back();
    },

    // ⭐ 계정설정화면으로 이동하는 함수 (로그인 확인 로직 추가) ⭐
    // ⭐️ [수정] 계정설정화면 (CustomConfirm 사용)
    goToAccountView() {
      if (!this.isLoggedIn) {
        // 1. App.vue에 '확인창' 띄우기 요청
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          // 2. '확인' 눌렀을 때 실행할 함수 전달
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
          // '취소'를 누르면 onCancel이 null이므로 그냥 창만 닫힘
        });
        return; // 페이지 이동 방지
      }
      // 로그인이 되어 있다면, 계정설정 페이지로 이동
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
    // ⭐⭐⭐ 수정 4: SettingsModal을 띄우는 전용 메서드를 사용합니다. ⭐⭐⭐
    showSettingsModal() {
      this.isSettingsModalOpen = true;
    },

    // ⭐⭐⭐ 수정 5: SettingsModal에서 @login 이벤트 발생 시 호출되는 메서드입니다. ⭐⭐⭐
    goToLoginView() {
      this.$router.push({ name: 'login' });
      this.isSettingsModalOpen = false; // 모달을 닫습니다.
    },

    // 관심 목록 페이지로 이동
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

    // 저장된 추천 코스로 이동하는 함수
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
    // 공지사항 페이지로 이동
    goToNotice() {
      this.$router.push({ name: 'Notice' });
    },
    // ⭐ 로그아웃 로직 ⭐
    handleLogout() {
      console.log('MyPageView에서 로그아웃 로직 실행');

      // 🟢 1. Pinia 스토어의 logout 액션 호출 (setup에서 반환된 authStore 사용)
      this.authStore.logout();

      // 2. 모달 닫기
      this.isSettingsModalOpen = false;

      // 3. 메인 페이지로 이동 (replace를 사용해 뒤로가기 막기)
      this.$router.replace({ name: 'Home' }); // 'Home'은 router/index.js에 정의된 이름
      console.log('로그아웃 완료 및 페이지 이동');
    },

    // ⭐ 회원탈퇴 로직 (백엔드 통신 추가) ⭐
    async handleWithdraw() {
      // 1. 최종 확인 (실제로는 비밀번호 재확인 모달을 띄워야 합니다.)
      if (!confirm('정말로 회원 탈퇴하시겠습니까? 탈퇴하시면 모든 정보가 삭제되며 복구할 수 없습니다.')) {
        return;
      }

      // 2. 🟢 로그인 상태 확인 (Pinia 스토어 사용)
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

        // 4. 응답 처리: HTTP 204 No Content (삭제 성공)
        if (response.status === 204) {
          eventBus.emit('show-global-alert', {
            message: '회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다',
            type: 'error'
          });

          // 🟢 5. 탈퇴 성공 후 로그아웃 처리 (Pinia 스토어)
          this.handleLogout();
        }
      } catch (error) {
        // 🟢 (axiosSetup.js가 401(토큰만료)을 자동으로 처리 시도)
        console.error('회원 탈퇴 실패:', error);
        if (error.response && error.response.data) {
          eventBus.emit('show-global-alert', {
            message: '회원 탈퇴 실패: ' + error.response.data,
            type: 'error'
          });
        } else {
          eventBus.emit('show-global-alert', {
            message: '회원 탈퇴 중 알 수 없는 오류가 발생했습니다.: ',
            type: 'error'
          });
        }
        this.isSettingsModalOpen = false;
      }
    }
  }
}
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-style: normal;
}

#my-page {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  background-color: #ffffff;
  min-height: 100vh;
  padding: 0;
}

.header-section {
  position: sticky;
  top: 0;
  background-color: #ffffff;
  z-index: 100;
  padding-top: 16px;
}

.content-wrapper {
  position: relative;
}

.profile-pic {
  width: 60px;
  height: 60px;
}

.profile-badge {
  width: 24px;
  height: 24px;
  bottom: -5px;
  right: -5px;
  font-size: 14px;
  background-color: #4A7CEC;
  border-color: #4A7CEC;
}

.profile-badge:active {
  filter: brightness(90%);
}

.stamp-card {
  background-color: #8fa4c3;
  border: none;
  transition: filter 0.2s ease-in-out;
}

.stamp-card:active {
  filter: brightness(90%);
}

.saved-route-btn {
  background-color: #4A7CEC;
  border-color: #4A7CEC;
  transition: filter 0.2s ease-in-out;
  margin-bottom: 32px;
  /* 추가된 간격 */
}

.saved-route-btn:active {
  filter: brightness(90%);
}

.custom-rounded {
  border-radius: 0.75rem !important;
}

.text-heart-red {
  color: #ea4335;
}

.list-group-item-action {
  cursor: pointer;
}

.list-group-item-action span {
  color: #000000;
}

.list-group-item-action:hover {
  background-color: #ffffff;
}

.list-group-item-action:active {
  background-color: #f8f9fa;
}
</style>
