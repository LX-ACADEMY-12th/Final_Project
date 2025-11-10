<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        스탬프 투어
      </div>
      <div class="header-right" style="flex: 1;">
        </div>
    </div>

    <div class="content-container">

      <div class="progress-card shadow-sm">
        <div class="mb-2">
          <span class="badge bg-primary-subtle text-primary-emphasis rounded-pill fw-medium">
            <i class="bi bi-patch-check-fill me-1"></i> 나의 스탬프 현황
          </span>
        </div>
        <div class="progress-display">
          <span class="display-4 fw-bolder">{{ collectedStampsCount }}</span>
          <span class="fs-5 text-muted ms-2">/ {{ totalStampsCount }} 개</span>
        </div>
        <div class="progress mt-3" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0" aria-valuemax="100" style="height: 10px;">
          <div class="progress-bar rounded-pill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>

      <div class="segmented-control-wrapper d-flex justify-content-center flex-shrink-0">
        <div class="segmented-control d-flex gap-3">
          <button
            type="button"
            class="spec-button shadow-sm"
            :class="{ 'active': selectedTab === '전시' }"
            @click="selectTab('전시')"
          >
            <i class="bi bi-easel2-fill me-1"></i> 과학관 전시
          </button>
          <button
            type="button"
            class="spec-button shadow-sm"
            :class="{ 'active': selectedTab === '답사' }"
            @click="selectTab('답사')"
          >
            <i class="bi bi-binoculars-fill me-1"></i> 과학 여행
          </button>
        </div>
      </div>

      <div class="stamp-categories d-flex justify-content-around p-3">
        <button
          v-for="category in categories"
          :key="category.key"
          type="button"
          class="category-button d-flex flex-column align-items-center"
          :class="{ 'active': selectedCategory === category.key }"
          @click="selectCategory(category.key)"
        >
          <i :class="['bi', category.icon, 'fs-4', 'mb-1']"></i>
          <span class="fw-medium">{{ category.name }}</span>
        </button>
      </div>

      <div class="stamp-grid-wrapper">
        <div v-if="filteredStamps.length > 0" class="stamp-grid">
          <div
            v-for="stamp in filteredStamps"
            :key="stamp.id"
            class="stamp-item shadow-sm"
            :class="{ 'collected': stamp.collected, 'uncollected': !stamp.collected }"
          >
            <div class="stamp-badge">
              <i v-if="stamp.collected" class="bi bi-check-circle-fill text-success"></i>
              <i v-else class="bi bi-lock-fill text-muted"></i>
            </div>
            
            <div class="stamp-circle">
              <i :class="['bi', stamp.icon]"></i>
            </div>
            
            <div class="stamp-info">
              <div class="stamp-unit">{{ stamp.unitName }}</div>
              <div class="stamp-label">
                {{ stamp.name }}
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-grid-message text-center text-muted">
          <i class="bi bi-emoji-frown fs-1 mb-2"></i>
          <p class="mb-0">
            '{{ selectedTab }}'의 '{{ selectedCategory }}' 과목에<br> 해당하는 스탬프가 없습니다.
          </p>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'StampView',
  data() {
    return {
      // 1. 필터 상태
      selectedTab: '전시', // '전시' 또는 '답사'
      selectedCategory: '물리', // 과목(주제)

      // 2. 필터 버튼 정의
      // 과목(카테고리) 버튼
      categories: [
        { key: '물리', name: '물리', icon: 'bi-magnet-fill' },
        { key: '화학', name: '화학', icon: 'bi-beaker' },
        { key: '생명', name: '생명', icon: 'bi-tree-fill' },
        { key: '지구', name: '지구', icon: 'bi-globe-americas' },
      ],

      // 3. (Mock Data) 'type' (전시/답사) 필드 추가
      allStamps: [
        // 물리 (5개)
        { id: 1, name: '국립과천과학관 (물리존)', type: '전시', category: '물리', grade: '초등 3학년', unitName: '힘과 에너지', icon: 'bi-building', collected: true },
        { id: 2, name: '전기 체험관', type: '전시', category: '물리', grade: '초등 6학년', unitName: '전기와 자기', icon: 'bi-lightning-charge', collected: true },
        { id: 3, name: '로봇 연구소', type: '전시', category: '물리', grade: '초등 3학년', unitName: '힘과 에너지', icon: 'bi-robot', collected: false },
        { id: 4, name: '자기장 연구실', type: '전시', category: '물리', grade: '초등 4학년', unitName: '전기와 자기', icon: 'bi-magnet', collected: true },
        { id: 21, name: '열 체험관', type: '전시', category: '물리', grade: '초등 5학년', unitName: '열', icon: 'bi-thermometer-half', collected: false }, 

        // 화학 (5개)
        { id: 5, name: '물질의 성질관', type: '전시', category: '화학', grade: '초등 3학년', unitName: '물체와 물질', icon: 'bi-beaker', collected: true },
        { id: 6, name: '원소 주기율표관', type: '전시', category: '화학', grade: '초등 5학년', unitName: '물질의 성질', icon: 'bi-grid-3x3', collected: true },
        { id: 7, name: '수질 연구 센터', type: '답사', category: '화학', grade: '초등 5학년', unitName: '물질의 성질', icon: 'bi-droplet-fill', collected: false },
        { id: 8, name: '화학 실험실', type: '전시', category: '화학', grade: '초등 6학년', unitName: '물질의 성질', icon: 'bi-eyedropper', collected: false },
        { id: 9, name: '고분자 체험관', type: '전시', category: '화학', grade: '초등 4학년', unitName: '물질의 성질', icon: 'bi-share-fill', collected: true },
        
        // 생명 (6개)
        { id: 10, name: '곤충 생태관', type: '전시', category: '생명', grade: '초등 3학년', unitName: '생물의 연속성', icon: 'bi-bug-fill', collected: true },
        { id: 11, name: '서울숲 (생태공원)', type: '답사', category: '생명', grade: '초등 4학년', unitName: '환경과 생태계', icon: 'bi-tree-fill', collected: true },
        { id: 12, name: 'DNA 분석실', type: '전시', category: '생명', grade: '초등 5학년', unitName: '생명의 구조와 에너지', icon: 'bi-fingerprint', collected: true },
        { id: 13, name: '인체 신비관', type: '전시', category: '생명', grade: '초등 5학년', unitName: '생명의 구조와 에너지', icon: 'bi-person-fill', collected: true },
        { id: 14, name: '국립 식물원', type: '답사', category: '생명', grade: '초등 6학년', unitName: '생물의 구조와 에너지', icon: 'bi-flower1', collected: false },
        { id: 15, name: '해양 생물관', type: '전시', category: '생명', grade: '초등 3학년', unitName: '생물의 구조와 에너지', icon: 'bi-water', collected: false },
        
        // 지구 (5개)
        { id: 16, name: '한천강 지질 탐방로', type: '답사', category: '지구', grade: '초등 5학년', unitName: '고체 지구', icon: 'bi-map-fill', collected: true },
        { id: 17, name: '서울 천문대', type: '답사', category: '지구', grade: '초등 4학년', unitName: '천체', icon: 'bi-stars', collected: true },
        { id: 18, name: '기상 관측소', type: '답사', category: '지구', grade: '초등 5학년', unitName: '유체지구', icon: 'bi-cloud-sun-fill', collected: false },
        { id: 19, name: '갯벌 체험관', type: '답사', category: '지구', grade: '초등 3학년', unitName: '유체지구', icon: 'bi-tsunami', collected: false }, 
        { id: 20, name: '별자리 관측소', type: '답사', category: '지구', grade: '초등 6학년', unitName: '천체', icon: 'bi-moon-stars-fill', collected: false },
      ],
    };
  },

  computed: {
    // 탭(전시/답사) + 과목(카테고리)으로 이중 필터링
    filteredStamps() {
      // 1. 탭(전시/답사) 필터링
      let filtered = this.allStamps.filter(stamp => 
        stamp.type === this.selectedTab
      );

      // 2. 과목(카테고리) 필터링
      filtered = filtered.filter(stamp => 
        stamp.category === this.selectedCategory
      );

      return filtered;
    },

    // 전체 진행률은 필터링과 상관없이 'allStamps' 기준
    totalStampsCount() {
      return this.allStamps.length;
    },
    collectedStampsCount() {
      return this.allStamps.filter(stamp => stamp.collected).length;
    },
    progressPercent() {
      if (this.totalStampsCount === 0) return 0;
      return (this.collectedStampsCount / this.totalStampsCount) * 100;
    },
  },

  methods: {
    // 뒤로가기
    goBack() {
      this.$router.back();
    },
    // 과목(카테고리) 탭 선택
    selectCategory(categoryKey) {
      this.selectedCategory = categoryKey;
    },
    // 탭(전시/답사) 선택
    selectTab(tabName) {
      this.selectedTab = tabName;
    },
  },
};
</script>

