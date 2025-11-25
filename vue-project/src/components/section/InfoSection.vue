<template>
  <section class="info-section" v-if="item.title">
    <div class="top-meta">
      <div class="image-area">
        <img :src="mainImageSrc" alt="메인 이미지" class="main-image">
        <!-- 이미 방문한 상태 -->
        <div v-if="item.visited" class="visit-stamp" @click="onAuthButtonClick">
          <i class="bi bi-postage-heart-fill"></i>
          <span>방문 인증</span>
        </div>
        <!-- 방문 전 상태 -->
        <div v-else class="visit-stamp inactive" @click="onAuthButtonClick">
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

      <div class="exhibition-section" v-if="formattedExhibitions.length > 0">
        <hr class="divider-line" />

        <div class="exhibition-header">
          <div class="header-left">
            <i class="bi bi-palette-fill"></i>
            <span class="exhibition-title">교과 연계 추천 전시</span>
            <span class="exhibition-count">({{ exhibitionCount }}개)</span>
          </div>
        </div>
        <div class="carousel-wrapper">
          <div class="carousel-container">
            <div class="carousel-track">

              <div v-for="(exhibition, index) in formattedExhibitions" :key="index" class="exhibition-card">
                <div class="card-header-area">
                  <span class="card-number">{{ index + 1 }}</span>
                  <span class="card-title">{{ exhibition.title }}</span>
                </div>

                <div class="card-image-area">
                  <img :src="exhibition.imgSrc" alt="전시 이미지" class="card-img" />
                </div>
              </div>

            </div>
          </div>
          <div class="scroll-hint"></div>
        </div>

      </div>

      <div class="simulation-accordion" v-if="currentSimulationComponent">

        <button type="button" class="btn-simulation-toggle" @click="showSimulation = !showSimulation"
          :class="{ 'is-open': showSimulation }" :aria-expanded="showSimulation">
          <div class="simulation-header-left">
            <i class="bi bi-flask"></i>
            <span>전시물 과학 원리 체험</span>
          </div>
          <i class="bi chevron-icon" :class="showSimulation ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </button>

        <transition name="slide-fade">
          <div v-if="showSimulation" class="simulation-content-wrapper">
            <div class="simulation-inner">
              <div class="simulation-content">
                <component :is="currentSimulationComponent"></component>
              </div>
            </div>
          </div>
        </transition>
      </div>

    </div>
  </section>
</template>

<script>
// ✨ 1. SubjectTag 컴포넌트를 가져옵니다.
import PillTag from '@/components/tag/PillTag.vue';
import Hashtag from '@/components/tag/HashTag.vue';
// import TypeTag from '@/components/tag/TypeTag.vue';
import ColumnarJoint from '@/components/simulations/ColumnarJoint.vue';
import StatesOfMatter from '@/components/simulations/StatesOfMatterSimulation.vue';
import Ecosystem from '@/components/simulations/EcosystemSimulation.vue';
import MagnetField from '@/components/simulations/MagnetField.vue';
import ThermalConductivity from '@/components/simulations/ThermalConductivitySim.vue';
import ElectricShow from '@/components/simulations/ElectricShow.vue';
import MagneticTrain from '@/components/simulations/MagneticTrain.vue';
import ElectroClothes from '@/components/simulations/ElectroClothes.vue';
import AlternatorShow from '@/components/simulations/AlternatorShow.vue';
import DigitalPhysicssShow from '../simulations/DigitalPhysicssShow.vue';
import CentrifugalBike from '../simulations/CentrifugalBike.vue';
import FireTool from '../simulations/FireTool.vue';

// [!!] 1. 이미지 기본 URL 정의
const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';

