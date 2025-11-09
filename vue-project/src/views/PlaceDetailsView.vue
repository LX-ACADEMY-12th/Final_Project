<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시 상세" :isFavorite="computedIsFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else-if="pageType === 'science_place'" pageTitle="장소 상세" :isFavorite="computedIsFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else pageTitle="로딩 중..." />
    </div>

    <div class="scroll-content">

      <!--전시일때-->
      <div v-if="pageType === 'exhibition'">
        <InfoSection :exhibition="exhibition" imageTag="전시 태그" :mainCategory="exhibition.mainCategory"
          :subCategories="exhibition.subCategories" :gradeTag="exhibition.gradeTag"
          @authenticate-visit="handleVisitAuthentication" />
        <hr class="divider" />
        <TabSection :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="exhibitionInformation" :exhibition="exhibition" :isPlace="false"
            :target-id="currentId" :target-type="pageType" @review-posted="handleReviewPosted"
            @review-deleted="handleReviewDeleted" :photo-review-count="exhibition.photoReviewCount" />
        </div>
        <!--코스추천-->
        <div v-else-if="currentTab === 'recommend'">
          <!-- 개선된 AI 추천 로딩 -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">
              <!-- AI 아이콘 섹션 -->
              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <span class="ai-icon">🤖</span>
                  <div class="pulse-effect"></div>
                </div>
              </div>

              <!-- 메시지 섹션 -->
              <div class="loading-message">
                <h3>AI가 맞춤 코스를 생성 중입니다</h3>
                <p class="sub-message">{{ loadingMessages[currentMessageIndex] }}</p>
              </div>

              <!-- 진행 단계 -->
              <div class="progress-steps">
                <div class="step-item" v-for="(step, index) in progressSteps" :key="index"
                  :class="{ active: currentStepIndex >= index, completed: currentStepIndex > index }">
                  <div class="step-dot">
                    <span v-if="currentStepIndex > index">✓</span>
                  </div>
                  <span class="step-label">{{ step }}</span>
                </div>
              </div>

              <!-- 스켈레톤 카드 -->
              <div class="skeleton-cards">
                <div v-for="n in 3" :key="n" class="skeleton-card" :style="{ animationDelay: `${n * 0.1}s` }">
                  <div class="card-number">{{ n + 1 }}</div>
                  <div class="card-content">
                    <div class="skeleton-image"></div>
                    <div class="skeleton-info">
                      <div class="skeleton-title"></div>
                      <div class="skeleton-location"></div>
                      <div class="skeleton-tags">
                        <span class="skeleton-tag"></span>
                        <span class="skeleton-tag"></span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 팁 메시지 -->
              <div class="loading-tip">
                <span class="tip-emoji">💡</span>
                <span class="tip-text">{{ tips[currentTipIndex] }}</span>
              </div>
            </div>
          </div>

          <!-- 실제 추천 결과 -->
          <CourseRecommend v-else :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
            @request-new-course="fetchRecommendedCourse" @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

      <!--장소일때-->
      <div v-else-if="pageType === 'science_place'">
        <InfoSection :exhibition="place" imageTag="장소 태그" :mainCategory="place.mainCategory"
          :subCategories="place.subCategories" :gradeTag="place.gradeTag"
          @authenticate-visit="handleVisitAuthentication" />
        <hr class="divider" />
        <TabSection :isPlace="true" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="placeInformation" :exhibition="place" :target-id="currentId"
            :target-type="pageType" :isPlace="true" @review-posted="handleReviewPosted"
            @review-deleted="handleReviewDeleted" :photo-review-count="place.photoReviewCount" />
        </div>
        <!--코스추천-->
        <div v-else-if="currentTab === 'recommend'">
          <!-- 개선된 AI 추천 로딩 (장소도 동일) -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">
              <!-- AI 아이콘 섹션 -->
              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <span class="ai-icon">🤖</span>
                  <div class="pulse-effect"></div>
                </div>
              </div>

              <!-- 메시지 섹션 -->
              <div class="loading-message">
                <h3>AI가 맞춤 코스를 생성 중입니다</h3>
                <p class="sub-message">{{ loadingMessages[currentMessageIndex] }}</p>
              </div>

              <!-- 진행 단계 -->
              <div class="progress-steps">
                <div class="step-item" v-for="(step, index) in progressSteps" :key="index"
                  :class="{ active: currentStepIndex >= index, completed: currentStepIndex > index }">
                  <div class="step-dot">
                    <span v-if="currentStepIndex > index">✓</span>
                  </div>
                  <span class="step-label">{{ step }}</span>
                </div>
              </div>

              <!-- 스켈레톤 카드 -->
              <div class="skeleton-cards">
                <div v-for="n in 3" :key="n" class="skeleton-card" :style="{ animationDelay: `${n * 0.1}s` }">
                  <div class="card-number">{{ n + 1 }}</div>
                  <div class="card-content">
                    <div class="skeleton-image"></div>
                    <div class="skeleton-info">
                      <div class="skeleton-title"></div>
                      <div class="skeleton-location"></div>
                      <div class="skeleton-tags">
                        <span class="skeleton-tag"></span>
                        <span class="skeleton-tag"></span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 팁 메시지 -->
              <div class="loading-tip">
                <span class="tip-emoji">💡</span>
                <span class="tip-text">{{ tips[currentTipIndex] }}</span>
              </div>
            </div>
          </div>

          <!-- 실제 추천 결과 -->
          <CourseRecommend v-else :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
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

