<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
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

      <div v-else class="card-list-wrapper">
        <UserLikeCourseCard v-for="(item, index) in filteredItems" :key="item.id || index" :item="item"
          @click="goToCourseDetail(item)" />
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
      // API 응답을 저장할 배열
      userLikeCourseCardItem: [],
      loading: true,
      error: null,
    };
  },

  computed: {
    filteredItems() {
      if (this.selectedTab === '전시') {
        // '전시' 탭일 때
        return this.userLikeCourseCardItem.filter(item =>
          item.type === '전시' || item.type === 'inner_course'
        );
      } else {
        // '답사' 탭일 때
        return this.userLikeCourseCardItem.filter(item =>
          item.type === '답사' || item.type === 'ai_course'
        );
      }
    },
  },

  // 라이프사이클 훅
  created() {
    // 탭 설정 로직
    const tabFromQuery = this.$route.query.tab;
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    }
    // API 호출 함수 실행 -> 사용자 ID로 저장한 코스 아이템 가져오기
    this.fetchUserLikeCourse();
  },

  methods: {
    // API 호출하고 데이터 매핑
    async fetchUserLikeCourse() {
      this.loading = true;
      this.error = null;

      // Pinia 스토어를 통해 로그인 상태를 확인
      if (!this.isLoggedIn) {
        this.error = "로그인이 필요한 기능입니다. 로그인 후 다시 시도해주세요.";
        this.loading = false;
        // 로그인 페이지로 이동
        this.$router.push('/login');
        return;
      }

      try {
        // 백엔드 API 호출
        const response = await axios.get(`api/schedules/user/${this.currentUserId}`);

        // response.data가 List<UserScheduleDTO> 형태
        // 프론트에서 (userLikeCourseCardItem) 구조로 변환
        this.userLikeCourseCardItem = response.data.map(schedule => {

          // schedule.items (ScheduleItemDetailDTO 리스트)를
          // courseItem 구조로 변환
          const mappedCourseItems = schedule.items.map(item => ({
            id: item.sourceItemId, // 소스 아이템 id
            number: item.sequence, // 스케줄 내 순서번호
            title: item.itemName,
            place: item.addressDetail,
            imageUrl: item.mainImageUrl,
            lat: item.latitude,
            lng: item.longitude,
            type: null, // '상설', '기획'

            scienceCenter: item.scienceCenterName,         // 과학관 이름
            hallName: item.hallName,             // 전시관 이름
            subject: item.mainCategoryNames || [], // 과학 영역 배열
            grade: item.gradeNames || [],        // 학년 배열
            hashtags: item.subCategoryNames || [],  // 세부 카테고리 배열,
            itemType: item.itemType,
            exhibitionList: item.exhibitionList || []
          }));

          // UserScheduleDto를 상위 객체 구조로 변환
          // 🚨 item.id의 안정성 확보: scheduleId가 없을 경우 임시 ID 할당
          const uniqueId = schedule.scheduleId || `temp_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;

          return {
            id: uniqueId,
            ExhibitionName: schedule.scheduleName,
            type: schedule.sourceCourseType, // 'inner_course' 또는 'ai_course' (이 값이 탭 필터링에 사용됨)

            // --- 첫 번째 아이템 정보로 대표값 설정 ---
            address: mappedCourseItems[0] ? mappedCourseItems[0].place : '정보 없음', // 대표 주소
            scienceCenter: mappedCourseItems[0] ? mappedCourseItems[0].scienceCenter : '정보 없음',  // 대표 과학관
            grade: mappedCourseItems[0] ? mappedCourseItems[0].grade?.[0] : null,          // 대표 학년
            subject: mappedCourseItems[0] ? mappedCourseItems[0].subject?.[0] : null,        // 대표 과학 영역

            // --- 아이템 목록에서 가공 ---
            coursePlaces: mappedCourseItems.map(item => item.title),   // 아이템 이름 목록
            courseItems: mappedCourseItems,               // 변환된 아이템 상세 리스트
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

    // 카드 클릭 시 상세 페이지로 이동
    goToCourseDetail(item) {
      console.log('goToCourseDetail - 클릭된 item:', item);

      // sessionStorage에 데이터 저장
      sessionStorage.setItem(`courseData_${item.id}`, JSON.stringify(item));

      this.$router.push({
        name: 'UserLikeCourseDetail',
        params: {
          courseId: item.id
        },
        state: {
          courseData: item,
          fromList: true
        }
      });
    },

    changeTab(tabName) {
      this.selectedTab = tabName;
      this.$router.replace({ query: { tab: tabName } });
    },

    goBack() {
      this.$router.back();
    },
  }
}
</script>

<style scoped>
/* -------------------- 레이아웃 및 컨테이너 -------------------- */
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
  font-weight: 600;
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
  /* gap 제거 */
}

.spec-button {
  flex: 1;
  /* 너비를 균등하게 나눔 */
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
  /* 활성화된 탭에만 은은한 그림자 */
}

/* -------------------- 콘텐츠 및 스크롤 영역 -------------------- */
.content-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f9f9f9;

  /* 스크롤바 숨기기 */
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

/* 로딩/에러/빈 상태 메시지 중앙 정렬 */
.status-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

/* -------------------- 기타 버튼 스타일 (참조용) -------------------- */
/* 이 코드는 해당 파일에 직접 사용되지는 않지만, 다른 곳에서 사용될 수 있으므로 남겨둡니다. */
.btn {
  /* .status-container 내 다시 시도 버튼 등에서 사용 */
  border-radius: 30px;
}
</style>
