<template>
  <div class="view-detail-page">

    <div class="simulation-accordion" v-if="currentSimulationComponent">

      <button type="button" class="btn-simulation-toggle" @click="showSimulation = !showSimulation"
        :class="{ 'is-open': showSimulation }" :aria-expanded="showSimulation">
        <i class="bi bi-flask"></i>
        <span>가상 전시물 체험</span>
        <i class="bi" :class="showSimulation ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
      </button>

      <transition name="slide-fade">
        <div v-if="showSimulation" class="simulation-content-wrapper">
          <div class="simulation-inner">
            <div class="simulation-content">
              <component :is="currentSimulationComponent"></component>
            </div>
          </div>
        </div>
      </transition>

    </div>
    <LocationSection v-if="isPlace" :placeInformation="placeInformation" class="mt-3" />
    <LocationSection v-else :exhibitionInformation="exhibitionInformation" class="mt-3" />

    <hr class="divider" />

    <div v-if="isPlace" class="activity-section-wrapper">
      <hr class="divider" />
      <ActivityRecommender :place-id="targetId" :place-main-category="place.mainCategory"
        :place-sub-categories="place.subCategories" :place-grade-tag="place.gradeTag" />
    </div>

    <ReviewSection ref="reviewSectionRef" :target-id="targetId" :target-type="targetType"
      :current-user-id="currentUserId" :rating="exhibition.rating" :review-count="exhibition.reviewCount"
      :photo-review-count="photoReviewCount || 0" :is-place="isPlace" @show-modal="showModal"
      @edit-review="handleEditReview" @request-delete="handleRequestDelete" />

    <ReviewModal v-if="showReviewModal" :reviewText="reviewText" :selectedRating="selectedRating"
      :uploadedFiles="uploadedFiles" :isFormValid="isFormValid" @update:reviewText="reviewText = $event"
      @update:selectedRating="selectedRating = $event" @close="closeModal" @submit="submitReview"
      :isEditing="!!editingReviewId" @files-selected="handleFiles" @remove-file="handleRemoveFile" />

    <ConfirmDeleteModal :show="showConfirmDeleteModal" message="리뷰를 삭제하시겠습니까?" @confirm="confirmDelete"
      @close="cancelDelete" />
  </div>
</template>

<script>
import axios from '@/api/axiosSetup';

import { useAuthStore } from '@/stores/authStore';
import eventBus from '@/utils/eventBus'; // 💡 [추가] 글로벌 알림용

// 필요한 모든 하위 컴포넌트들을 불러옵니다.
import LocationSection from '@/components/section/LocationSection.vue';
import ReviewSection from '@/components/section/ReviewSection.vue';
import ReviewModal from '@/components/modal/ReviewModal.vue';
import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';
import ActivityRecommender from '@/components/recommend/ActivityRecommender.vue';


import ColumnarJoint from '@/components/simulations/ColumnarJoint.vue';
import StatesOfMatter from '@/components/simulations/StatesOfMatterSimulation.vue';
import Ecosystem from '@/components/simulations/EcosystemSimulation.vue';
import MagnetField from '@/components/simulations/MagnetField.vue';
import ThermalConductivity from '@/components/simulations/ThermalConductivitySim.vue';

// API 베이스 (Vite 환경변수 우선)
const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080';

