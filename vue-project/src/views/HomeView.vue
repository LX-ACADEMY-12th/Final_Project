<template>
  <div class="home-root d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">
    <!-- 상단 헤더 -->
    <div class="home-header">
      <h2 class="home-header-title">과학 어디가</h2>
      <button class="ai-tutor-button" @click="goToAiTutor">
        <i class="bi bi-robot"></i>
        <span>AI튜터</span>
      </button>
    </div>

    <!-- 스크롤 영역 -->
    <div class="home-scroll flex-grow-1">
      <!-- 프로필 카드 섹션 -->
      <div class="home-section home-section--profile">
        <div class="profile-card" @click="goToMyPage()" role="button" tabindex="0" @keydown.enter="goToMyPage()"
          :aria-label="`${userName} 프로필 보기`">
          <!-- 아바타 -->
          <div class="profile-avatar">
            <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필 이미지" class="profile-image" />
            <div v-else class="profile-avatar-fallback">
              <i class="bi bi-person-fill"></i>
            </div>
          </div>

          <!-- 텍스트 영역 -->
          <div class="profile-info">
            <div class="profile-greeting">안녕하세요</div>
            <div class="profile-name">{{ userName }}</div>

            <!-- 학년 / 과목 상태 한줄 요약 -->
            <div class="profile-status">
              <button type="button" class="profile-pill" @click.stop.prevent="isModalOpen = true">
                {{ selectedGrade }}
                <i class="bi bi-chevron-down"></i>
              </button>
              <span class="status-dot">·</span>
              <button type="button" class="profile-pill" @click.stop.prevent="isModalOpen = true">
                {{ selectedSubject }}
                <i class="bi bi-chevron-down"></i>
              </button>
              <span class="status-text">과목 학습 중 입니다.</span>
            </div>
          </div>
        </div>
        <!-- 보조 문구 -->
        <p class="profile-helper">
          *맞춤 콘텐츠를 추천해드려요
        </p>
      </div>

      <!-- 교과 진도 섹션 헤더 (과학관이랑 동일 패턴) -->
      <div class="home-section-header home-section-header--chalkboard">

        <h5 class="section-title">우리 아이 교과 진도</h5>
      </div>

      <!-- 교과 진도 카드 섹션 -->
      <div class="home-section home-section--chalkboard">
        <div class="chalkboard-frame">
          <!-- 실제 칠판 -->
          <div class="chalkboard-text">
            <!-- 탭 -->
            <div class="chalkboard-header">
              <div class="chalkboard-tabs">
                <button type="button" class="chalkboard-tab-button" :class="{ active: selectedSemester === '1학기' }"
                  @click="selectedSemester = '1학기'">
                  1학기
                </button>
                <button type="button" class="chalkboard-tab-button" :class="{ active: selectedSemester === '2학기' }"
                  @click="selectedSemester = '2학기'">
                  2학기
                </button>
              </div>
            </div>

            <!-- 내용 -->
            <div v-for="semesterData in chalkboardContent" :key="semesterData.semester">
              <div v-if="(selectedSemester === '1학기' && semesterData.semester.includes('1학기')) ||
                (selectedSemester === '2학기' && semesterData.semester.includes('2학기'))">
                <h6 class="chalkboard-heading title">
                  {{ semesterData.semester }}
                </h6>

                <ul v-if="semesterData.units.length > 0" class="chalkboard-list">
                  <li v-for="(unit, index) in semesterData.units" :key="unit.title">
                    <span>
                      <span class="index">{{ index + 1 }}</span>
                      <span>{{ unit.title }}</span>
                    </span>
                    <div v-if="unit.description" class="chalkboard-description">
                      {{ unit.description }}
                    </div>
                  </li>
                </ul>

                <p v-else class="chalkboard-no-data">
                  {{ selectedSemester }}에는 교과 정보가 준비 중이에요. 다른 학기나 과목을 선택해볼까요?
                </p>
              </div>
            </div>
          </div>

          <!-- 칠판 받침대 + 분필 + 지우개 -->
          <div class="chalkboard-shelf">
            <span class="chalk-piece"></span>
            <span class="chalk-piece brand-soft"></span>
            <span class="chalk-eraser"></span>
          </div>
        </div>
      </div>


      <!-- 과학관 섹션 -->
      <div class="home-section-header">
        <h5 class="section-title">과학관</h5>
      </div>
      <div class="home-section-carousel">
        <div class="card-carousel-container">
          <div v-if="isSearching" class="loading-state">
            <div class="spinner-border text-primary loading-spinner" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="exhibitionItems.length === 0" class="empty-state">
            <i class="bi bi-inbox empty-icon"></i>
            <p class="empty-text-main">아직 추천할 과학관이 없어요</p>
            <p class="empty-text-sub">다른 학년이나 과목을 선택해보세요</p>
          </div>
          <div v-else class="card-row">
            <PlaceReviewCard v-for="item in exhibitionItems" :key="item.id" :item="item" @add="goToDetail(item, '전시')"
              @item-click="goToDetail(item, '전시')" />
          </div>
        </div>
      </div>

      <!-- 과학 체험 학습 섹션 -->
      <div class="home-section-header">
        <h5 class="section-title">과학과 함께하는 여행</h5>
      </div>
      <div class="home-section-carousel">
        <div class="card-carousel-wrapper">
          <div class="card-carousel-container">
            <div v-if="isSearching" class="loading-state">
              <div class="spinner-border text-primary loading-spinner" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
              <p class="loading-text">추천 장소를 찾고 있어요...</p>
            </div>
            <div v-else-if="fieldTripItems.length === 0" class="empty-state">
              <p class="empty-text-main">추천 현장학습이 없습니다.</p>
            </div>
            <div v-else class="card-row">
              <PlaceReviewCard v-for="item in fieldTripItems" :key="item.id" :item="item" @add="goToDetail(item, '답사')"
                @item-click="goToDetail(item, '답사')" />
            </div>
          </div>
          <div class="scroll-hint-gradient"></div>
        </div>
      </div>
    </div>

    <!-- 하단 네비 -->
    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" style="flex-shrink: 0;" />
    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" :showLocationOptions="false" />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import eventBus from '@/utils/eventBus';
