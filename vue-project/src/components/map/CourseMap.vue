<template>
  <div class="map-rv-container">
    <div ref="mapContainer" class="course-map" v-show="!isRoadviewActive"></div>
    <div ref="roadviewContainer" class="course-map" v-show="isRoadviewActive"></div>
    <div v-if="props.isSingleLocation" class="map-toggle-buttons">
      <button @click="toggleRoadview(false)" :class="{ active: !isRoadviewActive }">지도</button>
      <button @click="toggleRoadview(true)" :class="{ active: isRoadviewActive }">로드뷰</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
// ✅ 카카오맵 로더 import
import { isKakaoMapLoaded, loadKakaoMap } from '@/utils/kakaoMapLoader';

// --- Props 정의 ---
const props = defineProps({
  items: {
    type: Array,
    required: true,
  },
  title: {
    type: String,
    default: '코스 지도',
  },
  isSingleLocation: {
    type: Boolean,
    default: false
  },
  defaultZoomLevel: {
    type: Number,
    default: 5
  },
  pageType: {
    type: String,
    default: 'place'
  }
});

// --- 맵과 요소들을 참조할 ref ---
const mapContainer = ref(null);
const map = ref(null);
const markers = ref([]);
const polyline = ref(null);
const roadviewContainer = ref(null);
const roadview = ref(null);
const isRoadviewActive = ref(false);
const rvClient = ref(null);

// ✅ 맵 초기화 (카카오맵 로더 사용)
onMounted(async () => {
  try {
    // 카카오맵이 로드되지 않았다면 대기
    if (!isKakaoMapLoaded()) {
      console.log('[CourseMap] 카카오맵 대기 중...');
      await loadKakaoMap();
    }

    if (window.kakao && window.kakao.maps) {
      const options = {
        center: new window.kakao.maps.LatLng(36.3758, 127.3845),
        level: props.isSingleLocation ? props.defaultZoomLevel : 5,
      };
      map.value = new window.kakao.maps.Map(mapContainer.value, options);

      roadview.value = new window.kakao.maps.Roadview(roadviewContainer.value);
      rvClient.value = new window.kakao.maps.RoadviewClient();

      drawCourseOnMap(props.items);
    } else {
      console.error('[CourseMap] 카카오맵 API가 로드되지 않았습니다.');
    }
  } catch (error) {
    console.error('[CourseMap] 지도 초기화 실패:', error);
  }
});

// --- props.items가 변경될 때마다 맵 다시 그리기 ---
watch(() => props.items, (newItems) => {
  if (!map.value) return;
  drawCourseOnMap(newItems);

  if (isRoadviewActive.value) {
    toggleRoadview(false);
  }
});

// API 경로 가져오기
const getRoutePathFromAPI = async (items) => {
  if (props.pageType === 'exhibition') {
    console.log('[API] pageType이 "exhibition"이므로 API 경로 계산을 건너뛰고 직선 경로를 사용합니다.');
    return null;
  }

  if (items.length < 2) {
    console.warn('[API] 경로를 계산하기에 아이템이 부족합니다.');
    return null;
  }

  const origin = items[0];
  const destination = items[items.length - 1];
  const waypoints = items.slice(1, -1);

  const payload = {
    origin: { x: origin.lng.toString(), y: origin.lat.toString() },
    destination: { x: destination.lng.toString(), y: destination.lat.toString() },
    waypoints: waypoints.map(item => ({ x: item.lng.toString(), y: item.lat.toString() })),
    priority: "TIME",
  };

  const API_KEY = import.meta.env.VITE_KAKAO_REST_KEY;
  const API_URL = 'https://apis-navi.kakaomobility.com/v1/waypoints/directions';

  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `KakaoAK ${API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`Kakao Directions API 오류: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();

    if (data.routes && data.routes.length > 0) {
      const route = data.routes[0];
      const allPoints = [];
      route.sections.forEach(section => {
        section.roads.forEach(road => {
          road.vertexes.forEach((coord, index) => {
            if (index % 2 === 0) {
              const x = coord;
              const y = road.vertexes[index + 1];
              allPoints.push(new window.kakao.maps.LatLng(y, x));
            }
          });
        });
      });
      console.log(`[API] 길찾기 경로 좌표 ${allPoints.length}개 추출 완료.`);
      return allPoints;
    }
    return null;
  } catch (error) {
    console.error('[API] 길찾기 API 호출 중 오류 발생:', error);
    return null;
  }
};

