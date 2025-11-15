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
      <!--
        기존 <img> 태그를 user.profileImageUrl과 연동되는 v-if/v-else 블록으로 변경합니다.
      -->
      <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 48px; height: 48px; background-color: rgba(0,0,0,0.1); overflow: hidden;">

        <!-- 스토어에 이미지가 있으면 <img> 표시 -->
        <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필"
          style="width: 100%; height: 100%; object-fit: cover;">
        <!-- 스토어에 이미지가 없으면 기본 아이콘 표시 (v-else) -->
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="#888" class="bi bi-person-fill"
          viewBox="0 0 16 16">
          <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zM8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
        </svg>
      </div>

      <div class="flex-grow-1" style="font-family: 'SUIT', sans-serif">
        <div class="text-secondary" style="font-size: 0.9rem;">안녕하세요</div>
        <div class="fw-bold" style="font-size: 1.1rem;">{{ userName }}</div>
      </div>
      <button class="btn btn-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 48px; height: 48px; background-color: #4A7CEC; border-color: #4A7CEC; color: white;"
        @click.prevent="isModalOpen = true">
        <i class="bi bi-search fs-5"></i>
      </button>

    </div>
    <div class="position-absolute d-flex flex-row" style="z-index: 10; top: 104px; left: 18px; gap: 8px;">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="changeTab('전시')">과학관</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
        @click="changeTab('답사')">과학과 함께하는 여행</button>
    </div>

    <div class="position-absolute d-flex flex-column" style="
        z-index: 10;
        gap: 10px;
        right: 18px;
        /* 하단 UI 요소들(캐러셀, 네비바) 위에 위치 */
        bottom: calc(170px + 63px + 16px);
      ">
      <!-- <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-geo-alt" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">과학관 실내</span>
      </button> -->
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
          <div v-else-if="filteredItems.length === 0" class="text-center p-5 text-muted w-100">
            <div>표시할 장소가 없습니다.</div>
            <div v-if="locationType === 'radius'" class="text-sm mt-2">
              현재위치: {{ currentUserLocation ? `${currentUserLocation.lat.toFixed(4)},
              ${currentUserLocation.lng.toFixed(4)}` : '없음' }}<br>
              반경: {{ searchRadius }}km / 과목: {{ selectedSubject }} / 학년: {{ selectedGrade }}
            </div>
          </div>
          <PlaceCard v-else v-for="item in filteredItems" :key="item.id" :item="item" @add="goToDetail(item)"
            :id="`card-${item.id}`" :class="{ 'active-card': item.id === activeItemId }"
            @item-click="handleItemClick(item)" />
        </div>
      </div>
    </div>

    <FilterModal v-if="isModalOpen" :initialLocationType="locationType" :initialRadius="searchRadius"
      :initialRegion="selectedRegion" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />

    <!-- 템플릿에 로딩 오버레이 추가 -->
    <transition name="fade">
      <div v-if="isMapLoading" class="map-loading-overlay">
        <div class="spinner"></div>
      </div>
    </transition>
  </div>
</template>

<script>
// <KeepAlive>가 인식할 수 있도록 name을 지정
export default {
  name: 'MapComponent'
}
</script>

