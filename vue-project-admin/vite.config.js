import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  // 🚨 [추가] Proxy 설정
  server: {
    proxy: {
      // '/api'로 시작하는 모든 요청을 'http://localhost:8080'으로 전달
      '/api': {
        target: 'http://localhost:8080', // Spring Boot 서버 주소
        changeOrigin: true, // 호스트 헤더를 백엔드 URL로 변경
        // rewrite: (path) => path.replace(/^\/api/, ''), // /api 경로를 유지하기 위해 주석 처리하거나 제거
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
