<template>
  <div>
    <!-- 게시물 작성버튼 -->
    <CreatePostFAB />

    <div class="feed-list">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import PostCard from '@/components/PostCard.vue'; // PostCard 컴포넌트를 불러옵니다.
import CreatePostFAB from '@/components/CreatePostFAB.vue';

// 실제 앱에서는 API를 통해 받아올 게시물 데이터 예시입니다.
const posts = ref([
  {
    id: 1,
    author: '차민정',
    avatar: null, // 아바타 이미지 URL
    timestamp: '2시간 전',
    text: '안녕하세요 텍스트 게시글 테스트입니다. 만나서 반갑습니다. 안녕하세요. 텍스트 게시글 테스트입니다.',
    imageUrl: null,
    likes: 32,
    comments: 6,
  },
  {
    id: 2,
    author: '차민정',
    avatar: null,
    timestamp: '5시간 전',
    text: null,
    imageUrl: 'https://images.unsplash.com/photo-1579264444789-7a54164165c4?q=80&w=2574&auto=format&fit=crop',
    likes: 128,
    comments: 21,
  },
  {
    id: 3,
    author: '차민정',
    avatar: null,
    timestamp: '1일 전',
    text: '멋진 풍경과 함께하는 하루! 📸',
    imageUrl: 'https://images.unsplash.com/photo-1506748686214-e9df14d4d9d0?q=80&w=2574&auto=format&fit=crop',
    likes: 256,
    comments: 45,
  },
]);
</script>

<style scoped>
.feed-list {
  display: flex;
  flex-direction: column;
}
</style>
