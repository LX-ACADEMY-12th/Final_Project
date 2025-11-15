<template>
  <div class="course-container">

    <div class="map-section">
      <CourseMap :items="courseItems" :pageType="type" />
    </div>

    <div class="d-flex justify-content-center align-items-center mb-4 mt-3" v-if="type === 'exhibition'">
      <button class="btn virtual-tour-btn" @click="goToVirtualTour">
        <i class="bi bi-compass me-2"></i>
        가상 답사 시작하기
      </button>
    </div>

    <div class="timeline-list">
      <h5 class="list-title">코스 순서</h5>
      <div class="list-wrapper">
        <div v-if="type === 'exhibition'">
          <AiRecommendCourseExhibitionCard v-for="item in courseItems" :key="item.id" :item="item" courseType="전시" />
        </div>
        <div v-else>
          <AiRecommendCoursePlaceCard v-for="item in courseItems" :key="item.id" :item="item" />
        </div>
      </div>
    </div>

  </div>
  <RecommendationCTA @request-new="fetchNewCourse" :secondary-loading="isLoading" :secondary-disabled="isLoading"
    @save-route="saveCurrentRoute" />
</template>

<script>
import router from '@/router';
import CourseMap from '@/components/map/CourseMap.vue';
import RecommendationCTA from '@/components/RecommendationCTA.vue';
import AiRecommendCourseExhibitionCard from '@/components/card/AiRecommendCourseExhibitionCard.vue';
import AiRecommendCoursePlaceCard from '@/components/card/AiRecommendCoursePlaceCard.vue';

export default {
  name: 'CourseRecommended',
  components: {
    CourseMap,
    AiRecommendCourseExhibitionCard,
    AiRecommendCoursePlaceCard,
    RecommendationCTA,
  },
  emits: ['save-recommended-course', 'request-new-course'], // 부모로 전달할 이벤트 정의
  props: {
    courseItems: {
      type: Array,
      required: true,
    },
    // 부모가 주는 타입 ('exhibition' | 'place')
    type: {
      type: String,
      default: 'AI 추천 코스'
    },
    // 부모의 로딩 상태를 받습니다.
    isLoading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    fetchNewCourse() {
      console.log('새로운 코스 요청');

      // 부모 컴포넌트로 이벤트 전달
      this.$emit('request-new-course');
    },
    saveCurrentRoute() {
      console.log('경로 저장 요청 (CourseRecommend)');
      // 아이템 목록을 부모로 전달
      this.$emit('save-recommended-course', this.courseItems);
    },
    goToVirtualTour() {
      const tourStops = this.courseItems.map(item => ({
        id: item.id, // 각 장소의 고유 ID (필요하다면)
        title: item.title, // 탭에 표시될 이름 (예: "창의나래관")
        sceneId: item.sceneId
      }));
      router.push({
        name: 'virtualTour',
        state: {
          items: tourStops
        }
      });
    }
  }
}
</script>

<style scoped>
.course-container {
  width: 100%;
  margin: 0 auto;
  /* 스크롤 가능한 콘텐츠 영역을 만들기 위해 flex-grow: 1을 부모가 관리해야 함 */
}

/* 지도 섹션 */
.map-section {
  /* 지도 컨테이너가 꽉 차도록 설정 */
  width: 100%;
  height: auto;
  margin-bottom: 0;
  border-bottom: 1px solid #eee;
}

/* 가상 답사 버튼 스타일 */
.virtual-tour-btn {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #6366F1;
  /* 테마 색상 유지 */
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  /* 그림자 추가 */
  transition: transform 0.2s;
}

.virtual-tour-btn:hover {
  transform: translateY(-1px);
}

/* 코스 목록 섹션 */
.timeline-list {
  /* 🚨 좌우 패딩만 유지하고 상하 패딩은 내부에서 관리 */
  padding: 0 16px;
  background-color: #ffffff;
  padding-bottom: 80px;
  /* CTA 버튼 공간 확보 */
}

.list-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  padding: 16px 0;
  margin: 0;
}

.list-wrapper {
  /* 카드 목록 자체의 간격을 위해 flex column과 gap 사용 */
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
