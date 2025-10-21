<template>
  <div class="vh-100 overflow-auto bg-white" style="font-family: 'SUIT', sans-serif">
    <!-- 사용자 프로필 영역 헤더 -->
    <div class="d-flex align-items-center gap-3 p-3 border-bottom">
      <!-- 프로필 이미지 영역 -->
      <img src="https://placehold.co/600x400" class="rounded-circle" style="width: 48px; height: 48px; flex-shrink: 0;">
      <!-- 환영 문구 -->
      <div class="flex-grow-1">
        <div class="text-secondary" style="font-size: 0.9rem;">안녕하세요</div>
        <div class="fw-bold" style="font-size: 1.1rem;">김민수 학부모님</div>
      </div>
    </div>

    <!-- 안내문구 -->
    <div class="p-3">
      <h2 class="fw-bold fs-4">우리 아이 맞춤 학습</h2>
      <div class="text-secondary" style="font-size: 0.9rem;">학년과 과목을 선택하여 최적의 학습 장소를 찾아보세요</div>
    </div>

    <!-- 검색섹션 -->
    <div class="d-flex justify-content-between align-items-center p-3">

      <div class="d-flex flex-column gap-2">
        <div class="d-flex align-items-center gap-2">
          <span>우리 아이는</span>
          <a href="#" class="text-decoration-none fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedSubject }}
            <i class="bi bi-chevron-down small"></i>
          </a> 를 좋아하는
        </div>
        <div class="d-flex align-items-center gap-2">
          <a href="#" class="text-decoration-none fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedGrade }}
            <i class="bi bi-chevron-down small"></i>
          </a>
          <span>입니다.</span>
        </div>
      </div>

      <!-- 검색 버튼 -->
      <button class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 50px; height: 50px;" @click="performSearch">
        <i class="bi bi-search fs-5"></i>
      </button>

    </div>

    <!-- 교과과정 정보제공 섹션 -->
    <div class="p-3">
      <h5 class="fw-bold fs-6">교과과정 내용</h5>
      <div class="p-3 bg-body-tertiary rounded-4 mt-2">
        <div class="mb-3">
          <h6 class="fw-bold text-secondary" style="font-size: 0.9rem;">초등 3학년 1학기 - 물리</h6>
          <ul class="list-unstyled mb-0 mt-2 d-flex flex-column gap-2">
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>물체의 움직임 관찰하기</span>
            </li>
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>소리의 특성 알아보기</span>
            </li>
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>빛과 그림자 탐구하기</span>
            </li>
          </ul>
        </div>
        <div>
          <h6 class="fw-bold text-secondary" style="font-size: 0.9rem;">초등 3학년 2학기 - 물리</h6>
          <ul class="list-unstyled mb-0 mt-2 d-flex flex-column gap-2">
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>물체의 움직임 관찰하기</span>
            </li>
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>소리의 특성 알아보기</span>
            </li>
            <li class="d-flex align-items-center gap-2 text-secondary">
              <i class="bi bi-circle-fill" style="font-size: 0.5rem;"></i>
              <span>빛과 그림자 탐구하기</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 추천 학습 장소 섹션 -->
    <div class="d-flex justify-content-between align-items-center p-3">
      <h5 class="fw-bold fs-6 mb-0">추천 학습 장소</h5>
      <i class="bi bi-chevron-right text-secondary"></i>
    </div>

    <!-- 버튼 섹션 -->
    <div class="d-flex gap-2 px-3">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="selectedTab = '전시'">전시</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '탐험' }"
        @click="selectedTab = '탐험'">탐험</button>
    </div>

    <!-- 카드 아이템 열차 창문 섹션 -->
    <div class="mt-3" style="height: 170px;">
      <!-- 카드 아이템 열차 레일 섹션 -->
      <div style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
        <!-- 카드 아이템 열차 섹션 -->
        <div class="d-flex flex-row align-items-center" style="gap: 16px; height: 100%; padding: 0 16px;">
          <!-- 카드 아이템 반복 -->
          <VenueCard v-for="item in carouselItems" :key="item.id" :item="item" />

        </div>
      </div>
    </div>

    <!-- 최근 저장 경로 섹션 -->
    <div class="p-3 mt-3">
      <h5 class="fw-bold fs-6 mb-0">최근 저장 경로</h5>
    </div>

    <div class="px-3">
      <div class="rounded-4 overflow-hidden shadow-sm">
        <img src="https://via.placeholder.com/390x150/e9e9e9/aaa?text=Map+Image" class="img-fluid" alt="Map">
      </div>
    </div>

    <div class="p-3">
      <div class="d-flex align-items-center p-3 bg-white rounded-4 shadow-sm gap-3">
        <img src="https://via.placeholder.com/80/e9e9e9/aaa?text=Map" class="rounded-3"
          style="width: 80px; height: 80px; flex-shrink: 0;">
        <div class="flex-grow-1">
          <span class="badge rounded-pill" style="background-color: #E0F1FF; color: #007AFF;">지구</span>
          <h5 class="fw-bold m-0 mt-2">전시명</h5>
          <span class="text-secondary" style="font-size: 0.9rem;">국립과천과학관</span>
        </div>
        <i class="bi bi-heart-fill text-danger fs-4"></i>
      </div>
    </div>

    <div style="height: 50px;"></div>

    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />
    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import FilterModal from '@/components/FilterModal.vue';
