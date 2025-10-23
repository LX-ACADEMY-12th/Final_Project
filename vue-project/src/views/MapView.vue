<template>
  <div class="vh-100 position-relative overflow-hidden bg-light" style="font-family: 'SUIT', sans-serif">

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
      <img src="https://placehold.co/600x400" class="rounded-circle" style="width: 48px; height: 48px; flex-shrink: 0;">

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
    <!-- 프로필 영역 아래 전시 및 답사 버튼 -->
    <div class="position-absolute d-flex flex-row" style="z-index: 10; top: 104px; left: 18px; gap: 8px;">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="changeTab('전시')">전시</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
        @click="changeTab('답사')">답사</button>
    </div>

    <!-- 지도 상 실내지도, 방문장소, 현위치 버튼 -->
    <div class="position-absolute d-flex flex-column" style="
        z-index: 10;
        gap: 10px;
        right: 18px;
        /* 하단 UI 요소들(캐러셀, 네비바) 위에 위치 */
        bottom: calc(170px + 63px + 16px);
      ">
      <!-- <button class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center"
        @click="goToIndoorMap">
        <i class="bi bi-layers-half" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">실내지도</span>
      </button> -->
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

    <!-- 카드 아이템 열차 창문 섹션 -->
    <div class="position-absolute" style="
        bottom: 63px; /* 하단 네비게이션바 높이 */
        left: 0;
        right: 0;
        height: 170px; /* 카드(137px) + 상하 여백 */
        z-index: 20;
      ">
      <!-- 카드 아이템 열차 레일 섹션 -->
      <div class="card-carousel"
        style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
        <!-- 카드 아이템 열차 섹션 -->
        <div class="d-flex flex-row align-items-center" style="gap: 16px; height: 100%; padding: 0 18px;">
          <!-- 카드 아이템 반복 -->
          <PlaceCard v-for="item in currentCarouselItems" :key="item.id" :item="item" @add=goToDetail(item)
            @item-click="handleItemClick(item)" />
        </div>
      </div>
    </div>

    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />
    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />
  </div>
</template>

<script>
// <KeepAlive>가 인식할 수 있도록 name을 지정
// 이 이름이 App.vue의 include="MapComponent"와 일치해야 합니다.
export default {
  name: 'MapComponent'
}
</script>

