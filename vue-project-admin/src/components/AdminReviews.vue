<template>
  <div>
    <ContentHeader iconClass="bi bi-chat-square-text" title="1. 신고 후기 관리" description="신고 접수된 후기를 처리하는 핵심 기능입니다." />
    <div class="card shadow-sm border-0 mb-4">
      <div class="card-body">
        <form class="row g-3 align-items-center" @submit.prevent="handleSearch">
          <div class="col-md-4">
            <select id="reviewRating" class="form-select" v-model="categoryFilter" @change="handleSearch">
              <option value="">전체</option>
              <option value="science-center">과학관 전시</option>
              <option value="science-trip">과학 여행</option>
            </select>
          </div>

        </form>
      </div>
    </div>

    <div class="card shadow-sm border-0">
      <div class="card-header fs-5 fw-bold bg-white">
        신고 접수 후기 목록 ({{ totalElements }}개)
      </div>
      <div class="card-body p-0">
        <div v-if="isLoading" class="p-4 text-center text-primary">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-2">신고 목록을 불러오는 중...</p>
        </div>
        <div v-else-if="error" class="p-4 text-center text-danger">
          {{ error }}
        </div>
        <div v-else-if="reviews.length === 0" class="p-4 text-center">
          처리할 신고 후기가 없습니다.
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th scope="col">ID</th>
                <th scope="col">신고 횟수</th>
                <th scope="col" style="min-width: 300px">후기 내용</th>
                <th scope="col">작성자 (ID)</th>
                <th scope="col">별점</th>
                <th scope="col">대상 (장소/전시)</th>
                <th scope="col">작성일</th>
                <th scope="col" style="min-width: 150px">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="review in reviews" :key="review.reviewId">
                <td>{{ review.reviewId }}</td>
                <td style="white-space: nowrap;">
                  <span class="badge bg-danger">{{ review.reportCount }}</span>
                </td>

                <td>{{ review.content }}</td>
                <td>{{ review.authorName }} ({{ review.authorId }})</td>
                <td>{{ getRatingStars(review.rating) }}</td>
                <td>{{ review.targetName }}</td>
                <td>{{ formatDate(review.createdAt) }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-2" @click="handleRejectReport(review.reviewId)">
                    신고 반려
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDeleteReview(review.reviewId)">
                    후기 삭제
                  </button>
                </td>

              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer bg-white border-top-0">
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="changePage(currentPage - 1)" :disabled="currentPage === 1">
                이전
              </button>
            </li>

            <li v-for="page in displayPages" :key="page" class="page-item" :class="{ active: currentPage === page }">
              <button class="page-link" @click="changePage(page)">
                {{ page }}
              </button>
            </li>

            <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
              <button class="page-link" @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">
                다음
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
import ContentHeader from "./ContentHeader.vue";
import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const apiClient = axios.create({
  baseURL: API_BASE_URL
});

export default {
  name: "AdminReviews",
  components: {
    ContentHeader,
  },
  props: {
    reviews: {
      type: Array,
      default: () => [],
    },
    totalElements: {
      type: Number,
      default: 0,
    },
    currentPage: {
      type: Number,
      default: 1,
    },
    totalPages: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      isLoading: false,
      error: null,
      categoryFilter: "",
    };
  },
  computed: {
    displayPages() {
      const pages = [];
      const maxPages = Math.max(1, this.totalPages);
      const startPage = Math.max(1, this.currentPage - 2);
      const endPage = Math.min(maxPages, this.currentPage + 2);

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      return pages.length > 0 ? pages : [1];
    },
  },
  methods: {
    handleSearch() {
      console.log("검색 기능 - 카테고리:", this.categoryFilter);
      this.$emit("search", this.categoryFilter);
    },

    changePage(page) {
      const maxPages = Math.max(1, this.totalPages);
      if (page < 1 || page > maxPages) return;
      this.$emit("page-change", page);
    },

    async handleDeleteReview(reviewId) {
      if (!confirm("정말로 이 리뷰를 삭제(Soft Delete) 처리하시겠습니까?")) {
        return;
      }
      this.$emit("delete-review", reviewId);
    },

    async handleRejectReport(reviewId) {
      if (!confirm("이 리뷰에 대한 신고를 반려하고, 리뷰를 유지하시겠습니까?")) {
        return;
      }
      try {
        await apiClient.put(`/api/admin/reviews/${reviewId}/reject`);
        alert(`리뷰 ${reviewId}번에 대한 신고가 성공적으로 반려되었습니다.`);
        this.$emit("page-change", this.currentPage);
      } catch (e) {
        alert(
          "신고 반려 처리에 실패했습니다: " + (e.response?.data || e.message)
        );
        console.error("신고 반려 처리 실패:", e);
      }
    },

    getRatingStars(rating) {
      if (rating === null || rating === undefined) return "";
      const roundedRating = Math.round(rating);
      return "⭐️".repeat(roundedRating);
    },

    formatDate(dateString) {
      if (!dateString) return "";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("ko-KR", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
        });
      } catch (e) {
        return dateString;
      }
    },
  },
};
</script>

<style scoped>
.card {
  border-radius: 8px;
}

.card-header {
  background-color: white !important;
  border-bottom: 1px solid #eef1f4;
  padding: 15px 20px;
}

.pagination {
  margin-top: 20px;
}

.page-link {
  color: #4a7cec;
  border-color: #dee2e6;
}

.page-link:hover {
  background-color: #f8f9fa;
  color: #3a65d0;
}

.page-item.active .page-link {
  background-color: #4a7cec;
  border-color: #4a7cec;
  color: white;
}

.page-item.disabled .page-link {
  color: #6c757d;
  pointer-events: none;
  background-color: #fff;
  border-color: #dee2e6;
}
</style>
