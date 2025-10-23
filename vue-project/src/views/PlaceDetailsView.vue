<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시 상세" />
      <ExhibitionHeader v-else-if="pageType === 'place'" pageTitle="장소 상세" />
      <ExhibitionHeader v-else pageTitle="로딩 중..." />
    </div>

    <div class="scroll-content">

      <div v-if="pageType === 'exhibition'">
        <InfoSection :exhibition="exhibition" imageTag="전시 태그" :subjectTags="exhibition.subjectTag" />
        <hr class="divider" />
        <TabSection :isPlace="false" :activeTab="currentTab" @updateTab="(tabName) => currentTab = tabName" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :info="exhibitionInformation" :description="exhibition.description" />
        </div>
        <div v-else-if="currentTab === 'recommend'">
          <CourseRecommend :course-items="courseItems" type="exhibition" />
        </div>
      </div>

      <div v-else-if="pageType === 'place'">
        <InfoSection :exhibition="place" imageTag="장소 태그" :subjectTags="place.subjectTag" />
        <hr class="divider" />
        <TabSection :isPlace="true" :activeTab="currentTab" @updateTab="(tabName) => currentTab = tabName" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :info="placeInformation" :description="place.description" />
        </div>
        <div v-else-if="currentTab === 'recommend'">
          <CourseRecommend :course-items="courseItems" type="place" />
        </div>
      </div>

      <div v-else class="loading-container">
        <p>데이터를 불러오는 중입니다...</p>
      </div>

    </div>
  </div>
</template>

<script>
// 6개의 하위 컴포넌트를 불러옵니다.
import ExhibitionHeader from '@/components/header/ExhibitionHeader.vue';
import InfoSection from '@/components/section/InfoSection.vue';
import TabSection from '@/components/section/TabSection.vue';
import ContentDetailView from './ContentDetailView.vue'; // 전시/ 장소 상세 정보 vue
import CourseRecommend from './CourseRecommend.vue'; // 추천 연계 / 실내 관람 코스 보여주는 vue
// import ReviewModal from '@/components/ReviewModal.vue'; // 후기 모달 (필요시)

