<template>
  <!-- <div class="map-container"> -->
  <!-- 맵 뷰 -->
  <MapView v-show="currentView === 'map'" @marker-click="handleMarkerClick" />

  <!-- 피드 뷰 (화면 전환 시뮬레이션) -->
  <FeedView v-show="currentView === 'feed' && selectedFeedData" :feed-data="selectedFeedData"
    @back-to-map="handleBackToMap" />
  <!-- </div> -->
</template>

<script setup>
import { ref, nextTick } from 'vue';
import MapView from '@/components/MapView.vue';
import FeedView from '@/components/FeedView.vue';

//================================================================
// 1. 상태 관리 (State Management)
// 이 섹션은 애플리케이션의 전반적인 상태를 관리합니다.
//================================================================

// --- UI 상태 ---
const currentView = ref('map'); // 현재 보여줄 화면 ('map' 또는 'feed')
const selectedFeedData = ref(null); // 사용자가 선택한 피드 정보
const mapViewRef = ref(null); // MapView 컴포넌트 인스턴스를 참조하기 위한 ref

//================================================================
// 2. UI 이벤트 핸들러 (UI Event Handlers)
// 이 섹션은 자식 컴포넌트로부터 받은 이벤트를 처리합니다.
//================================================================

/** * MapView에서 마커가 클릭되었을 때 호출됩니다.
 * @param {object} feedInfo - { markerId, locationName }
 */
const handleMarkerClick = (feedInfo) => {
  selectedFeedData.value = feedInfo;
  currentView.value = 'feed';
};

/** FeedView에서 '뒤로가기' 버튼 클릭 시 맵 화면으로 돌아가고,
 * 지도 레이아웃을 새로고침하여 상태를 복원합니다.
 */
const handleBackToMap = async () => {
  currentView.value = 'map';
  selectedFeedData.value = null;

  // DOM 업데이트(v-show)가 완료된 후 실행되도록 보장
  await nextTick();

  // MapView 컴포넌트에 노출된 refreshMapLayout 함수를 호출
  if (mapViewRef.value) {
    mapViewRef.value.refreshMapLayout();
  }
};
</script>
