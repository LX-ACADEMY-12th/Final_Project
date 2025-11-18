<template>
  <div class="activity-recommender-section">

    <!-- 헤더 섹션 개선 -->
    <div class="section-header">
      <div class="header-icon-wrapper">
        <div class="icon-bg">
          <span class="header-icon">🎯</span>
        </div>
      </div>
      <div class="header-content">
        <h4 class="section-title">AI 맞춤 체험활동 추천</h4>
        <p class="section-subtitle">교과 연계 맞춤 활동을 찾아드려요</p>
      </div>
    </div>

    <!-- 기준 정보 카드 -->
    <div v-if="canRecommend" class="criteria-card">
      <div class="criteria-content">
        <div class="criteria-text">
          <p class="criteria-description">
            <span class="highlight-badge">{{ placeGradeTag }}</span>
            <span class="highlight-badge subject-badge">{{ placeMainCategory }}</span>
            기준으로 추천합니다
          </p>
        </div>
      </div>
    </div>

    <!-- 정보 부족 안내 -->
    <div v-else class="criteria-card warning-card">
      <div class="criteria-content">
        <div class="criteria-icon warning">
          <i class="bi bi-exclamation-circle-fill"></i>
        </div>
        <div class="criteria-text">
          <p class="criteria-description">
            이 장소에 연계된 교과 정보가 부족하여<br>
            AI 추천을 제공할 수 없습니다.
          </p>
        </div>
      </div>
    </div>

    <!-- 추천 버튼 개선 -->
    <div class="button-wrapper">
      <button class="btn-recommend" @click="fetchRecommendations" :disabled="isLoading || !canRecommend"
        :class="{ 'loading': isLoading, 'disabled': !canRecommend }">
        <span v-if="isLoading" class="btn-content loading">
          <span class="spinner"></span>
          <span class="btn-text">AI가 분석 중...</span>
        </span>
        <span v-else class="btn-content">
          <i class="bi bi-stars"></i>
          <span class="btn-text">AI 추천 받기</span>
        </span>
      </button>
    </div>

    <!-- 로딩 상태 개선 -->
    <transition name="fade">
      <div v-if="isLoading" class="loading-container">
        <div class="loading-animation">
          <div class="robot-icon">
            <span class="robot-emoji">🤖</span>
            <div class="loading-pulse"></div>
          </div>
          <div class="loading-text">
            <h5 class="loading-title">AI가 분석 중입니다</h5>
            <p class="loading-description">
              {{ placeGradeTag }} <strong>{{ placeMainCategory }}</strong> 단원과<br>
              관련된 체험활동을 찾고 있습니다
            </p>
          </div>

          <!-- 로딩 스켈레톤 -->
          <div class="skeleton-cards">
            <div v-for="n in 2" :key="n" class="skeleton-card" :style="{ animationDelay: `${n * 0.1}s` }">
              <div class="skeleton-header">
                <div class="skeleton-badge"></div>
              </div>
              <div class="skeleton-title"></div>
              <div class="skeleton-desc line-1"></div>
              <div class="skeleton-desc line-2"></div>
              <div class="skeleton-location"></div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 추천 결과 개선 -->
    <transition-group name="slide-up" tag="div" class="results-wrapper">
      <div v-for="(rec, index) in recommendations" :key="`rec-${index}`" class="recommend-card"
        :style="{ animationDelay: `${index * 0.1}s` }">

        <div class="card-header">
          <div class="badge-wrapper">
            <span class="unit-badge">
              <i class="bi bi-book"></i>
              {{ rec.relatedUnit }}
            </span>
          </div>
          <h5 class="card-title">{{ rec.title }}</h5>
        </div>

        <p class="card-description">{{ rec.description }}</p>

        <div v-if="rec.relatedExhibit" class="card-footer">
          <div class="location-info">
            <i class="bi bi-geo-alt-fill"></i>
            <span class="location-label">활동 위치</span>
            <span class="location-value">{{ rec.relatedExhibit }}</span>
          </div>
        </div>
      </div>
    </transition-group>

    <!-- 결과 없음 개선 -->
    <transition name="fade">
      <div v-if="!isLoading && recommendations.length === 0 && hasSearched" class="no-results">
        <div class="no-results-icon">
          <span class="icon-emoji">🔍</span>
        </div>
        <h5 class="no-results-title">추천 결과가 없습니다</h5>
        <p class="no-results-text">
          해당 조건으로 추천할 만한<br>
          맞춤 활동을 찾지 못했어요
        </p>
        <button class="btn-retry" @click="fetchRecommendations">
          <i class="bi bi-arrow-clockwise"></i>
          다시 시도하기
        </button>
      </div>
    </transition>

  </div>
</template>

<script>
import axios from '@/api/axiosSetup';

