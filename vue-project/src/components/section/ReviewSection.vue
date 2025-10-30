<template>
  <section class="review-section">
    <h3 class="section-title">{{ sectionTitle }}</h3>

    <div class="review-summary">
      <span class="review-stars" v-html="getFilledStars(rating)"></span>
      <span class="total-score">{{ rating.toFixed(1) }}</span>
      <span class="review-count">({{ totalReviews }}개 리뷰)</span>
    </div>

    <span class="subsection-title">사진 후기 ({{ photoReviewCount }})개</span>

    <div class="photo-reviews">
      <button v-for="(p, i) in allPhotoThumbnails" :key="p.reviewId || i" class="photo-btn"
        :style="{ backgroundImage: `url(${p.url})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
        @click="openModalFromThumb(p, i)" :title="`리뷰 ${p.reviewId}`"></button>
      
      <button class="photo-btn more-btn" v-if="morePhotoCountToShow > 0" @click="goToAllPhotosPage">
        <i class="bi bi-plus-circle"></i>
      </button>
      
      <button class="photo-btn more-btn" v-else-if="allPhotoThumbnails.length === 0 && photoReviewCount === 0">사진 없음</button>
    </div>

    <button class="write-review-btn" @click="$emit('show-modal')">
      <i class="bi bi-pencil-square"></i> 후기작성
    </button>

    <div v-if="isLoading" class="loading-message">리뷰 로딩 중...</div>
    <div v-else-if="error" class="error-message">리뷰 로딩 실패</div>

    <div class="review-list" v-else>
      <div class="review-item" v-for="review in reviews" :key="review.reviewId" :data-review-id="review.reviewId">

        <div class="reviewer-profile">
          <img :src="review.authorProfileImageUrl || 'https://via.placeholder.com/40'" alt="프로필 이미지" class="avatar">
          <span class="name">{{ review.authorName }}</span>
          <span class="stars" v-html="getFilledStars(review.rating)"></span>
        </div>

        <p class="review-content">{{ review.content }}</p>

        <div v-if="Array.isArray(review.photoUrls) && review.photoUrls.length" class="review-photos-grid">
          <button v-for="(url, idx) in review.photoUrls" :key="idx" class="photo-cell"
            :style="{ backgroundImage: `url(${url})` }" @click="openPhotoViewer(review, idx)"
            :aria-label="`리뷰 ${review.reviewId} 사진 ${idx + 1}`"></button>
        </div>

        <div class="review-meta">
          <span class="date">{{ formatReviewDate(review.createdAt) }}</span>
          <span class="likes" :class="{ active: likedStatus[review.reviewId] }" @click="toggleLike(review.reviewId)">
            <i :class="getLikeIcon(review.reviewId)"></i> 도움됐어요 {{ review.likeCount }}
          </span>
        </div>

        <button class="more-options" @click="toggleReportMenu(review.reviewId)">
          <i class="bi bi-three-dots"></i>
        </button>

        <div class="report-menu" v-if="openReportMenuId === review.reviewId">
          <div v-if="review.authorId === currentUserId">
            <button class="edit-btn" @click="$emit('edit-review', review)">
              <i class="bi bi-pencil"></i>
              수정하기
            </button>
            <button class="edit-btn-delete" @click="onClickDelete(review.reviewId)">
              <i class="bi bi-trash3"></i>
              삭제하기
            </button>
          </div>
          <div v-else>
            <button class="report-btn" @click="reportReview(review.reviewId)">
              <i class="bi bi-bell"></i> 신고하기
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="totalPages > 1 && !isLoading">
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">
        &lt; 이전
      </button>

      <button v-for="page in totalPages" :key="page" @click="goToPage(page)" class="page-btn"
        :class="{ active: page === currentPage }">
        {{ page }}
      </button>

      <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">
        다음 &gt;
      </button>
    </div>

    <PhotoModal :show="photoModal.visible" :images="photoModal.images" :startIndex="photoModal.startIndex"
      @close="photoModal.visible = false" />
  </section>
</template>

<script>
import axios from 'axios'
import PhotoModal from '../modal/PhotoModal.vue';
import router from '@/router';

// API 베이스 (Vite 환경변수 우선)
const API_BASE = import.meta.env?.VITE_API_BASE || 'http://localhost:8080';

