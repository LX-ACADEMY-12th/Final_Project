<template>
  <div class="lab-container">
    <header class="lab-header">
      <div class="header-content">
        <h2 class="app-title">🧪 물질의 성질 실험실</h2>
        <button class="guide-btn" @click="showGuide = true">
          <span>📖 연구 노트</span>
        </button>
      </div>
    </header>

    <main class="viewport-area">
      <div class="hud-layer">
        <div class="zone-label water-zone">🌊 물 (밀도 실험)</div>
        <div class="zone-label magnet-zone">🧲 자석 (자성 실험)</div>
      </div>

      <div class="canvas-host" ref="canvasHostRef"></div>

      <transition name="pop">
        <div class="status-toast" :class="statusClass" v-if="statusText">
          <span class="toast-icon">{{ statusIcon }}</span>
          <span class="toast-text">{{ statusText }}</span>
        </div>
      </transition>
    </main>

    <section class="control-bar">
      <div class="control-desc">
        물건을 드래그해서 <strong>물</strong>이나 <strong>자석</strong>에 가져가 보세요!
      </div>
      <button class="reset-fab" @click="resetSimulation">
        🔄 정리
      </button>
    </section>

    <div v-if="showGuide" class="modal-overlay" @click.self="showGuide = false">
      <div class="guide-modal">
        <div class="modal-header">
          <h3>📖 연구 노트</h3>
          <button class="close-btn" @click="showGuide = false">✕</button>
        </div>
        <div class="guide-content">
          <div class="guide-item">
            <div class="g-icon">🧱</div>
            <div class="g-text">
              <h4>물체와 물질</h4>
              <p><strong>물체</strong>는 모양이 있는 물건이고, <strong>물질</strong>은 물체를 만드는 재료예요.</p>
            </div>
          </div>
          <div class="guide-item">
            <div class="g-icon">🌊</div>
            <div class="g-text">
              <h4>물에서의 성질 (밀도)</h4>
              <p>나무, 플라스틱은 물에 <strong>뜨고</strong>,<br>철, 유리, 돌은 <strong>가라앉아요</strong>.</p>
            </div>
          </div>
          <div class="guide-item">
            <div class="g-icon">🧲</div>
            <div class="g-text">
              <h4>자석과의 반응 (자성)</h4>
              <p><strong>철</strong>로 된 물체만 자석에 붙어요.<br>(고무, 나무, 유리는 안 붙어요)</p>
            </div>
          </div>
        </div>
        <button class="confirm-btn" @click="showGuide = false">실험하러 가기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import P5 from 'p5'

const canvasHostRef = ref(null)
const p5Instance = ref(null)

// UI 상태
const showGuide = ref(false)
const currentAction = ref('idle') // idle, floating, sinking, magnet_attract, magnet_ignore
const activeObject = ref(null)

// 상태 메시지 로직
const statusText = computed(() => {
  if (!activeObject.value) return null // 평소엔 숨김
  const objName = activeObject.value.name
  const material = activeObject.value.materialName

  switch (currentAction.value) {
    case 'floating': return `${material}로 만든 ${objName}은(는) 물에 떠요!`
    case 'sinking': return `${material}로 만든 ${objName}은(는) 가라앉아요.`
    case 'magnet_attract': return `착! ${material}은(는) 자석에 붙어요!`
    case 'magnet_ignore': return `${material}은(는) 자석에 붙지 않아요.`
    default: return `${objName}(${material})`
  }
})

const statusIcon = computed(() => {
  switch (currentAction.value) {
    case 'floating': return '⛵'
    case 'sinking': return '⚓'
    case 'magnet_attract': return '⚡'
    case 'magnet_ignore': return '💨'
    default: return '🖐️'
  }
})

const statusClass = computed(() => {
  if (['floating', 'magnet_attract'].includes(currentAction.value)) return 'success'
  if (currentAction.value === 'idle') return 'neutral'
  return 'info'
})

// 초기 데이터
const createInitialObjects = (w, h) => {
  // 화면 하단 테이블 위에 배치
  const startY = h - 80
  const gap = w / 6
  return [
    { id: 1, name: '나무 블록', materialName: '나무', x: gap * 1, y: startY, baseX: gap * 1, baseY: startY, icon: '🪵', color: '#8B4513', density: 0.6, magnetic: false },
    { id: 2, name: '철 열쇠', materialName: '철', x: gap * 2, y: startY, baseX: gap * 2, baseY: startY, icon: '🗝️', color: '#64748b', density: 7.8, magnetic: true },
    { id: 3, name: '오리', materialName: '플라스틱', x: gap * 3, y: startY, baseX: gap * 3, baseY: startY, icon: '🐤', color: '#FCD34D', density: 0.9, magnetic: false },
    { id: 4, name: '유리 구슬', materialName: '유리', x: gap * 4, y: startY, baseX: gap * 4, baseY: startY, icon: '🔮', color: '#60A5FA', density: 2.5, magnetic: false },
    { id: 5, name: '지우개', materialName: '고무', x: gap * 5, y: startY, baseX: gap * 5, baseY: startY, icon: '✏️', color: '#F87171', density: 1.5, magnetic: false }
  ]
}

