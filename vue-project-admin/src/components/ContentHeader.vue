<template>
  <div class="content-header mb-4">
    <h1 class="content-title">
      <i :class="['me-2', iconClass]"></i>
      {{ title }}
    </h1>
    <p class="content-description">
      {{ description }}
    </p>
  </div>
</template>

<script>
export default {
  name: "ContentHeader",
  props: {
    // 제목 앞에 표시할 아이콘 클래스 (예: 'bi bi-chat-square-text')
    iconClass: {
      type: String,
      required: true,
    },
    // 페이지 제목 (예: '1. 후기 관리')
    title: {
      type: String,
      required: true,
    },
    // 페이지 설명
    description: {
      type: String,
      required: true,
    },
  },
};
</script>

<style scoped>
/* ContentHeader 관련 스타일을 이곳으로 옮깁니다. */
.content-header {
  background-color: white;
  padding: 25px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px !important;
}

.content-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--bs-primary);
  margin-bottom: 8px;
}

.content-title i {
  font-size: 1.8rem;
  vertical-align: middle;
}

.content-description {
  font-size: 0.9rem;
  color: #667085;
  margin-bottom: 0;
}
</style>