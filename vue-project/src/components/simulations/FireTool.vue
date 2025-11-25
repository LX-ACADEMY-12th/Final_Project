<template>
  <div class="fire-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-fire"></i>
        </div>
        <div class="header-text">
          <div class="header-title">불 지피기: 도구를 이용해 나무에 열을 모아볼까요?</div>
          <div class="header-note">
            돋보기, 마찰, 부싯돌을 이용해
            <span class="caption-accent">나무에 열을 모아 불을 붙이는 원리</span>를 실험해 보세요.
          </div>
        </div>
      </div>
    </div>

    <!-- 컨트롤 카드 -->
    <div class="sim-control-card">
      <div class="control-row tool-row">
        <span class="control-label">도구 선택</span>
        <div class="tool-buttons">
          <button
            v-for="t in tools"
            :key="t.id"
            class="tool-btn"
            :class="{ active: tool === t.id }"
            @click="selectTool(t.id)"
          >
            <span class="emoji">{{ t.emoji }}</span>
            <span class="name">{{ t.label }}</span>
          </button>
        </div>
      </div>

      <div class="control-row">
        <span class="control-label">도구 사용 세기</span>
        <div class="slider-wrap">
          <input
            type="range"
            min="1"
            max="3"
            step="1"
            v-model.number="power"
            @input="updatePower"
          />
          <span class="slider-level">{{ powerText }}</span>
        </div>
      </div>

      <div class="control-row action-row">
        <button class="action-btn secondary" @click="onReset">
          <i class="bi bi-arrow-repeat"></i>
          <span>초기화</span>
        </button>
        <button class="action-btn primary" @click="onStart">
          <i class="bi bi-play-fill"></i>
          <span>실험 시작</span>
        </button>
      </div>
    </div>

    <!-- 캔버스 -->
    <div class="canvas-card themed">
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

// 도구: 1 돋보기, 2 마찰, 3 부싯돌
const tool = ref(1)
const tools = [
  { id: 1, label: '돋보기', emoji: '🔍' },
  { id: 2, label: '마찰(막대)', emoji: '🪵' },
  { id: 3, label: '부싯돌', emoji: '🪨' }
]

// 힘(세기): 1 약하게, 2 보통, 3 아주 세게
const power = ref(2)
const powerText = computed(() => ['약하게', '보통', '아주 세게'][power.value - 1])

function selectTool (id) {
  tool.value = id
  if (p5Instance?.setTool) p5Instance.setTool(id)
}

function onReset () {
  power.value = 2
  tool.value = 1
  if (p5Instance?.resetSketch) p5Instance.resetSketch()
  if (p5Instance?.setTool) p5Instance.setTool(tool.value)
  if (p5Instance?.setPower) p5Instance.setPower(power.value)
}

function onStart () {
  if (!p5Instance?.startExperiment) return
  p5Instance.startExperiment()
}

function updatePower () {
  if (p5Instance?.setPower) p5Instance.setPower(power.value)
}

