<template>
  <div class="view-detail-page">

    <LocationSection :exhibition-information="exhibitionInformation" />

    <hr class="divider" />

    <ReviewSection :reviews="reviews" :rating="exhibition.rating" :review-count="exhibition.reviewCount"
      :photo-review-count="exhibition.photoReviewCount" :is-place="false" @show-modal="showModal" />

    <ReviewModal v-if="showReviewModal" :reviewText="reviewText" :selectedRating="selectedRating"
      :uploadedImageCount="uploadedImageCount" :isFormValid="isFormValid" @update:reviewText="reviewText = $event"
      @update:selectedRating="selectedRating = $event" @close="closeModal" @submit="submitReview" />
  </div>
</template>

<script>
// 필요한 모든 하위 컴포넌트들을 불러옵니다.
import LocationSection from '@/components/section/LocationSection.vue';
import ReviewSection from '@/components/section/ReviewSection.vue';
import ReviewModal from '@/components/modal/ReviewModal.vue';

export default {
  // 컴포넌트들을 등록하여 사용할 수 있게 합니다.
  components: {
    LocationSection,
    ReviewSection,
    ReviewModal,
  },

  name: 'ViewDetailWithModal',

  // 모든 데이터를 중앙에서 관리합니다.
  data() {
    return {
      // **모달 제어 데이터:**
      showReviewModal: false,

      // === 메인 상세 페이지 데이터 ===
      exhibition: {
        title: '단위와 양자 나라의 앨리스 - 큐빗(Cubit)에서 큐비트(Qubit)까지',
        rating: 4.7,
        reviewCount: 516,
        photoReviewCount: 120, // 사진 후기 개수 추가

        // subjectTag는 배열 형태로 변경하여 InfoSection으로 전달되도록 수정
        subjectTag: ['물리'],

        description: `‘이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.`,
        mainImage: 'https://www.sciencecenter.go.kr/scipia/File/110062/CKEDITOR_ATTATCHMENTS/7551/7551.jpg',
      },
      // LocationSection에 전달할 전시 정보
      exhibitionInformation: {
        exhibitionLocation: '국립과천과학관 2층 첨단기술관',
        operationPeriod: '2025.08.12(화) - 10.12(일)',
        operationHours: '오전 9:30 - 오후 5:30 (매주 월요일 정기 휴관)',
        entranceFee: '무료'
      },
      // ReviewSection에 전달할 리뷰 목록
      reviews: [
        { id: 1, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 2, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 3, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 4, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 5, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
      ],

      // === 모달 폼 데이터 ===
      reviewText: '', // 모달 내 후기 텍스트 입력값
      selectedRating: 4.0, // 모달 내 별점 선택값
      uploadedImageCount: 0, // 모달 내 업로드된 이미지 개수
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
    },

    // 'X' 버튼 또는 후기 제출 후 모달을 닫는 함수
    closeModal() {
      this.showReviewModal = false;
      // 모달 닫을 때 폼 데이터 초기화
      this.reviewText = '';
      this.selectedRating = 4.0;
      this.uploadedImageCount = 0;
    },

    // 모달 내 후기 제출 함수
    submitReview() {
      // 1. 새로운 후기 객체를 생성합니다. (임시)
      const newReview = {
        id: this.reviews.length + 1,
        avatar: '', // 임시 프로필 이미지
        name: '새 작성자', // 임시 이름
        stars: this.selectedRating,
        content: this.reviewText,
        // 현재 날짜를 '2025.05.15' 형식으로 포맷
        date: new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace(/\.$/, ''),
        likes: 0,
        dislikes: 0
      };

      // 2. reviews 배열의 맨 앞에 새 후기를 추가합니다.
      this.reviews.unshift(newReview);

      console.log('후기 제출 완료:', newReview); // 제출 데이터 확인

      // 3. 제출 후 모달을 닫고 사용자에게 알림을 줍니다.
      this.closeModal();
      alert('후기가 성공적으로 등록되었습니다.'); // 사용자에게 알림
    }
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
