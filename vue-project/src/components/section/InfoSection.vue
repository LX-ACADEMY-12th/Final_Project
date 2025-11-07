<template>
  <section class="info-section" v-if="item.title">
    <div class="top-meta">
      <div class="image-area">
        <img :src="mainImageSrc" alt="메인 이미지" class="main-image">

        <div v-if="item.isVisited" class="visit-stamp">
            <i class="bi bi-postage-heart-fill"></i>
            <span>방문 인증</span>
        </div>

        <div v-else class="visit-stamp inactive">
          <i class="bi bi-postage-heart"></i>
          <span>방문 인증</span>
        </div>
      </div>
      
    </div>

    <div class="content-body">
      <h2 class="title">
        {{ item.title }}
      </h2>
      <div class="rating">
        <span class="stars">
          <i v-for="i in Math.floor(item.rating)" :key="'full-' + i" class="bi bi-star-fill"></i>
          <i v-if="item.rating % 1 >= 0.5" class="bi bi-star-half"></i>
          <i v-for="i in (5 - Math.ceil(item.rating))" :key="'empty-' + i" class="bi bi-star"></i>
        </span>

        <span class="score">
          <span class="rating-value">{{ item.rating }}점</span>
          <span class="review-count-value">({{ item.reviewCount }})</span>
        </span>

        <div v-if="mainCategory || gradeTag" class="subject-tags-container">
          <PillTag v-if="mainCategory" :text="mainCategory" type="subject" />
          <PillTag v-if="gradeTag" :text="gradeTag.replace('초등 ', '')" type="grade" />
        </div>

      </div>

      <div v-if="subCategories.length > 0" class="hashtags-area">
        <Hashtag v-for="tag in subCategories" :key="tag" :text="tag" />
      </div>

        <div class="description" :class="{ 'expanded': isExpanded }">
          {{ displayedDescription }}
          <button v-if="isLongText" @click="toggleDescription" class="btn btn-white">
            {{ isExpanded ? '접기' : '(더보기)' }}
          </button>
        </div>

      <div class="floor-map-section" v-if="item.type === '전시'"> 
        <div class="map-container">
          <img :src="item.floorMapUrl" alt="층별 지도" class="img-fluid rounded-3 border" 
            v-if="item.floorMapUrl" />
          <div v-else class="map-placeholder">
          층별 지도 이미지가 없습니다.
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue';

// (1) 컴포넌트 임포트 (기존 UI용)
import PillTag from '@/components/tag/PillTag.vue';
import Hashtag from '@/components/tag/HashTag.vue';

// (2) Props 정의 (부모로부터 모든 데이터를 받음)
const props = defineProps({
  // 'item' 객체는 API 응답의 핵심 데이터를 포함
  item: {
    type: Object,
    required: true,
    default: () => ({})
  },
  // 'mainCategory', 'gradeTag', 'subCategories'는
  // 부모(PlaceDetailView)가 $route.query에서 추출하여 넘겨줌
  mainCategory: {
    type: String,
    default: ''
  },
  gradeTag: {
    type: String,
    default: ''
  },
  subCategories: {
    type: Array,
    default: () => []
  }
});


// (3) 기존 로직: 메인 이미지 URL 계산 (변수명 유지)
const mainImageSrc = computed(() => {
  const BASE_URL = 'http://localhost:8080/images/';
  
  // ▼▼▼▼▼ [여기 수정!] ▼▼▼▼▼
  // props.item.mainImageUrl (X) -> props.item.mainImage (O)
  const imageUrl = props.item.mainImage;

  if (imageUrl && !imageUrl.startsWith('http')) {
    // URL이 있고, http로 시작하지 않으면 (예: 'exhibition/1.jpg')
    return BASE_URL + imageUrl;
  }
  
  // URL이 http로 시작하거나(https://via.placeholder...), 
  // URL이 없으면(null) 그대로 반환
  return imageUrl; 
  // ▲▲▲▲▲ [여기 수정!] ▲▲▲▲▲
});


// (4) 신규 로직: 맞춤형 팁 + 더보기/접기

// '맞춤 팁' 계산 (내부 임시 변수)
const personalizedTip = computed(() => {
  // [API 필요] item.viewingTips 배열이 API 응답에 포함되어야 함
  // (예: [{ grade: '초등 5학년', subject: '지구', tip: '...' }])
  if (!props.item.viewingTips || !Array.isArray(props.item.viewingTips)) {
    return null;
  }
  
  // 부모에게서 받은 과목/학년과 일치하는 팁 찾기
  const foundTip = props.item.viewingTips.find(tip =>
    tip.grade === props.gradeTag && tip.subject === props.mainCategory
  );
  
  return foundTip ? foundTip.tip : null; // 찾으면 팁 텍스트, 못찾으면 null
});

// [핵심] '기본 설명'과 '맞춤 팁'을 하나로 합친 텍스트 (내부 임시 변수)
const fullText = computed(() => {
  // [API 필요] item.description 필드
  let baseDesc = props.item.description || "제공된 설명이 없습니다.";
  
  if (personalizedTip.value) {
    // 팁이 존재하면, 기본 설명 뒤에 예쁘게 붙여줍니다.
    baseDesc += `\n\n✨ ${props.gradeTag} ${props.mainCategory} 맞춤 팁!\n${personalizedTip.value}`;
  }
  
  return baseDesc;
});

// --- 요청하신 변수명 사용 ---
const isExpanded = ref(false);
const TRUNCATE_LENGTH = 100; // '더보기' 기준 글자 수 (이 값은 조절하셔도 됩니다)

