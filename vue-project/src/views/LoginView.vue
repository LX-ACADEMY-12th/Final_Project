<template>
  <div class="login-container">
    <div class="login-form">
      <header class="login-header">
        <div class="logo">
          <svg width="28" height="28" fill="#4285F4" viewBox="0 0 16 16">
            <path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10zm0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6z" />
          </svg>
          <h1>MapSocial</h1>
        </div>
        <h2 class="login-title">로그인</h2>
        <p class="login-subtitle">당신의 이메일과 비밀번호를 입력하세요.</p>
      </header>

      <form @submit.prevent="handleLogin" novalidate>
        <div class="input-group">
          <label for="email">이메일 *</label>
          <input type="email" id="email" v-model="email" placeholder="이메일 입력" required />
        </div>

        <div class="input-group">
          <label for="password">비밀번호 *</label>
          <div class="password-wrapper">
            <input :type="isPasswordVisible ? 'text' : 'password'" id="password" v-model="password"
              placeholder="비밀번호 입력" required />
            <button type="button" @click="togglePasswordVisibility" class="password-toggle" aria-label="비밀번호 보기 토글">
              <svg width="22" height="22" fill="#757575" viewBox="0 0 16 16">
                <path
                  d="m10.79 12.912-1.614-1.615a3.5 3.5 0 0 1-4.474-4.474l-2.06-2.06C.938 6.278 0 8 0 8s3 5.5 8 5.5a7.029 7.029 0 0 0 2.79-.588zM5.21 3.088A7.028 7.028 0 0 1 8 2.5c5 0 8 5.5 8 5.5s-.939 1.721-2.641 3.238l-2.062-2.062a3.5 3.5 0 0 0-4.474-4.474L5.21 3.089z" />
                <path
                  d="M5.525 7.646a2.5 2.5 0 0 0 2.829 2.829l-2.83-2.829zm4.95.708-2.829-2.83a2.5 2.5 0 0 1 2.829 2.829zm3.171 6-12-12 .708-.708 12 12-.708.708z" />
              </svg>
            </button>
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

        <button type="submit" class="login-button">로그인</button>
      </form>

      <footer class="login-footer">
        <p>계정이 없으신가요? <router-link :to="{ name: 'signup' }">회원가입</router-link></p>
      </footer>
    </div>
  </div>
</template>

<script setup>
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

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.logo h1 {
  font-size: 26px;
  font-weight: 700;
  color: #000000;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px;
}

.login-subtitle {
  font-size: 15px;
  color: #616161;
  margin: 0;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  margin-bottom: 8px;
}

.input-group input {
  width: 100%;
  padding: 14px 16px;
  font-size: 16px;
  border: 1px solid #000000;
  border-radius: 10px;
  box-sizing: border-box;
  -webkit-appearance: none;
}

.input-group input::placeholder {
  color: #BDBDBD;
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 12px;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 4px;
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

.login-button {
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

.login-footer {
  text-align: center;
  margin-top: 24px;
}

.login-footer p {
  font-size: 14px;
  color: #000000;
}

.login-footer a {
  font-weight: 500;
  color: #000000;
  text-decoration: underline;
}
</style>
