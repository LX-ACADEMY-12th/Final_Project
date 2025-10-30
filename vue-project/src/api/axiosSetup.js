import axios from 'axios'
// 1. Pinia 스토어(authStore)를 가져옵니다. (경로가 다를 수 있습니다)
import { useAuthStore } from '@/stores/authStore'

// API 베이스 URL 설정
const API_BASE = import.meta.env?.VITE_API_BASE || 'http://localhost:8080'

// 2. 새로운 Axios 인스턴스 생성
const axiosInstance = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 3. ⭐️ 요청 인터셉터 (Request Interceptor)
axiosInstance.interceptors.request.use(
  (config) => {
    // 4. Pinia 스토어에서 authStore를 가져옵니다.
    const authStore = useAuthStore()

    // 5. 스토어에 accessToken이 있는지 확인합니다.
    if (authStore.accessToken) {
      // 6. 헤더에 'Authorization' 토큰을 추가합니다.
      config.headers['Authorization'] = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// (참고: 401 오류 발생 시 토큰을 재발급하는 '응답 인터셉터'도 여기에 추가할 수 있습니다)

// 7. 설정된 인스턴스를 내보냅니다.
export default axiosInstance
