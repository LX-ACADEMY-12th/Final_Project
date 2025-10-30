<template>
  <div id="my-page" class="container px-4 py-4">

    <div class="d-flex align-items-center justify-content-between pb-4 border-bottom">
      <button class="btn p-0 me-3 border-0" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <h2 class="h5 mb-0 fw-bold text-dark">마이페이지</h2>
      <div class="btn p-0 me-3 border-0" style="visibility: hidden;">
        <i class="bi bi-arrow-left fs-4"></i>
      </div>
    </div>

    <div class="d-flex align-items-center mt-4 mb-4">
      <div class="position-relative me-3">
        <div
          class="profile-pic rounded-circle d-flex align-items-center justify-content-center bg-body-secondary text-secondary">
          <i class="bi bi-emoji-smile" style="font-size: 2.5rem;"></i>
        </div>
        <button
          class="btn btn-primary rounded-circle p-0 position-absolute profile-badge d-flex align-items-center justify-content-center"
          @click="goToAccountView">
          <i class="bi bi-gear-fill"></i>
        </button>
      </div>
      <div>
        <div class="fw-bold text-dark">{{ user.name || '로그인 해주세요' }}</div> 
        <div class="small text-dark">{{ user.email || ' ' }}</div> 
      </div>
    </div>

    <button type="button" class="p-4 mb-4 text-white custom-rounded stamp-card w-100 text-start">
      <div class="d-flex align-items-center mb-2">
        <i class="bi bi-crosshair me-2 fs-5"></i>
        <span class="fw-bold">스탬프 투어</span>
      </div>
      <div class="display-4 fw-bolder">12 / 20</div>
    </button>
    <button
      class="btn btn-primary w-100 p-3 mb-4 custom-rounded text-start d-flex align-items-center justify-content-between saved-route-btn">
      <div class="d-flex align-items-center">
        <i class="bi bi-bookmark-plus-fill me-2 fs-5"></i>
        <span class="fw-bold">저장된 추천 경로</span>
      </div>
      <i class="bi bi-plus-lg fs-5" @click="goToUserLikeCouseList"></i>
    </button>
    <ul class="list-group list-group-flush">
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-heart-fill me-3 fs-5 text-heart-red"></i>
          <span>관심 목록</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
      <!-- <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-heart-fill me-3 fs-5 text-heart-red"></i>
          <span>관심 장소 목록</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li> -->
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0"
        @click=" isSettingsModalOpen = true">
        <div class=" d-flex align-items-center">
          <i class="bi bi-gear-fill me-3 fs-5 text-secondary"></i>
          <span>로그아웃/탈퇴</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-bell-fill me-3 fs-5 text-warning"></i>
          <span>공지사항</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
    </ul>

    <SettingsModal :show="isSettingsModalOpen" @close="isSettingsModalOpen = false" @logout="handleLogout"
      @withdraw="handleWithdraw" />
  </div>
</template>

<script>
import SettingsModal from '@/components/modal/SettingsModal.vue';
import axios from 'axios'; // axios import

// API 기본 경로 설정
const API_BASE_URL = 'http://localhost:8080/api/user';

