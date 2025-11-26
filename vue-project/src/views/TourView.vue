<template>
  <div class="tour-page-layout">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        동선 미리보기
      </div>
      <div class="header-right" style="flex: 1;"></div>
    </div>

    <div class="main-content">
      <VirtualTour ref="virtualTourRef" :config="dynamicTourConfig" @scene-changed="handleSceneChange"
        @hotspot-click="handleHotspotClick" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import VirtualTour from '@/components/virtual/VirtualTour.vue'
import { getMasterConfig } from '@/utils/tourMapper.js'

const virtualTourRef = ref(null)
const router = useRouter()

// 세션에서 코스 아이템 읽기
const tourItems = ref([])
try {
  const cacheKey = sessionStorage.getItem('pdv:courseCacheKey')
  if (cacheKey) {
    const rawData = sessionStorage.getItem(cacheKey)
    if (rawData) {
      const payload = JSON.parse(rawData)
      if (payload && Array.isArray(payload.items)) {
        tourItems.value = payload.items
      }
    }
  }
} catch (e) {
  console.error('세션 스토리지에서 투어 아이템 로딩 실패:', e)
  tourItems.value = []
}
console.log('가상 답사 페이지(TourView)가 (세션에서) 전달받은 탭 목록:', tourItems.value)

// 동적 투어 설정
const dynamicTourConfig = computed(() => {
  console.log('동적 투어 설정 계산 시작. 아이템:', tourItems.value)

  const masterTourConfig = getMasterConfig(tourItems.value)

  const allowedSceneIds = new Set(
    tourItems.value.map(item => item.sceneId).filter(Boolean)
  )

  const firstValidSceneId = tourItems.value
    .map(item => item.sceneId)
    .find(id => allowedSceneIds.has(id))

  const newConfig = {
    ...masterTourConfig,
    default: {
      ...masterTourConfig.default,
      firstScene: firstValidSceneId || masterTourConfig.default.firstScene
    }
  }

  console.log('필터링 없이 첫 씬만 수정한 코스 설정을 반환합니다:', newConfig)
  return newConfig
})

// 현재 씬
const currentSceneId = ref(dynamicTourConfig.value.default.firstScene)

// 씬 이동
function handleNavigation(sceneId) {
  virtualTourRef.value?.loadScene(sceneId)
}

// pannellum에서 씬 바뀔 때
function handleSceneChange(newSceneId) {
  currentSceneId.value = newSceneId
  console.log('✅ [TourView] 씬 변경:', newSceneId)
}

// 핫스팟 클릭 (나가기용)
function handleHotspotClick({ hotspot }) {
  const text = hotspot?.text || ''

  if (!text.includes('미리보기를 종료합니다')) return

  sessionStorage.setItem('pdv:returnFromTour', '1')

  const raw = sessionStorage.getItem('pdv:returnTo')
  if (raw) {
    const { type, id, query } = JSON.parse(raw)
    const name = type === 'science_place' ? 'PlaceDetail' : 'ExhibitionDetail'

    router.replace({ name, params: { id }, query })
  } else {
    router.replace({ name: 'Home' })
  }
}

function goBack() {
  router.back()
}

// 탭 하이라이트용 정보 (기존 로직)
const isEntranceView = computed(() => {
  if (!tourItems.value || tourItems.value.length === 0) return false
  return tourItems.value.some(
    item => item.sceneId && item.sceneId.endsWith('_entrance')
  )
})

const currentZone = computed(() => {
  const id = currentSceneId.value || ''

  if (isEntranceView.value) {
    if (id.startsWith('hall_1')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_1_entrance')
      return 'hall_1_entrance'
    }
    if (id.startsWith('hall_2')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_2_entrance')
      return 'hall_2_entrance'
    }
    if (id.startsWith('hall_3')) {
      console.log('✅ [TourView] activeZone 계산 (입구 모드): hall_3_entrance')
      return 'hall_3_entrance'
    }
    console.log('✅ [TourView] activeZone 계산 (입구 모드): (일치 항목 없음)')
    return ''
  } else {
    console.log(`✅ [TourView] activeZone 계산 (상세 모드): ${id}`)
    return id
  }
})
</script>

<style scoped>
.tour-page-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.main-content {
  flex-grow: 1;
  height: 100%;
  overflow: hidden;
  position: relative;
}
</style>
