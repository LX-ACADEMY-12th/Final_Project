2<template>
  <div id="notice-page">
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

      <!-- 공지사항 목록 -->
      <div v-else class="notice-list">
        <!-- 공지사항 아이템 -->
        <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="goToNoticeDetail(notice.id)">
          <div class="notice-item-content">
            <h2 class="notice-item-title">{{ notice.title }}</h2>
            <div class="notice-item-meta">
              <span class="meta-date">{{ formatDate(notice.createdAt) }}</span>
              <span class="meta-views">조회 {{ notice.views }}</span>
            </div>
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
      apiBaseUrl: import.meta.env.VITE_API_BASE_URL + "/api",
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
  background: #ffffff;
  min-height: 100vh;
  padding: 0;
  position: relative;
}

/* 헤더 - 고정 */
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

/* 콘텐츠 영역 */
.content-wrapper {
  position: relative;
}

/* 공지사항 리스트 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 공지사항 아이템 */
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #e9ecef;
  gap: 12px;
}

.notice-item:active {
  background-color: #f8f9fa;
}

.notice-item:hover {
  background-color: #f8f9fa;
}

.notice-item-content {
  flex: 1;
  min-width: 0;
}

.notice-item-title {
  font-size: 15px;
  font-weight: 600;
  color: #212529;
  margin: 0 0 8px 0;
  line-height: 1.4;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #868e96;
}

.meta-date,
.meta-views {
  display: flex;
  align-items: center;
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