// 로드뷰 토글
const toggleRoadview = (showRoadview) => {
  isRoadviewActive.value = showRoadview;
  if (showRoadview) {
    if (!props.items || props.items.length === 0) return;
    const item = props.items[0];
    const position = new window.kakao.maps.LatLng(Number(item.lat), Number(item.lng));

    setTimeout(() => {
      roadview.value.relayout();

      rvClient.value.getNearestPanoId(position, 200, (panoId) => {
        if (panoId) {
          roadview.value.setPanoId(panoId, position);
          new window.kakao.maps.Marker({
            position: position,
            map: roadview.value
          });
        } else {
          alert('이 위치에는 로드뷰 정보가 없습니다.');
          isRoadviewActive.value = false;
        }
      });
    }, 0);
  } else {
    if (map.value) {
      setTimeout(() => {
        map.value.relayout();
        if (bounds && !bounds.isEmpty()) {
          if (props.isSingleLocation) {
            const centerPosition = new window.kakao.maps.LatLng(Number(props.items[0].lat), Number(props.items[0].lng));
            map.value.setCenter(centerPosition);
          } else {
            map.value.setBounds(bounds);
          }
        }
      }, 0);
    }
  }
};

// 맵 요소 제거
const clearMapElements = () => {
  if (markers.value.length > 0) {
    markers.value.forEach(overlay => overlay.setMap(null));
    markers.value = [];
  }
  if (polyline.value) {
    polyline.value.setMap(null);
    polyline.value = null;
  }
};

let bounds = null;

