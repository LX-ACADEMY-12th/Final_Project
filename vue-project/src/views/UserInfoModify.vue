<template>
  <div class="modify-container">
    <header class="header">
      <div class="logo">
        <svg xmlns="http://www.w3.org/2000/svg" width="25" height="25" fill="currentColor" class="bi bi-geo-alt-fill" viewBox="0 0 16 16">
          <path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10m0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6"/>
        </svg>
        <h1>MapSocial</h1>
      </div>
      <h2>정보 수정</h2>
    </header>

    <form @submit.prevent="handleSubmit" class="modify-form">
      <div class="form-group">
        <label for="email">이메일 *</label>
        <div class="input-with-button">
          <input type="email" id="email" v-model="email" required class="text-input" readonly />
        </div>
      </div>

      <div class="form-group">
        <label for="current-password">현재 비밀번호 *</label>
        <div class="input-with-icon">
          <input :type="passwordVisible.currentPassword ? 'text' : 'password'" id="current-password" v-model="currentPassword" placeholder="현재 비밀번호를 입력하세요" required class="text-input" />
          
          <span class="password-toggle-icon" @click="togglePasswordVisibility('currentPassword')">
            <svg v-if="passwordVisible.currentPassword" 
                xmlns="http://www.w3.org/2000/svg" 
                width="20" height="20" 
                fill="currentColor" 
                class="bi bi-eye-fill" 
                viewBox="0 0 16 16">
              <path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0"/>
              <path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8m8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7"/>
            </svg>
            
            <svg v-else 
                xmlns="http://www.w3.org/2000/svg" 
                width="20" height="20" 
                fill="currentColor" 
                class="bi bi-eye-slash" 
                viewBox="0 0 16 16">
              <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7 7 0 0 0-2.79.588l.77.771A6 6 0 0 1 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13 13 0 0 1 14.828 8q-.086.13-.195.288c-.335.48-.83 1.12-1.465 1.755q-.247.248-.517.486z"/>
              <path d="M11.297 9.176a3.5 3.5 0 0 0-4.474-4.474l.823.823a2.5 2.5 0 0 1 2.829 2.829zm-2.943 1.299.822.822a3.5 3.5 0 0 1-4.474-4.474l.823.823a2.5 2.5 0 0 0 2.829 2.829"/>
              <path d="M3.35 5.47q-.27.24-.518.487A13 13 0 0 0 1.172 8l.195.288c.335.48.83 1.12 1.465 1.755C4.121 11.332 5.881 12.5 8 12.5c.716 0 1.39-.133 2.02-.36l.77.772A7 7 0 0 1 8 13.5C3 13.5 0 8 0 8s.939-1.721 2.641-3.238l.708.709zm10.296 8.884-12-12 .708-.708 12 12z"/>
              <path fill-rule="evenodd" d="M3.646 12.354l7-7 .708.708-7 7-.708-.708z"/>
            </svg>
          </span>
        </div>
      </div>

      <div class="form-group">
        <label for="new-password">새 비밀번호 *</label>
        <div class="input-with-icon">
          <input :type="passwordVisible.password ? 'text' : 'password'" id="new-password" v-model="password" placeholder="영문, 숫자 조합 8~16자리" required class="text-input" />
          <span class="password-toggle-icon" @click="togglePasswordVisibility('password')">
            <svg v-if="passwordVisible.password" 
                 xmlns="http://www.w3.org/2000/svg" 
                 width="20" height="20" 
                 fill="currentColor" 
                 class="bi bi-eye-fill" 
                 viewBox="0 0 16 16">
              <path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0"/>
              <path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8m8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7"/>
            </svg>
            <svg v-else 
                 xmlns="http://www.w3.org/2000/svg" 
                 width="20" height="20" 
                 fill="currentColor" 
                 class="bi bi-eye-slash" 
                 viewBox="0 0 16 16">
              <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7 7 0 0 0-2.79.588l.77.771A6 6 0 0 1 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13 13 0 0 1 14.828 8q-.086.13-.195.288c-.335.48-.83 1.12-1.465 1.755q-.247.248-.517.486z"/>
              <path d="M11.297 9.176a3.5 3.5 0 0 0-4.474-4.474l.823.823a2.5 2.5 0 0 1 2.829 2.829zm-2.943 1.299.822.822a3.5 3.5 0 0 1-4.474-4.474l.823.823a2.5 2.5 0 0 0 2.829 2.829"/>
              <path d="M3.35 5.47q-.27.24-.518.487A13 13 0 0 0 1.172 8l.195.288c.335.48.83 1.12 1.465 1.755C4.121 11.332 5.881 12.5 8 12.5c.716 0 1.39-.133 2.02-.36l.77.772A7 7 0 0 1 8 13.5C3 13.5 0 8 0 8s.939-1.721 2.641-3.238l.708.709zm10.296 8.884-12-12 .708-.708 12 12z"/>
              <path fill-rule="evenodd" d="M3.646 12.354l7-7 .708.708-7 7-.708-.708z"/>
            </svg>
          </span>
        </div>
      </div>

      <div class="form-group">
        <label for="confirm-password">새 비밀번호 확인 *</label>
        <div class="input-with-icon">
          <input :type="passwordVisible.confirmPassword ? 'text' : 'password'" id="confirm-password" v-model="confirmPassword" placeholder="비밀번호 입력" required class="text-input" />
          <span class="password-toggle-icon" @click="togglePasswordVisibility('confirmPassword')">
            <svg v-if="passwordVisible.confirmPassword" 
                 xmlns="http://www.w3.org/2000/svg" 
                 width="20" height="20" 
                 fill="currentColor" 
                 class="bi bi-eye-fill" 
                 viewBox="0 0 16 16">
              <path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0"/>
              <path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8m8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7"/>
            </svg>
            <svg v-else 
                 xmlns="http://www.w3.org/2000/svg" 
                 width="20" height="20" 
                 fill="currentColor" 
                 class="bi bi-eye-slash" 
                 viewBox="0 0 16 16">
              <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7 7 0 0 0-2.79.588l.77.771A6 6 0 0 1 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13 13 0 0 1 14.828 8q-.086.13-.195.288c-.335.48-.83 1.12-1.465 1.755q-.247.248-.517.486z"/>
              <path d="M11.297 9.176a3.5 3.5 0 0 0-4.474-4.474l.823.823a2.5 2.5 0 0 1 2.829 2.829zm-2.943 1.299.822.822a3.5 3.5 0 0 1-4.474-4.474l.823.823a2.5 2.5 0 0 0 2.829 2.829"/>
              <path d="M3.35 5.47q-.27.24-.518.487A13 13 0 0 0 1.172 8l.195.288c.335.48.83 1.12 1.465 1.755C4.121 11.332 5.881 12.5 8 12.5c.716 0 1.39-.133 2.02-.36l.77.772A7 7 0 0 1 8 13.5C3 13.5 0 8 0 8s.939-1.721 2.641-3.238l.708.709zm10.296 8.884-12-12 .708-.708 12 12z"/>
              <path fill-rule="evenodd" d="M3.646 12.354l7-7 .708.708-7 7-.708-.708z"/>
            </svg>
          </span>
        </div>
      </div>

      <div class="form-group">
        <label for="phone">휴대폰 번호 *</label>
        <input type="tel" id="phone" v-model="phone" placeholder="휴대폰 번호 입력" required class="text-input" />
      </div>

      <button type="submit" class="submit-button">수정</button>
    </form>
  </div>
