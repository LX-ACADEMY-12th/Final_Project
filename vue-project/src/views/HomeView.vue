<template>
  <div class="d-flex flex-column h-100 bg-white" style="font-family: 'SUIT', sans-serif">

    <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-white"
      style="flex-shrink: 0; position: sticky; top: 0; z-index: 1020;">
      <div style="width: 24px; height: 24px;"></div>
      <h2 class="h5 mb-0 fw-bold">교과서</h2>
      <button class="btn p-0 border-0 d-flex flex-column align-items-center" style="font-size: 0.75rem; color: #4A7CEC;"
        @click="goToAiTutor">
        <i class="bi bi-robot"></i>
        AI튜터
      </button>
    </div>

    <div class="flex-grow-1" style="overflow-y: auto; min-height: 0;">

      <div class="p-3" @click="goToMyPage()()">
        <div class="d-flex align-items-center gap-3 p-3 rounded-4 shadow-sm"
          style="background-color: #4A7CEC; color: white;">
          <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" style="
     width: 48px;
     height: 48px;
     background-color: rgba(255, 255, 255, 0.3);
     overflow: hidden;
    ">
            <img v-if="user?.profileImageUrl" :src="user.profileImageUrl" alt="프로필 이미지" style="
      width: 100%;
      height: 100%;
      object-fit: cover;
     " />
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="white" class="bi bi-person-fill"
              viewBox="0 0 16 16">
              <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zM8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6" />
            </svg>
          </div>
          <div class="flex-grow-1">
            <div class="fw-bold fs-6">안녕하세요</div>
            <div class="fw-bold fs-5">{{ userName }}</div>
          </div>
        </div>
      </div>

      <div class="p-3">
        <p class="text-secondary" style="font-size: 0.9rem;">학년을 선택하면 콘텐츠가 게시됩니다.</p>
        <div class="fs-5 flex-wrap quick-badge-group"> <span>우리 아이는 </span>
          <a href="#" class="text-decoration fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedSubject }}
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-chevron-down" viewBox="0 0 16 16">
              <path fill-rule="evenodd"
                d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708 .708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
            </svg>
          </a>
          <span>가 궁금한 </span>
          <a href="#" class="text-decoration fw-bold" style="color: #4A7CEC;" @click.prevent="isModalOpen = true">
            {{ selectedGrade }}
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-chevron-down" viewBox="0 0 16 16">
              <path fill-rule="evenodd"
                d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708 .708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
            </svg>
          </a>
          <span>입니다.</span>
        </div>
      </div>



      <div class="p-3">
        <div class="rounded-3 shadow-sm" style="background-color: #8B5A2B; padding: 10px; border-radius: 12px;">
          <div style="background-color: #2E4F2F; min-height: 180px; border-radius: 8px; position: relative;"
            class="p-3 chalkboard-text">

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

            <div v-for="semesterData in chalkboardContent" :key="semesterData.semester">

              <div v-if="(selectedSemester === '1학기' && semesterData.semester.includes('1학기')) ||
                (selectedSemester === '2학기' && semesterData.semester.includes('2학기'))">

                <h6 class="fw-bold chalkboard-heading title">{{ semesterData.semester }}</h6>

                <ul v-if="semesterData.units.length > 0" class="chalkboard-list">
                  <li v-for="(unit, index) in semesterData.units" :key="unit.title">
                    <span class="index">{{ index + 1 }}</span>
                    <span>{{ unit.title }}</span>
                    <div v-if="unit.description" class="chalkboard-description">
                      {{ unit.description }}
                    </div>
                  </li>
                </ul>

                <p v-else class="chalkboard-no-data">해당 학기에 연관된 단원이 없습니다.</p>
              </div>
            </div>

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
          @click="changeTab('전시')">전시</button>
        <button type="button" class="spec-button shadow-sm flex-grow-1" :class="{ 'active': selectedTab === '답사' }"
          @click="changeTab('답사')">답사</button>
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
          <div v-else-if="displayedItems.length === 0"
            class="d-flex justify-content-center align-items-center text-muted w-100" style="min-height: 350px;">
            추천 장소가 없습니다.
          </div>

          <div v-else class="d-flex flex-row" style="gap: 16px; padding-left: 1rem; padding-right: 1rem;">

            <PlaceReviewCard v-for="item in displayedItems" :key="item.id" :item="item" @add="goToDetail(item)"
              @item-click="goToDetail(item)" />

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
            G
          </svg>
        </div>
      </div>

      <div style="height: 80px;"></div>

    </div>

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" style="flex-shrink: 0;" />

    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" :showLocationOptions="false" />

  </div>
