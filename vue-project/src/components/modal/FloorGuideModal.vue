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
    floorGuides: {
      type: Array,
      default: () => []
    }
  },
  emits: ['close', 'floor-change'],
  data() {
    return {
      currentFloorIndex: 0,
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
        this.currentFloorIndex = 0;
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = 'auto';
      }
    },
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
    handleTouchStart(e) {
      this.touchStartX = e.touches[0].clientX;
    },
    handleTouchMove(e) {
      this.touchEndX = e.touches[0].clientX;
    },
    handleTouchEnd() {
      if (this.floorGuides.length <= 1) return;
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
        if (this.floorGuides.length > 1) {
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
/* 모달 오버레이 */
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

/* 모달 컨텐츠 */
.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 450px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

/* 모달 헤더 */
.modal-header {
  border-radius: 12px 12px 0 0;
}

/* 슬라이더 컨테이너 - 모달 너비에 맞게 개선 */
.slider-container {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 350px;
  padding: 0;
  display: flex;
  align-items: center;
}

/* 슬라이더 래퍼 */
.slider-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease-out;
  touch-action: pan-y;
  user-select: none;
  cursor: grab;
}

.slider-wrapper:active {
  cursor: grabbing;
}

/* 각 슬라이드 - 모달 전체 너비 사용 & 중앙 정렬 */
.floor-slide {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  height: 100%;
  flex-shrink: 0;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 이미지 스타일 개선 - 비율 유지 */
.floor-image {
  max-width: 100%;
  width: auto;
  height: auto;
  max-height: 100%;
  object-fit: contain;
  background: #f8f9fa;
  border-radius: 8px;
  user-select: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: block;
}

/* 인디케이터 */
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

.indicator:hover {
  background-color: #adb5bd;
}

.indicator.active {
  width: 24px;
  border-radius: 4px;
  background-color: #0d6efd;
}

/* 층 버튼 */
.floor-btn {
  min-width: 50px;
  font-size: 0.9rem;
  padding: 0.4rem 0.8rem;
  transition: all 0.2s ease;
}

.floor-btn:hover {
  transform: translateY(-1px);
}

/* 스크롤바 스타일 */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

/* 반응형 - 작은 화면 */
@media (max-width: 480px) {
  .modal-content {
    max-width: 95%;
  }

  .slider-container {
    height: 280px;
  }

  .floor-slide {
    padding: 15px;
  }

  .floor-btn {
    font-size: 0.85rem;
    padding: 0.35rem 0.7rem;
    min-width: 45px;
  }
}
</style>
