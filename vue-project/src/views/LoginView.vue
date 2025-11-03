<template>
  <div class="login-container">
    <div class="login-form">
      <header class="header">
        <!-- <BigLogo/> -->
        <h2>로그인</h2>
      </header>

      <form @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label for="id">아이디</label>
          <input type="text" id="id" v-model="id" placeholder="아이디 입력" required class="text-input" />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <div class="input-with-icon">
            <input :type="isPasswordVisible ? 'text' : 'password'" id="password" v-model="password"
              placeholder="비밀번호 입력" required class="text-input" />
            <span @click="togglePasswordVisibility" class="password-toggle-icon">
              <i v-if="isPasswordVisible" class="bi bi-eye"></i>
              <i v-else class="bi bi-eye-slash"></i>
            </span>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <!-- <label class="radio-group">
            <input type="checkbox" id="keep-logged-in" v-model="keepLoggedIn" name="keep-login" />
            <span class="checkbox-label">로그인 유지</span>
          </label> -->

        <button type="submit" class="submit-button">로그인</button>
      </form>

      <p class="signup-link-wrapper">
        아직 계정이 없으신가요? <router-link to="/signup">회원가입</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
// 🟢 Pinia 스토어 import
import { useAuthStore } from '@/stores/authStore';

// API URL (Login API는 토큰이 필요 없으므로 Base URL만 사용)
const API_BASE = import.meta.env?.VITE_API_BASE || 'http://localhost:8080';
const API_URL = `${API_BASE}/api/user`; // '/api/user' 경로

// export default를 사용하여 컴포넌트의 로직(데이터, 계산된 속성, 메서드 등)을 정의합니다.
export default {
  // 컴포넌트의 이름 설정
  name: 'LoginForm',

  // 1. 상태(Data) 정의
  data() {
    return {
      id: '',
      password: '',
      // keepLoggedIn: false,
      isPasswordVisible: false,
      errorMessage: '',
    };
  },

  // 2. 계산된 속성(Computed)
  computed: {
    isFormValid() {
      // 아이디와 비밀번호가 모두 채워져야 로그인 버튼 활성화
      return this.id.length > 0 && this.password.length > 0;
    }
  },

  // 3. 메서드(Methods)
  methods: {
    // 비밀번호 표시/숨김 토글 (변동 없음)
    togglePasswordVisibility() {
      this.isPasswordVisible = !this.isPasswordVisible;
    },

    // ⭐ 로그인 폼 제출 핸들러 ⭐
    async handleLogin() {
      // 🟢 클라이언트 측 유효성 검사
      if (!this.id || !this.password) {
        this.errorMessage = '아이디와 비밀번호를 모두 입력해주세요.';
        return;
      }

      this.errorMessage = ''; // 에러 메시지 초기화

      // 🟢 Pinia 스토어 인스턴스 가져오기
      const authStore = useAuthStore();

      // 🟢 서버로 전송할 데이터 객체 생성 (LoginRequestDTO의 필드명과 일치)
      const loginData = {
        loginId: this.id, // 프론트 'id'를 백엔드 'loginId'로 매핑
        password: this.password,
      };

      console.log('로그인 요청 데이터:', loginData);

      try {
        // 🟢 백엔드 API 호출: POST /api/user/login
        const response = await axios.post(`${API_URL}/login`, loginData);

        // 🟢 응답 처리 (성공: HTTP 200 OK)
        if (response.status === 200) {
          // 🟢 Pinia 스토어의 login 액션 호출
          // (response.data = LoginResponseDTO { userId, ..., accessToken, refreshToken })
          authStore.login(response.data);

          console.log('로그인 성공. Pinia authStore 상태:', authStore.$state);

          // 메인 페이지('/home' )로 이동
          this.$router.replace('/home');
        }
      } catch (error) {
        // 에러 처리 (실패: HTTP 401 Unauthorized 등)
        console.error('로그인 요청 실패:', error);

        // 백엔드에서 보낸 에러 메시지(401)를 출력합니다.
        if (error.response && error.response.status === 401) {
          this.errorMessage = error.response.data || '아이디 또는 비밀번호를 확인해주세요.';
        } else {
          this.errorMessage = '로그인 처리 중 오류가 발생했습니다.';
        }
      }
    }
  },
};
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-style: normal;
}

.password-toggle-icon i {
  font-size: 20px;
}

.login-container {
  position: relative;
  font-family: 'SUIT Variable', sans-serif;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 60px 20px;
  background-color: #fff;
  min-height: 100vh;
  box-sizing: border-box;
}

.login-form {
  width: 100%;
  max-width: 360px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h2 {
  font-size: 20px;
  font-weight: 500;
  color: #000000;
  margin: 10px 0 0;
}

.form-group {
  margin-bottom: 40px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  margin-bottom: 4px;
}

.text-input {
  width: 100%;
  padding: 12px 14px;
  font-size: 16px;
  border: 1px solid #DEDEDE;
  border-radius: 15px;
  box-sizing: border-box;
}

.text-input::placeholder {
  color: #BDBDBD;
}

.text-input:focus {
  outline: none;
  border-color: #000000;
}

.input-with-icon {
  position: relative;
}

.input-with-icon .text-input {
  padding-right: 40px;
}

.password-toggle-icon {
  position: absolute;
  top: 50%;
  right: 12px;
  transform: translateY(-50%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
}

.error-message {
  color: #ff3b30;
  text-align: center;
  font-size: 14px;
  margin-top: -8px;
  margin-bottom: 20px;
}

/* .radio-group {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 28px;
}

.radio-group input[type="checkbox"] {
  width: 20px;
  height: 20px;
  border: 1px solid #000000;
  border-radius: 50%;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  position: relative;
  outline: none;
  cursor: pointer;
  margin-right: 8px;
}

.radio-group input[type="checkbox"]:checked {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.radio-group input[type="checkbox"]:checked::after {
  content: '';
  width: 10px;
  height: 10px;
  background-color: #000000;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.checkbox-label {
  font-size: 15px;
  color: #000000;
} */

.submit-button {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  background-color: #4A7CEC;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  /* ⭐ 수정: 체크박스 삭제로 인해 상단 간격을 조정했습니다. ⭐ */
  margin-top: 10px;
}

.submit-button:hover {
  background-color: #4A7CEC;
}

.signup-link-wrapper {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #000000;
}

.signup-link-wrapper a {
  font-weight: 500;
  color: #4A7CEC;
  text-decoration: none;
}

.signup-link-wrapper a:hover {
  text-decoration: underline;
}

.text-input::-ms-reveal,
.text-input::-webkit-reveal {
  display: none;
}
</style>
