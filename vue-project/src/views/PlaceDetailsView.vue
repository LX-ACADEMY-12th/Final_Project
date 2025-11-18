<template>
  <div class="exhibition-detail-page">

    <!-- 헤더: 전시관/과학장소에 따라 타이틀 변경 -->
    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시관 상세정보" :isFavorite="computedIsFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else-if="pageType === 'science_place'" pageTitle="체험장소 상세정보" :isFavorite="computedIsFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else pageTitle="로딩 중..." />
    </div>

    <!-- 스크롤 가능한 메인 콘텐츠 영역 -->
    <div class="scroll-content">

      <!-- ==================== 전시관 섹션 ==================== -->
      <div v-if="pageType === 'exhibition'">
        <!-- 기본 정보 섹션 (이미지, 제목, 평점 등) -->
        <InfoSection :exhibition="exhibition" imageTag="전시 태그" :mainCategory="exhibition.mainCategory"
          :subCategories="exhibition.subCategories" :gradeTag="exhibition.gradeTag"
          @authenticate-visit="handleVisitAuthentication" />
        <hr class="divider" />

        <!-- 탭 메뉴 (상세정보 / AI 추천) -->
        <TabSection :key="currentTab" :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

        <!-- 상세정보 탭 -->
        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="exhibitionInformation" :exhibition="exhibition" :isPlace="false"
            :target-id="currentId" :target-type="pageType" @review-posted="handleReviewPosted"
            @review-deleted="handleReviewDeleted" :photo-review-count="exhibition.photoReviewCount" />
        </div>

        <!-- AI 추천 탭 (전시관) -->
        <div v-else-if="currentTab === 'recommend'">
          <!-- 로딩 화면 -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">

              <!-- AI 아이콘 애니메이션 -->
              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <div class="ai-icon-bg">
                    <span class="ai-icon">🤖</span>
                  </div>
                  <div class="pulse-ring"></div>
                  <div class="pulse-ring-delayed"></div>
                </div>
              </div>

              <!-- 로딩 메시지 (전시관용) -->
              <div class="loading-message">
                <h3 class="main-title">AI가 맞춤 동선을 생성 중입니다</h3>
                <transition name="fade" mode="out-in">
                  <p class="sub-message" :key="currentMessageIndex">
                    {{ loadingMessages[currentMessageIndex] }}
                  </p>
                </transition>
              </div>

              <!-- 프로그레스 바 -->
              <div class="progress-bar-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
                </div>
                <p class="progress-text">{{ progressPercentage }}%</p>
              </div>

              <!-- 진행 단계 표시 -->
              <div class="progress-steps">
                <div class="step-connector"></div>
                <div class="step-item" v-for="(step, index) in progressSteps" :key="index"
                  :class="{ active: currentStepIndex >= index, completed: currentStepIndex > index }">
                  <div class="step-dot">
                    <transition name="check-fade" mode="out-in">
                      <i v-if="currentStepIndex > index" class="bi bi-check-lg" key="check"></i>
                      <span v-else class="step-number" key="number">{{ index + 1 }}</span>
                    </transition>
                  </div>
                  <span class="step-label">{{ step }}</span>
                </div>
              </div>

              <!-- 스켈레톤 카드 (로딩 중 미리보기) -->
              <div class="skeleton-cards">
                <div v-for="n in 3" :key="n" class="skeleton-card" :style="{ animationDelay: `${n * 0.15}s` }">
                  <div class="card-header">
                    <div class="card-number">
                      <span class="number-text">{{ n }}</span>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="skeleton-image">
                      <div class="shimmer"></div>
                    </div>
                    <div class="skeleton-info">
                      <div class="skeleton-title">
                        <div class="shimmer"></div>
                      </div>
                      <div class="skeleton-location">
                        <div class="shimmer"></div>
                      </div>
                      <div class="skeleton-tags">
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 팁 메시지 (로테이션) -->
              <transition name="fade" mode="out-in">
                <div class="loading-tip" :key="currentTipIndex">
                  <span class="tip-icon">💡</span>
                  <span class="tip-text">{{ tips[currentTipIndex] }}</span>
                </div>
              </transition>
            </div>
          </div>

          <!-- 추천 결과 표시 -->
          <CourseRecommend v-else :key="courseRerenderKey" :course-items="courseItems" :type="pageType"
            :is-loading="isRecommending" @request-new-course="fetchRecommendedCourse"
            @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

      <!-- ==================== 과학 장소 섹션 ==================== -->
      <div v-else-if="pageType === 'science_place'">
        <!-- 기본 정보 섹션 -->
        <InfoSection :exhibition="place" imageTag="장소 태그" :mainCategory="place.mainCategory"
          :subCategories="place.subCategories" :gradeTag="place.gradeTag"
          @authenticate-visit="handleVisitAuthentication" />
        <hr class="divider" />

        <!-- 탭 메뉴 -->
        <TabSection :isPlace="true" :activeTab="currentTab" @updateTab="handleTabChange" />

        <!-- 상세정보 탭 -->
        <div v-if="currentTab === 'detail'">
          <ContentDetailView :placeInformation="placeInformation" :place="place" :target-id="currentId"
            :target-type="pageType" :isPlace="true" @review-posted="handleReviewPosted"
            @review-deleted="handleReviewDeleted" :photo-review-count="place.photoReviewCount" />
        </div>

        <!-- AI 추천 탭 (과학 장소 - 날씨 고려) -->
        <div v-else-if="currentTab === 'recommend'">
          <!-- 로딩 화면 -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">

              <!-- AI 아이콘 애니메이션 -->
              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <div class="ai-icon-bg">
                    <span class="ai-icon">🤖</span>
                  </div>
                  <div class="pulse-ring"></div>
                  <div class="pulse-ring-delayed"></div>
                </div>
              </div>

              <!-- 로딩 메시지 (과학 장소용 - 날씨 강조) -->
              <div class="loading-message">
                <h3 class="main-title">AI가 날씨를 고려한 맞춤 코스를 생성 중입니다</h3>
                <transition name="fade" mode="out-in">
                  <p class="sub-message" :key="currentMessageIndex">
                    {{ loadingMessages[currentMessageIndex] }}
                  </p>
                </transition>
              </div>

              <!-- 프로그레스 바 -->
              <div class="progress-bar-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
                </div>
                <p class="progress-text">{{ progressPercentage }}%</p>
              </div>

              <!-- 진행 단계 표시 (5단계) -->
              <div class="progress-steps">
                <div class="step-connector"></div>
                <div class="step-item" v-for="(step, index) in progressSteps" :key="index"
                  :class="{ active: currentStepIndex >= index, completed: currentStepIndex > index }">
                  <div class="step-dot">
                    <transition name="check-fade" mode="out-in">
                      <i v-if="currentStepIndex > index" class="bi bi-check-lg" key="check"></i>
                      <span v-else class="step-number" key="number">{{ index + 1 }}</span>
                    </transition>
                  </div>
                  <span class="step-label">{{ step }}</span>
                </div>
              </div>

              <!-- 스켈레톤 카드 -->
              <div class="skeleton-cards">
                <div v-for="n in 3" :key="n" class="skeleton-card" :style="{ animationDelay: `${n * 0.15}s` }">
                  <div class="card-header">
                    <div class="card-number">
                      <span class="number-text">{{ n }}</span>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="skeleton-image">
                      <div class="shimmer"></div>
                    </div>
                    <div class="skeleton-info">
                      <div class="skeleton-title">
                        <div class="shimmer"></div>
                      </div>
                      <div class="skeleton-location">
                        <div class="shimmer"></div>
                      </div>
                      <div class="skeleton-tags">
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                        <span class="skeleton-tag">
                          <div class="shimmer"></div>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 팁 메시지 (날씨 관련) -->
              <transition name="fade" mode="out-in">
                <div class="loading-tip" :key="currentTipIndex">
                  <span class="tip-icon">💡</span>
                  <span class="tip-text">{{ tips[currentTipIndex] }}</span>
                </div>
              </transition>
            </div>
          </div>

          <!-- ★ 추천 결과 표시 (날씨 카드 포함) -->
          <div v-else>
            <!-- 날씨 정보 카드 (과학 장소만 표시) -->
            <div v-if="weatherInfo" class="weather-info-card">
              <div class="weather-header">
                <span class="weather-icon">{{ weatherInfo.icon }}</span>
                <div class="weather-text">
                  <h4>오늘의 날씨 정보</h4>
                  <p>{{ weatherInfo.description }}</p>
                </div>
              </div>
              <div class="weather-recommendation">
                <i class="bi bi-lightbulb-fill"></i>
                <span>{{ weatherInfo.recommendation }}</span>
              </div>
            </div>

            <CourseRecommend :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
              @request-new-course="fetchRecommendedCourse" @save-recommended-course="handleSaveRecommendedCourse" />
          </div>
        </div>
      </div>

      <!-- 초기 로딩 화면 -->
      <div v-else class="loading-container">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-3">데이터를 불러오는 중입니다...</p>
      </div>

    </div>
  </div>
