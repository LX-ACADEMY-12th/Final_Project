import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

// ===================================================================
// 1. API 베이스 URL 설정
// ===================================================================
const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080'

// ===================================================================
// 2. Axios 인스턴스 생성
// ===================================================================
const axiosInstance = axios.create({
  baseURL: API_BASE,
})

// ===================================================================
// 3. 요청 인터셉터 (Request Interceptor)
// ===================================================================
axiosInstance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    let token = authStore.accessToken

    // 메모리에 없으면 localStorage에서 가져오기
    if (!token) {
      const persistedAuth = localStorage.getItem('auth')
      if (persistedAuth) {
        try {
          const parsed = JSON.parse(persistedAuth)
          token = parsed.accessToken
          console.log('[Axios Interceptor] localStorage에서 accessToken 복원')
        } catch (e) {
          console.error('[Axios Interceptor] localStorage 파싱 실패:', e)
        }
      }
    }

    // 토큰이 있으면 Authorization 헤더 추가
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      console.log('[Axios Interceptor] Authorization 헤더 추가됨')
    } else {
      console.warn('[Axios Interceptor] accessToken이 없습니다.')
    }

    // FormData 처리
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    } else if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// ===================================================================
// 4. 응답 인터셉터 (Response Interceptor)
// ===================================================================
axiosInstance.interceptors.response.use(
  (response) => {
    // 2xx 범위의 성공적인 응답은 그대로 반환
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // ===================================================================
    // 5. 로그인 없이도 접근 가능한 Public API 목록
    // ===================================================================
    const publicAPIs = [
      '/api/content/search', // 콘텐츠 검색 (전시관 + 답사지)
      '/api/reviews', // 리뷰 조회
      '/api/reviews/target', // 특정 대상 리뷰 조회
      '/api/reviews/photos-summary', // 리뷰 사진 썸네일
      '/api/places/search', // 장소 검색
      '/api/halls', // 전시관 정보
      '/api/centers', // 과학관 정보
      '/api/place', // 과학 장소 정보
      '/api/exhibitions',
      '/api/recommend/course', // 추천 코스 조회
      '/api/notices', // 공지사항
    ]

    // 현재 요청 URL이 public API인지 확인
    const isPublicAPI = publicAPIs.some((api) => originalRequest.url?.includes(api))

    // ===================================================================
    // 6. 401 Unauthorized 에러 처리
    // ===================================================================
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Public API는 401이어도 로그인 페이지로 리디렉션하지 않음
      if (isPublicAPI) {
        console.warn('[Axios] Public API 401 에러 - 로그인 리디렉션 생략')
        return Promise.reject(error)
      }

      // 재시도 플래그 설정 (무한 루프 방지)
      originalRequest._retry = true
      const authStore = useAuthStore()

      // 리프레시 토큰이 없는 경우 로그아웃 처리
      if (!authStore.refreshToken) {
        console.error('[Axios] 리프레시 토큰이 없습니다. 로그아웃 처리.')
        authStore.logout()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        // 리프레시 토큰으로 새 액세스 토큰 요청
        console.log('[Axios] 리프레시 토큰으로 액세스 토큰 갱신 시도...')
        const response = await axios.post(`${API_BASE}/api/token/refresh`, {
          refreshToken: authStore.refreshToken,
        })

        // 새로 발급받은 액세스 토큰
        const newAccessToken = response.data.accessToken
        console.log('[Axios] 새 액세스 토큰 발급 성공')

        // Pinia 스토어에 새 액세스 토큰 저장
        authStore.refreshAccessToken(newAccessToken)

        // 원본 요청의 헤더에 새 토큰 삽입
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`

        // 원본 요청 재시도
        console.log('[Axios] 원본 요청 재시도')
        return axiosInstance(originalRequest)
      } catch (refreshError) {
        // 리프레시 토큰 갱신도 실패한 경우 (리프레시 토큰도 만료됨)
        console.error('[Axios] 리프레시 토큰 갱신 실패:', refreshError)
        authStore.logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // ===================================================================
    // 7. 기타 에러 처리
    // ===================================================================
    // 401이 아닌 다른 에러는 그대로 반환
    if (error.response) {
      console.error(`[Axios Error] ${error.response.status}: ${error.response.statusText}`)
    } else if (error.request) {
      console.error('[Axios Error] 서버로부터 응답이 없습니다.')
    } else {
      console.error('[Axios Error]', error.message)
    }

    return Promise.reject(error)
  },
)

// ===================================================================
// 8. 설정된 Axios 인스턴스 내보내기
// ===================================================================
export default axiosInstance
