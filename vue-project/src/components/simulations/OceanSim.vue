<template>
  <div class="sim-container">
    <header class="sim-header">
      <h2 class="sim-title">🌊 움직이는 바다</h2>
      <div class="weather-badge">
        <span class="badge-icon">{{ weatherIcon }}</span>
        <span class="badge-text">{{ timeLabel }} / {{ tideLabel }}</span>
      </div>
    </header>

    <main class="viewport-area">
      <div class="hud-layer">
        <div class="observation-tag landform" v-if="showTags">
          📍 해식 절벽
        </div>
        <div class="observation-tag mudflat" v-if="showTags && isLowTide">
          📍 갯벌 (생물 등장!)
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
          🌔 달(인력)
        </button>
        <button :class="['tab-btn', { active: activeTab === 'wind' }]" @click="activeTab = 'wind'">
          💨 바람(파도)
        </button>
      </div>

      <div class="control-body">
        <div v-if="activeTab === 'tide'" class="control-group">
          <div class="slider-header">
            <span class="label">달의 위치</span>
            <span class="value">{{ moonLabel }}</span>
          </div>
          <input type="range" min="0" max="100" v-model.number="moonPhase" class="custom-range moon-range">
          <p class="desc-text">
            달이 물을 당겨 <strong>{{ tideLabel }}</strong>이 됐어요.
            <span v-if="isLowTide" class="highlight">갯벌 발견!</span>
          </p>
        </div>

        <div v-if="activeTab === 'wind'" class="control-group">
          <div class="slider-header">
            <span class="label">바람 세기</span>
            <span class="value">{{ windLabel }}</span>
          </div>
          <input type="range" min="0" max="100" v-model.number="windSpeed" class="custom-range wind-range">
          <p class="desc-text">
            바람 때문에 <strong>파도</strong>가 칩니다.
            <span v-if="windSpeed > 70" class="highlight">절벽 침식 중!</span>
          </p>
        </div>
      </div>

      <div class="action-row">
        <button class="toggle-btn" @click="toggleDayNight">
          {{ isNight ? '☀️ 낮' : '🌙 밤' }}
        </button>
        <button class="toggle-btn sub" @click="showTags = !showTags">
          {{ showTags ? '🏷️ 숨기기' : '🏷️ 보기' }}
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
const activeTab = ref('tide')
const moonPhase = ref(50)
const windSpeed = ref(20)
const isNight = ref(false)
const showTags = ref(true)

// === Computed ===
const isLowTide = computed(() => moonPhase.value < 30)
const isHighTide = computed(() => moonPhase.value > 70)

const tideLabel = computed(() => isHighTide.value ? '밀물' : isLowTide.value ? '썰물' : '흐름')
const moonLabel = computed(() => moonPhase.value < 30 ? '멀어짐' : moonPhase.value > 70 ? '가까움' : '보통')
const windLabel = computed(() => windSpeed.value < 20 ? '잔잔' : windSpeed.value < 60 ? '적당' : '강풍')
const timeLabel = computed(() => isNight.value ? '밤' : '낮')
const weatherIcon = computed(() => isNight.value ? '🌙' : '☀️')

const infoMessage = computed(() => {
  if (activeTab.value === 'tide') {
    if (isLowTide.value) return '물이 빠져 갯벌이 보여요!'
    if (isHighTide.value) return '물이 가득 찼어요.'
  }
  if (activeTab.value === 'wind') {
    if (windSpeed.value > 80) return '파도가 절벽을 깎아요(침식).'
  }
  return null
})

// === Methods ===
function toggleDayNight() {
  isNight.value = !isNight.value
  if (p5Instance.value) p5Instance.value.redraw()
}

