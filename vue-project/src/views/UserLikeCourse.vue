<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 추천 코스 목록
      </div>
      <div class="header-right" style="flex: 1;">
      </div>
    </div>

    <div class="segmented-control-wrapper p-3 d-flex justify-content-center flex-shrink-0">
      <div class="segmented-control d-flex gap-3">
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
          @click="selectedTab = '전시'">전시</button>
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '탐험' }"
          @click="selectedTab = '탐험'">탐험</button>
      </div>
    </div>

    <div class="user-like-course">
      <UserLikeCourseCard v-for="idx in userLikeCourseCardItem" :key="idx.id" :item="idx"
        @click="goToCourseDetail(idx)" />
    </div>

  </div>
</template>

<script>
import { ref } from 'vue';
import UserLikeCourseCard from '@/components/UserLikeCourseCard.vue';
import router from '@/router';

export default {
  name: 'UserLikeCourse',
  components: {
    UserLikeCourseCard,
  },

  data() {
    return {
      selectedTab: '전시',

      // 🚨 [수정됨] v-for의 :key는 고유해야 합니다.
      // ExhibitionName이 중복되므로, 고유한 id 값을 추가했습니다.
      userLikeCourseCardItem: [
        {
          id: 1, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTxekcExxoOXGzyH63f9600ntpcb3S7f_5Lag&s',
          category: '지구',
          ExhibitionName: '전시명1',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 2, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '화학',
          ExhibitionName: '전시명2',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 3, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '물리',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 4, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '생명',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 5, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '지구',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 6, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '지구',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 7, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '지구',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
        {
          id: 8, // 👈 고유 ID
          imageSrc: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnbtgyWIAr34Y56OU_Cc1CXVI-JY3RBnoSpw&s',
          category: '지구',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          addressDetail: '상세주소입니다!!',
        },
      ]
    };

  },

  methods: {
    goToCourseDetail(item) {
      // ExhibitionName을 URL 파라미터로 전달
      router.push(`/UserLikeCourseDetail/${item.ExhibitionName}`);

      // 참고: 이름이 중복되므로, id로 보내는 것이 더 안전합니다.
      // router.push(`/UserLikeCourseDetail/${item.id}`);
      // (이 경우 router/index.js와 상세페이지 로직도 id 기준으로 변경해야 함)
    },

    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },
  },
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