<script setup>
import { ref, onMounted, computed, watch, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';

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

// map 객체와 markers 배열을 ref로 관리합니다.
const map = ref(null);
const markers = ref([]);
// 현위치 핀 1개를 별도로 관리할 ref 추가
const currentLocationMarker = ref(null);

// 실내지도 버튼 클릭 시 호출 함수
// const goToIndoorMap = () => {
//   router.push('/indoormap');
// }

// URL 쿼리에서 탭 상태를 읽어와 selectedTab의 초기값을 설정
// (setup 스코프에서 router.currentRoute.value로 현재 라우트 정보에 접근)
const tabFromQuery = router.currentRoute.value.query.tab;
if (tabFromQuery === '답사') {
  selectedTab.value = '답사';
}

// [수정 2] onActivated 훅을 추가합니다.
onActivated(() => {
  // 이 컴포넌트가 <KeepAlive>에 의해 다시 화면에 나타날 때마다 실행됩니다.
  console.log('지도 컴포넌트가 다시 활성화됨. 네비바를 "지도"로 설정합니다.');
  // 이 컴포넌트는 '지도' 페이지이므로, 네비바 상태를 '지도'로 강제 설정합니다.
  selectedNavItem.value = '지도';
});

// 탭 변경 시 URL도 함께 변경하는 함수 정의
const changeTab = (tabName) => {
  selectedTab.value = tabName;
  // router.replace를 사용하여 히스토리 스택에 추가하지 않고 URL 변경
  router.replace({ query: { tab: tabName } });
};

// 장소 상세페이지 이동 함수
const goToDetail = (item) => {
  console.log(`상세 페이지로 이동:`, item.title);
  if (selectedTab.value === '전시') {
    // '전시' 탭이면 /exhibition/ID 로 이동
    console.log(`전시 상세로 이동 (ID: ${item.id}):`, item.title);
    router.push(`/exhibition/${item.id}`);
  } else {
    // '답사' 탭이면 /place/ID 로 이동
    console.log(`장소 상세로 이동 (ID: ${item.id}):`, item.title);
    router.push(`/place/${item.id}`);
  }
}

// 카드 본체 클릭 시 호출될 함수 (지도 이동)
const handleItemClick = (item) => {
  console.log(`지도 이동:`, item.title);
  moveMapToItem(item.lat, item.lng);
}

// 현위치로 이동하고 핀을 찍는 함수
const goToCurrentLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        // === 현위치 가져오기 성공 ===
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        // [!!] 현위치 정확도(미터 단위)
        const accuracy = position.coords.accuracy;

        const currentLocation = new window.kakao.maps.LatLng(lat, lng);

        // 맵 중심을 현위치로 이동
        map.value.setCenter(currentLocation);
        map.value.setLevel(3); // 현위치 기준 줌 레벨

        // --- [!!] 현위치 '원' 생성 로직 (기존 마커 로직 대체) ---

        // 1. 기존 현위치 '원'이 있다면 제거
        if (currentLocationMarker.value) {
          currentLocationMarker.value.setMap(null);
        }

        // 2. 새 '원' 생성
        const newCircle = new window.kakao.maps.Circle({
          center: currentLocation,      // 원의 중심좌표
          radius: accuracy,             // 원의 반지름 (m 단위, 정확도)
          strokeWeight: 2,              // 선의 두께
          strokeColor: '#007AFF',       // 선 색상
          strokeOpacity: 0.8,           // 선 불투명도
          strokeStyle: 'solid',       // 선 스타일
          fillColor: '#007AFF',        // 채우기 색상
          fillOpacity: 0.2              // 채우기 불투명도
        });

        // 3. '원'을 맵에 표시
        newCircle.setMap(map.value);

        // 4. ref에 새 '원' 저장
        currentLocationMarker.value = newCircle;
        // --- 원 생성 끝 ---
      },
      (error) => {
        // === 현위치 가져오기 실패 ===
        console.error("Geolocation error: ", error.message);
        alert("현위치를 가져오는 데 실패했습니다.");
      }
    );
  } else {
    // === Geolocation API를 지원하지 않는 브라우저 ===
    console.error("Geolocation is not supported by this browser.");
    alert("이 브라우저에서는 현위치 기능을 지원하지 않습니다.");
  }
}

// 지도 이동(panTo) 함수
const moveMapToItem = (lat, lng) => {
  // 지도가 없으면 중단
  if (!map.value) return;
  const moveLatLon = new window.kakao.maps.LatLng(lat, lng);
  // 1. 설정한 레벨로 줌을 먼저 설정
  map.value.setLevel(2);
  // 부드럽게 이동
  map.value.panTo(moveLatLon);
}

// 모든 핀이 보이도록 맵 범위를 조절하는 함수
const fitMapToBounds = (items) => {
  // 맵이 없거나, 아이템이 없으면 중단
  if (!map.value || !items || items.length === 0) return;

  // LatLngBounds 객체에 모든 좌표를 추가
  const bounds = new window.kakao.maps.LatLngBounds();
  items.forEach(item => {
    bounds.extend(new window.kakao.maps.LatLng(item.lat, item.lng));
  });

  // 계산된 범위로 지도의 중심과 줌 레벨을 조절
  map.value.setBounds(bounds);
}

// 기존 마커(핀) 제거 함수
const clearMarkers = () => {
  if (markers.value.length > 0) {
    markers.value.forEach((marker) => marker.setMap(null));
    markers.value = []; // 배열 비우기
  }
}

// 새 마커(핀) 그리기 함수
const drawMarkers = (items) => {
  if (!map.value) return; // 맵이 없으면 중단

  items.forEach(item => {
    const markerPosition = new window.kakao.maps.LatLng(item.lat, item.lng);
    const marker = new window.kakao.maps.Marker({
      position: markerPosition
    });
    marker.setMap(map.value);
    markers.value.push(marker);
  });
}

