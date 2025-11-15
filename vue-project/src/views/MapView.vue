<template>
  <div class="vh-100 position-relative overflow-hidden bg-light" style="font-family: 'SUIT', sans-serif">

    <div ref="mapContainer" class="map-placeholder map-full-screen"></div>

    <div class="position-absolute d-flex align-items-center p-3 bg-white rounded-4 shadow map-top-bar">

      <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 profile-avatar-wrapper">
        <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필" class="profile-image-cover">
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="#888" class="bi bi-person-fill"
          viewBox="0 0 16 16">
          <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zM8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
        </svg>
      </div>

      <div class="flex-grow-1" style="font-family: 'SUIT', sans-serif">
        <div class="text-secondary map-greeting">안녕하세요</div>
        <div class="fw-bold map-username">{{ userName }}</div>
      </div>

      <button
        class="btn btn-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0 search-btn"
        @click.prevent="isModalOpen = true">
        <i class="bi bi-search fs-5"></i>
      </button>
    </div>

    <div class="position-absolute d-flex flex-row map-tabs-wrapper">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="changeTab('전시')">전시관</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
        @click="changeTab('답사')">과학 탐험지</button>
    </div>

    <div class="position-absolute d-flex flex-column map-control-buttons">
      <button class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center"
        @click="goToCurrentLocation">
        <i class="bi bi-bullseye" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">현위치</span>
      </button>
    </div>

    <div class="position-absolute map-carousel-wrapper">
      <div class="card-carousel">
        <div class="d-flex flex-row align-items-center card-row-inner">

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

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" class="bottom-navbar-fixed" />

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
import rawAxios from 'axios';
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';
import eventBus from '@/utils/eventBus';
// 🟢 Pinia 스토어 관련 import 추가
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import { useCurriculumStore } from '@/stores/curriculumStore';

const router = useRouter();
const activeItemId = ref(null);

// 🟢 시연을 위한 대전 시청 고정 좌표
const DEMO_LOCATION = { lat: 36.3504119, lng: 127.3845475 };

// 🟢 Pinia 스토어 초기화 및 상태 가져오기
const authStore = useAuthStore();
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

const infoOverlay = ref(null);
// 🚨 경로 선을 배열로 저장하도록 초기화
const directionsPolyline = ref([]);
const routeMarkers = ref([]);
const KAKAO_REST_KEY = import.meta.env.VITE_KAKAO_REST_KEY;

// 1. 마커 이미지 크기/옵션 설정 (핀 크기에 맞게 조절하세요)
const imageSize = new window.kakao.maps.Size(32, 37); // 예: 32x37px 핀
const imageOption = { offset: new window.kakao.maps.Point(16, 37) }; // 핀의 하단 중앙
// 2. '전시' (과학관) 핀 이미지 객체 생성
const exhibitionMarkerImage = new window.kakao.maps.MarkerImage(
  '/museum.png',
  imageSize,
  imageOption
);
// 3. '답사' (현장학습) 핀 이미지 객체 생성
const fieldTripMarkerImage = new window.kakao.maps.MarkerImage(
  '/experiment.png',
  imageSize,
  imageOption
);

// 🟢 user 상태에 따라 화면에 표시할 이름을 계산하는 computed 속성
const userName = computed(() => {
  if (user.value?.name) {
    return `${user.value.name} 학부모님`;
  }
  return '로그인 필요';
});

// --- 필터 및 검색 상태 ---
const locationType = ref('all');
const searchRadius = ref(5);
const selectedRegion = ref('');

const currentUserLocation = ref(null);
const isSearching = ref(false);

//'전체' 데이터를 보관할 새 Ref
const allFetchedItems = ref([]);

