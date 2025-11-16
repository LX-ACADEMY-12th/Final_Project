<template>
  <div class="columnar-wrap" ref="wrapRef">
    <div class="toolbar">
      <button class="btn" @click="reset">초기화</button>
      <span class="title">물질 분리 탐정단: 사라진 소금을 찾아라!</span>
    </div>

    <div class="tool-selector">
      <strong>도구 선택:</strong>
      <button v-for="tool in tools" :key="tool.key" :class="['btn-tool', { active: currentTool === tool.key }]"
        @click="useTool(tool.key)">
        <span class="tool-main">{{ tool.label }}</span>
        <span class="tool-step">{{ tool.step }}단계</span>
      </button>
    </div>

    <div class="canvas-host" ref="canvasHostRef"></div>

    <p class="desc">
      비커 안에는 <strong>철가루, 모래, 소금</strong>이 뒤섞여 있어요.
      <strong>자석 → 물 붓기 → 거름종이 → 가열</strong> 순서대로 도구를 눌러
      물질의 <strong>성질</strong>을 이용해 혼합물을 끝까지 분리해 보세요.
    </p>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import p5 from 'p5'

const wrapRef = ref(null)
const canvasHostRef = ref(null)
let p5Instance = null

const tools = ref([
  { key: 'magnet', label: '자석', step: 1 },
  { key: 'water', label: '물 붓기', step: 2 },
  { key: 'filter', label: '거름종이', step: 3 },
  { key: 'heat', label: '가열', step: 4 }
])
const currentTool = ref(null)

function reset() {
  currentTool.value = null
  p5Instance?.resetSketch?.()
}

function useTool(key) {
  currentTool.value = key
  p5Instance?.applyTool?.(key)
}

