<template>
  <div id="panorama-container" ref="panoramaContainer"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import 'pannellum/build/pannellum.css';
import 'pannellum';

const props = defineProps({ config: { type: Object, required: true } });
const emit = defineEmits(['scene-changed', 'hotspot-click']);

const viewer = ref(null);
const panoramaContainer = ref(null);

/** 모든 핫스팟에 커스텀 UI/클래스/이벤트 주입 */
function decorateConfig(original) {
  // deep clone
  const cfg = typeof structuredClone === 'function'
    ? structuredClone(original)
    : JSON.parse(JSON.stringify(original));

  const scenes = cfg.scenes || {};
  Object.keys(scenes).forEach((sid) => {
    const s = scenes[sid];

    (s.hotSpots || []).forEach((hs) => {

      // ★[수정] 로직을 'scene'과 'info'로 분리해서 처리합니다.

      // 1. 'scene' 타입 핫스팟 (이동 화살표)
      if (hs.type === 'scene') {
        hs.cssClass = ((hs.cssClass || '') + ' nav-hotspot').trim();

        // 텍스트가 보이도록 args 주입
        hs.createTooltipArgs = { text: hs.text };

        hs.createTooltipFunc = function (hotSpotDiv, args) {
          hotSpotDiv.classList.add('nh-wrap');
          hotSpotDiv.innerHTML = `
            <div class="nh-btn" role="button" aria-label="${(args?.text || '이동').replace(/"/g, '&quot;')}">
              <span class="nh-icon"></span>
              ${args?.text ? `<span class="nh-chip">${args.text}</span>` : ''}
            </div>
          `;
        };
      }

      // 2. 'info' 타입 핫스팟 (답사 종료 버튼)
      if (hs.type === 'info') {
        // ★[수정]
        // 핫스팟 자체에 클릭 핸들러를 주입합니다.
        // Pannellum에서 전달하는 인자(event, hotspotArgs) 대신,
        // 클로저(closure)에 있는 'hs' 객체를 사용해야 정확합니다.
        hs.clickHandlerFunc = (event) => {
          console.log("[VirtualTour] Info Hotspot Clicked (Handler):", hs); // 'hs' 사용

          // 부모(TourView)에게 'hotspot-click' 이벤트를 보냅니다.
          emit('hotspot-click', { hotspot: hs }); // 'hs' 전달
        };
      }

    });
  });

  return cfg;
}

function onResize() {
  viewer.value?.resize();
}

onMounted(() => {
  if (!panoramaContainer.value) return;

  const decorated = decorateConfig(props.config);

  viewer.value = window.pannellum.viewer(panoramaContainer.value, decorated);

  viewer.value.on('load', () => {
    const newSceneId = viewer.value.getScene();
    emit('scene-changed', newSceneId);
  });

  // ★[수정]
  // 드래그(회전)를 막는 주범이었던 mousedown 리스너를
  // ★★★ 완전히 삭제했습니다. ★★★
  /*
  viewer.value.on('mousedown', (event) => {
     (이 코드 블록 전체 삭제)
  });
  */

  window.addEventListener('resize', onResize);
});

defineExpose({
  loadScene(sceneId) {
    viewer.value?.loadScene(sceneId);
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', onResize);
  if (viewer.value) {
    viewer.value.destroy();
    viewer.value = null;
  }
});
</script>
<style>
#panorama-container {
  width: 100%;
  height: 100%;
}

/* --- 커스텀 핫스팟 스타일 시작 --- */

/* 1. Pannellum 기본 툴팁 숨기기 (우리가 만든 UI만 보이도록) */
.pnlm-hotspot-base.nav-hotspot {
  background: transparent !important;
}

.pnlm-hotspot-base.nav-hotspot .pnlm-sprite {
  display: none !important;
}

/* 2. 우리 커스텀 UI (nh-wrap)의 기본 스타일 */
.nav-hotspot .pnlm-tooltip.nh-wrap {
  background: transparent;
  box-shadow: none;
  border: none;
  padding: 0;
  pointer-events: none;
}

/* 3. 실제 클릭 가능한 버튼 (nh-btn) */
.nav-hotspot .nh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  /* ★[수정] 호버 시 텍스트가 나올 공간 확보 */
  padding: 10px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.55);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
  transform: translateY(0);
  transition: transform 0.12s ease, background 0.2s ease;
  pointer-events: auto;
  cursor: pointer;
}

/* 4. 호버 및 클릭 반응 */
.nav-hotspot .nh-btn:active {
  transform: translateY(1px);
}

.nav-hotspot .nh-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

/* 5. 아이콘 (화살표) */
.nav-hotspot .nh-icon {
  width: 16px;
  height: 16px;
  display: inline-block;
  background: #fff;
  mask: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill="%23000" d="M10 3l6 6h-4v8H8V9H4l6-6z"/></svg>') no-repeat center / contain;

  /* ★[추가] 텍스트가 나타날 때 아이콘이 밀리지 않도록 */
  flex-shrink: 0;
}

/* 6. 호버 시 뜨는 글자 (nh-chip) */
.nav-hotspot .nh-chip {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -0.2px;
  line-height: 1;

  /* ★★★ [수정] 호버 애니메이션 ★★★ */
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  white-space: nowrap;
  /* 텍스트가 줄바꿈되지 않도록 */
  transition: max-width 0.3s ease-out, opacity 0.2s 0.05s ease-out;
  will-change: max-width, opacity;
}

/* ★[추가] 호버 시 텍스트(chip) 표시 */
.nav-hotspot .nh-btn:hover .nh-chip {
  max-width: 150px;
  /* 텍스트 최대 길이 (필요시 조절) */
  opacity: 1;
}

/* 7. 등장 애니메이션 */
.nav-hotspot {
  animation: nh-pop 0.18s ease-out;
}

@keyframes nh-pop {
  from {
    transform: scale(0.96);
    opacity: 0.6;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
