<template>
  <div class="d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">
    <div class="home-header d-flex justify-content-between align-items-center p-3 border-bottom bg-white">
      <h2 class="home-header-title h5 mb-0 fw-bold">교과서</h2>
      <!-- AI튜터 버튼 텍스트를 아이콘 옆에 배치 -->
      <button class="ai-tutor-button btn p-0 border-0 d-flex align-items-center gap-2" @click="goToAiTutor">
        <i class="bi bi-robot fs-5"></i>
        <span>AI튜터</span>
      </button>
    </div>
    <div class="flex-grow-1" style="overflow-y: auto; min-height: 0;">
      <div class="px-3 pt-3" @click="goToMyPage()">
        <div class="profile-card d-flex align-items-center gap-3 p-3 rounded-4 shadow-sm" @click="goToMyPage()"
          role="button" tabindex="0" @keydown.enter="goToMyPage()" :aria-label="`${userName} 프로필 보기`">
          <div class="profile-avatar rounded-circle d-flex align-items-center justify-content-center flex-shrink-0">
            <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필 이미지" class="profile-image" />
            <div v-else>
              <i class="bi bi-person-fill fs-2"></i>
            </div>
          </div>
          <div class="profile-info flex-grow-1">
            <div class="fw-bold fs-6">안녕하세요</div>
            <div class="fw-bold fs-5">{{ userName }}</div>
          </div>
        </div>
      </div>
      <!-- 더 자연스러운 문구로 수정 -->
      <div class="px-3 pt-3">
        <p class="text-secondary mb-2 ms-1" style="font-size: 0.9rem;">
          맞춤 콘텐츠를 추천해드려요
        </p>
        <div class="fs-5 flex-wrap quick-badge-group">
          <span>우리 아이는</span>
          <a href="#" class="text-decoration fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedGrade }}
            <i class="bi bi-chevron-down" style="width: 16px; height: 16px;"></i>
          </a>
          <span class="d-none d-sm-inline">학생이며,</span>
          <a href="#" class="text-decoration fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedSubject }}
            <i class="bi bi-chevron-down" style="width: 16px; height: 16px;"></i>
          </a>
          <span>과목을 학습 중입니다</span>
        </div>
      </div>

      <!-- 칠판 영역 -->
      <div class="px-3 pt-3">
        <div class="rounded-3 shadow-sm" style="background-color: #8B5A2B; padding: 10px; border-radius: 12px;">
          <div style="background-color: #2E4F2F; min-height: 180px; border-radius: 8px; position: relative;"
            class="p-3 chalkboard-text">
            <!-- 칠판 헤더: 탭 + 버튼 -->
            <div class="chalkboard-header d-flex justify-content-between align-items-center mb-2">
              <div class="chalkboard-tabs">
                <button type="button" class="chalkboard-tab-button" :class="{ 'active': selectedSemester === '1학기' }"
                  @click="selectedSemester = '1학기'">
                  1학기
                </button>
                <button type="button" class="chalkboard-tab-button" :class="{ 'active': selectedSemester === '2학기' }"
                  @click="selectedSemester = '2학기'">
                  2학기
                </button>
              </div>

              <!-- 가상실험 버튼 (시뮬레이션이 있을 때만 표시) -->
              <button v-if="currentUnitSimulation" type="button" class="btn-virtual-experiment"
                @click="showSimulation = !showSimulation" :class="{ 'active': showSimulation }">
                <i class="bi" :class="showSimulation ? 'bi-play-fill' : 'bi-flask'"></i>
                <span>{{ showSimulation ? '숨기기' : '실험' }}</span>
              </button>
            </div>

            <!-- 칠판 내용 -->
            <div v-for="semesterData in chalkboardContent" :key="semesterData.semester">
              <div v-if="(selectedSemester === '1학기' && semesterData.semester.includes('1학기')) ||
                (selectedSemester === '2학기' && semesterData.semester.includes('2학기'))">
                <h6 class="fw-bold chalkboard-heading title mt-2">{{ semesterData.semester }}</h6>
                <ul v-if="semesterData.units.length > 0" class="chalkboard-list">
                  <li v-for="(unit, index) in semesterData.units" :key="unit.title">
                    <span class="index">{{ index + 1 }}</span>
                    <span>{{ unit.title }}</span>
                    <div v-if="unit.description" class="chalkboard-description">
                      {{ unit.description }}
                    </div>
                  </li>
                </ul>
                <!-- 메시지 개선 -->
                <p v-else class="chalkboard-no-data">
                  {{ selectedSemester }}에는 학습 내용이 준비되지 않았어요
                </p>
              </div>
            </div>
            <div class="chalkboard-stand position-absolute w-100">
              <div class="chalkboard-eraser position-absolute">
              </div>
            </div>
          </div>
        </div>

        <!-- 시뮬레이션 영역 (토글) -->
        <div class="simulation-container" v-if="currentUnitSimulation && showSimulation">
          <div class="simulation-inner">
            <div class="simulation-header">
              <div class="d-flex justify-content-between align-items-start gap-3">
                <div class="flex-grow-1">
                  <h5 class="fw-bold fs-6 mb-1">
                    <i class="bi bi-flask-fill me-2" style="color: #4A7CEC;"></i>
                    가상실험: {{ selectedSemester }} {{ selectedSubject }}
                  </h5>
                  <p class="text-muted small mb-0" style="font-size: 0.85rem;">
                    {{ chalkboardContent[selectedSemester === '1학기' ? 0 : 1]?.units[0]?.title || '학습 내용' }}을(를) 직접
                    체험해보세요
                  </p>
                </div>
                <button type="button" class="btn-close-simulation" @click="showSimulation = false" title="닫기">
                  <i class="bi bi-x"></i>
                </button>
              </div>
            </div>

            <!-- 동적 컴포넌트 렌더링 -->
            <div class="simulation-content">
              <component :is="currentUnitSimulation"></component>
            </div>
          </div>
        </div>
      </div>

      <!-- 과학관 섹션 -->
      <div class="d-flex justify-content-between align-items-center px-3 pt-3 pb-0">
        <h5 class="fw-bold fs-6 mb-0">과학관</h5>
      </div>
      <div>
        <div class="card-carousel-container"
          style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; padding-top: 1rem; padding-bottom: 1rem;">
          <div v-if="isSearching" class="d-flex justify-content-center align-items-center text-muted w-100"
            style="min-height: 350px;">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <!-- 결과가 없을 때 더 친근한 메시지 -->
          <div v-else-if="exhibitionItems.length === 0"
            class="d-flex flex-column justify-content-center align-items-center text-muted w-100"
            style="min-height: 250px;">
            <i class="bi bi-inbox fs-1 mb-3" style="color: #D1D5DB;"></i>
            <p class="mb-2">아직 추천할 과학관이 없어요</p>
            <p class="small text-muted">다른 학년이나 과목을 선택해보세요</p>
          </div>
          <div v-else class="d-flex flex-row" style="gap: 16px; padding-left: 1rem; padding-right: 1rem;">
            <PlaceReviewCard v-for="item in exhibitionItems" :key="item.id" :item="item" @add="goToDetail(item, '전시')"
              @item-click="goToDetail(item, '전시')" />
          </div>
        </div>
      </div>

      <!-- 과학 체험 학습 섹션 -->
      <div class="d-flex justify-content-between align-items-center px-3 pt-3 pb-0">
        <h5 class="fw-bold fs-6 mb-0">과학 체험 학습</h5>
      </div>
      <div>
        <div class="card-carousel-wrapper position-relative">
          <div class="card-carousel-container"
            style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; padding-top: 1rem; padding-bottom: 1rem;">
            <div v-if="isSearching"
              class="d-flex flex-column justify-content-center align-items-center text-muted w-100"
              style="min-height: 250px;">
              <div class="spinner-border text-primary mb-3" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
              <p class="text-muted small">추천 장소를 찾고 있어요...</p>
            </div>
            <div v-else-if="fieldTripItems.length === 0"
              class="d-flex justify-content-center align-items-center text-muted w-100" style="min-height: 350px;">
              추천 현장학습이 없습니다.
            </div>
            <div v-else class="d-flex flex-row" style="gap: 16px; padding-left: 1rem; padding-right: 1rem;">
              <PlaceReviewCard v-for="item in fieldTripItems" :key="item.id" :item="item" @add="goToDetail(item, '답사')"
                @item-click="goToDetail(item, '답사')" />
            </div>
          </div>
          <div class="scroll-hint-gradient"></div>
        </div>
      </div>
    </div>

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

