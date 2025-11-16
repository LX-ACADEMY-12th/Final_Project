<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 상단 헤더 -->
    <div class="stamp-header">
      <button class="header-back-btn" type="button" @click="goBack">
        <i class="bi bi-arrow-left"></i>
      </button>
      <div class="header-title">스탬프 투어</div>
      <div class="header-right-space"></div>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="content-container">
      <!-- 진행률 카드 -->
      <div class="progress-card">
        <div class="progress-chip-row">
          <span class="progress-chip">
            <i class="bi bi-patch-check-fill"></i>
            <span>나의 스탬프 현황</span>
          </span>
        </div>

        <div class="progress-main">
          <div class="progress-count">
            <span class="count-number">{{ collectedStampsCount }}</span>
            <span class="count-total">/ {{ totalStampsCount }}</span>
          </div>
          <div class="count-caption">
            전체 스탬프의 {{ Math.round(progressPercent) }}%를 모았어요
          </div>
        </div>

        <div class="progress-track" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0"
          aria-valuemax="100">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>

      <!-- 전시 / 답사 탭 -->
      <div class="segmented-control-wrapper">
        <div class="segmented-control">
          <button type="button" class="spec-button" :class="{ active: selectedTab === '전시' }" @click="selectTab('전시')">
            <i class="bi bi-easel2-fill"></i>
            <span>과학관 전시</span>
          </button>
          <button type="button" class="spec-button" :class="{ active: selectedTab === '답사' }" @click="selectTab('답사')">
            <i class="bi bi-binoculars-fill"></i>
            <span>과학 여행</span>
          </button>
        </div>
      </div>

      <!-- 스탬프 그리드 -->
      <div class="stamp-grid-wrapper">
        <div v-if="isLoading" class="loading-spinner">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="loading-text">스탬프 정보를 불러오는 중...</p>
        </div>

        <div v-else-if="filteredStamps.length > 0" class="stamp-grid">
          <div v-for="stamp in filteredStamps" :key="stamp.id" class="stamp-item"
            :class="{ collected: stamp.collected, uncollected: !stamp.collected }">
            <div class="stamp-name" :title="stamp.targetName">
              {{ stamp.targetName }}
            </div>
            <div class="stamp-circle">
              <img src="/stamp.png" alt="스탬프" class="stamp-image" />
            </div>
          </div>
        </div>

        <div v-else class="empty-grid-message">
          <i class="bi bi-emoji-frown empty-icon"></i>
          <p class="empty-text">
            '{{ selectedTab }}'에 해당하는<br />스탬프가 없습니다.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
// [추가] Composition API 관련 함수 임포트
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'StampView',

  // [수정] 모든 로직을 setup()으로 이동
  setup() {
    // === 1. 스토어 및 라우터 설정 ===
    const authStore = useAuthStore();
    const { currentUserId } = storeToRefs(authStore); // Pinia 스토어에서 ID 가져오기
    const router = useRouter(); // vue-router 사용

    // === 2. data() -> ref() ===
    // data()에 있던 상태 변수들을 ref()로 선언
    const selectedTab = ref('전시');
    const isLoading = ref(true); // 로딩 상태

    // [수정] 획득한 스탬프 목록 (API가 채워줌)
    const collectedStamps = ref([]);
    // [수정] '전체 스탬프 개수'는 하드코딩 (진행률 표시용)
    // TODO: 이 값(21)도 API (예: /api/stamps/total-count)로 가져오는 것이 좋음
    const TOTAL_STAMPS_COUNT = 39;

    // === 3. computed() ===
    const filteredStamps = computed(() => {
      // 1. 현재 선택된 한글 탭(selectedTab)을 API가 반환하는 영문 타입으로 매핑
      const typeToMatch = (selectedTab.value === '전시')
        ? 'exhibition'
        : 'science_place';
      // [수정] API가 반환한 'collectedStamps'를 필터링
      return collectedStamps.value.filter(stamp =>
        // [수정] API가 반환한 targetType ('전시'/'답사')과 탭 이름('전시'/'답사') 비교
        stamp.targetType === typeToMatch
      );
    });

    // [수정] 전체 개수
    const totalStampsCount = computed(() => TOTAL_STAMPS_COUNT);

    // 획득 개수
    const collectedStampsCount = computed(() => collectedStamps.value.length);

    const progressPercent = computed(() => {
      if (totalStampsCount.value === 0) return 0;
      return (collectedStampsCount.value / totalStampsCount.value) * 100;
    });

    // === 4. methods() -> function ===
    const goBack = () => {
      router.back();
    };

    const selectTab = (tabName) => {
      selectedTab.value = tabName;
    };

    // 스탬프 조회 API 호출
    // [수정] API 호출 함수 (매우 간단해짐)
    const fetchCollectedStamps = async () => {
      isLoading.value = true;

      try {
        const response = await axios.get(`/api/stamps/user/${currentUserId.value}`);
        // [핵심] API 응답(이름, 아이콘 포함)을 그대로 상태에 저장
        collectedStamps.value = response.data.map(stamp => ({
          ...stamp,
          collected: true // UI 호환성을 위해 'collected' 상태 고정
        }));

        console.log('[StampView] API 응답 (획득한 스탬프 목록):', collectedStamps.value);

      } catch (error) {
        console.error("[StampView] 스탬프 획득 정보 로딩 실패:", error);
        if (error.response && error.response.status === 401) {
          alert("세션이 만료되었거나 로그인이 필요합니다. (401)");
        }
      } finally {
        isLoading.value = false;
      }
    };
    // === 5. mounted() -> onMounted() ===
    onMounted(() => {
      if (!currentUserId.value) {
        alert("로그인이 필요한 기능입니다.");
        router.push('/login');
      } else {
        fetchCollectedStamps();
      }
    });

    // === 6. return (템플릿에서 사용할 것들 반환) ===
    return {
      selectedTab,
      isLoading,
      filteredStamps,
      collectedStampsCount,
      progressPercent,
      goBack,
      selectTab,
      totalStampsCount,
      Math
    };
  },
};
</script>

