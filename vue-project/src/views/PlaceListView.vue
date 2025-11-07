<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        추천 장소 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
        <i class="bi bi-sliders fs-5" style="cursor: pointer;" @click.prevent="isModalOpen = true">
        </i>
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

    <!-- 필터모달: 학년과 과학영역만 선택 가능하도록 설정 -->
    <FilterModal v-if="isModalOpen" :showLocationOptions="false" :initialSubject="selectedSubject"
      :initialGrade="selectedGrade" @close="isModalOpen = false" @complete="handleFilterComplete" />
  </div>
</template>

<script>
import PlaceCard2 from '@/components/card/PlaceCard2.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import axios from 'axios';
import eventBus from '@/utils/eventBus';

export default {
  name: 'PlaceList',
  components: {
    PlaceCard2,
    FilterModal
  },
  data() {
    return {
      isModalOpen: false,
      // 필터 상태를 data에 추가
      selectedSubject: '물리',
      selectedGrade: '초등 3학년',
      // API 결과를 담을 데이터 배열
      displayedItems: [],
      // 로딩 상태
      isSearching: false,
    };
  },
  computed: {
  },
  methods: {

    // 장소 상세페이지 이동 함수
    goToDetail(item) {
      console.log(`상세 페이지로 이동:`, item.title);

      // 상세 페이지로 전달할 쿼리 파라미터 정의
      const queryParams = {
        mainCategoryTags: this.selectedSubject, // data의 selectedSubject
        subCategoryTags: item.hashtags,
        gradeTags: this.selectedGrade,          // data의 selectedGrade
      };

      if (item.itemType === '전시') {
        // '전시' 탭이면 /exhibition/ID 로 이동
        console.log(`전시 상세로 이동 (ID: ${item.id}):`, item.title);
        // push하여 query 전달
        this.$router.push({
          path: `/exhibition/${item.id}`,
          query: queryParams
        });
      } else {
        // '답사' 탭이면 /place/ID 로 이동
        console.log(`장소 상세로 이동 (ID: ${item.id}):`, item.title);
        // push하여 query 전달
        this.$router.push({
          path: `/place/${item.id}`,
          query: queryParams
        });
      }
    },

    // 아이템 클릭 핸들러 (카드 클릭 시)
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },

    // 모달에서 '선택 완료를 눌렀을 때 실행되는 함수'
    handleFilterComplete(filterData) {
      console.log(`필터 선택 완료:`, filterData);

      this.selectedSubject = filterData.subject;
      this.selectedGrade = filterData.grade;

      this.isModalOpen = false;

      this.performSearch();
    },

    async performSearch() {
      console.log(`전체 목록 검색 실행:`, {
        subject: this.selectedSubject,
        grade: this.selectedGrade
      });

      // 검색 API 호출 로직 구현
      this.isSearching = true;
      this.displayedItems = []; // 목록 초기화

      const baseParams = {
        // PlaceList.vue는 위치 필터를 사용하지 않으므로 'all'로 고정
        searchType: 'all',
        subject: this.selectedSubject,
        grade: this.selectedGrade
      };

      const fetchScienceHalls = axios.get('http://localhost:8080/api/places/search', {
        params: { ...baseParams, itemType:'전시'}
      });

      const fetchPlaceField = axios.get('http://localhost:8080/api/places/search', {
        params: { ...baseParams, itemType:'답사'}
      });

      try {
        // 3. 두 API를 병렬로 동시에 호출
        const [responseScienceHall, responsePlaceField] = await Promise.all([
          fetchScienceHalls,
          fetchPlaceField
        ]);

        // 과학관 결과 처리: itemType과 badgeLabel 추가
        let scienceHallResults = [];
        if (responseScienceHall.data && Array.isArray(responseScienceHall.data)) {
          scienceHallResults = responseScienceHall.data.map(item => ({
            ...item,
            itemType: '전시',
            badgeLabel: '과학관' // 뱃지에 표시할 텍스트
          }));
        }

        // 답사 결과 처리: badgeLabel 추가
        let placeFieldResults = [];
        if (responsePlaceField.data && Array.isArray(responsePlaceField.data)) {
          placeFieldResults = responsePlaceField.data.map(item => ({
            ...item,
            itemType: '전시',
          }));
        }

        // 두 결과를 하나의 배열로 합치기 (과학관 먼저, 그 다음 답사)
        this.displayedItems = [...scienceHallResults, ...placeFieldResults];
        console.log('API 통합 결과 ', this.displayedItems.length, '개');
      } catch (error) {
        console.error("API 검색 중 오류 발생", error.response ? error.response.data : error.message);
        eventBus.emit('show-global-alert', {
          message: '장소를 검색하는 중 오류가 발생했습니다.',
          type: 'error'
        });
        this.displayedItems = [];
      } finally {
        this.isSearching = false;
      }
    },
  },
  created() {
    // 컴포넌트 생성 시(최초 로드 시) API 호출
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
}
</style>
