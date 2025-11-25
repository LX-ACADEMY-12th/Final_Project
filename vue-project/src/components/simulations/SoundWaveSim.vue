<template>
  <div class="sim-container">
    <header class="sim-header">
      <h2 class="sim-title">🎵 소리의 발생과 특성</h2>
      <div class="status-badge">
        <span class="badge-icon">🔊</span>
        <span class="badge-text">{{ isPlaying ? '진동 중...' : '대기 중' }}</span>
      </div>
    </header>

    <div class="sim-body">
      <div class="canvas-wrapper">
        <div class="canvas-host" ref="canvasHostRef"></div>
        <div class="canvas-label">
          <span>🎸 현의 진동 시각화</span>
        </div>
      </div>

      <section class="control-section">
        <div class="control-group">
          <div class="slider-header">
            <span class="label">진폭 (소리 크기)</span>
            <span class="value">{{ amplitudeLabel }}</span>
          </div>
          <input type="range" min="10" max="60" v-model.number="amplitude" class="custom-range amp-range" />
        </div>

        <div class="control-group">
          <div class="slider-header">
            <span class="label">주파수 (높낮이)</span>
            <span class="value">{{ frequencyLabel }}</span>
          </div>
          <input type="range" min="1" max="5" step="0.5" v-model.number="frequency" class="custom-range freq-range" />
        </div>

        <button class="pluck-btn" @click="pluckString" :disabled="isPlaying">
          <span class="btn-icon">{{ isPlaying ? '〰️' : '🎸' }}</span>
          <span>{{ isPlaying ? '소리가 나는 중...' : '줄 튕기기' }}</span>
        </button>
      </section>

      <section class="info-section">
        <div class="info-card">
          <div class="card-icon">🔊</div>
          <div class="card-content">
            <div class="card-title">소리의 발생</div>
            <div class="card-desc">물체가 진동하면 공기도 함께 흔들려요.</div>
          </div>
        </div>
        <div class="info-card">
          <div class="card-icon">📢</div>
          <div class="card-content">
            <div class="card-title">소리의 세기</div>
            <div class="card-desc">진폭이 클수록 큰 소리가 나요.</div>
          </div>
        </div>
        <div class="info-card">
          <div class="card-icon">🎼</div>
          <div class="card-content">
            <div class="card-title">소리의 높낮이</div>
            <div class="card-desc">주파수가 높으면 높은 소리가 나요.</div>
          </div>
        </div>
      </section>

      <section class="propagation-section">
        <div class="section-header">
          <span class="section-title">소리의 전달 속도</span>
          <span class="section-desc">고체일수록 빨라요!</span>
        </div>
        <div class="medium-row">
          <div class="medium-item" v-for="medium in mediums" :key="medium.name">
            <div class="medium-icon">{{ medium.icon }}</div>
            <div class="medium-info">
              <span class="m-name">{{ medium.name }}</span>
              <span class="m-speed">{{ medium.speed }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import P5 from 'p5'

const canvasHostRef = ref(null)
const p5Instance = ref(null)

const amplitude = ref(30)
const frequency = ref(2)
const isPlaying = ref(false)
const currentTime = ref(0)
const decayFactor = ref(1)

// === Computed ===
const amplitudeLabel = computed(() => {
  if (amplitude.value < 25) return '작은 소리'
  if (amplitude.value < 45) return '보통 소리'
  return '큰 소리'
})

const frequencyLabel = computed(() => {
  if (frequency.value < 2) return '낮은 음'
  if (frequency.value < 4) return '중간 음'
  return '높은 음'
})

const mediums = [
  { name: '공기', speed: '340 m/s', icon: '💨' },
  { name: '물', speed: '1,500 m/s', icon: '💧' },
  { name: '철', speed: '5,000 m/s', icon: '🔩' },
]

// === Audio Logic ===
let animationId = null
let audioContext = null
let oscillator = null
let gainNode = null

function initAudio() {
  if (!audioContext) audioContext = new (window.AudioContext || window.webkitAudioContext)()
  if (audioContext.state === 'suspended') audioContext.resume()
}

function playSound() {
  initAudio()
  if (oscillator) { oscillator.stop(); oscillator.disconnect(); }
  if (gainNode) { gainNode.disconnect(); }

  oscillator = audioContext.createOscillator()
  gainNode = audioContext.createGain()

  const freq = 150 + (frequency.value - 1) * 150
  oscillator.frequency.setValueAtTime(freq, audioContext.currentTime)
  oscillator.type = 'sine'

  const volume = 0.05 + (amplitude.value - 10) * 0.008
  gainNode.gain.setValueAtTime(volume, audioContext.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 2.5)

  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)

  oscillator.start(audioContext.currentTime)
  oscillator.stop(audioContext.currentTime + 2.5)
}