import axios from '@/api/axiosSetup';
import { useCurriculumStore } from '@/stores/curriculumStore';

import FilterModal from '@/components/modal/FilterModal.vue';
import BottomNavbar from '@/components/BottomNavbar.vue';
import PlaceReviewCard from '@/components/card/PlaceReviewCard.vue';
import { curriculumData } from '@/data/scienceCurriculum';

export default {
  components: {
    FilterModal,
    BottomNavbar,
    PlaceReviewCard,
  },
  data() {
    return {
      allSubjects: curriculumData
    }
  },

  setup() {
    const authStore = useAuthStore();
    const { user } = storeToRefs(authStore);

    const curriculumStore = useCurriculumStore();
    const { selectedGrade, selectedSubject } = storeToRefs(curriculumStore);

    const userName = computed(() => {
      if (user.value?.name) {
        return `${user.value.name} 학부모님`;
      }
      return '로그인 필요';
    });

    const router = useRouter();
    const isModalOpen = ref(false);
    const selectedNavItem = ref('홈');

    const selectedSemester = ref('1학기');

    const displayedItems = ref([]);
    const isSearching = ref(false);

    const exhibitionItems = ref([]);
    const fieldTripItems = ref([]);

    const categorizeItems = (items) => {
      const exhibitions = [];
      const fieldTrips = [];

      items.forEach(item => {
        if (item.itemType === 'exhibition') {
          exhibitions.push(item);
        } else if (item.itemType === 'science_place') {
          fieldTrips.push(item);
        }
      });

      exhibitionItems.value = exhibitions;
      fieldTripItems.value = fieldTrips;
    };

    const performSearch = async () => {
      console.log('==== Home API 검색 실행 시작 ====');
      isSearching.value = true;
      displayedItems.value = [];
      exhibitionItems.value = [];
      fieldTripItems.value = [];

      const params = {
        searchType: 'all',
        subject: selectedSubject.value,
        grade: selectedGrade.value,
      };

      try {
        console.log('API 요청 파라미터:', params);
        const response = await axios.get('/api/content/search', { params });

        if (response.data && Array.isArray(response.data)) {
          const itemsWithReviews = await Promise.all(
            response.data.slice(0, 20).map(async (item) => {
              try {
                const targetType = item.itemType;

                console.log(`아이템 타입: ${item.itemType}, ID: ${item.id}, 제목: ${item.title}`);

                if (!targetType) {
                  console.warn(`item.itemType이 비어있습니다. (ID: ${item.id})`);
                }

                const reviewParams = {
                  targetId: item.id,
                  targetType: targetType,
                  page: 1,
                  size: 1
                };

                const reviewResponse = await axios.get('/api/reviews', { params: reviewParams });
                const reviewPage = reviewResponse.data;
                const latestReview = reviewPage?.content?.[0] || null;

                let photoThumbnails = [];
                try {
                  const photoParams = {
                    targetId: item.id,
                    targetType: targetType,
                    limit: 3
                  };
                  const { data: thumbs } = await axios.get('/api/reviews/photos-summary', { params: photoParams });
                  photoThumbnails = Array.isArray(thumbs) ? thumbs : [];
                } catch (photoErr) {
                  console.warn(`사진 썸네일 로드 실패:`, photoErr);
                }

                return {
                  ...item,
                  totalReviews: reviewPage?.totalElements || 0,
                  totalPages: reviewPage?.totalPages || 0,
                  latestReview: latestReview ? {
                    reviewId: latestReview.reviewId,
                    authorName: latestReview.authorName,
                    authorProfileImageUrl: latestReview.authorProfileImageUrl,
                    rating: latestReview.rating,
                    content: latestReview.content,
                    createdAt: latestReview.createdAt,
                    photoUrls: latestReview.photoUrls || []
                  } : null,
                  photoThumbnails: photoThumbnails,
                  averageRating: item.averageRating || 0
                };
              } catch (reviewError) {
                console.warn(`리뷰 로드 실패:`, reviewError);
                return {
                  ...item,
                  latestReview: null,
                  totalReviews: 0,
                  photoThumbnails: [],
                  averageRating: item.averageRating || 0
                };
              }
            })
          );

          displayedItems.value = itemsWithReviews;

          console.log('분류 전 전체 아이템:', itemsWithReviews.length);
          categorizeItems(itemsWithReviews);
          console.log('==== 분류 완료 ====');
        } else {
          console.error('API 응답 형식이 잘못되었습니다:', response.data);
          displayedItems.value = [];
          exhibitionItems.value = [];
          fieldTripItems.value = [];
        }
      } catch (error) {
        console.error("Home API 검색 중 오류:", error);
        eventBus.emit('show-global-alert', {
          message: '추천 장소를 불러오는 중 오류가 발생했습니다.',
          type: 'error'
        });
        displayedItems.value = [];
        exhibitionItems.value = [];
        fieldTripItems.value = [];
      } finally {
        isSearching.value = false;
        console.log('==== Home API 검색 완료 ====');
      }
    };

    const formatReviewDate = (dateString) => {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        return date
          .toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
          .replace(/\. /g, '.')
          .replace(/\.$/, '');
      } catch (error) {
        console.log(`${error}`);
        return dateString;
      }
    };

    onMounted(() => {
      performSearch();
    });

    const carouselItems = ref([
      { id: 1, subject: '지구', grade: '초등 3학년', place: '장소명', type: '전시', title: '전시명', },
      { id: 2, subject: '물리', grade: '초등 5학년', place: '서울천문대', type: '답사', title: '천문대답사' },
      { id: 3, subject: '화학', grade: '초등 4학년', place: '한천강지질공원', type: '답사', title: '지질답사' }
    ]);

    const chalkboardContent = computed(() => {
      let gradeKey = selectedGrade.value;
      if (!['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년'].includes(gradeKey)) {
        gradeKey = '초등 3학년';
      }

      const subjectKey = selectedSubject.value;

      const gradeData = curriculumData[gradeKey];
      if (!gradeData) {
        return [{ semester: '데이터 없음', units: [] }];
      }

      const semester1Units = gradeData['1학기'][subjectKey] || [];
      const semester2Units = gradeData['2학기'][subjectKey] || [];

      return [
        {
          semester: `${gradeKey} 1학기 - ${subjectKey}`,
          units: semester1Units
        },
        {
          semester: `${gradeKey} 2학기 - ${subjectKey}`,
          units: semester2Units
        }
      ];
    });

    const goToDetail = (item) => {
      console.log(`상세 페이지로 이동:`, item.title);

      if (item.itemType === 'exhibition') {
        router.push({
          path: `/exhibition/${item.id}`,
          query: {
            mainCategoryTags: (item.subject && item.subject.length > 0) ? item.subject : null,
            subCategoryTags: item.hashtags,
            gradeTags: item.grade,
          }
        });
      } else {
        router.push({
          path: `/place/${item.id}`,
          query: {
            mainCategoryTags: (item.subject && item.subject.length > 0) ? item.subject : null,
            subCategoryTags: item.hashtags,
            gradeTags: item.grade,
          }
        });
      }
    };

    const goToMyPage = () => {
      if (!user.value) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            router.push({ name: 'login' });
          }
        });
        return;
      }

      console.log('마이페이지로 이동');
      router.push('/mypage');
    }

    const handleFilterComplete = (filterData) => {
      console.log('모달에서 받은 데이터:', filterData);
      curriculumStore.setFilter(filterData.grade, filterData.subject);
      console.log('스토어에 저장된 학년:', curriculumStore.selectedGrade);
      console.log('스토어에 저장된 과목:', curriculumStore.selectedSubject);

      isModalOpen.value = false;
      performSearch();
    };

    const handleNavigation = (navItemName) => {
      console.log(navItemName, '클릭됨.');
      selectedNavItem.value = navItemName;

      if (navItemName === '코스관리' && !user.value) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            router.push({ name: 'login' });
          }
        });
        return;
      }

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

    const goToAiTutor = () => {
      if (!user.value) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            router.push({ name: 'login' });
          }
        });
        return;
      }

      router.push('/aitutor');
    }

    return {
      user,
      userName,
      isModalOpen,
      selectedSubject,
      selectedGrade,
      selectedNavItem,
      carouselItems,
      chalkboardContent,
      selectedSemester,
      displayedItems,
      isSearching,
      exhibitionItems,
      fieldTripItems,
      goToDetail,
      goToMyPage,
      handleFilterComplete,
      handleNavigation,
      goToAiTutor,
      formatReviewDate
    };
  }
}
</script>

