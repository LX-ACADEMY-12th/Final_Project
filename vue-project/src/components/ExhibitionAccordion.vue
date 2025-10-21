<template>
  <div class="mb-3">
    <button 
      class="btn btn-white w-100 d-flex justify-content-between align-items-center border rounded p-3"
      type="button" 
      @click="$emit('toggle')"
    >
      <span class="fw-bold">{{ title }}</span>
      <i class="bi" :class="isActive ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
    </button>
    
    <div v-if="isActive" class="bg-white border border-top-0 rounded-bottom p-3">
      <div class="map-placeholder bg-light rounded mb-3 d-flex align-items-center justify-content-center" style="height: 200px;">
        <span class="text-muted">전시관 지도</span>
      </div>
      <div class="text-center">
        <button class="btn btn-primary rounded-pill px-4" @click="$emit('open-modal')">
          층별안내도보기
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExhibitionAccordion',
  props: {
    title: {
      type: String,
      required: true
    },
    isActive: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle', 'open-modal']
}
</script>

<style scoped>
.btn-white {
  background-color: white;
  text-align: left;
}

.btn-white:hover {
  background-color: #f8f9fa;
}

.map-placeholder {
  border: 1px dashed #dee2e6;
}

.bi {
  transition: transform 0.3s ease;
}
</style>