/* ===================== p5 스케치 ===================== */
const sketch = (p) => {
  let w = 430
  let h = 420

  // 장작 위치
  let groundY = 0
  let logWidth = 0
  let logHeight = 0

  // 상태값
  let currentTool = 1
  let powerLevel = 2
  let heat = 0        // 0~1
  let burning = false
  let running = false
  let frameCountLocal = 0

  const igniteThreshold = 0.7   // 이 이상이면 “완전 불붙음”
  const maxFrames = 220         // 그 전에 무조건 붙게

  p.setup = () => {
    const host = canvasHostRef.value
    const hostW = host?.clientWidth || 430
    w = Math.max(320, hostW)
    h = 420

    const c = p.createCanvas(w, h)
    c.parent(host)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))
    p.frameRate(60)

    initLayout()
    resetState()
  }

  p.windowResized = () => {
    const host = canvasHostRef.value
    if (!host) return
    const hostW = host.clientWidth || 430
    w = Math.max(320, hostW)
    h = 420
    p.resizeCanvas(w, h)
    initLayout()
  }

  function initLayout () {
    groundY = h * 0.72
    logWidth = w * 0.42
    logHeight = 26
  }

  function resetState () {
    heat = 0
    burning = false
    running = false
    frameCountLocal = 0
  }

  p.draw = () => {
    p.background(248, 249, 253)

    drawTitle()
    drawGround()
    drawLog()
    drawTool()
    drawHeatBar()
    drawStatusText()

    if (running) {
      stepExperiment()
      frameCountLocal++
    }
  }

  /* -------- 텍스트 제목 -------- */
  function drawTitle () {
    p.push()
    p.textAlign(p.CENTER, p.TOP)
    p.textSize(13)
    p.fill(55, 65, 81)
    p.text('도구로 나무에 열을 모아 불을 붙여 보세요.', w / 2, 10)
    p.pop()
  }

  /* -------- 바닥/배경 -------- */
  function drawGround () {
    p.push()
    const grad = p.drawingContext.createLinearGradient(0, 0, 0, h)
    grad.addColorStop(0, '#eef2ff')
    grad.addColorStop(0.5, '#f9fafb')
    grad.addColorStop(1, '#fef2f2')
    p.drawingContext.fillStyle = grad
    p.noStroke()
    p.rect(0, 40, w, h - 40)

    p.fill(229, 231, 235)
    p.rect(0, groundY + 10, w, h - groundY - 10)
    p.pop()
  }

  /* -------- 장작 + 불 -------- */
  function drawLog () {
    const x = w / 2
    const y = groundY

    p.push()
    p.rectMode(p.CENTER)
    p.noStroke()
    const coolColor = p.color('#92400e')
    const hotColor = p.color('#f97316')
    const lerpAmt = p.constrain(heat, 0, 1)
    const bodyColor = p.lerpColor(coolColor, hotColor, lerpAmt)
    p.fill(bodyColor)
    p.rect(x, y, logWidth, logHeight, 12)

    p.push()
    p.translate(x, y - 18)
    p.rotate(-0.25)
    p.fill(120, 53, 15)
    p.rect(0, 0, logWidth * 0.6, logHeight * 0.7, 10)
    p.pop()

    p.push()
    p.translate(x, y - 22)
    p.rotate(0.32)
    p.fill(133, 77, 14)
    p.rect(0, 0, logWidth * 0.6, logHeight * 0.7, 10)
    p.pop()

    // 🔥 불 그리기 (heat 값만 올라가도 보이게)
    if (burning) {
      drawFlame(x, y - 40, 1)
    } else if (heat > 0.25) {
      drawFlame(x, y - 40, heat) // 열이 쌓이면 바로 작은 불
    }

    p.pop()
  }

  function drawFlame (cx, cy, intensity = 1) {
    p.push()
    p.noStroke()
    const t = p.frameCount * 0.12
    const baseScale = 0.6 + 0.7 * p.constrain(intensity, 0, 1)

    for (let i = 0; i < 3; i++) {
      const scale = baseScale * (1 - i * 0.18)
      const flicker = 4 * Math.sin(t + i)

      // 바깥 불꽃
      p.fill(248, 113, 113, 200 - i * 40)
      p.beginShape()
      p.vertex(cx, cy - 12 * scale + flicker)
      p.bezierVertex(
        cx + 22 * scale, cy - 6 * scale,
        cx + 16 * scale, cy + 26 * scale,
        cx,             cy + 28 * scale
      )
      p.bezierVertex(
        cx - 16 * scale, cy + 26 * scale,
        cx - 22 * scale, cy - 6 * scale,
        cx,              cy - 12 * scale + flicker
      )
      p.endShape(p.CLOSE)

      // 안쪽 불꽃
      p.fill(251, 191, 36, 220 - i * 60)
      p.beginShape()
      p.vertex(cx, cy - 4 * scale + flicker * 0.6)
      p.bezierVertex(
        cx + 14 * scale, cy,
        cx + 8 * scale,  cy + 18 * scale,
        cx,              cy + 18 * scale
      )
      p.bezierVertex(
        cx - 8 * scale,  cy + 18 * scale,
        cx - 14 * scale, cy,
        cx,              cy - 4 * scale + flicker * 0.6
      )
      p.endShape(p.CLOSE)

      cy -= 6
    }
    p.pop()
  }

  /* -------- 도구 그림 -------- */
  function drawTool () {
    const x = w * 0.22
    const y = groundY - 40

    p.push()
    p.translate(x, y)

    if (currentTool === 1) {
      // 돋보기
      p.noFill()
      p.stroke('#1d4ed8')
      p.strokeWeight(3)
      p.ellipse(0, 0, 46, 46)
      p.line(16, 16, 32, 32)

      // 햇빛 + 태양
      p.stroke('#facc15')
      p.strokeWeight(2)
      for (let i = -1; i <= 1; i++) {
        p.line(10 + i * 2, -20, w * 0.1, groundY - y - 10)
      }
      p.noStroke()
      p.fill('#fde68a')
      p.circle(-26, -26, 18)
    } else if (currentTool === 2) {
      // 마찰 막대
      p.noStroke()
      p.fill('#a16207')
      p.rect(-12, -8, 64, 14, 8)

      // 왕복 움직이는 손
      const t = (p.frameCount % 40) / 40
      const shift = p.map(Math.sin(t * p.TWO_PI), -1, 1, -10, 10)
      p.fill('#e5e7eb')
      p.rect(shift - 18, -22, 38, 12, 6)
    } else {
      // 부싯돌 두 개
      p.noStroke()
      p.fill('#4b5563')
      p.quad(-16, 0, 4, -18, 18, -4, -2, 16)
      p.fill('#6b7280')
      p.quad(10, -4, 28, -16, 36, 2, 18, 10)

      // 튀는 불꽃 (항상 약간 보이게)
      p.fill('#facc15')
      for (let i = 0; i < 6; i++) {
        const ang = -p.QUARTER_PI + (i - 2.5) * 0.18
        const dist = 18 + i * 2
        const fx = Math.cos(ang) * dist
        const fy = Math.sin(ang) * dist
        p.circle(fx, fy, 3)
      }
    }

    p.pop()
  }

  /* -------- 열(온도) 표시 바 -------- */
  function drawHeatBar () {
    const boxW = w * 0.86
    const boxH = 70
    const boxX = (w - boxW) / 2
    const boxY = groundY + 20

    p.push()
    p.rectMode(p.CORNER)

    p.noStroke()
    p.fill(248, 250, 252)
    p.rect(boxX, boxY, boxW, boxH, 14)

    const innerX = boxX + 16
    const innerY = boxY + 12
    const innerW = boxW - 32

    p.textAlign(p.LEFT, p.TOP)
    p.textSize(11)
    p.fill('#4b5563')
    p.text('나무에 모인 열', innerX, innerY)

    const barY = innerY + 20
    const barH = 10

    p.fill('#e5e7eb')
    p.rect(innerX, barY, innerW, barH, 999)

    const grd = p.drawingContext.createLinearGradient(innerX, barY, innerX + innerW, barY)
    grd.addColorStop(0, '#60a5fa')
    grd.addColorStop(0.5, '#facc15')
    grd.addColorStop(1, '#f97316')
    p.drawingContext.fillStyle = grd
    p.rect(innerX, barY, innerW * p.constrain(heat, 0, 1), barH, 999)

    const thresholdX = innerX + innerW * igniteThreshold
    p.stroke('#f97316')
    p.strokeWeight(2)
    p.line(thresholdX, barY - 4, thresholdX, barY + barH + 4)
    p.noStroke()
    p.fill('#f97316')
    p.textSize(10)
    p.text('불이 붙는 온도', thresholdX - 36, barY + barH + 8)

    p.pop()
  }

  /* -------- 상태 텍스트 -------- */
  function drawStatusText () {
    const boxW = w * 0.86
    const boxH = 46
    const boxX = (w - boxW) / 2
    const boxY = 52

    let sentence = ''

    if (!running && !burning && frameCountLocal === 0) {
      sentence = '도구와 세기를 정한 뒤 [실험 시작]을 눌러 보세요.'
    } else if (burning) {
      sentence = '충분한 열이 모여 나무에 불이 붙었어요!'
    } else if (!running && frameCountLocal > 0 && heat < 0.3) {
      sentence = '열이 충분히 모이지 않아 불이 붙지 않았어요.'
    } else if (!running && frameCountLocal > 0 && heat >= 0.3 && heat < igniteThreshold) {
      sentence = '나무가 많이 뜨거워졌지만 아직 불꽃은 생기지 않았어요.'
    } else {
      if (currentTool === 1) {
        sentence = '돋보기로 햇빛을 한 점에 모을수록 열이 더 잘 모여요.'
      } else if (currentTool === 2) {
        sentence = '막대를 빠르게 문지를수록 마찰열이 더 많이 생겨요.'
      } else {
        sentence = '부싯돌을 세게 치면 순간적으로 강한 열과 불꽃이 생겨요.'
      }
    }

    p.push()
    p.rectMode(p.CORNER)
    p.noStroke()
    p.fill(255, 255, 255)
    p.rect(boxX, boxY, boxW, boxH, 14)

    p.textAlign(p.CENTER, p.CENTER)
    p.textSize(11)
    p.fill('#4b5563')
    p.text(sentence, boxX + boxW / 2, boxY + boxH / 2)
    p.pop()
  }

  /* -------- 실험 진행 로직 (프레임당 증가) -------- */
  function stepExperiment () {
    // 도구별 기본 증가량 (프레임당)
    let base = 0.006     // 돋보기
    if (currentTool === 2) base = 0.008   // 마찰
    if (currentTool === 3) base = 0.012   // 부싯돌

    const powerFactor = [0, 0.7, 1, 1.3][powerLevel]
    heat += base * powerFactor
    heat -= 0.001        // 살짝 식기
    heat = p.constrain(heat, 0, 1.05)

    if (heat >= igniteThreshold || frameCountLocal > maxFrames) {
      burning = true
      running = false
    }
  }

  /* ===== Vue에서 호출하는 메서드 ===== */
  p.setTool = (id) => {
    currentTool = id || 1
  }

  p.setPower = (lv) => {
    powerLevel = lv || 2
  }

  p.startExperiment = () => {
    resetState()
    running = true
  }

  p.resetSketch = () => {
    resetState()
  }
}

onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return

  p5Instance = new p5(sketch, canvasHostRef.value)

  if (p5Instance.setTool) p5Instance.setTool(tool.value)
  if (p5Instance.setPower) p5Instance.setPower(power.value)
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
.fire-sim {
  background: #f7f8fc;
  border-radius: 12px;
  padding: 14px;
  font-family: 'SUIT', sans-serif;
}

/* 헤더 카드 */
.sim-header-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fbfcff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.04);
  margin-bottom: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fee2e2 0%, #ffedd5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(248, 113, 113, 0.25);
}

.icon-circle .bi {
  font-size: 20px;
  color: #f97316;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 0.98rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.4;
}

.header-note {
  font-size: 0.86rem;
  color: #4b5563;
  font-weight: 500;
  line-height: 1.4;
  margin-top: 2px;
}

.caption-accent {
  color: #2563eb;
  font-weight: 600;
  letter-spacing: -0.02em;
}

/* 컨트롤 카드 */
.sim-control-card {
  margin-top: 8px;
  margin-bottom: 10px;
  padding: 10px 14px 12px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(148, 163, 184, 0.16);
}

.control-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.tool-row {
  margin-bottom: 12px;
}

.control-label {
  font-size: 0.84rem;
  font-weight: 600;
  color: #374151;
}