</template>

<script>
import axios from '@/api/axiosSetup';

// 하위 컴포넌트 import
import ExhibitionHeader from '@/components/header/ExhibitionHeader.vue';
import InfoSection from '@/components/section/InfoSection.vue';
import TabSection from '@/components/section/TabSection.vue';
import ContentDetailView from './ContentDetailView.vue';
import CourseRecommend from './CourseRecommend.vue';

// Pinia 스토어 (로그인 상태 관리)
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

// 유틸리티
import eventBus from '@/utils/eventBus';
import { getSceneIdFromTitle } from '@/utils/tourMapper';

export default {
  name: 'PlaceDetailsView',

  components: {
    ExhibitionHeader,
    InfoSection,
    TabSection,
    CourseRecommend,
    ContentDetailView,
  },

  /**
   * 라우트 가드: 가상 투어로 이동 시 코스 캐시 유지
   */
  beforeRouteLeave(to, from, next) {
    if (this.isTourRoute(to)) {
      // 투어 페이지로 이동하는 경우 캐시 유지
      this.saveCourseCache();
      sessionStorage.setItem('pdv:tabAfterBack', 'recommend');
      sessionStorage.setItem('pdv:courseCacheKey', this.cacheKey);
      sessionStorage.setItem(
        'pdv:returnTo',
        JSON.stringify({
          type: this.pageType,
          id: this.currentId,
          query: this.$route.query
        })
      );
      return next();
    }
    // 다른 페이지로 이동하는 경우 캐시 클리어
    this.clearCourseCache();
    next();
  },

  /**
   * keep-alive 활성화 시 탭 복원
   */
  activated() {
    this.restoreTabIfCameBack();
  },

  /**
   * Pinia 스토어 설정
   */
  setup() {
    const authStore = useAuthStore();
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);
    return {
      isLoggedIn,
      currentUserId
    };
  },

  computed: {
    /**
     * 찜 상태 반환
     */
    computedIsFavorite() {
      return this.isWished;
    },

    /**
     * 세션 스토리지 캐시 키 생성
     */
    cacheKey() {
      return `course-cache:${this.pageType}:${this.currentId}`;
    },

    /**
     * 진행 단계 (전시관 4단계 / 과학 장소 5단계)
     */
    progressSteps() {
      return this.pageType === 'science_place'
        ? this.sciencePlaceProgressSteps
        : this.exhibitionProgressSteps;
    },

    /**
     * 로딩 메시지 (pageType에 따라 다름)
     */
    loadingMessages() {
      return this.pageType === 'science_place'
        ? this.sciencePlaceLoadingMessages
        : this.exhibitionLoadingMessages;
    },

    /**
     * 팁 메시지 (pageType에 따라 다름)
     */
    tips() {
      return this.pageType === 'science_place'
        ? this.sciencePlaceTips
        : this.exhibitionTips;
    },

    /**
     * 프로그레스 퍼센트 계산
     */
    progressPercentage() {
      const total = this.progressSteps.length;
      const current = this.currentStepIndex + 1;
      return Math.min(Math.round((current / total) * 100), 100);
    }
  },

  data() {
    return {
      // ==================== 공통 데이터 ====================
      courseRerenderKey: 0,           // 코스 컴포넌트 강제 리렌더링용
      currentId: null,                 // 현재 전시/장소 ID
      pageType: null,                  // 'exhibition' | 'science_place'
      currentTab: 'detail',            // 현재 활성 탭
      isWished: false,                 // 찜 상태
      isLoading: false,                // 로딩 상태

      // ==================== 전시관 데이터 ====================
      exhibition: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        mainCategory: '',
        subCategories: [],
        gradeTag: '',
        type: '',
        description: '',
        mainImage: 'https://via.placeholder.com/600x400',
        photoReviewCount: 0,
        exhibitionList: [],
        visited: false,
        liked: false
      },

      exhibitionInformation: {
        exhibitionLocation: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: '',
        lat: 0,
        lng: 0,
        scienceCenterName: '',
        hallId: 0
      },

      // ==================== 과학 장소 데이터 ====================
      place: {
        title: '데이터 로딩 중...',
        rating: 0,
        reviewCount: 0,
        mainCategory: '',
        subCategories: [],
        gradeTag: '',
        type: '',
        description: '',
        mainImage: 'https://via.placeholder.com/600x400',
        photoReviewCount: 0,
        visited: false,
        liked: false
      },

      placeInformation: {
        placeAddress: '',
        operationPeriod: '',
        operationHours: '',
        entranceFee: '',
        lat: 0,
        lng: 0
      },

      // ==================== AI 추천 코스 ====================
      courseItems: [],                 // 추천 코스 아이템 리스트
      hasLoadedRecommendations: false, // 추천 로딩 완료 여부
      isRecommending: false,           // 추천 생성 중 여부
      weatherInfo: null,               // ★ 날씨 정보

      // ==================== 전시관 로딩 애니메이션 ====================
      exhibitionProgressSteps: ['데이터 분석', '유사 전시관 탐색', '경로 최적화', '동선 완성'],
      exhibitionLoadingMessages: [
        '현재 전시관의 특징을 분석하고 있어요',
        '비슷한 테마의 전시관들을 찾고 있어요',
        '최적의 관람 동선을 계산하고 있어요',
        '추천 동선을 마무리하고 있어요'
      ],
      exhibitionTips: [
        '🚀 AI는 평점과 리뷰를 기반으로 추천해드려요',
        '📊 전시 개수와 교육적 가치를 고려해요',
        '💾 생성된 동선은 관심 코스에 저장할 수 있어요'
      ],

      // ==================== 과학 장소 로딩 애니메이션 (날씨 강조) ====================
      sciencePlaceProgressSteps: ['날씨 확인', '데이터 분석', '유사 장소 탐색', '경로 최적화', '코스 완성'],
      sciencePlaceLoadingMessages: [
        '☀️ 각 장소의 실시간 날씨를 확인하고 있어요',
        '날씨와 장소의 특징을 함께 분석하고 있어요',
        '날씨에 적합한 장소들을 찾고 있어요',
        '날씨를 고려한 최적 경로를 계산하고 있어요',
        '날씨 기반 맞춤 코스를 완성하고 있어요'
      ],
      sciencePlaceTips: [
        '☀️ 맑은 날에는 야외 체험장을 우선 추천해요',
        '🌧️ 비 오는 날에는 실내 장소를 중심으로 추천해요',
        '🌡️ 각 장소의 기온과 강수량을 실시간으로 고려해요',
        '💾 AI 추천 코스는 관심 코스에 저장할 수 있어요'
      ],

      // ==================== 애니메이션 상태 ====================
      currentStepIndex: 0,             // 현재 진행 단계 인덱스
      currentMessageIndex: 0,          // 현재 메시지 인덱스
      currentTipIndex: 0,              // 현재 팁 인덱스
      stepInterval: null,              // 단계 진행 인터벌
      tipInterval: null                // 팁 로테이션 인터벌
    };
  },

  /**
   * 컴포넌트 생성 시 데이터 로딩
   */
  created() {
    // URL에서 ID 추출 및 타입 결정
    const id = this.$route.params.id;
    this.currentId = id;
    const isPlace = this.$route.path.startsWith('/place/');
    this.pageType = isPlace ? 'science_place' : 'exhibition';

    // 데이터 로딩
    if (isPlace) {
      this.fetchPlaceData(id);
    } else {
      this.fetchExhibitionData(id);
    }

    console.log('[PlaceDetailsView] currentUserId:', this.currentUserId);
  },

  /**
   * 컴포넌트 마운트 시 이벤트 리스너 등록
   */
  mounted() {
    this.restoreTabIfCameBack();
    // bfcache 대응 (브라우저 뒤로가기)
    this._onPageShow = () => this.restoreTabIfCameBack();
    window.addEventListener('pageshow', this._onPageShow);
  },

  /**
   * 데이터 변경 감지
   */
  watch: {
    /**
     * URL 파라미터 변경 감지 (다른 전시/장소로 이동)
     */
    '$route.params.id'(newId, oldId) {
      if (oldId && newId && newId !== oldId) {
        // 이전 캐시 제거
        const oldKey = `course-cache:${this.pageType}:${oldId}`;
        this.clearCourseCache(oldKey);
        this.hasLoadedRecommendations = false;

        // 새 데이터 로딩
        this.currentId = newId;
        const isPlace = this.$route.path.startsWith('/place/');
        this.pageType = isPlace ? 'science_place' : 'exhibition';
        if (isPlace) {
          this.fetchPlaceData(newId);
        } else {
          this.fetchExhibitionData(newId);
        }
      }
    },

    /**
     * 로그인 상태 변경 감지
     */
    currentUserId(newUserId, oldUserId) {
      // 로그인 시 데이터 갱신
      if (newUserId && !oldUserId && this.currentId) {
        if (this.pageType === 'exhibition') {
          this.fetchExhibitionData(this.currentId);
        } else if (this.pageType === 'science_place') {
          this.fetchPlaceData(this.currentId);
        }
      }
      // 로그아웃 시 찜 상태 초기화
      else if (!newUserId && oldUserId) {
        this.isWished = false;
      }
    }
  },

  /**
   * 컴포넌트 언마운트 시 정리
   */
  beforeUnmount() {
    this.clearLoadingIntervals();
    if (this._onPageShow) window.removeEventListener('pageshow', this._onPageShow);
  },

  methods: {

    /**
     * AI 추천 코스 저장 핸들러
     * @param {Array} items - 저장할 코스 아이템 리스트
     */
    async handleSaveRecommendedCourse(items) {
      console.log('[Parent] save received, items =', Array.isArray(items) ? items.length : items);

      // 로그인 체크
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => this.$router.push({ name: 'login' })
        });
        return;
      }

      // 아이템 검증
      if (!Array.isArray(items) || items.length === 0) {
        eventBus.emit('show-global-alert', { message: '저장할 코스 정보가 없습니다.', type: 'error' });
        return;
      }

      try {
        const currentItemData = (this.pageType === 'science_place') ? this.place : this.exhibition;
        const scheduleName = `AI 추천: ${currentItemData.title || '코스'}`;
        const sourceId = this.currentId;

        // 백엔드 형식으로 변환
        const backendItems = items.map(item => ({
          exhibitionId: this.pageType !== 'science_place' ? item.id : null,
          placeId: this.pageType === 'science_place' ? item.id : null,
          sequence: item.number,
          itemType: item.type === 'exhibition' ? 'exhibition' : 'science_place',
          categoryName: item.subject,
          gradeName: item.grade,
          subCategories: item.hashtags
        }));

        const requestDto = {
          scheduleName,
          sourceId,
          sourceCourseType: this.pageType === 'science_place' ? 'ai_course' : 'inner_course',
          items: backendItems,
          userId: this.currentUserId
        };

        console.log('[Parent] POST /api/schedules/save-recommended', requestDto);
        const res = await axios.post('/api/schedules/save-recommended', requestDto);

        if (res.status === 200) {
          eventBus.emit('show-global-alert', { message: '추천 코스가 "관심 코스"에 저장되었습니다.', type: 'success' });
        } else {
          eventBus.emit('show-global-alert', { message: `코스 저장 중 문제가 발생했습니다: ${res.statusText}`, type: 'error' });
        }
      } catch (err) {
        console.error('save error', err);
        const msg = err.response?.data || err.message;
        eventBus.emit('show-global-alert', { message: `코스 저장 중 오류: ${msg}`, type: 'error' });
      }
    },

    /**
     * 가상 투어 라우트 감지
     * @param {Object} route - Vue Router 라우트 객체
     * @returns {Boolean} 투어 라우트 여부
     */
    isTourRoute(route) {
      const n = (route?.name || '').toString().toLowerCase();
      const p = (route?.path || '').toString().toLowerCase();
      return (
        n === 'virtualtour' ||
        n === 'tour' ||
        n.includes('virtual') ||
        n.includes('tour') ||
        /virtual|tour|vr|webgl/.test(p)
      );
    },

    /**
     * 투어에서 복귀 시 탭 및 캐시 복원
     */
    async restoreTabIfCameBack() {
      const desired = sessionStorage.getItem('pdv:tabAfterBack');
      if (desired === 'recommend') {
        this.currentTab = 'recommend';

        const key = sessionStorage.getItem('pdv:courseCacheKey');
        if (key && key === this.cacheKey) {
          // 캐시 재사용 시도
          const reused = this.loadCourseCache();
          if (!reused && !this.hasLoadedRecommendations) {
            this.hasLoadedRecommendations = false;
            this.fetchRecommendedCourse();
          }
        } else {
          // 캐시가 없으면 새로 로딩
          if (!this.hasLoadedRecommendations) this.fetchRecommendedCourse();
        }

        // 세션 스토리지 정리
        sessionStorage.removeItem('pdv:tabAfterBack');
        sessionStorage.removeItem('pdv:courseCacheKey');
      }
    },

    /**
     * 코스 캐시 로드
     * @returns {Boolean} 캐시 로드 성공 여부
     */
    loadCourseCache() {
      try {
        const raw = sessionStorage.getItem(this.cacheKey);
        if (!raw) return false;
        const { items } = JSON.parse(raw);
        if (!Array.isArray(items) || items.length === 0) return false;

        this.courseItems = items;
        this.hasLoadedRecommendations = true;
        this.courseRerenderKey = Date.now();
        console.log('♻️ 코스 캐시 재사용:', this.courseItems);
        return true;
      } catch (e) {
        console.warn('코스 캐시 파싱 실패:', e);
        return false;
      }
    },

    /**
     * 코스 캐시 저장
     */
    saveCourseCache() {
      try {
        const payload = { items: this.courseItems };
        sessionStorage.setItem(this.cacheKey, JSON.stringify(payload));
        console.log('💾 코스 캐시 저장 완료:', this.cacheKey);
      } catch (e) {
        console.warn('코스 캐시 저장 실패:', e);
      }
    },

    /**
     * 코스 캐시 삭제
     * @param {String} key - 삭제할 캐시 키 (기본값: 현재 캐시 키)
     */
    clearCourseCache(key = this.cacheKey) {
      try {
        sessionStorage.removeItem(key);
        console.log('🧹 코스 캐시 제거:', key);
      } catch (e) {
        console.warn('코스 캐시 제거 실패:', e);
      }
    },

    /**
     * 날씨 정보를 사용자 친화적 형태로 변환
     * @param {String} weatherText - 백엔드에서 받은 날씨 텍스트 (예: "맑음, 기온 15.0℃, 습도 60%")
     * @returns {Object} 날씨 정보 객체
     */
    parseWeatherInfo(weatherText) {
      if (!weatherText) return null;

      // 날씨 상태 파싱
      let icon = '☀️';
      let condition = '맑음';
      let recommendation = '야외 활동하기 좋은 날씨입니다. 실외 체험장도 추천해드렸어요!';

      if (weatherText.includes('비')) {
        icon = '🌧️';
        condition = '비';
        recommendation = '비가 오고 있어요. 실내 장소를 중심으로 추천해드렸어요!';
      } else if (weatherText.includes('눈')) {
        icon = '❄️';
        condition = '눈';
        recommendation = '눈이 오고 있어요. 따뜻한 실내 장소를 추천해드렸어요!';
      } else if (weatherText.includes('흐림')) {
        icon = '☁️';
        condition = '흐림';
        recommendation = '흐린 날씨예요. 실내외 모두 즐길 수 있는 장소를 추천해드렸어요!';
      } else if (weatherText.includes('구름많음')) {
        icon = '⛅';
        condition = '구름많음';
        recommendation = '구름이 많지만 활동하기 좋은 날씨예요!';
      }

      return {
        icon,
        description: weatherText,
        recommendation
      };
    },

    /**
     * 전시관 DTO를 컴포넌트 상태로 매핑
     * @param {Object} dto - 백엔드에서 받은 전시관 DTO
     */
    mapExhibitionDTO(dto) {
      const title = dto.exhibitionHallName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;

      // 서브카테고리 배열 변환
      let subCategoriesArray = [];
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } else if (Array.isArray(subCategoryData)) {
        subCategoriesArray = subCategoryData.map(tag => String(tag).trim()).filter(Boolean);
      }

      // 전시관 정보 매핑
      this.exhibition = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category,
        subCategories: subCategoriesArray,
        gradeTag: grade,
        type: dto.type ?? 'exhibition',
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
        photoReviewCount: dto.totalPhotoReviews ?? 0,
        exhibitionList: dto.exhibitionList,
        visited: dto.visited ?? false,
        liked: dto.liked ?? false
      };

      this.isWished = dto.liked ?? false;

      // 전시관 상세 정보 매핑
      this.exhibitionInformation = {
        exhibitionLocation: dto.location ?? '정보 없음',
        operationPeriod: this.formatPeriod(dto.startDate, dto.endDate),
        operationHours: dto.openingHours ?? '정보 없음',
        entranceFee: this.formatFee(dto.admissionFee),
        lat: dto.latitude,
        lng: dto.longitude,
        scienceCenterName: (dto.location || '').split(' ')[0] || '',
        hallId: dto.hallId
      };
    },

    /**
     * 과학 장소 DTO를 컴포넌트 상태로 매핑
     * @param {Object} dto - 백엔드에서 받은 과학 장소 DTO
     */
    mapPlaceDTO(dto) {
      const title = dto.placeName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;

      // 서브카테고리 배열 변환
      let subCategoriesArray = [];
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } else if (Array.isArray(subCategoryData)) {
        subCategoriesArray = subCategoryData.map(tag => String(tag).trim()).filter(Boolean);
      }

      // 장소 정보 매핑
      this.place = {
        title,
        rating: dto.averageRating ?? 0,
        reviewCount: dto.totalReviews ?? 0,
        mainCategory: category,
        subCategories: subCategoriesArray,
        gradeTag: grade,
        description: dto.description ?? '',
        mainImage: dto.mainImageUrl || 'https://via.placeholder.com/600x400',
        photoReviewCount: dto.totalPhotoReviews ?? 0,
        type: dto.type ?? 'science_place',
        visited: dto.visited ?? false,
        liked: dto.liked ?? false
      };

      this.isWished = dto.liked ?? false;

      // 장소 상세 정보 매핑
      this.placeInformation = {
        placeAddress: dto.addressDetail ?? '정보 없음',
        operationPeriod: this.formatPeriod(null, null),
        operationHours: dto.openingHours ?? '정보 없음',
        entranceFee: dto.admissionFee ?? '정보 없음',
        lat: dto.latitude,
        lng: dto.longitude
      };
    },

    /**
     * 운영 기간 포맷팅
     * @param {String} start - 시작일
     * @param {String} end - 종료일
     * @returns {String} 포맷된 기간 문자열
     */
    formatPeriod(start, end) {
      if (!start && !end) return '상시 운영';
      if (start && !end) return `${start} ~ 별도 안내까지`;
      if (!start && end) return `~ ${end}`;
      return `${start} ~ ${end}`;
    },

    /**
     * 입장료 포맷팅
     * @param {Number} fee - 입장료
     * @returns {String} 포맷된 입장료 문자열
     */
    formatFee(fee) {
      if (fee === null || fee === undefined) return '정보 없음';
      if (fee === 0) return '무료';
      return `${fee.toLocaleString('ko-KR')}`;
    },

    /**
     * 전시관 상세 데이터 조회
     * @param {Number} id - 전시관 ID
     */
    async fetchExhibitionData(id) {
      try {
        const res = await axios.get(`/api/exhibitions`, {
          params: {
            exhibitionId: id,
            userId: this.currentUserId,
            mainCategoryTags: this.$route.query.mainCategoryTags ?? '',
            gradeTags: this.$route.query.gradeTags
          }
        });

        const dto = res.data;
        if (!dto || Object.keys(dto).length === 0) {
          console.warn('전시 데이터가 비어 있습니다.');
          return;
        }
        this.mapExhibitionDTO(dto);
      } catch (error) {
        console.error('전시 상세 조회 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '전시 정보를 불러오지 못했습니다.',
          type: 'error'
        });
      }
    },

    /**
     * 과학 장소 상세 데이터 조회
     * @param {Number} id - 장소 ID
     */
    async fetchPlaceData(id) {
      try {
        const res = await axios.get(`/api/place`, {
          params: {
            placeId: id,
            userId: this.currentUserId
          }
        });

        const dto = res.data;
        if (!dto || Object.keys(dto).length === 0) {
          console.warn('장소 데이터가 비어 있습니다.');
          return;
        }

        this.mapPlaceDTO(dto);
      } catch (error) {
        console.error('장소 상세 조회 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '장소 정보를 불러오지 못했습니다.',
          type: 'error'
        });
      }
    },

    /**
     * 데이터 갱신 (리뷰 등록/삭제 후)
     */
    refreshData() {
      if (this.pageType === 'exhibition') {
        this.fetchExhibitionData(this.currentId);
      } else if (this.pageType === 'science_place') {
        this.fetchPlaceData(this.currentId);
      }
    },

    /**
     * 리뷰 등록 후 핸들러
     */
    handleReviewPosted() {
      this.refreshData();
    },

    /**
     * 리뷰 삭제 후 핸들러
     */
    handleReviewDeleted() {
      this.refreshData();
    },

    /**
     * 탭 변경 핸들러
     * @param {String} tabName - 변경할 탭 이름
     */
    handleTabChange(tabName) {
      this.currentTab = tabName;

      // AI 추천 탭으로 전환 시 처리
      if (tabName === 'recommend') {
        // 캐시 재사용 시도
        const reused = this.loadCourseCache();
        if (reused) return;

        // 캐시가 없으면 새로 로딩
        if (!this.hasLoadedRecommendations) {
          this.fetchRecommendedCourse();
        }
      }
    },

    /**
     * 로딩 애니메이션 시작
     * - 과학 장소: 5단계, 700ms 간격
     * - 전시관: 4단계, 1200ms 간격
     */
    startLoadingAnimation() {
      this.currentStepIndex = 0;
      this.currentMessageIndex = 0;

      // 과학 장소는 단계가 많아서 조금 빠르게 진행
      const intervalTime = this.pageType === 'science_place' ? 700 : 1200;

      // 단계 진행 인터벌
      this.stepInterval = setInterval(() => {
        if (this.currentStepIndex < this.progressSteps.length - 1) {
          this.currentStepIndex++;
          this.currentMessageIndex++;
        }
      }, intervalTime);

      // 팁 로테이션 인터벌
      this.currentTipIndex = 0;
      this.tipInterval = setInterval(() => {
        this.currentTipIndex = (this.currentTipIndex + 1) % this.tips.length;
      }, 2500);
    },

    /**
     * 로딩 인터벌 정리
     */
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

    /**
     * AI 추천 코스 조회
     * - 백엔드에서 추천 코스 가져오기
     * - 현재 아이템 + AI 추천 아이템 병합
     * - 과학 장소의 경우 날씨 정보도 함께 처리
     * - 캐시에 저장
     */
    async fetchRecommendedCourse() {
      console.log('🤖 AI 추천 코스를 검색합니다...');

      this.isRecommending = true;
      this.startLoadingAnimation();

      await this.$nextTick();

      try {
        const apiUrl = `/api/recommend/course`;
        const params = {
          type: this.pageType,
          currentId: this.currentId,
          mainCategoryTags: this.$route.query.mainCategoryTags,
          subCategoryTags: this.$route.query.subCategoryTags,
          gradeTags: this.$route.query.gradeTags
        };

        // 최소 3.5초 로딩 (UX 개선)
        const [res] = await Promise.all([
          axios.get(apiUrl, { params }),
          new Promise(resolve => setTimeout(resolve, 3500))
        ]);

        // ★ 백엔드 응답에서 날씨 정보 추출
        const responseData = res.data;
        let aiRecommendedDtos = [];
        let weatherText = null;

        // 백엔드가 { recommendations: [...], weatherInfo: "..." } 형태로 반환하는 경우
        if (responseData.recommendations) {
          aiRecommendedDtos = responseData.recommendations;
          weatherText = responseData.weatherInfo;
        } else {
          // 기존 방식 (배열만 반환)
          aiRecommendedDtos = responseData;
        }

        // ★ 과학 장소인 경우에만 날씨 정보 파싱
        if (this.pageType === 'science_place' && weatherText) {
          this.weatherInfo = this.parseWeatherInfo(weatherText);
        } else {
          this.weatherInfo = null; // 전시관은 날씨 정보 없음
        }

        const currentItemData = this.pageType === 'science_place' ? this.place : this.exhibition;
        const currentItemInfo =
          this.pageType === 'science_place' ? this.placeInformation : this.exhibitionInformation;

        // 현재 아이템 포맷팅 (첫 번째 아이템)
        const currentItemFormatted = {
          id: this.currentId,
          number: 1,
          imageUrl: currentItemData.mainImage || 'https://via.placeholder.com/60x60',
          title: currentItemData.title || '제목 없음',
          subject: currentItemData.mainCategory || '분류 없음',
          grade: currentItemData.gradeTag || '학년 정보 없음',
          hashtags: Array.isArray(currentItemData.subCategories)
            ? currentItemData.subCategories
            : currentItemData.subCategories
              ? [currentItemData.subCategories]
              : [],
          type: currentItemData.type,
          place:
            currentItemInfo.placeAddress ||
            currentItemInfo.exhibitionLocation ||
            '주소 정보 없음',
          exhibitionList: currentItemData.exhibitionList,
          lat: currentItemInfo.lat || 0,
          lng: currentItemInfo.lng || 0,
          sceneId: getSceneIdFromTitle(currentItemData.title)
        };

        // AI 추천 아이템 포맷팅
        const aiItemsFormatted = aiRecommendedDtos.map((item, index) => ({
          id: item.placeId,
          number: index + 2,
          imageUrl: item.imageUrl || 'https://via.placeholder.com/60x60',
          title: item.placeName,
          subject: item.subjectName,
          grade: item.gradeName,
          hashtags: item.hashtags,
          place: item.address || '주소 정보 없음',
          exhibitionList: item.exhibitionList,
          lat: item.latitude,
          lng: item.longitude,
          type: item.type,
          sceneId: getSceneIdFromTitle(item.placeName)
        }));

        // 현재 아이템 + AI 추천 아이템 병합
        this.courseItems = [currentItemFormatted, ...aiItemsFormatted];
        this.hasLoadedRecommendations = true;

        // 캐시에 저장
        this.saveCourseCache();
        this.courseRerenderKey = Date.now();
      } catch (error) {
        console.error('AI 추천 코스 로딩 실패:', error);
        this.hasLoadedRecommendations = true;

        eventBus.emit('show-global-alert', {
          message: 'AI 추천 코스를 불러오는데 실패했습니다. 다시 시도해주세요.',
          type: 'error'
        });
      } finally {
        // 로딩 종료 (300ms 딜레이로 자연스럽게)
        setTimeout(() => {
          this.clearLoadingIntervals();
          this.isRecommending = false;
          this.currentStepIndex = 0;
          this.currentMessageIndex = 0;
          console.log('🏁 fetchRecommendedCourse 완료. isRecommending:', this.isRecommending);
        }, 300);
      }
    },

    /**
     * 찜 토글 핸들러
     * - 로그인 체크
     * - 찜 추가/삭제 API 호출
     */
    async handleToggleFavorite() {
      // 로그인 체크
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

      const isExhibition = this.pageType === 'exhibition';
      const currentState = this.isWished;
      const currentItem = isExhibition ? this.exhibition : this.place;

      const requestData = {
        targetId: this.currentId,
        targetType: this.pageType,
        mainCategory: currentItem.mainCategory,
        gradeTag: currentItem.gradeTag
      };

      try {
        if (currentState) {
          // 찜 삭제
          await axios.delete(`/api/wishlist`, { data: requestData });
          this.isWished = false;
          eventBus.emit('show-global-alert', { message: '관심 목록에서 삭제되었습니다.', type: 'success' });
        } else {
          // 찜 추가
          await axios.post(`/api/wishlist`, requestData);
          console.log('[wishlist] add payload:', requestData);
          this.isWished = true;
          eventBus.emit('show-global-alert', { message: '관심 목록에 추가되었습니다.', type: 'success' });
        }
      } catch (error) {
        const status = error.response?.status;

        // 409: 중복 오류 처리
        if (status === 409) {
          eventBus.emit('show-global-alert', {
            message: '중복된 찜 항목입니다. 자동으로 취소합니다.',
            type: 'error'
          });
          try {
            await axios.delete(`/api/wishlist`, { data: requestData });
            this.isWished = false;
            eventBus.emit('show-global-alert', { message: '찜이 취소되었습니다.', type: 'success' });
          } catch (deleteError) {
            console.error('409 후 찜 취소 실패:', deleteError);
            eventBus.emit('show-global-alert', {
              message: '찜 상태 동기화에 실패했습니다. (다음 클릭 시 취소됩니다.)',
              type: 'error'
            });
          }
        }
        // 403: 권한 오류
        else if (status === 403) {
          eventBus.emit('show-global-alert', {
            message: '로그인이 필요하거나 권한이 없습니다.',
            type: 'error'
          });
        }
        // 기타 오류
        else {
          console.error('찜 처리 중 에러:', error);
          eventBus.emit('show-global-alert', {
            message: '찜 처리에 실패했습니다. 다시 시도해 주세요.',
            type: 'error'
          });
        }
      } finally {
        this.isLoading = false;
      }
    },

    /**
     * 방문 인증 핸들러
     * - GPS 좌표 획득
     * - 스탬프 인증 API 호출
     */
    async handleVisitAuthentication() {
      console.log('PlaceDetailView: 방문 인증 시작');
      try {
        // 이미 방문한 장소인지 확인
        const isAlreadyVisited =
          this.pageType === 'exhibition' ? this.exhibition.visited : this.place.visited;
        if (isAlreadyVisited) {
          eventBus.emit('show-global-alert', { message: '이미 방문한 장소입니다.', type: 'error' });
          return;
        }

        // 로그인 체크
        if (!this.isLoggedIn) {
          eventBus.emit('show-global-confirm', {
            message: '로그인이 필요한 기능입니다.',
            onConfirm: () => this.$router.push({ name: 'login' })
          });
          return;
        }

        const targetType = this.pageType;
        const targetId = this.currentId;
        if (!targetType || !targetId) {
          throw new Error('인증 대상(targetId/targetType)을 식별할 수 없습니다.');
        }

        // GPS 좌표 획득
        const coords = await this.getUserCoordinates();

        // 스탬프 인증 요청
        const requestDTO = {
          userId: this.currentUserId,
          targetType,
          targetId,
          latitude: coords.latitude,
          longitude: coords.longitude
        };

        const response = await axios.post('/api/stamps', requestDTO);

        eventBus.emit('show-global-alert', { message: '스탬프 획득 성공!', type: 'success' });
        console.log('인증 성공:', response.data);

        // 방문 상태 업데이트
        if (this.pageType === 'exhibition') this.exhibition.visited = true;
        else if (this.pageType === 'science_place') this.place.visited = true;
      } catch (error) {
        const errorMessage = error.response?.data?.error || error.response?.data || error.message;
        if (String(errorMessage).includes('GPS')) {
          alert(`GPS 오류: ${errorMessage}`);
        } else {
          eventBus.emit('show-global-alert', { message: `${errorMessage}`, type: 'error' });
        }
        console.error('스탬프 인증 중 오류:', error);
      }
    },
    /**
    * GPS 좌표 획득 (실제 위치 사용)
    * @returns {Promise<Object>} 좌표 객체 {latitude, longitude}
    */
    getUserCoordinates() {
      console.log('GPS: 현재 위치 정보 획득 시도');

      return new Promise((resolve, reject) => {
        // Geolocation API를 지원하는지 확인
        if (!navigator.geolocation) {
          console.error('GPS: Geolocation이 지원되지 않습니다.');
          reject(new Error("Geolocation not supported"));
          return;
        }

        // 현재 위치 정보 획득
        navigator.geolocation.getCurrentPosition(
          (position) => {
            // 성공 시 처리
            const realLocation = {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            };
            console.log('GPS 좌표 획득 성공 (실제)', realLocation);
            resolve(realLocation);
          },
          (error) => {
            // 실패 시 처리 (사용자 거부, 시간 초과 등)
            console.error('GPS 좌표 획득 실패:', error.code, error.message);
            reject(error);
          },
          {
            // 옵션 설정
            enableHighAccuracy: true, // 높은 정확도 (GPS 사용 시도)
            timeout: 10000,         // 10초 타임아웃
            maximumAge: 0           // 캐시된 위치 사용 안 함
          }
        );
      });
    }
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/variable/woff2/SUIT-Variable.css');

