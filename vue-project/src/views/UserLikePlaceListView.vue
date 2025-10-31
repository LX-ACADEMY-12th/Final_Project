<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 장소 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
      </div>
    </div>

    <!-- 전시 / 답사 선택 토글-->
    <div class="segmented-control-wrapper p-3 d-flex justify-content-center flex-shrink-0">
      <div class="segmented-control d-flex gap-3">
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
          @click="changeTab('전시')">전시</button>
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
          @click="changeTab('답사')">답사</button>
      </div>
    </div>

    <div class="user-like-course">

      <div v-if="isSearching" class="text-center p-5 text-muted w-100" style="margin-top: 20px;">
        검색 중...
      </div>
      <div v-else-if="displayedItems.length === 0" class="text-center p-5 text-muted w-100" sytle="margin-top: 20px;">
        <div>표시할 장소가 없습니다.</div>
        <div class="text-sm mt-2" style="font-size:  0.9rem; color: #888">
          과목 : {{ selectedSubject }} / 학년: {{ selectedGrade.replace('초등 ', '') }}
        </div>
      </div>

      <template v-else>
        <PlaceCard2 v-for="item in displayedItems" :key="item.id" :item="item" @add="goToDetail(item)"
          @item-click="handleItemClick(item)" />
      </template>
    </div>

  </div>
</template>
<script>
import PlaceCard2 from '@/components/card/PlaceCard2.vue';
import eventBus from '@/utils/eventBus';
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

