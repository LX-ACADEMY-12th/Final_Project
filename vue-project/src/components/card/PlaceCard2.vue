<template>
  <div class="place-card d-flex flex-row bg-white align-items-center px-3 py-3 mb-3 rounded-4 shadow gap-3" style="
    font-family: 'SUIT' , sans-serif"
    @click="onCardClick">

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
          @click.stop="onIconClick">
          <i v-if="props.iconType === 'heart'" class="bi bi-heart-fill fs-5" style="color: red;"></i>
          <i v-else class="bi bi-plus fs-5"></i>
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

// ▼▼▼ 3. 'item-click'을 emit 목록에 추가합니다. ▼▼▼
const emit = defineEmits(['add', 'toggle-favorite', 'item-click']);

// 부모로부터 item 객체 전달받기
const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  iconType: {
    type: String,
    default: 'add' // 기본값은 add
  }
});


// [!!] 1. 이미지 기본 URL 정의
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

// [!!] 2. 이미지 URL을 계산하는 computed 속성 추가
const computedImageUrl = computed(() => {
  // 부모 뷰가 main_image_url을 imageUrl로 매핑해줬다는 가정
  const url = props.item.imageUrl; 
  
  if (url && !url.startsWith('http')) {
    // URL이 있고, http로 시작하지 않으면 (즉, 'exhibition/1.jpg' 이면)
    return IMAGE_BASE_URL + url;
  }
  // URL이 http로 시작하거나, URL이 없으면(null) 그대로 반환
  // (URL이 없으면 엑박 대신 alt 텍스트가 표시됨)
  return url;
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

// [수정 없음] 아이콘 클릭 핸들러
const onIconClick =() => {
  if(props.iconType === 'heart') {
    emit('toggle-favorite');
  } else {
    emit('add');
  }
};

// ▼▼▼ 4. 카드 전체 클릭 시 'item-click'을 emit하는 함수를 추가합니다. ▼▼▼
const onCardClick = () => {
  emit('item-click');
};
</script>


<style scoped>
/* --- 레이아웃의 핵심 부분 --- */

/* [카드 전체]
  'display: flex'를 사용해  | [콘텐츠 영역]으로
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

/* ▼▼▼ [신규] 이미지 컨테이너 스타일 ▼▼▼ */
.image-container {
  position: relative;
  /* 뱃지가 이 태그를 기준으로 위치하도록 설정 */
  width: 149px;
  height: 126px;
  flex-shrink: 0;
}

/* ▼▼▼ [신규] 뱃지 스타일 ▼▼▼ */
.card-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #4A7CEC;
  /* 파란색 배경 */
  color: white;
  padding: 4px 10px;
  font-size: 14px;
  /* 12px */
  font-weight: 600;
  /* 이미지의 둥근 모서리(8px)에 맞춰줍니다. */
  border-top-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 1;
  /* 이미지 위에 표시 */
}

/* 이미지 */
.map-thumbnail {
  width: 100%;
  height: 100%;
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