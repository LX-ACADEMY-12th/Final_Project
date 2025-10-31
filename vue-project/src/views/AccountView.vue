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
    
    <form @submit.prevent="handleUpdate">
      <div class="form-group mb-3">
        <label for="login-id" class="form-label">로그인 아이디</label>
        <input type="text" class="form-control" id="login-id" v-model="localUser.loginId" disabled>
      </div>
      <div class="form-group mb-3">
        <label for="username" class="form-label">사용자 이름</label>
        <input type="text" class="form-control" id="username" placeholder="사용자 이름 입력" v-model="localUser.name">
      </div>
      <div class="form-group mb-3">
        <label for="email" class="form-label">이메일</label>
        <input type="email" class="form-control" id="email" placeholder="이메일 입력" v-model="localUser.email">
      </div>
      <div class="form-group mb-3">
        <label for="phone" class="form-label">휴대폰 번호</label>
        <input type="tel" class="form-control" id="phone" placeholder="휴대폰 번호 입력" v-model="localUser.phoneNumber">
      </div>
      <div class="form-group mb-3">
        <label class="form-label">성별</label>
        <div class="d-flex gap-3">
          <button type="button" class="btn w-100"
            :class="localUser.gender === '남성' ? 'btn-gender-fill' : 'btn-gender-outline'" @click="selectGender('male')">
            남성
          </button>
          <button type="button" class="btn w-100"
            :class="localUser.gender === '여성' ? 'btn-gender-fill' : 'btn-gender-outline'" @click="selectGender('female')">
            여성
          </button>
        </div>
      </div>
      <div class="form-group mb-3">
        <label for="region" class="form-label">지역</label> 
        <input type="text" class="form-control" id="region" placeholder="지역 입력" v-model="localUser.region">
      </div>
      
      <div class="form-group mb-3">
        <label for="child-grade" class="form-label d-flex justify-content-between align-items-center">
          <span class="fw-medium">자녀정보 (학년)</span>
          <small class="text-muted fw-normal">(자녀의 학년에 맞는 정보를 안내합니다.)</small>
        </label>
        <div class="btn-group w-100 dropup">
          <button 
            type="button" 
            class="btn dropdown-toggle w-100 dropup-btn" 
            data-bs-toggle="dropdown" 
            aria-expanded="false"
            :class="{ 'btn-selected': localUser.childGrade }"
          >
          {{ localUser.childGrade || '자녀 학년 선택' }}
          </button>
        
          <ul class="dropdown-menu w-100 custom-dropdown-menu">
            <li v-for="grade in childGrades" :key="grade">
              <a 
                class="dropdown-item" 
                href="#" 
                @click.prevent="selectChildGrade(grade)"
                :class="{ 'active': localUser.childGrade === grade }"
              >
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
</template>

<script>
// ❌ axios import 제거 (Pinia 액션 내에서 사용되도록 위임)
// 🟢 Pinia 스토어 import는 유지
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
// ❌ useRouter import 제거 (this.$router 사용)
import eventBus from '@/utils/eventBus';

export default {
  name: 'AccountSettingsView',

  // setup()은 그대로 유지
  setup() {
    const authStore = useAuthStore();
    const { user, isLoggedIn } = storeToRefs(authStore);

    return {
      authStore,
      user, 
      isLoggedIn, 
    };
  },

  // data()는 그대로 유지
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
      childGrades: ['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년']
    };
  },

  created() {
    this.initializeFormFromPinia();
  },

  methods: {

    goBack() {
      this.$router.back();
    },

    // ⭐ 1. Pinia의 user 정보를 localUser로 복사 및 로그인 확인 ⭐
    initializeFormFromPinia() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      if (this.user) {
        // Pinia user 데이터를 localUser로 복사 (이 부분이 중요)
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
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }
    },

    // ⭐ 2. 사용자 정보 업데이트 (수정하기 버튼 클릭 시) ⭐
    async handleUpdate() {
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '인증 토큰이 없습니다. 다시 로그인 해주세요.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      // 🟢 [수정] 백엔드에 보낼 데이터는 localUser의 현재 값을 사용
      const updateData = {
        name: this.localUser.name,
        email: this.localUser.email,
        phoneNumber: this.localUser.phoneNumber,
        gender: this.localUser.gender,
        region: this.localUser.region,
        childGrade: this.localUser.childGrade,
        loginId: this.localUser.loginId,
      };

      try {
        // 🟢 Pinia 액션 호출 및 localUser 데이터를 전달
        await this.authStore.updateUser(updateData);

        alert('사용자 정보가 성공적으로 수정되었습니다.');
        this.goBack();
 
      } catch (error) {
        console.error('정보 수정 실패:', error);
        const errorMessage = error.response?.data?.message || error.response?.data || '정보 수정 중 알 수 없는 오류가 발생했습니다.';
        alert(`정보 수정 실패: ${errorMessage}`);
      } 
    },

    // ⭐ 1. 성별 선택 메서드 수정: 'male'/'female' 대신 '남성'/'여성' 저장 ⭐
    selectGender(gender) {
      // 🟢 [수정] this.user 대신 this.localUser를 사용
      if (gender === 'male') {
        this.localUser.gender = '남성'; 
      } else if (gender === 'female') {
        this.localUser.gender = '여성'; 
      }
    },

    // ⭐ 자녀 학년 선택 메서드 추가 ⭐
    selectChildGrade(grade) {
      // 🟢 [수정] this.user 대신 this.localUser를 사용
      this.localUser.childGrade = grade;
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

.container {
  flex: 1;
  overflow-y: auto;

  /* 스크롤바 숨기기 */
  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;
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

.form-control,
.dropup-btn { /* dropup-btn에도 공통 스타일 적용 */
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid #DEDEDE;
}

.form-control::placeholder {
  color: #BDBDBD;
}

.form-control:focus,
.dropup-btn:focus { /* 포커스 스타일 통일 */
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

/* ⬇️ Dropup 버튼 관련 스타일 ⬇️ */

/* 버튼 배경 및 텍스트 색상 기본값 */
.dropup-btn {
  background-color: #fff;
  color: #BDBDBD; /* 기본 텍스트 색상 (placeholder) */
  text-align: left;
}

/* 선택되었을 때의 버튼 색상 */
.dropup-btn.btn-selected {
  color: #495057; /* 선택 후 텍스트 색상 */
}

/* 드롭다운 버튼의 배경과 테두리를 .form-control과 동일하게 유지 */
.dropup-btn {
    border-color: #DEDEDE;
}

/* 드롭다운 메뉴 자체 스타일 (테두리 둥글게) */
.custom-dropdown-menu {
    border-radius: 12px;
    border: 1px solid #DEDEDE;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 드롭다운 항목 */
.dropdown-item {
    padding: 10px 16px;
    font-weight: 500;
}

/* 드롭다운 항목 호버/포커스 시 */
.dropdown-item:hover,
.dropdown-item:focus {
    background-color: #f8f9fa; /* 은은한 배경색 */
    color: #000;
}

/* 선택된 항목 (active) 스타일 */
.dropdown-item.active,
.dropdown-item:active {
    background-color: #3674B5; /* 파란색 배경 */
    color: #fff;
}

/* Bootstrap의 드롭다운 토글 버튼의 포커스 시 박스 쉐도우 제거 통일 */
.dropup-btn:focus {
    box-shadow: none !important;
}

/* ⬆️ Dropup 버튼 관련 스타일 끝 ⬆️ */

</style>