export default {
  name: 'ViewDetailWithModal',
  // 컴포넌트들을 등록하여 사용할 수 있게 합니다.
  components: {
    LocationSection,
    ReviewSection,
    ReviewModal,
    ConfirmDeleteModal,
    ActivityRecommender,
    ColumnarJoint,
    StatesOfMatter,
    Ecosystem,
    MagnetField,
    ThermalConductivity
  },

  // 1. Props 정의
  // 부모인 PlaceDetailsView로부터 데이터를 받도록 Props를 정의
  props: {
    // LocationSection에 전달할 정보
    exhibitionInformation: {
      type: Object,
      default: () => ({}) // 기본값을 빈 객체로 설정
    },
    // ReviewSection에 전달할 정보 (rating, reviewCount 포함)
    exhibition: {
      type: Object,
      default: () => ({})
    },
    // LocationSection에 전달할 정보
    placeInformation: {
      type: Object,
      default: () => ({}) // 기본값을 빈 객체로 설정
    },
    // ReviewSection에 전달할 정보 (rating, reviewCount 포함)
    place: {
      type: Object,
      default: () => ({})
    },
    // ✨ (추가) 현재 '장소' 상세인지 '전시' 상세인지 구분
    isPlace: {
      type: Boolean,
      default: false
    },
    targetId: {
      type: [Number, String],
      required: true
    },
    targetType: {
      type: String,
      required: true
    },
    photoReviewCount: {
      type: Number,
      default: 0
    }
  },

  // 모든 데이터를 중앙에서 관리합니다.
  data() {
    return {
      // **모달 제어 데이터:**
      showReviewModal: false,
      showSimulation: false,

      // === 모달 폼 데이터 ===
      reviewText: '', // 모달 내 후기 텍스트 입력값
      selectedRating: 5.0, // 모달 내 별점 선택값
      uploadedFiles: [], //⭐️ 이 배열이 핵심입니다.
      // 수정/삭제 기능 관련 상태
      editingReviewId: null,
      showConfirmDeleteModal: false,
      pendingDeleteReviewId: null,
      isDeleting: false,
      loading: true,
      error: null,
    };
  },

  // 계산된 속성 (Computed Properties)
  computed: {
    // 폼 유효성 검사: 모달에 전달되어 '후기 올리기' 버튼의 활성화/비활성화를 결정합니다.
    isFormValid() {
      // 텍스트가 비어있지 않고, 별점이 0보다 크면 true를 반환합니다.
      return this.reviewText.trim().length > 0 && this.selectedRating > 0;
    },

    // Pinia에서 직접 끌어와서 this.isLoggedIn / this.currentUserId로 사용
    isLoggedIn() {
      const auth = useAuthStore();
      return !!auth.user; // 스토어 설계대로 user가 있으면 로그인
    },
    currentUserId() {
      const auth = useAuthStore();
      return auth.user?.userId ?? null;
    },
    // :흰색_확인_표시: --- 여기부터 3개의 computed 속성을 추가합니다 ---
    simulationMap() {
      // 맵핑 데이터 (필요에 따라 더 추가하세요)
      return {
        '초등 3학년': {
          '물리': MagnetField,
          '화학': StatesOfMatter,
          '생명': Ecosystem,
          '지구': ColumnarJoint
        },
        '초등 4학년': {
          '생명': Ecosystem,
          '지구': ColumnarJoint,
        },
        '초등 5학년': {
          '생명': Ecosystem,
          '화학': StatesOfMatter,
          '물리': ThermalConductivity,
          '지구': ColumnarJoint
        }
      };
    },
    currentSimulationComponent() {
      // data()의 'exhibition' 객체를 사용합니다.
      const grade = this.exhibition?.gradeTag; // '4학년' 대신 '초등 4학년'이 필요할 수 있습니다.
      const subject = this.exhibition?.mainCategory;
      // :느낌표:️ 중요: 'this.exhibition.gradeTag'에 '초등 3학년'처럼 '초등'이 포함되어 있는지,
      // 'this.exhibition.mainCategory'에 '물리', '생명' 등이 정확히 들어있는지 확인하세요.
      // 맵의 키(key)와 데이터 값이 정확히 일치해야 합니다.
      console.log(`[Sim Match] Grade: ${grade}, Subject: ${subject}`);
      if (grade && subject && this.simulationMap[grade] && this.simulationMap[grade][subject]) {
        return this.simulationMap[grade][subject];
      }
      return null;
    },

    // :흰색_확인_표시: --- 여기까지 추가 ---
  },

  // 사용자 정의 함수 (메서드): 모든 로직은 여기서 처리됩니다.
  methods: {
    // '후기작성' 버튼 클릭 시 모달을 표시하는 함수
    showModal() {
      if (!this.isLoggedIn) {
        this.$router.push('/login');
        return;
      }
      this.showReviewModal = true;
      this.editingReviewId = null; // 수정 모드 해제
      this.reviewText = '';
      this.selectedRating = 5.0;
      this.uploadedFiles = []; // ⭐️ [추가] 모달 열 때 파일 목록 초기화
    },

    // 'X' 버튼 또는 후기 제출 후 모달을 닫는 함수
    closeModal() {
      this.showReviewModal = false;
      // 모달 닫을 때 폼 데이터 초기화
      this.reviewText = '';
      this.selectedRating = 5.0;
      this.uploadedFiles = []; // ⭐️ [추가] 모달 닫을 때 파일 목록 초기화
      // ⭐️ [제거] this.uploadedImageCount = 0;
    },

    // 자식에게서 받은 파일들을 처리하는 메서드
    handleFiles(fileList) {
      const newFiles = Array.from(fileList);

      const remainingSlots = 5 - this.uploadedFiles.length;
      if (newFiles.length > remainingSlots) {
        eventBus.emit('show-global-alert', {
          message: `최대 5개의 이미지만 업로드할 수 있습니다.  ${remainingSlots}개만 추가됩니다.`,
          type: 'error'
        });
      }

      const filesToAdd = newFiles.slice(0, remainingSlots);
      this.uploadedFiles.push(...filesToAdd);

      console.log('현재 업로드된 파일 목록:', this.uploadedFiles);

      // ⭐️ [제거] this.uploadedImageCount = this.uploadedFiles.length;
    },
    // ⭐️ [추가] 자식(ReviewModal)이 요청한 파일 삭제 처리
    handleRemoveFile(index) {
      this.uploadedFiles.splice(index, 1);
      console.log('파일 제거됨. 현재 파일:', this.uploadedFiles);
    },

    handleEditReview(reviewToEdit) {
      this.editingReviewId = reviewToEdit.reviewId; // 수정 모드로 설정
      this.reviewText = reviewToEdit.content;      // 기존 내용 채우기
      this.selectedRating = reviewToEdit.rating;   // 기존 별점 채우기
      this.uploadedFiles = [];
      this.showReviewModal = true;                   // 모달 열기
    },

    // 모달 내 후기 제출 함수 api 연결 -> 여기서 새글/ 편집을 if문으로 나누자
    // 수정완료 - 로그인 연동
    async submitReview() {
      this.loading = true;
      this.error = null;

      const loggedIn = this.isLoggedIn?.value ?? this.isLoggedIn;
      // Pinia 스토어를 통해 로그인 상태를 확인
      if (!this.isLoggedIn) {
        this.error = "로그인이 필요한 기능입니다. 로그인 후 다시 시도해주세요.";
        this.loading = false;
        this.$router.push('/login');
        return;
      }


      // 1. FormData 객체를 생성합니다.
      const formData = new FormData();

      // 1. 리뷰 텍스트 데이터 생성
      // 서버 @RequestPart("dto")와 동일한 키 사용
      const dto = {
        targetId: this.targetId, // prop으로 받은 값
        targetType: this.targetType, // 'exhibition' | 'science_place' (서버와 합의값)
        content: this.reviewText, // data()의 값
        rating: this.selectedRating, // data()의 값
        authorId: this.currentUserId
      };

      // 3. ⭐️ 중요: dto 객체를 JSON 문자열로 변환하여 'Blob'으로 감싸고,
      // 'dto'라는 이름(KEY)으로 FormData에 추가합니다.
      // 백엔드의 @RequestPart("dto")와 이름이 일치해야 합니다.
      formData.append('dto', new Blob([JSON.stringify(dto)], {
        type: 'application/json'
      }
      ));

      // 4. (⭐️ 중요) handleFiles가 저장해 둔 파일 목록을 FormData에 추가합니다.
      //    (data에 this.uploadedFiles가 있다고 가정)
      //    백엔드의 @RequestPart("files")와 이름이 일치해야 합니다.
      if (this.uploadedFiles && this.uploadedFiles.length > 0) {
        this.uploadedFiles.forEach(file => {
          formData.append('files', file); // 'files'라는 KEY로 실제 파일 추가
        });
      }

      try {

        const cfg = {
          transformRequest: [(data, headers) => {
            if (data instanceof FormData) {
              // 🔑 boundary 자동 세팅 위해 Content-Type 제거
              delete headers['Content-Type'];
            }
            return data;
          }],
        };

        if (this.editingReviewId) {
          // 서버에 수정용 엔드포인트가 실제로 있을 때만 사용
          await axios.put(`/api/reviews/${this.editingReviewId}/upload`, formData, cfg);
          eventBus.emit('show-global-alert', {
            message: '리뷰가 성공적으로 수정되었습니다.',
            type: 'success'
          });
          this.$emit('review-posted');
        } else {
          await axios.post(`/api/reviews/upload`, formData, cfg);
          eventBus.emit('show-global-alert', {
            message: '리뷰가 성공적으로 등록되었습니다.',
            type: 'success'
          });
          this.$emit('review-posted');
        }

        this.closeModal();
        await this.$refs.reviewSectionRef?.fetchReviews();

      } catch (error) {
        // 5. API 호출 실패 시
        console.error('리뷰 등록 실패:', error);
        const msg = error?.response?.data || `리뷰 ${this.editingReviewId ? '수정' : '등록'}에 실패했습니다.`;
        eventBus.emit('show-global-alert', {
          message: msg,
          type: 'error'
        });
      } finally {
        this.loading = false;
      }
    },

    handleRequestDelete({ reviewId }) {
      this.pendingDeleteReviewId = reviewId;
      this.showConfirmDeleteModal = true;
    },
    // 리뷰 삭제 모달 관련
    // 삭제 버튼 클릭 시 호출될 핸들러
    handleDeleteReview(reviewIdToDelete) {
      this.deletingReviewId = reviewIdToDelete;
      this.showConfirmDeleteModal = true;
    },

    // ⭐️ 8. [추가] 삭제 확인 모달에서 '취소' 시 호출
    cancelDelete() {
      this.showConfirmDeleteModal = false;
      this.pendingDeleteReviewId = null;
    },

    // ⭐️ 9. [추가] 삭제 확인 모달에서 '확인' 시 호출 - 로그인 연동 완료
    async confirmDelete() {
      if (!this.pendingDeleteReviewId) return;
      this.isDeleting = true;

      try {
        // 9-1. 백엔드 API 호출 (DELETE /api/reviews/{reviewId})
        // (백엔드에 DELETE API 필요)
        await axios.delete(`api/reviews/${this.pendingDeleteReviewId}/delete`);

        // 9-2. 성공 처리
        eventBus.emit('show-global-alert', {
          message: '리뷰가 삭제되었습니다.',
          type: 'success'
        });
        this.showConfirmDeleteModal = false;
        this.pendingDeleteReviewId = null;

        // 9-3. 목록 새로고침
        if (this.$refs.reviewSectionRef) {
          this.$refs.reviewSectionRef.fetchReviews();
        }
        // 9-4. (중요) 부모에게 카운트 감소 알림
        this.$emit('review-deleted'); // 새 이벤트 정의 필요

      } catch (error) {
        console.error('리뷰 삭제 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '리뷰가 삭제에 실패했습니다.',
          type: 'error'
        });
        this.showConfirmDeleteModal = false; // 실패해도 모달은 닫기
        this.pendingDeleteReviewId = null;
      } finally {
        this.isDeleting = false;
      }
    },
  }
};
</script>

