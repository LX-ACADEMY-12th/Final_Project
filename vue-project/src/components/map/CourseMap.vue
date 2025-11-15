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
  // 🚨 [추가] 부모로부터 코스 타입을 받아 경로 로직을 분기합니다.
  pageType: {
    type: String,
    default: 'place' // 'place' (답사)가 기본, 'exhibition' (전시)는 직선 경로
  }
});

// --- 맵과 요소들을 참조할 ref ---
const mapContainer = ref(null); // 템플릿의 div와 연결
const map = ref(null); // 카카오맵 인스턴스
const markers = ref([]); // 마커 목록
const polyline = ref(null); // 폴리라인(선)
const roadviewContainer = ref(null); // 템플릿의 로드뷰 div와 연결
const roadview = ref(null); // 카카오 로드뷰 인스턴스
const isRoadviewActive = ref(false); // 로드뷰 활성 상태
const rvClient = ref(null); // 로드뷰 PanoID 탐색기

// --- 맵 초기화 (컴포넌트 마운트 시) ---
onMounted(() => {
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
    console.error('카카오맵 API가 로드되지 않았습니다.');
  }
});

// --- props.items가 변경될 때마다 맵 다시 그리기 ---
watch(() => props.items, (newItems) => {
  if (!map.value) return;
  drawCourseOnMap(newItems);

  if (isRoadviewActive.value) {
    toggleRoadview(false); // 지도로 복귀
  }
});

// 🚨 [수정] API 키와 URL을 사용하여 경로 데이터를 가져오는 비동기 함수
const getRoutePathFromAPI = async (items) => {
  // 🚨 [분기 로직 추가] pageType이 'exhibition'이면 API 호출 건너뛰고 null 반환
  if (props.pageType === 'exhibition') {
    console.log('[API] pageType이 "exhibition"이므로 API 경로 계산을 건너뛰고 직선 경로를 사용합니다.');
    return null;
  }

  // 1. items가 2개 미만이면 (경로 계산 불필요) null을 반환합니다.
  if (items.length < 2) {
    console.warn('[API] 경로를 계산하기에 아이템이 부족합니다.');
    return null;
  }

  // 2. 출발지, 목적지, 경유지를 items 배열에서 분리합니다.
  const origin = items[0];
  const destination = items[items.length - 1];
  const waypoints = items.slice(1, -1);

  // 3. API 요청 본문(Payload)을 구성합니다.
  const payload = {
    origin: { x: origin.lng.toString(), y: origin.lat.toString() },
    destination: { x: destination.lng.toString(), y: destination.lat.toString() },
    waypoints: waypoints.map(item => ({ x: item.lng.toString(), y: item.lat.toString() })),
    priority: "TIME",
  };

  // 4. 환경 변수에서 REST API 키를 가져옵니다.
  const API_KEY = import.meta.env.VITE_KAKAO_REST_KEY;
  const API_URL = 'https://apis-navi.kakaomobility.com/v1/waypoints/directions';

  try {
    // 5. fetch를 사용하여 POST 요청을 보냅니다.
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

    // 6. 응답 데이터(data)에서 경로 좌표를 추출합니다.
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

// [신규] 로드뷰 <-> 지도 토글 함수
const toggleRoadview = (showRoadview) => {
  isRoadviewActive.value = showRoadview;
  if (showRoadview) {
    // --- 로드뷰 켜기 ---
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
          isRoadviewActive.value = false; // 다시 지도로 강제 복귀
        }
      });
    }, 0);
  } else {
    // --- 지도 켜기 ---
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

// --- 기존 맵 요소들(핀, 선) 제거 함수 ---
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

// [수정] bounds 변수를 함수 밖(전역)으로 이동
let bounds = null;

// --- 맵에 핀과 선을 그리는 핵심 함수 ---
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
      console.error(`[CourseMap]  아이템 ${index} MarkerImage 생성 중 오류 발생:`, imgError);
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
      console.error(`[CourseMap]  아이템 ${index} 마커 생성 중 오류 발생:`, markerError);
    }
  });

  markers.value = newMarkers;

  // 🚨 [핵심 수정] pageType이 'exhibition'이면 API 호출 없이 straightPath 사용
  let polylinePath = straightPath;
  if (props.pageType !== 'exhibition') {
    const apiPath = await getRoutePathFromAPI(items);
    // API 경로가 성공하면 그것을 사용하고, 실패하면 straightPath를 사용
    if (apiPath) {
      polylinePath = apiPath;
      console.log('[CourseMap] API 경로 사용 완료.');
    } else {
      console.log('[CourseMap] API 경로 실패. 직선 경로로 대체합니다.');
    }
  } else {
    console.log('[CourseMap] "exhibition" 타입이므로 직선 경로를 사용합니다.');
  }

  // --- 폴리라인(선) 생성 ---
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
      console.error('[CourseMap]  폴리라인 생성 중 오류 발생:', polyError, polylinePath);
    }
  } else {
    console.log('[CourseMap] 폴리라인을 그리기에 점이 부족합니다.');
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
        console.error('[CourseMap]  지도 범위 설정 중 오류 발생:', boundsError, bounds);
      }
    }
  } else {
    console.warn('[CourseMap] 유효한 범위가 없어 지도 범위를 설정할 수 없습니다.');
  }
};

// --- 마커 색상 결정 함수
const getCourseItemColor = (itemNumber) => {
  const colors = ['#FF5A5A', '#4A7CEC', '#28A745', '#FFC107', '#DC3545', '#6F42C1', '#E83E8C'];
  if (typeof itemNumber !== 'number' || isNaN(itemNumber) || itemNumber < 1) {
    return colors[1];
  }
  return colors[(itemNumber - 1) % colors.length];
}

// --- 마커 SVG 이미지 생성 함수
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
/* 맵/로드뷰를 감싸는 부모 */
.map-rv-container {
  position: relative;
  width: 100%;
  height: 250px;
  background-color: #eee;
}

/* 맵 컨테이너의 크기를 지정해야 합니다. */
.course-map {
  width: 100%;
  height: 250px;
  background-color: #eee;
}

/* 맵과 로드뷰 div 공통 스타일 */
.course-map {
  width: 100%;
  height: 100%;
}

/* 토글 버튼 스타일 */
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
/* [중요]
  CustomOverlay의 스타일은 <style scoped>가 아닌
  일반 <style> 태그에 정의해야 카카오맵이 인식할 수 있습니다.
*/
.custom-marker {
  /* [요청 1] 동그란 핀 */
  width: 32px;
  height: 32px;
  border-radius: 50%;
  /* [요청 2] 색상 - 인라인 스타일(style=...)로 적용됨 */
  /* [요청 3] 숫자 - 텍스트 스타일 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
  /* 그림자 및 테두리 (디자인 개선) */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
</style>
