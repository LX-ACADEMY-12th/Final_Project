<!-- MyPagePuzzleSkeleton.vue (수정본) -->
<template>
  <div class="view-container">
    <header class="profile-header">
      <div class="profile-avatar">
      </div>
      <div class="profile-details">
        <span class="username">차민정</span>

        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-value">572</span>
            <span class="stat-label">posting</span>
          </div>
          <div class="stat-item" @click="goToFollowers()">
            <span class="stat-value">372</span>
            <span class="stat-label">followers</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">222</span>
            <span class="stat-label">following</span>
          </div>
        </div>

        <div class="profile-actions">
          <button class="btn btn-dark btn-sm edit-button" @click="goToProfileModify()">프로필 편집</button>
          <button class="btn btn-white options-button" @click="goToSetting()">
            <i class="bi bi-three-dots"></i>
          </button>
        </div>
      </div>
    </header>

    <nav class="tab-navigation">
      <button class="btn col-4" :class="{ 'active': activeTab === 'myFeed' }" @click="changeTab('myFeed')">
        <i class="bi bi-stack"></i>
      </button>
      <button class="btn col-4" :class="{ 'active': activeTab === 'myPuzzle' }" @click="changeTab('myPuzzle')">
        <i class="bi bi-puzzle-fill"></i>
      </button>
      <button class="btn col-4" :class="{ 'active': activeTab === 'myRandom' }" @click="changeTab('myRandom')">
        <i class="bi bi-compass-fill"></i>
      </button>
    </nav>

    <main class="content-area">
      <div v-if="activeTab === 'myFeed'" class="tab-content-panel">
        <MyPageFeedView />
      </div>
      <div v-else-if="activeTab === 'myPuzzle'" class="tab-content-panel">
        <MyPageRegionView />
      </div>
      <div v-else-if="activeTab === 'myRandom'" class="tab-content-panel">
        <MyPageRandomView />
      </div>
      <div v-else class="w-100 h-100 d-flex justify-content-center align-items-center">
        <span>탭 준비중…</span>
      </div>
    </main>
  </div>
</template>

<script>
import { nextTick } from 'vue'
import MyPageFeedView from './MyFeedView.vue'
import MyPageRegionView from './MyPageRegionView.vue'
import MyPageRandomView from './MyPageRandomView.vue'

export default {
  name: 'MyPageView',
  components: { MyPageFeedView, MyPageRegionView, MyPageRandomView },

  data() {
    return {
      activeTab: localStorage.getItem('myPage.activeTab') || 'myFeed',
    }
  },

  methods: {
    async changeTab(tab) {
      this.activeTab = tab
      localStorage.setItem('myPage.activeTab', tab)
      // 지도/임베드 등이 있다면 전환 직후 사이즈 보정
      await nextTick()
      window.dispatchEvent(new Event('resize'))
    },
    goToProfileModify() {
      this.$router.push('/profileEdit')
    },

    goToSetting() {
      this.$router.push('/setting')
    },
    goToFollowers() {
      this.$router.push('/friendList')
    }
  },
}
</script>

<style scoped>
.view-container {
  width: 100%;
  /* ✅ 부모(.main-container) 너비의 100% */
  height: 100%;
  /* ✅ 부모(.main-container) 높이의 100% */

  /* 내부 레이아웃 및 스크롤 설정 */
  display: flex;
  flex-direction: column;
}

/* 1. 프로필 헤더 */
.profile-header {
  display: flex;
  align-items: center;
  padding: 16px;
  /* 아바타와 정보 사이 간격 */
  gap: 20px;
}

.profile-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background-color: #e2e8f0;
  flex-shrink: 0;
  /* 컨테이너가 줄어들어도 크기 유지 */
}

.profile-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  /* 이름, 통계, 버튼 사이 세로 간격 */
  width: 100%;
}

.username {
  font-weight: bold;
  font-size: 1.2rem;
}

/* 유저 통계 */
.user-stats {
  display: flex;
  justify-content: space-between;
  /* 통계 아이템 간격 균등 배분 */
  text-align: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: start;
}

.stat-value {
  font-weight: bold;
  font-size: 1rem;
}

.stat-label {
  font-size: 0.8rem;
  color: #64748b;
}

/* 프로필 액션 버튼 */
.profile-actions {
  display: flex;
  gap: 8px;
  /* 버튼 사이 간격 */
}

.edit-button {
  flex-grow: 1;
  /* 편집 버튼이 남은 공간을 모두 차지 */
}

/* 2. 탭 네비게이션 */
.tab-navigation {
  display: flex;
  border-top: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.tab-navigation .btn {
  border-radius: 0;
  padding: 12px 0;
  color: #64748b;
}

.tab-navigation .btn.active {
  color: #000;
  border-bottom: 2px solid #000;
  background-color: transparent;
}

.tab-navigation .btn:focus {
  box-shadow: none;
}

.tab-navigation i {
  font-size: 1.3rem;
}

/* 3. 콘텐츠 영역 */
.content-area {
  /* 남은 모든 세로 공간을 차지 */
  flex-grow: 1;
  /* 내용이 길어지면 스크롤 */
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.tab-content-panel {
  flex-grow: 1;
  /* 부모(content-area)의 남은 공간을 모두 차지 */
}
</style>
