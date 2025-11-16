<template>
  <div class="electric-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-lightning-charge-fill"></i>
        </div>
        <div class="header-text">
          <div class="header-title">번개는 어디로 흐를까?</div>
        </div>
      </div>

      <div class="header-actions">
        <button class="chip-btn" @click="reset">초기화</button>
        <button class="chip-btn primary" @click="run">번개 보기</button>
      </div>
    </div>

    <!-- 조작 영역 -->
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">장치 선택</span>
        <div class="control-options">
          <button v-for="dev in devices" :key="dev.key" :class="['pill-btn', { active: currentDevice === dev.key }]"
            @click="selectDevice(dev.key)">
            {{ dev.label }}
          </button>
        </div>
      </div>

      <div class="control-row voltage-row">
        <span class="control-label">전압 세기</span>
        <div class="slider-wrap">
          <input type="range" min="1" max="3" step="1" v-model.number="level" @input="updateLevel" />
          <span class="slider-level">{{ levelText }}</span>
        </div>
      </div>
    </div>

    <!-- 캔버스 카드 -->
    <div class="canvas-card">
      <div class="canvas-host" ref="canvasHostRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import p5 from 'p5'

const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

const devices = ref([
  { key: 'tesla', label: '테슬라 코일' },
  { key: 'vandag', label: '반데그라프' },
  { key: 'ladder', label: '방전 사다리' }
])

const currentDevice = ref('tesla')
const level = ref(2) // 1: 약, 2: 중, 3: 강

const levelText = computed(() => {
  if (level.value === 1) return '약'
  if (level.value === 2) return '중'
  return '강'
})

function reset() {
  if (p5Instance && p5Instance.resetSketch) {
    p5Instance.resetSketch()
  }
}

function run() {
  if (p5Instance && p5Instance.fireOnce) {
    p5Instance.fireOnce()
  }
}

function selectDevice(key) {
  currentDevice.value = key
  if (p5Instance && p5Instance.setDevice) {
    p5Instance.setDevice(key)
  }
}

function updateLevel() {
  if (p5Instance && p5Instance.setLevel) {
    p5Instance.setLevel(level.value)
  }
}

