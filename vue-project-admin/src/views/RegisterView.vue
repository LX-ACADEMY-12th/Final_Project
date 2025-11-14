<template>
  <div class="auth-layout">
    <div class="auth-card card shadow-sm border-0">
      <div class="card-body p-4 p-md-5">
        <div class="text-center mb-4">
          <h3 class="fs-4 fw-bold text-primary">교과서 Admin</h3>
          <p class="text-muted mb-0">새로운 관리자 계정을 등록합니다.</p>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="mb-3">
            <label for="email" class="form-label">이메일 주소</label>
            <input
              type="email"
              class="form-control"
              id="email"
              placeholder="admin@example.com"
              v-model="email"
              required
            />
          </div>

          <!-- [추가] 전화번호 입력란 -->
          <div class="mb-3">
            <label for="phoneNumber" class="form-label">전화번호</label>
            <input
              type="tel"
              class="form-control"
              id="phoneNumber"
              placeholder="010-1234-5678"
              v-model="phoneNumber"
              @input="formatPhoneNumber"
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
          <div class="mb-3">
            <label for="confirmPassword" class="form-label">비밀번호 확인</label>
            <input
              type="password"
              class="form-control"
              id="confirmPassword"
              placeholder="••••••••"
              v-model="confirmPassword"
              required
            />
          </div>
          <div class="d-grid mb-3">
            <button type="submit" class="btn btn-primary btn-lg">
              회원가입
            </button>
          </div>
          <div class="text-center">
            <span class="text-muted">이미 계정이 있으신가요?</span>
            <router-link to="/login" class="ms-1">로그인</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "RegisterView",
  data() {
    return {
      email: "",
      phoneNumber: "", // [추가] phoneNumber 데이터
      password: "",
      confirmPassword: "",
    };
  },
  methods: {
    // [추가] 전화번호 자동 하이픈 (-) 기능
    formatPhoneNumber() {
      let rawPhoneNumber = this.phoneNumber;

      // 숫자만 남김
      let cleanedNumber = rawPhoneNumber.replace(/[^0-9]/g, "");

      // 11자리 초과 방지
      if (cleanedNumber.length > 11) {
        cleanedNumber = cleanedNumber.substring(0, 11);
      }

      let formattedNumber = "";

      // 하이픈(-) 추가 로직 (010-1234-5678)
      if (cleanedNumber.length < 4) {
        formattedNumber = cleanedNumber;
      } else if (cleanedNumber.length < 8) {
        formattedNumber =
          cleanedNumber.substring(0, 3) + "-" + cleanedNumber.substring(3);
      } else {
        formattedNumber =
          cleanedNumber.substring(0, 3) +
          "-" +
          cleanedNumber.substring(3, 7) +
          "-" +
          cleanedNumber.substring(7, 11);
      }

      // data 속성 업데이트
      this.phoneNumber = formattedNumber;
    },

    handleRegister() {
      if (this.password !== this.confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }
      // 실제 회원가입 로직을 여기에 구현합니다.
      console.log(
        "Register attempt:",
        this.email,
        this.phoneNumber, // [추가] 콘솔 로그에 phoneNumber 포함
        this.password
      );
      // 회원가입 성공 시 로그인 페이지로 이동
      // this.$router.push('/login');
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
  background-color: #f8f9fa; /* AdminLayout의 .main-content 배경색 */
}

/* --- 2. 인증 카드 --- */
.auth-card {
  width: 100%;
  max-width: 450px;
  border-radius: 8px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); /* 그림자 강조 */
}

.text-primary {
  color: #34495e !important; /* AdminLayout의 .sidebar-header h3 색상 */
}

.form-label {
  font-weight: 500;
  color: #34495e;
}

.form-control {
  border-radius: 6px;
  border-color: #e2e8f0;
  padding: 0.75rem 1rem; /* 폼 크기 키움 */
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
  padding: 0.75rem; /* 버튼 크기 키움 */
  font-weight: 600;
}

.text-muted {
  color: #667085 !important;
}

a {
  color: #4a7cec;
  text-decoration: none;
  font-weight: 500;
}
a:hover {
  text-decoration: underline;
}
</style>