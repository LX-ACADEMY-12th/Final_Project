<template>
  <div class="friend-list-container">
    <div class="friend-list-form">
      <header class="friend-list-header">
        <SmallLogo/>
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
          <FriendListItem
            v-for="friend in friends"
            :key="friend.id"
            :friend="friend"
          />
        </ul>
      </main>
    </div>
  </div>
</template>

<script setup>
import SmallLogo from '@/components/SmallLogo.vue';
import FriendListItem from '@/components/FriendListItem.vue';
import { ref } from 'vue';

const friends = ref([
  { id: 1, name: '조계연' },
  { id: 2, name: '이태민' },
  { id: 3, name: '안병주' },
  { id: 4, name: '이동민' },
  { id: 5, name: '차민정' },
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
</style>
