<template>
  <span class="hashtag-badge bg-light text-secondary fw-normal px-2 py-1"
    style="font-family: 'SUIT', sans-serif; font-size: 0.7rem;">
    #{{ displayText }}
  </span>
</template>

<script setup>

import { computed } from 'vue';

const props = defineProps({
  text: {
    type: String,
    required: true
  }
});

// 텍스트를 정제하는 computed 속성 추가
const displayText = computed(() => {
  // props.text가 null이나 undefined일 경우를 대비해 || '' 추가
  // replace()와 정규식(/[{}"']/g)을 사용해 모든 {, }, " 문자를 제거
  return (props.text || '').replace(/[{}"]/g, '');
});

</script>

<style scoped>
.hashtag-badge {
  /* 1. 말줄임표 적용을 위한 필수 스타일 */
  display: inline-block;
  /* 너비(width)를 가질 수 있도록 변경 */
  /* [!!] 원하는 최대 너비를 여기서 조절하세요 */
  max-width: 100px;
  /* 텍스트 줄바꿈 방지 */
  white-space: nowrap;
  /* 너비를 넘어가는 텍스트 숨김 */
  overflow: hidden;
  /* 숨겨진 텍스트를 말줄임표(...)로 표시 */
  text-overflow: ellipsis;

  /* 2. 세로 정렬을 위한 미세 조정 (선택 사항) */
  /* 다른 텍스트와 세로 중앙 정렬 */
  vertical-align: middle;
}
</style>
