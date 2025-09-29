<template>
  <button class="post-creation-fab" @click="goToPostView">
    <i class="bi bi-pencil-fill"></i>
    <span>현재 위치에서 게시물 작성</span>
  </button>
</template>
<script setup>
import { useRouter } from 'vue-router';

// useRouter 훅을 사용해 router 인스턴스를 가져옴
const router = useRouter();

// 버튼을 클릭 시 실행될 함수
const goToPostView = () => {
  // '/post 경로로 페이지를 이동'
  router.push('/post');
};
</script>

<style scoped>
.post-creation-fab {
  position: absolute;
  bottom: 110px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  white-space: nowrap;
  border-radius: 25px;
  border: none;
  background-color: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  color: white;
  font-size: 0.9rem;
  font-weight: bold;
  cursor: pointer;
}
</style>
