<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app-container">
    <RouterView />
  </div>
</template>

<style>
html,
body {
  height: 100%;
  width: 100%;
  margin: 0;
  /* 원하는 배경색 */
  background-color: #E2EBFB;
}

/* #app을 Flex 컨테이너로 만들고 자식 요소를 중앙 정렬합니다. */
#app {
  width: 100%;
  height: 100%;
  display: flex;
  /* 가로 중앙 정렬 */
  justify-content: center;
  /* 세로 중앙 정렬 */
  align-items: center;
  padding: 0;
}
</style>

<style scoped>
#app-container {
  width: 412px;
  height: 917px;
  position: relative;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
}
</style>
