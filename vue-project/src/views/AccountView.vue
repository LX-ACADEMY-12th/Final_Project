<template>
  <div class="mypage-root d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">

    <div class="mypage-header d-flex justify-content-between align-items-center">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold"
        style="flex: 1; text-align: center; font-size: 1rem; font-weight: 700; color: #111827;">
        계정설정
      </div>
      <div class="header-right d-flex justify-content-end" style="flex: 1;"></div>
    </div>

    <div class="mypage-scroll flex-grow-1">

      <div class="d-flex justify-content-center my-4">
        <div class="position-relative">
          <div
            class="profile-pic rounded-circle d-flex align-items-center justify-content-center bg-body-secondary text-secondary">
            <img v-if="imagePreviewUrl" :src="imagePreviewUrl" alt="프로필 미리보기" class="profile-pic-image" />
            <i v-else class="bi bi-emoji-smile" style="font-size: 3rem;"></i>
          </div>
          <button
            class="btn btn-primary rounded-circle p-0 position-absolute profile-badge d-flex align-items-center justify-content-center"
            @click="triggerFileUpload">
            <i class="bi bi-plus-lg"></i>
          </button>
        </div>
      </div>
      <input type="file" ref="fileInput" @change="onFileSelected" accept="image/*" style="display: none;" />

      <form @submit.prevent="handleUpdate">
        <div class="form-group mb-3">
          <label for="login-id" class="form-label">로그인 아이디</label>
          <input type="text" class="form-control" id="login-id" v-model="localUser.loginId" disabled />
        </div>

        <div class="form-group mb-3">
          <label for="username" class="form-label">사용자 이름</label>
          <input type="text" class="form-control" id="username" placeholder="사용자 이름 입력" v-model="localUser.name" />
        </div>

        <div class="form-group mb-3">
          <label for="email" class="form-label">이메일</label>
          <input type="email" class="form-control" id="email" placeholder="이메일 입력" v-model="localUser.email" />
        </div>

        <div class="form-group mb-3">
          <label for="phone" class="form-label">휴대폰 번호</label>
          <input type="tel" class="form-control" id="phone" placeholder="휴대폰 번호 입력" v-model="localUser.phoneNumber" />
        </div>

        <div class="form-group mb-3">
          <label class="form-label">성별</label>
          <div class="d-flex gap-3">
            <button type="button" class="btn w-100"
              :class="localUser.gender === '남성' ? 'btn-gender-fill' : 'btn-gender-outline'"
              @click="selectGender('male')">남성</button>
            <button type="button" class="btn w-100"
              :class="localUser.gender === '여성' ? 'btn-gender-fill' : 'btn-gender-outline'"
              @click="selectGender('female')">여성</button>
          </div>
        </div>

        <div class="form-group mb-3">
          <label for="region" class="form-label">지역</label>
          <input type="text" class="form-control" id="region" placeholder="지역 입력" v-model="localUser.region" />
        </div>

        <div class="form-group mb-3">
          <label for="child-grade" class="form-label d-flex justify-content-between align-items-center">
            <span class="fw-medium">자녀정보 (학년)</span>
            <small class="text-muted fw-normal">(자녀의 학년에 맞는 정보를 안내합니다.)</small>
          </label>

          <div class="btn-group w-100 dropup">
            <button type="button" class="btn dropdown-toggle w-100 dropup-btn" data-bs-toggle="dropdown"
              aria-expanded="false" :class="{ 'btn-selected': localUser.childGrade }">
              {{ localUser.childGrade || '자녀 학년 선택' }}
            </button>

            <ul class="dropdown-menu w-100 custom-dropdown-menu">
              <li v-for="grade in childGrades" :key="grade">
                <a class="dropdown-item" href="#" @click.prevent="selectChildGrade(grade)"
                  :class="{ 'active': localUser.childGrade === grade }">
                  {{ grade }}
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div class="mt-4 mb-4">
          <button type="submit" class="btn btn-primary w-100 py-3 fw-bold submit-btn">수정하기</button>
        </div>
      </form>

    </div>
  </div>
</template>

<script>
// (Script 태그 내용은 변경 사항 없음)
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import eventBus from '@/utils/eventBus';

