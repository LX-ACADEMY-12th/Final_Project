<template>
  <div class="timeline-item-container" style="font-family: 'SUIT', sans-serif">
    <!-- 타임라인 섹션 -->
    <div class="timeline-marker-wrapper">
      <div class="timeline-marker-svg" :style="{ backgroundImage: `url(${markerSvgImage})` }">
      </div>
      <!-- 일직선 줄 -->
      <div class="timeline-line"></div>
    </div>
    <!-- 컨텐츠 카드 -->
    <div class="content-card">
      <!-- 카드 몸체 -->
      <div class="card-body">
        <!-- 장소 이미지 -->
        <div class="card-image">
          <img :src="item.imageUrl" alt="장소 이미지" />
        </div>
        <!-- 카드 텍스트 -->
        <div class="card-text">
          <div class="d-flex align-items-center justify-content-left gap-1">
            <!-- 전시명 -->
            <h5 class="place-name">{{ item.title }}</h5>
          </div>
          <!-- 알약 태그 영역 -->
          <div class="d-flex gap-1">
            <!-- 과학영역 태그 -->
            <PillTag :text="item.subject" type="subject" />
            <!-- 학년 태그 -->
            <PillTag :text="item.grade.replace('초등 ', '')" type="grade" />
          </div>
          <!-- 중분류 태그 영역-->
          <div class="d-flex gap-1">
            <HashTag v-for="tag in visibleHashtags" :key="tag" :text="tag" />
            <!-- +N 숫자 -->
            <span v-if="hasMoreHashtags" class="more-tags">
              +{{ remainingHashtagsCount }}
            </span>
          </div>
        </div>
      </div>
      <!-- 밑줄 -->
      <hr class="hr" />
      <span class="location-label">
        상세주소
        <!-- 상세주소 -->
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
    // [추가] 마커 SVG 이미지 생성 함수
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
/* 부트스트랩 아이콘 (index.html에 CDN이 없다면 필요) */
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.timeline-item-container {
  display: flex;
  position: relative;
  /* 고정 너비 */
  max-width: 360px;
}

/* [추가] SVG 이미지를 배경으로 사용하는 새로운 마커 스타일 */
.timeline-marker-svg {
  width: 24px;
  /* SVG 이미지의 width와 동일하게 */
  height: 35px;
  /* SVG 이미지의 height와 동일하게 */
  background-size: contain;
  /* 이미지가 요소 안에 꽉 차도록 */
  background-repeat: no-repeat;
  background-position: center;
  z-index: 2;
  /* SVG 이미지에 따라 마커의 상단 여백을 조절할 수 있습니다. */
  /* margin-top: -XXpx; */
}

/* 1. 타임라인 마커 (왼쪽) */
.timeline-marker-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  /* 고정 너비 */
  flex-shrink: 0;
  margin-right: 12px;
}

/* .timeline-marker {
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
} */

.timeline-line {
  width: 2px;
  flex-grow: 1;
  /* 마커 아래 공간을 채우는 선 */
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
  margin-bottom: 16px;
  /* 아이템 간 간격 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  min-width: 0;
}

/* 2-2. 컨텐츠 정보 */
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

/* [수정] .address와 .description 스타일 분리 */
.description {
  font-size: 14px;
  color: #777;
  margin: 2px 0 0 0;
}

.address {
  font-size: 14px;
  color: #555;
  margin: 0;
  /* 상단 마진 제거 */
}

/* [추가] 구분선 스타일 */
.hr {
  border: none;
  height: 1px;
  background-color: rgb(0, 0, 0);
  /* 위아래 여백 */
  margin: 12px 0;
}

/* [추가] '전시관'/'상세주소' 라벨 스타일 */
.location-label {
  display: flex;
  align-items: center;
  /* 세로 중앙 정렬 */
  gap: 10px;
  /* 라벨과 주소 사이 간격 */
  font-size: 14px;
  font-weight: 500;
  color: #4A7CEC;
  /* 파란색 계열 */
  flex-shrink: 0;
  /* 글자가 길어져도 줄어들지 않게 */
}
</style>
