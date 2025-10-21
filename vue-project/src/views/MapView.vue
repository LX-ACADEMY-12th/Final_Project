<template>
  <div class="vh-100 position-relative overflow-hidden bg-light">

    <div ref="mapContainer" class="map-placeholder"></div>
    <!-- 사용자 프로필 영역 -->
    <div class="position-absolute d-flex align-items-center p-3 bg-white rounded-4 shadow" style="
        z-index: 20;
        gap: 12px;
        width: calc(100% - 36px); /* 좌우 18px 여백 */
        height: 72px;
        left: 18px;
        top: 16px;
      ">
      <img src="https://via.placeholder.com/48/E0E0E0/808080?text=Av" class="rounded-circle"
        style="width: 48px; height: 48px; flex-shrink: 0;">

      <div class="flex-grow-1" style="font-family: 'SUIT', sans-serif">
        <div class="text-secondary" style="font-size: 0.9rem;">안녕하세요</div>
        <div class="fw-bold" style="font-size: 1.1rem;">김민수 학부모님</div>
      </div>
      <!-- 검색버튼 -->
      <button class="btn btn-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 48px; height: 48px;" @click.prevent="isModalOpen = true">
        <i class="bi bi-search fs-5"></i>
      </button>
    </div>
    <!-- 프로필 영역 아래 전시 및 탐험 버튼 -->
    <div class="position-absolute d-flex flex-row" style="z-index: 10; top: 104px; left: 18px; gap: 8px;">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="selectedTab = '전시'">전시</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '탐험' }"
        @click="selectedTab = '탐험'">탐험</button>
    </div>

    <!-- 지도 상 실내지도, 방문장소, 현위치 버튼 -->
    <div class="position-absolute d-flex flex-column" style="
        z-index: 10;
        gap: 10px;
        right: 18px;
        /* 하단 UI 요소들(캐러셀, 네비바) 위에 위치 */
        bottom: calc(170px + 63px + 16px);
      ">
      <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-layers-half" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">실내지도</span>
      </button>
      <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-geo-alt" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">방문장소</span>
      </button>
      <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-bullseye" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">현위치</span>
      </button>
    </div>

    <!-- 카드 아이템 열차 창문 섹션 -->
    <div class="position-absolute" style="
        bottom: 63px; /* 하단 네비게이션바 높이 */
        left: 0;
        right: 0;
        height: 170px; /* 카드(137px) + 상하 여백 */
        z-index: 20;
      ">
      <!-- 카드 아이템 열차 레일 섹션 -->
      <div style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
        <!-- 카드 아이템 열차 섹션 -->
        <div class="d-flex flex-row align-items-center" style="gap: 16px; height: 100%; padding: 0 18px;">
          <!-- 카드 아이템 반복 -->
          <VenueCard v-for="item in carouselItems" :key="item.id" :item="item" />
        </div>
      </div>
    </div>

    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />
    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/FilterModal.vue';
import VenueCard from '@/components/PlaceCard.vue';

const router = useRouter();
// '전시/탐험' 탭 상태 ('전시'가 활성화됨)
const selectedTab = ref('전시');
// 하단 네비게이션바 상태 ('지도'가 활성화됨)
const selectedNavItem = ref('지도');
// 모달의 열림/닫힘 상태를 관리
const isModalOpen = ref(false);
// 필터 상태
const selectedSubject = ref('물리');
const selectedGrade = ref('초등 3학년');
// 지도컨테이너 DOM을 참조할 ref를 생성
const mapContainer = ref(null);
// 컴포넌트 마운트된 후 카카오맵을 초기화
onMounted(() => {
  // Kakao 객체 및 maps API가 로드되었는지 확인
  if (window.kakao && window.kakao.maps) {
    // 지도 생성 옵션
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567), // (예: 서울시청 위도/경도)
      level: 3, // 확대 레벨
    };

    // 지도 생성 (mapContainer.value는 <div ref="mapContainer"> DOM 요소를 가리킴)
    const map = new window.kakao.maps.Map(mapContainer.value, options);

    // (선택 사항) 지도 컨트롤(줌) 추가
    // const zoomControl = new window.kakao.maps.ZoomControl();
    // map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// 캐러셀 아이템 데이터 (이전 코드 재사용)
const carouselItems = ref([
  {
    id: 1,
    iconClass: 'bi-triangle-fill',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '지구',  // 데이터
    grade: '3학년',  // 데이터
    place: '장소명',  // 데이터
    type: '전시',    // 데이터
    title: '전시명'
  },
  {
    id: 2,
    iconClass: 'bi-star-fill',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '물리',  // '우주' -> '물리' (색상표에 맞게)
    grade: '5학년',
    place: '서울천문대',
    type: '탐험',
    title: '천문대탐험'
  },
  {
    id: 3,
    iconClass: 'bi-beaker',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '화학',
    grade: '4학년',
    place: '한천강지질공원',
    type: '탐험',
    title: '지질탐험'
  }
]);

// 모달에서 '선택 완료를 눌렀을 때 실행되는 함수'
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  // 부모의 상태(ref)를 모달에서 전달받은 값으로 업데이트
  selectedSubject.value = filterData.subject;
  selectedGrade.value = filterData.grade;
  // 모달 닫기
  isModalOpen.value = false;
  // 필터 변경 후 바로 검색 실행
  performSearch();
}

// 검색 실행되는 함수
const performSearch = () => {
  console.log(`검색 실행:`, {
    subject: selectedSubject.value,
    grade: selectedGrade.value
  });
  // 검색 API 호출 로직을 구현
  // fetchResults(selectedSubject.value, selectedGrade.value)
}

// BottomNav의 @navigate 이벤트를 처리할 함수 추가
const handleNavigation = (navItemName) => {
  console.log(navItemName, '클릭됨.');
  // 활성 탭 상태 업데이트
  selectedNavItem.value = navItemName;
  // [!!] 경로는 실제 router/index.js에 정의된 path와 일치해야 합니다.
  if (navItemName === '홈') {
    router.push('/home');
  } else if (navItemName === '목록') {
    router.push('/list');
  } else if (navItemName === '지도') {
    router.push('/map');
  } else if (navItemName === '코스관리') {
    router.push('/course');
  } else if (navItemName === '마이페이지') {
    router.push('/mypage');
  }
}
</script>


<style scoped>
/* 지도 플레이스홀더 */
.map-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #e9e9e9;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  color: #aaa;
  z-index: 0;
}

/* 상단 필터 버튼 (전시, 탐험) */
.spec-button {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  /* [!!] padding 수정 (좌우 16px) */
  padding: 5px 16px;
  gap: 8px;

  position: relative;
  /* [!!] absolute -> relative (부모 div가 위치를 잡음) */
  /* [!!] 고정 width 제거 -> 텍스트 길이에 맞게 자동 조절 */
  /* width: 84px; */
  height: 38px;

  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  transition: background-color 0.2s, color 0.2s;
  font-family: 'SUIT', sans-serif;
  font-weight: 500;
}

.spec-button.active {
  /* [!!] Figma 디자인의 파란색으로 변경 */
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}

/* 우측 플로팅 원형 버튼 */
.btn-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  padding: 0;
  font-weight: 500;
}
</style>
