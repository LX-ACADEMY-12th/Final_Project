<template>
  <div class="map-controls">
    <button @click="$emit('zoom-in')" aria-label="지도 확대">
      <i class="bi bi-zoom-in"></i>
    </button>
    <button @click="$emit('zoom-out')" aria-label="지도 축소">
      <i class="bi bi-zoom-out"></i>
    </button>
    <button @click="$emit('recenter')" aria-label="현재 위치로 이동">
      <i class="bi bi-cursor"></i>
    </button>
  </div>
</template>

<script setup>
// 부모로 이벤트를 전달하기 위해 emit 정의
defineEmits(['zoom-in', 'zoom-out', 'recenter']);
</script>

<style scoped>
.map-controls {
  position: absolute;
  top: 90px;
  right: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 10;
}

.map-controls button {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 1.2rem;
  color: #333;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.2s ease;
}

.map-controls button:hover {
  background-color: #f5f5f5;
}
</style>
