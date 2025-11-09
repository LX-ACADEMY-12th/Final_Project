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
  // 단일 위치 표시 모드인지 여부
  isSingleLocation: {
    type: Boolean,
    default: false // 기본값 false (기존 코드에 영향 없음)
  },
  // 단일 위치 모드일 때 사용할 고정 줌 레벨
  defaultZoomLevel: {
    type: Number,
    default: 5 // LocationSection에서 원하는 레벨
  }
});
// --- 맵과 요소들을 참조할 ref ---
const mapContainer = ref(null); // 템플릿의 div와 연결
const map = ref(null); // 카카오맵 인스턴스
const markers = ref([]); // 10.24 추가 : 마커 목록
const polyline = ref(null); // 폴리라인(선)
// :큰_초록색_원: [추가] 로드뷰 관련 ref
const roadviewContainer = ref(null); // 템플릿의 로드뷰 div와 연결
const roadview = ref(null); // 카카오 로드뷰 인스턴스
const isRoadviewActive = ref(false); // 로드뷰 활성 상태
const rvClient = ref(null); // 로드뷰 PanoID 탐색기
// --- 맵 초기화 (컴포넌트 마운트 시) ---
onMounted(() => {
  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(36.3758, 127.3845), // 기본 중심 (예: 첫 아이템)
      // 단일 모드일 때는 props의 defaultZoomLevel, 아니면 5
      level: props.isSingleLocation ? props.defaultZoomLevel : 5,
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);
    // :큰_초록색_원: [추가] 로드뷰 인스턴스 및 클라이언트 생성
    roadview.value = new window.kakao.maps.Roadview(roadviewContainer.value);
    rvClient.value = new window.kakao.maps.RoadviewClient();
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
  // :큰_초록색_원: [추가] 아이템이 바뀌면 로드뷰는 닫기
  if (isRoadviewActive.value) {
    toggleRoadview(false); // 지도로 복귀
  }
});
// :큰_초록색_원: [신규] 로드뷰 <-> 지도 토글 함수
const toggleRoadview = (showRoadview) => {
  isRoadviewActive.value = showRoadview;
  if (showRoadview) {
    // --- 로드뷰 켜기 ---
    if (!props.items || props.items.length === 0) return;
    const item = props.items[0]; // 단일 모드이므로 첫 번째 아이템 사용
    const position = new window.kakao.maps.LatLng(Number(item.lat), Number(item.lng));
    // 1. 로드뷰 컨테이너가 보이게 된 후 relayout (중요)
    // v-show가 DOM을 'display: block'으로 바꿔줄 시간을 줌
    setTimeout(() => {
      roadview.value.relayout();
      // 2. (핵심) 좌표로 PanoID 찾기 (비동기)
      rvClient.value.getNearestPanoId(position, 200, (panoId) => {
        if (panoId) {
          // 3. PanoID로 로드뷰 위치 설정
          roadview.value.setPanoId(panoId, position);
          // 4. 로드뷰에 마커 추가
          new window.kakao.maps.Marker({
            position: position,
            map: roadview.value
          });
        } else {
          // 로드뷰 정보가 없는 경우
          alert('이 위치에는 로드뷰 정보가 없습니다.');
          isRoadviewActive.value = false; // 다시 지도로 강제 복귀
        }
      });
    }, 0); // DOM 업데이트가 반영된 직후 실행
  } else {
    // --- 지도 켜기 ---
    if (map.value) {
      setTimeout(() => {
        map.value.relayout();
        // 맵의 중심을 다시 설정
        // :총격전: [수정] bounds가 null이 아닐 때만 isEmpty()를 체크해야 합니다.
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
// :총격전: [수정] bounds 변수를 함수 밖(전역)으로 이동
let bounds = null;
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
  // :총격전: [수정] 'const'를 지우고 전역 'let bounds' 변수에 할당
  bounds = new window.kakao.maps.LatLngBounds();
  items.forEach((item, index) => {
    console.log(`[CourseMap] 아이템 ${index} 처리 중:`, item);
    // --- 좌표 유효성 검사 ---
    if (item.lat == null || item.lng == null || isNaN(Number(item.lat)) || isNaN(Number(item.lng))) {
      console.error(`[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 아이템 ${index}의 좌표가 유효하지 않거나 없습니다. 마커 생성을 건너<0xEB><0x9A><0x8E>니다.`, item);
      return; // 이 아이템 건너뛰기
    }
    const position = new window.kakao.maps.LatLng(Number(item.lat), Number(item.lng));
    console.log(`[CourseMap] 아이템 ${index} 위치(Position) 생성됨:`, position);
    // --- 마커 이미지 소스 생성 및 유효성 검사 ---
    const itemNumber = props.isSingleLocation ? '' : (item.number || (index + 1));
    // isSingleLocation이 true면 빨간색, 아니면 기존 로직
    const markerColor = props.isSingleLocation ? '#FF5A5A' : getCourseItemColor(itemNumber);
    const markerImageSrc = createMarkerImage(itemNumber, markerColor);
    console.log(`[CourseMap] 아이템 ${index} - 번호: ${itemNumber}, 색상: ${markerColor}, 이미지 소스(앞부분): ${markerImageSrc?.substring(0, 50)}...`);
    if (!markerImageSrc || typeof markerImageSrc !== 'string' || !markerImageSrc.startsWith('data:image/svg+xml')) {
      console.error(`[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 아이템 ${index}에 대한 마커 이미지 소스가 유효하지 않습니다. 마커 생성을 건너<0xEB><0x9A><0x8E>니다.`, markerImageSrc);
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
      console.error(`[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 아이템 ${index} MarkerImage 생성 중 오류 발생:`, imgError, markerImageSrc);
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
      bounds.extend(position); // :왼쪽을_가리키는_손_모양: 여기가 전역 bounds를 채움
    } catch (markerError) {
      // insertBefore 에러는 주로 여기서 발생 (image 값이 잘못되었을 때)
      console.error(`[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 아이템 ${index} 마커 생성 중 오류 발생:`, markerError, { position, markerImage });
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
      console.error('[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 폴리라인 생성 중 오류 발생:', polyError, path);
    }
  } else {
    console.log('[CourseMap] 폴리라인을 그리기에 점이 부족합니다.');
  }
  // --- 마커 저장 및 지도 범위 설정 ---
  markers.value = newMarkers;
  if (!bounds.isEmpty()) {
    // 1. 단일 위치 모드인 경우 (숫자 없고, 줌 고정)
    if (props.isSingleLocation) {
      const centerPosition = new window.kakao.maps.LatLng(Number(items[0].lat), Number(items[0].lng));
      map.value.setCenter(centerPosition); // :왼쪽을_가리키는_손_모양: 수정된 부분
      map.value.setLevel(props.defaultZoomLevel);
      console.log(`[CourseMap] 단일 위치 모드: 줌 레벨 ${props.defaultZoomLevel}로 고정.`);
      // 2. 기존 코스 모드인 경우 (여러 핀, setBounds 사용)
    } else {
      try {
        map.value.setBounds(bounds);
        console.log('[CourseMap] 지도 범위 설정 완료.');
      } catch (boundsError) {
        console.error('[CourseMap] :느낌표:️:느낌표:️:느낌표:️ 지도 범위 설정 중 오류 발생:', boundsError, bounds);
      }
    }
  } else {
    console.warn('[CourseMap] 유효한 범위가 없어 지도 범위를 설정할 수 없습니다.');
  }
}; // --- drawCourseOnMap 함수 끝 ---
// --- 마커 색상 결정 함수
const getCourseItemColor = (itemNumber) => {
  // CourseMap.vue의 getMarkerColor 함수와 동일한 로직 사용
  // item.number는 1번부터 시작하므로, index로 변환하려면 -1을 해야 합니다.
  const colors = ['#FF5A5A', '#4A7CEC', '#28A745', '#FFC107', '#DC3545', '#6F42C1', '#E83E8C'];
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
/* 맵/로드뷰를 감싸는 부모 */
.map-rv-container {
  position: relative;
  /* 버튼을 띄우기 위한 기준점 */
  width: 100%;
  height: 250px;
  background-color: #eee;
}

/* 맵 컨테이너의 크기를 지정해야 합니다. */
.course-map {
  width: 100%;
  height: 250px;
  /* 높이를 적절하게 조절하세요 */
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
