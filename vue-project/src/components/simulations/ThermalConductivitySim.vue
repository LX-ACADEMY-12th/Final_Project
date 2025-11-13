<template>
  <div class="columnar-wrap" ref="wrapRef">
    <div class="toolbar">
      <button class="btn" @click="reset">초기화</button>
      <button class="btn primary" @click="run">가열 시작 (Run)</button>
      <span class="title">어떤 냄비에서 물이 더 빨리 끓을까요?</span>
    </div>

    <div class="material-selector">
      <strong>냄비 재료 선택:</strong>
      <button v-for="mat in materials" :key="mat.key" :class="['btn-mat', { active: selectedMaterialKey === mat.key }]"
        :style="{ '--mat-color': mat.colorStr }" @click="selectMaterial(mat.key)">
        {{ mat.name }}
      </button>
    </div>

    <div class="canvas-host" ref="canvasHostRef"></div>

    <p class="desc">
      선택한 재료의 냄비에 열을 가합니다. <em>Run</em>을 누르면
      재료의 <strong>열전도율(k)</strong>에 따라 물의 온도가 상승하는 속도가 달라집니다.
      온도계를 관찰하며 재료별 차이를 확인해 보세요.
    </p>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import p5 from 'p5'

// --- 1. Vue의 상태 및 DOM 참조 ---
const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

// (W/m·K 기준) 상대적 열전도율 (스테인리스를 1로 설정)
// 구리: ~400, 알루미늄: ~240, 무쇠: ~60, 스테인리스: ~16
const materials = ref([
  { key: 'copper', name: '구리', k: 25, color: [184, 115, 51], colorStr: 'rgb(184, 115, 51)' },
  { key: 'aluminum', name: '알루미늄', k: 15, color: [192, 192, 192], colorStr: 'rgb(192, 192, 192)' },
  { key: 'iron', name: '무쇠', k: 3.75, color: [60, 60, 60], colorStr: 'rgb(60, 60, 60)' },
  { key: 'steel', name: '스테인리스', k: 1, color: [210, 210, 210], colorStr: 'rgb(210, 210, 210)' }
])
const selectedMaterialKey = ref('steel') // 기본값

// --- 2. Vue 템플릿과 연결되는 프록시 함수 ---
function reset() {
  if (p5Instance) p5Instance.resetSketch()
}

function run() {
  if (p5Instance) p5Instance.runSketch()
}

function selectMaterial(key) {
  selectedMaterialKey.value = key
  if (p5Instance) {
    p5Instance.setMaterial(key)
  }
}

