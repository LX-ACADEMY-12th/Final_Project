<template>
  <div class="d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">

    <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-white"
      style="flex-shrink: 0; position: sticky; top: 0; z-index: 1020;">
      <div style="width: 24px; height: 24px;"></div>
      <h2 class="h5 mb-0 fw-bold">홈</h2>
      <button class="btn p-0 border-0 d-flex flex-column align-items-center"
        style="font-size: 0.75rem; color: #4A7CEC;">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-robot"
          viewBox="0 0 16 16">
          <path
            d="M6 12.5a.5.5 0 0 1 .5-.5h3a.5.5 0 0 1 0 1h-3a.5.5 0 0 1-.5-.5M3 8.062C3 6.76 4.235 5.765 5.53 5.5S8.1 6.656 8.1 8.062v0a.5.5 0 0 1-.5.5H3.5a.5.5 0 0 1-.5-.5m9.03 0v-.001C12.03 6.76 10.794 5.765 9.5 5.5S6.93 6.656 6.93 8.062v0a.5.5 0 0 1-.5.5h4.1a.5.5 0 0 1-.5-.5" />
          <path
            d="M0 11.5A1.5 1.5 0 0 1 1.5 10h13A1.5 1.5 0 0 1 16 11.5v2A1.5 1.5 0 0 1 14.5 15h-13A1.5 1.5 0 0 1 0 13.5zM1.5 11a.5.5 0 0 0-.5.5v2a.5.5 0 0 0 .5.5h13a.5.5 0 0 0 .5-.5v-2a.5.5 0 0 0-.5-.5zM2 12.5a.5.5 0 0 1 .5-.5h.09a.5.5 0 0 1 0 1H2.5a.5.5 0 0 1-.5-.5m3.5 0a.5.5 0 0 1 .5-.5h.09a.5.5 0 0 1 0 1H6a.5.5 0 0 1-.5-.5m3.5 0a.5.5 0 0 1 .5-.5h.09a.5.5 0 0 1 0 1H9.5a.5.5 0 0 1-.5-.5m3.5 0a.5.5 0 0 1 .5-.5h.09a.5.5 0 0 1 0 1H13a.5.5 0 0 1-.5-.5" />
        </svg>
        AI튜터
      </button>
    </div>

    <div class="flex-grow-1" style="overflow-y: auto; min-height: 0;">

      <div class="p-3">
        <div class="d-flex align-items-center gap-3 p-3 rounded-4 shadow-sm"
          style="background-color: #4A7CEC; color: white;">
          <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
            style="width: 48px; height: 48px; background-color: rgba(255,255,255,0.3);">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="white" class="bi bi-person-fill"
              viewBox="0 0 16 16">
              <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zM8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
            </svg>
          </div>
          <div class="flex-grow-1">
            <div class="fw-bold fs-5">안녕하세요</div>
            <div class="fw-bold fs-5">김민수님!</div>
          </div>
        </div>
      </div>

      <div class="p-3">
        <p class="text-secondary" style="font-size: 0.9rem;">학년을 선택하면 콘텐츠가 게시됩니다.</p>
        <div class="fs-5 flex-wrap"> <span>우리 아이는 </span>
          <a href="#" class="text-decoration-none fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedSubject }}
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-chevron-down" viewBox="0 0 16 16">
              <path fill-rule="evenodd"
                d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
            </svg>
          </a>
          <span>가 궁금한 </span>
          <a href="#" class="text-decoration-none fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedGrade }}
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-chevron-down" viewBox="0 0 16 16">
              <path fill-rule="evenodd"
                d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
            </svg>
          </a>
          <span>입니다.</span>
        </div>
      </div>

      <div class="p-3">
        <div class="rounded-3 shadow-sm" style="background-color: #8B5A2B; padding: 10px; border-radius: 12px;">
          <div style="background-color: #2E4F2F; height: 180px; border-radius: 8px; position: relative;">
            <div class="position-absolute w-100"
              style="background-color: #D2B48C; height: 20px; bottom: -20px; left: 0; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;">
              <div class="position-absolute"
                style="background-color: #fff; width: 30px; height: 10px; bottom: 5px; right: 20px; border-radius: 2px;">
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center p-3 mt-3">
        <h5 class="fw-bold fs-6 mb-3">추천 학습 장소</h5>
      </div>

      <div class="d-flex gap-2 px-3">
        <button type="button" class="spec-button shadow-sm flex-grow-1" :class="{ 'active': selectedTab === '전시' }"
          @click="selectedTab = '전시'">전시</button>
        <button type="button" class="spec-button shadow-sm flex-grow-1" :class="{ 'active': selectedTab === '탐험' }"
          @click="selectedTab = '탐험'">탐험</button>
      </div>

      <div class="mt-3" style="height: 450px;">
        <div style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
          <div class="d-flex flex-row align-items-start" style="gap: 16px; height: 100%; padding: 0 16px;">

            <div v-for="item in carouselItems" :key="item.id" class="border rounded-4 shadow-sm p-3"
              style="flex-shrink: 0; width: 340px;">

              <PlaceCard :item="item" @add="goToDetail(item)" />

              <div class="mt-3">
                <h6 class="fw-bold small ps-1 mb-2">방문자 후기</h6>
                <div class="d-flex gap-3 px-1">
                  <div class="flex-shrink-0">
                    <img src="https://placehold.co/48x48/e0e0e0/333?text=김" class="rounded-circle"
                      style="width: 48px; height: 48px;">
                  </div>
                  <div class="flex-grow-1 min-w-0">
                    <div class="fw-bold">김OO 학부모</div>
                    <div class="text-secondary small mb-1">체험명</div>
                    <div class="d-flex align-items-center mb-2" style="color: #FFC107;">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                        class="bi bi-star-fill" viewBox="0 0 16 16">
                        <path
                          d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                      </svg>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                        class="bi bi-star-fill" viewBox="0 0 16 16">
                        <path
                          d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                      </svg>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                        class="bi bi-star-fill" viewBox="0 0 16 16">
                        <path
                          d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                      </svg>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#e0e0e0"
                        class="bi bi-star-fill" viewBox="0 0 16 16">
                        <path
                          d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                      </svg>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#e0e0e0"
                        class="bi bi-star-fill" viewBox="0 0 16 16">
                        <path
                          d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                      </svg>
                    </div>
                    <p class="text-truncate">"아이가 정말 좋아했어요!"</p>
                  </div>
                </div>
                <div class="d-flex gap-2 mt-2 px-1">
                  <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
                    style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진1</div>
                  <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
                    style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진2</div>
                  <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
                    style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진3</div>
                  <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary fw-bold"
                    style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">+24</div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

      <div class="p-3 mt-3">
        <h5 class="fw-bold fs-6 mb-3">최근 저장 경로</h5>
      </div>
      <div class="px-3">
        <div class="rounded-4 overflow-hidden shadow-sm">
          <img src="https://via.placeholder.com/390x150/e9e9e0/aaa?text=Map+Image" class="img-fluid" alt="Map">
        </div>
      </div>
      <div class="p-3 mt-3">
        <div class="d-flex align-items-center p-3 bg-white rounded-4 shadow-sm gap-3">
          <img src="https://via.placeholder.com/80/e9e9e0/aaa?text=Map" class="rounded-3"
            style="width: 80px; height: 80px; flex-shrink: 0;">
          <div class="flex-grow-1">
            <span class="badge rounded-pill" style="background-color: #E0F1FF; color: #007AFF;">지구</span>
            <h5 class="fw-bold m-0 mt-2">전시명</h5>
            <span class="text-secondary" style="font-size: 0.9rem;">국립과천과학관</span>
          </div>
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="red" class="bi bi-heart-fill fs-4"
            viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M8 1.314C12.438-3.248 23.534 4.735 8 15-7.534 4.736 3.562-3.248 8 1.314" />
          </svg>
        </div>
      </div>

      <div style="height: 80px;"></div>

    </div>
    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" style="flex-shrink: 0;" />

    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// 컴포넌트 임포트