export default {
  name: 'InfoSection',

  // ✨ 2. components에 SubjectTag를 등록하여 템플릿에서 사용할 수 있게 합니다.
  components: {
    PillTag,
    Hashtag,
    // TypeTag,
    ColumnarJoint,
    StatesOfMatter,
    Ecosystem,
    MagnetField,
    ThermalConductivity,
    ElectricShow,
    MagneticTrain,
    ElectroClothes,
    AlternatorShow,
    DigitalPhysicssShow,
    CentrifugalBike,
    FireTool
  },

  emits: ['authenticate-visit'],

  // 컴포넌트 생성 시 props 확인
  created() {
    console.log('✅ [InfoSection] created - 전달받은 Props:', {
      mainCategory: this.mainCategory,
      subCategories: this.subCategories,
      gradeTag: this.gradeTag
    });
  },
  // isExpanded: 전시 소개 글이 펼쳐져있나 확인하는 변수
  data() {
    return {
      isExpanded: false, // 기본값은 '접힌' 상태
      showSimulation: false,
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
    mainCategory: {
      type: String,
      default: '' // HashTag용
    },
    subCategories: {
      type: Array,
      default: () => [] // HashTag용
    },
    gradeTag: {
      type: String,
      default: ''
    }
  },

  // 계산된 속성(Computed Properties)을 사용하여 템플릿에서 사용할 데이터를 통합합니다.
  computed: {

    // ▼▼▼ 추가할 부분 ▼▼▼
    formattedExhibitions() {
      // this.item은 이미 InfoSection에 정의되어 있으므로 바로 사용하면 됩니다.
      const list = this.item.exhibitionList;

      if (!list || !Array.isArray(list)) {
        return [];
      }

      // 상수: 이미지가 없을 때 보여줄 기본 이미지
      const DEFAULT_IMG = 'https://via.placeholder.com/150?text=No+Image';
      const BASE_URL = 'https://storage.googleapis.com/science_book/'; // 버킷 주소

      return list.map(exhibitionString => {
        // 1. '^' 기호로 분리 (백엔드에서 이렇게 묶었으니까요!)
        // parts[0]: 전시명
        // parts[1]: 이미지경로 (있을 수도, 없을 수도 있음)
        const parts = exhibitionString.split('^');

        const title = parts[0];
        let rawImg = parts[1] || ''; // 없으면 빈 문자열

        // 2. 이미지 경로 가공
        // 빈 칸이거나 'NO_IMAGE'면 기본 이미지 사용
        // http로 시작하면 그대로 사용
        // 그 외(content/...)는 버킷 주소 붙여서 사용
        let finalImg = DEFAULT_IMG;

        if (rawImg && rawImg !== 'NO_IMAGE') {
          if (rawImg.startsWith('http')) {
            finalImg = rawImg;
          } else {
            finalImg = BASE_URL + rawImg;
          }
        } else {
          // 전시 이미지가 없으면 -> 부모(전시관)의 메인 이미지를 쓸지, 기본 이미지를 쓸지 선택
          // 여기서는 부모 메인 이미지를 대체재로 사용 (선택사항)
          finalImg = this.mainImageSrc || DEFAULT_IMG;
        }

        // 3. 객체로 리턴 (템플릿에서 .title, .imgSrc로 쓰기 위해)
        return {
          title: title,
          imgSrc: finalImg
        };
      });
    },

    exhibitionCount() {
      return this.formattedExhibitions.length;
    },
    // 1. 실제로 화면에 표시할 메인 이미지 URL을 결정합니다.
    mainImageSrc() {
      // [!!] 2. PlaceCard2.vue와 동일한 로직 적용
      // 부모(PlaceDetailsView)가 DTO를 매핑하며 'mainImage' 필드에 넣어준 값을 가져옵니다.
      const rawUrl = this.exhibition?.mainImage || this.place?.mainImage;

      if (rawUrl && !rawUrl.startsWith('http')) {
        // 'exhibition/1.jpg' 같은 값이면 '앞주소'를 붙여줍니다.
        return IMAGE_BASE_URL + rawUrl;
      }

      // 'http://...'로 시작하거나, null이거나, 'https://via.placeholder...' 같은 fallback이면
      // 그대로 반환합니다.
      return rawUrl || 'https://via.placeholder.com/600x400';
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
    },

    simulationMap() {
      // [!!] 맵의 키를 '전시관명'과 '중분류(해시태그)'로 변경합니다.
      // [!!] 중분류의 '#'는 빼고 텍스트만 키로 사용하는 것이 좋습니다.
      return {
        '창의나래관': { // 예시: '전시관명'
          '전기와 자기': ElectricShow,     // 예시: '중분류'
          '자기': MagnetField,
          '물질의 상태': StatesOfMatter,
          '빛과 파동': StatesOfMatter,
          '힘과 에너지': DigitalPhysicssShow,
          '생명과학과 인간의 생활': StatesOfMatter,
          '생물의 구조와 에너지': StatesOfMatter
        },
        '야외전시': {
          '전기와 자기': MagneticTrain,
          '힘과 에너지': MagneticTrain
        },
        '과학기술관': {
          '전기와 자기': ElectroClothes,
          '힘과 에너지': CentrifugalBike
        },
        '미래기술관': {
          '전기와 자기': AlternatorShow,
          '힘과 에너지': AlternatorShow,
          '빛과 파동': AlternatorShow,
          '물체와 물질': AlternatorShow
        },
        '인류관': {
          '힘과 에너지': FireTool,
          '생태계': Ecosystem
        },
        '어린이과학관': {
          '힘과 에너지': ThermalConductivity,
          '혼합물': Ecosystem
        },
        '천체관': {
          '생명과학과 인간의 생활': StatesOfMatter,
          '혼합물': Ecosystem
        },
        '생물탐구관': {
          '생물의 구조와 에너지': StatesOfMatter,
        }
        // ... (필요한 만큼 추가) ...
      };
    },

    // --- [!!] 3. currentSimulationComponent 로직 수정 ---
    currentSimulationComponent() {
      // 1. [!!] props에서 직접 데이터를 가져옵니다.
      // (this.isPlace가 true면 this.place, false면 this.exhibition)
      const item = this.isPlace ? this.place : this.exhibition;

      // 2. [!!] 'undefined' 에러 방지용 가드 (가장 중요!)
      // (item이 null이거나, title이 아직 '데이터 로딩 중...'일 수 있습니다)
      if (!item || !item.title || item.title === '데이터 로딩 중...') {
        return null; // 데이터 로딩 전이므로 null 반환
      }

      const name = item.title; // e.g., "국립과천과학관"
      const subTagsRaw = item.subCategories; // e.g., ["전기", "발전"]

      if (!subTagsRaw || subTagsRaw.length === 0) {
        console.log(`[Sim Match] '${name}'의 중분류가 없습니다.`);
        return null;
      }

      // 3. 맵에서 '전시관명'으로 1차 조회를 합니다.
      const subMap = this.simulationMap[name];
      if (!subMap) {
        console.log(`[Sim Match] '${name}'에 해당하는 맵이 없습니다.`);
        return null;
      }

      // 4. '중분류 배열'을 순회하며 맵에 일치하는 컴포넌트를 찾습니다.
      // (InfoSection/Hashtag에서 했던 정제 로직을 여기서도 수행)
      const subTags = Array.isArray(subTagsRaw)
        ? subTagsRaw.map(tag => (tag || '').replace(/[{}"]/g, ''))
        : (typeof subTagsRaw === 'string' ? subTagsRaw.split(',').map(s => s.trim().replace(/[{}"]/g, '')) : []);

      for (const tag of subTags) {
        const key = tag; // 이미 '#'가 없는 "전기"

        if (subMap[key]) {
          console.log(`[Sim Match] 성공! '${name}' -> '${key}'`);
          return subMap[key];
        }
      }

      console.log(`[Sim Match] '${name}'은(는) 찾았으나, [${subTags.join(', ')}] 중 일치하는 중분류가 없습니다.`);
      return null;
    },
  },

  methods: {
    toggleDescription() {
      this.isExpanded = !this.isExpanded;
    },
    onAuthButtonClick() {
      console.log(`InfoSection: 버튼 클릭! 부모에게 알림.`);
      this.$emit('authenticate-visit');
    }
  }
};
</script>

<style scoped>
/* === 기본 폰트 및 레이아웃 === */
.info-section {
  flex: auto;
  flex-direction: column;
  padding: 8px 20px;
  background-color: white;
  font-family: 'SUIT', sans-serif;
}

/* ... (상단 이미지, 평점 등 기존 스타일 유지) ... */
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
  display: flex;
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

.subject-tags-container {
  display: flex;
  gap: 8px;
  margin-left: 10px;
}

.description {
  font-size: 14px;
  font-weight: 400;
  color: #333;
  line-height: 1.4;
  white-space: pre-line;
  margin-bottom: 12px;
}

.btn {
  font-size: 13px;
  padding: 0px;
}

/* 방문 인증 스탬프 */
.visit-stamp {
  position: absolute;
  top: 25%;
  left: 11%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: rgba(230, 255, 230, 0.95);
  /* 가독성을 위해 투명도 조절 */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  /* 입체감 추가 */
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  z-index: 10;
}

.visit-stamp i {
  font-size: 28px;
  margin-bottom: 4px;
  color: #008A00;
}

.visit-stamp span {
  color: #008A00;
}

.visit-stamp.inactive {
  background-color: rgba(245, 245, 245, 0.95);
}

.visit-stamp.inactive i,
.visit-stamp.inactive span {
  color: #9E9E9E;
}


/* =========================================
   ★ 교과 연계 추천 전시 (아코디언 & 캐러셀) ★
   ========================================= */

.exhibition-section {
  display: flex;
  flex-direction: column;
  padding-top: 0px;
}

.divider-line {
  border: none;
  height: 1px;
  background-color: #eee;
  margin: 0;
}

/* 1. 헤더 스타일 (클릭 버튼처럼 보이게) */
.exhibition-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  /* 좌우 끝 정렬 */
  padding: 12px 4px;
  /* 클릭 영역 확보 */
  cursor: pointer;
  user-select: none;
  /* 텍스트 드래그 방지 */
  transition: background-color 0.2s;
  border-radius: 8px;
}

.exhibition-header:active {
  background-color: #f5f5f5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.exhibition-header .bi-palette-fill {
  color: #6366f1;
  font-size: 18px;
}

.exhibition-title {
  font-size: 14px;
  color: #333;
}

.exhibition-count {
  color: #888;
  font-size: 14px;
  font-weight: 500;
}

/* 화살표 아이콘 회전 애니메이션 */
.toggle-icon {
  color: #999;
  font-size: 14px;
  transition: transform 0.3s ease;
  /* 부드럽게 회전 */
}

.toggle-icon.rotate {
  transform: rotate(180deg);
  /* 180도 회전 */
}


/* 2. 캐러셀 래퍼 (애니메이션 & 스크롤 영역) */
.carousel-wrapper {
  position: relative;
  overflow: hidden;
  /* 애니메이션 시 패딩이 튀는 것을 방지하기 위해 마진 사용 */
  margin-top: 4px;
}

.carousel-container {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px;
  /* 하단 패딩은 그림자 잘림 방지 */

  /* 스크롤바 숨김 처리 */
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.carousel-container::-webkit-scrollbar {
  display: none;
}

.carousel-track {
  display: flex;
  gap: 12px;
}

/* 3. 카드 디자인 (사진 하단, 제목 상단) */
.exhibition-card {
  /* 카드 크기 고정 */
  width: 150px;
  min-width: 150px;
  flex-shrink: 0;

  display: flex;
  flex-direction: column;

  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

/* 클릭 효과 (옵션) */
.exhibition-card:active {
  transform: scale(0.98);
}

/* 상단: 제목 영역 */
.card-header-area {
  height: 52px;
  /* 높이 고정 (두 줄 고려) */
  padding: 8px 12px;
  display: flex;
  align-items: center;
  /* 상단 정렬 */
  gap: 8px;
  background-color: #fff;
  border-bottom: 1px solid #f3f4f6;

}

.card-number {
  margin-top: 2px;
  /* 텍스트 줄맞춤 미세조정 */
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background: #6366f1;
  color: white;
  border-radius: 50%;
  font-size: 10px;
  font-weight: 800;
  flex-shrink: 0;
}

.card-title {
  font-size: 13px;
  color: #1f2937;
  font-weight: 600;
  line-height: 1.35;
  /* 두 줄 말줄임표 (...) 처리 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: keep-all;
  /* 단어 단위 줄바꿈 */
}

/* 하단: 이미지 영역 */
.card-image-area {
  width: 100%;
  height: 100px;
  /* 이미지 높이 고정 */
  background-color: #f9fafb;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  /* 하단 빈 공간 제거 */
}

/* 오른쪽 그라데이션 힌트 (더 있다는 느낌) */
.scroll-hint {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 24px;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0));
  /* 필요하다면 rgba(255,255,255,0.8) 로 변경 */
  pointer-events: none;
}


/* === 아코디언 애니메이션 (slide-fade) === */
.slide-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 300px;
  /* 펼쳐졌을 때의 대략적인 최대 높이 */
  opacity: 1;
}

.slide-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 300px;
  opacity: 1;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
  transform: translateY(-5px);
}

/* ===========================
   시뮬레이션 아코디언 컨테이너
   → exhibition-section 느낌으로
   =========================== */
.simulation-accordion {
  /* 카드 느낌 제거 */
  background-color: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;

  display: flex;
  flex-direction: column;
  padding-top: 0;
  margin-top: 4px;
  /* exhibition-section의 carousel 아래랑 비슷하게 살짝 띄우기 */
}

/* 헤더 레이아웃: exhibition-header 맞추기 */
.btn-simulation-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  padding: 12px 4px;
  /* exhibition-header 와 동일 */
  background-color: transparent;
  border: none;
  cursor: pointer;
  user-select: none;

  font-size: 14px;
  font-weight: 500;
  color: #333;

  transition: background-color 0.2s;
}

.btn-simulation-toggle:hover {
  background-color: #f5f5f5;
}

.simulation-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.simulation-header-left .bi-flask {
  color: #6366f1;
  /* palette-fill 아이콘이랑 톤 맞추기 */
  font-size: 18px;
}

.simulation-header-left span {
  font-size: 14px;
  color: #333;
}

/* 오른쪽 화살표 아이콘 exhibition-header와 동일 느낌 */
.chevron-icon {
  color: #999;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.btn-simulation-toggle.is-open .chevron-icon {
  transform: rotate(180deg);
}

/* 내용 래퍼: 섹션 안에 자연스럽게 들어가도록 */
.simulation-content-wrapper {
  padding: 0;
  /* exhibition-section의 carousel-wrapper처럼 플랫하게 */
  margin: 0;
  border-top: none;
  /* 카드 구분선 제거 */
  overflow: hidden;
}

/* slide-fade 트랜지션은 그대로 사용해도 되고,
   이미 위에 정의된 걸 쓰고 있다면 중복 정의는 지워도 됨 */

/* 시뮬레이션 내용 카드 자체는 살짝 카드 느낌 유지 */
.simulation-inner {
  background: linear-gradient(135deg,
      rgba(74, 124, 236, 0.05) 0%,
      rgba(16, 185, 129, 0.05) 100%);
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.simulation-content {
  padding: 16px;
  background: #ffffff;
  min-height: 260px;
}
</style>
