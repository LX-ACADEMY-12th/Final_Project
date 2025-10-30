<template>
  <div class="view-detail-page">

    <LocationSection v-if="isPlace" :placeInformation="exhibitionInformation" />
    <LocationSection v-else :exhibitionInformation="exhibitionInformation" />

    <hr class="divider" />

      <ReviewSection
        ref="reviewSectionRef"
        :target-id="targetId"
        :target-type="targetType"
        :current-user-id="currentUserId"
        :rating="exhibition.rating" 
        :review-count="exhibition.reviewCount"
        :photo-review-count="photoReviewCount || 0" 
        :is-place="isPlace" 
        @show-modal="showModal" 
        @edit-review="handleEditReview"
        @request-delete="handleRequestDelete"
      />

      <ReviewModal v-if="showReviewModal" 
        :reviewText="reviewText" 
        :selectedRating="selectedRating"
        :uploadedFiles="uploadedFiles"  
        :isFormValid="isFormValid" 
        @update:reviewText="reviewText = $event"
        @update:selectedRating="selectedRating = $event" 
        @close="closeModal" 
        @submit="submitReview" 
        :isEditing="!!editingReviewId"
        @files-selected="handleFiles"
        @remove-file="handleRemoveFile"
        />

        <ConfirmDeleteModal
        :show="showConfirmDeleteModal"
        @confirm="confirmDelete"
        @close="cancelDelete"
        />
  </div>
</template>

<script>
import axios from 'axios';
// 필요한 모든 하위 컴포넌트들을 불러옵니다.
import LocationSection from '@/components/section/LocationSection.vue';
import ReviewSection from '@/components/section/ReviewSection.vue';
import ReviewModal from '@/components/modal/ReviewModal.vue';
import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';

// API 베이스 (Vite 환경변수 우선)
const API_BASE = import.meta.env?.VITE_API_BASE || 'http://localhost:8080';

export default {
  // 컴포넌트들을 등록하여 사용할 수 있게 합니다.
  components: {
    LocationSection,
    ReviewSection,
    ReviewModal,
    ConfirmDeleteModal,
  },

  name: 'ViewDetailWithModal',

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
    currentUserId: [String, Number],
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

      // === 모달 폼 데이터 ===
      reviewText: '', // 모달 내 후기 텍스트 입력값
      selectedRating: 5.0, // 모달 내 별점 선택값
      uploadedFiles: [], //⭐️ 이 배열이 핵심입니다.
      // 수정/삭제 기능 관련 상태
      editingReviewId: null,
      showConfirmDeleteModal: false,
      pendingDeleteReviewId: null,
      isDeleting: false,
    };
  },

  // 계산된 속성 (Computed Properties)
  computed: {
    // 폼 유효성 검사: 모달에 전달되어 '후기 올리기' 버튼의 활성화/비활성화를 결정합니다.
    isFormValid() {
      // 텍스트가 비어있지 않고, 별점이 0보다 크면 true를 반환합니다.
      return this.reviewText.trim().length > 0 && this.selectedRating > 0;
    }
  },

  // 사용자 정의 함수 (메서드): 모든 로직은 여기서 처리됩니다.
  methods: {
    // '후기작성' 버튼 클릭 시 모달을 표시하는 함수
    showModal() {
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
        this.$alert(`최대 5개의 이미지만 업로드할 수 있습니다. ${remainingSlots}개만 추가됩니다.`);
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
    async submitReview() {
      // 1. FormData 객체를 생성합니다.
      const formData = new FormData();
      
      // 1. 리뷰 텍스트 데이터 생성
      const dto = {
        targetId: this.targetId, // prop으로 받은 값
        targetType : this.targetType,
        content: this.reviewText, // data()의 값
        rating: this.selectedRating, // data()의 값
        authorId: 1 // 아직 로그인 아이디가 없어서 하드코딩함
      };

      // 3. ⭐️ 중요: dto 객체를 JSON 문자열로 변환하여 'Blob'으로 감싸고,
      // 'dto'라는 이름(KEY)으로 FormData에 추가합니다.
      // 백엔드의 @RequestPart("dto")와 이름이 일치해야 합니다.
      formData.append('dto', new Blob([JSON.stringify(dto)], {
        type: 'application/json'
      }));

      // 4. (⭐️ 중요) handleFiles가 저장해 둔 파일 목록을 FormData에 추가합니다.
      //    (data에 this.uploadedFiles가 있다고 가정)
      //    백엔드의 @RequestPart("files")와 이름이 일치해야 합니다.
      if (this.uploadedFiles && this.uploadedFiles.length > 0) {
        this.uploadedFiles.forEach(file => {
          formData.append('files', file); // 'files'라는 KEY로 실제 파일 추가
        });
      }

      try {
        if(this.editingReviewId) {
          // 수정 모드 api 호출 : PUT /api/reviews/{reviewId} 호출
            await axios.put(`${API_BASE}/api/reviews/${this.editingReviewId}`, formData);
            this.$alert('리뷰가 성공적으로 수정되었습니다.');
            // 수정 성공시 부모에게 알림
            this.$emit('review-posted'); // (posted 이벤트를 재사용)
          } else {
            // API호출(POST/api/reviews) -> 그냥 새 리뷰
            await axios.post(`${API_BASE}/api/reviews`, formData);
            this.$alert(`리뷰가 성공적으로 등록되었습니다.`);
            // 부모(PlaceDetailsView)에게 "리뷰 등록됨" 이벤트 쏘기
            this.$emit('review-posted');
          }
        
        this.closeModal(); // 모달 닫기
        // 수정/등록 후 목록 새로고침 (기존 로직)
        // 자식(ReviewSection)의 리뷰 목록을 '새로고침'
        // <template>에서 "ref"로 지정한 컴포넌트의 메소드를 직접 호출 -> 이게 먼말
        if (this.$refs.reviewSectionRef) {
          this.$refs.reviewSectionRef.fetchReviews();
        }

      } catch(error) {
        // 5. API 호출 실패 시
        console.error('리뷰 등록 실패:', error);
        this.$alert(`리뷰 ${this.editingReviewId ? '수정' : '등록'}에 실패했습니다.`);
      }
    },

    handleRequestDelete({reviewId}) {
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
    // ⭐️ 9. [추가] 삭제 확인 모달에서 '확인' 시 호출
    async confirmDelete() {
      if (!this.pendingDeleteReviewId) return;
      this.isDeleting = true;

      try {
        // 9-1. 백엔드 API 호출 (DELETE /api/reviews/{reviewId})
        // (백엔드에 DELETE API 필요)
        await axios.delete(`${API_BASE}/api/reviews/${this.pendingDeleteReviewId}`);

        // 9-2. 성공 처리
        this.$alert('리뷰가 삭제되었습니다.');
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
        this.$alert('리뷰 삭제에 실패했습니다.');
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
  /* 전체 페이지의 높이를 뷰포트 높이(화면 높이)로 설정합니다. */
  height: 100%;
  /* Flexbox를 사용하여 콘텐츠를 쌓고 높이 관리를 용이하게 합니다. */
  display: flex;
  flex-direction: column;
  padding: 5px;
  background-color: #f7f7f7;
}

/* TabSection 아래, 스크롤이 필요한 영역에 스타일 적용 */
.scroll-content {
  /* 남은 모든 공간(높이)을 차지하도록 합니다. */
  flex-grow: 1;
  /* 필수: 이 영역에서만 스크롤이 발생하도록 합니다. */
  overflow-y: auto;
}

.divider {
  border: none;
  height: 10px;
  background-color: #f7f7f7;
  margin: 0;
}
</style>