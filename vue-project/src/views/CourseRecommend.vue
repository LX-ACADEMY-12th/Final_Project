<template>
  <div class="course-container">

    <CourseMap :items="courseItems" :title="courseTitle" />

    <div class="timeline-list">
      <CoursePlaceCard v-for="item in courseItems" :key="item.number" :item="item" />
    </div>

  </div>

  <RecommendationCTA @request-new="fetchNewCourse" />
</template>

<script>
import { ref } from 'vue';
import CourseMap from '../components/CourseMap.vue';
import CoursePlaceCard from '../components/CoursePlaceCard.vue';
import RecommendationCTA from '@/components/RecommendationCTA.vue';

// --- ✨ 2. 추천 코스 데이터 "은행" ---
// (실제로는 API로 받아오거나 별도 파일로 분리할 수 있습니다)

// 추천 코스 A
const courseSetA = [
  {
    number: 1,
    color: '#8B5CF6',
    placeName: '과학탐구관',
    address: '5학년 과학/물질과 운동 단원',
    description: '전시물1, 전시물2, 전시물3',
    imageSrc: 'https://via.placeholder.com/50/8B5CF6/FFFFFF?text=A',
    latlng: [36.375788, 127.376580]
  },
  {
    number: 2,
    color: '#10B981',
    placeName: '첨단기술관',
    address: '3학년 2학기 / 물질의 변화',
    description: '전시물1, 전시물2, 전시물3',
    imageSrc: 'https://via.placeholder.com/50/10B981/FFFFFF?text=B',
    latlng: [36.377311, 127.388255]
  },
  {
    number: 3,
    color: '#EF4444',
    placeName: '자연생태관',
    address: '4학년 1학기 / 생물의 한살이',
    description: '전시물1, 전시물2, 전시물3',
    imageSrc: 'https://via.placeholder.com/50/EF4444/FFFFFF?text=C',
    latlng: [36.368954, 127.388073]
  },
  {
    number: 4,
    color: '#EF4444',
    placeName: '한국과학문명관',
    address: '경기도 과천 000',
    description: '다양한 과학과 관련된 체험을 할 수 있는 장소',
    imageSrc: 'https://via.placeholder.com/50/EF4444/FFFFFF?text=D',
    latlng: [36.365123, 127.381234]
  }
];

// 추천 코스 B (새로운 데이터)
const courseSetB = [
  {
    number: 1,
    color: '#3B82F6',
    placeName: '우주항공관',
    address: '6학년 과학 / 지구와 달',
    description: '우주선, 로켓 전시',
    imageSrc: 'https://via.placeholder.com/50/3B82F6/FFFFFF?text=A',
    latlng: [36.376000, 127.377000]
  },
  {
    number: 2,
    color: '#F59E0B',
    placeName: '물리연구실',
    address: '5학년 과학 / 에너지',
    description: '빛, 소리, 전기 체험',
    imageSrc: 'https://via.placeholder.com/50/F59E0B/FFFFFF?text=B',
    latlng: [36.378000, 127.389000]
  },
  // (아이템 2개만 있는 코스 예시)
];


export default {
  name: 'CourseRecommended',
  components: {
    CourseMap,
    CoursePlaceCard,
    RecommendationCTA
  },
  setup() {
    const currentCourseKey = ref('A');
    // ✨ 3. 현재 보여줄 코스 아이템과 제목을 ref로 관리
    const courseItems = ref(courseSetA);
    const courseTitle = ref('AI 추천 코스 1');

    // ✨ 4. 'refresh' 신호를 받으면 실행될 함수
    const fetchNewCourse = () => {
      // (간단한 예시: A면 B로, B면 A로 토글)
      if (currentCourseKey.value === 'A') {
        courseItems.value = courseSetB;
        courseTitle.value = 'AI 추천 코스 2';
        currentCourseKey.value = 'B';
      } else {
        courseItems.value = courseSetA;
        courseTitle.value = 'AI 추천 코스 1';
        currentCourseKey.value = 'A';
      }
    };

    // ✨ 5. 템플릿에서 사용할 수 있도록 반환
    return {
      courseItems,
      courseTitle,
      fetchNewCourse
    };
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
