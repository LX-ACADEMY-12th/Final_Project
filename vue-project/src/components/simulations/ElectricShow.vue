<template>
  <div class="electric-show" ref="wrapRef">
    <div class="toolbar">
      <div class="row">
        <label class="radio"><input type="radio" value="tesla" v-model="mode" /> 테슬라 코일</label>
        <label class="radio"><input type="radio" value="ladder" v-model="mode" /> 방전 사다리</label>
        <label class="radio"><input type="radio" value="vdg" v-model="mode" /> 반데그라프</label>
        <label class="radio"><input type="radio" value="storm" v-model="mode" /> 번개 실험</label>
      </div>

      <div class="row">
        <label class="check"><input type="checkbox" v-model="running" /> 재생</label>
        <label class="check"><input type="checkbox" v-model="autoParams" /> 자동 파라미터</label>
      </div>

      <div class="row">
        <span class="label">전압 강도</span>
        <input type="range" min="0" max="1" step="0.01" v-model.number="intensity" />
        <span class="val">{{ (intensity * 100).toFixed(0) }}%</span>
      </div>

      <div class="row">
        <span class="label">주파수/속도</span>
        <input type="range" min="0" max="1.5" step="0.01" v-model.number="speed" />
        <span class="val">{{ speed.toFixed(2) }}</span>
      </div>

      <div class="row">
        <button class="btn" @click="burst()">⚡ 번개 일발</button>
        <button class="btn" @click="reset()">리셋</button>
      </div>
    </div>

    <div class="canvas-host" ref="hostRef"></div>
    <p class="helper">캔버스를 클릭하면 추가 스파크가 발생합니다.</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import P5 from 'p5'

const wrapRef = ref(null)
const hostRef = ref(null)

const mode = ref('tesla')     // tesla | ladder | vdg | storm
const running = ref(true)
const autoParams = ref(true)
const intensity = ref(0.6)    // 0~1
const speed = ref(0.6)        // 0~1.5

let p5Instance = null

// -------- Spark particle util --------
class Spark {
  constructor(p, x, y, power = 1) {
    this.p = p
    this.path = [{ x, y }]
    this.life = 24 + p.random(16) * power
    this.alpha = 255
    this.thickness = 1.5 + power * 1.5
    this._build(power)
  }
  _build(power) {
    const p = this.p
    let seg = 14 + Math.floor(power * 18)
    const z = p.random(1000)
    for (let i = 0; i < seg; i++) {
      const prev = this.path[this.path.length - 1]
      const ang = p.noise(i * 0.2, z) * p.TWO_PI
      const step = 10 + p.random(8) * power
      const nx = prev.x + Math.cos(ang) * step
      const ny = prev.y + Math.sin(ang) * step
      this.path.push({ x: nx, y: ny })
    }
  }
  draw() {
    const p = this.p
    p.stroke(170, 210, 255, this.alpha)
    p.strokeWeight(this.thickness)
    p.noFill()
    p.beginShape()
    for (const v of this.path) p.vertex(v.x, v.y)
    p.endShape()
    // glow
    p.stroke(70, 150, 255, this.alpha * 0.4)
    p.strokeWeight(this.thickness * 3)
    p.beginShape()
    for (const v of this.path) p.vertex(v.x, v.y)
    p.endShape()
    this.alpha -= 255 / this.life
    return this.alpha > 6
  }
}

// -------- Jacob’s Ladder arc --------
class ClimbingArc {
  constructor(p, x1, y1, x2, y2, speed = 1) {
    this.p = p; this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2
    this.y = Math.min(y1, y2) - 10
    this.speed = speed
  }
  step() {
    const p = this.p
    this.y -= (0.8 + 1.8 * this.speed)
    if (this.y < Math.min(this.y1, this.y2) - 180) this.y = Math.max(this.y1, this.y2) - 8
  }
  draw() {
    const p = this.p
    const seg = 10
    p.noFill()
    p.stroke(180, 220, 255)
    p.strokeWeight(2)
    p.beginShape()
    for (let i = 0; i <= seg; i++) {
      const t = i / seg
      const x = p.lerp(this.x1, this.x2, t)
      const n = p.noise(t * 7 + p.millis() * 0.001) * 20 - 10
      const y = this.y + n
      p.vertex(x, y)
    }
    p.endShape()
    // glow
    p.stroke(60, 140, 255, 120)
    p.strokeWeight(6)
    p.beginShape()
    for (let i = 0; i <= seg; i++) {
      const t = i / seg
      const x = p.lerp(this.x1, this.x2, t)
      const n = p.noise(t * 7 + p.millis() * 0.001) * 20 - 10
      const y = this.y + n
      p.vertex(x, y)
    }
    p.endShape()
    this.step()
  }
}

