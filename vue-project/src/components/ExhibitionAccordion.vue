<template>
  <div class="mb-3">
    <button class="btn btn-white w-100 d-flex justify-content-between align-items-center border rounded p-3"
      type="button" @click="$emit('toggle')">
      <span class="fw-bold">{{ title }}</span>
      <i class="bi" :class="isActive ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
    </button>

    <div v-if="isActive" class="bg-white border border-top-0 rounded-bottom p-3">

      <div v-if="previewImageUrl" class="mb-3">
        <img :src="previewImageUrl" :alt="`${title} 미리보기`" class="accordion-map-image rounded" />
      </div>

      <div v-else class="map-placeholder bg-light rounded mb-3 d-flex align-items-center justify-content-center"
        style="height: 200px;">
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
    },
    // 미리보기 이미지 URL을 받을 prop
    previewImageUrl: {
      type: String,
      default: null
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

/* [추가] 새로 추가된 이미지 스타일 */
.accordion-map-image {
  width: 100%;
  height: 200px;
  /* 기존 플레이스홀더 높이와 맞춤 */
  object-fit: cover;
  /* 이미지가 비율을 유지하며 꽉 차게 */
  background: #f8f9fa;
  border: 1px solid #dee2e6;
}
</style>