<style scoped>
:root {
  --brand: #4a7cec;
  --ink: #111827;
  --muted: #6b7280;
  --border-subtle: #e5e7eb;
  --bg-soft: #f3f4f6;
}

/* 페이지 기본 레이아웃 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f9fafb;
}

/* 헤더 */
.stamp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  flex-shrink: 0;
  background-color: #ffffff;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.header-back-btn {
  border: none;
  background: transparent;
  padding: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.header-back-btn i {
  font-size: 1.1rem;
  color: #4b5563;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 1rem;
  font-weight: 700;
  color: var(--ink);
}

.header-right-space {
  width: 28px;
  /* back 버튼과 균형 맞추기용 빈 박스 */
}

/* 스크롤 콘텐츠 영역 */
.content-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #ffffff;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.content-container::-webkit-scrollbar {
  display: none;
}

/* 진행률 카드 */
.progress-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.progress-chip-row {
  margin-bottom: 8px;
}

.progress-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background-color: #e5edff;
  color: #1d4ed8;
  font-size: 0.75rem;
  font-weight: 600;
}

.progress-chip i {
  font-size: 0.9rem;
}

.progress-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 4px;
  margin-bottom: 12px;
}

.progress-count {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
}

.count-number {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--ink);
}

.count-total {
  font-size: 0.9rem;
  color: #4b5563;
}

.count-caption {
  font-size: 0.8rem;
  color: var(--muted);
}

/* 커스텀 progress bar */
.progress-track {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background-color: #e5e7eb;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  background-color: var(--brand);
  transition: width 0.25s ease-out;
}

/* 전시/답사 세그먼트 탭 */
.segmented-control-wrapper {
  display: flex;
  justify-content: center;
}

.segmented-control {
  width: 100%;
  max-width: 420px;
  display: flex;
  padding: 4px;
  border-radius: 999px;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  gap: 4px;
}

.spec-button {
  flex: 1;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #4b5563;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 8px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.16s ease, color 0.16s ease, box-shadow 0.16s ease,
    transform 0.1s ease;
}

.spec-button i {
  font-size: 0.95rem;
}

.spec-button.active {
  background-color: #ffffff;
  color: var(--brand);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.12);
  transform: translateY(-1px);
}

/* 스탬프 그리드 */
.stamp-grid-wrapper {
  min-height: 0;
}

.loading-spinner {
  padding: 40px 16px 24px;
  text-align: center;
  color: var(--muted);
}

.loading-text {
  margin-top: 12px;
  font-size: 0.85rem;
}

.stamp-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  /* 3열 고정 */
  /* 카드 최소 120px, 화면 크면 자동으로 2~3열 */
  gap: 12px;
  padding-bottom: 16px;
}


.stamp-item {
  position: relative;
  aspect-ratio: 1 / 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px 14px;
  /* 8,4,10 → 살짝 여유 */
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  background-color: #f9fafb;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease,
    background-color 0.15s ease;
}


.stamp-item.collected {
  background-color: #eef3ff;
  border-color: rgba(74, 124, 236, 0.6);
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.12);
}

.stamp-item.collected:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.18);
}

.stamp-item.uncollected {
  opacity: 0.6;
}

/* 이름 텍스트 */
.stamp-name {
  width: 100%;
  text-align: center;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--ink);
  padding: 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 원형 스탬프 크기 업! */
.stamp-circle {
  width: 96px;
  /* 68 → 96 */
  height: 96px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.stamp-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 비어 있을 때 */
.empty-grid-message {
  padding: 40px 16px 24px;
  text-align: center;
  color: var(--muted);
}

.empty-icon {
  font-size: 2.4rem;
  margin-bottom: 12px;
  color: #d1d5db;
}

.empty-text {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 포커스 링 */
:where(button):focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 124, 236, 0.3);
}
</style>
