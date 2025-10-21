<template>
  <div id="account-settings" class="container px-4 py-4">

    <div class="d-flex align-items-center justify-content-between pb-4 border-bottom">
      <button class="btn p-0 me-3 border-0" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <h2 class="h5 mb-0 fw-bold text-dark">계정설정</h2>
      <div class="btn p-0 me-3 border-0" style="visibility: hidden;">
        <i class="bi bi-arrow-left fs-4"></i>
      </div>
    </div>
    <div class="d-flex justify-content-center my-4">
      <div class="position-relative">
        <div
          class="profile-pic rounded-circle d-flex align-items-center justify-content-center bg-body-secondary text-secondary">
          <i class="bi bi-emoji-smile" style="font-size: 3rem;"></i>
        </div>
        <button
          class="btn btn-primary rounded-circle p-0 position-absolute profile-badge d-flex align-items-center justify-content-center">
          <i class="bi bi-plus-lg"></i>
        </button>
      </div>
    </div>
    <form>
      <div class="form-group mb-3">
        <label for="username" class="form-label">사용자 이름</label>
        <input type="text" class="form-control" id="username" placeholder="사용자 이름 입력">
      </div>
      <div class="form-group mb-3">
        <label for="email" class="form-label">이메일</label>
        <input type="email" class="form-control" id="email" placeholder="이메일 입력">
      </div>
      <div class="form-group mb-3">
        <label for="phone" class="form-label">휴대폰 번호</label>
        <input type="tel" class="form-control" id="phone" placeholder="휴대폰 번호 입력">
      </div>
      <div class="form-group mb-3">
        <label class="form-label">성별</label>
        <div class="d-flex gap-3">
          <button type="button" class="btn w-100"
            :class="selectedGender === 'male' ? 'btn-gender-fill' : 'btn-gender-outline'" @click="selectGender('male')">
            남성
          </button>
          <button type="button" class="btn w-100"
            :class="selectedGender === 'female' ? 'btn-gender-fill' : 'btn-gender-outline'"
            @click="selectGender('female')">
            여성
          </button>
        </div>
      </div>
      <div class="form-group mb-3">
        <label for="region" class="form-label">지역</label>
        <input type="text" class="form-control" id="region" placeholder="지역 입력">
      </div>
      <div class="form-group mb-3">
        <label for="child-info" class="form-label d-flex justify-content-between align-items-center">
          <span>자녀정보</span>
          <small class="text-muted fw-normal">(자녀의 학년에 맞는 정보를 안내합니다.)</small>
        </label>
        <button type="button" class="btn add-child-btn w-100">
          <i class="bi bi-plus-lg me-2"></i> 자녀 정보 추가하기
        </button>
      </div>
    </form>
    <div class="mt-4 mb-4">
      <button type="button" class="btn btn-primary w-100 py-3 fw-bold submit-btn">수정하기</button>
    </div>

  </div>
</template>

<script>
export default {
  name: 'AccountSettingsView',
  data() {
    return {
      selectedGender: 'male',
    };
  },
  methods: {
    selectGender(gender) {
      this.selectedGender = gender;
    },
    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },
  }
}
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.woff2') format('woff2-variations');
  font-weight: 100 900;
  font-style: normal;
}

#account-settings {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  min-height: 100vh;
  background-color: #ffffff;
  margin: 0 auto;
}

.profile-pic {
  width: 80px;
  height: 80px;
}

.profile-badge {
  width: 28px;
  height: 28px;
  bottom: 0;
  right: 0;
  font-size: 0.9rem;
  border: 2px solid white;
  background-color: #3674B5;
  border-color: #3674B5;
}

.profile-badge:active {
  filter: brightness(90%);
}

.form-label {
  font-weight: 500;
  color: #000;
  margin-bottom: 8px;
}

.form-label small {
  font-size: 0.8rem;
}

.form-control {
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid #DEDEDE;
}

.form-control::placeholder {
  color: #BDBDBD;
}

.form-control:focus {
  border-color: #000;
  box-shadow: none;
}

.btn-gender-outline,
.btn-gender-fill,
.submit-btn,
.add-child-btn {
  border-radius: 12px !important;
  padding-top: 12px;
  padding-bottom: 12px;
  font-weight: 600;
}

.btn-gender-outline {
  background-color: #fff;
  border: 1px solid #ced4da;
  color: #495057;
}

.btn-gender-outline:active {
  background-color: #f8f9fa;
}

.btn-gender-outline:hover {
  background-color: #fff;
  border-color: #ced4da;
  color: #495057;
}

.btn-gender-fill {
  background-color: #3674B5;
  border-color: #3674B5;
  color: #fff;
}

.btn-gender-fill:active {
  filter: brightness(90%);
}

.btn-gender-fill:hover {
  background-color: #3674B5;
  border-color: #3674B5;
  color: #fff;
}

.add-child-btn {
  border: 1px solid #DEDEDE;
  color: #BDBDBD;
  background-color: #fff;
  text-align: left;
  display: flex;
  align-items: center;
}

.add-child-btn:active {
  background-color: #f8f9fa;
}

.submit-btn {
  padding-top: 14px;
  padding-bottom: 14px;
  background-color: #3674B5;
  border-color: #3674B5;
}

.submit-btn:active {
  filter: brightness(90%);
}

.submit-btn.btn-primary:hover {
  background-color: #3674B5;
  border-color: #3674B5;
}
</style>
