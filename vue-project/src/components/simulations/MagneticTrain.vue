<template>
  <div class="maglev-sim" ref="wrapRef">
    <div class="sim-header-card">
      <div class="header-left">
        <div class="icon-circle">
          <i class="bi bi-train-front"></i>
        </div>
        <div class="header-text">
          <div class="header-title">자기부상열차 원리 시뮬레이션</div>
        </div>
      </div>
      <div class="header-actions">
        <button class="chip-btn" @click="reset">초기화</button>
        <button class="chip-btn primary" @click="run">시뮬레이션 시작</button>
      </div>
    </div>
    <div class="sim-control-card">
      <div class="control-row">
        <span class="control-label">부상력 세기</span>
        <div class="slider-wrap">
          <input type="range" min="1" max="5" v-model.number="levitationPower" @input="updatePower" />
          <span class="slider-level">{{ levitationPower }}</span>
        </div>
      </div>
      <div class="control-row">
        <span class="control-label">추진력 세기</span>
        <div class="slider-wrap">
          <input type="range" min="1" max="5" v-model.number="propulsionPower" @input="updatePower" />
          <span class="slider-level">{{ propulsionPower }}</span>
        </div>
      </div>
    </div>
    <div class="canvas-card">
      <div class="canvas-host" ref="canvasHostRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from "vue";
import p5 from "p5";

const wrapRef = ref(null);
const canvasHostRef = ref(null);
let p5Instance = null;

const levitationPower = ref(3);
const propulsionPower = ref(3);

function reset() {
  if (p5Instance && p5Instance.resetSketch) {
    p5Instance.resetSketch();
  }
}

function run() {
  if (p5Instance && p5Instance.runSimulation) {
    p5Instance.runSimulation();
  }
}

function updatePower() {
  if (p5Instance) {
    p5Instance.setLevitationPower?.(levitationPower.value);
    p5Instance.setPropulsionPower?.(propulsionPower.value);
  }
}

