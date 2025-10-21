<template>
  <div class="course-card" style="font-family: 'SUIT', sans-serif">
    <img :src="item.imageSrc" :alt="item.ExhibitionName" class="map-thumbnail" />

    <div class="content-area">
      <div class="content-header">
        <PillTag :text="item.category" type="subject" />
        <div class="action-icons">
          <i class="bi bi-heart-fill"></i>
        </div>
      </div>
      <div class="title">{{ item.ExhibitionName }}</div>
      <div class="address">{{ item.address }}</div>
    </div>
  </div>
</template>

<script>
// 1. PillTag 컴포넌트를 import 합니다.
// (PillTag.vue 파일의 실제 경로에 맞게 수정하세요)
import PillTag from './PillTag.vue'; // './PillTag.vue' 또는 '@/components/PillTag.vue'

export default {
  name: 'UserLikeCourseCard',
  // 2. 자식 컴포넌트로 PillTag를 등록합니다.
  components: {
    PillTag
  },
  props: {
    item: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
/* --- 레이아웃의 핵심 부분 --- */

/* [카드 전체]
  'display: flex'를 사용해 [이미지] | [콘텐츠 영역]으로
  가로 2단 분리합니다.
*/
.course-card {
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

.course-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* [왼쪽 이미지] */
.map-thumbnail {
  width: 149px;
  height: 126px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
  flex-shrink: 0;
  /* 이미지가 찌그러지지 않도록 함 */
}

/* [오른쪽 콘텐츠 영역]
  'display: flex'와 'flex-direction: column'을 사용해
  [헤더], [제목], [주소]를 세로로 쌓습니다.
  'flex: 1'은 이미지를 제외한 나머지 공간을 모두 차지하게 합니다.
*/
.content-area {
  flex: 1;
  /* 남은 공간 모두 차지 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  /* (중요) 세로(column) 방향이므로 height: 100%가 필요할 수 있으나,
     부모(.course-card)가 align-items: center이므로 필요 없을 수 있습니다.
     만약 title, address가 위로 쏠린다면 height: 100% 또는
     justify-content: center 추가 */
  height: 100%;
  /* 부모 높이(168px)에 맞춰 꽉 채움 */
}

/* [콘텐츠 헤더 (태그 + 아이콘)]
  'display: flex'와 'justify-content: space-between'을 사용해
  [PillTag]는 왼쪽에, [action-icons]는 오른쪽에 배치합니다.
*/
.content-header {
  display: flex;
  justify-content: space-between;
  /* 양쪽 끝으로 밀어냄 */
  align-items: center;
  margin-bottom: 16px;
  width: 100%;
  /* 부모(.content-area) 너비에 맞춤 */
}

/* [아이콘 영역] */
.action-icons {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #dc3545;
  /* 하트 아이콘이므로 빨간색으로 변경 (예시) */
  flex-shrink: 0;
}

/* [제목] */
.title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  /* 'flex-grow: 1'이 있으면 안 됩니다. (다른 요소들을 밀어냄) */
}

/* [주소] */
.address {
  font-size: 14px;
  color: #777;
}
</style>