// 컴포넌트 임포트
import FilterModal from '@/components/modal/FilterModal.vue';
import BottomNavbar from '@/components/BottomNavbar.vue';
import PlaceReviewCard from '@/components/card/PlaceReviewCard.vue';
import { curriculumData } from '@/data/scienceCurriculum';
import ColumnarJoint from '@/components/simulations/ColumnarJoint.vue';
import StatesOfMatter from '@/components/simulations/StatesOfMatterSimulation.vue'
import Ecosystem from '@/components/simulations/EcosystemSimulation.vue'
import MagnetField from '@/components/simulations/MagnetField.vue'

export default {
  components: {
    FilterModal,
    BottomNavbar,
    PlaceReviewCard,
    ColumnarJoint,
    StatesOfMatter,
    Ecosystem,
    MagnetField
  },
  data() {
    return {
      allSubjects: curriculumData
    }
  },

  setup() {
    const authStore = useAuthStore();
    const { user } = storeToRefs(authStore);
    const userName = computed(() => {
      if (user.value?.name) {
        return `${user.value.name} 학부모님`;
      }
      return '로그인 필요';
    });

    const router = useRouter();
    const isModalOpen = ref(false);
    const selectedSubject = ref('물리');
    const selectedGrade = ref('초등 3학년');
    const selectedNavItem = ref('홈');

    // 1학기/2학기 탭 상태
    const selectedSemester = ref('1학기');
    // ✅ 시뮬레이션 토글 상태
    const showSimulation = ref(false);
    // 검색/데이터 상태
    const displayedItems = ref([]);
    const isSearching = ref(false);

    const exhibitionItems = ref([]);
    const fieldTripItems = ref([]);

    /**
     * 전시와 체험학습 분류
     */
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

    /**
     * API 검색 실행 함수
     */
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

          const exhibitions = [];
          const fieldTrips = [];

          itemsWithReviews.forEach(item => {
            console.log(`분류 중 - ID: ${item.id}, Type: ${item.itemType}, Title: ${item.title}`);

            if (item.itemType === 'exhibition') {
              exhibitions.push(item);
            } else if (item.itemType === 'science_place') {
              fieldTrips.push(item);
            } else {
              console.warn(`알 수 없는 itemType: ${item.itemType}`);
            }
          });

          exhibitionItems.value = exhibitions;
          fieldTripItems.value = fieldTrips;

          console.log('=== 분류 결과 ===');
          console.log('전시 아이템:', exhibitions.length, exhibitions);
          console.log('체험학습 아이템:', fieldTrips.length, fieldTrips);

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

    /**
     * 리뷰 날짜 포맷
     */
    const formatReviewDate = (dateString) => {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        return date
          .toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
          .replace(/\. /g, '.')
          .replace(/\.$/, '');
      } catch (error) {
        return dateString;
      }
    };

    /**
     * 컴포넌트 마운트 시 첫 검색 실행
     */
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

    /**
     * 상세 페이지로 이동
     */
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

    /**
     * 필터 완료 시 API 재호출
     */
    const handleFilterComplete = (filterData) => {
      console.log(`필터 선택 완료:`, filterData);
      selectedSubject.value = filterData.subject;
      selectedGrade.value = filterData.grade;
      isModalOpen.value = false;
      showSimulation.value = false; // 필터 변경 시 시뮬레이션 닫기
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

    // ✅ 현재 선택된 학습 단원의 시뮬레이션 정보
    const currentUnitSimulation = computed(() => {
      const gradeKey = selectedGrade.value;
      const subjectKey = selectedSubject.value;
      const semesterKey = selectedSemester.value === '1학기' ? '1학기' : '2학기';

      const gradeData = curriculumData[gradeKey];
      if (!gradeData) return null;

      const semesterData = gradeData[semesterKey];
      if (!semesterData) return null;

      const units = semesterData[subjectKey];
      if (!units || units.length === 0) return null;

      const simulation = units[0]?.simulationComponent;
      return simulation || null;
    });

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
      showSimulation,
      displayedItems,
      isSearching,
      exhibitionItems,
      fieldTripItems,
      currentUnitSimulation,
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
/* =============================
    HomeView Polished Theme
    - Palette via CSS variables
    - Subtle glass & depth
    - Crisp typography & spacing
    ============================= */
:root {
  --bg: #FFFFFF;
  --ink: #1F2937;
  --muted: #6B7280;
  --brand: #4A7CEC;
  --brand-ink: #0F172A;
  --accent: #10B981;
  --warn: #F59E0B;
  --danger: #EF4444;
  --card: #FFFFFF;
  --card-border: rgba(15, 23, 42, 0.08);
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.06), 0 1px 1px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 8px 24px rgba(2, 6, 23, 0.08);
  --shadow-lg: 0 16px 40px rgba(2, 6, 23, 0.12);
  --ring: 0 0 0 4px rgba(74, 124, 236, 0.14);
}

.home-header {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1020;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(255, 255, 255, 0.66));
  backdrop-filter: saturate(1.2) blur(10px);
  border-bottom: 1px solid var(--card-border);
}

