<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo-container">
        <div class="logo-icon">
          <i class="bi bi-book-fill"></i>
        </div>
        <div>
          <h4 class="mb-0 fw-bold">과학 어디가?</h4>
          <small class="text-white">관리자 대시보드</small>
        </div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <h6 class="sidebar-heading">메뉴</h6>


      <a href="#" class="nav-item" :class="{ active: currentView === 'places' }" @click.prevent="changeView('places')">
        <i class="bi bi-database-fill-gear"></i>
        <span>컨텐츠 관리</span>
        <div class="nav-item-indicator"></div>
      </a>

      <a href="#" class="nav-item" :class="{ active: currentView === 'reviews' }"
        @click.prevent="changeView('reviews')">
        <i class="bi bi-chat-square-text-fill"></i>
        <span>신고된 후기 관리</span>
        <div class="nav-item-indicator"></div>
      </a>

      <a href="#" class="nav-item" :class="{ active: currentView === 'notices' }"
        @click.prevent="changeView('notices')">
        <i class="bi bi-megaphone-fill"></i>
        <span>공지사항 관리</span>
        <div class="nav-item-indicator"></div>
      </a>

      <a href="#" class="nav-item" :class="{ active: currentView === 'spatial-analysis' }"
        @click.prevent="changeView('spatial-analysis')">
        <i class="bi bi-bar-chart-fill"></i>
        <span>방문 트렌드 분석</span>
        <div class="nav-item-indicator"></div>
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="footer-content">
        <i class="bi bi-shield-check"></i>
        <small>© 2025 과학 어디가 Service</small>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Sidebar",
  props: {
    currentView: {
      type: String,
      default: "reviews",
    },
  },
  methods: {
    changeView(view) {
      this.$emit("change-view", view);
    },
  },
};
</script>

<style scoped>
.sidebar {
  width: 280px;
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  border-right: 1px solid #e2e8f0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  padding: 24px 20px;
  background: linear-gradient(135deg, #4A7CEC 0%, #5c8ef5 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #ffffff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.sidebar-header h4 {
  color: #ffffff;
  font-size: 1.25rem;
  margin-bottom: 2px;
}

.sidebar-header small {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.8125rem;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

.sidebar-heading {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #94a3b8;
  padding: 0 12px 8px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 14px 16px;
  margin: 4px 0;
  border-radius: 10px;
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  font-size: 0.9375rem;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.nav-item i {
  font-size: 1.125rem;
  margin-right: 14px;
  width: 22px;
  text-align: center;
  color: #94a3b8;
  transition: all 0.25s ease;
  z-index: 1;
}

.nav-item span {
  z-index: 1;
}

.nav-item-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 0;
  background: #4A7CEC;
  border-radius: 0 4px 4px 0;
  transition: height 0.3s ease;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(74, 124, 236, 0.05) 100%);
  transform: translateX(-100%);
  transition: transform 0.3s ease;
}

.nav-item:hover {
  color: #4A7CEC;
  background-color: rgba(74, 124, 236, 0.05);
  transform: translateX(4px);
}

.nav-item:hover i {
  color: #4A7CEC;
  transform: scale(1.1);
}

.nav-item:hover::before {
  transform: translateX(0);
}

/* 선택된 메뉴 스타일 */
.nav-item.active {
  background: linear-gradient(90deg, rgba(74, 124, 236, 0.15) 0%, rgba(74, 124, 236, 0.08) 100%);
  color: #4A7CEC;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(74, 124, 236, 0.15);
}

.nav-item.active i {
  color: #4A7CEC;
  transform: scale(1.15);
}

.nav-item.active .nav-item-indicator {
  height: 60%;
}

.sidebar-footer {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-top: 1px solid #e2e8f0;
}

.footer-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.8125rem;
}

.footer-content i {
  color: #4A7CEC;
  font-size: 1rem;
}

/* 스크롤바 스타일링 */
.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
