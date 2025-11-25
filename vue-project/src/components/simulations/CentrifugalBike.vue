<template>
  <div class="bike-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-bicycle"></i>
        </div>
        <div class="header-text">
          <div class="header-title">원형 레일 자전거: 힘에 따라 얼마나 올라갈까?</div>
          <div class="header-note">
            페달을 얼마나 세게 밟는지에 따라
            <span class="caption-accent">자전거가 올라가는 높이</span>가 달라져요.
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button class="action-btn secondary" @click="onReset">
          <i class="bi bi-arrow-repeat"></i>
          <span>초기화</span>
        </button>
        <button class="action-btn primary" @click="onRun">
          <i class="bi bi-play-fill"></i>
          <span>자전거 타기</span>
        </button>
      </div>
    </div>

    <!-- 컨트롤 -->
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">페달 힘</span>
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

const power = ref(2) // 1: 약, 2: 보통, 3: 강
const powerText = computed(() => (['약하게', '보통', '아주 세게'][power.value - 1]))

function onReset () {
  power.value = 2
  if (p5Instance?.setPower) p5Instance.setPower(power.value)
  if (p5Instance?.resetSketch) p5Instance.resetSketch()
}

function onRun () {
  if (p5Instance?.startRide) p5Instance.startRide()
}

function updatePower () {
  if (p5Instance?.setPower) p5Instance.setPower(power.value)
}