.home-header-title {
  letter-spacing: 0.2px;
  color: var(--brand-ink);
}

.ai-tutor-button {
  font-size: 0.75rem;
  color: #4A7CEC;
  gap: 2px;
  transition: transform .15s ease, opacity .2s ease;
}

.ai-tutor-button:hover {
  transform: translateY(-1px);
  opacity: .9;
}

.profile-card {
  background-color: #4A7CEC;
  color: white;
  box-shadow: 0 8px 24px rgba(74, 124, 236, 0.2) !important;
  cursor: pointer;
}

.profile-avatar {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.quick-badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background-color: #F8F9FA;
  padding: 1rem;
  border-radius: 16px;
  border: 1px solid var(--card-border);
  font-size: 1.15rem;
}

.quick-badge-group a {
  color: var(--brand) !important;
  font-weight: 600;
}

.quick-badge-group .bi {
  vertical-align: -2px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  letter-spacing: .2px;
  color: var(--brand-ink);
}

.section-title .hint {
  font-weight: 600;
  color: var(--muted);
  font-size: .92rem;
}

.rounded-3.shadow-sm {
  background: radial-gradient(100% 100% at 100% 0%, rgba(74, 124, 236, 0.06) 0%, rgba(255, 255, 255, 0) 60%), var(--card);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
  transition: transform .12s ease, box-shadow .2s ease, border-color .2s ease;
}