/* ========================================
   공통 레이아웃
======================================== */
.exhibition-detail-page {
  font-family: 'SUIT', sans-serif;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
}

.scroll-content {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  padding-bottom: 40px;
  background-color: white;

  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.divider {
  border: none;
  height: 10px;
  background-color: #f7f7f7;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #666;
  font-size: 15px;
}

/* ========================================
   날씨 정보 카드 (NEW!)
======================================== */
.weather-info-card {
  margin: 20px 20px 16px 20px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  border-radius: 16px;
  border: 1px solid rgba(66, 165, 245, 0.3);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.15);
  animation: slide-down 0.4s ease-out;
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.weather-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.weather-icon {
  font-size: 42px;
  animation: weather-pulse 3s ease-in-out infinite;
}

@keyframes weather-pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

.weather-text h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1565C0;
}

.weather-text p {
  margin: 0;
  font-size: 14px;
  color: #1976D2;
  font-weight: 500;
}

.weather-recommendation {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  font-size: 13px;
  color: #0D47A1;
  font-weight: 600;
}

.weather-recommendation i {
  font-size: 18px;
  color: #FFA726;
}

/* ========================================
   AI 추천 로딩
======================================== */
.recommend-loading-container {
  background: linear-gradient(180deg, #ffffff 0%, #f8faff 100%);
  min-height: 600px;
  padding: 50px 20px 40px;
}

.loading-content {
  max-width: 520px;
  margin: 0 auto;
}

.ai-icon-section {
  text-align: center;
  margin-bottom: 36px;
}

.ai-icon-wrapper {
  position: relative;
  display: inline-block;
}

.ai-icon-bg {
  position: relative;
  z-index: 2;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A7CEC 0%, #5B8EF5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(74, 124, 236, 0.25);
}

.ai-icon {
  font-size: 48px;
  display: inline-block;
  animation: gentle-float 3s ease-in-out infinite;
}

@keyframes gentle-float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90px;
  height: 90px;
  border: 3px solid rgba(74, 124, 236, 0.4);
  border-radius: 50%;
  animation: pulse-expand 2.5s ease-out infinite;
}