<style scoped>
/* 섹션 헤더: 과학관 / 교과 진도 공통 */
.home-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 0;
}

.section-title {
  margin: 0;
  color: #111827;
  font-weight: 700;
  font-size: 1rem;
  /* ↑ 섹션 타이틀 원래 느낌 정도로 */
}

/* 전체 레이아웃 */
.home-root {
  height: 100%;
  max-height: 100vh;
  overflow: hidden;
  overflow-x: hidden;
}

/* 스크롤 영역 */
.home-scroll {
  flex: 1 1 auto;
  overflow-y: auto;
  min-height: 0;
  padding-bottom: 80px;
  /* 4 * 20 */
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background-color: #FFFFFF;
}

.home-scroll::-webkit-scrollbar {
  display: none;
}

/* 헤더 */
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  /* 12,16 */
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1020;
  background-color: #FFFFFF;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.home-header-title {
  margin: 0;
  letter-spacing: 0.1px;
  color: #111827;
  font-weight: 700;
  font-size: 1.25rem;
  /* Bootstrap h5 정도 */
}

.ai-tutor-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  font-size: 0.875rem;
  /* ↑ */
  color: #4A7CEC;
  cursor: pointer;
  transition: transform .15s ease, opacity .2s ease;
}

.ai-tutor-button i {
  font-size: 1.05rem;
}

