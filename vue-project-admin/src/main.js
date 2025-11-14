// main.js (또는 main.ts)
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

// Bootstrap CSS 및 Icon CSS 로드
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

const app = createApp(App);
app.use(router);
app.mount("#app");