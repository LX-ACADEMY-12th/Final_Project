<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        추천 과학 컨텐츠 장소 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
        <i class="bi bi-sliders fs-5" style="cursor: pointer;" @click.prevent="isModalOpen = true">
        </i>
      </div>
    </div>

    <div class=" segmented-control-wrapper p-3 d-flex justify-content-center flex-shrink-0">
      <div class="segmented-control d-flex gap-3">
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
          @click="selectedTab = '전시'">전시</button>
        <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
          @click="selectedTab = '답사'">답사</button>
      </div>
    </div>

    <div class="user-like-course">
      <!-- 카드 아이템 반복 -->
      <PlaceCard2 v-for="item in currentCarouselItems" :key="item.id" :item="item" @add=goToDetail(item)
        @item-click="handleItemClick(item)" />
    </div>
    <FilterModal v-if="isModalOpen" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />
  </div>
</template>

<script>
import PlaceCard2 from '@/components/PlaceCard2.vue';
import FilterModal from '@/components/FilterModal.vue';

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
      // 필터 상태를 data에 추가
      selectedSubject: '물리',
      selectedGrade: '초등 3학년',
      // '전시' 탭을 위한 목데이터
      exhibitionItems: [
        {
          id: 1,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '습지생물코너',
          type: '상설',
          place: '국립중앙과학관 자연사관',
          hashtags: ['항상성과 몸의 조절', '생명과학과 인간의 생활'],
          lat: 36.3758, // 국립중앙과학관
          lng: 127.3845
        },
        {
          id: 2,
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '4학년',
          title: '빛의 원리',
          type: '기획',
          place: '국립과천과학관',
          hashtags: ['파동', '빛', '물리1', '체험'],
          lat: 37.4363, // 국립과천과학관
          lng: 126.9746
        },
        {
          id: 3,
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '5학년',
          title: '미래 에너지',
          type: '상설',
          place: '서울시립과학관',
          hashtags: ['에너지', '화학 반응', '미래 기술'],
          lat: 37.6094, // 서울시립과학관
          lng: 127.0706
        }
      ],
      // '답사' 탭을 위한 목데이터 추가
      fieldTripItems: [
        {
          id: 4, // ID 중복 방지 (1 -> 4)
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '5학년',
          title: '해운대',
          place: '부산시 해운대구',
          hashtags: ['고체지구', '유체지구', '천체'],
          lat: 35.1587, // 해운대
          lng: 129.1604
        },
        {
          id: 5, // ID 중복 방지 (2 -> 5)
          imageUrl: 'https://placehold.co/600x400/CCBBAA/000000',
          subject: '물리',
          grade: '4학년',
          title: '서울숲',
          place: '서울시 성동구',
          hashtags: ['고체지구', '유체지구', '천체'],
          lat: 37.5445, // 서울숲
          lng: 127.0374
        }
      ]
    };
  },
  computed: {
    // selectedTab에 따라 표시할 아이템을 동적으로 결정하는 computed 속성
    currentCarouselItems() {
      if (this.selectedTab === '전시') {
        return this.exhibitionItems;
      } else if (this.selectedTab === '답사') {
        return this.fieldTripItems;
      }
      return []; // 기본값
    }
  },
  methods: {
    // 템플릿에 맞게 goToCourseDetail -> goToDetail로 이름 변경
    goToDetail(item) {
      // ExhibitionName 대신 id로 전달 (목데이터에 ExhibitionName이 없음)
      this.$router.push(`/coursedetail/${item.id}`);
    },

    // 뒤로가기 함수
    goBack() {
      // Vue.Router를 이용하여 이전페이지로 이동
      this.$router.back();
    },

    // 모달에서 '선택 완료를 눌렀을 때 실행되는 함수'
    handleFilterComplete(filterData) { // [!!] filterData 인자 추가
      console.log(`필터 선택 완료:`, filterData);

      this.selectedSubject = filterData.subject;
      this.selectedGrade = filterData.grade;

      this.isModalOpen = false;

      this.performSearch();
    },
    performSearch() {
      console.log(`검색 실행:`, {
        subject: this.selectedSubject,
        grade: this.selectedGrade
      });
      // TODO: 검색 API 호출 로직 구현
    }
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