const objects = ref([])
const draggingObj = ref(null)

function resetSimulation() {
  if (!p5Instance.value) return
  const p = p5Instance.value
  objects.value = createInitialObjects(p.width, p.height)
  currentAction.value = 'idle'
  activeObject.value = null
  p.redraw()
}

// P5 Sketch
function createSketch() {
  const sketch = (p) => {
    let w = 0, h = 0
    let waterLevel = 0
    let magnetPos = { x: 0, y: 0 }

    p.setup = () => {
      w = canvasHostRef.value?.clientWidth || window.innerWidth
      h = canvasHostRef.value?.clientHeight || window.innerHeight
      p.createCanvas(w, h).parent(canvasHostRef.value)
      p.textFont('SUIT, system-ui, sans-serif')

      // 반응형 위치 설정
      waterLevel = h * 0.45 // 화면 중간 조금 위부터 물 시작
      magnetPos = { x: w - 80, y: 100 }

      objects.value = createInitialObjects(w, h)
    }

    p.windowResized = () => {
      w = canvasHostRef.value.clientWidth
      h = canvasHostRef.value.clientHeight
      p.resizeCanvas(w, h)
      waterLevel = h * 0.45
      magnetPos = { x: w - 80, y: 100 }

      // 물체들 바닥 위치 재조정 (선택 사항)
      const startY = h - 80
      objects.value.forEach(obj => {
        if (!draggingObj.value) {
          obj.baseY = startY
          if (obj.y > startY) obj.y = startY
        }
      })
    }

    p.draw = () => {
      p.clear()
      p.background(248, 250, 252)

      // === 1. 배경 구역 (수조 & 자석) ===

      // [수조] (화면 왼쪽 절반 차지)
      const tankW = w * 0.55
      const tankH = h - waterLevel - 120 // 하단 테이블 공간 남김

      // 수조 유리벽
      p.noStroke()
      p.fill(224, 242, 254, 100)
      p.rect(20, waterLevel - 20, tankW, tankH + 20, 0, 0, 15, 15)

      // 물 (웨이브 애니메이션)
      p.fill(56, 189, 248, 150) // 맑은 파랑
      p.beginShape()
      p.vertex(20, waterLevel)
      for (let x = 20; x <= 20 + tankW; x += 10) {
        p.vertex(x, waterLevel + Math.sin(p.frameCount * 0.04 + x * 0.03) * 6)
      }
      p.vertex(20 + tankW, waterLevel + tankH)
      p.vertex(20, waterLevel + tankH)
      p.endShape(p.CLOSE)

      // 수조 테두리
      p.stroke(186, 230, 253)
      p.strokeWeight(2)
      p.noFill()
      p.rect(20, waterLevel - 20, tankW, tankH + 20, 0, 0, 15, 15)

      // [자석] (우측 상단)
      p.push()
      p.translate(magnetPos.x, magnetPos.y)
      // 자기장 효과
      p.noFill()
      p.stroke(239, 68, 68, 40 + Math.sin(p.frameCount * 0.1) * 30)
      p.strokeWeight(2)
      p.drawingContext.setLineDash([5, 5])
      p.circle(0, 0, 140)
      p.drawingContext.setLineDash([])

      // 자석 본체
      p.rotate(-0.2)
      p.noStroke()
      // N
      p.fill(239, 68, 68)
      p.rect(-25, -40, 25, 60, 5, 0, 0, 5)
      // S
      p.fill(59, 130, 246)
      p.rect(0, -40, 25, 60, 0, 5, 5, 0)
      // U자 곡선
      p.noFill()
      p.stroke(100)
      p.strokeWeight(25)
      p.strokeCap(p.ROUND)
      p.arc(0, -40, 50, 50, p.PI, 0)

      // 텍스트
      p.noStroke()
      p.fill(255)
      p.textSize(16); p.textStyle(p.BOLD)
      p.text('N', -15, -10)
      p.text('S', 10, -10)
      p.pop()

      // [실험대 바닥]
      p.noStroke()
      p.fill(226, 232, 240)
      p.rect(0, h - 110, w, 110)
      p.fill(203, 213, 225) // 그림자 디테일
      p.rect(0, h - 110, w, 4)

      // === 2. 물체 물리 로직 ===
      objects.value.forEach(obj => {
        let inWater = false
        let nearMagnet = false

        if (draggingObj.value && draggingObj.value.id === obj.id) {
          obj.x = p.mouseX
          obj.y = p.mouseY
        } else {
          // 1. 자석 반응
          const dMag = p.dist(obj.x, obj.y, magnetPos.x, magnetPos.y)
          if (dMag < 100) {
            nearMagnet = true
            if (obj.magnetic) {
              obj.x = p.lerp(obj.x, magnetPos.x - 10, 0.15)
              obj.y = p.lerp(obj.y, magnetPos.y + 30, 0.15)
            } else {
              // 자석 영향 없음 -> 중력
              if (obj.y < obj.baseY) obj.y += 5
            }
          }
          // 2. 물 반응 (수조 영역 내)
          else if (obj.x > 20 && obj.x < 20 + tankW && obj.y > waterLevel) {
            inWater = true
            if (obj.density < 1) { // 뜸
              const floatY = waterLevel + Math.sin(p.frameCount * 0.05 + obj.x) * 5 + 15
              obj.y = p.lerp(obj.y, floatY, 0.05)
            } else { // 가라앉음
              const floorY = waterLevel + tankH - 20
              if (obj.y < floorY) obj.y += 4
            }
            // 물 저항 (X축 감속)
            // obj.x = p.lerp(obj.x, obj.x, 0.9)
          }
          // 3. 공기 중 (바닥으로)
          else {
            if (obj.y < obj.baseY) obj.y += 8
            else obj.y = obj.baseY
          }

          // 벽 충돌 방지
          obj.x = p.constrain(obj.x, 30, w - 30)
          obj.y = p.constrain(obj.y, 0, h - 40)
        }

        // === 3. 물체 그리기 ===
        p.push()
        p.translate(obj.x, obj.y)

        // 그림자
        if (obj.y >= obj.baseY - 5) {
          p.noStroke(); p.fill(0, 0, 0, 15); p.ellipse(0, 25, 50, 10)
        }

        // 본체
        p.stroke(255); p.strokeWeight(3)
        p.fill(obj.color)
        p.circle(0, 0, 56) // 터치하기 좋게 큼직하게

        // 아이콘
        p.noStroke()
        p.textSize(28); p.textAlign(p.CENTER, p.CENTER)
        p.text(obj.icon, 0, 2)

        // 이름표
        p.fill(30, 41, 59); p.textSize(11); p.textStyle(p.BOLD)
        p.text(obj.name, 0, 40)
        p.pop()

        // 상태 업데이트 (드래그 종료 후 UI용)
        if (!draggingObj.value && activeObject.value && activeObject.value.id === obj.id) {
          if (nearMagnet) currentAction.value = obj.magnetic ? 'magnet_attract' : 'magnet_ignore'
          else if (inWater) currentAction.value = obj.density < 1 ? 'floating' : 'sinking'
          else if (obj.y >= obj.baseY - 5) { /* idle 유지 */ }
        }
      })
    }

    p.mousePressed = () => {
      for (let i = objects.value.length - 1; i >= 0; i--) {
        const obj = objects.value[i]
        if (p.dist(p.mouseX, p.mouseY, obj.x, obj.y) < 40) {
          draggingObj.value = obj
          activeObject.value = obj
          currentAction.value = 'idle'
          return
        }
      }
    }
    p.mouseReleased = () => { draggingObj.value = null }
  }
  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => { nextTick(() => createSketch()) })
