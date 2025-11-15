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

      <div v-if="pageType === 'exhibition'">
        <InfoSection :exhibition="exhibition" imageTag="전시 태그" :mainCategory="exhibition.mainCategory"
          :subCategories="exhibition.subCategories" :gradeTag="exhibition.gradeTag"
          @authenticate-visit="handleVisitAuthentication" />
        <hr class="divider" />

        <!-- 가상 실험 버튼 개선 -->
        <button type="button" class="experiment-toggle-btn" v-if="currentSimulationComponent"
          @click="showSimulation = !showSimulation">
          <span class="btn-content">
            <i class="bi bi-flask-fill"></i>
            <span class="btn-text">가상 실험</span>
          </span>
          <i class="bi chevron-icon" :class="showSimulation ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </button>

        <!-- 가상 실험 컨테이너 개선 -->
        <transition name="slide-fade">
          <div v-if="showSimulation && currentSimulationComponent" class="simulation-wrapper">
            <div class="simulation-container">
              <div class="simulation-inner">
                <div class="simulation-header">
                  <div class="header-content">
                    <div class="header-title">
                      <i class="bi bi-flask-fill header-icon"></i>
                      <h5 class="title-text">{{ experimentTitle }}</h5>
                    </div>
                    <button type="button" class="btn-close-simulation" @click="showSimulation = false" title="닫기"
                      aria-label="시뮬레이션 닫기">
                      <i class="bi bi-x-lg"></i>
                    </button>
                  </div>
                </div>
                <div class="simulation-content">
                  <component :is="currentSimulationComponent"></component>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <TabSection :key="currentTab" :isPlace="false" :activeTab="currentTab" @updateTab="handleTabChange" />

        <div v-if="currentTab === 'detail'">
          <ContentDetailView :exhibitionInformation="exhibitionInformation" :exhibition="exhibition" :isPlace="false"
            :target-id="currentId" :target-type="pageType" @review-posted="handleReviewPosted"
            @review-deleted="handleReviewDeleted" :photo-review-count="exhibition.photoReviewCount" />
        </div>

        <!-- AI 추천 탭 개선 -->
        <div v-else-if="currentTab === 'recommend'">
          <!-- 로딩 상태 -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">

              <!-- AI 아이콘 섹션 개선 -->
              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <div class="ai-icon-bg">
                    <span class="ai-icon">🤖</span>
                  </div>
                  <div class="pulse-ring"></div>
                  <div class="pulse-ring-delayed"></div>
                </div>
              </div>

              <!-- 메시지 섹션 개선 -->
              <div class="loading-message">
                <h3 class="main-title">AI가 맞춤 코스를 생성 중입니다</h3>
                <transition name="fade" mode="out-in">
                  <p class="sub-message" :key="currentMessageIndex">
                    {{ loadingMessages[currentMessageIndex] }}
                  </p>
                </transition>
              </div>

              <!-- 프로그레스 바 추가 -->
              <div class="progress-bar-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
                </div>
                <p class="progress-text">{{ progressPercentage }}%</p>
              </div>

              <!-- 진행 단계 개선 -->
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

              <!-- 스켈레톤 카드 개선 -->
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

              <!-- 팁 메시지 개선 -->
              <transition name="fade" mode="out-in">
                <div class="loading-tip" :key="currentTipIndex">
                  <span class="tip-icon">💡</span>
                  <span class="tip-text">{{ tips[currentTipIndex] }}</span>
                </div>
              </transition>
            </div>
          </div>

          <!-- 추천 결과 -->
          <CourseRecommend v-else :key="courseRerenderKey" :course-items="courseItems" :type="pageType"
            :is-loading="isRecommending" @request-new-course="fetchRecommendedCourse"
            @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

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

        <div v-else-if="currentTab === 'recommend'">
          <!-- 로딩 상태 (전시와 동일) -->
          <div v-if="isRecommending" class="recommend-loading-container">
            <div class="loading-content">

              <div class="ai-icon-section">
                <div class="ai-icon-wrapper">
                  <div class="ai-icon-bg">
                    <span class="ai-icon">🤖</span>
                  </div>
                  <div class="pulse-ring"></div>
                  <div class="pulse-ring-delayed"></div>
                </div>
              </div>

              <div class="loading-message">
                <h3 class="main-title">AI가 맞춤 코스를 생성 중입니다</h3>
                <transition name="fade" mode="out-in">
                  <p class="sub-message" :key="currentMessageIndex">
                    {{ loadingMessages[currentMessageIndex] }}
                  </p>
                </transition>
              </div>

              <div class="progress-bar-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
                </div>
                <p class="progress-text">{{ progressPercentage }}%</p>
              </div>

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

              <transition name="fade" mode="out-in">
                <div class="loading-tip" :key="currentTipIndex">
                  <span class="tip-icon">💡</span>
                  <span class="tip-text">{{ tips[currentTipIndex] }}</span>
                </div>
              </transition>
            </div>
          </div>

          <CourseRecommend v-else :course-items="courseItems" :type="pageType" :is-loading="isRecommending"
            @request-new-course="fetchRecommendedCourse" @save-recommended-course="handleSaveRecommendedCourse" />
        </div>
      </div>

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

