import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/api/axiosSetup' // 👈 [추가] axiosSetup 인스턴스를 사용하기 위해 import

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
        phoneNumber: loginData.phoneNumber,
        gender: loginData.gender, // 🟢 [추가] 성별 필드 추가
        region: loginData.region, // 🟢 [추가] 지역 필드 추가
        childGrade: loginData.childGrade, // 🟢 [추가] 자녀정보 필드 추가
        profileImageUrl: loginData.profileImageUrl, // 🟢 [추가] 프로필 이미지 URL
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

    // ⭐ [추가] 사용자 정보 업데이트 액션 ⭐
    /**
     * 사용자 정보를 서버에 업데이트하고 성공 시 Pinia 상태를 갱신합니다.
     * @param {object} formData - { name, email, phoneNumber, gender, region, childGrade }
     */
    async function updateUser(formData) {
      console.log('[authStore] 사용자 정보 업데이트 시도:')

      try {
        // 1. 🟢 [수정] axios.put 호출 시 headers 객체를 제거합니다.
        const response = await axios.put(
          '/api/user/update',
          formData, // 👈 axios가 formData를 보고 자동으로 헤더를 설정합니다.
        )

        // 2. 🟢 [중요] 서버가 업데이트된 'UserDTO'를 반환한다고 가정합니다.
        //    (UserController가 "수정 성공" 문자열을 반환하면 여기에서 에러가 발생합니다.)
        const updatedUserInfo = response.data

        if (typeof updatedUserInfo !== 'object' || updatedUserInfo === null) {
          console.error('[authStore] 업데이트 응답이 객체가 아닙니다:', updatedUserInfo)
          throw new Error(
            '서버 응답 형식이 잘못되었습니다. 컨트롤러가 DTO를 반환하는지 확인하세요.',
          )
        }

        // 3. Pinia의 user 상태를 갱신합니다.
        user.value = {
          ...user.value, // 기존 userId, loginId 등은 유지
          ...updatedUserInfo, // 🟢 서버에서 받은 최신 필드(name, email, profileImageUrl)로 갱신
        }

        console.log('사용자 정보 업데이트 성공 및 Pinia 상태 갱신 완료:', user.value)

        // 성공적으로 처리되었음을 호출자에게 알립니다.
        return true
      } catch (error) {
        console.error('[authStore] updateUser 액션 실패:', error)
        // AccountView에서 오류를 처리할 수 있도록 에러를 다시 던집니다.
        throw error
      }
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
      updateUser, // ⭐ [추가] 새로 정의된 액션을 반환합니다.
    }
  },
  {
    // 🟢 'user'와 'refreshToken'은 localStorage - 하드, 등등에 저장
    persist: {
      paths: ['user', 'refreshToken'],
    },
  },
)
