<template>
  <div class="app-container" ref="wrapRef">
    <header class="app-header">
      <div class="header-content">
        <h2 class="app-title">🌱 내 손안의 식물</h2>
        <div class="stage-badge">
          <span class="badge-icon">{{ stages[currentStageIndex].icon }}</span>
          <span class="badge-text">{{ stages[currentStageIndex].name }}</span>
        </div>
      </div>
    </header>

    <main class="viewport-area">
      <div class="hud-top">
        <div class="health-pill">
          <span class="heart-icon">💚</span>
          <div class="hp-bar-track">
            <div class="hp-bar-fill" :style="{ width: health + '%', background: healthColor }"></div>
          </div>
          <span class="hp-text">{{ health }}%</span>
        </div>
      </div>

      <div class="canvas-host" ref="canvasHostRef"></div>

      <transition name="fade">
        <div class="status-toast" :class="statusType" v-if="statusMessage">
          {{ statusMessage }}
        </div>
      </transition>
    </main>

    <section class="control-panel">
      <div class="setting-grid">
        <div class="setting-item">
          <span class="setting-label">💧 물 주기</span>
          <div class="segment-control">
            <button v-for="level in ['low', 'medium', 'high']" :key="'water-' + level" @click="waterLevel = level"
              :class="['segment-btn', { active: waterLevel === level }]">
              {{ level === 'low' ? '적게' : level === 'medium' ? '보통' : '많이' }}
            </button>
          </div>
        </div>
        <div class="setting-item">
          <span class="setting-label">☀️ 햇빛 양</span>
          <div class="segment-control">
            <button v-for="level in ['low', 'medium', 'high']" :key="'sun-' + level" @click="sunLevel = level"
              :class="['segment-btn', { active: sunLevel === level }]">
              {{ level === 'low' ? '그늘' : level === 'medium' ? '보통' : '강함' }}
            </button>
          </div>
        </div>
      </div>

      <div class="action-grid">
        <button class="action-btn water" @click="giveWater" :disabled="!canAct">
          <span class="btn-icon">🚿</span>
          <span class="btn-label">물주기</span>
        </button>
        <button class="action-btn sun" @click="giveSun" :disabled="!canAct">
          <span class="btn-icon">🌤️</span>
          <span class="btn-label">햇빛쬐기</span>
        </button>
        <button class="action-btn time" @click="passTime" :disabled="!canAct">
          <span class="btn-icon">⏰</span>
          <span class="btn-label-main">하루 보내기</span>
          <span class="btn-label-sub">성장 +1</span>
        </button>
      </div>

      <div class="timeline-area">
        <div class="timeline-track">
          <div v-for="(stage, idx) in stages" :key="stage.name" :class="['timeline-step', {
            active: currentStageIndex === idx,
            passed: currentStageIndex > idx
          }]">
            <div class="step-dot"></div>
            <div class="step-label">{{ stage.name }}</div>
          </div>
        </div>
      </div>
    </section>

    <div v-if="isDead || isComplete" class="modal-overlay">
      <div class="result-modal">
        <div class="result-icon">{{ isComplete ? '🎉' : '😢' }}</div>
        <h3 class="result-title">{{ isComplete ? '수확 성공!' : '식물이 시들었어요' }}</h3>
        <p class="result-desc">{{ isComplete ? '정성스럽게 키운 덕분에 열매를 맺었어요.' : '환경을 조절해서 다시 도전해보세요.' }}</p>
        <button class="restart-btn" @click="resetPlant">다시 심기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import P5 from 'p5'

const canvasHostRef = ref(null)
const p5Instance = ref(null)
const wrapRef = ref(null)

// 환경 조건
const waterLevel = ref('medium')
const sunLevel = ref('medium')

// 식물 상태
const currentStageIndex = ref(0)
const health = ref(80)
const growthPoints = ref(0)
const waterGiven = ref(0)
const sunGiven = ref(0)
const isDead = ref(false)
const isComplete = ref(false)

const stages = [
  { name: '씨앗', icon: '🫘', threshold: 0 },
  { name: '발아', icon: '🌱', threshold: 20 },
  { name: '성장', icon: '🌿', threshold: 45 },
  { name: '개화', icon: '🌸', threshold: 70 },
  { name: '결실', icon: '🍎', threshold: 90 },
]

const canAct = computed(() => !isDead.value && !isComplete.value)

const statusType = computed(() => {
  if (isDead.value) return 'dead'
  if (isComplete.value) return 'complete'
  if (health.value < 30) return 'warning'
  return 'normal'
})

