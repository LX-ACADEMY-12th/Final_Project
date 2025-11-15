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
      <CourseMap :items="courseItems" :key="mapKey" :pageType="pageType" class="map-area" />

      <div class="course-root-name-improved">
        <span class="fw-bolder me-2">
          {{ exhibitionName || '코스 이름 없음' }}
        </span>

        <span v-if="pageType" class="type-badge"
          :class="{ 'badge-place': pageType === 'place', 'badge-exhibition': pageType === 'exhibition' }">
          {{ pageType === 'place' ? '답사 코스' : '전시 코스' }}
        </span>
      </div>

      <div class="d-flex justify-content-center align-items-center mt-2 mb-2" v-if="pageType === 'exhibition'">
        <button class="btn virtual-tour-btn" @click="goToVirtualTour">
          가상 관람 시작하기
        </button>
      </div>

      <div class="scrollable-content">
        <div class="course-list-container">
          <div v-if="pageType === 'exhibition'">
            <CourseExhibitionCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
              couseType="전시" @delete="handleDelete(course.id)" />
          </div>

          <div v-else-if="pageType === 'place'">
            <draggable v-model="courseItems" :animation="200" ghost-class="ghost-item" chosen-class="chosen-item"
              drag-class="drag-item" @start="onDragStart" @end="onDragEnd" @change="onDragChange" item-key="id">
              <template #item="{ element }">
                <CoursePlaceEditCard :item="element" :showControls="true" couseType="답사"
                  @delete="handleDelete(element.id)" class="draggable-item" />
              </template>
            </draggable>

            <div class="d-flex justify-content-center mt-3">
              <button class="btn btn-outline-secondary rounded-pill add-item-inline" @click="openAddModal">
                <i class="bi bi-plus-circle-fill"></i> 장소 추가하기
              </button>
            </div>
          </div>

          <div v-else>
            <p class="text-muted">잘못된 코스 타입입니다.</p>
          </div>
        </div>
      </div>

      <div class="save-section" v-if="hasChanges">
        <div v-if="saveMessage" class="save-status-message"
          :class="`alert-${saveStatus === 'success' ? 'success' : 'danger'}`">
          {{ saveMessage }}
        </div>
        <button class="btn save-btn-bottom" @click="saveChanges" :disabled="!hasChanges || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isSaving ? '저장 중...' : '변경사항 저장' }}
        </button>
      </div>
    </template>

    <ConfirmDeleteModal :show="showDeleteModal" message="장소를 삭제하시겠어요?" @confirm="confirmDeleteItem"
      @close="closeDeleteModal" />
    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
import axios from '@/api/axiosSetup';
import draggable from 'vuedraggable';

