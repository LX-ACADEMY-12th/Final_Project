<template>
  <div class="vh-100 position-relative overflow-hidden bg-light" style="font-family: 'SUIT', sans-serif">

    <div ref="mapContainer" class="map-placeholder"></div>
    <div class="position-absolute d-flex align-items-center p-3 bg-white rounded-4 shadow" style="
        z-index: 20;
        gap: 12px;
        width: calc(100% - 36px); /* 좌우 18px 여백 */
        height: 72px;
        left: 18px;
        top: 16px;
      ">
      <img src="https://placehold.co/600x400" class="rounded-circle" style="width: 48px; height: 48px; flex-shrink: 0;">

      <div class="flex-grow-1" style="font-family: 'SUIT', sans-serif">
        <div class="text-secondary" style="font-size: 0.9rem;">안녕하세요</div>
        <div class="fw-bold" style="font-size: 1.1rem;">김민수 학부모님</div>
      </div>
      <button class="btn btn-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 48px; height: 48px;" @click.prevent="isModalOpen = true">
        <i class="bi bi-search fs-5"></i>
      </button>
    </div>
    <div class="position-absolute d-flex flex-row" style="z-index: 10; top: 104px; left: 18px; gap: 8px;">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="changeTab('전시')">전시</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
        @click="changeTab('답사')">답사</button>
    </div>

    <div class="position-absolute d-flex flex-column" style="
        z-index: 10;
        gap: 10px;
        right: 18px;
        /* 하단 UI 요소들(캐러셀, 네비바) 위에 위치 */
        bottom: calc(170px + 63px + 16px);
      ">
      <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-geo-alt" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">방문장소</span>
      </button>
      <button class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center"
        @click="goToCurrentLocation">
        <i class="bi bi-bullseye" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">현위치</span>
      </button>
    </div>

    <div class="position-absolute" style="
        bottom: 63px; /* 하단 네비게이션바 높이 */
        left: 0;
        right: 0;
        height: 170px; /* 카드(137px) + 상하 여백 */
        z-index: 20;
      ">
      <div class="card-carousel"
        style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
        <div class="d-flex flex-row align-items-center" style="gap: 16px; height: 100%; padding: 0 18px;">
          <div v-if="isSearching" class="text-center p-5 text-muted w-100">
            검색 중...
          </div>
          <div v-else-if="displayedItems.length === 0" class="text-center p-5 text-muted w-100">
            <div>표시할 장소가 없습니다.</div>
            <div v-if="searchType === 'radius'" class="text-sm mt-2">
              현재위치: {{ currentUserLocation ? `${currentUserLocation.lat.toFixed(4)},
              ${currentUserLocation.lng.toFixed(4)}` : '없음' }}<br>
              반경: {{ searchRadius }}km / 과목: {{ selectedSubject }} / 학년: {{ selectedGrade }}
            </div>
          </div>
          <PlaceCard v-else v-for="item in displayedItems" :key="item.id" :item="item" @add="goToDetail(item)"
            @item-click="handleItemClick(item)" />
        </div>
      </div>
    </div>

    <FilterModal v-if="isModalOpen" :initialSearchType="searchType" :initialRadius="searchRadius"
      :initialRegion="selectedRegion" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />
  </div>
</template>

<script>
// <KeepAlive>가 인식할 수 있도록 name을 지정
export default {
  name: 'MapComponent'
}
</script>

