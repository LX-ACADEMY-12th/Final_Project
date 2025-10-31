import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// (설치: npm install pinia-plugin-persistedstate)

export const useAuthStore = defineStore(
  // 스토어의 ID
  'auth',
  () => {
    // 1. 상태 (State)
    const user = ref(null) // 사용자 정보 (id, name, email 등)
    const accessToken = ref(null) // API 접근용 (메모리 저장)
    const refreshToken = ref(null) // 토큰 재발급용 (localStorage 저장)

    // 2. 게터 (Getters)

    // 🟢 로그인 여부는 'localStorage'에 저장된 'user' 정보가 있는지로 판단합니다.
    const isLoggedIn = computed(() => !!user.value)

    // 🟢 'user' 객체가 있을 때만 userId를 반환합니다.
    const currentUserId = computed(() => user.value?.userId || null)

    // 3. 액션 (Actions)

    function login(loginData) {
      console.log('[authStore] 로그인 성공:', loginData)

      // LoginResponseDTO에서 user 정보만 분리
      const userData = {
        userId: loginData.userId,
        loginId: loginData.loginId,
        name: loginData.name,
        email: loginData.email,
      }

      user.value = userData // 👈 (Persisted)
      accessToken.value = loginData.accessToken // (Memory only)
      refreshToken.value = loginData.refreshToken // 👈 (Persisted)
    }

    function logout() {
      console.log('[authStore] 로그아웃')
      user.value = null
      accessToken.value = null
      refreshToken.value = null
    }

    function refreshAccessToken(newAccessToken) {
      console.log('[authStore] 액세스 토큰 갱신')
      accessToken.value = newAccessToken
    }

    return {
      user,
      accessToken,
      refreshToken,
      isLoggedIn, // 🟢 이제 'user' 기준으로 계산됨
      currentUserId, // 🟢 이제 'user' 기준으로 계산됨
      login,
      logout,
      refreshAccessToken,
    }
  },
  {
    // 🟢 'user'와 'refreshToken'은 localStorage - 하드, 등등에 저장
    persist: {
      paths: ['user', 'refreshToken'],
    },
  },
)
