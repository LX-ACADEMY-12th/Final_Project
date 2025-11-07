<template>
  <div id="notice-page" class="container px-3 py-4">
    <!-- 상단 헤더 -->
    <div class="header-section mb-4">
      <button class="btn-back" @click="goBack">
        <i class="bi bi-arrow-left"></i>
      </button>
      <h1 class="header-title">공지사항</h1>
      <div class="header-spacer"></div>
    </div>

    <!-- 로딩 중 표시 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
    </div>

    <!-- 공지사항 목록 -->
    <div v-else class="notice-list">
      <!-- 공지사항 아이템 -->
      <div
        v-for="notice in notices"
        :key="notice.id"
        class="notice-card"
        @click="goToNoticeDetail(notice.id)"
      >
        <div class="notice-card-header">
          <h2 class="notice-card-title">{{ notice.title }}</h2>
        </div>
        <div class="notice-card-meta">
          <span class="meta-date">
            <i class="bi bi-calendar3 me-1"></i>
            {{ formatDate(notice.createdAt) }}
          </span>
          <span class="meta-views">
            <i class="bi bi-eye me-1"></i>
            {{ notice.views }}
          </span>
        </div>
      </div>

      <!-- 공지사항이 없을 때 -->
      <div v-if="notices.length === 0" class="empty-state">
        <i class="bi bi-bell-slash"></i>
        <p>등록된 공지사항이 없습니다.</p>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="error" class="alert alert-danger mt-4" role="alert">
      {{ error }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoticeView',
  data() {
    return {
      notices: [],
      loading: true,
      error: null,
      apiBaseUrl: "http://localhost:8080/api",
    };
  },
  mounted() {
    this.fetchNotices();
  },
  methods: {
    async fetchNotices() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${this.apiBaseUrl}/notices`);
        if (response.ok) {
          this.notices = await response.json();
          // 최신 순으로 정렬
          this.notices.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else {
          this.error = "공지사항을 불러올 수 없습니다.";
          console.error("공지사항 조회 실패:", response.status);
        }
      } catch (error) {
        this.error = "네트워크 오류가 발생했습니다.";
        console.error("공지사항 조회 오류:", error);
      } finally {
        this.loading = false;
      }
    },

    goBack() {
      this.$router.back();
    },

    goToNoticeDetail(noticeId) {
      this.$router.push({ name: 'NoticeDetail', params: { id: noticeId } });
    },

    formatDate(dateString) {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}.${month}.${day}`;
    }
  }
};
</script>

<style scoped>
@font-face {
  font-family: 'SUIT Variable';
  src: url('@/assets/fonts/SUIT-Variable.ttf') format('truetype');
  font-weight: 100 900;
  font-style: normal;
}

#notice-page {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  min-height: 100vh;
}

/* 헤더 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.btn-back {
  background: none;
  border: none;
  padding: 0;
  font-size: 24px;
  color: #212529;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-back:active {
  opacity: 0.7;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #212529;
  margin: 0;
}

.header-spacer {
  width: 24px;
}

/* 공지사항 리스트 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 공지사항 카드 */
.notice-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e9ecef;
}

.notice-card:active {
  transform: translateY(2px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.notice-card:hover {
  border-color: #4A7CEC;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.1);
}

.notice-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.notice-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #212529;
  margin: 0;
  line-height: 1.4;
  flex: 1;
  word-break: break-word;
}

/* 메타 정보 */
.notice-card-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #868e96;
}

.meta-date,
.meta-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-date i,
.meta-views i {
  font-size: 14px;
  color: #4A7CEC;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #adb5bd;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 12px;
  display: block;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}
</style>
