import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/api/axiosSetup'; // 👈 [추가] axiosSetup 인스턴스를 사용하기 위해 import

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
        gender: loginData.gender,       // 🟢 [추가] 성별 필드 추가
        region: loginData.region,       // 🟢 [추가] 지역 필드 추가
        childGrade: loginData.childGrade, // 🟢 [추가] 자녀정보 필드 추가
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
     * @param {object} updateData - { name, email, phoneNumber, gender, region, childGrade }
     */
    async function updateUser(updateData) {
      console.log('[authStore] 사용자 정보 업데이트 시도:', updateData);
        
      try {
        // 1. PUT /user/update API 호출 (토큰은 axiosSetup 인터셉터가 자동으로 처리)
        // 서버의 엔드포인트가 /user/update 라고 가정합니다.
        const response = await axios.put('/api/user/update', updateData);

        // 2. 서버 응답 데이터에서 업데이트된 최신 사용자 정보를 가져옵니다.
        //    서버가 업데이트 성공 후 최신 사용자 정보를 반환한다고 가정합니다.
        //    response.data가 전체 사용자 정보 객체라고 가정합니다.
        const updatedUserInfo = response.data;
        
        // 3. Pinia의 user 상태를 갱신합니다. (Persisted 상태이므로 localStorage에도 갱신됨)
        //    기존 user.value에 새로운 필드를 덮어씌웁니다.
        user.value = {
            ...user.value, // 기존 userId, loginId 등은 유지
            ...updatedUserInfo // 서버에서 받은 최신 필드(name, email, phone...)로 갱신
        };

        console.log("사용자 정보 업데이트 성공 및 Pinia 상태 갱신 완료:", user.value);
        
        // 성공적으로 처리되었음을 호출자에게 알립니다.
        return true;
          
      } catch (error) {
        console.error("[authStore] updateUser 액션 실패:", error);
        // AccountView에서 오류를 처리할 수 있도록 에러를 다시 던집니다.
        throw error; 
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