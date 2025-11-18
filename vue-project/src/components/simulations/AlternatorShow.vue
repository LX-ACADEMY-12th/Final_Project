<template>
  <div class="acgen-sim" ref="wrapRef">
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-lightbulb-fill"></i>
        </div>
        <div class="header-text">
          <div class="header-title">
            코일을 돌리면 자석이 교류전류(+)와 (-)를 만듭니다.
          </div>
          <div class="header-note">
            코일이 N극 앞에 있을 땐 +, S극일 때는 - 전압. <span class="caption-accent">코일이 움직이면 불 밝기와 그래프가 함께 바뀝니다!</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button class="action-btn secondary" @click="reset">
          <i class="bi bi-arrow-repeat"></i>
          <span>초기화</span>
        </button>
        <button class="action-btn primary" @click="run">
          <i class="bi bi-lightning-fill"></i>
          <span>발전기 돌리기</span>
        </button>
      </div>
    </div>
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
          <input type="range" min="1" max="4" v-model.number="turns" @input="updateTurns" />
          <span class="slider-level">{{ turnsText }}</span>
        </div>
      </div>
    </div>
    <div class="canvas-card themed">
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
function reset() { if (p5Instance?.resetSketch) p5Instance.resetSketch(); }
function run() { if (p5Instance?.startGen) p5Instance.startGen(); }
function updateSpeed() { p5Instance?.setSpeed && p5Instance.setSpeed(speed.value); }
function updateTurns() { p5Instance?.setTurns && p5Instance.setTurns(turns.value); }
const sketch = (p) => {
  let w = 430, h = 420;
  let coilTurns = 2, coilSpeed = 3, running = false, angle = 0, voltage = 0;
  let wavePoints = [];
  const maxWaveLen = 120;
  p.setup = () => {
    w = canvasHostRef.value.clientWidth ? Math.max(320, canvasHostRef.value.clientWidth) : 430;
    h = 420;
    const c = p.createCanvas(w, h);
    c.parent(canvasHostRef.value);
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1));
    wavePoints = [];
    p.noLoop(); p.redraw();
  };
  p.draw = () => {
    p.clear();
    drawMainArea();
    drawWaveGraph();
    if (running) {
      angle += coilSpeed * 0.045;
      if (angle > p.TWO_PI) angle -= p.TWO_PI;
      voltage = Math.sin(angle) * (coilTurns * coilSpeed * 10);
      wavePoints.push(voltage);
      if (wavePoints.length > maxWaveLen) wavePoints.shift();
      p.loop();
    }
  };
  function drawMainArea() {
    const genX = w * 0.25, genY = h * 0.31;
    // 자석: 둥글고 밝은 팝컬러
    p.push();
    p.stroke(180, 194, 220, 50); p.strokeWeight(3);
    p.fill(255, 84, 84);
    p.rect(genX - 44, genY - 28, 38, 44, 13);
    p.fill(70, 155, 234);
    p.rect(genX + 44, genY - 28, 38, 44, 13);
    p.noStroke();
    p.fill("#fff"); p.textAlign(p.CENTER, p.CENTER);
    p.textSize(15); p.textStyle(p.BOLD);
    p.text("N", genX - 25, genY - 3);
    p.text("S", genX + 63, genY - 3);
    p.pop();
    // 코일 (진한 주황~갈색 감긴 라인) 및 중심 노란 원
    p.push();
    p.translate(genX, genY);
    for (let i = 0; i < coilTurns + 2; i++) {
      p.stroke(232-(i*7),115+(i*15),65+(i*2), 195);
      p.strokeWeight(6);
      let rOut = 19 + i * 5.2;
      p.noFill();
      p.arc(0, 0, rOut*2, rOut*2, angle - p.HALF_PI - 0.64, angle - p.HALF_PI + 0.64);
    }
    p.noStroke();
    p.fill(253, 236, 110); p.ellipse(0, 0, 36, 36);
    p.pop();
    // 실시간 코일 극성
    const phase = Math.sin(angle);
    let leftPol = phase > 0 ? "+" : "-";
    let rightPol = phase > 0 ? "-" : "+";
    p.push();
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(20);
    p.fill(leftPol === "+" ? "#FBBF24" : "#4FA5F6");
    p.text(leftPol, genX - 52, genY);
    p.fill(rightPol === "+" ? "#FBBF24" : "#4FA5F6");
    p.text(rightPol, genX + 53, genY);
    p.pop();
    // 중심축 + 회전 동그라미
    p.push();
    p.stroke(190, 184, 98, 120); p.strokeWeight(2.6);
    p.line(genX, genY, genX, genY - 42);
    p.noStroke(); p.fill(245, 213, 97);
    p.ellipse(genX, genY - 42, 13, 13);
    p.pop();
    p.push();
    p.translate(genX, genY);
    p.rotate(angle);
    p.fill(250, 220, 95, 170);
    p.ellipse(0, 0, 14, 14);
    p.pop();
    // 전구: 크고 밝게, 광륜+필라멘트 상세 표현
    const bulbX = w * 0.75, bulbY = genY;
    const voltAbs = Math.abs(voltage);
    p.push();
    p.noStroke();
    // 광륜
    p.fill(250, 212, 42, 114+voltAbs*1.18);
    p.ellipse(bulbX, bulbY, 68 + voltAbs * 0.21, 68 + voltAbs * 0.18);
    // 본체
    p.fill("#FFD321"); p.ellipse(bulbX, bulbY, 42, 42);
    // 안쪽
    p.fill("#FEF5C0"); p.ellipse(bulbX, bulbY, 23, 23);
    // 필라멘트/반짝이
    p.stroke("#FFDF81"); p.strokeWeight(3.9); p.noFill();
    p.arc(bulbX, bulbY, 16, 16, -0.2, 0.8);
    p.arc(bulbX, bulbY, 18, 18, 2.1, 2.8);
    p.noStroke();
    // 소켓+베이스
    p.fill("#FFEEC5"); p.ellipse(bulbX, bulbY+20, 15, 14);
    p.fill("#DADADD"); p.rect(bulbX-7, bulbY+12, 14, 10, 5);
    p.pop();
    // 밝기 텍스트
    p.push();
    p.fill("#FBA33C"); p.textAlign(p.CENTER,p.CENTER); p.textSize(15.7);
    p.text("불 밝기: " + Math.floor(voltAbs), bulbX, bulbY + 38);
    p.pop();
  }
  function drawWaveGraph() {
    const gw = w * 0.91, gh = 95, gx = (w - gw) / 2, gy = h * 0.62;
    // 제목
    p.push();
    p.textAlign(p.CENTER, p.BOTTOM); p.textSize(13.3); p.fill("#548FEC");
    p.text("전압과 불 밝기 변화", gx + gw / 2, gy - 12);
    p.pop();
    // 박스
    p.push();
    p.stroke("#E5EDFA"); p.strokeWeight(2);
    p.fill(249, 252, 255, 245);
    p.rect(gx, gy, gw, gh, 16);
    p.pop();
    p.push();
    p.textAlign(p.LEFT, p.CENTER); p.textSize(13.3);
    p.fill("#7DB6FD"); p.text("+", gx + 10, gy + 19);
    p.fill("#AAA7CE"); p.text("-", gx + 10, gy + gh - 19);
    p.pop();
    p.push();
    p.stroke("#E0EAFD"); p.strokeWeight(1.7);
    p.line(gx + 18, gy + gh / 2, gx + gw - 18, gy + gh / 2);
    p.pop();
    // 곡선 + 동기화 포인트
    p.push();
    p.drawingContext.save();
    p.drawingContext.beginPath();
    p.drawingContext.rect(gx + 14, gy + 6, gw - 28, gh - 12);
    p.drawingContext.clip();
    p.stroke("#41a8fff0"); p.strokeWeight(4);
    p.noFill();
    p.beginShape();
    for (let i = 0; i < wavePoints.length; i++) {
      let x = gx + 21 + i * ((gw - 42) / maxWaveLen);
      let y = gy + gh / 2 - wavePoints[i] * 0.35;
      y = p.constrain(y, gy + 7, gy + gh - 7);
      p.vertex(x, y);
    }
    p.endShape();
    // 동기화 포인트
    let t = (angle % p.TWO_PI) / p.TWO_PI;
    let idx = Math.floor(t * (wavePoints.length - 3));
    let xNow = gx + 21 + idx * ((gw - 42) / maxWaveLen);
    let yNow = gy + gh / 2 - wavePoints[idx] * 0.35;
    yNow = p.constrain(yNow, gy + 7, gy + gh - 7);
    p.noStroke();
    p.fill("#FBBF24"); p.ellipse(xNow, yNow, 20, 20);
    p.stroke("#FFDE7B"); p.strokeWeight(2);
    p.line(xNow, gy+gh/2, xNow, yNow);
    p.drawingContext.restore();
    p.pop();
  }
  p.setSpeed = (v) => { coilSpeed = v || 3; p.redraw(); }
  p.setTurns = (v) => { coilTurns = v || 2; p.redraw(); }
  p.startGen = () => { running = true; p.loop(); }
  p.resetSketch = () => { running = false; coilSpeed = 3; coilTurns = 2; angle = 0; wavePoints = []; voltage = 0; p.noLoop(); p.redraw(); }
};
onMounted(async () => {
  await nextTick();
  if (!canvasHostRef.value) return;
  p5Instance = new p5(sketch, canvasHostRef.value);
  if (p5Instance.setSpeed) p5Instance.setSpeed(speed.value);
  if (p5Instance.setTurns) p5Instance.setTurns(turns.value);
});
onBeforeUnmount(() => { try { p5Instance?.remove(); p5Instance = null; } catch (e) {} });
</script>