<script setup>
import { ref, onMounted, watch, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';
// Haversine 거리 계산 함수 import
import { calculateDistance } from '@/utils/distance'; // (경로는 실제 파일 위치에 맞게 수정!)

const router = useRouter();
const selectedTab = ref('전시');
const selectedNavItem = ref('지도');
const isModalOpen = ref(false);
const mapContainer = ref(null);
const map = ref(null);
const markers = ref([]);
const currentLocationMarker = ref(null);

// --- 필터 및 검색 상태 ---
const searchType = ref('filter'); // 'filter', 'radius', 'region'
const searchRadius = ref(300);     // km
const selectedRegion = ref('');  // 예: "서울시 강남구"
const selectedSubject = ref('물리');
const selectedGrade = ref('초등 3학년'); // 모달 기본값과 일치

const currentUserLocation = ref(null); // { lat: number, lng: number }
const displayedItems = ref([]);      // 최종적으로 화면/지도에 보여줄 목록
const isSearching = ref(false);      // (선택) 로딩 상태

// --- 목 데이터 ---
const exhibitionItems = ref([
  { id: 1, imageUrl: 'https://placehold.co/600x400', city: '대전', district: '유성구', grade: '3학년', subject: '지구', title: '습지생물코너', type: '상설', place: '국립중앙과학관 자연사관', hashtags: ['생명과학'], lat: 36.3758, lng: 127.3845 },
  { id: 2, imageUrl: 'https://placehold.co/600x400', city: '과천', district: '', grade: '4학년', subject: '물리', title: '빛의 원리', type: '기획', place: '국립과천과학관', hashtags: ['파동', '빛'], lat: 37.4363, lng: 126.9746 },
  { id: 3, imageUrl: 'https://placehold.co/600x400', city: '서울', district: '노원구', grade: '5학년', subject: '화학', title: '미래 에너지', type: '상설', place: '서울시립과학관', hashtags: ['에너지', '화학 반응'], lat: 37.6094, lng: 127.0706 },
  { id: 101, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '대전', district: '유성구', grade: '3학년', subject: '물리', title: '떠오르는 힘, 부력', type: '상설', place: '국립중앙과학관', hashtags: ['유체', '물리1'], lat: 36.3758, lng: 127.3845 },
  { id: 102, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '서울', district: '노원구', grade: '5학년', subject: '지구', title: '태양계 행성 탐험', type: '기획', place: '서울시립과학관', hashtags: ['우주', '천체'], lat: 37.6094, lng: 127.0706 },
  { id: 103, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '과천', district: '', grade: '6학년', subject: '생명', title: '인체의 신비', type: '상설', place: '국립과천과학관', hashtags: ['소화', '순환'], lat: 37.4363, lng: 126.9746 },
  { id: 104, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '서울', district: '노원구', grade: '4학년', subject: '화학', title: '반짝반짝 결정', type: '기획', place: '서울시립과학관', hashtags: ['물질', '용액'], lat: 37.6094, lng: 127.0706 },
  { id: 105, imageUrl: 'https://placehold.co/600x400/CC99FF/000', city: '대전', district: '유성구', grade: '6학년', subject: '지구', title: '움직이는 대륙', type: '상설', place: '국립중앙과학관', hashtags: ['판 구조론', '화산'], lat: 36.3758, lng: 127.3845 },
  { id: 106, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '과천', district: '', grade: '5학년', subject: '물리', title: '전기와 자기', type: '상설', place: '국립과천과학관', hashtags: ['전자기', '회로'], lat: 37.4363, lng: 126.9746 },
  { id: 107, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서울', district: '노원구', grade: '3학년', subject: '생명', title: '동물의 한살이', type: '기획', place: '서울시립과학관', hashtags: ['곤충', '동물'], lat: 37.6094, lng: 127.0706 },
]);
const fieldTripItems = ref([
  { id: 4, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '부산', district: '해운대구', grade: '5학년', subject: '지구', title: '해운대 (지질 탐사)', place: '부산시 해운대구', hashtags: ['고체지구', '퇴적암'], lat: 35.1587, lng: 129.1604 },
  { id: 5, imageUrl: 'https://placehold.co/600x400/CCBBAA/000000', city: '서울', district: '성동구', grade: '4학년', subject: '물리', title: '서울숲 (공원 산책)', place: '서울시 성동구', hashtags: ['운동', '자연'], lat: 37.5445, lng: 127.0374 },
  { id: 201, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '인천', district: '남동구', grade: '3학년', subject: '화학', title: '갯벌 체험 (염전)', place: '인천 소래습지', hashtags: ['소금', '물질의 특성'], lat: 37.4021, lng: 126.7301 },
  { id: 202, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '영월', district: '', grade: '5학년', subject: '지구', title: '영월 한반도 지형', place: '강원도 영월군', hashtags: ['침식', '퇴적'], lat: 37.2045, lng: 128.4557 },
  { id: 203, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서천', district: '', grade: '6학년', subject: '생명', title: '양서류 관찰', place: '국립생태원', hashtags: ['생태계', '동물'], lat: 36.6631, lng: 126.6913 },
  { id: 204, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '서울', district: '강서구', grade: '6학년', subject: '화학', title: '암모니아 분수 실험장', place: 'LG사이언스파크', hashtags: ['산염기', '기체'], lat: 37.5649, lng: 126.8300 },
  { id: 205, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '서울', district: '광진구', grade: '3학년', subject: '물리', title: '그림자 놀이 체험', place: '어린이대공원', hashtags: ['빛', '그림자'], lat: 37.5492, lng: 127.0747 },
  { id: 206, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서울', district: '강서구', grade: '4학년', subject: '생명', title: '서울 식물원', place: '서울 식물원', hashtags: ['식물', '광합성'], lat: 37.5704, lng: 126.8359 },
  { id: 207, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '포천', district: '', grade: '4학년', subject: '지구', title: '화성암 관찰 (한탄강)', place: '포천 한탄강', hashtags: ['화성암', '지층'], lat: 38.0069, lng: 127.2088 }
]);

// URL 쿼리 복원
const tabFromQuery = router.currentRoute.value.query.tab;
if (tabFromQuery === '답사') {
  selectedTab.value = '답사';
}

// KeepAlive 활성화 시 네비바 복원
onActivated(() => {
  selectedNavItem.value = '지도';
});

// 탭 변경 함수
const changeTab = (tabName) => {
  selectedTab.value = tabName;
  router.replace({ query: { tab: tabName } });
  performSearch();
};

// 상세 페이지 이동
const goToDetail = (item) => {
  if (selectedTab.value === '전시') {
    router.push(`/exhibition/${item.id}`);
  } else {
    router.push(`/place/${item.id}`);
  }
};

// 카드 클릭 시 지도 이동
const handleItemClick = (item) => {
  moveMapToItem(item.lat, item.lng);
};

// 현재 위치 가져오기
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      return reject(new Error("Geolocation 미지원"));
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentUserLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        console.log('현재 위치 획득:', currentUserLocation.value);
        resolve(currentUserLocation.value);
      },
      (error) => {
        console.error('위치 정보 획득 실패:', error);
        currentUserLocation.value = null;
        reject(error);
      }
    );
  });
};