// '필터링'된 결과를 보여줄 computed 속성
const filteredItems = computed(() => {
  const items = allFetchedItems.value;

  if (selectedTab.value === '전시') {
    return items.filter(item => item.itemType === 'exhibition');
  } else {
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
  activeItemId.value = item.id;
  // 지도 이동
  smoothPanTo(item.lat, item.lng);

  setTimeout(() => {
    // 🚨 맵 이동 후 충분한 시간을 주고 애니메이션 호출
    highlightMarker(item);
    showDirectionsToItem(item);
  }, 500); // 맵 이동 시간(300ms) + 추가 지연 200ms
};

// 마커 하이라이트 함수
const highlightMarker = (item) => {
  // 🚨 [핵심 수정] Animation 객체가 존재하는지 안전하게 확인
  const BOUNCE_ANIMATION = window.kakao?.maps?.Animation?.BOUNCE;

  if (!BOUNCE_ANIMATION) {
    // 객체가 없으면 경고만 출력하고 실행 중단 (TypeError 방지)
    console.warn("Kakao maps Animation 객체가 로드되지 않았거나 BOUNCE 속성을 찾을 수 없습니다.");
    return;
  }

  // 마커 순회 및 하이라이트 적용
  markers.value.forEach(marker => {
    const position = marker.getPosition();

    // 위도와 경도가 일치하는 마커를 찾습니다.
    if (position.getLat() === item.lat && position.getLng() === item.lng) {

      marker.setAnimation(BOUNCE_ANIMATION);

      setTimeout(() => {
        marker.setAnimation(null);
      }, 2000);
    }
  });
};

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

      map.value.panTo(currentLatLng);

      setTimeout(() => {
        map.value.setLevel(3, {
          animate: {
            duration: 300
          }
        });
      }, 300);

      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null);
      }

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

    map.value.panTo(itemLatLng);

    setTimeout(() => {
      const targetLevel = 4;
      map.value.setLevel(targetLevel, {
        animate: {
          duration: 300
        }
      });
    }, 300);
  }
};

/**
 * 길찾기 실행 (Orchestrator)
 */
