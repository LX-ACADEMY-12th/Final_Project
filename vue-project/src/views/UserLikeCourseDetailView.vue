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
      <CourseMap :items="courseItems" :key="mapKey" class="map-area" />

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
          <!-- 전시 타입: 드래그 없음 -->
          <div v-if="pageType === 'exhibition'">
            <CourseExhibitionCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
              couseType="전시" @edit="handleEdit" @delete="handleDelete" />
          </div>

          <!-- 답사 타입: 드래그 가능 -->
          <div v-else-if="pageType === 'place'">
            <draggable v-model="courseItems" :animation="200" ghost-class="ghost-item" chosen-class="chosen-item"
              drag-class="drag-item" @start="onDragStart" @end="onDragEnd" item-key="id">
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
      </div>

      <!-- 저장 섹션 -->
      <div class="save-section" v-if="hasChanges">
        <div v-if="saveMessage" class="save-status-message"
          :class="`alert-${saveStatus === 'success' ? 'success' : 'danger'}`">
          {{ saveMessage }}
        </div>
        <button class="btn btn-primary save-btn-bottom" @click="saveChanges" :disabled="!hasChanges || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isSaving ? '저장 중...' : '변경사항 저장' }}
        </button>
      </div>
    </template>

    <ConfirmDeleteModal :show="showDeleteModal" @confirm="confirmDeleteItem" @close="closeDeleteModal" />
    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
import axios from 'axios';
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
          const response = await axios.get(`http://localhost:8080/api/schedules/user/${this.userId}`);

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
      // 각 아이템의 고유 식별자 생성 (id 또는 커스텀 아이템의 경우 대체 식별자)
      const createItemIdentifier = (item) => {
        if (item.id) return item.id;
        // 커스텀 아이템의 경우 여러 속성을 조합한 고유 식별자 생성
        return `custom_${item.title}_${item.place}_${item.lat}_${item.lng}`;
      };

      // 현재 순서와 원본 순서 비교
      const currentOrder = this.courseItems.map((item, index) => ({
        identifier: createItemIdentifier(item),
        position: index
      }));

      const originalOrder = this.originalCourseItems.map((item, index) => ({
        identifier: createItemIdentifier(item),
        position: index
      }));

      // 순서 변경 확인
      const orderChanged = JSON.stringify(currentOrder) !== JSON.stringify(originalOrder);

      // 아이템 개수 변경 확인
      const countChanged = this.courseItems.length !== this.originalCourseItems.length;

      // 새로운 아이템 추가 확인
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

      this.isSaving = true;
      this.saveMessage = '';

      try {
        const scheduleId = this.course.id;

        // 백엔드로 전송할 데이터 구성
        const updateData = {
          scheduleId: scheduleId,
          items: this.courseItems.map((item, index) => {

            const isCustom = item.itemType === 'custom';

            return {
              itemId: item.itemId || null, // 기존 아이템의 경우 itemId 포함
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
          `http://localhost:8080/api/schedules/items`,
          updateData
        );

        if (response.status === 200) {
          this.saveStatus = 'success';
          this.saveMessage = '변경사항이 성공적으로 저장되었습니다.';

          // 원본 데이터 업데이트
          this.originalCourseItems = JSON.parse(JSON.stringify(this.courseItems));
          this.hasChanges = false;

          // sessionStorage 업데이트
          const updatedCourse = { ...this.course, courseItems: this.courseItems };
          sessionStorage.setItem(`courseData_${this.course.id}`, JSON.stringify(updatedCourse));

          // 3초 후 메시지 숨기기
          setTimeout(() => {
            this.saveMessage = '';
          }, 3000);
        }

      } catch (error) {
        console.error('저장 실패:', error);
        this.saveStatus = 'error';
        this.saveMessage = error.response?.data?.message || '저장 중 오류가 발생했습니다.';

        // 5초 후 메시지 숨기기
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

    // 지도 강제 업데이트 메서드 추가
    updateMapKey() {
      this.mapKey += 1;
      console.log('지도 키 업데이트:', this.mapKey);
    },

    // 드래그 종료시 지도 업데이트
    onDragEnd() {
      this.isDragging = false;
      console.log('드래그 종료');
      this.checkForChanges();
      this.updateMapKey(); // 추가
    },

    // 아이템 삭제시 지도 업데이트
    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);
      this.checkForChanges();
      this.updateMapKey(); // 추가
      this.closeDeleteModal();
    },

    // 아이템 추가시 고유 ID 생성 + 지도 업데이트
    addNewItem(place) {
      console.log('추가할 장소:', JSON.stringify(place, null, 2));

      const newItem = {
        id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 고유 ID 생성
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
.course-recommend-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.save-section {
  padding: 1rem;
  background-color: white;
  border-top: 1px solid #eee;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.save-status-message {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.alert-success {
  background-color: #d1edff;
  color: #0c5460;
  border: 1px solid #b8daff;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.save-btn-bottom {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #007bff;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.save-btn-bottom:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 드래그 관련 스타일 */
.draggable-item {
  transition: transform 0.2s ease;
  cursor: grab;
}

.draggable-item:active {
  cursor: grabbing;
}

.ghost-item {
  opacity: 0.5;
  background-color: #f8f9fa;
}

.chosen-item {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.drag-item {
  transform: rotate(5deg);
  opacity: 0.8;
}

/* 기존 스타일들... */
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
}

.chat-header {
  position: relative;
  flex-shrink: 0;
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
