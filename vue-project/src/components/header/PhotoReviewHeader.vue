<template>
  <header class="header">
    <button class="back-btn" @click="goBack">
      <i class="bi bi-arrow-left"></i>
    </button>
    
    <h1 class="page-title">사진후기 전체보기</h1>
    
    <div class="header-placeholder"></div>
  </header>
</template>

<script>
export default {
  name: 'PhotosHeader',
  
  methods: {
    goBack() {
      this.$router.back();
    },
  },
};
</script>

<style scoped>
/* === 헤더 스타일 === */
.header {
  display: flex;
  justify-content: space-between; /* 3개 항목의 좌/중앙/우 정렬 */
  align-items: center;
  padding: 10px 20px;
  background-color: white;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
  height: 50px;
}

/* [★수정] .favorite-btn 제거, .header-placeholder 추가 */
.back-btn,
.header-placeholder {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
  padding: 0;

  /* [★추가] 너비를 고정하여 양쪽 균형을 맞춥니다. */
  width: 24px; 
}

/* [★추가] placeholder는 클릭되지 않도록 설정 */
.header-placeholder {
  cursor: default;
  pointer-events: none;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  
  /* [참고] 
    양쪽에 24px의 고정 너비가 생겼으므로 
    제목은 자동으로 정중앙에 위치하게 됩니다.
  */
}
</style>