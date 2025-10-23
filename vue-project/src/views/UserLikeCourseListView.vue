<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
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

    <div class="user-like-course">
      <UserLikeCourseCard v-for="idx in filteredItems" :key="idx.id" :item="idx" @click="goToCourseDetail(idx)" />
    </div>

  </div>
</template>

<script>
import UserLikeCourseCard from '@/components/card/UserLikeCourseCard.vue';

export default {
  name: 'UserLikeCourse',
  components: {
    UserLikeCourseCard,
  },

  data() {
    return {
      selectedTab: '전시',
      userLikeCourseCardItem: [
        {
          id: 1, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명1',
          address: '국립과천과학관',
          coursePlaces: ['전시명1', '전시명2', '전시명3'],
          type: '전시',
        },
        {
          id: 2, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '3학년',
          ExhibitionName: '전시명2',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 3, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 4, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '생명',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 5, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 6, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 7, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 8, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
        },
        {
          id: 9, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '장소명1',
          address: '장소명1 주소',
          type: '답사',
        },
        {
          id: 10, // 👈 고유 ID
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '장소명2',
          address: '장소명2 주소',
          type: '답사',
        },
      ]
    };

  },
  computed: {
    /**
     * 'filteredItems'는 data()의 selectedTab 값이 바뀔 때마다
     * 자동으로 다시 계산되는 새로운 배열입니다.
     */
    // template 코드에서 v-for가 userLikeCourseCardItem이 아닌, filteredItems를 사용할 수 있도록 변경함
    filteredItems() {
      // 1. this.selectedTab 값 (예: '전시')을 가져옵니다.
      // 2. this.userLikeCourseCardItem (원본 배열)에서
      //    item의 type이 '전시'인 것만 골라(filter) 새 배열로 만듭니다.
      return this.userLikeCourseCardItem.filter(item => {
        return item.type === this.selectedTab;
      });
    },
  },
  methods: {
    goToCourseDetail(item) {
      // ExhibitionName을 URL 파라미터로 전달
      // '전시'든 '답사'든 같은 경로('/coursedetail')로 보낸다.
      // 대신, URL 뒤에 ?type=전시 또는 ?type=답사 꼬리표를 붙인다.
      //this.$router.push(`/coursedetail/${item.ExhibitionName}`);

      this.$router.push({
        name: 'UserLikeCourseDetail', // path: '/coursedetail/${item.ExhibitionName'
        params: { ExhibitionName: item.ExhibitionName }, // :ExhibitionName부분
        query: { type: item.type } // URL 뒤에 ?type=... 을 붙인다.
      })
      // 참고: 이름이 중복되므로, id로 보내는 것이 더 안전합니다.
      // router.push(`/UserLikeCourseDetail/${item.id}`);
      // (이 경우 router/index.js와 상세페이지 로직도 id 기준으로 변경해야 함)
    },
    // 탭 변경 시 URL도 함께 변경하는 함수 정의
    changeTab(tabName) {
      this.selectedTab = tabName;
      // router.replace를 사용하여 히스토리 스택에 추가하지 않고 URL 변경
      this.$router.replace({ query: { tab: tabName } });
    },
    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },
  },
  created() {
    // 컴포넌트가 생성될 때 URL 쿼리를 확인합니다.
    // (이 시점에는 'this.$route'로 접근할 수 있습니다.)
    const tabFromQuery = this.$route.query.tab;

    // 만약 쿼리 값이 '답사'이면, data의 selectedTab 값을 덮어씁니다.
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    }
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
  /* [!!] padding 수정 (좌우 16px) */
  padding: 5px 16px;
  gap: 8px;

  position: relative;
  /* [!!] absolute -> relative (부모 div가 위치를 잡음) */
  /* [!!] 고정 width 제거 -> 텍스트 길이에 맞게 자동 조절 */
  /* width: 84px; */
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
  /* [!!] Figma 디자인의 파란색으로 변경 */
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}


/* 페이지 전체 컨테이너
  - 화면 전체 높이(100vh)를 차지
  - flex-direction: column (자식 요소를 세로로 배치)
*/
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  /* 이 컨테이너 자체가 스크롤되는 것을 방지 */
}

/* [헤더]
   채팅방 헤더와 동일한 구조
*/
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

/* 카드 목록 영역 (자식 2)
  - flex: 1 (나머지 세로 공간을 모두 차지)
  - overflow-y: auto (내용이 넘칠 경우에만 세로 스크롤바 생성)
*/
.user-like-course {
  flex: 1;
  overflow-y: auto;

  /* 스크롤바 숨기기 */
  /* Chrome, Safari, Edge 등 (웹킷 브라우저) */
  &::-webkit-scrollbar {
    display: none;
  }

  /* Firefox */
  scrollbar-width: none;

  /* IE (구형) */
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
