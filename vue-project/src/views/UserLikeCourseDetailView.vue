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
      </div>

    <div v-else-if="error" class="status-container">
      </div>

    <template v-else>
      <CourseMap :items="courseItems" :key="mapKey" class="map-area" />

      <div class="course-root-name">
        <span>
          {{ exhibitionName || '코스 이름 없음' }}
        </span>
      </div>
      <div class="scrollable-content">
        <div class="course-list-container">
          <!-- 전시 타입: 드래그 없음 -->
          <div v-if="pageType === 'exhibition'">
            <CourseExhibitionCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
              couseType="전시" @edit="handleEdit" @delete="handleDelete" />
          </div>

          <!-- 답사 타입: 드래그 가능 -->
          <div v-else-if="pageType === 'place'">
            <draggable v-model="courseItems" :animation="200" ghost-class="ghost-item" chosen-class="chosen-item"
              drag-class="drag-item" @start="onDragStart" @end="onDragEnd" @change="onDragChange" item-key="id">
              <template #item="{ element }">
                <CoursePlaceEditCard :item="element" :showControls="true" couseType="답사" @edit="handleEdit"
                  @delete="handleDelete" class="draggable-item" />
              </template>
            </draggable>
          </div>

          <div v-else>
            <p class="text-muted">잘못된 코스 타입입니다.</p>
          </div>
        </div>
      
      </template>
    
    </div>
</template>

<script>
import axios from '@/api/axiosSetup';
import draggable from 'vuedraggable';

import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';
import AddPlaceModal from '@/components/modal/AddPlaceModal.vue';
import CourseMap from '@/components/map/CourseMap.vue';
import CourseExhibitionCard from '@/components/card/CourseExhibitionCard.vue';
import CoursePlaceEditCard from '@/components/card/CoursePlaceEditCard.vue';

