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
          id: 1,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '전시명1',
          address: '국립과천과학관',
          coursePlaces: ['전시명1', '전시명2', '전시명3'],
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 1,
              number: 1,
              title: '습지생물코너',
              place: '국립중앙과학관 자연사관',
              lat: 36.3758,
              lng: 127.3845
            },
            {
              id: 2,
              number: 2,
              title: '빛의 원리',
              place: '국립과천과학관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 3,
              number: 3,
              title: '미래 에너지',
              place: '서울시립과학관',
              lat: 37.6094,
              lng: 127.0706
            }
          ]
        },
        {
          id: 2,
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '3학년',
          ExhibitionName: '전시명2',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 4,
              number: 1,
              title: '화학 실험실',
              place: '국립과천과학관 화학관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 5,
              number: 2,
              title: '분자 모형 전시',
              place: '국립과천과학관 분자관',
              lat: 37.4360,
              lng: 126.9750
            }
          ]
        },
        {
          id: 3,
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '3학년',
          ExhibitionName: '전시명3',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 6,
              number: 1,
              title: '물리 체험관',
              place: '국립과천과학관 물리관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 7,
              number: 2,
              title: '전기 실험실',
              place: '국립과천과학관 전기관',
              lat: 37.4365,
              lng: 126.9748
            },
            {
              id: 8,
              number: 3,
              title: '자기장 체험',
              place: '국립과천과학관 자기관',
              lat: 37.4368,
              lng: 126.9752
            }
          ]
        },
        {
          id: 4,
          imageUrl: 'https://placehold.co/600x400',
          subject: '생명',
          grade: '3학년',
          ExhibitionName: '생명과학 탐험',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 9,
              number: 1,
              title: '생명의 기원',
              place: '국립과천과학관 생명관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 10,
              number: 2,
              title: 'DNA 모형',
              place: '국립과천과학관 유전자관',
              lat: 37.4366,
              lng: 126.9749
            }
          ]
        },
        {
          id: 5,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '지구과학 여행',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 11,
              number: 1,
              title: '지구의 구조',
              place: '국립과천과학관 지구관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 12,
              number: 2,
              title: '화석 전시관',
              place: '국립과천과학관 화석관',
              lat: 37.4370,
              lng: 126.9755
            }
          ]
        },
        {
          id: 6,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '천체 관측',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 13,
              number: 1,
              title: '천체 투영관',
              place: '국립과천과학관 천체관',
              lat: 37.4363,
              lng: 126.9746
            }
          ]
        },
        {
          id: 7,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '우주 탐험',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 14,
              number: 1,
              title: '우주선 모형',
              place: '국립과천과학관 우주관',
              lat: 37.4363,
              lng: 126.9746
            },
            {
              id: 15,
              number: 2,
              title: '달 탐사',
              place: '국립과천과학관 달관',
              lat: 37.4372,
              lng: 126.9758
            }
          ]
        },
        {
          id: 8,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '환경 보호',
          address: '국립과천과학관',
          type: '전시',
          // 코스 상세 데이터 추가
          courseItems: [
            {
              id: 16,
              number: 1,
              title: '환경 오염',
              place: '국립과천과학관 환경관',
              lat: 37.4363,
              lng: 126.9746
            }
          ]
        },
        {
          id: 9,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '장소명1',
          address: '장소명1 주소',
          type: '답사',
          // 답사 코스 상세 데이터 추가
          courseItems: [
            {
              id: 17,
              number: 1,
              title: '해운대 해변',
              place: '부산시 해운대구 해운대해변로',
              lat: 35.1587,
              lng: 129.1604
            },
            {
              id: 18,
              number: 2,
              title: '동백섬',
              place: '부산시 해운대구 동백로',
              lat: 35.1532,
              lng: 129.1635
            },
            {
              id: 19,
              number: 3,
              title: '해운대 온천',
              place: '부산시 해운대구 중동',
              lat: 35.1598,
              lng: 129.1588
            }
          ]
        },
        {
          id: 10,
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          ExhibitionName: '장소명2',
          address: '장소명2 주소',
          type: '답사',
          // 답사 코스 상세 데이터 추가
          courseItems: [
            {
              id: 20,
              number: 1,
              title: '경복궁 정문',
              place: '서울특별시 종로구 사직로 161',
              lat: 37.5796,
              lng: 126.9770
            },
            {
              id: 21,
              number: 2,
              title: '근정전',
              place: '경복궁 근정전',
              lat: 37.5794,
              lng: 126.9769
            },
            {
              id: 22,
              number: 3,
              title: '경회루',
              place: '경복궁 경회루',
              lat: 37.5802,
              lng: 126.9765
            }
          ]
        },
      ]
    };
  },

  computed: {
    filteredItems() {
      return this.userLikeCourseCardItem.filter(item => {
        return item.type === this.selectedTab;
      });
    },
  },

  methods: {
    goToCourseDetail(item) {
      this.$router.push({
        name: 'UserLikeCourseDetail',
        params: { ExhibitionName: item.ExhibitionName },
        query: { type: item.type }
      });
    },

    changeTab(tabName) {
      this.selectedTab = tabName;
      this.$router.replace({ query: { tab: tabName } });
    },

    goBack() {
      this.$router.back();
    },
  },

  created() {
    const tabFromQuery = this.$route.query.tab;
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
