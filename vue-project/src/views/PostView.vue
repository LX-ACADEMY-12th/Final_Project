<template>
  <div class="post-creator-container">
    <div class="post-form">
      <header class="post-header">
        <div class="user-info">
          <div class="profile-pic-wrapper">
            <svg width="24" height="24" fill="#333" viewBox="0 0 16 16">
              <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
            </svg>
          </div>
          <span class="username">dbucknerphotography</span>
        </div>
        <button class="cancel-button">취소</button>
      </header>

      <main class="post-content">
        <div class="textarea-wrapper">
          <textarea ref="textareaRef" class="post-textarea" placeholder="무슨 일이 일어나고 있나요?" rows="4"
            @input="autoResizeTextarea"></textarea>
          <button class="icon-button camera-button">
            <svg width="24" height="24" fill="currentColor" viewBox="0 0 16 16">
              <path
                d="M15 12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h1.172a3 3 0 0 0 2.12-.879l.83-.828A1 1 0 0 1 6.827 3h2.344a1 1 0 0 1 .707.293l.828.828A3 3 0 0 0 12.828 5H14a1 1 0 0 1 1 1v6zM2 4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1.172a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 9.172 2H6.828a2 2 0 0 0-1.414.586l-.828-.828A2 2 0 0 1 3.172 4H2z" />
              <path
                d="M8 11a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5zm0 1a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM3 6.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0z" />
            </svg>
          </button>
        </div>

        <div class="photo-placeholder-section">
          <div class="photo-placeholder">
            <svg width="32" height="32" fill="currentColor" viewBox="0 0 16 16">
              <path d="M6.002 5.5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z" />
              <path
                d="M2.002 1a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2h-12zm12 1a1 1 0 0 1 1 1v6.5l-3.777-1.947a.5.5 0 0 0-.577.093l-3.71 3.71-2.66-1.772a.5.5 0 0 0-.63.062L1.002 12V3a1 1 0 0 1 1-1h12z" />
            </svg>
            <span>사진</span>
          </div>
        </div>

        <div class="location-section">
          <label for="location">위치</label>
          <input type="text" id="location" class="location-input" />
          <div class="map-placeholder">
          </div>
        </div>
      </main>

      <footer class="post-footer">
        <button class="register-button">게시물 등록</button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
const textareaRef = ref(null);
const autoResizeTextarea = () => {
  const textarea = textareaRef.value;
  if (textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
  }
};
</script>

<style scoped>
.post-creator-container {
  display: flex;
  justify-content: center;
  padding: 16px;
  min-height: 100vh;
  box-sizing: border-box;
}

.post-form {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
}

/* -------------------- 헤더 스타일 -------------------- */
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-pic-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #EFEFEF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8E8E8E;
}

.username {
  font-size: 14px;
  font-weight: 600;
}

.cancel-button {
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 500;
  color: #000;
  cursor: pointer;
}

/* 메인 콘텐츠 */
.post-content {
  flex-grow: 1;
}

.textarea-wrapper {
  position: relative;
  display: flex;
  align-items: flex-start;
  border-bottom: 1px solid #EFEFEF;
  padding-bottom: 16px;
  margin-bottom: 24px;
}

.post-textarea {
  flex-grow: 1;
  width: 100%;
  border: none;
  resize: none;
  overflow-y: hidden;
  font-size: 16px;
  padding-right: 40px;
  box-sizing: border-box;
  font-family: inherit;
}

.post-textarea:focus {
  outline: none;
}

.post-textarea::placeholder {
  color: #BDBDBD;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  color: #8E8E8E;
}

.camera-button {
  position: absolute;
  top: 0;
  right: 4px;
}

.photo-placeholder {
  width: 100%;
  height: 150px;
  background-color: #F7F7F8;
  border: 2px dashed #E0E0E0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #BDBDBD;
  cursor: pointer;
  transition: background-color 0.2s;
}

.photo-placeholder:hover {
  background-color: #F0F0F0;
}

.photo-placeholder span {
  font-size: 14px;
  font-weight: 500;
}

.location-section {
  margin-bottom: 24px;
}

.location-section label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.location-input {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border: none;
  border-bottom: 1px solid #000;
  box-sizing: border-box;
  margin-bottom: 16px;
}

.location-input:focus {
  outline: none;
}



.map-placeholder {
  width: 100%;
  height: 180px;
  background-color: #F0F0F0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #BDBDBD;
  font-size: 14px;
  background-image: url('');
  background-size: cover;
  background-position: center;
}

/* 푸터 */
.post-footer {
  margin-top: auto;
}

.register-button {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background-color: #333333;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>
