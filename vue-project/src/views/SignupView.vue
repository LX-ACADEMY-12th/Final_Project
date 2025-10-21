<template>
  <div class="signup-container">
    <header class="header">
      <BigLogo/>
      <h2>회원가입</h2>
    </header>

    <form @submit.prevent="handleSubmit" class="signup-form">
      <div class="form-group">
        <label for="id">아이디</label>
        <div class="input-with-button">
          <input type="text" id="id" v-model="id" placeholder="아이디 입력" required class="text-input" />
          <button type="button" class="check-button">중복확인</button>
        </div>
      </div>

      <div class="form-group">
        <label for="password">비밀번호</label>
        <div class="input-with-icon">
          <input :type="passwordVisible.password ? 'text' : 'password'" id="password" v-model="password" @input="checkPasswordValidity"
            @focus="handlePasswordFocus" @blur="handlePasswordBlur" placeholder="영문, 숫자, 특수문자 조합 8~16자리" required
            class="text-input" />
          <span class="password-toggle-icon" @click="togglePasswordVisibility('password')">
            <i v-if="passwordVisible.password" class="bi bi-eye-fill"></i>
            <i v-else class="bi bi-eye-slash-fill"></i>
          </span>
        </div>

        <div class="password-validation-list" v-if="isPasswordFocused">
          <div :class="['validation-item', { 'valid': passwordCriteria.length }]">
            <i :class="['validation-icon', passwordCriteria.length ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            최소 8자
          </div>
          <div :class="['validation-item', { 'valid': passwordCriteria.number }]">
            <i :class="['validation-icon', passwordCriteria.number ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            최소 1개의 숫자
          </div>
          <div :class="['validation-item', { 'valid': passwordCriteria.specialChar }]">
            <i :class="['validation-icon', passwordCriteria.specialChar ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            최소 1개의 특수문자
          </div>
        </div>
      </div>

      <div class="form-group">
        <label for="username">사용자 이름</label>
        <input type="text" id="username" v-model="username" placeholder="사용자 이름 입력" required class="text-input" />
      </div>

      <div class="form-group">
        <label for="email">이메일</label>
        <input type="email" id="email" v-model="email" placeholder="이메일 입력" required class="text-input" />
      </div>
      
      <div class="form-group">
        <label for="phone">휴대폰 번호</label>
        <input type="tel" id="phone" v-model="phone" placeholder="휴대폰 번호 입력" required class="text-input" />
      </div>

      <p class="terms-text">
        가입하면 과학 어디가의 약관, 데이터 정책 및 쿠키 정책에 동의하게 됩니다.
      </p>

      <button type="submit" class="submit-button" :disabled="!isFormValid">회원가입</button>

      <p class="login-link-wrapper">
        이미 계정이 있으신가요? <router-link :to="{ name: 'login' }" replace>로그인</router-link>
      </p>
    </form>
  </div>
</template>

<script setup>
import BigLogo from '@/components/BigLogo.vue';
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';

const id = ref('');
const password = ref('');
const username = ref('');
const email = ref(''); 
const phone = ref('');
const passwordVisible = reactive({
  password: false
});
const isPasswordFocused = ref(false);
const passwordCriteria = reactive({
  length: false,
  number: false,
  specialChar: false
});
const router = useRouter();

const isEmailValid = computed(() => {
    return /\S+@\S+\.\S+/.test(email.value);
});


const isFormValid = computed(() => {
  return (
    id.value &&
    email.value &&
    isEmailValid.value && 
    username.value &&
    phone.value &&
    passwordCriteria.length &&
    passwordCriteria.number &&
    passwordCriteria.specialChar
  );
});

const togglePasswordVisibility = (field) => {
  passwordVisible[field] = !passwordVisible[field];
};

const handlePasswordFocus = () => {
  isPasswordFocused.value = true;
};

const handlePasswordBlur = () => {
  setTimeout(() => {
    isPasswordFocused.value = false;
  }, 200);
};

const checkPasswordValidity = () => {
  passwordCriteria.length = password.value.length >= 8 && password.value.length <= 16;
  passwordCriteria.number = /[0-9]/.test(password.value);
  passwordCriteria.specialChar = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password.value);
};

const handleSubmit = () => {
  if (!isFormValid.value) {
    alert('모든 필수 정보를 입력하고 비밀번호 및 이메일 조건을 충족해야 합니다.');
    return;
  }
  
  if (!isEmailValid.value) {
      alert('유효한 이메일 주소를 입력해주세요.');
      return;
  }

  console.log('회원가입 정보:', {
    id: id.value,
    email: email.value, 
    password: password.value,
    username: username.value,
    phone: phone.value
  });
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
  color: #000000;
}

.password-validation-list {
  margin-top: 8px;
  font-size: 14px;
}

.validation-item {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  color: #BDBDBD;
}

.validation-item.valid {
  color: #000000; 
}

.validation-icon {
  font-size: 14px;
  margin-right: 8px;
  width: 14px; 
  text-align: center;
}

.validation-item:not(.valid) .validation-icon {
  color: #FF6666; 
}

.validation-item.valid .validation-icon {
  color: #4CAF50; 
}

.signup-container {
  font-family: 'SUIT Variable', sans-serif;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 60px 20px;
  background-color: #fff;
  min-height: 100vh;
  box-sizing: border-box;
}

.signup-form {
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
  margin-bottom: 24px; 
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

.input-with-button {
  display: flex;
  align-items: center;
}

.input-with-button .text-input {
  flex-grow: 1;
  margin-right: 8px;
}

.check-button {
  padding: 12px 16px; 
  font-size: 16px;
  font-weight: 500; 
  color: #fff;
  background-color: #3674B5; 
  border: none;
  border-radius: 15px; 
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
}

.check-button:hover {
  background-color: #3367D6;
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
}

.terms-text {
  font-size: 14px;
  color: #616161;
  text-align: center;
  margin-top: 24px;
  margin-bottom: 28px; 
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

.login-link-wrapper {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #000000; 
}

.login-link-wrapper a {
  font-weight: 500;
  color: #3674B5; 
  text-decoration: none; 
}

.login-link-wrapper a:hover {
  text-decoration: underline;
}

.text-input::-ms-reveal,
.text-input::-webkit-reveal {
  display: none;
}
</style>