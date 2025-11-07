<template>
  <div class="page-container" style="font-family: 'SUIT', sans-serif">
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 장소 목록
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

      <div v-else>
        <PlaceCard2 v-for="item in displayedItems" :key="item.id" :item="item"  
          :iconType="'heart'"
          @toggle-favorite="removeFromFavorites(item)"
          @add="goToDetail(item)"
          @item-click="handleItemClick(item)" />
      </div>
      
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
  setup() {
    const authStore = useAuthStore();
    const { isLoggedIn, currentUserId } = storeToRefs(authStore);
    return {
      isLoggedIn: isLoggedIn,
      currentUserId: currentUserId
    }
  },
  data() {
    return {
      // [삭제] allWishlistItems는 더 이상 필요 없습니다.
      // allWishlistItems: [], 

      // 화면에 실제로 표시할 데이터
      displayedItems: [], // ⬅ API 결과를 여기에 바로 담습니다.

      // 로딩 상태
      isSearching: false,
    };
  },
  computed: {
    // [삭제] computed 섹션 전체가 필요 없습니다.
    // gradeTag() { ... }
  },
  methods: {

    // [수정 없음] goToDetail은 item.itemType을 사용하므로 그대로 작동합니다.
    goToDetail(item) {
      const queryParams = {
        mainCategoryTags: item.subject, 
        subCategoryTags: item.hashtags,
        gradeTags: item.grade,
      };
      const typePath = item.itemType === 'exhibition' ? '/exhibition' : '/place';
      this.$router.push({
        path: `${typePath}/${item.id}`,
        query: queryParams
      });
    },

    // [수정 없음]
    handleItemClick(item) {
      this.goToDetail(item);
    },

    // [수정 없음]
    goBack() {
      this.$router.back();
    },

    // [삭제] applyFilters 메서드 전체를 삭제합니다.
    // applyFilters() { ... },


    // 🟢 찜 목록을 가져오는 함수 (수정됨)
    async performSearch() {
      console.log('performSearch 함수 시작');

      if (!this.isLoggedIn) {
        console.warn('로그인 상태가 아니므로 찜 목록을 로드하지 않습니다.');
        eventBus.emit('show-global-confirm', { /* ... */ });
        return;
      }

      this.isSearching = true;
      this.displayedItems = []; // [수정] displayedItems를 초기화

      try {
        console.log('🔵 API 호출 시작: /api/wishlist/my-list');
        const response = await axios.get(`/api/wishlist/my-list`);
        console.log('🟢 API 응답 받음:', response.data);

        if (response.data && Array.isArray(response.data)) {
          
          // ▼▼▼▼▼ [핵심 수정] ▼▼▼▼▼
          // allWishlistItems 대신 displayedItems에 바로 매핑합니다.
          this.displayedItems = response.data.map(item => {
            // PlaceCard2가 뱃지를 표시할 수 있도록 badgeLabel을 추가합니다.
            const badgeLabel = item.targetType === 'exhibition' ? '과학관' : null;

            return {
              // PlaceCard2 컴포넌트에 필요한 필드 매핑
              id: item.targetId,
              title: item.title,
              imageUrl: item.mainImageUrl,
              subject: item.mainCategory,
              grade: item.gradeTag,
              hashtags: item.subCategories,
              type: item.type, // '상설', '기획' 등
              place: item.place,

              // 필터링 및 뱃지용 내부 데이터
              itemType: item.targetType, // 'exhibition' or 'science_place'
              badgeLabel: badgeLabel   // ⬅ '과학관' or null
            };
          });
          // ▲▲▲▲▲ [핵심 수정] ▲▲▲▲▲

          console.log('API 응답 결과 (표시용): ', this.displayedItems.length, '개');

          // [삭제] applyFilters() 호출을 삭제합니다.
          // this.applyFilters(); 

        } else {
          console.error('API 응답 형식이 잘못되었습니다.', response.data);
          this.displayedItems = [];
        }
      } catch (error) {
        console.error("찜 목록 조회 중 오류 발생", error.response ? error.response.data : error.message);
        eventBus.emit("show-global-alert", { /* ... */ });
        this.displayedItems = [];
      } finally {
        this.isSearching = false;
      }
    },

    async removeFromFavorites(item) {
      console.log('찜 삭제 시도:', item.title);

      // 1. API에 삭제 요청 (PlaceDetailView의 찜삭제 로직과 동일)
      try {
        const requestData = {
          targetId: item.id,
          targetType: item.itemType,
          mainCategory: item.subject,
          gradeTag: item.grade
        };
        
        // 🌟 axios.delete는 { data: ... }로 body를 보내야 합니다.
        await axios.delete('/api/wishlist', { data: requestData });
        
        // 2. (성공 시) UI에서 즉시 제거
        // 	  (API를 다시 호출하는 것보다 빠르고 효율적입니다)
        this.displayedItems = this.displayedItems.filter(
          displayItem => displayItem.id !== item.id
        );
        
        eventBus.emit('show-global-alert', {
          message: '찜 목록에서 삭제되었습니다.',
          type: 'success'
        });

      } catch (error) {
        console.error("찜 삭제 실패:", error);
        eventBus.emit('show-global-alert', {
          message: '삭제에 실패했습니다. 다시 시도해 주세요.',
          type: 'error'
        });
      }
    }
  },

  created() {
    // [삭제] URL에서 탭 상태를 로드하는 로직 전체를 삭제합니다.
    // const tabFromQuery = this.$route.query.tab;
    // if (tabFromQuery === '답사') { ... } ...
    
    // [수정 없음] 컴포넌트 생성 시 API 호출은 그대로 둡니다.
    this.performSearch();
  }
}
</script>
<style scoped>
/* [삭제]
  .spec-button과 .spec-button.active 스타일은
  더 이상 사용되지 않으므로 삭제합니다.
*/

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