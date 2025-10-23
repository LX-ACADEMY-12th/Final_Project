<template>
  <div class="modal-backdrop">
    <div class="review-modal">

      <header class="modal-header">
        <div class="title-wrap">
          <span class="icon"><i class="bi bi-pencil-square"></i></span>
          <h2 class="modal-title">후기작성</h2>
        </div>
        <button class="close-btn" @click="$emit('close')">
          &times;
        </button>
      </header>

      <hr class="header-divider" />

      <main class="modal-body">
        <div class="image-upload-area">
          <span class="gallery-icon"><i class="bi bi-image"></i></span>
          <span class="upload-count">{{ uploadedImageCount }}/5</span>
        </div>

        <textarea 
          :value="reviewText"
          @input="$emit('update:reviewText', $event.target.value)" 
          placeholder="후기를 작성하세요." 
          class="review-textarea"
        ></textarea>

        <div class="rating-area">
          <span 
            v-for="star in 5" 
            :key="star" 
            class="star-icon"
            :class="{ 'filled': star <= selectedRating }"
            @click="setRating(star)"
          >
            ★
          </span>
          <span class="rating-score">{{ selectedRating.toFixed(1) }}</span>
        </div>
      </main>

      <footer class="modal-footer">
        <button 
          class="submit-btn" 
          :disabled="!isFormValid"
          @click="$emit('submit')"
        >
          후기 올리기
        </button>
      </footer>

    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewModal',
  // 부모로부터 모달의 상태 및 폼 데이터를 props로 받습니다.
  props: {
    reviewText: String,
    selectedRating: Number,
    uploadedImageCount: Number,
    isFormValid: Boolean, // 버튼 활성화 여부
  },
  // 부모에게 보낼 이벤트를 명시합니다.
  emits: ['close', 'submit', 'update:reviewText', 'update:selectedRating'],
  methods: {
    // 별점 클릭 시 부모에게 변경된 별점 값을 전달합니다.
    setRating(star) {
      this.$emit('update:selectedRating', star);
    },
  }
};
</script>

<style scoped>
/* === 모달 스타일 (후기 작성) === */
.modal-backdrop {
  /* 모달 배경: 전체 화면을 덮고 모달을 중앙에 위치시킵니다. */
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.review-modal {
  /* 모달 창 자체의 스타일 */
  background: white;
  width: 90%;
  max-width: 400px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  /* 모달 헤더 (제목 및 닫기 버튼) */
  background: #4A7CEC;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 32px;
}

.title-wrap {
  display: flex;
  align-items: center;
}

.modal-title {
  font-size: 20px;
  font-weight: bold;
  color: white;
  margin: 0;
  margin-left: 8px;
}

.icon {
  font-size: 20px;
  /* Bootstrap Icons 크기 조절 */
  line-height: 1.2;
  color: white;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: white;
  cursor: pointer;
  padding: 0;
  line-height: 1.2;
}

.header-divider {
  border: none;
  border-top: 1px solid #eee;
  margin: 0;
}

.modal-body {
  padding: 20px;
}

.image-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background-color: #f0f0f0;
  border-radius: 8px;
  margin: 0 auto 20px auto;
  cursor: pointer;
}

.gallery-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.upload-count {
  font-size: 12px;
  color: #666;
}

.review-textarea {
  width: 100%;
  height: 150px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  resize: none;
  box-sizing: border-box;
  font-family: inherit;
}

.review-textarea::placeholder {
  color: #999;
  text-align: center;
  line-height: 120px;
}

.rating-area {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.star-icon {
  font-size: 36px;
  color: #ccc;
  cursor: pointer;
  transition: color 0.2s;
  margin: 0 2px;
}

.star-icon.filled {
  color: #FFC107;
}

.rating-score {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-left: 16px;
  width: 50px;
  text-align: left;
}

.modal-footer {
  padding: 0 20px 20px 20px;
}

.submit-btn {
  width: 100%;
  background-color: #4A7CEC;
  color: white;
  border: none;
  padding: 14px 0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:disabled {
  background-color: #c0c0c0;
  cursor: not-allowed;
}
</style>