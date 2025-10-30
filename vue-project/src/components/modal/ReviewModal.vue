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
        
        <div class="image-preview-list">
          <div 
            v-for="(preview, index) in filePreviews" 
            :key="index" 
            class="preview-item"
            :style="{ backgroundImage: 'url(' + preview.url + ')' }"
          >
            <button class="remove-btn" @click.stop="removeFile(index)">&times;</button>
            <span class="preview-label">사진{{ index + 1 }}</span>
          </div>

          <div 
            class="image-upload-area" 
            @click="openFilePicker" 
            v-if="uploadedFiles.length < 5" >
            <span class="gallery-icon"><i class="bi bi-image"></i></span>
            <span class="upload-count">{{ uploadedFiles.length }}/5</span>
          </div>
        </div>

        <input type="file"
              ref="fileInput"
              @change="onFileChange"
              hidden
              multiple
              accept="image/*">    
        
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
  props: {
    reviewText: String,
    selectedRating: Number,
    // ⭐️ [수정됨] uploadedImageCount prop 삭제
    isFormValid: Boolean, // 버튼 활성화 여부
    uploadedFiles: {
      type: Array,
      default: () => []
    }
  },
  emits: ['close', 'submit', 'update:reviewText', 'update:selectedRating', 'files-selected', 'remove-file'],
  
  computed: {
    filePreviews() {
      // ⭐️ [수정됨] 방어 코드: 부모가 undefined를 보내도 오류 안 나게
      if (!this.uploadedFiles) return []; 
      
      return this.uploadedFiles.map(file => {
        return { 
          file: file, 
          url: URL.createObjectURL(file) 
        };
      });
    }
  },

  methods: {
    setRating(star) {
      this.$emit('update:selectedRating', star);
    },
    openFilePicker() {
      this.$refs.fileInput.click();
    },
    onFileChange(event) {
      const files = event.target.files;
      if (files.length > 0) {
        this.$emit('files-selected', files);
      }
      event.target.value = null;
    },
    removeFile(index) {
      this.$emit('remove-file', index);
    }
  },

  // ⭐️ [수정됨] beforeDestroy -> beforeUnmount (Vue 3 호환)
  beforeUnmount() {
    // 생성했던 Object URL들을 메모리에서 해제
    if (this.filePreviews) {
      this.filePreviews.forEach(preview => {
        URL.revokeObjectURL(preview.url);
      });
    }
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

/* === [수정] modal-body 스타일 === */
.modal-body {
  padding: 20px;
}

/* [추가] 이미지 그리드 컨테이너 */
.image-preview-list {
  display: grid;
  /* 3열 그리드, 1:1 비율 */
  grid-template-columns: repeat(3, 1fr);
  gap: 10px; /* 아이템 간 간격 */
  margin-bottom: 20px;
}

/* [수정] 기존 업로드 버튼 스타일 (그리드 아이템 공통) */
.image-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%; /* 그리드에 맞게 100% */
  aspect-ratio: 1 / 1; /* 정사각형 */
  background-color: #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  /* [제거] margin: 0 auto 20px auto; */
}

/* [추가] 미리보기 아이템 스타일 */
.preview-item {
  position: relative; /* 삭제 버튼 위치 기준 */
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 8px;
  background-color: #eee; /* 이미지 로딩 전 배경 */
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden; /* 라벨 등이 삐져나가지 않게 */
}

/* [추가] 미리보기 아이템 위 '사진1' 라벨 (선택) */
.preview-label {
    background-color: rgba(0,0,0,0.3);
    color: white;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 4px;
}

/* [추가] 삭제 버튼 'X' */
.remove-btn {
  position: absolute;
  top: -5px;      /* 모서리 바깥으로 살짝 */
  right: -5px;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  font-weight: bold;
  line-height: 24px; /* 텍스트 중앙 정렬 */
  text-align: center;
  cursor: pointer;
  padding: 0;
  z-index: 2; /* 미리보기 이미지 위로 */
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