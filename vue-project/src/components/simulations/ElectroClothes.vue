<template>
  <div class="static-sim" ref="wrapRef">
    <!-- 상단 설명 카드 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-lightning-charge"></i>
        </div>
        <div class="header-text">
          <div class="header-title">어떤 옷이 정전기를<br> 많이 만들까?</div>
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
            :class="['pill-btn', { active: currentFabric === fabric.key }]" @click="selectFabric(fabric.key)">
            {{ fabric.label }}
          </button>
        </div>
      </div>
      <div class="control-row">
        <span class="control-label">마찰대 선택</span>
        <div class="control-options">
          <button v-for="block in blocks" :key="block.key" :class="['pill-btn', { active: currentBlock === block.key }]"
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
const TSHIRT_IMG_PATH = '/Tshirt.png'

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
  level.value = 2
  selectFabric('wool')
  selectBlock('styro')
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
  let h = 220

  let fabric = 'wool'
  let block = 'styro'
  let powerLevel = 2

  let infoTitle = ''
  let infoDesc = ''

  let staticCharge = 0
  let isRubbing = false
  let rubStart = 0
  const rubDuration = 2000

  let clothCenter, blockCenter

  let tshirtImg = null          // 🔵 티셔츠 이미지

  p.setup = () => {
    w = canvasHostRef.value?.clientWidth || 600
    h = 330

    const c = p.createCanvas(w, h)
    c.parent(canvasHostRef.value)
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1))

    // 🔵 여기에서 이미지 로드
    tshirtImg = p.loadImage(
      TSHIRT_IMG_PATH,
      (img) => {
        // ✅ 콜백으로 넘어온 img를 써야 width/height가 제대로 들어옴
        tshirtImg = img
        console.log('tshirt loaded', img.width, img.height)
        p.redraw()
      },
      (err) => {
        console.error('tshirt load error', err)
      }
    )


    blockCenter = { x: w * 0.5, y: h * 0.64 }
    const bodyH = 90
    const blockH = 30
    const gap = 2

    clothCenter = {
      x: w * 0.5,
      y: blockCenter.y - blockH / 2 - gap - bodyH / 2
    }

    staticCharge = 0

    p.noLoop()
    p.redraw()
  }



  p.draw = () => {
    p.background(245)
    drawInfoBox()
    drawStage()

    drawBlock()
    drawFabric()

    if (isRubbing) {
      drawChargeTransfer()
    } else if (staticCharge > 0) {
      drawStaticCharges()
      drawChargeAmountText()
    } else {
      drawNeutralState()
    }

    if (isRubbing && p.millis() - rubStart > rubDuration) {
      isRubbing = false
      p.noLoop()
      p.redraw()
    }
  }

  /* ---------- 상단 설명 박스 (수정 없음) ---------- */
  function drawInfoBox() {
    if (fabric === 'wool') {
      infoTitle = '양모'
      infoDesc = '양모 스웨터는 마찰하면 정전기가 아주 잘 생겨요.'
    } else if (fabric === 'poly') {
      infoTitle = '폴리에스터'
      infoDesc = '폴리에스터는 합성 섬유라 정전기가 잘 쌓이는 옷감이에요.'
    } else if (fabric === 'cotton') {
      infoTitle = '면'
      infoDesc = '면 티셔츠는 정전기가 비교적 덜 생기는 편이에요.'
    } else {
      infoTitle = '실크'
      infoDesc = '실크는 정전기가 중간 정도로 쌓이는 섬유예요.'
    }
    const boxW = w * 0.9
    const boxH = 62
    const x = w * 0.05
    const y = 14
    p.push()
    p.noStroke()
    p.fill(230, 243, 255)
    p.rect(x, y, boxW, boxH, 10)
    p.fill(37, 47, 63)
    p.textSize(15)
    p.textAlign(p.LEFT, p.TOP)
    p.text(infoTitle, x + 14, y + 10)
    p.fill(75, 85, 99)
    p.textSize(12)
    p.text(infoDesc, x + 14, y + 32, boxW - 24, boxH - 30)
    p.pop()
  }

  /* ---------- 배경/바닥 (수정 없음) ---------- */
  function drawStage() {
    p.push()
    p.noStroke()
    p.fill(240)
    p.ellipse(clothCenter.x, clothCenter.y + 10, w * 0.7, 80)
    // 바닥영역
    // p.fill(235)
    // p.rect(0, blockCenter.y + 25, w, h - (blockCenter.y + 25))
    // p.pop()
  }

  /* ---------- 옷감(티셔츠) : public 이미지 사용 ---------- */
  function drawFabric() {
    if (!tshirtImg || !tshirtImg.width || !tshirtImg.height) return

    p.push()

    // 마찰할 때는 좌우로만 살짝 흔들어줌
    let currentX = clothCenter.x
    let currentY = clothCenter.y
    if (isRubbing) {
      const progress = (p.millis() - rubStart) / rubDuration
      const rubAmount = p.sin(progress * p.TWO_PI * 6) * 10
      currentX += rubAmount
    }

    p.translate(currentX, currentY)
    p.imageMode(p.CENTER)

    // 원본 비율 유지하면서 적당한 크기로 축소
    const targetWidth = 140            // 티셔츠 원하는 폭(px)
    const scale = targetWidth / tshirtImg.width
    const imgW = tshirtImg.width * scale
    const imgH = tshirtImg.height * scale

    // 티셔츠 이미지 그리기
    p.image(tshirtImg, 0, 0, imgW, imgH)

    // 티셔츠 아래에 옷감 이름 표시
    p.noStroke()
    p.fill(49, 49, 56)
    p.textSize(13)
    p.textAlign(p.CENTER, p.TOP)
    p.text(infoTitle, 0, 0)

    p.pop()
  }

  /* ---------- 마찰대 (수정 없음) ---------- */
  function drawBlock() {
    p.push()
    p.rectMode(p.CENTER)
    const label =
      block === 'styro' ? '스티로폼' : block === 'plastic' ? '플라스틱' : '금속'
    let baseColor
    if (block === 'styro') baseColor = p.color(185, 230, 255)
    else if (block === 'plastic') baseColor = p.color(196, 196, 214)
    else baseColor = p.color(224, 224, 224)
    p.noStroke()
    p.fill(209, 213, 219, 140)
    p.ellipse(blockCenter.x, blockCenter.y + 10, 210, 26)
    p.fill(baseColor)
    p.rect(blockCenter.x, blockCenter.y, 190, 40, 14)
    p.noFill()
    p.stroke(148, 163, 184)
    p.strokeWeight(1.5)
    p.rect(blockCenter.x, blockCenter.y, 190, 40, 14)
    p.noStroke()
    p.fill(55, 65, 81)
    p.textSize(14)
    p.textAlign(p.CENTER, p.CENTER)
    p.text(label, blockCenter.x, blockCenter.y)
    p.pop()
  }

  /* ---------- 중성 상태 그리기 (수정 없음) ---------- */
  function drawNeutralState() {
    p.push()
    p.textSize(12) // 아이콘 안 텍스트 크기
    p.textAlign(p.CENTER, p.CENTER)
    p.noStroke()

    const neutralColorPlus = p.color(239, 68, 68, 90) // 흐린 빨간색
    const neutralColorMinus = p.color(59, 130, 246, 90) // 흐린 파란색
    const neutralTextColor = p.color(255, 255, 255, 150) // 흐린 흰색
    const iconSize = 12 // 아이콘 크기

    for (let i = 0; i < 3; i++) {
      let x = clothCenter.x + p.random(-40, 40)
      let y = clothCenter.y + p.random(-20, 20)
      p.fill(neutralColorPlus)
      p.ellipse(x - (iconSize / 2 + 1), y, iconSize, iconSize)
      p.fill(neutralTextColor)
      p.text('+', x - (iconSize / 2 + 1), y + 1)
      p.fill(neutralColorMinus)
      p.ellipse(x + (iconSize / 2 + 1), y, iconSize, iconSize)
      p.fill(neutralTextColor)
      p.text('−', x + (iconSize / 2 + 1), y + 1)
    }

    for (let i = 0; i < 3; i++) {
      let x = blockCenter.x + p.random(-50, 50)
      let y = blockCenter.y + p.random(-10, 10)
      p.fill(neutralColorPlus)
      p.ellipse(x - (iconSize / 2 + 1), y, iconSize, iconSize)
      p.fill(neutralTextColor)
      p.text('+', x - (iconSize / 2 + 1), y + 1)
      p.fill(neutralColorMinus)
      p.ellipse(x + (iconSize / 2 + 1), y, iconSize, iconSize)
      p.fill(neutralTextColor)
      p.text('−', x + (iconSize / 2 + 1), y + 1)
    }

    // p.fill(100)
    // p.textSize(12)
    // p.text(
    //   `정전기량: 0 (중성 상태)`,
    //   w * 0.5,
    //   blockCenter.y + 52
    // )
    p.pop()
  }

  /* ---------- 마찰 중 전하 이동 애니메이션 (수정 없음) ---------- */
  function drawChargeTransfer() {
    p.push()
    const { clothSign, blockSign } = getChargeSigns()

    if (blockSign === 0) {
      p.pop()
      return
    }
    let fromY, toY
    const clothRubY = clothCenter.y + 40
    const blockRubY = blockCenter.y - 20
    const movingFromCloth = (clothSign === 1)

    if (movingFromCloth) {
      fromY = clothRubY
      toY = blockRubY
    } else {
      fromY = blockRubY
      toY = clothRubY
    }
    const col = p.color(59, 130, 246)
    col.setAlpha(200)
    p.noStroke()
    p.textSize(18)
    p.textAlign(p.CENTER, p.CENTER)

    const totalToMove = Math.floor(staticCharge / 15) + 3

    for (let i = 0; i < totalToMove; i++) {
      let particleProgress = ((p.frameCount + i * 3) % 20) / 20.0
      let easedProgress = p.sin(particleProgress * p.PI)
      let x = clothCenter.x + p.random(-50, 50)
      let y = p.lerp(fromY, toY, easedProgress)

      p.fill(col)
      p.ellipse(x, y, 22, 22)
      p.fill(255)
      p.text('−', x, y + 1)
    }
    p.pop()
  }

  /* ---------- 정전기 계산 (수정 없음) ---------- */
  function calcStaticCharge() {
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
    return Math.floor(
      100 * baseFabric[fabric] * blockFactor[block] * intensity[powerLevel - 1]
    )
  }

  /* ---------- 부호 계산 (수정 없음) ---------- */
  function getChargeSigns() {
    if (block === 'metal') {
      return { clothSign: +1, blockSign: 0 }
    }
    if (fabric === 'cotton' || fabric === 'silk') {
      return { clothSign: -1, blockSign: +1 }
    }
    return { clothSign: +1, blockSign: -1 }
  }

  /* ---------- 정전기 아이콘 그리기 (수정 없음) ---------- */
  function drawStaticCharges() {
    const { clothSign, blockSign } = getChargeSigns()
    const total = Math.max(6, Math.floor(staticCharge / 20))
    const clothCount = Math.floor(total * 0.6)
    const blockCount = total - clothCount

    p.push()
    p.textAlign(p.CENTER, p.CENTER)
    p.textSize(18)
    p.noStroke()

    for (let i = 0; i < clothCount; i++) {
      const angle = p.random(-p.PI * 0.2, p.PI * 0.2)
      const radius = 70 + p.random(-8, 12)
      const x = clothCenter.x + radius * Math.cos(angle)
      const y = clothCenter.y - 10 + radius * Math.sin(angle)
      const isPositive = clothSign >= 0
      const col = isPositive ? p.color(239, 68, 68) : p.color(59, 130, 246)
      col.setAlpha(230)
      p.fill(col)
      p.ellipse(x, y, 22, 22)
      p.fill(255)
      p.text(isPositive ? '+' : '-', x, y + 1)
    }

    for (let i = 0; i < blockCount && blockSign !== 0; i++) {
      const angle = p.random(p.PI * 0.8, p.PI * 1.2)
      const radius = 80 + p.random(-10, 10)
      const x = blockCenter.x + radius * Math.cos(angle)
      const y = blockCenter.y + radius * Math.sin(angle)
      const isPositive = blockSign >= 0
      const col = isPositive ? p.color(239, 68, 68) : p.color(59, 130, 246)
      col.setAlpha(230)
      p.fill(col)
      p.ellipse(x, y, 22, 22)
      p.fill(255)
      p.text(isPositive ? '+' : '−', x, y + 1)
    }
    p.pop()
  }

  // ===== Vue에서 쓰는 API (수정 없음) =====
  p.setFabric = (key) => {
    fabric = key || 'wool'
    staticCharge = 0
    p.redraw()
  }

  p.setBlock = (key) => {
    block = key || 'styro'
    staticCharge = 0
    p.redraw()
  }

  p.setLevel = (lv) => {
    powerLevel = lv || 2
    staticCharge = 0
    p.redraw()
  }

  p.rubOnce = () => {
    if (isRubbing) return;
    isRubbing = true
    rubStart = p.millis()
    staticCharge = calcStaticCharge()
    p.loop()
  }

  p.resetSketch = () => {
    isRubbing = false
    staticCharge = 0
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
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: #E0F2FE;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle .bi {
  font-size: 18px;
  color: #2563EB;
}

.header-title {
  font-size: 0.97rem;
  font-weight: 700;
  color: #111827;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chip-btn {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #E5E7EB;
  background: #F9FAFB;
  font-size: 0.78rem;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s ease, transform 0.05s ease;
}

.chip-btn.primary {
  background: #4F46E5;
  color: #fff;
  border-color: #4F46E5;
}

.chip-btn:active {
  transform: translateY(1px);
}

.sim-control-card {
  margin-top: 10px;
  margin-bottom: 12px;
  padding: 10px 12px 12px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
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
  border: 1px solid #D1D5DB;
  background: #fff;
  font-size: 0.78rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pill-btn.active {
  background: #4F46E5;
  border-color: #4F46E5;
  color: #fff;
}

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

.canvas-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  overflow: hidden;
}

.canvas-host {
  width: 100%;
  min-height: 220px;
  background: #fff;
}

.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  display: block;
}
</style>