function pluckString() {
  if (isPlaying.value) return
  isPlaying.value = true
  currentTime.value = 0
  decayFactor.value = 1
  playSound()

  const animate = () => {
    currentTime.value += 0.1
    decayFactor.value = Math.exp(-currentTime.value * 0.3)
    if (p5Instance.value) p5Instance.value.redraw()

    if (decayFactor.value > 0.01) {
      animationId = requestAnimationFrame(animate)
    } else {
      isPlaying.value = false
      decayFactor.value = 0
      if (p5Instance.value) p5Instance.value.redraw()
    }
  }
  animate()
}

// === P5 Sketch ===
function createSketch() {
  const sketch = (p) => {
    let w = 0, h = 0

    p.setup = () => {
      // 컨테이너 너비에 맞춤
      w = canvasHostRef.value?.clientWidth || 300
      h = 240 // 고정 높이 (스크롤 내부)
      p.createCanvas(w, h).parent(canvasHostRef.value)
      p.noLoop()
      p.textFont('SUIT, system-ui, sans-serif')
    }

    p.windowResized = () => {
      if (canvasHostRef.value) {
        w = canvasHostRef.value.clientWidth
        p.resizeCanvas(w, h)
        p.redraw()
      }
    }

    p.draw = () => {
      p.clear()
      p.background(248, 250, 252) // 배경색 일치

      const currentAmp = amplitude.value * decayFactor.value
      const currentFreq = frequency.value

      // 1. 기타 줄 영역
      const stringY = 70
      const stringStartX = 40
      const stringEndX = w - 40
      const stringLength = stringEndX - stringStartX

      // 고정점
      p.fill(139, 90, 43); p.noStroke()
      p.rect(stringStartX - 10, stringY - 15, 12, 30, 3)
      p.rect(stringEndX - 2, stringY - 15, 12, 30, 3)

      // 줄 (진동)
      p.stroke(80); p.strokeWeight(3); p.noFill()
      p.beginShape()
      for (let x = stringStartX; x <= stringEndX; x += 2) {
        const normalizedX = (x - stringStartX) / stringLength
        const envelopeVal = Math.sin(normalizedX * Math.PI)
        const waveY = stringY + currentAmp * envelopeVal * Math.sin(currentFreq * normalizedX * Math.PI * 4 + currentTime.value * 10)
        p.vertex(x, waveY)
      }
      p.endShape()

      // 2. 파동 영역 (아래쪽)
      const waveStartY = 150
      const waveHeight = 80
      const centerY = waveStartY + waveHeight / 2

      // 그리드
      p.stroke(226, 232, 240); p.strokeWeight(1)
      p.line(20, centerY, w - 20, centerY) // 중심선

      // 파동 그리기
      p.stroke(59, 130, 246); p.strokeWeight(2); p.noFill()
      p.beginShape()
      for (let x = 20; x <= w - 20; x += 2) {
        const normalizedX = (x - 20) / (w - 40)
        const waveY = centerY + currentAmp * 0.8 * Math.sin(currentFreq * normalizedX * Math.PI * 6 - currentTime.value * 8)
        p.vertex(x, waveY)
      }
      p.endShape()

      // 라벨링
      if (currentAmp > 5) {
        // 진폭 화살표
        const arrowX = w - 40
        p.stroke(239, 68, 68); p.strokeWeight(2)
        p.line(arrowX, centerY - currentAmp * 0.8, arrowX, centerY + currentAmp * 0.8)
        // 파장 라인
        const wavelength = (w - 40) / (currentFreq * 3)
        p.stroke(34, 197, 94)
        p.line(30, centerY + 50, 30 + wavelength, centerY + 50)

        p.noStroke(); p.textSize(10)
        p.fill(239, 68, 68); p.text('진폭', arrowX - 25, centerY)
        p.fill(34, 197, 94); p.text('파장', 30 + wavelength / 2 - 10, centerY + 65)
      }
    }
  }
  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

// === Watchers & Lifecycle ===
// 미리듣기 (Debounce)
let previewTimeout = null
function playPreviewSound() {
  initAudio()
  if (oscillator) { oscillator.stop(); oscillator.disconnect(); }
  if (gainNode) { gainNode.disconnect(); }

  oscillator = audioContext.createOscillator()
  gainNode = audioContext.createGain()

  const freq = 150 + (frequency.value - 1) * 150
  oscillator.frequency.setValueAtTime(freq, audioContext.currentTime)
  oscillator.type = 'sine'

  const volume = 0.03 + (amplitude.value - 10) * 0.005
  gainNode.gain.setValueAtTime(volume, audioContext.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.3)

  oscillator.connect(gainNode); gainNode.connect(audioContext.destination)
  oscillator.start(audioContext.currentTime); oscillator.stop(audioContext.currentTime + 0.3)
}

watch([amplitude, frequency], () => {
  if (p5Instance.value && !isPlaying.value) {
    decayFactor.value = 0.5; p5Instance.value.redraw()
    if (previewTimeout) clearTimeout(previewTimeout)
    previewTimeout = setTimeout(() => { playPreviewSound() }, 100)
    setTimeout(() => {
      if (!isPlaying.value) { decayFactor.value = 0; p5Instance.value.redraw() }
    }, 300)
  }
})

onMounted(() => { nextTick(() => createSketch()) })
onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (oscillator) { oscillator.stop(); oscillator.disconnect(); }
  if (audioContext) audioContext.close()
  if (p5Instance.value) p5Instance.value.remove()
})
</script>

