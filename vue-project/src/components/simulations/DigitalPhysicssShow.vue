<template>
  <div class="force-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-record-circle"></i>
        </div>
        <div class="header-text">
          <div class="header-title">디지털 물리쇼: 힘을 모아 공을 움직여요</div>
          <div class="header-note">
            위·아래·왼쪽·오른쪽에서 미는 힘의 크기를 바꾸면
            <span class="caption-accent">공이 어느 방향으로 움직이는지</span> 확인해 보세요.
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
          <span>공 움직이기</span>
        </button>
      </div>
    </div>

    <!-- 슬라이더 컨트롤 -->
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">위쪽에서 미는 힘</span>
        <div class="slider-wrap">
          <input
            type="range"
            min="0"
            max="3"
            v-model.number="forceN"
            @input="updateForces"
          />
          <span class="slider-level">{{ forceTexts[forceN] }}</span>
        </div>
      </div>

      <div class="control-row">
        <span class="control-label">아래쪽에서 미는 힘</span>
        <div class="slider-wrap">
          <input
            type="range"
            min="0"
            max="3"
            v-model.number="forceS"
            @input="updateForces"
          />
          <span class="slider-level">{{ forceTexts[forceS] }}</span>
        </div>
      </div>

      <div class="control-row">
        <span class="control-label">왼쪽에서 미는 힘</span>
        <div class="slider-wrap">
          <input
            type="range"
            min="0"
            max="3"
            v-model.number="forceL"
            @input="updateForces"
          />
          <span class="slider-level">{{ forceTexts[forceL] }}</span>
        </div>
      </div>

      <div class="control-row">
        <span class="control-label">오른쪽에서 미는 힘</span>
        <div class="slider-wrap">
          <input
            type="range"
            min="0"
            max="3"
            v-model.number="forceR"
            @input="updateForces"
          />
          <span class="slider-level">{{ forceTexts[forceR] }}</span>
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
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import p5 from 'p5'

const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

// 0: 없음, 1: 약하게, 2: 보통, 3: 세게
const forceTexts = ['없음', '약하게', '보통', '세게']

const forceN = ref(0)
const forceS = ref(0)
const forceL = ref(0)
const forceR = ref(2) // 기본: 오른쪽 보통

function onReset () {
  forceN.value = 0
  forceS.value = 0
  forceL.value = 0
  forceR.value = 2
  updateForces()
  if (p5Instance?.resetSketch) p5Instance.resetSketch()
}

function onRun () {
  if (p5Instance?.startRun) p5Instance.startRun()
}

function updateForces () {
  if (p5Instance?.setForces) {
    p5Instance.setForces(
      forceN.value,
      forceS.value,
      forceL.value,
      forceR.value
    )
  }
}

