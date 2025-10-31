<template>
  <div v-if="show" class="modal-overlay" @click.self="close" style="font-family: 'SUIT', sans-serif">
    <div class="modal-content">
      <p>리뷰를 삭제하시겠습니까?</p>
      <div class="button-group">
        <button class="btn btn-cancel" @click="close">취소</button>
        <button class="btn btn-confirm" @click="confirm">리뷰 삭제</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConfirmDeleteModal',
  // show prop으로 부모 컴포넌트에서 모달을 제어합니다.
  props: {
    show: {
      type: Boolean,
      default: false,
    },
  },
  // 'confirm'과 'close' 이벤트를 부모에게 전달합니다.
  emits: ['confirm', 'close'],
  methods: {
    confirm() {
      this.$emit('confirm');
    },
    close() {
      this.$emit('close');
    },
  },
};
</script>

<style scoped>
/* 모달 배경 (화면 전체) */
.modal-overlay {
  position: absolute;
  inset: 0;
  /* top, right, bottom, left = 0 */
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 모달 본문 (흰색 박스) */
.modal-content {
  
  background: white;
  padding: 24px;
  border-radius: 16px;
  width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.modal-content p {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 0.2px solid rgba(0, 0, 0, 0.15);
}

.button-group {
  display: flex;
  /*flex-direction: column;*/
  gap: 10px;
}

.btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  /*부드러운 색상 전환 효과*/
  transition: background-color 0.2s, color 0.2s; 
}

/* '리뷰 삭제' 버튼 (이미지 참고) */
.btn-confirm {
  background-color: white;
  /* (조정 필요) */
  color: #cc0000;
  border: 1px solid;
}

/** '예' 버튼 호버 시 (아웃라인) */
.btn-confirm:hover {
  /* (조정 필요) */
  color: white;
  background-color: #cc0000;
}

/* '아니요' 버튼 */
.btn-cancel {
  color: #5887FF;
  background-color: white;
  border: 1px solid #5887FF;
}

/* '아니요' 버튼 호버 시 */
.btn-cancel:hover {
  background-color: #5887FF;
  color: white;
}
</style>