/* ===================== p5 스케치 ===================== */
const sketch = (p) => {
  let w = 430
  let h = 380

  // 트랙
  let cx = 0
  let cy = 0
  let radius = 0

  // 자전거 상태
  let angle = 0 // 라디안, 아래쪽에서 시작
  let angVel = 0
  let riding = false
  let elapsed = 0
  const rideDuration = 3200 // ms

  // 에너지 느낌 값 (0~1)
  let speedEnergy = 0.5
  let heightEnergy = 0.2

  // 레벨에 따른 설정
  let powerLevel = 2 // 1,2,3

  p.setup = () => {
    const host = canvasHostRef.value
    const hostW = host?.clientWidth || 430
    w = Math.max(320, hostW)
    h = 380

    const c = p.createCanvas(w, h)
    c.parent(host)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    initLayout()
    resetState()
    p.noLoop()
    p.redraw()
  }

  p.windowResized = () => {
    const host = canvasHostRef.value
    if (!host) return
    const hostW = host.clientWidth || 430
    w = Math.max(320, hostW)
    h = 380
    p.resizeCanvas(w, h)
    initLayout()
    resetState()
    p.redraw()
  }

  function initLayout () {
    cx = w / 2
    cy = h * 0.38
    radius = Math.min(w, h) * 0.22
  }

  function resetState () {
    angle = p.HALF_PI // 아래쪽 (90도)
    angVel = 0
    elapsed = 0
    riding = false

    // 기본 에너지 느낌값
    updateEnergyFromAngle()
  }

  p.draw = () => {
    p.background(247, 248, 252)

    drawTitle()
    drawTrack()
    drawBike()
    drawEnergyBars()
    drawStatusText()

    if (riding) {
      stepRide()
      if (elapsed > rideDuration) {
        riding = false
        p.noLoop()
      }
    } else {
      p.noLoop()
    }
  }

  /* -------- 상단 타이틀 -------- */
  function drawTitle () {
    p.push()
    p.textAlign(p.CENTER, p.TOP)
    p.textSize(13)
    p.fill(55, 65, 81)
    p.text('페달 힘에 따라 자전거가 올라가는 높이가 달라져요', w / 2, 10)
    p.pop()
  }

  /* -------- 트랙 -------- */
  function drawTrack () {
    p.push()

    // 트랙 바탕 그림자
    p.noStroke()
    p.fill(209, 213, 219, 70)
    p.ellipse(cx, cy + 6, radius * 2.05, radius * 2.05)

    // 트랙 테두리
    p.noFill()
    p.stroke('#dbeafe')
    p.strokeWeight(8)
    p.ellipse(cx, cy, radius * 2, radius * 2)

    // 안쪽 가이드 라인
    p.stroke('#e5e7eb')
    p.strokeWeight(1.5)
    p.ellipse(cx, cy, radius * 1.6, radius * 1.6)

    // 바닥선
    p.stroke('#e5e7eb')
    p.strokeWeight(2)
    p.line(cx - radius * 1.2, cy + radius + 16, cx + radius * 1.2, cy + radius + 16)

    p.pop()
  }

  /* -------- 자전거(공) -------- */
  function drawBike () {
    // 트랙 위 위치 계산
    const x = cx + radius * Math.cos(angle)
    const y = cy + radius * Math.sin(angle)
    const r = Math.min(w, h) * 0.035

    // 그림자
    p.push()
    p.noStroke()
    p.fill(15, 23, 42, 40)
    p.ellipse(x, y + r * 0.6, r * 1.4, r * 0.7)
    p.pop()

    // 자전거 바디(공)
    p.push()
    p.noStroke()
    const grad = p.drawingContext.createRadialGradient(
      x - r * 0.4,
      y - r * 0.4,
      r * 0.2,
      x,
      y,
      r
    )
    grad.addColorStop(0, '#4f46e5')
    grad.addColorStop(1, '#2563eb')
    p.drawingContext.fillStyle = grad
    p.ellipse(x, y, r * 2, r * 2)

    // 하이라이트
    p.fill(255, 255, 255, 130)
    p.ellipse(x - r * 0.3, y - r * 0.4, r, r * 0.7)
    p.pop()

    // 간단한 "사람" 실루엣
    p.push()
    p.stroke(248, 250, 252)
    p.strokeWeight(2)
    p.noFill()
    p.arc(x, y - r * 0.4, r * 1.2, r * 1.2, p.PI * 1.2, p.PI * 1.9)
    p.pop()
  }

  /* -------- 에너지 바 -------- */
  function drawEnergyBars () {
    const boxW = w * 0.86
    const boxH = 70
    const boxX = (w - boxW) / 2
    const boxY = cy + radius + 26

    p.push()
    p.rectMode(p.CORNER)

    // 바탕 박스
    p.noStroke()
    p.fill(248, 250, 252)
    p.rect(boxX, boxY, boxW, boxH, 14)

    const innerX = boxX + 14
    const innerY = boxY + 10
    const innerW = boxW - 28

    p.textAlign(p.LEFT, p.TOP)
    p.textSize(11)
    p.fill('#4b5563')
    p.text('에너지 변화 보기', innerX, innerY)

    const barY1 = innerY + 18
    const barY2 = innerY + 38
    const barH = 8

    // 속도 에너지 바
    p.fill('#e5edff')
    p.rect(innerX, barY1, innerW, barH, 999)
    p.fill('#4f46e5')
    p.rect(innerX, barY1, innerW * speedEnergy, barH, 999)

    p.fill('#6b7280')
    p.text('속도 에너지', innerX + 2, barY1 - 12)

    // 높이 에너지 바
    p.fill('#fee2e2')
    p.rect(innerX, barY2, innerW, barH, 999)
    p.fill('#f97316')
    p.rect(innerX, barY2, innerW * heightEnergy, barH, 999)

    p.fill('#6b7280')
    p.text('높이 에너지', innerX + 2, barY2 - 12)

    p.pop()
  }

  /* -------- 아래 상태 텍스트 -------- */
  function drawStatusText () {
    const boxW = w * 0.86
    const boxH = 46
    const boxX = (w - boxW) / 2
    const boxY = h - boxH - 10

    let sentence = ''
    const topReached = angle < 0 || angle > p.TWO_PI // 거의 위쪽

    if (!riding && elapsed === 0) {
      sentence = '페달 힘을 정하고 [자전거 타기] 버튼을 눌러 보세요.'
    } else {
      if (heightEnergy > 0.8 && speedEnergy < 0.4) {
        sentence = '거의 꼭대기까지 올라갔다가 천천히 내려와요.'
      } else if (topReached && powerLevel === 3) {
        sentence = '아주 세게 밟아서 한 바퀴를 돕니다!'
      } else if (heightEnergy < 0.4) {
        sentence = '힘이 약해서 조금만 올라갔다가 다시 내려와요.'
      } else {
        sentence = '속도가 빠를수록 더 높은 곳까지 올라갑니다.'
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

  /* -------- 움직임 업데이트 -------- */
  function stepRide () {
    const dt = p.deltaTime || 16
    elapsed += dt

    // 힘에 따른 기본 속도 / 감쇠
    let baseSpeed = 0.035
    let decay = 0.994

    if (powerLevel === 1) {
      baseSpeed = 0.025
      decay = 0.992
    } else if (powerLevel === 3) {
      baseSpeed = 0.05
      decay = 0.996
    }

    // 처음에는 한 번 퍽! 밀어줌
    if (elapsed < 200) {
      angVel = baseSpeed
    } else {
      // "위로 올라갈수록 느려지고, 내려오면 빨라지는" 느낌만 추가
      const heightFactor = (1 - Math.sin(angle)) * 0.35 // 위로 갈수록 값 커짐
      angVel += -heightFactor * 0.0008
    }

    // 약한 힘일 때는 일정 각도 이상 못 올라가게
    if (powerLevel === 1) {
      const maxAngle = p.PI * 1.1 // 약간 오른쪽 위까지
      if (angle < maxAngle && angVel < 0) angVel *= -0.4 // 다시 아래로
    } else if (powerLevel === 2) {
      const maxAngle = p.PI * 1.45 // 거의 위근처
      if (angle < maxAngle && angVel < 0) angVel *= -0.35
    } else {
      // 3단계는 한 바퀴를 거의 돌 수 있게
      if (elapsed > rideDuration * 0.7) {
        angVel *= 0.97
      }
    }

    angVel *= decay
    angle += angVel

    // 각도 범위 정리
    if (angle > p.TWO_PI + p.HALF_PI) angle -= p.TWO_PI
    if (angle < -p.HALF_PI) angle += p.TWO_PI

    updateEnergyFromAngle()
  }

  function updateEnergyFromAngle () {
    // 높이 에너지: 위로 갈수록 커짐 (0~1 근사)
    // 아래(90도) -> 0, 위(270도) -> 1
    const norm = (Math.cos(angle) * -1 + 1) / 2
    heightEnergy = p.constrain(norm, 0, 1)

    // 속도 에너지: "전체 에너지 - 위치 에너지" 느낌으로
    let totalE = 0.6
    if (powerLevel === 1) totalE = 0.5
    if (powerLevel === 3) totalE = 0.9

    speedEnergy = p.constrain(totalE - heightEnergy * 0.7, 0.05, 1)
  }

  /* ===== Vue에서 사용하는 메서드 ===== */
  p.setPower = (lv) => {
    powerLevel = lv || 2
    // 에너지 느낌값도 살짝 조정
    updateEnergyFromAngle()
    if (!riding) p.redraw()
  }

  p.startRide = () => {
    resetState()
    riding = true
    p.loop()
  }

  p.resetSketch = () => {
    resetState()
    p.noLoop()
    p.redraw()
  }
}

onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return

  p5Instance = new p5(sketch, canvasHostRef.value)

  if (p5Instance.setPower) {
    p5Instance.setPower(power.value)
  }
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
.bike-sim {
  background: #f7f8fc;
  border-radius: 12px;
  padding: 14px;
  font-family: 'SUIT', sans-serif;
}

/* 헤더 카드 */
.sim-header-card {
  display: flex;
  justify-content: space-between;
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
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(59, 130, 246, 0.18);
}

.icon-circle .bi {
  font-size: 20px;
  color: #2563eb;
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
  color: #4b6fb1;
  font-weight: 500;
  line-height: 1.4;
  margin-top: 2px;
}

.caption-accent {
  color: #2563eb;
  font-weight: 600;
  letter-spacing: -0.02em;
}

/* 버튼 영역 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 9px 14px;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(148, 163, 184, 0.26);
  white-space: nowrap;
  background: #f3f4f6;
  color: #374151;
}

.action-btn .bi {
  font-size: 1.05rem;
}

.action-btn.secondary:hover {
  background: #e5e7eb;
  color: #1f2937;
}

.action-btn.primary {
  background: linear-gradient(135deg, #4f46e5 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 3px 12px rgba(37, 99, 235, 0.3);
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, #4338ca 0%, #1d4ed8 100%);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.35);
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
  margin-bottom: 6px;
}

.control-label {
  font-size: 0.84rem;
  font-weight: 600;
  color: #374151;
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
  min-width: 3.8rem;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  background: #f3f4f6;
  padding: 5px 9px;
  border-radius: 8px;
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
  min-height: 340px;
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
  .header-actions {
    justify-content: stretch;
  }
  .action-btn {
    flex: 1;
    padding: 9px 10px;
    font-size: 0.86rem;
  }
}
</style>