export default {
  name: 'MyPageView',
  components: {
    SettingsModal
  },
  
  // 1. 상태(Data) 정의
  data() {
    return {
      isSettingsModalOpen: false,
      // ⭐ 사용자 정보를 담을 객체 추가 ⭐
      user: {
        name: '',
        email: '',
        loginId: '',
        // 다른 필드도 필요하다면 여기에 추가
      }
    }
  },

  // 2. 컴포넌트 생성 후 사용자 정보를 불러오는 로직
  created() {
    // 컴포넌트가 생성된 직후, 사용자 정보를 가져오는 메서드를 호출
    this.fetchUserInfo();
  },

  // 3. 메서드(Methods)
  methods: {
    // ⭐ 사용자 정보를 가져오는 비동기 메서드 ⭐
    async fetchUserInfo() {
      // 1. 로컬 또는 세션 스토리지에서 인증 토큰을 가져옵니다.
      const token = localStorage.getItem('user-auth-token') || sessionStorage.getItem('user-auth-token');

      if (!token) {
        console.log('토큰 없음. 로그인 페이지로 리다이렉션 필요.');
        // 토큰이 없을 때 이동하는 화면
        // this.$router.replace({ name: 'Mypage' }); 
        return;
      }

      try {
        // 2. 백엔드 API 호출: /api/user/info (사용자 정보를 가져오는 API라고 가정)
        // Spring Security 등을 사용할 경우, 토큰을 HTTP Authorization 헤더에 담아 전송합니다.
        const response = await axios.get(`${API_BASE_URL}/info`, {
          headers: {
            'Authorization': `Bearer ${token}` // JWT 토큰 형식으로 전송
          }
        });

        // 3. 성공적으로 사용자 정보를 가져왔다면 data에 저장합니다.
        const userInfo = response.data;
        this.user.name = userInfo.name;
        this.user.email = userInfo.email;
        this.user.loginId = userInfo.loginId;
        console.log('사용자 정보 로드 성공:', userInfo);

      } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        // 토큰 만료 등 인증 실패 시, 토큰을 지우고 이동할 화면
        localStorage.removeItem('user-auth-token');
        sessionStorage.removeItem('user-auth-token');
        // this.$router.replace({ name: 'Mypage' }); 
      }
    },

    // 뒤로가기 함수 
    goBack() {
      this.$router.back();
    },
    // // 계정설정화면으로 이동하는 함수 
    // goToAccountView() {
    //   this.$router.push({ name: 'AccountView' })
    // },

    // ⭐ 계정설정화면으로 이동하는 함수 (로그인 확인 로직 추가) ⭐
    goToAccountView() {
      // this.user.loginId가 비어있다면, 로그인이 되지 않은 상태로 간주합니다.
      if (!this.user.loginId) {
        // 1. 알림 메시지 띄우기
        alert('로그인이 필요한 서비스입니다. 로그인 페이지로 이동합니다.');
        // 2. 로그인 페이지로 이동
        this.$router.push({ name: 'login' });
        return;
      }

      // 로그인이 되어 있다면, 계정설정 페이지로 이동
      this.$router.push({ name: 'AccountView' })
    },

    // 저장된 추천 코스로 이동하는 함수 
    goToUserLikeCouseList() {
      this.$router.push({ name: 'UserLikeCourseList' })
    },

    // ⭐ 로그아웃 로직 ⭐
    handleLogout() {
      console.log('MyPageView에서 로그아웃 로직 실행');

      // 1. 저장된 토큰 삭제
      localStorage.removeItem('user-auth-token');
      sessionStorage.removeItem('user-auth-token');

      // 2. 사용자 정보 초기화
      this.user.name = '';
      this.user.email = '';

      // 3. 모달 닫기
      this.isSettingsModalOpen = false;

      // 4. 로그인 페이지 또는 메인 페이지로 이동
      this.$router.replace({ name: 'Home' }); 
      console.log('로그아웃 완료 및 페이지 이동');
    },

    // ⭐ 회원탈퇴 로직 (백엔드 통신 추가) ⭐
    async handleWithdraw() {
      // 1. 최종 확인 (실제로는 비밀번호 재확인 모달을 띄워야 합니다.)
      if (!confirm('정말로 회원 탈퇴하시겠습니까? 탈퇴하시면 모든 정보가 삭제되며 복구할 수 없습니다.')) {
        return;
      }

      // 2. 인증 토큰 가져오기 
      const token = localStorage.getItem('user-auth-token') || sessionStorage.getItem('user-auth-token');
      if (!token) {
        alert('로그인 상태를 확인할 수 없습니다.');
        this.handleLogout();
        return;
      }

      try {
        // 3. 백엔드 API 호출: DELETE /api/user/withdraw
        const response = await axios.delete(`${API_BASE_URL}/withdraw`, {
          headers: {
            'Authorization': `Bearer ${token}` // 인증된 토큰을 헤더에 담아 전송
          }
        });

        // 4. 응답 처리: HTTP 204 No Content (삭제 성공)
        if (response.status === 204) {
          alert('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
          // 탈퇴 성공 후 로그아웃 처리
          this.handleLogout(); 
        }
      } catch (error) {
        console.error('회원 탈퇴 실패:', error);
        if (error.response && error.response.data) {
          alert('회원 탈퇴 실패: ' + error.response.data);
        } else {
          alert('회원 탈퇴 중 알 수 없는 오류가 발생했습니다.');
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
  src: url('@/assets/fonts/SUIT-Variable.woff2') format('woff2-variations');
  font-weight: 100 900;
  font-style: normal;
}

#my-page {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  background-color: #ffffff;
  min-height: 100vh;
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
  background-color: #0d6efd;
  border-color: #0d6efd;
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
  background-color: #3674B5;
  border-color: #3674B5;
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
