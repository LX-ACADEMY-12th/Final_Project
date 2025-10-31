<template>
    <div v-if="show" class="modal-backdrop" @click.self="closeModal">
        <div class="report-modal">

            <header class="modal-header report-header">
                <div class="title-wrap">
                    <span class="icon"><i class="bi bi-bell-fill"></i></span>
                    <h2 class="modal-title">리뷰 신고하기</h2>
                </div>
                <button class="close-btn" @click="closeModal">
                    &times;
                </button>
            </header>

            <hr class="header-divider" />

            <main class="modal-body">
                <p class="report-guide">신고 사유를 입력해주세요.</p>

                <textarea v-model="reason" rows="6" placeholder="예: 욕설/모욕, 스팸/광고성, 부적절한 사진 등"
                    class="review-textarea"></textarea>
            </main>

            <footer class="modal-footer two-button-footer">
                <button @click="closeModal" class="btn-cancel">취소</button>
                <button @click="submitReport" class="btn-submit">제출</button>
            </footer>

        </div>
    </div>
</template>

<script>
export default {
    name: 'ReportModal',
    props: {
        show: { type: Boolean, default: false }
    },
    emits: ['close', 'submit'],
    data() {
        return {
            reason: '' // 입력된 신고 사유
        }
    },
    methods: {
        closeModal() {
            this.reason = ''; // 텍스트 초기화
            this.$emit('close');
        },
        submitReport() {
            this.$emit('submit', this.reason); // 사유와 함께 submit
            // 💡 [수정] 제출 성공/실패는 부모가 하므로 텍스트 초기화 제거
            // (부모가 모달을 닫을 때 텍스트가 초기화됩니다)
        }
    },
    // 💡 [추가] 모달이 닫힐 때(show=false) 텍스트를 초기화하는 watch
    watch: {
        show(newVal) {
            if (!newVal) {
                this.reason = '';
            }
        }
    }
}
</script>

<style scoped>
/* === 모달 스타일 (신고) === */
.modal-backdrop {
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

/* 💡 [수정] 'ReviewModal'의 스타일 적용 */
.report-modal {
    background: white;
    width: 90%;
    max-width: 368px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

/* 💡 [추가] 헤더 스타일 */
.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
}

/* 💡 [추가] 신고 모달은 빨간색 헤더 */
.report-header {
    background: #4A7CEC;
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

/* 💡 [추가] 바디 스타일 */
.modal-body {
    padding: 20px;
}

.report-guide {
    /* 바디 안의 p태그 스타일 */
    font-size: 14px;
    color: #666;
    margin-top: 0;
    margin-bottom: 16px;
}

/* 💡 [수정] 텍스트 에어리어 스타일 (ReviewModal과 동일) */
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
}

/* 💡 [수정] 푸터 스타일 */
.modal-footer {
    padding: 0 20px 20px 20px;
}

/* 💡 [추가] 버튼 2개를 위한 푸터 */
.two-button-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

/* 💡 [수정] 버튼 기본 스타일 */
.modal-footer button {
    padding: 12px 0;
    /* 높이 통일 */
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    flex: 1;
    /* 1:1 비율로 채우기 */
}

.btn-cancel {
    background-color: #f0f0f0;
    color: #333;
}

.btn-submit {
    background-color: #eb3223;
    color: white;
}
</style>