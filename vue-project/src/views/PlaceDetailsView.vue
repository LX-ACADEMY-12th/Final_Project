<template>
  <div class="exhibition-detail-page">

    <div class="header">
      <ExhibitionHeader v-if="pageType === 'exhibition'" pageTitle="전시관 상세정보" :isFavorite="computedIsFavorite"
        @toggle-favorite="handleToggleFavorite" />
      <ExhibitionHeader v-else-if="pageType === 'science_place'" pageTitle="체험장소 상세정보" :isFavorite="computedIsFavorite"
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
        <TabSection :key="currentTab" :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

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
          <CourseRecommend v-else :key="courseRerenderKey" :course-items="courseItems" :type="pageType"
            :is-loading="isRecommending" @request-new-course="fetchRecommendedCourse"
            @save-recommended-course="handleSaveRecommendedCourse" />
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
          <ContentDetailView :placeInformation="placeInformation" :place="place" :target-id="currentId"
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

// Pinia (로그인 상태 확인)
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

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

  // 컴포넌트 라우트 가드
  beforeRouteLeave(to, from, next) {
    if (this.isTourRoute(to)) {
      // 떠나기 직전 현재 courseItems 스냅샷 저장
      this.saveCourseCache();
      // 돌아오면 추천 탭으로 열기
      sessionStorage.setItem('pdv:tabAfterBack', 'recommend');
      // 추천 카드/지도 캐시도 재사용
      sessionStorage.setItem('pdv:courseCacheKey', this.cacheKey);
      // (선택) 복귀용 정보
      sessionStorage.setItem(
        'pdv:returnTo',
        JSON.stringify({
          type: this.pageType,
          id: this.currentId,
          query: this.$route.query
        })
      );
      return next(); // 캐시는 유지
    }
    // 투어가 아니면 평소처럼 캐시 클리어
    this.clearCourseCache();
    next();
  },

  activated() {
    this.restoreTabIfCameBack();
  },

  // Options API에서 Pinia 사용
  setup() {
    const authStore = useAuthStore();
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);
    return {
      isLoggedIn,
      currentUserId
    };
  },

  computed: {
    computedIsFavorite() {
      return this.isWished;
    },
    cacheKey() {
      return `course-cache:${this.pageType}:${this.currentId}`;
    }
  },

  data() {
    return {
      courseRerenderKey: 0,
      currentId: null,
      pageType: null, // 'exhibition' | 'science_place'
      currentTab: 'detail',
      isWished: false,

      // 전시 상세
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
      isLoading: false,

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

      // 장소 상세
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

      // 추천 코스
      courseItems: [],
      hasLoadedRecommendations: false,
      isRecommending: false,

      // 로딩 애니메이션
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
      tipInterval: null
    };
  },

  created() {
    // URL에서 ID/타입 결정
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

  mounted() {
    this.restoreTabIfCameBack();
    // 브라우저 bfcache 뒤로가기 케이스 대응
    this._onPageShow = () => this.restoreTabIfCameBack();
    window.addEventListener('pageshow', this._onPageShow);
  },

  watch: {
    '$route.params.id'(newId, oldId) {
      if (oldId && newId && newId !== oldId) {
        const oldKey = `course-cache:${this.pageType}:${oldId}`;
        this.clearCourseCache(oldKey);
        this.hasLoadedRecommendations = false;

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
    currentUserId(newUserId, oldUserId) {
      if (newUserId && !oldUserId && this.currentId) {
        if (this.pageType === 'exhibition') {
          this.fetchExhibitionData(this.currentId);
        } else if (this.pageType === 'science_place') {
          this.fetchPlaceData(this.currentId);
        }
      } else if (!newUserId && oldUserId) {
        this.isWished = false;
      }
    }
  },

  beforeUnmount() {
    this.clearLoadingIntervals();
    if (this._onPageShow) window.removeEventListener('pageshow', this._onPageShow);
  },

  methods: {

    async handleSaveRecommendedCourse(items) {
      console.log('[Parent] save received, items =', Array.isArray(items) ? items.length : items);

      // 로그인 가드
      if (!this.isLoggedIn) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => this.$router.push({ name: 'login' })
        });
        return;
      }
      if (!Array.isArray(items) || items.length === 0) {
        eventBus.emit('show-global-alert', { message: '저장할 코스 정보가 없습니다.', type: 'error' });
        return;
      }

      try {
        const currentItemData = (this.pageType === 'science_place') ? this.place : this.exhibition;
        const scheduleName = `AI 추천: ${currentItemData.title || '코스'}`;
        const sourceId = this.currentId;

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

    // 투어 라우트 감지 (name/path 모두 느슨하게)
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

    // 뒤로 복귀 시 탭/캐시 복원
    async restoreTabIfCameBack() {
      const desired = sessionStorage.getItem('pdv:tabAfterBack');
      if (desired === 'recommend') {
        this.currentTab = 'recommend';

        const key = sessionStorage.getItem('pdv:courseCacheKey');
        if (key && key === this.cacheKey) {
          const reused = this.loadCourseCache();
          if (!reused && !this.hasLoadedRecommendations) {
            this.hasLoadedRecommendations = false;
            this.fetchRecommendedCourse();
          }
        } else {
          if (!this.hasLoadedRecommendations) this.fetchRecommendedCourse();
        }

        sessionStorage.removeItem('pdv:tabAfterBack');
        sessionStorage.removeItem('pdv:courseCacheKey');
      }
    },

    loadCourseCache() {
      try {
        const raw = sessionStorage.getItem(this.cacheKey);
        if (!raw) return false;
        const { items } = JSON.parse(raw);
        if (!Array.isArray(items) || items.length === 0) return false;
        this.courseItems = items;
        this.hasLoadedRecommendations = true;
        this.courseRerenderKey = Date.now(); // ⬅️ 자식 강제 리렌더
        console.log('♻️ 코스 캐시 재사용:', this.courseItems);
        return true;
      } catch (e) {
        console.warn('코스 캐시 파싱 실패:', e);
        return false;
      }
    },

    saveCourseCache() {
      try {
        const payload = { items: this.courseItems };
        sessionStorage.setItem(this.cacheKey, JSON.stringify(payload));
        console.log('💾 코스 캐시 저장 완료:', this.cacheKey);
      } catch (e) {
        console.warn('코스 캐시 저장 실패:', e);
      }
    },

    clearCourseCache(key = this.cacheKey) {
      try {
        sessionStorage.removeItem(key);
        console.log('🧹 코스 캐시 제거:', key);
      } catch (e) {
        console.warn('코스 캐시 제거 실패:', e);
      }
    },

    // DTO -> 상태 매핑 (Exhibition)
    mapExhibitionDTO(dto) {
      const title = dto.exhibitionHallName ?? '제목 없음';

      const category = this.$route.query.mainCategoryTags ?? '';
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;

      let subCategoriesArray = [];
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } else if (Array.isArray(subCategoryData)) {
        subCategoriesArray = subCategoryData.map(tag => String(tag).trim()).filter(Boolean);
      }

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

      //this.courseItems = [];
    },

    // DTO -> 상태 매핑 (Place)
    mapPlaceDTO(dto) {
      const title = dto.placeName ?? '제목 없음';
      const category = this.$route.query.mainCategoryTags ?? '';
      const subCategoryData = this.$route.query.subCategoryTags;
      const grade = this.$route.query.gradeTags;

      let subCategoriesArray = [];
      if (typeof subCategoryData === 'string') {
        subCategoriesArray = subCategoryData
          .split(',')
          .map(tag => tag.trim())
          .filter(Boolean);
      } else if (Array.isArray(subCategoryData)) {
        subCategoriesArray = subCategoryData.map(tag => String(tag).trim()).filter(Boolean);
      }

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

      this.placeInformation = {
        placeAddress: dto.addressDetail ?? '정보 없음',
        operationPeriod: this.formatPeriod(null, null),
        operationHours: dto.openingHours ?? '정보 없음',
        entranceFee: dto.admissionFee ?? '정보 없음',
        lat: dto.latitude,
        lng: dto.longitude
      };

      //this.courseItems = [];
    },

    // Helper: 기간
    formatPeriod(start, end) {
      if (!start && !end) return '상시 운영';
      if (start && !end) return `${start} ~ 별도 안내까지`;
      if (!start && end) return `~ ${end}`;
      return `${start} ~ ${end}`;
    },

    // Helper: 요금
    formatFee(fee) {
      if (fee === null || fee === undefined) return '정보 없음';
      if (fee === 0) return '무료';
      return `${fee.toLocaleString('ko-KR')}원`;
    },

    /** 전시 상세 조회 */
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

    /** 장소 상세 조회 */
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

    refreshData() {
      if (this.pageType === 'exhibition') {
        this.fetchExhibitionData(this.currentId);
      } else if (this.pageType === 'science_place') {
        this.fetchPlaceData(this.currentId);
      }
    },

    handleReviewPosted() {
      this.refreshData();
    },

    handleReviewDeleted() {
      this.refreshData();
    },

    handleTabChange(tabName) {
      this.currentTab = tabName;

      if (tabName === 'recommend') {
        const reused = this.loadCourseCache();
        if (reused) return;

        if (!this.hasLoadedRecommendations) {
          this.fetchRecommendedCourse();
        }
      }
    },

    // 로딩 애니메이션 제어
    startLoadingAnimation() {
      this.currentStepIndex = 0;
      this.currentMessageIndex = 0;

      this.stepInterval = setInterval(() => {
        if (this.currentStepIndex < this.progressSteps.length - 1) {
          this.currentStepIndex++;
          this.currentMessageIndex++;
        }
      }, 1200);

      this.currentTipIndex = 0;
      this.tipInterval = setInterval(() => {
        this.currentTipIndex = (this.currentTipIndex + 1) % this.tips.length;
      }, 2500);
    },

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

    // 추천 코스 불러오기
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

        const [res] = await Promise.all([
          axios.get(apiUrl, { params }),
          new Promise(resolve => setTimeout(resolve, 3500))
        ]);

        const aiRecommendedDtos = res.data;

        const currentItemData = this.pageType === 'science_place' ? this.place : this.exhibition;
        const currentItemInfo =
          this.pageType === 'science_place' ? this.placeInformation : this.exhibitionInformation;

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

        this.courseItems = [currentItemFormatted, ...aiItemsFormatted];
        this.hasLoadedRecommendations = true;

        this.saveCourseCache();
        this.courseRerenderKey = Date.now(); // ⬅️ 자식 강제 리렌더
      } catch (error) {
        console.error('AI 추천 코스 로딩 실패:', error);
        this.hasLoadedRecommendations = true;

        eventBus.emit('show-global-alert', {
          message: 'AI 추천 코스를 불러오는데 실패했습니다. 다시 시도해주세요.',
          type: 'error'
        });
      } finally {
        setTimeout(() => {
          this.clearLoadingIntervals();
          this.isRecommending = false;
          this.currentStepIndex = 0;
          this.currentMessageIndex = 0;
          console.log('🏁 fetchRecommendedCourse 완료. isRecommending:', this.isRecommending);
        }, 300);
      }
    },

    // 찜 토글
    async handleToggleFavorite() {
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
          await axios.delete(`/api/wishlist`, { data: requestData });
          this.isWished = false;
          eventBus.emit('show-global-alert', { message: '찜 목록에서 삭제되었습니다.', type: 'success' });
        } else {
          await axios.post(`/api/wishlist`, requestData);
          console.log('[wishlist] add payload:', requestData);
          this.isWished = true;
          eventBus.emit('show-global-alert', { message: '찜 목록에 추가되었습니다.', type: 'success' });
        }
      } catch (error) {
        const status = error.response?.status;
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
        } else if (status === 403) {
          eventBus.emit('show-global-alert', {
            message: '로그인이 필요하거나 권한이 없습니다.',
            type: 'error'
          });
        } else {
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

    // 방문 인증
    async handleVisitAuthentication() {
      console.log('PlaceDetailView: 방문 인증 시작');
      try {
        const isAlreadyVisited =
          this.pageType === 'exhibition' ? this.exhibition.visited : this.place.visited;
        if (isAlreadyVisited) {
          eventBus.emit('show-global-alert', { message: '이미 방문한 장소입니다.', type: 'error' });
          return;
        }

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

        const coords = await this.getUserCoordinates();

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

    // 데모용 좌표 반환(로컬 테스트)
    getUserCoordinates() {
      console.log('GPS: localhost 임시 좌표 사용');
      const DEMO_LOCATION = {
        latitude: 36.6448020,
        longitude: 127.4714750
      };
      return new Promise(resolve => {
        setTimeout(() => {
          console.log('GPS 좌표 획득 성공 (임시)', DEMO_LOCATION);
          resolve(DEMO_LOCATION);
        }, 500);
      });
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
