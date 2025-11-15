<template>
  <div class="course-card" style="font-family: 'SUIT', sans-serif" @click="handleCardClick">
    <div class="map-container" ref="mapContainer" @click.stop="handleMapClick">
      <div v-if="!mapGenerated" class="map-placeholder">
        지도 생성 중...
      </div>
    </div>

    <div class="content-area" @click="handleContentClick">
      <div class="content-header">
        <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
          <PillTag :text="item.subject" type="subject" />
          <PillTag :text="item.grade?.replace('초등 ', '') ?? ''" type="grade" />
        </div>
        <div class="action-icons" @click.stop="handleHeartClick">
          <i class="bi bi-heart-fill fs-4"></i>
        </div>
      </div>
      <div class="title">{{ item.ExhibitionName }}</div>
      <div class="address">{{ item.scienceCenter }}</div>
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
      mapHeight: 126,
      routePath: []
    };
  },
  computed: {
    // 🚨 [추가] 전시 코스 여부 확인
    isExhibitionCourse() {
      // 백엔드 타입 'inner_course'를 사용하여 전시 코스인지 확인
      return this.item.type === 'inner_course';
    },

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
    // 카드 클릭 핸들러, 부모에게 'click' 이벤트를 전달
    handleCardClick(event) {
      console.log('🔵 UserLikeCourseCard clicked!', event.target);
      console.log('🔵 Item data:', this.item);
      this.$emit('click', this.item); // 아이템 데이터도 함께 전달
    },

    // 지도 클릭 시에도 카드 클릭으로 처리
    handleMapClick(event) {
      console.log('🗺️ Map area clicked, triggering card click');
      event.stopPropagation();
      this.handleCardClick(event);
    },

    // 콘텐츠 영역 클릭
    handleContentClick() {
      // 이벤트 버블링으로 자동으로 handleCardClick 실행됨
      console.log('📝 Content area clicked');
    },

    // 하트 아이콘 클릭 (카드 클릭과 분리)
    handleHeartClick(event) {
      console.log('❤️ Heart icon clicked');
      event.stopPropagation(); // 카드 클릭 이벤트 차단
      // 찜하기 로직 추가
    },

    // 지도 초기화
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

    // 🚨 [수정] 카카오 모빌리티 API 호출 함수 (전시 코스 분기 처리)
    async getRoutePathFromAPI(coordinates) {
      // 🚨 [분기] 전시 코스일 경우 API 호출 건너뛰고 null 반환 (직선 경로 사용 유도)
      if (this.isExhibitionCourse) {
        console.log('[API] 전시 코스이므로 API 경로 계산을 건너뛰고 직선 경로를 사용합니다.');
        return null;
      }

      if (coordinates.length < 2) {
        console.warn('[API] 경로를 계산하기에 아이템이 부족합니다.');
        return null;
      }

      const origin = coordinates[0];
      const destination = coordinates[coordinates.length - 1];
      const waypoints = coordinates.slice(1, -1);

      const payload = {
        origin: { x: origin.lng.toString(), y: origin.lat.toString() },
        destination: { x: destination.lng.toString(), y: destination.lat.toString() },
        waypoints: waypoints.map(item => ({ x: item.lng.toString(), y: item.lat.toString() })),
        priority: "TIME",
      };

      const API_KEY = import.meta.env.VITE_KAKAO_REST_KEY;
      const API_URL = 'https://apis-navi.kakaomobility.com/v1/waypoints/directions';

      try {
        const response = await fetch(API_URL, {
          method: 'POST',
          headers: {
            'Authorization': `KakaoAK ${API_KEY}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload)
        });

        if (!response.ok) {
          throw new Error(`Kakao Directions API 오류: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();

        if (data.routes && data.routes.length > 0) {
          const route = data.routes[0];
          const allPoints = [];

          route.sections.forEach(section => {
            section.roads.forEach(road => {
              road.vertexes.forEach((coord, index) => {
                if (index % 2 === 0) {
                  const x = coord; // 경도 (lng)
                  const y = road.vertexes[index + 1]; // 위도 (lat)
                  allPoints.push(new window.kakao.maps.LatLng(y, x));
                }
              });
            });
          });
          console.log(`[API] 카드 지도 경로 좌표 ${allPoints.length}개 추출 완료.`);
          return allPoints;
        }
        return null;
      } catch (error) {
        console.error('[API] 카드 지도 길찾기 API 호출 중 오류 발생. 직선으로 표시:', error);
        return null;
      }
    },

    // 지도에 코스 정보 띄우기
    async createCourseMap() {
      const container = this.$refs.mapContainer;
      if (!container) return;

      if (this.courseCoordinates.length === 0) {
        this.showFallbackImage();
        return;
      }

      const options = {
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

      // 지도 경계 설정 및 마커 추가 로직 호출
      this.updateMapBounds();
      await this.addCourseMarkersAndRoute();

      // 지도 컨트롤 숨기기
      this.hideMapControls();

      this.mapGenerated = true;
    },

    // 지도 경계 계산
    calculateMapBounds() {
      const coordinates = this.courseCoordinates;

      if (coordinates.length === 0) {
        return { center: { lat: 37.566826, lng: 126.9786567 }, level: 5 };
      }

      const bounds = new window.kakao.maps.LatLngBounds();

      coordinates.forEach(coord => {
        bounds.extend(new window.kakao.maps.LatLng(coord.lat, coord.lng));
      });

      return {
        bounds: bounds
      };
    },

    // 계산된 경계를 지도에 적용합니다.
    updateMapBounds() {
      if (!this.map || this.courseCoordinates.length === 0) return;

      const { bounds } = this.calculateMapBounds();

      if (bounds) {
        this.map.setBounds(bounds);
      }
    },

    // 🚨 [수정] 마커랑 경로 띄우기 (API 호출 분기 포함)
    async addCourseMarkersAndRoute() {
      if (this.courseCoordinates.length === 0) return;

      const markerPositions = [];

      this.clearMapElements();

      // 마커 추가
      this.courseCoordinates.forEach((coord, index) => {
        const position = new window.kakao.maps.LatLng(coord.lat, coord.lng);
        markerPositions.push(position);

        const markerImageSrc = this.createMarkerImage(
          coord.number || (index + 1),
          this.getMarkerColor(index)
        );

        const markerImage = new window.kakao.maps.MarkerImage(
          markerImageSrc,
          new window.kakao.maps.Size(24, 35),
          { offset: new window.kakao.maps.Point(12, 35) }
        );

        const marker = new window.kakao.maps.Marker({
          position: position,
          image: markerImage,
          map: this.map
        });

        this.markers.push(marker);
      });

      // 🚨 [핵심 수정] 경로 결정: 전시 코스 여부에 따라 API 호출 여부 결정
      let finalPath = markerPositions; // 기본값은 직선 경로 (API 실패 또는 전시 코스)

      if (!this.isExhibitionCourse) {
        // 전시 코스가 아닐 경우(답사 코스 등)에만 API 호출
        const apiPath = await this.getRoutePathFromAPI(this.courseCoordinates);
        if (apiPath && apiPath.length > 1) {
          finalPath = apiPath; // API 경로 성공 시 사용
        } else {
          console.log('API 경로 호출 실패 또는 경로 부족. 직선 경로를 사용합니다.');
        }
      } else {
        console.log('전시 코스이므로 직선 경로를 사용합니다.');
      }

      this.routePath = finalPath;

      // 5. 폴리라인 추가 (경로 연결선)
      if (finalPath.length > 1) {
        this.polyline = new window.kakao.maps.Polyline({
          path: finalPath,
          strokeWeight: 3,
          strokeColor: '#4A7CEC',
          strokeOpacity: 0.8,
          strokeStyle: 'solid'
        });
        this.polyline.setMap(this.map);
      }
    },

    // 코스 아이템 변경 시 지도 업데이트
    async updateMapWithCourse() {
      if (!this.map) return;

      this.clearMapElements();

      if (this.courseCoordinates.length > 0) {
        this.updateMapBounds();
        await this.addCourseMarkersAndRoute();
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
  /* 클릭 이벤트 보장 */
  position: relative;
}

.course-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.course-list {
  font-size: 12px;
  color: #666;
  line-height: 1.4;

  /* ▼▼▼ [수정] 텍스트가 2줄을 넘어가면 ... 처리 ▼▼▼ */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  /* 텍스트를 2줄로 제한합니다. (1줄로 바꾸셔도 됩니다) */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  /* ▲▲▲ [수정] 여기까지 추가 ▲▲▲ */
}

/* 지도 컨테이너 클릭 가능하게 */
.map-container {
  width: 149px;
  height: 126px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  flex-shrink: 0;
  position: relative;
  background-color: #f8f9fa;
  cursor: pointer;
  /* 지도 상호작용 비활성화하고 클릭만 허용 */
  pointer-events: auto;
}

/* 하트 아이콘 클릭 가능 */
.action-icons {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #dc3545;
  flex-shrink: 0;
  cursor: pointer;
  padding: 4px;
  /* 클릭 영역 확대 */
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

.title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;

  /* ▼▼▼ [수정] 제목을 2줄로 제한 ▼▼▼ */
  display: -webkit-box;
  -webkit-line-clamp: 1;
  /* 2줄로 제한 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.address {
  font-size: 14px;
  color: #777;
  margin-bottom: 4px;

  /* ▼▼▼ [수정] 주소를 1줄로 제한 ▼▼▼ */
  white-space: nowrap;
  /* 1줄로 제한 */
  overflow: hidden;
  text-overflow: ellipsis;
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