import BottomNavbar from '@/components/BottomNavbar.vue';
import VenueCard from '@/components/PlaceCard.vue';

const router = useRouter();
// 전시/탐험 버튼 상태 ('전시'가 활성화됨)
const selectedTab = ref('전시');
// 모달의 열림/닫힘 상태를 관리
const isModalOpen = ref(false);
// 홈 화면에 표시될 필터 상태
const selectedSubject = ref('물리');
const selectedGrade = ref('초등 3학년');
// 네비게이션 상태
const selectedNavItem = ref('홈')

// 캐러셀 아이템 데이터 (이전 코드 재사용)
const carouselItems = ref([
  {
    id: 1,
    iconClass: 'bi-triangle-fill',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '지구',  // 데이터
    grade: '3학년',  // 데이터
    place: '장소명',  // 데이터
    type: '전시',    // 데이터
    title: '전시명'
  },
  {
    id: 2,
    iconClass: 'bi-star-fill',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '물리',  // '우주' -> '물리' (색상표에 맞게)
    grade: '5학년',
    place: '서울천문대',
    type: '탐험',
    title: '천문대탐험'
  },
  {
    id: 3,
    iconClass: 'bi-beaker',
    iconBgColor: '#F0EDF9',
    iconColor: '#C0B0F0',
    subject: '화학',
    grade: '4학년',
    place: '한천강지질공원',
    type: '탐험',
    title: '지질탐험'
  }
]);

// 모달에서 '선택 완료를 눌렀을 때 실행되는 함수'
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  // 홈 화면의 ref 값을 모달에서 받은 값으로 업데이트
  selectedSubject.value = filterData.subject;
  selectedGrade.value = filterData.grade;
  // 모달 닫기
  isModalOpen.value = false;
}

// 검색 버튼을 눌렀을 때 실행되는 함수
const performSearch = () => {
  console.log(`검색 실행:`, {
    subject: selectedSubject.value,
    grade: selectedGrade.value
  });
  // 검색 API 호출 로직을 구현
  // fetchResults(selectedSubject.value, selectedGrade.value)
}

// BottomNav의 @navigate 이벤트를 처리할 함수
const handleNavigation = (navItemName) => {
  console.log(navItemName, '클릭됨.');
  // 활성 탭 상태 업데이트
  selectedNavItem.value = navItemName;
  // [!!] 경로는 실제 router/index.js에 정의된 path와 일치해야 합니다.
  if (navItemName === '홈') {
    router.push('/home');
  } else if (navItemName === '목록') {
    router.push('/list');
  } else if (navItemName === '지도') {
    router.push('/map');
  } else if (navItemName === '코스관리') {
    router.push('/course');
  } else if (navItemName === '마이페이지') {
    router.push('/mypage');
  }
}
</script>

<style scoped>
/* 폰트(SUIT)가 시스템에 없어도 UI가 깨지지 않도록
  -apple-system을 fallback으로 지정합니다.
*/
[style*="font-family: 'SUIT'"] {
  font-family: 'SUIT', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* bg-body-tertiary가 5.3 미만 부트스트랩에서
  작동하지 않을 경우를 대비한 fallback
*/
.bg-body-tertiary {
  background-color: #f8f9fa !important;
}

/* 상단 필터 버튼 (전시, 탐험) */
.spec-button {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  /* [!!] padding 수정 (좌우 16px) */
  padding: 5px 16px;
  gap: 8px;

  position: relative;
  /* [!!] absolute -> relative (부모 div가 위치를 잡음) */
  /* [!!] 고정 width 제거 -> 텍스트 길이에 맞게 자동 조절 */
  /* width: 84px; */
  height: 38px;

  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  transition: background-color 0.2s, color 0.2s;
  font-family: 'SUIT', sans-serif;
  font-weight: 500;
}

.spec-button.active {
  /* [!!] Figma 디자인의 파란색으로 변경 */
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}
</style>s
