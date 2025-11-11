<template>
  <div class="states-of-matter-component">
    <div class="simulation-layout">

      <div class="canvas-wrapper">
        <div ref="canvasContainer"></div>
      </div>

      <div class="controls-wrapper">
        <header class="entry-header">
          <h1 class="entry-title">물질의 상태</h1>
        </header>

        <div class="controls-grid">
          <div class="control-group radio-group">
            <label>
              <input type="radio" v-model="matterState" value="solid" /> 고체
            </label>
            <label>
              <input type="radio" v-model="matterState" value="liquid" /> 액체
            </label>
            <label>
              <input type="radio" v-model="matterState" value="gas" /> 기체
            </label>
          </div>
          <label class="checkbox-label">
            <input type="checkbox" v-model="isRunning" /> Run
          </label>
          <label class="checkbox-label">
            <input type="checkbox" v-model="showAttraction" /> 입자 사이의 인력 표시
          </label>
        </div>

        <div class="entry-content">
          <h2>물질의 상태</h2>
          <p>우리 주변에 있는 물질은 고체, 액체, 기체의 세 가지 상태로 존재합니다. 고체는 얼음, 나무 등과 같이 일정한 온도와 압력에서 모양과 부피가 변하지 않습니다. 액체는 물, 주스처럼 흐르는
            성질이 있고, 담긴 그릇에 따라 모양이 변하지만, 일정한 온도와 압력에서 부피는 변하지 않습니다. 공기, 이산화 탄소 등과 같은 기체는 모양과 부피가 일정하지 않으며, 기체의 부피는 온도와
            압력에 따라 크게 변합니다.</p>
          <p>얼음을 가열하면 물이 되고, 물을 계속 가열하면 수증기가 됩니다. 반대로 수증기를 냉각하면 물이 되고, 물을 냉각하면 얼음이 됩니다. 이와 같이 물질에 열을 가하거나 냉각하면 물질의 상태가
            변하는데, 이를 상태 변화라고 합니다.</p>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import p5 from 'p5';

// --- 1. Vue의 반응형 상태 (DOM 컨트롤) ---
const canvasContainer = ref(null);
const matterState = ref('gas'); // 'solid', 'liquid', 'gas'
const isRunning = ref(true);
const showAttraction = ref(true);

let p5Instance = null;

