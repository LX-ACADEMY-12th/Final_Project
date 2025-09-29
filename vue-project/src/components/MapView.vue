<template>
  <div class="view-wrapper">
    <GNB />

    <main class="map-view">
      <div id="map"></div>

      <MapControls @zoom-in="zoomIn" @zoom-out="zoomOut" @recenter="recenterMap" />
    </main>

    <CreatePostFAB />

    <BottomNav />
  </div>
</template>

<script setup>
// Vue 3 Composition API에서 반응형 데이터와 라이프사이클 훅을 사용하기 위해 필요한 함수들을 import합니다.
import { ref, onMounted } from 'vue';
// 화면을 구성하는 각 UI 컴포넌트들을 import합니다.
import GNB from './GNB.vue';
import MapControls from './MapControls.vue';
import CreatePostFAB from './CreatePostFAB.vue';
import BottomNav from './BottomNav.vue';

// 이 컴포넌트가 부모 컴포넌트로 이벤트를 발생시킬 수 있도록 emit 함수를 정의합니다.
// 'marker-click' 이라는 이름의 이벤트를 부모로 보낼 수 있습니다.
const emit = defineEmits(['marker-click']);

//================================================================
// 1. 상태 관리 (State Management) - 컴포넌트의 데이터들을 반응형으로 관리합니다.
//================================================================

// Google 지도 객체 인스턴스를 저장할 ref입니다. 지도 API가 로드된 후 여기에 할당됩니다. (초기값: null)
const mapInstance = ref(null);
// 현재 지도 화면에 표시되고 있는 마커들을 효율적으로 관리하기 위한 Map 객체입니다.
// { key: markerId, value: { marker: 구글마커객체, data: 원본데이터 } } 형태로 저장됩니다.
const currentMarkersOnMap = ref(new Map());
// 사용자의 현재 위치를 나타내는 파란색 점 마커 객체를 저장할 ref입니다.
const userLocationMarker = ref(null);
// 서버로부터 받아온 모든 게시물(마커)의 원본 데이터를 저장하는 배열입니다.
// 실제 앱에서는 이 데이터는 API 호출을 통해 비동기적으로 받아옵니다.
const allMarkersData = ref([
  { id: 1, locationName: '서울 시청', position: { lat: 37.5665, lng: 126.9780 }, thumbnailUrl: 'https://placehold.co/150x150/7B66FF/FFFFFF?text=Post1', type: 'image' },
  { id: 2, locationName: '남산타워', position: { lat: 37.5512, lng: 126.9882 }, text: '오늘 남산에서 본 야경이 정말 아름다웠어요!', type: 'text' },
  { id: 3, locationName: '경복궁', position: { lat: 37.5797, lng: 126.9770 }, thumbnailUrl: 'https://placehold.co/150x150/66FF99/FFFFFF?text=Post3', type: 'image' },
  { id: 4, locationName: '코엑스', position: { lat: 37.5127, lng: 127.0589 }, text: '코엑스 별마당 도서관 최고', type: 'text' },
  { id: 5, locationName: '롯데월드', position: { lat: 37.5111, lng: 127.0982 }, thumbnailUrl: 'https://placehold.co/150x150/DAF7A6/FFFFFF?text=Post5', type: 'image' },
  { id: 6, locationName: '홍대입구역', position: { lat: 37.5569, lng: 126.9239 }, thumbnailUrl: 'https://placehold.co/150x150/900C3F/FFFFFF?text=Post6', type: 'image' },
]);
// (추가) 컴포넌트가 잠시 가려졌다가 다시 표시될 때(예: 탭 전환) 지도 상태를 복원하기 위해,
// 지도의 마지막 중심 좌표와 줌 레벨을 저장하는 ref입니다.
const lastCenter = ref(null);
const lastZoom = ref(null);

//================================================================
// 2. 지도 관련 로직 (Map Logic)
//================================================================

/**
 * 현재 지도 경계(bounds) 안에 들어오는 마커들을 지도에 추가하거나 업데이트합니다.
 * @param {google.maps.Map} map - Google 지도 인스턴스
 * @param {google.maps.LatLngBounds} bounds - 현재 보이는 지도의 경계
 * @returns {Set<number>} 현재 화면에 보여야 할 마커들의 ID Set
 */