export default {
  name: 'UserLikeCourseDetail',
  components: {
    draggable,
    CourseMap,
    ConfirmDeleteModal,
    AddPlaceModal,
    CourseExhibitionCard,
    CoursePlaceEditCard
  },
  setup() {
    const authstore = useAuthStore();
    // ⭐ [수정 1] isLoggedin -> isLoggedIn (L과 I가 대문자)
    const { isLoggedIn, currentUserId } = storeToRefs(authstore);
    return {
      isLoggedIn, // ⭐ [수정 2]
      currentUserId
    }
  },

  data() {
    return {
      course: null,
      error: null,
      loading: true,

      exhibitionName: '',
      pageType: '',
      courseItems: [],
      originalCourseItems: [], // 원본 데이터 보관

      // 모달 상태
      showDeleteModal: false,
      showAddModal: false,
      itemToDeleteId: null,

      // 저장 관련 상태
      hasChanges: false,
      isSaving: false,
      saveMessage: '',
      saveStatus: '', // 'success' or 'error'

      // 드래그 상태
      isDragging: false,

      userId: 1, // 백엔드 호출로 유저 id

      mapKey: 0, // 지도 강제 리렌더링용
    };
  },

  watch: {
    courseItems: {
      handler(newItems, oldItems) {
        // 드래그 중이 아닐 때만 변경 감지
        if (!this.isDragging && oldItems && oldItems.length > 0) {
          this.checkForChanges();
        }
      },
      deep: true
    }
  },

  created() {
    this.loadCourse();
  },

  methods: {
    async loadCourse() {
      this.loading = true;
      this.error = null;
      let targetCourse = null;
      
      // ⭐ [수정 3] isLoggedin -> isLoggedIn (L과 I가 대문자)
      if (!this.isLoggedIn) {
        this.error = '로그인이 필요합니다.';
        this.loading = false; // (loadding -> loading 오타도 수정)
        this.$router.push('/login');
        return;
      }  

      try {
        const courseId = this.$route.params.courseId;

        // 1단계: history.state 확인
        const courseDataFromState = history.state?.courseData;
        if (courseDataFromState && courseDataFromState.id == courseId) {
          console.log('history.state에서 코스 데이터 로드:', courseDataFromState);
          targetCourse = courseDataFromState;
        }

        // 2단계: sessionStorage 확인
        if (!targetCourse) {
          const storedData = sessionStorage.getItem(`courseData_${courseId}`);
          if (storedData) {
            console.log('sessionStorage에서 코스 데이터 로드');
            targetCourse = JSON.parse(storedData);
          }
        }

        // 3단계: API fallback
        if (!targetCourse) {
          console.log('저장된 데이터 없음. API로 fallback 시도');
          const response = await axios.get(`/api/schedules/user/${this.userId}`);

          const allMappedCourses = response.data.map(schedule => {
            const mappedCourseItems = schedule.items
              .map(item => ({
                id: item.sourceItemId,
                number: item.sequence,
                title: item.itemName,
                place: item.addressDetail,
                imageUrl: item.mainImageUrl,
                lat: item.latitude,
                lng: item.longitude,
                type: null, subject: null, grade: null, hashtags: [],
              }))
              .sort((a, b) => a.number - b.number); // sequence 순서로 정렬

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

          sessionStorage.setItem(`courseData_${courseId}`, JSON.stringify(targetCourse));
        }

        // 데이터 설정
        this.course = targetCourse;
        this.exhibitionName = targetCourse.ExhibitionName;
        // 커스텀 아이템에 임시 ID 할당
        this.courseItems = (targetCourse.courseItems || []).map(item => {
          if (item.itemType === 'custom' && !item.id) {
            return {
              ...item,
              id: `custom_${item.title}_${item.place}_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`
            };
          }
          return item;
        });
        this.originalCourseItems = JSON.parse(JSON.stringify(this.courseItems)); // 깊은 복사

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

    // 변경사항 확인 메서드
    checkForChanges() {
      // ( ... 원본과 동일 ... )
      const createItemIdentifier = (item) => {
        if (item.id) return item.id;
        return `custom_${item.title}_${item.place}_${item.lat}_${item.lng}`;
      };
      const currentOrder = this.courseItems.map((item, index) => ({
        identifier: createItemIdentifier(item),
        position: index
      }));
      const originalOrder = this.originalCourseItems.map((item, index) => ({
        identifier: createItemIdentifier(item),
        position: index
      }));
      const orderChanged = JSON.stringify(currentOrder) !== JSON.stringify(originalOrder);
      const countChanged = this.courseItems.length !== this.originalCourseItems.length;
      const currentIdentifiers = new Set(currentOrder.map(item => item.identifier));
      const originalIdentifiers = new Set(originalOrder.map(item => item.identifier));
      const hasNewItems = currentIdentifiers.size !== originalIdentifiers.size ||
        ![...currentIdentifiers].every(id => originalIdentifiers.has(id));
      this.hasChanges = orderChanged || countChanged || hasNewItems;
      if (this.hasChanges) {
        this.reorderCourseItems();
      }
    },

    // 드래그 시작
    onDragStart() {
      this.isDragging = true;
      console.log('드래그 시작');
    },

    // 변경사항 저장
    async saveChanges() {
      if (!this.hasChanges || this.isSaving) return;
      
      // ⭐ [수정됨] 로그인 상태 확인
      if (!this.isLoggedIn) {
        this.error = '로그인이 필요합니다.';
        this.loading = false; // (loadding -> loading 오타도 수정)
        this.$router.push('/login');
        return;
      }  
      this.isSaving = true;
      this.saveMessage = '';

      try {
        const scheduleId = this.course.id;
        const updateData = {
          scheduleId: scheduleId,
          items: this.courseItems.map((item, index) => {
            const isCustom = item.itemType === 'custom';
            return {
              itemId: item.itemId || null, 
              sourceItemId: isCustom ? null : item.id,
              sequence: index + 1,
              itemType: item.itemType,
              customName: isCustom ? (item.title || null) : null,
              customAddress: isCustom ? (item.place || null) : null,
              customLatitude: isCustom ? (item.lat || null) : null,
              customLongitude: isCustom ? (item.lng || null) : null,
            };
          })
        };

        console.log('저장할 데이터:', JSON.stringify(updateData, null, 2));

        // API 호출 - Post 요청
        const response = await axios.post(
          `/api/schedules/items`,
          updateData
        );

        if (response.status === 200) {
          this.saveStatus = 'success';
          this.saveMessage = '변경사항이 성공적으로 저장되었습니다.';
          this.originalCourseItems = JSON.parse(JSON.stringify(this.courseItems));
          this.hasChanges = false;
          const updatedCourse = { ...this.course, courseItems: this.courseItems };
          sessionStorage.setItem(`courseData_${this.course.id}`, JSON.stringify(updatedCourse));
          setTimeout(() => {
            this.saveMessage = '';
          }, 3000);
        }

      } catch (error) {
        console.error('저장 실패:', error);
        this.saveStatus = 'error';
        this.saveMessage = error.response?.data?.message || '저장 중 오류가 발생했습니다.';
        setTimeout(() => {
          this.saveMessage = '';
        }, 5000);
      } finally {
        this.isSaving = false;
      }
    },

    goBack() {
      if (this.hasChanges) {
        if (confirm('저장하지 않은 변경사항이 있습니다. 정말 나가시겠습니까?')) {
          this.$router.back();
        }
      } else {
        this.$router.back();
      }
    },

    handleEdit(id) {
      console.log('수정할 ID:', id);
      // TODO: 수정 로직 구현
    },

    closeDeleteModal() {
      this.itemToDeleteId = null;
      this.showDeleteModal = false;
    },

    openAddModal() {
      this.showAddModal = true;
    },

    closeAddModal() {
      this.showAddModal = false;
    },

    reorderCourseItems() {
      this.courseItems.forEach((item, index) => {
        item.number = index + 1;
      });
    },

    updateMapKey() {
      this.mapKey += 1;
      console.log('지도 키 업데이트:', this.mapKey);
    },

    // 드래그 종료 함수
    onDragEnd() {
      this.isDragging = false;
      console.log('드래그 종료 (상태 변경만 처리)');
    },
    // 드래그 종료시 변경
    onDragChange() {
      console.log('드래그로 인해 v-model 변경됨. 번호 및 지도 갱신 시작.');
      // 1. 변경사항 감지 및 번호 재정렬
      this.checkForChanges();
      // 2. 지도 강제 리렌더링
      this.updateMapKey();
    },

    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);
      this.checkForChanges();
      this.updateMapKey(); // 추가
      this.closeDeleteModal();
    },

    addNewItem(place) {
      console.log('추가할 장소:', JSON.stringify(place, null, 2));

      const newItem = {
        id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, 
        number: this.courseItems.length + 1,
        color: '#3B82F6',
        imageUrl: place.imageUrl || 'https://placehold.co/600x400/AACCFF/000000',
        subject: place.subject || '미지정',
        grade: place.grade || '공통',
        title: place.name,
        type: '사용자 추가 장소',
        place: place.address,
        hashtags: ['새로 추가됨'],
        lat: place.lat,
        lng: place.lng,
        itemType: 'custom',
        isNew: true,
        customName: place.name,
        customAddress: place.address,
        customLatitude: place.lat,
        customLongitude: place.lng,
      };

      this.courseItems.push(newItem);
      this.checkForChanges();
      this.updateMapKey(); // 추가
      this.closeAddModal();
    }
  }
}
</script>

<style scoped>
/* (스타일 태그는 원본과 동일합니다) */
.course-recommend-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}
/* ... */
.course-list-container {
  padding: 24px;
  background-color: #f8f9fa;
}
</style>