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
  // 이 컴포넌트 이름을 설정
  name: 'CourseExhibitionCard',
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

    // 마커 SVG 이미지 생성 함수
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
      // 이 카드는 UserLikeCourseDetailView에서 옴
      // UserLikeCourseDetailView가 이미 imageUrl을 만들어줌
      const url = this.item.imageUrl; 
      
      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      // http로 시작하거나, 없는 경우
      return url || 'https://placehold.co/800x600/AACCFF/000000'; // 기존 fallback 유지
    },

    // ⚠️ **추가:** 코스 순서에 따른 마커 색상 계산
    itemColor() {
      // item.number는 보통 1부터 시작합니다. 배열 인덱스를 위해 1을 뺍니다.
      const index = (this.item.number || 1) - 1;
      return this.getMarkerColor(index);
    },
    // computed 속성: SVG 이미지 URL 생성 (item.color 의존성 제거)
    markerSvgImage() {
      // getCourseItemColor 함수 사용
      const color = this.getCourseItemColor(this.item.number);
      return this.createMarkerSvg(this.item.number, color);
    },
    // ⚠️ **추가:** 계산된 색상과 번호로 SVG Data URL 생성
    markerSvgUrl() {
      const number = this.item.number || 1;
      const color = this.itemColor;
      return this.createMarkerImage(number, color);
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
    // subject 표시용 computed 속성
    displaySubject() {
      // 1. item.subject가 배열이고 요소가 있는지 확인
      if (Array.isArray(this.item.subject) && this.item.subject.length > 0) {
        // 2. 첫 번째 요소만 반환 (예: "물리")
        return this.item.subject[0];
      }
      // 3. 유효하지 않으면 null 반환 (태그 숨김)
      return null;
    },
    //  grade 표시용 computed 속성
    displayGrade() {
      // 1. item.grade가 배열이고 요소가 있는지 확인
      if (Array.isArray(this.item.grade) && this.item.grade.length > 0) {
        // 2. 첫 번째 요소 가져오기 (예: "초등 4학년")
        const firstGrade = this.item.grade[0];
        // 3. "초등 " 문자열 제거 (예: "4학년")
        return (firstGrade || '').replace('초등 ', '');
      }
      // 4. 유효하지 않으면 null 반환 (태그 숨김)
      return null;
    },
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
.timeline-marker {
  width: 24px;
  height: 35px;
  background-image: var(--marker-url);
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center bottom;
  display: block;
  text-indent: -9999px;
  z-index: 2;
  position: relative;
  border-radius: 0;
  color: transparent;
  font-weight: normal;
  font-size: 0;
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
  flex-shrink: 0;
}
</style>