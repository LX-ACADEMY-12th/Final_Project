<template>
  <div id="map" class="map-container"></div>
</template>

<script>
// [수정됨] import를 <script> 태그 안으로 이동
import { onMounted, watch } from 'vue';

// [수정됨] export default를 명시적으로 사용
export default {
  name: 'CourseView',
  props: {
    items: {
      type: Array,
      required: true,
      default: () => []
    }
  },
  // [수정됨] setup() 함수를 명시적으로 정의
  setup(props) {
    let map = null; // 지도 인스턴스를 저장할 변수

    // setup() 스코프 내에서 함수들을 정의
    function initMap() {
      const container = document.getElementById('map');
      const options = {
        center: new window.kakao.maps.LatLng(36.375788, 127.376580),
        level: 5
      };
      map = new window.kakao.maps.Map(container, options);

      if (props.items.length > 0) {
        drawMapElements(props.items);
      }
    }

    function drawMapElements(items) {
      if (!window.kakao || !window.kakao.maps) return;
      const { kakao } = window;
      
      const path = [];
      const bounds = new kakao.maps.LatLngBounds();

      items.forEach(item => {
        const latlng = new kakao.maps.LatLng(item.latlng[0], item.latlng[1]);
        path.push(latlng);

        const marker = createCustomMarker(latlng, item.number, item.color);
        marker.setMap(map);

        bounds.extend(latlng);
      });

      new kakao.maps.Polyline({
        map: map,
        path: path,
        strokeWeight: 3,
        strokeColor: '#000000',
        strokeOpacity: 0.7,
        strokeStyle: 'solid'
      });

      map.setBounds(bounds);
    }

    function createCustomMarker(position, number, color) {
      const { kakao } = window;
      const content = `<div style="
        display: flex; 
        align-items: center; 
        justify-content: center;
        width: 28px; 
        height: 28px; 
        border-radius: 50%; 
        background-color: ${color}; 
        border: 2px solid white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        color: white; 
        font-weight: bold; 
        font-size: 14px;
        ">
          ${number}
        </div>`;

      return new kakao.maps.CustomOverlay({
          position: position,
          content: content,
          yAnchor: 1
      });
    }

    // 라이프사이클 훅과 watch는 setup 안에서 호출
    onMounted(() => {
      if (window.kakao && window.kakao.maps) {
        initMap();
      } else {
        console.error("카카오 지도 스크립트가 로드되지 않았습니다.");
      }
    });

    watch(() => props.items, (newItems) => {
      if (map && newItems.length > 0) {
        drawMapElements(newItems);
      }
    }, { deep: true });

    // setup() 함수는 템플릿에서 사용할 변수나 함수를 반환해야 하지만,
    // 이 컴포넌트는 <template> 영역에서 사용하는 변수가 없으므로
    // 아무것도 반환하지 않아도 됩니다.
    return {
      // (템플릿에서 사용할 변수나 함수가 있다면 여기에 추가)
    };
  }
}
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 200px;
}
</style>