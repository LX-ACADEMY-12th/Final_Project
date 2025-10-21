<template>
  <div v-if="show" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <!-- 모달 헤더 -->
      <div class="modal-header bg-primary text-white p-3 d-flex align-items-center">
        <i class="bi bi-map me-2"></i>
        <span class="fw-bold flex-grow-1">{{ hallName }}</span>
        <button class="btn btn-link text-white p-0" @click="closeModal">
          <i class="bi bi-x-lg fs-5"></i>
        </button>
      </div>

      <!-- 층 표시 -->
      <div class="text-center py-3">
        <h4 class="fw-bold mb-0">{{ floors[currentFloorIndex] }}</h4>
      </div>

      <!-- 이미지 슬라이더 -->
      <div class="slider-container">
        <div class="slider-wrapper" :style="{ transform: `translateX(-${currentFloorIndex * 100}%)` }"
          @touchstart="handleTouchStart" @touchmove="handleTouchMove" @touchend="handleTouchEnd"
          @mousedown="handleMouseDown">
          <div v-for="(floor, index) in floors" :key="index" class="floor-slide">
            <div class="floor-image-placeholder">
              <i class="bi bi-geo-alt fs-1 text-muted"></i>
              <p class="text-muted mt-2">실내 평면도</p>
              <small class="text-muted">{{ floor }} · 구조도</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 인디케이터 (점) -->
      <div class="indicators d-flex justify-content-center gap-2 py-3">
        <span v-for="(floor, index) in floors" :key="index" class="indicator"
          :class="{ active: index === currentFloorIndex }" @click="goToFloor(index)"></span>
      </div>

      <!-- 층 버튼들 -->
      <div class="floor-buttons d-flex justify-content-center gap-3 pb-3">
        <button v-for="(floor, index) in floors" :key="index" class="btn floor-btn"
          :class="{ 'btn-primary': index === currentFloorIndex, 'btn-outline-secondary': index !== currentFloorIndex }"
          @click="goToFloor(index)">
          {{ floor }}
        </button>
      </div>

      <!-- 안내 문구 -->
      <div class="text-center pb-3">
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
    floors: {
      type: Array,
      default: () => ['B1F', '1F', '2F', '3F']
    }
  },
  emits: ['close', 'floor-change'],
  data() {
    return {
      currentFloorIndex: 1,
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
        this.currentFloorIndex = 1;
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = 'auto';
      }
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
      const swipeDistance = this.touchStartX - this.touchEndX;
      const minSwipeDistance = 50;

      if (Math.abs(swipeDistance) > minSwipeDistance) {
        if (swipeDistance > 0 && this.currentFloorIndex < this.floors.length - 1) {
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
        const swipeDistance = this.mouseStartX - this.mouseEndX;
        const minSwipeDistance = 50;

        if (Math.abs(swipeDistance) > minSwipeDistance) {
          if (swipeDistance > 0 && this.currentFloorIndex < this.floors.length - 1) {
            this.currentFloorIndex++;
          } else if (swipeDistance < 0 && this.currentFloorIndex > 0) {
            this.currentFloorIndex--;
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
/* 모달 스타일 */
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

/* 슬라이더 */
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
  min-width: 100%;
  flex-shrink: 0;
  padding: 0 20px;
}

.floor-image-placeholder {
  background: #e9ecef;
  border-radius: 8px;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  width: 100%;
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
}

/* 스크롤바 커스텀 */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
</style>
