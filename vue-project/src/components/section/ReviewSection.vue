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

      <button class="photo-btn more-btn" v-else-if="allPhotoThumbnails.length === 0 && photoReviewCount === 0">사진
        없음</button>
    </div>

    <button class="write-review-btn" @click="onClickWriteReview">
      <i class="bi bi-pencil-square"></i> 후기작성
    </button>

    <div v-if="isLoading" class="loading-message">리뷰 로딩 중...</div>
    <div v-else-if="error" class="error-message">리뷰 로딩 실패</div>

    <div class="review-list" v-else>
      <div class="review-item" v-for="review in reviews" :key="review.reviewId" :data-review-id="review.reviewId">
        <div class="reviewer-profile">
          <img :src="review.authorProfileImageUrl || 'https://via.placeholder.com/40'" alt="프로필 이미지" class="avatar" />
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
          <div v-if="String(review.authorId) === String(currentUserId)">
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
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">&lt; 이전</button>

      <button v-for="page in totalPages" :key="page" @click="goToPage(page)" class="page-btn"
        :class="{ active: page === currentPage }">
        {{ page }}
      </button>

      <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">다음
        &gt;</button>
    </div>

    <PhotoModal :show="photoModal.visible" :images="photoModal.images" :startIndex="photoModal.startIndex"
      @close="photoModal.visible = false" />

    <ReportModal :show="reportModal.visible" @close="reportModal.visible = false" @submit="handleReportSubmit" />
  </section>
</template>
<script>
import axios from '@/api/axiosSetup' // ✅ axiosSetup 고정 사용
import PhotoModal from '../modal/PhotoModal.vue'
import ReportModal from '../modal/ReportModal.vue';
import router from '@/router'
import eventBus from '@/utils/eventBus'; // 💡 [추가] 글로벌 알림용

