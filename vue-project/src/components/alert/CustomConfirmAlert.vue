<template>
  <div v-if="show" class="confirm-overlay">
    <div class="confirm-box">
      <p class="message" v-html="message.replace(/\n/g, '<br>')"></p>

      <div class="button-group">
        <button class="btn-cancel" @click="$emit('cancel')">취소</button>
        <button class="btn-confirm" @click="$emit('confirm')">{{ msg }}</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomConfirm',
  props: {
    show: { type: Boolean, default: false }, // 보일지 말지
    message: { type: String, default: '메시지를 입력하세요.' }, // 내용
    msg: {
      type: String,
      default: '로그인', // 부모가 값을 안주면 이 텍스트가 나옴
    },
  },
  // 부모에게 'cancel' 또는 'confirm' 이벤트를 보냅니다.
  emits: ['cancel', 'confirm'],
};
</script>

<style scoped>
/* CustomAlert와 거의 동일하지만, z-index를 alert보다 낮게 설정할 수 있습니다. */
.confirm-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9990; /* Alert(9998)보다 낮거나 같게 설정 */
}

.confirm-box {
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  width: 80%;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.message {
  font-size: 16px;
  color: #333;
  margin: 0 0 24px 0; /* 버튼 공간 확보를 위해 margin-bottom 증가 */
  line-height: 1.5;
  word-break: keep-all;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  width: 100%;
  gap: 10px; /* 버튼 사이 간격 */
}

/* 버튼 공통 스타일 */
.button-group button {
  flex: 1; /* 버튼이 공간을 절반씩 차지 */
  border: none;
  padding: 12px 0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

/* 취소 버튼 (회색) */
.btn-cancel {
  background-color: #e0e0e0; /* 회색 계열 */
  color: #333;
}

/* 확인 버튼 (파란색) */
.btn-confirm {
  background-color: #4A7CEC; /* 메인 색상 */
  color: white;
}
</style>