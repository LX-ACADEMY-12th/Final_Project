<template>
  <div class="course-card" style="font-family: 'SUIT', sans-serif">
    <!-- 지도 -->
    <img :src="item.imageUrl" :alt="item.ExhibitionName" class="map-thumbnail" />
    <div class="content-area">
      <div class="content-header">
        <!-- 알약 태그 프레임 -->
        <div class="d-flex flex-row gap-2 flex-shrink-1 min-w-0">
          <PillTag :text="item.subject" type="subject" />
          <PillTag :text="item.grade" type="grade" />
        </div>
        <!-- 찜 아이콘 -->
        <div class="action-icons">
          <i class="bi bi-heart-fill"></i>
        </div>
      </div>
      <!-- 타이틀 -->
      <div class="title">{{ item.ExhibitionName }}</div>
      <!-- 위치 -->
      <div class="address">{{ item.address }}</div>
      <!-- 코스 순서 -->
      <div class="course-list">{{ courseSequenceText }}</div>
    </div>
  </div>
</template>

<script>
// PillTag 컴포넌트를 import 합니다.
import PillTag from '@/components/tag/PillTag.vue';

export default {
  name: 'UserLikeCourseCard',
  // 자식 컴포넌트로 PillTag를 등록합니다.
  components: {
    PillTag
  },
  props: {
    item: {
      type: Object,
      required: true
      /* [중요] item 객체에 coursePlaces 배열이 필요합니다.
        item: {
          ...
          ExhibitionName: '국립중앙과학관 코스',
          address: '대전 유성구',
          subject: '과학',
          grade: '초등',
          coursePlaces: ['자연사관', '과학기술관', '미래기술관', '천체관']
        }
      */
    }
  },
  computed: {
    /**
     * coursePlaces 배열을 "장소1 → 장소2 → 장소3" 형태의 문자열로 변환
     */
    courseSequenceText() {
      if (!Array.isArray(this.item.coursePlaces) || this.item.coursePlaces.length === 0) {
        return '코스 정보가 없습니다.'; // 또는 빈 문자열 ''
      }
      return this.item.coursePlaces.join(' → '); // '→' 아이콘으로 연결
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

.course-list {
  font-size: 0.8rem;
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
  margin-bottom: 4px;
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
