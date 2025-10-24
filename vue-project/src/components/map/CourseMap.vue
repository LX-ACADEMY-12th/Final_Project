<template>
  <div ref="mapContainer" class="course-map"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

// --- 1. Props 정의 ---
const props = defineProps({
  // 부모로부터 코스 아이템 배열을 받음
  items: {
    type: Array,
    required: true,
  },
  // 부모로부터 코스 제목을 받음 (필요시 사용)
  title: {
    type: String,
    default: '코스 지도',
  },
});

// --- 2. 맵과 요소들을 참조할 ref ---
const mapContainer = ref(null); // 템플릿의 div와 연결
const map = ref(null); // 카카오맵 인스턴스
const markers = ref([]); // 10.24 추가 : 마커 목록
const polyline = ref(null); // 폴리라인(선)

// --- 3. 맵 초기화 (컴포넌트 마운트 시) ---
onMounted(() => {
  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(36.3758, 127.3845), // 기본 중심 (예: 첫 아이템)
      level: 5,
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    // 맵이 로드되면 바로 핀과 선을 그립니다.
    drawCourseOnMap(props.items);

  } else {
    console.error('카카오맵 API가 로드되지 않았습니다.');
  }
});

// --- 4. props.items가 변경될 때마다 맵 다시 그리기 ---
watch(() => props.items, (newItems) => {
  if (!map.value) return; // 맵이 아직 준비되지 않았다면 중단
  drawCourseOnMap(newItems);
});

// --- 5. 기존 맵 요소들(핀, 선) 제거 함수 ---
const clearMapElements = () => {
  // 마커 제거
  if (markers.value.length > 0) {
    markers.value.forEach(overlay => overlay.setMap(null));
    markers.value = [];
  }
  // 기존 폴리라인(선) 제거
  if (polyline.value) {
    polyline.value.setMap(null);
    polyline.value = null;
  }
};

// --- 6. 맵에 핀과 선을 그리는 핵심 함수 ---
const drawCourseOnMap = (items) => {
  if (!map.value || !items || items.length === 0) return;

  // 1. 그리기 전에 기존 요소들 모두 삭제
  clearMapElements();

  const newMarkers = [];
  const path = []; // 폴리라인 경로 배열
  const bounds = new window.kakao.maps.LatLngBounds(); // 맵 범위 재설정용

  // 2. [핀/마커] 아이템 목록을 순회하며 핀과 경로 생성
  items.forEach((item, index) => {
    // 2-1. 마커 위치
    const position = new window.kakao.maps.LatLng(item.lat, item.lng);

    // 2-2. 마커 이미지 생성
    // `getMarkerColor` 함수를 사용하여 색상을 가져옵니다.
    const markerImageSrc = createMarkerImage(
      item.number || (index + 1), // item.number가 없으면 순서(index+1) 사용
      getMarkerColor(index)
    );

    const markerImage = new window.kakao.maps.MarkerImage(
      markerImageSrc,
      new window.kakao.maps.Size(24, 35),
      { offset: new window.kakao.maps.Point(12, 35) } // 핀 모양에 맞게 이미지 중심 설정
    );

    // 2-3. 커스텀 오버레이 생성
    const marker = new window.kakao.maps.Marker({
      position: position,
      image: markerImage,
      map: map.value,
    });

    newMarkers.push(marker); // 나중에 지우기 위해 목록에 추가

    // 2-4. [요청사항 4] 폴리라인 경로 배열에 좌표 추가
    path.push(position);

    // 2-5. 맵 범위 계산을 위해 bounds에 좌표 추가
    bounds.extend(position);
  });

  // 3. [선 연결] 폴리라인 생성 및 표시
  if (path.length > 1) {
    const newPolyline = new window.kakao.maps.Polyline({
      path: path,
      strokeWeight: 4,
      strokeColor: '#4A7CEC', // 선 색상 (원하는 대로)
      strokeOpacity: 0.8,
      strokeStyle: 'solid',
    });

    newPolyline.setMap(map.value);
    polyline.value = newPolyline; // 나중에 지우기 위해 ref에 저장
  }

  // 4. (저장) 생성된 오버레이 목록을 ref에 저장
  markers.value = newMarkers;

  // 5. [맵 범위] 모든 핀이 보이도록 맵 범위 조절
  map.value.setBounds(bounds);
};

// --- 마커 색상 결정 함수 (UserLikeCourseCard와 통일) ---
const getMarkerColor = (index) => {
  // 코스 순서에 따른 색상 배열
  const colors = ['#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
  return colors[index % colors.length];
};

// --- 마커 SVG 이미지 생성 함수 (UserLikeCourseCard와 통일) ---
const createMarkerImage = (number, color) => {
  // SVG로 코스 순서 마커 이미지 생성 (물방울 모양 + 숫자)
  const svg = `
    <svg width="24" height="35" viewBox="0 0 24 35" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 0C5.373 0 0 5.373 0 12c0 9 12 23 12 23s12-14 12-23c0-6.627-5.373-12-12-12z"
          fill="${color}" stroke="#fff" stroke-width="2"/>
      <circle cx="12" cy="12" r="8" fill="#fff"/>
      <text x="12" y="16" text-anchor="middle" font-family="Arial, sans-serif"
          font-size="10" font-weight="bold" fill="${color}">${number}</text>
    </svg>
  `;
  // SVG 문자열을 Data URL 형식으로 인코딩하여 반환합니다.
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
};
</script>

<style scoped>
/* 맵 컨테이너의 크기를 지정해야 합니다. */
.course-map {
  width: 100%;
  height: 250px;
  /* 높이를 적절하게 조절하세요 */
  background-color: #eee;
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