const statusMessage = computed(() => {
  if (isDead.value) return null
  if (isComplete.value) return null

  const water = waterLevel.value
  const sun = sunLevel.value

  if (water === 'low' && sun === 'low') return '물과 햇빛이 모두 부족해요!'
  if (water === 'low') return '목이 말라요. 물을 주세요.'
  if (water === 'high') return '물이 너무 많아요. 뿌리가 힘들어요.'
  if (sun === 'low') return '어두워요. 햇빛이 필요해요.'
  if (sun === 'high' && water !== 'high') return '햇빛이 너무 뜨거워요!'

  if (water === 'medium' && sun === 'medium') {
    if (health.value > 90) return '최고의 환경이에요! 쑥쑥 자라요.'
    return '편안한 환경이에요.'
  }
  return '환경을 조절해주세요.'
})

const healthColor = computed(() => {
  if (health.value > 60) return '#22c55e'
  if (health.value > 30) return '#facc15'
  return '#ef4444'
})

// === 로직 함수들 (이전과 동일) ===
function giveWater() {
  if (!canAct.value) return
  waterGiven.value += 1
  playSound('water')
}
function giveSun() {
  if (!canAct.value) return
  sunGiven.value += 1
  playSound('sun')
}
function passTime() {
  if (!canAct.value) return
  const water = waterLevel.value
  const sun = sunLevel.value
  let healthChange = 0
  let growthChange = 0

  if (water === 'medium' && sun === 'medium') { healthChange = 5; growthChange = 15; }
  else if (water === 'medium' && sun === 'low') { healthChange = -2; growthChange = 5; }
  else if (water === 'medium' && sun === 'high') { healthChange = -5; growthChange = 8; }
  else if (water === 'low') { healthChange = -10; growthChange = 2; }
  else if (water === 'high') { healthChange = -8; growthChange = 3; }

  health.value = Math.max(0, Math.min(100, health.value + healthChange))
  growthPoints.value = Math.min(100, growthPoints.value + growthChange)

  for (let i = stages.length - 1; i >= 0; i--) {
    if (growthPoints.value >= stages[i].threshold) {
      if (currentStageIndex.value < i) {
        currentStageIndex.value = i
        playSound('grow')
      }
      break
    }
  }

  if (health.value <= 0) { isDead.value = true; playSound('fail'); }
  if (growthPoints.value >= 100 && health.value > 30) { isComplete.value = true; playSound('complete'); }

  if (p5Instance.value) p5Instance.value.redraw()
}

function resetPlant() {
  currentStageIndex.value = 0
  health.value = 80
  growthPoints.value = 0
  waterGiven.value = 0
  sunGiven.value = 0
  isDead.value = false
  isComplete.value = false
  waterLevel.value = 'medium'
  sunLevel.value = 'medium'
  if (p5Instance.value) p5Instance.value.redraw()
}

// 오디오 컨텍스트 (간소화)
let audioContext = null
function playSound(type) {
  try {
    if (!audioContext) audioContext = new (window.AudioContext || window.webkitAudioContext)()
    if (audioContext.state === 'suspended') audioContext.resume()

    const osc = audioContext.createOscillator()
    const gain = audioContext.createGain()
    osc.connect(gain)
    gain.connect(audioContext.destination)

    const now = audioContext.currentTime
    let freq = 440
    if (type === 'water') freq = 600; else if (type === 'sun') freq = 800;
    else if (type === 'grow') freq = 400; else if (type === 'complete') freq = 523;
    else if (type === 'fail') freq = 200;

    osc.frequency.setValueAtTime(freq, now)
    osc.frequency.exponentialRampToValueAtTime(freq / 2, now + 0.3)
    gain.gain.setValueAtTime(0.1, now)
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3)
    osc.start(now)
    osc.stop(now + 0.3)
  } catch (e) { }
}

