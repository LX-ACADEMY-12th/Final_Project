<template>
  <div v-if="show" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header bg-primary text-white p-3 d-flex align-items-center">
        <i class="bi bi-map me-2"></i>
        <span class="fw-bold flex-grow-1">{{ hallName }}</span>
        <button class="btn btn-link text-white p-0" @click="closeModal">
          <i class="bi bi-x-lg fs-5"></i>
        </button>
      </div>

      <div class="text-center py-3" v-if="floorGuides.length > 0">
        <h4 class="fw-bold mb-0">{{ floorGuides[currentFloorIndex].floorName }}</h4>
      </div>

      <div class="slider-container">
        <div class="slider-wrapper" :style="{ transform: `translateX(-${currentFloorIndex * 100}%)` }"
          @touchstart="handleTouchStart" @touchmove="handleTouchMove" @touchend="handleTouchEnd"
          @mousedown="handleMouseDown">

          <div v-for="(guide, index) in floorGuides" :key="index" class="floor-slide">
            <img :src="guide.imageUrl" :alt="`${hallName} ${guide.floorName} 안내도`" class="floor-image"
              @dragstart.prevent />
          </div>
        </div>
      </div>

      <div class="indicators d-flex justify-content-center gap-2 py-3">
        <span v-for="(guide, index) in floorGuides" :key="index" class="indicator"
          :class="{ active: index === currentFloorIndex }" @click="goToFloor(index)"></span>
      </div>

      <div class="floor-buttons d-flex justify-content-center gap-3 pb-3">
        <button v-for="(guide, index) in floorGuides" :key="index" class="btn floor-btn"
          :class="{ 'btn-primary': index === currentFloorIndex, 'btn-outline-secondary': index !== currentFloorIndex }"
          @click="goToFloor(index)">
          {{ guide.floorName }}
        </button>
      </div>

      <div class="text-center pb-3" v-if="floorGuides.length > 1">
        <small class="text-muted">
          <i class="bi bi-info-circle me-1"></i>
          좌우로 스와이프 하여 다른 층을 확인하세요
        </small>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FloorGuideModal',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    hallName: {
      type: String,
      required: true
    },
    // [수정] 'floors' (문자열 배열) 대신 'floorGuides' (객체 배열)를 받습니다.
    floorGuides: {
      type: Array, // [{ floorName: '1F', imageUrl: '...' }, ...]
      default: () => []
    }
  },
  emits: ['close', 'floor-change'],
  data() {
    return {
      currentFloorIndex: 0, // [수정] 1이 아닌 0 (첫 번째 인덱스)
      touchStartX: 0,
      touchEndX: 0,
      mouseStartX: 0,
      mouseEndX: 0,
      isDragging: false
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        // [수정] 1(2번째)이 아닌 0(첫 번째)으로 초기화
        this.currentFloorIndex = 0;
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = 'auto';
      }
    },
    // [선택] floorGuides가 바뀔 때(다른 전시관 클릭)도 0으로 초기화
    floorGuides() {
      this.currentFloorIndex = 0;
    }
  },
  methods: {
    closeModal() {
      this.$emit('close');
    },
    goToFloor(index) {
      this.currentFloorIndex = index;
      this.$emit('floor-change', index);
    },
    // [수정] 스와이프 로직에서 this.floors -> this.floorGuides 로 변경
    handleTouchStart(e) {
      this.touchStartX = e.touches[0].clientX;
    },
    handleTouchMove(e) {
      this.touchEndX = e.touches[0].clientX;
    },
    handleTouchEnd() {
      if (this.floorGuides.length <= 1) return; // 층이 하나면 스와이프 안 함
      const swipeDistance = this.touchStartX - this.touchEndX;
      const minSwipeDistance = 50;

      if (Math.abs(swipeDistance) > minSwipeDistance) {
        if (swipeDistance > 0 && this.currentFloorIndex < this.floorGuides.length - 1) {
          this.currentFloorIndex++;
        } else if (swipeDistance < 0 && this.currentFloorIndex > 0) {
          this.currentFloorIndex--;
        }
      }
    },
    handleMouseDown(e) {
      this.isDragging = true;
      this.mouseStartX = e.clientX;
      document.addEventListener('mousemove', this.handleMouseMove);
      document.addEventListener('mouseup', this.handleMouseUp);
    },
    handleMouseMove(e) {
      if (this.isDragging) {
        this.mouseEndX = e.clientX;
      }
    },
    handleMouseUp() {
      if (this.isDragging) {
        if (this.floorGuides.length > 1) { // 층이 하나 이상일 때만
          const swipeDistance = this.mouseStartX - this.mouseEndX;
          const minSwipeDistance = 50;

          if (Math.abs(swipeDistance) > minSwipeDistance) {
            if (swipeDistance > 0 && this.currentFloorIndex < this.floorGuides.length - 1) {
              this.currentFloorIndex++;
            } else if (swipeDistance < 0 && this.currentFloorIndex > 0) {
              this.currentFloorIndex--;
            }
          }
        }

        this.isDragging = false;
        document.removeEventListener('mousemove', this.handleMouseMove);
        document.removeEventListener('mouseup', this.handleMouseUp);
      }
    }
  },
  beforeUnmount() {
    document.removeEventListener('mousemove', this.handleMouseMove);
    document.removeEventListener('mouseup', this.handleMouseUp);
    document.body.style.overflow = 'auto';
  }
}
</script>

<style scoped>
/* 모달 스타일 (기존과 동일) */
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  border-radius: 12px 12px 0 0;
}

/* 슬라이더 (기존과 동일) */
.slider-container {
  position: relative;
  overflow: hidden;
  padding: 0;
}

.slider-wrapper {
  display: flex;
  transition: transform 0.3s ease-out;
  touch-action: pan-y;
  user-select: none;
  cursor: grab;
}

.slider-wrapper:active {
  cursor: grabbing;
}

.floor-slide {
  min-width: 80%;
  flex-shrink: 0;
  padding: 0;
  box-sizing: border-box;
  /* 패딩이 너비에 포함되도록 */
}

/* [수정] Placeholder 대신 실제 이미지 스타일 */
.floor-image {
  width: 100%;
  height: 250px;
  /* 고정 높이 */
  /* 이미지가 비율을 유지하며 꽉 차게 포함됨 */
  object-fit: cover;
  ;
  background: #f8f9fa;
  /* 이미지가 없는 경우 배경색 */
  border-radius: 8px;
  user-select: none;
  /* 이미지 드래그 방지 */
}

/* 인디케이터 (기존과 동일) */
.indicators {
  user-select: none;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #dee2e6;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  width: 24px;
  border-radius: 4px;
  background-color: #0d6efd;
}

/* 층 버튼 (기존과 동일) */
.floor-btn {
  min-width: 50px;
  font-size: 0.9rem;
  padding: 0.4rem 0.8rem;
}

/* 스크롤바 (기존과 동일) */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
</style>
