// src/utils/kakaoMapLoader.js
let kakaoMapPromise = null
let isLoading = false
let isLoaded = false

export function loadKakaoMap() {
  // 이미 로드 완료
  if (isLoaded && window.kakao && window.kakao.maps) {
    return Promise.resolve(window.kakao)
  }

  // 로딩 중이면 기존 Promise 반환
  if (isLoading && kakaoMapPromise) {
    return kakaoMapPromise
  }

  isLoading = true

  kakaoMapPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${import.meta.env.VITE_KAKAO_MAP_KEY}&libraries=services&autoload=false`
    script.async = true
    script.defer = true

    script.onload = () => {
      if (window.kakao && window.kakao.maps) {
        window.kakao.maps.load(() => {
          console.log('✅ 카카오맵 전역 로드 완료')
          isLoaded = true
          isLoading = false
          resolve(window.kakao)
        })
      } else {
        isLoading = false
        reject(new Error('카카오맵 스크립트 로드 실패'))
      }
    }

    script.onerror = () => {
      isLoading = false
      reject(new Error('카카오맵 스크립트 로드 실패'))
    }

    document.head.appendChild(script)
  })

  return kakaoMapPromise
}

// 카카오맵이 로드되었는지 확인하는 헬퍼 함수
export function isKakaoMapLoaded() {
  return isLoaded && window.kakao && window.kakao.maps
}
