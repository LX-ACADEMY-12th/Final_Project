<!-- src/views/MyPageRegionView.vue -->
<template>
  <div class="korea-map-wrapper">
    <div class="map-header">
      <button class="btn btn-dark btn-sm" @click="goToFriendList()">친구와 함께</button>
      <button class="btn-white options-button">랜덤</button>
    </div>

    <div id="map" class="map-container"></div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import * as am5 from '@amcharts/amcharts5'
import * as am5map from '@amcharts/amcharts5/map'
import { feature } from 'topojson-client'   // ← 필요 (아래 설치 한 줄 참고)
import router from '@/router'

let root

onMounted(async () => {
  root = am5.Root.new('map')
  root.duration = 0
  root.maxTooltipDistance = 8

  const chart = root.container.children.push(
    am5map.MapChart.new(root, {
      panX: 'translateX',
      panY: 'translateY',
      wheelX: 'zoomX',
      wheelY: 'zoomY',
      projection: am5map.geoMercator(),
      pinchZoomEnabled: true
    })
  )

  const zoomControl = am5map.ZoomControl.new(root, {})
  chart.set('zoomControl', zoomControl)

  // 1) TopoJSON → GeoJSON (외곽/홀 winding 문제 깔끔 해결)
  let geo
  try {
    const topo = await fetch('/korea.topo.json').then(r => r.json())
    const objKey = Object.keys(topo.objects)[0] // 첫 오브젝트 사용
    geo = feature(topo, topo.objects[objKey])   // ← GeoJSON FeatureCollection
  } catch (e) {
    console.error('TopoJSON load/convert error:', e)
    return
  }

  // 2) 폴리곤 시리즈
  const polygonSeries = chart.series.push(
    am5map.MapPolygonSeries.new(root, {
      geoJSON: geo,
      calculateAggregates: true
    })
  )

  polygonSeries.mapPolygons.template.setAll({
    tooltipText: '{SIG_KOR_NM}',
    stroke: am5.color(0x333333),
    strokeOpacity: 0.8,
    strokeWidth: 0.7,
    fill: am5.color(0xffffff),
    interactive: true
  })

  polygonSeries.mapPolygons.template.states.create('hover', {
    fill: am5.color(0xfff1a8),
    strokeWidth: 1.2
  })
  polygonSeries.mapPolygons.template.states.create('active', {
    fill: am5.color(0xffd56b),
    strokeWidth: 1.5
  })

  polygonSeries.mapPolygons.template.events.on('click', (ev) => {
    const target = ev.target
    const item = target.dataItem
    if (!item) return
    const isActive = target.isStateActive('active')
    polygonSeries.mapPolygons.each(p => p.set('active', false))
    target.set('active', !isActive)

    const props = item.dataContext?.properties || {}
    console.log('clicked:', { code: props.SIG_CD, name: props.SIG_KOR_NM })
  })

  polygonSeries.events.on('datavalidated', () => {
    const b = polygonSeries.getPrivate('geoBounds')
    if (b) chart.zoomToGeoBounds(b, 20)
  })
})

onBeforeUnmount(() => {
  if (root) { root.dispose(); root = null }
})

function goToFriendList() {
  router.push('/friendList')
}
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
  border-bottom: 1px solid rgb(84, 82, 84);
  background-color: #fff;
  flex-shrink: 0;
  /* 공간이 부족해도 헤더의 높이는 줄어들지 않도록 설정 */
}

/** 지도 위 버튼 */
.map-header button {
  margin: 5px;
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

/* 리셋 CSS 충돌 방지 */
.leaflet-container img { max-width: none !important; }
</style>
