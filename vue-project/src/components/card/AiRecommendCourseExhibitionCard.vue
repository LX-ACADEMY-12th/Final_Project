<template>
  <div class="timeline-item-container" style="font-family: 'SUIT', sans-serif">
    <div class="timeline-marker-wrapper">
      <div class="timeline-marker-svg" :style="{ backgroundImage: `url(${markerSvgImage})` }">
      </div>
      <div class="timeline-line"></div>
    </div>
    <div class="content-card">
      <div class="card-body">
        <div class="card-image">
          <img :src="computedImageUrl" alt="장소 이미지" />
        </div>
        <div class="card-text">
          <div class="d-flex align-items-center justify-content-left gap-1">
            <h5 class="place-name">{{ item.title }}</h5>
          </div>
          <div class="d-flex gap-1">
            <PillTag :text="item.subject" type="subject" />
            <PillTag :text="item.grade.replace('초등 ', '')" />
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

      <!-- 수정된 전시 리스트 표시 부분 -->
      <div class="exhibition-section">
        <div class="exhibition-header">
          <i class="bi bi-palette-fill"></i>
          <span class="exhibition-title">추천 전시</span>
          <span class="exhibition-count">({{ exhibitionCount }}개)</span>
        </div>
        <div class="exhibition-list">
          <div v-for="(exhibition, index) in formattedExhibitions" :key="index" class="exhibition-item">
            <span class="exhibition-number">{{ index + 1 }}</span>
            <span class="exhibition-name">{{ exhibition }}</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import PillTag from '@/components/tag/PillTag.vue';
// import TypeTag from '@/components/tag/TypeTag.vue';
import HashTag from '@/components/tag/HashTag.vue';

export default {
  name: 'CourseExhibitionCard',
  components: {
    PillTag,
    HashTag,
    // TypeTag
  },
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  computed: {
    // 기존 computed 속성들...
    computedImageUrl() {
      const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';;
      const url = this.item.imageUrl;
      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url;
    },

    // 전시 리스트 관련 새로운 computed 속성들
    formattedExhibitions() {
      if (!this.item.exhibitionList || !Array.isArray(this.item.exhibitionList)) {
        return [];
      }

      // ⭐️ [수정된 로직] 이름^URL 문자열에서 이름만 추출
      return this.item.exhibitionList.map(fullString => {
        // 1. 이름과 URL을 구분하는 '^'를 기준으로 분리
        const parts = fullString.split('^');

        let exhibitionName = parts[0] || fullString; // '^' 앞부분(이름)이 있다면 사용

        // // 2. 추가 처리: 만약 이름 앞에 순서 번호(1, 2, 3...)가 붙어 있다면 제거
        // // 예: "2괴짜과학자의 바이러스" -> "괴짜과학자의 바이러스"
        // // 첫 글자가 숫자이고, 그 뒤에 글자가 있다면 숫자를 제거합니다.
        // if (exhibitionName.length > 1 && !isNaN(exhibitionName[0]) && isNaN(exhibitionName[1])) {
        //   exhibitionName = exhibitionName.substring(1);
        // }

        return exhibitionName.trim();
      });
    },

    exhibitionCount() {
      return this.formattedExhibitions.length;
    },

    // 기존 해시태그 관련 computed...
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

    markerSvgImage() {
      const color = this.getCourseItemColor(this.item.number);
      return this.createMarkerSvg(this.item.number, color);
    }
  },

  methods: {
    getCourseItemColor(itemNumber) {
      const colors = ['#FF5A5A', '#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      if (itemNumber === 1) {
        return '#FF5A5A';
      }
      return colors[(itemNumber - 1) % colors.length];
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
  }
};
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.timeline-item-container {
  display: flex;
  position: relative;
}

.timeline-marker-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  flex-shrink: 0;
  margin-right: 12px;
}

.timeline-marker-svg {
  width: 24px;
  height: 35px;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  z-index: 2;
}

.timeline-line {
  width: 2px;
  flex-grow: 1;
  background-color: #e0e0e0;
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
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-width: 0;
}

.card-body {
  display: flex;
}

.card-image img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  margin-right: 16px;
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

.place-name {
  font-size: 16px;
  font-weight: 600;
  margin: 2px 0;
  margin-bottom: 4px;
}

.hr {
  border: none;
  height: 1px;
  background-color: #e9ecef;
  margin: 12px 0;
}

/* 새로운 전시 리스트 스타일 */
.exhibition-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exhibition-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.exhibition-header .bi-palette-fill {
  color: #6366f1;
  font-size: 16px;
}

.exhibition-title {
  color: #333;
}

.exhibition-count {
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
}

.exhibition-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 4px;
}

.exhibition-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-radius: 8px;
  border-left: 3px solid #6366f1;
  transition: all 0.2s ease;
}

.exhibition-item:hover {
  background: linear-gradient(135deg, #f3f4f6 0%, #fff 100%);
  transform: translateX(2px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.exhibition-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #6366f1;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.exhibition-name {
  font-size: 14px;
  color: #374151;
  line-height: 1.4;
  flex-grow: 1;
}

/* 반응형 처리 */
@media (max-width: 480px) {
  .exhibition-item {
    padding: 6px 10px;
  }

  .exhibition-name {
    font-size: 13px;
  }
}
</style>
