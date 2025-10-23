<template>
  <header class="header">
    <button class="back-btn" @click="goBack">
      <i class="bi bi-arrow-left"></i>
    </button>
    <h1 class="page-title">{{ pageTitle }}</h1>
    <button class="favorite-btn" @click="toggleFavorite">
      <i :class="isFavorite ? 'bi-heart-fill' : 'bi-heart'" :style="{ color: isFavorite ? 'red' : 'black' }"></i>
    </button>
  </header>
</template>

<script>
export default {
  name: 'PageHeader',
  // 부모 컴포넌트로부터 pageTitle을 props로 받습니다.
  props: {
    pageTitle: String,
  },

  data() {
    return {
      isFavorite: false,
    };
  },

  methods: {
    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },
    toggleFavorite() {
      // this.isFavorite 값을 현재 값의 반대(true면 false로, false면 true로)로 바꿉니다.
      this.isFavorite = !this.isFavorite;
    },
  },
};
</script>

<style scoped>
/* === 헤더 스타일 === */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background-color: white;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
  height: 50px;
}

.back-btn,
.favorite-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  /* Bootstrap Icons 크기 조절 */
  line-height: 1;
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}
</style>