const sketch = (p) => {
  // 🔽 전체 캔버스 비율/높이 줄이기
  const aspect = 0.42; // 기존 0.55
  let w = 600;
  let h = 260;

  let levitation = 3; // 1~5
  let propulsion = 3; // 1~5

  let trackY;
  const trackHeight = 26;

  const car = {
    x: 0,
    y: 0,
    w: 140,
    h: 46,
    vx: 0,
    vy: 0,
  };

  let isRunning = false;
  let t = 0;

  const coilSpacing = 55;

  p.setup = () => {
    const hostW = canvasHostRef.value?.clientWidth || 600;
    w = hostW;
    h = Math.max(220, Math.round(hostW * aspect)); // 최소 높이도 살짝 줄임

    const c = p.createCanvas(w, h);
    c.parent(canvasHostRef.value);
    p.pixelDensity(Math.min(2, window.devicePixelRatio || 1));

    initLayout();
    p.noLoop();
    p.redraw();
  };

  p.windowResized = () => {
    const hostW = canvasHostRef.value?.clientWidth || 600;
    w = hostW;
    h = Math.max(220, Math.round(hostW * aspect));
    p.resizeCanvas(w, h);
    initLayout();
    p.redraw();
  };

  function initLayout() {
    // 🔽 레일 위치도 살짝 올려서 아래 여백 줄이기 (0.7 → 0.66)
    trackY = h * 0.66;

    car.x = w * 0.1;
    car.y = trackY - trackHeight / 2 - car.h / 2 - baseGap();
    car.vx = 0;
    car.vy = 0;
  }

  function baseGap() {
    return 6 + levitation * 4.5;
  }

  p.draw = () => {
    t += p.deltaTime * 0.001;

    p.background(248);

    drawBackground();
    drawTrack();
    drawCoils();
    updateCarPhysics();
    drawFields();
    drawCar();
    drawHud();
  };

  function updateCarPhysics() {
    const targetY =
      trackY - trackHeight / 2 - car.h / 2 - baseGap();

    const k = 0.12;
    const d = 0.18;
    const forceY = (targetY - car.y) * k;
    car.vy += forceY;
    car.vy *= 1 - d;
    car.y += car.vy;

    const bobAmp = 1.2 + levitation * 0.6;
    car.y += Math.sin(t * 2.4) * (bobAmp * 0.15);

    if (isRunning) {
      const accel = 0.006 * propulsion;
      car.vx += accel;
    } else {
      car.vx *= 0.9;
    }

    const maxV = 2.0 + propulsion * 0.3;
    if (car.vx > maxV) car.vx = maxV;

    car.vx *= 0.996;
    car.x += car.vx;

    if (car.x > w + car.w) {
      car.x = -car.w - 10;
    }
  }

  function drawBackground() {
    p.push();
    p.noStroke();
    p.fill(241, 245, 249);
    // 위쪽 배경은 그대로
    p.rect(0, 0, w, trackY + 40);

    // 🔽 바닥 시작 위치를 위로, 높이는 얇게 (40 → 24)
    p.fill(229, 231, 235);
    p.rect(0, trackY + 24, w, h - (trackY + 24));
    p.pop();
  }

  function drawTrack() {
    p.push();
    p.rectMode(p.CORNER);

    p.noStroke();
    p.fill(148, 163, 184);
    p.rect(0, trackY - trackHeight / 2, w, trackHeight, 6);

    p.fill(75, 85, 99);
    p.rect(0, trackY - trackHeight / 2 - 6, w, 6, 4);

    p.pop();
  }

  function drawCoils() {
    p.push();
    p.rectMode(p.CENTER);
    const baseY = trackY + trackHeight * 0.1;

    for (let x = 0; x < w + coilSpacing; x += coilSpacing) {
      const centerX = x + coilSpacing * 0.5;

      const distToCar = Math.abs(centerX - (car.x + car.w / 2));
      const activeRange = 120 + propulsion * 8;
      const active = distToCar < activeRange;

      const isN = Math.floor(centerX / coilSpacing) % 2 === 0;

      if (active) {
        p.noStroke();
        const strength = p.map(
          distToCar,
          0,
          activeRange,
          1,
          0.1,
          true
        );
        p.fill(59, 130, 246, 90 * strength);
        p.ellipse(centerX, baseY - 12, 60, 42);
      }

      p.stroke(148, 163, 184);
      p.strokeWeight(1.5);
      p.fill(248, 250, 252);
      p.rect(centerX, baseY, 32, 22, 6);

      if (isN) {
        p.fill(59, 130, 246);
      } else {
        p.fill(239, 68, 68);
      }
      p.rect(centerX, baseY, 32, 8, 4);

      p.noStroke();
      p.fill(255);
      p.textAlign(p.CENTER, p.CENTER);
      p.textSize(9);
      p.text(isN ? "N" : "S", centerX, baseY);
    }
    p.pop();
  }

  function drawFields() {
    const cx = car.x + car.w / 2;
    const gapBaseY = car.y + car.h / 2;
    const gapTopY = trackY - trackHeight / 2;

    const fieldCount = 4 + levitation;
    for (let i = 0; i < fieldCount; i++) {
      const offsetX = (i - fieldCount / 2) * 8;

      p.push();
      p.noFill();
      const alpha = 40 + i * 12;
      p.stroke(59, 130, 246, alpha);
      p.strokeWeight(1);

      p.beginShape();
      const amp = 10 + levitation * 2;

      for (let tStep = 0; tStep <= 12; tStep++) {
        const tt = tStep / 12;
        const x = cx + offsetX + (tt - 0.5) * 26;
        const y =
          p.lerp(gapBaseY, gapTopY, tt) -
          Math.sin(tt * p.PI) * amp;
        p.vertex(x, y);
      }
      p.endShape();
      p.pop();
    }
  }

  function drawCar() {
    p.push();
    p.rectMode(p.CORNER);
    p.ellipseMode(p.CENTER);

    p.noStroke();
    p.fill(248, 250, 252);
    p.rect(car.x, car.y, car.w, car.h, 10);

    p.fill(59, 130, 246);
    p.rect(car.x, car.y, car.w, 10, 10, 10, 0, 0);

    p.fill(191, 219, 254);
    const winY = car.y + 18;
    for (let i = 0; i < 3; i++) {
      const wx = car.x + 18 + i * 32;
      p.rect(wx, winY, 22, 14, 4);
    }

    p.fill(148, 163, 184);
    p.rect(
      car.x + car.w - 18,
      car.y + 8,
      18,
      car.h - 12,
      0,
      10,
      10,
      0
    );

    const magnetY = car.y + car.h + 6;
    const magnetW = 40;
    const magnetH = 12;

    p.fill(59, 130, 246);
    p.rect(
      car.x + car.w * 0.3 - magnetW / 2,
      magnetY,
      magnetW,
      magnetH,
      3
    );
    p.fill(239, 68, 68);
    p.rect(
      car.x + car.w * 0.7 - magnetW / 2,
      magnetY,
      magnetW,
      magnetH,
      3
    );

    p.fill(255);
    p.textAlign(p.CENTER, p.CENTER);
    p.textSize(9);
    p.text("N", car.x + car.w * 0.3, magnetY + magnetH / 2);
    p.text("S", car.x + car.w * 0.7, magnetY + magnetH / 2);

    p.pop();
  }

  function drawHud() {
    p.push();
    p.textAlign(p.LEFT, p.TOP);
    p.textSize(12);
    p.fill(55, 65, 81);

    const panelX = 12;
    const panelY = 10;

    p.noStroke();
    p.fill(255, 255, 255, 220);
    p.rect(panelX - 8, panelY - 6, 210, 54, 8);

    p.fill(15, 23, 42);
    p.text(
      `부상력 ${levitation}단계: 자석이 서로 밀어내`,
      panelX,
      panelY
    );
    p.text(
      `추진력 ${propulsion}단계: 코일 전류가`,
      panelX,
      panelY + 16
    );
    p.text(`열차를 앞으로 끌어당겨요.`, panelX, panelY + 32);
    p.pop();
  }

  p.runSimulation = () => {
    isRunning = true;
    if (car.vx < 0.5) {
      car.vx = 0.3 + propulsion * 0.15;
    }
    p.loop();
  };

  p.resetSketch = () => {
    isRunning = false;
    initLayout();
    p.noLoop();
    p.redraw();
  };

  p.setLevitationPower = (val) => {
    levitation = val || 1;
    p.redraw();
  };

  p.setPropulsionPower = (val) => {
    propulsion = val || 1;
    p.redraw();
  };
};

onMounted(async () => {
  await nextTick();
  if (!canvasHostRef.value) return;
  p5Instance = new p5(sketch, canvasHostRef.value);

  p5Instance.setLevitationPower?.(levitationPower.value);
  p5Instance.setPropulsionPower?.(propulsionPower.value);
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
.maglev-sim {
  background: #F7F7F7;
  border-radius: 12px;
  padding: 16px;
}
/* styles similar to the lightning sim */
.sim-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.04);
  margin-bottom: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.icon-circle {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: #DBEAFE;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-circle .bi {
  font-size: 18px;
  color: #2563EB;
}
.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
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
  flex-shrink: 0;
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
  border-color: #4F46E5;
  color: #FFFFFF;
}
.chip-btn:active {
  transform: translateY(1px);
}
.sim-control-card {
  margin-top: 10px;
  margin-bottom: 12px;
  padding: 10px 12px 12px;
  background: #FFFFFF;
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
.slider-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.slider-wrap input[type="range"] {
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
  background: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  overflow: hidden;
}
.canvas-host {
  width: 100%;
  min-height: 220px;
  background: #FFFFFF;
}
.canvas-host :deep(canvas) {
  max-width: 100%;
  height: auto !important;
  display: block;
}
</style>