</template>

<script>
// (스크립트 부분은 변경 사항이 없습니다. 기존 코드와 동일합니다.)
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


export default {
  components: {
    FilterModal,
    BottomNavbar,
    PlaceReviewCard
  },

  setup() {
    const authStore = useAuthStore();
    const { user } = storeToRefs(authStore); // 여기서 user를 갖고오고있음
    const userName = computed(() => {
      if (user.value?.name) {
        return `${user.value.name} 학부모님`;
      }
      return '로그인 필요';
    });


    const router = useRouter();
    const selectedTab = ref('전시');
    const isModalOpen = ref(false);
    const selectedSubject = ref('물리');
    const selectedGrade = ref('초등 3학년');
    const selectedNavItem = ref('홈');

    // 1학기/2학기 탭 상태
    const selectedSemester = ref('1학기');
    // 검색/데이터 상태
    const displayedItems = ref([]);
    const isSearching = ref(false);

    /**
     * 탭 변경 시 API 재호출
     */
    const changeTab = (tabName) => {
      selectedTab.value = tabName;
      performSearch();
    };

    /**
     * API 검색 실행 함수 - 장소 정보와 리뷰를 함께 가져옴
     */
    const performSearch = async () => {
      console.log('==== Home API 검색 실행 시작 ====');
      isSearching.value = true;
      displayedItems.value = [];

      const params = {
        searchType: 'all', // Home은 위치기반이 아니므로 'all'
        itemType: selectedTab.value, // '전시' or '답사'
        subject: selectedSubject.value,
        grade: selectedGrade.value,
      };

      try {
        console.log('API 요청 파라미터:', params);

        // 1. 장소/전시 정보 가져오기
        const response = await axios.get('/api/places/search', { params });

        if (response.data && Array.isArray(response.data)) {
          // 2. 각 장소에 대해 최신 리뷰와 사진 요약 정보 가져오기
          const itemsWithReviews = await Promise.all(
            response.data.slice(0, 10).map(async (item) => {
              try {
                // 각 장소의 targetType 결정
                const targetType = selectedTab.value === '전시' ? 'exhibition' : 'science_place';

                // 2-1. 리뷰 API 호출 (ReviewSection과 동일한 구조)
                const reviewParams = {
                  targetId: item.id,
                  targetType: targetType,
                  page: 1,
                  size: 1 // 최신 리뷰 1개만
                };

                const reviewResponse = await axios.get('/api/reviews', { params: reviewParams });

                // 페이징 응답에서 content 추출
                const reviewPage = reviewResponse.data;
                const latestReview = reviewPage?.content?.[0] || null;

                // 2-2. 사진 썸네일 정보 가져오기 (선택사항)
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
                  console.warn(`사진 썸네일 로드 실패 (장소 ID: ${item.id}):`, photoErr);
                }

                // 최종 데이터 구조
                return {
                  ...item,
                  // 리뷰 페이징 정보
                  totalReviews: reviewPage?.totalElements || 0,
                  totalPages: reviewPage?.totalPages || 0,
                  // 최신 리뷰 정보
                  latestReview: latestReview ? {
                    reviewId: latestReview.reviewId,
                    authorId: latestReview.authorId,
                    authorName: latestReview.authorName,
                    authorProfileImageUrl: latestReview.authorProfileImageUrl,
                    rating: latestReview.rating,
                    content: latestReview.content,
                    createdAt: latestReview.createdAt,
                    photoUrls: latestReview.photoUrls || [],
                    likeCount: latestReview.likeCount || 0
                  } : null,
                  // 사진 썸네일 정보
                  photoThumbnails: photoThumbnails,
                  // 평균 평점 (API에서 제공한다면)
                  averageRating: item.averageRating || 0
                };
              } catch (reviewError) {
                console.warn(`리뷰 로드 실패 (장소 ID: ${item.id}):`, reviewError);
                // 리뷰 로드 실패해도 장소 정보는 표시
                return {
                  ...item,
                  latestReview: null,
                  totalReviews: 0,
                  totalPages: 0,
                  photoThumbnails: [],
                  averageRating: item.averageRating || 0
                };
              }
            })
          );

          displayedItems.value = itemsWithReviews;
          console.log('API 응답 결과 (리뷰 포함):', JSON.stringify(displayedItems.value, null, 2));

        } else {
          console.error('API 응답 형식이 잘못되었습니다:', response.data);
          displayedItems.value = [];
        }
      } catch (error) {
        console.error("Home API 검색 중 오류:", error.response ? error.response.data : error.message);
        eventBus.emit('show-global-alert', {
          message: '추천 장소를 불러오는 중 오류가 발생했습니다.',
          type: 'error'
        });
        displayedItems.value = [];
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

    // [수정] curriculumData의 구조를 { title: '...', description: '...' } 객체 배열로 변경
    const curriculumData = {
      '초등 3학년': {
        '1학기': {
          '물리': [
            { title: '힘과 에너지', description: '밀기와 당기기, 무게, 수평잡기, 도구의 이용을 배웁니다.' },
          ],
          '화학': [],
          '생명': [
            { title: '생물의 구조와 에너지', description: '동물의 생김새, 식물의 생김새' },
            { title: '생물의 연속성', description: '동물의 한살이, 식물의 한살이, 식물이 자라는 조건, 다양한 환경에 사는 동물과 식물, 특징에 따른 동물과 식물 분류' },
            { title: '생명과학과 인간의 생활', description: '생활 속에서 동물과 식물의 이용' }
          ],
          '지구': []
        },
        '2학기': {
          '물리': [
            { title: '빛과 파동', description: '소리의 발생, 소리의 세기, 소리의 높낮이, 소리의 전달' }
          ],
          '화학': [
            { title: '물체와 물질', description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화' }
          ],
          '생명': [
            { title: '생명과학과 인간의 생활', description: '생명과학과 우리 생활: 감염병과 우리의 생활' }
          ],
          '지구': [
            { title: '유체지구', description: '바다의 특징, 밀물과 썰물 ,파도 ,바닷가 주변 지형 ,갯벌 보전, 지구의 대기' }
          ]
        }
      },

      '초등 4학년': {
        '1학기': {
          '물리': [
            { title: '전기와 자기', description: '자석과 물체 사이의 힘 ,자석과 자석 사이의 힘 ,자석의 극 ,자석의 이용' }
          ],
          '화학': [
            { title: '물질의 성질', description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화' }
          ],
          '생명': [
            { title: '생물의 구조와 에너지', description: '균류, 원생생물, 세균의 특징' },
            { title: '생명과학과 인간의 생활', description: '균류, 원생생물, 세균의 이용' }
          ],
          '지구': [
            { title: '고체지구', description: '강 주변 지형, 화산 활동, 화성암, 지진 대처 방법' }
          ]
        },
        '2학기': {
          '물리': [],
          '화학': [
            { title: '물질의 성질', description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화' }
          ],
          '생명': [
            { title: '환경과 생태계', description: '생물의 요소와 비생물 요소, 환경오염이 생물에 미치는 영향, 먹이사슬과 먹이그물' }
          ],
          '지구': [
            { title: '천체', description: '달의 모양과 표면, 달의 위상변화 ,태양계 행성, 별과 별자리' },
            { title: '기후변화와 우리 생활', description: null }
          ]
        }
      },
      '초등 5학년': {
        '1학기': {
          '물리': [
            { title: '빛과 파동', description: '빛의 직진, 평면거울에서의 빛의 반사, 빛의 굴절, 렌즈의 이용' }
          ],
          '화학': [
            { title: '물질의 성질', description: '용해, 용액, 용질의 종류, 용질의 녹는 양, 용액의 진하기' }
          ],
          '생명': [
            { title: '생명의 구조와 에너지', description: '세포의 구조, 뼈와 근육의 구조와 기능, 소화, 순환, 호흡, 배설 기관의 구조와 기능' }
          ],
          '지구': [
            { title: '고체 지구', description: '지층, 퇴적암, 화석의 생성, 과거 생물과 환경' }
          ]
        },
        '2학기': {
          '물리': [
            { title: '열', description: '온도, 열의 이동, 단열' },
          ],
          '화학': [
            { title: '물체와 물질', description: '혼합물의 분리' }
          ],
          '생명': [
            { title: '자원과 에너지', description: '재생에너지 종류, 에너지 사용' }
          ],
          '지구': [
            { title: '유체지구', description: '날씨와 기상요소, 이슬, 안개, 구름, 고기압과 저기압' }
          ]
        }
      },
      '초등 6학년': {
        '1학기': {
          '물리': [
            { title: '힘과 에너지', description: '위치의 변화, 속력, 속력과 안전' }
          ],
          '화학': [
            { title: '물질의 성질', description: '용액의 분류, 지시약, 산성 용액, 염기성 용액' }
          ],
          '생명': [
            { title: '생물의 구조와 에너지', description: '뿌리, 줄기, 잎, 꽃의 구조와 기능' }
          ],
          '지구': [
            { title: '천체', description: '태양과 별의 위치 변화, 지구의 자전과 공전, 계절별 별자리 변화' }
          ]
        },
        '2학기': {
          '물리': [
            { title: '전기와 자기', description: '전기 회로, 전지의 직렬연결 ,전자석, 전기 안전' }
          ],
          '화학': [
            { title: '물질의 변화', description: '연소조건, 연소 생성물' }
          ],
          '생명': [
            { title: '환경과 생태계', description: '생물의 요소와 비생물 요소, 환경오염이 생물에 미치는 영향, 먹이사슬과 먹이그물' }
          ],
          '지구': [
            { title: '천체', description: '태양 고도의 일변화, 계절별 낮의 길이' }
          ]
        }
      }
    };

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

      if (selectedTab.value === '전시') {
        router.push({
          path: `/exhibition/${item.id}`,
          query: {
            mainCategoryTags: selectedSubject.value,
            subCategoryTags: item.hashtags,
            gradeTags: selectedGrade.value,
          }
        });
      } else {
        router.push({
          path: `/place/${item.id}`,
          query: {
            mainCategoryTags: selectedSubject.value,
            subCategoryTags: item.hashtags,
            gradeTags: selectedGrade.value,
          }
        });
      }
    };

    const goToMyPage = () => {
      if (!user.value) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            router.push({ name: 'login' }); // 👈 this.$router 대신 router 사용
          }
        });
        return; // 페이지 이동 중단
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
      performSearch();
    };

    const handleNavigation = (navItemName) => {
      console.log(navItemName, '클릭됨.');
      selectedNavItem.value = navItemName;

      if (navItemName === '코스관리' && !user.value) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            router.push({ name: 'login' }); // 👈 this.$router 대신 router 사용
          }
        });
        return; // 페이지 이동 중단
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
            router.push({ name: 'login' }); // 👈 this.$router 대신 router 사용
          }
        });
        return; // 페이지 이동 중단
      }

      router.push('/aitutor');
    }

    return {
      user,
      userName,
      selectedTab,
      isModalOpen,
      selectedSubject,
      selectedGrade,
      selectedNavItem,
      carouselItems,
      chalkboardContent,
      selectedSemester,
      displayedItems,
      isSearching,
      changeTab,
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
   HomeView Polished Theme (layout untouched)
   - Palette via CSS variables
   - Subtle glass & depth
   - Crisp typography & spacing
   ============================= */
:root {
  --bg: #ffffff;
  --ink: #1f2937;
  --muted: #6b7280;
  --brand: #4A7CEC;
  --brand-ink: #0f172a;
  --accent: #10b981;
  --warn: #f59e0b;
  --danger: #ef4444;
  --card: #ffffff;
  --card-border: rgba(15, 23, 42, 0.08);
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.06), 0 1px 1px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 8px 24px rgba(2, 6, 23, 0.08);
  --shadow-lg: 0 16px 40px rgba(2, 6, 23, 0.12);
  --ring: 0 0 0 4px rgba(74, 124, 236, 0.14);
}

