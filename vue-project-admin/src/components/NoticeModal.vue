<template>
  <Teleport to="body">
    <div v-if="show" class="modal-backdrop" @click.self="$emit('close')">
      <div class="modal-content">
        <!-- 헤더 -->
        <div class="modal-header">
          <div class="header-left">
            <i class="bi bi-megaphone"></i>
            <h2>{{ isEditMode ? '공지사항 수정' : '새 공지사항 작성' }}</h2>
          </div>
          <button class="close-btn" @click="$emit('close')" type="button">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <!-- 본문 -->
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <!-- 제목 입력 -->
            <div class="form-group">
              <input type="text" class="title-input" id="noticeTitle" v-model="formData.title" placeholder="제목을 입력하세요"
                required />
            </div>

            <!-- 구분선 -->
            <div class="divider"></div>

            <!-- 내용 입력 -->
            <div class="form-group">
              <textarea class="content-textarea" id="noticeContent" v-model="formData.content" @input="onContentInput"
                placeholder="내용을 입력하세요&#10;&#10;" required></textarea>
            </div>

            <!-- 하단 버튼 -->
            <div class="modal-footer">
              <div class="footer-left">
                <span class="char-count">{{ formData.content.length }}자</span>
              </div>
              <div class="footer-right">
                <button type="button" class="btn-cancel" @click="$emit('close')">
                  취소
                </button>
                <button type="submit" class="btn-submit">
                  {{ isEditMode ? '수정하기' : '등록하기' }}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
export default {
  name: "NoticeModal",
  props: {
    show: Boolean,
    notice: Object,
    isEditMode: Boolean,
  },
  emits: ["close", "save-notice"],
  data() {
    return {
      formData: {
        title: "",
        content: "",
      },
    };
  },
  watch: {
    show(val) {
      if (val) {
        document.body.style.overflow = "hidden";
        this.initializeForm();
      } else {
        setTimeout(() => {
          document.body.style.overflow = "";
        }, 200);
      }
    },
    notice: {
      handler() {
        if (this.show) {
          this.initializeForm();
        }
      },
      deep: true,
    },
  },
  methods: {
    initializeForm() {
      if (this.isEditMode && this.notice) {
        this.formData.title = this.notice.title || "";
        this.formData.content = this.notice.content || "";
      } else {
        this.formData.title = "";
        this.formData.content = "";
      }
    },
    // ✅ [추가] input 이벤트 핸들러
    onContentInput() {
      // input 이벤트가 발생하면 formData.content가 이미 업데이트됨
      // 이 메서드는 Vue 반응성을 명시적으로 트리거하기 위함
      this.$forceUpdate();
    },
    handleSubmit() {
      if (!this.formData.title || !this.formData.content) {
        alert("제목과 내용을 모두 입력하세요.");
        return;
      }

      const noticeData = {
        ...this.formData,
      };

      if (this.isEditMode && this.notice) {
        noticeData.id = this.notice.id;
      }

      this.$emit("save-notice", noticeData);
      this.resetForm();
    },
    resetForm() {
      this.formData.title = "";
      this.formData.content = "";
    },
  },
  beforeUnmount() {
    document.body.style.overflow = "";
  },
};
</script>

<style scoped>
/* 모달 배경 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  padding: 20px;
}

/* 모달 컨텐츠 */
.modal-content {
  width: 100%;
  max-width: 900px;
  height: 90vh;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalSlideUp 0.3s ease-out;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, #4A7CEC 0%, #3a65d0 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left i {
  font-size: 28px;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 본문 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  background-color: #fafafa;
}

.modal-body form {
  background-color: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 폼 그룹 */
.form-group {
  margin-bottom: 0;
}

/* 제목 입력 */
.title-input {
  width: 100%;
  font-size: 28px;
  font-weight: 700;
  border: none;
  outline: none;
  padding: 16px 0;
  color: #1a1a1a;
  background-color: transparent;
}

.title-input::placeholder {
  color: #adb5bd;
  font-weight: 600;
}

.title-input:focus {
  outline: none;
}

/* 구분선 */
.divider {
  height: 1px;
  background: linear-gradient(to right, #e9ecef 0%, #dee2e6 50%, #e9ecef 100%);
  margin: 24px 0;
}

/* 내용 입력 */
.content-textarea {
  width: 100%;
  min-height: 400px;
  font-size: 16px;
  line-height: 1.8;
  border: none;
  outline: none;
  padding: 0;
  resize: none;
  color: #495057;
  background-color: transparent;
  font-family: 'SUIT', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.content-textarea::placeholder {
  color: #adb5bd;
  line-height: 1.8;
}

.content-textarea:focus {
  outline: none;
}

/* 하단 버튼 영역 */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 32px;
  margin-top: 32px;
  border-top: 1px solid #e9ecef;
}

.footer-left {
  display: flex;
  align-items: center;
}

.char-count {
  font-size: 14px;
  color: #868e96;
}

.footer-right {
  display: flex;
  gap: 12px;
}

/* 버튼 스타일 */
.btn-cancel,
.btn-submit {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background-color: #f1f3f5;
  color: #495057;
}

.btn-cancel:hover {
  background-color: #e9ecef;
}

.btn-submit {
  background: linear-gradient(135deg, #4A7CEC 0%, #3a65d0 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.3);
}

.btn-submit:hover {
  box-shadow: 0 6px 16px rgba(74, 124, 236, 0.4);
  transform: translateY(-2px);
}

/* 스크롤바 스타일 */
.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f3f5;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #adb5bd;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #868e96;
}

/* 반응형 */
@media (max-width: 768px) {
  .modal-content {
    max-width: 100%;
    height: 100vh;
    border-radius: 0;
  }

  .modal-body form {
    padding: 24px;
  }

  .title-input {
    font-size: 24px;
  }

  .content-textarea {
    min-height: 300px;
    font-size: 15px;
  }
}
</style>