.pulse-ring-delayed {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90px;
  height: 90px;
  border: 3px solid rgba(74, 124, 236, 0.3);
  border-radius: 50%;
  animation: pulse-expand 2.5s ease-out infinite;
  animation-delay: 0.8s;
}

@keyframes pulse-expand {
  0% {
    width: 90px;
    height: 90px;
    opacity: 0.8;
  }

  100% {
    width: 140px;
    height: 140px;
    opacity: 0;
  }
}

.loading-message {
  text-align: center;
  margin-bottom: 32px;
}

.loading-message .main-title {
  color: #1a1a1a;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 12px;
  line-height: 1.3;
}

.sub-message {
  color: #555;
  font-size: 15px;
  line-height: 1.5;
  margin: 0;
  min-height: 22px;
}

.progress-bar-container {
  margin-bottom: 32px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(74, 124, 236, 0.1);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4A7CEC 0%, #5B8EF5 100%);
  border-radius: 10px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px rgba(74, 124, 236, 0.4);
}

.progress-text {
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #4A7CEC;
  margin: 0;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  padding: 0 8px;
  position: relative;
}

.step-connector {
  position: absolute;
  top: 18px;
  left: 12%;
  right: 12%;
  height: 3px;
  background: linear-gradient(90deg,
      rgba(74, 124, 236, 0.2) 0%,
      rgba(74, 124, 236, 0.1) 100%);
  border-radius: 10px;
  z-index: 0;
}

