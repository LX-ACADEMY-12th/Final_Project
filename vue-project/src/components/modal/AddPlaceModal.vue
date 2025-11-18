<template>
  <div v-if="show" class="modal-overlay" style="font-family: 'SUIT', sans-serif" @click.self="close">
    <div class="modal-content">
      <!-- 헤더 -->
      <div class="header">
        <span class="title">방문할 장소 추가</span>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="content">
        <!-- 현재 위치 표시 -->
        <div class="current-location">
          <i class="bi bi-geo-alt-fill"></i> 현재 위치: {{ currentLocation }}
        </div>

        <!-- 검색창 -->
        <div class="search-bar">
          <i class="bi bi-search"></i>
          <input type="text" placeholder="과학관 이름 또는 지역 검색" v-model="searchQuery" @input="onSearchInput"
            @keyup.enter="searchPlaces" />
          <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <span>검색 중...</span>
        </div>

        <!-- 검색 결과 없음 -->
        <div v-else-if="searchQuery && places.length === 0 && !loading" class="no-results">
          <i class="bi bi-search"></i>
          <span>"{{ searchQuery }}"에 대한 검색 결과가 없습니다.</span>
        </div>

        <!-- 장소 목록 -->
        <ul v-else class="place-list">
          <li v-for="place in places" :key="place.id" class="place-item">
            <div class="place-info">
              <span class="place-name">{{ place.name }}</span>
              <span class="place-address">{{ place.address }}</span>
              <span v-if="place.phone" class="place-phone">{{ place.phone }}</span>
              <span v-if="place.category" class="place-category">{{ place.category }}</span>
            </div>
            <!-- 추가 버튼 (이미 추가된 장소는 비활성화) -->
            <button class="btn-add" :disabled="isPlaceAdded(place.id)" @click="addItem(place)">
              <i v-if="!isPlaceAdded(place.id)" class="bi bi-plus"></i>
              {{ isPlaceAdded(place.id) ? '추가됨' : '추가' }}
            </button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddPlaceModal',
  props: {
    show: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['close', 'add-item'],
  data() {
    return {
      searchQuery: '',           // 검색어
      places: [],                // 검색 결과 목록
      loading: false,            // 로딩 상태
      currentLocation: '위치 정보 가져오는 중...', // 현재 위치 텍스트
      addedPlaceIds: new Set(),  // 이번 세션에 추가된 장소 ID (Set 자료구조)
      searchTimeout: null,       // 디바운싱용 타이머
      ps: null,                  // 카카오 Places 서비스
      geocoder: null,            // 카카오 Geocoder 서비스
      currentLat: null,          // 현재 위도
      currentLng: null,          // 현재 경도
    };
  },
  async mounted() {
    // 컴포넌트 마운트 시 카카오맵 서비스 초기화 및 현재 위치 가져오기
    this.initializeKakaoServices();

    try {
      await this.getCurrentLocation();
    } catch (e) {
      console.warn('초기 위치 획득 실패 (Promise 거부됨):', e);
    }
  },
  methods: {
    /**
     * 카카오 Places 및 Geocoder 서비스 초기화
     */
    initializeKakaoServices() {
      if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
        this.ps = new window.kakao.maps.services.Places();
        this.geocoder = new window.kakao.maps.services.Geocoder();
        console.log('카카오 Places 및 Geocoder 서비스 초기화 완료.');
      } else {
        console.error('카카오맵 services API가 로드되지 않았습니다.');
        this.currentLocation = '위치 서비스를 사용할 수 없습니다.';
      }
    },

    /**
     * 브라우저 Geolocation API로 현재 위치(위도/경도) 가져오기
     */
    getCurrentLocation() {
      return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
          this.currentLocation = '브라우저가 Geolocation을 지원하지 않습니다.';
          console.error('Geolocation not supported by this browser.');
          reject(new Error("Geolocation not supported")); // Promise 거부
          return;
        }

        this.currentLocation = '현재 위치 확인 중...';

        navigator.geolocation.getCurrentPosition(
          (position) => {
            // 성공: 위도/경도 저장 (this에 직접 할당)
            this.currentLat = position.coords.latitude;
            this.currentLng = position.coords.longitude;

            // 주소 변환은 Promise 성공 후 호출자가 처리하도록 수정
            this.getAddressFromCoords(this.currentLat, this.currentLng);

            // [!! Promise 성공 처리 !!]
            resolve({ lat: this.currentLat, lng: this.currentLng });
          },
          (error) => {
            // 실패: 기본 위치(서울)로 설정
            this.currentLocation = '위치 정보를 가져올 수 없습니다.';
            console.error('Geolocation 에러:', error.message);
            this.currentLat = 37.566826;
            this.currentLng = 126.9786567;
            this.currentLocation = '기본 위치(서울) 사용';

            // [!! Promise 성공 처리 (기본 위치 사용을 알림) !!]
            resolve({ lat: this.currentLat, lng: this.currentLng });
          },
          { enableHighAccuracy: true, timeout: 50000, maximumAge: 0 }
        );
      });
    },

    /**
     * 카카오 Geocoder로 좌표 → 주소 변환
     * @param {Number} lat - 위도
     * @param {Number} lng - 경도
     */
    getAddressFromCoords(lat, lng) {
      if (!this.geocoder) {
        console.error('Geocoder 서비스가 초기화되지 않았습니다.');
        return;
      }
      const coord = new window.kakao.maps.LatLng(lat, lng);
      this.geocoder.coord2Address(coord.getLng(), coord.getLat(), (result, status) => {
        if (status === window.kakao.maps.services.Status.OK) {
          // 도로명 주소 우선, 없으면 지번 주소
          const address = result[0]?.road_address?.address_name || result[0]?.address?.address_name;
          if (address) {
            this.currentLocation = address;
            console.log('주소 변환 성공:', address);
          } else {
            this.currentLocation = '주소 정보를 찾을 수 없습니다.';
            console.warn('주소 변환 결과는 있으나, 주소 이름이 없습니다:', result);
          }
        } else {
          this.currentLocation = '주소 변환 실패';
          console.error('Geocoder 실패:', status);
        }
      });
    },

    /**
     * 검색창 입력 이벤트 (디바운싱 적용)
     * - 500ms 대기 후 자동 검색
     */
    onSearchInput() {
      // 기존 타이머 취소
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }

      // 입력이 비어있으면 검색 안 함
      if (!this.searchQuery.trim()) {
        return;
      }

      // 500ms 후 검색 실행 (타이핑 끝날 때까지 대기)
      this.searchTimeout = setTimeout(() => {
        this.searchPlaces();
      }, 500);
    },

    /**
     * 카카오 Places API로 장소 검색
     * - 현재 위치 기준 반경 10km 이내 검색
     */
    searchPlaces() {
      // 전제 조건 체크: Places 서비스, 검색어, 현재 좌표
      if (!this.ps || !this.searchQuery.trim() || this.currentLat === null || this.currentLng === null) {
        console.warn('검색 전제 조건 미충족:', this.ps, this.searchQuery, this.currentLat, this.currentLng);
        return;
      }

      this.loading = true;

      // 검색 옵션: 현재 위치 중심, 반경 10km
      const searchOptions = {
        location: new window.kakao.maps.LatLng(this.currentLat, this.currentLng),
        radius: 10000, // 10km (미터 단위)
        sort: window.kakao.maps.services.SortBy.ACCURACY // 정확도순 정렬
      };

      // 키워드 검색 실행
      this.ps.keywordSearch(
        this.searchQuery,
        this.searchCallback.bind(this),
        searchOptions
      );
    },

    /**
     * 카카오 Places 검색 결과 콜백
     * @param {Array} data - 검색 결과 배열
     * @param {String} status - 검색 상태
     */
    searchCallback(data, status, pagination) {
      this.loading = false;

      if (status === window.kakao.maps.services.Status.OK) {
        // 성공: 결과를 컴포넌트 형식으로 변환
        this.places = data.map((place, index) => ({
          id: place.id || `search-${index}`, // 카카오 장소 ID 또는 임시 ID
          name: place.place_name,
          address: place.road_address_name || place.address_name, // 도로명 우선
          lat: parseFloat(place.y),
          lng: parseFloat(place.x),
          phone: place.phone || '',
          category: place.category_name || '',
          url: place.place_url || '',
        }));
        console.log('검색 결과:', this.places);
      } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
        // 결과 없음
        this.places = [];
        console.log('검색 결과가 없습니다.');
      } else {
        // 오류
        console.error('검색 중 오류 발생:', status);
        this.places = [];
      }
    },

    /**
     * 검색어 및 검색 결과 초기화
     */
    clearSearch() {
      this.searchQuery = '';
      this.places = [];
    },

    /**
     * 장소가 이미 추가되었는지 확인
     * @param {String} placeId - 장소 ID
     * @returns {Boolean} 추가 여부
     */
    isPlaceAdded(placeId) {
      return this.addedPlaceIds.has(placeId);
    },

    /**
     * 장소 추가
     * @param {Object} place - 추가할 장소 객체
     */
    addItem(place) {
      // 이번 세션에 추가된 장소로 표시 (중복 방지)
      this.addedPlaceIds.add(place.id);

      // 부모 컴포넌트(UserLikeCourseDetail)로 장소 정보 전달
      this.$emit('add-item', {
        name: place.name,
        address: place.address,
        lat: place.lat,
        lng: place.lng,
        phone: place.phone || '',
        category: place.category || '',
        imageUrl: 'https://placehold.co/800x600/AACCFF/000000' // 임시 이미지
      });

      console.log('장소 추가:', place);
    },

    /**
     * 모달 닫기 및 상태 초기화
     * ★ 핵심: addedPlaceIds도 초기화하여 다음에 모달 열 때 다시 추가 가능
     */
    close() {
      // 모든 상태 초기화
      this.searchQuery = '';
      this.places = [];
      this.addedPlaceIds.clear(); // ★ Set 초기화 (중요!)

      // 부모 컴포넌트에 닫기 이벤트 전달
      this.$emit('close');
    },
  },

  /**
   * 컴포넌트 언마운트 시 타이머 정리
   */
  beforeUnmount() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
  }
};
</script>

