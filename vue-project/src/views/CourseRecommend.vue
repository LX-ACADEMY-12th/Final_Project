<template>
  <div class="course-container">

    <CourseMap :items="courseItems" />
    <div class="d-flex justify-content-center align-items-center mt-2 mb-2">
      <button class="btn" style="background-color: #6366F1; color: white;" @click="goToVirtualTour">가상 답사 시작하기</button>
    </div>

    <div class="timeline-list">
      <div v-if="type === 'exhibition'">
        <AiRecommendCourseExhibitionCard v-for="item in courseItems" :key="item.id" :item="item" courseType="전시" />
      </div>
      <div v-else>
        <AiRecommendCoursePlaceCard v-for="item in courseItems" :key="item.id" :item="item" />
      </div>
    </div>
  </div>
  <RecommendationCTA @request-new="fetchNewCourse" :secondary-loading="isLoading" :secondary-disabled="isLoading"
    @save-route="saveCurrentRoute" />
</template>

<script>
//import { ref } from 'vue';
import router from '@/router';
import CourseMap from '@/components/map/CourseMap.vue';
import RecommendationCTA from '@/components/RecommendationCTA.vue';
import AiRecommendCourseExhibitionCard from '@/components/card/AiRecommendCourseExhibitionCard.vue';
import AiRecommendCoursePlaceCard from '@/components/card/AiRecommendCoursePlaceCard.vue';

export default {
  name: 'CourseRecommended',
  components: {
    CourseMap,
    // RecommendationCTA,
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
      // 1. "가상 답사" 페이지에 필요한 정보만
      //    (예: 이름, ID, 대표 이미지 등) 추출해서 넘기는 것이 좋습니다.
      const tourStops = this.courseItems.map(item => ({
        id: item.id, // 각 장소의 고유 ID (필요하다면)
        title: item.title, // 탭에 표시될 이름 (예: "창의나래관")
        sceneId: item.sceneId // :전구: 1단계에서 추가한 sceneId
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
  max-width: 600px;
  margin: 0 auto;
}

.timeline-list {
  padding: 16px;
  background-color: #ffffff;
  padding-bottom: 12px;
  padding-left: 12px;
}
</style>