/* Base */
.d-flex.flex-column.h-100.bg-white {
  background: var(--bg);
  color: var(--ink);
  font-family: 'SUIT', sans-serif !important;
}

/* Sticky top bar (keeps exact markup) */
.d-flex.justify-content-between.align-items-center.p-3.border-bottom.bg-white {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(255, 255, 255, 0.66));
  backdrop-filter: saturate(1.2) blur(10px);
  border-bottom: 1px solid var(--card-border) !important;
}

/* Header title */
h2.h5.fw-bold {
  letter-spacing: 0.2px;
  color: var(--brand-ink);
}

/* AI Tutor icon button */
button.btn.p-0.border-0.d-flex.flex-column.align-items-center {
  gap: 2px;
  transition: transform .15s ease, opacity .2s ease;
}

button.btn.p-0.border-0.d-flex.flex-column.align-items-center:hover {
  transform: translateY(-1px);
  opacity: .9;
}

/* [추가] 프로필 카드 (기존 shadow-sm을 오버라이드) */
.rounded-4.shadow-sm {
  box-shadow: 0 8px 24px rgba(74, 124, 236, 0.2) !important;
}

/* [수정] 학년/과목 선택 영역 */
.quick-badge-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 16px;
  /* 둥근 모서리 */
  border: 1px solid var(--card-border);
  font-size: 1.15rem;
  /* 폰트 크기 살짝 키움 */
}

