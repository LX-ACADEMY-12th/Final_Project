<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="header">
        <span class="title">방문할 장소 추가</span>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="content">
        <!-- 검색창 -->
        <div class="search-bar">
          <i class="bi bi-search"></i>
          <input type="text" placeholder="장소 이름 또는 주소 검색" v-model="searchQuery" @input="onSearchInput"
            @keyup.enter="searchPlaces" />
          <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <!-- 빠른 검색 태그 -->
        <div class="quick-search">
          <span class="tag" @click="quickSearch('박물관')">박물관</span>
          <span class="tag" @click="quickSearch('과학관')">과학관</span>
          <span class="tag" @click="quickSearch('체험관')">체험관</span>
          <span class="tag" @click="quickSearch('전시관')">전시관</span>
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
        <ul v-else-if="places.length > 0" class="place-list">
          <li v-for="place in places" :key="place.id" class="place-item">
            <div class="place-info">
              <span class="place-name">{{ place.name }}</span>
              <span class="place-address">{{ place.address }}</span>

              <!-- 거리 정보 + 경고 -->
              <div class="distance-info">
                <span class="distance" :class="getDistanceClass(place.distanceFromCourse)">
                  <i class="bi bi-geo-alt"></i>
                  코스에서 {{ place.distanceFromCourse }}
                </span>
                <span v-if="place.isFarAway" class="warning-badge">
                  <i class="bi bi-exclamation-circle"></i> 경로 이탈
                </span>
              </div>

              <span v-if="place.category" class="place-category">{{ place.category }}</span>
            </div>
            <button class="btn-add" :disabled="isPlaceAdded(place.id)" @click="addItem(place)">
              <i v-if="!isPlaceAdded(place.id)" class="bi bi-plus"></i>
              {{ isPlaceAdded(place.id) ? '추가됨' : '추가' }}
            </button>
          </li>
        </ul>

        <!-- 초기 상태 -->
        <div v-else class="initial-state">
          <i class="bi bi-search"></i>
          <p>추가할 장소를 검색해보세요</p>
          <p class="hint">전국 어디든 검색 가능합니다</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddPlaceModal',
  props: {
    show: { type: Boolean, default: false },
    courseItems: { type: Array, default: () => [] }
  },
  emits: ['close', 'add-item'],
  data() {
    return {
      searchQuery: '',
      places: [],
      loading: false,
      addedPlaceIds: new Set(),
      searchTimeout: null,
      ps: null,
      isInitialized: false,
    };
  },
  watch: {
    show(newVal) {
      if (newVal && !this.isInitialized) {
        this.$nextTick(() => {
          this.initialize();
        });
      }
    }
  },
  methods: {
    initialize() {
      console.log('AddPlaceModal 초기화');
      this.initializeKakaoServices();
      this.isInitialized = true;
    },

    initializeKakaoServices() {
      let attempts = 0;
      const maxAttempts = 30;

      const checkKakao = () => {
        if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
          this.ps = new window.kakao.maps.services.Places();
          console.log('✅ 카카오 Places 서비스 초기화 완료');
          return true;
        }

        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(checkKakao, 100);
        } else {
          console.error('❌ 카카오맵 API 로드 실패');
        }
        return false;
      };

      checkKakao();
    },

    /**
     * 코스 중심점 계산
     */
    getCourseCenter() {
      if (!this.courseItems || this.courseItems.length === 0) {
        return { lat: 37.566826, lng: 126.9786567 }; // 서울 기본값
      }

      const totalLat = this.courseItems.reduce((sum, item) => sum + (item.lat || 0), 0);
      const totalLng = this.courseItems.reduce((sum, item) => sum + (item.lng || 0), 0);

      return {
        lat: totalLat / this.courseItems.length,
        lng: totalLng / this.courseItems.length
      };
    },

    /**
     * 두 좌표 간 거리 계산
     */
    calculateDistance(lat1, lng1, lat2, lng2) {
      const R = 6371e3;
      const φ1 = lat1 * Math.PI / 180;
      const φ2 = lat2 * Math.PI / 180;
      const Δφ = (lat2 - lat1) * Math.PI / 180;
      const Δλ = (lng2 - lng1) * Math.PI / 180;

      const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

      return R * c;
    },

    /**
     * 장소가 코스에서 얼마나 떨어져 있는지 계산
     */
    calculateDistanceFromCourse(placeLat, placeLng) {
      if (!this.courseItems || this.courseItems.length === 0) {
        return { distance: 0, isFar: false };
      }

      // 코스의 모든 장소와의 최소 거리 계산
      let minDistance = Infinity;

      this.courseItems.forEach(item => {
        if (item.lat && item.lng) {
          const distance = this.calculateDistance(
            placeLat, placeLng,
            item.lat, item.lng
          );
          if (distance < minDistance) {
            minDistance = distance;
          }
        }
      });

      // 20km 이상이면 "멀다"고 판단
      const isFar = minDistance > 20000;

      return {
        distance: minDistance,
        isFar: isFar
      };
    },

    formatDistance(meters) {
      if (meters < 1000) {
        return `${Math.round(meters)}m`;
      }
      return `${(meters / 1000).toFixed(1)}km`;
    },

    /**
     * 거리에 따른 CSS 클래스 반환
     */
    getDistanceClass(distanceStr) {
      const distance = parseFloat(distanceStr);

      if (distance < 5) return 'distance-near';      // 5km 이하: 가까움 (녹색)
      if (distance < 15) return 'distance-medium';   // 5-15km: 보통 (파란색)
      return 'distance-far';                         // 15km+: 멀음 (주황색)
    },

    onSearchInput() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }

      if (!this.searchQuery.trim()) {
        this.places = [];
        return;
      }

      this.searchTimeout = setTimeout(() => {
        this.searchPlaces();
      }, 500);
    },

    quickSearch(keyword) {
      this.searchQuery = keyword;
      this.searchPlaces();
    },

    /**
     * 전국 검색 (위치 제한 없음)
     */
    searchPlaces() {
      if (!this.ps || !this.searchQuery.trim()) {
        console.warn('검색 조건 미충족');
        return;
      }

      console.log(`🔍 검색: "${this.searchQuery}"`);
      this.loading = true;

      // ⭐ 위치 제한 없이 전국 검색
      this.ps.keywordSearch(
        this.searchQuery,
        this.searchCallback.bind(this)
      );
    },

    searchCallback(data, status) {
      this.loading = false;

      if (status === window.kakao.maps.services.Status.OK) {
        const courseCenter = this.getCourseCenter();

        this.places = data.map((place, index) => {
          // 코스와의 거리 계산
          const { distance, isFar } = this.calculateDistanceFromCourse(
            parseFloat(place.y),
            parseFloat(place.x)
          );

          return {
            id: place.id || `search-${index}`,
            name: place.place_name,
            address: place.road_address_name || place.address_name,
            lat: parseFloat(place.y),
            lng: parseFloat(place.x),
            phone: place.phone || '',
            category: place.category_name || '',
            distanceFromCourse: this.formatDistance(distance),
            distanceMeters: distance,
            isFarAway: isFar
          };
        }).sort((a, b) => a.distanceMeters - b.distanceMeters); // 거리순 정렬

        console.log(`✅ 검색 결과: ${this.places.length}개`);
      } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
        this.places = [];
        console.log('검색 결과 없음');
      } else {
        console.error('검색 오류:', status);
        this.places = [];
      }
    },

    clearSearch() {
      this.searchQuery = '';
      this.places = [];
    },

    isPlaceAdded(placeId) {
      return this.addedPlaceIds.has(placeId);
    },

    addItem(place) {
      this.addedPlaceIds.add(place.id);

      this.$emit('add-item', {
        name: place.name,
        address: place.address,
        lat: place.lat,
        lng: place.lng,
        phone: place.phone,
        category: place.category,
        imageUrl: 'https://placehold.co/800x600/AACCFF/000000'
      });

      console.log('✅ 장소 추가:', place.name);
    },

    close() {
      this.searchQuery = '';
      this.places = [];
      this.addedPlaceIds.clear();
      this.isInitialized = false;
      this.$emit('close');
    }
  },

  beforeUnmount() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
  }
};
</script>

<style scoped>
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

.search-bar {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 12px;
}

.search-bar input {
  border: none;
  background: none;
  outline: none;
  width: 100%;
  font-size: 16px;
  margin: 0 8px;
}

.clear-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
}

.quick-search {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tag {
  padding: 6px 12px;
  background: #f0f0f0;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag:hover {
  background: #5887FF;
  color: white;
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

.initial-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: #888;
  text-align: center;
}

.initial-state .bi {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.initial-state p {
  margin: 4px 0;
  font-size: 15px;
}

.initial-state .hint {
  font-size: 13px;
  color: #aaa;
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
  gap: 4px;
}

.place-name {
  font-size: 16px;
  font-weight: 500;
}

.place-address {
  font-size: 13px;
  color: #777;
}

.distance-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.distance {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.distance-near {
  color: #22c55e;
}

.distance-medium {
  color: #3b82f6;
}

.distance-far {
  color: #f59e0b;
}

.warning-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 3px;
  font-weight: 500;
}

.place-category {
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-block;
  align-self: flex-start;
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
