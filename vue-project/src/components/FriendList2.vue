<template>
  <li class="friend-item">
    <div class="profile-image-placeholder">
      <svg width="24" height="24" fill="#BDBDBD" viewBox="0 0 16 16">
        <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
      </svg>
    </div>
    <span class="friend-name">{{ friend.name }}</span>
    <button class="unfollow-button">팔로우 취소</button>
    <div class="location-icon">
      <svg width="20" height="20" fill="#212121" viewBox="0 0 16 16">
        <path d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10zm0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6z"/>
      </svg>
    </div>
  </li>
</template>


<script setup>
defineProps({
  friend: {
    type: Object,
    required: true
  }
});
</script>


<style scoped>
.friend-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.profile-image-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.friend-name {
  font-size: 16px;
  font-weight: 500;
  color: #212121;
  flex-grow: 1;
  border-bottom: 1px solid #eee;
  padding-bottom: 4px;
  margin-right: 16px;
}

.unfollow-button {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background-color: #000000;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  margin-right: 12px;
  white-space: nowrap;
}

.location-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
</style>
