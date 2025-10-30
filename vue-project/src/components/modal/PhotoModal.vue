<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      
      <button class="close-btn-corner" @click="close">&times;</button>
      
      <div class="image-wrapper">
        <img 
          v-if="images && images.length > 0" 
          :src="images[currentIndex]" 
          alt="리뷰 사진" 
          class="modal-image"
        />
      </div>

      <p v-if="images && images.length > 1" class="image-counter">
        {{ currentIndex + 1 }} / {{ images.length }}
      </p>
      
      <div v-if="images && images.length > 1" class="button-group">
        <button class="btn btn-nav" @click="prevImage">
          이전
        </button>
        <button class="btn btn-nav" @click="nextImage">
          다음
        </button>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'PhotoModal',
  
  props: {
    show: Boolean, 
    images: {
      type: Array,
      default: () => [] // ⭐️ 기본값을 빈 배열로 설정 (안전)
    },
    startIndex: {
      type: Number,
      default: 0 // ⭐️ 기본값을 0으로 설정 (안전)
    }
  },

  // ⭐️ 부모에게 'close' 이벤트를 보낸다고 명시
  emits: ['close'],

  // ⭐️ 현재 보고 있는 이미지의 인덱스를 관리
  data() {
    return {
      currentIndex: 0
    };
  },

  methods: {
    // ⭐️ 모달 닫기
    close() {
      this.$emit('close');
    },
    
    // ⭐️ 다음 이미지
    nextImage() {
      // (현재 인덱스 + 1) % 전체 이미지 개수
      this.currentIndex = (this.currentIndex + 1) % this.images.length;
    },

    // ⭐️ 이전 이미지
    prevImage() {
      // (현재 인덱스 - 1 + 전체 개수) % 전체 개수 (음수 방지)
      this.currentIndex = (this.currentIndex - 1 + this.images.length) % this.images.length;
    }
  },

  // ⭐️ 모달이 열릴 때(show prop이 true로 바뀔 때)
  watch: {
    show(newVal) {
      if (newVal) {
        // 부모가 "이 인덱스부터 보여줘"라고 한 startIndex로 설정
        this.currentIndex = this.startIndex;
      }
    }
  }
};
</script>

<style scoped>
/* 모달 배경 (화면 전체) */
.modal-overlay {
  /* ⭐️ 부모(#app-container)를 기준으로 꽉 채웁니다 */
  position: absolute; 
  inset: 0; /* top: 0; left: 0; right: 0; bottom: 0; */
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 모달 본문 (흰색 박스) */
.modal-content {
  position: relative; /* 닫기 버튼 기준 */
  background: white;
  padding: 20px;
  border-radius: 16px;
  
  /* ⭐️ 부모(412px) 너비의 90% */
  width: 90%; 
  /* ⭐️ 최대 너비는 400px (안전 장치) */
  max-width: 400px; 
  
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
}

/* 닫기 버튼 (우측 상단) */
.close-btn-corner {
  position: absolute;
  top: 10px;
  right: 15px;
  background: none;
  border: none;
  font-size: 28px;
  line-height: 1;
  color: #aaa;
  cursor: pointer;
  padding: 0;
  z-index: 2; /* 이미지 위로 */
}

/* 이미지를 감싸는 영역 */
.image-wrapper {
  width: 100%;
  margin-bottom: 16px;
}

/* 모달에 표시될 이미지 */
.modal-image {
  width: 100%;
  height: auto; /* 비율 유지 */
  
  /* ⭐️ vh 대신 px 고정값 사용 (레이아웃 안정) */
  max-height: 400px; 
  
  object-fit: contain;
  border-radius: 8px;
}

/* 이미지 카운터 (예: 1 / 3) */
.image-counter {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

/* 버튼 그룹 (가로 배치) */
.button-group {
  display: flex;
  flex-direction: row;
  gap: 10px;
}

.btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  flex-grow: 1;
}

/* '이전/다음' 버튼 스타일 */
.btn-nav {
  background-color: #f0f0f0;
  color: #333;
}

.btn-nav:hover {
  background-color: #e0e0e0;
}
</style>