.quick-badge-group a {
  color: var(--brand) !important;
  font-weight: 600;
}

.quick-badge-group .bi {
  vertical-align: -2px;
}


/* Section title */
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

/* Cards */
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

/* Chalkboard section (keeps container & layout) */
.chalkboard-text {
  --chalkboard: #213a2a;
  --chalk-stroke: #f0f7ee;
  --chalk-green: #b6f2cf;
  --chalk-yellow: #ffe19c;
  color: var(--chalk-stroke);
  background: radial-gradient(120% 120% at 0% 0%, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0) 40%), var(--chalkboard);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.1), inset 0 -2px 0 rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, .2);
  position: relative;
  overflow: hidden;
  /* [수정] 템플릿의 인라인 스타일을 CSS로 이동 */
  min-height: 180px;
  border-radius: 8px;
  position: relative;
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

/* 칠판 받침대 (템플릿 인라인 스타일 제거) */
div[style*="bottom: -20px"] {
  background-color: #D2B48C !important;
}

/* Chalkboard tabs */
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
}

.chalkboard-tab-button:hover {
  transform: translateY(-1px);
  background: rgba(0, 0, 0, .22);
  border-color: rgba(255, 255, 255, .35);
}

.chalkboard-tab-button.active {
  background: rgba(255, 255, 255, .08);
  color: var(--chalk-yellow);
  border-color: rgba(255, 255, 255, .45);
}