// --- 2. p5.js 스케치 (시뮬레이션 엔진) ---
const sketch = (p) => {

  // --- javalab 원본의 전역 변수들 ---
  let minX = 40, maxX = 960, topY = 40, minY = 40, maxY = 960;
  let moleMax = 54;
  let moleX = [], moleY = [], moleV = [], moleA = [];

  // --- javalab.js에 숨겨진 헬퍼 함수 복원 ---
  // (sin/cos는 p5의 기본 함수가 radians를 사용하므로,
  //  각도(degree)를 사용하는 javalab 로직을 위해 변환 함수를 만듭니다)
  const dcos = (angle) => p.cos(p.radians(angle));
  const dsin = (angle) => p.sin(p.radians(angle));
  const getAngle = (x, y) => p.degrees(p.atan2(y, x));
  const mod = (a, b) => ((a % b) + b) % b; // 자바스크립트의 % 대신 정확한 모듈로 연산

  p.setup = () => {
    const containerWidth = canvasContainer.value.offsetWidth;
    const containerHeight = containerWidth; // 1:1 비율
    p.createCanvas(containerWidth, containerHeight);
    p.frameRate(30);

    initializeParticles();
    initializeState(); // setup2() -> initializeState()로 이름 변경
  };

  // javalab의 setup2() -> Vue의 watch로 분리될 로직
  const initializeState = () => {
    const state = matterState.value; // Vue의 ref에서 상태 읽기

    if (state !== "gas") {
      for (let i = 0; i < moleMax; i++) {
        if (moleA[i] > 180) moleA[i] = p.int(p.random(40, 140));
      }
    }
    if (state === "solid") {
      for (let i = 0; i < moleMax; i++) moleV[i] = 4;
    }
    if (state === "liquid") {
      for (let i = 0; i < moleMax; i++) moleV[i] = 4;
    }
    if (state === "gas") {
      for (let i = 0; i < moleMax; i++) moleV[i] = 8;
    }
  };

  // javalab의 setup()에 있던 입자 초기화 로직
  const initializeParticles = () => {
    for (let i = 0; i < moleMax; i++) {
      moleX[i] = p.random(minX, maxX);
      moleY[i] = p.random(minY, maxY);
      moleA[i] = p.round(p.random(360));
    }
  };

  p.windowResized = () => {
    const containerWidth = canvasContainer.value.offsetWidth;
    const containerHeight = containerWidth;
    p.resizeCanvas(containerWidth, containerHeight);
  };

  p.draw = () => {
    if (p.touches.length > 1) return;
    p.background(255);
    p.textAlign(p.CENTER, p.CENTER);
    p.push();
    p.scale(p.width / 1000); // javalab 원본 (1000px) 기준으로 스케일링

    if (isRunning.value) { // Vue의 ref에서 상태 읽기
      runPhysics(); // javalab의 running() -> runPhysics()로 이름 변경
    }

    p.noStroke();
    // javalab의 lerpColor(color(255), color(128, 255, 255), ...)
    let c1 = p.color(255);
    let c2 = p.color(128, 255, 255);
    p.fill(p.lerpColor(c1, c2, p.map(minY, topY, maxY, -1, 1)));
    p.rect(minX - 20, minY, maxX - minX + 40, maxY - minY + 40);

    p.noFill();
    p.stroke(255, 0, 0);
    p.strokeWeight(3);

    // Vue의 ref에서 상태 읽기
    if (matterState.value !== "gas" && showAttraction.value) {
      for (let i = 0; i < moleMax - 1; i++) {
        for (let j = i + 1; j < moleMax; j++) {
          if (p.dist(moleX[i], moleY[i], moleX[j], moleY[j]) < 35) {
            p.line(moleX[i], moleY[i], moleX[j], moleY[j]);
          }
        }
      }
    }

    p.noStroke();
    for (let i = 0; i < moleMax; i++) {
      let xOffset = 0;
      let yOffset = 0;
      if (isRunning.value && matterState.value === "solid") {
        xOffset = p.random(-1, 1);
        yOffset = p.random(-1, 1);
      }
      p.fill(0, 0, 255);
      if (showAttraction.value) {
        p.ellipse(moleX[i] + xOffset, moleY[i] + yOffset, 28, 28);
      } else {
        p.ellipse(moleX[i] + xOffset, moleY[i] + yOffset, 34, 34);
      }
    }

    p.noFill();
    p.stroke(64);
    p.strokeWeight(40);
    p.rect(minX - 30, topY - 30, maxX - minX + 60, maxY - topY + 60);
    p.rect(minX - 30, topY - 30, maxX - minX + 60, maxY - topY + 60, 40);

    p.pop();
  };

  // javalab의 running() -> runPhysics()로 이름 변경
  function runPhysics() {
    let solidLock = false;
    const state = matterState.value; // Vue의 ref에서 상태 읽기

    if (state === "gas") {
      minY = topY;
    } else {
      minY = maxY;
      for (let i = 0; i < moleMax; i++) {
        minY = p.min(minY, moleY[i]);
      }
      if (minY >= (maxY - 60)) {
        if (state === "solid") {
          solidLock = true;
          for (let i = 0; i < moleMax - 1; i++) {
            for (let j = i + 1; j < moleMax; j++) {
              if (moleX[j] < moleX[i]) {
                let temp = moleX[i]; moleX[i] = moleX[j]; moleX[j] = temp;
                temp = moleY[i]; moleY[i] = moleY[j]; moleY[j] = temp;
              }
            }
          }
        }
      }
      if (!solidLock) {
        let moto = 0;
        for (let i = 1; i < moleMax; i++) {
          if (moleY[i] < moleY[moto]) moto = i;
        }
        moleA[moto] = p.int(p.random(40, 140));
      }
      minY = p.min(minY, maxY - 60);
    }

    if (solidLock) {
      for (let i = 0; i < moleMax; i += 2) {
        if (moleY[i + 1] < moleY[i]) {
          let temp = moleX[i]; moleX[i] = moleX[i + 1]; moleX[i + 1] = temp;
          temp = moleY[i]; moleY[i] = moleY[i + 1]; moleY[i + 1] = temp;
        }
        let x = p.map(i, 0, moleMax - 2, minX + 13, maxX - 13);
        let y0 = maxY - 42;
        let y1 = maxY - 8;
        moleX[i + 0] = p.lerp(moleX[i], x, 0.1);
        moleX[i + 1] = p.lerp(moleX[i + 1], x, 0.1);
        moleY[i + 0] = p.lerp(moleY[i], y0, 0.1);
        moleY[i + 1] = p.lerp(moleY[i + 1], y1, 0.1);
      }
    }

    let tollerence = 30;
    for (let i = 0; i < moleMax - 1; i++) {
      for (let j = i + 1; j < moleMax; j++) {
        let pix = moleX[i] + moleV[i] * dcos(moleA[i]);
        let piy = moleY[i] + moleV[i] * dsin(moleA[i]);
        let pjx = moleX[j] + moleV[j] * dcos(moleA[j]);
        let pjy = moleY[j] + moleV[j] * dsin(moleA[j]);

        if (p.dist(moleX[i], moleY[i], moleX[j], moleY[j]) < tollerence) {
          if (p.dist(moleX[i], moleY[i], moleX[j], moleY[j]) > p.dist(pix, piy, pjx, pjy)) {
            let vix = moleV[i] * dcos(moleA[i]);
            let viy = moleV[i] * dsin(moleA[i]);
            let vjx = moleV[j] * dcos(moleA[j]);
            let vjy = moleV[j] * dsin(moleA[j]);
            let vcx = (vix + vjx) / 2.;
            let vcy = (viy + vjy) / 2.;
            let vcix = vix - vcx;
            let vciy = viy - vcy;
            let vcjx = vjx - vcx;
            let vcjy = vjy - vcy;
            let vci = p.dist(0, 0, vcix, vciy);
            let vcj = p.dist(0, 0, vcjx, vcjy);
            let aci = getAngle(vcix, vciy);
            let acj = getAngle(vcjx, vcjy);
            let aij = getAngle(moleX[j] - moleX[i], moleY[j] - moleY[i]);
            let aji = getAngle(moleX[i] - moleX[j], moleY[i] - moleY[j]);
            aci = mod(2 * aij + 180 - aci, 360);
            acj = mod(2 * aji + 180 - acj, 360);
            vix = vci * dcos(aci);
            viy = vci * dsin(aci);
            vjx = vcj * dcos(acj);
            vjy = vcj * dsin(acj);
            vix += vcx; viy += vcy;
            vjx += vcx; vjy += vcy;

            if (state === "gas") {
              moleV[i] = p.dist(0, 0, vix, viy);
              moleV[j] = p.dist(0, 0, vjx, vjy);
            }
            moleA[i] = getAngle(vix, viy);
            moleA[j] = getAngle(vjx, vjy);
          }
        }
      }
    }

    for (let i = 0; i < moleMax; i++) {
      if (!solidLock) {
        moleX[i] += moleV[i] * dcos(moleA[i]);
        moleY[i] += moleV[i] * dsin(moleA[i]);
      }
      if (moleX[i] < minX) { moleX[i] = 2 * minX - moleX[i]; moleA[i] = mod(180 - moleA[i], 360); }
      if (moleX[i] > maxX) { moleX[i] = 2 * maxX - moleX[i]; moleA[i] = mod(180 - moleA[i], 360); }
      if (moleY[i] < minY) { moleY[i] = 2 * minY - moleY[i]; moleA[i] = 360 - moleA[i]; }
      if (moleY[i] > maxY) { moleY[i] = 2 * maxY - moleY[i]; moleA[i] = 360 - moleA[i]; }
    }
  }

  // --- Vue가 p5를 제어하기 위한 브릿지 함수 ---
  // p5 인스턴스에 함수를 추가하여 Vue에서 호출할 수 있게 함
  p.myCustomRedrawAccordingToState = () => {
    initializeState();
  };
};

