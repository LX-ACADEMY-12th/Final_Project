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
          <img :src="computedImageUrl" alt="장소 이미지" @load="onImageLoaded" @error="onImageError" />
        </div>
        <div class="card-text">
          <div class="d-flex align-items-center justify-content-left gap-1">
            <h5 class="place-name">{{ item.title }}</h5>
          </div>
          <div class="d-flex gap-1">
            <PillTag :text="item.subject" type="subject" />
            <PillTag :text="(item.grade || '').replace('초등 ', '')" type="grade" />
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
  // 이 컴포넌트 이름을 설정
  name: 'CoursePlaceCard',
  // 컴포넌트 등록
  components: {
    PillTag,
    HashTag,
  },
  props: {
    // 부모로부터 'item' 객체를 받음
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
    // 🔴 추가: 이미지 로드 완료
    onImageLoaded() {
      console.log(`✅ 이미지 로드 완료 (${this.item.number}번 - ${this.item.title})`);
    },
    // 🔴 추가: 이미지 로드 실패
    onImageError() {
      console.error(`❌ 이미지 로드 실패 (${this.item.number}번 - ${this.item.title}): ${this.item.imageUrl}`);
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
    // 코스 순서에 따른 색상 결정 함수 (CourseMap.vue와 동일하게)
    getCourseItemColor(itemNumber) {
      // CourseMap.vue의 getMarkerColor 함수와 동일한 로직 사용
      // 여기서는 item.number를 직접 사용해야 합니다. (index 아님)
      // item.number는 1번부터 시작하므로, index로 변환하려면 -1을 해야 합니다.
      const colors = ['#FF5A5A', '#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      // 첫 번째 항목 (number: 1)은 특별한 빨간색, 나머지는 blue
      if (itemNumber === 1) {
        return '#FF5A5A';
      }
      // item.number는 1부터 시작하므로 배열 인덱스에 맞추기 위해 -1
      return colors[(itemNumber - 1) % colors.length];
    }
  },
  computed: {
    // [!!] 1. 이미지 URL을 계산하는 computed 속성 추가
    computedImageUrl() {
      const IMAGE_BASE_URL = 'http://localhost:8080/images/';
      // 이 카드는 PlaceDetailsView -> CourseRecommend -> 여기로 옴
      // PlaceDetailsView가 이미 imageUrl을 만들어줌
      const url = this.item.imageUrl; 
      
      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url;
    },

    // 화면에 표시할 해시태그 목록 (최대 2개)
    visibleHashtags() {
      // item.hashtags가 배열이 아니거나 비어있으면 빈 배열 반환
      if (!Array.isArray(this.item.hashtags)) {
        return [];
      }
      // 최대 2개까지만 잘라서 반환
      return this.item.hashtags.slice(0, 2);
    },
    // 숨겨진 해시태그가 더 있는지 여부
    hasMoreHashtags() {
      if (!Array.isArray(this.item.hashtags)) {
        return false;
      }
      return this.item.hashtags.length > 2;
    },
    // 숨겨진 해시태그의 개수
    remainingHashtagsCount() {
      if (!Array.isArray(this.item.hashtags)) {
        return 0;
      }
      return this.item.hashtags.length - 2;
    },
    // computed 속성: SVG 이미지 URL 생성 (item.color 의존성 제거)
    markerSvgImage() {
      // getCourseItemColor 함수 사용
      const color = this.getCourseItemColor(this.item.number);
      return this.createMarkerSvg(this.item.number, color);
    }
  },
};
</script>

<style scoped>
/* (스타일은 변경사항 없습니다) */
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");
.timeline-item-container {
  display: flex;
  position: relative;
  max-width: 360px;
}
.timeline-marker-svg {
  width: 24px;
  height: 35px;
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
  margin-right: 12px;
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
.category {
  font-size: 12px;
  font-weight: bold;
}
.place-name {
  font-size: 16px;
  font-weight: 600;
  margin: 2px 0;
  margin-bottom: 4px;
}
.description {
  font-size: 14px;
  color: #777;
  margin: 2px 0 0 0;
}
.address {
  font-size: 14px;
  color: #555;
  margin: 0;
}
.hr {
  border: none;
  height: 1px;
  background-color: rgb(0, 0, 0);
  margin: 12px 0;
}
.location-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #4A7CEC;
  flex-shrink: 0;
}
</style>