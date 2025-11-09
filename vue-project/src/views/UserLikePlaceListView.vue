<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 목록
      </div>
      <div class="header-right" style="flex: 1; text-align: right;">
      </div>
    </div>

    <div class="user-like-course">

      <div v-if="isSearching" class="text-center p-5 text-muted w-100" style="margin-top: 20px;">
        가져오는 중...
      </div>
      <div v-else-if="displayedItems.length === 0" class="text-center p-5 text-muted w-100" sytle="margin-top: 20px;">
        <div>표시할 장소가 없습니다.</div>
      </div>

      <template v-else>

        <PlaceCard2 v-for="item in displayedItems" :key="item.id" :item="item" iconType="heart" @add="goToDetail(item)"
          @toggle-favorite="handleRemoveFavorite(item)" @item-click="handleItemClick(item)" />

      </template>
    </div>

  </div>
</template>
<script>
import PlaceCard2 from '@/components/card/PlaceCard2.vue';
import eventBus from '@/utils/eventBus';
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

export default {
  name: 'LikePlace',
  components: {
    PlaceCard2,
  },
  // 컴포넌트가 생성되기 전에 실행되는 진입점
  setup() {
    const authStore = useAuthStore();
    // 1. 두 값을 모두 꺼냅니다.
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);

    // 2. 두 값 모두 반환합니다.
    return {
      isLoggedIn: isLoggedIn,
      currentUserId: currentUserId
    }
  },
  data() {
    return {
      // API 원본 데이터를 저장할 배열
      allWishlistItems: [],

      // 화면에 실제로 표시할 데이터 (필터링 결과)
      displayedItems: [],

      // 로딩 상태
      isSearching: false,
    };
  },
  computed: {
  },
  methods: {

    async handleRemoveFavorite(item) {
      console.log(`[찜 취소] ${item.title} (ID: ${item.id}, Type: ${item.itemType})`);
      // 1. 찜 취소 로직을 실행하기 전에, 먼저 확인창을 띄웁니다.
      eventBus.emit('show-global-confirm', {
        message: '해당 관심 장소를 삭제하시겠습니까?',
        // 2. 사용자가 '확인'을 눌렀을 때만 실행될 async 함수
        onConfirm: async () => {
          console.log(`[찜 취소] 확인 완료. API 호출`);
          try {
            // [수정 1] API 주소를 '/api/wishlist'로 변경 (뒤에 /delete 제거)
            // [수정 2] 'data:'를 사용 (params: 아님)
            await axios.delete(`/api/wishlist`, {
              data: {
                targetId: item.id,
                targetType: item.itemType
                // (참고: PlaceDetailView는 mainCategory와 gradeTag도 보냈지만,
                // 삭제 시에는 이 두 개만 있어도 될 겁니다.)
              }
            });
            // (성공 로직 - 동일)
            this.displayedItems = this.displayedItems.filter(i => i.id !== item.id);
            this.allWishlistItems = this.allWishlistItems.filter(i => i.id !== item.id);
            eventBus.emit('show-global-alert', {
              message: '관심 목록에서 삭제되었습니다.',
              type: 'success'
            });
          } catch (error) {
            // (에러 처리 - 동일)
            console.error('찜 취소 실패:', error);
            eventBus.emit('show-global-alert', {
              message: error.response?.data?.message || '삭제에 실패했습니다. 다시 시도해주세요.',
              type: 'error'
            });
          }
        }// onConfirm 끝
      }); // event.Bus 끝
    }, // handleRemoveFavorite 끝

    // 장소 상세페이지 이동 함수 (기존 로직 유지)
    goToDetail(item) {
      const queryParams = {
        mainCategoryTags: item.subject, // 👈 [수정] 아이템의 카테고리를 사용
        subCategoryTags: item.hashtags,
        gradeTags: item.grade,     // 👈 [수정] 아이템의 학년 태그를 사용
      };

      // item.itemType (exhibition/science_place)에 따라 경로 설정
      const typePath = item.itemType === 'exhibition' ? '/exhibition' : '/place';

      this.$router.push({
        path: `${typePath}/${item.id}`,
        query: queryParams
      });
    },

    // 아이템 클릭 핸들러 (기존 로직 유지)
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // 뒤로가기 함수 (기존 로직 유지)
    goBack() {
      this.$router.back();
    },

    // 🟢 찜 목록을 최초에 한 번만 가져오는 함수
    async performSearch() {

      console.log('performSearch 함수 시작')

      // 로그인 상태 확인 (setup에서 반환된 isLoggedIn 사용)
      if (!this.isLoggedIn) {
        console.warn('로그인 상태가 아니므로 찜 목록을 로드하지 않습니다.');
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      this.isSearching = true;
      this.allWishlistItems = [];
      this.displayedItems = []; // 화면 목록 초기화

      try {
        console.log('🔵 API 호출 시작: /api/wishlist/my-list'); // 디버깅용
        // 백엔드 API 호출
        const response = await axios.get(`/api/wishlist/my-list`);

        console.log('🟢 API 응답 받음:', response); // 전체 응답 확인
        console.log('🟢 response.data:', response.data); // 데이터 확인

        if (response.data && Array.isArray(response.data)) {
          // API 응답을 원본 데이터 배열에 저장
          this.allWishlistItems = response.data.map(item => {
            return {
              // PlaceCard2 컴포넌트에 필요한 필드 매핑
              id: item.targetId,
              title: item.title,
              imageUrl: item.mainImageUrl,

              subject: item.mainCategory,
              grade: item.gradeTag,
              hashtags: item.subCategories,

              type: item.type,                    // 기획 OR 상설
              place: item.place,

              // 필터링을 위한 내부 데이터
              itemType: item.targetType,
            };
          });

          console.log('API 응답 결과 (원본): ', this.allWishlistItems.length, '개');
          this.displayedItems = this.allWishlistItems;
          console.log(`[표시 완료] 전체 ${this.displayedItems.length}개`)

        } else {
          console.error('API 응답 형식이 잘못되었습니다.', response.data);
          this.allWishlistItems = [];
          this.displayedItems = [];
        }
      } catch (error) {
        console.error("찜 목록 조회 중 오류 발생", error.response ? error.response.data : error.message);
        eventBus.emit("show-global-alert", {
          message: '찜 목록을 불러오는 중 오류가 발생했습니다.',
          type: 'error'
        });
        this.allWishlistItems = [];
        this.displayedItems = [];
      } finally {
        this.isSearching = false;
      }
    },
  },

  created() {
    // URL에서 탭 상태 로드 (기존 로직 유지)
    const tabFromQuery = this.$route.query.tab;
    if (tabFromQuery === '답사') {
      this.selectedTab = '답사';
    } else {
      this.selectedTab = '전시';
    }
    // 컴포넌트 생성 시 (최초 로드 시) API 호출
    this.performSearch();
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

/* 페이지 전체 컨테이너 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 헤더 */
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

/* 카드 목록 영역 */
.user-like-course {
  flex: 1;
  overflow-y: auto;

  /* 스크롤바 숨기기 */
  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
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
