<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-content">
        <h2 class="app-title">🌊 움직이는 바다 실험실</h2>
        <div class="weather-badge">
          <span class="badge-icon">{{ weatherIcon }}</span>
          <span class="badge-text">{{ timeLabel }} / {{ tideLabel }}</span>
        </div>
      </div>
    </header>

    <main class="viewport-area">
      <div class="hud-layer">
        <div class="observation-tag landform" v-if="showTags">
          📍 해식 절벽 (침식)
        </div>
        <div class="observation-tag mudflat" v-if="showTags && isLowTide">
          📍 갯벌 (퇴적) - 생물 등장!
        </div>
      </div>

      <div class="canvas-host" ref="canvasHostRef"></div>

      <transition name="fade">
        <div class="info-toast" v-if="infoMessage">
          {{ infoMessage }}
        </div>
      </transition>
    </main>

    <section class="control-panel">
      <div class="control-tabs">
        <button :class="['tab-btn', { active: activeTab === 'tide' }]" @click="activeTab = 'tide'">
          🌔 달과 밀썰물
        </button>
        <button :class="['tab-btn', { active: activeTab === 'wind' }]" @click="activeTab = 'wind'">
          💨 바람과 파도
        </button>
      </div>

      <div class="control-body">
        <div v-if="activeTab === 'tide'" class="control-group">
          <div class="slider-header">
            <span class="label">달의 위치 (인력)</span>
            <span class="value">{{ moonLabel }}</span>
          </div>
          <input type="range" min="0" max="100" v-model.number="moonPhase" class="custom-range moon-range">
          <p class="desc-text">
            달이 바닷물을 당겨서 <strong>{{ tideLabel }}</strong>이 되었습니다.
            <span v-if="isLowTide" class="highlight">갯벌이 드러났어요!</span>
          </p>
        </div>

        <div v-if="activeTab === 'wind'" class="control-group">
          <div class="slider-header">
            <span class="label">바람 세기 (대기)</span>
            <span class="value">{{ windLabel }}</span>
          </div>
          <input type="range" min="0" max="100" v-model.number="windSpeed" class="custom-range wind-range">
          <p class="desc-text">
            바람이 불어 <strong>파도</strong>가 칩니다.
            <span v-if="windSpeed > 70" class="highlight">해안가가 깎이고 있어요!</span>
          </p>
        </div>
      </div>

      <div class="action-row">
        <button class="toggle-btn" @click="toggleDayNight">
          {{ isNight ? '☀️ 낮으로 변경' : '🌙 밤으로 변경' }}
        </button>
        <button class="toggle-btn sub" @click="showTags = !showTags">
          {{ showTags ? '🏷️ 명칭 숨기기' : '🏷️ 지형 명칭 보기' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import P5 from 'p5'

const canvasHostRef = ref(null)
const p5Instance = ref(null)

// === 상태 변수 ===
const activeTab = ref('tide') // 'tide' | 'wind'
const moonPhase = ref(50)     // 0(저조/썰물) ~ 100(고조/밀물) 시뮬레이션용 변수
const windSpeed = ref(20)     // 0 ~ 100
const isNight = ref(false)
const showTags = ref(true)

// === Computed Logic ===
const isLowTide = computed(() => moonPhase.value < 30)
const isHighTide = computed(() => moonPhase.value > 70)

const tideLabel = computed(() => {
  if (isHighTide.value) return '밀물 (만조)'
  if (isLowTide.value) return '썰물 (간조)'
  return '흐르는 중'
})

const moonLabel = computed(() => {
  if (moonPhase.value < 30) return '약함 (멀어짐)'
  if (moonPhase.value > 70) return '강함 (가까움)'
  return '보통'
})

const windLabel = computed(() => {
  if (windSpeed.value < 20) return '잔잔함'
  if (windSpeed.value < 60) return '적당함'
  return '강풍'
})

const timeLabel = computed(() => isNight.value ? '밤' : '낮')
const weatherIcon = computed(() => isNight.value ? '🌙' : '☀️')

const infoMessage = computed(() => {
  if (activeTab.value === 'tide') {
    if (isLowTide.value) return '물이 빠져나가 갯벌과 조개들이 보입니다!'
    if (isHighTide.value) return '물이 가득 찼습니다. 배가 들어올 수 있어요.'
  }
  if (activeTab.value === 'wind') {
    if (windSpeed.value > 80) return '강한 파도가 바위를 때려 절벽을 만듭니다 (침식).'
  }
  return null
})

// === Methods ===
function toggleDayNight() {
  isNight.value = !isNight.value
  if (p5Instance.value) p5Instance.value.redraw()
}

// === P5.js Visualization ===
function createSketch() {
  const sketch = (p) => {
    let w = 0, h = 0
    // 애니메이션용 변수
    let waveOffset = 0
    let cloudPos = []

    // 생물 변수 (게, 조개)
    const creatures = [
      { x: 0.6, y: 0, type: 'crab', offset: 0 },
      { x: 0.75, y: 0, type: 'clam', offset: 2 },
      { x: 0.85, y: 0, type: 'crab', offset: 4 }
    ]

    p.setup = () => {
      w = canvasHostRef.value?.clientWidth || window.innerWidth
      h = canvasHostRef.value?.clientHeight || 400
      p.createCanvas(w, h).parent(canvasHostRef.value)

      // 구름 초기화
      for (let i = 0; i < 5; i++) cloudPos.push({ x: p.random(w), y: p.random(50, 150), s: p.random(0.5, 1.2) })
    }

    p.windowResized = () => {
      w = canvasHostRef.value.clientWidth
      h = canvasHostRef.value.clientHeight
      p.resizeCanvas(w, h)
    }

    p.draw = () => {
      // 1. 대기(하늘) 그리기
      drawSky(p, w, h)

      // 2. 지형 그리기 (해식 절벽 & 갯벌 바닥)
      drawTerrain(p, w, h)

      // 3. 바다(파도) 그리기
      drawOcean(p, w, h)

      // 4. 전경 및 갯벌 생물 (썰물일 때만)
      if (isLowTide.value) {
        drawCreatures(p, w, h)
      }
    }

    function drawSky(p, w, h) {
      if (isNight.value) {
        p.background(20, 24, 82) // 밤하늘
        // 별
        p.fill(255, 255, 255, 150)
        p.noStroke()
        if (p.frameCount % 60 === 0) p.randomSeed(10) // 반짝임 고정
        for (let i = 0; i < 20; i++) {
          p.ellipse(p.random(w), p.random(h / 2), 2, 2)
        }
      } else {
        // 낮하늘 (그라데이션 흉내)
        p.background(135, 206, 235)
        p.noStroke()
        p.fill(255, 220, 0)
        p.circle(w - 50, 60, 60) // 태양
      }

      // 구름 이동 (바람 세기에 영향)
      p.fill(255, 255, 255, isNight.value ? 50 : 200)
      p.noStroke()
      const windFactor = (windSpeed.value + 10) * 0.05

      cloudPos.forEach(c => {
        c.x += windFactor * 0.5
        if (c.x > w + 50) c.x = -50

        p.push()
        p.translate(c.x, c.y)
        p.scale(c.s)
        p.ellipse(0, 0, 60, 40)
        p.ellipse(25, -10, 50, 35)
        p.ellipse(25, 10, 50, 35)
        p.pop()
      })
    }

    function drawTerrain(p, w, h) {
      // 갯벌 바닥 (경사면)
      p.fill(101, 67, 33) // 진흙색
      p.noStroke()

      p.beginShape()
      p.vertex(0, h)
      p.vertex(0, h * 0.4) // 절벽 시작점
      // 절벽 라인
      p.vertex(w * 0.2, h * 0.4)
      p.vertex(w * 0.25, h * 0.6) // 절벽 아래
      // 완만한 갯벌 경사
      p.vertex(w, h * 0.8)
      p.vertex(w, h)
      p.endShape(p.CLOSE)

      // 절벽 디테일 (침식 지형)
      p.fill(80, 60, 40)
      p.rect(0, h * 0.4, w * 0.2, h * 0.2) // 절벽 단면
    }

    function drawOcean(p, w, h) {
      // 수위 계산 (달의 위상 + 주기적 변동)
      // moonPhase 0 -> 수위 낮음 (h*0.75), 100 -> 수위 높음 (h*0.45)
      const tideH = p.map(moonPhase.value, 0, 100, h * 0.75, h * 0.45)

      // 파도 높이 (바람)
      const waveH = p.map(windSpeed.value, 0, 100, 5, 40)
      const waveSpeed = p.map(windSpeed.value, 0, 100, 0.02, 0.1)

      p.noStroke()
      // 물 색상 (깊이에 따라 2단계)

      // 뒷 물결
      p.fill(30, 144, 255, 150)
      p.beginShape()
      p.vertex(0, h)
      p.vertex(0, tideH)
      for (let x = 0; x <= w; x += 10) {
        const y = tideH + Math.sin(x * 0.02 + waveOffset) * waveH * 0.8
        p.vertex(x, y)
      }
      p.vertex(w, h)
      p.endShape(p.CLOSE)

      // 앞 물결 (메인)
      p.fill(65, 105, 225, 200)
      p.beginShape()
      p.vertex(0, h)
      p.vertex(0, tideH + 10)
      for (let x = 0; x <= w; x += 10) {
        const y = tideH + 10 + Math.sin(x * 0.03 + waveOffset + 1) * waveH
        p.vertex(x, y)
      }
      p.vertex(w, h)
      p.endShape(p.CLOSE)

      waveOffset += waveSpeed

      // 파도 칠 때 절벽에 물보라 효과
      if (tideH < h * 0.55 && windSpeed.value > 50) {
        p.fill(255, 255, 255, 150)
        p.noStroke()
        const splashSize = (windSpeed.value - 50)
        p.circle(w * 0.25, h * 0.55, splashSize * p.random(0.5, 1))
      }
    }

    function drawCreatures(p, w, h) {
      // 갯벌 위에 생물 그리기
      creatures.forEach(c => {
        // 위치 계산 (경사면에 맞게)
        const cx = w * c.x
        // 갯벌 라인 근사치: (0.25, 0.6) -> (1.0, 0.8)
        // y = 0.26x + 0.53 (대략적)
        const slopeY = h * (0.53 + 0.26 * c.x)

        // 애니메이션 (숨쉬기)
        const floatY = Math.sin(p.frameCount * 0.1 + c.offset) * 2

        p.push()
        p.translate(cx, slopeY + floatY)

        if (c.type === 'crab') {
          // 게
          p.fill(255, 100, 100) // 빨간색
          p.ellipse(0, 0, 20, 14) // 몸통
          p.stroke(200, 50, 50); p.strokeWeight(2)
          p.line(-8, -5, -12, -10); p.line(8, -5, 12, -10) // 다리
        } else {
          // 조개
          p.fill(240, 230, 200)
          p.noStroke()
          p.arc(0, 0, 18, 16, p.PI, 0)
        }
        p.pop()
      })
    }
  }
  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => {
  nextTick(() => createSketch())
})

onBeforeUnmount(() => {
  if (p5Instance.value) p5Instance.value.remove()
})
</script>

<style scoped>
/* S24 Ultra 풀스크린 최적화 */
.app-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f9ff;
  font-family: 'SUIT', 'Pretendard', sans-serif;
  overflow: hidden;
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 헤더 */
.app-header {
  padding: 14px 20px;
  background: white;
  border-bottom: 1px solid #e0f2fe;
  flex-shrink: 0;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-title {
  font-size: 18px;
  font-weight: 800;
  color: #0369a1;
  margin: 0;
}

.weather-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #e0f2fe;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  color: #0284c7;
}

/* 메인 뷰포트 */
.viewport-area {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #87ceeb;
  /* p5 로드 전 배경 */
}

.canvas-host {
  width: 100%;
  height: 100%;
}

/* HUD (관찰 태그) */
.hud-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 5;
}

.observation-tag {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  color: #334155;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  animation: float 3s ease-in-out infinite;
}

.landform {
  top: 35%;
  left: 5%;
}

.mudflat {
  bottom: 25%;
  right: 10%;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

/* 팝업 토스트 */
.info-toast {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.85);
  color: white;
  padding: 10px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  backdrop-filter: blur(4px);
  z-index: 20;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 컨트롤 패널 */
.control-panel {
  background: white;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding: 20px 24px 10px;
  /* safe-area 고려 */
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 20;
}

/* 탭 메뉴 */
.control-tabs {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
}

.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: white;
  color: #0369a1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 컨트롤 바디 */
.control-group {
  background: #f8fafc;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.desc-text {
  font-size: 12px;
  color: #64748b;
  margin: 12px 0 0;
  line-height: 1.4;
}

.highlight {
  color: #d97706;
  font-weight: 700;
}

/* 커스텀 레인지 슬라이더 */
.custom-range {
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: #cbd5e1;
  outline: none;
  -webkit-appearance: none;
}

.moon-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fbbf24;
  border: 2px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.wind-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #38bdf8;
  border: 2px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

/* 하단 버튼 */
.action-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.toggle-btn {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  border: none;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  background: #0f172a;
  color: white;
}

.toggle-btn.sub {
  background: #f1f5f9;
  color: #334155;
}
</style>