// --- 3. Vue 생명주기 훅 ---
onMounted(() => {
  p5Instance = new p5(sketch, canvasContainer.value);
});

onUnmounted(() => {
  if (p5Instance) {
    p5Instance.remove();
  }
});

// --- 4. Vue Watcher (가장 중요) ---
// Vue의 'matterState' ref가 변경될 때마다,
// p5 인스턴스 내부의 'myCustomRedrawAccordingToState' 함수를 호출
watch(matterState, () => {
  if (p5Instance) {
    p5Instance.myCustomRedrawAccordingToState();
  }
});

</script>

<style>
/* 이 스타일은 <style scoped>가 아닌 전역 <style>에 두는 것이
         다른 javalab 시뮬레이션과 스타일을 공유할 때 유리할 수 있습니다.
         (이전 SpringScaleSimulation.vue와 중복됨) */
.themeform input,
.themeform select,
.themeform textarea,
.themeform button,
.themeform label {
  font-size: inherit;
  font-weight: inherit;
}

input[type="checkbox"] {
  width: 20px;
  height: 20px;
  vertical-align: middle;
  margin: 0 6px 0 0;
}

input[type="radio"] {
  width: 20px;
  height: 20px;
  vertical-align: middle;
  margin-right: 6px;
}
</style>

<style scoped>
/* javalab 원본의 레이아웃을 현대적인 flex로 재구성 */
.states-of-matter-component {
  font-family: 'SUIT', sans-serif;
  color: #333;
}

.simulation-layout {
  display: flex;
  flex-wrap: wrap;
  /* 모바일에서 세로로 쌓임 */
  gap: 20px;
}

.canvas-wrapper {
  flex: 1;
  /* 캔버스 영역 */
  min-width: 300px;
  /* 캔버스 최소 너비 */
  max-width: 750px;
  /* 최대 너비 (원본과 유사하게) */
  aspect-ratio: 1 / 1;
  /* 1:1 비율 유지 */
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

/* p5.js가 생성하는 캔버스에 스타일 적용 */
:deep(canvas) {
  display: block;
  width: 100% !important;
  height: 100% !important;
  /* 래퍼에 100% 맞춤 */
}

.controls-wrapper {
  flex: 1;
  /* 컨트롤+텍스트 영역 */
  min-width: 300px;
  /* 최소 너비 */
}

.entry-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
  margin-bottom: 1.5rem;
}

.entry-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
}

/* Vue로 만든 컨트롤 그룹 스타일 */
.controls-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 1.1rem;
}

.radio-group label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.entry-content p {
  line-height: 1.7;
  margin-bottom: 1.5rem;
}

.entry-content h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-top: 2rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}
</style>