.step-item {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  opacity: 0.4;
  transition: opacity 0.4s ease;
}

.step-item.active {
  opacity: 1;
}

.step-item.completed {
  opacity: 1;
}

.step-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ffffff;
  border: 3px solid #E0E0E0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.step-item.active .step-dot {
  background: #ffffff;
  border-color: #4A7CEC;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.3);
  transform: scale(1.15);
}

.step-item.completed .step-dot {
  background: linear-gradient(135deg, #4A7CEC 0%, #5B8EF5 100%);
  border-color: #4A7CEC;
  color: white;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.35);
}

.step-number {
  font-size: 14px;
  color: #999;
  font-weight: 700;
}

.step-item.active .step-number {
  color: #4A7CEC;
}

.step-label {
  font-size: 12px;
  color: #999;
  text-align: center;
  white-space: nowrap;
  font-weight: 500;
}

.step-item.active .step-label {
  color: #4A7CEC;
  font-weight: 600;
}

.step-item.completed .step-label {
  color: #666;
  font-weight: 600;
}

.check-fade-enter-active,
.check-fade-leave-active {
  transition: all 0.3s ease;
}

.check-fade-enter-from {
  opacity: 0;
  transform: scale(0.5);
}

.check-fade-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.skeleton-cards {
  margin-bottom: 32px;
}

