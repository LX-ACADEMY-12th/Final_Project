<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold" style="flex: 1; text-align: center;">
        관심 코스 목록
      </div>
      <div class="header-right" style="flex: 1;">
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

    <div v-if="loading" class="content-container status-container">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2 text-muted">관심 코스를 불러오고 있습니다...</p>
    </div>

    <div v-else-if="error" class="content-container status-container">
      <p class="text-danger">{{ error }}</p>
      <button @click="fetchUserLikeCourse" class="btn btn-sm btn-outline-primary">
        다시 시도
      </button>
    </div>

    <div v-else class="content-container">

      <div v-if="filteredItems.length === 0" class="status-container empty-state">
        <p class="text-muted">
          '{{ selectedTab }}' 탭에 해당하는 관심 코스가 없습니다.
        </p>
      </div>

      <!-- ✅ v-show로 변경하여 DOM을 유지하면서 보이기/숨기기만 처리 -->
      <div v-show="filteredItems.length > 0" class="card-list-wrapper">
        <UserLikeCourseCard v-for="item in filteredItems" :key="item.id" :item="item" @click="goToCourseDetail(item)" />
      </div>
    </div>
  </div>
</template>

<script>
import UserLikeCourseCard from '@/components/card/UserLikeCourseCard.vue';

import axios from '@/api/axiosSetup';

import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

export default {
  name: 'UserLikeCourse',
  components: {
    UserLikeCourseCard,
  },

  setup() {
    const authStore = useAuthStore();
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);

    return {
      isLoggedIn,
      currentUserId
    };
  },

  data() {
    return {
      selectedTab: '전시',
      userLikeCourseCardItem: [],
      loading: true,
      error: null,
      // ✅ 네비게이션 중 플래그
      isNavigating: false,
    };
  },

  computed: {
    filteredItems() {
      if (this.selectedTab === '전시') {
        return this.userLikeCourseCardItem.filter(item =>
          item.type === '전시' || item.type === 'inner_course'
        );
      } else {
        return this.userLikeCourseCardItem.filter(item =>
          item.type === '답사' || item.type === 'ai_course'
        );
      }
    },
  },

  created() {
    const tabFromQuery = this.$route.query.tab;
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    }
    this.fetchUserLikeCourse();
  },

  methods: {
    async fetchUserLikeCourse() {
      this.loading = true;
      this.error = null;

      if (!this.isLoggedIn) {
        this.error = "로그인이 필요한 기능입니다. 로그인 후 다시 시도해주세요.";
        this.loading = false;
        this.$router.push('/login');
        return;
      }

      try {
        const response = await axios.get(`api/schedules/user/${this.currentUserId}`);

        this.userLikeCourseCardItem = response.data.map(schedule => {
          const mappedCourseItems = schedule.items.map(item => ({
            id: item.sourceItemId,
            number: item.sequence,
            title: item.itemName,
            place: item.addressDetail,
            imageUrl: item.mainImageUrl,
            lat: item.latitude,
            lng: item.longitude,
            type: null,
            scienceCenter: item.scienceCenterName,
            hallName: item.hallName,
            subject: item.mainCategoryNames || [],
            grade: item.gradeNames || [],
            hashtags: item.subCategoryNames || [],
            itemType: item.itemType,
            exhibitionList: item.exhibitionList || []
          }));

          const uniqueId = schedule.scheduleId || `temp_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;

          return {
            id: uniqueId,
            ExhibitionName: schedule.scheduleName,
            type: schedule.sourceCourseType,
            address: mappedCourseItems[0] ? mappedCourseItems[0].place : '정보 없음',
            scienceCenter: mappedCourseItems[0] ? mappedCourseItems[0].scienceCenter : '정보 없음',
            grade: mappedCourseItems[0] ? mappedCourseItems[0].grade?.[0] : null,
            subject: mappedCourseItems[0] ? mappedCourseItems[0].subject?.[0] : null,
            coursePlaces: mappedCourseItems.map(item => item.title),
            courseItems: mappedCourseItems,
          };
        });

        console.log('✅ fetchUserLikeCourse - 최종 매핑된 데이터 (userLikeCourseCardItem):',
          JSON.stringify(this.userLikeCourseCardItem, null, 2)
        );
      } catch (err) {
        console.error("관심 코스 조회 실패:", err);
        this.error = "데이터를 불러오는 데 실패했습니다.";
      } finally {
        this.loading = false;
      }
    },

    // ✅ 수정: 네비게이션 중 플래그 사용
    goToCourseDetail(item) {
      // 이미 네비게이션 중이면 무시
      if (this.isNavigating) {
        return;
      }

      this.isNavigating = true;
      console.log('goToCourseDetail - 클릭된 item:', item);

      try {
        const plainItem = JSON.parse(JSON.stringify(item));
        sessionStorage.setItem(`courseData_${item.id}`, JSON.stringify(plainItem));

        this.$router.push({
          name: 'UserLikeCourseDetail',
          params: {
            courseId: item.id
          }
        }).catch(err => {
          console.error('라우터 네비게이션 에러:', err);
          this.isNavigating = false;
        });
      } catch (error) {
        console.error('상세 페이지 이동 중 에러:', error);
        this.isNavigating = false;
      }
    },

    // ✅ 간단하게 수정: URL 동기화 제거
    changeTab(tabName) {
      // 이미 선택된 탭이면 무시
      if (this.selectedTab === tabName) {
        return;
      }

      // 탭만 변경 (URL 동기화 제거)
      this.selectedTab = tabName;
    },

    goBack() {
      this.$router.back();
    },
  }
}
</script>

<style scoped>
/* 기존 스타일 동일 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f9f9f9;
}

.chat-header {
  position: relative;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-size: 16px;
}

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

.content-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f9f9f9;

  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.card-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 16px;
}

.status-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

.btn {
  border-radius: 30px;
}
</style>
