<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        추천 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
        <i class="bi bi-sliders fs-5" style="cursor: pointer;" @click.prevent="isModalOpen = true">
        </i>
      </div>
    </div>

    <div class="segmented-control-wrapper p-3 d-flex justify-content-center flex-shrink-0">
      <div class="segmented-control d-flex gap-3">
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
          @click="changeTab('전시')">과학관 전시</button>
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
          @click="changeTab('답사')">과학 여행</button>
      </div>
    </div>

    <div class="user-like-course">

      <div v-if="isSearching" class="text-center p-5 text-muted w-100" style="margin-top: 20px;">
        검색 중...
      </div>

      <div v-else-if="filteredItems.length === 0" class="text-center p-5 text-muted w-100" sytle="margin-top: 20px;">
        <div>
          '{{ selectedTab }}' 탭에 표시할 장소가 없습니다.
        </div>
        <div class="text-sm mt-2" style="font-size: 0.9rem; color: #888">
          과목 : {{ selectedSubject }} / 학년: {{ selectedGrade.replace('초등 ', '') }}
        </div>
      </div>

      <template v-else>

        <PlaceCard2 v-for="item in filteredItems" :key="item.id" :item="item" @add="goToDetail(item)"
          @item-click="handleItemClick(item)" />

      </template>
    </div>


    <FilterModal v-if="isModalOpen" :showLocationOptions="false" :initialSubject="selectedSubject"
      :initialGrade="selectedGrade" @close="isModalOpen = false" @complete="handleFilterComplete" />

  </div>
</template>

<script>
import PlaceCard2 from '@/components/card/PlaceCard2.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import axios from '@/api/axiosSetup'; // [!!] axios 경로 수정 (MapComponent와 동일하게)
import eventBus from '@/utils/eventBus';

import { mapState, mapActions } from 'pinia';
import { useCurriculumStore } from '@/stores/curriculumStore';

export default {
  name: 'PlaceList',
  components: {
    PlaceCard2,
    FilterModal
  },
  data() {
    return {
      selectedTab: '전시',
      isModalOpen: false,

      // 3. displayedItems -> allFetchedItems로 이름 변경 (전체 목록)
      allFetchedItems: [],

      isSearching: false,
    };
  },
  computed: {
    // 'filteredItems' computed 속성 추가
    filteredItems() {
      if (this.selectedTab === '전시') {
        // 백엔드 API 응답의 itemType이 'exhibition'인 경우 필터링
        return this.allFetchedItems.filter(item => item.itemType === 'exhibition');
      } else {
        // '답사' 탭일 경우
        return this.allFetchedItems.filter(item => item.itemType === 'science_place');
      }
    },
    ...mapState(useCurriculumStore, ['selectedGrade', 'selectedSubject'])
  },
  methods: {

    ...mapActions(useCurriculumStore, ['setFilter']),

    // [!!] 5. changeTab에서 API 호출(performSearch) 제거
    changeTab(tabName) {
      this.selectedTab = tabName;
      this.$router.replace({ query: { tab: tabName } });
      // this.performSearch(); // [!!] 탭 변경 시 API 호출 제거
    },

    // 상세 페이지 이동 함수 (수정 없음)
    goToDetail(item) {
      console.log(`상세 페이지로 이동:`, item.title);
      const queryParams = {
        mainCategoryTags: this.selectedSubject,
        subCategoryTags: item.hashtags,
        gradeTags: this.selectedGrade,
      };

      // [!!] item.itemType을 기준으로 경로 결정 (selectedTab 대신)
      if (item.itemType === 'exhibition') {
        console.log(`전시 상세로 이동 (ID: ${item.id}):`, item.title);
        this.$router.push({
          path: `/exhibition/${item.id}`,
          query: queryParams
        });
      } else { // 'science_place'
        console.log(`장소 상세로 이동 (ID: ${item.id}):`, item.title);
        this.$router.push({
          path: `/place/${item.id}`,
          query: queryParams
        });
      }
    },

    // 아이템 클릭 핸들러 (수정 없음)
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // 뒤로가기 함수 (수정 없음)
    goBack() {
      this.$router.back();
    },

    // 필터 완료 핸들러
    handleFilterComplete(filterData) {
      console.log(`필터 선택 완료:`, filterData);
      this.setFilter(filterData.grade, filterData.subject);
      this.isModalOpen = false;
      this.performSearch(); // [!!] 필터 변경 시에는 API 다시 호출
    },

    // performSearch 로직 수정
    async performSearch() {
      console.log(`검색 실행 (모든 타입):`, {
        subject: this.selectedSubject,
        grade: this.selectedGrade
      });

      this.isSearching = true;
      this.allFetchedItems = []; // [!!] allFetchedItems 초기화

      const params = {
        searchType: 'all',
        // [!!] itemType: this.selectedTab, // 'itemType' 파라미터 제거
        subject: this.selectedSubject,
        grade: this.selectedGrade
      };

      try {
        const response = await axios.get('/api/content/search', { params }); // [!!] URL 경로 수정 (MapComponent와 동일하게)

        if (response.data && Array.isArray(response.data)) {
          // API 응답(전체)을 'allFetchedItems'에 저장
          this.allFetchedItems = response.data;

          const processedItems = response.data.map(item => {

            // 1. 'exhibition' (과학관/전시) 타입인 경우
            if (item.itemType === 'exhibition') {
              return {
                ...item,
                // 'item.type' ('상설'/'기획') 대신 '과학관' 텍스트를 뱃지로 사용
                badgeLabel: '과학관'
              };
            }
            // 2. 'science_place' (답사/과학장소) 타입인 경우
            // 뱃지 없이 그대로 반환합니다.
            else {
              return item;
            }
          });

          // 처리된 아이템을 최종 목록으로 저장
          this.allFetchedItems = processedItems;
          console.log('API 응답 결과 (전체): ', this.allFetchedItems.length, '개');
        } else {
          console.error('API 응답 형식이 잘못되었습니다.', response.data);
          this.allFetchedItems = [];
        }
      } catch (error) {
        console.error("API 검색 중 오류 발생", error.response ? error.response.data : error.message);
        eventBus.emit('show-global-alert', {
          message: '장소를 검색하는 중 오류가 발생했습니다.',
          type: 'error'
        });
        this.allFetchedItems = [];
      } finally {
        this.isSearching = false;
      }
    },
  },
  created() {
    // URL 에서 ?tab= ... 값을 읽어온다.
    const tabFromQuery = this.$route.query.tab;

    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    } else {
      this.selectedTab = '전시';
    }

    // [!!] 7. 컴포넌트 생성 시 API 1회 호출 (수정 없음)
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
  width: 115px;
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
