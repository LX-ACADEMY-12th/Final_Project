import './assets/main.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Bootstrap CSS 및 아이콘 CSS 임포트
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