export default {
  name: 'ReviewSection',
  components: { PhotoModal },
  props: {
    targetId: {
      type: [Number, String],
      required: true
    },
    targetType: {
      type: String,
      required: true
    },
    currentUserId: {
      type: [Number, String],
      required: true
    },
    rating: {
      type: Number,
      required: true,
      default: 0
    },
    reviewCount: {
      type: Number,
      required: true,
      default: 0
    },
    photoReviewCount: {
      type: Number,
      default: 0
    },
    isPlace: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  emits: ['show-modal', 'edit-review', 'request-delete'],

  computed: {
    sectionTitle() {
      return this.isPlace ? '장소 후기' : '방문자 후기';
    },
    allPhotoUrls() {
      const allPhotos = [];
      for (const r of this.reviews) {
        if (Array.isArray(r.photoUrls)) {
          allPhotos.push(...r.photoUrls);
        }
      }
      return allPhotos.filter(Boolean);
    },
    morePhotoCountToShow() {
      const remaining = this.photoReviewCount - this.allPhotoThumbnails.length;
      return Math.max(0, remaining);
    },
  },

  watch: {
    targetId: {
      handler(newId) {
        if (newId && this.targetType) {
          console.log(`✅ [ReviewSection] ID 변경 감지, 1페이지 로드`);
          this.currentPage = 1;
          this.fetchReviews();
          this.fetchPhotoThumbnails(); // ⭐️ [추가] 썸네일도 새로고침
          this.allPhotoUrlsCache = null; // ⭐️ [추가] 캐시 비우기
        }
      },
      immediate: true
    },
    targetType: {
      handler(newType) {
        if (newType && this.targetId) {
          console.log(`✅ [ReviewSection] Type 변경 감지, 1페이지 로드`);
          this.currentPage = 1;
          this.fetchReviews();
          this.fetchPhotoThumbnails(); // ⭐️ [추가] 썸네일도 새로고침
          this.allPhotoUrlsCache = null; // ⭐️ [추가] 캐시 비우기
        }
      },
      immediate: true
    }
  },

  data() {
    return {
      reviews: [],
      isLoading: false,
      error: null,
      openReportMenuId: null,
      likedStatus: {},
      photoModal: {
        visible: false,
        images: [],
        startIndex: 0
      },
      currentPage: 1,
      pageSize: 3,
      totalPages: 1,
      totalReviews: 0,
      allPhotoThumbnails: [],
      allPhotoUrlsCache: null, // ⭐️ [추가] 이 선언이 빠졌습니다.
    };
  },

  // ⭐️ [수정] methods 블록 시작
  methods: {
    goToPage(pageNumber) {
      if (pageNumber < 1 || pageNumber > this.totalPages || pageNumber === this.currentPage) {
        return;
      }
      this.currentPage = pageNumber;
      this.fetchReviews();
    },
    // ⭐️ [신규] 1~3번 썸네일 클릭 시 모달 여는 메서드
    async openModalFromThumb(clickedThumbnail, index) {
      let allPhotos = this.allPhotoUrlsCache;

      // 1. 캐시가 없으면 API로 '모든' 사진 URL을 가져옵니다.
      if (!allPhotos) {
        console.log('[modal-thumb] 캐시 없음. 전체 사진 URL 로드 시도...');
        try {
          // ❗️ 이 API는 백엔드에 있어야 합니다.
          // (지난 대화에서 확인한 '사진 전체 모아보기' API)
          const { data } = await axios.get(`${API_BASE}/api/reviews/target/${this.targetType}/${this.targetId}/photos`);
          
          if (!Array.isArray(data) || data.length === 0) {
              console.error('[modal-thumb] 사진이 없습니다.');
              return;
          }
          
          allPhotos = data;
          this.allPhotoUrlsCache = allPhotos; // ⭐️ 캐시에 저장
          console.log(`[modal-thumb] 전체 사진 ${allPhotos.length}개 로드 및 캐시 완료.`);

        } catch (err) {
          console.error('[modal-thumb] 전체 사진 URL 로드 실패:', err);
          alert('사진을 불러오는 데 실패했습니다.');
          return;
        }
      } else {
          console.log(`[modal-thumb] 캐시된 사진 ${allPhotos.length}개 사용.`);
      }

      // 2. 모달을 엽니다.
      // 썸네일(limit 3)과 전체 사진이 같은 순서(예: 최신순)라고 가정합니다.
      // 만약 순서가 다르면, clickedThumbnail.url로 findIndex를 수행해야 합니다.
      let startIndex = allPhotos.indexOf(clickedThumbnail.url);
      
      if (startIndex === -1) {
          console.warn(`[modal-thumb] 썸네일 URL(${clickedThumbnail.url})을 전체 목록에서 찾지 못했습니다. 썸네일 순서(${index})를 사용합니다.`);
          // 썸네일 순서(0, 1, 2)를 시작 인덱스로 사용
          startIndex = index; 
      }

      this.photoModal.images = allPhotos;
      this.photoModal.startIndex = startIndex;
      this.photoModal.visible = true;
    },

    openPhotoViewer(review, startIndex = 0) {
      const imgs = Array.isArray(review.photoUrls) ? review.photoUrls.filter(Boolean) : [];
      if (!imgs.length) return;
      this.photoModal.images = imgs;
      this.photoModal.startIndex = startIndex;
      this.photoModal.visible = true;
    },

    onClickDelete(reviewId) {
      this.$emit('request-delete', { reviewId });
      this.openReportMenuId = null;
    },

    // ⭐️ [신규] 썸네일 전용 API 호출 메서드
    async fetchPhotoThumbnails() {
      if (!this.targetId || !this.targetType) return;

      try {
        const params = {
          targetId: this.targetId,
          targetType: this.targetType,
          limit: 3 // ⭐️ UI에 표시할 썸네일 개수
        };

        // ❗️ [중요] 이 API는 백엔드에 새로 만들어야 할 수 있습니다.
        // targetId/Type에 해당하는 "모든" 사진 리뷰 중 
        // 최신순 4개의 썸네일(reviewId, url)을 반환하는 API입니다.
        const { data: thumbs } = await axios.get(`${API_BASE}/api/reviews/photos-summary`, { params });
        this.allPhotoThumbnails = Array.isArray(thumbs) ? thumbs : [];
        console.log('[photo-thumbs] 썸네일 로드 성공:', this.allPhotoThumbnails);

      } catch (err) {
        console.error('[photo-thumbs] 썸네일 로드 실패:', err?.response?.data || err.message);
        this.allPhotoThumbnails = []; // 실패시 비움
      }
    },

    // ⭐️ [수정] fetchReviews 메서드 구조 수정
    async fetchReviews() {
      if (!this.targetId || !this.targetType) return;

      this.isLoading = true;
      this.error = null;

      // ⭐️ [수정] 불필요한 바깥쪽 try 제거
      try {
        // 1) 목록 API 호출
        const params1 = {
          targetId: this.targetId,
          targetType: this.targetType,
          page: this.currentPage,
          size: this.pageSize
        };
        console.log('[reviews] params =', params1, 'API_BASE=', API_BASE);

        const { data: reviewPage } = await axios.get(`${API_BASE}/api/reviews`, { params: params1 });
        console.log('[reviews] data =', reviewPage);

        this.reviews = Array.isArray(reviewPage.content) ? reviewPage.content : [];
        this.totalPages = reviewPage.totalPages || 1;
        this.totalReviews = reviewPage.totalElements || 0;

        // ⭐️ [복원] 좋아요 상태 API 호출 로직 (이전 버전 기준)
        const seed = {};
        for (const r of this.reviews) {
          if (typeof r.likedByCurrentUser === 'boolean') seed[r.reviewId] = r.likedByCurrentUser;
        }

        let likedSet = new Set();
        if (this.currentUserId != null && this.currentUserId !== '') {
          const params2 = {
            targetId: this.targetId,
            targetType: this.targetType,
            viewerUserId: this.currentUserId
          };
          console.log('[liked-status] params =', params2);
          try {
            const { data: liked } = await axios.get(`${API_BASE}/api/reviews/liked-status`, { params: params2 });
            console.log('[liked-status] data =', liked);
            const ids = Array.isArray(liked?.likedReviewIds) ? liked.likedReviewIds : [];
            likedSet = new Set(ids.map(x => Number(x)));
          } catch (e) {
            console.error('[liked-status] FAIL:', e?.response?.status, e?.response?.data || e.message);
          }
        }

        const newLikedStatus = {};
        for (const r of this.reviews) {
          const idNum = Number(r.reviewId);
          if (typeof seed[r.reviewId] === 'boolean') {
            newLikedStatus[r.reviewId] = seed[r.reviewId];
          } else {
            newLikedStatus[r.reviewId] = likedSet.has(idNum);
          }
        }
        this.likedStatus = newLikedStatus;

      } catch (err) {
        console.error('[reviews] FAIL:', err?.response?.status, err?.response?.data || err.message);
        this.error = err;
      } finally {
        this.isLoading = false;
      }
    }, // ⭐️ [수정] fetchReviews 메서드 닫기

    // (날짜 포맷팅)
    formatReviewDate(dateString) {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
          year: 'numeric', month: '2-digit', day: '2-digit'
        }).replace(/\. /g, '.').replace(/\.$/, '');
      } catch (error) {
        console.log(`날짜 포맷팅 실패 -> ${error}`);
        return dateString;
      }
    },

    // (별점 그리기)
    getFilledStars(score) {
      if (typeof score !== 'number') score = 0;
      const fullStars = Math.floor(score);
      const halfStar = score % 1 >= 0.5 ? 1 : 0;
      const emptyStars = 5 - fullStars - halfStar;

      let starsHtml = '';
      starsHtml += '<i class="bi bi-star-fill"></i>'.repeat(fullStars);
      starsHtml += '<i class="bi bi-star-half"></i>'.repeat(halfStar);
      starsHtml += '<i class="bi bi-star"></i>'.repeat(emptyStars);
      return starsHtml;
    },

    // (신고 메뉴)
    toggleReportMenu(reviewId) {
      if (this.openReportMenuId === reviewId) {
        this.openReportMenuId = null;
      } else {
        this.openReportMenuId = reviewId;
      }
    },

    // (신고)
    async reportReview(reviewId) {
      try {
        const review = this.reviews.find(r => r.reviewId === reviewId);
        if (review && String(review.authorId) === String(this.currentUserId)) {
          alert('본인 리뷰는 신고할 수 없습니다.');
          return;
        }

        const reason = window.prompt('신고 사유를 입력하세요 (예: 욕설/모욕, 스팸 등)');
        if (!reason || !reason.trim()) return;

        await axios.post(`${API_BASE}/api/reviews/${reviewId}/report`, {
          reason: reason.trim(),
          reporterUserId: this.currentUserId
        });

        alert('신고가 접수되었습니다.');
      } catch (e) {
        console.error('신고 실패:', e);
        alert(`신고에 실패했습니다: ${e.response?.data || e.message}`);
      } finally {
        this.openReportMenuId = null;
      }
    },

    // (좋아요 아이콘)
    getLikeIcon(reviewId) {
      return this.likedStatus[reviewId] ? 'bi bi-hand-thumbs-up-fill' : 'bi bi-hand-thumbs-up';
    },

    // (좋아요 토글)
    async toggleLike(reviewId) {
      const isLiked = this.likedStatus[reviewId];

      try {
        if (isLiked) {
          await axios.delete(`${API_BASE}/api/reviews/${reviewId}/like`);
        } else {
          await axios.post(`${API_BASE}/api/reviews/${reviewId}/like`);
        }
        this.likedStatus[reviewId] = !isLiked;
        const review = this.reviews.find(r => r.reviewId === reviewId);
        if (review) {
          review.likeCount += (isLiked ? -1 : 1);
        }
        console.log(`리뷰 ID: ${reviewId}, API 성공 후 상태: ${!isLiked ? '좋아요' : '취소'}`);
      } catch (error) {
        console.error('좋아요 처리 실패:', error);
        alert(`좋아요 처리에 실패했습니다: ${error.response?.data || error.message}`);
      }
    },

    goToAllPhotosPage() {
      const path = `/photos/${this.targetType}/${this.targetId}`;
      console.log('전체 사진 페이지로 이동:', path);
      router.push(path);
    },

  }, // ⭐️ [수정] methods 블록 닫기

  created() {
    // (created 훅은 watch의 immediate: true로 대체되었으므로 비워둡니다)
  }
};
</script>
<style scoped>
/* (스타일 태그는 변경 사항 없음) */
.review-section {
  padding: 15px;
  background-color: white;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
}

