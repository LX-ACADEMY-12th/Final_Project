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

/** 모든 scene 핫스팟에 커스텀 UI/클래스를 주입 */
function decorateConfig(original) {
  // deep clone (현 프로젝트 환경에 맞게 선택. 최신 브라우저면 structuredClone OK)
  const cfg = typeof structuredClone === 'function'
    ? structuredClone(original)
    : JSON.parse(JSON.stringify(original));

  const scenes = cfg.scenes || {};
  Object.keys(scenes).forEach((sid) => {
    const s = scenes[sid];
    (s.hotSpots || []).forEach((hs) => {
      if (hs.type !== 'scene') return;

      // 기본 스프라이트 숨길 식별자
      hs.cssClass = ((hs.cssClass || '') + ' nav-hotspot').trim();

      // 우리 UI 그리기
      hs.createTooltipFunc = function (hotSpotDiv, args) {
        // pannellum이 .pnlm-tooltip을 전달해줌
        hotSpotDiv.classList.add('nh-wrap');
        hotSpotDiv.innerHTML = `
          <div class="nh-btn" role="button" aria-label="${(args?.text || '이동').replace(/"/g,'&quot;')}">
            <span class="nh-icon"></span>
            ${args?.text ? `<span class="nh-chip">${args.text}</span>` : ''}
          </div>
        `;
      };

      // (선택) info처럼 별도 이벤트도 받고 싶다면
      // 클릭은 기본 동작(씬 이동)이 유지되므로 생략 가능.
      // hs.clickHandlerFunc = () => { /* 커스텀 로직 */ };
    });
  });

  return cfg;
}

function onResize() {
  viewer.value?.resize();
}

onMounted(() => {
  if (!panoramaContainer.value) return;

  // ★ 여기서 먼저 데코레이션
  const decorated = decorateConfig(props.config);

  viewer.value = window.pannellum.viewer(panoramaContainer.value, decorated);

  viewer.value.on('load', () => {
    const newSceneId = viewer.value.getScene();
    emit('scene-changed', newSceneId);
  });

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
  /* 클릭 이벤트가 통과되도록 */
  pointer-events: none;
}

/* 3. 실제 클릭 가능한 버튼 (nh-btn) */
.nav-hotspot .nh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.55);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
  transform: translateY(0);
  transition: transform 0.12s ease, background 0.2s ease;
  
  /* ⭐️ 클릭 이벤트를 받도록 설정 (중요) */
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
}

/* 6. 호버 시 뜨는 글자 (nh-chip) */
.nav-hotspot .nh-chip {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -0.2px;
  line-height: 1;
}

/* 7. 등장 애니메이션 */
.nav-hotspot { 
  animation: nh-pop 0.18s ease-out; 
}
@keyframes nh-pop { 
  from { transform: scale(0.96); opacity: 0.6; }
  to { transform: scale(1); opacity: 1; }
}
</style>