<template>
  <div class="account-container">
    <div class="account-form">
      <header class="account-header">
        <SmallLogo/>
        <h2 class="account-title">계정 관리</h2>
      </header>

      <main class="menu-container">
        <div class="menu-item">
          <div class="menu-icon">
            <svg width="24" height="24" fill="#424242" viewBox="0 0 16 16">
              <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
            </svg>
          </div>
          <span class="menu-label" @click="goToUserInfoModify()">정보 수정</span>
        </div>
        <div class="menu-item">
          <div class="menu-icon">
            <svg width="24" height="24" fill="#424242" viewBox="0 0 16 16">
              <path fill-rule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"/>
              <path fill-rule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
            </svg>
          </div>
          <span class="menu-label">로그아웃</span>
        </div>
      </main>

    </div>
  </div>
</template>

<script setup>
import SmallLogo from '@/components/SmallLogo.vue';
import router from '@/router';

function goToUserInfoModify() {
  router.push('/userInfoModify')
}

</script>

<style scoped>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.account-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
  background-color: #fff;
  min-height: 100vh;
  box-sizing: border-box;
}

.account-form {
  width: 100%;
  max-width: 380px;
}

.account-header {
  margin-bottom: 32px;
}

.account-title {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0;
  text-align: center;
}

.menu-container {
  background-color: #f7f7f8; 
  border-radius: 12px;
  padding: 8px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #efeff0;
}

.menu-item:first-child {
  border-bottom: 1px solid #e0e0e0;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #000000;
}

.menu-label {
  font-size: 16px;
  color: #000000;
  font-weight: 500;
}
</style>