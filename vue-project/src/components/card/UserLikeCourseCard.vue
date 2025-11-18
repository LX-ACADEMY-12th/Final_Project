<template>
  <div class="course-card" style="font-family: 'SUIT', sans-serif" @click="handleCardClick">
    <!-- ✅ courseCoordinates가 없으면 지도 영역 자체를 렌더링하지 않음 -->
    <div v-if="courseCoordinates.length > 0" class="map-container" ref="mapContainer" @click.stop="handleMapClick">
      <div v-if="!mapGenerated" class="map-placeholder">
        지도 생성 중...
      </div>
    </div>

    <!-- ✅ courseCoordinates가 없으면 플레이스홀더 이미지 표시 -->
    <div v-else class="map-container">
      <div class="map-fallback">
        <img src="https://placehold.co/149x126/e9ecef/6c757d?text=No+Map" alt="지도 없음"
          style="width: 100%; height: 100%; object-fit: cover;">
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
// ✅ 카카오맵 로더 import
import { isKakaoMapLoaded, loadKakaoMap } from '@/utils/kakaoMapLoader';

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
      routePath: [],
      isUnmounting: false,
      mapInitAttempted: false
    };
  },
  computed: {
    isExhibitionCourse() {
      return this.item.type === 'inner_course';
    },

    courseSequenceText() {
      if (Array.isArray(this.item.coursePlaces) && this.item.coursePlaces.length > 0) {
        return this.item.coursePlaces.join(' → ');
      }

      if (Array.isArray(this.item.courseItems) && this.item.courseItems.length > 0) {
        return this.item.courseItems
          .map(courseItem => courseItem.title || courseItem.place)
          .join(' → ');
      }

      return '코스 정보가 없습니다.';
    },

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
    if (this.courseCoordinates.length > 0) {
      // ✅ 약간의 지연을 주어 여러 카드가 동시에 로드될 때 성능 개선
      setTimeout(() => {
        this.initializeMap();
      }, 100);
    } else {
      console.log('코스 좌표가 없어 지도를 생성하지 않습니다:', this.item.id);
    }
  },
  watch: {
    'item.courseItems': {
      handler(newVal) {
        if (this.isUnmounting) return;

        if (newVal && Array.isArray(newVal) && newVal.length > 0) {
          if (this.map) {
            this.updateMapWithCourse();
          } else if (!this.mapInitAttempted) {
            this.initializeMap();
          }
        }
      },
      deep: true
    }
  },
  beforeUnmount() {
    this.isUnmounting = true;
    this.clearMap();
  },
  methods: {
    handleCardClick(event) {
      console.log('🔵 UserLikeCourseCard clicked!', event.target);
      console.log('🔵 Item data:', this.item);
      this.$emit('click', this.item);
    },

    handleMapClick(event) {
      console.log('🗺️ Map area clicked, triggering card click');
      event.stopPropagation();
      this.handleCardClick(event);
    },

    handleContentClick() {
      console.log('📝 Content area clicked');
    },

    handleHeartClick(event) {
      console.log('❤️ Heart icon clicked');
      event.stopPropagation();
    },

    // ✅ 수정: 카카오맵 로더 사용
    async initializeMap() {
      if (this.mapInitAttempted || this.isUnmounting) return;

      this.mapInitAttempted = true;

      if (this.courseCoordinates.length === 0) {
        console.log('좌표가 없어 지도 초기화를 건너뜁니다.');
        return;
      }

      try {
        // ✅ 카카오맵이 로드되지 않았다면 대기
        if (!isKakaoMapLoaded()) {
          console.log('[UserLikeCourseCard] 카카오맵 대기 중...');
          await loadKakaoMap();
        }

        if (!window.kakao || !window.kakao.maps) {
          console.error('카카오맵 API가 로드되지 않았습니다.');
          return;
        }

        await this.createCourseMap();
      } catch (error) {
        console.error('지도 생성 실패:', error);
      }
    },

    async getRoutePathFromAPI(coordinates) {
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
                  const x = coord;
                  const y = road.vertexes[index + 1];
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

    async createCourseMap() {
      if (this.isUnmounting) return;

      const container = this.$refs.mapContainer;
      if (!container) {
        console.warn('mapContainer ref를 찾을 수 없습니다.');
        return;
      }

      if (this.courseCoordinates.length === 0) {
        console.warn('좌표가 없어 지도를 생성할 수 없습니다.');
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

      this.map = new window.kakao.maps.Map(container, options);

      this.updateMapBounds();
      await this.addCourseMarkersAndRoute();

      this.hideMapControls();

      this.mapGenerated = true;
    },

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

    updateMapBounds() {
      if (!this.map || this.isUnmounting || this.courseCoordinates.length === 0) return;

      const { bounds } = this.calculateMapBounds();

      if (bounds) {
        this.map.setBounds(bounds);
      }
    },

    async addCourseMarkersAndRoute() {
      if (this.isUnmounting || this.courseCoordinates.length === 0) return;

      const markerPositions = [];

      this.clearMapElements();

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

      let finalPath = markerPositions;

      if (!this.isExhibitionCourse) {
        const apiPath = await this.getRoutePathFromAPI(this.courseCoordinates);
        if (apiPath && apiPath.length > 1) {
          finalPath = apiPath;
        } else {
          console.log('API 경로 호출 실패 또는 경로 부족. 직선 경로를 사용합니다.');
        }
      } else {
        console.log('전시 코스이므로 직선 경로를 사용합니다.');
      }

      this.routePath = finalPath;

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

    async updateMapWithCourse() {
      if (!this.map || this.isUnmounting) return;

      this.clearMapElements();

      if (this.courseCoordinates.length > 0) {
        this.updateMapBounds();
        await this.addCourseMarkersAndRoute();
      }
    },

    clearMapElements() {
      try {
        if (this.markers && this.markers.length > 0) {
          this.markers.forEach(marker => {
            if (marker && marker.setMap) {
              marker.setMap(null);
            }
          });
          this.markers = [];
        }

        if (this.polyline && this.polyline.setMap) {
          this.polyline.setMap(null);
          this.polyline = null;
        }
      } catch (error) {
        console.error('지도 요소 정리 중 에러:', error);
      }
    },

    clearMap() {
      try {
        this.clearMapElements();

        if (this.map) {
          this.map = null;
        }
      } catch (error) {
        console.error('지도 정리 중 에러:', error);
      }
    },

    getMarkerColor(index) {
      const colors = ['#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      return colors[index % colors.length];
    },

    createMarkerImage(number, color) {
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
      if (this.isUnmounting) return;

      setTimeout(() => {
        if (this.isUnmounting) return;

        const container = this.$refs.mapContainer;
        if (container) {
          const controls = container.querySelectorAll('.kakao-map-control, .MapTypeControl, .ZoomControl');
          controls.forEach(control => {
            control.style.display = 'none';
          });
        }
      }, 100);
    }
  }
}
</script>

<style scoped>
/* 기존 스타일 동일 */
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
  position: relative;
}

.course-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.course-list {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

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
  pointer-events: auto;
}

.action-icons {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #dc3545;
  flex-shrink: 0;
  cursor: pointer;
  padding: 4px;
}

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
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.address {
  font-size: 14px;
  color: #777;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

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