<script setup>
import { ref, onMounted, watch, onActivated, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from '@/api/axiosSetup';
import rawAxios from 'axios';         // ⬅️ [추가] (외부 API용)
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';
import eventBus from '@/utils/eventBus';
// 🟢 Pinia 스토어 관련 import 추가
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import { useCurriculumStore } from '@/stores/curriculumStore';

const router = useRouter();
const activeItemId = ref(null); // ⬅️ [추가]

// 🟢 [추가] 시연을 위한 대전 시청 고정 좌표
const DEMO_LOCATION = { lat: 36.3504119, lng: 127.3845475 };

// 🟢 [추가] Pinia 스토어 초기화 및 상태 가져오기
const authStore = useAuthStore();
// user 객체와 isLoggedIn 상태를 반응형으로 가져옵니다.
const { user } = storeToRefs(authStore);
const curriculumStore = useCurriculumStore();
const { selectedGrade, selectedSubject } = storeToRefs(curriculumStore);

// 상태 Ref
const selectedTab = ref('전시');
const selectedNavItem = ref('지도');
const isModalOpen = ref(false);
const mapContainer = ref(null);
const map = ref(null);
const markers = ref([]);
const currentLocationMarker = ref(null);

const infoOverlay = ref(null); // :왼쪽_화살표: [유지] 호버 시 열릴 오버레이를 추적할 ref
const directionsPolyline = ref(null); // ⬅️ [추가] 길찾기 경로 선을 저장할 ref
const routeMarkers = ref([]);         // ⬅️ [추가] 길찾기 시작/도착 마커를 저장할 ref
// Kakao Developers에서 발급받은 'REST API 키'를 입력하세요.
const KAKAO_REST_KEY = import.meta.env.VITE_KAKAO_REST_KEY;

// 1. 마커 이미지 크기/옵션 설정 (핀 크기에 맞게 조절하세요)
const imageSize = new window.kakao.maps.Size(32, 37); // 예: 32x37px 핀
const imageOption = { offset: new window.kakao.maps.Point(16, 37) }; // 핀의 하단 중앙
// 2. '전시' (과학관) 핀 이미지 객체 생성
const exhibitionMarkerImage = new window.kakao.maps.MarkerImage(
  '/museum.png', // :왼쪽_화살표: public 폴더의 '전시' 핀 이미지 경로
  imageSize,
  imageOption
);
// 3. '답사' (현장학습) 핀 이미지 객체 생성
const fieldTripMarkerImage = new window.kakao.maps.MarkerImage(
  '/experiment.png', // :왼쪽_화살표: public 폴더의 '답사' 핀 이미지 경로
  imageSize,
  imageOption
);

// 🟢 user 상태에 따라 화면에 표시할 이름을 계산하는 computed 속성
const userName = computed(() => {
  // user.value에 정보가 있고 name이 있다면 'OOO 학부모님' 형식으로 반환
  if (user.value?.name) {
    return `${user.value.name} 학부모님`;
  }
  // user 정보가 없으면 기본 메시지 반환
  return '로그인 필요';
});

// --- 필터 및 검색 상태 ---
const locationType = ref('all'); // 'all', 'radius', 'region' (이름 및 기본값 변경)
const searchRadius = ref(5); // km (모달 기본값과 일치)
const selectedRegion = ref(''); // 예: "서울시 강남구"

const currentUserLocation = ref(null); // { lat: number, lng: number }
const displayedItems = ref([]);      // 최종적으로 화면/지도에 보여줄 목록
const isSearching = ref(false);      // (선택) 로딩 상태

//'전체' 데이터를 보관할 새 Ref
const allFetchedItems = ref([]);

// '필터링'된 결과를 보여줄 computed 속성
const filteredItems = computed(() => {
  const items = allFetchedItems.value;

  if (selectedTab.value === '전시') {
    // API 응답에 itemType: 'exhibition'이 포함되어 있다고 가정
    return items.filter(item => item.itemType === 'exhibition');
  } else { // '답사'
    // API 응답에 itemType: 'science_place'가 포함되어 있다고 가정
    return items.filter(item => item.itemType === 'science_place');
  }
});

// URL 쿼리
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
  activeItemId.value = null;
  router.replace({ query: { tab: tabName } });
};

// 상세 페이지 이동
const goToDetail = (item) => {

  if (selectedTab.value === '전시') {
    router.push({
      path: `/exhibition/${item.id}`,
      query: {
        mainCategoryTags: selectedSubject.value,
        subCategoryTags: item.hashtags,
        gradeTags: selectedGrade.value,
      }
    });
  } else {
    router.push({
      path: `/place/${item.id}`,
      query: {
        mainCategoryTags: selectedSubject.value,
        subCategoryTags: item.hashtags,
        gradeTags: selectedGrade.value,
      }
    });
  }
};

// 카드 클릭 핸들러 개선
const handleItemClick = (item) => {
  // 0. 활성 ID 설정
  activeItemId.value = item.id;

  // 1. 먼저 해당 위치로 부드럽게 이동
  smoothPanTo(item.lat, item.lng);

  // 2. 마커 하이라이트 효과
  highlightMarker(item);

  // 3. 일정 시간 후 길찾기 표시
  setTimeout(() => {
    showDirectionsToItem(item);
  }, 500); // 맵 이동 시간(300ms) + 하이라이트 시간 고려
};

