<template>

  <div class="korea-map-wrapper">
    <div class="map-header">
      <p>전국 157개 중 000개 맞춤</p>
    </div>

    <div id="map" class="map-container"></div>
  </div>
</template>

<script setup>
import { onMounted, nextTick } from 'vue'
import L from 'leaflet'

onMounted(async () => {
  const map = L.map('map', {
    attributionControl: false,
    preferCanvas: true,
    zoomAnimation: true
  })

  // GeoJSON 로드 (public/korea.geojson)
  const res = await fetch('/korea.geojson')
  const geo = await res.json()

  // 시·군·구 폴리곤만 표시 (라벨 없음)
  const layer = L.geoJSON(geo, {
    style: {
      color: '#222',       // 경계선 색
      weight: 1,           // 경계선 두께
      fillColor: '#fff',   // 내부 채움
      fillOpacity: 1
    }
  }).addTo(map)

  await nextTick()
  map.invalidateSize(true)
  map.fitBounds(layer.getBounds(), { padding: [20, 20] })
})
</script>

<style>
/* 래퍼에도 높이 100%를 적용하여 자식 요소로 높이값을 전달 */
.korea-map-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

/* 헤더 영역 스타일링 */
.map-header {
  padding: 16px;
  text-align: center;
  border-bottom: 1px solid #e0e-e0;
  background-color: #fff;
  flex-shrink: 0;
  /* 공간이 부족해도 헤더의 높이는 줄어들지 않도록 설정 */
}

.map-header p {
  margin: 0;
  font-weight: 500;
}

/* 지도 컨테이너 스타일링 */
.map-container {
  flex-grow: 1;
  width: 100%;
  min-height: 0;
}

/* 밑바탕(타일) 없이 흰 배경 */
#map.leaflet-container {
  background: #fff;
}

/* 리셋 CSS 충돌 방지(필요 시) */
.leaflet-container img {
  max-width: none !important;
}
</style>