// 현위치 버튼 클릭 핸들러
const goToCurrentLocation = async () => {
  try {
    await getCurrentLocation(); // 현재 위치 업데이트
    if (currentUserLocation.value && map.value) {
      const currentLatLng = new window.kakao.maps.LatLng(
        currentUserLocation.value.lat,
        currentUserLocation.value.lng
      );
      map.value.setCenter(currentLatLng);
      map.value.setLevel(3); // 현위치 근처로 확대

      // --- [수정] 원 대신 커스텀 오버레이(작은 원) 생성 및 표시 ---

      // 1. 기존 현위치 표시(마커 또는 오버레이) 제거
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null);
      }

      // 2. 새 커스텀 오버레이 생성 (HTML 컨텐츠 사용)
      const content = '<div class="current-location-dot"></div>'; // CSS로 스타일링될 div
      const newOverlay = new window.kakao.maps.CustomOverlay({
        position: currentLatLng,
        content: content,
        // yAnchor: 0.5, // 필요시 오버레이 위치 미세 조정
        // xAnchor: 0.5
      });

      // 3. 오버레이를 맵에 표시
      newOverlay.setMap(map.value);

      // 4. ref에 새 오버레이 저장
      currentLocationMarker.value = newOverlay;
      // --- 오버레이 생성 끝 ---

    }
  } catch (error) {
    console.error("현위치 이동 실패:", error);
    alert("현위치를 가져올 수 없습니다. GPS가 켜져 있는지 확인해주세요.");
  }
};

