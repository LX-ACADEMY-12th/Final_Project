<template>
  <div id="notice-detail-page">
    <!-- 상단 헤더 -->
    <div class="header-section">
      <button class="btn-back" @click="goBack">
        <i class="bi bi-arrow-left"></i>
      </button>
      <h1 class="header-title">공지사항</h1>
      <div class="header-spacer"></div>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="content-wrapper">
      <!-- 로딩 중 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
      </div>

      <!-- 공지사항 상세 내용 -->
      <div v-else-if="notice" class="notice-detail-container">
        <!-- 제목 영역 -->
        <div class="notice-header-section">
          <h1 class="notice-detail-title">{{ notice.title }}</h1>
          <div class="notice-meta-info">
            <div class="meta-item">
              <i class="bi bi-calendar3"></i>
              <span>{{ formatDate(notice.createdAt) }}</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <i class="bi bi-eye"></i>
              <span>조회 {{ notice.views }}</span>
            </div>
          </div>
        </div>

        <!-- 본문 내용 -->
        <div class="notice-content-section">
          <div class="notice-content">{{ notice.content }}</div>
        </div>

      </div>

      <!-- 로딩 실패 또는 공지사항이 없을 때 -->
      <div v-else class="empty-state">
        <i class="bi bi-exclamation-circle"></i>
        <p>공지사항을 찾을 수 없습니다.</p>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger mt-4" role="alert">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoticeDetailView',
  data() {
    return {
      notice: null,
      loading: true,
      error: null,
      apiBaseUrl: import.meta.env.VITE_API_BASE_URL + "/api/admin",
    };
  },
  created() {
    this.loadNotice();
  },
  methods: {
    async loadNotice() {
      const noticeId = parseInt(this.$route.params.id);
      this.loading = true;
      this.error = null;

      try {
        const response = await fetch(`${this.apiBaseUrl}/notices/${noticeId}`);
        if (response.ok) {
          this.notice = await response.json();
        } else if (response.status === 404) {
          this.error = "공지사항을 찾을 수 없습니다.";
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

    formatDate(dateString) {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}.${month}.${day}`;
    }
  },
  watch: {
    '$route.params.id'() {
      this.loadNotice();
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

#notice-detail-page {
  font-family: 'SUIT Variable', sans-serif;
  max-width: 480px;
  background: #ffffff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0;
}

/* 헤더 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  position: sticky;
  top: 0;
  background: #ffffff;
  z-index: 100;
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
  font-size: 18px;
  font-weight: 700;
  color: #212529;
  margin: 0;
}

.header-spacer {
  width: 24px;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
}

/* 공지사항 상세 컨테이너 */
.notice-detail-container {
  padding: 20px 16px;
}

/* 제목 영역 */
.notice-header-section {
  margin-bottom: 24px;
}

.notice-detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #212529;
  line-height: 1.4;
  margin: 0 0 16px 0;
  word-break: break-word;
}

/* 메타 정보 */
.notice-meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13x;
  color: #868e96;
}

.meta-item i {
  color: #4A7CEC;
  font-size: 14px;
}

.meta-divider {
  width: 1px;
  height: 14px;
  background: #e9ecef;
}

/* 본문 내용 */
.notice-content-section {
  border-top: 1px solid #e9ecef;
  padding-top: 20px;
}

.notice-content {
  font-size: 15px;
  line-height: 1.8;
  color: #495057;
  white-space: pre-wrap;
  word-wrap: break-word;
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