export default {
  name: 'AccountSettingsView',
  setup() {
    const authStore = useAuthStore();
    const { user, isLoggedIn } = storeToRefs(authStore);
    return { authStore, user, isLoggedIn };
  },
  data() {
    return {
      localUser: {
        loginId: '',
        name: '',
        email: '',
        phoneNumber: '',
        gender: '남성',
        region: '',
        childGrade: '',
      },
      childGrades: ['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년'],
      selectedFile: null,
      imagePreviewUrl: null,
    };
  },
  created() {
    this.initializeFormFromPinia();
    if (this.user && this.user.profileImageUrl) {
      this.imagePreviewUrl = this.user.profileImageUrl;
    }
  },
  methods: {
    goBack() { this.$router.back(); },
    triggerFileUpload() { this.$refs.fileInput.click(); },
    onFileSelected(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e) => { this.imagePreviewUrl = e.target.result; };
      reader.readAsDataURL(file);
    },
    initializeFormFromPinia() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => { this.$router.push({ name: 'login' }); }
        });
        return;
      }
      if (this.user) {
        this.localUser.loginId = this.user.loginId || '';
        this.localUser.name = this.user.name || '';
        this.localUser.email = this.user.email || '';
        this.localUser.phoneNumber = this.user.phoneNumber || '';
        this.localUser.gender = this.user.gender || '남성';
        this.localUser.region = this.user.region || '';
        this.localUser.childGrade = this.user.childGrade || '';
      } else {
        eventBus.emit('show-global-confirm', {
          message: '사용자 정보를 불러오는 데 실패했습니다. 다시 로그인해주세요.',
          onConfirm: () => { this.$router.push({ name: 'login' }); }
        });
      }
    },
    async handleUpdate() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '인증 토큰이 없습니다. 다시 로그인 해주세요.',
          onConfirm: () => { this.$router.push({ name: 'login' }); }
        });
        return;
      }
      const updateData = {
        name: this.localUser.name,
        email: this.localUser.email,
        phoneNumber: this.localUser.phoneNumber,
        gender: this.localUser.gender,
        region: this.localUser.region,
        childGrade: this.localUser.childGrade,
        loginId: this.localUser.loginId,
      };
      const formData = new FormData();
      formData.append('dto', new Blob([JSON.stringify(updateData)], { type: 'application/json' }));
      if (this.selectedFile) {
        formData.append('profileImage', this.selectedFile, this.selectedFile.name);
      }
      try {
        await this.authStore.updateUser(formData);
        eventBus.emit('show-global-alert', { message: '사용자 정보가 성공적으로 수정되었습니다.', type: 'success' });
        this.goBack();
      } catch (error) {
        console.error('정보 수정 실패:', error);
        const msg = error?.response?.data?.message || error?.response?.data || '정보 수정 중 알 수 없는 오류가 발생했습니다.';
        alert(`정보 수정 실패: ${msg}`);
      }
    },
    selectGender(gender) {
      this.localUser.gender = gender === 'male' ? '남성' : '여성';
    },
    selectChildGrade(grade) {
      this.localUser.childGrade = grade;
    },
  }
};
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-style: normal;
}

/* 🟢 [수정]
  #account-settings -> .mypage-root (MyPageView와 통일)
*/
.mypage-root {
  font-family: 'SUIT Variable', sans-serif;
  margin: 0 auto;
  background-color: #ffffff;
  width: 100%;
  /* 스크롤 구조 핵심 */
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100vh;
  overflow: hidden;
}

/* 🟢 [추가]
  MyPageView의 .mypage-header 스타일
*/
.mypage-header {
  display: flex;
  height: 63px;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1020;
  background-color: #FFFFFF;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

/* 🟢 [수정]
  .content-wrapper -> .mypage-scroll (MyPageView와 통일)
  padding-bottom은 하단 탭이 없으므로 80px 대신 24px 정도로 설정
*/
.mypage-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 🟢 MyPageView와 동일한 좌우 여백(16px) 및 하단 여백(24px) 적용 */
  padding: 12px 16px 24px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.mypage-scroll::-webkit-scrollbar {
  display: none;
}


/* ( ... 이하 폼, 버튼, 드롭다운 등 기존 스타일은 그대로 유지 ... ) */

/* ── 프로필 업로드 ───────────────── */
.profile-pic {
  width: 80px;
  height: 80px;
  overflow: hidden;
}

/* ( ... ) */
.profile-pic-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

/* ── 폼 공통 ───────────────── */
.form-label {
  font-weight: 500;
  color: #000;
  margin-bottom: 8px;
}

.form-label small {
  font-size: 0.8rem;
}

.form-control,
.dropup-btn {
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid #DEDEDE;
}

.form-control::placeholder {
  color: #BDBDBD;
}

.form-control:focus,
.dropup-btn:focus {
  border-color: #000;
  box-shadow: none;
}

/* ── 성별/버튼 ───────────────── */
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

.btn-gender-outline:hover {
  background-color: #fff;
  border-color: #ced4da;
  color: #495057;
}

.btn-gender-outline:active {
  background-color: #f8f9fa;
}

.btn-gender-fill {
  background-color: #3674B5;
  border-color: #3674B5;
  color: #fff;
}

.btn-gender-fill:hover {
  background-color: #3674B5;
  border-color: #3674B5;
  color: #fff;
}

.btn-gender-fill:active {
  filter: brightness(90%);
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

.submit-btn.btn-primary:hover {
  background-color: #3674B5;
  border-color: #3674B5;
}

.submit-btn:active {
  filter: brightness(90%);
}

/* ── Dropup ───────────────── */
.dropup-btn {
  background-color: #fff;
  color: #BDBDBD;
  text-align: left;
  border-color: #DEDEDE;
}

.dropup-btn.btn-selected {
  color: #495057;
}

.custom-dropdown-menu {
  border-radius: 12px;
  border: 1px solid #DEDEDE;
  box-shadow: 0 4px 6px rgba(0, 0, 0, .1);
}

.dropdown-item {
  padding: 10px 16px;
  font-weight: 500;
}

.dropdown-item:hover,
.dropdown-item:focus {
  background-color: #f8f9fa;
  color: #000;
}

.dropdown-item.active,
.dropdown-item:active {
  background-color: #3674B5;
  color: #fff;
}

.dropup-btn:focus {
  box-shadow: none !important;
}
</style>
