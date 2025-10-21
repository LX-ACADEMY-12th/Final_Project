<template>
  <div class="login-container">
    <div class="login-form">
      <header class="header">
        <BigLogo/>
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
              <i v-if="isPasswordVisible" class="bi bi-eye-fill"></i>
              <i v-else class="bi bi-eye-slash-fill"></i>
            </span>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <label class="radio-group">
          <input type="checkbox" id="keep-logged-in" v-model="keepLoggedIn" name="keep-login" />
          <span class="checkbox-label">로그인 유지</span>
        </label>

        <button type="submit" class="submit-button">로그인</button>
      </form>

      <p class="signup-link-wrapper">
        아직 계정이 없으신가요? <router-link to="/signup">회원가입</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import BigLogo from '@/components/BigLogo.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const id = ref('');
const password = ref('');
const keepLoggedIn = ref(false); 
const isPasswordVisible = ref(false);
const errorMessage = ref('');
const router = useRouter();

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value;
};

const handleLogin = async () => {
  if (!id.value || !password.value) {
    errorMessage.value = '아이디와 비밀번호를 모두 입력해주세요.';
    return;
  }

  try {
    // 실제 서버로 보낼 데이터를 콘솔에 출력 (디버깅 용)
    console.log('로그인 요청 데이터:', { id: id.value, password: password.value });
    
    // 이곳에 실제 서버로 로그인 요청을 보내는 API 호출 코드를 작성해야함

    // 아래는 서버 연동 전 임시 성공 처리
    console.log('서버에 로그인 요청을 보냈다고 가정, 성공적으로 응답 받음.');
    errorMessage.value = ''; // 성공 시 에러 메시지 초기화

    router.replace('/'); // 실제 메인 페이지의 경로로 변경해야함

  } catch (error) {
    console.error("로그인 요청 실패:", error);
    errorMessage.value = '아이디 또는 비밀번호를 확인해주세요.';
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

.radio-group {
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
}

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