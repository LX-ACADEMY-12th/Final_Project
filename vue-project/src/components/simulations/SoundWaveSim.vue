<template>
  <div class="sound-wrap" ref="wrapRef">
    <!-- 상단 설명 -->
    <div class="header">
      <h3 class="title">🎵 소리의 발생과 특성</h3>
      <p class="desc">줄을 튕겨서 소리가 어떻게 만들어지는지 알아보세요!</p>
    </div>

    <!-- 컨트롤 패널 -->
    <div class="control-panel">
      <div class="control-row">
        <span class="control-label">진폭 (소리 세기)</span>
        <input type="range" min="10" max="60" v-model.number="amplitude" class="slider" />
        <span class="control-value">{{ amplitudeLabel }}</span>
      </div>
      <div class="control-row">
        <span class="control-label">주파수 (소리 높낮이)</span>
        <input type="range" min="1" max="5" step="0.5" v-model.number="frequency" class="slider" />
        <span class="control-value">{{ frequencyLabel }}</span>
      </div>
    </div>

    <!-- p5 캔버스 -->
    <div class="canvas-host" ref="canvasHostRef"></div>

    <!-- 줄 튕기기 버튼 -->
    <button class="pluck-btn" @click="pluckString" :disabled="isPlaying">
      {{ isPlaying ? '진동 중...' : '🎸 줄 튕기기' }}
    </button>

    <!-- 정보 카드들 -->
    <div class="info-cards">
      <div class="info-card">
        <div class="card-icon">🔊</div>
        <div class="card-title">소리의 발생</div>
        <div class="card-desc">물체가 진동하면 주변 공기도 함께 진동하여 소리가 만들어져요.</div>
      </div>
      <div class="info-card">
        <div class="card-icon">📢</div>
        <div class="card-title">소리의 세기</div>
        <div class="card-desc">진폭이 클수록 소리가 커요. (진동 폭이 크면 큰 소리)</div>
      </div>
      <div class="info-card">
        <div class="card-icon">🎼</div>
        <div class="card-title">소리의 높낮이</div>
        <div class="card-desc">주파수가 높으면 높은 소리, 낮으면 낮은 소리가 나요.</div>
      </div>
    </div>

    <!-- 파동 전달 시각화 -->
    <div class="propagation-section">
      <div class="section-title">소리의 전달</div>
      <div class="propagation-desc">
        소리는 공기 분자의 진동이 퍼져나가는 것이에요. 진공에서는 소리가 전달되지 않아요!
      </div>
      <div class="medium-row">
        <div class="medium-item" v-for="medium in mediums" :key="medium.name">
          <div class="medium-icon">{{ medium.icon }}</div>
          <div class="medium-name">{{ medium.name }}</div>
          <div class="medium-speed">{{ medium.speed }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import P5 from 'p5'

const wrapRef = ref(null)
const canvasHostRef = ref(null)
const p5Instance = ref(null)

const amplitude = ref(30)      // 진폭 (10~60)
const frequency = ref(2)       // 주파수 (1~5)
const isPlaying = ref(false)
const currentTime = ref(0)
const decayFactor = ref(1)

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
  { name: '공기', speed: '약 340 m/s', icon: '💨' },
  { name: '물', speed: '약 1,500 m/s', icon: '💧' },
  { name: '철', speed: '약 5,000 m/s', icon: '🔩' },
]

let animationId = null
let audioContext = null
let oscillator = null
let gainNode = null

// 오디오 컨텍스트 초기화
function initAudio() {
  if (!audioContext) {
    audioContext = new (window.AudioContext || window.webkitAudioContext)()
  }
  if (audioContext.state === 'suspended') {
    audioContext.resume()
  }
}

// 소리 재생
function playSound() {
  initAudio()

  // 이전 소리 정리
  if (oscillator) {
    oscillator.stop()
    oscillator.disconnect()
  }
  if (gainNode) {
    gainNode.disconnect()
  }

  // 새 오실레이터 생성
  oscillator = audioContext.createOscillator()
  gainNode = audioContext.createGain()

  // 주파수 설정 (frequency.value 1~5를 200Hz~800Hz로 매핑)
  const freq = 150 + (frequency.value - 1) * 150 // 150Hz ~ 750Hz
  oscillator.frequency.setValueAtTime(freq, audioContext.currentTime)
  oscillator.type = 'sine' // 부드러운 사인파

  // 볼륨 설정 (amplitude.value 10~60을 0.1~0.5로 매핑)
  const volume = 0.05 + (amplitude.value - 10) * 0.008
  gainNode.gain.setValueAtTime(volume, audioContext.currentTime)

  // 감쇠 효과 (서서히 소리가 줄어듦)
  gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 2.5)

  // 연결
  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)

  // 재생
  oscillator.start(audioContext.currentTime)
  oscillator.stop(audioContext.currentTime + 2.5)
}

