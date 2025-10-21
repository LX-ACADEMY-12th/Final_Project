<template>
  <div class="timeline-item-container">
    <div class="timeline-marker-wrapper">
      <div class="timeline-marker" :style="{ backgroundColor: item.color }">
        {{ item.number }}
      </div>
      <div class="timeline-line"></div>
    </div>

    <div class="content-card">
      <div class="icon-buttons">
        <i class="bi bi-pencil" @click="onEdit"></i>
        <i class="bi bi-trash" @click="onDelete"></i>
      </div>

      <div class="card-body">
        <div class="card-image">
          <img :src="item.imageSrc" alt="장소 이미지" />
        </div>
        <div class="card-text">
          <h5 class="place-name">{{ item.placeName }}</h5>
          <p class="address">{{ item.address }}</p>
          <p class="description">{{ item.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserLikeCourseItem',
  props: {
    // 부모(UserLikeCourseDetail)로부터 'item' 객체를 받음
    item: {
      type: Object,
      required: true,
    },
  },
  // 부모에게 'edit', 'delete' 이벤트를 전달(emit)하겠다고 선언
  emits: ['edit', 'delete'],
  methods: {
    // '수정' 아이콘 클릭 시
    onEdit() {
      // 'edit' 이벤트를 부모에게 전달 (자신의 ID와 함께)
      this.$emit('edit', this.item.id);
    },
    // '삭제' 아이콘 클릭 시
    onDelete() {
      // 'delete' 이벤트를 부모에게 전달 (자신의 ID와 함께)
      this.$emit('delete', this.item.id);
    },
  },
};
</script>

<style scoped>
/* 부트스트랩 아이콘 (index.html에 CDN이 없다면 필요) */
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.timeline-item-container {
  display: flex;
  position: relative;
}

/* 1. 타임라인 마커 (왼쪽) */
.timeline-marker-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px; /* 고정 너비 */
  flex-shrink: 0;
  margin-right: 12px;
}
.timeline-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
  z-index: 2;
}
.timeline-line {
  width: 2px;
  flex-grow: 1; /* 마커 아래 공간을 채우는 선 */
  background-color: #e0e0e0;
}
/* 마지막 아이템은 선이 필요 없음 */
.timeline-item-container:last-child .timeline-line {
  display: none;
}

/* 2. 컨텐츠 카드 (오른쪽) */
.content-card {
  position: relative;
  flex-grow: 1;
  background-color: white;
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 16px;
  margin-bottom: 16px; /* 아이템 간 간격 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 2-1. 아이콘 버튼 */
.icon-buttons {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 12px;
  z-index: 3;
}
.icon-buttons i {
  font-size: 18px;
  color: #888;
  cursor: pointer;
}
.icon-buttons i:hover {
  color: #333;
}
.icon-buttons .bi-trash:hover {
  color: #e53e3e; /* 삭제 아이콘은 빨간색 */
}

/* 2-2. 컨텐츠 정보 */
.card-body {
  display: flex;
}
.card-image img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  margin-right: 16px;
  object-fit: cover;
}
.card-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.category {
  font-size: 12px;
  font-weight: bold;
}
.place-name {
  font-size: 18px;
  font-weight: 600;
  margin: 2px 0;
}
.address, .description {
  font-size: 14px;
  color: #555;
  margin: 2px 0 0 0;
}
.description {
  color: #777;
}
</style>