<style scoped>
/* === 공통 스타일 === */
.view-detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
  /* 좌우 여백을 5px -> 12px로 늘림 */
  background-color: #f7f7f7;
}

.scroll-content {
  flex-grow: 1;
  overflow-y: auto;
}

.divider {
  border: none;
  height: 10px;
  background-color: #f7f7f7;
  margin: 0;
}

/* ★★★★★
  [수정]
  아래 스타일을 모두 적용/수정합니다.
  ★★★★★
*/

/* 1. 아코디언 전체 컨테이너 (흰색 배경) */
.simulation-accordion {
  background-color: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  /* 둥근 모서리 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-top: 8px;
  overflow: hidden;
  /* 중요: 자식 컨텐츠가 둥근 모서리를 넘지 않게 함 */
  transition: all 0.3s ease-out;
}

/* 2. 토글 버튼 (아코디언 헤더) */
.btn-simulation-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  /* 아이콘과 텍스트 간격 */

  background-color: transparent;
  /* 배경색 없음 (부모 흰색 사용) */
  border: none;

  width: 100%;
  /* 부모 꽉 채움 */
  padding: 16px;
  /* 넉넉한 클릭 영역 */

  color: #4A7CEC;
  /* 앱의 메인 컬러 */
  font-size: 0.95rem;
  /* 살짝 키움 */
  font-weight: 600;

  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn-simulation-toggle:hover {
  background-color: #f8f9fa;
  /* 마우스 오버 시 살짝 회색 */
}