// P5 Sketch (반응형 사이즈 적용)
function createSketch() {
  const sketch = (p) => {
    let w = 0
    let h = 0

    p.setup = () => {
      // 컨테이너의 실제 너비를 가져와서 캔버스 생성
      w = canvasHostRef.value?.clientWidth || window.innerWidth
      h = canvasHostRef.value?.clientHeight || 320
      const canvas = p.createCanvas(w, h)
      canvas.parent(canvasHostRef.value)
      p.noLoop()
    }

    // 윈도우 리사이즈 대응 (회전 등)
    p.windowResized = () => {
      if (!canvasHostRef.value) return
      w = canvasHostRef.value.clientWidth
      h = canvasHostRef.value.clientHeight
      p.resizeCanvas(w, h)
      p.redraw()
    }

    p.draw = () => {
      // 배경 (그라데이션 느낌)
      const skyBrightness = sunLevel.value === 'high' ? 240 : sunLevel.value === 'medium' ? 220 : 180
      p.background(135, 206, skyBrightness)

      // === 반응형 위치 계산 ===
      const centerX = w / 2
      const groundY = h - 40 // 바닥 위치

      // 태양
      const sunSize = sunLevel.value === 'high' ? w * 0.2 : sunLevel.value === 'medium' ? w * 0.15 : w * 0.1
      const sunX = w - sunSize
      const sunY = sunSize

      p.noStroke()
      p.fill(255, 220, 50, sunLevel.value === 'high' ? 255 : 200)
      p.circle(sunX, sunY, sunSize)

      // 태양 광선
      if (sunLevel.value !== 'low') {
        p.stroke(255, 220, 50, 100)
        p.strokeWeight(2)
        for (let i = 0; i < 8; i++) {
          const a = (i / 8) * p.TWO_PI
          p.line(sunX + Math.cos(a) * sunSize * 0.6, sunY + Math.sin(a) * sunSize * 0.6,
            sunX + Math.cos(a) * sunSize * 0.8, sunY + Math.sin(a) * sunSize * 0.8)
        }
      }

      // 땅
      p.noStroke()
      p.fill(139, 90, 43) // 흙
      p.rect(0, groundY, w, 40)
      p.fill(76, 153, 76) // 잔디
      p.rect(0, groundY - 5, w, 15)

      // 땅 상태 변화
      if (waterLevel.value === 'high') {
        p.fill(60, 100, 160, 100)
        p.ellipse(centerX, groundY + 5, 120, 15)
      } else if (waterLevel.value === 'low') {
        p.stroke(100, 70, 30); p.strokeWeight(1)
        p.line(centerX - 30, groundY + 10, centerX - 20, groundY + 20)
        p.line(centerX + 30, groundY + 10, centerX + 20, groundY + 20)
      }

      // 식물 그리기 (중심 좌표 기준)
      const potX = centerX
      const potY = groundY - 5
      const hr = health.value / 100

      if (isDead.value) {
        drawDeadPlant(p, potX, potY)
      } else {
        switch (currentStageIndex.value) {
          case 0: drawSeed(p, potX, potY); break;
          case 1: drawSprout(p, potX, potY, hr); break;
          case 2: drawGrowing(p, potX, potY, hr); break;
          case 3: drawFlowering(p, potX, potY, hr); break;
          case 4: drawFruiting(p, potX, potY, hr); break;
        }
      }

      // 물주기 이펙트
      if (waterGiven.value > 0) {
        p.fill(100, 180, 255, 180); p.noStroke()
        for (let i = 0; i < 5; i++) p.circle(potX - 30 + Math.random() * 60, potY - 100 + Math.random() * 40, 6)
      }
    }

    // (드로잉 헬퍼 함수들은 그대로 유지하되, 크기를 약간 반응형으로 조정 가능. 여기선 기본 유지)
    function drawSeed(p, x, y) { p.fill(101, 67, 33); p.noStroke(); p.ellipse(x, y + 10, 20, 12); p.fill(139, 90, 43); p.ellipse(x, y + 10, 14, 8); }
    function drawSprout(p, x, y, hr) { p.stroke(76, 153, 76); p.strokeWeight(4 * hr + 2); p.noFill(); p.line(x, y, x, y - 30); p.fill(120, 180, 80); p.noStroke(); p.ellipse(x - 12, y - 25, 18, 10); p.ellipse(x + 12, y - 25, 18, 10); }
    function drawGrowing(p, x, y, hr) {
      const h = 70 + hr * 30; p.stroke(60, 130, 60); p.strokeWeight(5); p.noFill(); p.beginShape(); p.vertex(x, y); p.quadraticVertex(x + 5, y - h / 2, x, y - h); p.endShape();
      p.fill(80, 160, 80); p.noStroke(); p.circle(x - 10, y - h * 0.4, 18); p.circle(x + 10, y - h * 0.6, 20); p.circle(x, y - h, 14);
    }
    function drawFlowering(p, x, y, hr) {
      const h = 100; p.stroke(60, 130, 60); p.strokeWeight(6); p.noFill(); p.line(x, y, x, y - h);
      p.fill(255, 100, 150); p.noStroke(); p.circle(x, y - h, 45); p.fill(255, 220, 100); p.circle(x, y - h, 18);
    }
    function drawFruiting(p, x, y, hr) {
      const h = 110; p.stroke(60, 130, 60); p.strokeWeight(7); p.noFill(); p.line(x, y, x, y - h);
      p.fill(220, 50, 50); p.noStroke(); p.circle(x, y - h + 15, 40); p.fill(80, 160, 80); p.circle(x - 10, y - h / 2, 22);
    }
    function drawDeadPlant(p, x, y) {
      p.stroke(100, 80, 50); p.strokeWeight(4); p.noFill(); p.line(x, y, x + 15, y - 30); p.fill(120, 100, 60); p.noStroke(); p.circle(x + 15, y - 30, 12);
    }
  }
  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => {
  nextTick(() => { createSketch() })
})
onBeforeUnmount(() => {
  if (p5Instance.value) p5Instance.value.remove()
  if (audioContext) audioContext.close()
})
watch([waterLevel, sunLevel, health, currentStageIndex, isDead], () => { if (p5Instance.value) p5Instance.value.redraw() })
</script>

