<template>
  <div class="course-card" style="font-family: 'SUIT', sans-serif">
    <!-- 코스 경로가 표시된 지도 -->
    <div class="map-container" ref="mapContainer">
      <div v-if="!mapGenerated" class="map-placeholder">
        지도 생성 중...
      </div>
    </div>

    <div class="content-area">
      <div class="content-header">
        <!-- 알약 태그 프레임 -->
        <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
          <PillTag :text="item.subject" type="subject" />
          <PillTag :text="item.grade" type="grade" />
        </div>
        <!-- 찜 아이콘 -->
        <div class="action-icons">
          <i class="bi bi-heart-fill"></i>
        </div>
      </div>
      <!-- 타이틀 -->
      <div class="title">{{ item.ExhibitionName }}</div>
      <!-- 위치 -->
      <div class="address">{{ item.address }}</div>
      <!-- 코스 순서 -->
      <div class="course-list">{{ courseSequenceText }}</div>
    </div>
  </div>
</template>

<script>
import PillTag from '@/components/tag/PillTag.vue';

export default {
  name: 'UserLikeCourseCard',
  components: {
    PillTag
  },
  props: {
    item: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      mapGenerated: false,
      map: null,
      markers: [],
      polyline: null,
      mapWidth: 149,
      mapHeight: 126
    };
  },
  computed: {
    courseSequenceText() {
      // 기존 coursePlaces 배열 사용 (하위 호환성)
      if (Array.isArray(this.item.coursePlaces) && this.item.coursePlaces.length > 0) {
        return this.item.coursePlaces.join(' → ');
      }

      // 새로운 courseItems 배열 사용
      if (Array.isArray(this.item.courseItems) && this.item.courseItems.length > 0) {
        return this.item.courseItems
          .map(courseItem => courseItem.title || courseItem.place)
          .join(' → ');
      }

      return '코스 정보가 없습니다.';
    },

    // 코스 장소들의 좌표 배열
    courseCoordinates() {
      if (!this.item.courseItems || !Array.isArray(this.item.courseItems)) {
        return [];
      }

      return this.item.courseItems
        .filter(item => item.lat && item.lng)
        .map(item => ({
          lat: item.lat,
          lng: item.lng,
          title: item.title || item.place,
          number: item.number || 1
        }));
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initializeMap();
    });
  },
  watch: {
    'item.courseItems': {
      handler() {
        if (this.map) {
          this.updateMapWithCourse();
        }
      },
      deep: true
    }
  },
  beforeUnmount() {
    this.clearMap();
  },
  methods: {
    async initializeMap() {
      if (!window.kakao || !window.kakao.maps) {
        console.error('카카오맵 API가 로드되지 않았습니다.');
        this.showFallbackImage();
        return;
      }

      try {
        await this.createCourseMap();
      } catch (error) {
        console.error('지도 생성 실패:', error);
        this.showFallbackImage();
      }
    },

    async createCourseMap() {
      const container = this.$refs.mapContainer;
      if (!container) return;

      // 코스 좌표가 없으면 기본 지도 표시
      if (this.courseCoordinates.length === 0) {
        this.showFallbackImage();
        return;
      }

      const options = {
        // 초기에 중심점과 레벨을 임의로 설정 (bounds를 사용하여 실제 조정 예정)
        center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
        level: 5, 
        draggable: false,
        scrollwheel: false,
        disableDoubleClick: true,
        disableDoubleClickZoom: true,
        keyboardShortcuts: false
      };

      // 지도 생성
      this.map = new window.kakao.maps.Map(container, options);

      // ⚠️ **추가/수정된 부분:** 지도 경계 설정 및 마커 추가 로직 호출
      this.updateMapBounds(); // 새로 추가된 지도 경계 설정 함수 호출
      this.addCourseMarkersAndRoute();

      // 지도 컨트롤 숨기기
      this.hideMapControls();

      this.mapGenerated = true;
    },

    calculateMapBounds() {
      const coordinates = this.courseCoordinates;

      if (coordinates.length === 0) {
        return { center: { lat: 37.566826, lng: 126.9786567 }, level: 5 };
      }

      // 1. **Bounds 객체 생성:** 카카오맵의 LatLngBounds 객체를 생성합니다.
      // 이 객체는 지도에 표시할 마커들의 영역을 담아주는 역할을 합니다.
      const bounds = new window.kakao.maps.LatLngBounds();

      // 2. **좌표 추가:** 모든 코스 좌표를 bounds 객체에 추가하여 영역을 확장합니다.
      coordinates.forEach(coord => {
        // LatLng 객체를 생성하여 bounds에 넣습니다.
        bounds.extend(new window.kakao.maps.LatLng(coord.lat, coord.lng));
      });

      // 3. **결과 반환:** 지도 설정을 위해 bounds 객체를 반환합니다.
      return {
          bounds: bounds
      };
    },

    // ⚠️ **새로 추가된 메서드:** 계산된 경계를 지도에 적용합니다.
    updateMapBounds() {
        if (!this.map || this.courseCoordinates.length === 0) return;

        // calculateMapBounds()에서 계산된 bounds 객체를 가져옵니다.
        const { bounds } = this.calculateMapBounds();

        if (bounds) {
            // 1. **경계 적용:** `setBounds()` 메서드를 사용하여 지도의 중심과 줌 레벨을
            //    계산된 bounds 영역에 딱 맞게 자동으로 조정합니다.
            this.map.setBounds(bounds);

            // 2. **레벨 조정:** 카드 크기가 작아 너무 타이트하게 보일 수 있으므로,
            //    지도 레벨(줌)을 약간만 조정하여 여백을 줍니다. (선택적)
            //    현재 레벨에서 1을 더하면(숫자가 클수록 줌 아웃) 더 넓은 영역이 보입니다.
            //    this.map.setLevel(this.map.getLevel() + 1);
        }
    },

    addCourseMarkersAndRoute() {
      if (this.courseCoordinates.length === 0) return;

      const positions = [];

      // 기존 마커와 라인 제거
      this.clearMapElements();

      // 마커 추가
      this.courseCoordinates.forEach((coord, index) => {
        const position = new window.kakao.maps.LatLng(coord.lat, coord.lng);
        positions.push(position);

        // 커스텀 마커 이미지 생성
        const markerImageSrc = this.createMarkerImage(
          coord.number || (index + 1),
          this.getMarkerColor(index)
        );

        const markerImage = new window.kakao.maps.MarkerImage(
          markerImageSrc,
          new window.kakao.maps.Size(24, 35),
          { offset: new window.kakao.maps.Point(12, 35) }
        );

        // 마커 생성
        const marker = new window.kakao.maps.Marker({
          position: position,
          image: markerImage,
          map: this.map
        });

        this.markers.push(marker);
      });

      // 폴리라인 추가 (경로 연결선)
      if (positions.length > 1) {
        this.polyline = new window.kakao.maps.Polyline({
          path: positions,
          strokeWeight: 3,
          strokeColor: '#4A7CEC',
          strokeOpacity: 0.8,
          strokeStyle: 'solid'
        });
        this.polyline.setMap(this.map);
      }
    },

    // ⚠️ **수정된 메서드:** 코스 아이템 변경 시 지도 업데이트
    updateMapWithCourse() {
      if (!this.map) return;

      // 기존 마커와 라인 제거
      this.clearMapElements();

      if (this.courseCoordinates.length > 0) {
        // ⚠️ **수정된 부분:** 중심점/레벨 대신 경계(bounds)를 설정하는 함수 호출
        this.updateMapBounds();
        this.addCourseMarkersAndRoute();
      }
    },

    clearMapElements() {
      // 기존 마커 제거
      this.markers.forEach(marker => marker.setMap(null));
      this.markers = [];

      // 기존 폴리라인 제거
      if (this.polyline) {
        this.polyline.setMap(null);
        this.polyline = null;
      }
    },

    clearMap() {
      this.clearMapElements();
      if (this.map) {
        this.map = null;
      }
    },

    getMarkerColor(index) {
      // 코스 순서에 따른 색상 배열
      const colors = ['#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      return colors[index % colors.length];
    },

    createMarkerImage(number, color) {
      // SVG로 코스 순서 마커 이미지 생성
      const svg = `
        <svg width="24" height="35" viewBox="0 0 24 35" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 0C5.373 0 0 5.373 0 12c0 9 12 23 12 23s12-14 12-23c0-6.627-5.373-12-12-12z"
                fill="${color}" stroke="#fff" stroke-width="2"/>
          <circle cx="12" cy="12" r="8" fill="#fff"/>
          <text x="12" y="16" text-anchor="middle" font-family="Arial, sans-serif"
                font-size="10" font-weight="bold" fill="${color}">${number}</text>
        </svg>
      `;

      return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    },

    hideMapControls() {
      // 지도 컨트롤 숨기기
      setTimeout(() => {
        const container = this.$refs.mapContainer;
        if (container) {
          const controls = container.querySelectorAll('.kakao-map-control, .MapTypeControl, .ZoomControl');
          controls.forEach(control => {
            control.style.display = 'none';
          });
        }
      }, 100);
    },

    showFallbackImage() {
      const container = this.$refs.mapContainer;
      if (!container) return;

      container.innerHTML = `
        <div class="map-fallback">
          <img src="https://placehold.co/${this.mapWidth}x${this.mapHeight}/e9ecef/6c757d?text=Course+Map"
               alt="코스 지도" style="width: 100%; height: 100%; object-fit: cover;">
        </div>
      `;
      this.mapGenerated = true;
    }
  }
}
</script>

<style scoped>
.course-card {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  gap: 15px;
  margin: 0 0 0 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.2s ease;
  height: 168px;
}

.course-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.course-list {
  font-size: 0.8rem;
  color: #666;
  line-height: 1.4;
}

/* 지도 컨테이너 */
.map-container {
  width: 149px;
  height: 126px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  flex-shrink: 0;
  position: relative;
  background-color: #f8f9fa;
}

/* 지도 플레이스홀더 */
.map-placeholder {
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

.map-fallback {
  width: 100%;
  height: 100%;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  width: 100%;
}

.action-icons {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #dc3545;
  flex-shrink: 0;
}

.title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.address {
  font-size: 14px;
  color: #777;
  margin-bottom: 4px;
}

/* 카카오맵 컨트롤 숨기기 */
.map-container :deep(.kakao-map-control) {
  display: none !important;
}

.map-container :deep(.MapTypeControl) {
  display: none !important;
}

.map-container :deep(.ZoomControl) {
  display: none !important;
}
</style>
