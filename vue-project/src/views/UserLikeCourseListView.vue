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

    <div class="segmented-control-wrapper p-3 d-flex justify-content-center flex-shrink-0">
      <div class="segmented-control d-flex gap-3">
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
          @click="changeTab('전시')">전시</button>
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
          @click="changeTab('답사')">답사</button>
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
        <UserLikeCourseCard v-for="item in filteredItems" :key="item.id" :item="item" @click="goToCourseDetail" />
      </div>
    </div>

  </div>
</template>

<script>
import UserLikeCourseCard from '@/components/card/UserLikeCourseCard.vue';
import axios from 'axios';

export default {
  name: 'UserLikeCourse',
  components: {
    UserLikeCourseCard,
  },

  data() {
    return {
      selectedTab: '전시',
      // API 응답을 저장할 배열
      userLikeCourseCardItem: [],
      loading: true,
      error: null,
      userId: 1, // 예시용 사용자 ID (실제로는 로그인 정보에서 가져오기)
    };
  },

  computed: {
    filteredItems() {
      if (this.selectedTab === '전시') {
        // '전시' 탭일 때
        // item.type이 '전시'이거나 'inner_course' 등 전시 관련 타입들을 모두 포함
        return this.userLikeCourseCardItem.filter(item =>
          item.type === '전시' || item.type === 'inner_course'
        );
      } else {
        // '답사' 탭일 때
        // item.type이 '답사'이거나 'ai_course' 등 답사 관련 타입들을 모두 포함
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
      try {
        // 백엔드 API 호출
        const response = await axios.get(`http://localhost:8080/api/schedules/user/${this.userId}`);

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
            scienceCenter: item.scienceCenterName,                 // 과학관 이름
            hallName: item.hallName,                          // 전시관 이름
            subject: item.mainCategoryNames || [], // 과학 영역 배열
            grade: item.gradeNames || [],               // 학년 배열
            hashtags: item.subCategoryNames || [],   // 세부 카테고리 배열,
            itemType: item.itemType
          }));

          // UserScheduleDto를 상위 객체 구조로 변환
          return {
            id: schedule.scheduleId,
            ExhibitionName: schedule.scheduleName,
            type: schedule.sourceCourseType, // 'inner_course' 또는 'ai_course' (이 값이 탭 필터링에 사용됨)

            // --- 첫 번째 아이템 정보로 대표값 설정 ---
            address: mappedCourseItems[0] ? mappedCourseItems[0].place : '정보 없음', // 대표 주소
            scienceCenter: mappedCourseItems[0] ? mappedCourseItems[0].scienceCenter : '정보 없음', // 대표 과학관
            grade: mappedCourseItems[0] ? mappedCourseItems[0].grade?.[0] : null, // 대표 학년
            subject: mappedCourseItems[0] ? mappedCourseItems[0].subject?.[0] : null, // 대표 과학 영역

            // --- 아이템 목록에서 가공 ---
            coursePlaces: mappedCourseItems.map(item => item.title), // 아이템 이름 목록
            courseItems: mappedCourseItems, // 변환된 아이템 상세 리스트

          };
        });
        // --- 👇 [로그 추가] 매핑된 최종 데이터 확인 ---
        console.log('✅ fetchUserLikeCourse - 최종 매핑된 데이터 (userLikeCourseCardItem):',
          // JSON.stringify를 사용하면 중첩된 객체도 펼쳐서 보여줍니다 (선택 사항).
          // JSON.stringify(this.userLikeCourseCardItem, null, 2)
          JSON.stringify(this.userLikeCourseCardItem, null, 2)// 객체 그대로 로깅하면 콘솔에서 펼쳐볼 수 있습니다.
        );
        // --- 👆 [로그 추가] ---
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

.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

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

.user-like-course {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;

  padding: 16px;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
