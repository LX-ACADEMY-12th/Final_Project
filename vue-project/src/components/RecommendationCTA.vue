<template>
 <div class="cta-wrap" :class="{ inset: inset }">
  <div class="cta-inner">

   <div class="cta-title">{{ title }}</div>

   <div class="cta-divider" v-if="showDivider"></div>

   <div class="cta-sub">
    <!-- [수정] 가독성을 위해 v-html 제거하고 일반 텍스트로 변경 -->
    <span>{{ subtitleLine1 }}</span><br />
    <span>{{ subtitleLine2 }}</span>
   </div>

   <div class="cta-actions">
    <!-- [수정] props(primaryText)와 disabled 속성 다시 연결 -->
    <button
     class="btn btn-primary"
     type="button"
     :disabled="primaryDisabled || primaryLoading"
     @click="$emit('save-route')"
    >
     <i class="bi bi-heart"></i>
     <span>{{ primaryText }}</span>
    </button>

    <!-- [수정] disabled 속성 다시 연결 -->
    <button
     class="btn btn-secondary"
     type="button"
     :disabled="secondaryDisabled || secondaryLoading"
     @click="$emit('request-new')"
    >
     <i class="bi bi-arrow-clockwise"></i>
     <span>{{ secondaryText }}</span>
    </button>
   </div>

  </div>
 </div>
</template>

<script>
export default {
 name: 'RecommendationCTA',
 props: {
  title: { type: String, default: '추천코스가 마음에 드세요?' },
  subtitleLine1: { type: String, default: '추천받은 일정을 저장하면' },
  subtitleLine2: { type: String, default: '마이페이지에서 언제든 확인하고 편집할 수 있어요!' },
  primaryText: { type: String, default: '경로 저장하기' },
  secondaryText: { type: String, default: '새로운 추천받기' },
  showDivider: { type: Boolean, default: true },
  inset: { type: Boolean, default: true },
  primaryLoading: { type: Boolean, default: false },
  secondaryLoading: { type: Boolean, default: false },
  primaryDisabled: { type: Boolean, default: false },
  secondaryDisabled: { type: Boolean, default: false }
 },
 emits: [ 'save-route', 'request-new' ],
}
</script>

<style scoped>
.cta-wrap {
 padding: 4px 0;
}
.cta-wrap.inset .cta-inner {
 margin: 0 16px;
 margin-bottom: 32px;
}
.cta-inner {
 background-color: #f8f9fa; /* [수정] 배경색을 살짝 어둡게 변경 */
 border: 1px solid #e9ecef; /* [수정] 테두리 추가 */
 border-radius: 16px;
 padding: 24px 16px;
 text-align: center;
}
.cta-title {
 font-size: 20px;
 font-weight: 800;
 color: #333;
 margin-bottom: 8px;
}
.cta-divider {
 width: 300px; /* [수정] 길이를 짧게 */
 height: 2px; /* [수정] 두께를 조금 두껍게 */
 background: #d6d6d6; /* [수정] 포인트 색상으로 변경 */
 margin: 0 auto 12px;
 border-radius: 2px;
}
.cta-sub {
 font-size: 13px; /* [수정] 가독성을 위해 크기 살짝 키움 */
 color: #555;
 line-height: 1.6;
 margin-bottom: 20px;
}
.cta-actions {
 display: grid;
 grid-template-columns: 1fr 1fr;
 gap: 12px;
}
.btn {
 height: 48px;
 border-radius: 12px;
 font-size: 14px;
 font-weight: 700;
 border: 1px solid #ced4da; /* [수정] 기본 테두리 색상 */
 background: #ffffff;
 display: inline-flex;
 align-items: center;
 justify-content: center;
 gap: 8px;
 width: 100%;
 box-shadow: 0 1px 3px rgba(0,0,0,0.05); /* [수정] 그림자 약하게 */
 transition: all 0.2s ease;
}

/* [수정] 주요 버튼은 눈에 띄게 */
.btn-primary {
 background-color: #ffffff;
 color: #333; 
}

/* [수정] 보조 버튼은 기본 스타일로 */
.btn-secondary {
 background-color: #ffffff;
 border-color: #ced4da;
 color: #333;
}

button:disabled {
 opacity: 0.5;
 cursor: not-allowed;
}
</style>