export default {
  name: 'PlaceDetailsView',
  // 컴포넌트들을 등록하여 사용할 수 있게 합니다.
  components: {
    ExhibitionHeader,
    InfoSection,
    TabSection,
    CourseRecommend,
    ContentDetailView
  },

  // 모든 데이터를 중앙에서 관리합니다.
  data() {
    return {
      // 템플릿 v-if 분기용 변수
      pageType: null, // 'exhibition' 또는 'place'가 저장될 곳
      currentTab: 'detail', // 초기값
      // **모달 제어 데이터:**
      showReviewModal: false,

      // --- '전시' 데이터를 담을 객체 (초기 상태) ---
      exhibition: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        subjectTag: [],
        type: '', // 'exhibition' 또는 'place'가 채워질 곳
        description: '',
        mainImage: 'https://via.placeholder.com/600x400', // 로딩 이미지
      },
      exhibitionInformation: {
        exhibitionLocation: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: ''
      },

      // --- '장소' 데이터를 담을 객체 (초기 상태) ---
      place: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        subjectTag: [],
        type: '', // 'exhibition' 또는 'place'가 채워질 곳
        description: '',
        mainImage: 'https://via.placeholder.com/600x400', // 로딩 이미지
      },

      placeInformation: {
        exhibitionLocation: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: ''
      },

      // --- 공통 데이터 (리뷰, 추천 코스) ---
      reviews: [],
      courseItems: [], // 추천 코스 데이터 (초기 상태)

      // === 모달 폼 데이터 ===
      reviewText: '',
      selectedRating: 4.0,
      uploadedImageCount: 0,
    };
  },

  // [추가] 컴포넌트 생성 시 URL을 확인하여 데이터 로드
  created() {
    // URL에서 ID 값을 가져옵니다. (예: "1" 또는 "4")
    const id = this.$route.params.id;
    // URL 경로에 '/place/'가 포함되어 있는지 확인합니다.
    const isPlace = this.$route.path.includes('/place/');

    if (isPlace) {
      // 1. pageType을 'place'로 설정
      this.pageType = 'place';
      console.log(`장소 상세 페이지 로드 (ID: ${id})`);
      // 2. '장소' 데이터 fetch 함수 호출
      this.fetchPlaceData(id);
    } else {
      // 1. pageType을 'exhibition'으로 설정
      this.pageType = 'exhibition';
      console.log(`전시 상세 페이지 로드 (ID: ${id})`);
      // 2. '전시' 데이터 fetch 함수 호출
      this.fetchExhibitionData(id);
    }
  },

  // 계산된 속성 (Computed Properties)
  computed: {
    // (기존 코드와 동일)
    isFormValid() {
      return this.reviewText.trim().length > 0 && this.selectedRating > 0;
    }
  },

  // 사용자 정의 함수 (메서드)
  methods: {

    // [id: 1] '전시' 데이터를 불러오는 함수
    fetchExhibitionData() {
      // (id 값과 무관하게 하드코딩된 '전시' 데이터 로드)
      // === 'this.exhibition' 관련 객체에 데이터 삽입 ===
      this.exhibition = {
        title: '단위와 양자 나라의 앨리스 - 큐빗(Cubit)에서 큐비트(Qubit)까지',
        rating: 4.7,
        reviewCount: 516,
        subjectTag: ['물리', '생명'],
        type: 'exhibition', // 하위 컴포넌트 전달용
        description: `이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
             이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
             이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다.
             이상한 나라의 앨리스’를 모티브로, 유아부터 성인까지 \n 단위와 양자 기술의 발전사를 쉽고 재미있게 \n 체험할 수 있도록 기획되었습니다`,
        mainImage: 'https://www.sciencecenter.go.kr/scipia/File/110062/CKEDITOR_ATTATCHMENTS/7551/7551.jpg',
      };
      this.exhibitionInformation = {
        exhibitionLocation: '국립과천과학관 2층 첨단기술관',
        operationPeriod: '2025.08.12(화) - 10.12(일)',
        operationHours: '오전 9:30 - 오후 5:30 (매주 월요일 정기 휴관)',
        entranceFee: '무료'
      };
      this.reviews = [
        { id: 1, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요!', date: '2025.05.15', likes: 12 },
        { id: 2, avatar: '', name: '학부모', stars: 5, content: '아이가 정말 좋아했어요!', date: '2025.05.15', likes: 12 },
      ];
      // '전시' 추천 코스 (ExhibitionCourseCard용)
      this.courseItems = [
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
          lat: 36.3758, // 국립중앙과학관
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
          lat: 37.4363, // 국립과천과학관
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
          lat: 37.6094, // 서울시립과학관
          lng: 127.0706
        }
      ];
    },

    // [id: 4] '장소(답사)' 데이터를 불러오는 함수
    fetchPlaceData() {
      // (id 값과 무관하게 하드코딩된 '장소' 데이터 로드)
      // === 'this.place' 관련 객체에 데이터 삽입 ===
      this.place = {
        title: '해운대',
        rating: 4.8,
        reviewCount: 1500,
        subjectTag: ['지구', '5학년'],
        type: 'place', // 하위 컴포넌트 전달용
        description: '부산광역시 해운대구에 위치한 대한민국 대표 해수욕장입니다. 다양한 해양 생태와 지질학적 특성을 관찰할 수 있습니다.\n부산광역시 해운대구에 위치한 대한민국 대표 해수욕장입니다. 다양한 해양 생태와 지질학적 특성을 관찰할 수 있습니다.',
      };

      this.placeInformation = {
        exhibitionLocation: '부산광역시 해운대구',
        operationPeriod: '연중무휴',
        operationHours: '24시간 개방',
        entranceFee: '무료'
      };
      this.reviews = [
        { id: 1, avatar: '', name: '방문객', stars: 5, content: '아이와 모래놀이하기 좋았어요.', date: '2025.07.10', likes: 20 },
      ];
      // '장소' 추천 코스 (PlaceCourseCard용)
      this.courseItems = [
        {
          id: 1,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '해운대',
          place: '부산시 해운대구',
          hashtags: ['항상성과 몸의 조절', '생명과학과 인간의 생활'],
          lat: 36.3758, // 국립중앙과학관
          lng: 127.3845
        },
        {
          id: 2,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '4학년',
          title: '서울숲',
          place: '서울시 성동구',
          hashtags: ['파동', '빛', '물리1', '체험'],
          lat: 37.4363, // 국립과천과학관
          lng: 126.9746
        },
        {
          id: 3,
          number: 3,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '5학년',
          title: '지질연구원',
          place: '대전시 유성구',
          hashtags: ['에너지', '화학 반응', '미래 기술'],
          lat: 37.6094, // 서울시립과학관
          lng: 127.0706
        }
      ];
    },

    // --- (기존 모달/리뷰 관련 메서드) ---
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

/* 로딩 중일 때 스타일 */
.loading-container {
  padding: 40px;
  text-align: center;
  color: #888;
  font-size: 16px;
}
</style>
