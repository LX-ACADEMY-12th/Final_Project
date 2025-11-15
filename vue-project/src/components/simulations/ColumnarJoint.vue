<template>
  <div class="columnar-wrap" ref="wrapRef">
    <div class="toolbar">
      <button class="btn" @click="reset">랜덤 재배치</button>
      <button class="btn primary" @click="run">Run</button>
      <span class="title">주상절리의 형성</span>
    </div>

    <div class="canvas-host" ref="canvasHostRef"></div>

    <p class="desc">
      점(냉각핵)을 드래그해 배치할 수 있어요. <em>Run</em>을 누르면
      모든 방향으로 등속 수축이 진행되며 이웃 핵과 만나면 그 방향 성장이 멈추고
      다각형 경계가 형성됩니다.
    </p>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import p5 from 'p5'

// --- 1. Vue의 상태 및 DOM 참조 (최상위) ---
const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

// --- 2. Vue 템플릿과 연결되는 프록시 함수 (최상위) ---
// 이 함수들은 p5 인스턴스가 준비되면 내부의 함수를 호출합니다.
function reset() {
  if (p5Instance) p5Instance.resetSketch()
}
function run() {
  if (p5Instance) p5Instance.runSketch()
}

// --- 3. p5.js 스케치 정의 (최상위) ---
// 이 함수 하나에 모든 p5 로직이 캡슐화됩니다.
const sketch = (p) => {

  // --- p5 스케치 내부의 변수들 ---
  let isRunning = false

  // ----- 파라미터 -----
  const aspect = 0.5
  const coreMax = 48
  const growthVectorMax = 36
  let time = -1
  let img = null

  // ----- 시뮬레이션 데이터 -----
  let coreColor = new Array(coreMax)
  let coreX = new Array(coreMax)
  let coreY = new Array(coreMax)
  let spotStopped = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(false))
  let spotPolyX = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(0))
  let spotPolyY = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(0))
  let growthVectorX = new Array(growthVectorMax)
  let growthVectorY = new Array(growthVectorMax)

  // 드래그 상태
  let dragged = -1
  let offsetX = 0
  let offsetY = 0

  // --- p5 스케치 내부의 헬퍼 함수 ---
  function measureCanvasSize(hostEl) {
    const fallbackEl = hostEl || document.documentElement
    const maxW = fallbackEl.clientWidth || 694
    const maxH = (window.innerHeight - 160) / aspect
    const w = Math.floor(Math.min(maxW, maxH))
    const h = Math.floor(w * aspect)
    return { w, h }
  }

  // --- p5 라이프사이클 함수 ---
  p.setup = () => {
    p.frameRate(20)
    const { w, h } = measureCanvasSize(canvasHostRef.value)
    const c = p.createCanvas(w, h)
    if (canvasHostRef.value) c.parent(canvasHostRef.value)
    c.id('myP5Canvas')

      // 배경 이미지 비동기 로드 (없어도 정상 동작)
      ; (async () => {
        try {
          img = await p.loadImage('/img/sim/joint1.jpg')
        } catch {
          img = null
        }
      })()

    // 성장 방향 벡터 초기화
    for (let i = 0; i < growthVectorMax; i++) {
      const t = p.map(i, 0, growthVectorMax, 0, p.TWO_PI)
      growthVectorX[i] = 0.001 * Math.cos(t)
      growthVectorY[i] = 0.001 * Math.sin(t)
    }

    // 내부 배열 재초기화
    spotStopped = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(false))
    spotPolyX = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(0))
    spotPolyY = Array.from({ length: coreMax }, () => new Array(growthVectorMax).fill(0))

    setup2() // 초기 실행
  }

  p.windowResized = () => {
    const { w, h } = measureCanvasSize(canvasHostRef.value)
    p.resizeCanvas(w, h)
  }

  function setup2() {
    time = -1
    for (let i = 0; i < coreMax; i++) {
      coreColor[i] = Math.round(p.random(96, 160))
      for (let n = 0; n < 200; n++) {
        const x = p.random(0.01, 0.65)
        const y = p.random(0.01, 0.49)
        let tooNear = false
        for (let ii = 0; ii < i; ii++) {
          if (i !== ii && p.dist(x, y, coreX[ii], coreY[ii]) < 0.06) { tooNear = true; break }
        }
        if (!tooNear) { coreX[i] = x; coreY[i] = y; break }
      }
    }
    setup3()
  }

  function setup3() {
    for (let i = 0; i < coreMax; i++) {
      for (let j = 0; j < growthVectorMax; j++) {
        spotStopped[i][j] = false
        spotPolyX[i][j] = coreX[i]
        spotPolyY[i][j] = coreY[i]
      }
    }
  }

  p.draw = () => {
    try {
      if (Array.isArray(p.touches) && p.touches.length > 1) return

      p.background(233)
      p.strokeWeight(1)
      p.textSize(20)

      if (isRunning && time >= 0 && time < 200) time++ // isRunning 플래그 체크

      for (let i = 0; i < coreMax; i++) {
        for (let j = 0; j < growthVectorMax; j++) {
          if (spotStopped[i][j]) continue
          const x = coreX[i] + time * growthVectorX[j]
          const y = coreY[i] + time * growthVectorY[j]

          let invasion = false
          for (let ii = 0; ii < coreMax; ii++) {
            if (Math.abs(coreX[ii] - coreX[i]) > 0.3) continue
            if (Math.abs(coreY[ii] - coreY[i]) > 0.3) continue
            if (i !== ii) {
              const tdist = p.dist(x, y, coreX[ii], coreY[ii])
              if (tdist < time * 0.001) { invasion = true; spotStopped[i][j] = true; break }
            }
          }
          if (!invasion) { spotPolyX[i][j] = x; spotPolyY[i][j] = y }
        }
      }

      p.noStroke()
      for (let i = 0; i < coreMax; i++) {
        p.fill(coreColor[i])
        p.beginShape()
        for (let j = 0; j < growthVectorMax; j++) {
          p.vertex(p.width * spotPolyX[i][j], p.width * spotPolyY[i][j])
        }
        p.endShape(p.CLOSE)
      }

      p.fill(0)
      for (let i = 0; i < coreMax; i++) {
        p.ellipse(p.width * coreX[i], p.width * coreY[i], p.width * 0.005, p.width * 0.005)
      }

      if (img) {
        p.image(img, p.width * 0.66, 0, p.width * 0.34, p.width * 0.5)
      }
    } catch (e) {
      console.error('[ColumnarJoint.draw] error:', e)
    }
  }

  // --- p5 이벤트 핸들러 ---
  const contain = (mx, my, x, y, w, h) => (mx >= x && mx <= x + w && my >= y && my <= y + h)

  p.mousePressed = () => {
    if (!contain(p.mouseX, p.mouseY, 0, 0, p.width * 0.7, p.height)) return
    let d1 = 999
    dragged = -1
    for (let i = 0; i < coreMax; i++) {
      const d2 = p.dist(p.mouseX / p.width, p.mouseY / p.width, coreX[i], coreY[i])
      if (d2 < d1) { d1 = d2; dragged = i }
    }
    if (dragged >= 0) {
      offsetX = p.mouseX / p.width - coreX[dragged]
      offsetY = p.mouseY / p.width - coreY[dragged]
    }
  }

  p.mouseDragged = () => {
    if (dragged < 0) return
    coreX[dragged] = p.mouseX / p.width - offsetX
    coreY[dragged] = p.mouseY / p.width - offsetY
    coreX[dragged] = p.constrain(coreX[dragged], 0.01, 0.65)
    coreY[dragged] = p.constrain(coreY[dragged], 0.01, 0.49)
    time = -1
    isRunning = false // 드래그 시 시뮬레이션 중지
    for (let j = 0; j < growthVectorMax; j++) {
      spotStopped[dragged][j] = false
      spotPolyX[dragged][j] = coreX[dragged]
      spotPolyY[dragged][j] = coreY[dragged]
    }
    return false
  }

  p.mouseReleased = () => { dragged = -1 }
  p.touchStarted = () => { p.mousePressed() }
  p.touchMoved = () => {
    if (Array.isArray(p.touches) && p.touches.length > 1) return
    return p.mouseDragged()
  }
  p.touchEnded = () => { p.mouseReleased() }


  // --- 4. Vue와의 "Bridge" 함수 (p5 인스턴스에 추가) ---
  // Vue의 최상위 reset() 함수가 호출할 함수
  p.resetSketch = () => {
    time = -1
    isRunning = false
    setup2() // 내부의 setup2 호출
    p.loop() // p5 루프 다시 시작 (필요시)
  }

  // Vue의 최상위 run() 함수가 호출할 함수
  p.runSketch = () => {
    if (time < 0) {
      time = 0
      isRunning = true
      p.loop() // p5 루프 다시 시작
    }
  }
} // --- End of sketch function ---


// --- 5. Vue 생명주기 훅 (최상위) ---
onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return

  // 캔버스 컨테이너를 두 번째 인수로 전달하여 p5 인스턴스 생성
  p5Instance = new p5(sketch, canvasHostRef.value)
})

onBeforeUnmount(() => {
  try {
    p5Instance?.remove()
  } catch (e) {
    console.error('Error removing p5 instance:', e)
  }
})
</script>

<style scoped>
/* <style> 태그는 변경 사항 없습니다 */
.columnar-wrap {
  background: #f7f7f7;
  border-radius: 12px;
  padding: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.title {
  font-weight: 700;
  font-size: 1.1rem;
  margin-left: 8px;
}

.canvas-host {
  width: 100%;
  min-height: 120px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.desc {
  color: #666;
  font-size: 0.9rem;
  margin-top: 10px;
}

.btn {
  padding: 8px 12px;
  border-radius: 8px;
  background: #efefef;
  border: 1px solid #e2e2e2;
  cursor: pointer;
}

.btn:hover {
  filter: brightness(0.97);
}

.btn.primary {
  background: #6366F1;
  color: #fff;
  border-color: #5b5ee8;
}

.btn.ghost {
  background: transparent;
  border: 1px dashed #bbb;
}
</style>
