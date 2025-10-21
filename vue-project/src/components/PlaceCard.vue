<template>
  <div class="venue-card d-flex flex-row bg-white align-items-start p-3 rounded-4 shadow">

    <div class="icon-frame rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
      :style="{ 'background-color': item.iconBgColor }">
      <i :class="item.iconClass" class="fs-1" :style="{ 'color': item.iconColor, 'opacity': 0.6 }"></i>
    </div>

    <div class="content-frame d-flex flex-column flex-grow-1">
      <div class="d-flex justify-content-between align-items-start">
        <div class="d-flex flex-row" style="gap: 6px;">

          <PillTag :text="item.subject" type="subject" />

          <PillTag :text="item.grade" type="grade" />

        </div>
        <button class="btn btn-sm rounded-circle d-flex align-items-center justify-content-center btn-add"
          @click="onAddClick">
          <i class="bi bi-plus fs-5"></i>
        </button>
      </div>

      <div class="d-flex flex-column" style="gap: 6px;">
        <h5 class="fw-bold m-0">{{ item.place }}</h5>
        <div class="d-flex flex-row align-items-center" style="gap: 6px">

          <TypeTag :text="item.type" />

          <span>{{ item.title }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 태그 컴포넌트들 임포트
import PillTag from './PillTag.vue';
import TypeTag from './TypeTag.vue';

// [!!] 1. 'add' 이벤트를 부모에게 전달하기 위해 defineEmits를 추가합니다.
const emit = defineEmits(['add']);

// [!!] 2. 버튼 클릭 시 'add' 이벤트를 발생시키는 함수
const onAddClick = () => {
  emit('add');
};

// 부모(Home.vue)로부터 item 객체를 통째로 받음
defineProps({
  item: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
/* 기존 인라인 스타일을 클래스로 분리 */
.venue-card {
  gap: 16px;
  width: 300px;
  height: 137px;
  z-index: 10;
  flex-shrink: 0;
}

.icon-frame {
  width: 80px;
  height: 80px;
  margin-top: 8px;
}

.content-frame {
  gap: 10px;
  margin-top: 8px;
}

.btn-add {
  width: 28px;
  height: 28px;
  border: 1.5px solid #C6C6C8;
}
</style>
