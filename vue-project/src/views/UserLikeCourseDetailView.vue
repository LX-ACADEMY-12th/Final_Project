<template>
  <div class="course-recommend-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 코스
      </div>
      <div class="header-right" style="flex: 1;">
      </div>
    </div>

    <CourseMap :items="courseItems" class="map-area" />

    <div class="course-root-name">
      <span>
        {{ exhibitionName || '코스 로딩 중...' }}
      </span>
    </div>

    <div class="course-add-btn" v-if="pageType === 'place'">
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-plus"></i> 경로추가
      </button>
    </div>

    <div class="scrollable-content">
      <div class="course-list-container">
        <div v-if="pageType === 'exhibition'">
          <CourseExhibitionCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
            couseType="전시" @edit="handleEdit" @delete="handleDelete" />
        </div>

        <div v-else-if="pageType === 'place'">
          <CoursePlaceEditCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
            couseType="답사" @edit="handleEdit" @delete="handleDelete" />
        </div>

        <div v-else>
          <p>코스 상세 정보를 불러오는 중입니다....</p>
        </div>

      </div>
    </div>

    <ConfirmDeleteModal :show="showDeleteModal" @confirm="confirmDeleteItem" @close="closeDeleteModal" />

    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';
import AddPlaceModal from '@/components/modal/AddPlaceModal.vue';
import CourseMap from '@/components/map/CourseMap.vue';
import CourseExhibitionCard from '@/components/card/CourseExhibitionPlaceCard.vue';
import CoursePlaceEditCard from '@/components/card/CoursePlaceEditCard.vue';

// '전시명1', '전시명2' 등에서 사용할 전시 코스 데이터
const exhibitionCourseData = [
  {
    id: 1,
    number: 1,
    color: '#e53e3e',
    imageUrl: 'https://placehold.co/600x400',
    subject: '지구',
    grade: '3학년',
    title: '습지생물코너',
    type: '상설',
    place: '국립중앙과학관 자연사관',
    hashtags: ['항상성과 몸의 조절', '생명과학과 인간의 생활'],
    lat: 36.3758,
    lng: 127.3845
  },
  {
    id: 2,
    number: 2,
    color: '#e53e3e',
    imageUrl: 'https://placehold.co/600x400',
    subject: '물리',
    grade: '4학년',
    title: '빛의 원리',
    type: '기획',
    place: '국립과천과학관',
    hashtags: ['파동', '빛', '물리1', '체험'],
    lat: 37.4363,
    lng: 126.9746
  },
  {
    id: 3,
    number: 3,
    color: '#e53e3e',
    imageUrl: 'https://placehold.co/600x400',
    subject: '화학',
    grade: '5학년',
    title: '미래 에너지',
    type: '상설',
    place: '서울시립과학관',
    hashtags: ['에너지', '화학 반응', '미래 기술'],
    lat: 37.6094,
    lng: 127.0706
  }
];

// '장소명1', '장소명2' 등에서 사용할 답사 코스 데이터 (샘플)
const placeCourseData = [
  {
    id: 9,
    number: 1,
    color: '#3B82F6',
    imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
    subject: '지구',
    grade: '3학년',
    title: '해운대',
    type: '답사',
    place: '부산시 해운대구',
    hashtags: ['고체지구', '유체지구', '천체'],
    lat: 35.1587,
    lng: 129.1604
  }
];

// ExhibitionName을 Key로 사용하는 "가짜 데이터베이스" 객체
const courseDataBank = {
  '전시명1': exhibitionCourseData,
  '전시명2': exhibitionCourseData, // (임시로 동일 데이터 사용)
  '전시명3': exhibitionCourseData, // (임시로 동일 데이터 사용)
  '장소명1': placeCourseData,
  '장소명2': placeCourseData, // (임시로 동일 데이터 사용)
};

