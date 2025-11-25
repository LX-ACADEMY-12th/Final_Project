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

/* --- 커스텀 핫스팟 스타일 --- */

/* 1. Pannellum 기본 아이콘 숨기기 (기존 동일) */
.pnlm-hotspot-base.nav-hotspot {
  background: transparent !important;
}
.pnlm-hotspot-base.nav-hotspot .pnlm-sprite {
  display: none !important;
}

/* 2. 툴팁 래퍼 초기화 (기존 동일) */
.nav-hotspot .pnlm-tooltip.nh-wrap {
  background: transparent;
  box-shadow: none;
  border: none;
  padding: 0;
  pointer-events: none;
  margin-top: -3px; /* 위치 미세 조정 */
}

/* 3. 실제 버튼 (nh-btn) */
.nav-hotspot .nh-btn {
  display: flex;
  align-items: center;
  gap: 8px; /* 아이콘과 글자 사이 간격 */
  padding: 8px 12px; /* 안쪽 여백 */
  border-radius: 20px;
  
  /* 배경을 항상 진하게 설정 (글자가 잘 보이도록) */
  background: rgba(0, 0, 0, 0.7); 
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  
  pointer-events: auto;
  cursor: pointer;
  transition: transform 0.1s ease, background 0.2s;
}

/* 호버 시 배경만 약간 더 진하게 (크기 변화 X) */
.nav-hotspot .nh-btn:hover {
  background: rgba(0, 0, 0, 0.85);
  transform: scale(1.05); /* 살짝 커지는 효과 */
}

/* 4. 아이콘 (화살표) */
.nav-hotspot .nh-icon {
  width: 16px;
  height: 16px;
  display: block;
  background: #fff;
  /* 아이콘 모양 */
  mask: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill="%23000" d="M10 3l6 6h-4v8H8V9H4l6-6z"/></svg>') no-repeat center / contain;
  flex-shrink: 0;
}

/* 5. 글자 (nh-chip) - ★여기가 핵심 수정★ */
.nav-hotspot .nh-chip {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  
  /* 👇 기존의 숨김(max-width: 0, opacity: 0) 코드를 모두 삭제하고, 항상 보이게 설정 */
  display: block; 
  opacity: 1;
  max-width: none;
}

/* 6. 등장 애니메이션 (선택사항) */
.nav-hotspot {
  animation: nh-pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes nh-pop {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>