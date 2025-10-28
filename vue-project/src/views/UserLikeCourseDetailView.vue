<template>
  <div class="course-recommend-container" style="font-family: 'SUIT', sans-serif">
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

    <div v-if="loading" class="status-container">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2 text-muted">코스 정보를 불러오는 중입니다...</p>
    </div>

    <div v-else-if="error" class="status-container">
      <p class="text-danger">{{ error }}</p>
      <button @click="goBack" class="btn btn-sm btn-outline-primary">목록으로 돌아가기</button>
    </div>

    <template v-else>
      <CourseMap :items="courseItems" class="map-area" />

      <div class="course-root-name">
        <span>
          {{ exhibitionName || '코스 이름 없음' }}
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
            <p class="text-muted">잘못된 코스 타입입니다.</p>
          </div>

        </div>
      </div>
    </template>

    <ConfirmDeleteModal :show="showDeleteModal" @confirm="confirmDeleteItem" @close="closeDeleteModal" />
    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
import axios from 'axios'; // [신규] axios 임포트
import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';
import AddPlaceModal from '@/components/modal/AddPlaceModal.vue';
import CourseMap from '@/components/map/CourseMap.vue';
import CourseExhibitionCard from '@/components/card/CourseExhibitionCard.vue';
import CoursePlaceEditCard from '@/components/card/CoursePlaceEditCard.vue';

