<template>
  <header class="gnb">
    <div id="gnb-profile" class="profile-icon" @click="goToMyPage">
      <i class="bi bi-person-circle"></i>
    </div>
    <div id="gnb-post-creation" class="post-creation-input" @click="goToPostView">
      <span>000에서 무슨 생각을 하고 계신가요?</span>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';
// useRouter 훅을 사용해 router 인스턴스를 가져옴
const router = useRouter();

const goToPostView = () => {
  router.push('/post');
}
const goToMyPage = () => {
  router.push('/mypage')
}
</script>

<style scoped>
.gnb {
  position: absolute;
  top: 20px;
  left: 15px;
  right: 15px;
  z-index: 10;
  display: flex;
  padding: 4px 8px;
  align-items: center;
  background-color: #ffffff;
  border-radius: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  gap: 10px;
}

.profile-icon {
  font-size: 2.2rem;
  color: #555;
  cursor: pointer;
}

.post-creation-input {
  flex-grow: 1;
  padding: 8px 16px;
  background-color: #f0f2f5;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #555;
}
</style>