// 마커 하이라이트 함수
const highlightMarker = (item) => {
  markers.value.forEach(marker => {
    const position = marker.getPosition();
    if (position.getLat() === item.lat && position.getLng() === item.lng) {
      // 바운스 효과 또는 크기 변경
      marker.setAnimation(window.kakao.maps.Animation.BOUNCE);

      // 2초 후 애니메이션 중지
      setTimeout(() => {
        marker.setAnimation(null);
      }, 2000);
    }
  });
};

// // 현재 위치 가져오기
// const getCurrentLocation = () => {
//   return new Promise((resolve, reject) => {
//     if (!navigator.geolocation) {
//       return reject(new Error("Geolocation 미지원"));
//     }
//     navigator.geolocation.getCurrentPosition(
//       (position) => {
//         currentUserLocation.value = {
//           lat: position.coords.latitude,
//           lng: position.coords.longitude
//         };
//         console.log('현재 위치 획득:', currentUserLocation.value);
//         resolve(currentUserLocation.value);
//       },
//       (error) => {
//         console.error('위치 정보 획득 실패:', error);
//         currentUserLocation.value = null;
//         reject(error);
//       }
//     );
//   });
// };

// 현재 위치 가져오기 (대전 시청으로 고정)
const getCurrentLocation = () => {
  return new Promise((resolve) => {
    currentUserLocation.value = DEMO_LOCATION;
    console.log('현재 위치 고정 (대전 시청):', currentUserLocation.value);
    resolve(currentUserLocation.value);
  });
};

const goToCurrentLocation = async () => {
  try {
    await getCurrentLocation();
    if (currentUserLocation.value && map.value) {
      const currentLatLng = new window.kakao.maps.LatLng(
        currentUserLocation.value.lat,
        currentUserLocation.value.lng
      );

      // panTo로 부드럽게 이동
      map.value.panTo(currentLatLng);

      // 애니메이션과 함께 줌 레벨 조정
      setTimeout(() => {
        map.value.setLevel(3, {
          animate: {
            duration: 300
          }
        });
      }, 300); // panTo가 끝나는 시점과 비슷하게 맞춤

      // 현위치 마커 애니메이션 추가
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null);
      }

      // 펄스 애니메이션이 있는 현위치 표시
      const content = `
        <div class="current-location-wrapper">
          <div class="current-location-dot"></div>
          <div class="current-location-pulse"></div>
        </div>
      `;

      const newOverlay = new window.kakao.maps.CustomOverlay({
        position: currentLatLng,
        content: content,
      });

      newOverlay.setMap(map.value);
      currentLocationMarker.value = newOverlay;
    }
  } catch (error) {
    console.error("현위치 이동 실패:", error);
  }
};

// 지도 특정 위치로 이동
const moveMapToItem = (lat, lng) => {
  if (map.value) {
    const itemLatLng = new window.kakao.maps.LatLng(lat, lng);

    // panTo로 부드럽게 이동
    map.value.panTo(itemLatLng);

    // 줌 레벨 변경도 단계적으로
    setTimeout(() => {
      // 핀 클릭 시 줌 레벨 (1은 너무 가깝습니다. 3~4 추천)
      const targetLevel = 4;
      map.value.setLevel(targetLevel, {
        animate: {
          duration: 300
        }
      });
    }, 300);

    // map.value.setCenter(itemLatLng);
    // map.value.setLevel(1); // 줌인
  }
};

// 부드러운 줌 함수
const smoothZoom = (targetLevel, step = 1) => {
  if (!map.value) return;

  const currentLevel = map.value.getLevel();

  if (currentLevel === targetLevel) return;

  const zoomIn = currentLevel > targetLevel;
  const nextLevel = zoomIn
    ? Math.max(currentLevel - step, targetLevel)
    : Math.min(currentLevel + step, targetLevel);

  map.value.setLevel(nextLevel, {
    animate: {
      duration: 250 // 애니메이션 시간(ms)
    }
  });

  // 목표 레벨에 도달할 때까지 재귀
  if (nextLevel !== targetLevel) {
    setTimeout(() => smoothZoom(targetLevel, step), 260);
  }
};

/**
 * 길찾기 실행 (Orchestrator)
 */