/* ===================== p5 스케치 ===================== */
const sketch = (p) => {
  const aspect = 0.9
  let w = 600
  let h = 540

  let device = 'tesla'
  let powerLevel = 2

  let isFiring = false
  let fireStart = 0
  const fireDuration = 2500

  // 타깃 위치 (사람, 피뢰침)
  let targetLeft, targetRight

  // 인포 박스용 텍스트
  let infoTitle = ''
  let infoDesc = ''

  p.setup = () => {
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH

    const c = p.createCanvas(w, h)
    c.parent(canvasHostRef.value)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    initLayout()
    p.noLoop()
    p.redraw()
  }

  p.windowResized = () => {
    if (!canvasHostRef.value) return
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH
    p.resizeCanvas(w, h)
    initLayout()
    p.redraw()
  }

  function initLayout() {
    targetLeft = {
      x: w * 0.22,
      y: h * 0.72
    }
    targetRight = {
      x: w * 0.78,
      y: h * 0.67
    }
  }

  p.draw = () => {
    p.background(255)

    // 장치별 설명 문자열 세팅
    if (device === 'tesla') {
      infoTitle = '테슬라 코일'
      infoDesc =
        '높은 전압으로 공기 중에 긴 번개를 만들어 둘 중 더 전기가 통하기 쉬운 곳으로 스파크가 튑니다.'
    } else if (device === 'vandag') {
      infoTitle = '반데그라프'
      infoDesc =
        '둥근 금속 구에 전하가 계속 모이다가, 한계에 도달하면 가까운 물체로 짧은 번개가 튑니다.'
    } else {
      infoTitle = '방전 사다리'
      infoDesc =
        '전기가 아래에서 위로 타고 오르며 번개가 올라가는 듯 보입니다. 뜨거운 공기가 전류를 위로 끌어올립니다.'
    }

    drawStage()
    drawInfoBox()
    drawTargets()
    drawDevice()

    if (isFiring) {
      drawSparks()
      if (p.millis() - fireStart > fireDuration) {
        isFiring = false
        p.noLoop()
      }
    }
  }

  /* -------- 배경/무대 -------- */
  function drawStage() {
    // 바닥
    p.push()
    p.noStroke()
    p.fill(240)
    p.rectMode(p.CORNER)
    p.rect(0, h * 0.8, w, h * 0.2)

    p.fill(209, 213, 219)
    p.rect(0, h * 0.78, w, h * 0.02)
    p.pop()
  }

  /* -------- 상단 인포 박스 (크게 확장) -------- */
  function drawInfoBox() {
    // 양 옆 여백을 조금만 두고 거의 전체 폭 사용
    const boxMarginX = w * 0.04
    const boxMarginY = h * 0.03
    const boxW = w - boxMarginX * 2    // ← 여기서 폭 넓게 사용
    const boxH = h * 0.18              // 높이도 살짝 여유 있게

    p.push()
    p.rectMode(p.CORNER)

    // 배경
    p.noStroke()
    p.fill(232, 244, 255)
    p.rect(boxMarginX, boxMarginY, boxW, boxH, 12)

    const textPaddingX = 14
    const textPaddingY = 10

    // 제목
    p.fill(37, 47, 63)
    p.textAlign(p.LEFT, p.TOP)
    p.textSize(13)
    p.text(
      infoTitle,
      boxMarginX + textPaddingX,
      boxMarginY + textPaddingY
    )

    // 설명
    p.fill(75, 85, 99)
    p.textSize(11)
    p.text(
      infoDesc,
      boxMarginX + textPaddingX,
      boxMarginY + textPaddingY + 18,
      boxW - textPaddingX * 2,   // 이 폭 안에서만 줄바꿈 → 더 안 잘림
      boxH - textPaddingY * 2
    )

    p.pop()
  }

  /* -------- 사람 / 피뢰침 -------- */
  function drawTargets() {
    // 학생 + 풍선
    p.push()
    p.rectMode(p.CENTER)
    p.ellipseMode(p.CENTER)

    p.fill(96, 165, 250)
    p.rect(targetLeft.x, targetLeft.y, w * 0.08, h * 0.16, 10)

    p.fill(248, 250, 252)
    p.ellipse(targetLeft.x, targetLeft.y - h * 0.11, w * 0.06, w * 0.06)

    p.stroke(55, 65, 81)
    p.strokeWeight(2)
    p.line(
      targetLeft.x + w * 0.03,
      targetLeft.y - h * 0.04,
      targetLeft.x + w * 0.08,
      targetLeft.y - h * 0.14
    )

    const balloonX = targetLeft.x + w * 0.11
    const balloonY = targetLeft.y - h * 0.2
    p.noStroke()
    p.fill(251, 191, 36)
    p.ellipse(balloonX, balloonY, w * 0.06, w * 0.08)
    p.triangle(
      balloonX,
      balloonY + w * 0.04,
      balloonX - w * 0.01,
      balloonY + w * 0.06,
      balloonX + w * 0.01,
      balloonY + w * 0.06
    )
    p.pop()

    // 피뢰침
    p.push()
    p.rectMode(p.CENTER)
    p.stroke(75, 85, 99)
    p.strokeWeight(3)
    p.line(targetRight.x, h * 0.4, targetRight.x, h * 0.78)
    p.line(targetRight.x - w * 0.04, h * 0.78, targetRight.x + w * 0.04, h * 0.78)

    p.noStroke()
    p.fill(251, 146, 60)
    const lx = targetRight.x
    const ly = h * 0.42
    p.beginShape()
    p.vertex(lx - w * 0.015, ly)
    p.vertex(lx + w * 0.005, ly + h * 0.03)
    p.vertex(lx - w * 0.005, ly + h * 0.03)
    p.vertex(lx + w * 0.015, ly + h * 0.07)
    p.vertex(lx - w * 0.005, ly + h * 0.04)
    p.vertex(lx + w * 0.005, ly + h * 0.04)
    p.endShape(p.CLOSE)
    p.pop()
  }

  /* -------- 장치 본체 -------- */
  function drawDevice() {
    if (device === 'tesla') drawTeslaCoil()
    else if (device === 'vandag') drawVanDeGraaff()
    else drawLadder()
  }

  function drawTeslaCoil() {
    const x = w * 0.5
    const baseY = h * 0.8

    p.push()
    p.rectMode(p.CENTER)
    p.noStroke()

    p.fill(156, 163, 175)
    p.rect(x, baseY - h * 0.08, w * 0.09, h * 0.12, 8)

    p.stroke(55, 65, 81)
    p.strokeWeight(2)
    for (let i = 0; i < 6; i++) {
      const yy = baseY - h * 0.13 + i * h * 0.02
      p.line(x - w * 0.035, yy, x + w * 0.035, yy)
    }

    p.noStroke()
    p.fill(209, 213, 219)
    p.ellipse(x, baseY - h * 0.19, w * 0.16, h * 0.05)
    p.pop()
  }

  function drawVanDeGraaff() {
    const x = w * 0.5
    const baseY = h * 0.8

    p.push()
    p.rectMode(p.CENTER)
    p.ellipseMode(p.CENTER)

    p.noStroke()
    p.fill(148, 163, 184)
    p.rect(x, baseY - h * 0.09, w * 0.06, h * 0.16, 6)

    p.fill(229, 231, 235)
    p.ellipse(x, baseY - h * 0.22, w * 0.16, w * 0.16)

    p.fill(243, 244, 246, 180)
    p.ellipse(x - w * 0.03, baseY - h * 0.235, w * 0.05, w * 0.04)
    p.pop()
  }

  function drawLadder() {
    const x = w * 0.5
    const topY = h * 0.26
    const bottomY = h * 0.78

    p.push()
    p.rectMode(p.CENTER)
    p.noFill()
    p.stroke(75, 85, 99)
    p.strokeWeight(4)

    p.line(x - w * 0.05, bottomY, x - w * 0.02, topY)
    p.line(x + w * 0.05, bottomY, x + w * 0.02, topY)
    p.line(x - w * 0.05, bottomY, x + w * 0.05, bottomY)
    p.pop()
  }

  /* -------- 스파크 -------- */
  function drawSparks() {
    let count = 6
    if (powerLevel === 2) count = 10
    if (powerLevel === 3) count = 14

    for (let i = 0; i < count; i++) {
      if (device === 'ladder') {
        drawLadderSpark()
      } else if (device === 'vandag') {
        drawSphereSpark()
      } else {
        drawCoilSpark()
      }
    }
  }

  function sparkStroke() {
    const baseAlpha = powerLevel === 1 ? 120 : powerLevel === 2 ? 180 : 230
    p.stroke(96, 165, 250, baseAlpha)
    p.strokeWeight(2 + powerLevel * 0.5)
  }

  function drawCoilSpark() {
    const startX = w * 0.5
    const startY = h * 0.61

    const biasToRod = 0.55 + powerLevel * 0.1
    const toRod = Math.random() < biasToRod
    const target = toRod ? targetRight : targetLeft
    const endX = target.x + p.random(-w * 0.02, w * 0.02)
    const endY = target.y - h * 0.18 + p.random(-h * 0.02, h * 0.02)

    drawJaggedLine(startX, startY, endX, endY)
  }

  function drawSphereSpark() {
    const centerX = w * 0.5
    const centerY = h * 0.58
    const radius = w * 0.08

    const angle = p.random(-p.PI * 0.6, -p.PI * 0.1)
    const startX = centerX + radius * Math.cos(angle)
    const startY = centerY + radius * Math.sin(angle)

    const length = w * (0.08 + powerLevel * 0.03)
    const endX = startX + length * Math.cos(angle + p.random(-0.3, 0.3))
    const endY = startY + length * Math.sin(angle + p.random(-0.2, 0.2))

    drawJaggedLine(startX, startY, endX, endY)
  }

  function drawLadderSpark() {
    const baseY = h * 0.74
    const topY = h * 0.32
    const progress = (p.millis() - fireStart) / fireDuration
    const yCenter = p.lerp(baseY, topY, p.constrain(progress * (0.8 + powerLevel * 0.1), 0, 1))

    const startX = w * 0.5 - w * 0.03
    const endX = w * 0.5 + w * 0.03
    const startY = yCenter + h * 0.02
    const endY = yCenter - h * 0.02

    drawJaggedLine(startX, startY, endX, endY)
  }

  function drawJaggedLine(x1, y1, x2, y2) {
    sparkStroke()
    const segments = 10
    p.noFill()
    p.beginShape()
    for (let i = 0; i <= segments; i++) {
      const t = i / segments
      let x = p.lerp(x1, x2, t)
      let y = p.lerp(y1, y2, t)

      const offset = (1 - Math.abs(0.5 - t) * 2) * (10 + powerLevel * 6)
      x += p.random(-offset, offset)
      y += p.random(-offset, offset)

      if (i === 0 || i === segments) {
        x = i === 0 ? x1 : x2
        y = i === 0 ? y1 : y2
      }

      p.vertex(x, y)
    }
    p.endShape()
  }

  /* -------- Vue에서 쓰는 API -------- */
  p.setDevice = (key) => {
    device = key || 'tesla'
    p.redraw()
  }

  p.setLevel = (lv) => {
    powerLevel = lv || 2
    p.redraw()
  }

  p.fireOnce = () => {
    isFiring = true
    fireStart = p.millis()
    p.loop()
  }

  p.resetSketch = () => {
    isFiring = false
    fireStart = 0
    device = 'tesla'
    powerLevel = 2
    p.noLoop()
    p.redraw()
  }

  function measureCanvasSize(hostEl) {
    const fallbackEl = hostEl || document.documentElement
    const maxW = fallbackEl.clientWidth || 600
    const w = Math.floor(maxW)
    const h = Math.floor(w * aspect)
    return { canvasW: w, canvasH: h }
  }
}

onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return
  p5Instance = new p5(sketch, canvasHostRef.value)

  if (p5Instance.setDevice) p5Instance.setDevice(currentDevice.value)
  if (p5Instance.setLevel) p5Instance.setLevel(level.value)
})

onBeforeUnmount(() => {
  try {
    p5Instance?.remove()
    p5Instance = null
  } catch (e) {
    console.error('Error removing p5 instance:', e)
  }
})
</script>

<style scoped>
.electric-sim {
  background: #f7f7f7;
  border-radius: 12px;
  padding: 16px;
}

/* 상단 카드 */
.sim-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.04);
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.icon-circle {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: #e0f2fe;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-circle .bi {
  font-size: 18px;
  color: #2563eb;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.header-title {
  font-size: 0.97rem;
  font-weight: 700;
  color: #111827;
}

.header-sub {
  font-size: 0.78rem;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.chip-btn {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-size: 0.78rem;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s ease, transform 0.05s ease;
}

.chip-btn.primary {
  background: #4f46e5;
  border-color: #4f46e5;
  color: #ffffff;
}

.chip-btn:active {
  transform: translateY(1px);
}

/* 컨트롤 카드 */
.sim-control-card {
  margin-top: 10px;
  margin-bottom: 12px;
  padding: 10px 12px 12px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.control-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.control-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
}

.control-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pill-btn {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  font-size: 0.78rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pill-btn.active {
  background: #4f46e5;
  border-color: #4f46e5;
  color: #ffffff;
  box-shadow: 0 2px 6px rgba(79, 70, 229, 0.35);
}

/* 전압 슬라이더 */
.voltage-row {
  margin-bottom: 0;
}

.slider-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.slider-wrap input[type='range'] {
  flex: 1;
}

.slider-level {
  min-width: 2rem;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
}

/* 캔버스 카드 */
.canvas-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.canvas-host {
  width: 100%;
  min-height: 340px;
  background: #ffffff;
}

.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  display: block;
}
</style>
