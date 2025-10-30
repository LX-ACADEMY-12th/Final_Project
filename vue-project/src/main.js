import './assets/main.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// ⭐️ 1. 우리가 만든 이벤트 버스(방송국)를 가져옵니다.
import eventBus from './utils/eventBus';

// Bootstrap CSS 및 아이콘 CSS 임포트
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

const app = createApp(App)

// ⭐️ 2. '$alert'라는 이름의 전역 함수를 등록합니다.
// 이제 모든 컴포넌트에서 this.$alert(...)를 사용할 수 있습니다.
app.config.globalProperties.$alert = (payload) => {
  let message = '';
  let type = 'success'; // 기본값

  if (typeof payload === 'string') {
    // 1) this.$alert('저장 성공') 처럼 문자열만 보낸 경우
    message = payload;
  } else if (typeof payload === 'object' && payload !== null) {
    // 2) this.$alert({ type: 'error', message: '실패' }) 처럼 객체로 보낸 경우
    message = payload.message || '';
    type = payload.type || 'success';
  }
  
  // ⭐️ 3. App.vue가 들을 수 있도록 방송국에 'show-global-alert' 이벤트를 보냅니다.
  // (이때 알림 메시지와 타입을 함께 보냅니다.)
  eventBus.emit('show-global-alert', { message, type });
};

app.use(createPinia())
// createPinia()를 호출하여 Pinia 인스턴스를 생성합니다.
const pinia = createPinia()

// Pinia 인스턴스에 'pinia-plugin-persistedstate' 플러그인을 등록합니다.
pinia.use(piniaPluginPersistedState)

// 앱에 설정이 완료된 Pinia 인스턴스를 등록합니다.
app.use(pinia)

app.use(router)

app.mount('#app')
