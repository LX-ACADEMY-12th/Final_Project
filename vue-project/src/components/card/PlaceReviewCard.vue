<template>
  <div class="border rounded-4 shadow-sm p-3" style="flex-shrink: 0; width: 340px;">

    <div class="place-card-content d-flex flex-row align-items-center gap-3 bg-white rounded-4 mt-2"
      @click="onItemClick">

      <div class="image-frame rounded-3 d-flex align-items-center justify-content-center flex-shrink-0">
        <img v-if="item.imageUrl" :src="item.imageUrl" alt="전시 이미지" class="place-image rounded-3"
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
      <h6 class="fw-bold small mb-2">방문자 후기</h6>

      <div v-if="item.latestReview">
        <div class="d-flex gap-3">
          <div class="flex-shrink: 0">
            <img :src="item.latestReview.authorProfileImageUrl || 'https://placehold.co/48x48/e0e0e0/333?text=U'"
              class="rounded-circle" style="width: 48px; height: 48px; object-fit: cover;">
          </div>

          <div class="flex-grow-1 min-w-0">
            <div class="fw-bold">{{ item.latestReview.authorName }}님</div>

            <div class="d-flex align-items-center mb-2" style="color: #FFC107;">
              <i v-for="i in 5" :key="i" class="bi" :class="i <= item.latestReview.rating ? 'bi-star-fill' : 'bi-star'"
                :style="{ color: i <= item.latestReview.rating ? '#FFC107' : '#e0e0e0' }"></i>
            </div>
            <p class="text-truncate">"{{ item.latestReview.content }}"</p>
          </div>
        </div>

        <div v-if="item.latestReview.photoUrls && item.latestReview.photoUrls.length > 0" class="d-flex gap-2 mt-2">

          <div v-for="i in 5" :key="i" class="rounded-3 d-flex align-items-center justify-content-center text-secondary"
            style="background-color: #e0e0e0; height: 60px; /* 높이 살짝 줄임 */ flex-basis: 0; flex-grow: 1; overflow: hidden;">

            <img v-if="item.latestReview.photoUrls && item.latestReview.photoUrls[i - 1]"
              :src="item.latestReview.photoUrls[i - 1]" alt="리뷰 사진"
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
// [기존 코드] <script setup> 내용은 수정할 필요가 없습니다.
// prop으로 item을 받는 것은 동일합니다.
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
  emit('item-click', props.item); // [수정] item-click 시에도 item 객체를 전달
}
</script>

<style scoped>
/* [수정] 루트 래퍼 스타일 */
.border.rounded-4.shadow-sm.p-3 {
  background-color: var(--bs-body-bg, #fff);
  /* 모든 자식 요소의 간격/정렬은
    flex, gap, mt/mb 및 루트의 p-3로 제어합니다.
  */
}

/* [수정] '+' 버튼 스타일 */
.btn-add-new {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
  padding: 0;
  background-color: white;
  /* 루트 p-3 내부에 있으므로,
    장소 정보 카드와의 간격은
    장소 정보 카드의 `mt-2` (삭제됨) 대신
    이 버튼이 속한 div의 `margin-bottom`으로 제어할 수 있으나,
    현재 구조(다음 요소가 mt-3)도 괜찮습니다.
  */
}

/* [수정] 장소 정보 컨텐츠 영역 (기존 .place-card-content) */
.place-card-content {
  /* `border`가 제거되어 더 이상 카드-인-카드 구조가 아님.
     `p-3`가 제거되어 부모의 `p-3`를 따름.
  */
  z-index: 1;
  overflow: hidden;
  cursor: pointer;
  transition: background-color 0.15s ease-out;
}

.place-card-content:hover {
  background-color: #f8f9fa !important;
  /* 클릭 가능 영역 피드백 */
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
  /* [수정] contain -> cover */
  object-fit: cover;
}

/* (이하 스타일은 기존과 동일) */
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