export default {
  name: 'ActivityRecommender',
  props: {
    placeId: {
      type: [String, Number],
      required: true
    },
    placeMainCategory: String,
    placeSubCategories: {
      type: Array,
      default: () => []
    },
    placeGradeTag: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      isLoading: false,
      hasSearched: false,
      recommendations: []
    };
  },
  computed: {
    parsedGrade() {
      if (!this.placeGradeTag) return null;
      const match = this.placeGradeTag.match(/(\d+)학년/);
      return match ? match[1] : null;
    },
    canRecommend() {
      return !!this.placeId && !!this.parsedGrade && !!this.placeMainCategory;
    }
  },
  methods: {
    async fetchRecommendations() {
      if (!this.canRecommend) {
        alert('추천에 필요한 교과 정보(학년 또는 단원)가 부족합니다.');
        return;
      }
      this.isLoading = true;
      this.hasSearched = true;
      this.recommendations = [];

      try {
        const response = await axios.get('/api/recommend/activities', {
          params: {
            placeId: this.placeId,
            grade: this.parsedGrade,
            unit: this.placeMainCategory
          }
        });

        // 최소 로딩 시간 (UX)
        await new Promise(resolve => setTimeout(resolve, 1500));
        this.recommendations = response.data;

      } catch (error) {
        console.error("AI 체험활동 추천 실패:", error);
        alert("추천 활동을 불러오는 데 실패했습니다.");
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style scoped>
/* ========================================
   컴포넌트 기본 레이아웃
======================================== */
.activity-recommender-section {
  padding: 24px 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8faff 100%);
  border-radius: 16px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* ========================================
   헤더 섹션 개선
======================================== */
.section-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.header-icon-wrapper {
  flex-shrink: 0;
}

.icon-bg {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4A7CEC 0%, #5B8EF5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.25);
}

.header-icon {
  font-size: 28px;
}

.header-content {
  flex: 1;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  line-height: 1.3;
}

.section-subtitle {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

/* ========================================
   기준 정보 카드
======================================== */
.criteria-card {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
  border: 1px solid rgba(74, 124, 236, 0.15);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.08);
}

.criteria-card.warning-card {
  background: linear-gradient(135deg, #fff8f0 0%, #fff4e8 100%);
  border-color: rgba(255, 152, 0, 0.2);
}

.criteria-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.criteria-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(74, 124, 236, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.criteria-icon i {
  font-size: 20px;
  color: #4A7CEC;
}

.criteria-icon.warning {
  background: rgba(255, 152, 0, 0.15);
}

.criteria-icon.warning i {
  color: #ff9800;
}

.criteria-text {
  flex: 1;
  padding-top: 2px;
}

.criteria-description {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
}

.highlight-badge {
  display: inline-block;
  background: rgba(74, 124, 236, 0.12);
  color: #4A7CEC;
  padding: 3px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  margin: 0 4px;
}

.highlight-badge.subject-badge {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}

/* ========================================
   추천 버튼 개선
======================================== */
.button-wrapper {
  margin-bottom: 20px;
}

.btn-recommend {
  width: 100%;
  padding: 14px 20px;
  background: linear-gradient(135deg, #4A7CEC 0%, #5B8EF5 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.25);
  position: relative;
  overflow: hidden;
}

.btn-recommend::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-recommend:hover:not(.disabled):not(.loading) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 124, 236, 0.35);
}

.btn-recommend:hover:not(.disabled):not(.loading)::before {
  left: 100%;
}

.btn-recommend:active:not(.disabled):not(.loading) {
  transform: translateY(0);
  box-shadow: 0 3px 8px rgba(74, 124, 236, 0.2);
}

.btn-recommend.disabled {
  background: linear-gradient(135deg, #cbd5e1 0%, #94a3b8 100%);
  cursor: not-allowed;
  box-shadow: none;
}

.btn-recommend.loading {
  cursor: wait;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-content i {
  font-size: 18px;
}

.btn-text {
  font-size: 15px;
}

/* 스피너 애니메이션 */
.spinner {
  width: 18px;
  height: 18px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ========================================
   로딩 상태 개선
======================================== */
.loading-container {
  margin-top: 20px;
}

.loading-animation {
  text-align: center;
  padding: 30px 20px;
}

.robot-icon {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.robot-emoji {
  font-size: 56px;
  display: inline-block;
  animation: float 2.5s ease-in-out infinite;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-10px);
  }
}

.loading-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 70px;
  height: 70px;
  border: 3px solid rgba(74, 124, 236, 0.3);
  border-radius: 50%;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
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

.loading-text {
  margin-bottom: 24px;
}

.loading-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.loading-description {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin: 0;
}

.loading-description strong {
  color: #4A7CEC;
  font-weight: 600;
}

/* 스켈레톤 카드 */
.skeleton-cards {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  background: white;
  border: 1px solid #efefef;
  border-radius: 14px;
  padding: 16px;
  opacity: 0;
  animation: skeleton-slide-up 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes skeleton-slide-up {
  from {
    transform: translateY(15px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.skeleton-header {
  margin-bottom: 12px;
}

.skeleton-badge {
  width: 80px;
  height: 22px;
  background: #f5f5f5;
  border-radius: 11px;
  position: relative;
  overflow: hidden;
}

.skeleton-badge::after,
.skeleton-title::after,
.skeleton-desc::after,
.skeleton-location::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: shimmer 1.8s infinite;
}

@keyframes shimmer {
  0% {
    left: -100%;
  }

  100% {
    left: 100%;
  }
}

.skeleton-title {
  width: 70%;
  height: 20px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.skeleton-desc {
  height: 16px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-bottom: 8px;
  position: relative;
  overflow: hidden;
}

.skeleton-desc.line-1 {
  width: 100%;
}

.skeleton-desc.line-2 {
  width: 85%;
}

.skeleton-location {
  width: 60%;
  height: 16px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-top: 12px;
  position: relative;
  overflow: hidden;
}

/* ========================================
   추천 결과 카드 개선
======================================== */
.results-wrapper {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  animation: card-appear 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes card-appear {
  from {
    transform: translateY(20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(74, 124, 236, 0.15);
  border-color: rgba(74, 124, 236, 0.3);
}

.card-header {
  margin-bottom: 12px;
}

.badge-wrapper {
  margin-bottom: 10px;
}

.unit-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #EFF6FF;
  /* 더 부드러운 배경색 */
  color: #3B82F6;
  /* 선명한 파란색 텍스트 */
  font-size: 12px;
  padding: 6px 12px;
  /* 여백 조금 더 줌 */
  border-radius: 99px;
  /* [변경] 둥근 캡슐 모양 */
  font-weight: 700;
  border: 1px solid rgba(59, 130, 246, 0.1);
  /* 아주 연한 테두리 추가 */
}

.unit-badge i {
  font-size: 13px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.4;
}

.card-description {
  font-size: 14px;
  color: #444;
  line-height: 1.7;
  margin: 12px 0 0 0;
  white-space: pre-line;
}

.card-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #F0F0F0;
  /* 점선 대신 얇은 실선으로 깔끔하게 */
}

.location-info {
  display: flex;
  /* [필수] 가로 배치 */
  align-items: flex-start;
  /* [핵심] 글자가 두 줄 이상일 때 위쪽 기준 정렬 */
  gap: 8px;
  /* 아이콘, 라벨, 내용 사이 간격 */
  font-size: 13.5px;
  /* 글자 크기 미세 조정 */
  color: #555;
  line-height: 1.5;
  /* 줄 간격 확보 */
}

.location-info i {
  color: #4A7CEC;
  font-size: 16px;
  flex-shrink: 0;
  /* 아이콘 찌그러짐 방지 */
}

.location-label {
  color: #888;
  font-weight: 500;
  flex-shrink: 0;
  /* [핵심] 공간 좁아도 줄바꿈 안 됨 */
  white-space: nowrap;
  /* [핵심] "활동 위치" 글자 깨짐 방지 */
}

.location-value {
  color: #1A1A1A;
  font-weight: 700;
  /* 강조 */
  word-break: keep-all;
  /* [핵심] 한글 단어 중간 끊김 방지 (매우 중요) */
  flex: 1;
  /* 남은 공간 꽉 채우기 */
}

/* ========================================
   결과 없음 개선
======================================== */
.no-results {
  text-align: center;
  padding: 50px 20px;
  background: white;
  border-radius: 14px;
  border: 1px dashed #e0e0e0;
  margin-top: 20px;
}

.no-results-icon {
  margin-bottom: 16px;
}

.icon-emoji {
  font-size: 48px;
  display: inline-block;
  opacity: 0.7;
}

.no-results-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.no-results-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.btn-retry {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
  border: 1px solid rgba(74, 124, 236, 0.2);
  border-radius: 10px;
  padding: 10px 20px;
  color: #4A7CEC;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-retry:hover {
  background: linear-gradient(135deg, #e8f0ff 0%, #dce8ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.15);
}

.btn-retry i {
  font-size: 16px;
}

/* ========================================
   트랜지션 애니메이션
======================================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

/* ========================================
   반응형 디자인
======================================== */
@media (max-width: 480px) {
  .activity-recommender-section {
    padding: 20px 16px;
  }

  .icon-bg {
    width: 50px;
    height: 50px;
  }

  .header-icon {
    font-size: 24px;
  }

  .section-title {
    font-size: 17px;
  }

  .section-subtitle {
    font-size: 12px;
  }

  .criteria-card {
    padding: 14px;
  }

  .criteria-icon {
    width: 36px;
    height: 36px;
  }

  .criteria-icon i {
    font-size: 18px;
  }

  .btn-recommend {
    padding: 12px 18px;
    font-size: 14px;
  }

  .robot-emoji {
    font-size: 48px;
  }

  .loading-title {
    font-size: 16px;
  }

  .loading-description {
    font-size: 13px;
  }

  .recommend-card {
    padding: 16px;
  }

  .card-title {
    font-size: 15px;
  }

  .card-description {
    font-size: 13px;
  }
}

@media (max-width: 360px) {
  .activity-recommender-section {
    padding: 18px 14px;
  }

  .section-header {
    gap: 12px;
  }

  .highlight-badge {
    font-size: 12px;
    padding: 2px 8px;
  }
}
</style>
