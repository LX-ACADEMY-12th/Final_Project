<template>
  <div class="magnet-wrap" ref="wrapRef">
    <div class="toolbar">
      <div class="row">
        <label class="radio">
          <input type="radio" value="bar" v-model="mode" />
          <span>막대자석</span>
        </label>
        <label class="radio">
          <input type="radio" value="earth" v-model="mode" />
          <span>지구</span>
        </label>
      </div>

      <div class="row">
        <label class="check">
          <input type="checkbox" v-model="autoRotate" />
          <span>Auto Rotate</span>
        </label>
      </div>

      <div class="row">
        <span class="label">자침의 두께</span>
        <input type="range" min="1" max="5" step="0.1" v-model.number="needleThickness" />
        <span class="val">{{ needleThickness.toFixed(1) }}</span>
      </div>
    </div>

    <div class="canvas-host" ref="hostRef"></div>
    <p class="helper">마우스(또는 터치) 드래그로 회전합니다.</p>
  </div>
</template>

<script setup>
// Vue & p5
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import P5 from 'p5'

// ====== Refs / State ======
const hostRef = ref(null)
const wrapRef = ref(null)

const mode = ref('bar') // 'bar' | 'earth'
const autoRotate = ref(true)
const needleThickness = ref(2)

let p5Instance = null

// ====== p5 내부 상태 ======
let rotX = -0.3
let rotY = 0
let dragged = false
let pmx = 0
let pmy = 0

const needleX = []
const needleY = []
const needleZ = []
const vectorX = []
const vectorY = []
const vectorZ = []

let barImg = null
let earthImg = null

// ====== 유틸 ======
function buildNeedlesAndVectors(p) {
  needleX.length = 0
  needleY.length = 0
  needleZ.length = 0
  vectorX.length = 0
  vectorY.length = 0
  vectorZ.length = 0

  const getPosition = (r) => {
    let x = 0, y = 0, z = 0
    for (let n = 0; n < 500; n++) {
      if (r > 10) {
        x = p.random(-350, 350)
        y = p.random(-500, 500)
        z = p.random(-350, 350)
      } else {
        x = p.random(-100, 100)
        y = p.random(-100, 100)
        z = p.random(-100, 100)
      }
      if (p.mag(x, z) > 350) continue
      let tooClose = false
      for (let i = 0; i < needleX.length; i++) {
        if (p.dist(x, y, z, needleX[i], needleY[i], needleZ[i]) < r) tooClose = true
      }
      if (!tooClose) break
    }
    return { x, y, z }
  }

  for (let i = 0; i < 200; i++) {
    const pos = getPosition(20)
    needleX.push(pos.x); needleY.push(pos.y); needleZ.push(pos.z)
  }
  for (let i = 0; i < 40; i++) {
    const pos = getPosition(5)
    needleX.push(pos.x)
    needleY.push(pos.y + (i < 20 ? 200 : -200))
    needleZ.push(pos.z)
  }

  // 가우스 자극(원본과 동일)
  const xxxGause = [0, 0]
  const yyyGause = [-200, 200]
  const zzzGause = [0, 0]
  const iGause = [-1, 1]

  for (let i = 0; i < needleX.length; i++) {
    let vx = 0, vy = 0, vz = 0
    for (let j = 0; j < xxxGause.length; j++) {
      const len = p.dist(needleX[i], needleY[i], needleZ[i], xxxGause[j], yyyGause[j], zzzGause[j])
      vx += iGause[j] / (len * len) * ((needleX[i] - xxxGause[j]) / Math.abs(len))
      vy += iGause[j] / (len * len) * ((needleY[i] - yyyGause[j]) / Math.abs(len))
      vz += iGause[j] / (len * len) * ((needleZ[i] - zzzGause[j]) / Math.abs(len))
    }
    const m = p.dist(0, 0, 0, vx, vy, vz)
    vectorX[i] = (vx / m) * 13
    vectorY[i] = (vy / m) * 13
    vectorZ[i] = (vz / m) * 13
  }
}

