<template>
  <div class="border rounded-4 shadow-sm p-3" style="flex-shrink: 0; width: 340px;">

    <div class="d-flex justify-content-end">
      <button class="btn btn-sm rounded-circle d-flex align-items-center justify-content-center btn-add-new"
        @click.stop="onAddClick">
        <i class="bi bi-plus fs-5"></i>
      </button>
    </div>

    <div class="place-card-content d-flex flex-row align-items-center gap-3 bg-white px-3 py-3 rounded-4 border mt-2"
      @click="onItemClick">
      <div class="image-frame rounded-3 d-flex align-items-center justify-content-center flex-shrink-0">
        <img v-if="item.imageUrl" :src="item.imageUrl" alt="전시 이미지" class="place-image rounded-3">
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
            <TypeTag :text="item.type" class="flex-shrink-0" />
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
      <h6 class="fw-bold small ps-1 mb-2">방문자 후기</h6>
      <div class="d-flex gap-3 px-1">
        <div class="flex-shrink: 0">
          <img src="https://placehold.co/48x48/e0e0e0/333?text=김" class="rounded-circle"
            style="width: 48px; height: 48px;">
        </div>
        <div class="flex-grow-1 min-w-0">
          <div class="fw-bold">김OO 학부모</div>
          <div class="text-secondary small mb-1">체험명</div>
          <div class="d-flex align-items-center mb-2" style="color: #FFC107;">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-star-fill" viewBox="0 0 16 16">
              <path
                d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
            </svg>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-star-fill" viewBox="0 0 16 16">
              <path
                d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
            </svg>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
              class="bi bi-star-fill" viewBox="0 0 16 16">
              <path
                d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
            </svg>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#e0e0e0"
              class="bi bi-star-fill" viewBox="0 0 16 16">
              <path
                d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
            </svg>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#e0e0e0"
              class="bi bi-star-fill" viewBox="0 0 16 16">
              <path
                d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
            </svg>
          </div>
          <p class="text-truncate">"아이가 정말 좋아했어요!"</p>
        </div>
      </div>
      <div class="d-flex gap-2 mt-2 px-1">
        <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
          style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진1</div>
        <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
          style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진2</div>
        <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
          style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">사진3</div>
        <div class="rounded-3 d-flex align-items-center justify-content-center text-secondary fw-bold"
          style="background-color: #e0e0e0; height: 80px; flex-basis: 0; flex-grow: 1;">+24</div>
      </div>
    </div>
    </div>
</template>

<script setup>
// PlaceCard.vue에서 사용하던 컴포넌트들을 그대로 import
import PillTag from '@/components/tag/PillTag.vue';
import TypeTag from '@/components/tag/TypeTag.vue';
import HashTag from '@/components/tag/HashTag.vue';
import { computed } from 'vue';

// HomeView로부터 item 데이터를 prop으로 받습니다.
const props = defineProps({
  item: {
    type: Object,
    required: true
  }
});

// HomeView로 'add'와 'item-click' 이벤트를 전달하기 위해 emit을 정의
const emit = defineEmits(['add', 'item-click']);

// --- PlaceCard.vue에서 가져온 스크립트 로직 ---
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
// ------------------------------------------

// 새 '+' 버튼 클릭 핸들러
const onAddClick = () => {
  emit('add', props.item);
};

// 카드 본체 클릭 핸들러 (이제 안쪽 카드를 클릭해야 호출됨)
const onItemClick = () => {
  emit('item-click');
}
</script>

<style scoped>
/* 1. PlaceReviewCard의 루트 래퍼 스타일 */
.border.rounded-4.shadow-sm.p-3 {
  background-color: var(--bs-body-bg, #fff);
  /* [수정] 루트 래퍼는 클릭 이벤트를 받지 않음 */
  /* cursor: pointer; */
}

/* 2. [수정] '+' 버튼 스타일 (position:absolute 제거) */
.btn-add-new {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
  padding: 0;
  background-color: white; 
}

/* 3. PlaceCard에서 가져온 컨텐츠 영역 스타일 */
.place-card-content {
  z-index: 1;
  overflow: hidden;
  /* [추가] 안쪽 카드에만 클릭 이벤트 적용 */
  cursor: pointer; 
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
  object-fit: contain;
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
</style>