const showDirectionsToItem = async (item) => {
  // 1. 현위치(출발지)가 없으면 알림
  if (!currentUserLocation.value) {
    eventBus.emit('show-global-alert', {
      message: '현위치를 먼저 확인해주세요.',
      type: 'error'
    });
    return;
  }

  // 2. 이전 경로 제거
  clearDirections();

  const origin = {
    lat: currentUserLocation.value.lat,
    lng: currentUserLocation.value.lng
  };
  const destination = {
    lat: item.lat,
    lng: item.lng
  };

  try {
    // 3. Kakao Navi API로 경로 데이터 요청
    const { path, bounds } = await fetchDirections(origin, destination);

    if (path.length > 0) {
      // 4. 지도에 경로 Polyline 그리기
      drawDirectionsPolyline(path);

      // 5. [선택] 출발/도착 마커 그리기
      drawRouteStartEndMarkers(origin, destination);

      // 6. 경로가 모두 보이도록 맵 범위 재설정
      map.value.setBounds(bounds);
    }
  } catch (error) {
    console.error("길찾기 실패:", error);
    eventBus.emit('show-global-alert', {
      message: '경로를 찾는 데 실패했습니다.',
      type: 'error'
    });
    // 실패 시 장소 마커들 다시 그림 (선택적)
    // drawMarkers(filteredItems.value);
  }
};

/**
 * Kakao Mobility (Navi) REST API 호출 함수
 */
const fetchDirections = async (origin, destination) => {
  const url = 'https://apis-navi.kakaomobility.com/v1/directions';

  const params = {
    origin: `${origin.lng},${origin.lat}`,
    destination: `${destination.lng},${destination.lat}`,
    priority: 'RECOMMEND' // 추천 경로
  };

  try {
    const response = await rawAxios.get(url, {
      params,
      headers: {
        'Authorization': `KakaoAK ${KAKAO_REST_KEY}`
      }
    });

    if (response.data && response.data.routes && response.data.routes.length > 0) {
      const route = response.data.routes[0];
      const sections = route.sections;
      const path = []; // Polyline을 그릴 좌표 배열
      const bounds = new window.kakao.maps.LatLngBounds(); // 경로에 맞게 맵을 조정할 Bounds

      sections.forEach(section => {
        section.roads.forEach(road => {
          // 'vertexes'는 [x1, y1, x2, y2, ...] 형태의 1차원 배열입니다.
          for (let i = 0; i < road.vertexes.length; i += 2) {
            const lng = road.vertexes[i];
            const lat = road.vertexes[i + 1];
            const latLng = new window.kakao.maps.LatLng(lat, lng);
            path.push(latLng);
            bounds.extend(latLng); // Bounds 확장
          }
        });
      });
      return { path, bounds };
    } else {
      throw new Error('API 응답에서 경로를 찾을 수 없습니다.');
    }
  } catch (error) {
    console.error('Kakao Navi API 호출 에러:', error);
    throw error;
  }
};

/**
 * 지도에 경로 Polyline 및 마커 그리기
 */
const drawDirectionsPolyline = (path) => {
  // 기존 Polyline 제거
  if (directionsPolyline.value) {
    directionsPolyline.value.setMap(null);
  }

  // 새 Polyline 생성
  const polyline = new window.kakao.maps.Polyline({
    path: path,
    strokeWeight: 5,
    strokeColor: '#FF0000', // 빨간색 경로
    strokeOpacity: 0.7,
    strokeStyle: 'solid'
  });

  polyline.setMap(map.value);
  directionsPolyline.value = polyline; // ref에 저장
};

// 경로의 시작점과 끝점에 마커를 그리는 함수
const drawRouteStartEndMarkers = (origin, destination) => {
  // 기존 마커 제거 (clearMapElements에서 이미 했지만, 중복 방지)
  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];

  const startPos = new window.kakao.maps.LatLng(origin.lat, origin.lng);
  const endPos = new window.kakao.maps.LatLng(destination.lat, destination.lng);

  // 출발 마커 (현위치)
  const startMarker = new window.kakao.maps.Marker({
    position: startPos,
    title: '출발지'
  });
  startMarker.setMap(map.value);
  routeMarkers.value.push(startMarker);

  // 도착 마커
  const endMarker = new window.kakao.maps.Marker({
    position: endPos,
    title: '도착지'
  });
  endMarker.setMap(map.value);
  routeMarkers.value.push(endMarker);
};


/**
 * [신규] 1. 장소 핀(마커)과 정보창만 지우는 함수
 */
const clearLocationMarkers = () => {
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];
  if (infoOverlay.value) {
    infoOverlay.value.setMap(null);
    infoOverlay.value = null;
  }
};

/**
 * [신규] 2. 길찾기 경로선과 출발/도착 마커만 지우는 함수
 */