.skeleton-card {
  background: #ffffff;
  border: 1px solid #efefef;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
  opacity: 0;
  animation: card-slide-up 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

@keyframes card-slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.skeleton-card .card-header {
  margin-bottom: 12px;
}

.skeleton-card .card-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A7CEC 0%, #5B8EF5 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.25);
}

.skeleton-card .card-body {
  display: flex;
  gap: 14px;
}

.skeleton-image {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: #f5f5f5;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-title {
  height: 18px;
  width: 75%;
  background: #f5f5f5;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
}

.skeleton-location {
  height: 14px;
  width: 55%;
  background: #f5f5f5;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
}

.skeleton-tags {
  display: flex;
  gap: 6px;
}

.skeleton-tag {
  height: 22px;
  width: 52px;
  background: #f5f5f5;
  border-radius: 11px;
  position: relative;
  overflow: hidden;
}

.shimmer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.6) 50%,
      transparent 100%);
  animation: shimmer-slide 1.8s infinite;
}

@keyframes shimmer-slide {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

.loading-tip {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
  border-radius: 12px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(74, 124, 236, 0.15);
  box-shadow: 0 2px 10px rgba(74, 124, 236, 0.08);
}

.tip-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.tip-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========================================
   반응형 디자인
======================================== */
@media (max-width: 480px) {
  .recommend-loading-container {
    padding: 40px 16px 32px;
  }

  .loading-message .main-title {
    font-size: 20px;
  }

  .sub-message {
    font-size: 14px;
  }

  .ai-icon-bg {
    width: 80px;
    height: 80px;
  }

  .ai-icon {
    font-size: 42px;
  }

  .step-dot {
    width: 32px;
    height: 32px;
  }

  .step-label {
    font-size: 11px;
  }

  .skeleton-card {
    padding: 14px;
  }

  .skeleton-image {
    width: 56px;
    height: 56px;
  }

  .loading-tip {
    padding: 14px 16px;
  }

  .tip-text {
    font-size: 13px;
  }

  .weather-info-card {
    margin: 16px;
    padding: 16px 18px;
  }

  .weather-icon {
    font-size: 36px;
  }

  .weather-text h4 {
    font-size: 15px;
  }

  .weather-text p {
    font-size: 13px;
  }

  .weather-recommendation {
    font-size: 12px;
    padding: 10px 12px;
  }
}

@media (max-width: 360px) {
  .step-label {
    font-size: 10px;
  }

  .progress-steps {
    padding: 0 4px;
  }

  .step-connector {
    left: 10%;
    right: 10%;
  }
}
</style>
