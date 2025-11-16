<template>
  <div id="notice-page">
    <!-- 상단 헤더 -->
    <div class="header-section">
      <button class="btn-back" @click="goBack">
        <i class="bi bi-arrow-left"></i>
      </button>

      <h1 class="header-title">공지사항</h1>

      <!-- 오른쪽 균형 맞추는 더미 영역 -->
      <div class="header-spacer"></div>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="content-wrapper">
      <!-- 로딩 중 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
      </div>

      <!-- 공지사항 목록 -->
      <div v-else class="notice-list">
        <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="goToNoticeDetail(notice.id)">
          <div class="notice-item-content">
            <h2 class="notice-item-title">
              {{ notice.title }}
            </h2>

            <div class="notice-item-meta">
              <span class="meta-date">{{ formatDate(notice.createdAt) }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-views">조회 {{ notice.views }}</span>
            </div>
          </div>

          <i class="bi bi-chevron-right notice-chevron"></i>
        </div>

        <!-- 공지사항이 없을 때 -->
        <div v-if="notices.length === 0" class="empty-state">
          <div class="empty-icon-circle">
            <i class="bi bi-bell-slash"></i>
          </div>
          <p>등록된 공지사항이 없습니다.</p>
        </div>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger error-alert" role="alert">
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
      apiBaseUrl: import.meta.env.VITE_API_BASE_URL + '/api/admin',
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
          this.notices.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else {
          this.error = '공지사항을 불러올 수 없습니다.';
          console.error('공지사항 조회 실패:', response.status);
        }
      } catch (error) {
        this.error = '네트워크 오류가 발생했습니다.';
        console.error('공지사항 조회 오류:', error);
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
    },
  },
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
  width: 100%;
  margin: 0 auto;
  background: #f3f4f6;
  /* 홈뷰처럼 연한 회색 배경 */
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 헤더 (홈뷰 / 마이페이지와 톤 맞춤) */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
  position: sticky;
  top: 0;
  z-index: 100;
  height: 63px;
  font-size: 16px;
  font-weight: 600px;
}

.btn-back {
  background: none;
  border: none;
  padding: 0;
  font-size: 22px;
  color: #111827;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-back:active {
  opacity: 0.7;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  text-align: center;
}

.header-spacer {
  width: 22px;
  /* back 버튼과 균형 맞추기용 */
}

/* 콘텐츠 영역 */
.content-wrapper {
  flex: 1;
  padding: 12px 12px 20px;
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

/* 공지사항 리스트 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 개별 공지 카드 (홈뷰 카드 느낌) */
.notice-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 8px rgba(15, 23, 42, 0.03);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease, background-color 0.12s ease;
  gap: 10px;
}

.notice-item:hover {
  background-color: #f9fafb;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.notice-item:active {
  background-color: #eef2ff;
  transform: translateY(0);
}

.notice-item-content {
  flex: 1;
  min-width: 0;
}

.notice-item-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
  line-height: 1.4;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-item-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #6b7280;
}

.meta-dot {
  font-size: 14px;
  line-height: 1;
}

.notice-chevron {
  font-size: 16px;
  color: #9ca3af;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px 40px;
  color: #9ca3af;
}

.empty-icon-circle {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  color: #4f46e5;
}

.empty-icon-circle i {
  font-size: 26px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 에러 메시지 */
.error-alert {
  margin-top: 16px;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 13px;
}
</style>