const clearDirections = () => {
  if (directionsPolyline.value) {
    directionsPolyline.value.setMap(null);
    directionsPolyline.value = null;
  }
  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];
};

// 마커/오버레이 초기화
const clearMapElements = () => {
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];

  // 정보 오버레이 제거
  if (infoOverlay.value) {
    infoOverlay.value.setMap(null);
    infoOverlay.value = null;
  }

  // 길찾기 경로 선 제거
  if (directionsPolyline.value) {
    directionsPolyline.value.setMap(null);
    directionsPolyline.value = null;
  }

  // 길찾기 마커(출발/도착) 제거
  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];
};

// 아이템들을 '커스텀 핀'과 '호버 오버레이'로 표시
const drawMarkers = (items) => {
  // 맵이 없으면 중단
  if (!map.value) return;
  clearLocationMarkers();
  // 아이템이 0개일 때 바로 리턴
  if (!items.length) {
    return;
  }

  items.forEach(item => {
    const markerImage = (item.itemType === 'exhibition')
      ? exhibitionMarkerImage
      : fieldTripMarkerImage;
    const markerPosition = new window.kakao.maps.LatLng(item.lat, item.lng);
    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: item.title,
      image: markerImage
    });
    marker.setMap(map.value);
    markers.value.push(marker);
    // 3. [신규] 'mouseover' (마우스 올리기) 이벤트 리스너
    window.kakao.maps.event.addListener(marker, 'mouseover', () => {
      if (infoOverlay.value) {
        infoOverlay.value.setMap(null);
      }
      // ▼▼▼▼▼ [수정] HTML 컨텐츠를 새 이미지 스타일로 변경 ▼▼▼▼▼
      const content = `
        <div class="info-window">
          <div class="info-title">${item.title}</div>
          <div class="info-line info-rating">
            <span class="star">:별:</span>
            <span>${item.rating || 'N/A'} 점 (${item.reviewCount || 0}개)</span>
          </div>
          <div class="info-line">
            <span class="icon"><i class="bi bi-info-circle-fill"></i></span>
            <span>${item.subject || '분류 없음'}</span>
          </div>
          <div class="info-line">
            <span class="icon"><i class="bi bi-info-circle-fill"></i></span>
            <span>${item.grade || '학년 없음'}</span>
          </div>
        </div>
      `;
      // ▲▲▲▲▲ [수정] HTML 컨텐츠 끝 ▲▲▲▲▲
      const overlay = new window.kakao.maps.CustomOverlay({
        map: map.value,
        position: markerPosition,
        content: content,
        xAnchor: 0.5,
        yAnchor: 1.5, // 핀(37px)보다 위로 띄우기 (값 조절 필요)
        zIndex: 3
      });
      infoOverlay.value = overlay;
    });
    // 'mouseout' (마우스 내리기) 이벤트 리스너
    window.kakao.maps.event.addListener(marker, 'mouseout', () => {
      if (infoOverlay.value) {
        infoOverlay.value.setMap(null);
        infoOverlay.value = null;
      }
    });
    // 'click' (마커 클릭) 이벤트 리스너
    window.kakao.maps.event.addListener(marker, 'click', () => {
      // 지도 이동 함수 호출
      moveMapToItem(item.lat, item.lng);

      // 2. [추가] 활성 ID 설정
      activeItemId.value = item.id;

      // 3. [추가] 해당 카드로 스크롤
      const cardElement = document.getElementById(`card-${item.id}`);
      if (cardElement) {
        cardElement.scrollIntoView({
          behavior: 'smooth',  // 부드럽게 스크롤
          inline: 'center',   // 가로축 중앙에 오도록
          block: 'nearest'    // 세로축은 움직이지 않음
        });
      }
    });
  });
};

// 지도 로딩 중 표시
const isMapLoading = ref(false);

