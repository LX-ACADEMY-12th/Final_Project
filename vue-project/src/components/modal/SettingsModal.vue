<template>
  <div v-if="show" class="modal-overlay" style="font-family: 'SUIT', sans-serif" @click.self="close">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0">
        <div class="modal-body text-center p-4">
          <h5 class="fw-bold text-dark mb-4" id="settingsModalLabel">설정</h5>
          
          <button type="button" class="btn btn-primary w-100 p-3 fw-bold mb-3 rounded-pill modal-logout-btn"
            @click="onMainButtonClick">
            {{ isLoggedIn ? '로그아웃' : '로그인' }}
          </button>
          
          <button type="button" class="btn btn-outline-primary w-100 p-3 fw-bold rounded-pill modal-withdraw-btn"
            @click="onWithdrawClick">
            회원탈퇴
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SettingsModal',

  // [!!] 1. 부모로부터 'show' 값을 받을 수 있도록 props를 선언합니다.
  props: {
    show: {
      type: Boolean,
      default: false
    },
    // ⭐ 로그인 상태를 받기 위한 prop ⭐
    isLoggedIn: {
      type: Boolean,
      default: false
    }
  },

  // [!!] 2. 부모가 @close 이벤트를 들을 수 있도록 emits에 'close'를 추가합니다.
  emits: ['logout', 'withdraw', 'close', 'login'],

  methods: {
    // ⭐ 메인 버튼 클릭 시 동작 ⭐
    onMainButtonClick() {
      if (this.isLoggedIn) {
        // 로그인 상태: 로그아웃 이벤트 발생
        this.$emit('logout');
      } else {
        // 로그아웃 상태: 로그인 이벤트 발생 (MyPageView에서 로그인 페이지로 이동 처리)
        this.$emit('login');
      }
      this.$emit('close'); // 버튼 클릭 후 모달 닫기
    },

    onWithdrawClick() {
      this.$emit('withdraw');
      this.$emit('close'); // [!!] 3. 탈퇴 클릭 시 모달을 닫도록 close 이벤트 발생
    },

    close() {
      this.$emit('close');
    },
  }
}
</script>

<style scoped>
/* [수정]
  모달 오버레이 스타일: align-items를 'center'로 변경
*/
.modal-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  /* 👈 flex-end에서 center로 수정 */
  align-items: center;
  /* 모바일 화면 여백 */
  padding: 20px;
}

.modal-dialog {
  width: 90%;
  background-color: #ffffff;
  margin-left: auto;
  margin-right: auto;
  border-radius: 16px;
}

.modal-logout-btn {
  background-color: #4A7CEC;
  border-color: #4A7CEC;
  font-weight: 600;
  font-size: 1rem;
}

.modal-logout-btn:hover {
  background-color: #4A7CEC;
  border-color: #4A7CEC;
}

.modal-logout-btn:active {
  filter: brightness(90%);
}

.modal-withdraw-btn {
  color: #4A7CEC;
  border-color: #4A7CEC;
  border-width: 1px;
  font-weight: 600;
  font-size: 1rem;
}

.modal-withdraw-btn:active {
  color: #ffffff;
  background-color: #4A7CEC;
  border-color: #4A7CEC;
}

.modal-body h5 {
  font-weight: 700;
  font-size: 1.2rem;
}

.btn:hover {
  filter: none;
  opacity: 1;
}
</style>