// 맵에 핀과 선 그리기
const drawCourseOnMap = async (items) => {
  console.log('[CourseMap] drawCourseOnMap 호출됨 / pageType:', props.pageType);

  if (!map.value || !mapContainer.value || !items || items.length === 0) {
    if (!items || items.length === 0) clearMapElements();
    return;
  }

  clearMapElements();

  const newMarkers = [];
  const straightPath = [];

  bounds = new window.kakao.maps.LatLngBounds();

  items.forEach((item, index) => {
    if (item.lat == null || item.lng == null || isNaN(Number(item.lat)) || isNaN(Number(item.lng))) {
      console.error(`[CourseMap] 아이템 ${index}의 좌표가 유효하지 않습니다. 마커 생성을 건너뜁니다.`, item);
      return;
    }

    const position = new window.kakao.maps.LatLng(Number(item.lat), Number(item.lng));

    const itemNumber = props.isSingleLocation ? '' : (item.number || (index + 1));
    const markerColor = props.isSingleLocation ? '#FF5A5A' : getCourseItemColor(itemNumber);
    const markerImageSrc = createMarkerImage(itemNumber, markerColor);

    if (!markerImageSrc || typeof markerImageSrc !== 'string' || !markerImageSrc.startsWith('data:image/svg+xml')) {
      console.error(`[CourseMap] 아이템 ${index} 마커 이미지 소스가 유효하지 않습니다.`, markerImageSrc);
      return;
    }

    let markerImage;
    try {
      markerImage = new window.kakao.maps.MarkerImage(
        markerImageSrc,
        new window.kakao.maps.Size(24, 35),
        { offset: new window.kakao.maps.Point(12, 35) }
      );
    } catch (imgError) {
      console.error(`[CourseMap] 아이템 ${index} MarkerImage 생성 중 오류 발생:`, imgError);
      return;
    }

    try {
      const marker = new window.kakao.maps.Marker({
        position: position,
        image: markerImage,
        map: map.value,
      });

      newMarkers.push(marker);
      straightPath.push(position);
      bounds.extend(position);
    } catch (markerError) {
      console.error(`[CourseMap] 아이템 ${index} 마커 생성 중 오류 발생:`, markerError);
    }
  });

  markers.value = newMarkers;

  if (props.isSingleLocation) {
    console.log('[CourseMap] 단일 위치 모드. 경로를 그리지 않고 마커만 표시합니다.');
  } else {
    let polylinePath = straightPath;
    if (props.pageType !== 'exhibition') {
      const apiPath = await getRoutePathFromAPI(items);
      if (apiPath) {
        polylinePath = apiPath;
        console.log('[CourseMap] API 경로 사용 완료.');
      } else {
        console.log('[CourseMap] API 경로 실패. 직선 경로로 대체합니다.');
      }
    } else {
      console.log('[CourseMap] "exhibition" 타입이므로 직선 경로를 사용합니다.');
    }

    if (polylinePath.length > 1) {
      try {
        const newPolyline = new window.kakao.maps.Polyline({
          path: polylinePath,
          strokeWeight: 4,
          strokeColor: '#4A7CEC',
          strokeOpacity: 0.8,
          strokeStyle: 'solid',
        });
        newPolyline.setMap(map.value);
        polyline.value = newPolyline;
        console.log('[CourseMap] 폴리라인 생성 및 추가 완료.');
      } catch (polyError) {
        console.error('[CourseMap] 폴리라인 생성 중 오류 발생:', polyError, polylinePath);
      }
    } else {
      console.log('[CourseMap] 폴리라인을 그리기에 점이 부족합니다.');
    }
  }

  if (!bounds.isEmpty()) {
    if (props.isSingleLocation) {
      const centerPosition = new window.kakao.maps.LatLng(Number(items[0].lat), Number(items[0].lng));
      map.value.setCenter(centerPosition);
      map.value.setLevel(props.defaultZoomLevel);
    } else {
      try {
        map.value.setBounds(bounds);
      } catch (boundsError) {
        console.error('[CourseMap] 지도 범위 설정 중 오류 발생:', boundsError, bounds);
      }
    }
  } else {
    console.warn('[CourseMap] 유효한 범위가 없어 지도 범위를 설정할 수 없습니다.');
  }
};

// 마커 색상 결정
const getCourseItemColor = (itemNumber) => {
  const colors = ['#FF5A5A', '#4A7CEC', '#28A745', '#FFC107', '#DC3545', '#6F42C1', '#E83E8C'];
  if (typeof itemNumber !== 'number' || isNaN(itemNumber) || itemNumber < 1) {
    return colors[1];
  }
  return colors[(itemNumber - 1) % colors.length];
}

// 마커 SVG 이미지 생성
const createMarkerImage = (number, color) => {
  const svg = `
  <svg width="24" height="35" viewBox="0 0 24 35" xmlns="http://www.w3.org/2000/svg">
    <path d="M12 0C5.373 0 0 5.373 0 12c0 9 12 23 12 23s12-14 12-23c0-6.627-5.373-12-12-12z"
      fill="${color}" stroke="#fff" stroke-width="2"/>
    <circle cx="12" cy="12" r="8" fill="#fff"/>
    <text x="12" y="16" text-anchor="middle" font-family="Arial, sans-serif"
      font-size="10" font-weight="bold" fill="${color}">${number}</text>
  </svg>
  `;
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
};
</script>

<style scoped>
.map-rv-container {
  position: relative;
  width: 100%;
  height: 250px;
  background-color: #eee;
}

.course-map {
  width: 100%;
  height: 250px;
  background-color: #eee;
}

.course-map {
  width: 100%;
  height: 100%;
}

.map-toggle-buttons {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  display: flex;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.map-toggle-buttons button {
  padding: 5px 10px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: #555;
}

.map-toggle-buttons button.active {
  background-color: #4A7CEC;
  color: white;
  border-radius: 5px;
}
</style>

<style>
.custom-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
</style>