function pluckString() {
  if (isPlaying.value) return

  isPlaying.value = true
  currentTime.value = 0
  decayFactor.value = 1

  // 소리 재생
  playSound()

  const animate = () => {
    currentTime.value += 0.1
    decayFactor.value = Math.exp(-currentTime.value * 0.3)

    if (p5Instance.value) {
      p5Instance.value.redraw()
    }

    if (decayFactor.value > 0.01) {
      animationId = requestAnimationFrame(animate)
    } else {
      isPlaying.value = false
      decayFactor.value = 0
      if (p5Instance.value) {
        p5Instance.value.redraw()
      }
    }
  }

  animate()
}

function createSketch() {
  const sketch = (p) => {
    let w = 0
    let h = 0

    p.setup = () => {
      w = canvasHostRef.value?.clientWidth || 360
      h = 280
      const canvas = p.createCanvas(w, h)
      canvas.parent(canvasHostRef.value)
      p.noLoop()
      p.textFont('SUIT, system-ui, sans-serif')
    }

    p.draw = () => {
      p.clear()
      p.background(250, 252, 255)

      const currentAmp = amplitude.value * decayFactor.value
      const currentFreq = frequency.value

      // =================== 기타/현 그리기 ===================
      const stringY = 80
      const stringStartX = 40
      const stringEndX = w - 40
      const stringLength = stringEndX - stringStartX

      // 현 고정점 (너트, 브릿지)
      p.fill(139, 90, 43)
      p.noStroke()
      p.rect(stringStartX - 10, stringY - 15, 12, 30, 3)
      p.rect(stringEndX - 2, stringY - 15, 12, 30, 3)

      // 진동하는 현 그리기
      p.stroke(80, 80, 80)
      p.strokeWeight(3)
      p.noFill()

      p.beginShape()
      for (let x = stringStartX; x <= stringEndX; x += 2) {
        const normalizedX = (x - stringStartX) / stringLength
        // 양 끝은 고정, 중앙이 최대 진폭
        const envelopeVal = Math.sin(normalizedX * Math.PI)
        const waveY = stringY + currentAmp * envelopeVal *
          Math.sin(currentFreq * normalizedX * Math.PI * 4 + currentTime.value * 10)
        p.vertex(x, waveY)
      }
      p.endShape()

      // =================== 파동 시각화 ===================
      const waveStartY = 160
      const waveHeight = 100

      // 배경 그리드
      p.stroke(230)
      p.strokeWeight(1)
      for (let y = waveStartY; y <= waveStartY + waveHeight; y += 20) {
        p.line(20, y, w - 20, y)
      }

      // 중심선
      p.stroke(180)
      p.strokeWeight(1)
      const centerY = waveStartY + waveHeight / 2
      p.line(20, centerY, w - 20, centerY)

      // 파동 그리기
      p.stroke(59, 130, 246)
      p.strokeWeight(3)
      p.noFill()

      p.beginShape()
      for (let x = 20; x <= w - 20; x += 2) {
        const normalizedX = (x - 20) / (w - 40)
        const waveY = centerY + currentAmp * 0.8 *
          Math.sin(currentFreq * normalizedX * Math.PI * 6 - currentTime.value * 8)
        p.vertex(x, waveY)
      }
      p.endShape()

      // =================== 라벨 ===================
      p.noStroke()
      p.fill(100)
      p.textAlign(p.CENTER, p.CENTER)
      p.textSize(11)
      p.text('진동하는 줄', w / 2, stringY - 35)
      p.text('소리 파동', w / 2, waveStartY - 12)

      // 진폭 표시 화살표
      if (currentAmp > 5) {
        const arrowX = w - 50
        p.stroke(239, 68, 68)
        p.strokeWeight(2)
        p.line(arrowX, centerY - currentAmp * 0.8, arrowX, centerY + currentAmp * 0.8)

        // 화살표 머리
        p.line(arrowX - 5, centerY - currentAmp * 0.8 + 5, arrowX, centerY - currentAmp * 0.8)
        p.line(arrowX + 5, centerY - currentAmp * 0.8 + 5, arrowX, centerY - currentAmp * 0.8)
        p.line(arrowX - 5, centerY + currentAmp * 0.8 - 5, arrowX, centerY + currentAmp * 0.8)
        p.line(arrowX + 5, centerY + currentAmp * 0.8 - 5, arrowX, centerY + currentAmp * 0.8)

        p.noStroke()
        p.fill(239, 68, 68)
        p.textSize(10)
        p.text('진폭', arrowX, centerY)
      }

      // 파장 표시
      if (currentAmp > 5) {
        const wavelength = (w - 40) / (currentFreq * 3)
        const waveMarkY = waveStartY + waveHeight + 15

        p.stroke(34, 197, 94)
        p.strokeWeight(2)
        p.line(20, waveMarkY, 20 + wavelength, waveMarkY)

        // 양쪽 끝 표시
        p.line(20, waveMarkY - 5, 20, waveMarkY + 5)
        p.line(20 + wavelength, waveMarkY - 5, 20 + wavelength, waveMarkY + 5)

        p.noStroke()
        p.fill(34, 197, 94)
        p.textSize(10)
        p.text('파장 (1주기)', 20 + wavelength / 2, waveMarkY + 12)
      }

      // 상태 표시
      if (!isPlaying.value && decayFactor.value === 0) {
        p.fill(150)
        p.textSize(12)
        p.text('버튼을 눌러 줄을 튕겨보세요!', w / 2, stringY + 30)
      }
    }
  }

  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => {
  createSketch()
})

