<template>
  <div class="photo-grid-page">

    <PhotoReviewHeader />

    <div class="scroll-content">

      <div v-if="isLoading" class="loading-container">
        <p>사진을 불러오는 중입니다...</p>
      </div>

      <div v-else-if="photos.length > 0" class="photo-grid-container">
        <div v-for="(photoUrl, index) in photos" :key="index" class="photo-item" @click="openModal(index)">
          <img :src="photoUrl" alt="후기 사진" />
        </div>
      </div>

    </div>

    <PhotoModal :show="photoModal.visible" :images="photoModal.images" :startIndex="photoModal.startIndex"
      @close="photoModal.visible = false" />
  </div>
</template>

<script>
import axios from 'axios';
import PhotoReviewHeader from '@/components/header/PhotoReviewHeader.vue';
import PhotoModal from '@/components/modal/PhotoModal.vue';
import eventBus from '@/utils/eventBus';

const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080';

export default {
  name: 'ReviewPhotoGridView',

  components: {
    PhotoReviewHeader,
    PhotoModal,
  },

  // ⭐️ (추가) router/index.js의 'props: true' 옵션으로 URL 파라미터를 받습니다.
  props: {
    targetType: {
      type: String,
      required: true
    },
    targetId: {
      type: [String, Number],
      required: true
    }
  },

  data() {
    return {
      // (수정) currentId와 pageType이 props로 대체되었으므로 data에서 제거합니다.
      photos: [],
      isLoading: true,
      // photoModal 상태관리
      photoModal: {
        visible: false,
        images: [],
        startIndex: 0
      }
    };
  },

  created() {
    // ⭐️ (수정) 라우터 props를 직접 확인합니다.
    if (this.targetId && this.targetType) {
      this.fetchReviewPhotos();
    } else {
      console.error('targetId 또는 targetType이 props로 전달되지 않았습니다.');
      eventBus.emit('show-global-alert', {
        message: '잘못된 접근입니다.',
        type: 'error'
      });
      this.isLoading = false;
    }
  },

  methods: {
    /**
    * (수정) 백엔드 API에 맞춰 URL 및 호출 방식 수정
    */
    async fetchReviewPhotos() {
      this.isLoading = true;

      // ⭐️ (수정) 우리가 만든 백엔드 API 엔드포인트
      const apiUrl = `${API_BASE}/api/reviews/target/${this.targetType}/${this.targetId}/photos`;

      // (수정) params 객체는 더 이상 필요 없으므로 삭제합니다.

      try {
        // ⭐️ (수정) URL 자체에 모든 정보가 포함되어 있으므로 params 객체 없이 호출
        const res = await axios.get(apiUrl);

        this.photos = res.data;

        console.log(`✅ [ReviewPhotoGridView] ${this.targetType}/${this.targetId}의 사진 ${res.data.length}개 로드 완료.`);

      } catch (error) {
        console.error('후기 사진 전체 목록 조회 실패:', error);
        eventBus.emit('show-global-alert', {
          message: '사진을 불러오지 못했습니다.',
          type: 'error'
        });
        this.photos = [];
      } finally {
        this.isLoading = false;
      }
    },

    // 이미지 클릭 시 모달을 여는 메서드
    openModal(clickedIndex) {
      // 1. 모달에 띄울 이미지 목록 = 현재 페이지의 전체 사진(this.photos)
      this.photoModal.images = this.photos;
      // 2. 모달이 시작될 인덱스 = 내가 클릭한 사진의 인덱스(clickedIndex)
      this.photoModal.startIndex = clickedIndex;
      // 3. 모달 보이기
      this.photoModal.visible = true;
    }
  }
}
</script>

<style scoped>
/* (스타일은 동일하므로 수정 없음) */
.photo-grid-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
}

.scroll-content {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  padding: 16px;
  background-color: #fff;

  &::-webkit-scrollbar {
    display: none;
  }

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.loading-container {
  padding: 60px 20px;
  text-align: center;
  color: #888;
  font-size: 16px;
}

.photo-grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.photo-item {
  position: relative;
  width: 100%;
  padding-top: 100%;
  background-color: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.photo-item img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}
</style>
