<template>
  <div class="timeline-item-container" style="font-family: 'SUIT', sans-serif">
    <!-- 타임라인 섹션 -->
    <div class="timeline-marker-wrapper">
      <!-- 당구공 -->
      <div class="timeline-marker" :style="{ '--marker-url': `url('${markerSvgUrl}')` }"></div>
      <!-- 일직선 줄 -->
      <div class="timeline-line"></div>
    </div>
    <!-- 컨텐츠 카드 -->
    <div class="content-card">
      <div class="icon-buttons">
        <!-- 편집 버튼 -->
        <i class="bi bi-pencil" @click="onEdit"></i>
        <!-- 삭제 버튼 -->
        <i class="bi bi-trash" @click="onDelete"></i>
      </div>

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
            <PillTag :text="item.grade" type="grade" />
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
      /*
        [필요한 item 속성 예시]
        item: {
          number: 1,
          color: '#4A7CEC',
          imageSrc: 'https://...',
          zoneName: '습지생물코너',
          subject: '생명',
          grade: '초등 3학년',
          hashtags: ['항상성', '몸의 조절', '생명과학'], // [중요] 이 배열을 기반으로 computed가 작동
          placeName: '국립중앙과학관' */
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

    // ⚠️ **추가:** 지도 컴포넌트와 동일한 색상 결정 로직
    getMarkerColor(index) {
      // 코스 순서에 따른 색상 배열 (지도와 통일)
      const colors = ['#4A7CEC', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#e83e8c'];
      return colors[index % colors.length];
    },
    
    // ⚠️ **추가:** 지도 컴포넌트와 동일한 SVG 마커 이미지 생성 로직
    createMarkerImage(number, color) {
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
  },
  computed: {
    // ⚠️ **추가:** 코스 순서에 따른 마커 색상 계산
    itemColor() {
      // item.number는 보통 1부터 시작합니다. 배열 인덱스를 위해 1을 뺍니다.
      const index = (this.item.number || 1) - 1;
      return this.getMarkerColor(index);
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

/* ⚠️ **수정:** SVG 배경 이미지로 설정하여 지도 마커와 완벽 통일 */
.timeline-marker {
  /* SVG 크기(24x35)에 맞추어 마커 영역 조정 */
  width: 24px;
  height: 35px;

  /* Computed 속성에서 받아온 SVG Data URL을 배경 이미지로 사용 */
  background-image: var(--marker-url);
  background-size: contain; 
  background-repeat: no-repeat;
  background-position: center bottom; 

  /* 내부 텍스트(숫자)는 SVG에 포함되므로 숨김 */
  display: block; 
  text-indent: -9999px; 

  z-index: 2;
  position: relative;

  /* 기존 원형 마커 관련 스타일 제거 */
  border-radius: 0; 
  color: transparent;
  font-weight: normal;
font-size: 0;
}

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
  color: #e53e3e;
  /* 삭제 아이콘은 빨간색 */
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
  flex-shrink: 0;
  /* 글자가 길어져도 줄어들지 않게 */
}
</style>
