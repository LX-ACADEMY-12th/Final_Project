<template>
 <div class="tour-page-layout">
  <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        가상 답사
      </div>
      <div class="header-right" style="flex: 1;"></div>
  </div>

      <div class="main-content">
        <VirtualTour
          ref="virtualTourRef"
          :config="tourConfig"
          @scene-changed="handleSceneChange"
          @hotspot-click="handleHotspotClick"
        />
  </div>

      <TourBottombar
        :active-zone="currentZone"
            :items="tourItems" 
        @navigate-to="handleNavigation"
      />
 </div>
</template>

// TourView.vue

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import VirtualTour from '@/components/virtual/VirtualTour.vue';
import { tourConfig } from '@/data/tourConfig.js';
import TourBottombar from '@/components/virtual/TourBottombar.vue';

const virtualTourRef = ref(null);
const currentSceneId = ref(tourConfig.default.firstScene);
const router = useRouter();

const tourItems = ref(history.state?.items || []);
console.log('가상 답사 페이지(TourView)가 전달받은 탭 목록:', tourItems.value);


// ✨ [추가] 1. 탭 데이터가 '입구' 기준인지 '상세' 기준인지 확인
const isEntranceView = computed(() => {
  if (!tourItems.value || tourItems.value.length === 0) {
    return false; // 기본값
  }
  // 탭 목록에 '_entrance'로 끝나는 sceneId가 하나라도 있으면 "입구 뷰"
  return tourItems.value.some(item => item.sceneId && item.sceneId.endsWith('_entrance'));
});


function handleNavigation(sceneId) {
 virtualTourRef.value?.loadScene(sceneId);
}
function handleSceneChange(newSceneId) {
 currentSceneId.value = newSceneId;
 console.log('✅ [TourView] 씬 변경:', newSceneId);
}
function handleHotspotClick({ hotspot }) {
  const text = hotspot?.text || '';
  if (!text.includes('가상 답사를 종료')) return;

  // 돌아오자마자 추천 탭 열도록 플래그
  sessionStorage.setItem('pdv:returnFromTour', '1');

  // 상세 복귀 정보
  const raw = sessionStorage.getItem('pdv:returnTo');
  if (raw) {
    const { type, id, query } = JSON.parse(raw);
    const name = type === 'science_place' ? 'PlaceDetail' : 'ExhibitionDetail';

    // ❗히스토리 무시하고 강제 이동
    router.replace({ name, params: { id }, query });
  } else {
    router.replace({ name: 'Home' });
  }
}

// 💡 [수정] 2. 'active' 버튼을 파악하기 위한 로직
const currentZone = computed(() => {
 const id = currentSceneId.value || '';
  
  // ✨ [수정] 
  // 1. "입구 뷰" (AI 추천 코스)일 때만 기존 그룹핑 로직을 사용합니다.
  if (isEntranceView.value) {
    if (id.startsWith('hall_1')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_1_entrance');
      return 'hall_1_entrance'; 
    }
    if (id.startsWith('hall_2')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_2_entrance');
      return 'hall_2_entrance';
    }
    if (id.startsWith('hall_3')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_3_entrance');
      return 'hall_3_entrance';
    }
    console.log('✅ [TourView] activeZone 계산 (입구 모드): (일치 항목 없음)');
    return '';

  // 2. "상세 뷰" (관심 코스)일 때는 씬 ID를 그대로 반환합니다.
  } else {
    console.log(`✅ [TourView] activeZone 계산 (상세 모드): ${id}`);
    return id; 
  }
});

function goBack() { router.back(); }
</script>

<style scoped>
/* (스타일은 변경 없음) */
.tour-page-layout {
 display: flex;
 flex-direction: column;
 width: 100%;
 height: 100vh;
 overflow: hidden;
}
.main-content {
 flex-grow: 1;
 height: 100%;
 overflow: hidden;
 position: relative;
}
</style>