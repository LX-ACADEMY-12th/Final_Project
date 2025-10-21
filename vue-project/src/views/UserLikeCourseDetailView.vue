<template>
  <div class="course-recommend-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 추천 코스 목록
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

    <div class="course-add-btn">
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-plus"></i> 경로추가
      </button>
    </div>

    <div class="scrollable-content">
      <div class="course-list-container">
        <CoursePlaceCard v-for="course in courseItems" :key="course.id" :item="course" @edit="handleEdit"
          @delete="handleDelete" />
      </div>
    </div>

    <ConfirmDeleteModal :show="showDeleteModal" @confirm="confirmDeleteItem" @close="closeDeleteModal" />

    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
// [추가] 모달 컴포넌트들을 import 합니다.
import ConfirmDeleteModal from '@/components/ConfirmDeleteModal.vue';
import AddPlaceModal from '@/components/AddPlaceModal.vue';
import CourseMap from '@/components/CourseMap.vue';
import CoursePlaceCard from '@/components/CoursePlaceCard.vue';


// ( ... mockCourseDatabase는 동일 ... )
const mockCourseDatabase = {
  '전시명1': [
    {
      id: 101, number: 1, color: '#8B5CF6',
      category: '지구',
      placeName: '과학탐구관',
      address: '5학년 과학/물질과 운동 단원',
      description: '전시물1, 전시물2, 전시물3',
      imageSrc: 'https://via.placeholder.com/80/8B5CF6/FFFFFF?text=A',
      latlng: [36.375788, 127.376580]
    },
    {
      id: 102, number: 2, color: '#10B981',
      category: '우주',
      placeName: '첨단기술관',
      address: '6학년 과학/전기와 자기',
      description: '로봇, AI, 반도체',
      imageSrc: 'https://via.placeholder.com/80/10B981/FFFFFF?text=B',
      latlng: [36.377311, 127.388255]
    },
  ],
};


export default {
  name: 'CourseRecommendDetail',
  components: {
    CourseMap,
    CoursePlaceCard,
    ConfirmDeleteModal, // [추가]
    AddPlaceModal      // [추가]
  },

  data() {
    return {
      exhibitionName: null,
      courseItems: [],

      // [추가] 모달 상태 관리를 위한 데이터
      showDeleteModal: false,
      showAddModal: false,
      itemToDeleteId: null, // 삭제할 아이템의 id를 임시 저장
    };
  },

  created() {
    const nameFromUrl = this.$route.params.ExhibitionName;
    this.exhibitionName = nameFromUrl;
    this.fetchCourseData(nameFromUrl);
  },

  methods: {
    fetchCourseData(name) {
      const data = mockCourseDatabase[name];
      if (data) {
        this.courseItems = data;
      } else {
        console.error(`'${name}'에 해당하는 코스 데이터를 찾을 수 없습니다.`);
        this.courseItems = mockCourseDatabase['전시명1'];
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
