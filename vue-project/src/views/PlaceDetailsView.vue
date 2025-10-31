<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시 상세" :isFavorite="exhibition.isFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else-if="pageType === 'science_place'" pageTitle="장소 상세" :isFavorite="place.isFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else pageTitle="로딩 중..." />
    </div>

    <div class="scroll-content">

      <!--전일때-->
      <div v-if="pageType === 'exhibition'">
        <InfoSection :exhibition="exhibition" imageTag="전 태그" :mainCategory="exhibition.mainCategory"
          :subCategories="exhibition.subCategories" :gradeTag="exhibition.gradeTag" />
        <hr class="divider" />
        <TabSection :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="exhibitionInformation" :exhibition="exhibition" :isPlace="false"
            :target-id="currentId" :target-type="pageType" :current-user-id="currentUserId"
            @review-posted="handleReviewPosted" @review-deleted="handleReviewDeleted"
            :photo-review-count="exhibition.photoReviewCount" />
        </div>
        <!--코스추천-->
        <div v-else-if="currentTab === 'recommend'">
          <div v-if="isRecommending" class="recommend-loading">
            <p>🤖 AI가 코스를 생성 중입니다.</p>
            <p>잠만 기다려 주세요...</p>
          </div>
          <CourseRecommend :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
            @request-new-course="fetchRecommendedCourse" @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

      <!--장소일때-->
      <div v-else-if="pageType === 'science_place'">
        <InfoSection :exhibition="place" imageTag="장소 태그" :mainCategory="place.mainCategory"
          :subCategories="place.subCategories" :gradeTag="place.gradeTag" />
        <hr class="divider" />
        <TabSection :isPlace="true" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="placeInformation" :exhibition="place" :target-id="currentId"
            :target-type="pageType" :current-user-id="tempCurrentUserId" :isPlace="true"
            @review-posted="handleReviewPosted" @review-deleted="handleReviewDeleted"
            :photo-review-count="place.photoReviewCount" />
        </div>
        <!--코스추천-->
        <div v-else-if="currentTab === 'recommend'">
          <div v-if="isRecommending" class="recommend-loading">
            <p>🤖 AI가 코스를 생성 중입니다.</p>
            <p>잠만 기다려 주세요...</p>
          </div>
          <CourseRecommend :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
            @request-new-course="fetchRecommendedCourse" @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

      <div v-else class="loading-container">
        <p>데이터를 불러오는 중입니다...</p>
      </div>

    </div>
  </div>
</template>

<script>
import axios from '@/api/axiosSetup';

// 하위 컴포넌트들
import ExhibitionHeader from '@/components/header/ExhibitionHeader.vue';
import InfoSection from '@/components/section/InfoSection.vue';
import TabSection from '@/components/section/TabSection.vue';
import ContentDetailView from './ContentDetailView.vue';
import CourseRecommend from './CourseRecommend.vue';
// 🟢 [추가] Pinia 스토어 (로그인 상태 확인용)
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';