const showDirectionsToItem = async (item) => {
  if (!currentUserLocation.value) {
    eventBus.emit('show-global-alert', {
      message: '현위치를 먼저 확인해주세요.',
      type: 'error'
    });
    return;
  }

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
    const { path, bounds } = await fetchDirections(origin, destination);

    if (path.length > 0) {
      drawDirectionsPolyline(path);
      drawRouteStartEndMarkers(origin, destination); // 🚨 마커 생성은 이 함수에서 처리
      map.value.setBounds(bounds);
    }
  } catch (error) {
    console.error("길찾기 실패:", error);
    eventBus.emit('show-global-alert', {
      message: '경로를 찾는 데 실패했습니다.',
      type: 'error'
    });
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
    priority: 'RECOMMEND'
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
      const path = [];
      const bounds = new window.kakao.maps.LatLngBounds();

      sections.forEach(section => {
        section.roads.forEach(road => {
          for (let i = 0; i < road.vertexes.length; i += 2) {
            const lng = road.vertexes[i];
            const lat = road.vertexes[i + 1];
            const latLng = new window.kakao.maps.LatLng(lat, lng);
            path.push(latLng);
            bounds.extend(latLng);
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
  if (directionsPolyline.value.length > 0) {
    directionsPolyline.value.forEach(p => p.setMap(null));
  }
  directionsPolyline.value = []; // 배열로 변경

  // 1. [배경 경로] 굵고 진한 테마색 선 (밑바탕)
  const backgroundPolyline = new window.kakao.maps.Polyline({
    path: path,
    strokeWeight: 6,
    strokeColor: '#4A7CEC', // 주 테마색 파란색
    strokeOpacity: 0.9,
    strokeStyle: 'solid'
  });
  backgroundPolyline.setMap(map.value);
  directionsPolyline.value.push(backgroundPolyline);

  // 2. [트레일 경로] 얇은 흰색 점선 (흐름 강조)
  const trailPolyline = new window.kakao.maps.Polyline({
    path: path,
    strokeWeight: 3,
    strokeColor: '#FFFFFF', // 흰색 점선
    strokeOpacity: 1,
    strokeStyle: 'dashed', // 점선 효과
  });
  trailPolyline.setMap(map.value);
  directionsPolyline.value.push(trailPolyline); // 배열에 모두 저장
};

// 경로의 시작점과 끝점에 마커를 그리는 함수
const drawRouteStartEndMarkers = (origin, destination) => {
  // 🚨 마커 생성 로직을 비활성화하고 배열만 초기화합니다.
  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];
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
  // 🚨 배열인지 확인하고, 배열의 각 요소에 대해 setMap(null) 호출
  if (directionsPolyline.value && Array.isArray(directionsPolyline.value)) {
    directionsPolyline.value.forEach(p => p.setMap(null));
  }
  directionsPolyline.value = []; // 배열 초기화

  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];
};

// 마커/오버레이 초기화
const clearMapElements = () => {
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];

  if (infoOverlay.value) {
    infoOverlay.value.setMap(null);
    infoOverlay.value = null;
  }

  // 🚨 길찾기 경로 선 제거 (배열 처리)
  if (directionsPolyline.value && Array.isArray(directionsPolyline.value)) {
    directionsPolyline.value.forEach(p => p.setMap(null));
  }
  directionsPolyline.value = []; // 배열 초기화

  routeMarkers.value.forEach(marker => marker.setMap(null));
  routeMarkers.value = [];
};

// 아이템들을 '커스텀 핀'과 '호버 오버레이'로 표시
const drawMarkers = (items) => {
  if (!map.value) return;
  clearLocationMarkers();
  if (!items.length) {
    return;
  }

  items.forEach(item => {
    // 🚨 위도/경도가 유효한 숫자인지 확인
    if (item.lat === null || item.lng === null || isNaN(Number(item.lat)) || isNaN(Number(item.lng))) {
      console.warn(`[NaN Error] 유효하지 않은 좌표를 건너뜁니다:`, item);
      return; // 유효하지 않으면 이 아이템은 건너뜁니다.
    }
    const markerImage = (item.itemType === 'exhibition') ? exhibitionMarkerImage : fieldTripMarkerImage;
    const markerPosition = new window.kakao.maps.LatLng(item.lat, item.lng);
    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: item.title,
      image: markerImage
    });
    marker.setMap(map.value);
    markers.value.push(marker);

    window.kakao.maps.event.addListener(marker, 'mouseover', () => {
      if (infoOverlay.value) {
        infoOverlay.value.setMap(null);
      }
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
      const overlay = new window.kakao.maps.CustomOverlay({
        map: map.value,
        position: markerPosition,
        content: content,
        xAnchor: 0.5,
        yAnchor: 1.5,
        zIndex: 3
      });
      infoOverlay.value = overlay;
    });

    window.kakao.maps.event.addListener(marker, 'mouseout', () => {
      if (infoOverlay.value) {
        infoOverlay.value.setMap(null);
        infoOverlay.value = null;
      }
    });

    window.kakao.maps.event.addListener(marker, 'click', () => {
      moveMapToItem(item.lat, item.lng);
      activeItemId.value = item.id;

      const cardElement = document.getElementById(`card-${item.id}`);
      if (cardElement) {
        cardElement.scrollIntoView({
          behavior: 'smooth',
          inline: 'center',
          block: 'nearest'
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

  isSearching.value = true;
  allFetchedItems.value = [];

  if (map.value) {
    clearMapElements();
  }

  const params = {
    searchType: locationType.value,
    subject: selectedSubject.value,
    grade: selectedGrade.value,
  };

  try {
    if (locationType.value === 'radius') {
      if (!currentUserLocation.value) {
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
      if (!selectedRegion.value) {
        console.warn('지역 검색 선택했으나 지역명 없음. 전체 검색으로 간주.');
        params.searchType = 'all';
      } else {
        params.region = selectedRegion.value;
      }
    }

    const response = await axios.get('/api/content/search', { params });

    if (response.data && Array.isArray(response.data)) {
      allFetchedItems.value = response.data;
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
    setTimeout(() => {
      isMapLoading.value = false;
    }, 300);

    isSearching.value = false;
  }
};

// 부드러운 지도 이동 함수 추가
const smoothPanTo = (lat, lng, duration = 300) => {
  if (!map.value) return;

  const targetLatLng = new window.kakao.maps.LatLng(lat, lng);
  map.value.panTo(targetLatLng);
};

// --- 맵 초기화 시 첫 검색 실행 ---
onMounted(async () => {

  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
      level: 7,
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    window.kakao.maps.event.addListener(map.value, 'click', () => {
      activeItemId.value = null;
      clearDirections();
    });

    try {
      await getCurrentLocation();
      if (currentUserLocation.value && map.value) {
        const currentLatLng = new window.kakao.maps.LatLng(
          currentUserLocation.value.lat,
          currentUserLocation.value.lng
        );
        map.value.setCenter(currentLatLng);
        map.value.setLevel(7);

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
      map.value.setCenter(new window.kakao.maps.LatLng(37.566826, 126.9786567));
    }

    await performSearch();

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// MapComponent.vue의 watch(filteredItems, ...) 핸들러 내부
watch(filteredItems, (newItems) => {
  if (!map.value) return;

  clearMarkersWithAnimation().then(() => {
    drawMarkers(newItems);

    if (newItems.length > 0) {
      const bounds = new window.kakao.maps.LatLngBounds();
      let hasValidItem = false; // 유효한 아이템이 하나라도 있는지 확인

      newItems.forEach(item => {
        // 🚨 [추가 검증] 유효한 좌표만 bounds에 포함
        if (item.lat !== null && item.lng !== null && !isNaN(Number(item.lat)) && !isNaN(Number(item.lng))) {
          bounds.extend(new window.kakao.maps.LatLng(item.lat, item.lng));
          hasValidItem = true;
        }
      });

      if (hasValidItem) { // 유효한 좌표가 있어야 setBounds 호출
        if (newItems.length === 1) {
          // 단일 아이템일 경우, moveMapToItem 호출
          moveMapToItem(newItems[0].lat, newItems[0].lng);
        } else {
          // 여러 아이템일 경우
          map.value.setBounds(bounds);
        }
      } else {
        // 모든 아이템의 좌표가 무효할 경우
        console.warn("경계 설정: 유효한 좌표를 가진 아이템이 없어 지도를 이동하지 않습니다.");
      }
    }
  });
});

// 마커 애니메이션과 함께 제거
const clearMarkersWithAnimation = () => {
  return new Promise((resolve) => {
    markers.value.forEach((marker, index) => {
      setTimeout(() => {
        marker.setMap(null);
      }, index * 20);
    });

    setTimeout(() => {
      markers.value = [];
      resolve();
    }, markers.value.length * 20 + 100);
  });
};

// --- 모달 완료 핸들러 (기존 로직 유지) ---
const handleFilterComplete = (filterData) => {
  locationType.value = filterData.locationType;
  searchRadius.value = filterData.radius;
  selectedRegion.value = filterData.region;
  curriculumStore.setFilter(filterData.grade, filterData.subject);
  isModalOpen.value = false;

  performSearch();
};

// 하단 네비게이션 핸들러
const handleNavigation = (navItemName) => {
  selectedNavItem.value = navItemName;
  if (navItemName === '홈') router.push('/home');
  else if (navItemName === '목록') router.push('/list');
  else if (navItemName === '지도') router.push('/map');
  else if (navItemName === '코스관리') router.push('/usercourselist');
  else if (navItemName === '마이페이지') router.push('/mypage');
};

</script>

<style>
/* ------------------------------------------------------------------ */
/* [전역 스타일] Custom Overlay CSS (MapComponent.vue와 분리되어야 함) */
/* ------------------------------------------------------------------ */
.current-location-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #4A7CEC;
  border: 3px solid white;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  transform: translate(-50%, -50%);
}

.info-window {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  border: 1px solid #ccc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  padding: 12px;
  width: 220px;
  z-index: 1;
  transform: translateY(-10px);
}

.info-window::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 10px 10px 0 10px;
  border-style: solid;
  border-color: rgba(255, 255, 255, 0.9) transparent transparent transparent;
}

.info-window::before {
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
}

.info-line .star {
  color: #FFC107;
}

.info-rating {
  font-size: 13px;
  color: #666;
}
</style>

<style scoped>
/* ------------------------------------------------------------------ */
/* [Scoped 스타일] MapComponent.vue */
/* ------------------------------------------------------------------ */

/* 🚨 0. Root Container (vh-100) */
.vh-100 {
  height: 100vh;
}

/* 🚨 1. 지도 영역 (화면 전체를 채움) */
.map-full-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 🚨 2. 상단 프로필/검색 버튼 (위치 및 스타일 정리) */
.map-top-bar {
  z-index: 20;
  gap: 12px;
  width: calc(100% - 36px);
  height: 72px;
  left: 18px;
  top: 16px;
}

.profile-avatar-wrapper {
  width: 48px;
  height: 48px;
  background-color: #f0f0f0;
  border: 1px solid #eee;
}

.profile-image-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.map-greeting {
  font-size: 0.9rem;
}

.map-username {
  font-size: 1.1rem;
}

.search-btn {
  width: 48px;
  height: 48px;
  background-color: #4A7CEC;
  border-color: #4A7CEC;
  color: white;
}

/* 🚨 3. 탭 버튼 (위치 정리) */
.map-tabs-wrapper {
  position: absolute;
  z-index: 10;
  top: 104px;
  left: 18px;
  gap: 8px;
}

/* 상단 필터 버튼 (전시, 탐험) - 기존 스타일 유지 */
.spec-button {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px 16px;
  width: 140px;
  height: 38px;
  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  font-weight: 500;
}

.spec-button.active {
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}

/* 🚨 4. 우측 현위치 버튼 (위치 정리) */
.map-control-buttons {
  position: absolute;
  z-index: 10;
  gap: 10px;
  right: 18px;
  /* 카드 캐러셀 (170px) + 네비바 (63px) + 여백 (16px) 위 */
  bottom: calc(170px + 63px + 16px);
}

/* 우측 플로팅 원형 버튼 - 기존 스타일 유지 */
.btn-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  padding: 0;
  font-weight: 500;
}

/* 🚨 5. 하단 카드 캐러셀 (위치 정리) */
.map-carousel-wrapper {
  position: absolute;
  bottom: 63px;
  /* 하단 네비게이션바 높이 (63px) 위 */
  left: 0;
  right: 0;
  height: 170px;
  z-index: 20;
}

.card-carousel {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  height: 100%;
}

.card-row-inner {
  gap: 16px;
  height: 100%;
  padding: 0 18px;
}

/* 하단 카드 캐러셀 스크롤바 숨기기 */
.card-carousel::-webkit-scrollbar {
  display: none;
}

.card-carousel {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 🚨 6. 하단 네비바 고정을 위한 클래스 (BottomNavbar 컴포넌트에 적용 필요) */
.bottom-navbar-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 63px;
}

/* [추가] 활성 카드 하이라이트 스타일 */
:deep(.active-card .place-card) {
  border: 2px solid #4A7CEC;
  box-shadow: 0 6px 20px rgba(74, 124, 236, 0.3);
  transform: translateY(-5px);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

/* 현위치 펄스 애니메이션 */
.current-location-wrapper {
  position: relative;
  transform: translate(-50%, -50%);
}

.current-location-dot {
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