// 하위 컴포넌트들
import ExhibitionHeader from '@/components/header/ExhibitionHeader.vue';
import InfoSection from '@/components/section/InfoSection.vue';
import TabSection from '@/components/section/TabSection.vue';
import ContentDetailView from './ContentDetailView.vue';
import CourseRecommend from './CourseRecommend.vue';

import ColumnarJoint from '@/components/simulations/ColumnarJoint.vue';
import StatesOfMatter from '@/components/simulations/StatesOfMatterSimulation.vue';
import Ecosystem from '@/components/simulations/EcosystemSimulation.vue';
import MagnetField from '@/components/simulations/MagnetField.vue';
import ThermalConductivity from '@/components/simulations/ThermalConductivitySim.vue';

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
    ColumnarJoint,
    StatesOfMatter,
    Ecosystem,
    MagnetField,
    ThermalConductivity
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
    },
    simulationMap() {
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
      const grade = this.exhibition?.gradeTag;
      const subject = this.exhibition?.mainCategory;

      console.log(`[Sim Match] Grade: ${grade}, Subject: ${subject}`);
      if (grade && subject && this.simulationMap[grade] && this.simulationMap[grade][subject]) {
        return this.simulationMap[grade][subject];
      }
      return null;
    },
    experimentTitle() {
      return `가상실험: ${this.exhibition?.gradeTag} ${this.exhibition?.mainCategory}`;
    },
    // 프로그레스 퍼센트 계산
    progressPercentage() {
      const total = this.progressSteps.length;
      const current = this.currentStepIndex + 1;
      return Math.min(Math.round((current / total) * 100), 100);
    }
  },

  data() {
    return {
      showSimulation: false,
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
        this.courseRerenderKey = Date.now();
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
      return `${fee.toLocaleString('ko-KR')}`;
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
        this.courseRerenderKey = Date.now();
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
/* ========================================
   공통 레이아웃
======================================== */
.exhibition-detail-page {
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

  /* 스크롤바 숨기기 */
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
   가상 실험 버튼 (개선)
======================================== */
.experiment-toggle-btn {
  width: calc(100% - 40px);
  margin: 0 20px 20px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 12px;
  border: 1px solid #4A7CEC;
  color: #4A7CEC;
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.experiment-toggle-btn:hover {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.2);
}

.experiment-toggle-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(74, 124, 236, 0.15);
}

.experiment-toggle-btn .btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.experiment-toggle-btn .bi-flask-fill {
  font-size: 18px;
}

.experiment-toggle-btn .chevron-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

/* ========================================
   시뮬레이션 컨테이너 (개선)
======================================== */
.simulation-wrapper {
  margin: 0 20px 20px 20px;
}

.simulation-container {
  animation: slideDownIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.simulation-inner {
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.03) 0%, rgba(16, 185, 129, 0.03) 100%);
  border: 1px solid rgba(74, 124, 236, 0.12);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(74, 124, 236, 0.08);
}

.simulation-header {
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.08), rgba(16, 185, 129, 0.06));
  padding: 16px 20px;
  border-bottom: 1px solid rgba(74, 124, 236, 0.1);
  backdrop-filter: blur(10px);
}

.simulation-header .header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.simulation-header .header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.simulation-header .header-icon {
  color: #4A7CEC;
  font-size: 20px;
}

.simulation-header .title-text {
  color: #333;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}

.btn-close-simulation {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.btn-close-simulation:hover {
  background: rgba(255, 255, 255, 1);
  color: #333;
  border-color: rgba(0, 0, 0, 0.12);
}

.simulation-content {
  padding: 24px 20px;
  background: #fff;
}

/* 슬라이드 애니메이션 */
.slide-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-15px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes slideDownIn {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========================================
   AI 추천 로딩 (대폭 개선)
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

/* AI 아이콘 섹션 개선 */
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

/* 메시지 섹션 개선 */
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

/* 프로그레스 바 추가 */
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

/* 진행 단계 개선 */
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

/* 체크 페이드 애니메이션 */
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

/* 스켈레톤 카드 개선 */
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

/* Shimmer 효과 개선 */
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

/* 팁 메시지 개선 */
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

/* 페이드 트랜지션 */
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

  .experiment-toggle-btn {
    padding: 12px 16px;
    font-size: 14px;
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
