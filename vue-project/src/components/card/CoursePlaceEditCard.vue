<template>
  <div class="timeline-item-container" style="font-family: 'SUIT', sans-serif">
    <div class="timeline-marker-wrapper">
      <div class="timeline-marker-svg" :style="{ backgroundImage: `url(${markerSvgImage})` }">
      </div>
      <div class="timeline-line"></div>
    </div>
    <div class="content-card">
      <div class="icon-buttons">
        <i class="bi bi-trash" @click="onDelete"></i>
      </div>

      <div class="card-body">
        <div class="card-image">
          <img :src="computedImageUrl" alt="장소 이미지" />
        </div>
        <div class="card-text">
          <div class="d-flex align-items-center justify-content-left gap-1">
            <h5 class="place-name">{{ item.title }}</h5>
          </div>
          <div class="d-flex gap-1">

            <PillTag v-if="displaySubject" :text="displaySubject" type="subject" />

            <PillTag v-if="displayGrade" :text="displayGrade" type="grade" />

          </div>
          <div class="d-flex gap-1">

            <HashTag v-for="tag in visibleHashtags" :key="tag" :text="tag" />
            <span v-if="hasMoreHashtags" class="more-tags">
              +{{ remainingHashtagsCount }}
            </span>

          </div>
        </div>
      </div>

      <hr class="hr" />
      <span class="location-label">
        상세주소
        <span class="address">{{ item.place }}</span>
      </span>

    </div>
  </div>
</template>

<script>
import PillTag from '@/components/tag/PillTag.vue';
import HashTag from '@/components/tag/HashTag.vue';

export default {
  name: 'CourseExhibitionCard',
  components: {
    PillTag,
    HashTag,
  },
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  emits: ['edit', 'delete'],
  methods: {
    onEdit() {
      this.$emit('edit', this.item.id);
    },
    onDelete() {
      this.$emit('delete', this.item.id);
    },

    createMarkerSvg(number, color) {
      const svg = `
    <svg width="24" height="35" viewBox="0 0 24 35" xmlns="http://www.w3.org/2000/svg">
     <path d="M12 0C5.373 0 0 5.373 0 12c0 9 12 23 12 23s12-14 12-23c0-6.627-5.373-12-12-12z"
       fill="${color}" stroke="#fff" stroke-width="2"/>
     <circle cx="12" cy="12" r="8" fill="#fff"/>
     <text x="12" y="16" text-anchor="middle" font-family="Arial, sans-serif"
       font-size="10" font-weight="bold" fill="${color}">${number}</text>
    </svg>
   `;
      return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    },

    getCourseItemColor(itemNumber) {
      const colors = ['#FF5A5A', '#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      if (itemNumber === 1) {
        return '#FF5A5A';
      }
      return colors[(itemNumber - 1) % colors.length];
    }
  },
  computed: {
    computedImageUrl() {
      const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';
      const url = this.item.imageUrl;

      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url || 'https://placehold.co/800x600/AACCFF/000000?text=%E2%80%8B';
    },

    // computed 속성: SVG 이미지 URL 생성 (item.color 의존성 제거)
    markerSvgImage() {
      const color = this.getCourseItemColor(this.item.number);
      return this.createMarkerSvg(this.item.number, color);
    },

    // ❗️ [수정] 불필요하고 오류가 있던 computed 속성 제거
    // itemColor() { ... }
    // markerSvgUrl() { ... }

    // 화면에 표시할 해시태그 목록 (최대 2개)
    visibleHashtags() {
      if (!Array.isArray(this.item.hashtags)) {
        return [];
      }
      return this.item.hashtags.slice(0, 2);
    },
    hasMoreHashtags() {
      if (!Array.isArray(this.item.hashtags)) {
        return false;
      }
      return this.item.hashtags.length > 2;
    },
    remainingHashtagsCount() {
      if (!Array.isArray(this.item.hashtags)) {
        return 0;
      }
      return this.item.hashtags.length - 2;
    },

    // subject 표시용 computed 속성
    displaySubject() {
      if (Array.isArray(this.item.subject) && this.item.subject.length > 0) {
        return this.item.subject[0];
      }
      return null;
    },
    // grade 표시용 computed 속성
    displayGrade() {
      if (Array.isArray(this.item.grade) && this.item.grade.length > 0) {
        const firstGrade = this.item.grade[0];
        return (firstGrade || '').replace('초등 ', '');
      }
      return null;
    },
  },
};
</script>