.ai-tutor-button:hover {
  transform: translateY(-1px);
  opacity: .9;
}

/* 공통 섹션 래퍼 */
.home-section {
  padding: 16px 16px 0;
  /* 16,16,0 */
}

.home-section--profile {
  padding-top: 16px;
}

.home-section--chalkboard {
  padding-top: 16px;
}

/* =========================
   프로필 카드
   ========================= */
.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #4A7CEC;
  color: #FFFFFF;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  border-radius: 20px;
  padding: 16px;
  transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.15s ease;
  line-height: 1.4;
}

.profile-card:hover {
  transform: translateY(-2px);
  opacity: 0.99;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
}

.profile-avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  background-color: rgba(15, 23, 42, 0.15);
  border-radius: 999px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar-fallback i {
  font-size: 2rem;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  flex: 1 1 auto;
}

.profile-greeting {
  font-size: 0.875rem;
  /* ↑ */
  opacity: 0.9;
}

.profile-name {
  font-size: 1.1rem;
  /* ↑ */
  font-weight: 700;
}

/* 학년 / 과목 줄 */
.profile-status {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.profile-pill {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.88);
  background-color: rgba(15, 23, 42, 0.18);
  color: #FFFFFF;
  font-size: 0.875rem;
  /* ↑ */
  padding: 2px 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.profile-pill i {
  font-size: 0.875rem;
}

.status-dot {
  font-size: 0.875rem;
  opacity: 0.9;
}

.status-text {
  font-size: 0.875rem;
  opacity: 0.92;
}

.profile-helper {
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 12px;
  margin-left: 10px;
  /* ↑ */
  opacity: 0.8;
}

/* =========================
   교과 진도 카드 = 칠판 프레임 (단일)
   ========================= */
.chalkboard-frame {
  background-color: #3A2518;
  border-radius: 18px;
  padding: 8px 8px 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  border: 1px solid #24140E;
  position: relative;
}

/* 칠판 판넬 */
.chalkboard-text {
  background-color: #1A3C34;
  color: #E8F5E9;
  border-radius: 9px;
  border: 1px solid rgba(0, 0, 0, 0.6);
  min-height: 128px;
  padding: 12px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    inset 0 -2px 4px rgba(0, 0, 0, 0.45);
  line-height: 1.45;
}

/* 헤더 / 탭 */
.chalkboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px dashed rgba(232, 245, 233, 0.3);
}

