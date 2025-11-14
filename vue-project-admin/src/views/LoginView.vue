<template>
  <div class="auth-layout">
    <div class="auth-card card shadow-sm border-0">
      <div class="card-body p-4 p-md-5">
        <div class="text-center mb-4">
          <h3 class="fs-4 fw-bold text-primary">교과서 Admin</h3>
          <p class="text-muted mb-0">관리자 로그인이 필요합니다.</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label for="loginId" class="form-label">아이디</label>
            <input
              type="text"
              class="form-control"
              id="loginId"
              placeholder="아이디를 입력하세요"
              v-model="loginId"
              required
            />
          </div>
          <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input
              type="password"
              class="form-control"
              id="password"
              placeholder="••••••••"
              v-model="password"
              required
            />
          </div>
          
          <!-- 에러 메시지 표시 -->
          <div v-if="errorMessage" class="alert alert-danger" role="alert">
            {{ errorMessage }}
          </div>

          <div class="d-grid mb-3">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="isLoading">
              {{ isLoading ? '로그인 중...' : '로그인' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "LoginView",
  data() {
    return {
      loginId: "",
      password: "",
      errorMessage: "",
      isLoading: false
    };
  },
  methods: {
    async handleLogin() {
      this.errorMessage = "";
      this.isLoading = true;

      try {
        // 백엔드 로그인 API 호출
        const response = await axios.post('http://localhost:8080/api/user/login', {
          loginId: this.loginId,
          password: this.password
        });

        const data = response.data;
        console.log('로그인 응답:', data);

        // role이 ADMIN인지 확인
        if (data.role !== 'ADMIN') {
          this.errorMessage = '관리자 권한이 없습니다.';
          this.isLoading = false;
          return;
        }

        // 토큰과 사용자 정보를 localStorage에 저장
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('role', data.role);
        localStorage.setItem('adminName', data.name);
        localStorage.setItem('userId', data.userId);

        console.log('로그인 성공! 관리자 대시보드로 이동합니다.');

        // 관리자 대시보드로 이동 (라우터 경로는 프로젝트에 맞게 수정)
        this.$router.push('/admin');

      } catch (error) {
        console.error('로그인 실패:', error);
        
        if (error.response) {
          // 서버에서 에러 응답이 온 경우
          if (error.response.status === 401) {
            this.errorMessage = '아이디 또는 비밀번호가 올바르지 않습니다.';
          } else {
            this.errorMessage = '로그인 중 오류가 발생했습니다.';
          }
        } else {
          // 네트워크 에러 등
          this.errorMessage = '서버와 연결할 수 없습니다.';
        }
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
/* 폰트 */
@import url('https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/static/woff2/SUIT.css');

:global(body) {
  font-family: 'SUIT', sans-serif;
}

/* --- 1. 레이아웃 --- */
.auth-layout {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* --- 2. 인증 카드 --- */
.auth-card {
  width: 100%;
  max-width: 450px;
  border-radius: 8px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.text-primary {
  color: #34495e !important;
}

.form-label {
  font-weight: 500;
  color: #34495e;
}

.form-control {
  border-radius: 6px;
  border-color: #e2e8f0;
  padding: 0.75rem 1rem;
}

.form-control:focus {
  border-color: #4a7cec;
  box-shadow: 0 0 0 0.25rem rgba(74, 124, 236, 0.25);
}

.btn-primary {
  --bs-btn-bg: #4a7cec;
  --bs-btn-border-color: #4a7cec;
  --bs-btn-hover-bg: #3a65d0;
  --bs-btn-hover-border-color: #3a65d0;
  --bs-btn-active-bg: #3155b1;
  --bs-btn-active-border-color: #3155b1;
  --bs-btn-focus-shadow-rgb: 74, 124, 236;
  padding: 0.75rem;
  font-weight: 600;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.text-muted {
  color: #667085 !important;
}

.alert-danger {
  border-radius: 6px;
  font-size: 0.9rem;
}
</style>
