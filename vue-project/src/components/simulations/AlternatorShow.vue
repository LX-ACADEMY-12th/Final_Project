<template>
  <div class="acgen-sim" ref="wrapRef">
    <!-- 상단 카드: 설명 -->
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-lightbulb"></i>
        </div>
        <div class="header-text">
          <div class="header-title">코일을 돌리면 자석이 교류전류(+)와 (-)를 만듭니다.</div>
          <div class="header-sub">속도를 높이거나 감기 횟수를 늘리면 불이 밝아집니다!</div>
        </div>
      </div>
      <div class="header-actions">
        <button class="chip-btn" @click="reset">초기화</button>
        <button class="chip-btn primary" @click="run">발전기 돌리기</button>
      </div>
    </div>
    <!-- 조작 영역 -->
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">코일 회전 속도</span>
        <div class="slider-wrap">
          <input type="range" min="1" max="5" v-model.number="speed" @input="updateSpeed" />
          <span class="slider-level">{{ speedText }}</span>
        </div>
      </div>
      <div class="control-row">
        <span class="control-label">코일 감기 횟수</span>
        <div class="slider-wrap">
          <input type="range" min="1" max="3" v-model.number="turns" @input="updateTurns" />
          <span class="slider-level">{{ turnsText }}</span>
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
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from "vue";
import p5 from "p5";
const wrapRef = ref(null);
const canvasHostRef = ref(null);
let p5Instance = null;
const speed = ref(3);
const turns = ref(2);
const speedText = computed(() => ["매우 느림", "느림", "보통", "빠름", "매우 빠름"][speed.value - 1]);
const turnsText = computed(() => turns.value + "회");
function reset() {
  if (p5Instance && p5Instance.resetSketch) {
    p5Instance.resetSketch();
  }
}
function run() {
  if (p5Instance && p5Instance.startGen) {
    p5Instance.startGen();
  }
}
function updateSpeed() {
  if (p5Instance && p5Instance.setSpeed) p5Instance.setSpeed(speed.value);
}
function updateTurns() {
  if (p5Instance && p5Instance.setTurns) p5Instance.setTurns(turns.value);
}
const sketch = (p) => {
  let w = 420, h = 410;
  let coilTurns = 2, coilSpeed = 3;
  let time = 0;
  let running = false;
  let angle = 0;
  let voltage = 0, prevVolt = 0;
  let wavePoints = [];
  const maxWaveLen = 110;
  p.setup = () => {
    w = canvasHostRef.value.clientWidth || 420;
    const c = p.createCanvas(w, h);
    c.parent(canvasHostRef.value);
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1));
    wavePoints = [];
    p.noLoop();
    p.redraw();
  };
  p.draw = () => {
    p.background(252, 252, 255);
    drawMainArea();
    drawWave();
    // 발전 중일 때 움직임/계산
    if (running) {
      time += 1;
      angle += coilSpeed * 0.05;
      if (angle > p.TWO_PI) angle -= p.TWO_PI;
      voltage = Math.sin(angle) * (coilTurns * coilSpeed * 8);
      wavePoints.push(voltage);
      if (wavePoints.length > maxWaveLen) wavePoints.shift();
      p.loop();
    }
  };
  function drawMainArea() {
    // 발전기 + 전구 영역 전체 중앙에
    const cx = w * 0.31, cy = h * 0.36;
    // 자석 (N/S극)
    p.push();
    p.stroke(80);
    p.strokeWeight(3);
    p.fill(228, 77, 66); // N극(빨강)
    p.rect(cx-37, cy-24, 27, 32, 8);
    p.fill(77, 123, 244); // S극(파랑)
    p.rect(cx+37, cy-24, 27, 32, 8);
    p.pop();
    // 코일
    p.push();
    p.stroke(234, 184, 40);
    p.strokeWeight(5);
    for (let i=0; i < coilTurns*3; i++) {
      p.noFill();
      p.arc(cx, cy, 65-i*5, 30-i*3, angle-p.HALF_PI-0.23, angle-p.HALF_PI+0.23);
    }
    // 축
    p.strokeWeight(2);
    p.stroke(90);
    p.line(cx, cy, cx, cy-39);
    p.fill(80); p.ellipse(cx, cy-39, 11, 11);
    // 회전 핸들 중심 원
    p.push();
    p.translate(cx, cy);
    p.rotate(angle);
    p.fill(244, 222, 177);
    p.ellipse(0, 0, 20, 20);
    p.pop();
    p.pop();
    // 발전기 바닥 (투명한 블루 타원)
    p.push();
    p.noStroke();
    p.fill(167, 207, 255, 60);
    p.ellipse(cx, cy+35, 110, 25);
    p.pop();
    // 불 밝기 큰 숫자 + 전구
    const bulbX = w * 0.74, bulbY = cy;
    const voltAbs = Math.abs(voltage);
    p.push();
    p.noStroke();
    p.fill(250, 247, 173, 140 + voltAbs*2);
    p.ellipse(bulbX, bulbY, 34 + voltAbs*0.17, 34 + voltAbs*0.17);
    p.fill(230, 208, 40, 100 + voltAbs*2);
    p.ellipse(bulbX, bulbY, 27, 27);
    p.fill(112, 112, 128);
    p.ellipse(bulbX, bulbY + 17, 10, 13);
    p.fill(225, 216, 145);
    p.rect(bulbX-6, bulbY+10, 12, 8, 5);
    // 밝기 텍스트
    p.textSize(14);
    p.fill(35, 49, 63);
    p.text("불 밝기: " + Math.floor(voltAbs), bulbX - 18, bulbY + 35);
    p.pop();
  }
  function drawWave() {
    // 교류파(AC 전압) 그래프: 맨 아래
    const graphX = w*0.07, graphY = h*0.8, gw = w*0.86, gh = 48;
    p.push();
    p.fill(245, 248, 255);
    p.noStroke();
    p.rect(graphX-5, graphY-16, gw+10, gh+18, 11);
    p.stroke(70, 100, 210); p.strokeWeight(2.2);
    p.noFill();
    p.beginShape();
    for(let i=0; i<wavePoints.length; i++) {
      let x = graphX + i*(gw/maxWaveLen);
      let y = graphY + gh/2 - wavePoints[i]*0.38;
      p.vertex(x, y);
    }
    p.endShape();
    p.pop();
    // 플러스, 마이너스 표시
    p.textSize(13);
    p.fill(110,130,190,150);
    p.text("+", graphX+gw-14, graphY+7);
    p.text("-", graphX+9, graphY+7);
    // 밑 작은 설명
    p.textSize(12);
    p.fill(70,70,120,80);
    p.text("교류 전압 변화 (실제 발전기 AC형태)", graphX+gw/2-59, graphY-4);
  }
  // API
  p.setSpeed = (val) => { coilSpeed = val || 3; p.redraw(); }
  p.setTurns = (val) => { coilTurns = val || 2; p.redraw(); }
  p.startGen = () => { running = true; p.loop(); }
  p.resetSketch = () => {
    running = false;
    coilSpeed = 3;
    coilTurns = 2;
    angle = 0;
    time = 0;
    wavePoints = [];
    voltage = 0; prevVolt = 0;
    p.noLoop();
    p.redraw();
  }
};
onMounted(async () => {
  await nextTick();
  if (!canvasHostRef.value) return;
  p5Instance = new p5(sketch, canvasHostRef.value);
  if (p5Instance.setSpeed) p5Instance.setSpeed(speed.value);
  if (p5Instance.setTurns) p5Instance.setTurns(turns.value);
});
onBeforeUnmount(() => {
  try {
    p5Instance?.remove();
    p5Instance = null;
  } catch (e) {
    console.error("Error removing p5 instance:", e);
  }
});
</script>
<style scoped>
.acgen-sim { background: #F7F7F7; border-radius: 12px; padding: 16px; }
.sim-header-card {
  display: flex; justify-content: space-between; align-items: center; gap: 12px;
  padding: 12px 14px; background: #fff; border-radius: 12px; border: 1px solid #E5E7EB; margin-bottom: 12px;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.icon-circle { width: 32px; height: 32px; border-radius: 999px; background: #E0F2FE;
  display: flex; align-items: center; justify-content: center;
}
.icon-circle .bi { font-size: 18px; color: #FACC15; }
.header-title { font-size: 0.99rem; font-weight: 700; color: #111827; }
.header-sub { font-size: 0.78rem; color: #6B7280; margin-top: 2px;}
.header-actions { display: flex; align-items: center; gap: 8px; }
.chip-btn {
  padding: 6px 10px; border-radius: 999px; border: 1px solid #E5E7EB; background: #F9FAFB;
  font-size: 0.78rem; color: #374151; cursor: pointer; transition: background 0.15s ease, transform 0.05s ease;
}
.chip-btn.primary { background: #FACC15; border-color: #FACC15; color: #fff; }
.chip-btn:active { transform: translateY(1px); }
.sim-control-card { margin-top: 10px; margin-bottom: 12px; padding: 10px 12px 12px; background: #fff; border-radius: 12px; border: 1px solid #E5E7EB; }
.control-row { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.control-label { font-size: 0.8rem; font-weight: 600; color: #374151; }
.slider-wrap { display: flex; align-items: center; gap: 10px; }
.slider-wrap input[type='range'] { flex: 1; }
.slider-level { min-width: 2rem; text-align: center; font-size: 0.8rem; font-weight: 600; color: #374151; }
.canvas-card { background: #fff; border-radius: 12px; border: 1px solid #E5E7EB; overflow: hidden; }
.canvas-host { width: 100%; min-height: 340px; background: #fff;}
.canvas-host :deep(canvas) { max-width: 100%; height: auto !important; display: block;}
</style>