<style scoped>
.acgen-sim {
  background: #f7f8fc;
  border-radius: 12px;
  padding: 14px;
  font-family: 'SUIT', sans-serif;
}

/* 헤더 카드 */
.sim-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fbfcff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.04);
  margin-bottom: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

/* 아이콘은 노랑 → 파랑 톤으로 */
.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(59, 130, 246, 0.18);
}

.icon-circle .bi {
  font-size: 20px;
  color: #2563eb;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 0.98rem;   /* ↓ 줄임 */
  font-weight: 700;
  color: #0f172a;
  line-height: 1.4;
}

.header-note {
  font-size: 0.86rem;   /* ↓ 줄임 */
  color: #4b6fb1;
  font-weight: 500;
  line-height: 1.4;
  margin-top: 2px;
}

.caption-accent {
  color: #2563eb;       /* 노랑 → 포인트 블루 */
  font-weight: 600;
  letter-spacing: -0.02em;
}

/* 버튼 영역 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;    /* ↓ 줄임 */
  font-weight: 600;
  padding: 9px 14px;    /* ↓ 줄임 */
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(148, 163, 184, 0.26);
  white-space: nowrap;
  background: #f3f4f6;
  color: #374151;
}

.action-btn .bi {
  font-size: 1.05rem;
}

