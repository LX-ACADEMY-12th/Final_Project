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
  computed: {
    // [!!] 1. 이미지 URL (기존 로직 유지)
    computedImageUrl() {
      const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';
      const url = this.item.imageUrl;

      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url || 'https://via.placeholder.com/60x60';
    },

    // [!!] 2. 해시태그 (기존 로직 유지)
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

    // [!!] 3. 마커 SVG (기존 로직 유지)
    markerSvgImage() {
      const color = this.getCourseItemColor(this.item.number);
      return this.createMarkerSvg(this.item.number, color);
    },

    // [!!] 4. '추천 전시' 목록 (기존 로직 유지)
    formattedExhibitions() {
      const list = this.item.exhibitionList || [];
      if (!Array.isArray(list)) {
        return [];
      }
      return list.map(exhibition => {
        const parts = exhibition.split(' - ');
        return parts[0] || exhibition;
      });
    },
    exhibitionCount() {
      return this.formattedExhibitions.length;
    },

    // [!!] 5. subject/grade를 안전하게 처리하는 computed 속성 (다시 추가)
    displaySubject() {
      // 1. item.subject가 배열이고 요소가 있는지 확인
      if (Array.isArray(this.item.subject) && this.item.subject.length > 0) {
        // 2. 첫 번째 요소만 반환 (예: "물리")
        return this.item.subject[0];
      }
      // (방어 코드) 만약 배열이 아니라 문자열로 왔다면 그대로 반환
      if (typeof this.item.subject === 'string') {
        return this.item.subject;
      }
      // 3. 유효하지 않으면 null 반환 (태그 숨김)
      return null;
    },

    displayGrade() {
      // 1. item.grade가 배열이고 요소가 있는지 확인
      if (Array.isArray(this.item.grade) && this.item.grade.length > 0) {
        // 2. 첫 번째 요소 가져오기 (예: "초등 3학년")
        const firstGrade = this.item.grade[0];
        // 3. (안전 장치) firstGrade가 문자열인지 확인 후 .replace()
        return (typeof firstGrade === 'string') ? firstGrade.replace('초등 ', '') : null;
      }
      // (방어 코드) 만약 배열이 아니라 문자열로 왔다면
      if (typeof this.item.grade === 'string') {
        return this.item.grade.replace('초등 ', '');
      }
      // 4. 유효하지 않으면 null 반환 (태그 숨김)
      return null;
    },

  },

  // [!!] 6. 마커 생성을 위한 methods (기존 로직 유지)
  methods: {
    // ... (getCourseItemColor 와 createMarkerSvg 함수는 그대로 유지) ...
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
  /* 태그와의 간격을 위해 유지 */
}

/* 해시태그 관련 스타일 */
.more-tags {
  font-size: 12px;
  color: #6b7280;
  align-self: center;
  font-weight: 500;
}

.hr {
  border: none;
  height: 1px;
  background-color: #e9ecef;
  /* 약간 더 연한 회색으로 변경 */
  margin: 12px 0;
}

/* [!!] '전시관' 위치 정보 스타일 (제거됨) */
/*
.location-label {
 display: flex;
 align-items: center;
 gap: 10px;
 font-size: 14px;
 font-weight: 500;
 flex-shrink: 0;
}
.address {
 font-size: 14px;
 color: #555;
 margin: 0;
}
*/


/* [!!] 새로운 전시 리스트 스타일 추가 */
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
  /* 테마 색상 (보라색) */
  font-size: 16px;
}

.exhibition-title {
  color: #333;
}

.exhibition-count {
  color: #6b7280;
  /* 회색 */
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
  /* 은은한 그라데이션 배경 */
  border-radius: 8px;
  border-left: 3px solid #6366f1;
  /* 왼쪽 테마 색상 강조 */
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
  /* 어두운 회색 */
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

/* (기존 아이콘 버튼 스타일 - 필요시 C1에서 가져오기) */
/*
.icon-buttons { ... }
.icon-buttons i { ... }
*/
</style>
