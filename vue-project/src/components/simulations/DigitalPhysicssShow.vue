<template>
  <div class="sim-container" ref="wrapRef">
    <header class="sim-header">
      <h2 class="sim-title">⚽ 힘과 운동</h2>
      <div class="status-badge">
        <span class="badge-icon">{{ running ? '🏃' : '🛑' }}</span>
        <span class="badge-text">{{ running ? '움직이는 중' : '대기 중' }}</span>
      </div>
    </header>

    <main class="viewport-area">
      <div class="canvas-host" ref="canvasHostRef"></div>

      <transition name="fade">
        <div class="info-toast" v-if="resultMessage">
          {{ resultMessage }}
        </div>
      </transition>
    </main>

    <section class="control-panel">
      <div class="panel-header">
        <span class="panel-title">힘의 크기 조절 (0 ~ 3)</span>
        <button class="reset-icon-btn" @click="onReset" title="초기화">
          <i class="bi bi-arrow-counterclockwise"></i>
        </button>
      </div>

      <div class="force-grid">
        <div class="force-item item-top">
          <span class="force-label">위</span>
          <input type="range" min="0" max="3" v-model.number="forceN" @input="updateForces"
            class="custom-range vertical">
          <span class="force-val">{{ forceTexts[forceN] }}</span>
        </div>

        <div class="force-center-row">
          <div class="force-item item-left">
            <span class="force-label">좌</span>
            <input type="range" min="0" max="3" v-model.number="forceL" @input="updateForces" class="custom-range">
            <span class="force-val">{{ forceTexts[forceL] }}</span>
          </div>

          <button class="run-btn" @click="onRun" :disabled="running">
            <span class="btn-icon">▶</span>
            <span>출발</span>
          </button>

          <div class="force-item item-right">
            <span class="force-label">우</span>
            <input type="range" min="0" max="3" v-model.number="forceR" @input="updateForces" class="custom-range">
            <span class="force-val">{{ forceTexts[forceR] }}</span>
          </div>
        </div>

        <div class="force-item item-bottom">
          <span class="force-label">아래</span>
          <input type="range" min="0" max="3" v-model.number="forceS" @input="updateForces"
            class="custom-range vertical">
          <span class="force-val">{{ forceTexts[forceS] }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import p5 from 'p5'

const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

const forceTexts = ['0', '1', '2', '3']
const forceN = ref(0)
const forceS = ref(0)
const forceL = ref(0)
const forceR = ref(2)
const running = ref(false)

const resultMessage = computed(() => {
  const netX = forceR.value - forceL.value
  const netY = forceS.value - forceN.value // p5 좌표계에선 아래가 +y

  if (Math.abs(netX) < 0.1 && Math.abs(netY) < 0.1) return '힘의 평형! 공이 움직이지 않아요.'

  let msg = '합력 방향: '
  if (netY < 0) msg += '위'
  if (netY > 0) msg += '아래'
  if (netX < 0) msg += '왼쪽'
  if (netX > 0) msg += '오른쪽'
  return msg + '으로 움직여요!'
})

function onReset() {
  forceN.value = 0
  forceS.value = 0
  forceL.value = 0
  forceR.value = 2
  running.value = false
  updateForces()
  if (p5Instance?.resetSketch) p5Instance.resetSketch()
}

function onRun() {
  if (p5Instance?.startRun) {
    p5Instance.startRun()
    running.value = true
    // 2.6초 후 running 상태 해제 (애니메이션 종료 시점)
    setTimeout(() => { running.value = false }, 2600)
  }
}

function updateForces() {
  if (p5Instance?.setForces) {
    p5Instance.setForces(forceN.value, forceS.value, forceL.value, forceR.value)
  }
}

// === P5 Sketch ===
const sketch = (p) => {
  let w = 300, h = 240
  const board = { left: 0, right: 0, top: 0, bottom: 0 }
  const forces = { n: 0, s: 0, l: 0, r: 2 }
  let netFx = 0, netFy = 0
  let ballX = 0, ballY = 0, velX = 0, velY = 0
  let isSimulating = false
  let elapsed = 0
  const totalTime = 2600

  p.setup = () => {
    w = canvasHostRef.value?.clientWidth || 300
    h = 260
    p.createCanvas(w, h).parent(canvasHostRef.value)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    initLayout()
    resetBall()
    p.noLoop()
  }

  p.windowResized = () => {
    if (!canvasHostRef.value) return
    w = canvasHostRef.value.clientWidth
    p.resizeCanvas(w, h)
    initLayout()
    if (!isSimulating) resetBall()
    p.redraw()
  }

  function initLayout() {
    const boardW = w * 0.7
    const boardH = h * 0.6
    const cx = w / 2
    const cy = h / 2
    board.left = cx - boardW / 2
    board.right = cx + boardW / 2
    board.top = cy - boardH / 2
    board.bottom = cy + boardH / 2
  }

  function resetBall() {
    ballX = (board.left + board.right) / 2
    ballY = (board.top + board.bottom) / 2
    velX = 0; velY = 0; elapsed = 0
    // 초기 힘 계산
    netFx = forces.r - forces.l
    netFy = forces.s - forces.n
  }

  p.draw = () => {
    p.clear()
    p.background(240, 249, 255) // 아주 연한 하늘색 배경

    // 운동장
    p.rectMode(p.CORNERS)
    p.noStroke(); p.fill(255)
    p.rect(board.left, board.top, board.right, board.bottom, 12)

    // 격자
    p.stroke(226, 232, 240); p.strokeWeight(1)
    for (let x = board.left + 20; x < board.right; x += 20) p.line(x, board.top, x, board.bottom)
    for (let y = board.top + 20; y < board.bottom; y += 20) p.line(board.left, y, board.right, y)

    // 테두리
    p.noFill(); p.stroke('#bae6fd'); p.strokeWeight(2)
    p.rect(board.left, board.top, board.right, board.bottom, 12)

    // 힘 화살표 (외부)
    drawForceArrows()

    // 공
    drawBall()

    // 내부 합력 화살표 (정지 상태일 때만 표시)
    if (!isSimulating) drawNetArrow()

    if (isSimulating) {
      stepPhysics()
      if (elapsed > totalTime) {
        isSimulating = false
        p.noLoop()
        running.value = false // Vue 상태 동기화
      }
    }
  }

  function drawBall() {
    const r = 12
    // 그림자
    p.noStroke(); p.fill(0, 0, 0, 20)
    p.ellipse(ballX, ballY + 4, r * 2.2, r * 0.8)
    // 공 본체
    p.fill('#2563eb'); p.circle(ballX, ballY, r * 2)
    // 하이라이트
    p.fill(255, 255, 255, 100); p.circle(ballX - 3, ballY - 3, r)
  }

  function drawNetArrow() {
    const cx = (board.left + board.right) / 2
    const cy = (board.top + board.bottom) / 2
    const mag = Math.sqrt(netFx * netFx + netFy * netFy)

    if (mag < 0.1) {
      // 평형 상태 (X 표시)
      p.stroke('#94a3b8'); p.strokeWeight(2)
      p.line(cx - 5, cy - 5, cx + 5, cy + 5)
      p.line(cx + 5, cy - 5, cx - 5, cy + 5)
      return
    }
    // 합력 화살표
    const scale = 30
    drawArrow(cx, cy, cx + netFx * scale, cy + netFy * scale, '#ef4444') // 빨간색 강조
  }

  function drawForceArrows() {
    const cx = (board.left + board.right) / 2
    const cy = (board.top + board.bottom) / 2
    const offset = 20 // 보드에서 떨어진 거리
    const scale = 15 // 힘 크기 비례

    // 상
    if (forces.n > 0) drawArrow(cx, board.top - offset, cx, board.top - offset - forces.n * scale, '#3b82f6')
    // 하
    if (forces.s > 0) drawArrow(cx, board.bottom + offset, cx, board.bottom + offset + forces.s * scale, '#3b82f6')
    // 좌
    if (forces.l > 0) drawArrow(board.left - offset, cy, board.left - offset - forces.l * scale, cy, '#3b82f6')
    // 우
    if (forces.r > 0) drawArrow(board.right + offset, cy, board.right + offset + forces.r * scale, cy, '#3b82f6')
  }

  function drawArrow(x1, y1, x2, y2, color) {
    p.push()
    p.stroke(color); p.fill(color); p.strokeWeight(3)
    p.line(x1, y1, x2, y2)
    const angle = Math.atan2(y2 - y1, x2 - x1)
    p.translate(x2, y2); p.rotate(angle)
    p.triangle(0, 0, -6, 3, -6, -3)
    p.pop()
  }

  function stepPhysics() {
    const dt = p.deltaTime || 16
    elapsed += dt
    const ax = netFx * 0.005
    const ay = netFy * 0.005
    velX += ax * (dt / 16); velY += ay * (dt / 16)
    velX *= 0.98; velY *= 0.98 // 마찰
    ballX += velX; ballY += velY

    // 벽 충돌 (간단하게 멈춤)
    const r = 12
    if (ballX < board.left + r || ballX > board.right - r || ballY < board.top + r || ballY > board.bottom - r) {
      // 튕기지 않고 멈추게 하려면
      ballX = p.constrain(ballX, board.left + r, board.right - r)
      ballY = p.constrain(ballY, board.top + r, board.bottom - r)
      velX = 0; velY = 0
    }
  }

  p.setForces = (n, s, l, r) => {
    forces.n = n; forces.s = s; forces.l = l; forces.r = r
    netFx = forces.r - forces.l
    netFy = forces.s - forces.n
    if (!isSimulating) p.redraw()
  }
  p.startRun = () => {
    resetBall()
    isSimulating = true
    p.loop()
  }
  p.resetSketch = () => {
    resetBall()
    isSimulating = false
    p.noLoop()
    p.redraw()
  }
}

onMounted(async () => {
  await nextTick()
  if (canvasHostRef.value) {
    p5Instance = new p5(sketch, canvasHostRef.value)
    // 초기값 설정
    p5Instance.setForces(forceN.value, forceS.value, forceL.value, forceR.value)
  }
})

onBeforeUnmount(() => {
  if (p5Instance) p5Instance.remove()
})
</script>

<style scoped>
/* 위젯 컨테이너 */
.sim-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 520px;
  background: #ffffff;
  font-family: 'SUIT', sans-serif;
  color: #1e293b;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

/* 1. 헤더 */
.sim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid #f3f4f6;
}