.review-summary {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.review-stars {
  color: #FFC107;
  margin-right: 8px;
  font-size: 24px;
}

.total-score {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-right: 8px;
}

.review-count {
  font-size: 14px;
  line-height: 1.2;
  color: #999;
}

.subsection-title {
  font-size: 16px;
  font-weight: bold;
  margin: 16px 0 16px;
}

.photo-reviews {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  margin-bottom: 16px;
}

.photo-btn {
  background-color: #f0f0f0;
  color: #666;
  border: none;
  padding: 0;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  flex-basis: 25%;
  width: 0;
  aspect-ratio: 1 / 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.more-btn {
  background-color: #ddd;
  font-weight: bold;
}

.write-review-btn {
  width: 100%;
  background-color: #4A89F3;
  color: white;
  border: none;
  padding: 12px 0;
  border-radius: 48px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-bottom: 8px;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 16px 0;
  position: relative;
}

.reviewer-profile {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 8px;
  background-color: #ddd;
}

.name {
  font-weight: bold;
  font-size: 14px;
  margin-right: 8px;
}

.review-item .stars {
  color: #FFC107;
  font-size: 14px;
}

.review-content {
  font-size: 14px;
  font-weight: 400;
  color: #333;
  line-height: 1.4;
  margin: 5px 0 10px 50px;
}

.review-content-photo {
  font-size: 14px;
  font-weight: 400;
  color: #333;
  line-height: 1.4;
  margin: 5px 0 10px 50px;

  background-color: #202020;
  color: #666;
  border: none;
  padding: 0;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  flex-basis: 25%;
  width: 100px;
  aspect-ratio: 1 / 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.review-meta {
  display: flex;
  font-size: 12px;
  line-height: 1.2;
  color: #999;
  margin-left: 50px;
}

.review-meta .date {
  margin-right: 8px;
}

.review-meta .likes {
  cursor: pointer;
}

.review-meta .likes .active {
  color: #4A89F3;
  font-weight: 600;
}

.more-options {
  position: absolute;
  top: 16px;
  right: 0;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.report-menu {
  position: absolute;
  top: 40px;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.edit-btn {
  display: block;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #202020;
  cursor: pointer;
  white-space: nowrap;
}

.edit-btn-delete {
  display: block;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #EB3223;
  cursor: pointer;
  white-space: nowrap;
}

.report-btn {
  display: block;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #EB3223;
  cursor: pointer;
  white-space: nowrap;
}

.report-btn:hover {
  background-color: #f0f0f0;
}

/* ⭐️ [추가] 로딩/에러 메시지 스타일 */
.loading-message,
.error-message {
  padding: 40px 20px;
  text-align: center;
  color: #888;
  font-size: 14px;
}

.error-message {
  color: #EB3223;
  /* 에러 색상 */
}

/* 리뷰 본문 아래 사진 그리드 */
.review-photos-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  /* 4칸 */
  gap: 8px;
  margin: 8px 0 10px 50px;
  /* 본문 들여쓰기와 정렬 */
}

/* 정사각형 셀 */
.photo-cell {
  aspect-ratio: 1 / 1;
  border: none;
  border-radius: 8px;
  background-color: #f0f0f0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  cursor: pointer;
  box-shadow: 0 0 0 1px #eee inset;
}

/* ⭐️ [추가] 페이지네이션 스타일 예시 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0 10px 0;
  /* 위쪽 여백 */
}

.page-btn {
  border: 1px solid #ddd;
  background-color: white;
  padding: 8px 12px;
  margin: 0 4px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  transition: background-color 0.2s, color 0.2s;
}

.page-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
  background-color: #f9f9f9;
}

.page-btn.active {
  background-color: #4A7CEC;
  /* (기존 .write-review-btn 색상과 통일) */
  color: white;
  border-color: #4A7CEC;
  font-weight: bold;
}

.page-btn:not(:disabled):not(.active):hover {
  background-color: #f0f0f0;
}
</style>