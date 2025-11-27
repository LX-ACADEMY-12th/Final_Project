<template>
  <div class="physics-wrap" ref="wrapRef">
    <div class="header">
      <h3 class="title">🍔 소화 기관의 여행</h3>
      <p class="desc">음식을 먹으면 우리 몸 안에서 어떤 여행을 할까요?</p>
    </div>

    <div class="status-panel" :class="digestStatusClass">
      <div class="status-icon">{{ digestStatusIcon }}</div>
      <div class="status-text">{{ digestStatusText }}</div>
    </div>

    <canvas ref="canvasRef" class="canvas-host" width="360" height="340"></canvas>

    <button class="reset-btn" @click="eatFood" :disabled="isDigesting">
      {{ isDigesting ? '🤤 소화 중입니다...' : '🍎 사과 먹기 (시작)' }}
    </button>

    <div class="info-cards">
      <div class="info-card">
        <div class="card-icon">🧪</div>
        <div class="card-title">위 (소화)</div>
        <div class="card-desc">위액이 음식을 죽처럼 녹이고 세균을 없애요.</div>
      </div>
      <div class="info-card">
        <div class="card-icon">✨</div>
        <div class="card-title">소장 (흡수)</div>
        <div class="card-desc">구불구불 길을 지나며 영양소를 몸으로 흡수해요.</div>
      </div>
      <div class="info-card">
        <div class="card-icon">💩</div>
        <div class="card-title">대장 (배설)</div>
        <div class="card-desc">물을 흡수하고 남은 찌꺼기를 밖으로 내보내요.</div>
      </div>
    </div>

    <div class="theory-section">
      <div class="section-title">💡 핵심 원리: 구조와 기능</div>
      <div class="theory-content">
        <div class="formula-box">
          <span class="f-item">음식물</span>
          <span class="f-equal">➡</span>
          <span class="f-item">잘게 쪼개짐 (소화)</span>
          <span class="f-equal">➡</span>
          <span class="f-item">영양소 흡수</span>
          <span class="f-equal">➡</span>
          <span class="f-item">찌꺼기 배출</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const canvasRef = ref(null)
const isDigesting = ref(false)
const currentStage = ref('ready')
const progress = ref(0)
let frameCount = 0
let animationFrameId = null

const digestStatusText = computed(() => {
  switch (currentStage.value) {
    case 'ready': return '버튼을 눌러 사과를 먹어보세요!'
    case 'esophagus': return '꿀꺽! 식도를 타고 내려갑니다.'
    case 'stomach': return '위에서 음식물을 섞고 녹이는 중...'
    case 'small_intestine': return '소장에서 영양분을 쏙쏙 흡수해요!'
    case 'large_intestine': return '대장에서 물을 흡수해 단단해져요.'
    case 'finished': return '화장실 도착! 소화 끝!'
    default: return '소화 기관 관찰하기'
  }
})

const digestStatusIcon = computed(() => {
  switch (currentStage.value) {
    case 'ready': return '🍽️'
    case 'esophagus': return '👄'
    case 'stomach': return '🔥'
    case 'small_intestine': return '✨'
    case 'large_intestine': return '💧'
    case 'finished': return '🚽'
    default: return '🧐'
  }
})

const digestStatusClass = computed(() => {
  if (currentStage.value === 'ready') return 'neutral'
  if (currentStage.value === 'finished') return 'success'
  return 'warning'
})

function eatFood() {
  if (isDigesting.value) return
  isDigesting.value = true
  currentStage.value = 'esophagus'
  progress.value = 0
}

function lerp(a, b, t) {
  return a + (b - a) * t
}

function lerpColor(col1, col2, t) {
  const c1 = parseInt(col1.slice(1), 16)
  const c2 = parseInt(col2.slice(1), 16)

  const r1 = (c1 >> 16) & 255
  const g1 = (c1 >> 8) & 255
  const b1 = c1 & 255

  const r2 = (c2 >> 16) & 255
  const g2 = (c2 >> 8) & 255
  const b2 = c2 & 255

  const r = Math.round(lerp(r1, r2, t))
  const g = Math.round(lerp(g1, g2, t))
  const b = Math.round(lerp(b1, b2, t))

  return `rgb(${r}, ${g}, ${b})`
}

