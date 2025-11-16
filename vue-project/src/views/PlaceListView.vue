<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold" style="flex: 1; text-align: center; font-size: 16px;">
        추천 목록
      </div>
      <div class="header-right d-flex justify-content-end" style="flex: 1;">
        <i class="bi bi-sliders filter-icon" style="cursor: pointer;" @click.prevent="isModalOpen = true"></i>
      </div>
    </div>

    <div class="segmented-control-wrapper flex-shrink-0">
      <div class="segmented-control">
        <button type="button" class="spec-button" :class="{ 'active': selectedTab === '전시' }"
          @click="changeTab('전시')">전시관</button>
        <button type="button" class="spec-button" :class="{ 'active': selectedTab === '답사' }"
          @click="changeTab('답사')">과학 탐험지</button>
      </div>
    </div>

    <div class="user-like-course">

      <div v-if="isSearching" class="text-center p-5 text-muted w-100 status-message">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2 text-muted">검색 중...</p>
      </div>

      <div v-else-if="filteredItems.length === 0" class="text-center p-5 text-muted w-100 status-message">
        <div>
          '{{ selectedTab }}' 탭에 표시할 장소가 없습니다.
        </div>
        <div class="text-sm mt-2" style="font-size: 0.9rem; color: #888">
          과목 : {{ selectedSubject }} / 학년: {{ selectedGrade.replace('초등 ', '') }}
        </div>
      </div>

      <template v-else>

        <PlaceCard2 v-for="(item, index) in filteredItems" :key="item.id || index" :item="item" @add="goToDetail(item)"
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
import axios from '@/api/axiosSetup';
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

      // 전체 목록을 저장
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

    // 탭 변경 시 API 호출 제거 (performSearch 함수는 필터 변경 시에만 호출)
    changeTab(tabName) {
      this.selectedTab = tabName;
      this.$router.replace({ query: { tab: tabName } });
    },

    // 상세 페이지 이동 함수
    goToDetail(item) {
      console.log(`상세 페이지로 이동:`, item.title);
      const queryParams = {
        mainCategoryTags: this.selectedSubject,
        subCategoryTags: item.hashtags,
        gradeTags: this.selectedGrade,
      };

      // item.itemType을 기준으로 경로 결정 (selectedTab 대신)
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

    // 아이템 클릭 핸들러
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // 뒤로가기 함수
    goBack() {
      this.$router.back();
    },

    // 필터 완료 핸들러
    handleFilterComplete(filterData) {
      console.log(`필터 선택 완료:`, filterData);
      this.setFilter(filterData.grade, filterData.subject);
      this.isModalOpen = false;
      this.performSearch(); // 필터 변경 시에는 API 다시 호출
    },

    // 검색 로직 (전시 및 답사 장소 모두 한 번에 호출)
    async performSearch() {
      console.log(`검색 실행 (모든 타입):`, {
        subject: this.selectedSubject,
        grade: this.selectedGrade
      });

      this.isSearching = true;
      this.allFetchedItems = []; // allFetchedItems 초기화

      const params = {
        searchType: 'all',
        subject: this.selectedSubject,
        grade: this.selectedGrade
      };

      try {
        const response = await axios.get('/api/content/search', { params });

        if (response.data && Array.isArray(response.data)) {
          // API 응답(전체)을 'allFetchedItems'에 저장
          const processedItems = response.data.map(item => {
            // item.id가 null일 경우를 대비하여 고유한 임시 키 생성
            if (!item.id) {
              item.id = `temp_${item.itemType}_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
            }

            // 'exhibition' (과학관/전시) 타입인 경우 뱃지 레이블 추가
            if (item.itemType === 'exhibition') {
              return {
                ...item,
                badgeLabel: '과학관'
              };
            }
            // 'science_place' (답사/과학장소) 타입인 경우 그대로 반환
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

    // 컴포넌트 생성 시 API 1회 호출
    this.performSearch();
  }
}
</script>

<style scoped>
/* -------------------- 레이아웃 및 컨테이너 -------------------- */
/* 페이지 전체 컨테이너 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f9f9f9;
}

/* 헤더 */
.chat-header {
  position: relative;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-weight: 600;
}

.filter-icon {
  font-size: 20px;
  /* 아이콘 크기 통일 */
}

/* -------------------- 🚨 개선된 탭 영역 스타일 🚨 -------------------- */
.segmented-control-wrapper {
  display: flex;
  justify-content: center;
  padding: 12px 0;
  background-color: white;
  border-bottom: 1px solid #eee;
}

.segmented-control {
  display: flex;
  width: 90%;
  max-width: 327px;
  background-color: #e0e0e0;
  border-radius: 20px;
}

.spec-button {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 38px;
  padding: 5px 16px;
  border-radius: 20px;
  background: transparent;
  color: #666;
  border: none;
  box-shadow: none;
  /* 그림자 제거 */
  transition: all 0.2s ease-in-out;
  font-weight: 500;
  font-size: 14px;
}

.spec-button.active {
  background: #4A7CEC;
  color: white;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* -------------------- 콘텐츠 및 스크롤 영역 -------------------- */
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

  padding: 16px;
  background-color: #f9f9f9;

  display: flex;
  flex-direction: column;
  /* 카드 아이템 간 간격 */
  gap: 16px;
}

/* 로딩/빈 상태 메시지 컨테이너 */
.status-message {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  min-height: 200px;
  margin-top: 0 !important;
  /* 인라인 스타일 덮어쓰기 */
}
</style>
