<template>
  <div class="border rounded-4 shadow-sm p-3" style="flex-shrink: 0; width: 340px;">

    <div class="place-card d-flex flex-row bg-white align-items-center rounded-4 gap-3" style="
      font-family: 'SUIT' , sans-serif" @click="onItemClick">
      <div class="image-container">
        <div v-if="item.badgeLabel" class="card-badge">
          {{ item.badgeLabel }}
        </div>
        <img :src="computedImageUrl" :alt="item.title" class="map-thumbnail" />
      </div>

      <div class="content-frame d-flex flex-column flex-grow-1 gap-2 min-w-0">
        <div class="d-flex justify-content-between align-items-center gap-1">
          <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
            <PillTag :text="item.subject || ''" type="subject" />
            <PillTag :text="(item.grade || '').replace('초등 ', '')" type="grade" />
          </div>

          <button
            class="btn btn-sm rounded-circle d-flex align-items-center justify-content-center btn-add flex-shrink-0 ps-4"
            @click.stop="onAddClick">
            <i class="bi bi-plus fs-4"></i>
          </button>
        </div>

        <div class="text-frame d-flex flex-column gap-1 min-w-0">
          <div class="d-flex align-items-center gap-1 min-w-0">
            <h5 class="fw-bold m-0 text-truncate flex-grow-1 min-w-0">{{ item.title || '' }}</h5>
          </div>
          <div class="d-flex flex-row align-items-center gap-2 min-w-0">
            <span class="text-truncate flex-grow-1 min-w-0">{{ item.place || '' }}</span>
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
            style="background-color: #e0e0e0; height: 60px; flex-basis: 0; flex-grow: 1; overflow: hidden;">
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
import HashTag from '@/components/tag/HashTag.vue';
import { computed } from 'vue';

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
});

// [!!] 'add' emit 추가 (HomeView가 사용)
const emit = defineEmits(['add', 'item-click']);

// [!!] 1. 이미지 기본 URL 정의
const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';

// [!!] 2. 메인 이미지 URL 계산 (기존 로직 유지)
const computedImageUrl = computed(() => {
  const url = props.item.mainImageUrl || props.item.imageUrl;
  if (url && !url.startsWith('http')) {
    return IMAGE_BASE_URL + url;
  }
  return url;
});

// [!!] 3. 리뷰 사진 URL 목록 계산 (기존 로직 유지)
const computedReviewPhotos = computed(() => {
  if (props.item.latestReview?.photoUrls) {
    return props.item.latestReview.photoUrls.map(url => {
      if (url && !url.startsWith('http')) {
        return IMAGE_BASE_URL + url;
      }
      return url;
    });
  }
  return [];
});

// [!!] 4. 해시태그 로직 (기존 로직 유지)
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

// [!!] 5. 카드 본체 클릭 (기존 로직 유지 - item 객체 전달)
const onItemClick = () => {
  emit('item-click', props.item);
}

// [!!] 6. '+' 버튼 클릭 핸들러 (새로 추가)
const onAddClick = () => {
  emit('add', props.item);
}
</script>

<style scoped>
/* [카드 전체] */
.place-card {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  /* [!!] 부모가 p-3(12px)을 갖고 있으므로, 이 카드의 padding은 12px 정도로 줄임 */
  padding: 8px;
  gap: 15px;
  cursor: pointer;
  transition: box-shadow 0.2s ease;

  /* [!!] 높이는 146px로 고정 */
  height: 146px;
  flex-shrink: 0;

  /* [!!] width는 330px이 아닌 부모(340px)의 패딩(p-3)을 뺀 100%로 설정 */
  width: 100%;
  margin: 0;
}

.place-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* [!!] 2. 이미지 컨테이너 (PlaceCard2 스타일) */
.image-container {
  position: relative;
  /* [!!] 3. 카드 높이(146px) - 패딩(24px) = 122px로 높이 조절 */
  width: 134px;
  /* (너비는 134px 유지) */
  height: 122px;
  /* 114px -> 122px (패딩이 12px로 줄었으므로) */
  flex-shrink: 0;
}

/* [!!] 뱃지 (PlaceCard2 스타일) */
.card-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #4A7CEC;
  color: white;
  padding: 4px 10px;
  font-size: 14px;
  font-weight: 600;
  border-top-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 1;
}

/* [!!] 이미지 (PlaceCard2 스타일) */
.map-thumbnail {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
  flex-shrink: 0;
}

/* [!!] 콘텐츠 영역 (PlaceCard2 스타일) */
.content-frame {
  min-width: 0;
  min-height: 0;
  height: 122px;
  /* 이미지 높이와 동일하게 */
}

.text-frame {
  min-width: 0;
  min-height: 0;
}

/* [!!] 버튼 스타일 (PlaceCard2 스타일 + PlaceCard 테두리) */
.btn-add {
  width: 28px;
  height: 28px;
}

/* [!!] 해시태그 컨테이너 (PlaceCard2 스타일) */
.hashtag-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: nowrap;
  overflow: hidden;
  min-width: 0;
}

/* [!!] '+N' 태그 (PlaceCard2 스타일) */
.more-tags {
  font-size: 0.7rem;
  color: #6C757D;
  white-space: nowrap;
  flex-shrink: 0;
}

/* [!!] 3. 후기 부분에 필요한 스타일 (원본) */
.text-truncate {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
