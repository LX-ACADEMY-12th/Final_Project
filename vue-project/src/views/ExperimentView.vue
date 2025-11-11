<template>
  <div class="experiment-container d-flex flex-column h-100 bg-white">
    <div class="experiment-header d-flex justify-content-between align-items-center p-3 border-bottom bg-white">
      <button @click="goBack" class="btn p-0 border-0" aria-label="뒤로 가기">
        <i class="bi bi-chevron-left fs-5"></i>
      </button>

      <h5 class="mb-0 fw-bold fs-6 flex-grow-1 text-center truncate">
        {{ experimentTitle }}
      </h5>

      <button @click="openInNewTab" class="btn btn-outline-primary btn-sm" title="새 창에서 열기" aria-label="새 창에서 열기">
        <i class="bi bi-box-arrow-up-right"></i>
      </button>
    </div>

    <div class="experiment-content flex-grow-1 position-relative">

      <div v-if="isLoading" class="loading-overlay">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-3 text-muted small">과학 실험실 로딩 중...</p>
      </div>

      <iframe v-show="!isLoading" :src="experimentUrl" frameborder="0" width="100%" height="100%" allowfullscreen
        @load="onIframeLoad">
        이 브라우저는 iframe을 지원하지 않습니다.
      </iframe>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const experimentUrl = ref('');
const experimentTitle = ref('과학 실험');
const isLoading = ref(true); // 처음엔 항상 로딩 중

onMounted(() => {
  // HomeView에서 쿼리 파라미터로 넘겨준 URL과 제목을 받습니다.
  experimentUrl.value = route.query.url || '';
  experimentTitle.value = route.query.title || '과학 실험';

  // URL이 없으면 잘못 접근한 것이므로 홈으로 돌려보냅니다.
  if (!experimentUrl.value) {
    router.replace('/home');
  }
});

// iframe 로드가 완료되면 호출될 함수
const onIframeLoad = () => {
  isLoading.value = false;
};

// 뒤로 가기
const goBack = () => {
  router.back();
};

// 새 탭에서 열기 (비상용)
const openInNewTab = () => {
  if (experimentUrl.value) {
    window.open(experimentUrl.value, '_blank', 'noopener,noreferrer');
  }
};
</script>

<style scoped>
.experiment-container {
  font-family: 'SUIT', sans-serif;
}

.experiment-header {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.experiment-content {
  /* 스크롤바는 iframe 내부에서 처리하도록 */
  overflow: hidden;
}

/* iframe을 덮는 로딩 오버레이 */
.loading-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  z-index: 5;
  transition: opacity 0.3s ease;
}

iframe {
  display: block;
  /* 하단 여백 제거 */
}

/* 제목이 길어질 경우 ... 처리 */
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0 1rem;
}
</style>