/* Chalkboard list */
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
  /* [수정] 세로 정렬 */
  align-items: flex-start;
  /* [수정] 좌측 정렬 */
  gap: 4px;
  /* [수정] 간격 조정 */
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

/* [수정] li > span (제목) */
.chalkboard-list li>span {
  font-weight: 600;
  color: var(--chalk-stroke);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* [수정] 인덱스 번호 */
.chalkboard-list li .index {
  font-weight: 800;
  width: 22px;
  /* 살짝 줄임 */
  height: 22px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, .08);
  border-radius: 6px;
  /* 모서리 */
  color: var(--chalk-yellow);
  border: 1px solid rgba(255, 255, 255, .22);
  font-size: 0.85rem;
}

/* [수정] 설명 텍스트 */
.chalkboard-description {
  font-size: 0.9rem;
  color: var(--chalk-green);
  opacity: 0.9;
  padding-left: 30px;
  /* (인덱스 너비 + 갭) */
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

/* [추가] 데이터 없음 */
.chalkboard-no-data {
  font-size: 1rem;
  color: var(--muted);
  font-weight: 500;
  padding: 1rem 0;
  opacity: 0;
  animation: fadeSlide .32s ease-out forwards;
}


/* ========================================
   🔽 [추가] "추천 학습 장소" 이하 기존 스타일 🔽
   ========================================
*/

.card-carousel-container {
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE/Edge */
}

.card-carousel-container::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari, Opera */
}