/* 3. 버튼 안의 아이콘/텍스트 정렬 */
.btn-simulation-toggle .bi-flask {
  font-size: 1.1rem;
}

.btn-simulation-toggle span {
  flex-grow: 1;
  /* 텍스트가 남은 공간 다 차지 (왼쪽 정렬) */
  text-align: left;
}

.btn-simulation-toggle .bi-chevron-down,
.btn-simulation-toggle .bi-chevron-up {
  font-size: 1rem;
}

/* 4. 펼쳐지는 컨텐츠 래퍼 */
.simulation-content-wrapper {
  padding: 0;
  margin: 0;
  border-top: 1px solid #E5E7EB;
  /* 헤더와 컨텐츠 구분선 */
  /* 트랜지션을 위해 overflow: hidden이 필요합니다 */
  overflow: hidden;
}

/* 5. 트랜지션 (슬라이드+페이드) */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
  max-height: 500px;
  /* 시뮬레이션 최대 높이 (필요시 조절) */
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
  max-height: 500px;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
  max-height: 0px;
}

.slide-fade-enter-to,
.slide-fade-leave-from {
  opacity: 1;
  max-height: 500px;
  transform: translateY(0);
}


/* --- (이하 시뮬레이션 컨테이너 스타일은 그대로 유지) --- */

/* [수정]
   .simulation-container는 제거되었으므로,
   .simulation-inner에 직접 스타일을 적용합니다.
*/
.simulation-inner {
  /* [수정]
    부모(.simulation-accordion)가 둥근 모서리를 관리하므로,
    자식의 둥근 모서리는 모두 0으로 설정합니다.
  */
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.05) 0%, rgba(16, 185, 129, 0.05) 100%);
  border: none;
  border-radius: 0;
  /* 부모가 둥근 모서리 관리 */
  overflow: hidden;
  box-shadow: none;
  /* 부모가 그림자 관리 */
}

.simulation-header {
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.1), rgba(16, 185, 129, 0.08));
  padding: 16px;
  border-bottom: 1px solid rgba(74, 124, 236, 0.12);
  backdrop-filter: blur(10px);
  align-items: center;
  /* 헤더의 상단 둥근 모서리 제거 */
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.btn-close-simulation {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #EF4444;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1.2rem;
  padding: 0;
  line-height: 1;
}

.btn-close-simulation:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  transform: scale(1.05);
}

.simulation-content {
  padding: 1.5rem;
  background: #FFFFFF;
  min-height: 300px;
}

/* [제거] @keyframes slideDownIn는 <transition>으로 대체됨 */
</style>
