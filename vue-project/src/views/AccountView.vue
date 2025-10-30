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

    <!-- @submit.prevent를 사용하여 폼 제출 시 페이지 새로고침 방지 -->
    <form @submit.prevent="handleUpdate">
      <div class="form-group mb-3">
        <label for="login-id" class="form-label">로그인 아이디</label>
        <!-- 아이디는 수정 불가능하게 설정 -->
        <input type="text" class="form-control" id="login-id" v-model="user.loginId" disabled>
      </div>
      <div class="form-group mb-3">
        <label for="username" class="form-label">사용자 이름</label>
        <input type="text" class="form-control" id="username" placeholder="사용자 이름 입력" v-model="user.name">
      </div>
      <div class="form-group mb-3">
        <label for="email" class="form-label">이메일</label>
        <input type="email" class="form-control" id="email" placeholder="이메일 입력" v-model="user.email">
      </div>
      <div class="form-group mb-3">
        <label for="phone" class="form-label">휴대폰 번호</label>
        <input type="tel" class="form-control" id="phone" placeholder="휴대폰 번호 입력" v-model="user.phoneNumber">
      </div>
      <div class="form-group mb-3">
        <label class="form-label">성별</label>
        <div class="d-flex gap-3">
          <button type="button" class="btn w-100"
            :class="user.gender === '남성' ? 'btn-gender-fill' : 'btn-gender-outline'" @click="selectGender('male')">
            남성
          </button>
          <button type="button" class="btn w-100"
            :class="user.gender === '여성' ? 'btn-gender-fill' : 'btn-gender-outline'" @click="selectGender('female')">
            여성
          </button>
        </div>
      </div>
      <div class="form-group mb-3">
        <label for="region" class="form-label">지역</label> 
        <input type="text" class="form-control" id="region" placeholder="지역 입력" v-model="user.region">
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
            :class="{ 'btn-selected': user.childGrade }"
          >
          {{ user.childGrade || '자녀 학년 선택' }}
          </button>
        
          <ul class="dropdown-menu w-100 custom-dropdown-menu">
            <li v-for="grade in childGrades" :key="grade">
              <a 
                class="dropdown-item" 
                href="#" 
                @click.prevent="selectChildGrade(grade)"
                :class="{ 'active': user.childGrade === grade }"
              >
                {{ grade }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 폼 제출 버튼 -->
      <div class="mt-4 mb-4">
        <button type="submit" class="btn btn-primary w-100 py-3 fw-bold submit-btn">수정하기</button>
      </div>
    </form>

  </div>
</template>

<script>
import axios from 'axios';

// API 기본 경로 설정 (MyPageView와 동일하게 설정)
const API_BASE_URL = 'http://localhost:8080/api/user'; 

export default {
  name: 'AccountSettingsView',
  data() {
    return {
      // ⭐ 사용자 정보를 저장할 객체 ⭐
      user: {
        loginId: '',
        name: '',
        email: '',
        phoneNumber: '',
        gender: '남성', // 기본값 설정
        region: '',
        childGrade: '',
        // password는 보안상 여기서는 관리하지 않습니다.
      },

    // ⭐ 드롭다운 항목 정의 ⭐
    childGrades: ['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년']
    };
  },
  created() {
    // 페이지가 생성될 때 사용자 정보를 불러와 폼을 채웁니다.
    this.fetchUserInfo();
  },
  methods: {
    // ⭐ 1. 사용자 정보 조회 및 폼 채우기 ⭐
    async fetchUserInfo() {
      const token = localStorage.getItem('user-auth-token') || sessionStorage.getItem('user-auth-token');

      if (!token) {
        alert('로그인이 필요합니다.');
        this.$router.replace({ name: 'Login' }); // 로그인 페이지로 리다이렉션
        return;
      }

      try {
        // GET /api/user/info API 호출
        const response = await axios.get(`${API_BASE_URL}/info`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        // 응답받은 데이터를 user 객체에 바인딩
        const userInfo = response.data;
        this.user.loginId = userInfo.loginId || '';
        this.user.name = userInfo.name || '';
        this.user.email = userInfo.email || '';
        this.user.phoneNumber = userInfo.phoneNumber || '';
        // DB에 gender 값이 '남성' 또는 '여성'로 저장된다고 가정
        this.user.gender = userInfo.gender || '남성'; 
        this.user.region = userInfo.region || '';
        this.user.childGrade = userInfo.childGrade || '';

      console.log('사용자 정보 로드 성공:', userInfo);

      } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        alert('사용자 정보를 불러오는 데 실패했습니다. 다시 로그인해주세요.');
        // 인증 실패 시 로그인 페이지로 이동
        // // this.$router.replace({ name: 'Login' }); 
      }
    },

    // ⭐ 2. 사용자 정보 업데이트 (수정하기 버튼 클릭 시) ⭐
    async handleUpdate() {
      const token = localStorage.getItem('user-auth-token') || sessionStorage.getItem('user-auth-token');

      if (!token) {
        alert('인증 토큰이 없습니다. 다시 로그인해주세요.');
        return;
      }
      
      // 백엔드에 보낼 데이터 (user 객체 전체)
      const updateData = {
        name: this.user.name,
        email: this.user.email,
        phoneNumber: this.user.phoneNumber,
        gender: this.user.gender,
        region: this.user.region,
        childGrade: this.user.childGrade,
        // loginId는 헤더 토큰에서 추출되지만, 만약을 위해 포함 (백엔드에서 무시해도 됨)
        loginId: this.user.loginId, 
      };

      try {
        // PUT /api/user/update API 호출
        const response = await axios.put(`${API_BASE_URL}/update`, updateData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        // 수정 성공
        if (response.status === 200) {
          alert(response.data); // "사용자 정보가 성공적으로 수정되었습니다."
          // 마이페이지로 복귀하거나 현재 페이지 새로고침
          this.goBack();  
        }
      } catch (error) {
        console.error('정보 수정 실패:', error);
        if (error.response && error.response.data) {
          alert('정보 수정 실패: ' + error.response.data);
        } else {
           alert('정보 수정 중 알 수 없는 오류가 발생했습니다.');
        }
      }   
    },

    // ⭐ 1. 성별 선택 메서드 수정: 'male'/'female' 대신 '남성'/'여성' 저장 ⭐
    selectGender(gender) {
      // 기존: this.user.gender = gender;
      if (gender === 'male') {
          this.user.gender = '남성'; // 'male' 대신 '남성' 저장
      } else if (gender === 'female') {
          this.user.gender = '여성'; // 'female' 대신 '여성' 저장
      }
    },

    // ⭐ 자녀 학년 선택 메서드 추가 ⭐
    selectChildGrade(grade) {
      this.user.childGrade = grade;
    },

    goBack() {
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