.chalkboard-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chalkboard-tab-button {
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  background-color: rgba(15, 23, 42, 0.28);
  color: #E5F9EC;
  font-weight: 600;
  padding: 4px 12px;
  font-size: 0.875rem;
  /* ↑ */
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s ease, border-color 0.15s ease, transform 0.12s ease, box-shadow 0.12s ease;
}

.chalkboard-tab-button.active {
  background-color: rgba(74, 124, 236, 0.9);
  /* 0.9 정도만 */
  border-color: rgba(148, 180, 255, 0.95);
  /* 살짝 밝은 파랑 */
  border-color: #4A7CEC;
  color: #FFFFFF;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.35);
  /* 그림자도 살짝 줄임 */
  transform: translateY(-1px);
}

/* 제목 */
.chalkboard-heading.title {
  font-size: 0.95rem;
  /* ↑ */
  font-weight: 700;
  color: #F9FAFB;
  margin-top: 8px;
  margin-bottom: 0;
}

/* 리스트 */
.chalkboard-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.chalkboard-list li {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 9px;
  background-color: rgba(6, 15, 13, 0.65);
  border: 1px dashed rgba(209, 250, 229, 0.35);
  font-size: 0.875rem;
  color: #E9FBE6;
  animation: fadeSlide 0.2s ease-out both;
}

/* ▶ 1) 큰 제목(예: 생물의 구조와 에너지) 살짝 더 강조 */
.chalkboard-list li>span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  /* 제목 느낌 조금 더 크게 */
}

.index {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background-color: rgba(37, 99, 235, 0.22);
  border: 1px solid rgba(191, 219, 254, 0.9);
  font-size: 0.78rem;
  /* 살짝 키움 */
  color: #DBEAFE;
  font-weight: 700;
  display: grid;
  place-items: center;
}

