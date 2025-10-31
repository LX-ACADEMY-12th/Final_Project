<template>
  <div class="place-card d-flex flex-row bg-white align-items-center px-3 py-3 rounded-4 shadow gap-3" style="
    font-family: 'SUIT' , sans-serif">
    <!-- 이미지  -->
    <img :src="item.imageUrl" :alt="item.title" class="map-thumbnail" />
    <!-- 컨텐츠 프레임 -->
    <div class="content-frame d-flex flex-column flex-grow-1 gap-2 min-w-0">
      <!-- 첫 줄 프레임 -->
      <div class="d-flex justify-content-between align-items-center gap-1">
        <!-- 알약 태그 프레임 -->
        <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
          <PillTag :text="item.subject || ''" type="subject" />
          <PillTag :text="(item.grade || '').replace('초등 ', '')" type="grade" />
        </div>
        <!-- 더보기 버튼 -->
        <button class="btn btn-sm rounded-circle d-flex align-items-center justify-content-center btn-add flex-shrink-0"
          @click="onAddClick">
          <i class="bi bi-plus fs-5"></i>
        </button>
      </div>
      <!-- 섹션 프레임 -->
      <div class="text-frame d-flex flex-column gap-1 min-w-0">
        <!-- 첫 줄 프레임 -->
        <div class="d-flex align-items-center gap-1 min-w-0">
          <!-- 상설 및 기획 태그 -->
          <TypeTag :text="item.type || ''" class="flex-shrink-0" />
          <!-- 전시명 -->
          <h5 class="fw-bold m-0 text-truncate flex-grow-1 min-w-0">{{ item.title || '' }}</h5>
        </div>
        <!-- 두번째 줄 프레임 -->
        <div class="d-flex flex-row align-items-center gap-2 min-w-0">
          <!-- 전시관명 -->
          <span class="text-truncate flex-grow-1 min-w-0">{{ item.place || '' }}</span>
        </div>
        <!-- 세번째 줄 프레임 -->
        <div class="hashtag-container">
          <!-- 해시태그 반복 -->
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
import TypeTag from '@/components/tag/TypeTag.vue';
import HashTag from '@/components/tag/HashTag.vue';

import { computed } from 'vue';
const emit = defineEmits(['add']);

// 부모로부터 item 객체 전달받기
const props = defineProps({
  item: {
    type: Object,
    required: true
    /* item 객체 예시:
      {
        imageUrl: 'https://example.com/some-image.jpg',
        grade: '초등 3학년',
        subject: '과학',
        type: '상설',
        place: '국립중앙과학관',
        title: '과학 탐험대',
        hashtags: ['대전', '체험', '교육', '재미있는', '학습']
      }
    */
  }
});

// 1. 최대 2개의 해시태그만 표시
const maxHashtags = 2;

// 2. 2개만 잘라서 보여줄 해시태그 목록
const visibleHashtags = computed(() => {
  return props.item.hashtags?.slice(0, maxHashtags) || [];
});

// 3. 더 많은 해시태그가 있는지 여부
const hasMoreHashtags = computed(() => {
  return props.item.hashtags?.length > maxHashtags;
});

// 4. 숨겨진 해시태그의 개수
const remainingHashtagsCount = computed(() => {
  return props.item.hashtags?.length - maxHashtags || 0;
});

const onAddClick = () => {
  emit('add');
};

</script>


<style scoped>
/* --- 레이아웃의 핵심 부분 --- */

/* [카드 전체]
  'display: flex'를 사용해 [이미지] | [콘텐츠 영역]으로
  가로 2단 분리합니다.
*/
.place-card {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  gap: 15px;
  margin: 0 0 0 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.2s ease;
  height: 168px;
  /* 카드 높이 고정 */
}

.place-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* 이미지 */
.map-thumbnail {
  width: 149px;
  height: 126px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
  /* 이미지가 찌그러지지 않도록 함 */
  flex-shrink: 0;
}

/* 콘텐츠 영역 */
.content-frame {
  /* 최대 너비 제한으로 오버플로우 방지 */
  min-width: 0;
  /* 이미지 너비 + gap 제외 */
  max-width: calc(100% - 80px - 12px);
  min-height: 0;
}

.text-frame {
  min-width: 0;
  min-height: 0;
}

/* 버튼 스타일 */
.btn-add {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
}

/* 해시태그 컨테이너 */
.hashtag-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: nowrap;
  overflow: hidden;
  min-width: 0;
}

/* '+N' 태그 스타일 추가 */
.more-tags {
  font-size: 0.7rem;
  /* 폰트 크기 (HashTag와 유사하게) */
  color: #6c757d;
  /* 텍스트 색상 (Bootstrap secondary) */
  white-space: nowrap;
  /* 줄바꿈 방지 */
  flex-shrink: 0;
  /* 찌그러짐 방지 */
}
</style>
