<template>
  <div class="physics-wrap" ref="wrapRef">
    <div class="header">
      <h3 class="title">⚖️ 양팔 저울로 배우는 수평 잡기</h3>
      <p class="desc">물체를 저울 근처로 가져가면 자석처럼 붙어요!</p>
    </div>

    <div class="status-panel" :class="balanceStatusClass">
      <div class="status-icon">{{ balanceStatusIcon }}</div>
      <div class="status-text">{{ balanceStatusText }}</div>
    </div>

    <div class="canvas-host" ref="canvasHostRef"></div>

    <button class="reset-btn" @click="resetSimulation">
      🔄 다시 하기 (초기화)
    </button>

    <div class="info-cards">
      <div class="info-card">
        <div class="card-icon">🧱</div>
        <div class="card-title">무게</div>
        <div class="card-desc">물체가 무거울수록 저울을 누르는 힘이 커져요.</div>
      </div>
      <div class="info-card">
        <div class="card-icon">📏</div>
        <div class="card-title">거리</div>
        <div class="card-desc">중심에서 멀리 놓을수록 힘이 더 세져요.</div>
      </div>
      <div class="info-card">
        <div class="card-icon">🤝</div>
        <div class="card-title">수평의 원리</div>
        <div class="card-desc">양쪽의 (무게 × 거리)가 같으면 수평이 돼요!</div>
      </div>
    </div>

    <div class="theory-section">
      <div class="section-title">💡 핵심 원리: 지레의 법칙</div>
      <div class="theory-content">
        <div class="formula-box">
          <span class="f-item">왼쪽 (무게 × 거리)</span>
          <span class="f-equal">=</span>
          <span class="f-item">오른쪽 (무게 × 거리)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import P5 from 'p5'

const canvasHostRef = ref(null)
const p5Instance = ref(null)

// 시뮬레이션 상태
const beamAngle = ref(0)
const targetAngle = ref(0)
const objects = ref([])
const draggingObj = ref(null)
const hoveredSlot = ref(null) // 현재 마우스가 가리키고 있는 슬롯 (미리보기용)

// 초기 물체 생성
const createInitialObjects = (w, h) => {
  return [
    { id: 1, weight: 10, color: '#3b82f6', x: 50, y: h - 50, baseX: 50, baseY: h - 50, slot: null, label: '10' },
    { id: 2, weight: 10, color: '#3b82f6', x: 110, y: h - 50, baseX: 110, baseY: h - 50, slot: null, label: '10' },
    { id: 3, weight: 20, color: '#ef4444', x: 170, y: h - 50, baseX: 170, baseY: h - 50, slot: null, label: '20' },
    { id: 4, weight: 30, color: '#10b981', x: 230, y: h - 50, baseX: 230, baseY: h - 50, slot: null, label: '30' },
  ]
}

// 상태 계산
const leftTorque = computed(() => {
  return objects.value
    .filter(o => o.slot !== null && o.slot < 0)
    .reduce((sum, o) => sum + (o.weight * Math.abs(o.slot)), 0)
})

const rightTorque = computed(() => {
  return objects.value
    .filter(o => o.slot !== null && o.slot > 0)
    .reduce((sum, o) => sum + (o.weight * Math.abs(o.slot)), 0)
})

const balanceStatusText = computed(() => {
  if (leftTorque.value === 0 && rightTorque.value === 0) return '물체를 올려놓아 보세요.'
  if (leftTorque.value === rightTorque.value) return '완벽한 수평입니다! (평형)'
  if (leftTorque.value > rightTorque.value) return '왼쪽이 더 무거워요!'
  return '오른쪽이 더 무거워요!'
})

const balanceStatusIcon = computed(() => {
  if (leftTorque.value === 0 && rightTorque.value === 0) return '🤔'
  if (leftTorque.value === rightTorque.value) return '🎉'
  return '⚖️'
})

const balanceStatusClass = computed(() => {
  if (leftTorque.value === 0 && rightTorque.value === 0) return 'neutral'
  if (leftTorque.value === rightTorque.value) return 'success'
  return 'warning'
})

function resetSimulation() {
  if (!p5Instance.value) return
  const p = p5Instance.value
  objects.value = createInitialObjects(p.width, p.height)
  beamAngle.value = 0
  targetAngle.value = 0
  hoveredSlot.value = null
  p.redraw()
}