onBeforeUnmount(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (oscillator) {
    oscillator.stop()
    oscillator.disconnect()
  }
  if (gainNode) {
    gainNode.disconnect()
  }
  if (audioContext) {
    audioContext.close()
  }
  if (p5Instance.value) {
    p5Instance.value.remove()
    p5Instance.value = null
  }
})

// 슬라이더 조절 시 짧은 미리듣기 소리
let previewTimeout = null

function playPreviewSound() {
  initAudio()

  if (oscillator) {
    oscillator.stop()
    oscillator.disconnect()
  }
  if (gainNode) {
    gainNode.disconnect()
  }

  oscillator = audioContext.createOscillator()
  gainNode = audioContext.createGain()

  const freq = 150 + (frequency.value - 1) * 150
  oscillator.frequency.setValueAtTime(freq, audioContext.currentTime)
  oscillator.type = 'sine'

  const volume = 0.03 + (amplitude.value - 10) * 0.005
  gainNode.gain.setValueAtTime(volume, audioContext.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.3)

  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)

  oscillator.start(audioContext.currentTime)
  oscillator.stop(audioContext.currentTime + 0.3)
}

watch([amplitude, frequency], () => {
  if (p5Instance.value && !isPlaying.value) {
    // 미리보기용으로 잠깐 보여주기
    decayFactor.value = 0.5
    p5Instance.value.redraw()

    // 디바운스된 미리듣기 소리
    if (previewTimeout) clearTimeout(previewTimeout)
    previewTimeout = setTimeout(() => {
      playPreviewSound()
    }, 100)

    setTimeout(() => {
      if (!isPlaying.value) {
        decayFactor.value = 0
        p5Instance.value.redraw()
      }
    }, 300)
  }
})
</script>

<style scoped>
.sound-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  font-family: 'SUIT', system-ui, sans-serif;
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

.control-panel {
  background: #ffffff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.control-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.control-row:last-child {
  margin-bottom: 0;
}

.control-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  min-width: 110px;
}

.slider {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #e2e8f0;
  border-radius: 3px;
  outline: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  background: #3b82f6;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.control-value {
  font-size: 11px;
  color: #3b82f6;
  font-weight: 600;
  min-width: 60px;
  text-align: right;
}

.canvas-host {
  width: 100%;
  min-height: 280px;
  background: #fafcff;
  border-radius: 12px;
  overflow: hidden;
}

.pluck-btn {
  width: 100%;
  padding: 14px;
  margin: 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.pluck-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.pluck-btn:active:not(:disabled) {
  transform: translateY(0);
}

.pluck-btn:disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
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
  font-size: 24px;
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
}

.propagation-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.propagation-desc {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 10px;
  line-height: 1.5;
}

.medium-row {
  display: flex;
  justify-content: space-around;
  gap: 8px;
}

.medium-item {
  text-align: center;
  flex: 1;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
}

.medium-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.medium-name {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.medium-speed {
  font-size: 10px;
  color: #3b82f6;
  font-weight: 500;
}

/* 모바일 최적화 */
@media (max-width: 400px) {
  .sound-wrap {
    padding: 12px;
  }

  .info-cards {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .info-card {
    display: flex;
    align-items: center;
    text-align: left;
    gap: 10px;
  }

  .card-icon {
    font-size: 28px;
    margin-bottom: 0;
  }

  .card-title {
    margin-bottom: 2px;
  }
}
</style>