<style scoped>
/* 페이지 기본 레이아웃 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f8f9fa;
}

/* 헤더 */
.chat-header {
  position: relative;
  flex-shrink: 0;
}

/* 2. 스크롤 콘텐츠 영역 */
.content-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px; /* 섹션 간 간격 */

  /* 스크롤바 숨기기 */
  &::-webkit-scrollbar { display: none; }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 2-1. 진행률 카드 */
.progress-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #e9ecef;
  flex-shrink: 0; 
}

.progress-display {
  line-height: 1.1;
}

/* 2-2. 전시/답사 필터 */
.segmented-control-wrapper {
  flex-shrink: 0;
}
.segmented-control {
  width: 100%;
  max-width: 400px; /* 최대 너비 */
}
.spec-button {
  display: flex;
  flex: 1; /* 버튼이 균등하게 너비 차지 */
  justify-content: center;
  align-items: center;
  padding: 5px 16px; 
  gap: 8px;
  height: 38px;
  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  transition: all 0.2s;
  font-family: 'SUIT', sans-serif;
  font-weight: 500;
  font-size: 0.95rem; 
}
.spec-button.active {
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}


/* 2-3. 카테고리 탭 */
.stamp-categories {
  background-color: #ffffff;
  border-radius: 16px;
  border: 1px solid #e9ecef;
  flex-shrink: 0; /* 높이 고정 */
}

