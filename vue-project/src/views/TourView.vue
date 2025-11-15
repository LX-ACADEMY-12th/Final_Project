<template>
  <div class="tour-page-layout">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        가상 관람
      </div>
      <div class="header-right" style="flex: 1;"></div>
    </div>

    <div class="main-content">
      <VirtualTour ref="virtualTourRef" :config="dynamicTourConfig" @scene-changed="handleSceneChange"
        @hotspot-click="handleHotspotClick" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import VirtualTour from '@/components/virtual/VirtualTour.vue';

// 1. 마스터 설정 파일로 가져옵니다.
import { tourConfig as masterTourConfig } from '@/data/tourConfig.js';

const virtualTourRef = ref(null);
const router = useRouter();

// [수정 1]
// PlaceDetailsView의 beforeRouteLeave가 저장한 sessionStorage에서 코스 데이터를 불러옵니다.
const tourItems = ref([]); // 기본값은 빈 배열

try {
  // 1-1. PlaceDetailsView가 저장해 둔 '캐시 키'를 불러옵니다.
  const cacheKey = sessionStorage.getItem('pdv:courseCacheKey');

  if (cacheKey) {
    // 1-2. 그 '캐시 키'를 이용해 실제 코스 데이터를 불러옵니다.
    const rawData = sessionStorage.getItem(cacheKey);

    if (rawData) {
      const payload = JSON.parse(rawData);
      // 1-3. items 배열을 tourItems.value에 할당합니다.
      if (payload && Array.isArray(payload.items)) {
        tourItems.value = payload.items;
      }
    }
  }
} catch (e) {
  console.error("세션 스토리지에서 투어 아이템 로딩 실패:", e);
  tourItems.value = []; // 에러 발생 시 빈 배열로 초기화
}

console.log('가상 답사 페이지(TourView)가 (세션에서) 전달받은 탭 목록:', tourItems.value);


// ★ [수정 2] ★
// 전달받은 'tourItems'를 기반으로 동적 tourConfig를 생성합니다.
const dynamicTourConfig = computed(() => {
  console.log("동적 투어 설정 계산 시작. 아이템:", tourItems.value);

  // 2-1. 받은 아이템이 없으면, 마스터(기존) 설정을 그대로 반환
  if (!tourItems.value || tourItems.value.length === 0) {
    console.warn("전달받은 tourItems가 없습니다. 마스터 설정을 사용합니다.");
    return masterTourConfig;
  }

  // 2-2. 'tourItems'에서 sceneId 목록을 추출
  const allowedSceneIds = new Set(tourItems.value.map(item => item.sceneId).filter(Boolean));

  if (allowedSceneIds.size === 0) {
    console.warn("tourItems에 유효한 sceneId가 없습니다. tourMapper.js를 확인하세요. 마스터 설정을 사용합니다.");
    return masterTourConfig;
  }
  console.log("허용된 씬 ID:", allowedSceneIds);

  // 2-3. 새 설정 객체 생성 (기본 구조 복사)
  const newConfig = {
    default: { ...masterTourConfig.default }, // autoLoad, sceneFadeDuration 등 복사
    scenes: {}
  };

  // 2-4. 마스터 설정에서 허용된 씬만 깊은 복사
  allowedSceneIds.forEach(sceneId => {
    if (masterTourConfig.scenes[sceneId]) {
      // JSON.parse(JSON.stringify(...))로 깊은 복사
      newConfig.scenes[sceneId] = JSON.parse(JSON.stringify(masterTourConfig.scenes[sceneId]));
    } else {
      console.warn(`[데이터 불일치] 마스터 설정(tourConfig.js)에 '${sceneId}' 씬이 없습니다.`);
    }
  });

  // 2-5. (★중요★) 핫스팟 필터링: 추천 목록 내의 씬들끼리만 이동하도록
  Object.keys(newConfig.scenes).forEach(sceneId => {
    const scene = newConfig.scenes[sceneId];
    if (scene.hotSpots) {
      scene.hotSpots = scene.hotSpots.filter(hotspot => {
        // 'info' 타입 핫스팟 (e.g., '답사 종료')은 무조건 유지
        if (hotspot.type !== 'scene') {
          return true;
        }
        // 'scene' 타입이면, 'sceneId'가 우리가 허용한 목록(allowedSceneIds)에 있는지 확인
        return allowedSceneIds.has(hotspot.sceneId);
      });
    }
  });

  // [안전 장치]
  // 필터링 결과 씬이 하나도 없다면 (데이터 불일치로 인해),
  // 원본 masterConfig를 반환해서 최소한 기본 파노라마라도 띄웁니다.
  if (Object.keys(newConfig.scenes).length === 0) {
    console.error("!!!! 치명적 오류: tourItems의 sceneId와 tourConfig.js의 씬이 하나도 일치하지 않습니다.");
    console.error("!!!! tourMapper.js와 tourConfig.js의 씬 ID를 확인하세요. 기본 투어로 강제 대체합니다.");
    return masterTourConfig;
  }

  // 2-6. 첫 번째 씬 설정 (tourItems의 첫 번째 항목)
  const firstValidSceneId = tourItems.value.map(item => item.sceneId).find(id => allowedSceneIds.has(id));
  newConfig.default.firstScene = firstValidSceneId || masterTourConfig.default.firstScene; // 유효한 ID가 없으면 마스터 기본값

  console.log("새로 생성된 동적 투어 설정:", newConfig);
  return newConfig;
});


// [수정 3]
// currentSceneId의 기본값을 '동적' config에서 가져옵니다.
const currentSceneId = ref(dynamicTourConfig.value.default.firstScene);


// --- 이하 기존 로직 유지 ---

function handleNavigation(sceneId) {
  virtualTourRef.value?.loadScene(sceneId);
}

function handleSceneChange(newSceneId) {
  currentSceneId.value = newSceneId;
  console.log('✅ [TourView] 씬 변경:', newSceneId);
}

//
// ★★★ [수정된 부분] ★★★
//
function handleHotspotClick({ hotspot }) {
  const text = hotspot?.text || '';

  // '가상 답사를 종료' -> '가상 답사를 종료합니다'로 텍스트 수정
  if (!text.includes('가상 답사를 종료합니다')) return;

  // (이하 로직은 모두 동일)
  sessionStorage.setItem('pdv:returnFromTour', '1');

  const raw = sessionStorage.getItem('pdv:returnTo');
  if (raw) {
    const { type, id, query } = JSON.parse(raw);
    const name = type === 'science_place' ? 'PlaceDetail' : 'ExhibitionDetail';

    router.replace({ name, params: { id }, query });
  } else {
    router.replace({ name: 'Home' });
  }
}
// ★★★ [수정 완료] ★★★
//

function goBack() { router.back(); }


// (이하 탭 하이라이트를 위한 computed 로직은 동일)
const isEntranceView = computed(() => {
  if (!tourItems.value || tourItems.value.length === 0) {
    return false;
  }
  return tourItems.value.some(item => item.sceneId && item.sceneId.endsWith('_entrance'));
});

const currentZone = computed(() => {
  const id = currentSceneId.value || '';

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
  } else {
    console.log(`✅ [TourView] activeZone 계산 (상세 모드): ${id}`);
    return id;
  }
});
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