<style scoped>
/* 앱 전체 컨테이너 (Full Screen) */
.app-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  /* 뷰포트 전체 높이 */
  background: #ffffff;
  font-family: 'SUIT', system-ui, sans-serif;
  color: #1f2937;
  overflow: hidden;
  /* 전체 스크롤 방지 */

  /* Safe Area 대응 (Notch, 제스처바) */
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 1. 헤더 */
.app-header {
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f3f4f6;
  flex-shrink: 0;
  /* 크기 고정 */
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-title {
  font-size: 18px;
  font-weight: 800;
  color: #166534;
  margin: 0;
}

.stage-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f0fdf4;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #bbf7d0;
}

.badge-text {
  font-size: 13px;
  font-weight: 700;
  color: #15803d;
}

/* 2. 메인 뷰포트 (캔버스) - 유동적 높이 */
.viewport-area {
  flex: 1;
  /* 남은 공간 모두 차지 */
  position: relative;
  background: #e0f2fe;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  /* 캔버스를 아래로 */
}

.canvas-host {
  width: 100%;
  height: 100%;
}

.hud-top {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
}

.health-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.hp-bar-track {
  width: 100px;
  height: 10px;
  background: #e5e7eb;
  border-radius: 5px;
  overflow: hidden;
}

.hp-bar-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s ease;
}

.hp-text {
  font-size: 12px;
  font-weight: 700;
  color: #374151;
  min-width: 30px;
  text-align: right;
}

/* 상태 메시지 토스트 */
.status-toast {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  padding: 10px 20px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  z-index: 20;
}

.status-toast.warning {
  color: #d97706;
  border: 1px solid #fcd34d;
}

.status-toast.normal {
  color: #166534;
  border: 1px solid #86efac;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, 10px);
}

/* 3. 컨트롤 패널 */
.control-panel {
  flex-shrink: 0;
  /* 크기 고정 */
  background: #ffffff;
  padding: 20px 24px 10px;
  /* 하단 여백 safe-area가 padding-bottom 처리함 */
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 20px;
  z-index: 50;
}

/* 환경 설정 그리드 */
.setting-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-label {
  font-size: 13px;
  font-weight: 700;
  color: #4b5563;
}

.segment-control {
  display: flex;
  background: #f3f4f6;
  border-radius: 12px;
  padding: 4px;
}

.segment-btn {
  flex: 1;
  background: transparent;
  border: none;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
  /* 웹뷰 터치 인식 */
}

.segment-btn.active {
  background: #ffffff;
  color: #166534;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 액션 버튼 그리드 */
.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr;
  gap: 12px;
  height: 64px;
}

.action-btn {
  border: none;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
}

.action-btn:active:not(:disabled) {
  transform: scale(0.96);
}

.action-btn:disabled {
  opacity: 0.5;
}

.action-btn.water {
  background: #eff6ff;
  color: #2563eb;
}

.action-btn.sun {
  background: #fffbeb;
  color: #d97706;
}

.action-btn.time {
  background: #16a34a;
  color: white;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
}

.btn-icon {
  font-size: 20px;
}

.btn-label {
  font-size: 12px;
  font-weight: 700;
}

.btn-label-main {
  font-size: 13px;
  font-weight: 800;
}

.btn-label-sub {
  font-size: 10px;
  font-weight: 400;
  opacity: 0.9;
}

/* 타임라인 (간소화) */
.timeline-area {
  margin-top: 4px;
  padding-bottom: 10px;
}

.timeline-track {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 0 10px;
}

.timeline-track::before {
  content: '';
  position: absolute;
  top: 6px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.timeline-step {
  position: relative;
  z-index: 1;
  text-align: center;
}

.step-dot {
  width: 14px;
  height: 14px;
  background: #e5e7eb;
  border-radius: 50%;
  margin: 0 auto 6px;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #e5e7eb;
  transition: all 0.3s;
}

.step-label {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 600;
}

.timeline-step.active .step-dot {
  background: #22c55e;
  box-shadow: 0 0 0 1px #22c55e;
  transform: scale(1.2);
}

.timeline-step.active .step-label {
  color: #15803d;
}

.timeline-step.passed .step-dot {
  background: #86efac;
  box-shadow: 0 0 0 1px #86efac;
}

/* 결과 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.result-modal {
  background: white;
  padding: 30px;
  border-radius: 24px;
  text-align: center;
  width: 80%;
  max-width: 320px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.result-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.result-title {
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 8px;
}

.result-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 24px;
  line-height: 1.5;
}

.restart-btn {
  width: 100%;
  padding: 14px;
  background: #1f2937;
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}
</style>