// Pinia 스토어 관련 임포트
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import eventBus from '@/utils/eventBus';
import router from '@/router'

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
  // Pinia 스토어의 state/getter를 'this'에 바인딩
  setup() {
    const authStore = useAuthStore();
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);

    return {
      isLoggedIn,
      currentUserId // 'this.currentUserId'로 사용 가능
    };
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
          const response = await axios.get(`/api/schedules/user/${this.currentUserId}`);

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
                // --- 스냅샷 태그 매핑 ---

                // 1. type (itemType을 프론트엔드 'type'으로 매핑)
                // (백엔드 DTO의 itemType이 'exhibition' 또는 'science_place' 또는 'custom')
                type: item.itemType,
                itemType: item.itemType,

                // 2. subject (mainCategoryNames 리스트의 [첫 번째 값]을 사용)
                subject: (item.mainCategoryNames && item.mainCategoryNames.length > 0)
                  ? item.mainCategoryNames[0]
                  : null,

                // 3. grade (gradeNames 리스트의 [첫 번째 값]을 사용)
                grade: (item.gradeNames && item.gradeNames.length > 0)
                  ? item.gradeNames[0]
                  : null,

                // 4. hashtags (subCategoryNames 리스트 자체를 사용)
                hashtags: item.subCategoryNames || [],
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
          if (item.type === 'custom' && !item.id) {
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

        console.log("코스 상세 정보 로드 성공:", JSON.stringify(targetCourse, null, 2));
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

      // ID의 순서만 비교하도록 단순화
      const currentItemIds = this.courseItems.map(createItemIdentifier);
      const originalItemIds = this.originalCourseItems.map(createItemIdentifier);

      // 개수와 순서가 모두 일치하는지 한 번에 비교
      const isDifferent = JSON.stringify(currentItemIds) !== JSON.stringify(originalItemIds);

      this.hasChanges = isDifferent;

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
              itemType: isCustom ? 'custom' : item.itemType,
              customName: isCustom ? (item.title || null) : null,
              customAddress: isCustom ? (item.place || null) : null,
              customLatitude: isCustom ? (item.lat || null) : null,
              customLongitude: isCustom ? (item.lng || null) : null,

              // --- 스냅샷 태그 추가 ---
              // (백엔드 DTO 필드명 기준)
              // 1. categoryName (배열 -> 문자열)
              categoryName: (Array.isArray(item.subject) && item.subject.length > 0)
                ? item.subject[0]
                : (item.subject || null), // (이미 문자열이거나 custom item일 경우 대비)

              // 2. gradeName (배열 -> 문자열)
              gradeName: (Array.isArray(item.grade) && item.grade.length > 0)
                ? item.grade[0]
                : (item.grade || null),

              // 3. subCategories (List<String>) - 이 부분은 loadCourse가 올바르게 파싱해야 함
              subCategories: item.hashtags
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
          // 저장 성공 후, 세션스토리지를 지우고 API Fallback을 강제로 유도합니다.
          sessionStorage.removeItem(`courseData_${this.course.id}`);

          // '저장됨' 메시지 표시 후 1.5초 뒤에 새로고침하여 API Fallback 실행
          setTimeout(() => {
            this.saveMessage = '';
            this.loadCourse(); // API Fallback 실행 (loadCourse가 태그를 매핑하도록 수정됨)
            this.hasChanges = false; // 저장 버튼 숨기기
          }, 1500); // 1.5초간 메시지 표시
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
        eventBus.emit('show-global-confirm', {
          message: '저장하지 않은 변경사항이 있습니다. 정말 나가시겠습니까?',
          msg: '확인',
          onConfirm: () => {
            this.$router.back();
          }
        });
        return; // 확인 모달이 열렸으니 여기서 종료
      }

      this.$router.back();
    },

    handleDelete(id) {
      if (!id) {
        console.error('삭제할 ID가 전달되지 않았습니다.');
        return;
      }
      console.log('삭제할 ID (모달 열기):', id);
      // 1. 삭제할 ID를 data에 저장
      this.itemToDeleteId = id;
      // 2. 확인 모달을 엶
      this.showDeleteModal = true;
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

    // 드래그 종료 함수
    onDragEnd() {
      this.isDragging = false;
      console.log('드래그 종료 (상태 변경만 처리)');
    },
    // 드래그 종료시 변경
    onDragChange() {
      console.log('드래그로 인해 v-model 변경됨. 번호 및 지도 갱신 시작.');
      // 1. [순서 변경] 번호를 무조건 다시 매깁니다.
      this.reorderCourseItems();
      // 2. [상태 변경] 원본과 비교하여 '저장' 버튼 활성화 여부 결정
      this.checkForChanges();
      // 3. [지도 변경] 지도 강제 리렌더링
      this.updateMapKey();
    },

    // 아이템 삭제시 지도 업데이트
    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);

      // [순서 변경] 번호를 다시 매깁니다.
      this.reorderCourseItems();
      // [상태 변경] 원본과 비교
      this.checkForChanges();

      this.updateMapKey();
      this.closeDeleteModal();
    },

    // 아이템 추가시 고유 ID 생성 + 지도 업데이트
    addNewItem(place) {
      console.log('추가할 장소:', JSON.stringify(place, null, 2));

      const newItem = {
        id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 고유 ID 생성
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
      // [순서 변경] 번호를 다시 매깁니다.
      this.reorderCourseItems();
      // [상태 변경] 원본과 비교
      this.checkForChanges();

      this.updateMapKey();
      this.closeAddModal();
    },
    goToVirtualTour() {
      router.push('/virtualTour')
    }
  }
}
</script>

<style scoped>
/* -------------------- 레이아웃 및 컨테이너 -------------------- */
.course-recommend-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f8f9fa;
  /* 배경색 통일 */
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

.scrollable-content {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.scrollable-content::-webkit-scrollbar {
  display: none;
}

.course-list-container {
  padding: 24px;
}

/* -------------------- 제목 영역 개선 -------------------- */
.course-root-name-improved {
  display: flex;
  align-items: center;
  /* 뱃지와 텍스트 수직 정렬 */
  font-size: 24px;
  font-weight: 700;
  margin: 16px 24px 8px 24px;
  flex-shrink: 0;
  color: #212529;
}

.type-badge {
  /* 작은 뱃지 스타일 */
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  align-self: flex-start;
  line-height: 1.5;
  /* 텍스트 정렬 보정 */
  margin-top: 3px;
  /* 제목 폰트 사이즈에 맞게 살짝 아래로 */
}

.badge-place {
  background-color: #e0f2fe;
  /* Light Blue */
  color: #0c4a6e;
}

.badge-exhibition {
  background-color: #fef3c7;
  /* Light Yellow */
  color: #92400e;
}

/* -------------------- 버튼 공통 스타일 -------------------- */
.btn {
  /* 기본 버튼 스타일을 재정의하여 아래 전용 버튼 스타일과 충돌 방지 */
  width: auto;
  height: auto;
  border-radius: 8px;
  background-color: #4A7CEC;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

/* -------------------- 기능 버튼 스타일 -------------------- */

.virtual-tour-btn {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #6366F1;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
}

.save-btn-bottom {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #4A7CEC;
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

/* 🚨 경로 추가 인라인 버튼 스타일 */
.add-item-inline {
  width: auto;
  height: 38px;
  padding: 0.5rem 1.5rem;
  background-color: transparent;
  color: #6366F1;
  border: 1px dashed #6366F1;
  font-weight: 500;
  font-size: 14px;
  border-radius: 30px;
  /* 둥근 모양 유지 */
}

.add-item-inline:hover {
  background-color: #f0f0ff;
}

/* -------------------- 저장 상태 메시지 -------------------- */
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

/* -------------------- 드래그 스타일 -------------------- */
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
</style>