function makeSketch(p) {
  let sparks = []
  let ladder = null
  let burstCooldown = 0

  const computeSize = () => {
    const W = hostRef.value?.clientWidth || window.innerWidth
    const H = Math.min(520, window.innerHeight - 140)
    return { w: Math.max(320, Math.floor(W)), h: Math.max(320, Math.floor(H)) }
  }

  p.setup = () => {
    const { w, h } = computeSize()
    const cnv = p.createCanvas(w, h)
    cnv.parent(hostRef.value)
    p.pixelDensity(1)
    p.noiseSeed(Math.floor(Math.random() * 1e9))
    ladder = new ClimbingArc(p, w / 2 - 40, h - 40, w / 2 + 40, h - 40, 1)
  }

  p.windowResized = () => {
    const { w, h } = computeSize()
    p.resizeCanvas(w, h)
    ladder = new ClimbingArc(p, w / 2 - 40, h - 40, w / 2 + 40, h - 40, 1)
  }

  const glowRect = (x, y, w, h, r, col = [0, 140, 255], a = 70) => {
    p.noStroke()
    for (let i = 8; i >= 1; i--) {
      p.fill(col[0], col[1], col[2], a * (i / 8))
      p.rect(x, y, w, h, r)
    }
  }

  const drawTesla = () => {
    const w = p.width, h = p.height
    p.background(18)
    // base coil
    p.push()
    p.translate(w / 2, h - 60)
    p.fill(60); p.noStroke()
    p.rect(-40, 20, 80, 18, 8)
    p.fill(120, 60, 40)
    p.rect(-26, -60, 52, 100, 12)
    // top toroid
    p.fill(200)
    p.ellipse(0, -72, 120, 40)
    p.pop()
    // corona & bolts
    const bolts = 2 + Math.floor(intensity.value * 5)
    for (let i = 0; i < bolts; i++) {
      const ang = p.TWO_PI * i / bolts + p.millis() * 0.0007
      const cx = w / 2 + Math.cos(ang) * 10
      const cy = h - 132 + Math.sin(ang) * 6
      if (p.random() < 0.4 + intensity.value * 0.4) {
        sparks.push(new Spark(p, cx, cy, 0.8 + intensity.value))
      }
    }
  }

  const drawLadder = () => {
    const w = p.width, h = p.height
    p.background(15)
    // rods
    p.stroke(180); p.strokeWeight(6)
    p.line(w / 2 - 40, h - 40, w / 2 - 10, 70)
    p.line(w / 2 + 40, h - 40, w / 2 + 10, 70)
    // power supply box
    p.noStroke(); p.fill(70); p.rect(w / 2 - 55, h - 35, 110, 26, 6)
    // arc
    if (autoParams.value) ladder.speed = 0.4 + speed.value
    ladder.draw()
  }

  const drawVDG = () => {
    const w = p.width, h = p.height
    p.background(20)
    // dome
    p.noStroke()
    p.fill(190); p.ellipse(w / 2, h / 2 - 40, 200, 160)
    // column & base
    p.fill(160); p.rect(w / 2 - 26, h / 2 - 40, 52, 150, 10)
    p.fill(80); p.rect(w / 2 - 70, h / 2 + 110, 140, 20, 8)
    // hair (electric field lines)
    const hairs = 50
    for (let i = 0; i < hairs; i++) {
      const t = i / hairs * p.TWO_PI
      const len = 40 + 80 * intensity.value + p.noise(i * 0.3, p.millis() * 0.001) * 30
      const x1 = w / 2 + Math.cos(t) * 68
      const y1 = h / 2 - 40 + Math.sin(t) * 52
      const x2 = w / 2 + Math.cos(t) * (68 + len)
      const y2 = h / 2 - 40 + Math.sin(t) * (52 + len * 0.3)
      p.stroke(200, 200, 255, 140)
      p.strokeWeight(1.6)
      p.line(x1, y1, x2, y2)
      // tiny sparks on tips
      if (p.random() < 0.01 + 0.03 * intensity.value) {
        sparks.push(new Spark(p, x2, y2, 0.5))
      }
    }
  }

  const drawStorm = () => {
    const w = p.width, h = p.height
    // dark sky gradient
    p.noStroke()
    for (let y = 0; y < h; y++) {
      const c = p.lerpColor(p.color(8, 8, 18), p.color(40, 40, 60), y / h)
      p.fill(c); p.rect(0, y, w, 1)
    }
    // ground
    p.fill(30); p.rect(0, h - 40, w, 40)
    // clouds
    p.fill(60, 60, 80, 220)
    for (let i = 0; i < 6; i++) {
      const cx = 80 + i * (w - 160) / 5 + p.noise(i, p.millis() * 0.0005) * 60
      const cy = 80 + p.noise(i * 2, p.millis() * 0.0007) * 10
      p.ellipse(cx, cy, 180, 80)
    }
    // random lightning
    const chance = 0.01 + intensity.value * 0.06
    if (p.random() < chance || burstCooldown > 0) {
      const x = p.random(40, w - 40)
      sparks.push(new Spark(p, x, 60, 1.2 + intensity.value))
      burstCooldown = Math.max(0, burstCooldown - 1)
    }
  }

  p.mousePressed = () => {
    const power = 1 + intensity.value
    sparks.push(new Spark(p, p.mouseX, p.mouseY, power))
  }

  p.draw = () => {
    if (!running.value) {
      p.fill(0, 160); p.noStroke()
      p.rect(0, 0, p.width, p.height)
      p.fill(255); p.textAlign(p.CENTER, p.CENTER); p.text('일시정지', p.width / 2, p.height / 2)
      return
    }

    if (autoParams.value) {
      const t = p.millis() * 0.0004
      intensity.value = 0.45 + 0.35 * Math.sin(t * 1.7 + 1.2)
      speed.value = 0.7 + 0.5 * Math.sin(t * 2.1)
    }

    if (mode.value === 'tesla') drawTesla()
    else if (mode.value === 'ladder') drawLadder()
    else if (mode.value === 'vdg') drawVDG()
    else drawStorm()

    // sparks update/render
    p.blendMode(p.ADD)
    sparks = sparks.filter(s => s.draw())
    p.blendMode(p.BLEND)
  }

  // API to trigger burst
  return {
    api: {
      burst: () => { burstCooldown = 6; for (let i = 0; i < 5; i++) sparks.push(new Spark(p, p.random(p.width), 80, 1.4)) },
      reset: () => { sparks.length = 0 }
    }
  }
}

let sketchApi = { burst: () => { }, reset: () => { } }

function burst() { sketchApi.burst?.() }
function reset() { sketchApi.reset?.() }

onMounted(() => {
  // safety: avoid p5 friendly-errors halting on name conflicts (e.g., nf)
  try { P5.disableFriendlyErrors = true } catch { }
  p5Instance = new P5((p) => {
    const { api } = makeSketch(p)
    sketchApi = api
  })
})

onBeforeUnmount(() => {
  if (p5Instance) { p5Instance.remove(); p5Instance = null }
})

// ensure canvas attaches inside host
watch(hostRef, (el) => {
  if (!p5Instance || !el) return
  const cnv = el.querySelector('canvas')
  if (!cnv) return
  // already parented by setup; nothing else here
})
</script>

<style scoped>
.electric-show {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.toolbar {
  display: grid;
  gap: 8px 12px;
  margin-bottom: 12px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  align-items: center;
}

.row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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
  width: 100%;
  height: auto;
  display: block;
  touch-action: none;
}
</style>
