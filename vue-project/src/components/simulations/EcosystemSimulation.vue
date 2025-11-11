<template>
  <div class="ecosystem-component">

    <div class="canvas-wrapper">
      <div ref="canvasContainer" style="aspect-ratio: 2 / 1;"></div>
    </div>

    <div class="controls-bar">
      <button @click="removeHare">토끼 -</button>
      <button @click="addHare">토끼 +</button>
      <button @click="removeWolf">늑대 -</button>
      <button @click="addWolf">늑대 +</button>
      <label class="checkbox-label">
        <input type="checkbox" v-model="isRunning" /> 실행
      </label>
      <button @click="clearGraph">그래프 지우기</button>
    </div>

    <div class="text-content">
      <header class="entry-header">
        <h1 class="entry-title">생태계</h1>
      </header>
      <div class="entry-content">
        <h2>시뮬레이션의 설명</h2>
        <ul>
          <li>빨간색 바의 길이는 생명력을 나타냅니다. 시간에 따라 서서히 줄어듭니다. 생명력이 0이 되면 자연 사망합니다.</li>
          <li>녹색 바의 길이는 스테미나(영양상태)를 나타냅니다. 짧으면 배고픈 상태가 됩니다. 영양상태가 0이 되면 굶어서 사망합니다.</li>
          <li>토끼 번식 조건: 토끼의 스테미나가 50% 이상인 상태에서, 생명력이 각각 20%, 40%, 50%, 60%, 80%에 도달하면 토끼 1마리를 생산합니다.</li>
          <li>늑대 번식 조건: 늑대의 스테미나와 관계 없이, 생명력이 각각 40%, 60%에 도달하면 늑대 1마리를 생산합니다. </li>
          <li>토끼나 늑대 모두 배가 고파지면 먹이 찾는 능력이 향상됩니다.</li>
        </ul>

        <h2>피식자와 포식자의 관계</h2>
        <p>20세기 초반 미국의 엘로스톤 국립공원에서는 사슴을 보호하기 위해 천적인 늑대를 대대적으로 소탕한 적이 있었습니다. 늑대가 사라지자 한동안 사슴, 엘크, 순록의 개체수가 빠르게
          증가했습니다.<br>
          그러나 어느 정도 시간이 지나자 이들이 풀을 모두 먹어치우는 바람에 사슴의 개체수 역시 빠르게 감소하기 시작했습니다. 이에 당황한 국립공원 측은 캐나다에서 늑대 30마리를 수입하여 국립공원에
          방사함으로써 다시 생태계의 균형을 회복했습니다.</p>
        <p>여기서 짚고 넘어가야 할 것은 늑대의 존재가 오히려 사슴 보호에 도움이 된다는 것입니다. 늑대는 걸음이 느린 사슴이나 병약한 새끼들을 주로 잡아먹기 때문에 오히려 사슴의 개체군을 건강하게
          만들어줍니다.<br>
          건강한 생태계에서는 피식자가 완전히 사라지지도 않으며 포식자 또한 모두 굶어죽는 법이 없습니다. 피식자와 포식자는 일정 시차를 두고 증감을 반복하면서 평형을 이룹니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import p5 from 'p5';

// --- 1. Vue의 반응형 상태 (DOM 컨트롤) ---
const canvasContainer = ref(null);
const isRunning = ref(true);
let p5Instance = null;