.sim-title {
  font-size: 15px;
  font-weight: 800;
  color: #3b82f6;
  margin: 0;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  color: #2563eb;
}

/* 2. 뷰포트 */
.viewport-area {
  flex: 1;
  position: relative;
  background: #f0f9ff;
  overflow: hidden;
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.canvas-host {
  width: 100%;
  height: 100%;
}

/* 토스트 메시지 */
.info-toast {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  z-index: 10;
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
  background: white;
  padding: 16px;
  border-top: 1px solid #f3f4f6;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.reset-icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #94a3b8;
  font-size: 18px;
  padding: 4px;
  transition: color 0.2s;
}

.reset-icon-btn:hover {
  color: #3b82f6;
}

/* 힘 조절 그리드 */
.force-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.force-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.force-center-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  justify-content: center;
}

.force-label {
  font-size: 11px;
  font-weight: 600;
  color: #334155;
}

.force-val {
  font-size: 10px;
  color: #3b82f6;
  font-weight: 700;
}

/* 슬라이더 스타일 */
.custom-range {
  width: 80px;
  height: 6px;
  border-radius: 3px;
  background: #e2e8f0;
  outline: none;
  -webkit-appearance: none;
}

.custom-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #3b82f6;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

/* 실행 버튼 */
.run-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  cursor: pointer;
  transition: transform 0.1s;
  z-index: 5;
}

.run-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.run-btn:disabled {
  background: #cbd5e1;
  box-shadow: none;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 18px;
  margin-bottom: 2px;
}

.run-btn span:last-child {
  font-size: 10px;
  font-weight: 700;
}
</style>