export default {
  name: 'LikePlace',
  components: {
    PlaceCard2,
  },
  // 컴포넌트가 생성되기 전에 실행되는 진입점
  setup() {
    const authStore = useAuthStore();
    // 1. 두 값을 모두 꺼냅니다.
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);

    // 2. 두 값 모두 반환합니다.
    return {
      isLoggedIn: isLoggedIn,
      currentUserId: currentUserId
    }
  },
  data() {
    return {
      selectedTab: '전시', // '전시' 또는 '답사'

      // API 원본 데이터를 저장할 배열
      allWishlistItems: [],

      // 화면에 실제로 표시할 데이터 (필터링 결과)
      displayedItems: [],

      // 로딩 상태
      isSearching: false,
    };
  },
  computed: {
    // 🌟 [추가] 학년 태그를 백엔드에서 쓰는 형태로 정제
    gradeTag() {
      // 예: '초등 3학년' -> '3학년' 또는 '초등3학년'
      // 현재는 쿼리스트링에 전체를 사용하므로, 띄어쓰기만 제거합니다.
      return this.selectedGrade.replace(/\s/g, '');
    }
  },
  methods: {

    // 탭 클릭 시 상태 변경 (API 재호출이 아닌 필터링만 실행)
    changeTab(tabName) {
      this.selectedTab = tabName;
      this.$router.replace({ query: { tab: tabName } });
      // API 재호출 대신, 이미 로드된 데이터로 필터링만 수행
      this.applyFilters();
    },

    // 장소 상세페이지 이동 함수 (기존 로직 유지)
    goToDetail(item) {
      // ... (기존 로직 유지) ...
      const queryParams = {
        mainCategoryTags: item.mainCategory, // 👈 [수정] 아이템의 카테고리를 사용
        subCategoryTags: item.hashtags,
        gradeTags: item.gradeTag,     // 👈 [수정] 아이템의 학년 태그를 사용
      };

      // item.itemType (exhibition/science_place)에 따라 경로 설정
      const typePath = item.itemType === 'exhibition' ? '/exhibition' : '/place';

      this.$router.push({
        path: `${typePath}/${item.id}`,
        query: queryParams
      });
    },

    // 아이템 클릭 핸들러 (기존 로직 유지)
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // 뒤로가기 함수 (기존 로직 유지)
    goBack() {
      this.$router.back();
    },

    // 모달에서 '선택 완료를 눌렀을 때 실행되는 함수'
    handleFilterComplete(filterData) {
      console.log(`필터 선택 완료:`, filterData);

      this.selectedSubject = filterData.subject;
      this.selectedGrade = filterData.grade;

      this.isModalOpen = false;

      // API 재호출 없이, 로드된 데이터로 필터링만 수행
      this.applyFilters();
    },

    // 로드된 데이터를 필터 조건에 맞게 거르는 함수
    applyFilters() {
      // 1. 탭 필터: '전시' -> 'exhibition' / '답사' -> 'science_place'
      const typeFilter = this.selectedTab === '전시' ? 'exhibition' : 'science_place';

      // '답사' 탭이 선택되었는지 확인
      const isSciencePlaceTab = (this.selectedTab === '답사');

      // 2. 최종 필터링된 배열 생성
      this.displayedItems = this.allWishlistItems.filter(item => {
        // item.itemType는 백엔드에서 받은 실제 타입입니다. (exhibition 또는 science_place)
        const typeMatch = item.itemType === typeFilter;

        return typeMatch
      })
        .map(item => {
          // '답사' 탭일 경우, item.type을 빈 문자열로 덮어쓴 '새 객체'를 반환
          if (isSciencePlaceTab) {
            return {
              ...item, // item의 모든 기존 속성을 그대로 복사
              type: ''   // 'type' 속성만 빈 문자열로 덮어쓰기
            };
          }

          // '전시' 탭일 경우, item을 수정없이 그대로 반환
          return item;
        });
      // 결과 콘솔 출력
      console.log(`[필터링 완료] 표시 ${this.displayedItems.length}개`)
    },


    // 🟢 찜 목록을 최초에 한 번만 가져오는 함수
    async performSearch() {

      console.log('performSearch 함수 시작')

      // 로그인 상태 확인 (setup에서 반환된 isLoggedIn 사용)
      if (!this.isLoggedIn) {
        console.warn('로그인 상태가 아니므로 찜 목록을 로드하지 않습니다.');
        this.$alert('로그인이 필요한 서비스입니다.');
        this.$router.push({ name: 'login' }); // 로그인 페이지로 이동
        return; // API 호출 중단
      }

      this.isSearching = true;
      this.allWishlistItems = [];
      this.displayedItems = []; // 화면 목록 초기화

      try {
        console.log('🔵 API 호출 시작: /api/wishlist/my-list'); // 디버깅용
        // 백엔드 API 호출
        const response = await axios.get(`/api/wishlist/my-list`);

        console.log('🟢 API 응답 받음:', response); // 전체 응답 확인
        console.log('🟢 response.data:', response.data); // 데이터 확인

        if (response.data && Array.isArray(response.data)) {
          // API 응답을 원본 데이터 배열에 저장
          this.allWishlistItems = response.data.map(item => {
            return {
              // PlaceCard2 컴포넌트에 필요한 필드 매핑
              id: item.targetId,
              title: item.title,
              imageUrl: item.mainImageUrl,

              subject: item.mainCategory,
              grade: item.gradeTag,
              hashtags: item.subCategories,

              type: item.type,                    // 기획 OR 상설
              place: item.place,

              // 필터링을 위한 내부 데이터
              itemType: item.targetType,
            };
          });

          console.log('API 응답 결과 (원본): ', this.allWishlistItems.length, '개');

          // 모든 데이터를 가져온 후, 현재 선택된 필터로 즉시 필터링
          this.applyFilters();

        } else {
          console.error('API 응답 형식이 잘못되었습니다.', response.data);
          this.allWishlistItems = [];
          this.displayedItems = [];
        }
      } catch (error) {
        console.error("찜 목록 조회 중 오류 발생", error.response ? error.response.data : error.message);
        this.$alert("찜 목록을 불러오는 중 오류가 발생했습니다.");
        this.allWishlistItems = [];
        this.displayedItems = [];
      } finally {
        this.isSearching = false;
      }
    },
  },
  created() {
    // URL에서 탭 상태 로드 (기존 로직 유지)
    const tabFromQuery = this.$route.query.tab;
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    } else {
      this.selectedTab = '전시';
    }
   } catch (error) {
    console.error("찜 목록 조회 중 오류 발생", error.response ? error.response.data : error.message);
    eventBus.emit('show-global-alert', {
          message: '찜 목록을 불러오는 중 오류가 발생했습니다.',
          type: 'error'
        });
    this.allWishlistItems = [];
    this.displayedItems = [];
   } finally {
    this.isSearching = false;
   }
  },
 },
 created() {
  // URL에서 탭 상태 로드 (기존 로직 유지)
  const tabFromQuery = this.$route.query.tab;
  if (tabFromQuery === '답사') {
   this.selectedTab = '답사';
  } else {
   this.selectedTab = '전시';
  }

    // 컴포넌트 생성 시 (최초 로드 시) API 호출
    this.performSearch();
  }
}
</script>
<style scoped>
/* 상단 필터 버튼 (전시, 탐험) */
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
}

/* 페이지 전체 컨테이너 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 헤더 */
.chat-header {
  position: relative;
}

.chat-header .header-left,
.chat-header .header-right {
  flex: 1;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-weight: 600;
}

/* 카드 목록 영역 */
.user-like-course {
  flex: 1;
  overflow-y: auto;

  /* 스크롤바 숨기기 */
  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;

  /* 카드 목록이 잘 보이도록 패딩 추가 */
  padding: 16px;
  /* 목록 배경색 */
  background-color: #f9f9f9;

  display: flex;
  flex-direction: column;
  /* 카드 아이템 간 간격 */
  gap: 16px;
}
</style>
