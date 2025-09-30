<template>
  <div class="friend-list-container">
    <div class="friend-list-form">
      <header class="friend-list-header">
        <div class="logo">
          <svg width="24" height="24" fill="currentColor" viewBox="0 0 16 16">
            <path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10zm0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6z"/>
          </svg>
          <h1>여행 한 조각</h1>
        </div>
        <h2 class="friend-list-title">친구 목록</h2>
      </header>

      <main>
        <div class="search-bar-wrapper">
          <button type="button" class="search-button">
            <svg width="20" height="20" fill="#BDBDBD" viewBox="0 0 16 16">
              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
            </svg>
          </button>
          <input type="text" class="search-bar" placeholder="">
        </div>

        <ul class="friend-list">
          <li v-for="friend in friends" :key="friend.id" class="friend-item">
            <div class="profile-image-placeholder">
              <svg width="24" height="24" fill="#BDBDBD" viewBox="0 0 16 16">
                <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
              </svg>
            </div>
            <span class="friend-name">{{ friend.name }}</span>
            <button class="unfollow-button">팔로우 취소</button>
            <div class="location-icon">
              <svg width="20" height="20" fill="#212121" viewBox="0 0 16 16">
                <path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10zm0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6z"/>
              </svg>
            </div>
          </li>
        </ul>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const friends = ref([
  { id: 1, name: '조계연' },
  { id: 2, name: '이태민' },
  { id: 3, name: '안병주' },
  { id: 4, name: '이동민' },
  { id: 5, name: '이재빈' },
]);
</script>

<style scoped>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #fff;
}

.friend-list-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
  min-height: 100vh;
  box-sizing: border-box;
}

.friend-list-form {
  width: 100%;
  max-width: 380px;
}

.friend-list-header {
  margin-bottom: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 36px;
  padding-left: 8px;
}

.logo h1 {
  font-size: 20px;
  font-weight: 600;
  color: #000000;
}

.friend-list-title {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0;
  text-align: center;
}

.search-bar-wrapper {
  position: relative;
  margin-bottom: 24px;
}

.search-button {
  position: absolute;
  top: 50%;
  left: 20px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  background: none; 
  border: none; 
  padding: 0; 
  cursor: pointer; 
  outline: none; 
}

.search-bar {
  width: 100%;
  padding: 16px 20px 16px 50px; 
  font-size: 16px;
  border: none;
  border-radius: 28px; 
  background-color: #f7f7f8;
  box-sizing: border-box;
  outline: none;
}

.friend-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.friend-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.profile-image-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.friend-name {
  font-size: 16px;
  font-weight: 500;
  color: #212121;
  flex-grow: 1;
  border-bottom: 1px solid #eee;
  padding-bottom: 4px;
  margin-right: 16px;
}

.unfollow-button {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background-color: #000000;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  margin-right: 12px;
  white-space: nowrap;
}

.location-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
</style>