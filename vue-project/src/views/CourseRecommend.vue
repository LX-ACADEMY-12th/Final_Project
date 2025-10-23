<template>
  <div class="course-container">

    <CourseMap :items="courseItems" :title="title" />

    <div class="timeline-list">
      <div v-if="type === 'exhibition'">
        <ExhibitionCourseCard v-for="item in courseItems" :key="item.number" :item="item" courseType="전시" />
      </div>

      <div v-else>
        <CoursePlaceCard v-for="item in courseItems" :key="item.number" :item="item" />
      </div>

    </div>

  </div>
  <!--
  <RecommendationCTA @request-new="fetchNewCourse" />-->
</template>

<script>
//import { ref } from 'vue';
import CourseMap from '@/components/map/CourseMap.vue';
//import RecommendationCTA from '@/components/RecommendationCTA.vue';
import ExhibitionCourseCard from '@/components/card/CourseExhibitionPlaceCard.vue';
import CoursePlaceCard from '@/components/card/CoursePlaceCard.vue';

export default {
  name: 'CourseRecommended',
  components: {
    CourseMap,
    // RecommendationCTA,
    ExhibitionCourseCard,
    CoursePlaceCard,
  },

  props: {
    courseItems: {
      type: Array,
      required: true,
    },
    // 부모가 주는 타입 ('exhibition' | 'place')
    type: {
      type: String,
      default: 'AI 추천 코스'
    }
  },
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
