<template>
  <div class="activity-recommender-section">
    <h4 class="section-title">
      🤖 AI 맞춤 체험활동 추천
    </h4>

    <div class="criteria-summary" v-if="canRecommend">
      <p class="section-description">
        이 장소의 <strong class="text-primary">{{ placeMainCategory }}</strong> 과목과
        <strong class="text-primary">' {{ placeGradeTag }}'</strong> 을 기반으로
        체험활동을 제안합니다.
      </p>
    </div>

    <div v-else class="criteria-summary">
      <p class="section-description text-muted">
        이 장소에 연계된 교과 정보가 부족하여 AI 추천을 제공할 수 없습니다.
      </p>
    </div>

    <div class="button-wrapper">
      <button class="btn btn-primary w-100" @click="fetchRecommendations" :disabled="isLoading || !canRecommend">
        <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        <span v-else>AI 추천 받기</span>
      </button>
    </div>

    <div v-if="isLoading" class="loading-placeholder">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중</span>
      </div>
      <p>AI가 {{ placeGradeTag }} '{{ placeMainCategory }}' 단원과<br> 관련된 활동을 찾고 있습니다...</p>
    </div>

    <div class="activity-recommender-section">
      <div v-if="!isLoading && recommendations.length > 0" class="results-wrapper">

        <div v-for="(rec, index) in recommendations" :key="index" class="recommend-card">
          <div class="card-header">
            <span class="card-badge">{{ rec.relatedUnit }}</span>
            <h5 class="card-title">{{ rec.title }}</h5>
          </div>
          <p class="card-description">{{ rec.description }}</p>
          <div v-if="rec.relatedExhibit" class="card-location">
            <i class="bi bi-geo-alt-fill"></i>
            활동 위치: <strong>{{ rec.relatedExhibit }}</strong>
          </div>
        </div>


      </div>

      <div v-if="!isLoading && recommendations.length === 0 && hasSearched" class="no-results">
        <p>아쉽지만, 해당 조건으로 추천할 만한<br> 맞춤 활동을 찾지 못했어요. 😥</p>

      </div>
    </div>
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
    // [추가] 학년 태그를 props로 직접 받음
    placeGradeTag: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      // [제거] selectedGrade, selectedUnit
      isLoading: false,
      hasSearched: false,
      recommendations: []
    };
  },
  computed: {
    // [제거] availableUnits

    // [추가] "초등 6학년" -> "6"으로 변환하는 헬퍼
    parsedGrade() {
      if (!this.placeGradeTag) return null;
      // 정규식으로 숫자만 추출
      const match = this.placeGradeTag.match(/(\d+)학년/);
      return match ? match[1] : null; // "6"
    },

    // [추가] 추천이 가능한지 (필수 정보가 있는지) 확인
    canRecommend() {
      // 장소 ID, 파싱된 학년, 메인 단원 정보가 모두 있어야 함
      return !!this.placeId && !!this.parsedGrade && !!this.placeMainCategory;
    }
  },
  // [제거] watch (availableUnits)

  methods: {
    async fetchRecommendations() {
      // [수정] canRecommend computed로 유효성 검사
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
            // [수정] props에서 파싱한 값 사용
            grade: this.parsedGrade,
            unit: this.placeMainCategory
          }
        });

        await new Promise(resolve => setTimeout(resolve, 1000));
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
/* === 1. 컴포넌트 기본 레이아웃 === */
.activity-recommender-section {
  padding: 20px;
  background-color: #f8f9fa;
  /* 상위 섹션(흰색)과 구분되는 배경색 */
  border-radius: 8px;
}

.section-title {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 15px;
  /* 제목과 설명 사이 간격 */
}

/* === 2. 추천 기준 요약 (필터 대신) === */
.criteria-summary .section-description {
  font-size: 0.95rem;
  line-height: 1.6;
  color: #333;
  margin-bottom: 0;
}

.criteria-summary .text-primary {
  color: #0d6efd !important;
  /* 부트스트랩 primary 색상 강조 */
  font-weight: 600;
}

.criteria-summary .text-muted {
  font-size: 0.9rem;
  line-height: 1.5;
}

/* === 3. 추천 받기 버튼 === */
.button-wrapper {
  margin-top: 20px;
}

.btn-primary {
  font-weight: 600;
  padding: 0.6rem 0.75rem;
  /* 버튼 크기 */
  font-size: 0.95rem;
}

/* === 4. 로딩 및 결과 없음 === */
.loading-placeholder,
.no-results {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
}

.loading-placeholder .spinner-border {
  margin-bottom: 15px;
}

/* (웹 접근성) 스크린 리더용 */
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}


/* === 5. AI 추천 결과 카드 (v-for) === */
.results-wrapper {
  margin-top: 25px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 카드 사이 간격 */
}

.recommend-card {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.recommend-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  margin-bottom: 10px;
}

.card-badge {
  background-color: #e3f2fd;
  /* 파란 계열 */
  color: #0366d6;
  font-size: 0.75rem;
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: 500;
  display: inline-block;
}

.card-title {
  font-size: 1.05rem;
  font-weight: 600;
  margin-top: 8px;
  color: #222;
}

.card-description {
  font-size: 0.95rem;
  color: #444;
  line-height: 1.6;
  white-space: pre-line;
  /* AI가 \n으로 줄바꿈하면 반영 */
}

.card-location {
  font-size: 0.9rem;
  color: #555;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

.card-location i {
  color: #0d6efd;
  /* 부트스트랩 primary 색상 */
  margin-right: 4px;
}
</style>
