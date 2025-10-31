<template>
  <div id="app-container">
    <RouterView v-slot="{ Component }">
      <!-- 컴포넌트의 name 속성이 MapComponent 인 것만 기억해라 -->
      <KeepAlive include="MapComponent">
        <component :is="Component" />
      </KeepAlive>
    </RouterView>

    <CustomAlert :show="alert.show" :message="alert.message" :type="alert.type" @close="closeAlert" />

    <CustomConfirmAlert :show="confirm.show"
                        :message="confirm.message"
                        @confirm="handleConfirm"
                        @cancel="handleCancel" />
  </div>
</template>


<script setup>
import { RouterView } from 'vue-router'
</script>

<script>
import CustomAlert from './components/alert/CustomAlert.vue';
import CustomConfirmAlert from './components/alert/CustomConfirmAlert.vue';
import eventBus from './utils/eventBus';

export default {
  name: 'App',
  components: {
    CustomAlert, // ⭐️ 4. 컴포넌트 등록
    CustomConfirmAlert
  },
  data() {
    return {
      // ⭐️ 5. 알림의 상태를 관리할 data
      alert: {
        show: false,
        message: '',
        type: 'success', // 'success' or 'error'
      },
      // ⭐️ 3. [추가] 확인(Confirm) 상태
      confirm: {
        show: false,
        message: '',
        onConfirm: null, // '확인' 버튼을 눌렀을 때 실행할 함수
        onCancel: null, // '취소' 버튼을 눌렀을 때 실행할 함수
      },
    };
  },
  methods: {
    // ⭐️ 6. 'show-global-alert' 방송을 받으면 실행될 메서드
    showAlert(payload) {
      console.log('🔔 [App.vue] 알림 이벤트 수신:', payload);
      this.alert.message = payload.message;
      this.alert.type = payload.type;
      this.alert.show = true; // 알림창 띄우기!
    },
    // ⭐️ 7. CustomAlert의 '확인' 버튼을 누르면 실행될 메서드
    closeAlert() {
      this.alert.show = false; // 알림창 닫기
    },

    // ⭐️ 4. [추가] 확인(Confirm)창을 띄우는 메서드
    showConfirm(payload) {
      console.log('🔔 [App.vue] 확인 이벤트 수신:', payload);
      this.confirm.message = payload.message;
      // 콜백 함수가 있으면 저장, 없으면 null
      this.confirm.onConfirm = payload.onConfirm || null;
      this.confirm.onCancel = payload.onCancel || null;
      this.confirm.show = true; // 확인창 띄우기
    },
    // ⭐️ 5. [추가] '확인' 버튼을 눌렀을 때
    handleConfirm() {
      if (this.confirm.onConfirm) {
        this.confirm.onConfirm(); // 저장해둔 '확인' 콜백 함수 실행
      }
      this.closeConfirm(); // 창 닫기
    },

    // ⭐️ 6. [추가] '취소' 버튼을 눌렀을 때
    handleCancel() {
      if (this.confirm.onCancel) {
        this.confirm.onCancel(); // 저장해둔 '취소' 콜백 함수 실행
      }
      this.closeConfirm(); // 창 닫기
    },

    // ⭐️ 7. [추가] 확인창을 닫고 콜백 함수를 초기화
    closeConfirm() {
      this.confirm.show = false;
      this.confirm.onConfirm = null;
      this.confirm.onCancel = null;
    },
  },
  // ⭐️ 8. App.vue가 생성될 때 이벤트 버스(방송국) 청취 시작
  created() {
    eventBus.on('show-global-alert', this.showAlert);
    // ⭐️ 8. [추가] 확인(Confirm) 리스너 등록
    eventBus.on('show-global-confirm', this.showConfirm);
  },
  // ⭐️ 9. App.vue가 사라질 때 청취 중단 (메모리 누수 방지)
  beforeUnmount() {
    eventBus.off('show-global-alert', this.showAlert);
    // ⭐️ 9. [추가] 확인(Confirm) 리스너 해제
    eventBus.off('show-global-confirm', this.showConfirm);
  },
};

</script>


<style>
html,
body {
  height: 100%;
  width: 100%;
  margin: 0;
  /* 원하는 배경색 */
  background-color: #E2EBFB;
}

/* #app을 Flex 컨테이너로 만들고 자식 요소를 중앙 정렬합니다. */
#app {
  width: 100%;
  height: 100%;
  display: flex;
  /* 가로 중앙 정렬 */
  justify-content: center;
  /* 세로 중앙 정렬 */
  align-items: center;
  padding: 0;
  position: relative;
}
</style>

<style scoped>
#app-container {
  width: 412px;
  height: 917px;
  position: relative;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
}
</style>