/* ▶ 2) 설명(예: 동물의 생김새, 식물의 생김새) 한 단계 내려서 + 점 표시 */
.chalkboard-description {
  position: relative;
  margin-top: 2px;
  padding-left: 28px;
  /* 왼쪽으로 살짝 들여쓰기 */
  font-size: 0.8rem;
  /* 제목보다 살짝 작게 */
  line-height: 1.4;
  color: #D1FAE5;
  opacity: 0.95;
}

/* 앞에 작은 점 찍어주기 */
.chalkboard-description::before {
  content: "•";
  position: absolute;
  left: 16px;
  top: 40%;
  transform: translateY(-50%);
  /* 정확히 중앙으로 이동 */
  font-size: 1rem;
  color: #D1FAE5;
}

.chalkboard-no-data {
  font-size: 0.9rem;
  /* ↑ */
  color: #E5E7EB;
  font-weight: 500;
  padding: 24px 16px;
  text-align: center;
}

/* 받침대 + 분필 + 지우개 */
.chalkboard-shelf {
  margin-top: 8px;
  background-color: #332017;
  border-radius: 0 0 11px 11px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.chalk-piece {
  width: 20px;
  height: 4px;
  border-radius: 999px;
  background-color: #F9FAFB;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.28);
}

.chalk-piece.brand-soft {
  background-color: #E3EDFF;
}

.chalk-eraser {
  position: relative;
  width: 30px;
  height: 13px;
  border-radius: 4px;
  background-color: #9CA3AF;
  border: 1px solid #4B5563;
  box-shadow:
    0 1px 3px rgba(15, 23, 42, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.chalk-eraser::before {
  content: "";
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
  height: 4px;
  border-radius: 3px 3px 0 0;
  background-color: #E5E7EB;
}

/* =========================
   섹션 타이틀 / 캐러셀
   ========================= */
.home-section-carousel {
  padding: 0 16px 0;
}

/* 카드 캐러셀 */
.card-carousel-wrapper {
  position: relative;
}

.card-carousel-container {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 16px 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background-color: transparent;
}

.card-carousel-container::-webkit-scrollbar {
  display: none;
}

.card-row {
  display: flex;
  flex-direction: row;
  gap: 16px;
}

/* 로딩 / 비어있음 상태 */
.loading-state,
.empty-state {
  min-height: 224px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6B7280;
  text-align: center;
  padding: 16px;
}

.loading-spinner {
  margin-bottom: 12px;
}

.loading-text {
  margin-top: 4px;
  font-size: 0.875rem;
  /* ↑ */
  color: #6B7280;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
  color: #D1D5DB;
}

.empty-text-main {
  margin: 0 0 4px;
  font-size: 0.9rem;
  /* ↑ */
  color: #4B5563;
}

.empty-text-sub {
  margin: 0;
  font-size: 0.875rem;
  /* ↑ */
  color: #9CA3AF;
}

/* 스크롤 힌트 */
.scroll-hint-gradient {
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 100%;
  background-color: transparent;
  pointer-events: none;
  z-index: 1;
}

/* 포커스 링 */
:where(button, [role="button"], .btn, input, a):focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 124, 236, 0.25);
}

/* 리스트 애니메이션 */
@keyframes fadeSlide {
  from {
    opacity: 0;
    transform: translateY(4px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}
</style>

<style scoped>
/* 섹션 헤더: 과학관 / 교과 진도 공통 */
.home-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 0;
}

.section-title {
  margin: 0;
  color: #111827;
  font-weight: 700;
  font-size: 1rem;
  /* ↑ 섹션 타이틀 원래 느낌 정도로 */
}

/* 전체 레이아웃 */
.home-root {
  height: 100%;
  max-height: 100vh;
  overflow: hidden;
  overflow-x: hidden;
}

/* 스크롤 영역 */
.home-scroll {
  flex: 1 1 auto;
  overflow-y: auto;
  min-height: 0;
  padding-bottom: 80px;
  /* 4 * 20 */
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background-color: #FFFFFF;
}

.home-scroll::-webkit-scrollbar {
  display: none;
}

/* 헤더 */
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  /* 12,16 */
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1020;
  background-color: #FFFFFF;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.home-header-title {
  margin: 0;
  letter-spacing: 0.1px;
  color: #111827;
  font-weight: 700;
  font-size: 1.25rem;
  /* Bootstrap h5 정도 */
}

.ai-tutor-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  font-size: 0.875rem;
  /* ↑ */
  color: #4A7CEC;
  cursor: pointer;
  transition: transform .15s ease, opacity .2s ease;
}

