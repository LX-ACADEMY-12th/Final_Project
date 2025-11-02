<template>
  <div v-if="show" class="alert-overlay">
    <div class="alert-box">
      <i class="icon bi" :class="iconClass"></i>

      <p class="message" v-html="message.replace(/\n/g, '<br>')"></p>

      <button class="confirm-btn" @click="$emit('close')">확인</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomAlert',
  // App.vue로부터 받을 데이터들 (props)
  props: {
    show: { type: Boolean, default: false }, // 보일지 말지
    message: { type: String, default: '' }, // 알림 메시지
    type: { type: String, default: 'success' }, // 'success' 또는 'error'
  },
  // 부모(App.vue)에게 'close' 이벤트를 보낸다고 명시
  emits: ['close'],
  computed: {
    // 타입에 따라 아이콘 클래스를 결정
    iconClass() {
      return this.type === 'error'
        ? 'bi-exclamation-triangle-fill error-icon'
        : 'bi-check-circle-fill success-icon';
    },
  },
};
</script>

<style scoped>
/* 화면 전체를 덮는 반투명 검은색 배경 
  position: absolute; 로 설정해서 App.vue(#app) 기준으로 꽉 찹니다.
*/
.alert-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 반투명 검은색 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9998; /* 화면 내에서 가장 위에 보이도록 */
}

/* 실제 알림창 흰색 박스 */
.alert-box {
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  width: 80%; /* 화면 너비의 80% */
  max-width: 300px; /* 너무 커지는 것 방지 */
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

/* 아이콘 스타일 */
.icon {
  font-size: 40px;
  margin-bottom: 16px;
}
.success-icon {
  color: #28a745; /* 성공 (초록) */
}
.error-icon {
  color: #dc3545; /* 에러 (빨강) */
}

/* 메시지 텍스트 */
.message {
  font-size: 16px;
  color: #333;
  margin: 0 0 20px 0;
  line-height: 1.5;
  word-break: keep-all; /* 단어 단위로 줄바꿈 */
}

/* 확인 버튼 */
.confirm-btn {
  width: 100%;
  background-color: #4A7CEC; /* (이전 코드의 버튼 색상) */
  color: white;
  border: none;
  padding: 12px 0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}
</style>