import eventBus from '@/utils/eventBus'; // 💡 [추가] 글로벌 알림용


// API 베이스
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
  // 🟢 1. [추가] 이 computed 섹션을 setup() 함수 뒤, data() 앞 등에 추가하세요.
  computed: {
    // 💡 :isFavorite prop에 전달할 최종 찜 상태
    computedIsFavorite() {
      // 💡 data()에 있는 isWished 변수를 사용
      return this.isWished;
    }
  },

  data() {
    return {
      // 현재 ID를 저장할 변수
      currentId: null, // <-- 여기에 targetId를 저장
      // 화면 상태
      pageType: null,     // 'exhibition' | 'place' <-- 여기에 targetType을 저장
      currentTab: 'detail',
      isWished: false, // 찜 상태를 별도로 관리할 '신뢰할 수 있는' 변수

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
        photoReviewCount: 0,
        exhibitionList: [],
        isVisited: false
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
        isVisited: false
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

      // AI 로딩 애니메이션 관련
      progressSteps: ['데이터 분석', '유사 장소 탐색', '경로 최적화', '코스 완성'],
      currentStepIndex: 0,
      stepInterval: null,

      loadingMessages: [
        '현재 전시/장소의 특징을 분석하고 있어요',
        '비슷한 테마의 장소들을 찾고 있어요',
        '최적의 이동 경로를 계산하고 있어요',
        '추천 코스를 마무리하고 있어요'
      ],
      currentMessageIndex: 0,

      tips: [
        'AI는 평점과 리뷰를 기반으로 추천해드려요',
        '생성된 코스는 관심 코스에 저장할 수 있어요',
        '날씨와 시간대를 고려한 추천을 제공합니다'
      ],
      currentTipIndex: 0,
      tipInterval: null,
    };
  },

  // 컴포넌트 로드  훅 설정
  created() {
    // URL에서 ID 가져오기
    const id = this.$route.params.id; // url에서 id를 가져와서 targetId로 사용!
    // ID를 data()에 저장
    this.currentId = id;
    // URL 경로가 place 인지 판별
    const isPlace = this.$route.path.startsWith('/place/'); // 1. URL 경로를 분석해서 'targetType'으로 사용
    this.pageType = isPlace ? 'science_place' : 'exhibition'

    console.log(`created: 이미 User ID (${this.currentUserId}) 있음. 즉시 데이터 로드`);
    // 장소인 경우
    if (isPlace) {
      this.fetchPlaceData(id);
      // 전시인 경우
    } else {
      this.fetchExhibitionData(id);
    }

    // setup에서 가져온 currentUserId가 잘 찍히는지 확인
    console.log('[PlaceDetailsView] 현재 로그인된 User ID (from Pinia):', this.currentUserId);
  },

  watch: {
    // currentId 대신, $route.params.id 감시
    '$route.params.id'(newId) {
      if (newId) {
        this.currentId = newId;
        const isPlace = this.$route.path.startsWith('/place/');
        this.pageType = isPlace ? 'science_place' : 'exhibition';

        // 🟢 [수정] 로그인 여부와 관계없이 무조건 데이터를 로드합니다.
        console.log(`watch($route.params.id): 데이터 로드 (User ID: ${this.currentUserId ?? '로그아웃'})`);
        if (isPlace) {
          this.fetchPlaceData(newId);
        } else {
          this.fetchExhibitionData(newId);
        }
      }
    },
    currentUserId(newUserId, oldUserId) {
      // (falsey(null/undefined) -> truthy('28'))가 되고, ID가 이미 있다면
      if (newUserId && !oldUserId && this.currentId) {
        console.log(`User ID 감지 (${newUserId}), '찜 상태만' 새로고침`);

        // 🟢 전체 데이터를 다시 불러오는 대신, '찜 상태'만 새로고침
        this.fetchWishStatus();

      } else if (!newUserId && oldUserId) {
        // 🟢 5. [신규] 로그아웃 감지 시 찜 상태 false로 초기화
        this.isWished = false;
      }
    }
  },

  beforeUnmount() {
    this.clearLoadingIntervals();
  },

  methods: {

    /** DTO -> 프론트 상태 매핑 (Exhibition) */
    mapExhibitionDTO(dto) {
      const title = dto.exhibitionHallName ?? '제목 없음';
      // URL 쿼리에서 원본 데이터 가져오기
      const category = this.$route.query.mainCategoryTags ?? '';       // 대분류
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
      }
      // 만약 subCategoryData가 이미 배열일 경우 처리
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
        exhibitionList: dto.exhibitionList,
        isVisited: dto.isVisited ?? false
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
        type: dto.type ?? 'science_place',
        isVisited: dto.isVisited ?? false
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
      if (!start && !end) return '상시 운영';
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


    // 🟢 찜 상태만 별도로 조회하는 함수
    async fetchWishStatus() {
      // targetId, targetType이 아직 없거나, 로그아웃 상태면(currentUserId가 없으면) 실행 안함
      if (!this.currentId || !this.pageType || !this.currentUserId) {
        this.isWished = false;
        return;
      }

      try {
        // 백엔드에 새로 만든 API 호출
        // ⭐️ GET /api/wishlist/exhibition/123/status
        const res = await axios.get(
          `/api/wishlist/${this.pageType}/${this.currentId}/status`
        );

        // ⭐️ 응답 {"isWished": true} 에서 값을 꺼내 data의 isWished에 저장
        this.isWished = res.data.isWished;
        console.log(`✅ [fetchWishStatus] 찜 상태 갱신: ${this.isWished}`);

      } catch (error) {
        console.error('찜 상태 조회 실패:', error);
        // ⭐️ 실패 시에도 false로 초기화 (중요)
        this.isWished = false;
      }
    },

    /** 전시 상세 정보 가져오기 */
    async fetchExhibitionData(id) {
      try {
        const res = await axios.get(`/api/exhibitions`, {
          params: {
            exhibitionId: id,
            // pinia 스토어 userId를 파라미터로 추가
            userId: this.currentUserId,
            mainCategoryTags: this.$route.query.mainCategoryTags ?? '',       // 대분류
            gradeTags: this.$route.query.gradeTags
          },
        });

        const dto = res.data;
        console.log('✅ [PlaceDetailsView] API 원본 응답 (exhibition dto):', dto);

        if (!dto || Object.keys(dto).length === 0) {
          console.warn('전시 데이터가 비어 있습니다.');
          return;
        }
        this.mapExhibitionDTO(dto);

        // 🟢 찜 상태를 별도로 갱신
        await this.fetchWishStatus();

      } catch (error) {
        console.error('전시 상세 조회 실패:', error);

        eventBus.emit('show-global-alert', {
          message: '전시 정보를 불러오지 못했습니다.',
          type: 'error'
        });
      }
    },

    // 찜 기능 함수
    async handleToggleFavorite() {
      // 🟢 로그인 상태 확인 (Pinia 스토어)
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      if (this.isLoading) return;

      const isExhibition = (this.pageType === 'exhibition');
      // 🟢 신뢰할 수 있는 'isWished' data를 기준으로 삼음
      let currentState = this.isWished;
      const currentItem = isExhibition ? this.exhibition : this.place;

      const requestData = {
        targetId: this.currentId,
        targetType: this.pageType,
        mainCategory: currentItem.mainCategory,
        gradeTag: currentItem.gradeTag
      };

      try {
        if (currentState) {
          // 1. 찜 취소 (DELETE)
          // 🌟 data: deleteRequestData
          await axios.delete(`/api/wishlist`, {
            data: requestData
          });
          this.isWished = false;
          eventBus.emit('show-global-alert', {
            message: '찜 목록에서 삭제되었습니다.',
            type: 'success'
          });

        } else {
          // 2. 찜 추가 (POST)
          // 🌟 postRequestData (맥락 정보가 포함된 DTO 전송)
          await axios.post(`/api/wishlist`, requestData);
          // 요청 아이템
          JSON.stringify(console.log(requestData), null, 2);
          this.isWished = true;
          eventBus.emit('show-global-alert', {
            message: '찜 목록에 추가되었습니다.',
            type: 'success'
          });
        }
      } catch (error) {
        // 3. 에러 처리
        const status = error.response?.status;

        // 409 Conflict 에러 처리 (자동 취소)
        if (status === 409) {
          eventBus.emit('show-global-alert', {
            message: '중복된 찜 항목입니다. 자동으로 취소합니다.',
            type: 'error'
          });
          try {
            // DELETE 요청 재시도 (취소) - 🌟 [수정] data 속성 사용 🌟
            await axios.delete(`/api/wishlist`, {
              data: requestData
            });
            // 🟢 11. [수정] 409 롤백 시에도 isWished 사용
            this.isWished = false;
            eventBus.emit('show-global-alert', {
              message: '찜이 취소되었습니다.',
              type: 'success'
            });

          } catch (deleteError) {
            // DELETE 재시도 실패 시
            console.error('409 후 찜 취소 실패:', deleteError);
            eventBus.emit('show-global-alert', {
              message: '찜 상태 동기화에 실패했습니다. (다음 클릭 시 취소됩니다.)',
              type: 'error'
            });
          }
        }
        // 403 Forbidden 에러 처리 (권한 문제)
        else if (status === 403) {
          eventBus.emit('show-global-alert', {
            message: '로그인이 필요하거나 권한이 없습니다.',
            type: 'error'
          });
        }
        // 그 외 에러 처리
        else {
          console.error('찜 처리 중 에러 발생:', error);
          eventBus.emit('show-global-alert', {
            message: '찜 처리에 실패했습니다. 다시 시도해 주세요.',
            type: 'error'
          });
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
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      if (!items || items.length === 0) {
        console.warn('저장할 추천 코스 아이템이 없습니다.');
        // this.primaryLoading = false;
        eventBus.emit('show-global-alert', {
          message: '저장할 코스 정보가 없습니다.',
          type: 'error'
        });
        return;
      }

      try {
        // 1. 백엔드로 보낼 데이터 준비
        const currentItemData = (this.pageType === 'science_place') ? this.place : this.exhibition;
        const scheduleName = `AI 추천: ${currentItemData.title || '코스'}`; // 스케줄 이름 생성
        const sourceId = this.currentId; // 현재 보고 있는 상세 페이지의 ID

        // 프론트엔드 items 배열 -> 백엔드 DTO 형식으로 변환
        const backendItems = items.map(item => ({
          exhibitionId: this.pageType !== 'science_place' ? item.id : null,      // 프론트엔드 id -> exhibitionId
          placeId: this.pageType === 'science_place' ? item.id : null,
          sequence: item.number,  // 프론트엔드 number -> sequence
          itemType: item.type === 'exhibition' ? 'exhibition' : 'science_place', // 아이템 타입 설정 (백엔드와 일치 필요)
          // [스냅샷] 추가
          categoryName: item.subject,
          gradeName: item.grade,
          subCategories: item.hashtags
        }));

        // 최종 요청 페이로드
        const requestDto = {
          scheduleName: scheduleName,
          sourceId: sourceId,
          sourceCourseType: this.pageType === 'science_place' ? 'ai_course' : 'inner_course',
          items: backendItems,
          userId: this.currentUserId // 여기에 userId를 추가하세요
        };


        console.log('💾 [PlaceDetailsView] API 요청 데이터:', JSON.stringify(requestDto, null, 2));

        // 2. API 호출 (axios 사용)
        const response = await axios.post(`/api/schedules/save-recommended`, requestDto);

        // 3. 성공 처리
        if (response.status === 200) {
          console.log('✅ [PlaceDetailsView] 추천 코스 저장 성공!');
          eventBus.emit('show-global-alert', {
            message: '추천 코스가 "관심 코스"에 성공적으로 저장되었습니다.',
            type: 'success'
          }); // 성공 메지
          // TODO: (선택) 저장 후 사용자를 마이페이지나 다른 곳으로 이동킬 수 있습니다.
          // 예: this.$router.push('/mypage/likes');
        } else {
          // 200 외의 응답 처리 (필요)
          console.error('⚠️ [PlaceDetailsView] 추천 코스 저장 응답 오류:', response);
          eventBus.emit('show-global-alert', {
            message: `코스 저장 중 문제가 발생했습니다: ${response.data?.message || response.statusText}`,
            type: 'error'
          });
        }

      } catch (error) {
        console.error('💥 [PlaceDetailsView] 추천 코스 저장 API 호출 실패:', error);

        if (error.response?.status === 403) {
          eventBus.emit('show-global-alert', {
            message: '접근 권한이 없습니다.',
            type: 'error'
          });
        } else {
          eventBus.emit('show-global-alert', {
            message: `코스 저장 중 오류가 발생했습니다: ${error.response?.data || error.message}`,
            type: 'error'
          });
        }
      } finally {
        // 5. 로딩 상태 해제
      }

    },

    /** 장소 상세 - 백엔드 연동 **/
    async fetchPlaceData(id) {
      try {
        // API 호출
        const res = await axios.get(`api/place`, {
          params: {
            placeId: id,
            // pinia 스토어의 Id를 파라미터로 추가
            userId: this.currentUserId
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

        // 🟢 8. [추가] 찜 상태를 별도로 갱신
        await this.fetchWishStatus();

      } catch (error) {
        console.error('장소 상세 조회 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '장소 정보를 불러오지 못했습니다.',
          type: 'error'
        });
      }
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

    // 로딩 애니메이션 시작
    startLoadingAnimation() {
      // 진행 단계 애니메이션
      this.currentStepIndex = 0;
      this.currentMessageIndex = 0;

      this.stepInterval = setInterval(() => {
        if (this.currentStepIndex < this.progressSteps.length - 1) {
          this.currentStepIndex++;
          this.currentMessageIndex++;
        }
      }, 1200);

      // 팁 로테이션
      this.currentTipIndex = 0;
      this.tipInterval = setInterval(() => {
        this.currentTipIndex = (this.currentTipIndex + 1) % this.tips.length;
      }, 2500);
    },

    // 로딩 애니메이션 정리
    clearLoadingIntervals() {
      if (this.stepInterval) {
        clearInterval(this.stepInterval);
        this.stepInterval = null;
      }
      if (this.tipInterval) {
        clearInterval(this.tipInterval);
        this.tipInterval = null;
      }
    },

    // '새로운 추천 받기 버튼'이 이 함수를 직접 호출
    async fetchRecommendedCourse() {
      console.log('🤖 AI 추천 코스를 검색합니다...');

      // 로딩 상태를 true로 변경하고 애니메이션 시작
      this.isRecommending = true;
      this.startLoadingAnimation();

      await this.$nextTick();

      try {
        // 1. AI 추천 API 호출 (2번, 3번... 항목들)
        const apiUrl = `/api/recommend/course`;
        const params = {
          type: this.pageType,
          currentId: this.currentId,
          mainCategoryTags: this.$route.query.mainCategoryTags,
          subCategoryTags: this.$route.query.subCategoryTags,
          gradeTags: this.$route.query.gradeTags,
        };

        // 최소 로딩 시간 보장 (UX 개선)
        const [res] = await Promise.all([
          axios.get(apiUrl, { params }),
          new Promise(resolve => setTimeout(resolve, 3500)) // 최소 3.5초
        ]);

        const aiRecommendedDtos = res.data; // (백엔드가 보낸 DTO 리스트)

        // 2. "1번 항목" (현재 페이지 장소) 데이터 준비
        // (created()에서 이미 불러온 this.place 또는 this.exhibition 객체 활용)
        const currentItemData = (this.pageType === 'science_place') ? this.place : this.exhibition;
        const currentItemInfo = (this.pageType === 'science_place') ? this.placeInformation : this.exhibitionInformation;


        // 3. "1번 항목"을 카드 형식으로 포맷
        const currentItemFormatted = {
          id: this.currentId,
          number: 1,
          imageUrl: currentItemData.mainImage || 'https://via.placeholder.com/60x60',
          title: currentItemData.title || '제목 없음',  // ← null 체크 추가
          subject: currentItemData.mainCategory || '분류 없음',  // ← null 체크 추가
          grade: currentItemData.gradeTag || '학년 정보 없음',  // ← null 체크 추가
          hashtags: Array.isArray(currentItemData.subCategories)
            ? currentItemData.subCategories
            : (currentItemData.subCategories ? [currentItemData.subCategories] : []),  // ← 예외 처리
          type: currentItemData.type,
          place: currentItemInfo.placeAddress || currentItemInfo.exhibitionLocation || '주소 정보 없음',  // ← null 체크 추가
          exhibitionList: currentItemData.exhibitionList,
          lat: currentItemInfo.lat || 0,
          lng: currentItemInfo.lng || 0,
        };


        // 4. "2번, 3번..." (AI 추천 목록)을 카드 형식으로 포맷
        const aiItemsFormatted = aiRecommendedDtos.map((item, index) => {
          // (item = 백엔드 DTO: { placeId, placeName, imageUrl, address, latitude, longitude ... })
          return {
            id: item.placeId,
            number: index + 2,
            imageUrl: item.imageUrl || 'https://via.placeholder.com/60x60',
            title: item.placeName,
            subject: item.subjectName,
            grade: item.gradeName,
            hashtags: item.hashtags,
            place: item.address || '주소 정보 없음',
            exhibitionList: item.exhibitionList,
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

        eventBus.emit('show-global-alert', {
          message: 'AI 추천 코스를 불러오는데 실패했습니다. 다시 시도해주세요.',
          type: 'error'
        });
      } finally {
        // 애니메이션 정리 및 로딩 종료
        setTimeout(() => {
          this.clearLoadingIntervals();
          this.isRecommending = false;
          this.currentStepIndex = 0;
          this.currentMessageIndex = 0;
          console.log('🏁 fetchRecommendedCourse 완료. isRecommending:', this.isRecommending);
        }, 300);
      }
    },

    async handleVisitAuthentication() {
      console.log('PlaceDetailView: 신호 받음! 인증을 시작합니다.');
      try {
        // A. [핵심] Pinia 스토어에서 가져온 ID 사용
        const userId = this.currentUserId;
        // B. [방어 코드] 로그인이 안 되어있으면 중단
        // (setup()에서 isLoggedIn도 가져왔으므로 this.isLoggedIn 사용 가능)
        if (!this.isLoggedIn) {
          console.warn('스탬프 인증 실패: 로그인이 필요합니다.');
          // (기존에 사용하시던 eventBus 로직 재활용)
          eventBus.emit('show-global-confirm', {
            message: '로그인이 필요한 기능입니다.',
            onConfirm: () => {
              this.$router.push({ name: 'login' });
            }
          });
          return; // 함수 종료
        }
        // C. [수정] data()에 이미 저장된 pageType과 currentId 사용
        // (created 훅에서 이미 'science_place' 또는 'exhibition'으로 설정해 둠)
        const targetType = this.pageType;
        const targetId = this.currentId;
        if (!targetType || !targetId) {
          throw new Error('인증 대상(targetId/targetType)을 식별할 수 없습니다.');
        }
        // D. GPS 좌표 가져오기
        console.log('PlaceDetailView: GPS 좌표를 요청합니다...');
        const coords = await this.getUserCoordinates(); // (아래에 추가한 헬퍼 함수)
        // E. 백엔드에 보낼 DTO 준비
        const requestDTO = {
          userId: userId,
          targetType: targetType,
          targetId: targetId,
          latitude: coords.latitude,
          longitude: coords.longitude
        };
        // F. API 호출
        console.log('PlaceDetailView: 스탬프 인증 API 호출', requestDTO);
        const response = await axios.post('/api/stamps', requestDTO);
        // G. 성공 처리
        eventBus.emit('show-global-alert', {
          message: '스탬프 획득 성공!',
          type: 'success'
        });
        console.log('인증 성공:', response.data);
        //
        // :아래를_가리키는_손_모양: [핵심!] UI 갱신 코드 추가
        //
        if (this.pageType === 'exhibition') {
          this.exhibition.isVisited = true;
        } else if (this.pageType === 'science_place') {
          this.place.isVisited = true;
        }
      } catch (error) {
        // H. 실패 처리
        // (백엔드 400 에러, GPS 권한 거부 에러 등)
        const errorMessage = error.response?.data?.error || error.response?.data || error.message;
        if (String(errorMessage).includes('GPS')) {
          alert(`GPS 오류: ${errorMessage}`);
        } else {
          eventBus.emit('show-global-alert', {
            message: `${errorMessage}`,
            type: 'error'
          });
          //alert(`인증 실패: ${errorMessage}`);
        }
        console.error('스탬프 인증 중 오류:', error);
      }
    },
    /**
     * [필수 수정] (헬퍼 함수) Geolocation API - http://localhost 테스트용
     * (임시로 고정된 좌표를 0.5초 뒤에 반환)
     */
    getUserCoordinates() {
      console.log('GPS: http://xn--localhost-js61bn11a 임시 좌표를 사용합니다.');
      // 1. 여기에 테스트할 임시 좌표를 입력하세요.
      // (DB에 있는 장소 근처의 '가까운' 좌표로 설정하세요)
      const DEMO_LOCATION = {
        latitude: 36.6448020,  // (예시) 인증 '성공' 테스트용 위도
        longitude: 127.4714750 // (예시) 인증 '성공' 테스트용 경도
      };
      // (주의!) MapView에서는 'lat', 'lng'를 썼을 수 있지만
      // 백엔드 DTO는 'latitude', 'longitude'를 쓰므로 꼭 이 이름으로 맞춰야 합니다.
      return new Promise((resolve) => {
        // 2. 실제 GPS가 좌표를 가져오는 것처럼 0.5초 딜레이 (UX 테스트용)
        setTimeout(() => {
          console.log('GPS 좌표 획득 성공 (임시)', DEMO_LOCATION);
          resolve(DEMO_LOCATION);
        }, 500); // 0.5초
      });
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

/* === 개선된 AI 추천 로딩 스타일 === */
.recommend-loading-container {
  background-color: #ffffff;
  min-height: 500px;
  padding: 40px 20px;
}

.loading-content {
  max-width: 500px;
  margin: 0 auto;
}

/* AI 아이콘 섹션 */
.ai-icon-section {
  text-align: center;
  margin-bottom: 30px;
}

.ai-icon-wrapper {
  position: relative;
  display: inline-block;
}

.ai-icon {
  font-size: 48px;
  display: inline-block;
  animation: gentle-bounce 2s ease-in-out infinite;
}

@keyframes gentle-bounce {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

.pulse-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 70px;
  height: 70px;
  border: 2px solid rgba(103, 58, 183, 0.3);
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
}

@keyframes pulse {
  0% {
    width: 70px;
    height: 70px;
    opacity: 0.8;
  }

  100% {
    width: 100px;
    height: 100px;
    opacity: 0;
  }
}

/* 메시지 섹션 */
.loading-message {
  text-align: center;
  margin-bottom: 35px;
}

.loading-message h3 {
  color: #333;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.sub-message {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
  margin: 0;
  min-height: 20px;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

/* 진행 단계 */
.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  padding: 0 10px;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 15px;
  left: 10%;
  right: 10%;
  height: 2px;
  background: #e0e0e0;
  z-index: 0;
}

.step-item {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.step-item.active {
  opacity: 1;
}

.step-dot {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f5f5f5;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s ease;
  font-size: 12px;
  color: #673ab7;
  font-weight: bold;
}

.step-item.active .step-dot {
  background: #fff;
  border-color: #673ab7;
  animation: scaleIn 0.3s ease;
}

.step-item.completed .step-dot {
  background: #673ab7;
  border-color: #673ab7;
  color: white;
}

@keyframes scaleIn {
  from {
    transform: scale(0.8);
  }

  to {
    transform: scale(1);
  }
}

.step-label {
  font-size: 11px;
  color: #999;
  text-align: center;
  white-space: nowrap;
}

.step-item.active .step-label {
  color: #666;
  font-weight: 500;
}

/* 스켈레톤 카드 */
.skeleton-cards {
  margin-bottom: 30px;
}

.skeleton-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
  opacity: 0;
  animation: slideUp 0.4s ease forwards;
}

@keyframes slideUp {
  from {
    transform: translateY(15px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.card-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #673ab7;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  display: flex;
  gap: 12px;
}

.skeleton-image {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-title {
  height: 18px;
  width: 70%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-location {
  height: 14px;
  width: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}

.skeleton-tags {
  display: flex;
  gap: 6px;
}

.skeleton-tag {
  height: 20px;
  width: 45px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 10px;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }

  100% {
    background-position: 200% 0;
  }
}

/* 팁 메시지 */
.loading-tip {
  background: #f8f5ff;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e8dfff;
}

.tip-emoji {
  font-size: 18px;
  flex-shrink: 0;
}

.tip-text {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
}

/* 반응형 */
@media (max-width: 480px) {
  .loading-message h3 {
    font-size: 18px;
  }

  .sub-message {
    font-size: 13px;
  }

  .step-label {
    font-size: 10px;
  }

  .skeleton-card {
    padding: 12px;
  }

  .loading-tip {
    padding: 12px;
  }

  .tip-text {
    font-size: 12px;
  }
}
</style>
