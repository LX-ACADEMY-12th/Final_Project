<template>
 <div class="bottom-nav-bar">
  <div class="nav-buttons">
      
      <template v-for="item in items" :key="item.id">
        
        <button
          v-if="item.sceneId" 
          
          :class="{ 'active': activeZone === item.sceneId }"
          
          @click="emitNavigation(item.sceneId)"
        >
          {{ item.title }} 
        </button>
      </template>
      
  </div>
 </div>
</template>

<script setup>
// 💡 [수정 6] props를 정의하여 'items' 배열을 받습니다.
defineProps({
 activeZone: String,
  items: {
    type: Array,
    default: () => [] // 기본값은 빈 배열
  }
});

// 💡 [수정 7] emit 정의 (기존과 동일)
const emit = defineEmits(['navigate-to']);

// 💡 [수정 8] 클릭 시 sceneId를 emit (기존과 동일)
function emitNavigation(sceneId) {
 emit('navigate-to', sceneId);
}
</script>

<style scoped>
/* (스타일은 변경 없음) */
.bottom-nav-bar {
 flex-shrink: 0;
 width: 100%;
 background-color: rgba(255, 255, 255, 0.8); 
 backdrop-filter: blur(8px);
 box-sizing: border-box;
}
.nav-buttons {
 display: flex;
 justify-content: center;
 align-items: center;
 gap: 10px;
}
.nav-buttons button {
 background: none;
 border: none;
 border-top: 3px solid transparent; 
 color: #555;
 font-size: 15px;
 font-weight: 500;
 cursor: pointer;
 padding: 16px 20px;
 transition: all 0.2s ease-in-out;
}
.nav-buttons button:hover {
 color: #000;
}
.nav-buttons button.active {
 color: #007bff;
 font-weight: 700;
 border-top-color: #007bff;
}
</style>