// === P5 Logic ===
function createSketch() {
  const sketch = (p) => {
    let w = 0, h = 0
    let waveOffset = 0
    let cloudPos = []
    const creatures = [
      { x: 0.6, y: 0, type: 'crab', offset: 0 },
      { x: 0.75, y: 0, type: 'clam', offset: 2 },
      { x: 0.85, y: 0, type: 'crab', offset: 4 }
    ]

    p.setup = () => {
      // 부모 컨테이너 크기에 맞춤
      w = canvasHostRef.value?.clientWidth || 300
      h = canvasHostRef.value?.clientHeight || 200
      p.createCanvas(w, h).parent(canvasHostRef.value)

      for (let i = 0; i < 5; i++) cloudPos.push({ x: p.random(w), y: p.random(20, 80), s: p.random(0.4, 0.8) })
    }

    p.windowResized = () => {
      // 윈도우 리사이즈 시에도 컨테이너 크기 다시 계산
      if (canvasHostRef.value) {
        w = canvasHostRef.value.clientWidth
        h = canvasHostRef.value.clientHeight
        p.resizeCanvas(w, h)
      }
    }

    p.draw = () => {
      drawSky(p, w, h)
      drawTerrain(p, w, h)
      drawOcean(p, w, h)
      if (isLowTide.value) drawCreatures(p, w, h)
    }

    function drawSky(p, w, h) {
      if (isNight.value) {
        p.background(20, 24, 82)
        p.fill(255, 255, 255, 150); p.noStroke()
        if (p.frameCount % 60 === 0) p.randomSeed(10)
        for (let i = 0; i < 10; i++) p.ellipse(p.random(w), p.random(h / 2), 2, 2)
      } else {
        p.background(135, 206, 235)
        p.noStroke(); p.fill(255, 220, 0)
        p.circle(w - 40, 40, 40)
      }

      p.fill(255, 255, 255, isNight.value ? 50 : 200); p.noStroke()
      const windFactor = (windSpeed.value + 10) * 0.05
      cloudPos.forEach(c => {
        c.x += windFactor * 0.5
        if (c.x > w + 40) c.x = -40
        p.push(); p.translate(c.x, c.y); p.scale(c.s)
        p.ellipse(0, 0, 50, 30); p.ellipse(20, -5, 40, 25); p.ellipse(20, 5, 40, 25)
        p.pop()
      })
    }

    function drawTerrain(p, w, h) {
      p.fill(101, 67, 33); p.noStroke()
      p.beginShape()
      p.vertex(0, h); p.vertex(0, h * 0.4)
      p.vertex(w * 0.2, h * 0.4); p.vertex(w * 0.25, h * 0.6)
      p.vertex(w, h * 0.8); p.vertex(w, h)
      p.endShape(p.CLOSE)
      p.fill(80, 60, 40); p.rect(0, h * 0.4, w * 0.2, h * 0.2)
    }

    function drawOcean(p, w, h) {
      const tideH = p.map(moonPhase.value, 0, 100, h * 0.75, h * 0.45)
      const waveH = p.map(windSpeed.value, 0, 100, 2, 20)
      const waveSpeed = p.map(windSpeed.value, 0, 100, 0.02, 0.1)

      p.noStroke()
      p.fill(30, 144, 255, 150)
      p.beginShape(); p.vertex(0, h); p.vertex(0, tideH)
      for (let x = 0; x <= w; x += 10) {
        p.vertex(x, tideH + Math.sin(x * 0.02 + waveOffset) * waveH * 0.8)
      }
      p.vertex(w, h); p.endShape(p.CLOSE)

      p.fill(65, 105, 225, 200)
      p.beginShape(); p.vertex(0, h); p.vertex(0, tideH + 10)
      for (let x = 0; x <= w; x += 10) {
        p.vertex(x, tideH + 10 + Math.sin(x * 0.03 + waveOffset + 1) * waveH)
      }
      p.vertex(w, h); p.endShape(p.CLOSE)

      waveOffset += waveSpeed

      if (tideH < h * 0.55 && windSpeed.value > 50) {
        p.fill(255, 255, 255, 150); p.noStroke()
        const splashSize = (windSpeed.value - 50) * 0.5
        p.circle(w * 0.25, h * 0.55, splashSize * p.random(0.5, 1))
      }
    }

    function drawCreatures(p, w, h) {
      creatures.forEach(c => {
        const cx = w * c.x
        const slopeY = h * (0.53 + 0.26 * c.x)
        const floatY = Math.sin(p.frameCount * 0.1 + c.offset) * 2
        p.push(); p.translate(cx, slopeY + floatY)
        if (c.type === 'crab') {
          p.fill(255, 100, 100); p.ellipse(0, 0, 14, 10)
          p.stroke(200, 50, 50); p.strokeWeight(1.5); p.line(-6, -3, -9, -7); p.line(6, -3, 9, -7)
        } else {
          p.fill(240, 230, 200); p.noStroke(); p.arc(0, 0, 12, 10, p.PI, 0)
        }
        p.pop()
      })
    }
  }
  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => { nextTick(() => createSketch()) })
onBeforeUnmount(() => { if (p5Instance.value) p5Instance.value.remove() })
</script>

<style scoped>
/* 임베디드 컨테이너 스타일 */
.sim-container {
  display: flex;
  flex-direction: column;
  /* 부모 요소(simulation-content) 안에 꽉 차도록 */
  width: 100%;
  height: 100%;
  min-height: 500px;
  /* 위젯의 최소 높이 보장 */
  background: #f0f9ff;
  font-family: 'SUIT', sans-serif;
  color: #1e293b;
  border-radius: 12px;
  overflow: hidden;
  /* 둥근 모서리 유지 */
  position: relative;
}

/* 1. 헤더 (컴팩트) */
.sim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid #e0f2fe;
  flex-shrink: 0;
}

.sim-title {
  font-size: 15px;
  font-weight: 800;
  color: #0369a1;
  margin: 0;
}

.weather-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #e0f2fe;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  color: #0284c7;
}

/* 2. 메인 뷰포트 (남은 높이 모두 차지) */
.viewport-area {
  flex: 1;
  position: relative;
  background: #87ceeb;
  overflow: hidden;
  min-height: 200px;
  /* 캔버스 최소 높이 */
}

.canvas-host {
  width: 100%;
  height: 100%;
}

/* HUD 태그 */
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
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  color: #334155;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  animation: float 3s ease-in-out infinite;
}

.landform {
  top: 30%;
  left: 5%;
}

.mudflat {
  bottom: 20%;
  right: 5%;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-3px);
  }
}

/* 토스트 */
.info-toast {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.85);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  backdrop-filter: blur(2px);
  z-index: 10;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 3. 컨트롤 패널 (컴포넌트 내부 하단) */
.control-panel {
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.control-tabs {
  display: flex;
  background: #f1f5f9;
  padding: 3px;
  border-radius: 8px;
}

.tab-btn {
  flex: 1;
  padding: 8px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  cursor: pointer;
}

.tab-btn.active {
  background: white;
  color: #0369a1;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.control-group {
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #334155;
}

.desc-text {
  font-size: 11px;
  color: #64748b;
  margin: 8px 0 0;
  line-height: 1.3;
}

.highlight {
  color: #d97706;
  font-weight: 700;
}

.custom-range {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #cbd5e1;
  outline: none;
  -webkit-appearance: none;
}

.custom-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.moon-range::-webkit-slider-thumb {
  background: #fbbf24;
}

.wind-range::-webkit-slider-thumb {
  background: #38bdf8;
}

.action-row {
  display: flex;
  gap: 8px;
}

.toggle-btn {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
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