export default {
  name: 'UserLikeCourseDetail',
  components: {
    CourseMap,
    ConfirmDeleteModal, // [추가]
    AddPlaceModal,      // [추가]
    CourseExhibitionCard,
    CoursePlaceEditCard
  },

  data() {
    return {
      exhibitionName: null,
      courseItems: [],
      pageType: null,

      // [추가] 모달 상태 관리를 위한 데이터
      showDeleteModal: false,
      showAddModal: false,
      itemToDeleteId: null, // 삭제할 아이템의 id를 임시 저장
    };
  },

  created() {
    const nameFromUrl = this.$route.params.ExhibitionName;
    // URL 쿼리에서 type (?type=...)을 가져옵니다.
    const typeFromQuery = this.$route.query.type; // '전시' 또는 '답사'
    this.exhibitionName = nameFromUrl;

    // pageType을 쿼리 기준으로 정확히 설정
    if (typeFromQuery === '답사') { // 목록에서 '답사'라고 넘겨줌
      this.pageType = 'place';
    } else { // '전시' 또는 기타
      this.pageType = 'exhibition';
    }

    // 코스의 이름으로 데이터를 불러오는것
    this.fetchCourseData(nameFromUrl);
  },

  methods: {
    // 데이터를 "데이터베이스"에서 이름(key)으로 찾아오는 로직
    fetchCourseData(name) {
      // 객체에서 key로 데이터(배열)을 찾는다.
      const data = courseDataBank[name];
      if (data) {
        this.courseItems = data; // 찾은 배열을 courseItems에 할당
      } else {
        console.error(`'${name}'에 해당하는 코스 데이터를 찾을 수 없습니다.`);
        // 데이터가 없으면 기본 전시 데이터 보여주기
        this.courseItems = courseDataBank['전시명1'];
      }
    },

    goBack() {
      this.$router.go(-1); // this.$router로 접근
    },

    // --- [추가] 수정/삭제 이벤트 핸들러 ---
    handleEdit(id) {
      console.log('수정할 ID:', id);
      // (TODO: 수정 로직 구현)
    },

    handleDelete(id) {
      console.log('삭제 모달 열기, ID:', id);
      this.itemToDeleteId = id;    // 삭제할 ID 저장
      this.showDeleteModal = true; // 삭제 모달 열기
    },

    // --- [추가] 삭제 모달용 함수 ---
    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      // courseItems 배열에서 해당 id를 가진 아이템을 제거
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);
      this.closeDeleteModal(); // 모달 닫기
    },

    closeDeleteModal() {
      this.itemToDeleteId = null;  // ID 초기화
      this.showDeleteModal = false; // 모달 닫기
    },

    // --- [추가] 장소 추가 모달용 함수 ---
    openAddModal() {
      this.showAddModal = true;
    },

    closeAddModal() {
      this.showAddModal = false;
    },

    addNewItem(place) {
      console.log('추가할 장소:', place);

      // (TODO: 🚨 중요 🚨)
      // place 객체를 courseItems에 맞는 형식으로 변환해야 합니다.
      // (예: number, color, latlng, description 등 추가)
      /*
      const newItem = {
        id: new Date().getTime(), // 임시 고유 ID
        number: this.courseItems.length + 1,
        color: '#EF4444', // (임시 색상)
        placeName: place.name,
        address: place.address,
        description: '새로 추가된 장소입니다.', // (임시 설명)
        imageSrc: 'https://via.placeholder.com/80/EF4444/FFFFFF?text=N',
        latlng: [36.365123, 127.381234] // (🚨 임시 좌표 🚨)
      };
      this.courseItems.push(newItem);
      */

      this.closeAddModal(); // 모달 닫기
    }
  }
}
</script>

<style scoped>
/* (스타일은 기존과 동일) */
.course-recommend-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 60px);
}

.map-area {
  height: 200px;
  width: 100%;
  flex-shrink: 0;
}

.course-root-name {
  display: flex;
  font-size: large;
  margin: 16px;
  flex-shrink: 0;
}

.course-add-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

/* [헤더]
   채팅방 헤더와 동일한 구조
*/
.chat-header {
  position: relative;
}

.chat-header .header-left,
.chat-header .header-right {
  flex: 1;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-weight: 600;
}

.btn {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #007bff;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

.btn .bi-plus {
  margin-right: 8px;
}

.scrollable-content {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
}

.course-list-container {
  padding: 24px;
  background-color: #f8f9fa;
}
</style>
