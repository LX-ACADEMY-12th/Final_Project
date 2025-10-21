<template>
  <span class="badge bg-dark type-badge">
    {{ text }}
  </span>
</template>

<script setup>
defineProps({ text: String });
</script>

<style scoped>
.type-badge {
  /* 기존 인라인 스타일을 클래스로 이동 */
  font-size: 0.75rem;
  padding: 3px 6px;
  border-radius: 4px;
}
</style>
