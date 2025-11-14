<template>
  <div class="d-flex admin-layout">
    <Sidebar :currentView="currentView" @change-view="currentView = $event" />

    <ContentView :currentView="currentView" :stats="stats" :reviews="reviews" :notices="notices" :places="places"
      :reviewsTotalElements="reviewsTotalElements" :reviewsCurrentPage="reviewsCurrentPage"
      :reviewsTotalPages="reviewsTotalPages" :totalElements="totalElements" :currentPage="currentPage"
      :totalPages="totalPages" @review-page-change="handleReviewPageChange" @search-reviews="handleReviewSearch"
      @page-change="handlePageChange" @delete-review="deleteReview" @edit-place="editPlace" @delete-place="deletePlace"
      @edit-notice="editNotice" @delete-notice="deleteNotice" @add-notice="addNotice" @add-place="addPlace" />
  </div>
</template>

<script>
import Sidebar from "@/components/Sidebar.vue";
import ContentView from "@/components/ContentView.vue";
import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:8080/api",
});

export default {
  components: {
    Sidebar,
    ContentView,
  },
  name: "AdminLayout",
  data() {
    return {
      currentView: "reviews",
      stats: {
        todayStamps: 120,
        totalStamps: 15880,
        topPlace: "국립과천과학관",
        activeUsers: 450,
      },
      reviews: [],
      reviewsCurrentPage: 1,
      reviewsPageSize: 10,
      reviewsTotalElements: 0,
      reviewsTotalPages: 0,
      reviewsCategoryFilter: '',
      notices: [],
      places: [],
      currentPage: 1,
      pageSize: 10,
      totalElements: 0,
      totalPages: 0,
    };
  },
  mounted() {
    const savedView = localStorage.getItem("currentAdminView");
    if (savedView) {
      this.currentView = savedView;
    }

    this.fetchReviews(1, this.reviewsCategoryFilter);
    this.fetchNotices();
  },
  watch: {
    currentView(newValue) {
      localStorage.setItem("currentAdminView", newValue);
    },
  },
  methods: {
    async fetchReviews(page, category = this.reviewsCategoryFilter) {
      if (page < 1) return;

      try {
        const params = {
          page: page - 1,
          size: this.reviewsPageSize,
        };

        if (category) {
          params.category = category;
        }

        const response = await apiClient.get("/admin/reviews/reported", {
          params: params,
        });

        this.reviews = response.data.content;
        this.reviewsTotalElements = response.data.totalElements;
        this.reviewsCurrentPage = response.data.pageNumber + 1;
        this.reviewsTotalPages = response.data.totalPages;

        this.reviewsCategoryFilter = category;

        console.log("✅ 후기 목록 로드 성공:", this.reviews);
      } catch (error) {
        console.error("후기 목록 로드 실패:", error);
        alert("후기 데이터 로드에 실패했습니다.");
      }
    },

    handleReviewSearch(category) {
      console.log("검색 실행 - 카테고리:", category);
      this.fetchReviews(1, category);
    },

    handleReviewPageChange(newPage) {
      this.fetchReviews(newPage, this.reviewsCategoryFilter);
    },

    async deleteReview(id) {
      try {
        await apiClient.put(`/admin/reviews/${id}/delete`);
        alert("후기가 삭제되었습니다.");
        console.log("✅ 후기 삭제 성공");
        await this.fetchReviews(this.reviewsCurrentPage);
      } catch (error) {
        console.error("❌ 후기 삭제 오류:", error);
        alert("후기 삭제 중 오류가 발생했습니다.");
      }
    },

    async fetchNotices() {
      try {
        const response = await apiClient.get("/admin/notices");
        this.notices = response.data;
        console.log("✅ 공지사항 조회 성공:", this.notices);
      } catch (error) {
        console.error("공지사항 조회 오류:", error);
      }
    },

    async addNotice(noticeData) {
      try {
        console.log("=== 공지사항 등록 시작 ===");
        console.log("요청 데이터:", {
          title: noticeData.title,
          content: noticeData.content,
          author: "관리자",
        });

        const response = await apiClient.post("/admin/notices", {
          title: noticeData.title,
          content: noticeData.content,
          author: "관리자",
        });

        console.log("✅ 공지사항 등록 성공:", response.data);

        const newNotice = response.data;
        if (!newNotice.views) {
          newNotice.views = 0;
        }

        this.notices.unshift(newNotice);
        alert("새 공지사항이 등록되었습니다.");
      } catch (error) {
        console.error("❌ 공지사항 등록 오류:", error);
        alert("공지사항 등록 중 오류: " + error.message);
      }
    },

    async editNotice(noticeData) {
      try {
        console.log("공지사항 수정 시작:", noticeData);

        // 기존: const response = await apiClient.put(`/notices/${noticeData.id}`, {
        // 수정: 아래처럼 경로 변경
        const response = await apiClient.put(`/admin/notices/${noticeData.id}`, {
          title: noticeData.title,
          content: noticeData.content,
          author: "관리자",
        });

        console.log("공지사항 수정 성공:", response.data);
        const index = this.notices.findIndex((n) => n.id === noticeData.id);
        if (index !== -1) {
          this.notices[index] = response.data;
        }
        alert("공지사항이 수정되었습니다.");
      } catch (error) {
        console.error("공지사항 수정 오류:", error);
        alert("공지사항 수정 중 오류가 발생했습니다: " + error.message);
      }
    },

    deleteNotice(id) {
      if (confirm(`[공지 ID: ${id}] 정말 삭제하시겠습니까?`)) {
        this.deleteNoticeFromAPI(id);
      }
    },

    async deleteNoticeFromAPI(id) {
      try {
        await apiClient.delete(`/admin/notices/${id}`);
        this.notices = this.notices.filter((n) => n.id !== id);
        alert("공지사항이 삭제되었습니다.");
        console.log("공지사항 삭제 성공");
      } catch (error) {
        console.error("공지사항 삭제 오류:", error);
        alert("공지사항 삭제 중 오류가 발생했습니다.");
      }
    },

    handlePageChange(newPage) {
      this.fetchPlaces(newPage);
    },

    async addPlace(placeData, file) {
      const formData = new FormData();
      formData.append(
        "placeData",
        new Blob([JSON.stringify(placeData)], { type: "application/json" })
      );
      if (file) {
        formData.append("file", file);
      }

      try {
        await apiClient.post("/admin/contents", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });
        alert("새 컨텐츠가 등록되었습니다.");
        await this.fetchPlaces(1);
      } catch (error) {
        console.error("컨텐츠 등록 실패:", error);
        alert("등록에 실패했습니다.");
      }
    },

    async editPlace(id, placeData, file) {
      const formData = new FormData();
      formData.append(
        "placeData",
        new Blob([JSON.stringify(placeData)], { type: "application/json" })
      );
      if (file) {
        formData.append("file", file);
      }

      try {
        await apiClient.put(`/admin/contents/${id}`, formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        });
        alert("컨텐츠가 수정되었습니다.");
        await this.fetchPlaces(this.currentPage);
      } catch (error) {
        console.error("컨텐츠 수정 실패:", error);
        alert("수정에 실패했습니다.");
      }
    },

    async deletePlace(id) {
      if (confirm(`[ID: ${id}] 정말 삭제하시겠습니까?`)) {
        try {
          await apiClient.delete(`/admin/contents/${id}`);
          alert("삭제되었습니다.");
          await this.fetchPlaces(this.currentPage);
        } catch (error) {
          console.error("삭제 실패:", error);
          alert("삭제에 실패했습니다.");
        }
      }
    },
  },
};
</script>

