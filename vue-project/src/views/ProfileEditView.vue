<template>
  <div class="profile-edit-container">
    <header class="header">
      <button class="back-button">
        </button>
      <h2>프로필 편집</h2>
      <button @click="handleUpdate" class="complete-button">
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" class="bi bi-check" viewBox="0 0 16 16">
          <path d="M10.97 4.97a.75.75 0 0 1 1.07 1.05l-3.99 4.99a.75.75 0 0 1-1.08.02L4.324 8.384a.75.75 0 1 1 1.06-1.06l2.094 2.093 3.473-4.425z"/>
        </svg>
      </button>
    </header>

    <div class="profile-content">
      <div class="profile-image-section">
        <img :src="profileImage" alt="프로필 이미지" class="profile-image" @click="handleImageChange" />
        <div class="image-overlay">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-camera" viewBox="0 0 16 16">
            <path d="M15 12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h1.172a3 3 0 0 0 2.12-.879l.83-.828A1 1 0 0 1 6.827 3h2.344a1 1 0 0 1 .707.293l.828.828A3 3 0 0 0 12.828 5H14a1 1 0 0 1 1 1zM2 4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1.172a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 9.172 2H6.828a2 2 0 0 0-1.414.586l-.828.828A2 2 0 0 1 3.172 4z"/>
            <path d="M8 11a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5m0 1a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7M3 6.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0"/>
          </svg>
        </div>
      </div>
      
      <div class="input-group">
        <label for="username">사용자 이름</label>
        <input type="text" id="username" v-model="username" placeholder="사용자 이름" class="text-input" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileEdit',
  data() {
    return {
      // 초기 프로필 이미지는 임의의 URL로 설정
      profileImage: 'https://cdn.pixabay.com/photo/2017/09/25/13/12/cocker-spaniel-2785074_1280.jpg',
      username: 'username'
    };
  },
  methods: {
    handleImageChange() {
      // 실제 이미지 변경 로직 (파일 업로드 등)
      alert('갤러리 or 사진촬영 선택');
    },
    handleUpdate() {
      // 사용자 이름 업데이트 로직
      console.log('업데이트된 프로필 정보:', {
        username: this.username
      });
      alert('프로필이 성공적으로 업데이트되었습니다.');
    }
  }
};
</script>

<style scoped>
/* 전역 스타일 (이전과 동일) */
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 프로필 편집 컨테이너 (이전과 동일) */
.profile-edit-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 60px 20px;
  background-color: #fff;
  min-height: 100vh;
  box-sizing: border-box;
}

/*
 * 헤더: 텍스트 중앙 정렬 및 체크 버튼 우측 정렬
 */
.header {
  width: 100%;
  max-width: 360px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
}

.header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 auto; /* 좌우 마진을 auto로 설정하여 중앙 정렬 */
}

/* 뒤로가기 버튼이나 빈 공간을 위한 투명 버튼 */
.back-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  color: #000000;
  width: 40px; /* 완료 버튼과 동일한 크기로 설정 */
}

.complete-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  color: #000000;
}

/* 프로필 내용 컨테이너 (이전과 동일) */
.profile-content {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 프로필 이미지 섹션 (이전과 동일) */
.profile-image-section {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: 40px;
}

.profile-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ddd;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background-color: #000000;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border: 2px solid #fff;
}

.image-overlay svg {
  color: #fff;
}

/* 입력 필드 그룹 (이전과 동일) */
.input-group {
  width: 100%;
  margin-bottom: 20px;
}

.input-group label {
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
</style>