// --- 2. p5.js 스케치 (시뮬레이션 엔진) ---
const sketch = (p) => {

  // --- javalab 원본의 전역 변수들 ---
  let imageHerb, imageHare, imageWolf;
  let resolution = 1.15;
  let herbMax = 0, herbX = [], herbY = [];
  let hareMax = 0, hareHPMax = 400, hareMPMax = 200;
  let hareHP = [], hareMP = [], hareX = [], hareY = [], hareT = [], hareV = 0.002;
  let wolfMax = 0, wolfHPMax = 600, wolfMPMax = 300;
  let wolfHP = [], wolfMP = [], wolfX = [], wolfY = [], wolfT = [], wolfV = 0.002;
  let graphYMax = 1, graphHerb = [], graphHare = [], graphWolf = [];

  // --- javalab.js 헬퍼 함수 복원 ---
  const mod = (a, b) => ((a % b) + b) % b;

  // ✅ 2. p.setup 함수를 async로 변경하고, 로딩 로직을 맨 위로 가져옵니다.
  p.setup = async () => { // <-- "async" 키워드 추가
    const containerWidth = canvasContainer.value.offsetWidth;
    const containerHeight = containerWidth * 0.5; // 2:1 비율
    p.createCanvas(containerWidth, containerHeight);
    p.frameRate(60);

    // --- 3. 이미지 로딩 로직 (async/await 사용) ---
    const imagePrefix = "/img/sim/";
    try {
      imageHerb = await p.loadImage(imagePrefix + "ecosystem_herb.png");
      console.log('✅ Herb image loaded');
    } catch (e) {
      console.warn('⚠️ Herb image loading failed, using shape instead');
      imageHerb = null;
    }

    try {
      imageHare = await p.loadImage(imagePrefix + "ecosystem_hare.png");
      console.log('✅ Hare image loaded');
    } catch (e) {
      console.warn('⚠️ Hare image loading failed, using shape instead');
      imageHare = null;
    }

    try {
      imageWolf = await p.loadImage(imagePrefix + "ecosystem_wolf.png");
      console.log('✅ Wolf image loaded');
    } catch (e) {
      console.warn('⚠️ Wolf image loading failed, using shape instead');
      imageWolf = null;
    }
    // --- 로딩 완료 ---

    // 4. 로딩이 끝난 후 나머지 setup 로직을 실행합니다.
    p.imageMode(p.CENTER);

    // --- javalab 원본의 helper 함수들을 p5 인스턴스에 연결 ---
    p.addHare = () => hareAdd();
    p.removeHare = () => { if (hareMax > 0) hareDelete(hareMax - 1); };
    p.addWolf = () => wolfAdd();
    p.removeWolf = () => { if (wolfMax > 0) wolfDelete(wolfMax - 1); };
    p.clearGraph = () => doButtonErase();

    // --- 초기 개체 생성 ---
    for (let i = 0; i < 50; i++) herbAdd();
    for (let i = 0; i < 30; i++) hareAdd();
    for (let i = 0; i < 10; i++) wolfAdd();
    doButtonErase(); // 그래프 초기화
  };

  p.windowResized = () => {
    const containerWidth = canvasContainer.value.offsetWidth;
    const containerHeight = containerWidth * 0.5;
    p.resizeCanvas(containerWidth, containerHeight);
  };

  // --- javalab 원본 헬퍼 함수들 ---
  function herbAdd() {
    herbX[herbMax] = p.random(resolution * 0.05, resolution * 0.95);
    herbY[herbMax] = p.random(0.05, 0.95);
    herbMax++;
  }

  function herbDelete(index) {
    herbMax--;
    herbX[index] = herbX[herbMax];
    herbY[index] = herbY[herbMax];
  }

  function hareAdd() {
    hareHP[hareMax] = p.int(p.random(hareHPMax * 0.25, hareHPMax * 0.75));
    hareMP[hareMax] = p.int(p.random(hareMPMax * 0.25, hareMPMax * 0.75));
    hareX[hareMax] = p.random(1) * resolution;
    hareY[hareMax] = p.random(1);
    hareT[hareMax] = p.random(p.TWO_PI);
    hareMax++;
  }

  function hareBorn(index) {
    hareHP[hareMax] = hareHPMax;
    hareMP[hareMax] = hareMPMax * 0.5;
    hareX[hareMax] = hareX[index];
    hareY[hareMax] = hareY[index];
    hareT[hareMax] = p.random(p.TWO_PI);
    hareMax++;
  }

  function hareDelete(index) {
    hareMax--;
    hareHP[index] = hareHP[hareMax];
    hareMP[index] = hareMP[hareMax];
    hareX[index] = hareX[hareMax];
    hareY[index] = hareY[hareMax];
    hareT[index] = hareT[hareMax];
  }

  function wolfAdd() {
    wolfHP[wolfMax] = p.int(p.random(wolfHPMax * 0.25, wolfHPMax * 0.75));
    wolfMP[wolfMax] = p.int(p.random(wolfMPMax * 0.25, wolfMPMax * 0.75));
    wolfX[wolfMax] = p.random(1) * resolution;
    wolfY[wolfMax] = p.random(1);
    wolfT[wolfMax] = p.random(p.TWO_PI);
    wolfMax++;
  }

  function wolfBorn(index) {
    wolfHP[wolfMax] = wolfHPMax;
    wolfMP[wolfMax] = wolfMPMax * 0.5;
    wolfX[wolfMax] = wolfX[index];
    wolfY[wolfMax] = wolfY[index];
    wolfT[wolfMax] = p.random(p.TWO_PI);
    wolfMax++;
  }

  function wolfDelete(index) {
    wolfMax--;
    wolfHP[index] = wolfHP[wolfMax];
    wolfMP[index] = wolfMP[wolfMax];
    wolfX[index] = wolfX[wolfMax];
    wolfY[index] = wolfY[wolfMax];
    wolfT[index] = wolfT[wolfMax];
  }

  function doButtonErase() {
    graphHerb = [herbMax * 3];
    graphHare = [hareMax];
    graphWolf = [wolfMax];
  }

  // --- javalab의 핵심 물리/AI 로직 ---
  function running() {
    // 풀 자동 생성
    if (herbMax < 2000) {
      if (mod(p.frameCount, 5) == 0) {
        let x = p.random(resolution * 0.05, resolution * 0.95);
        let y = p.random(0.05, 0.95);
        let d1 = 999, d2 = 999;
        for (let j = 0; j < herbMax; j++) {
          d1 = p.abs(x - herbX[j]) + p.abs(y - herbY[j]);
          if (d1 < d2) d2 = d1;
        }
        if (d2 > 0.05) herbAdd();
      }
    }

    // 토끼 로직
    for (let i = 0; i < hareMax; i++) {
      hareT[i] += p.random(-1, 1);
      if (hareMP[i] < hareMPMax * 0.7) {
        let d1 = 999, d2 = 999, jj = -1;
        for (let j = 0; j < herbMax; j++) {
          d1 = p.abs(hareX[i] - herbX[j]) + p.abs(hareY[i] - herbY[j]);
          if (d1 < d2) { d2 = d1; jj = j; }
        }
        if (jj >= 0) {
          hareT[i] = p.atan2(herbY[jj] - hareY[i], herbX[jj] - hareX[i]);
          if (d2 < 0.03) {
            herbDelete(jj);
            hareMP[i] = hareMPMax;
          }
        }
      }
      hareX[i] += hareV * p.cos(hareT[i]);
      hareY[i] += hareV * p.sin(hareT[i]);
      hareX[i] = p.constrain(hareX[i], resolution * 0.05, resolution * 0.95);
      hareY[i] = p.constrain(hareY[i], 0.05, 0.95);

      hareHP[i]--;
      if (hareHP[i] <= 0) { hareDelete(i); continue; }
      hareMP[i]--;
      if (hareMP[i] <= 0) { hareDelete(i); continue; }

      // 번식
      if (hareHP[i] == p.int(hareHPMax * 0.2)) if (hareMP[i] > hareMPMax * 0.5) hareBorn(i);
      if (hareHP[i] == p.int(hareHPMax * 0.4)) if (hareMP[i] > hareMPMax * 0.5) hareBorn(i);
      if (hareHP[i] == p.int(hareHPMax * 0.5)) if (hareMP[i] > hareMPMax * 0.5) hareBorn(i);
      if (hareHP[i] == p.int(hareHPMax * 0.6)) if (hareMP[i] > hareMPMax * 0.5) hareBorn(i);
      if (hareHP[i] == p.int(hareHPMax * 0.8)) if (hareMP[i] > hareMPMax * 0.5) hareBorn(i);
    }

    // 늑대 로직
    for (let i = 0; i < wolfMax; i++) {
      wolfT[i] += p.random(-1, 1);
      if (wolfMax < hareMax / 4) {
        if (wolfMP[i] < wolfMPMax * 0.5) {
          let d1 = 999, d2 = 999, jj = -1;
          for (let j = 0; j < hareMax; j++) {
            d1 = p.abs(wolfX[i] - hareX[j]) + p.abs(wolfY[i] - hareY[j]);
            if (d1 < d2) { d2 = d1; jj = j; }
          }
          if (jj >= 0) {
            wolfT[i] = p.atan2(hareY[jj] - wolfY[i], hareX[jj] - wolfX[i]);
            if (d2 < 0.02) {
              hareDelete(jj);
              wolfMP[i] = wolfMPMax;
            }
          }
        }
      }
      wolfX[i] += wolfV * p.cos(wolfT[i]);
      wolfY[i] += wolfV * p.sin(wolfT[i]);
      wolfX[i] = p.constrain(wolfX[i], resolution * 0.05, resolution * 0.95);
      wolfY[i] = p.constrain(wolfY[i], 0.05, 0.95);

      wolfHP[i]--;
      if (wolfHP[i] <= 0) { wolfDelete(i); continue; }
      wolfMP[i]--;
      if (wolfMP[i] <= 0) { wolfDelete(i); continue; }

      // 번식
      if (wolfHP[i] == p.int(wolfHPMax * 0.4)) wolfBorn(i);
      if (wolfHP[i] == p.int(wolfHPMax * 0.6)) wolfBorn(i);
    }

    // 그래프 데이터 업데이트
    if (mod(p.frameCount, 30) == 0) {
      graphHerb.push(herbMax * 3);
      graphHare.push(hareMax);
      graphWolf.push(wolfMax);
      if (graphHerb.length > 100) {
        graphHerb.splice(0, 1);
        graphHare.splice(0, 1);
        graphWolf.splice(0, 1);
      }
      graphYMax = p.max(p.max(graphHerb), p.max(graphHare), p.max(graphWolf));
    }
  }

  // --- p5.js의 메인 draw 루프 ---
  p.draw = () => {
    if (p.touches.length > 1) return;
    p.background(233);
    p.strokeWeight(p.width / 500);
    p.textSize(p.width / 40);
    p.textAlign(p.CENTER, p.CENTER);

    if (isRunning.value) {
      running();
    }

    p.noStroke();

    // 1. 시뮬레이션 영역 그리기
    p.fill(233, 222, 191);
    p.rect(0, 0, p.height * resolution, p.height);

    // 개선: 이미지 없으면 도형으로 대체
    for (let i = 0; i < herbMax; i++) {
      if (imageHerb && imageHerb.width > 0) {
        p.image(imageHerb, herbX[i] * p.height, herbY[i] * p.height, p.width * 0.05, p.width * 0.05);
      } else {
        // 풀 대체: 작은 초록 원
        p.fill(34, 139, 34);
        p.circle(herbX[i] * p.height, herbY[i] * p.height, p.width * 0.03);
      }
    }

    for (let i = 0; i < hareMax; i++) {
      if (imageHare && imageHare.width > 0) {
        p.image(imageHare, hareX[i] * p.height, hareY[i] * p.height, p.width * 0.05, p.width * 0.05);
      } else {
        // 토끼 대체: 연갈색 원
        p.fill(160, 110, 80);
        p.circle(hareX[i] * p.height, hareY[i] * p.height, p.width * 0.04);
      }
      // HP 바
      p.fill(255, 0, 0);
      p.rect(hareX[i] * p.height - p.width * 0.025, hareY[i] * p.height + p.width * 0.02,
        p.map(hareHP[i], 0, hareHPMax, 0, p.width * 0.05), p.width * 0.004);
      // MP 바
      p.fill(0, 255, 0);
      p.rect(hareX[i] * p.height - p.width * 0.025, hareY[i] * p.height + p.width * 0.025,
        p.map(hareMP[i], 0, hareMPMax, 0, p.width * 0.05), p.width * 0.004);
    }

    for (let i = 0; i < wolfMax; i++) {
      if (imageWolf && imageWolf.width > 0) {
        p.image(imageWolf, wolfX[i] * p.height, wolfY[i] * p.height, p.width * 0.05, p.width * 0.05);
      } else {
        // 늑대 대체: 갈색 원
        p.fill(139, 69, 19);
        p.circle(wolfX[i] * p.height, wolfY[i] * p.height, p.width * 0.05);
      }
      // HP 바
      p.fill(255, 0, 0);
      p.rect(wolfX[i] * p.height - p.width * 0.025, wolfY[i] * p.height + p.width * 0.025,
        p.map(wolfHP[i], 0, wolfHPMax, 0, p.width * 0.05), p.width * 0.004);
      // MP 바
      p.fill(0, 255, 0);
      p.rect(wolfX[i] * p.height - p.width * 0.025, wolfY[i] * p.height + p.width * 0.03,
        p.map(wolfMP[i], 0, wolfMPMax, 0, p.width * 0.05), p.width * 0.004);
    }

    // 2. 그래프 영역 그리기
    p.fill(255);
    p.rect(p.height * resolution, 0, p.width, p.height);

    let graphX1 = p.width * 0.62;
    let graphX2 = p.width;
    let graphY1 = p.height * 0.5;
    let graphY2 = p.height * 0.995;

    p.noFill();
    p.stroke(0);
    p.line(graphX1, graphY1, graphX1, graphY2);
    p.line(graphX1, graphY2, graphX2, graphY2);

    // 풀 그래프 (초록)
    p.stroke(64, 255, 64);
    p._renderer.beginShape();
    for (let i = 0; i < graphHerb.length; i++) {
      p._renderer.vertex(p.map(i, 0, 100, graphX1, graphX2), p.map(graphHerb[i], 0, graphYMax, graphY2, graphY1));
    }
    p._renderer.endShape();
    p.line(graphX1 + p.width * 0.1, p.height * 0.1, graphX1 + p.width * 0.2, p.height * 0.1);

    if (imageHerb && imageHerb.width > 0) {
      p.image(imageHerb, graphX1 + p.width * 0.23, p.height * 0.1, p.width * 0.05, p.width * 0.05);
    } else {
      p.fill(34, 139, 34);
      p.circle(graphX1 + p.width * 0.23, p.height * 0.1, p.width * 0.03);
    }

    // 토끼 그래프 (회색)
    p.stroke(90);
    p._renderer.beginShape();
    for (let i = 0; i < graphHare.length; i++) {
      p._renderer.vertex(p.map(i, 0, 100, graphX1, graphX2), p.map(graphHare[i], 0, graphYMax, graphY2, graphY1));
    }
    p._renderer.endShape();
    p.line(graphX1 + p.width * 0.1, p.height * 0.23, graphX1 + p.width * 0.2, p.height * 0.23);

    if (imageHare && imageHare.width > 0) {
      p.image(imageHare, graphX1 + p.width * 0.23, p.height * 0.23, p.width * 0.05, p.width * 0.05);
    } else {
      p.fill(160, 110, 80);
      p.circle(graphX1 + p.width * 0.23, p.height * 0.23, p.width * 0.04);
    }

    // 늑대 그래프 (갈색)
    p.stroke(191, 64, 0);
    p._renderer.beginShape();
    for (let i = 0; i < graphWolf.length; i++) {
      p._renderer.vertex(p.map(i, 0, 100, graphX1, graphX2), p.map(graphWolf[i], 0, graphYMax, graphY2, graphY1));
    }
    p._renderer.endShape();
    p.line(graphX1 + p.width * 0.1, p.height * 0.36, graphX1 + p.width * 0.2, p.height * 0.36);

    if (imageWolf && imageWolf.width > 0) {
      p.image(imageWolf, graphX1 + p.width * 0.23, p.height * 0.36, p.width * 0.05, p.width * 0.05);
    } else {
      p.fill(139, 69, 19);
      p.circle(graphX1 + p.width * 0.23, p.height * 0.36, p.width * 0.05);
    }

    // 개체수 텍스트
    p.fill(0);
    p.noStroke();
    p.textAlign(p.LEFT, p.CENTER);
    p.text(p.int(herbMax * 3), graphX1 + p.width * 0.26, p.height * 0.1);
    p.text(hareMax, graphX1 + p.width * 0.26, p.height * 0.23);
    p.text(wolfMax, graphX1 + p.width * 0.26, p.height * 0.36);
  };
};