function createSketch() {
  const sketch = (p) => {
    let w = 0
    let h = 0

    // 설정 상수
    const BEAM_W = 280
    const SLOT_DIST = 30
    const SNAP_DISTANCE = 50 // 자석 감지 범위 (픽셀)

    p.setup = () => {
      w = canvasHostRef.value?.clientWidth || 360
      h = 320
      const canvas = p.createCanvas(w, h)
      canvas.parent(canvasHostRef.value)
      p.textFont('SUIT, system-ui, sans-serif')
      objects.value = createInitialObjects(w, h)
    }

    p.draw = () => {
      p.clear()
      p.background(250, 252, 255)

      // 1. 물리 계산
      const diff = rightTorque.value - leftTorque.value
      if (diff === 0) targetAngle.value = 0
      else if (diff > 0) targetAngle.value = 20 * (Math.PI / 180)
      else targetAngle.value = -20 * (Math.PI / 180)

      beamAngle.value = p.lerp(beamAngle.value, targetAngle.value, 0.1)

      const centerX = w / 2
      const centerY = h / 2 - 20

      // 2. 저울 그리기
      p.push()
      p.translate(centerX, centerY)

      // 받침점
      p.fill(100)
      p.noStroke()
      p.triangle(0, 0, -15, 30, 15, 30)

      // 저울대 회전
      p.rotate(beamAngle.value)

      // 저울대 몸체
      p.fill(203, 213, 225)
      p.stroke(148, 163, 184)
      p.strokeWeight(2)
      p.rectMode(p.CENTER)
      p.rect(0, 0, BEAM_W + 20, 10, 5)

      // 눈금
      p.textAlign(p.CENTER, p.CENTER)
      p.textSize(10)
      p.noStroke()

      for (let i = -4; i <= 4; i++) {
        if (i === 0) continue
        const xPos = i * SLOT_DIST

        // 눈금선
        p.stroke(148, 163, 184)
        p.strokeWeight(1)
        p.line(xPos, -5, xPos, 5)

        // 숫자
        p.noStroke()
        p.fill(100)
        p.text(Math.abs(i), xPos, 15)
      }
      p.pop()

      // 3. 드래그 중 자석 감지 및 미리보기 그리기
      hoveredSlot.value = null

      if (draggingObj.value) {
        let minMsgDist = SNAP_DISTANCE
        let closestSlot = null
        let closestX = 0
        let closestY = 0

        // 모든 슬롯 위치를 계산해서 마우스와 가장 가까운 슬롯 찾기
        for (let i = -4; i <= 4; i++) {
          if (i === 0) continue

          // 현재 저울 기울기 반영한 슬롯 좌표 계산
          const slotDist = i * SLOT_DIST
          const rotX = centerX + slotDist * Math.cos(beamAngle.value)
          const rotY = centerY + slotDist * Math.sin(beamAngle.value)

          const d = p.dist(p.mouseX, p.mouseY, rotX, rotY)

          // 이미 물체가 있는 슬롯은 제외
          const isOccupied = objects.value.some(o => o.slot === i && o.id !== draggingObj.value.id)

          if (!isOccupied && d < minMsgDist) {
            minMsgDist = d
            closestSlot = i
            closestX = rotX
            closestY = rotY
          }
        }

        // 감지된 슬롯이 있으면 미리보기 표시 (Ghost UI)
        if (closestSlot !== null) {
          hoveredSlot.value = closestSlot
          p.noStroke()
          p.fill(draggingObj.value.color)
          // 투명도 줘서 미리보기 느낌 내기 (RGBA)
          const c = p.color(draggingObj.value.color)
          c.setAlpha(100)
          p.fill(c)
          p.circle(closestX, closestY, 30)

          // 가이드 선
          p.stroke(draggingObj.value.color)
          p.strokeWeight(1)
          p.drawingContext.setLineDash([5, 5]) // 점선
          p.line(p.mouseX, p.mouseY, closestX, closestY)
          p.drawingContext.setLineDash([]) // 점선 해제
        }
      }

      // 4. 물체 그리기
      objects.value.forEach(obj => {
        // 드래그 중인 물체
        if (draggingObj.value && draggingObj.value.id === obj.id) {
          obj.x = p.mouseX
          obj.y = p.mouseY
        }
        // 저울에 올라간 물체
        else if (obj.slot !== null) {
          const dist = obj.slot * SLOT_DIST
          const cosA = Math.cos(beamAngle.value)
          const sinA = Math.sin(beamAngle.value)

          // 물체 높이만큼 살짝 위(-18)로 띄움
          const localX = dist
          const localY = -18

          obj.x = centerX + (localX * cosA - localY * sinA)
          obj.y = centerY + (localX * sinA + localY * cosA)
        }

        // 그리기
        p.stroke(255)
        p.strokeWeight(2)

        // 그림자
        p.noStroke()
        p.fill(0, 0, 0, 20)
        const size = 25 + (obj.weight / 5)
        p.ellipse(obj.x + 2, obj.y + 2, size, size)

        // 본체
        p.stroke(255)
        p.fill(obj.color)
        if (obj.weight >= 30) p.rectMode(p.CENTER).rect(obj.x, obj.y, size, size, 6)
        else p.ellipse(obj.x, obj.y, size, size)

        // 텍스트
        p.fill(255)
        p.noStroke()
        p.textAlign(p.CENTER, p.CENTER)
        p.textSize(12)
        p.text(obj.label, obj.x, obj.y)
      })
    }

    p.mousePressed = () => {
      // 위에서부터 클릭 체크 (겹쳤을 때 위에 있는 것 잡기)
      for (let i = objects.value.length - 1; i >= 0; i--) {
        const obj = objects.value[i]
        const d = p.dist(p.mouseX, p.mouseY, obj.x, obj.y)
        if (d < 25) { // 터치 범위도 25로 살짝 여유 있게
          draggingObj.value = obj
          obj.slot = null
          return
        }
      }
    }

    p.mouseReleased = () => {
      if (!draggingObj.value) return

      const obj = draggingObj.value

      // 드래그 중 계산해둔 hoveredSlot이 있으면 거기로 스냅
      if (hoveredSlot.value !== null) {
        obj.slot = hoveredSlot.value
        // 스냅 성공음 효과(옵션)나 햅틱 피드백을 줄 수도 있음
      } else {
        // 어디에도 속하지 않으면 원래 자리(바닥)로
        obj.x = obj.baseX
        obj.y = obj.baseY
        obj.slot = null
      }

      draggingObj.value = null
      hoveredSlot.value = null
    }
  }

  p5Instance.value = new P5(sketch, canvasHostRef.value)
}

onMounted(() => {
  createSketch()
})

onBeforeUnmount(() => {
  if (p5Instance.value) {
    p5Instance.value.remove()
    p5Instance.value = null
  }
})
</script>

<style scoped>
/* 스타일은 기존과 동일합니다. 그대로 두셔도 됩니다. */
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
  height: 320px;
  background: #fafcff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
  touch-action: none;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.formula-box {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  padding: 8px;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
}

.f-item {
  color: #3b82f6;
}

.f-equal {
  color: #64748b;
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

  .formula-box {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