const sketch = (p) => {
  let step = 0
  let message =
    '비커 속 철가루, 모래, 소금을 잘 관찰하고 도구를 순서대로 사용해 보세요.'
  let subMessage = ''

  let isHeating = false
  let evapStart = 0
  const evapDuration = 8000

  let particles = []

  // 세로 비율
  const aspect = 1.3
  let w = 600
  let h = 600

  let beakerRect, filterRect, filtrateBeakerRect, magnetRect, heatRect

  p.setup = () => {
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH

    const c = p.createCanvas(w, h)
    c.parent(canvasHostRef.value)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    initLayoutRects()
    initParticles()
    p.noLoop()
    p.redraw()
  }

  p.windowResized = () => {
    if (!canvasHostRef.value) return
    const { canvasW, canvasH } = measureCanvasSize(canvasHostRef.value)
    w = canvasW
    h = canvasH
    p.resizeCanvas(w, h)
    initLayoutRects()
    p.redraw()
  }

  p.draw = () => {
    p.background(255)

    // 위: 설명 + 범례
    drawMessagesCard()

    // 아래: 장치 / 비커
    drawBeaker(beakerRect, '혼합물 비커')
    drawBeaker(
      filtrateBeakerRect,
      step >= 3 ? '소금물 / 소금 비커' : '빈 비커'
    )
    drawMagnet()
    drawFilterSetup()
    drawHeater()
    drawParticlesByContainer()

    if (step >= 2) drawSolutionWater()
    if (step === 4) drawEvaporation()
  }

  // -------- 레이아웃 --------
  function initLayoutRects() {
    const margin = w * 0.08

    beakerRect = {
      x: w * 0.26,
      y: h * 0.75,
      w: w * 0.28,
      h: h * 0.38
    }

    filtrateBeakerRect = {
      x: w * 0.74,
      y: h * 0.77,
      w: w * 0.26,
      h: h * 0.36
    }

    filterRect = {
      x: (beakerRect.x + filtrateBeakerRect.x) / 2,
      y: h * 0.55,
      w: w * 0.26,
      h: h * 0.08
    }

    magnetRect = {
      x: margin + w * 0.08,
      y: h * 0.55,
      w: w * 0.18,
      h: h * 0.1
    }

    heatRect = {
      x: filtrateBeakerRect.x,
      y: h * 0.97,
      w: filtrateBeakerRect.w * 0.9,
      h: h * 0.04
    }
  }

  function initParticles() {
    particles = []
    const mixRegion = { x: 0.1, y: 0.45, w: 0.8, h: 0.45 }

    const makeGroup = (type, count) => {
      for (let i = 0; i < count; i++) {
        particles.push({
          type,
          container: 'mixture',
          rx: mixRegion.x + p.random() * mixRegion.w,
          ry: mixRegion.y + p.random() * mixRegion.h
        })
      }
    }

    makeGroup('iron', 18)
    makeGroup('sand', 18)
    makeGroup('salt', 18)
  }

  // -------- 설명 + 범례 --------
  function drawMessagesCard() {
    const paddingX = w * 0.06
    const areaTop = h * 0.04
    const textWidth = w - paddingX * 2
    let y = areaTop

    p.textAlign(p.LEFT, p.TOP)
    p.rectMode(p.CORNER)

    // 제목
    p.fill(0)
    p.textSize(13)
    p.text('혼합물 분리 실험', paddingX, y)
    y += 18

    // 본문 메시지
    p.fill(80)
    p.textSize(12)
    const bodyHeight = h * 0.14
    p.text(message, paddingX, y, textWidth, bodyHeight)
    y += bodyHeight + 4

    // 부가 설명
    if (subMessage) {
      p.fill(120)
      p.textSize(11)
      const subHeight = h * 0.12
      p.text(subMessage, paddingX, y, textWidth, subHeight)
      y += subHeight + 4
    }

    // 범례
    const legendTop = h * 0.28
    const gapY = 16

    p.textAlign(p.LEFT, p.CENTER)
    p.textSize(11)

    // 철가루
    p.noStroke()
    p.fill(60)
    p.ellipse(paddingX + 6, legendTop, 8, 8)
    p.fill(0)
    p.text('철가루(자석에 붙음)', paddingX + 18, legendTop)

    // 모래
    p.fill(194, 145, 86)
    p.ellipse(paddingX + 6, legendTop + gapY, 8, 8)
    p.fill(0)
    p.text('모래(물에 녹지 않음)', paddingX + 18, legendTop + gapY)

    // 소금
    p.fill(220)
    p.ellipse(paddingX + 6, legendTop + gapY * 2, 8, 8)
    p.fill(0)
    p.text('소금(물에 녹음)', paddingX + 18, legendTop + gapY * 2)
  }

  // -------- 비커 / 자석 / 거름종이 / 가열 --------
  function drawBeaker(rect, label) {
    const { x, y, w: bw, h: bh } = rect
    const topY = y - bh * 0.45
    const bottomY = y + bh * 0.45

    p.push()
    p.rectMode(p.CENTER)
    p.noFill()
    p.stroke(120)
    p.strokeWeight(2)
    p.rect(x, (topY + bottomY) / 2, bw, bh, 10)

    p.noStroke()
    p.fill(80)
    p.textAlign(p.CENTER, p.TOP)
    p.textSize(11)
    p.text(label, x, bottomY + 6)
    p.pop()
  }

  function drawMagnet() {
    const r = magnetRect
    p.push()
    p.rectMode(p.CENTER)

    p.stroke(80)
    p.strokeWeight(2)
    p.fill(230)
    p.rect(r.x, r.y, r.w, r.h, 12)

    const uW = r.w * 0.7
    const uH = r.h * 0.8
    p.noFill()
    p.stroke(200, 0, 0)
    p.strokeWeight(4)
    p.arc(r.x, r.y + uH * 0.1, uW, uH, p.PI, 0)
    p.line(r.x - uW / 2, r.y, r.x - uW / 2, r.y - uH * 0.2)
    p.line(r.x + uW / 2, r.y, r.x + uW / 2, r.y - uH * 0.2)

    p.noStroke()
    p.fill(0)
    p.textAlign(p.CENTER, p.BOTTOM)
    p.textSize(11)
    p.text('자석', r.x, r.y - uH * 0.6)
    p.pop()
  }

  function drawFilterSetup() {
    const r = filterRect
    p.push()
    p.rectMode(p.CENTER)

    p.noStroke()
    p.fill(245)
    p.triangle(r.x - r.w / 2, r.y, r.x + r.w / 2, r.y, r.x, r.y + r.h)

    p.fill(255)
    p.stroke(180)
    p.strokeWeight(1)
    p.rect(r.x, r.y, r.w, r.h * 0.35, 6)

    p.noStroke()
    p.fill(0)
    p.textAlign(p.CENTER, p.BOTTOM)
    p.textSize(11)
    p.text('거름종이', r.x, r.y - r.h * 0.7)
    p.pop()
  }

  function drawHeater() {
    const r = heatRect
    p.push()
    p.rectMode(p.CENTER)

    p.noStroke()
    p.fill(100)
    p.rect(r.x, r.y, r.w, r.h, 6)

    if (isHeating) {
      p.fill(255, 150, 0, 200)
      for (let i = 0; i < 5; i++) {
        const fx = r.x + p.random(-r.w * 0.4, r.w * 0.4)
        const fy = r.y - r.h * 0.6 + p.random(-3, 3)
        p.ellipse(fx, fy, 6, 14)
      }
      p.fill(0, 120, 255, 180)
      for (let i = 0; i < 3; i++) {
        const fx = r.x + p.random(-r.w * 0.3, r.w * 0.3)
        const fy = r.y - r.h * 0.5 + p.random(-2, 2)
        p.ellipse(fx, fy, 4, 10)
      }
    }

    p.fill(255)
    p.textAlign(p.CENTER, p.BOTTOM)
    p.textSize(11)
    p.text('가열 장치', r.x, r.y - r.h * 2.2)
    p.pop()
  }

  // -------- 입자 / 소금물 / 증발 --------
  function drawParticlesByContainer() {
    particles.forEach((pt) => {
      let rect
      if (pt.container === 'mixture') rect = beakerRect
      else if (pt.container === 'magnet') rect = magnetRect
      else if (pt.container === 'filter') rect = filterRect
      else if (pt.container === 'solution') rect = filtrateBeakerRect
      else rect = beakerRect

      const padX = rect.w * 0.1
      const padY = rect.h * 0.15
      const left = rect.x - rect.w / 2 + padX
      const top = rect.y - rect.h / 2 + padY
      const width = rect.w - padX * 2
      const height = rect.h - padY * 2

      const x = left + pt.rx * width
      const y = top + pt.ry * height

      if (pt.type === 'iron') p.fill(60)
      else if (pt.type === 'sand') p.fill(194, 145, 86)
      else if (pt.type === 'salt') p.fill(230)
      else p.fill(0)

      // 녹은 소금은 입자로 안 보이게
      if (pt.container === 'solution' && pt.type === 'salt') return

      let r = 6
      if (pt.type === 'iron') r = 5
      if (pt.type === 'salt') r = 5

      p.noStroke()
      p.ellipse(x, y, r, r)
    })
  }

  function drawSolutionWater() {
    const r = filtrateBeakerRect
    const top = r.y - r.h * 0.25
    const bottom = r.y + r.h * 0.4

    const hasSolution =
      particles.some(
        (pt) => pt.container === 'solution' && pt.type === 'salt'
      ) || step >= 3

    if (!hasSolution) return

    p.push()
    p.noStroke()
    p.fill(0, 150, 255, 80)
    p.rectMode(p.CORNERS)
    p.rect(r.x - r.w * 0.32, top, r.x + r.w * 0.32, bottom, 0, 0, 8, 8)
    p.pop()
  }

  // 🔥 여기서 큰 직사각형을 없애고, 소금 결정만 나타나게 단순화
  function drawEvaporation() {
    const r = filtrateBeakerRect
    const bottom = r.y + r.h * 0.4

    const now = p.millis()
    const t = p.constrain((now - evapStart) / evapDuration, 0, 1)

    p.push()
    // 남아 있는 물은 더 이상 별도 사각형으로 그리지 않음
    // → 큰 박스가 화면을 가리는 문제 제거

    // 시간에 따라 소금 결정 개수 증가
    const crystalCount = Math.floor(p.map(t, 0, 1, 4, 24))
    p.noStroke()
    p.fill(240)
    for (let i = 0; i < crystalCount; i++) {
      const cx = r.x - r.w * 0.28 + p.random() * r.w * 0.56
      const cy = bottom - p.random() * 6
      p.rect(cx, cy, 3, 3)
    }

    // 살짝 수증기 느낌
    p.fill(220, 220, 220, 80)
    for (let i = 0; i < 8; i++) {
      const sx = r.x + p.random(-r.w * 0.25, r.w * 0.25)
      const sy = r.y - r.h * 0.1 + p.random(-5, 5)
      p.ellipse(sx, sy - t * 20, 6, 10)
    }
    p.pop()

    if (t >= 1 && isHeating) {
      isHeating = false
      message = '모든 물이 증발하고, 비커 바닥에 소금만 남았어요!'
      subMessage =
        '물질의 성질(자성, 용해도, 입자 크기, 증발)을 이용해 혼합물을 완전히 분리했어요.'
      p.noLoop()
    }
  }

  // -------- Tool 적용 --------
  p.applyTool = (toolKey) => {
    const expectedOrder = ['magnet', 'water', 'filter', 'heat']
    const expectedTool = expectedOrder[step]

    if (toolKey !== expectedTool && step < 4) {
      const nameMap = {
        magnet: '자석',
        water: '물 붓기',
        filter: '거름종이',
        heat: '가열'
      }
      message = `먼저 '${nameMap[expectedTool]}' 도구를 사용해 보세요.`
      subMessage = ''
      p.redraw()
      return
    }

    if (toolKey === 'magnet' && step === 0) {
      particles.forEach((pt) => {
        if (pt.type === 'iron' && pt.container === 'mixture') {
          pt.container = 'magnet'
          pt.rx = 0.2 + p.random() * 0.6
          pt.ry = 0.2 + p.random() * 0.6
        }
      })
      message = '자석에 철가루만 쏙쏙 붙었어요!'
      subMessage = '철은 자석에 붙는 성질이 있어서 혼합물에서 분리할 수 있어요.'
      step = 1
      p.redraw()
      return
    }

    if (toolKey === 'water' && step === 1) {
      particles.forEach((pt) => {
        if (pt.type === 'salt' && pt.container === 'mixture') {
          pt.container = 'solution'
          pt.rx = 0.2 + p.random() * 0.6
          pt.ry = 0.3 + p.random() * 0.4
        }
      })
      message = '물을 붓자 소금이 모두 녹아 투명한 소금물이 되었어요.'
      subMessage =
        '소금은 물에 잘 녹는 성질(용해도)이 크기 때문에 분리할 수 있어요.'
      step = 2
      p.redraw()
      return
    }

    if (toolKey === 'filter' && step === 2) {
      particles.forEach((pt) => {
        if (pt.type === 'sand' && pt.container === 'mixture') {
          pt.container = 'filter'
          pt.rx = 0.2 + p.random() * 0.6
          pt.ry = 0.3 + p.random() * 0.4
        }
        if (pt.container === 'solution') {
          pt.rx = 0.2 + p.random() * 0.6
          pt.ry = 0.3 + p.random() * 0.4
        }
      })
      message =
        '거름종이를 통과하면서 모래는 거름종이에 걸리고, 소금물만 아래 비커로 내려갔어요.'
      subMessage =
        '모래 알갱이는 크기가 커서 걸러지고, 소금물은 필터의 작은 구멍을 통과해요.'
      step = 3
      p.redraw()
      return
    }

    if (toolKey === 'heat' && step === 3) {
      isHeating = true
      evapStart = p.millis()
      message = '소금물을 가열하면 물이 서서히 증발해요.'
      subMessage = '물이 모두 날아가면, 비커 바닥에는 소금 결정만 남게 돼요.'
      step = 4
      p.loop()
      return
    }

    if (step >= 4) {
      message = '실험이 이미 완료되었어요. 초기화를 눌러 다시 해 볼까요?'
      subMessage = ''
      p.redraw()
    }
  }

  p.resetSketch = () => {
    step = 0
    isHeating = false
    evapStart = 0
    message =
      '비커 속 철가루, 모래, 소금을 잘 관찰하고 도구를 순서대로 사용해 보세요.'
    subMessage = ''
    initParticles()
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
  font-size: 1.05rem;
  margin-left: 8px;
}

.canvas-host {
  width: 100%;
  min-height: 520px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
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
  font-size: 0.9rem;
  transition: filter 0.2s;
}

.btn:hover {
  filter: brightness(0.97);
}

.tool-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 10px;
  background: #eee;
  border-radius: 8px;
}

.tool-selector strong {
  font-size: 0.9rem;
  margin-right: 4px;
}

.btn-tool {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 8px;
  background: #fff;
  border: 2px solid #ccc;
  cursor: pointer;
  font-size: 0.85rem;
  min-width: 70px;
  transition: all 0.2s ease;
}

.btn-tool:hover {
  background: #f5f5f5;
}

.btn-tool.active {
  border-color: #6366f1;
  background-color: #6366f1;
  color: #fff;
  box-shadow: 0 2px 8px -2px rgba(99, 102, 241, 0.7);
  font-weight: 600;
}

.tool-main {
  line-height: 1.1;
}

.tool-step {
  font-size: 0.7rem;
  opacity: 0.8;
}
</style>