// 지도 특정 위치로 이동
const moveMapToItem = (lat, lng) => {
  if (map.value) {
    const itemLatLng = new window.kakao.maps.LatLng(lat, lng);
    map.value.setCenter(itemLatLng);
    map.value.setLevel(2); // 줌인
  }
};

// 마커 모두 제거
const clearMarkers = () => {
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];
};

// 아이템들을 지도에 마커로 표시
const drawMarkers = (items) => {
  if (!map.value || !items.length) return;

  items.forEach(item => {
    const markerPosition = new window.kakao.maps.LatLng(item.lat, item.lng);
    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: item.title
    });
    marker.setMap(map.value);
    markers.value.push(marker);

    // 마커 클릭 이벤트
    window.kakao.maps.event.addListener(marker, 'click', () => {
      handleItemClick(item);
    });
  });
};

// 모든 마커가 보이도록 지도 범위 조정
const fitMapToBounds = (items) => {
  if (!map.value || !items.length) return;

  const bounds = new window.kakao.maps.LatLngBounds();
  items.forEach(item => {
    bounds.extend(new window.kakao.maps.LatLng(item.lat, item.lng));
  });
  map.value.setBounds(bounds);
};

// 검색 실행 함수
const performSearch = async () => {
  console.log('==== 검색 실행 시작 ====');
  console.log('검색 타입:', searchType.value);
  console.log('탭:', selectedTab.value);
  console.log('과목:', selectedSubject.value);
  console.log('학년:', selectedGrade.value);
  console.log('반경:', searchRadius.value, 'km');

  isSearching.value = true;

  let baseList = (selectedTab.value === '전시') ? exhibitionItems.value : fieldTripItems.value;
  let filteredList = [];

  console.log('기본 목록 개수:', baseList.length);

  try {
    if (searchType.value === 'radius') {
      console.log('반경 검색 모드');

      if (!currentUserLocation.value) {
        console.log('현재 위치 없음, 위치 정보 요청');
        await getCurrentLocation();
      }

      if (currentUserLocation.value) {
        console.log('현재 위치:', currentUserLocation.value);

        // 거리별 정보 수집 (디버깅용)
        const itemsWithDistance = baseList.map(item => {
          const distance = calculateDistance(
            currentUserLocation.value.lat, currentUserLocation.value.lng,
            item.lat, item.lng
          );
          return { ...item, distance };
        });

        console.log('모든 아이템 거리 정보:');
        itemsWithDistance.forEach(item => {
          console.log(`${item.title}: ${item.distance.toFixed(1)}km, 과목: ${item.subject}, 학년: ${item.grade}`);
        });

        // 실제 필터링
        filteredList = itemsWithDistance.filter(item => {
          const withinRadius = item.distance <= searchRadius.value;
          const subjectMatch = item.subject === selectedSubject.value;
          const gradeMatch = item.grade === selectedGrade.value;

          console.log(`${item.title}: 반경내(${withinRadius}) 과목일치(${subjectMatch}) 학년일치(${gradeMatch})`);

          return withinRadius && subjectMatch && gradeMatch;
        });

        console.log('반경 내 필터링 결과:', filteredList.length, '개');

      } else {
        throw new Error("현재 위치 정보 없음");
      }
    } else if (searchType.value === 'region') {
      console.log('지역 검색 모드');
      filteredList = baseList.filter(item => {
        const regionString = `${item.city} ${item.district}`.trim();
        return (selectedRegion.value && regionString.includes(selectedRegion.value)) &&
          item.subject === selectedSubject.value &&
          item.grade === selectedGrade.value;
      });
    } else { // 'filter'
      console.log('일반 필터 검색 모드');
      filteredList = baseList.filter(item => {
        return item.subject === selectedSubject.value &&
          item.grade === selectedGrade.value;
      });
    }

    displayedItems.value = filteredList;
    console.log('최종 결과:', displayedItems.value.length, '개');

  } catch (error) {
    console.error("검색 중 오류 발생:", error);
    alert("검색 중 오류가 발생했습니다. " + (error.message.includes("위치") ? "위치 정보를 확인해주세요." : ""));
    displayedItems.value = [];
  } finally {
    isSearching.value = false;
    console.log('==== 검색 완료 ====');
  }
};