// --- 3. p5.js 스케치 정의 ---
const sketch = (p) => {

  // --- p5 스케치 내부 변수 ---
  let isRunning = false
  let waterTemp = 20.0
  let startTime = 0

  const startTemp = 20.0
  const boilTemp = 100.0
  const baseTimeToBoil = 20000 // 스테인리스(k=1)가 100도씨 되는데 걸리는 시간 (20초)

  let timeToBoil = baseTimeToBoil // 현재 재료의 끓는 시간

  let bubbles = []
  let steam = []

  // 재료 데이터 (p5 내부용)
  const materialData = {
    'copper': { name: '구리', k: 25, color: [184, 115, 51] },
    'aluminum': { name: '알루미늄', k: 15, color: [192, 192, 192] },
    'iron': { name: '무쇠', k: 3.75, color: [60, 60, 60] },
    'steel': { name: '스테인리스', k: 1, color: [210, 210, 210] }
  }
  let currentMaterial = materialData.steel // 기본값

  // 캔버스 크기 관련
  const aspect = 0.7
  let w = 600
  let h = w * aspect

  // --- p5 라이프사이클 함수 ---
  p.setup = () => {
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH

    const c = p.createCanvas(w, h)
    c.parent(canvasHostRef.value)

    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1)) // HiDPI/Retina 지원

    p.noLoop() // 'Run'을 누르기 전까지 루프 비활성화
    p.redraw() // 초기 상태 한 번 그리기
  }

  p.windowResized = () => {
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH
    p.resizeCanvas(w, h)
    p.redraw()
  }

  // --- p5 메인 드로우 함수 ---
  p.draw = () => {
    p.background(255) // 흰색 배경

    // --- 시뮬레이션 로직 ---
    if (isRunning) {
      let elapsed = p.millis() - startTime

      // 물 온도 계산: (시작시간, 끓는 목표 시간) -> (시작온도, 끓는점) 으로 매핑
      waterTemp = p.map(elapsed, 0, timeToBoil, startTemp, boilTemp)
      waterTemp = p.constrain(waterTemp, startTemp, boilTemp)
    }

    // --- 그리기 함수 호출 ---
    drawBurner(w / 2, h * 0.95, isRunning)
    drawPot(w / 2, h * 0.7, w * 0.5, h * 0.4)
    drawWater(w / 2, h * 0.7, w * 0.5, h * 0.4)

    // 온도에 따라 기포 및 수증기 생성
    if (waterTemp > startTemp) {
      updateAndDrawBubbles(w / 2, h * 0.7, w * 0.45, h * 0.38)
    }
    if (waterTemp >= boilTemp) {
      updateAndDrawSteam(w / 2, h * 0.52, w * 0.45)
    }

    drawThermometer(w * 0.1, h * 0.8, w * 0.05, h * 0.6)
  }

  // --- 그리기 헬퍼 함수 ---

  function drawBurner(x, y, isOn) {
    p.noStroke()
    p.fill(100) // 버너
    p.rectMode(p.CENTER)
    p.rect(x, y, w * 0.6, h * 0.05, 5)

    if (isOn) {
      // 불꽃 애니메이션
      p.fill(255, 150, 0, 200) // 주황색
      for (let i = 0; i < 5; i++) {
        let flameX = x + p.random(-w * 0.25, w * 0.25)
        let flameY = y - h * 0.05 + p.random(-h * 0.02, h * 0.02)
        p.ellipse(flameX, flameY, w * 0.03, h * 0.08)
      }
      p.fill(0, 150, 255, 180) // 파란색
      for (let i = 0; i < 3; i++) {
        let flameX = x + p.random(-w * 0.25, w * 0.25)
        let flameY = y - h * 0.05 + p.random(-h * 0.01, h * 0.01)
        p.ellipse(flameX, flameY, w * 0.02, h * 0.05)
      }
    }
  }

  function drawPot(x, y, potW, potH) {
    p.rectMode(p.CENTER)

    // 냄비 손잡이
    p.fill(80)
    p.noStroke()
    p.rect(x - potW * 0.6, y - potH * 0.3, potW * 0.1, potH * 0.2, 5)
    p.rect(x + potW * 0.6, y - potH * 0.3, potW * 0.1, potH * 0.2, 5)

    // 냄비 본체 (외부)
    p.fill(150) // 겉
    p.rect(x, y, potW, potH, 10)

    // 냄비 재료 (바닥) - 열전도율이 적용되는 부분
    p.fill(currentMaterial.color)
    p.rect(x, y + potH * 0.45, potW, potH * 0.1, 0, 0, 10, 10)
  }

  function drawWater(x, y, potW, potH) {
    // 물
    p.fill(0, 150, 255, 100) // 물 색깔 (반투명)
    p.noStroke()
    p.rectMode(p.CENTER)
    let waterY = y + p.map(potH, 0, potH, -potH * 0.05, potH * 0.3)
    p.rect(x, waterY, potW * 0.95, potH * 0.7, 5, 5, 0, 0)
  }

  function drawThermometer(x, y, tmW, tmH) {
    p.rectMode(p.CORNER)
    p.textAlign(p.CENTER, p.BOTTOM)
    p.fill(0)
    p.textSize(14)
    p.text(waterTemp.toFixed(1) + "°C", x, y - tmH - 25)

    // 온도계 배경
    p.fill(255)
    p.stroke(100)
    p.strokeWeight(2)
    p.rect(x - tmW / 2, y - tmH, tmW, tmH, 10)

    // 수은주
    let mercuryH = p.map(waterTemp, 0, 100, 0, tmH * 0.9)
    mercuryH = p.constrain(mercuryH, 0, tmH * 0.9)
    p.fill(255, 0, 0) // 빨간색
    p.noStroke()
    p.rect(x - tmW / 2 + 5, y - mercuryH, tmW - 10, mercuryH, 5)

    // 온도계 눈금
    p.stroke(120)
    p.strokeWeight(1)
    let y100 = y - tmH * 0.9
    let y0 = y
    p.line(x - tmW, y100, x - tmW / 2, y100) // 100도
    p.line(x - tmW, y0, x - tmW / 2, y0) // 0도
    p.textAlign(p.RIGHT, p.CENTER)
    p.fill(0)
    p.noStroke()
    p.textSize(10)
    p.text("100°", x - tmW / 2 - 5, y100)
    p.text("0°", x - tmW / 2 - 5, y0)
  }

  function updateAndDrawBubbles(x, y, waterW, waterH) {
    p.rectMode(p.CENTER)
    let waterSurfaceY = y - waterH / 2
    let waterBottomY = y + waterH / 2

    // 온도가 80도 이상이면 기포 생성 시작, 100도에 최대
    let bubbleRate = 0
    if (waterTemp > 80) {
      bubbleRate = p.map(waterTemp, 80, boilTemp, 0.1, 1.0)
    }

    if (p.random(1) < bubbleRate && isRunning) {
      let b = {
        x: x + p.random(-waterW / 2, waterW / 2),
        y: waterBottomY - p.random(10),
        r: p.random(1, p.map(waterTemp, 80, boilTemp, 3, 8)),
        speed: p.random(1, 3)
      }
      bubbles.push(b)
    }

    p.fill(255, 255, 255, 150)
    p.noStroke()
    for (let i = bubbles.length - 1; i >= 0; i--) {
      let b = bubbles[i]
      b.y -= b.speed
      b.x += p.random(-0.5, 0.5)
      p.ellipse(b.x, b.y, b.r, b.r)

      if (b.y < waterSurfaceY) {
        bubbles.splice(i, 1) // 수면에 닿으면 제거
      }
    }
  }

  function updateAndDrawSteam(x, ySurface, waterW) {
    // 끓을 때만 수증기 생성
    if (p.random(1) < 0.8 && isRunning) {
      let s = {
        x: x + p.random(-waterW / 2, waterW / 2),
        y: ySurface,
        r: p.random(5, 15),
        alpha: 100,
        speedY: p.random(1, 2)
      }
      steam.push(s)
    }

    p.noStroke()
    for (let i = steam.length - 1; i >= 0; i--) {
      let s = steam[i]
      s.y -= s.speedY
      s.alpha -= 2
      s.x += p.random(-1, 1)
      p.fill(200, 200, 200, s.alpha)
      p.ellipse(s.x, s.y, s.r, s.r)

      if (s.alpha < 0) {
        steam.splice(i, 1) // 투명해지면 제거
      }
    }
  }

  function measureCanvasSize(hostEl) {
    const fallbackEl = hostEl || document.documentElement
    const maxW = fallbackEl.clientWidth || 600
    const w = Math.floor(maxW)
    const h = Math.floor(w * aspect)
    return { canvasW: w, canvasH: h }
  }

  // --- 4. Vue와의 "Bridge" 함수 ---

  // 재료 변경
  p.setMaterial = (key) => {
    currentMaterial = materialData[key] || materialData.steel
    // 열전도율(k)에 반비례하여 끓는 시간 설정
    // k가 25(구리)이면 timeToBoil이 짧아지고, k가 1(강철)이면 길어짐
    timeToBoil = baseTimeToBoil / currentMaterial.k
    p.resetSketch() // 재료가 바뀌면 리셋
  }

  // 초기화
  p.resetSketch = () => {
    isRunning = false
    waterTemp = startTemp
    bubbles = []
    steam = []
    p.noLoop() // 루프 정지
    p.redraw() // 리셋된 상태로 1회 다시 그리기
  }

  // 실행
  p.runSketch = () => {
    if (isRunning) return // 이미 실행 중이면 무시

    // 리셋하고 다시 시작
    p.resetSketch()

    isRunning = true
    startTime = p.millis() // 시작 시간 기록
    p.loop() // p5 루프 다시 시작
  }
} // --- End of sketch function ---