/* 도구 버튼 */
.tool-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tool-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-size: 0.8rem;
  font-weight: 500;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.15s ease;
}

.tool-btn .emoji {
  font-size: 0.9rem;
}

.tool-btn.active {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: #3b82f6;
}

.tool-btn:hover {
  background: #f3f4f6;
}

/* 슬라이더 */
.slider-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.slider-wrap input[type='range'] {
  flex: 1;
}

.slider-level {
  min-width: 3.8rem;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  background: #f3f4f6;
  padding: 5px 9px;
  border-radius: 8px;
}

/* 실행 버튼 */
.action-row {
  flex-direction: row;
  justify-content: flex-end;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 600;
  padding: 8px 14px;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(148, 163, 184, 0.26);
  white-space: nowrap;
  background: #f3f4f6;
  color: #374151;
}

.action-btn.secondary:hover {
  background: #e5e7eb;
}

.action-btn.primary {
  background: linear-gradient(135deg, #4f46e5 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 3px 12px rgba(37, 99, 235, 0.3);
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, #4338ca 0%, #1d4ed8 100%);
}

/* 캔버스 카드 */
.canvas-card.themed {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(148, 163, 184, 0.24);
}

.canvas-host {
  width: 100%;
  min-height: 360px;
  background: #ffffff;
}

.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  display: block;
}

/* 반응형 */
@media (max-width: 768px) {
  .sim-header-card {
    flex-direction: column;
    align-items: stretch;
  }
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
  .action-btn {
    width: 100%;
  }
}
</style>
