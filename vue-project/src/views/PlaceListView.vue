<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        추천 장소 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
        <i class="bi bi-sliders fs-5" style="cursor: pointer;" @click.prevent="isModalOpen = true">
        </i>
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
      <!-- 카드 아이템 반복 -->
      <PlaceCard2 v-for="item in currentCarouselItems" :key="item.id" :item="item" @add=goToDetail(item)
        @item-click="handleItemClick(item)" />
    </div>
    <FilterModal v-if="isModalOpen" :showSearchOptions="false" :initialSearchType="'filter'"
      :initialSubject="selectedSubject" :initialGrade="selectedGrade" @close="isModalOpen = false"
      @complete="handleFilterComplete" />
  </div>
</template>

<script>
import PlaceCard2 from '@/components/card/PlaceCard2.vue';
import FilterModal from '@/components/modal/FilterModal.vue';

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
          grade: '3학년', // [수정] 형식 통일
          title: '습지생물코너',
          type: '상설',
          place: '국립중앙과학관 자연사관',
          hashtags: ['생명과학'],
          lat: 36.3758,
          lng: 127.3845
        },
        {
          id: 2,
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '4학년', // [수정] 형식 통일
          title: '빛의 원리',
          type: '기획',
          place: '국립과천과학관',
          hashtags: ['파동', '빛'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 3,
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '5학년', // [수정] 형식 통일
          title: '미래 에너지',
          type: '상설',
          place: '서울시립과학관',
          hashtags: ['에너지', '화학 반응'],
          lat: 37.6094,
          lng: 127.0706
        },
        {
          id: 101,
          imageUrl: 'https://placehold.co/600x400/FFCC00/000',
          subject: '물리',
          grade: '3학년', // [수정] 형식 통일
          title: '떠오르는 힘, 부력',
          type: '상설',
          place: '국립중앙과학관',
          hashtags: ['유체', '물리1'],
          lat: 36.3758,
          lng: 127.3845
        },
        {
          id: 102,
          imageUrl: 'https://placehold.co/600x400/00CCFF/000',
          subject: '지구',
          grade: '5학년', // [수정] 형식 통일
          title: '태양계 행성 탐험',
          type: '기획',
          place: '서울시립과학관',
          hashtags: ['우주', '천체'],
          lat: 37.6094,
          lng: 127.0706
        },
        // --- [신규 추가 5개] ---
        {
          id: 103,
          imageUrl: 'https://placehold.co/600x400/99FF99/000',
          subject: '생명',
          grade: '6학년',
          title: '인체의 신비',
          type: '상설',
          place: '국립과천과학관',
          hashtags: ['소화', '순환'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 104,
          imageUrl: 'https://placehold.co/600x400/FF99CC/000',
          subject: '화학',
          grade: '4학년',
          title: '반짝반짝 결정',
          type: '기획',
          place: '서울시립과학관',
          hashtags: ['물질', '용액'],
          lat: 37.6094,
          lng: 127.0706
        },
        {
          id: 105,
          imageUrl: 'https://placehold.co/600x400/CC99FF/000',
          subject: '지구',
          grade: '6학년',
          title: '움직이는 대륙',
          type: '상설',
          place: '국립중앙과학관',
          hashtags: ['판 구조론', '화산'],
          lat: 36.3758,
          lng: 127.3845
        },
        {
          id: 106,
          imageUrl: 'https://placehold.co/600x400/FFCC00/000',
          subject: '물리',
          grade: '5학년',
          title: '전기와 자기',
          type: '상설',
          place: '국립과천과학관',
          hashtags: ['전자기', '회로'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 107,
          imageUrl: 'https://placehold.co/600x400/99FF99/000',
          subject: '생명',
          grade: '3학년',
          title: '동물의 한살이',
          type: '기획',
          place: '서울시립과학관',
          hashtags: ['곤충', '동물'],
          lat: 37.6094,
          lng: 127.0706
        },
      ],
      // '답사' 탭을 위한 목데이터 추가
      fieldTripItems: [
        {
          id: 4,
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '5학년', // [수정] 형식 통일
          title: '해운대 (지질 탐사)',
          place: '부산시 해운대구',
          hashtags: ['고체지구', '퇴적암'],
          lat: 35.1587,
          lng: 129.1604
        },
        {
          id: 5,
          imageUrl: 'https://placehold.co/600x400/CCBBAA/000000',
          subject: '물리',
          grade: '4학년', // [수정] 형식 통일
          title: '서울숲 (공원 산책)',
          place: '서울시 성동구',
          hashtags: ['운동', '자연'],
          lat: 37.5445,
          lng: 127.0374
        },
        {
          id: 201,
          imageUrl: 'https://placehold.co/600x400/FF99AA/000',
          subject: '화학',
          grade: '3학년', // [수정] 형식 통일
          title: '갯벌 체험 (염전)',
          place: '인천 소래습지',
          hashtags: ['소금', '물질의 특성'],
          lat: 37.4021,
          lng: 126.7301
        },
        {
          id: 202,
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '5학년', // [수정] 형식 통일
          title: '영월 한반도 지형',
          place: '강원도 영월군',
          hashtags: ['침식', '퇴적'],
          lat: 37.2045,
          lng: 128.4557
        },
        // --- [신규 추가 5개] ---
        {
          id: 203,
          imageUrl: 'https://placehold.co/600x400/99FF99/000',
          subject: '생명',
          grade: '6학년',
          title: '양서류 관찰',
          place: '국립생태원',
          hashtags: ['생태계', '동물'],
          lat: 36.6631,
          lng: 126.6913
        },
        {
          id: 204,
          imageUrl: 'https://placehold.co/600x400/FF99CC/000',
          subject: '화학',
          grade: '6학년',
          title: '암모니아 분수 실험장',
          place: 'LG사이언스파크',
          hashtags: ['산염기', '기체'],
          lat: 37.5649,
          lng: 126.8300
        },
        {
          id: 205,
          imageUrl: 'https://placehold.co/600x400/FFCC00/000',
          subject: '물리',
          grade: '3학년',
          title: '그림자 놀이 체험',
          place: '어린이대공원',
          hashtags: ['빛', '그림자'],
          lat: 37.5492,
          lng: 127.0747
        },
        {
          id: 206,
          imageUrl: 'https://placehold.co/600x400/99FF99/000',
          subject: '생명',
          grade: '4학년',
          title: '서울 식물원',
          place: '서울 식물원',
          hashtags: ['식물', '광합성'],
          lat: 37.5704,
          lng: 126.8359
        },
        {
          id: 207,
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '4학년',
          title: '화성암 관찰 (한탄강)',
          place: '포천 한탄강',
          hashtags: ['화성암', '지층'],
          lat: 38.0069,
          lng: 127.2088
        }
      ]
    };
  },
  computed: {
    // selectedTab + selectedSubject + selectedGrade 따라 표시할 아이템을 동적으로 결정하는 computed 속성
    currentCarouselItems() {
      // 1. 탭에 따라 기본 목록 선택
      let baseList = [];
      if (this.selectedTab === '전시') {
        baseList = this.exhibitionItems;
      } else if (this.selectedTab === '답사') {
        baseList = this.fieldTripItems;
      }

      // 2. 선택된 필터로 baseList를 필터링
      const filteredList = baseList.filter(item => {
        // A. 과목이 일치하는가?
        const subjectMatch = item.subject === this.selectedSubject;

        // B. 학년이 일치하는가? (예: "3학년" === "초등 3학년" 방지)
        //    Modal에서 "초등 3학년"을 보내므로, 데이터의 "3학년"과 비교하려면 가공이 필요합니다.
        //    (여기서는 목데이터의 학년("3학년")과 모달의 학년("초등 3학년")이 다르다고 가정)

        //    [가정] 모달은 '초등 3학년', 데이터는 '3학년'
        const gradeMatch = item.grade === this.selectedGrade.replace('초등 ', '');

        //    [가정] 모달과 데이터 형식이 '3학년'으로 동일
        // const gradeMatch = item.grade === this.selectedGrade;

        // 둘 다 일치해야 함
        return subjectMatch && gradeMatch;
      });

      return filteredList;
    }
  },
  methods: {

    // 탭 클릭 시 URL과 상태를 함께 변경하는 메서드
    changeTab(tabName) {
      this.selectedTab = tabName;
      // 브라우저 히스토리에 쌓이지 않도록 'replace'를 사용
      this.$router.replace({ query: { tab: tabName } });
    },

    // 장소 상세페이지 이동 함수
    goToDetail(item) {
      console.log(`상세 페이지로 이동:`, item.title);
      if (this.selectedTab === '전시') {
        // '전시' 탭이면 /exhibition/ID 로 이동
        console.log(`전시 상세로 이동 (ID: ${item.id}):`, item.title);
        this.$router.push(`/exhibition/${item.id}`);
      } else {
        // '답사' 탭이면 /place/ID 로 이동
        console.log(`장소 상세로 이동 (ID: ${item.id}):`, item.title);
        this.$router.push(`/place/${item.id}`);
      }
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
    },
  },
  created() {
    // URL 에서 ?tab= ... 값을 읽어온다.
    const tabFromQuery = this.$route.query.tab;

    // 쿼리 값이 '답사' 이면 '답사' 탭을, 그 외에는 '전시'를 기본으로 선택
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    } else {
      this.selectedTab = '전시';
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
