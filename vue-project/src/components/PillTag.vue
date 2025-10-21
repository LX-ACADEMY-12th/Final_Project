<template>
  <span class="badge rounded-pill pill-tag" :style="tagStyle">
    {{ text }}
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  text: String,
  type: {
    type: String,
    default: 'grade' // 'subject' 또는 'grade'
  }
});

// 이미지(`image_492c0a.png`)를 기반으로 한 색상 맵
const subjectColors = {
  '지구': { bg: '#C9F0FF', text: '#007AFF' },
  '물리': { bg: '#E0D6FF', text: '#582DE8' },
  '화학': { bg: '#FFD6E8', text: '#D9006C' },
  '생명': { bg: '#A6FFD9', text: '#008A50' },
  '통합': { bg: '#333333', text: '#FFFFFF' },
  'default': { bg: '#EFEFEF', text: '#555' } // 학년 및 기타
};

const tagStyle = computed(() => {
  let colors;
  if (props.type === 'subject') {
    // text 값에 해당하는 색상을 찾고, 없으면 default 색상 사용
    colors = subjectColors[props.text] || subjectColors['default'];
  } else {
    // type이 'grade'인 경우
    colors = subjectColors['default'];
  }

  return {
    'background-color': colors.bg,
    'color': colors.text
  };
});
</script>

<style scoped>
.pill-tag {
  font-size: 0.85rem;
  font-weight: 600;
  padding: 0.3em 0.75em;
}
</style>
