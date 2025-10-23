<template>
  <div v-if="show" class="modal-overlay" style="font-family: 'SUIT', sans-serif" @click.self="close">
    <div class="modal-content">
      <div class="header">
        <span class="title">방문할 장소 추가</span>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="content">
        <div class="current-location">
          <i class="bi bi-geo-alt-fill"></i> 현재 위치: 서울특별시 강남구
        </div>

        <div class="search-bar">
          <i class="bi bi-search"></i>
          <input type="text" placeholder="과학관 이름 또는 지역 검색" />
        </div>

        <ul class="place-list">
          <li v-for="place in places" :key="place.id" class="place-item">
            <div class="place-info">
              <span class="place-name">{{ place.name }}</span>
              <span class="place-address">{{ place.address }}</span>
            </div>
            <button class="btn-add" :disabled="place.id === 6" @click="addItem(place)">
              <i v-if="place.id !== 6" class="bi bi-plus"></i>
              {{ place.id === 6 ? '추가됨' : '추가' }}
            </button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddPlaceModal',
  props: {
    show: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['close', 'add-item'],
  data() {
    return {
      // ( ... 스크립트 내용은 동일 ... )
      places: [
        { id: 1, name: '국립과천과학관', address: '경기 과천시 상하벌로 110' },
        { id: 2, name: '대전국립중앙과학관', address: '대전 유성구 대덕대로 481' },
        { id: 3, name: '서울시립과학관', address: '서울 노원구 하계로 288' },
        { id: 4, name: '부산과학기술관', address: '부산 북구 기장읍 동부산관광로 60' },
        { id: 5, name: '광주과학관', address: '광주 북구 첨단과기로 235' },
        { id: 6, name: '인천어린이과학관', address: '인천 계양구 방축로 21' },
      ],
    };
  },
  methods: {
    close() {
      this.$emit('close');
    },
    addItem(place) {
      this.$emit('add-item', place);
    },
  },
};
</script>

<style scoped>
/* [수정]
  모달 오버레이 스타일: align-items를 'center'로 변경
*/
.modal-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 👈 flex-end에서 center로 수정 */
  padding: 20px;
  /* 모바일 화면 여백 */
}

/* [수정]
  .bottom-sheet를 .modal-content로 변경하고
  중앙 모달 스타일로 수정
*/
.modal-content {
  width: 100%;
  max-width: 500px;
  /* 최대 너비 지정 */
  max-height: 85vh;
  /* 최대 높이 지정 */
  background: white;
  border-radius: 16px;
  /* 👈 모든 모서리에 radius 적용 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  /* 그림자 추가 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* 둥근 모서리 적용을 위해 */
}

.header {
  padding: 16px;
  text-align: center;
  position: relative;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

/* [수정]
  content 영역이 스크롤 되도록 overflow-y: auto 추가
*/
.content {
  padding: 16px;
  overflow-y: auto;
  /* 내용이 많아지면 스크롤 */
}

/* ( ... 이하 .content 내부 스타일은 동일 ... ) */

.current-location {
  font-size: 14px;
  color: #555;
  margin-bottom: 16px;
}

.current-location .bi {
  margin-right: 4px;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 16px;
}

.search-bar .bi {
  margin-right: 8px;
  color: #888;
}

.search-bar input {
  border: none;
  background: none;
  outline: none;
  width: 100%;
  font-size: 16px;
}

.place-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.place-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.place-info {
  display: flex;
  flex-direction: column;
}

.place-name {
  font-size: 16px;
  font-weight: 500;
}

.place-address {
  font-size: 13px;
  color: #777;
}

.btn-add {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 18px;
  font-weight: bold;
  cursor: pointer;
  background-color: #5887FF;
  /* (조정 필요) */
  color: white;
}

.btn-add:disabled {
  background-color: #f0f0f0;
  color: #aaa;
  cursor: not-allowed;
}
</style>