function draw(ctx, w, h, prog, frameCount) {
  if (!ctx) return

  ctx.clearRect(0, 0, w, h)
  ctx.save()

  const cx = w / 2
  const cy = 60
  ctx.translate(cx, cy)

  const skin = '#FFEDDD'

  // ===========================
  // [1] 통짜 말랑 사람 실루엣
  // ===========================
  ctx.fillStyle = skin

  // 몸 전체 (어깨·팔·몸이 한 덩어리인 젤리 모양)
  ctx.beginPath()
  ctx.moveTo(-60, 70)                         // 왼쪽 윗부분
  ctx.quadraticCurveTo(-90, 120, -60, 170)    // 왼쪽 팔/옆구리 볼록
  ctx.quadraticCurveTo(-50, 230, 0, 250)      // 아래쪽 둥근 엉덩이
  ctx.quadraticCurveTo(50, 230, 60, 170)      // 오른쪽 엉덩이 → 옆구리
  ctx.quadraticCurveTo(90, 120, 60, 70)       // 오른쪽 팔/어깨 볼록
  ctx.quadraticCurveTo(0, 40, -60, 70)        // 어깨 라인
  ctx.closePath()
  ctx.fill()

  // 목 (살짝만 표시 – 식도 뒤에 숨는 느낌)
  ctx.beginPath()
  ctx.roundRect?.(-16, 34, 32, 26, 13) ?? (() => {
    // roundRect 없는 브라우저용
    const r = 13
    const x = -16, y = 34, w2 = 32, h2 = 26
    ctx.moveTo(x + r, y)
    ctx.lineTo(x + w2 - r, y)
    ctx.quadraticCurveTo(x + w2, y, x + w2, y + r)
    ctx.lineTo(x + w2, y + h2 - r)
    ctx.quadraticCurveTo(x + w2, y + h2, x + w2 - r, y + h2)
    ctx.lineTo(x + r, y + h2)
    ctx.quadraticCurveTo(x, y + h2, x, y + h2 - r)
    ctx.lineTo(x, y + r)
    ctx.quadraticCurveTo(x, y, x + r, y)
  })()
  ctx.fill()

  // 머리
  ctx.beginPath()
  ctx.arc(0, 0, 45, 0, Math.PI * 2)
  ctx.fill()

  // 얼굴
  ctx.fillStyle = '#333'
  ctx.beginPath()
  ctx.arc(-8, -5, 4, 0, Math.PI * 2)
  ctx.fill()
  ctx.beginPath()
  ctx.arc(8, -5, 4, 0, Math.PI * 2)
  ctx.fill()

  ctx.strokeStyle = '#333'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.arc(0, 5, 8, 0, Math.PI)
  ctx.stroke()

  ctx.fillStyle = '#FFB6C1'
  ctx.beginPath()
  ctx.arc(-15, 5, 6, 0, Math.PI * 2)
  ctx.fill()
  ctx.beginPath()
  ctx.arc(15, 5, 6, 0, Math.PI * 2)
  ctx.fill()

  // ===========================
  // [2] 내부 장기 (몸 안에 꽉 차게)
  // ===========================
  // 폐
  ctx.fillStyle = '#FFD6E7'
  ctx.beginPath()
  ctx.roundRect?.(-40, 95, 30, 60, 15)
  ctx.fill()
  ctx.beginPath()
  ctx.roundRect?.(10, 95, 30, 60, 15)
  ctx.fill()

  // 심장
  ctx.fillStyle = '#FB7185'
  ctx.beginPath()
  ctx.arc(-5, 120, 10, 0, Math.PI * 2)
  ctx.arc(5, 120, 10, 0, Math.PI * 2)
  ctx.moveTo(-15, 125)
  ctx.quadraticCurveTo(0, 145, 15, 125)
  ctx.closePath()
  ctx.fill()

  // 간
  ctx.fillStyle = '#F97316'
  ctx.beginPath()
  ctx.ellipse(28, 175, 35, 18, 0, 0, Math.PI * 2)
  ctx.fill()

  // 위
  ctx.fillStyle = '#FB7185'
  ctx.beginPath()
  ctx.ellipse(-15, 150, 32, 26, 0, 0, Math.PI * 2)
  ctx.fill()

  // 췌장
  ctx.fillStyle = '#FACC15'
  ctx.beginPath()
  ctx.ellipse(-5, 185, 26, 10, 0, 0, Math.PI * 2)
  ctx.fill()

  // 식도 (목 가운데)
  ctx.strokeStyle = '#FFABAB'
  ctx.lineWidth = 10
  ctx.beginPath()
  ctx.moveTo(0, 35)
  ctx.lineTo(0, 115)
  ctx.stroke()

  // 소장
  ctx.strokeStyle = '#FACC15'
  ctx.lineWidth = 8
  ctx.beginPath()
  ctx.moveTo(5, 160)
  ctx.quadraticCurveTo(30, 175, 10, 190)
  ctx.quadraticCurveTo(-20, 200, -15, 220)
  ctx.stroke()

  // 대장 (살짝 둥근 U자)
  ctx.strokeStyle = '#8B5A2B'
  ctx.lineWidth = 18
  ctx.beginPath()
  ctx.moveTo(40, 220)
  ctx.quadraticCurveTo(48, 170, 40, 135)
  ctx.lineTo(0, 130)
  ctx.lineTo(-40, 135)
  ctx.quadraticCurveTo(-48, 170, -40, 220)
  ctx.stroke()

  // ===========================
  // [3] 음식물 애니메이션
  // ===========================
  if (isDigesting.value) {
    let foodX = 0
    let foodY = 0
    let foodSize = 11
    let foodColor = '#FF4B4B'

    if (prog < 15) {
      // 식도
      currentStage.value = 'esophagus'
      foodX = 0
      foodY = 35 + (prog / 15) * 70
      foodColor = '#FF4B4B'
      foodSize = 11
    } else if (prog < 40) {
      // 위
      currentStage.value = 'stomach'
      foodX = -5 + Math.sin(frameCount * 0.2) * 8
      foodY = 150 + Math.cos(frameCount * 0.15) * 4
      const t = (prog - 15) / 25
      foodColor = lerpColor('#FF4B4B', '#FFD700', t)
      foodSize = 10
    } else if (prog < 75) {
      // 소장
      currentStage.value = 'small_intestine'
      const t = (prog - 40) / 35
      foodX = Math.sin(t * Math.PI * 3) * 18
      foodY = 170 + t * 55
      foodColor = '#FFD700'
      foodSize = 10 - t * 3

      if (frameCount % 10 < 5) {
        ctx.strokeStyle = 'rgba(255, 255, 200, 0.8)'
        ctx.lineWidth = 2
        ctx.beginPath()
        ctx.arc(foodX, foodY, foodSize + 8, 0, Math.PI * 2)
        ctx.stroke()
      }
    } else if (prog < 95) {
      // 대장
      currentStage.value = 'large_intestine'
      const t = (prog - 75) / 20
      if (t < 0.33) {
        foodX = 30 + t * (10 / 0.33)
        foodY = 220 - t * (85 / 0.33)
      } else if (t < 0.66) {
        foodX = 40 - (t - 0.33) * (80 / 0.33)
        foodY = 135 + Math.sin(t * 30) * 2
      } else {
        foodX = -40
        foodY = 135 + (t - 0.66) * (85 / 0.34)
      }
      const ct = (prog - 75) / 20
      foodColor = lerpColor('#FFD700', '#8B4513', ct)
      foodSize = 7
    } else if (prog < 110) {
      // 배설
      currentStage.value = 'finished'
      foodX = -40
      foodY = 220 + ((prog - 95) / 15) * 60
      foodColor = '#8B4513'
      foodSize = 7
    } else {
      isDigesting.value = false
      currentStage.value = 'ready'
      progress.value = 0
    }

    ctx.fillStyle = foodColor
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)'
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.arc(foodX, foodY, foodSize, 0, Math.PI * 2)
    ctx.fill()
    ctx.stroke()
  }

  ctx.restore()
}

