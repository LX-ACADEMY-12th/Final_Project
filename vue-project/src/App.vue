<template>
  <div id="app-container">
    <RouterView v-slot="{ Component }">
      <KeepAlive include="MapComponent">
        <component :is="Component" />
      </KeepAlive>
    </RouterView>

    <CustomAlert :show="alert.show" :message="alert.message" :type="alert.type" @close="closeAlert" />

    <CustomConfirmAlert :show="confirm.show" :message="confirm.message" :msg="confirm.msg" @confirm="handleConfirm"
      @cancel="handleCancel" />
  </div>
</template>


<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
// ✅ 카카오맵 로더 import
import { loadKakaoMap } from '@/utils/kakaoMapLoader';

const authStore = useAuthStore();

console.log('App.vue onMounted: 앱 시작 (setup)');

onMounted(async () => {
  // 프로필 이미지 갱신
  authStore.refreshProfileUrl();

  // ✅ 카카오맵 사전 로드
  try {
    await loadKakaoMap();
    console.log('✅ 카카오맵 사전 로드 완료');
  } catch (error) {
    console.error('❌ 카카오맵 사전 로드 실패:', error);
  }
});
</script>

<script>
import CustomAlert from './components/alert/CustomAlert.vue';
import CustomConfirmAlert from './components/alert/CustomConfirmAlert.vue';
import eventBus from './utils/eventBus';

export default {
  name: 'App',
  components: {
    CustomAlert,
    CustomConfirmAlert
  },
  data() {
    return {
      alert: {
        show: false,
        message: '',
        type: 'success',
      },
      confirm: {
        show: false,
        message: '',
        msg: '',
        onConfirm: null,
        onCancel: null,
      },
    };
  },
  methods: {
    showAlert(payload) {
      console.log('🔔 [App.vue] 알림 이벤트 수신:', payload);
      this.alert.message = payload.message;
      this.alert.type = payload.type;
      this.alert.show = true;
    },
    closeAlert() {
      this.alert.show = false;
    },
    showConfirm(payload) {
      console.log('🔔 [App.vue] 확인 이벤트 수신:', payload);
      this.confirm.message = payload.message;
      this.confirm.onConfirm = payload.onConfirm || null;
      this.confirm.onCancel = payload.onCancel || null;

      this.confirm.msg = payload.msg || '확인';
      this.confirm.show = true;
    },
    handleConfirm() {
      if (this.confirm.onConfirm) {
        this.confirm.onConfirm();
      }
      this.closeConfirm();
    },
    handleCancel() {
      if (this.confirm.onCancel) {
        this.confirm.onCancel();
      }
      this.closeConfirm();
    },
    closeConfirm() {
      this.confirm.show = false;
      this.confirm.onConfirm = null;
      this.confirm.onCancel = null;
    },
  },
  created() {
    eventBus.on('show-global-alert', this.showAlert);
    eventBus.on('show-global-confirm', this.showConfirm);
  },
  beforeUnmount() {
    eventBus.off('show-global-alert', this.showAlert);
    eventBus.off('show-global-confirm', this.showConfirm);
  },
};

</script>


<style>
/* 1. Global Reset */
html,
body {
  height: 100%;
  width: 100%;
  margin: 0;
  background-color: #E2EBFB;
}

/* 2. Full Viewport App Container */
#app {
  width: 100%;
  height: 100%;
  display: block;
  padding: 0;
  position: relative;
}
</style>

<style scoped>
/* 3. Main Content Wrapper for Mobile View */
#app-container {
  width: 100vw;
  height: 100vh;
  margin: 0;
  position: relative;
  background-color: #f0f0f0;
  border-radius: 0;
  box-shadow: none;

  display: flex;
  flex-direction: column;

  scrollbar-width: none;
  -ms-overflow-style: none;
  overflow: hidden;
}

#app-container::-webkit-scrollbar {
  display: none;
}
</style>