export default {
  name: 'ReviewSection',
  components: { PhotoModal, ReportModal },
  props: {
    targetId: { type: [Number, String], required: true },
    targetType: { type: String, required: true },
    currentUserId: { type: [Number, String], required: true },
    rating: { type: Number, required: true, default: 0 },
    reviewCount: { type: Number, required: true, default: 0 },
    photoReviewCount: { type: Number, default: 0 },
    isPlace: { type: Boolean, required: false, default: false }
  },
  emits: ['show-modal', 'edit-review', 'request-delete'],

  computed: {
    sectionTitle() {
      return this.isPlace ? '장소 후기' : '방문자 후기'
    },
    // ... (다른 computed 속성들은 동일) ...
    allPhotoUrls() {
      const allPhotos = []
      for (const r of this.reviews) {
        if (Array.isArray(r.photoUrls)) {
          allPhotos.push(...r.photoUrls)
        }
      }
      return allPhotos.filter(Boolean)
    },
    morePhotoCountToShow() {
      const remaining = this.photoReviewCount - this.allPhotoThumbnails.length
      return Math.max(0, remaining)
    }
  },

  watch: {
    targetId: {
      handler(newId) {
        if (newId && this.targetType) {
          this.currentPage = 1
          this.fetchReviews()
          this.fetchPhotoThumbnails()
          this.allPhotoUrlsCache = null
        }
      },
      immediate: true
    },
    targetType: {
      handler(newType) {
        if (newType && this.targetId) {
          this.currentPage = 1
          this.fetchReviews()
          this.fetchPhotoThumbnails()
          this.allPhotoUrlsCache = null
        }
      },
      immediate: true
    },

    // 💡 [추가]
    // currentUserId가 null/"" 에서 '28'과 같은 유효한 값으로 바뀔 때 감지
    currentUserId(newUserId, oldUserId) {
      // oldUserId가 null 또는 "" (falsy) 였다가
      // newUserId가 '28' (truthy)이 된 경우 (즉, 최초 로그인 감지 시)
      if (newUserId && !oldUserId) {
        console.log('로그인 상태 감지, 좋아요 상태 새로고침');
        // '좋아요' 상태만 다시 불러옵니다.
        this.fetchLikedStatus();
      }
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
      allPhotoUrlsCache: null,
      // 💡 3. 신고 모달의 상태를 data에 추가합니다.
      reportModal: {
        visible: false,
        reviewId: null // 어떤 리뷰를 신고할지 ID를 저장
      }
    }
  },

  methods: {
    // 💡 fetchReviews에서는 '좋아요' 관련 로직 제거
    async fetchReviews() {
      if (!this.targetId || !this.targetType) return
      this.isLoading = true
      this.error = null

      try {
        const params1 = {
          targetId: this.targetId,
          targetType: this.targetType,
          page: this.currentPage,
          size: this.pageSize
        }
        const { data: reviewPage } = await axios.get(`/api/reviews`, { params: params1 })

        this.reviews = Array.isArray(reviewPage.content) ? reviewPage.content : []
        this.totalPages = reviewPage.totalPages || 1
        this.totalReviews = reviewPage.totalElements || 0

        // 💡 분리된 '좋아요' 상태 함수 호출
        await this.fetchLikedStatus();

      } catch (err) {
        console.error('[reviews] FAIL:', err?.response?.status, err?.response?.data || err.message)
        this.error = err
      } finally {
        this.isLoading = false
      }
    },
    onClickWriteReview() {
      if (!this.currentUserId) {
        // 💡 로그인 체크

        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 서비스입니다',
          // 2. '확인' 눌렀을 때 실행할 함수 전달
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
          // '취소'를 누르면 onCancel이 null이므로 그냥 창만 닫힘
        });
        return; // 페이지 이동 방지
      } else {
        // 로그인이 됐으면 기존 '리뷰 작성 모달' 요청
        this.$emit('show-modal');
      }
    },

    // 💡 [추가] '좋아요' 상태만 불러오는 함수
    async fetchLikedStatus() {
      // (seed 로직은 로그인 상태를 보정하는 것이므로 굳이 필요 없습니다. 삭제함.)

      // 로그인 시 서버 liked-status로 보정
      let likedSet = new Set()

      // ⭐️ currentUserId가 유효할 때만 API 호출
      if (this.currentUserId != null && this.currentUserId !== '') {

        // ⭐️ [매우 중요] params2 객체에서 'userId'는 반드시 제거해야 합니다.
        // (백엔드가 토큰에서 userId를 읽기 때문)
        const params2 = {
          targetId: this.targetId,
          targetType: this.targetType
        }

        try {
          // ⭐️ GET /api/reviews/liked-status?targetId=...&targetType=...
          const { data: liked } = await axios.get(`/api/reviews/liked-status`, { params: params2 })

          // ⭐️ API 응답(liked)은 객체가 아닌 순수 배열 [101, 105] 입니다.
          const ids = Array.isArray(liked) ? liked : []
          likedSet = new Set(ids.map((x) => Number(x)))

        } catch (e) {
          console.error('[liked-status] FAIL:', e?.response?.status, e?.response?.data || e.message)
        }
      }

      // likedStatus 객체 (화면 색칠용) 업데이트
      const newLikedStatus = {}
      for (const r of this.reviews) {
        const idNum = Number(r.reviewId)
        newLikedStatus[r.reviewId] = likedSet.has(idNum)
      }
      this.likedStatus = newLikedStatus
    },

    goToPage(pageNumber) {
      if (pageNumber < 1 || pageNumber > this.totalPages || pageNumber === this.currentPage) return
      this.currentPage = pageNumber
      this.fetchReviews()
    },

    async openModalFromThumb(clickedThumbnail, index) {
      let allPhotos = this.allPhotoUrlsCache
      if (!allPhotos) {
        try {
          const { data } = await axios.get(`/api/reviews/target/${this.targetType}/${this.targetId}/photos`)
          if (!Array.isArray(data) || data.length === 0) return
          allPhotos = data
          this.allPhotoUrlsCache = allPhotos
        } catch (err) {
          console.error('[modal-thumb] 전체 사진 URL 로드 실패:', err)
          eventBus.emit('show-global-alert', {
            message: '사진을 불러오는 데 실패했습니다.',
            type: 'error'
          });
          return
        }
      }
      let startIndex = allPhotos.indexOf(clickedThumbnail.url)
      if (startIndex === -1) startIndex = index
      this.photoModal.images = allPhotos
      this.photoModal.startIndex = startIndex
      this.photoModal.visible = true
    },

    openPhotoViewer(review, startIndex = 0) {
      const imgs = Array.isArray(review.photoUrls) ? review.photoUrls.filter(Boolean) : []
      if (!imgs.length) return
      this.photoModal.images = imgs
      this.photoModal.startIndex = startIndex
      this.photoModal.visible = true
    },

    onClickDelete(reviewId) {
      this.$emit('request-delete', { reviewId })
      this.openReportMenuId = null
    },

    async fetchPhotoThumbnails() {
      if (!this.targetId || !this.targetType) return
      try {
        const params = { targetId: this.targetId, targetType: this.targetType, limit: 3 }
        const { data: thumbs } = await axios.get(`/api/reviews/photos-summary`, { params })
        this.allPhotoThumbnails = Array.isArray(thumbs) ? thumbs : []
      } catch (err) {
        console.error('[photo-thumbs] 로드 실패:', err?.response?.data || err.message)
        this.allPhotoThumbnails = []
      }
    },

    formatReviewDate(dateString) {
      if (!dateString) return ''
      try {
        const date = new Date(dateString)
        return date
          .toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
          .replace(/\. /g, '.')
          .replace(/\.$/, '')
      } catch (error) {
        console.log(`에러->${error}`);
        return dateString
      }
    },

    getFilledStars(score) {
      if (typeof score !== 'number') score = 0
      const fullStars = Math.floor(score)
      const halfStar = score % 1 >= 0.5 ? 1 : 0
      const emptyStars = 5 - fullStars - halfStar
      let starsHtml = ''
      starsHtml += '<i class="bi bi-star-fill"></i>'.repeat(fullStars)
      starsHtml += '<i class="bi bi-star-half"></i>'.repeat(halfStar)
      starsHtml += '<i class="bi bi-star"></i>'.repeat(emptyStars)
      return starsHtml
    },

    toggleReportMenu(reviewId) {
      this.openReportMenuId = this.openReportMenuId === reviewId ? null : reviewId
    },

    // 사용자 리뷰 신고 함수
    async reportReview(reviewId) {
      // 💡 [추가] 로그인 체크
      if (!this.currentUserId) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          // '확인' 눌렀을 때 실행할 함수 전달
          onConfirm: () => {
            // ⭐️ router.push('/login');
            //    만약 router 객체를 setup에서 가져오지 않았다면,
            //    this.$router.push({ name: 'login' }); 를 사용해야 합니다.
            //    (Vue 3 <script setup> 에서는 useRouter()를,
            //     Options API에서는 this.$router를 사용합니다.)

            //    우선 this.$router로 가정하겠습니다.
            this.$router.push({ name: 'login' });
          }
        });
        return; // 함수 즉시 종료
      }

      // 본인 리뷰 체크 로직
      const review = this.reviews.find((r) => r.reviewId === reviewId)
      if (review && String(review.authorId) === String(this.currentUserId)) {
        eventBus.emit('show-global-alert', {
          message: '사용자 본인 리뷰는 신고할 수 없습니다.',
          type: 'error'
        });
        this.openReportMenuId = null;
        return;
      }

      // [수정]
      // window.prompt(...) 대신 모달 상태를 변경합니다.
      this.reportModal.reviewId = reviewId; // 신고할 ID 저장
      this.reportModal.visible = true;    // 모달 띄우기
      this.openReportMenuId = null;         // ...메뉴 닫기

    },

    async handleReportSubmit(reason) {
      const reviewId = this.reportModal.reviewId;

      // 사유를 입력했는지 체크
      if (!reason || !reason.trim()) {
        eventBus.emit('show-global-alert', {
          message: '신고 사유를 입력해야 합니다.',
          type: 'error'
        });
        return;
      }

      try {
        await axios.post(`/api/reviews/${reviewId}/report`)

        eventBus.emit('show-global-alert', {
          message: '신고가 접수되었습니다.',
          type: 'success' // 👈 타입을 'success'로 지정
        });
      } catch (e) {
        console.error('신고 실패:', e);

        // 💡 4. [수정] 에러 메시지 분기 처리
        // 백엔드 응답(e.response.data)에 "이미 신고함"이 포함되어 있는지 확인
        if (e.response && e.response.data && e.response.data.includes("이미 신고함")) {

          // [분기 1] 중복 신고일 경우
          eventBus.emit('show-global-alert', {
            message: '이미 접수된 신고입니다.', // 👈 요청하신 메시지
            type: 'error'
          });

        } else {

          // [분기 2] 그 외 모든 실패일 경우 (서버 다운, 500 에러 등)
          eventBus.emit('show-global-alert', {
            message: '신고가 실패되었습니다.', // 👈 기존 메시지
            type: 'error'
          });
        }
      } finally {
        // 모달 닫기
        this.reportModal.visible = false;
        this.reportModal.reviewId = null;
      }
    },
    getLikeIcon(reviewId) {
      return this.likedStatus[reviewId] ? 'bi bi-hand-thumbs-up-fill' : 'bi bi-hand-thumbs-up'
    },

    // ⭐️ toggleLike 로직은 완벽하므로 수정할 필요 없습니다.
    async toggleLike(reviewId) {

      // 💡 [추가] 로그인 체크
      if (!this.currentUserId) {
        eventBus.emit('show-global-confirm', {
          message: '로그인이 필요한 기능입니다.',
          onConfirm: () => {
            this.$router.push({ name: 'login' });
          }
        });
        return;
      }

      const isLiked = this.likedStatus[reviewId]
      try {
        if (isLiked) {
          await axios.delete(`/api/reviews/${reviewId}/unlike`)
        } else {
          await axios.post(`/api/reviews/${reviewId}/like`)
        }
        this.likedStatus[reviewId] = !isLiked
        const review = this.reviews.find((r) => r.reviewId === reviewId)
        if (review) review.likeCount += isLiked ? -1 : 1
      } catch (error) {
        console.error('좋아요 처리 실패:', error)
        eventBus.emit('show-global-alert', {
          message: `좋아요 처리에 실패했습니다 : ${error.response?.data || error.message}`,
          type: 'error'
        });
      }
    },

    goToAllPhotosPage() {
      const path = `/photos/${this.targetType}/${this.targetId}`
      router.push(path)
    }
  }
}
</script>
<style scoped>
/* (스타일은 네 원본 그대로) */
.review-section {
  padding: 15px;
  background-color: white;
}

/* ... 아래 스타일 전체 동일 ... (생략 안 하고 그대로 유지해도 됨) */
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
  color: #ffc107;
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
  background-color: #4a89f3;
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
  color: #ffc107;
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

.review-meta .likes.active {
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
  color: #eb3223;
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
  color: #eb3223;
  cursor: pointer;
  white-space: nowrap;
}

.report-btn:hover {
  background-color: #f0f0f0;
}

.loading-message,
.error-message {
  padding: 40px 20px;
  text-align: center;
  color: #888;
  font-size: 14px;
}

.error-message {
  color: #eb3223;
}

.review-photos-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin: 8px 0 10px 50px;
}

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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0 10px 0;
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
  background-color: #4a7cec;
  color: white;
  border-color: #4a7cec;
  font-weight: bold;
}

.page-btn:not(:disabled):not(.active):hover {
  background-color: #f0f0f0;
}
</style>
