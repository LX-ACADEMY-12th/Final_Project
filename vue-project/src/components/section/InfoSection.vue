<template>
  <section class="info-section" v-if="item.title">
    <div class="top-meta">
      <div class="image-area">
        <img :src="mainImageSrc" alt="메인 이미지" class="main-image">
      </div>
    </div>

    <div class="content-body">
      <h2 class="title">{{ item.title }}</h2>
      <div class="rating">
        <span class="stars">
          <i class="bi bi-star-fill"></i>
          <i class="bi bi-star-fill"></i>
          <i class="bi bi-star-fill"></i>
          <i class="bi bi-star-fill"></i>
          <i class="bi bi-star-half"></i>
        </span>
        <span class="score">
          <span class="rating-value">{{ item.rating }}점</span>
          <span class="review-count-value">({{ item.reviewCount }})</span>
        </span>

        <!-- 과목 태그 -->
        <div v-if="subjectTags && subjectTags.length > 0" class="subject-tags-container">
          <PillTag v-for="(tag, index) in subjectTags" :key="index" :text="tag" type="subject" />
        </div>

      </div>

      <div v-if="hashTags.length > 0" class="hashtags-area">
        <Hashtag v-for="(tag, index) in hashTags" :key="'hash-' + index" :text="tag" />
      </div>

      <div class="description" :class="{ 'expanded': isExpanded }">
        {{ displayedDescription }}
        <button v-if="isLongText" @click="toggleDescription" class="btn btn-white">
          {{ isExpanded ? '접기' : '(더보기)' }}
        </button>
      </div>



    </div>
  </section>
</template>

<script>
// ✨ 1. SubjectTag 컴포넌트를 가져옵니다.
import PillTag from '@/components/tag/PillTag.vue';
import Hashtag from '@/components/HashTag.vue';

export default {
  name: 'InfoSection',

  // ✨ 2. components에 SubjectTag를 등록하여 템플릿에서 사용할 수 있게 합니다.
  components: {
    PillTag,
    Hashtag,
  },

  // isExpanded: 전시 소개 글이 펼쳐져있나 확인하는 변수
  data() {
    return {
      isExpanded: false, // 기본값은 '접힌' 상태

      hashTags: ['빛과 파동'],
    };
  },

  // 부모 컴포넌트로부터 exhibition과 place 객체를 props로 받습니다.
  props: {
    // 전시 상세화면에서 사용
    exhibition: {
      type: Object,
      required: false, // 장소 상세화면에서는 이 props를 넘기지 않아도 되므로 false로 설정합니다.
      default: () => null, // 기본값을 null로 설정하여 데이터가 없는 상태를 명확히 합니다.
    },
    // 장소 상세화면에서 사용
    place: {
      type: Object,
      required: false,
      default: () => null,
    },

    // ✨ 3. subject-tags prop을 추가합니다. (ExhibitionDetailWithModal에서 전달받음)
    subjectTags: {
      type: Array, // 배열 형태로 과목 이름들을 받습니다.
      required: false,
      default: () => [], // 기본값은 빈 배열로 설정합니다.
    },
  },

  // 계산된 속성(Computed Properties)을 사용하여 템플릿에서 사용할 데이터를 통합합니다.
  computed: {
    // 1. 실제로 화면에 표시할 메인 이미지 URL을 결정합니다.
    mainImageSrc() {
      // 만약 exhibition 데이터가 있다면, 그 이미지(exhibition.mainImage)를 사용하고,
      // place 데이터가 있다면, 그 이미지(palce.mainImage)를 사용하며,
      // 둘 다 없다면 기본 이미지 URL을 사용합니다.
      return this.exhibition?.mainImage || this.place?.mainImage || 'https://via.placeholder.com/600x400';
    },
    // 2. 실제로 화면에 표시할 핵심 데이터를 결정합니다.
    // 이렇게 하면 템플릿 코드가 훨씬 단순해집니다.
    item() {
      // exhibition 데이터가 있다면, exhibition 객체를 사용하고,
      // 없다면 place 객체를 사용합니다.
      return this.exhibition || this.place || {};
    },
    // 3. 현재 표시하는 정보가 '전시 정보'인지 판단합니다.
    isExhibition() {
      // exhibition props가 null이 아닌 경우 true를 반환합니다.
      return !!this.exhibition;
    },
    // ✨ 4. 현재 표시하는 정보가 '장소 정보'인지 판단하는 속성을 추가합니다.
    isPlace() {
      return !!this.place;
    },
    // ✨ 5. 태그를 표시해야 하는지 판단하는 새로운 속성을 추가합니다.
    shouldShowTags() {
      // 전시 정보이거나 OR 장소 정보일 때 true를 반환합니다.
      return this.isExhibition || this.isPlace;
    },

    // 원본 텍스트가 150자봗 긴지 확인하는 computed
    isLongText() {
      return this.item.description && this.item.description.length > 150;
    },

    // 화면에 표시될 텍스트를 결정하는 computed
    displayedDescription() {
      // 텍스트가 짧거나(isLongText: false),
      // 또는 "더보기"가 눌린 상태(isExpanded: true)라면 원본 텍스트 반환
      if (!this.isLongText || this.isExpanded) {
        return this.item.description;
      }

      // 텍스트가 길고 "더보기"가 안 눌린 상태면 150자까지 자르고 "..." 붙이기
      return this.item.description.substring(0, 150) + '...';
    }
  },

  methods: {
    toggleDescription() {
      this.isExpanded = !this.isExpanded;
    }
  }
};
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
  height: 194px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-body {
  padding-top: 8px;
  padding-bottom: 8px;
}

.title {
  font-size: 20px;
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 8px;
}

.rating {
  display: flex;
  align-items: center;
  margin-bottom: 16px;

  /* ✨ 태그와 별점/점수 영역 사이에 공간이 필요하다면 gap을 활용합니다. */
  gap: 8px;
  /* flex-wrap: wrap; 을 추가하면 화면이 좁아질 때 태그들이 자동으로 다음 줄로 넘어갑니다. */
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
  /* 해시태그가 여러 줄로 표시될 수 있도록 flex-wrap을 적용합니다. */
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.description {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  line-height: 1.4;
  /* 줄바꿈 문자 처리 */
  white-space: pre-line;
}

/* ✨ 태그들을 감싸는 컨테이너 스타일 */
.subject-tags-container {
  /* 태그들이 여러 개일 때 옆으로 나란히 배치되도록 합니다. */
  display: flex;
  /* 태그들 사이에 간격을 줍니다. */
  gap: 8px;
  /* 태그 컨테이너가 별점/점수 영역에서 너무 멀리 떨어지지 않도록 마진을 조정할 수 있습니다. */
  margin-left: 10px;
}

.btn {
  font-size: 13px;
  padding: 0px;
}
</style>