const addOrUpdateMarkersInBounds = (map, bounds) => {
  // 현재 화면에 보여야 할 마커들의 ID를 저장하기 위한 Set을 생성합니다.
  const visibleMarkerIds = new Set();

  // 모든 마커 데이터(allMarkersData)를 순회합니다.
  allMarkersData.value.forEach(markerData => {
    // 마커의 위치가 현재 지도 경계(bounds) 안에 있는지 확인합니다.
    if (bounds.contains(markerData.position)) {
      // 경계 안에 있다면, 해당 마커 ID를 visibleMarkerIds Set에 추가합니다.
      visibleMarkerIds.add(markerData.id);

      // 만약 이 마커가 아직 지도에 그려지지 않았다면 (currentMarkersOnMap에 없다면),
      if (!currentMarkersOnMap.value.has(markerData.id)) {
        // Google 지도 마커를 생성하기 위한 옵션 객체를 설정합니다.
        let markerOptions = {
          map: map, // 마커를 표시할 지도 인스턴스
          position: markerData.position, // 마커의 위경도 좌표
          title: markerData.locationName, // 마커에 마우스를 올렸을 때 표시될 툴팁
        };

        // 마커 데이터의 타입이 'image'이고 썸네일 URL이 있다면, 커스텀 아이콘을 설정합니다.
        if (markerData.type === 'image' && markerData.thumbnailUrl) {
          markerOptions.icon = {
            url: markerData.thumbnailUrl, // 아이콘으로 사용할 이미지 주소
            scaledSize: new google.maps.Size(60, 60), // 아이콘 크기 조절
            anchor: new google.maps.Point(30, 60) // 아이콘의 기준점 (이미지 하단 중앙)
          };
        }

        // 설정된 옵션으로 새로운 Google 지도 마커 객체를 생성합니다.
        const marker = new google.maps.Marker(markerOptions);

        // (수정) 생성된 마커에 'click' 이벤트 리스너를 추가합니다.
        // 마커를 클릭하면 부모 컴포넌트로 'marker-click' 이벤트를 발생(emit)시킵니다.
        marker.addListener('click', () => emit('marker-click', { markerId: markerData.id, locationName: markerData.locationName }));

        // 새로 생성된 마커 객체와 원본 데이터를 currentMarkersOnMap에 저장하여 관리합니다.
        currentMarkersOnMap.value.set(markerData.id, { marker, data: markerData });
      }
    }
  });
  // 화면에 보여야 할 마커들의 ID 목록을 반환합니다.
  return visibleMarkerIds;
}

/**
 * 지도 경계(bounds)를 벗어난 마커들을 지도에서 제거합니다.
 * @param {Set<number>} visibleMarkerIds - 현재 화면에 보여야 할 마커들의 ID Set
 */
const removeOutOfBoundsMarkers = (visibleMarkerIds) => {
  // 현재 지도에 그려져 있는 모든 마커들(currentMarkersOnMap)을 순회합니다.
  currentMarkersOnMap.value.forEach((markerObject, id) => {
    // 만약 현재 지도에 있는 마커가 '보여야 할 마커 목록(visibleMarkerIds)'에 없다면,
    if (!visibleMarkerIds.has(id)) {
      // 해당 마커를 지도에서 보이지 않게 처리합니다. (실제 객체를 삭제하는 것보다 효율적)
      markerObject.marker.setMap(null);
      // 관리 목록(currentMarkersOnMap)에서도 해당 마커 정보를 삭제합니다.
      currentMarkersOnMap.value.delete(id);
    }
  });
}

/**
 * 지도 이동/줌 변경이 멈췄을 때 호출되어, 현재 보이는 영역의 마커들을 업데이트하는 메인 함수입니다.
 */
function updateVisibleMarkers() {
  const map = mapInstance.value;
  if (!map) return; // 지도 인스턴스가 없으면 함수를 종료합니다.

  const bounds = map.getBounds();
  if (!bounds) return; // 지도의 경계 정보를 가져올 수 없으면 종료합니다.

  // 1. 현재 경계 안에 있는 마커들을 지도에 그리거나, 이미 있다면 그대로 둡니다.
  const visibleMarkerIds = addOrUpdateMarkersInBounds(map, bounds);
  // 2. 현재 경계를 벗어난 마커들을 지도에서 제거합니다.
  removeOutOfBoundsMarkers(visibleMarkerIds);

  // (추가) 지도 상태가 변경될 때마다 마지막 중심 위치와 줌 레벨을 저장합니다.
  // 이는 나중에 refreshMapLayout 함수에서 지도 상태를 복원할 때 사용됩니다.
  lastCenter.value = map.getCenter();
  lastZoom.value = map.getZoom();
}

/**
 * 구글 지도를 주어진 좌표로 초기화하고 이벤트 리스너를 설정합니다.
 * @param {google.maps.LatLngLiteral} initialCenter - 지도의 초기 중심 좌표 {lat, lng}
 */