// --- 3. Vue 생명주기 훅 ---
onMounted(() => {
  try {
    p5Instance = new p5(sketch, canvasContainer.value);
  } catch (e) {
    console.error('❌ Failed to initialize p5.js:', e);
  }
});

onUnmounted(() => {
  if (p5Instance) {
    try {
      p5Instance.remove();
    } catch (e) {
      console.error('Error removing p5 instance:', e);
    }
  }
});

// --- 4. Vue -> p5.js 브릿지 함수들 ---
const addHare = () => { if (p5Instance) p5Instance.addHare(); };
const removeHare = () => { if (p5Instance) p5Instance.removeHare(); };
const addWolf = () => { if (p5Instance) p5Instance.addWolf(); };
const removeWolf = () => { if (p5Instance) p5Instance.removeWolf(); };
const clearGraph = () => { if (p5Instance) p5Instance.clearGraph(); };

</script>

<style scoped>
.ecosystem-component {
  font-family: 'SUIT', sans-serif;
  color: #333;
}

.canvas-wrapper {
  width: 100%;
  max-width: 840px;
  margin: 0 auto;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

:deep(canvas) {
  display: block;
  width: 100% !important;
  height: auto !important;
}

.controls-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-top: 1rem;
}

.controls-bar button {
  padding: 8px 12px;
  font-family: 'SUIT', sans-serif;
  font-weight: 600;
  font-size: 0.9rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.2s;
}

.controls-bar button:hover {
  background-color: #f0f0f0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
}

input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-right: 6px;
}

.text-content {
  margin-top: 1.5rem;
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

.entry-content p,
.entry-content ul {
  line-height: 1.7;
  margin-bottom: 1.5rem;
}

.entry-content ul {
  padding-left: 20px;
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