.rounded-3.shadow-sm:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: rgba(2, 6, 23, 0.12);
}

/* ===== Chalkboard Section ===== */
.chalkboard-text {
  --chalkboard: #213A2A;
  --chalk-stroke: #F0F7EE;
  --chalk-green: #B6F2CF;
  --chalk-yellow: #FFE19C;
  color: var(--chalk-stroke);
  background: radial-gradient(120% 120% at 0% 0%, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0) 40%), var(--chalkboard);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.1), inset 0 -2px 0 rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, .2);
  position: relative;
  overflow: hidden;
  min-height: 180px;
  border-radius: 8px;
}

.chalkboard-text::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.04), transparent 40%),
    radial-gradient(circle at 80% 0%, rgba(255, 255, 255, 0.03), transparent 50%);
  pointer-events: none;
}

.chalkboard-text .title {
  font-weight: 800;
  font-size: 1.1rem;
  letter-spacing: .4px;
  text-shadow: 0 1px 0 rgba(0, 0, 0, .35);
}

/* ===== Chalkboard Header ===== */
.chalkboard-header {
  padding-bottom: 1rem;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.15);
}

/* ===== Chalkboard Tabs ===== */
.chalkboard-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.chalkboard-tab-button {
  border-radius: 10px;
  border: 1px dashed rgba(255, 255, 255, .25);
  background: rgba(0, 0, 0, .15);
  color: var(--chalk-green);
  font-weight: 700;
  padding: 8px 10px;
  transition: transform .12s ease, background .15s ease, border-color .15s ease;
  font-family: 'SUIT', sans-serif;
  font-size: 0.9rem;
  cursor: pointer;
  user-select: none;
}

.chalkboard-tab-button:hover {
  transform: translateY(-1px);
  background: rgba(0, 0, 0, .22);
  border-color: rgba(255, 255, 255, .35);
}

.chalkboard-tab-button.active {
  color: var(--chalk-yellow);
  border-color: rgba(255, 255, 255, .45);
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 0 2px rgba(255, 225, 156, 0.3);
}

/* ===== Virtual Experiment Button ===== */
.btn-virtual-experiment {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(16, 185, 129, 0.1));
  border: 1.5px solid rgba(16, 185, 129, 0.4);
  color: var(--chalk-yellow);
  padding: 8px 12px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  font-family: 'SUIT', sans-serif;
  user-select: none;
}

