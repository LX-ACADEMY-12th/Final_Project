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
        </div>
        <div class="progress mt-3" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0"
          aria-valuemax="100" style="height: 10px;">

          <div class="progress-bar rounded-pill progress-bar-gradient" :style="{ width: progressPercent + '%' }"></div>

        </div>
      </div>

      <div class="segmented-control-wrapper d-flex justify-content-center flex-shrink-0">
        <div class="segmented-control d-flex gap-3">
          <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
            @click="selectTab('전시')">
            <i class="bi bi-easel2-fill me-1"></i> 과학관 전시
          </button>
          <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
            @click="selectTab('답사')">
            <i class="bi bi-binoculars-fill me-1"></i> 과학 여행
          </button>
        </div>
      </div>

      <div class="stamp-grid-wrapper">

        <div v-if="isLoading" class="loading-spinner text-center p-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-3 text-muted">스탬프 정보를 불러오는 중...</p>
        </div>

        <div v-else-if="filteredStamps.length > 0" class="stamp-grid">
          <div v-for="stamp in filteredStamps" :key="stamp.id" class="stamp-item"
            :class="{ 'collected': stamp.collected, 'uncollected': !stamp.collected }">
            <span>{{ stamp.targetName }}</span>
            <div class="stamp-circle">
              <img src="/stamp.png" alt="획득 스탬프" class="stamp-image" />
            </div>
          </div>
        </div>

        <div v-else class="empty-grid-message text-center text-muted">
          <i class="bi bi-emoji-frown fs-1 mb-2"></i>
          <p class="mb-0">
            '{{ selectedTab }}'에 해당하는<br> 스탬프가 없습니다.
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
    const TOTAL_STAMPS_COUNT = 21;

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
    };
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
  background-color: #F8F9FA;
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
  gap: 16px;
  /* 섹션 간 간격 */
  background-color: #FFFFFF;
  /* 스크롤바 숨기기 (Firefox, IE/Edge) */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* [수정] CSS 문법 오류 수정 */
.content-container::-webkit-scrollbar {
  display: none;
}

/* 2-1. 진행률 카드 */
.progress-card {
  background-color: #FFFFFF;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #E9ECEF;
  flex-shrink: 0;
}

.progress-display {
  line-height: 1.1;
}

.progress-bar-gradient {
  /* 그라데이션 적용 */
  background-image: linear-gradient(to right, #84F2B7, #70C1FF);
  /* 부트스트랩의 기본 단색 배경을 제거 */
  background-color: transparent;
}

/* 2-2. 전시/답사 필터 */
.segmented-control-wrapper {
  flex-shrink: 0;
}

.segmented-control {
  width: 100%;
  max-width: 400px;
  /* 최대 너비 */
}

.spec-button {
  display: flex;
  flex: 1;
  /* 버튼이 균등하게 너비 차지 */
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
  background-color: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #E9ECEF;
  flex-shrink: 0;
  /* 높이 고정 */
}

.category-button {
  border: none;
  background: none;
  padding: 8px;
  border-radius: 12px;
  color: #6C757D;
  /* 비활성 색상 */
  transition: all 0.2s ease-in-out;
  flex: 1;
  /* 4개 버튼이 공간을 균등하게 차지 */
}

.category-button .bi {
  transition: transform 0.2s ease;
}

.category-button:hover {
  background-color: #F8F9FA;
}

.category-button.active {
  color: #4A7CEC;
  /* 활성 색상 (앱 기본색) */
  font-weight: 600;
}

.category-button.active .bi {
  transform: scale(1.1);
}

/* [추가] 로딩 스피너 스타일 */
.loading-spinner {
  padding-top: 40px;
}

.stamp-grid {
  display: grid;
  /* 3열 유지를 위한 코드 (수정 안 함) */
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding-bottom: 16px;
}

.stamp-item {
  position: relative;
  aspect-ratio: 1 / 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* [수정] 스탬프를 수직 중앙 정렬 */
  justify-content: center;
  padding: 10px;
  /* [수정] 상하좌우 여백 */
  border-radius: 16px;
  transition: all 0.2s ease;
  min-width: 0;
}

.stamp-circle {
  /* [수정] 스탬프 크기 키우기 (예: 80px) */
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
  overflow: hidden;
  /* [삭제] margin-bottom: 8px; (텍스트가 없으므로 제거) */
}

/* [추가] <img> 태그 스타일 */
.stamp-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 이미지가 원을 꽉 채우도록 */
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
  background-color: #F8F9FA;
}

.stamp-item.uncollected .stamp-circle {
  background-color: #E9ECEF;
}

.stamp-item.uncollected .stamp-circle .bi {
  color: #ADB5BD;
}

.stamp-item.uncollected .stamp-unit {
  color: #ADB5BD;
}

.stamp-item.uncollected .stamp-label {
  color: #ADB5BD;
}

/* 획득한 스탬프 (collected) */
.stamp-item.collected {
  border-color: #4A7CEC;
}

.stamp-item.collected .stamp-circle {
  background-color: #E6F0FF;
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
