import './assets/main.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Bootstrap CSS 및 아이콘 CSS 임포트
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

const app = createApp(App)

// createPinia()를 호출하여 Pinia 인스턴스를 생성합니다.
const pinia = createPinia()

// Pinia 인스턴스에 'pinia-plugin-persistedstate' 플러그인을 등록합니다.
pinia.use(piniaPluginPersistedState)

// 앱에 설정이 완료된 Pinia 인스턴스를 등록합니다.
app.use(pinia)

app.use(router)

app.mount('#app')