// --- 검색 실행 함수 (API 호출 방식으로 변경) ---
const performSearch = async () => {
  isMapLoading.value = true;
  activeItemId.value = null;

  console.log('==== API 검색 실행 시작 ====');
  console.log('검색 타입:', locationType.value);
  console.log('탭:', selectedTab.value);
  console.log('과목:', selectedSubject.value);
  console.log('학년:', selectedGrade.value);

  isSearching.value = true;
  allFetchedItems.value = [];

  // 새로운 검색이므로, 지도 위의 핀과 경로를 모두 지웁니다.
  if (map.value) {
    clearMapElements();
  }

  // 1. API 요청 파라미터 준비
  const params = {
    searchType: locationType.value, // 'all', 'radius', 'region'
    subject: selectedSubject.value,
    grade: selectedGrade.value,
  };

  try {
    if (locationType.value === 'radius') {
      console.log('반경 검색 모드, 반경:', searchRadius.value, 'km');
      if (!currentUserLocation.value) {
        console.log('현재 위치 없음, 위치 정보 요청');
        await getCurrentLocation();
      }
      if (currentUserLocation.value) {
        params.lat = currentUserLocation.value.lat;
        params.lng = currentUserLocation.value.lng;
        params.radius = searchRadius.value;
      } else {
        throw new Error("현재 위치 정보 없음 (반경 검색 실패)");
      }
    } else if (locationType.value === 'region') {
      console.log('지역 검색 모드, 지역:', selectedRegion.value);
      if (!selectedRegion.value) {
        // (선택) 지역 값이 없으면 검색을 막거나 'all'로 간주할 수 있음
        console.warn('지역 검색 선택했으나 지역명 없음. 전체 검색으로 간주.');
        params.searchType = 'all'; // 또는 에러 처리
      } else {
        params.region = selectedRegion.value;
      }
    }

    console.log('API 요청 파라미터:', params);

    // API 호출 (axios 사용)
    //    (URL은 실제 백엔드 엔드포인트로 변경해야 함)
    const response = await axios.get('/api/content/search', { params });

    // 결과 처리
    if (response.data && Array.isArray(response.data)) {
      allFetchedItems.value = response.data;
      console.log('API 응답 결과:', allFetchedItems.value.length, '개');
    } else {
      console.error('API 응답 형식이 잘못되었습니다:', response.data);
      allFetchedItems.value = [];
    }

  } catch (error) {
    console.error("API 검색 중 오류 발생:", error.response ? error.response.data : error.message);
    eventBus.emit('show-global-alert', {
      message: '장소를 검색하는 중 오류가 발생했습니다.' + (error.message.includes("위치") ? "위치 정보를 확인해주세요." : ""),
      type: 'error'
    });
    allFetchedItems.value = [];
  } finally {
    // 부드러운 페이드 효과와 함께 로딩 종료
    setTimeout(() => {
      isMapLoading.value = false;
    }, 300);

    isSearching.value = false;
    console.log('==== API 검색 완료 ====');
  }
};

// 부드러운 지도 이동 함수 추가
const smoothPanTo = (lat, lng, duration = 300) => {
  if (!map.value) return;

  const targetLatLng = new window.kakao.maps.LatLng(lat, lng);

  // panTo는 부드러운 이동, setCenter는 즉시 이동
  map.value.panTo(targetLatLng);
};

