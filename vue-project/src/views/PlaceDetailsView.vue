<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader pageTitle="교과서" />
    </div>

    <!-- 헤더 빼고 전체 스크롤 가능하게 -->
    <div class="scroll-content">
      <InfoSection :exhibition="exhibition" imageTag="전시 태그" :subjectTags="exhibition.subjectTag" />

      <hr class="divider" />

      <TabSection :isPlace="false" :activeTab="currentTab" @updateTab="(tabName) => currentTab = tabName" />

      <!-- 여기서부터는 스크롤로 내려감-->
      <div v-if="currentTab === 'detail'">
        <ContentDetailView />
      </div>

      <div v-else-if="currentTab === 'recommend'">
        <div class="course-list-container">
          <CourseRecommend :course-items="courseItems" />
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// 6개의 하위 컴포넌트를 불러옵니다.
import ExhibitionHeader from '@/components/ExhibitionHeader.vue';
import InfoSection from '@/components/InfoSection.vue';
import TabSection from '@/components/TabSection.vue';
import CourseRecommend from './CourseRecommend.vue';
import ContentDetailView from './ContentDetailView.vue';

export default {
  // 컴포넌트들을 등록하여 사용할 수 있게 합니다.
  components: {
    ExhibitionHeader,
    InfoSection,
    TabSection,
    CourseRecommend,
    ContentDetailView
  },

  name: 'PlaceDetailsView',

  // 모든 데이터를 중앙에서 관리합니다.
  data() {
    return {

      // 탭 상태 관리 데이터
      currentTab: 'detail', // 초기값
      // **모달 제어 데이터:**
      showReviewModal: false,

      // === 메인 상세 페이지 데이터 ===
      exhibition: {
        title: '단위와 양자 나라의 앨리스 - 큐빗(Cubit)에서 큐비트(Qubit)까지',
        rating: 4.7,
        reviewCount: 516,
        // new
        subjectTag: ['물리', '생명'],
        description: `‘이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
                      이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
                      이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
                      이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.`,
        // 메인 이미지 URL을 데이터 객체 안에 넣습니다.
        mainImage: 'https://www.sciencecenter.go.kr/scipia/File/110062/CKEDITOR_ATTATCHMENTS/7551/7551.jpg',
      },
      exhibitionInformation: {
        exhibitionLocation: '국립과천과학관 2층 첨단기술관',
        operationPeriod: '2025.08.12(화) - 10.12(일)',
        operationHours: '오전 9:30 - 오후 5:30 (매주 월요일 정기 휴관)',
        entranceFee: '무료'
      },
      reviews: [
        { id: 1, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 2, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 3, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 4, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
        { id: 5, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요! 특히 화학 실험실에서 직접 실험해볼 수 있어서 재미있었습니다.', date: '2025.05.15', likes: 12 },
      ],

      // 'AI 추천 코스' 탭에서 사용할 코스 데이터
      courseItems: [
        {
          number: 1,
          mainTitle: '국립중앙과학관',
          imageSrc: 'https://lh3.googleusercontent.com/gps-cs-s/AC9h4nrV8BM0LV8Ut5eepaWN3hzpJj3slSODMEaAi02DvDiyR9F6hTm6cnx0oW4TmgaP04jlBbssTq9DXz7TQ1UKWBDLF7_99ALOrsp41q9CEDYYf7OU1CqBLFoHdRY9myX1jljB8Vo=w408-h306-k-no',
          placeName: '대전국립중앙과학관',
          address: '대전광역시 유성구 대덕대로 481',
          description: '과학, 기술, 자연사에 대한 인터랙티브 전시물이 있는 박물관',
        },
        {
          number: 2,
          mainTitle: '엑스포 과학공원',
          imageSrc: 'https://lh3.googleusercontent.com/gps-cs-s/AC9h4nqOOfxYhEFNAQpgZyk0ZBl7cg9QUdGno20Rb5JQzbSzeDKMKihJVDeev2sp-Ky1HC27OUqKFjbV1g5IjHaxRJF52WmJeKxCBWTSZbBZLzXm3ikeBB2yqShxTyv_jgfa0Sq4XAYA=w426-h240-k-no',
          placeName: '대전 엑스포 과학공원',
          address: '대전광역시 유성구 대덕대로 480',
          description: '1993 대전엑스포 부지에 조성된 과학·문화 공원',
        },
        {
          number: 3,
          mainTitle: '한밭수목원',
          imageSrc: 'https://lh3.googleusercontent.com/gps-cs-s/AC9h4nob7TboAJOVzNoQyQlJbMN-X_9S4Tc8_s5TvyVpZiUQPUXUKypKIvLBebC_0OdUSUs13Tto2mQOvlbtfNWCRv_m_mghRzf3PV3iD9YVJoXUa0l1VgxeswYjswMfD4OxpSoC0GCTlQ=w408-h305-k-no',
          placeName: '한밭수목원',
          address: '대전광역시 서구 둔산대로 169',
          description: '다양한 종의 나무와 식물을 볼 수 있는 도심 속 공원으로 놀이 시설과 큰 연못, 산책로가 있습니다.',
        },
      ],

      // === 모달 폼 데이터 ===
      reviewText: '',          // 모달 내 후기 텍스트 입력값
      selectedRating: 4.0,     // 모달 내 별점 선택값
      uploadedImageCount: 0,   // 모달 내 업로드된 이미지 개수
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
      // 모달 닫을 때 폼 데이터 초기화 (선택 사항)
      this.reviewText = '';
      this.selectedRating = 4.0;
      this.uploadedImageCount = 0;
    },

    // 모달 내 후기 제출 함수
    submitReview() {
      // 1. 제출할 데이터 객체를 만듭니다.
      const reviewData = {
        text: this.reviewText,
        rating: this.selectedRating,
        images: this.uploadedImageCount,
      };

      // 2. 새로운 후기 객체를 생성합니다. (임시)
      const newReview = {
        id: this.reviews.length + 1,
        avatar: '', // 임시 프로필 이미지
        name: '새 작성자', // 임시 이름
        stars: this.selectedRating,
        content: this.reviewText,
        date: new Date().toLocaleDateString('ko-KR').replace(/\. /g, '.').replace(/\.$/, ''),
        likes: 0,
        dislikes: 0
      };

      // 3. reviews 배열의 맨 앞에 새 후기를 추가합니다.
      this.reviews.unshift(newReview);

      console.log('후기 제출 완료:', reviewData); // 제출 데이터 확인

      // 4. 제출 후 모달을 닫고 사용자에게 알림을 줍니다.
      this.closeModal();
      alert('후기가 성공적으로 등록되었습니다.'); // 사용자에게 알림
    }
  }
};
</script>

<style scoped>
/* === 공통 스타일 === */
.exhibition-detail-page {
  /* 전체 페이지의 높이를 뷰포트 높이(화면 높이)로 설정합니다. */
  height: 100%;
  /* Flexbox를 사용하여 콘텐츠를 쌓고 높이 관리를 용이하게 합니다. */
  display: flex;
  flex-direction: column;

  background-color: #f7f7f7;
}

/* TabSection 아래, 스크롤이 필요한 영역에 스타일 적용 */
.scroll-content {
  /* 남은 모든 공간(높이)을 차지하도록 합니다. */
  flex-grow: 1;
  /* 필수: 이 영역에서만 스크롤이 발생하도록 합니다. */
  overflow-y: auto;
  min-height: 0;
  /* background-color: #fff; */
  /* 스크롤 영역 배경색이 필요하다면 추가 */
  padding-bottom: 40px;
}

.divider {
  border: none;
  height: 10px;
  background-color: #f7f7f7;
  margin: 0;
}
</style>
