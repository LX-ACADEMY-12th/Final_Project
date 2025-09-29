<!-- KoreaMap.vue -->
<template>
  <div class="phone d-flex flex-column justify-content-start align-items-center">
    <p class="fs-7 pt-2 mb-2 px-3 mt-0 text-center w-100">전국 157개 중 000개 맞춤</p>

    <!-- ✅ 가운데 정렬된 버튼 2개 -->
    <div class="w-100 d-flex justify-content-center align-items-center gap-2 mb-2">
      <button class="btn btn-outline-dark btn-sm px-3">친구와 함께</button>
      <button class="btn btn-dark btn-sm px-3">랜덤</button>
    </div>

    <div id="map" class="map-box mb-2"></div>
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

  const res = await fetch('/korea.geojson')
  const geo = await res.json()

  const layer = L.geoJSON(geo, {
    style: {
      color: '#222',
      weight: 1,
      fillColor: '#fff',
      fillOpacity: 1
    }
  }).addTo(map)

  await nextTick()
  map.invalidateSize(true)
  map.fitBounds(layer.getBounds(), { padding: [20, 20] })
})
</script>

<style>
/* 폭을 통일(중앙정렬이 어긋나지 않게) */
.phone {
  width: 320px;              /* 필요시 290px로 줄여도 됨 */
  height: 616px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid darkblue;
  border-radius: 12px;
}

/* 지도 상자: 부모 폭에 맞춰 100% */
.map-box{
  width: 100%;
  height: 520px;             /* 필요시 조절 */
  border:1px solid #000;
  border-radius:12px;
}

/* 밑바탕(타일) 없이 흰 배경 */
#map.leaflet-container { background:#fff; }

/* 리셋 CSS 충돌 방지 */
.leaflet-container img { max-width: none !important; }
</style>