function animate() {
  const canvas = canvasRef.value
  if (!canvas) return

  frameCount++

  // 느리게 소화 진행
  if (isDigesting.value) {
    progress.value += 0.25
  }

  const ctx = canvas.getContext('2d')
  draw(ctx, canvas.width, canvas.height, progress.value, frameCount)

  animationFrameId = requestAnimationFrame(animate)
}

onMounted(() => {
  animate()
})

onBeforeUnmount(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})
</script>


<style scoped>
.physics-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  font-family: 'SUIT', system-ui, sans-serif;
  max-width: 100%;
}

.header {
  text-align: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.status-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.status-panel.neutral {
  border-left: 4px solid #94a3b8;
}

.status-panel.success {
  border-left: 4px solid #10b981;
  background: #ecfdf5;
}

.status-panel.warning {
  border-left: 4px solid #f59e0b;
  background: #fffbeb;
}

.status-icon {
  font-size: 20px;
}

.status-text {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.canvas-host {
  width: 100%;
  display: block;
  background: #fafcff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
  touch-action: none;
  margin-bottom: 12px;
}

.reset-btn {
  width: 100%;
  padding: 12px;
  margin: 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.reset-btn:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.info-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 10px 8px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.card-title {
  font-size: 11px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 10px;
  color: #64748b;
  line-height: 1.4;
  word-break: keep-all;
}

.theory-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.formula-box {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  padding: 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  flex-wrap: wrap;
}

.f-item {
  color: #3b82f6;
}

.f-equal {
  color: #64748b;
  margin: 0 2px;
}

@media (max-width: 400px) {
  .info-cards {
    grid-template-columns: 1fr;
  }

  .info-card {
    display: flex;
    align-items: center;
    text-align: left;
    gap: 12px;
    padding: 12px;
  }

  .card-icon {
    margin-bottom: 0;
  }
}
</style>