.btn-virtual-experiment:hover {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(16, 185, 129, 0.2));
  border-color: rgba(16, 185, 129, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.btn-virtual-experiment.active {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.4), rgba(16, 185, 129, 0.3));
  border-color: rgba(16, 185, 129, 0.7);
  color: #ffffff;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.btn-virtual-experiment i {
  font-size: 1rem;
}

/* ===== Chalkboard List ===== */
.chalkboard-list {
  list-style: none;
  margin: 10px 0 0 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.chalkboard-list li {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, .18);
  border: 1px dashed rgba(255, 255, 255, .18);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, .06);
  opacity: 0;
  animation: fadeSlide .32s ease-out forwards;
  font-size: 1rem;
  font-weight: 500;
}

.chalkboard-list li>span {
  font-weight: 600;
  color: var(--chalk-stroke);
  display: flex;
  align-items: center;
  gap: 8px;
}

.chalkboard-list li .index {
  font-weight: 800;
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, .08);
  border-radius: 6px;
  color: var(--chalk-yellow);
  border: 1px solid rgba(255, 255, 255, .22);
  font-size: 0.85rem;
}

.chalkboard-description {
  font-size: 0.9rem;
  color: var(--chalk-green);
  opacity: 0.9;
  padding-left: 30px;
}

.chalkboard-list li:nth-child(1) {
  animation-delay: .06s;
}

.chalkboard-list li:nth-child(2) {
  animation-delay: .1s;
}

.chalkboard-list li:nth-child(3) {
  animation-delay: .14s;
}

.chalkboard-list li:nth-child(4) {
  animation-delay: .18s;
}

.chalkboard-list li:nth-child(5) {
  animation-delay: .22s;
}

.chalkboard-no-data {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  padding: 2rem 1rem;
  text-align: center;
  opacity: 0;
  animation: fadeSlide .32s ease-out forwards;
}

.chalkboard-stand {
  background-color: #D2B48C;
  height: 20px;
  bottom: -20px;
  left: 0;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.chalkboard-eraser {
  background-color: #fff;
  width: 30px;
  height: 10px;
  bottom: 5px;
  right: 20px;
  border-radius: 2px;
}

/* ===== Simulation Container ===== */
.simulation-container {
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  animation: slideDownIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.simulation-inner {
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.05) 0%, rgba(16, 185, 129, 0.05) 100%);
  border: 1px solid rgba(74, 124, 236, 0.15);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(74, 124, 236, 0.1);
}

.simulation-header {
  background: linear-gradient(135deg, rgba(74, 124, 236, 0.1), rgba(16, 185, 129, 0.08));
  padding: 1.25rem;
  border-bottom: 1px solid rgba(74, 124, 236, 0.12);
  backdrop-filter: blur(10px);
}

.simulation-header h5 {
  color: var(--brand-ink);
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.simulation-header p {
  margin: 0;
  color: #6B7280;
}

.btn-close-simulation {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #EF4444;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1.2rem;
}

.btn-close-simulation:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  transform: scale(1.05);
}

.simulation-content {
  padding: 1.5rem;
  background: #FFFFFF;
  min-height: 300px;
}

/* ===== Carousel ===== */
.card-carousel-wrapper {
  position: relative;
}

.scroll-hint-gradient {
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 100%;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.9));
  pointer-events: none;
  z-index: 1;
}

.card-carousel-container {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.card-carousel-container::-webkit-scrollbar {
  display: none;
}

.flex-grow-1[style*="overflow-y: auto"] {
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding-bottom: 90px;
}

[style*="overflow-x: auto"] {
  box-sizing: border-box;
}

.mt-tight {
  margin-top: 6px;
}

.mb-tight {
  margin-bottom: 6px;
}

.gap-6 {
  gap: 1.5rem;
}

/* ===== Focus & Interactive ===== */
:where(button, [role="button"], .btn, input, a):focus-visible {
  outline: none;
  box-shadow: var(--ring);
}

/* ===== Animations ===== */
@keyframes fadeSlide {
  from {
    opacity: 0;
    transform: translateY(6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDownIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }

  to {
    opacity: 1;
    transform: translateY(0);
    max-height: 1000px;
  }
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}

@media (max-width: 768px) {
  .chalkboard-header {
    flex-direction: column;
    gap: 1rem;
  }

  .btn-virtual-experiment {
    width: 100%;
    justify-content: center;
  }

  .simulation-header {
    padding: 1rem;
  }

  .simulation-header h5 {
    font-size: 1rem;
  }

  .simulation-content {
    padding: 1rem;
  }
}
</style>