.category-button {
  border: none;
  background: none;
  padding: 8px;
  border-radius: 12px;
  color: #6c757d; /* 비활성 색상 */
  transition: all 0.2s ease-in-out;
  flex: 1; /* 4개 버튼이 공간을 균등하게 차지 */
}

.category-button .bi {
  transition: transform 0.2s ease;
}

.category-button:hover {
  background-color: #f8f9fa;
}

.category-button.active {
  color: #4A7CEC; /* 활성 색상 (앱 기본색) */
  font-weight: 600;
}

.category-button.active .bi {
  transform: scale(1.1);
}

/* 2-4. 스탬프 그리드 */
.stamp-grid-wrapper {
  flex-grow: 1;
  min-height: 200px; /* 최소 높이 보장 */
}

.stamp-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3열 그리드 */
  gap: 12px;
  padding-bottom: 16px;
}

.stamp-item {
  position: relative;
  aspect-ratio: 1 / 1; 
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start; /* 위에서부터 정렬 */
  padding: 10px 8px; 
  padding-top: 20px; /* 아이콘 상단 여백 확보 */
  border-radius: 16px;
  background-color: #ffffff;
  border: 1px solid #e9ecef;
  transition: all 0.2s ease;
  min-width: 0; /* 텍스트 오버플로우 문제 해결 */
}

.stamp-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 1.3rem;
}

.stamp-circle {
  width: 50px; 
  height: 50px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  transition: all 0.2s ease;
  flex-shrink: 0; 
  margin-bottom: 8px; /* 아이콘과 텍스트 사이 간격 */
}

.stamp-circle .bi {
  font-size: 1.6rem; 
  transition: all 0.2s ease;
}

.stamp-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 100%;
  min-width: 0; /* 텍스트 오버플로우용 */
}

.stamp-unit {
  font-size: 0.75rem;
  font-weight: 500;
  width: 100%;
  padding: 0 2px;
  line-height: 1.3;
  
  /* 1줄 + 말줄임표 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stamp-label {
  font-size: 0.8rem;
  font-weight: 500;
  margin-top: 2px; 
  text-align: center;
  width: 100%;
  padding: 0 2px;
  line-height: 1.3; 

  /* 텍스트 줄바꿈 허용 */
  white-space: normal;
  word-wrap: break-word;

  /* 텍스트를 2줄로 제한하고 넘치면 ... 처리 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  
  min-height: calc(0.8rem * 1.3 * 2); 
}

.empty-grid-message {
  padding-top: 40px;
}
.empty-grid-message p {
  font-size: 0.95rem;
  line-height: 1.5;
}

/* --- 스탬프 상태별 스타일 --- */

/* 획득하지 못한 스탬프 (uncollected) */
.stamp-item.uncollected {
  background-color: #f8f9fa;
}
.stamp-item.uncollected .stamp-circle {
  background-color: #e9ecef;
}
.stamp-item.uncollected .stamp-circle .bi {
  color: #adb5bd;
}
.stamp-item.uncollected .stamp-unit {
  color: #adb5bd;
}
.stamp-item.uncollected .stamp-label {
  color: #adb5bd;
}

/* 획득한 스탬프 (collected) */
.stamp-item.collected {
  border-color: #4A7CEC;
}
.stamp-item.collected .stamp-circle {
  background-color: #e6f0ff;
}
.stamp-item.collected .stamp-circle .bi {
  color: #4A7CEC;
}
.stamp-item.collected .stamp-unit {
  color: #4A7CEC;
  font-weight: 600;
}
.stamp-item.collected .stamp-label {
  color: #333;
  font-weight: 600;
}
</style>