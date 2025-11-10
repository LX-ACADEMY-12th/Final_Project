<template>
  <div class="app-container">
    <!-- Header -->
    <div class="bg-white p-3 d-flex align-items-center border-bottom app-header">
      <button class="btn btn-link text-dark p-0 me-3" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <div class="flex-grow-1 text-center">
        <h5 class="mb-0 fw-bold">국립중앙과학관</h5>
        <!-- <small class="text-muted">{{ selectedHallId }}</small> -->
      </div>
      <div style="width:40px;"></div>
    </div>

    <!--
      Content - [수정]
      'scrollable-content' 클래스 추가
    -->
    <div class="d-flex flex-column flex-grow-1 p-3 gap-3 scrollable-content">

      <ExhibitionAccordion v-for="hall in sortedHalls" :key="hall.key" :title="hall.name || '전시관 정보없음'"
        :is-active="activeSection === hall.key" :preview-image-url="getPreviewUrl(hall.key)"
        @toggle="toggleSection(hall.key)" @open-modal="openModal(hall.key)" />

    </div>
  </div>

  <!-- 층별 안내도 모달 - 컴포넌트화됨 -->

  <FloorGuideModal :show="showModal" :hall-name="currentHallName" :floor-guides="floorGuides" @close="closeModal"
    @floor-change="handleFloorChange" />
</template>

<script>
// 1. 컴포넌트 import
import ExhibitionAccordion from '@/components/ExhibitionAccordion.vue';
import FloorGuideModal from '@/components/modal/FloorGuideModal.vue';
import rawAxios from 'axios';
import { floorGuideMap, defaultGuide } from '@/data/floorGuideMap.js'; // 맵 임포트

export default {
  name: 'ExhibitionHall',
  // 컴포넌트 등록
  components: {
    ExhibitionAccordion,
    FloorGuideModal
  },
  data() {
    return {
      activeSection: null,
      showModal: false,
      currentHall: null,
      floorGuides: [],
      selectedHallId: null, // 라우터에서 받을 ID
      halls: []
    }
  },
  computed: {
    /**
    * 선택된 항목을 맨 위로 정렬한 배열
    */
    sortedHalls() {
      if (!this.halls.length) return [];

      const hallsCopy = [...this.halls];

      const selectedIndex = hallsCopy.findIndex(
        hall => hall.key == this.selectedHallId
      );

      if (selectedIndex > 0) {
        const [selectedItem] = hallsCopy.splice(selectedIndex, 1);
        hallsCopy.unshift(selectedItem);
      }

      return hallsCopy;
    },

    currentHallName() {
      if (!this.currentHall) return '';
      const hall = this.halls.find(h => h.key === this.currentHall);
      return hall ? hall.name : '';
    }
  },
  /**
   * 페이지가 생성될 때 라우터 쿼리를 읽습니다.
   */
  created() {
    const { scienceCenterName, selectedHallId } = this.$route.query;

    this.selectedHallId = selectedHallId;

    if (scienceCenterName) {
      this.fetchHalls(scienceCenterName);
    }
  },
  methods: {
    /**
     * API를 호출하여 데이터를 가져오는 함수
     */
    async fetchHalls(scienceCenterName) {
      try {
        const response = await rawAxios.get(`http://localhost:8080/api/halls?scienceCenterName=${scienceCenterName}`);
        console.log(`API 호출, {}`, scienceCenterName)
        this.halls = response.data;
        console.log(JSON.stringify(this.halls, null, 2))

        if (this.selectedHallId && this.halls.some(h => h.key == this.selectedHallId)) {
          const activeHall = this.halls.find(h => h.key == this.selectedHallId);
          this.openModal(activeHall.key);
          this.activeSection = activeHall.key; // 아코디언도 열기
        }
      } catch (error) {
        console.error("전시관 목록 로딩 실패:", error);
      }
    },
    // [추가] 미리보기 이미지 URL을 반환하는 헬퍼 메서드
    getPreviewUrl(hallKey) {
      // openModal과 동일하게 floorGuideMap에서 가이드를 찾습니다.
      const guides = floorGuideMap[hallKey];

      // 가이드가 있고, 이미지가 1개 이상 있다면 첫 번째 이미지 URL을 반환합니다.
      if (guides && guides.length > 0) {
        return guides[0].imageUrl;
      }

      // 이미지가 없으면 null을 반환합니다.
      return null;
    },
    goBack() {
      this.$router.go(-1);
    },

    toggleSection(section) {
      // 같은 섹션을 클릭하면 닫고, 다른 섹션을 클릭하면 열기
      this.activeSection = this.activeSection === section ? null : section;
    },

    /**
     * 모달을 열 때, 해당 hall의 동적 층 정보를 전달
     */
    openModal(hallKey) {
      const hall = this.halls.find(h => h.key == hallKey);
      if (!hall) return;

      this.currentHall = hall.key; // currentHallName 계산용

      const guides = floorGuideMap[hallKey];

      if (guides && guides.length > 0) {
        this.floorGuides = guides;
      } else {
        console.warn(`'${hall.name}'(key: ${hallKey})에 대한 층별 안내도 맵이 없습니다.`);
        this.floorGuides = defaultGuide;
      }

      this.showModal = true;
    },

    closeModal() {
      this.showModal = false;
      this.floorGuides = []; // 모달이 닫히면 데이터 초기화
    },

    handleFloorChange(index) {
      console.log('Floor changed to:', this.floorGuides[index].floorName);
    }
  }
}
</script>

<style scoped>
.app-container {
  font-family: 'SUIT Variable', sans-serif;
  width: 100%;

  /* [수정] min-height -> height로 변경하여 뷰포트 높이로 고정 */
  /* 100dvh는 모바일 브라우저 주소창 크기 변화에도 대응합니다. */
  height: 100dvh;

  background: #f8fafc;
  box-shadow: 0 4px 24px rgba(90, 114, 210, 0.09);
  margin: 0 auto;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  position: relative;

  /* [추가] 자식 요소가 컨테이너를 넘어가도 잘리도록 설정 (중요) */
  overflow: hidden;
}

/* [추가] 스크롤이 필요한 콘텐츠 영역 */
.scrollable-content {
  flex: 1;
  /* 남은 공간을 모두 차지 (flex-grow:1, flex-shrink:1) */
  overflow-y: auto;
  /* 내용이 넘칠 경우 이 영역만 세로 스크롤 */
  min-height: 0;
  /* flex 아이템이 줄어들 수 있도록 보장 (중요) */

  /* --- 스크롤바 숨기기 (Cross-browser) --- */

  /* 1. Firefox */
  scrollbar-width: none;

  /* 2. IE, Edge (Legacy) */
  -ms-overflow-style: none;
}

/* 3. Chrome, Safari, Opera (WebKit) */
.scrollable-content::-webkit-scrollbar {
  display: none;
  /* 스크롤바를 보이지 않게 함 */
  width: 0;
  /* 스크롤바 너비 0 */
}


.app-header {
  border-radius: 18px 18px 0 0;
  /* [추가] 헤더는 줄어들지 않도록 설정 */
  flex-shrink: 0;
}

.btn-link {
  text-decoration: none;
}
</style>