.ai-tutor-button i {
  font-size: 1.05rem;
}

.ai-tutor-button:hover {
  transform: translateY(-1px);
  opacity: .9;
}

/* 공통 섹션 래퍼 */
.home-section {
  padding: 16px 16px 0;
  /* 16,16,0 */
}

.home-section--profile {
  padding-top: 16px;
}

.home-section--chalkboard {
  padding-top: 16px;
}

/* =========================
   프로필 카드
   ========================= */
.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #4A7CEC;
  color: #FFFFFF;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  border-radius: 20px;
  padding: 16px;
  transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.15s ease;
  line-height: 1.4;
}

.profile-card:hover {
  transform: translateY(-2px);
  opacity: 0.99;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
}

.profile-avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  background-color: rgba(15, 23, 42, 0.15);
  border-radius: 999px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar-fallback i {
  font-size: 2rem;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  flex: 1 1 auto;
}

.profile-greeting {
  font-size: 0.875rem;
  /* ↑ */
  opacity: 0.9;
}

.profile-name {
  font-size: 1.1rem;
  /* ↑ */
  font-weight: 700;
}

/* 학년 / 과목 줄 */
.profile-status {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.profile-pill {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.88);
  background-color: rgba(15, 23, 42, 0.18);
  color: #FFFFFF;
  font-size: 0.875rem;
  /* ↑ */
  padding: 2px 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.profile-pill i {
  font-size: 0.875rem;
}

.status-dot {
  font-size: 0.875rem;
  opacity: 0.9;
}

.status-text {
  font-size: 0.875rem;
  opacity: 0.92;
}

.profile-helper {
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 12px;
  margin-left: 10px;
  /* ↑ */
  opacity: 0.8;
}

/* =========================
   교과 진도 카드 = 칠판 프레임 (단일)
   ========================= */
.chalkboard-frame {
  background-color: #3A2518;
  border-radius: 18px;
  padding: 8px 8px 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  border: 1px solid #24140E;
  position: relative;
}

/* 칠판 판넬 */
.chalkboard-text {
  background-color: #1A3C34;
  color: #E8F5E9;
  border-radius: 9px;
  border: 1px solid rgba(0, 0, 0, 0.6);
  min-height: 128px;
  padding: 12px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    inset 0 -2px 4px rgba(0, 0, 0, 0.45);
  line-height: 1.45;
}

/* 헤더 / 탭 */
.chalkboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px dashed rgba(232, 245, 233, 0.3);
}

.chalkboard-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chalkboard-tab-button {
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  background-color: rgba(15, 23, 42, 0.28);
  color: #E5F9EC;
  font-weight: 600;
  padding: 4px 12px;
  font-size: 0.875rem;
  /* ↑ */
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s ease, border-color 0.15s ease, transform 0.12s ease, box-shadow 0.12s ease;
}

.chalkboard-tab-button.active {
  background-color: rgba(74, 124, 236, 0.9);
  /* 0.9 정도만 */
  border-color: rgba(148, 180, 255, 0.95);
  /* 살짝 밝은 파랑 */
  border-color: #4A7CEC;
  color: #FFFFFF;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.35);
  /* 그림자도 살짝 줄임 */
  transform: translateY(-1px);
}

/* 제목 */
.chalkboard-heading.title {
  font-size: 0.95rem;
  /* ↑ */
  font-weight: 700;
  color: #F9FAFB;
  margin-top: 8px;
  margin-bottom: 0;
}