<style scoped>
/* 임베디드 위젯 컨테이너 */
.sim-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 500px;
  /* 최소 높이 보장 */
  background: #ffffff;
  font-family: 'SUIT', sans-serif;
  color: #1e293b;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

/* 1. 헤더 */
.sim-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #f1f5f9;
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

/* 2. 바디 (스크롤 영역) */
.sim-body {
  flex: 1;
  overflow-y: auto;
  /* 내부 스크롤 허용 */
  background: #f8fafc;
}

/* 캔버스 래퍼 */
.canvas-wrapper {
  position: relative;
  background: #f8fafc;
  width: 100%;
  height: 240px;
  /* 캔버스 고정 높이 */
}

.canvas-host {
  width: 100%;
  height: 100%;
}

.canvas-label {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 10px;
  color: #94a3b8;
  font-weight: 500;
}

/* 컨트롤 섹션 */
.control-section {
  padding: 16px;
  background: white;
  border-radius: 20px 20px 0 0;
  /* 둥근 윗 모서리 */
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
}

.value {
  color: #3b82f6;
}

/* Custom Sliders */
.custom-range {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #e2e8f0;
  outline: none;
  -webkit-appearance: none;
}

.custom-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.amp-range::-webkit-slider-thumb {
  background: #ef4444;
}

.freq-range::-webkit-slider-thumb {
  background: #3b82f6;
}

/* Action Button */
.pluck-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  transition: transform 0.1s;
}

.pluck-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.pluck-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

/* Info Cards (Grid) */
.info-section {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.info-card {
  background: white;
  padding: 12px 8px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.card-icon {
  font-size: 20px;
}

.card-title {
  font-size: 11px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 2px;
}

.card-desc {
  font-size: 10px;
  color: #64748b;
  line-height: 1.3;
  word-break: keep-all;
}

/* Propagation Section */
.propagation-section {
  margin: 0 16px 16px;
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}

.section-desc {
  font-size: 11px;
  color: #64748b;
}

.medium-row {
  display: flex;
  gap: 8px;
}

.medium-item {
  flex: 1;
  background: #f8fafc;
  padding: 10px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.medium-icon {
  font-size: 18px;
}

.medium-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.m-name {
  font-size: 11px;
  font-weight: 700;
  color: #334155;
}

.m-speed {
  font-size: 10px;
  color: #3b82f6;
  font-weight: 600;
}

@media (max-width: 400px) {
  .info-section {
    grid-template-columns: 1fr;
  }

  .info-card {
    flex-direction: row;
    text-align: left;
    padding: 12px;
  }

  .card-content {
    display: flex;
    flex-direction: column;
  }
}
</style>
