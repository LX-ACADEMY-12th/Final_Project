<template>
  <div class="signup-container">
    <header class="header">
      <h2>회원가입</h2>
    </header>

    <form @submit.prevent="handleSubmit" class="signup-form">
      <div class="form-group">
        <label for="id">아이디 *</label>
        <div class="input-with-button">
          <input type="text" id="id" v-model="id" placeholder="아이디 입력" required class="text-input"
            @input="resetIdCheckStatus" />
          <button type="button" class="check-button" @click="checkIdDuplicate" :disabled="!id">중복확인</button>
        </div>
        <div v-if="isIdChecked && !idCheckLoading"
          :class="{ 'validation-message': true, 'valid': !isIdDuplicate, 'invalid': isIdDuplicate }">
          <span v-if="isIdDuplicate">이미 사용 중인 아이디입니다.</span>
          <span v-else>사용 가능한 아이디입니다!</span>
        </div>
      </div>
      <!-- 비밀번호 입력창 -->
      <div class="form-group">
        <label for="password">비밀번호 *</label>
        <div class="input-with-icon">
          <input :type="passwordVisible.password ? 'text' : 'password'" id="password" v-model="password"
            @input="checkPasswordValidity" @focus="handlePasswordFocus" @blur="handlePasswordBlur"
            placeholder="영문, 숫자, 특수문자 조합 8~16자리" required class="text-input" />
          <span class="password-toggle-icon" @click="togglePasswordVisibility('password')">
            <i v-if="passwordVisible.password" class="bi bi-eye"></i>
            <i v-else class="bi bi-eye-slash"></i>
          </span>
        </div>
        <!-- 비밀번호 조건 확인 -->
        <div class="password-validation-list" v-if="isPasswordFocused">
          <div :class="['validation-item', { 'valid': passwordCriteria.length }]">
            <i :class="['validation-icon', passwordCriteria.length ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            8 ~ 16 자
          </div>
          <div :class="['validation-item', { 'valid': passwordCriteria.number }]">
            <i :class="['validation-icon', passwordCriteria.number ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            최소 1개의 숫자
          </div>
          <div :class="['validation-item', { 'valid': passwordCriteria.specialChar }]">
            <i
              :class="['validation-icon', passwordCriteria.specialChar ? 'bi-check-circle-fill' : 'bi-x-circle-fill']"></i>
            최소 1개의 특수문자
          </div>
        </div>
      </div>

      <div class="form-group">
        <label for="username">사용자 이름 *</label>
        <input type="text" id="username" v-model="username" placeholder="사용자 이름 입력" required class="text-input" />
      </div>

      <div class="form-group">
        <label for="email">이메일 *</label>
        <input type="email" id="email" v-model="email" placeholder="이메일 입력" required class="text-input"
          @input="checkEmailDuplicateWithDebounce" />

        <div v-if="email.length > 0 && !isEmailValid" class="validation-message invalid">
          유효한 이메일 형식이 아닙니다.
        </div>

        <div v-if="isEmailValid && isEmailCheckedOnce"
          :class="{ 'validation-message': true, 'checking-status': isEmailChecking, 'valid': !isEmailDuplicate && !isEmailChecking, 'invalid': isEmailDuplicate && !isEmailChecking }">
          <span v-if="isEmailChecking">중복 확인 중...</span>
          <span v-else-if="isEmailDuplicate">이미 사용 중인 이메일입니다.</span>
          <span v-else>사용 가능한 이메일입니다!</span>
        </div>
      </div>

      <div class="form-group">
        <label for="phone">휴대폰 번호 *</label>
        <input type="tel" id="phone" v-model="phone" placeholder="휴대폰 번호 입력" required class="text-input"
          @input="formatPhoneNumber" />
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

  <!-- 회원가입 성공 모달 -->
  <div v-if="showSuccessModal" class="modal-overlay">
    <div class="modal-content">
      <h3>회원가입 성공</h3>
      <p>{{ username }}님, 환영합니다!</p>
      <p>로그인 후 서비스를 이용해 주세요.</p>
      <button @click="navigateToLogin" class="modal-button">로그인 화면으로 이동</button>
    </div>
  </div>

</template>

<script>
// 1. axios 라이브러리 import
import eventBus from '@/utils/eventBus';
import axios from 'axios';

// 백엔드 API의 기본 URL을 상수로 정의합니다. (실제 환경에 맞게 변경 필요)
const API_URL = 'http://localhost:8080/api/user';

