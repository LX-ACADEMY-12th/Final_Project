<template>
  <!-- feedData가 null이 아닐 때만 렌더링하도록 v-if 추가 -->
  <div v-if="feedData" class="view-wrapper feed-view">
    <header class="feed-header">
      <button @click="onBackClick" class="back-button">
        <i class="bi bi-arrow-left"></i>
      </button>
      <h1>{{ feedData.locationName }} 피드</h1>
    </header>
    <main class="feed-content">
      <p>이곳에 {{ feedData.locationName }}의 게시물 목록이 표시됩니다.</p>
      <p>(마커 ID: {{ feedData.markerId }})</p>
    </main>
  </div>
</template>

<script setup>
// 부모로부터 데이터를 받기 위한 props 정의
defineProps({
  feedData: {
    type: Object,
    // (수정) 이제 null을 받을 수 있으므로 required: false로 변경하거나,
    // 혹은 v-if 가드만으로도 충분합니다. 여기서는 그대로 둡니다.
    required: true,
  }
});

// 부모로 이벤트를 전달하기 위한 emit 정의
const emit = defineEmits(['back-to-map']);

const onBackClick = () => {
  emit('back-to-map');
};
</script>

<style scoped>
.view-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.feed-view {
  background-color: #ffffff;
}

.feed-header {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.back-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  margin-right: 15px;
  cursor: pointer;
}

.feed-header h1 {
  font-size: 1.2rem;
  margin: 0;
}

.feed-content {
  flex-grow: 1;
  padding: 20px;
  text-align: center;
}
</style>
