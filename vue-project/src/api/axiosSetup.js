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

// ===================================================================
// ⭐ 8. 응답 인터셉터 (Response Interceptor)
// ===================================================================
axiosInstance.interceptors.response.use(
  (response) => {
    // 2xx 범위의 성공적인 응답은 그대로 반환
    return response
  },
  async (error) => {
    // 원본 요청 정보
    const originalRequest = error.config

    // 8-1. 401 에러(Unauthorized)이고, 아직 재시도 안 한 경우
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true // 재시도 플래그 (무한 루프 방지)

      const authStore = useAuthStore()

      // 8-2. 리프레시 토큰이 있는지 확인
      if (!authStore.refreshToken) {
        console.error('[Axios] 리프레시 토큰이 없습니다. 로그아웃 처리.')
        authStore.logout()
        // (라우터 사용 시) router.push('/login');
        window.location.href = '/login' // 로그인 페이지로 강제 이동
        return Promise.reject(error)
      }

      try {
        // 8-3. 리프레시 토큰으로 새 액세스 토큰 요청 (백엔드 API 필요)
        // (이 요청은 'axios'의 기본 인스턴스를 사용해야 인터셉터 무한 루프에 빠지지 않습니다)
        const response = await axios.post(`${API_BASE}/api/token/refresh`, {
          refreshToken: authStore.refreshToken,
        })

        // 8-4. 새로 발급받은 액세스 토큰
        const newAccessToken = response.data.accessToken

        // 8-5. Pinia 스토어에 새 액세스 토큰 저장 (메모리에만)
        authStore.refreshAccessToken(newAccessToken)

        // 8-6. 원본 요청(실패했던)의 헤더에 새 토큰 삽입
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`

        // 8-7. 원본 요청 재시도 (axiosInstance 사용)
        console.log('[Axios] 토큰 갱신 성공, 원본 요청 재시도')
        return axiosInstance(originalRequest)
      } catch (refreshError) {
        // 8-8. 리프레시 토큰 갱신도 실패한 경우 (예: 리프레시 토큰도 만료됨)
        console.error('[Axios] 리프레시 토큰 갱신 실패', refreshError)
        authStore.logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // 401이 아닌 다른 에러는 그대로 반환
    return Promise.reject(error)
  },
)

// 7. 설정된 인스턴스를 내보냅니다.
export default axiosInstance