// ====== p5 스케치 ======
function createSketch(p) {

  // ❌ p.preload 함수 제거 (p5.js 2.0에서 폐기됨)
  /*
  p.preload = () => {
    ...
  }
  */

  const computeCanvasSize = () => {
    const host = hostRef.value
    const containerW = host ? host.clientWidth : window.innerWidth
    const target = Math.floor(Math.min(containerW, Math.max(320, window.innerHeight - 160)))
    return { w: target, h: target }
  }

  // ✅ p.setup을 async 함수로 변경
  p.setup = async () => {

    // ✅ Ecosystem.vue와 동일한 이미지 로딩 방식 및 경로 적용
    const imagePrefix = "/img/sim/";

    try {
      barImg = await p.loadImage(imagePrefix + "bar_magnet_texture.png");
      console.log('[MagnetField] bar texture loaded:', imagePrefix + "bar_magnet_texture.png");
    } catch (e) {
      console.error('[MagnetField] bar texture failed:', e);
      barImg = null;
    }

    try {
      earthImg = await p.loadImage(imagePrefix + "earth_movements_map.png");
      console.log('[MagnetField] earth texture loaded:', imagePrefix + "earth_movements_map.png");
    } catch (e) {
      console.error('[MagnetField] earth texture failed:', e);
      earthImg = null;
    }
    // --- 로딩 완료 ---

    const { w, h } = computeCanvasSize()
    const cnv = p.createCanvas(w, h, p.WEBGL)
    cnv.parent(hostRef.value)
    p.setAttributes('antialias', true)
    buildNeedlesAndVectors(p)
  }

  p.windowResized = () => {
    const { w, h } = computeCanvasSize()
    p.resizeCanvas(w, h)
  }

  p.draw = () => {
    if (mode.value === 'bar') p.background(233, 255, 233)
    else p.background(222)

    if (autoRotate.value) rotY += 0.01

    p.push()
    p.scale(p.width / 1000)
    p.rotateX(rotX)
    p.rotateY(rotY)

    // 본체
    if (mode.value === 'bar') {
      p.push()
      p.rotateZ(-p.HALF_PI)
      p.translate(-100, 0, 0)
      p.noStroke()
      p.fill(255, 0, 0); p.box(200, 50, 24)  // N
      p.translate(200, 0, 0)
      p.fill(0, 0, 255); p.box(200, 50, 24)  // S

      if (barImg) {
        p.translate(-100, 0, 13); p.texture(barImg); p.plane(400, 50)
        p.translate(0, 0, -26); p.texture(barImg); p.plane(400, 50)
      }
      p.pop()
    } else {
      p.push()
      p.noStroke()
      if (earthImg) {
        p.textureMode(p.NORMAL)
        p.textureWrap(p.CLAMP)
        p.texture(earthImg)
        p.sphere(200, 64, 64)
      } else {
        p.fill(120, 170, 255)
        p.sphere(200, 32, 24)
      }
      p.pop()
    }

    // 자침
    p.strokeWeight(needleThickness.value)

    p.stroke(255, 0, 0)
    for (let i = 0; i < needleX.length; i++) {
      p.line(
        needleX[i], needleY[i], needleZ[i],
        needleX[i] + vectorX[i], needleY[i] + vectorY[i], needleZ[i] + vectorZ[i]
      )
    }

    p.stroke(0, 0, 255)
    for (let i = 0; i < needleX.length; i++) {
      p.line(
        needleX[i], needleY[i], needleZ[i],
        needleX[i] - vectorX[i], needleY[i] - vectorY[i], needleZ[i] - vectorZ[i]
      )
    }

    p.pop()
  }

  // ===== 드래그 회전: 터치 + 마우스 공통 핸들러 =====
  const onPress = () => {
    if (!insideCanvas(p)) return
    pmx = (p.mouseX * 1000) / p.width
    pmy = (p.mouseY * 1000) / p.width
    dragged = true
  }

  const onDrag = () => {
    if (p.touches && p.touches.length > 1) return
    if (!dragged) return
    rotX += 0.002 * (pmy - (p.mouseY * 1000) / p.width)
    rotX = p.constrain(rotX, -p.HALF_PI, p.HALF_PI)
    rotY += 0.002 * ((p.mouseX * 1000) / p.width - pmx)
    pmx = (p.mouseX * 1000) / p.width
    pmy = (p.mouseY * 1000) / p.width
    return false
  }

  const onRelease = () => { dragged = false }

  p.touchStarted = onPress
  p.touchMoved = onDrag
  p.touchEnded = onRelease

  p.mousePressed = onPress
  p.mouseDragged = onDrag
  p.mouseReleased = onRelease

  function insideCanvas(p) {
    const x = p.mouseX, y = p.mouseY
    return x >= 0 && x <= p.width && y >= 0 && y <= p.height
  }
}

// ====== 라이프사이클 ======
// (이 부분은 p5 전역 오염 방지용이므로 그대로 둡니다)
try {
  // p5의 전역 충돌(friendly errors)로 스케치 중단되는 걸 방지
  // @ts-ignore
  if (P5) P5.disableFriendlyErrors = true
  // @ts-ignore
  if (P5.prototype) P5.prototype.disableFriendlyErrors = true
} catch (_) { }

onMounted(() => {
  const savedNf = globalThis.nf
  try {
    p5Instance = new P5(createSketch)
  } finally {
    if (typeof savedNf !== 'undefined') globalThis.nf = savedNf
    else delete globalThis.nf
  }
})

onBeforeUnmount(() => {
  if (p5Instance) {
    p5Instance.remove()
    p5Instance = null
  }
})

// (선택) 모드 변경 시 회전 초기화
watch(mode, () => {
  // rotX = -0.3
  // rotY = 0
})
</script>

<style scoped>
/* 스타일은 변경되지 않았습니다 */
.magnet-wrap {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.toolbar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px 12px;
  align-items: center;
  margin-bottom: 12px;
}

.row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.radio,
.check {
  display: flex;
  align-items: center;
  gap: 6px;
}

.label {
  font-weight: 600;
  margin-right: 6px;
}

.val {
  min-width: 36px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.btn {
  padding: 6px 10px;
  border: 1px solid #ccc;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
}

.btn:hover {
  background: #f1f1f1;
}

.canvas-host {
  width: 100%;
}

.helper {
  color: #777;
  font-size: 0.9rem;
  margin-top: 8px;
}

.canvas-host canvas {
  touch-action: none;
  /* 모바일에서 페이지 스크롤 방지 */
  display: block;
}
</style>