export default {
  // 컴포넌트의 이름 설정
  name: 'SignupForm',

  // 디바운싱 타이머를 컴포넌트 인스턴스에 추가하지 않고,
  // 컴포넌트 인스턴스가 아닌 옵션에 추가 (Vue 2 방식)
  debounceTimer: null,

  // 1. 상태(Data) 정의 (변동 없음)
  data() {
    return {
      id: '',
      password: '',
      username: '',
      email: '',
      phone: '',

      // 아이디 중복 확인 관련 상태
      isIdChecked: false, // 중복확인 버튼 클릭 여부
      isIdDuplicate: true, // 아이디 중복 여부 (초기값: 중복)
      idCheckLoading: false, // 아이디 중복 확인 로딩 상태

      // 비밀번호 표시/숨김
      passwordVisible: {
        password: false
      },

      // 비밀번호 유효성 메시지 표시
      isPasswordFocused: false,

      // 비밀번호 유효성 조건 충족 여부
      passwordCriteria: {
        length: false,
        number: false,
        specialChar: false
      },

      // 이메일 중복 확인 관련 상태
      isEmailDuplicate: true, // 이메일 중복 여부 (초기값: 중복)
      isEmailChecking: false, // 이메일 중복 확인 로딩 상태
      isEmailCheckedOnce: false, // 한 번이라도 중복 확인 로직을 실행했는지 여부

      // 모달 상태 추가
      showSuccessModal: false, // 성공 모달 표시 여부
    };
  },

  // 2. 계산된 속성(Computed) (변동 없음)
  computed: {
    isEmailValid() {
      // 이메일 형식 유효성 검사
      return /\S+@\S+\.\S+/.test(this.email);
    },

    isFormValid() {
      // 회원가입 버튼 활성화 조건 (변동 없음)
      return (
        this.id &&
        !this.isIdDuplicate && // 아이디 중복이 아니어야 함
        this.isIdChecked && // 아이디 중복확인 완료
        this.email &&
        this.isEmailValid && // 이메일 형식 유효
        !this.isEmailDuplicate && // 이메일 중복이 아니어야 함
        this.isEmailCheckedOnce && // 이메일 중복 확인이 완료되어야 함
        this.username &&
        this.phone &&
        this.passwordCriteria.length &&
        this.passwordCriteria.number &&
        this.passwordCriteria.specialChar
      );
    }
  },

  // 3. 메서드(Methods)
  methods: {
    // 비밀번호 표시/숨김 토글 (변동 없음)
    togglePasswordVisibility(field) {
      // ... (기존 코드 유지) ...
      this.passwordVisible[field] = !this.passwordVisible[field];

      // input type을 직접 변경하는 로직 (Vue 2 방식)
      this.$nextTick(() => {
        const passwordInput = document.getElementById(field);
        if (passwordInput) {
          passwordInput.type = this.passwordVisible[field] ? 'text' : 'password';
        }
      });
    },

    // 비밀번호 입력창 포커스 시 유효성 메시지 표시 (변동 없음)
    handlePasswordFocus() {
      this.isPasswordFocused = true;
    },

    // 비밀번호 입력창 포커스 해제 시 유효성 메시지 숨김 (변동 없음)
    handlePasswordBlur() {
      setTimeout(() => {
        this.isPasswordFocused = false;
      }, 200);
    },

    // 비밀번호 유효성 검사 (변동 없음)
    checkPasswordValidity() {
      // ... (기존 코드 유지) ...
      this.passwordCriteria.length = this.password.length >= 8 && this.password.length <= 16;
      this.passwordCriteria.number = /[0-9]/.test(this.password);
      this.passwordCriteria.specialChar = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(this.password);
    },

    // ⭐ 아이디 중복 확인 (백엔드 통신으로 수정) ⭐
    async checkIdDuplicate() {
      // 1. 유효성 검사
      if (!this.id || this.id.length < 4) {
        eventBus.emit('show-global-alert', {
          message: '아이디를 4자 이상 입력해 주세요.',
          type: 'error'
        });
        this.isIdChecked = false;
        this.idCheckLoading = false;
        this.isIdDuplicate = true;
        return;
      }

      this.idCheckLoading = true; // 로딩 시작

      try {
        // 2. 백엔드 API 호출: GET /api/user/check-id/{id}
        const response = await axios.get(`${API_URL}/check-id/${this.id}`);

        // 백엔드에서 true를 반환하면 중복, false를 반환하면 중복 아님
        const isDuplicate = response.data;

        this.isIdChecked = true; // 확인 완료 상태로 변경
        this.isIdDuplicate = isDuplicate; // 중복 여부 저장

        if (isDuplicate) {
          console.log('아이디 중복');
        } else {
          console.log('사용 가능 아이디');
        }

      } catch (error) {
        console.error('아이디 중복 확인 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '아이디 중복 확인 중 오류가 발생했습니다.',
          type: 'error'
        });
        this.isIdDuplicate = true; // 오류 발생 시 안전하게 중복으로 처리
      } finally {
        this.idCheckLoading = false; // 로딩 종료
      }
    },

    // 아이디 입력 시 중복확인 상태 초기화 (변동 없음)
    resetIdCheckStatus() {
      // 아이디 입력창에 글을 다시 입력하면 중복 확인 상태 초기화
      this.isIdChecked = false;
      this.isIdDuplicate = true;
    },

    // ⭐ 이메일 중복 확인 (디바운싱 + 백엔드 통신으로 수정) ⭐
    checkEmailDuplicateWithDebounce() {
      const emailValue = this.email.trim();
      this.isEmailCheckedOnce = true;
      this.isEmailDuplicate = true; // 확인 중에는 일단 중복으로 간주

      // 이메일이 비어있거나 형식이 유효하지 않으면 로직 중단 및 타이머 취소
      if (emailValue.length === 0 || !this.isEmailValid) {
        if (this.$options.debounceTimer) {
          clearTimeout(this.$options.debounceTimer);
        }
        this.isEmailChecking = false;
        return;
      }

      // 기존 타이머가 있으면 취소 (디바운싱의 핵심)
      if (this.$options.debounceTimer) {
        clearTimeout(this.$options.debounceTimer);
      }

      // 중복 확인 중 상태로 표시
      this.isEmailChecking = true;

      // 새 타이머 설정
      this.$options.debounceTimer = setTimeout(async () => {
        try {
          // ⭐ 백엔드 API 호출: GET /api/user/check-email/{email} ⭐
          const response = await axios.get(`${API_URL}/check-email/${emailValue}`);

          // 백엔드에서 true를 반환하면 중복, false를 반환하면 중복 아님
          const isDuplicate = response.data;

          this.isEmailDuplicate = isDuplicate;

        } catch (error) {
          console.error('이메일 중복 확인 실패:', error);
          // 오류 발생 시 사용자에게 알림 없이, 중복으로 간주
          this.isEmailDuplicate = true;
        } finally {
          this.isEmailChecking = false; // 로딩 상태 해제
        }
      }, 500); // 500ms 딜레이
    },

    // 휴대폰 번호 자동 포맷팅 (변동 없음)
    formatPhoneNumber() {
      // ... (기존 코드 유지) ...
      let rawPhoneNumber = this.phone;

      // 숫자만 남김
      let cleanedNumber = rawPhoneNumber.replace(/[^0-9]/g, '');

      // 11자리 초과 방지
      if (cleanedNumber.length > 11) {
        cleanedNumber = cleanedNumber.substring(0, 11);
      }

      let formattedNumber = '';

      // 하이픈(-) 추가 로직
      if (cleanedNumber.length < 4) {
        formattedNumber = cleanedNumber;
      } else if (cleanedNumber.length < 8) {
        formattedNumber = cleanedNumber.substring(0, 3) + '-' + cleanedNumber.substring(3);
      } else {
        formattedNumber =
          cleanedNumber.substring(0, 3) +
          '-' +
          cleanedNumber.substring(3, 7) +
          '-' +
          cleanedNumber.substring(7, 11);
      }

      // data 속성 업데이트
      this.phone = formattedNumber;
    },

    // ⭐ 폼 제출 핸들러 (백엔드 통신으로 수정) ⭐
    async handleSubmit() {
      if (!this.isFormValid) {
        eventBus.emit('show-global-alert', {
          message: '모든 필수 정보를 입력하고 아이디 중복 확인, 비밀번호 및 이메일 조건을 충족해야 합니다.',
          type: 'error'
        });
        return;
      }

      // 1. 서버로 전송할 데이터 객체 생성 (DTO 필드명과 일치시켜야 함)
      const userData = {
        loginId: this.id, // loginId (DTO) -> id (프론트엔드)
        password: this.password,
        name: this.username, // name (DTO) -> username (프론트엔드)
        email: this.email,
        phoneNumber: this.phone // phone_number (DTO) -> phone (프론트엔드)
      };

      console.log('회원가입 정보:', userData);

      try {
        // 2. 백엔드 API 호출: POST /api/user/signup
        const response = await axios.post(`${API_URL}/signup`, userData);

        // 3. 응답 처리
        if (response.status === 201) { // HTTP 201 Created
          console.log('회원가입 성공:', response.data);
          this.showSuccessModal = true; // 성공 모달 표시
        } else {
          // 200번대 상태 코드가 아닌 경우
          eventBus.emit('show-global-alert', {
            message: '회원가입에 실패했습니다. (응답 상태: ' + response.status + ')',
            type: 'error'
          });
        }
      } catch (error) {
        // 4. 에러 처리 (4xx, 5xx 에러 등)
        console.error('회원가입 요청 실패:', error);
        if (error.response && error.response.data) {
          eventBus.emit('show-global-alert', {
            message: '회원가입 요청 중 오류가 발생했습니다: ' + error.response.data,
            type: 'error'
          });
        } else {
          eventBus.emit('show-global-alert', {
            message: '회원가입 요청 중 알 수 없는 오류가 발생했습니다.',
            type: 'error'
          });
        }
      }
    },

    // 로그인 화면으로 이동하는 메서드 추가 (변동 없음)
    navigateToLogin() {
      // ... (기존 코드 유지) ...
      this.showSuccessModal = false; // 모달 닫기
      // $router.replace를 사용하여 로그인 페이지로 이동합니다. (현재 페이지 기록을 대체)
      this.$router.replace({ name: 'login' });
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
  background-color: #4A7CEC;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
}

.check-button:hover {
  background-color: #4A7CEC;
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
  font-size: 12px;
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
  background-color: #4A7CEC;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-button:hover {
  background-color: #4A7CEC;
}

.login-link-wrapper {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #000000;
}

.login-link-wrapper a {
  font-weight: 500;
  color: #4A7CEC;
  text-decoration: none;
}

.login-link-wrapper a:hover {
  text-decoration: underline;
}

.text-input::-ms-reveal,
.text-input::-webkit-reveal {
  display: none;
}

/* ⭐⭐ 아이디 유효성 메시지 스타일 추가 (기존 스타일 재활용) ⭐⭐ */
.validation-message {
  margin-top: 5px;
  font-size: 13px;
  font-weight: 500;
  padding-left: 5px;
}

.validation-message.invalid {
  color: #FF6666;
  /* 빨간색 (새로운 스타일의 오류 색상) */
}

.validation-message.valid {
  color: #4CAF50;
  /* 녹색 */
}

.checking-status {
  color: #4A7CEC;
  /* 버튼 색상과 유사한 파란색 */
}

/* ⭐⭐⭐ 모달 관련 스타일 수정 (ReviewModal 스타일 통일) ⭐⭐⭐ */
/* 기존 .modal-overlay를 .modal-backdrop 스타일로 수정 */
.modal-overlay {
  /* .modal-backdrop과 동일하게 position: absolute를 유지하며, 부모(.signup-container)를 기준으로 합니다. */
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  /* 투명도 0.6에서 0.5로 통일 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 기존 .modal-content를 .review-modal 스타일로 수정 */
.modal-content {
  background: white;
  width: 90%;
  max-width: 400px;
  /* 크기 통일 */
  border-radius: 12px;
  /* 둥근 모서리 통일 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  /* 그림자 통일 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  text-align: center;

  /* ReviewModal의 바디/푸터 스타일을 참고하여 내부 패딩 조정 */
  padding: 30px 20px 20px;
  /* 기존 모달 스타일 유지하면서 ReviewModal 크기/모양 반영 */
}

.modal-content h3 {
  font-size: 20px;
  font-weight: bold;
  /* bold로 통일 */
  color: #333;
  /* ReviewModal 본문 텍스트와 유사하게 변경 */
  margin-bottom: 10px;
  /* 간격 조정 */
}

.modal-content p {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
  /* 간격 조정 */
  line-height: 1.5;
}

/* 기존 .modal-button을 ReviewModal의 .submit-btn 스타일로 수정 */
.modal-button {
  margin-top: 20px;
  width: 100%;
  background-color: #4A7CEC;
  /* 버튼 색상 통일 */
  color: white;
  border: none;
  padding: 14px 0;
  /* 패딩 통일 */
  border-radius: 8px;
  /* 둥근 모서리 통일 */
  font-size: 16px;
  font-weight: bold;
  /* bold로 통일 */
  cursor: pointer;
  transition: background-color 0.2s;
  /* 트랜지션 통일 */
}

.modal-button:hover {
  background-color: #4A7CEC;
  /* 호버 색상 유지 */
}
</style>