async function initMap(initialCenter) {
  // window.google 객체가 없는 경우 (API 스크립트 로드 실패) 에러를 출력하고 종료합니다.
  if (typeof google === 'undefined') {
    console.error("Google Maps 스크립트가 로드되지 않았습니다.");
    return;
  }
  // Google Maps JavaScript API의 'maps' 라이브러리를 비동기적으로 가져옵니다. (최신 방식)
  const { Map } = await google.maps.importLibrary("maps");

  // 'map'이라는 id를 가진 div에 새로운 지도 객체를 생성합니다.
  const map = new Map(document.getElementById("map"), {
    zoom: 14, // 초기 줌 레벨
    center: initialCenter, // 전달받은 좌표로 중심 설정
    disableDefaultUI: true, // 기본 지도 UI(줌 컨트롤, 스트리트뷰 등)를 비활성화합니다.
  });

  // 생성된 지도 인스턴스를 ref 변수(mapInstance)에 저장하여 컴포넌트 전체에서 접근할 수 있게 합니다.
  mapInstance.value = map;

  // 지도 움직임이 멈췄을 때('idle' 이벤트) updateVisibleMarkers 함수를 호출하도록 리스너를 추가합니다.
  // 'bounds_changed' 이벤트보다 효율적입니다. (드래그 중 계속 호출되지 않음)
  map.addListener('idle', updateVisibleMarkers);

  // 지도의 기본 타일이 모두 로드된 후('tilesloaded' 이벤트), 딱 한 번만 마커를 표시하는 로직을 실행합니다.
  // 이를 통해 사용자는 빈 지도 대신 타일이 그려진 지도 위에서 마커를 처음 보게 되어 UX가 향상됩니다.
  google.maps.event.addListenerOnce(map, 'tilesloaded', () => {
    updateVisibleMarkers();
  });
}

/**
 * 컴포넌트가 마운트될 때 사용자의 현재 위치를 기반으로 지도를 초기화합니다.
 */
const loadMapWithInitialLocation = () => {
  // 브라우저가 Geolocation API를 지원하지 않는 경우,
  if (!navigator.geolocation) {
    console.warn("위치 정보 서비스가 지원되지 않아 기본 위치로 시작합니다.");
    const defaultLocation = { lat: 37.5665, lng: 126.9780 }; // 기본 위치: 서울 시청
    initMap(defaultLocation); // 기본 위치로 지도 초기화
    return;
  }

  // Geolocation API를 사용하여 현재 위치를 가져옵니다.
  navigator.geolocation.getCurrentPosition(
    // 성공 콜백: 위치 정보를 성공적으로 가져왔을 때 실행됩니다.
    (position) => {
      // position 객체에서 위도(latitude)와 경도(longitude)를 추출합니다.
      const userLocation = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
      // 사용자 위치로 지도를 초기화합니다.
      initMap(userLocation).then(() => {
        // 지도 초기화가 완료된 후, 현재 위치에 파란색 점 마커를 표시합니다.
        // false는 오류 발생 시 alert를 띄우지 않겠다는 의미입니다.
        panToCurrentUserLocation(false, userLocation);
      });
    },
    // 실패 콜백: 위치 정보 가져오기에 실패했을 때 실행됩니다.
    () => {
      console.warn("위치 정보 획득 실패. 기본 위치로 시작합니다.");
      const defaultLocation = { lat: 37.5665, lng: 126.9780 }; // 기본 위치: 서울 시청
      initMap(defaultLocation); // 기본 위치로 지도 초기화
    }
  );
};

// Vue의 onMounted 라이프사이클 훅: 컴포넌트가 DOM에 마운트된 직후에 실행됩니다.
onMounted(() => {
  // 지도 로딩 및 초기화 프로세스를 시작합니다.
  loadMapWithInitialLocation();
});

//================================================================
// 3. UI 이벤트 핸들러 (UI Event Handlers) - 사용자의 상호작용에 반응하는 함수들
//================================================================

// 지도를 1단계 확대합니다. optional chaining(?.)을 사용하여 mapInstance가 null일 때 에러를 방지합니다.
const zoomIn = () => mapInstance.value?.setZoom(mapInstance.value.getZoom() + 1);
// 지도를 1단계 축소합니다.
const zoomOut = () => mapInstance.value?.setZoom(mapInstance.value.getZoom() - 1);

/**
 * 사용자의 현재 위치를 찾아 지도를 그곳으로 이동시키고 마커를 표시하는 함수입니다.
 * @param {boolean} showAlertOnError - 오류 발생 시 사용자에게 alert 창을 표시할지 여부
 * @param {google.maps.LatLngLiteral | null} location - 미리 알고 있는 위치 정보 (선택 사항)
 */