// --- 맵 초기화 시 첫 검색 실행 ---
onMounted(async () => {

  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
      level: 7, // 초기 줌 레벨 조정 (필요시)
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    window.kakao.maps.event.addListener(map.value, 'click', () => {
      activeItemId.value = null; // 하이라이트 해제
      clearDirections();       // 그려진 경로 제거
    });

    try {
      await getCurrentLocation(); // 사용자 위치 먼저 시도
      if (currentUserLocation.value && map.value) {
        const currentLatLng = new window.kakao.maps.LatLng(
          currentUserLocation.value.lat,
          currentUserLocation.value.lng
        );
        map.value.setCenter(currentLatLng); // 사용자 위치 중심으로
        map.value.setLevel(7); // 초기 줌 레벨 조정

        // 현위치 오버레이 그리기 (기존 로직 유지)
        if (currentLocationMarker.value) {
          currentLocationMarker.value.setMap(null);
        }
        const content = '<div class="current-location-dot"></div>';
        currentLocationMarker.value = new window.kakao.maps.CustomOverlay({
          position: currentLatLng,
          content: content,
        });
        currentLocationMarker.value.setMap(map.value);
      }
    } catch (error) {
      console.warn("초기 현위치 로드 실패:", error.message);
      // 실패 시 기본 위치(서울시청) 사용
      map.value.setCenter(new window.kakao.maps.LatLng(37.566826, 126.9786567));
    }

    // --- 첫 API 검색 실행 ---
    await performSearch(); // performSearch 호출

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// --- filteredItems 변경 감지 (기존 로직 유지) ---
watch(filteredItems, (newItems) => {
  if (!map.value) return;

  // 마커 그리기 전 페이드아웃 효과
  clearMarkersWithAnimation().then(() => {
    drawMarkers(newItems);

    // === 줌 레벨 로직 (기존 로직 유지) ===
    if (newItems.length === 1) {
      const item = newItems[0];
      // setCenter/setLevel 대신 부드러운 이동 함수 사용
      moveMapToItem(item.lat, item.lng);
    } else if (newItems.length > 1) {
      const bounds = new window.kakao.maps.LatLngBounds();
      newItems.forEach(item => {
        bounds.extend(new window.kakao.maps.LatLng(item.lat, item.lng));
      });
      // setBounds 대신 panToBounds 사용 (부드러운 이동)
      map.value.panToBounds(bounds, {
        duration: 500
      });
    }
  });
});

// 마커 애니메이션과 함께 제거
const clearMarkersWithAnimation = () => {
  return new Promise((resolve) => {
    markers.value.forEach((marker, index) => {
      setTimeout(() => {
        marker.setMap(null);
      }, index * 20); // 순차적으로 사라지는 효과
    });

    setTimeout(() => {
      markers.value = [];
      resolve();
    }, markers.value.length * 20 + 100);
  });
};

// --- 모달 완료 핸들러 (기존 로직 유지) ---
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  locationType.value = filterData.locationType;
  searchRadius.value = filterData.radius;
  selectedRegion.value = filterData.region;
  curriculumStore.setFilter(filterData.grade, filterData.subject);
  isModalOpen.value = false;

  performSearch(); // performSearch 호출
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
  background-color: #4A7CEC;
  /* 파란색 */
  border: 3px solid white;
  /* 흰색 테두리 */
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  /* 그림자 */
  /* 오버레이 위치 보정 (핀 중심 맞추기) */
  transform: translate(-50%, -50%);
}

.info-window {
  position: relative;
  background: rgba(255, 255, 255, 0.55);
  ;
  border-radius: 8px;
  border: 1px solid #ccc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  padding: 12px;
  width: 220px;
  /* 정보창 너비 */
  z-index: 1;
  /* 말풍선 꼬리 */
  transform: translateY(-10px);
}

.info-window::after {
  content: '';
  position: absolute;
  bottom: -10px;
  /* 꼬리 위치 */
  left: 50%;
  transform: translateX(-50%);
  border-width: 10px 10px 0 10px;
  border-style: solid;
  border-color: white transparent transparent transparent;
}

.info-window::before {
  /* 꼬리 테두리 */
  content: '';
  position: absolute;
  bottom: -11px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 11px 11px 0 11px;
  border-style: solid;
  border-color: #ccc transparent transparent transparent;
  z-index: -1;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-line {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #555;
  margin-bottom: 4px;
  white-space: nowrap;
}

.info-line:last-child {
  margin-bottom: 0;
}

.info-line .icon,
.info-line .star {
  margin-right: 8px;
  font-size: 16px;
  color: #4A7CEC;
  /* 'i' 아이콘 */
}

.info-line .star {
  color: #FFC107;
  /* 별점 색상 */
}

.info-rating {
  font-size: 13px;
  color: #666;
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
  width: 140px;
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

/* [추가] 활성 카드 하이라이트 스타일 */
/* :deep()을 사용해 자식 컴포넌트의 루트(.place-card)를 선택합니다. */
:deep(.active-card .place-card) {
  border: 2px solid #4A7CEC;
  box-shadow: 0 6px 20px rgba(74, 124, 236, 0.3);
  transform: translateY(-5px);
  /* 카드를 살짝 위로 올리는 효과 */
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

/* 우측 플로팅 원형 버튼 */
.btn-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  padding: 0;
  font-weight: 500;
}

/* 현위치 펄스 애니메이션 */
.current-location-wrapper {
  position: relative;
  transform: translate(-50%, -50%);
}

.current-location-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #4A7CEC;
  border: 3px solid white;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 2;
}

.current-location-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: rgba(74, 124, 236, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    width: 16px;
    height: 16px;
    opacity: 1;
  }

  100% {
    width: 40px;
    height: 40px;
    opacity: 0;
  }
}

/* 정보창 페이드인 효과 */
.info-window {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(-10px);
  }
}

/* 마커 호버 효과 */
.custom-marker {
  transition: transform 0.2s ease;
}

.custom-marker:hover {
  transform: scale(1.1);
}

.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
