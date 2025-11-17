<template>
  <div class="place-card d-flex flex-row bg-white align-items-center px-3 py-3 rounded-4 shadow gap-3" style="
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

        <button class="btn btn-sm rounded-circle d-flex align-items-center justify-content-center btn-add flex-shrink-0"
          @click.stop="onAddClick"> <i class="bi bi-plus fs-4"></i>
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
</template>

<script setup>
import PillTag from '@/components/tag/PillTag.vue';
import HashTag from '@/components/tag/HashTag.vue';
import { computed } from 'vue';

// [!!] 1. 부모와 통신 (기존 PlaceCard.vue의 emit 유지)
const emit = defineEmits(['add', 'item-click']);

// [!!] 2. Props (기존 PlaceCard.vue의 props 유지)
const props = defineProps({
  item: {
    type: Object,
    required: true
  }
}); // (iconType prop은 제거됨)

// [!!] 3. 이미지 기본 URL 정의
const IMAGE_BASE_URL = 'https://storage.googleapis.com/science_book/';

// [!!] 4. computedImageUrl (기존 PlaceCard.vue의 로직 유지)
// (부모 MapView가 mainImageUrl을 내려주므로 이 로직이 맞습니다)
const computedImageUrl = computed(() => {
  const url = props.item.mainImageUrl || props.item.imageUrl;
  if (url && !url.startsWith('http')) {
    return IMAGE_BASE_URL + url;
  }
  return url;
});

// [!!] 5. 해시태그 로직 (PlaceCard2/PlaceCard 공통)
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

// [!!] 6. 클릭 핸들러 (기존 PlaceCard.vue 로직)
const onAddClick = () => {
  // item 객체 전체를 부모에게 전달
  emit('add', props.item);
};

// [!!] 7. 카드 본체 클릭 핸들러 (기존 PlaceCard.vue 로직)
const onItemClick = () => {
  emit('item-click');
};
</script>

<style scoped>
/* --- [!!] 스타일은 PlaceCard2의 것을 기반으로, 146px 높이에 맞게 수정 --- */

/* [카드 전체] */
.place-card {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  /* 상하 16px 패딩 */
  gap: 15px;
  margin: 0 0 0 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.2s ease;

  /* [!!] 1. 카드 크기는 원본 PlaceCard의 크기(146px)로 고정 */
  width: 330px;
  height: 146px;
  flex-shrink: 0;
}

.place-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* [!!] 2. 이미지 컨테이너 (PlaceCard2 스타일) */
.image-container {
  position: relative;
  /* [!!] 3. 카드 높이(146px) - 패딩(32px) = 114px로 높이 조절 */
  width: 134px;
  /* (비율에 맞게 너비도 살짝 조절) */
  height: 114px;
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
  /* [!!] 4. 카드 높이가 줄었으니, 텍스트 영역도 114px로 높이 제한 */
  height: 114px;
}

.text-frame {
  min-width: 0;
  min-height: 0;
}

/* [!!] 버튼 스타일 (PlaceCard2 스타일 + PlaceCard 테두리) */
.btn-add {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
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
</style>
