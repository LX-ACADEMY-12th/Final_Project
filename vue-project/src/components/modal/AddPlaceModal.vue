<template>
  <div v-if="show" class="modal-overlay" style="font-family: 'SUIT', sans-serif" @click.self="close">
    <div class="modal-content">
      <div class="header">
        <span class="title">방문할 장소 추가</span>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="content">
        <div class="current-location">
          <i class="bi bi-geo-alt-fill"></i> 현재 위치: {{ currentLocation }}
        </div>

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
      searchQuery: '',
      places: [],
      loading: false,
      currentLocation: '위치 정보 가져오는 중...', // 초기 메시지
      addedPlaceIds: new Set(), // 추가된 장소 ID 저장
      searchTimeout: null,
      ps: null, // 카카오 Places 서비스
    };
  },
  mounted() {
    this.initializeKakaoServices(); // [수정] 함수 이름 변경
    this.getCurrentLocation();      // [추가] 현재 위치 가져오기 호출
  },
  methods: {
    // 카카오 Places와 Geocoder 서비스 동시 초기화
    initializeKakaoServices() {
      if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
        this.ps = new window.kakao.maps.services.Places();
        this.geocoder = new window.kakao.maps.services.Geocoder(); // Geocoder 초기화
        console.log('카카오 Places 및 Geocoder 서비스 초기화 완료.');
      } else {
        console.error('카카오맵 services API가 로드되지 않았습니다.');
        this.currentLocation = '위치 서비스를 사용할 수 없습니다.'; // 에러 메시지 업데이트
      }
    },
    // 현재 위치 가져오는 함수
    getCurrentLocation() {
      if (!navigator.geolocation) {
        this.currentLocation = '브라우저가 Geolocation을 지원하지 않습니다.';
        console.error('Geolocation not supported by this browser.');
        return;
      }

      console.log('현재 위치 요청 중...');
      this.currentLocation = '현재 위치 확인 중...'; // 상태 업데이트

      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.currentLat = position.coords.latitude;
          this.currentLng = position.coords.longitude;
          console.log(`현재 좌표: ${this.currentLat}, ${this.currentLng}`);
          // 좌표를 주소로 변환
          this.getAddressFromCoords(this.currentLat, this.currentLng);
          // 좌표 얻은 후 기본 장소 로드 또는 검색 기준 변경 (선택)
          // 예: this.loadDefaultPlacesNearCurrentLocation();
          // 또는 searchOptions에서 사용
        },
        (error) => {
          this.currentLocation = '위치 정보를 가져올 수 없습니다.';
          console.error('Geolocation 에러:', error.message);
          // 위치 정보 실패 시 기본값(예: 서울)으로 검색하도록 설정 가능
          this.currentLat = 37.566826; // 서울 시청 위도 (예시)
          this.currentLng = 126.9786567; // 서울 시청 경도 (예시)
          this.currentLocation = '기본 위치(서울) 사용';
        },
        { // Geolocation 옵션
          enableHighAccuracy: false, // 높은 정확도 (배터리 소모 증가)
          timeout: 10000,         // 10초 타임아웃
          maximumAge: 0          // 캐시 사용 안 함
        }
      );
    },

    // 좌표를 주소로 변환하는 함수 (Geocoder 사용)
    getAddressFromCoords(lat, lng) {
      if (!this.geocoder) {
        console.error('Geocoder 서비스가 초기화되지 않았습니다.');
        return;
      }
      const coord = new window.kakao.maps.LatLng(lat, lng);
      this.geocoder.coord2Address(coord.getLng(), coord.getLat(), (result, status) => {
        if (status === window.kakao.maps.services.Status.OK) {
          // 행정동 주소 또는 도로명 주소 사용 (둘 다 있을 수 있음)
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

    // 검색 입력 이벤트
    onSearchInput() {
      // 기존 타이머 클리어
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }

      // 입력이 비어있으면 기본 장소 표시
      if (!this.searchQuery.trim()) {

        return;
      }

      // 500ms 후 검색 실행 (디바운싱)
      this.searchTimeout = setTimeout(() => {
        this.searchPlaces();
      }, 500);
    },
    // 장소 검색
    searchPlaces() {
      // this.currentLat, this.currentLng가 준비되었는지 확인
      if (!this.ps || !this.searchQuery.trim() || this.currentLat === null || this.currentLng === null) {
        // 아직 현재 위치 좌표를 못 가져왔으면 검색 보류 (또는 기본 위치 사용)
        console.warn('검색 전제 조건 미충족 (ps, searchQuery, currentCoords):', this.ps, this.searchQuery, this.currentLat, this.currentLng);
        // 필요하다면 여기서 사용자에게 알림 표시
        // this.error = '현재 위치를 확인 중입니다. 잠시 후 다시 시도하세요.';
        return;
      }

      this.loading = true;

      // 카카오 Places 검색 옵션: location을 현재 위치로 변경
      const searchOptions = {
        location: new window.kakao.maps.LatLng(this.currentLat, this.currentLng), // 현재 위치 좌표 사용
        radius: 10000, // 반경 (미터 단위, 예: 10km)
        // sort: window.kakao.maps.services.SortBy.DISTANCE // 거리순 정렬 (선택 사항)
        sort: window.kakao.maps.services.SortBy.ACCURACY // 또는 정확도순 유지
      };

      this.ps.keywordSearch(
        this.searchQuery,
        this.searchCallback,
        searchOptions
      );
    },
    // 검색 결과 롤백
    searchCallback(data, status, pagination) {
      this.loading = false;
      if (status === window.kakao.maps.services.Status.OK) {
        this.places = data.map((place, index) => ({
          id: place.id || `search-${index}`,
          name: place.place_name,
          address: place.road_address_name || place.address_name, // 도로명 주소 우선
          lat: parseFloat(place.y),
          lng: parseFloat(place.x),
          phone: place.phone || '',
          category: place.category_name || '', // 카테고리 이름 사용
          url: place.place_url || '',
          // 거리 정보 - searchOptions에 sort: DISTANCE 필요
          // distance: place.distance ? `${place.distance}m` : ''
        }));
        console.log('검색 결과:', this.places);
      } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
        this.places = [];
        console.log('검색 결과가 없습니다.');
      } else {
        console.error('검색 중 오류 발생:', status);
        this.places = [];
      }
    },

    // 검색어 초기화
    clearSearch() {
      this.searchQuery = '';
      this.places = []; // 기본 장소 로드 대신 결과 비우기
    },

    // 장소가 이미 추가되었는지 확인
    isPlaceAdded(placeId) {
      return this.addedPlaceIds.has(placeId);
    },

    // 장소 추가
    addItem(place) {
      // 추가된 장소로 표시
      this.addedPlaceIds.add(place.id);

      // 부모 컴포넌트에 전달
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

    // 모달 닫기
    close() {
      // 상태 초기화
      this.searchQuery = '';
      this.places = [];
      this.addedPlaceIds.clear();
      // 닫기 emit
      this.$emit('close');
    },
  },

  // 컴포넌트 제거 시 타이머 정리
  beforeUnmount() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
  }
};
</script>

<style scoped>
/* 기존 스타일 + 추가 스타일 */

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

/* 기존 스타일들... */
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
}

.btn-add:disabled {
  background-color: #f0f0f0;
  color: #aaa;
  cursor: not-allowed;
}
</style>
