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
            <span>{{ notice.views }}</span>
          </div>
        </div>
      </div>

      <!-- 본문 내용 -->
      <div class="notice-content-section">
        <div class="notice-content">{{ notice.content }}</div>
      </div>

      <!-- 목록으로 버튼 -->
      <button class="btn-back-to-list" @click="goToNoticeList">
        <i class="bi bi-arrow-left me-2"></i>
        목록으로
      </button>
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
</template>

<script>
export default {
  name: 'NoticeDetailView',
  data() {
    return {
      notice: null,
      loading: true,
      error: null,
      apiBaseUrl: "http://localhost:8080/api",
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

    goToNoticeList() {
      this.$router.push({ name: 'Notice' });
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
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
}

/* 헤더 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  margin-bottom: 24px;
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

/* 공지사항 상세 컨테이너 */
.notice-detail-container {
  flex: 1;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

/* 제목 영역 */
.notice-header-section {
  margin-bottom: 24px;
}

.notice-detail-title {
  font-size: 24px;
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
  font-size: 14px;
  color: #868e96;
}

.meta-item i {
  color: #4A7CEC;
  font-size: 16px;
}

.meta-divider {
  width: 1px;
  height: 16px;
  background: #e9ecef;
}

/* 본문 내용 */
.notice-content-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e9ecef;
}

.notice-content {
  font-size: 16px;
  line-height: 1.8;
  color: #495057;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 목록으로 버튼 */
.btn-back-to-list {
  background: linear-gradient(135deg, #4A7CEC 0%, #3a65d0 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 14px 20px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(74, 124, 236, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-back-to-list:active {
  transform: translateY(2px);
  box-shadow: 0 2px 6px rgba(74, 124, 236, 0.2);
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
