<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시 상세" />
      <ExhibitionHeader v-else-if="pageType === 'place'" pageTitle="장소 상세" />
      <ExhibitionHeader v-else pageTitle="로딩 중..." />
    </div>

    <div class="scroll-content">

      <!--전시일때-->
      <div v-if="pageType === 'exhibition'">
        <InfoSection :exhibition="exhibition" imageTag="전시 태그" :mainCategory="exhibition.mainCategory"
          :subCategories="exhibition.subCategories" :gradeTag="exhibition.gradeTag" />
        <hr class="divider" />
        <TabSection :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="exhibitionInformation" :exhibition="exhibition" :reviews="reviews"
            :isPlace="false" @submit-review="handleReviewSubmit" />
        </div>
        <!--코스추천-->
        <div v-else-if="currentTab === 'recommend'">
          <CourseRecommend :course-items="courseItems" type="exhibition" />
        </div>
      </div>

      <!--장소일때-->
      <div v-else-if="pageType === 'place'">
        <InfoSection :exhibition="place" imageTag="장소 태그" :mainCategory="place.mainCategory"
          :subCategories="place.subCategories" :gradeTag="place.gradeTag" />
        <hr class="divider" />
        <TabSection :isPlace="true" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="placeInformation" :exhibition="place" :reviews="reviews"
            :isPlace="true" @submit-review="handleReviewSubmit" />
        </div>
        <!--코스추천-->
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
import axios from 'axios';

// 하위 컴포넌트들
import ExhibitionHeader from '@/components/header/ExhibitionHeader.vue';
import InfoSection from '@/components/section/InfoSection.vue';
import TabSection from '@/components/section/TabSection.vue';
import ContentDetailView from './ContentDetailView.vue';
import CourseRecommend from './CourseRecommend.vue';

// API 베이스 (Vite 환경변수 우선)
const API_BASE = import.meta.env?.VITE_API_BASE || 'http://localhost:8080';

