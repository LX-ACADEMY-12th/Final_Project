<template>
  <div class="border rounded-4 shadow-sm p-3" style="flex-shrink: 0; width: 340px;">

    <div class="place-card-content d-flex flex-row align-items-center gap-3 bg-white rounded-4 mt-2"
      @click="onItemClick">

      <div class="image-frame rounded-3 d-flex align-items-center justify-content-center flex-shrink-0">
        <img v-if="computedImageUrl" :src="computedImageUrl" alt="전시 이미지" class="place-image rounded-3"
          style="width: 100%; height: 100%; object-fit: cover;">
      </div>

      <div class="content-frame d-flex flex-column flex-grow-1 gap-2 min-w-0">
        <div class="d-flex justify-content-between align-items-center gap-1">
          <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
            <PillTag :text="item.subject" type="subject" />
            <PillTag :text="item.grade.replace('초등 ', '')" type="grade" />
          </div>
        </div>
        <div class="text-frame d-flex flex-column gap-1 min-w-0">
          <div class="d-flex align-items-center gap-1 min-w-0">
            <!-- <TypeTag :text="item.type" class="flex-shrink-0" /> -->
            <h5 class="fw-bold m-0 text-truncate flex-grow-1 min-w-0">{{ item.title }}</h5>
          </div>
          <div class="d-flex flex-row align-items-center gap-2 min-w-0">
            <span class="text-truncate flex-grow-1 min-w-0">{{ item.place }}</span>
          </div>
          <div class="hashtag-container">
            <HashTag v-for="tag in visibleHashtags" :key="tag" :text="tag" />
            <span v-if="hasMoreHashtags" class="more-tags">
              +{{ remainingHashtagsCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <h6 class="fw-bold small mb-2">방문자 후기</h6>

      <div v-if="item.latestReview">
        <div class="d-flex gap-3">
          <div class="flex-shrink: 0">
            <img :src="item.latestReview.authorProfileImageUrl || 'https://placehold.co/48x48/e0e0e0/333?text=U'"
              class="rounded-circle" style="width: 48px; height: 48px; object-fit: cover;">
          </div>

          <div class="flex-grow-1 min-w-0 overflow-hidden">
            <div class="fw-bold">{{ item.latestReview.authorName }}님</div>

            <div class="d-flex align-items-center mb-2" style="color: #FFC107;">
              <i v-for="i in 5" :key="i" class="bi" :class="i <= item.latestReview.rating ? 'bi-star-fill' : 'bi-star'"
                :style="{ color: i <= item.latestReview.rating ? '#FFC107' : '#e0e0e0' }"></i>
            </div>
            <p class="text-truncate">"{{ item.latestReview.content }}"</p>
          </div>
        </div>

        <div v-if="computedReviewPhotos.length > 0" class="d-flex gap-2 mt-2">

          <div v-for="i in 5" :key="i" class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
            style="background-color: #e0e0e0; height: 60px; /* 높이 살짝 줄임 */ flex-basis: 0; flex-grow: 1; overflow: hidden;">

            <img v-if="computedReviewPhotos[i - 1]" :src="computedReviewPhotos[i - 1]" alt="리뷰 사진"
              style="width: 100%; height: 100%; object-fit: cover;">
          </div>
        </div>
      </div>

      <div v-else class="text-secondary text-center p-4"
        style="font-size: 0.9rem; background-color: #f8f9fa; border-radius: 8px;">
        등록된 리뷰가 없습니다.
      </div>
    </div>

  </div>
</template>

<script setup>
import PillTag from '@/components/tag/PillTag.vue';
import TypeTag from '@/components/tag/TypeTag.vue';
import HashTag from '@/components/tag/HashTag.vue';
import { computed } from 'vue';

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['add', 'item-click']);

// [!!] 1. 이미지 기본 URL 정의
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

// [!!] 2. 메인 이미지 URL 계산
const computedImageUrl = computed(() => {
  // HomeView는 mainImageUrl을 수정해서 보내줍니다.
  const url = props.item.mainImageUrl || props.item.imageUrl;
  if (url && !url.startsWith('http')) {
    return IMAGE_BASE_URL + url;
  }
  return url;
});

// [!!] 3. 리뷰 사진 URL 목록 계산
const computedReviewPhotos = computed(() => {
  if (props.item.latestReview?.photoUrls) {
    return props.item.latestReview.photoUrls.map(url => {
      // HomeView가 이미 photoThumbnails로 수정했지만, 혹시 모르니 여기서도 처리
      // (이중 확인)
      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url;
    });
  }
  return [];
});


const maxHashtags = 2;
const visibleHashtags = computed(() => {
  return props.item.hashtags?.slice(0, maxHashtags) || [];
});
const hasMoreHashtags = computed(() => {
  return props.item.hashtags?.length > maxHashtags;
});
const remainingHashtagsCount = computed(() => {
  return props.item.hashtags?.length - maxHashtags || 0;
});

const onItemClick = () => {
  emit('item-click', props.item);
}
</script>

<style scoped>
/* (스타일은 변경사항 없습니다) */
.border.rounded-4.shadow-sm.p-3 {
  background-color: var(--bs-body-bg, #fff);
}

.btn-add-new {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
  padding: 0;
  background-color: white;
}

.place-card-content {
  z-index: 1;
  overflow: hidden;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
}

.place-card-content:hover {
  background-color: #f8f9fa !important;
}

.image-frame {
  width: 80px;
  height: 80px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.place-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-frame {
  min-width: 0;
  max-width: calc(100% - 80px - 12px);
  min-height: 0;
}

.text-frame {
  min-width: 0;
  min-height: 0;
}

.hashtag-container {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  flex-wrap: nowrap;
  overflow: hidden;
  min-width: 0;
}

.more-tags {
  font-size: 0.7rem;
  color: #6c757d;
  white-space: nowrap;
  flex-shrink: 0;
}

.text-truncate {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
