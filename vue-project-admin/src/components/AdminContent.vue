<template>
  <div>
    <ContentHeader iconClass="bi bi-pencil-square" title="3. 컨텐츠 편집" description="전시관, 전시, 체험 장소 데이터를 관리합니다." />

    <ul class="nav nav-tabs mb-3">
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'halls' }" @click="switchTab('halls')">전시관 관리</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'exhibitions' }" @click="switchTab('exhibitions')">전시 관리</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" :class="{ active: activeTab === 'places' }" @click="switchTab('places')">체험 장소 관리</a>
      </li>
    </ul>

    <div class="d-flex justify-content-end align-items-center mb-4">
      <div v-if="activeTab === 'exhibitions' && filterHallId" class="me-auto">
        <span class="badge bg-primary fs-6 fw-normal d-flex align-items-center">
          <i class="bi bi-filter me-2"></i>
          '{{ filteredHallName }}' 전시만 보는 중
          <button type="button" class="btn-close btn-close-white ms-2" style="font-size: 0.75rem;"
            @click="clearExhibitionFilter" aria-label="Close"></button>
        </span>
      </div>

      <button class="btn btn-primary ms-3" @click="handleCreateClick">
        <i class="bi bi-plus-circle me-2"></i>
        {{ { halls: '전시관', exhibitions: '전시', places: '체험 장소' }[activeTab] }} 등록
      </button>
    </div>

    <div v-if="isLoading" class="text-center py-5">
    </div>

    <div v-show="!isLoading && activeTab === 'halls'">
      <HallListTable :halls="hallList" @edit="id => handleEditClick(id)" @delete="id => handleDeleteClick(id)"
        @view-exhibitions="handleViewExhibitions" />
    </div>
    <div v-show="!isLoading && activeTab === 'exhibitions'">
      <ExhibitionListTable :exhibitions="exhibitionList" @edit="id => handleEditClick(id)"
        @delete="id => handleDeleteClick(id)" />
    </div>
    <div v-show="!isLoading && activeTab === 'places'">
      <PlaceListTable :places="placeList" @edit="id => handleEditClick(id)" @delete="id => handleDeleteClick(id)" />
    </div>

    <ContentModal v-if="showModal" :show="showModal" :modal-type="modalType" :item-id-to-edit="itemIdToEdit"
      @close="showModal = false; itemIdToEdit = null;" @save="handleSaveSuccess" />
  </div>
</template>

<script>
import axios from 'axios';
import ContentHeader from "./ContentHeader.vue";
import ContentModal from "./ContentModal.vue";
import HallListTable from './HallListTable.vue';
import ExhibitionListTable from './ExhibitionListTable.vue';
import PlaceListTable from './PlaceListTable.vue';

export default {
  name: "AdminContent",
  components: {
    ContentHeader,
    ContentModal,
    HallListTable,
    ExhibitionListTable,
    PlaceListTable
  },
  data() {
    return {
      activeTab: 'halls',
      isLoading: false,
      hallList: [],
      exhibitionList: [],
      placeList: [],

      showModal: false,
      modalType: 'halls',
      itemIdToEdit: null,

      filterHallId: null, // [추가] 전시 탭 필터링용
      filteredHallName: '', // [추가] 필터링 중인 전시관 이름
    };
  },
  methods: {
    // [신규] 탭 클릭 핸들러 (필터 초기화)
    switchTab(tabName) {
      if (this.activeTab === tabName) return; // 같은 탭 클릭 방지

      if (tabName !== 'exhibitions') {
        this.filterHallId = null;
        this.filteredHallName = '';
      }
      this.activeTab = tabName;
    },

    // [R] 탭에 맞는 데이터 로드
    async loadDataForActiveTab() {
      this.isLoading = true;
      try {
        let url = `/api/admin/content/${this.activeTab}`;

        // [수정] 전시 탭 로드 시 필터 ID 추가
        if (this.activeTab === 'exhibitions' && this.filterHallId) {
          url += `?hallId=${this.filterHallId}`;
        }

        const response = await axios.get(url);

        console.log(JSON.stringify(response, null, 2));

        if (this.activeTab === 'halls') this.hallList = response.data;
        else if (this.activeTab === 'exhibitions') this.exhibitionList = response.data;
        else if (this.activeTab === 'places') this.placeList = response.data;

      } catch (error) {
        console.error("데이터 로드 실패:", error);
      } finally {
        this.isLoading = false;
      }
    },

    // [신규] '전시 개수' 버튼 클릭 핸들러
    handleViewExhibitions(hall) { // HallListDto 객체를 받음
      this.filterHallId = hall.id;
      this.filteredHallName = hall.name;
      this.activeTab = 'exhibitions'; // 탭 변경 -> watch가 loadData 호출
    },

    // [신규] 필터 지우기 핸들러
    clearExhibitionFilter() {
      this.filterHallId = null;
      this.filteredHallName = '';
      this.loadDataForActiveTab(); // 필터 없이 다시 로드
    },

    // [C] '등록' 버튼 클릭
    handleCreateClick() {
      this.modalType = this.activeTab; // 'halls', 'exhibitions', 'places'
      this.itemIdToEdit = null; // 생성 모드
      this.showModal = true;
    },

    // [U] '수정' 버튼 클릭 (id 받음)
    handleEditClick(id) {
      this.modalType = this.activeTab;
      this.itemIdToEdit = id; // 수정 모드 (ID 전달)
      this.showModal = true;
    },

    // [D] '삭제' 버튼 클릭 (id 받음)
    async handleDeleteClick(id) {
      const typeName = { halls: '전시관', exhibitions: '전시', places: '체험 장소' }[this.activeTab];
      if (!confirm(`[ID: ${id}] ${typeName}을(를) 정말 삭제하시겠습니까?`)) return;

      try {
        await axios.delete(`/api/admin/content/${this.activeTab}/${id}`);
        alert("삭제되었습니다.");
        this.loadDataForActiveTab(); // 목록 새로고침
      } catch (error) {
        console.error("삭제 실패:", error);
        alert("삭제에 실패했습니다. (다른 데이터에서 참조 중일 수 있습니다)");
      }
    },

    // 모달 저장 성공 시
    handleSaveSuccess() {
      this.showModal = false;
      this.itemIdToEdit = null;
      alert("저장되었습니다.");
      this.loadDataForActiveTab(); // 목록 새로고침
    }
  },
  created() {
    this.loadDataForActiveTab(); // 컴포넌트 생성 시 첫 탭 데이터 로드
  },
  watch: {
    // [수정] 탭이 바뀌면 (프로그래밍 또는 클릭) 데이터 로드
    activeTab(newTab, oldTab) {
      if (newTab !== oldTab) {
        this.loadDataForActiveTab();
      }
    }
  }
};
</script>

<style scoped>
.nav-tabs .nav-link {
  cursor: pointer;
}

.badge .btn-close {
  opacity: 1;
}

.badge .btn-close:hover {
  opacity: 0.75;
}
</style>