<template>
  <div class="app-container">
    <!-- Header -->
    <div class="bg-white p-3 d-flex align-items-center border-bottom app-header">
      <button class="btn btn-link text-dark p-0 me-3" @click="goBack">
        <i class="bi bi-arrow-left fs-4"></i>
      </button>
      <div class="flex-grow-1 text-center">
        <h5 class="mb-0 fw-bold">과천과학관</h5>
        <small class="text-muted">현재기술관</small>
      </div>
      <div style="width:40px;"></div>
    </div>

    <!-- Content - 컴포넌트화된 아코디언들 -->
    <div class="d-flex flex-column flex-grow-1 p-3 gap-3">
      <ExhibitionAccordion v-for="hall in halls" :key="hall.key" :title="hall.name"
        :is-active="activeSection === hall.key" @toggle="toggleSection(hall.key)" @open-modal="openModal(hall.key)" />
    </div>
  </div>

  <!-- 층별 안내도 모달 - 컴포넌트화됨 -->
  <FloorGuideModal :show="showModal" :hall-name="currentHallName" :floors="floors" @close="closeModal"
    @floor-change="handleFloorChange" />
</template>

<script>
// 1. 컴포넌트 import
import ExhibitionAccordion from '@/components/ExhibitionAccordion.vue';
import FloorGuideModal from '@/components/modal/FloorGuideModal.vue';

export default {
  name: 'ExhibitionHall',
  // 2. 컴포넌트 등록
  components: {
    ExhibitionAccordion,
    FloorGuideModal
  },
  data() {
    return {
      activeSection: null,
      showModal: false,
      currentHall: null,
      floors: ['B1F', '1F', '2F', '3F'],
      // 3. 전시관 데이터를 배열로 관리
      halls: [
        { key: 'tech', name: '과천과학관 현재기술관' },
        { key: 'nature', name: '과천과학관 자연사관' },
        { key: 'insect', name: '과천과학관 곤충생태관' },
        { key: 'kids', name: '과천과학관 유아체험관' }
      ]
    }
  },
  computed: {
    currentHallName() {
      const hall = this.halls.find(h => h.key === this.currentHall);
      return hall ? hall.name : '';
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    toggleSection(section) {
      // 같은 섹션을 클릭하면 닫고, 다른 섹션을 클릭하면 열기
      this.activeSection = this.activeSection === section ? null : section;
    },
    openModal(hall) {
      this.currentHall = hall;
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
    },
    handleFloorChange(index) {
      // 층이 변경될 때 필요한 로직 (현재는 로그만)
      console.log('Floor changed to:', this.floors[index]);
    }
  }
}
</script>

<style scoped>
.app-container {
  font-family: 'SUIT Variable', sans-serif;
  width: 100%;
  max-width: 430px;
  min-height: 100vh;
  background: #f8fafc;
  box-shadow: 0 4px 24px rgba(90, 114, 210, 0.09);
  margin: 0 auto;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.app-header {
  border-radius: 18px 18px 0 0;
}

.btn-link {
  text-decoration: none;
}
</style>
