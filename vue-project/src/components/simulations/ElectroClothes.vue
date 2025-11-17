<template>
  <div class="static-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-lightning-charge"></i>
        </div>
        <div class="header-text">
          <div class="header-title">어떤 옷이 정전기를 많이 만들까?</div>
        </div>
      </div>
      <div class="header-actions">
        <button class="chip-btn" @click="reset">초기화</button>
        <button class="chip-btn primary" @click="rub">마찰하기</button>
      </div>
    </div>
    <!-- 조작 영역 -->
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">옷감 선택</span>
        <div class="control-options">
          <button v-for="fabric in fabrics" :key="fabric.key"
            :class="['pill-btn', { active: currentFabric === fabric.key }]"
            @click="selectFabric(fabric.key)">
            {{ fabric.label }}
          </button>
        </div>
      </div>
      <div class="control-row">
        <span class="control-label">마찰대 선택</span>
        <div class="control-options">
          <button v-for="block in blocks" :key="block.key"
            :class="['pill-btn', { active: currentBlock === block.key }]"
            @click="selectBlock(block.key)">
            {{ block.label }}
          </button>
        </div>
      </div>
      <div class="control-row voltage-row">
        <span class="control-label">마찰 세기</span>
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
const fabrics = ref([
  { key: 'wool', label: '양모' },
  { key: 'cotton', label: '면' },
  { key: 'poly', label: '폴리에스터' },
  { key: 'silk', label: '실크' }
])
const blocks = ref([
  { key: 'styro', label: '스티로폼' },
  { key: 'plastic', label: '플라스틱' },
  { key: 'metal', label: '금속' }
])
const currentFabric = ref('wool')
const currentBlock = ref('styro')
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
function rub() {
  if (p5Instance && p5Instance.rubOnce) {
    p5Instance.rubOnce()
  }
}
function selectFabric(key) {
  currentFabric.value = key
  if (p5Instance && p5Instance.setFabric) {
    p5Instance.setFabric(key)
  }
}
function selectBlock(key) {
  currentBlock.value = key
  if (p5Instance && p5Instance.setBlock) {
    p5Instance.setBlock(key)
  }
}
function updateLevel() {
  if (p5Instance && p5Instance.setLevel) {
    p5Instance.setLevel(level.value)
  }
}
/* ===================== p5 스케치 ===================== */
const sketch = (p) => {
  let w = 600
  let h = 500
  let fabric = 'wool'
  let block = 'styro'
  let powerLevel = 2
  let infoTitle = ''
  let infoDesc = ''
  let staticCharge = 0
  let isRubbing = false
  let rubStart = 0
  const rubDuration = 2000
  p.setup = () => {
    w = canvasHostRef.value.clientWidth || 600
    const c = p.createCanvas(w, h)
    c.parent(canvasHostRef.value)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))
    p.noLoop()
    p.redraw()
  }
  p.draw = () => {
    p.background(245)
    drawInfoBox()
    // 옷감
    drawFabric()
    // 마찰대
    drawBlock()
    // 정전기 (볼/불꽃 등)
    drawStaticEffect()
    if (isRubbing) {
      staticCharge = calcStaticCharge()
      drawStaticBalls()
      if (p.millis() - rubStart > rubDuration) {
        isRubbing = false
        p.noLoop()
      }
    }
  }
  function drawInfoBox() {
    if (fabric === 'wool') {
      infoTitle = '양모'
      infoDesc = '양모는 정전기가 많이 발생하는 대표적 옷감입니다.'
    } else if (fabric === 'poly') {
      infoTitle = '폴리에스터'
      infoDesc = '폴리에스터는 양모만큼 정전기 발생량이 높으나, 마찰대에 따라 차이가 있습니다.'
    } else if (fabric === 'cotton') {
      infoTitle = '면'
      infoDesc = '면은 정전기 발생량이 비교적 낮습니다.'
    } else {
      infoTitle = '실크'
      infoDesc = '실크는 중간 정도의 정전기 특성을 보입니다.'
    }
    // 박스 그리기
    const boxW = w * 0.9, boxH = 75
    p.push()
    p.noStroke()
    p.fill(230, 243, 255)
    p.rect(w * 0.05, 16, boxW, boxH, 10)
    p.fill(37, 47, 63)
    p.textSize(16)
    p.text(infoTitle, w * 0.06, 36)
    p.fill(75, 85, 99)
    p.textSize(12)
    p.text(infoDesc, w * 0.06, 58)
    p.pop()
  }
  function drawFabric() {
    p.push()
    p.rectMode(p.CENTER)
    p.fill(245, 222, 179, 220)
    let y = h * 0.45
    let fabricLabel = infoTitle
    p.rect(w * 0.5, y, 210, 80, 14)
    p.fill(49, 49, 56)
    p.textSize(15)
    p.textAlign(p.CENTER, p.CENTER)
    p.text(fabricLabel, w * 0.5, y)
    p.pop()
  }
  function drawBlock() {
    p.push()
    p.ellipseMode(p.CENTER)
    let color = block === 'styro' ? [180, 230, 255] : (block === 'plastic' ? [170, 180, 210] : [220, 220, 220])
    p.fill(...color, 170)
    let y = h * 0.65
    p.ellipse(w * 0.5, y, 170, 35)
    p.fill(60, 60, 63)
    p.textSize(14)
    p.textAlign(p.CENTER, p.CENTER)
    p.text(block === 'styro' ? '스티로폼' : block === 'plastic' ? '플라스틱' : '금속', w * 0.5, y)
    p.pop()
  }
  function drawStaticEffect() {
    // 움직일 때만 정전기 볼 표현
    if (!isRubbing) return
    drawStaticBalls()
  }
  function calcStaticCharge() {
    // 가상 정전기량: 소재별, 마찰대별, 세기별
    // (실제 물리 공식은 소재별 정전기계수, 접촉면적, 마찰 횟수 반영)
    const baseFabric = {
      wool: 1.0,
      poly: 0.85,
      cotton: 0.3,
      silk: 0.55
    }
    const blockFactor = {
      styro: 1.1,
      plastic: 0.9,
      metal: 0.5
    }
    const intensity = [0.7, 1, 1.3]
    return Math.floor(100 * baseFabric[fabric] * blockFactor[block] * intensity[powerLevel-1])
  }
  function drawStaticBalls() {
    const charge = staticCharge
    // 정전기량에 따라 볼 또는 작은 번개효과 띄움 (볼 갯수 많을수록, 번개 길이 길수록 정전기 많음)
    p.push()
    p.noStroke()
    for (let i = 0; i < Math.max(4, Math.floor(charge / 30)); i++) {
      let x = w * 0.5 + p.random(-50, 50)
      let y = h * 0.38 + p.random(-30, 30)
      p.fill(255, 255, 130, 180)
      p.ellipse(x, y, 24 + p.random(12, 22))
    }
    p.pop()
    // 정전기 수치 표시
    p.fill(22, 22, 60)
    p.textSize(15)
    p.text('정전기량: ' + charge + ' (상대값)', w * 0.5, h * 0.86)
  }
  // API for Vue
  p.setFabric = (key) => {
    fabric = key || 'wool'
    staticCharge = calcStaticCharge()
    p.redraw()
  }
  p.setBlock = (key) => {
    block = key || 'styro'
    staticCharge = calcStaticCharge()
    p.redraw()
  }
  p.setLevel = (lv) => {
    powerLevel = lv || 2
    staticCharge = calcStaticCharge()
    p.redraw()
  }
  p.rubOnce = () => {
    isRubbing = true
    rubStart = p.millis()
    staticCharge = calcStaticCharge()
    p.loop()
  }
  p.resetSketch = () => {
    isRubbing = false
    fabric = 'wool'
    block = 'styro'
    powerLevel = 2
    staticCharge = calcStaticCharge()
    p.noLoop()
    p.redraw()
  }
}
onMounted(async () => {
  await nextTick()
  if (!canvasHostRef.value) return
  p5Instance = new p5(sketch, canvasHostRef.value)
  if (p5Instance.setFabric) p5Instance.setFabric(currentFabric.value)
  if (p5Instance.setBlock) p5Instance.setBlock(currentBlock.value)
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
.static-sim {
  background: #F7F7F7;
  border-radius: 12px;
  padding: 16px;
}
.sim-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  margin-bottom: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.icon-circle {
  width: 32px; height: 32px;
  border-radius: 999px; background: #E0F2FE;
  display: flex; align-items: center;
  justify-content: center;
}
.icon-circle .bi {
  font-size: 18px; color: #2563EB;
}
.header-title {
  font-size: 0.97rem; font-weight: 700; color: #111827;
}
.header-actions { display: flex; align-items: center; gap: 8px; }
.chip-btn {
  padding: 6px 10px; border-radius: 999px;
  border: 1px solid #E5E7EB; background: #F9FAFB;
  font-size: 0.78rem; color: #374151;
  cursor: pointer; transition: background 0.15s ease, transform 0.05s ease;
}
.chip-btn.primary { background: #4F46E5; color: #fff; border-color: #4F46E5;}
.chip-btn:active { transform: translateY(1px);}
.sim-control-card {
  margin-top: 10px; margin-bottom: 12px; padding: 10px 12px 12px;
  background: #fff; border-radius: 12px; border: 1px solid #E5E7EB;
}
.control-row {display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px;}
.control-label { font-size: 0.8rem; font-weight: 600; color: #374151;}
.control-options { display: flex; gap: 6px; flex-wrap: wrap;}
.pill-btn { padding: 6px 10px; border-radius: 999px; border: 1px solid #D1D5DB; background: #fff;
  font-size: 0.78rem; color: #374151; cursor: pointer; transition: all 0.15s ease;
}
.pill-btn.active { background: #4F46E5; border-color: #4F46E5; color: #fff;}
.voltage-row { margin-bottom: 0;}
.slider-wrap { display: flex; align-items: center; gap: 10px;}
.slider-wrap input[type='range'] { flex: 1;}
.slider-level { min-width: 2rem; text-align: center; font-size: 0.8rem; font-weight: 600; color: #374151;}
.canvas-card {
  background: #fff; border-radius: 12px; border: 1px solid #E5E7EB; overflow: hidden;
}
.canvas-host { width: 100%; min-height: 320px; background: #fff;}
.canvas-host :deep(canvas) { max-width: 100%; height: auto !important; display: block;}
</style>