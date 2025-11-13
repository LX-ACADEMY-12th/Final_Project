<template>
  <section class="tab-section">
    <div class="tabs">
      <button :class="['tab-item', { active: activeTab === 'detail' }]" @click="$emit('updateTab', 'detail')">상세보기
      </button>

      <button :class="['tab-item', { active: activeTab === 'recommend' }]" @click="$emit('updateTab', 'recommend')">
        {{ rightTabText }}
      </button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'TabMenu',
  // 부모 컴포넌트로부터 현재 화면이 장소인지 여부를 받습니다.
  props: {
    isPlace: {
      type: Boolean,
      required: true, // 이 prop은 반드시 전달되어야 합니다.
      default: false,
    },

    // 부모로부터 현재 활성화된 탭이 무엇인지 받는다.
    activeTab: {
      type: String,
      required: true,
      default: 'detail', // 기본값은 detail
    }
  },
  // 부모에게 'updateTab' 이벤트를 보낼 것이라고 명시합니다.
  emits: ['updateTab'],

  // 탭 텍스트를 동적으로 결정하는 계산된 속성
  computed: {
    // isPlace 값에 따라 오른쪽 탭의 텍스트를 결정합니다.
    rightTabText() {
      // isPlace가 true (장소 상세화면)이면 'AI 추천 코스'
      if (this.isPlace) {
        return 'AI 추천 코스';
      }
      // isPlace가 false (전시 상세화면)이면 '실내 코스 추천'
      else {
        return 'AI 추천 관람';
      }
    },
  },
};
</script>

<style scoped>
/* === 탭 버튼 섹션 스타일 === */
.tab-section {
  background-color: white;
  padding: 0 15px;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  padding: 12px 0;
  margin: 0 10px;
  background: none;
  border: none;
  font-size: 16px;
  color: #999;
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: #333;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #007bff;
}
</style>
