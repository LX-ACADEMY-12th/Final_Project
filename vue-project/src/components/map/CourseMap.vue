<template>
  <div ref="mapContainer" class="course-map"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

// --- Props 정의 ---
const props = defineProps({
  // 부모부터 코스 아이템 배열을 받음
  items: {
    type: Array,
    required: true,
  },
  // 부모로부터 코스 제목을 받음 (쓸지 안 쓸지 모르겠음)
  title: {
    type: String,
    default: '코스 지도',
  },
});

// --- 맵과 요소들을 참조할 ref ---
const mapContainer = ref(null); // 템플릿의 div와 연결
const map = ref(null); // 카카오맵 인스턴스
const markers = ref([]); // 10.24 추가 : 마커 목록
const polyline = ref(null); // 폴리라인(선)

// --- 맵 초기화 (컴포넌트 마운트 시) ---
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

// --- props.items가 변경될 때마다 맵 다시 그리기 ---
watch(() => props.items, (newItems) => {
  if (!map.value) return; // 맵이 아직 준비되지 않았다면 중단
  drawCourseOnMap(newItems);
});

// --- 기존 맵 요소들(핀, 선) 제거 함수 ---
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

// --- 맵에 핀과 선을 그리는 핵심 함수 ---
const drawCourseOnMap = (items) => {
  // --- 함수 시작 ---
  console.log('[CourseMap] drawCourseOnMap 호출됨 / items:', JSON.stringify(items || [], null, 2));

  // --- 맵 준비 상태 확인 ---
  if (!map.value) {
    console.warn('[CourseMap] 지도 인스턴스(map.value)가 아직 준비되지 않았습니다.');
    return;
  }
  if (!mapContainer.value) {
    console.warn('[CourseMap] 지도 컨테이너(mapContainer.value)가 아직 준비되지 않았습니다.');
    return;
  }
  if (!items || items.length === 0) {
    console.warn('[CourseMap] 지도에 표시할 아이템이 없습니다.');
    clearMapElements(); // 아이템이 없다면 기존 요소 지우기
    return;
  }

  console.log('[CourseMap] 기존 지도 요소 지우는 중...');
  clearMapElements();

  const newMarkers = [];
  const path = [];
  const bounds = new window.kakao.maps.LatLngBounds();

  items.forEach((item, index) => {
    console.log(`[CourseMap] 아이템 ${index} 처리 중:`, item);

    // --- 좌표 유효성 검사 ---
    if (item.lat == null || item.lng == null || isNaN(Number(item.lat)) || isNaN(Number(item.lng))) {
      console.error(`[CourseMap] ❗️❗️❗️ 아이템 ${index}의 좌표가 유효하지 않거나 없습니다. 마커 생성을 건너<0xEB><0x9A><0x8E>니다.`, item);
      return; // 이 아이템 건너뛰기
    }
    const position = new window.kakao.maps.LatLng(Number(item.lat), Number(item.lng));
    console.log(`[CourseMap] 아이템 ${index} 위치(Position) 생성됨:`, position);

    // --- 마커 이미지 소스 생성 및 유효성 검사 ---
    const itemNumber = item.number || (index + 1); // number 없으면 index+1 사용
    const markerColor = getCourseItemColor(itemNumber);
    const markerImageSrc = createMarkerImage(itemNumber, markerColor);

    console.log(`[CourseMap] 아이템 ${index} - 번호: ${itemNumber}, 색상: ${markerColor}, 이미지 소스(앞부분): ${markerImageSrc?.substring(0, 50)}...`);

    if (!markerImageSrc || typeof markerImageSrc !== 'string' || !markerImageSrc.startsWith('data:image/svg+xml')) {
      console.error(`[CourseMap] ❗️❗️❗️ 아이템 ${index}에 대한 마커 이미지 소스가 유효하지 않습니다. 마커 생성을 건너<0xEB><0x9A><0x8E>니다.`, markerImageSrc);
      return; // 이 아이템 건너뛰기
    }

    let markerImage;
    try {
      markerImage = new window.kakao.maps.MarkerImage(
        markerImageSrc,
        new window.kakao.maps.Size(24, 35),
        { offset: new window.kakao.maps.Point(12, 35) }
      );
      console.log(`[CourseMap] 아이템 ${index} MarkerImage 생성됨.`);
    } catch (imgError) {
      console.error(`[CourseMap] ❗️❗️❗️ 아이템 ${index} MarkerImage 생성 중 오류 발생:`, imgError, markerImageSrc);
      return; // MarkerImage 생성 실패 시 건너뛰기
    }

    // --- 마커 생성 ---
    try {
      const marker = new window.kakao.maps.Marker({
        position: position,
        image: markerImage, // 생성된 markerImage 객체 사용
        map: map.value,
      });
      console.log(`[CourseMap] 아이템 ${index} 마커 생성 및 지도에 추가 완료.`);
      newMarkers.push(marker);
      path.push(position);
      bounds.extend(position);
    } catch (markerError) {
      // insertBefore 에러는 주로 여기서 발생 (image 값이 잘못되었을 때)
      console.error(`[CourseMap] ❗️❗️❗️ 아이템 ${index} 마커 생성 중 오류 발생:`, markerError, { position, markerImage });
      // 필요시 에러를 다시 던지거나 다르게 처리
    }
  }); // --- forEach 끝 ---

  // --- 끝 ---
  console.log('[CourseMap] 아이템 처리 완료. 경로 개수:', path.length);

  // --- 폴리라인(선) 생성 ---
  if (path.length > 1) {
    try {
      const newPolyline = new window.kakao.maps.Polyline({
        path: path,
        strokeWeight: 4,
        strokeColor: '#4A7CEC',
        strokeOpacity: 0.8,
        strokeStyle: 'solid',
      });
      newPolyline.setMap(map.value);
      polyline.value = newPolyline;
      console.log('[CourseMap] 폴리라인 생성 및 추가 완료.');
    } catch (polyError) {
      console.error('[CourseMap] ❗️❗️❗️ 폴리라인 생성 중 오류 발생:', polyError, path);
    }
  } else {
    console.log('[CourseMap] 폴리라인을 그리기에 점이 부족합니다.');
  }

  // --- 마커 저장 및 지도 범위 설정 ---
  markers.value = newMarkers;
  if (!bounds.isEmpty()) {
    try {
      map.value.setBounds(bounds);
      console.log('[CourseMap] 지도 범위 설정 완료.');
    } catch (boundsError) {
      console.error('[CourseMap] ❗️❗️❗️ 지도 범위 설정 중 오류 발생:', boundsError, bounds);
    }
  } else {
    console.warn('[CourseMap] 유효한 범위가 없어 지도 범위를 설정할 수 없습니다.');
  }
}; // --- drawCourseOnMap 함수 끝 ---

// --- 마커 색상 결정 함수
const getCourseItemColor = (itemNumber) => {
  // CourseMap.vue의 getMarkerColor 함수와 동일한 로직 사용
  // item.number는 1번부터 시작하므로, index로 변환하려면 -1을 해야 합니다.
  const colors = ['#FF5A5A', '#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
  // itemNumber가 유효한 숫자인지 확인, 아니면 기본 색상 반환
  if (typeof itemNumber !== 'number' || isNaN(itemNumber) || itemNumber < 1) {
    console.warn(`[getCourseItemColor] Invalid itemNumber: ${itemNumber}. Using default color.`);
    return colors[1]; // 기본 파란색 반환
  }
  // item.number는 1부터 시작하므로 배열 인덱스에 맞추기 위해 -1
  return colors[(itemNumber - 1) % colors.length];
}

// --- 마커 SVG 이미지 생성 함수
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
