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

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import VirtualTour from '@/components/virtual/VirtualTour.vue'; // 💡 컴포넌트 임포트
import { tourConfig } from '@/data/tourConfig.js';
import TourBottombar from '@/components/virtual/TourBottombar.vue'; // 💡 컴포넌트 임포트

const virtualTourRef = ref(null);
const currentSceneId = ref(tourConfig.default.firstScene);
const router = useRouter();

// 💡 [추가] 2단계(CourseRecommend.vue)에서 state로 보낸 'items' 배열을 받습니다.
const tourItems = ref(history.state?.items || []);

// 💡 [확인용] 콘솔에 데이터가 잘 들어왔는지 찍어봅니다.
console.log('가상 답사 페이지(TourView)가 전달받은 탭 목록:', tourItems.value);


function handleNavigation(sceneId) {
 virtualTourRef.value?.loadScene(sceneId);
}
function handleSceneChange(newSceneId) {
 currentSceneId.value = newSceneId;
 console.log('✅ [TourView] 씬 변경:', newSceneId); // 👈 디버깅 로그
}
function handleHotspotClick({ hotspot }) {
 const text = hotspot?.text || '';
 if (text.includes('가상 답사를 종료')) {
  router.back();
 }
}

// 💡 [수정] 'active' 버튼을 파악하기 위한 로직
const currentZone = computed(() => {
 const id = currentSceneId.value || ''; // (예: 'hall_1_ex1')
  
  // 현재 씬(id)이 'hall_1'로 시작하면,
  // '창의나래관' 버튼의 sceneId인 'hall_1_entrance'를 반환
 if (id.startsWith('hall_1')) {
    console.log('✅ [TourView] activeZone 계산: hall_1_entrance'); // 👈 디버깅 로그
    return 'hall_1_entrance'; 
  }
 if (id.startsWith('hall_2')) {
    console.log('✅ [TourView] activeZone 계산: hall_2_entrance'); // 👈 디버깅 로그
    return 'hall_2_entrance';
  }
 if (id.startsWith('hall_3')) {
    console.log('✅ [TourView] activeZone 계산: hall_3_entrance'); // 👈 디버깅 로그
    return 'hall_3_entrance';
  }
  
  console.log('✅ [TourView] activeZone 계산: (일치 항목 없음)'); // 👈 디버깅 로그
 return ''; // 그 외에는 빈 값
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