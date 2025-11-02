<template>
  <span v-if="text !== 'science_place'" class="badge bg-dark type-badge">
    {{ text }}
  </span>
</template>

<script setup>
defineProps({ text: String });
</script>

<style scoped>
.type-badge {
  font-size: 0.75rem;
  padding: 3px 6px;
  border-radius: 4px;
}
</style>