<style scoped>
/* ========================================
   검색창 스타일
======================================== */
.search-bar {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 16px;
  position: relative;
}

.search-bar .bi {
  margin-right: 8px;
  color: #888;
}

.search-bar input {
  border: none;
  background: none;
  outline: none;
  width: 100%;
  font-size: 16px;
}

.clear-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 4px;
  margin-left: 8px;
}

/* ========================================
   로딩 상태
======================================== */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #666;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #5887FF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* ========================================
   검색 결과 없음
======================================== */
.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  color: #666;
  text-align: center;
}

.no-results .bi {
  font-size: 24px;
  margin-bottom: 10px;
  opacity: 0.5;
}

/* ========================================
   장소 정보 표시
======================================== */
.place-phone,
.place-category {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.place-category {
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  margin-top: 4px;
}

/* ========================================
   모달 레이아웃
======================================== */
.modal-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  max-height: 85vh;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 헤더 */
.header {
  padding: 16px;
  text-align: center;
  position: relative;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

/* 콘텐츠 영역 */
.content {
  padding: 16px;
  overflow-y: auto;
}

.current-location {
  font-size: 14px;
  color: #555;
  margin-bottom: 16px;
}

.current-location .bi {
  margin-right: 4px;
}

/* 장소 목록 */
.place-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.place-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.place-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.place-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.place-address {
  font-size: 13px;
  color: #777;
}

/* 추가 버튼 */
.btn-add {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 18px;
  font-weight: bold;
  cursor: pointer;
  background-color: #5887FF;
  color: white;
  flex-shrink: 0;
  margin-left: 12px;
  transition: background-color 0.2s;
}

.btn-add:hover:not(:disabled) {
  background-color: #4770E6;
}

.btn-add:disabled {
  background-color: #f0f0f0;
  color: #aaa;
  cursor: not-allowed;
}
</style>