/* 리스트 */
.chalkboard-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.chalkboard-list li {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 9px;
  background-color: rgba(6, 15, 13, 0.65);
  border: 1px dashed rgba(209, 250, 229, 0.35);
  font-size: 0.875rem;
  color: #E9FBE6;
  animation: fadeSlide 0.2s ease-out both;
}

/* ▶ 1) 큰 제목(예: 생물의 구조와 에너지) 살짝 더 강조 */
.chalkboard-list li>span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  /* 제목 느낌 조금 더 크게 */
}

.index {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background-color: rgba(37, 99, 235, 0.22);
  border: 1px solid rgba(191, 219, 254, 0.9);
  font-size: 0.78rem;
  /* 살짝 키움 */
  color: #DBEAFE;
  font-weight: 700;
  display: grid;
  place-items: center;
}

/* ▶ 2) 설명(예: 동물의 생김새, 식물의 생김새) 한 단계 내려서 + 점 표시 */
.chalkboard-description {
  position: relative;
  margin-top: 2px;
  padding-left: 28px;
  /* 왼쪽으로 살짝 들여쓰기 */
  font-size: 0.8rem;
  /* 제목보다 살짝 작게 */
  line-height: 1.4;
  color: #D1FAE5;
  opacity: 0.95;
}

/* 앞에 작은 점 찍어주기 */
.chalkboard-description::before {
  content: "•";
  position: absolute;
  left: 16px;
  top: 40%;
  transform: translateY(-50%);
  /* 정확히 중앙으로 이동 */
  font-size: 1rem;
  color: #D1FAE5;
}

.chalkboard-no-data {
  font-size: 0.9rem;
  /* ↑ */
  color: #E5E7EB;
  font-weight: 500;
  padding: 24px 16px;
  text-align: center;
}

/* 받침대 + 분필 + 지우개 */
.chalkboard-shelf {
  margin-top: 8px;
  background-color: #332017;
  border-radius: 0 0 11px 11px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.chalk-piece {
  width: 20px;
  height: 4px;
  border-radius: 999px;
  background-color: #F9FAFB;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.28);
}

.chalk-piece.brand-soft {
  background-color: #E3EDFF;
}

.chalk-eraser {
  position: relative;
  width: 30px;
  height: 13px;
  border-radius: 4px;
  background-color: #9CA3AF;
  border: 1px solid #4B5563;
  box-shadow:
    0 1px 3px rgba(15, 23, 42, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.chalk-eraser::before {
  content: "";
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
  height: 4px;
  border-radius: 3px 3px 0 0;
  background-color: #E5E7EB;
}

/* =========================
   섹션 타이틀 / 캐러셀
   ========================= */
.home-section-carousel {
  padding: 0 16px 0;
}

/* 카드 캐러셀 */
.card-carousel-wrapper {
  position: relative;
}

.card-carousel-container {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 16px 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
  background-color: transparent;
}

.card-carousel-container::-webkit-scrollbar {
  display: none;
}

.card-row {
  display: flex;
  flex-direction: row;
  gap: 16px;
}

/* 로딩 / 비어있음 상태 */
.loading-state,
.empty-state {
  min-height: 224px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6B7280;
  text-align: center;
  padding: 16px;
}

.loading-spinner {
  margin-bottom: 12px;
}

.loading-text {
  margin-top: 4px;
  font-size: 0.875rem;
  /* ↑ */
  color: #6B7280;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
  color: #D1D5DB;
}

.empty-text-main {
  margin: 0 0 4px;
  font-size: 0.9rem;
  /* ↑ */
  color: #4B5563;
}

.empty-text-sub {
  margin: 0;
  font-size: 0.875rem;
  /* ↑ */
  color: #9CA3AF;
}

/* 스크롤 힌트 */
.scroll-hint-gradient {
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 100%;
  background-color: transparent;
  pointer-events: none;
  z-index: 1;
}

/* 포커스 링 */
:where(button, [role="button"], .btn, input, a):focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 124, 236, 0.25);
}

/* 리스트 애니메이션 */
@keyframes fadeSlide {
  from {
    opacity: 0;
    transform: translateY(4px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