onBeforeUnmount(() => { if (p5Instance.value) p5Instance.value.remove() })
</script>

<style scoped>
/* Full Screen Container */
.lab-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  background: #f8fafc;
  font-family: 'SUIT', 'Pretendard', system-ui, sans-serif;
  color: #1e293b;
  overflow: hidden;

  /* Safe Area for S24 Ultra */
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 1. Header */
.lab-header {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.guide-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f1f5f9;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
}

/* 2. Viewport */
.viewport-area {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f8fafc;
}

.canvas-host {
  width: 100%;
  height: 100%;
}

/* HUD Labels */
.hud-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 5;
}

.zone-label {
  position: absolute;
  font-size: 14px;
  font-weight: 800;
  color: #3b82f6;
  background: rgba(255, 255, 255, 0.8);
  padding: 6px 12px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.water-zone {
  top: 30%;
  left: 20px;
  color: #0284c7;
}

.magnet-zone {
  top: 80px;
  right: 20px;
  color: #dc2626;
}

/* Status Toast */
.status-toast {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  z-index: 20;
  backdrop-filter: blur(4px);
  white-space: nowrap;
}

.toast-icon {
  font-size: 18px;
}

.toast-text {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.status-toast.success {
  border: 1px solid #4ade80;
}

.status-toast.info {
  border: 1px solid #60a5fa;
}

.status-toast.neutral {
  border: 1px solid #94a3b8;
}

.pop-enter-active,
.pop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px) scale(0.8);
}

/* 3. Control Bar */
.control-bar {
  background: #ffffff;
  padding: 16px 24px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  z-index: 20;
}

.control-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
  flex: 1;
}

.reset-fab {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f1f5f9;
  border: none;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.reset-fab:active {
  transform: scale(0.95);
  background: #e2e8f0;
}

/* 4. Guide Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(3px);
  padding: 20px;
}

.guide-modal {
  background: white;
  width: 100%;
  max-width: 400px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #94a3b8;
}

.guide-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.g-icon {
  font-size: 24px;
  background: #f8fafc;
  padding: 8px;
  border-radius: 12px;
}

.g-text h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.g-text p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.confirm-btn {
  width: 100%;
  padding: 16px;
  background: #3b82f6;
  color: white;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
