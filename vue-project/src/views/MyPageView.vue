<template>
  <div id="my-page" class="container px-4 py-4">

    <div class="d-flex align-items-center justify-content-between pb-4 border-bottom">
      <button class="btn p-0 me-3 border-0" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <h2 class="h5 mb-0 fw-bold text-dark">마이페이지</h2>
      <div class="btn p-0 me-3 border-0" style="visibility: hidden;">
        <i class="bi bi-arrow-left fs-4"></i>
      </div>
    </div>
    <div class="d-flex align-items-center mt-4 mb-4">
      <div class="position-relative me-3">
        <div
          class="profile-pic rounded-circle d-flex align-items-center justify-content-center bg-body-secondary text-secondary">
          <i class="bi bi-emoji-smile" style="font-size: 2.5rem;"></i>
        </div>
        <button
          class="btn btn-primary rounded-circle p-0 position-absolute profile-badge d-flex align-items-center justify-content-center"
          @click="goToAccountView">
          <i class="bi bi-gear-fill"></i>
        </button>
      </div>
      <div>
        <div class="fw-bold text-dark">김아무개</div>
        <div class="small text-dark">user@naver.com</div>
      </div>
    </div>
    <button type="button" class="p-4 mb-4 text-white custom-rounded stamp-card w-100 text-start">
      <div class="d-flex align-items-center mb-2">
        <i class="bi bi-crosshair me-2 fs-5"></i>
        <span class="fw-bold">스탬프 투어</span>
      </div>
      <div class="display-4 fw-bolder">12 / 20</div>
    </button>
    <button
      class="btn btn-primary w-100 p-3 mb-4 custom-rounded text-start d-flex align-items-center justify-content-between saved-route-btn">
      <div class="d-flex align-items-center">
        <i class="bi bi-bookmark-plus-fill me-2 fs-5"></i>
        <span class="fw-bold">저장된 추천 경로</span>
      </div>
      <i class="bi bi-plus-lg fs-5"></i>
    </button>
    <ul class="list-group list-group-flush">
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-heart-fill me-3 fs-5 text-heart-red"></i>
          <span>관심 전시 목록</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-heart-fill me-3 fs-5 text-heart-red"></i>
          <span>관심 장소 목록</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
      <li data-bs-toggle="modal" data-bs-target="#settingsModal" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-gear-fill me-3 fs-5 text-secondary"></i>
          <span>로그아웃/탈퇴</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
      <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3 px-0">
        <div class="d-flex align-items-center">
          <i class="bi bi-bell-fill me-3 fs-5 text-warning"></i>
          <span>공지사항</span>
        </div>
        <i class="bi bi-chevron-right text-muted"></i>
      </li>
    </ul>

    <div class="modal fade" id="settingsModal" tabindex="-1" aria-labelledby="settingsModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0">
          <div class="modal-body text-center p-4">
            <h5 class="fw-bold text-dark mb-4" id="settingsModalLabel">설정</h5>
            <button type="button" class="btn btn-primary w-100 p-3 fw-bold mb-3 rounded-pill modal-logout-btn">
              로그아웃
            </button>
            <button type="button" class="btn btn-outline-primary w-100 p-3 fw-bold rounded-pill modal-withdraw-btn" data-bs-dismiss="modal">
              회원탈퇴
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyPageView',
  methods: {
    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },
    // 계정설정화면으로 이동하는 함수
    goToAccountView() {
      this.$router.push({ name: 'AccountView' })
    }
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

#my-page {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  background-color: #ffffff;
  min-height: 100vh;
}

.profile-pic {
  width: 60px;
  height: 60px;
}

.profile-badge {
  width: 24px;
  height: 24px;
  bottom: -5px;
  right: -5px;
  font-size: 14px;
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.profile-badge:active {
  filter: brightness(90%);
}

.stamp-card {
  background-color: #8fa4c3;
  border: none;
  transition: filter 0.2s ease-in-out;
}

.stamp-card:active {
  filter: brightness(90%);
}

.saved-route-btn {
  background-color: #3674B5;
  border-color: #3674B5;
  transition: filter 0.2s ease-in-out;
  margin-bottom: 32px;
  /* 추가된 간격 */
}

.saved-route-btn:active {
  filter: brightness(90%);
}

.custom-rounded {
  border-radius: 0.75rem !important;
}

.text-heart-red {
  color: #ea4335;
}

.list-group-item-action {
  cursor: pointer;
}

.list-group-item-action span {
  color: #000000;
}

.list-group-item-action:hover {
  background-color: #ffffff;
}

.list-group-item-action:active {
  background-color: #f8f9fa;
}

.modal-dialog {
  max-width: 35%;
  margin-left: auto;
  margin-right: auto;
}

.modal-logout-btn {
  background-color: #5883e3;
  border-color: #5883e3;
  font-weight: 600;
  font-size: 1rem;
}

.modal-logout-btn:hover {
  background-color: #5883e3;
  border-color: #5883e3;
}

.modal-logout-btn:active {
  filter: brightness(90%);
}

.modal-withdraw-btn {
  color: #5883e3;
  border-color: #5883e3;
  border-width: 1px;
  font-weight: 600;
  font-size: 1rem;
}

.modal-withdraw-btn:active {
  color: #ffffff;
  background-color: #5883e3;
  border-color: #5883e3;
}

.modal-body h5 {
  font-weight: 700;
  font-size: 1.2rem;
}

.btn:hover {
  filter: none;
  opacity: 1;
}
</style>