// API 베이스
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
  // 🟢 Options API에서 Pinia를 사용하기 위한 setup()
  setup() {
    // 1. auth 스토어를 가져옵니다.
    const authStore = useAuthStore();

    // 2. storeToRefs를 사용해 스토어의 상태(state)와 게터(getter)를
    //    반응성을 유지(reactive)하면서 가져옵니다.
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);

    // 3. setup()에서 이 값들을 반환하면,
    //    computed, methods 등 다른 옵션에서 this.isLoggedIn, this.currentUserId로 접근할 수 있습니다.
    return {
      isLoggedIn,
      currentUserId // (authStore.js의 'currentUserId' getter)
    };
  },
  data() {
    return {
      // 현재 ID를 저장할 변수
      currentId: null, // <-- 여기에 targetId를 저장
      // 화면 상태
      pageType: null,     // 'exhibition' | 'place' <-- 여기에 targetType을 저장
      currentTab: 'detail',

      // 전 상세
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
        photoReviewCount: 0,
        isFavorite: false, // 찜 상태 (DB에서 불러온 초기값)
      },
      isLoading: false, // 중복 클릭 방지용

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
        photoReviewCount: 0,
        isFavorite: false, // 찜 상태 (DB에서 불러온 초기값)
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

      // 공통 <--
      // reviews: [],

      // AI 추천 코스 결과를 담을 배열
      courseItems: [],

      // 추천 코스를 이미 로드했는지 추적하는 플래그
      hasLoadedRecommendations: false,

      // AI 추천 API 로딩 상태를 추적할 변수 추가
      isRecommending: false,
    };
  },

  // 컴포넌트 로드  훅 설정
  created() {
    // URL에서 ID 가져오기
    const id = this.$route.params.id; // url에서 id를 가져와서 targetId로 사용!
    // ID를 data()에 저장
    this.currentId = id;
    // URL 경로가 place 작인지 판별
    const isPlace = this.$route.path.startsWith('/place/'); // 1. URL 경로를 분석해서 'targetType'으로 사용
    // 장소인 경우
    if (isPlace) {
      this.pageType = 'science_place';
      this.fetchPlaceData(id);
      // 전인 경우
    } else {
      this.pageType = 'exhibition';
      this.fetchExhibitionData(id);
    }
    // (디버깅) setup에서 가져온 currentUserId가 잘 찍히는지 확인
    console.log('[PlaceDetailsView] 현재 로그인된 User ID (from Pinia):', this.currentUserId);
  },

  computed: {
    // isFormValid() { ... } // (ContentDetailView가 관리)
  },

  methods: {

    /** DTO -> 프론트 상태 매핑 (Exhibition) */
    mapExhibitionDTO(dto) {
      const title = dto.exhibitionName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';       // 대분류
      // URL 쿼리에서 원본 데이터 가져오기
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;
      // subCategoriesArray를 빈 배열로 초기화
      let subCategoriesArray = [];
      // subCategoryData가 문자열일 때만 split 실행
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } // 만약 subCategoryData가 이미 배열일 경우 처리
      else if (Array.isArray(subCategoryData)) {
        // 각 요소를 문자열로 변환하고 공백 제거 (안전 장치)
        subCategoriesArray = subCategoryData
          .map(tag => String(tag).trim())
          .filter(Boolean);
      }

      this.exhibition = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category, // PillTag
        subCategories: subCategoriesArray,
        gradeTag: grade, // PillTag
        type: dto.type ?? 'exhibition',
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
        photoReviewCount: dto.totalPhotoReviews ?? 0,
        isFavorite: dto.isLiked ?? false
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
      // URL 쿼리에서 원본 데이터 가져오기
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;
      // subCategoriesArray를 빈 배열로 초기화
      let subCategoriesArray = [];
      // subCategoryData가 문자열일 때만 split 실행
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } // 만약 subCategoryData가 이미 배열일 경우 처리
      else if (Array.isArray(subCategoryData)) {
        // 각 요소를 문자열로 변환하고 공백 제거 (안전 장치)
        subCategoriesArray = subCategoryData
          .map(tag => String(tag).trim())
          .filter(Boolean);
      }

      this.place = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category, // PillTag
        subCategories: subCategoriesArray, // HashTag
        gradeTag: grade, // PillTag
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
        photoReviewCount: dto.totalPhotoReviews ?? 0,
        isFavorite: dto.isLiked ?? false
      };

      // LocationSection이 사용할 데이터 (PlaceDetailDTO.java 스펙에 맞게)
      this.placeInformation = {
        // dto.location -> dto.addressDetail
        placeAddress: dto.addressDetail ?? '정보 없음',
        // DTO에 기간 정보가 없으므로 '상 운영' 또는 '정보 없음' 처리
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
      if (!start && !end) return '상 운영';
      if (start && !end) return `${start} ~ 별도 안내까지`;
      if (!start && end) return `~ ${end}`;
      return `${start} ~ ${end}`;
    },

    // ✨ (Helper) 요금 포맷 함수 추가 (Number -> String)
    formatFee(fee) {
      if (fee === null || fee === undefined) return '정보 없음';
      if (fee === 0) return '무료';
      return `${fee.toLocaleString('ko-KR')}원`; // 4000 -> "4,000원"
    },

    /** 전 상세 - 백엔드 연동 */
    async fetchExhibitionData(id) {
      try {

        const res = await axios.get(`${API_BASE}/api/exhibitions`, {
          params: {
            exhibitionId: id,
            userId: this.tempCurrentUserId // 로그인 연결 전 임시로
          },
        });

        const dto = res.data;
        console.log('✅ [PlaceDetailsView] API 원본 응답 (exhibition dto):', dto);

        if (!dto || Object.keys(dto).length === 0) {
          console.warn('전 데이터가 비어 있습니다.');
          return;
        }
        this.mapExhibitionDTO(dto);
      } catch (error) {
        console.error('전 상세 조회 실패:', error);
        this.$alert('전 정보를 불러오지 못했습니다.');
      }
    },
    // 찜 기능 함수
    async handleToggleFavorite() {
      // 🟢 로그인 상태 확인 (Pinia 스토어)
      if (!this.isLoggedIn) {
        this.$alert('로그인이 필요한 서비스입니다.');
        this.$router.push({ name: 'login' }); // (라우터 이름이 'login'이라고 가정)
        return;
      }

      if (this.isLoading) return;

      const isExhibition = (this.pageType === 'exhibition');
      // 찜 상태와 현재 아이템 데이터 가져오기
      let currentState = isExhibition ? this.exhibition.isFavorite : this.place.isFavorite;
      const currentItem = isExhibition ? this.exhibition : this.place;
      const userId = this.tempCurrentUserId;

      // 1. 찜 취소 (DELETE) 요청 데이터 (기존과 동일)
      // 찜 취소는 해당 아이템의 모든 '찜'을 삭제하는 것으로 통일 (이것이 UX상 가장 간단합니다)
      const deleteRequestData = {
        targetId: this.currentId,
      };

      // 2. 찜 추가 (POST) 요청 데이터 (🌟 맥락 정보 추가 🌟)
      const postRequestData = {
        targetId: this.currentId,
        targetType: this.pageType,

        // 🌟 현재 페이지의 맥락(과학영역, 학년)을 함께 전송
        mainCategory: currentItem.mainCategory,
        gradeTag: currentItem.gradeTag
      };

      try {
        if (currentState) {
          // 1. 찜 취소 (DELETE)
          // 🌟 [수정] data: deleteRequestData
          await axios.delete(`${API_BASE}/api/wishlist`, {
            data: deleteRequestData
          });
          currentState = false;
          this.$alert('찜 목록에서 삭제되었습니다.');

        } else {
          // 2. 찜 추가 (POST)
          // 🌟 postRequestData (맥락 정보가 포함된 DTO 전송)
          await axios.post(`${API_BASE}/api/wishlist`, postRequestData);
          // 요청 아이템
          JSON.stringify(console.log(postRequestData), null, 2);
          currentState = true;
          this.$alert('찜 목록에 추가되었습니다.');
        }
        // 최종 상태 반영
        if (isExhibition) {
          this.exhibition.isFavorite = currentState;
        } else {
          this.place.isFavorite = currentState;
        }

      } catch (error) {
        // 3. 에러 처리
        const status = error.response?.status;

        // 409 Conflict 에러 처리 (자동 취소)
        if (status === 409) {
          this.$alert('중복된 찜 항목입니다. 자동으로 취소합니다.');
          // 찜 버튼을 누른 의도를 존중하여, UI를 '찜한 상태'로 강제 동기화합니다.
          if (isExhibition) {
            this.exhibition.isFavorite = true;
          } else {
            this.place.isFavorite = true;
          }
        } else if (status === 403 || status === 401) { // 401/403 (권한 없음)
          this.$alert('찜하기 권한이 없습니다. 다시 로그인해 주세요.');
          // (axiosSetup에서 401을 처리하겠지만, 403을 대비)
        } else {
          console.error('찜 처리 중 에러 발생:', error);
          this.$alert('찜 처리에 실패했습니다. 다시 시도해 주세요.');
        }
      } finally {
        this.isLoading = false;
      }
    },

    // 추천 코스 저장 요청 처리
    async handleSaveRecommendedCourse(items) {
      console.log('💾 [PlaceDetailsView] 추천 코스 저장 시작...', items);
      // 🟢 로그인 상태 확인
      if (!this.isLoggedIn) {
        alert('로그인이 필요한 기능입니다.');
        this.$router.push('/login'); // 로그인 페이지로 이동
        return;
      }

      if (!items || items.length === 0) {
        console.warn('저장할 추천 코스 아이템이 없습니다.');
        // this.primaryLoading = false;
        this.$alert('저장할 코스 정보가 없습니다.'); // 사용자 알림
        return;
      }

      try {
        // 1. 백엔드로 보낼 데이터 준비
        const currentItemData = (this.pageType === 'place') ? this.place : this.exhibition;
        const scheduleName = `AI 추천: ${currentItemData.title || '코스'}`; // 스케줄 이름 생성
        const sourceId = this.currentId; // 현재 보고 있는 상세 페이지의 ID

        // 프론트엔드 items 배열 -> 백엔드 DTO 형식으로 변환
        const backendItems = items.map(item => ({
          exhibitionId: this.pageType !== 'place' ? item.id : null,      // 프론트엔드 id -> exhibitionId
          placeId: this.pageType === 'place' ? item.id : null,
          sequence: item.number,  // 프론트엔드 number -> sequence
          itemType: item.type === 'exhibition' ? 'exhibition' : 'science_place' // 아이템 타입 설정 (백엔드와 일치 필요)
          // ❗️ 중요: item.type이 백엔드 Enum/String과 일치하는지 확인 필요
          // 예: 백엔드가 'science_place'만 받는다면 그에 맞게 조정
        }));

        // 최종 요청 페이로드
        const requestDto = {
          scheduleName: scheduleName,
          sourceId: sourceId,
          sourceCourseType: this.pageType === 'place' ? 'ai_course' : 'inner_course', // 전 추천 코스이면 'inner_course', 장소 추천 코스이면 'ai_course'
          items: backendItems
        };

        console.log('💾 [PlaceDetailsView] API 요청 데이터:', JSON.stringify(requestDto, null, 2));

        // 2. API 호출 (axios 사용)
        const response = await axios.post(`${API_BASE}/api/schedules/save-recommended`, requestDto);

        // 3. 성공 처리
        if (response.status === 200) {
          console.log('✅ [PlaceDetailsView] 추천 코스 저장 성공!');
          this.$alert('추천 코스가 "관심 코스"에 성공적으로 저장되었습니다.'); // 성공 메지
          // TODO: (선택) 저장 후 사용자를 마이페이지나 다른 곳으로 이동킬 수 있습니다.
          // 예: this.$router.push('/mypage/likes');
        } else {
          // 200 외의 응답 처리 (필요)
          console.error('⚠️ [PlaceDetailsView] 추천 코스 저장 응답 오류:', response);
          this.$alert(`코스 저장 중 문제가 발생했습니다: ${response.data?.message || response.statusText}`);
        }

      } catch (error) {
        // 4. 실패 처리
        // (401 오류는 axiosSetup.js가 자동으로 처리하므로, 여기서는 403, 500 등 다른 오류를 처리)
        console.error('💥 [PlaceDetailsView] 추천 코스 저장 API 호출 실패:', error);
        this.$alert(`코스 저장 중 오류가 발생했습니다: ${error.response?.data || error.message}`);
        if (error.response?.status === 403) {
          alert('접근 권한이 없습니다.');
        } else {
          alert(`코스 저장 중 오류가 발생했습니다: ${error.response?.data || error.message}`);
        }
      } finally {
        // 5. 로딩 상태 해제
      }
    },

    /** 장소 상세 - 백엔드 연동 ★★★ 버그 수정 ★★★ */
    async fetchPlaceData(id) {
      try {
        // API 호출
        const res = await axios.get(`${API_BASE}/api/place`, {
          params: {
            placeId: id,
            userId: this.tempCurrentUserId
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
        this.$alert('장소 정보를 불러오지 못했습니다.');
      }

      // ★ 수정: API 호출 후 Mock 데이터를 덮어쓰면 안되므로 삭제
      // this.reviews = [ ... ];
      // this.courseItems = [ ... ];
    },

    refreshData() {
      console.log(`리뷰 변경 감지 : 부모 데이터 새로고침`);
      if (this.pageType === 'exhibition') {
        this.fetchExhibitionData(this.currentId);
      } else if (this.pageType === 'science_place') { // 'place' 대신 정확한 'science_place' 사용
        this.fetchPlaceData(this.currentId);
      }
    },

    handleReviewPosted() {
      this.refreshData();
    },

    // 리뷰 삭제 모달 -> 삭제 카운트 감소
    handleReviewDeleted() {
      this.refreshData();
    },

    // 탭 변경 호출될 메서드
    handleTabChange(tabName) {
      this.currentTab = tabName;

      // 탭을 '처음' 클릭했고, 아직 추천 데이터를 로드한 적이 없으면 API 호출
      if (tabName === 'recommend' && !this.hasLoadedRecommendations) {
        this.fetchRecommendedCourse();
      }
    },
    // '새로운 추천 받기 버튼'이 이 함수를 직접 호출
    async fetchRecommendedCourse() {
      console.log('🤖 AI 추천 코스를 검색합니다...');

      // 로딩 상태를 true로 변경
      this.isRecommending = true;

      await this.$nextTick();

      try {
        // 1. AI 추천 API 호출 (2번, 3번... 항목들)
        const apiUrl = `${API_BASE}/api/recommend/course`;
        const params = {
          type: this.pageType,
          currentId: this.currentId,
          mainCategoryTags: this.$route.query.mainCategoryTags,
          subCategoryTags: this.$route.query.subCategoryTags,
          gradeTags: this.$route.query.gradeTags,
        };
        const res = await axios.get(apiUrl, { params });
        const aiRecommendedDtos = res.data; // (백엔드가 보낸 DTO 리스트)

        // 2. "1번 항목" (현재 페이지 장소) 데이터 준비
        // (created()에서 이미 불러온 this.place 또는 this.exhibition 객체 활용)
        const currentItemData = (this.pageType === 'place') ? this.place : this.exhibition;
        const currentItemInfo = (this.pageType === 'place') ? this.placeInformation : this.exhibitionInformation;

        // 3. "1번 항목"을 카드 형식으로 포맷
        const currentItemFormatted = {
          id: this.currentId, // 고유 ID
          number: 1,            // 1번으로 고정
          imageUrl: currentItemData.mainImage || 'https://via.placeholder.com/60x60',
          title: currentItemData.title,
          subject: currentItemData.mainCategory,
          grade: currentItemData.gradeTag,
          hashtags: Array.isArray(currentItemData.subCategories) ? currentItemData.subCategories : [currentItemData.subCategories].filter(Boolean),
          type: currentItemData.type,
          place: currentItemInfo.placeAddress || currentItemInfo.exhibitionLocation,
          // 지도(CourseMap)를 위한 1번 항목의 좌표
          lat: currentItemInfo.lat,
          lng: currentItemInfo.lng,
        };

        // 4. "2번, 3번..." (AI 추천 목록)을 카드 형식으로 포맷
        const aiItemsFormatted = aiRecommendedDtos.map((item, index) => {
          // (item = 백엔드 DTO: { placeId, placeName, imageUrl, address, latitude, longitude ... })
          return {
            id: item.placeId,
            number: index + 2,     // [!!] 2번부터 작
            imageUrl: item.imageUrl || 'https://via.placeholder.com/60x60',
            title: item.placeName,
            subject: item.subjectName,
            grade: item.gradeName,
            hashtags: item.hashtags,
            place: item.address || '주소 정보 없음',
            // 지도(CourseMap)를 위한 2,3,4번 항목의 좌표
            lat: item.latitude,
            lng: item.longitude,
            type: item.type
          };
        });

        // 1번 항목과 (2,3,4..) 항목 리스트를 합쳐서 최종 저장
        this.courseItems = [currentItemFormatted, ...aiItemsFormatted];
        this.hasLoadedRecommendations = true; // 에러  무한 재도 방지
        console.log('🤖 AI 추천 코스 수신 완료 (1번 + 추천 리스트):', this.courseItems);

      } catch (error) {
        console.error("AI 추천 코스 로딩 실패:", error);
        // 에러가 나도 로드는 되었다고 처리해야, 탭 이동 후 다 눌렀을 때 재도 가능
        this.hasLoadedRecommendations = true;
      } finally {
        this.isRecommending = false;
        console.log('🏁 fetchRecommendedCourse 완료. isRecommending:', this.isRecommending);
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

/* [!!] 5. AI 추천 로딩 스타일 추가 */
.recommend-loading {
  padding: 60px 20px;
  text-align: center;
  color: #555;
  font-size: 1.1rem;
  font-weight: 500;
  background-color: #fff;
  /* 또는 f7f7f7 */
}

.recommend-loading p:last-child {
  font-size: 0.9rem;
  color: #888;
  margin-top: 8px;
}
</style>