<style>
@import url("https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/static/woff2/SUIT.css");

body {
  font-family: "SUIT", sans-serif;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --bs-primary: #34495e;
  --bs-primary-rgb: 52, 73, 94;
  --bs-secondary: #6c757d;
  --bs-success: #28a745;
  --bs-info: #17a2b8;
  --bs-warning: #ffc107;
  --bs-danger: #dc3545;
  --bs-light: #f8f9fa;
  --bs-dark: #343a40;
}

.btn-primary {
  --bs-btn-bg: #4a7cec;
  --bs-btn-border-color: #4a7cec;
  --bs-btn-hover-bg: #3a65d0;
  --bs-btn-hover-border-color: #3a65d0;
  --bs-btn-active-bg: #3155b1;
  --bs-btn-active-border-color: #3155b1;
  --bs-btn-focus-shadow-rgb: 74, 124, 236;
}

.form-label {
  font-weight: 500;
  color: #34495e;
}

.form-control,
.form-select {
  border-radius: 6px;
  border-color: #e2e8f0;
}

.form-control:focus,
.form-select:focus {
  border-color: #4a7cec;
  box-shadow: 0 0 0 0.25rem rgba(74, 124, 236, 0.25);
}
</style>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #eef1f4;
  overflow: hidden;
}
</style>