/* Chalkboard section (keeps container & layout) */
.chalkboard-text {
  --chalkboard: #213a2a;
  --chalk-stroke: #f0f7ee;
  --chalk-green: #b6f2cf;
  --chalk-yellow: #ffe19c;
  color: var(--chalk-stroke);
  background: radial-gradient(120% 120% at 0% 0%, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0) 40%), var(--chalkboard);
  box-shadow: inset 0 2px 0 rgba(255, 255, 255, 0.1), inset 0 -2px 0 rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, .2);
  position: relative;
  overflow: hidden;
  /* [수정] 템플릿의 인라인 스타일을 CSS로 이동 */
  min-height: 180px;
  border-radius: 8px;
  position: relative;
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

/* 칠판 받침대 (템플릿 인라인 스타일 제거) */
div[style*="bottom: -20px"] {
  background-color: #D2B48C !important;
}

/* Chalkboard tabs */
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
}

.chalkboard-tab-button:hover {
  transform: translateY(-1px);
  background: rgba(0, 0, 0, .22);
  border-color: rgba(255, 255, 255, .35);
}

.chalkboard-tab-button.active {
  background: rgba(255, 255, 255, .08);
  color: var(--chalk-yellow);
  border-color: rgba(255, 255, 255, .45);
}

/* Chalkboard list */
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
  /* [수정] 세로 정렬 */
  align-items: flex-start;
  /* [수정] 좌측 정렬 */
  gap: 4px;
  /* [수정] 간격 조정 */
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

/* [수정] li > span (제목) */
.chalkboard-list li>span {
  font-weight: 600;
  color: var(--chalk-stroke);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* [수정] 인덱스 번호 */
.chalkboard-list li .index {
  font-weight: 800;
  width: 22px;
  /* 살짝 줄임 */
  height: 22px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, .08);
  border-radius: 6px;
  /* 모서리 */
  color: var(--chalk-yellow);
  border: 1px solid rgba(255, 255, 255, .22);
  font-size: 0.85rem;
}

/* [수정] 설명 텍스트 */
.chalkboard-description {
  font-size: 0.9rem;
  color: var(--chalk-green);
  opacity: 0.9;
  padding-left: 30px;
  /* (인덱스 너비 + 갭) */
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

/* [추가] 데이터 없음 */
.chalkboard-no-data {
  font-size: 1rem;
  color: var(--muted);
  font-weight: 500;
  padding: 1rem 0;
  opacity: 0;
  animation: fadeSlide .32s ease-out forwards;
}


/* ========================================
   🔽 [추가] "추천 학습 장소" 이하 기존 스타일 🔽
   ========================================
*/

.card-carousel-container {
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE/Edge */
}

.card-carousel-container::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari, Opera */
}

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

.spec-button.active {
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.4);
}

/* (스크롤바 스타일 ...) */
.flex-grow-1[style*="overflow-y: auto"] {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

[style*="overflow-x: auto"] {
  box-sizing: border-box;
}

/* Utility spacings (without touching DOM) */
.mt-tight {
  margin-top: 6px;
}

.mb-tight {
  margin-bottom: 6px;
}

.gap-6 {
  gap: 1.5rem;
}

/* Focus ring for all interactive children */
:where(button, [role="button"], .btn, input, a):focus-visible {
  outline: none;
  box-shadow: var(--ring);
}

/* Animations */
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

@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