export default {
  name: 'PlaceDetailsView',

  components: {
    ExhibitionHeader,
    InfoSection,
    TabSection,
    CourseRecommend,
    ContentDetailView,
  },

  data() {
    return {
      // 현재 ID를 저장할 변수
      currentId: null,
      // 화면 상태
      pageType: null,     // 'exhibition' | 'place'
      currentTab: 'detail',

      // 전시 상세
      exhibition: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        mainCategory: '',   // (PillTag용)
        subCategories: [],  // (HashTag용)
        gradeTag: '',    // (PillTag용)
        type: '',
        description: '',
        mainImage: 'https://via.placeholder.com/600x400',
      },
      // 이게 LocationSection에 들어갈 부분
      exhibitionInformation: {
        exhibitionLocation: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: '',
        lat: 0,
        lng: 0,
      },

      // 장소 상세
      place: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        mainCategory: '',   // (PillTag용)
        subCategories: [],  // (HashTag용)
        gradeTag: '',    // (PillTag용)
        type: '',
        description: '',
        mainImage: 'https://via.placeholder.com/600x400',
      },
      // (LocationSection이 'placeAddress'를 사용)
      placeInformation: {
        placeAddress: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: '',
        lat: 0,
        lng: 0,
      },

      // 공통
      reviews: [],
      // AI 추천 코스 결과를 담을 배열
      courseItems: [],

      // 추천 코스를 이미 로드했는지 추적하는 플래그
      hasLoadedRecommendations: false,
    };
  },

  // 컴포넌트 로드 시 훅 설정
  created() {
    // URL에서 ID 가져오기
    const id = this.$route.params.id;
    // ID를 data()에 저장
    this.currentId = id;
    // URL 경로가 place 시작인지 판별
    const isPlace = this.$route.path.startsWith('/place/');
    // 장소인 경우
    if (isPlace) {
      this.pageType = 'place';
      this.fetchPlaceData(id);
      // 전시인 경우
    } else {
      this.pageType = 'exhibition';
      this.fetchExhibitionData(id);
    }
  },

  computed: {
    // isFormValid() { ... } // (ContentDetailView가 관리)
  },

  methods: {
    /** DTO -> 프론트 상태 매핑 (Exhibition) */
    mapExhibitionDTO(dto) {
      const title = dto.exhibitionName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';       // 대분류
      const subCategory = this.$route.query.subCategoryTags ?? '';   // (중분류)
      const grade = this.$route.query.gradeTags;

      this.exhibition = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category, // PillTag
        subCategories: subCategory, // HashTag
        gradeTag: grade, // PillTag
        type: 'exhibition',
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
      };

      // LocationSection이 사용할 데이터
      this.exhibitionInformation = {
        exhibitionLocation: dto.location ?? '정보 없음',
        operationPeriod: this.formatPeriod(dto.startDate, dto.endDate),
        operationHours: dto.openingHours ?? '정보 없음',
        entranceFee: this.formatFee(dto.admissionFee),
        lat: dto.latitude,
        lng: dto.longitude,
      };

      console.log('✅ [PlaceDetailsView] mapExhibitionDTO 결과 (exhibition):', this.exhibition);
      console.log('✅ [PlaceDetailsView] mapExhibitionDTO 결과 (exhibitionInformation):', this.exhibitionInformation);

      // (리뷰/코스 데이터는 나중에 별도 API로 가져옵니다)
      this.reviews = [];
      this.courseItems = [];
    },

    /** DFile.save('PlaceDetailsView.vue');TO -> 프론트 상태 매핑 (Place) ★★★ 버그 수정 ★★★ */
    mapPlaceDTO(dto) {

      const title = dto.placeName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';       // 대분류
      const subCategory = this.$route.query.subCategoryTags ?? '';   // (중분류)
      const grade = this.$route.query.gradeTags;

      this.place = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category, // PillTag
        subCategories: subCategory, // HashTag
        gradeTag: grade, // PillTag
        type: 'place',
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
      };

      // LocationSection이 사용할 데이터 (PlaceDetailDTO.java 스펙에 맞게)
      this.placeInformation = {
        // dto.location -> dto.addressDetail
        placeAddress: dto.addressDetail ?? '정보 없음',
        // DTO에 기간 정보가 없으므로 '상시 운영' 또는 '정보 없음' 처리
        operationPeriod: this.formatPeriod(null, null),
        operationHours: dto.openingHours ?? '정보 없음',
        // ★ 수정: Place DTO의 admissionFee는 '무료' 같은 문자열(String)이므로 formatFee() 사용 안함
        entranceFee: dto.admissionFee ?? '정보 없음',
        lat: dto.latitude,
        lng: dto.longitude,
      };

      console.log('✅ [PlaceDetailsView] mapPlaceDTO 결과 (place):', this.place);
      console.log('✅ [PlaceDetailsView] mapPlaceDTO 결과 (placeInformation):', this.placeInformation);

      // (리뷰/코스 데이터는 나중에 별도 API로 가져옵니다)
      this.reviews = [];
      this.courseItems = [];
    },

    // ✨ (Helper) 날짜 포맷 함수 추가
    formatPeriod(start, end) {
      if (!start && !end) return '상시 운영';
      if (start && !end) return `${start} ~ 별도 안내시까지`;
      if (!start && end) return `~ ${end}`;
      return `${start} ~ ${end}`;
    },

    // ✨ (Helper) 요금 포맷 함수 추가 (Number -> String)
    formatFee(fee) {
      if (fee === null || fee === undefined) return '정보 없음';
      if (fee === 0) return '무료';
      return `${fee.toLocaleString('ko-KR')}원`; // 4000 -> "4,000원"
    },

    /** 전시 상세 - 백엔드 연동 */
    async fetchExhibitionData(id) {
      try {

        const res = await axios.get(`${API_BASE}/api/exhibitions`, {
          params: {
            exhibitionId: id,
          },
        });

        const dto = res.data;
        console.log('✅ [PlaceDetailsView] API 원본 응답 (exhibition dto):', dto);

        if (!dto || Object.keys(dto).length === 0) {
          console.warn('전시 데이터가 비어 있습니다.');
          return;
        }
        this.mapExhibitionDTO(dto);
      } catch (error) {
        console.error('전시 상세 조회 실패:', error);
        alert('전시 정보를 불러오지 못했습니다.');
      }
    },


    /** 장소 상세 - 백엔드 연동 ★★★ 버그 수정 ★★★ */
    async fetchPlaceData(id) {
      try {
        // API 호출
        const res = await axios.get(`${API_BASE}/api/place`, {
          params: {
            placeId: id,
          },
        });

        // DTO에 API 응답 담기
        const dto = res.data;
        console.log('✅ [PlaceDetailsView] API 원본 응답 (place dto):', dto);

        if (!dto || Object.keys(dto).length === 0) {
          console.warn('장소 데이터가 비어 있습니다.');
          return;
        }

        // 지도 정보
        this.mapPlaceDTO(dto);

      } catch (error) {
        console.error('장소 상세 조회 실패:', error);
        alert('장소 정보를 불러오지 못했습니다.');
      }

      // ★ 수정: API 호출 후 Mock 데이터를 덮어쓰면 안되므로 삭제
      // this.reviews = [ ... ];
      // this.courseItems = [ ... ];
    },

    // ✨ [추가] 자식(ContentDetailView)이 보낸 이벤트를 처리할 함수
    handleReviewSubmit(newReview) {
      // (나중에는 여기서 API로 POST 요청을 보냅니다)
      // 지금은 PlaceDetailsView의 data()에 있는 reviews 배열을 수정합니다.
      this.reviews.unshift(newReview);

      console.log('✅ [PlaceDetailsView] 새 리뷰 받음:', newReview);
      alert('후기가 성공적으로 등록되었습니다.');
    },

    // 탭 변경시 호출될 메서드
    handleTabChange(tabName) {
      this.currentTab = tabName;

      // 추천 탭을 클릭했고,
      // 아직 추천 데이터를 로드한 적이 없으면 API 호출
      if (tabName === 'recommend' && !this.hasLoadedRecommendations) {
        this.fetchRecommendedCourse();
      }
    },

    // AI 코스 API를 호출하는 메서드
    async fetchRecommendedCourse() {
      // 이미 로딩 중이거나 로드 완료됐다면 중복 실행 방지
      if (this.hasLoadedRecommendations) return;
      console.log('AI 추천 코스를 검색합니다.');

      try {
        // 백엔드에 만들어야 할 API 엔드포인트
        const apiUrl = `${API_BASE}/api/recommend/course`;

        // API에 보내야 할 파라미터
        const params = {
          type: this.pageType, // 'exhibition' 또는 'place'
          // $route 대신 data()에 저장된 ID 사용
          currentId: this.currentId,
          // 현재 페이지의 태그(쿼리) 정보를 그대로 전달
          mainCategoryTags: this.$route.query.mainCategoryTags,
          subCategoryTags: this.$route.query.subCategoryTags, // (이전 대화에서 subCategories로 키를 잡음)
          gradeTags: this.$route.query.gradeTags,
        };

        const res = await axios.get(apiUrl, { params });

        // API 응답 결과를 courseItems 데이터에 저장
        this.courseItems = res.data;
        this.hasLoadedRecommendations = true; // 로드 완료 플래그 설정

        console.log('🤖 AI 추천 코스 수신 완료:', this.courseItems);
      } catch (error) {
        console.error("AI 추천 코스 로딩 실패:", error);
        // (에러가 나도 한 번 시도했으므로 플래그를 true로 설정하여 무한 재시도 방지)
        this.hasLoadedRecommendations = true;
      }
    }
  }
}
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
  /* 이 영역에서만 스크롤이 발생하도록 합니다. */
  overflow-y: auto;
  min-height: 0;
  /* background-color: #fff; */
  /* 스크롤 영역 배경색이 필요하다면 추가 */
  padding-bottom: 40px;

  /* 스크롤바 숨기기 */
  /* Chrome, Safari, Edge 등 (웹킷 브라우저) */
  &::-webkit-scrollbar {
    display: none;
  }

  /* Firefox */
  scrollbar-width: none;

  /* IE (구형) */
  -ms-overflow-style: none;
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
