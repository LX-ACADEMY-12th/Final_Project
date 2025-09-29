<template>
  <nav class="bottom-nav">
    <a href="#" class="nav-item active">
      <i class="bi bi-map-fill"></i>
      <span>지도</span>
    </a>
    <a href="#" class="nav-item">
      <i class="bi bi-search"></i>
      <span>검색</span>
    </a>
    <a href="#" class="nav-item" @click="goToMyPage">
      <i class="bi bi-person-fill"></i>
      <span>내정보</span>
    </a>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goToMyPage = () => {
  router.push('/mypage');
}
</script>

<style scoped>
.bottom-nav {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  height: 70px;
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 10;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #999;
  text-decoration: none;
  font-size: 0.7rem;
}

.nav-item.active {
  color: #333;
}

.nav-item i {
  font-size: 1.5rem;
}
</style>
