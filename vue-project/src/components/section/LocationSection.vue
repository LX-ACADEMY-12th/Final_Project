<template>
  <section class="location-section" v-if="infoItems.length > 0">

    <div class="header-wrap">
      <h3 class="section-title">{{ sectionTitle }}</h3>
      <button class="indoor-map-btn" v-if="!isPlace" @click="goToIndoorMap">전시관 안내도</button>
    </div>

    <div class="map-area">
      <CourseMap :items="mapItems" :is-single-location="true" :default-zoom-level="8" />
    </div>

    <ul class="operation-info">
      <li class="info-item" v-for="item in infoItems" :key="item.label">
        <span class="detail-icon"><i :class="item.icon"></i></span>
        <span class="detail-label">{{ item.label }}</span>
        <span class="detail-text">{{ item.text }}</span>
      </li>
    </ul>
  </section>
</template>

<script>
import CourseMap from '@/components/map/CourseMap.vue';

export default {
  name: 'LocationSection',
  // 부모 컴포넌트로부터 정보 객체를 props로 받습니다.
  props: {
    exhibitionInformation: {
      type: Object,
      required: false,
      default: () => null,
    },
    placeInformation: {
      type: Object,
      required: false,
      default: () => null,
    },
  },
  components: {
    CourseMap
  },

  methods: {
    // 실내지도 버튼 클릭 시 호출 함수
    goToIndoorMap() {
      // exhibitionInformation 또는 placeInformation
      const info = this.information;

      // ExhibitionHall 페이지로 이동시
      // 1. 과학관 이름
      // 2. 내가 선택한 전시관 id
      // 를 쿼리로 전달
      this.$router.push({
        path: '/indoormap',
        query: {
          scienceCenterName: info.scienceCenterName,
          selectedHallId: info.hallId
        }
      });
    },
  },

  // 데이터를 목록 형태로 가공하는 계산된 속성
  computed: {
    // 현재 표시하는 정보가 '장소 정보'인지 판단합니다.
    isPlace() {
      return !!this.placeInformation;
    },

    // 섹션 제목을 동적으로 결정합니다.
    sectionTitle() {
      return this.isPlace ? '체험장소 정보' : '전시관 정보';
    },

    // 템플릿에서 사용할 통합된 정보 객체를 결정합니다.
    information() {
      return this.placeInformation || this.exhibitionInformation || {};
    },

    // CourseMap에 전달할 핀(items) 배열 생성
    mapItems() {
      const info = this.information;

      // 부모로부터 받은 정보에 lat, lng 좌표가 있는지 확인
      if (info && info.lat && info.lng) {

        // CourseMap은 [배열] 형태의 prop을 받으므로 배열로 감싸서 전달
        return [{
          lat: info.lat,
          lng: info.lng,
        }];
      }
      // 좌표가 없거나 info가 비어있으면
      // CourseMap에 빈 배열 전달하여 오류 방지
      return [];
    },

    // 최종적으로 목록에 표시할 배열 데이터를 생성합니다.
    infoItems() {
      const info = this.information;

      if (Object.keys(info).length === 0) {
        return [];
      }

      if (this.isPlace) {
        // 장소 상세화면 (주소, 휴무일, 운영시간, 입장료)
        return [
          { label: '주소', text: info.placeAddress, icon: 'bi bi-geo-alt' },
          { label: '휴무일', text: info.operationPeriod, icon: 'bi bi-calendar-event' },
          { label: '운영시간', text: info.operationHours, icon: 'bi bi-clock' },
          { label: '입장료', text: info.entranceFee, icon: 'bi bi-cash-coin' },
        ];
      } else {
        // 전시 상세화면 (장소, 기간, 운영시간, 입장료)
        return [
          { label: '장소', text: info.exhibitionLocation, icon: 'bi bi-geo-alt' },
          { label: '기간', text: info.operationPeriod, icon: 'bi bi-calendar-event' },
          { label: '운영시간', text: info.operationHours, icon: 'bi bi-clock' },
          { label: '입장료', text: info.entranceFee, icon: 'bi bi-cash-coin' },
        ];
      }
    }
  },
}
</script>

<style scoped>
.header-wrap {
  display: flex;
  /* 자식 요소들을 가로로 나열합니다. */
  justify-content: space-between;
  /* 제목과 버튼을 양 끝으로 배치합니다. */
  align-items: center;
  /* 세로 방향으로 중앙에 정렬합니다. */
  margin-bottom: 16px;
  /* map-area와의 간격을 유지합니다. */
}

/* === 1-4. 장소정보 섹션 스타일 === */
.location-section {
  padding: 15px;
  background-color: white;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.map-area {
  position: relative;
  margin-bottom: 20px;
}

.indoor-map-btn {
  background-color: white;
  color: #4A7CEC;
  border: 1px solid #4A7CEC;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
}

.operation-info {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: bold;
}

.detail-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #666;
  line-height: 1.5;
}

.detail-label {
  font-weight: bold;
  color: #666;
  width: 70px;
  /* 너비 고정 */
}

.detail-text {
  flex: 1;
  color: #333;
}
</style>