// '더보기' 버튼이 필요한지 계산
const isLongText = computed(() => {
  return fullText.value.length > TRUNCATE_LENGTH;
});

// '더보기' 상태에 따라 표시할 텍스트 계산
const displayedDescription = computed(() => {
  if (isExpanded.value || !isLongText.value) {
    return fullText.value; // 전체 텍스트
  }
  // 접힌 상태면, 자르고 '...' 추가
  return fullText.value.substring(0, TRUNCATE_LENGTH) + '... ';
});

// '더보기'/'접기' 버튼 클릭 시
const toggleDescription = () => {
  isExpanded.value = !isExpanded.value;
};
// --- 요청하신 변수명 끝 ---

</script>

<style scoped>
/* === 전시정보 섹션 스타일 === */
.info-section {
  flex: auto;
  flex-direction: column;
  padding: 8px 20px;
  background-color: white;
}

.image-area {
  position: relative;
  height: 250px;
  width: 100%;
  aspect-ratio: 16 /9;
  background-color: #f0f0f0;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /*컨테이너에 꽉차게*/
}

/* (3) 방문 인증 스탬프 (활성/비활성 공통) */
.visit-stamp {
  position: absolute;
  /* ▼▼▼ [수정] 정중앙 배치 ▼▼▼ */
  top: 18%;
  left: 11%;
  /* ▲▲▲ [수정] 정중앙 배치 ▲▲▲ */
  transform: translate(-50%, -50%); 

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none; 
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: rgba(230, 255, 230, 0.8); 
  
  /* 텍스트에도 공통 스타일 적용 (필요시) */
  font-size: 14px;
  font-weight: bold;
}

.visit-stamp i {
  font-size: 28px; /* 아이콘 크기 (bi-클래스는 font-size로 조절) */
  margin-bottom: 4px;
  color: #008a00; /* 활성 아이콘 색 */
}
/* ▲▲▲ [수정] img -> i (아이콘) ▲▲▲ */

/* span (텍스트) */
.visit-stamp span {
  color: #008a00; /* 활성 텍스트 색 */
}

/* (4) 미인증 스탬프 (회색 처리) */
.visit-stamp.inactive {
  background-color: rgba(238, 238, 238, 0.8);
}

/* ▼▼▼ [수정] img -> i (비활성 아이콘) ▼▼▼ */
.visit-stamp.inactive i {
  color: #9e9e9e; /* 비활성 아이콘 색 */
}
/* ▲▲▲ [수정] img -> i (비활성 아이콘) ▲▲▲ */

.visit-stamp.inactive span {
  color: #9e9e9e; /* 비활성 텍스트 색 */
}

.content-body {
  padding-top: 8px;
  padding-bottom: 8px;
}

.title {
  /* 태그와 텍스트를 가로로 배치 */
  display: flex;
  /* 가로 중앙 정렬 */
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 8px;
}

.rating {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 8px;
  flex-wrap: wrap;
}

.stars {
  color: #FFC107;
  margin-right: 8px;
  font-size: 12px;
}

.score {
  display: flex;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.hashtags-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}


/* 태그들을 감싸는 컨테이너 스타일 */
.subject-tags-container {
  display: flex;
  gap: 8px;
  margin-left: 10px;
}

.btn {
  font-size: 13px;
  padding: 0px;
}

/* ======================================= */
/* ▼▼▼ [신규] 층별 지도 + 맞춤 설명 스타일 ▼▼▼ */
/* ======================================= */

/* 신규 섹션을 기존 콘텐츠와 구분하기 위한 여백 */
.floor-map-section {
  margin-top: 8px; 
  padding-top: 8px; 
  border-top: 1px solid #f0f0f0; /* 연한 구분선 */
}

/* 층별 지도 컨테이너 */
.map-container img {
  width: 100%;
  object-fit: contain; /* 이미지 비율 유지 */
  max-height: 400px;
  border-radius: 8px; /* 부트스트랩 rounded-3와 유사 */
}

/* 지도 이미지가 없을 때의 플레이스홀더 */
.map-placeholder {
  width: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border: 1px dashed #ddd;
  border-radius: 8px;
  color: #aaa;
  font-weight: 500;
}

/* [유지] 설명 + 팁이 들어갈 텍스트 영역 (새 버전) */
.description {
  font-size: 0.95rem; /* 15px */
  line-height: 1.7; /* 줄 간격을 넉넉하게 */
  color: #333;
  
  /* [중요] \n 줄바꿈 문자를 CSS에서 그대로 표시 */
  white-space: pre-wrap; 
  
  transition: max-height 0.3s ease-out;
  position: relative;
  word-break: keep-all; /* 단어 단위로 줄바꿈 */
}

/* (접힌 상태) '더보기' 상태일 때 */
.description:not(.expanded) {
  /* 약 4줄 높이 (1.7 * 15px * 4줄 = 약 102px) */
  max-height: 102px; 
  overflow: hidden;
}

/* '더보기'/'접기' 버튼 스타일 (요청하신 .btn-white 클래스) */
.description .btn-white {
  background: none;
  border: none;
  color: #888;
  font-weight: bold;
  padding: 0 0 0 4px;
  font-size: 0.9rem;
  cursor: pointer;
  display: inline; 
}

/* 부트스트랩 'row' 사용 시 모바일에서 여백 제거 */
.row {
  margin-left: 0;
  margin-right: 0;
}
</style>