onMounted(() => {
  // Kakao 객체 및 maps API가 로드되었는지 확인
  if (window.kakao && window.kakao.maps) {
    // 기본 옵션 (서울시청 - 현위치 실패 시 fallback으로 사용)
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
      level: 3,
    };

    // 맵을 기본 옵션으로 우선 생성
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    // '전시' 탭의 핀은 항상 그립니다.
    drawMarkers(currentCarouselItems.value);

    // onMounted 시, 분리한 현위치 함수 호출
    goToCurrentLocation();

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// '전시' 탭을 위한 목데이터
const exhibitionItems = ref([
  {
    id: 1,
    imageUrl: 'https://placehold.co/600x400',
    subject: '지구',
    grade: '3학년', // [수정] 형식 통일
    title: '습지생물코너',
    type: '상설',
    place: '국립중앙과학관 자연사관',
    hashtags: ['생명과학'],
    lat: 36.3758,
    lng: 127.3845
  },
  {
    id: 2,
    imageUrl: 'https://placehold.co/600x400',
    subject: '물리',
    grade: '4학년', // [수정] 형식 통일
    title: '빛의 원리',
    type: '기획',
    place: '국립과천과학관',
    hashtags: ['파동', '빛'],
    lat: 37.4363,
    lng: 126.9746
  },
  {
    id: 3,
    imageUrl: 'https://placehold.co/600x400',
    subject: '화학',
    grade: '5학년', // [수정] 형식 통일
    title: '미래 에너지',
    type: '상설',
    place: '서울시립과학관',
    hashtags: ['에너지', '화학 반응'],
    lat: 37.6094,
    lng: 127.0706
  },
  {
    id: 101,
    imageUrl: 'https://placehold.co/600x400/FFCC00/000',
    subject: '물리',
    grade: '3학년', // [수정] 형식 통일
    title: '떠오르는 힘, 부력',
    type: '상설',
    place: '국립중앙과학관',
    hashtags: ['유체', '물리1'],
    lat: 36.3758,
    lng: 127.3845
  },
  {
    id: 102,
    imageUrl: 'https://placehold.co/600x400/00CCFF/000',
    subject: '지구',
    grade: '5학년', // [수정] 형식 통일
    title: '태양계 행성 탐험',
    type: '기획',
    place: '서울시립과학관',
    hashtags: ['우주', '천체'],
    lat: 37.6094,
    lng: 127.0706
  },
  {
    id: 103,
    imageUrl: 'https://placehold.co/600x400/99FF99/000',
    subject: '생명',
    grade: '6학년',
    title: '인체의 신비',
    type: '상설',
    place: '국립과천과학관',
    hashtags: ['소화', '순환'],
    lat: 37.4363,
    lng: 126.9746
  },
  {
    id: 104,
    imageUrl: 'https://placehold.co/600x400/FF99CC/000',
    subject: '화학',
    grade: '4학년',
    title: '반짝반짝 결정',
    type: '기획',
    place: '서울시립과학관',
    hashtags: ['물질', '용액'],
    lat: 37.6094,
    lng: 127.0706
  },
  {
    id: 105,
    imageUrl: 'https://placehold.co/600x400/CC99FF/000',
    subject: '지구',
    grade: '6학년',
    title: '움직이는 대륙',
    type: '상설',
    place: '국립중앙과학관',
    hashtags: ['판 구조론', '화산'],
    lat: 36.3758,
    lng: 127.3845
  },
  {
    id: 106,
    imageUrl: 'https://placehold.co/600x400/FFCC00/000',
    subject: '물리',
    grade: '5학년',
    title: '전기와 자기',
    type: '상설',
    place: '국립과천과학관',
    hashtags: ['전자기', '회로'],
    lat: 37.4363,
    lng: 126.9746
  },
  {
    id: 107,
    imageUrl: 'https://placehold.co/600x400/99FF99/000',
    subject: '생명',
    grade: '3학년',
    title: '동물의 한살이',
    type: '기획',
    place: '서울시립과학관',
    hashtags: ['곤충', '동물'],
    lat: 37.6094,
    lng: 127.0706
  },
]);

// '답사' 탭을 위한 목데이터 추가
const fieldTripItems = ref([
  {
    id: 4,
    imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
    subject: '지구',
    grade: '5학년', // [수정] 형식 통일
    title: '해운대 (지질 탐사)',
    place: '부산시 해운대구',
    hashtags: ['고체지구', '퇴적암'],
    lat: 35.1587,
    lng: 129.1604
  },
  {
    id: 5,
    imageUrl: 'https://placehold.co/600x400/CCBBAA/000000',
    subject: '물리',
    grade: '4학년', // [수정] 형식 통일
    title: '서울숲 (공원 산책)',
    place: '서울시 성동구',
    hashtags: ['운동', '자연'],
    lat: 37.5445,
    lng: 127.0374
  },
  {
    id: 201,
    imageUrl: 'https://placehold.co/600x400/FF99AA/000',
    subject: '화학',
    grade: '3학년', // [수정] 형식 통일
    title: '갯벌 체험 (염전)',
    place: '인천 소래습지',
    hashtags: ['소금', '물질의 특성'],
    lat: 37.4021,
    lng: 126.7301
  },
  {
    id: 202,
    imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
    subject: '지구',
    grade: '5학년', // [수정] 형식 통일
    title: '영월 한반도 지형',
    place: '강원도 영월군',
    hashtags: ['침식', '퇴적'],
    lat: 37.2045,
    lng: 128.4557
  },
  {
    id: 203,
    imageUrl: 'https://placehold.co/600x400/99FF99/000',
    subject: '생명',
    grade: '6학년',
    title: '양서류 관찰',
    place: '국립생태원',
    hashtags: ['생태계', '동물'],
    lat: 36.6631,
    lng: 126.6913
  },
  {
    id: 204,
    imageUrl: 'https://placehold.co/600x400/FF99CC/000',
    subject: '화학',
    grade: '6학년',
    title: '암모니아 분수 실험장',
    place: 'LG사이언스파크',
    hashtags: ['산염기', '기체'],
    lat: 37.5649,
    lng: 126.8300
  },
  {
    id: 205,
    imageUrl: 'https://placehold.co/600x400/FFCC00/000',
    subject: '물리',
    grade: '3학년',
    title: '그림자 놀이 체험',
    place: '어린이대공원',
    hashtags: ['빛', '그림자'],
    lat: 37.5492,
    lng: 127.0747
  },
  {
    id: 206,
    imageUrl: 'https://placehold.co/600x400/99FF99/000',
    subject: '생명',
    grade: '4학년',
    title: '서울 식물원',
    place: '서울 식물원',
    hashtags: ['식물', '광합성'],
    lat: 37.5704,
    lng: 126.8359
  },
  {
    id: 207,
    imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
    subject: '지구',
    grade: '4학년',
    title: '화성암 관찰 (한탄강)',
    place: '포천 한탄강',
    hashtags: ['화성암', '지층'],
    lat: 38.0069,
    lng: 127.2088
  }
]);

// selectedTab에 따라 표시할 아이템을 동적으로 결정하는 computed 속성
const currentCarouselItems = computed(() => {
  // 1. 탭에 따라 기본 목록 선택
  let baseList = [];
  if (selectedTab.value === '전시') {
    baseList = exhibitionItems.value;
  } else if (selectedTab.value === '답사') {
    baseList = fieldTripItems.value;
  }

  // 2. 선택된 필터로 baseList를 필터링
  const filteredList = baseList.filter(item => {
    // A. 과목이 일치하는가?
    const subjectMatch = item.subject === selectedSubject.value;

    // B. 학년이 일치하는가? 모달은 '초등 3학년', 데이터는 '3학년'
    const gradeMatch = item.grade === selectedGrade.value.replace('초등 ', '');

    // 둘 다 일치해야 함
    return subjectMatch && gradeMatch;
  });

  return filteredList;
});

// 탭 전환 시 핀을 다시 그리도록 currentCarouselItems를 감시(watch)
watch(currentCarouselItems, (newItems) => {
  if (!map.value) return; // 맵이 아직 로드되지 않았으면 중단

  clearMarkers(); // 기존 핀 모두 제거
  drawMarkers(newItems); // 새 핀 그리기
  fitMapToBounds(newItems); // 맵을 새 핀들에 맞추기
});

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
  // 경로는 실제 router/index.js에 정의된 path와 일치해야 합니다.
  if (navItemName === '홈') {
    router.push('/home');
  } else if (navItemName === '목록') {
    router.push('/list');
  } else if (navItemName === '지도') {
    router.push('/map');
  } else if (navItemName === '코스관리') {
    router.push('/usercourselist');
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

/* 하단 카드 캐러셀 스크롤바 숨기기 */
.card-carousel {

  /* Chrome, Safari, Edge */
  &::-webkit-scrollbar {
    display: none;
  }

  /* Firefox */
  scrollbar-width: none;
  /* IE */
  -ms-overflow-style: none;
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