/* 회색 보조 버튼 */
.action-btn.secondary:hover {
  background: #e5e7eb;
  color: #1f2937;
}

/* 메인 버튼: 노랑 그라데이션 → 브랜드 블루 */
.action-btn.primary {
  background: linear-gradient(135deg, #4f46e5 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 3px 12px rgba(37, 99, 235, 0.3);
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, #4338ca 0%, #1d4ed8 100%);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.35);
}

/* 컨트롤 카드 */
.sim-control-card {
  margin-top: 8px;
  margin-bottom: 10px;
  padding: 10px 14px 12px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(148, 163, 184, 0.16);
}

.control-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.control-row:last-child {
  margin-bottom: 0;
}

.control-label {
  font-size: 0.84rem;   /* ↓ 줄임 */
  font-weight: 600;
  color: #374151;
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
  min-width: 3.4rem;
  text-align: center;
  font-size: 0.8rem;    /* ↓ 줄임 */
  font-weight: 600;
  color: #4b5563;
  background: #f3f4f6;
  padding: 5px 9px;
  border-radius: 8px;
}

/* 캔버스 카드 */
.canvas-card.themed {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(148, 163, 184, 0.24);
}

.canvas-host {
  width: 100%;
  min-height: 340px;
  background: #ffffff;
}

.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  display: block;
}

/* 반응형 */
@media (max-width: 768px) {
  .sim-header-card {
    flex-direction: column;
    align-items: stretch;
  }
  .header-actions {
    justify-content: stretch;
  }
  .action-btn {
    flex: 1;
    padding: 9px 10px;
    font-size: 0.86rem;
  }
}
</style>