<style scoped>
/* 💡 [수정] 전체적으로 개선된 스타일 적용 */
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.timeline-item-container {
  display: flex;
  position: relative;
  /* ❗️ [FIX 1] align-items: flex-start 제거 -> 기본값 stretch로 변경 */
}

.timeline-marker-svg {
  width: 28px;
  /* 24px → 28px로 약간 크게 */
  height: 40px;
  /* 35px → 40px로 약간 크게 */
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  z-index: 2;
}

.timeline-marker-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  flex-shrink: 0;
  margin-right: 16px;
  /* 12px → 16px로 여유 증가 */
}

.timeline-line {
  width: 2px;
  flex-grow: 1;
  background-color: #e0e0e0;
  /* 마커와 선 사이 간격 추가 */
  margin-top: 4px;
}

.timeline-item-container:last-child .timeline-line {
  display: none;
}

.content-card {
  position: relative;
  flex-grow: 1;
  background-color: white;
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 20px;
  /* 16px → 20px로 내부 여백 증가 */
  margin-bottom: 20px;
  /* 16px → 20px로 카드 간 간격 증가 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-width: 0;
  transition: box-shadow 0.3s ease;
  /* 호버 효과를 위한 트랜지션 추가 */
}

.content-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  /* 호버시 그림자 강조 */
}

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
  color: #e53e3e;
}

.card-body {
  display: flex;
  align-items: center;
  /* 수직 중앙 정렬 추가 */
  gap: 16px;
  /* flex gap 사용으로 간격 일관성 개선 */
}

.card-image {
  flex-shrink: 0;
  /* 이미지 크기 고정 */
}

.card-image img {
  width: 80px;
  /* 60px → 80px로 이미지 크기 증가 */
  height: 80px;
  /* 60px → 80px로 이미지 크기 증가 */
  border-radius: 8px;
  object-fit: cover;
}

.card-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-grow: 1;
  gap: 8px;
  min-width: 0;
}

.d-flex {
  display: flex;
  align-items: center;
  /* 태그들 수직 정렬 */
}

.gap-1 {
  gap: 8px;
  /* 태그 간 간격 명확히 */
}

.place-name {
  font-size: 18px;
  /* 16px → 18px로 제목 크기 증가 */
  font-weight: 600;
  margin: 0 0 8px 0;
  /* 마진 정리 */
  color: #333;
  /* 색상 명확히 */
}

.hr {
  border: none;
  height: 1px;
  background-color: #e0e0e0;
  /* rgb(0,0,0) → #e0e0e0로 연하게 */
  margin: 16px 0;
  /* 12px → 16px로 여백 증가 */
}

.location-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  /* 500 → 600으로 굵게 */
  color: #4A7CEC;
  /* ❗️ [FIX 2] flex-shrink: 0 제거 및 min-width 추가 */
  min-width: 0;
}

.address {
  font-size: 14px;
  color: #666;
  /* #555 → #666으로 약간 연하게 */
  font-weight: 400;
  /* 일반 굵기 명시 */
  margin: 0;
  /* ❗️ [FIX 2] 말줄임표 처리를 위한 4+1 속성 */
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  /* 남은 공간 차지 */
}

.more-tags {
  font-size: 12px;
  color: #999;
  font-weight: 500;
  padding: 2px 6px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

/* 반응형 디자인 추가 */
@media (max-width: 768px) {
  .card-image img {
    width: 60px;
    height: 60px;
  }

  .place-name {
    font-size: 16px;
  }

  .content-card {
    padding: 16px;
  }
}

/* ❗️ 불필요한 .timeline-marker 클래스 제거 */
</style>