// 맵 초기화
onMounted(async () => {
  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567), // 서울시청
      level: 3,
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    try {
      await getCurrentLocation();
      if (currentUserLocation.value && map.value) {
        const currentLatLng = new window.kakao.maps.LatLng(
          currentUserLocation.value.lat,
          currentUserLocation.value.lng
        );
        map.value.setCenter(currentLatLng);

        // --- onMounted 시 현위치 커스텀 오버레이 그리기 ---

        // 1. 기존 표시 제거 (만약을 대비)
        if (currentLocationMarker.value) {
          currentLocationMarker.value.setMap(null);
        }
        // 2. 새 커스텀 오버레이 생성 및 표시
        const content = '<div class="current-location-dot"></div>';
        currentLocationMarker.value = new window.kakao.maps.CustomOverlay({
          position: currentLatLng,
          content: content,
          // yAnchor: 0.5,
          // xAnchor: 0.5
        });
        currentLocationMarker.value.setMap(map.value);
        // --- 오버레이 그리기 끝 ---
      }
    } catch (error) {
      console.warn("초기 현위치 로드 실패:", error.message);
      // 실패해도 기본 위치(서울시청)로 진행
    }

    // 첫 검색 실행
    await performSearch();

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// displayedItems 변경 감지하여 마커 업데이트
watch(displayedItems, (newItems) => {
  if (!map.value) return;
  clearMarkers();
  drawMarkers(newItems);
  fitMapToBounds(newItems);
});

// 모달 완료 핸들러
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  searchType.value = filterData.searchType;
  searchRadius.value = filterData.radius;
  selectedRegion.value = filterData.region;
  selectedSubject.value = filterData.subject;
  selectedGrade.value = filterData.grade;
  isModalOpen.value = false;

  performSearch();
};

// 하단 네비게이션 핸들러
const handleNavigation = (navItemName) => {
  selectedNavItem.value = navItemName;
  if (navItemName === '홈') router.push('/home');
  else if (navItemName === '목록') router.push('/list');
  else if (navItemName === '지도') router.push('/map'); // 현재 페이지
  else if (navItemName === '코스관리') router.push('/usercourselist');
  else if (navItemName === '마이페이지') router.push('/mypage');
};

</script>

<style>
.current-location-dot {
  width: 16px;
  /* 원 크기 */
  height: 16px;
  /* 원 크기 */
  border-radius: 50%;
  /* 동그랗게 */
  background-color: #007AFF;
  /* 파란색 */
  border: 3px solid white;
  /* 흰색 테두리 */
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  /* 그림자 */
  /* 오버레이 위치 보정 (핀 중심 맞추기) */
  transform: translate(-50%, -50%);
}
</style>

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

/* 하단 카드 캐러셀 스크롤바 숨기기 (표준 CSS) */
.card-carousel::-webkit-scrollbar {
  display: none;
}

.card-carousel {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 상단 필터 버튼 (전시, 탐험) */
.spec-button {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 5px 16px;
  gap: 8px;
  position: relative;
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