// --- 5. Vue 생명주기 훅 ---
onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return

  // p5 인스턴스 생성
  p5Instance = new p5(sketch, canvasHostRef.value)

  // p5 인스턴스가 준비된 후, Vue의 현재 선택된 재료로 p5 내부 상태 초기화
  if (p5Instance) {
    p5Instance.setMaterial(selectedMaterialKey.value)
  }
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
/* 원본 스타일 유지 */
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
  min-height: 200px;
  /* 최소 높이 확보 */
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  /* p5 캔버스가 부모 영역에 맞게 중앙 정렬되도록 */
  display: flex;
  justify-content: center;
  align-items: center;
}

/* p5 캔버스 자체에 대한 스타일 (Deep 셀렉터) */
.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  /* p5가 세팅하는 인라인 style을 덮어씀 */
  border-radius: 8px;
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

/* --- 추가된 스타일 --- */
.material-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 10px;
  background: #eee;
  border-radius: 8px;
}

.material-selector strong {
  font-size: 0.9rem;
  margin-right: 8px;
}

.btn-mat {
  padding: 6px 10px;
  border-radius: 6px;
  background: #fff;
  border: 2px solid #ccc;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.btn-mat:hover {
  background: #f0f0f0;
}

/* 선택된 버튼 스타일 */
.btn-mat.active {
  border-color: var(--mat-color, #6366F1);
  background-color: var(--mat-color, #6366F1);
  color: #fff;
  box-shadow: 0 2px 8px -2px var(--mat-color, #6366F1);
  font-weight: 600;
}
</style>