/* ===================== p5 스케치 ===================== */
const sketch = (p) => {
  let w = 430
  let h = 360

  // 운동장(보드) 영역
  const board = { left: 0, right: 0, top: 0, bottom: 0 }

  // 힘 값
  const forces = { n: 0, s: 0, l: 0, r: 2 }
  let netFx = forces.r - forces.l
  let netFy = forces.s - forces.n

  // 공 상태
  let ballX = 0
  let ballY = 0
  let velX = 0
  let velY = 0

  let running = false
  let elapsed = 0
  const totalTime = 2600 // ms

  p.setup = () => {
    const host = canvasHostRef.value
    const hostW = host?.clientWidth || 430
    w = Math.max(320, hostW)
    h = 360

    const c = p.createCanvas(w, h)
    c.parent(host)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    initLayout()
    resetBall()
    p.noLoop()
    p.redraw()
  }

  p.windowResized = () => {
    const host = canvasHostRef.value
    if (!host) return
    const hostW = host.clientWidth || 430
    w = Math.max(320, hostW)
    h = 360
    p.resizeCanvas(w, h)
    initLayout()
    resetBall()
    p.redraw()
  }

  function initLayout () {
    const boardW = w * 0.58
    const boardH = h * 0.3
    const cx = w / 2
    const cy = h * 0.42

    board.left = cx - boardW / 2
    board.right = cx + boardW / 2
    board.top = cy - boardH / 2
    board.bottom = cy + boardH / 2
  }

  function resetBall () {
    ballX = (board.left + board.right) / 2
    ballY = (board.top + board.bottom) / 2
    velX = 0
    velY = 0
    elapsed = 0
  }

  p.draw = () => {
    p.background(247, 248, 252)

    drawTitle()
    drawBoard()
    drawBall()
    drawNetArrow()
    drawForcePills()
    drawBottomText()

    if (running) {
      stepPhysics()
      if (elapsed > totalTime) {
        running = false
        p.noLoop()
      }
    } else {
      p.noLoop()
    }
  }

  /* -------- 상단 제목 -------- */
  function drawTitle () {
    p.push()
    p.textAlign(p.CENTER, p.TOP)
    p.textSize(13)
    p.fill(55, 65, 81)
    p.text('네 방향의 힘과 공의 운동', w / 2, 14)
    p.pop()
  }

  /* -------- 운동장(보드) -------- */
  function drawBoard () {
    p.push()
    p.rectMode(p.CORNERS)

    // 바탕
    p.noStroke()
    p.fill(255)
    p.rect(board.left, board.top, board.right, board.bottom, 18)

    // 연한 격자
    const gridGap = 20
    p.stroke(226, 232, 240)
    p.strokeWeight(1)
    for (let x = board.left + gridGap; x < board.right; x += gridGap) {
      p.line(x, board.top + 8, x, board.bottom - 8)
    }
    for (let y = board.top + gridGap; y < board.bottom; y += gridGap) {
      p.line(board.left + 8, y, board.right - 8, y)
    }

    // 보더
    p.noFill()
    p.stroke('#dbeafe')
    p.strokeWeight(2)
    p.rect(board.left, board.top, board.right, board.bottom, 18)
    p.pop()
  }

  /* -------- 공 -------- */
  function drawBall () {
    const radius = Math.min(w, h) * 0.04

    // 그림자
    p.push()
    p.noStroke()
    p.fill(15, 23, 42, 26)
    p.ellipse(ballX, ballY + radius * 0.45, radius * 1.3, radius * 0.6)
    p.pop()

    // 공
    p.push()
    p.noStroke()
    const grad = p.drawingContext.createRadialGradient(
      ballX - radius * 0.3,
      ballY - radius * 0.3,
      radius * 0.2,
      ballX,
      ballY,
      radius
    )
    grad.addColorStop(0, '#4f46e5')
    grad.addColorStop(1, '#2563eb')
    p.drawingContext.fillStyle = grad
    p.ellipse(ballX, ballY, radius * 2, radius * 2)

    p.fill(255, 255, 255, 130)
    p.ellipse(ballX - radius * 0.35, ballY - radius * 0.4, radius, radius * 0.7)
    p.pop()
  }

  /* -------- 합력 화살표 (보드 안, 중앙에서 하나만) -------- */
  function drawNetArrow () {
    const cx = (board.left + board.right) / 2
    const cy = (board.top + board.bottom) / 2

    const mag = Math.sqrt(netFx * netFx + netFy * netFy)
    if (mag < 0.1) {
      // 거의 0이면 동그라미만
      p.push()
      p.noFill()
      p.stroke('#cbd5f5')
      p.strokeWeight(2)
      p.ellipse(cx, cy, 20, 20)
      p.pop()
      return
    }

    const maxLen = Math.min(board.right - board.left, board.bottom - board.top) * 0.25
    const scale = maxLen / 4 // 최대 힘 4 기준
    const dx = netFx * scale
    const dy = netFy * scale

    drawArrow(cx, cy, cx + dx, cy + dy, '#4f46e5')
  }

  /* -------- 힘 pill 라벨 (보드 주변) -------- */
  function drawForcePills () {
    const cx = (board.left + board.right) / 2
    const cy = (board.top + board.bottom) / 2

    const pillH = 20
    const gapY = 10
    const gapX = 10

    // 위
    drawPill(
      cx,
      board.top - pillH - gapY,
      `위: ${forceTexts[forces.n]}`,
      '#fee2e2',
      '#b91c1c'
    )

    // 아래
    drawPill(
      cx,
      board.bottom + pillH + gapY,
      `아래: ${forceTexts[forces.s]}`,
      '#dbeafe',
      '#1d4ed8'
    )

    // 왼쪽
    drawPill(
      board.left - 60 - gapX,
      cy,
      `왼쪽: ${forceTexts[forces.l]}`,
      '#dcfce7',
      '#166534'
    )

    // 오른쪽
    drawPill(
      board.right + 60 + gapX,
      cy,
      `오른쪽: ${forceTexts[forces.r]}`,
      '#fef9c3',
      '#92400e'
    )
  }

  function drawPill (x, y, label, bgColor, textColor) {
    p.push()
    p.rectMode(p.CENTER)
    p.textAlign(p.CENTER, p.CENTER)
    p.textSize(10)

    const paddingX = 12
    const paddingY = 5

    const textW = p.textWidth(label)
    const wBox = textW + paddingX * 2
    const hBox = paddingY * 2 + 10

    p.noStroke()
    p.fill(bgColor)
    p.rect(x, y, wBox, hBox, hBox)
    p.fill(textColor)
    p.text(label, x, y + 1)
    p.pop()
  }

  /* -------- 아래 설명 텍스트 -------- */
  function drawBottomText () {
    const boxW = w * 0.86
    const boxH = 52
    const boxX = (w - boxW) / 2
    const boxY = board.bottom + 52

    const mag = Math.sqrt(netFx * netFx + netFy * netFy)
    let sentence = ''

    if (mag < 0.1) {
      sentence = '네 방향의 힘이 서로 같아서 공이 그대로 있어요. (힘의 평형)'
    } else {
      let dir = ''
      if (Math.abs(netFx) >= Math.abs(netFy)) {
        dir = netFx > 0 ? '오른쪽' : '왼쪽'
      } else {
        dir = netFy > 0 ? '아래쪽' : '위쪽'
      }
      sentence = `합쳐진 힘 때문에 공이 주로 ${dir}으로 움직여요.`
    }

    p.push()
    p.rectMode(p.CORNER)
    p.noStroke()
    p.fill(248, 250, 252)
    p.rect(boxX, boxY, boxW, boxH, 14)

    p.textAlign(p.CENTER, p.CENTER)
    p.textSize(11)
    p.fill('#4b5563')
    p.text(sentence, boxX + boxW / 2, boxY + boxH / 2)
    p.pop()
  }

  function drawArrow (x1, y1, x2, y2, color) {
    p.push()
    p.stroke(color)
    p.fill(color)
    p.strokeWeight(2.5)
    p.line(x1, y1, x2, y2)

    const angle = Math.atan2(y2 - y1, x2 - x1)
    const headSize = 7
    p.translate(x2, y2)
    p.rotate(angle)
    p.beginShape()
    p.vertex(0, 0)
    p.vertex(-headSize, headSize * 0.7)
    p.vertex(-headSize, -headSize * 0.7)
    p.endShape(p.CLOSE)
    p.pop()
  }

  /* -------- 물리 업데이트 -------- */
  function stepPhysics () {
    const dt = p.deltaTime || 16
    elapsed += dt

    const accScale = 0.004
    const ax = netFx * accScale
    const ay = netFy * accScale

    velX += ax * (dt / 16)
    velY += ay * (dt / 16)

    velX *= 0.99
    velY *= 0.99

    ballX += velX
    ballY += velY

    const radius = Math.min(w, h) * 0.04
    if (ballX < board.left + radius) {
      ballX = board.left + radius
      velX = 0
    }
    if (ballX > board.right - radius) {
      ballX = board.right - radius
      velX = 0
    }
    if (ballY < board.top + radius) {
      ballY = board.top + radius
      velY = 0
    }
    if (ballY > board.bottom - radius) {
      ballY = board.bottom - radius
      velY = 0
    }
  }

  /* ===== Vue에서 쓰는 메서드 ===== */
  p.setForces = (n, s, l, r) => {
    forces.n = n ?? 0
    forces.s = s ?? 0
    forces.l = l ?? 0
    forces.r = r ?? 0
    netFx = forces.r - forces.l
    netFy = forces.s - forces.n
    if (!running) p.redraw()
  }

  p.startRun = () => {
    resetBall()
    elapsed = 0
    running = true
    p.loop()
  }

  p.resetSketch = () => {
    forces.n = 0
    forces.s = 0
    forces.l = 0
    forces.r = 2
    netFx = forces.r - forces.l
    netFy = forces.s - forces.n
    resetBall()
    running = false
    p.noLoop()
    p.redraw()
  }
}

onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return

  p5Instance = new p5(sketch, canvasHostRef.value)

  if (p5Instance.setForces) {
    p5Instance.setForces(
      forceN.value,
      forceS.value,
      forceL.value,
      forceR.value
    )
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
.force-sim {
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
  border-radius: 999px;
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
  margin-bottom: 10px;
}

.control-row:last-child {
  margin-bottom: 0;
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
  min-width: 3.4rem;
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
