<template>
  <article class="post-card">
    <div class="post-avatar">
      <img :src="post.authorAvatar" :alt="`${post.author}의 프로필 사진`" class="avatar-image">
    </div>

    <div class="post-main">
      <header class="post-header">
        <span class="post-author">{{ post.author }}</span>
        <div class="post-meta">
          <span class="post-timestamp">{{ post.timestamp }}</span>
          <div class="dropdown dropstart" ref="dropdownRef">

            <button class="btn p-0" type="button" @click="toggleDropdown" aria-label="게시물 옵션">
              <i class="bi bi-three-dots"></i>
            </button>

            <ul v-if="isDropdownVisible" class="dropdown-menu show">
              <li><button class="dropdown-item" @click="handleEdit">수정</button></li>
              <li><button class="dropdown-item text-danger" @click="handleDelete">삭제</button></li>
            </ul>

          </div>
        </div>
      </header>

      <div class="post-body">
        <img v-if="post.imageUrl" :src="post.imageUrl" class="post-image" alt="게시물 이미지">
        <p v-if="post.text" class="post-text">{{ post.text }}</p>
      </div>

      <footer class="post-footer">
        <button type="button" class="post-action" :class="{ 'liked': isLiked }" @click="toggleLike">
          <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
          <span>{{ likeCount }}</span>
        </button>
        <button type="button" class="post-action">
          <i class="bi bi-chat"></i>
          <span>{{ post.comments }}</span>
        </button>
      </footer>
    </div>
  </article>
</template>

<script setup>
// Vue의 반응성 시스템과 생명주기 훅을 가져옵니다.
import { ref, onMounted, onUnmounted } from 'vue';

// defineProps: 부모 컴포넌트로부터 전달받을 props를 정의합니다.
const props = defineProps({
  post: {
    type: Object,
    required: true,
    // 데이터 예시를 명시하면 다른 개발자가 이해하기 쉽습니다.
    // default: () => ({ id: 1, author: '작성자', ... })
  }
});

// defineEmits: 부모 컴포넌트로 이벤트를 보내기 위해 emit 함수를 정의합니다.
// 이 컴포넌트가 어떤 이벤트를 발생시키는지 명확하게 알려주는 역할을 합니다.
const emit = defineEmits(['edit-post', 'delete-post']);

// --- 상태(State) 관리 ---

// 드롭다운 메뉴의 표시 여부를 제어하는 상태
const isDropdownVisible = ref(false);
// '좋아요' 여부를 제어하는 상태
const isLiked = ref(false);
// '좋아요' 개수를 제어하는 상태 (props를 직접 수정하지 않기 위함)
const likeCount = ref(props.post.likes);
// 드롭다운 DOM 요소를 참조하기 위한 ref
const dropdownRef = ref(null);


// --- 함수(Methods) 정의 ---

/**
 * 드롭다운 메뉴의 표시 상태를 토글(on/off)합니다.
 */
const toggleDropdown = () => {
  isDropdownVisible.value = !isDropdownVisible.value;
};

/**
 * '좋아요' 상태를 토글하고, 개수를 조절합니다.
 */
const toggleLike = () => {
  isLiked.value = !isLiked.value;
  likeCount.value += isLiked.value ? 1 : -1; // 삼항연산자로 더 간결하게 표현
};

/**
 * '수정' 버튼 클릭 시 부모에게 'edit-post' 이벤트를 전달합니다.
 * 게시물 id를 함께 보내 어떤 게시물을 수정할지 알려줍니다.
 */
const handleEdit = () => {
  emit('edit-post', props.post.id);
  isDropdownVisible.value = false; // 메뉴를 닫아줍니다.
};

/**
 * '삭제' 버튼 클릭 시 부모에게 'delete-post' 이벤트를 전달합니다.
 */
const handleDelete = () => {
  emit('delete-post', props.post.id);
  isDropdownVisible.value = false; // 메뉴를 닫아줍니다.
};

/**
 * 문서 전체의 클릭 이벤트를 감지하여 드롭다운 외부를 클릭했는지 확인합니다.
 * @param {Event} event - 클릭 이벤트 객체
 */
const handleClickOutside = (event) => {
  // 드롭다운이 열려있고, 클릭된 영역이 드롭다운 메뉴(.dropdownRef)의 일부가 아닐 경우
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isDropdownVisible.value = false; // 드롭다운을 닫습니다.
  }
};


// --- 생명주기(Lifecycle) 훅 ---

// onMounted: 컴포넌트가 DOM에 마운트된 직후 실행됩니다.
onMounted(() => {
  // 전역 document 객체에 클릭 이벤트 리스너를 추가합니다.
  document.addEventListener('click', handleClickOutside);
});

// onUnmounted: 컴포넌트가 DOM에서 언마운트되기 직전 실행됩니다.
onUnmounted(() => {
  // 컴포넌트가 사라질 때 이벤트 리스너를 제거하여 메모리 누수를 방지합니다.
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* 이전과 동일한 스타일 */
.post-card {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0 12px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.post-avatar {
  width: 40px;
  height: 40px;
  background-color: #e2e8f0;
  border-radius: 50%;
  grid-column: 1;
  grid-row: 1 / span 3;
}

/* 아바타 영역에 실제 이미지가 들어갈 경우를 대비한 스타일 */
.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.post-main {
  grid-column: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-author {
  font-weight: bold;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: #6c757d;
}

.post-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-text {
  margin: 0;
  line-height: 1.5;
  word-break: break-all;
}

.post-image {
  width: 100%;
  height: auto;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid #f0f0f0;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* div에서 button으로 변경되었습니다.
  기본 버튼 스타일을 초기화하여 기존 디자인을 유지합니다.
*/
.post-action {
  /* 버튼 기본 스타일 리셋 */
  background: none;
  border: none;
  padding: 0;
  font: inherit;

  /* 기존 스타일 */
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6c757d;
  cursor: pointer;

  /* 수정된 부분: transition 속도 조절 및 transform 추가
    너무 긴 transition(2.0s)은 사용자에게 답답함을 줄 수 있어 0.3s로 수정했습니다.
    transform 속성도 transition에 추가하여 hover 시 확대/축소 효과가 부드럽게 적용되도록 합니다.
  */
  transition: color 0.3s ease-in-out, transform 0.3s ease-in-out;
}

.post-action:not(.liked):hover {
  transform: scale(1.1);
}

.post-action.liked,
.post-action.liked .bi-heart-fill {
  color: #dc3545;
  font-weight: bold;
}

.dropdown {
  position: relative;
}

/* 드롭다운 메뉴 내의 버튼 스타일 */
.dropdown-menu .dropdown-item {
  cursor: pointer;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  left: auto;
  /* 다른 요소에 가려지지 않도록 z-index 추가 */
  z-index: 10;
}
</style>