// 데이터 (getUserLikeCourseData) 함수 전체 삭제
export default {
  name: 'UserLikeCourseDetail',
  components: {
    CourseMap,
    ConfirmDeleteModal,
    AddPlaceModal,
    CourseExhibitionCard,
    CoursePlaceEditCard
  },
  data() {
    return {
      course: null, // API 또는 history.state에서 받은 원본 데이터
      error: null,
      loading: true, // [신규] 로딩 상태 추가

      // 템플릿에서 직접 사용하는 데이터
      exhibitionName: '', // 코스 이름 (e.g., '전시명1')
      pageType: '',       // 'exhibition' or 'place'
      courseItems: [],    // 지도와 목록에 표시될 실제 장소 배열

      // 모달 상태
      showDeleteModal: false,
      showAddModal: false,
      itemToDeleteId: null,

      // API 호출을 위한 userId (목록 페이지와 동일하게 설정)
      userId: 1, // ❗️ 실제로는 로그인/세션/Vuex 등에서 가져와야 함
    };
  },

  created() {
    // [수정] created 훅에서는 loadCourse 함수만 호출
    this.loadCourse();
  },

  methods: {
    async loadCourse() {
      this.loading = true;
      this.error = null;
      let targetCourse = null;

      try {
        const courseId = this.$route.params.courseId;

        // 1단계: history.state 확인 (목록에서 정상 진입)
        const courseDataFromState = history.state?.courseData;
        if (courseDataFromState && courseDataFromState.id == courseId) {
          console.log('history.state에서 코스 데이터 로드:', courseDataFromState);
          targetCourse = courseDataFromState;
        }

        // 2단계: sessionStorage 확인 (새로고침 등)
        if (!targetCourse) {
          const storedData = sessionStorage.getItem(`courseData_${courseId}`);
          if (storedData) {
            console.log('sessionStorage에서 코스 데이터 로드');
            targetCourse = JSON.parse(storedData);
            // 사용 후 정리하지 않음 (새로고침 대비)
          }
        }

        // 3단계: API fallback (정말 필요한 경우만)
        if (!targetCourse) {
          console.log('저장된 데이터 없음. API로 fallback 시도');
          const response = await axios.get(`http://localhost:8080/api/schedules/user/${this.userId}`);

          const allMappedCourses = response.data.map(schedule => {
            const mappedCourseItems = schedule.items.map(item => ({
              id: item.sourceItemId,
              number: item.sequence,
              title: item.itemName,
              place: item.addressDetail,
              imageUrl: item.mainImageUrl,
              lat: item.latitude,
              lng: item.longitude,
              type: null, subject: null, grade: null, hashtags: [],
            }));
            return {
              id: schedule.scheduleId,
              ExhibitionName: schedule.scheduleName,
              type: schedule.sourceCourseType,
              imageUrl: mappedCourseItems[0]?.imageUrl || 'https://placehold.co/600x400',
              address: mappedCourseItems[0]?.place || '정보 없음',
              coursePlaces: mappedCourseItems.map(item => item.title),
              courseItems: mappedCourseItems,
              subject: null, grade: null,
            };
          });

          targetCourse = allMappedCourses.find(course => course.id == courseId);

          if (!targetCourse) {
            throw new Error(`ID [${courseId}]에 해당하는 코스를 찾을 수 없습니다.`);
          }

          // API로 가져온 데이터도 sessionStorage에 저장
          sessionStorage.setItem(`courseData_${courseId}`, JSON.stringify(targetCourse));
        }

        // 데이터 설정
        this.course = targetCourse;
        this.exhibitionName = targetCourse.ExhibitionName;
        this.courseItems = targetCourse.courseItems || [];

        if (targetCourse.type === 'inner_course') {
          this.pageType = 'exhibition';
        } else if (targetCourse.type === 'ai_course') {
          this.pageType = 'place';
        } else {
          this.pageType = 'exhibition';
        }

      } catch (err) {
        console.error("코스 상세 정보 로드 실패:", err);
        this.error = err.message || '코스 정보를 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },

    // [삭제] fetchCourseData (목 데이터용 함수) 삭제

    goBack() {
      this.$router.back();
    },

    // --- 수정/삭제 이벤트 핸들러 ---
    handleEdit(id) {
      console.log('수정할 ID:', id);
      // (TODO: 수정 로직 구현)
    },

    handleDelete(id) {
      console.log('삭제 모달 열기, ID:', id);
      this.itemToDeleteId = id;
      this.showDeleteModal = true;
    },

    // --- 삭제 모달용 함수 ---
    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);
      // 삭제 후 순서(number) 재정렬
      this.reorderCourseItems();
      this.closeDeleteModal();
    },

    closeDeleteModal() {
      this.itemToDeleteId = null;
      this.showDeleteModal = false;
    },

    // --- 장소 추가 모달용 함수 ---
    openAddModal() {
      this.showAddModal = true;
    },

    closeAddModal() {
      this.showAddModal = false;
    },

    addNewItem(place) {
      console.log('추가할 장소:', place); // AddPlaceModal에서 전달된 place 객체

      // '답사' (place) 형식에 맞게 새 아이템 생성
      // (place 객체에 name, address, lat, lng가 있다고 가정)
      const newItem = {
        id: new Date().getTime(), // 임시 고유 ID
        number: this.courseItems.length + 1,
        color: '#3B82F6', // 답사 기본 색상
        imageUrl: place.imageUrl || 'https://placehold.co/600x400/AACCFF/000000',
        subject: place.subject || '미지정', // (모달에서 받거나 임시값)
        grade: place.grade || '공통',   // (모달에서 받거나 임시값)
        title: place.name,    // 모달에서 받은 장소 이름
        type: '답사',
        place: place.address, // 모달에서 받은 주소
        hashtags: ['새로 추가됨'],
        lat: place.lat, // 모달에서 받은 위도
        lng: place.lng  // 모달에서 받은 경도
      };

      this.courseItems.push(newItem); // 데이터 배열에 추가
      this.closeAddModal(); // 모달 닫기
    },

    // 아이템 삭제/순서 변경 시 number를 다시 정렬하는 함수
    reorderCourseItems() {
      this.courseItems.forEach((item, index) => {
        item.number = index + 1;
      });
    }
  }
}
</script>

<style scoped>
.course-recommend-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 60px);
  /* ❗️(참고) vh 100%가 아님. 상위 레이아웃에 따라 조절 필요 */
}

/* [신규] 로딩/에러 상태 중앙 정렬 */
.status-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  text-align: center;
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
  margin-bottom: 1rem;
  /* [수정] 스크롤 영역과 간격 추가 */
}

/* [헤더]
    채팅방 헤더와 동일한 구조
*/
.chat-header {
  position: relative;
  flex-shrink: 0;
  /* [수정] 헤더 수축 방지 */
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