import FilterModal from '@/components/modal/FilterModal.vue';
import BottomNavbar from '@/components/BottomNavbar.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';

// --- 스크립트 변수 설정 ---
const router = useRouter(); // 페이지 이동용
const selectedTab = ref('전시'); // 전시/탐험 탭 상태
const isModalOpen = ref(false); // 필터 모달 표시 여부
const selectedSubject = ref('물리'); // 선택된 과목
const selectedGrade = ref('초등학생'); // 선택된 학년
const selectedNavItem = ref('홈'); // 하단 네비게이션 상태

// 추천 장소 가로 스크롤에 표시될 데이터
const carouselItems = ref([
  {
    id: 1,
    subject: '지구',
    grade: '초등 3학년',
    place: '장소명',
    type: '전시',
    title: '전시명',
  },
  {
    id: 2,
    subject: '물리',
    grade: '초등 5학년',
    place: '서울천문대',
    type: '탐험',
    title: '천문대탐험'
  },
  {
    id: 3,
    subject: '화학',
    grade: '초등 4학년',
    place: '한천강지질공원',
    type: '탐험',
    title: '지질탐험'
  }
]);

// --- 스크립트 함수 설정 ---

// 장소 카드 클릭 시 실행
const goToDetail = (item) => {
  console.log(`상세 페이지로 이동:`, item.title);
  router.push('/placedetail')
}

// 필터 모달에서 '선택 완료' 클릭 시 실행
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  selectedSubject.value = filterData.subject;
  selectedGrade.value = filterData.grade;
  isModalOpen.value = false; // 모달 닫기
}

// 하단 네비게이션 클릭 시 실행
const handleNavigation = (navItemName) => {
  console.log(navItemName, '클릭됨.');
  selectedNavItem.value = navItemName; // 아이콘 활성화

  // 클릭된 아이콘에 맞춰 페이지 이동
  if (navItemName === '홈') {
    router.push('/home');
  } else if (navItemName === '목록') {
    router.push('/list');
  } else if (navItemName === '지도') {
    router.push('/map');
  } else if (navItemName === '코스관리') {
    router.push('/usercourselist');
  } else if (navItemName === '마이페이지') {
    router.push('/mypage');
  }
}
</script>

<style scoped>
/* 폰트 스타일 */
[style*="font-family: 'SUIT'"] {
  font-family: 'SUIT', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 전시/탐험 버튼 스타일 */
.spec-button {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 5px 16px;
  gap: 8px;
  position: relative;
  height: 38px;
  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  transition: background-color 0.2s, color 0.2s;
  font-family: 'SUIT', sans-serif;
  font-weight: 500;
}

/* 활성화된 버튼 스타일 */
.spec-button.active {
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}

/* 가로 스크롤바 숨기기 */
[style*="overflow-x: auto"]::-webkit-scrollbar {
  display: none;
}

[style*="overflow-x: auto"] {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 카드 + 후기 묶음 배경색 */
.border.rounded-4.shadow-sm.p-3 {
  background-color: var(--bs-body-bg, #fff);
}
</style>
