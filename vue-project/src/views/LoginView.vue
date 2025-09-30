<template>
  <div class="login-container">
    <div class="login-form">
      <header class="header">
        <BigLogo/>
        <h2>로그인</h2>
        <p class="subtitle">당신의 이메일과 비밀번호를 입력하세요.</p>
      </header>

      <form @submit.prevent="handleLogin" novalidate>
        <div class="form-group">
          <label for="email">이메일 *</label>
          <input type="email" id="email" v-model="email" placeholder="이메일 입력" required class="text-input" />
        </div>

        <div class="form-group">
          <label for="password">비밀번호 *</label>
          <div class="input-with-icon">
            <input :type="isPasswordVisible ? 'text' : 'password'" id="password" v-model="password"
              placeholder="비밀번호 입력" required class="text-input" />
            <span @click="togglePasswordVisibility" class="password-toggle-icon">
              <svg v-if="isPasswordVisible" xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                fill="currentColor" class="bi bi-eye-fill" viewBox="0 0 16 16">
                <path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0" />
                <path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8m8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7" />
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                class="bi bi-eye-slash" viewBox="0 0 16 16">
                <path
                  d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7 7 0 0 0-2.79.588l.77.771A6 6 0 0 1 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13 13 0 0 1 14.828 8q-.086.13-.195.288c-.335.48-.83 1.12-1.465 1.755q-.247.248-.517.486z" />
                <path
                  d="M11.297 9.176a3.5 3.5 0 0 0-4.474-4.474l.823.823a2.5 2.5 0 0 1 2.829 2.829zm-2.943 1.299.822.822a3.5 3.5 0 0 1-4.474-4.474l.823.823a2.5 2.5 0 0 0 2.829 2.829" />
                <path
                  d="M3.35 5.47q-.27.24-.518.487A13 13 0 0 0 1.172 8l.195.288c.335.48.83 1.12 1.465 1.755C4.121 11.332 5.881 12.5 8 12.5c.716 0 1.39-.133 2.02-.36l.77.772A7 7 0 0 1 8 13.5C3 13.5 0 8 0 8s.939-1.721 2.641-3.238l.708.709zm10.296 8.884-12-12 .708-.708 12 12z" />
              </svg>
            </span>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <label class="checkbox-group">
          <input type="checkbox" id="keep-logged-in" v-model="keepLoggedIn" />
          <span class="checkmark"></span>
          <span class="checkbox-label">로그인 유지</span>
        </label>

        <button type="submit" class="submit-button">로그인</button>
      </form>

      <p class="signup-link-wrapper">
        계정이 없으신가요? <router-link to="/signup">회원가입</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import BigLogo from '@/components/BigLogo.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const keepLoggedIn = ref(false);
const isPasswordVisible = ref(false);
const errorMessage = ref('');
const router = useRouter();

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value;
};

const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = '이메일과 비밀번호를 모두 입력해주세요.';
    return;
  }

  try {
    // 이곳에 실제 서버로 로그인 요청을 보내는 API 호출 코드를 작성해야함

    // 아래는 서버 연동 전 임시 성공 처리
    console.log('서버에 로그인 요청을 보냈다고 가정, 성공적으로 응답 받음.');
    errorMessage.value = ''; // 성공 시 에러 메시지 초기화

    router.replace('/'); // 실제 메인 페이지의 경로로 변경해야함

  } catch (error) {
    console.error("로그인 요청 실패:", error);
    errorMessage.value = '이메일 또는 비밀번호를 확인해주세요.';
  }
};
</script>

<style scoped>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.login-container {
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
  margin-bottom: 32px;
}

.header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px;
}

.subtitle {
  font-size: 15px;
  color: #616161;
  margin: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #212121;
  margin-bottom: 8px;
}

.text-input {
  width: 100%;
  padding: 14px 16px;
  font-size: 16px;
  border: 1px solid #000000;
  border-radius: 10px;
  box-sizing: border-box;
}

.text-input::placeholder {
  color: #BDBDBD;
}

.text-input:focus {
  outline: none;
  border-color: #4285F4;
}

.input-with-icon {
  position: relative;
}

.input-with-icon .text-input {
  padding-right: 50px;
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
}

.error-message {
  color: #ff3b30;
  text-align: center;
  font-size: 14px;
  margin-top: -8px;
  margin-bottom: 20px;
}

.checkbox-group {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 24px;
}

.checkbox-group input {
  display: none;
}

.checkmark {
  width: 22px;
  height: 22px;
  border: 1.5px solid #000000;
  border-radius: 50%;
  margin-right: 10px;
  display: inline-block;
  position: relative;
}

.checkbox-group input:checked+.checkmark::after {
  content: "✔";
  color: #000000;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
}

.checkbox-label {
  font-size: 15px;
  color: #000000;
}

.submit-button {
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background-color: #000000;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.signup-link-wrapper {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #616161;
}

.signup-link-wrapper a {
  font-weight: 500;
  color: #000000;
  text-decoration: underline;
}
</style>
