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
// ⭐⭐⭐ 1. axios 라이브러리 import (설치 필요: npm install axios) ⭐⭐⭐
import axios from 'axios'; 

// 백엔드 API의 기본 URL을 상수로 정의합니다. (실제 환경에 맞게 변경 필요)
const API_URL = 'http://localhost:8080/api/user';

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

    // ⭐ 로그인 폼 제출 핸들러 (백엔드 통신으로 수정) ⭐
    async handleLogin() {
      // 1. 클라이언트 측 유효성 검사
      if (!this.id || !this.password) {
        this.errorMessage = '아이디와 비밀번호를 모두 입력해주세요.';
        return;
      }

      this.errorMessage = ''; // 에러 메시지 초기화

      // 2. 서버로 전송할 데이터 객체 생성 (LoginRequestDTO의 필드명과 일치)
      const loginData = { 
        loginId: this.id, // 프론트의 'id'를 백엔드의 'loginId'로 매핑
        password: this.password, 
      };

      console.log('로그인 요청 데이터:', loginData);

      try {
        // 3. 백엔드 API 호출: POST /api/user/login
        const response = await axios.post(`${API_URL}/login`, loginData);
        
        // 4. 응답 처리 (성공: HTTP 200 OK)
        if (response.status === 200) { 
          const userData = response.data; // LoginResponseDTO 데이터

          console.log('로그인 성공. 사용자 데이터:', userData);

          // ⭐ 수정 사항: 항상 토큰을 저장합니다. ⭐
          this.saveLoginSession(userData.token);
          
          // 메인 페이지('/home')로 이동 (Vue Router의 replace 사용)
          this.$router.replace('/home'); 
        }
      } catch (error) {
        // 5. 에러 처리 (실패: HTTP 401 Unauthorized 등)
        console.error('로그인 요청 실패:', error);
        
        // 백엔드에서 보낸 에러 메시지(401)를 출력합니다.
        if (error.response && error.response.status === 401) {
          this.errorMessage = error.response.data || '아이디 또는 비밀번호를 확인해주세요.';
        } else {
          this.errorMessage = '로그인 처리 중 오류가 발생했습니다.';
        }
      }
    },

    // // [코드 설명 4] 로그인 세션을 저장하는 새로운 메서드 (변동 없음)
    // saveLoginSession(token) {
    //   // '로그인 유지' 체크 여부에 따라 저장 방식을 결정합니다.
    //   if (this.keepLoggedIn) {
    //       // 체크 O: 로컬 스토리지에 저장 -> 브라우저 종료 후에도 유지
    //       localStorage.setItem('user-auth-token', token);
    //       console.log('로그인 유지 설정됨: LocalStorage에 토큰 저장');
    //   } else {
    //       // 체크 X: 세션 스토리지에 저장 -> 브라우저 탭/창 종료 시 사라짐
    //       sessionStorage.setItem('user-auth-token', token);
    //       console.log('로그인 유지 설정 안 됨: SessionStorage에 토큰 저장');
    //   }
    // },

    // ⭐ 수정 사항: 로그인 세션을 localStorage에 무조건 저장합니다. ⭐
    saveLoginSession(token) {
      // 💡 세션 스토리지 옵션 제거, 무조건 LocalStorage에 저장하여 세션 유지
      localStorage.setItem('user-auth-token', token);
      console.log('로그인 유지 설정됨: LocalStorage에 토큰 저장');
      
      // 혹시 이전에 남아있을 세션 스토리지 토큰은 삭제합니다.
      sessionStorage.removeItem('user-auth-token');
    },
  }
};
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.woff2') format('woff2-variations');
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
  background-color: #3674B5;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  /* ⭐ 수정: 체크박스 삭제로 인해 상단 간격을 조정했습니다. ⭐ */
  margin-top: 10px;
}

.submit-button:hover {
  background-color: #3367D6;
}

.signup-link-wrapper {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #000000;
}

.signup-link-wrapper a {
  font-weight: 500;
  color: #3674B5;
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