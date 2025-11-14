<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 
    App.vue 에서는 다른 컴포넌트 없이 RouterView만 남겨둡니다.
    이렇게 하면 HomeView.vue가 화면 전체를 차지하게 됩니다.
  -->
  <RouterView />
</template>

<style>
/* HomeView.vue의 vh-100 (뷰포트 높이 100%)가
  제대로 작동하려면 html, body, #app(Vue 앱 루트)의
  높이도 100%로 설정해주는 것이 좋습니다.
  이 스타일은 전역(scoped 아님)으로 적용합니다.
*/
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 관리자 페이지 자체 스크롤을 사용하므로 body 스크롤 방지 */
}

@font-face {
  font-family: 'SUIT Variable';
  src: url('./assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-display: swap;
}

* {
  font-family: 'SUIT Variable', sans-serif;
}

</style>