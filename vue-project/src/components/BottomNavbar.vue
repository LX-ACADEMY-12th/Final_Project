<template>
  <nav class="position-absolute bg-white border-top d-flex flex-row justify-content-center align-items-center p-0"
    style="
         width: 100%;
         height: 63px;
         left: 0;
         bottom: 0px;
         z-index: 1030;
       ">

    <a class="nav-item" :class="{ 'active-nav': selectedNavItem === '홈' }" @click="$emit('navigate', '홈')">
      <i class="bi bi-house"></i>
      <span>홈</span>
    </a>
    <a class="nav-item" :class="{ 'active-nav': selectedNavItem === '목록' }" @click="$emit('navigate', '목록')">
      <i class="bi bi-list-ul"></i>
      <span>목록</span>
    </a>
    <a class="nav-item" :class="{ 'active-nav': selectedNavItem === '지도' }" @click="$emit('navigate', '지도')">
      <i class="bi bi-map"></i>
      <span>지도</span>
    </a>
    <a class="nav-item" :class="{ 'active-nav': selectedNavItem === '코스관리' }" @click="$emit('navigate', '코스관리')">
      <i class="bi bi-journal-bookmark"></i>
      <span>코스관리</span>
    </a>
    <a class="nav-item" :class="{ 'active-nav': selectedNavItem === '마이페이지' }" @click="$emit('navigate', '마이페이지')">
      <i class="bi bi-person"></i>
      <span>마이페이지</span>
    </a>

  </nav>
</template>

<script setup>
// 부모로부터 현재 활성화된 탭이 무엇인지 props로 받습니다.
defineProps({
  selectedNavItem: String
});
// 부모에게 어떤 탭이 클릭되었는지 emit으로 알립니다.
defineEmits(['navigate']);
</script>

<style scoped>
.nav-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 96px;
  gap: 2px;
  height: 61px;
  text-decoration: none;
  color: #888;
  font-family: 'SUIT', sans-serif;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
}

.nav-item .bi {
  font-size: 1.25rem;
}

/* .router-link-active 대신 .active-nav를 다시 사용합니다. */
.nav-item.active-nav {
  color: #0d6efd;
  font-weight: 700;
}
</style>