const panToCurrentUserLocation = (showAlertOnError = false, location = null) => {
  // 인자로 위치 정보(location)가 직접 주어진 경우, API 요청 없이 바로 그 위치를 사용합니다.
  if (location) {
    mapInstance.value?.setCenter(location); // 지도의 중심을 해당 위치로 이동
    mapInstance.value?.setZoom(15); // 적절한 줌 레벨로 설정

    // 기존에 있던 현재 위치 마커가 있다면 지도에서 제거합니다. (중복 방지)
    if (userLocationMarker.value) userLocationMarker.value.setMap(null);

    // 새로운 현재 위치 마커(파란색 점)를 생성합니다.
    userLocationMarker.value = new google.maps.Marker({
      map: mapInstance.value,
      position: location,
      title: "현재 위치",
      icon: { // 커스텀 아이콘 설정
        path: google.maps.SymbolPath.CIRCLE, // 모양: 원
        scale: 8, // 크기
        fillColor: '#4285F4', // 채우기 색상 (구글 블루)
        fillOpacity: 1, // 채우기 불투명도
        strokeColor: 'white', // 테두리 색상
        strokeWeight: 2, // 테두리 두께
      }
    });
    return; // 함수 종료
  }

  // 인자로 위치 정보가 주어지지 않았다면, Geolocation API로 직접 위치를 요청합니다.
  if (!navigator.geolocation) {
    if (showAlertOnError) alert("이 브라우저에서는 위치 정보 서비스를 지원하지 않습니다.");
    return;
  }

  // 현재 위치를 요청합니다.
  navigator.geolocation.getCurrentPosition(
    // 성공 시: 받아온 위치 정보로 이 함수(panToCurrentUserLocation)를 다시 호출합니다.
    (position) => {
      const userLocation = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
      // 재귀적으로 호출하여 위치를 지도에 표시하는 로직을 실행합니다.
      panToCurrentUserLocation(showAlertOnError, userLocation);
    },
    // 실패 시: 에러 처리
    () => {
      if (showAlertOnError) {
        alert("위치 정보를 가져올 수 없습니다. 브라우저의 위치 정보 접근 권한을 확인해주세요.");
      } else {
        // 백그라운드에서 자동 위치 갱신 실패 시에는 콘솔에만 경고를 남깁니다.
        console.warn("자동으로 현재 위치를 가져오는 데 실패했습니다. 위치 권한을 확인해주세요.");
      }
    }
  );
};

// '현재 위치로' 버튼을 클릭했을 때 호출되는 함수입니다.
// panToCurrentUserLocation을 호출하되, 에러 발생 시 사용자에게 alert를 보여주도록 true를 전달합니다.
const recenterMap = () => panToCurrentUserLocation(true);

/**
 * (추가) 부모 컴포넌트에서 호출할 함수. 숨겨졌던 지도를 다시 표시할 때 발생할 수 있는 렌더링 문제를 해결합니다.
 * 예를 들어, 다른 탭에 갔다가 지도 탭으로 돌아왔을 때 지도가 회색으로 보이는 현상을 방지합니다.
 */
const refreshMapLayout = () => {
  // google API와 mapInstance가 모두 유효할 때만 실행합니다.
  if (google && mapInstance.value) {
    // Google 지도 API에 'resize' 이벤트를 강제로 발생시킵니다.
    // 이 이벤트는 지도가 자신의 컨테이너 크기를 다시 계산하고 화면을 새로 그리도록 명령합니다.
    google.maps.event.trigger(mapInstance.value, "resize");

    // 이전에 저장해둔 마지막 중심 위치와 줌 레벨로 지도 상태를 즉시 복원합니다.
    // 'resize' 이벤트만으로는 지도의 중심이 초기화될 수 있기 때문에 이 과정이 필요합니다.
    if (lastCenter.value) {
      mapInstance.value.setCenter(lastCenter.value);
    }
    if (lastZoom.value) {
      mapInstance.value.setZoom(lastZoom.value);
    }
  }
};

// (추가) defineExpose: <script setup>을 사용하는 컴포넌트는 기본적으로 닫혀있습니다.
// 이 구문을 통해 부모 컴포넌트가 ref를 통해 이 컴포넌트의 메서드(refreshMapLayout)를 호출할 수 있도록 외부에 노출시킵니다.
defineExpose({
  refreshMapLayout
});
</script>

<style scoped>
/* 'scoped' 속성은 이 스타일이 현재 컴포넌트(.vue 파일) 내의 요소에만 적용되도록 범위를 제한합니다. */
.view-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  /* 자식 요소들의 position 기준이 됩니다. */
  display: flex;
  /* Flexbox 레이아웃을 사용합니다. */
  flex-direction: column;
  /* 자식 요소들을 세로(위에서 아래로)로 배치합니다. */
}

.map-view {
  flex-grow: 1;
  /* GNB, BottomNav를 제외한 나머지 모든 수직 공간을 차지하도록 합니다. */
  position: relative;
  /* MapControls 같은 자식 요소를 position: absolute로 배치하기 위한 기준점입니다. */
}

#map {
  width: 100%;
  height: 100%;
  /* 부모인 .map-view의 전체 공간을 채웁니다. */
}
</style>