</template>

<script>
export default {
  name: 'ModifyForm',
  data() {
    return {
      email: 'test01@gamil.com', 
      password: '',
      confirmPassword: '',
      phone: '010-1111-2222',
      passwordVisible: {
        password: false,
        confirmPassword: false
      }
    };
  },
  methods: {
    togglePasswordVisibility(field) {
      this.passwordVisible[field] = !this.passwordVisible[field];
    },
    handleSubmit() {
      if (this.password !== this.confirmPassword) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
      }

      console.log('수정된 정보:', {
        password: this.password,
        phone: this.phone
      });

      alert('정보가 성공적으로 수정되었습니다.');
    }
  }
};
</script>

<style scoped>
/* 전역 스타일 */
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 컨테이너 및 폼 */
.modify-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 60px 20px;
  background-color: #fff;
  min-height: 100vh;
  box-sizing: border-box;
}

.modify-form {
  width: 100%;
  max-width: 360px;
}

/* 헤더 */
.header {
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

.header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0;
}

/* 폼 요소 */
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

/* 텍스트 입력 필드 */
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

/* 버튼이 있는 입력 그룹 */
.input-with-button {
  display: flex;
  align-items: center;
}

.input-with-button .text-input {
  flex-grow: 1;
  margin-right: 8px;
}

/* 아이콘이 있는 입력 그룹 */
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

/* 수정 버튼 */
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
</style>