<template>
  <section class="review-section">
    <h3 class="section-title">{{ sectionTitle }}</h3>

    <div class="review-summary">
      <span class="review-stars" v-html="getFilledStars(rating)">
        </span>
      <span class="total-score">{{ rating.toFixed(1) }}</span>
      <span class="review-count">({{ reviewCount }}개 리뷰)</span>
    </div>

    <h4 class="subsection-title">사진 후기</h4>
    <div class="photo-reviews">
      <button class="photo-btn">후기1</button>
      <button class="photo-btn">후기2</button>
      <button class="photo-btn">후기3</button>
      
      <button class="photo-btn more-btn" v-if="photoReviewCount > 0">+{{ photoReviewCount }}</button>
      <button class="photo-btn more-btn" v-else>사진 없음</button>
    </div>

    <button class="write-review-btn" @click="$emit('show-modal')">
      <i class="bi bi-pencil-square"></i> 후기작성
    </button>

    <div class="review-list">
      <div class="review-item" v-for="review in reviews" :key="review.id">
        <div class="reviewer-profile">
          <img :src="review.avatar" alt="프로필 이미지" class="avatar">
          <span class="name">{{ review.name }}</span>
          
          <span class="stars" v-html="getFilledStars(review.stars)"></span>
          
        </div>
        
        <p class="review-content">{{ review.content }}</p>
        <!-- 10.22 추가 : '도움됐어요' 버튼 작동 -->
        <div class="review-meta">
          <span class="date">{{ review.date }}</span>
          <span class="likes" @click="toggleLike(review.id, review.likes)">
            <i :class="getLikeIcon(review.id)"></i> 도움됐어요 {{ review.likes }}
          </span>
        </div>
        
        <button class="more-options" @click="toggleReportMenu(review.id)"><i class="bi bi-three-dots"></i></button>
        
        <!-- 10.22 추가사항 : 신고하기 버튼 -->
        <div class="report-menu" v-if="openReportMenuId === review.id">
          <button class="report-btn" @click="reportReview(review.id)"><i class="bi bi-bell"></i> 신고하기</button>
        </div>

      </div>
    </div>
  </section>
</template>

<script>
  export default {
    name: 'ReviewSection',
    // 부모 컴포넌트로부터 reviews 배열을 props로 받습니다.
    props: {
      // 🚀 추가: 리뷰 목록 데이터
      reviews: {
        type: Array,
        required: true,
        default: () => []
      },
      // 🚀 추가: 현재 페이지의 평균 평점 (예: 4.7)
      rating: {
        type: Number,
        required: true,
        default: 0
      },
      // 🚀 추가: 전체 리뷰 개수 (예: 516)
      reviewCount: {
        type: Number,
        required: true,
        default: 0
      },
      // 🚀 추가: 사진 후기 개수 (예: 513)
      photoReviewCount: {
        type: Number,
        required: true,
        default: 0
      },
      // 🚀 추가: 현재 페이지가 '장소'인지 '전시'인지 구분하기 위한 플래그
      isPlace: {
        type: Boolean,
        required: false,
        default: false
      }
    },
    // 이 컴포넌트가 부모에게 'show-modal' 이벤트를 발생시킨다는 것을 명시합니다.
    emits: ['show-modal'],
    
    // 🚀 추가: 계산된 속성 (별점과 제목을 동적으로 처리)
    computed: {
      // 현재 페이지 유형에 따라 섹션 제목을 결정합니다.
      sectionTitle() {
        return this.isPlace ? '장소 후기' : '방문자 후기';
      }
    },
    // 10.22 추가 
    data() {
      return {
        // 현재 '신고하기' 메뉴가 열려 있는 리뷰의 ID를 저장
        // 초기값 null
        openReportMenuId: null,

        // 각 리뷰별 좋아요 상태를 저장하는 객체. 
        // key = review.id / value = 좋아요 여부(boolean)
        likedStatus: {}
      };
    },
    methods: {
      // 별점 개수에 맞춰 채워진 별 아이콘을 표시하는 함수 (재사용성 향상)
      getFilledStars(score) {
        const fullStars = Math.floor(score);
        const halfStar = score % 1 >= 0.5 ? 1 : 0;
        const emptyStars = 5 - fullStars - halfStar;
        
        let starsHtml = '';
        for (let i = 0; i < fullStars; i++) {
          starsHtml += '<i class="bi bi-star-fill"></i>';
        }
        if (halfStar) {
          starsHtml += '<i class="bi bi-star-half"></i>';
        }
        for (let i = 0; i < emptyStars; i++) {
          starsHtml += '<i class="bi bi-star"></i>';
        }
        return starsHtml;
      },
      // 10.22 추가 : '더보기' 버튼 클릭 시 호출되는 메소드
      toggleReportMenu(reviewId) {
        // 현재 열려있는 메뉴의 ID가 클릭된 리뷰의 ID와 같다면
        if (this.openReportMenuId === reviewId) {
          this.openReportMenuId = null; // null로 설정하여 닫음.
        } else {
          // 다르다면, 클릭된 리뷰의 ID로 설정하여 해당 메뉴를 오픈
          this.openReportMenuId = reviewId;
        }
      },
      // 10.22 추가 : '신고하기' 버튼 클릭 시 호출되는 임시 메소드
      reportReview(reviewId) {
        // 실제로는 서버에 신고를 요청하는 API 호출 로직이 들어감.
        console.log(`리뷰 ID: ${reviewId}를 신고합니다.`);
        alert(`리뷰 ID: ${reviewId}가 신고되었습니다.`);

        // 신고 후 메뉴를 닫음
        this.openReportMenuId = null;
      },
      // 10.22 추가 : 좋아요 상태에 따라 아이콘 클래스를 반환하는 메소드
      getLikeIcon(reviewId) {
        // reviewId에 해당하는 상태가 true면 채워진 아이콘, false거나 없으면 비어있는 아이콘 반환
        return this.likedStatus[reviewId] ? 'bi bi-hand-thumbs-up-fill' : 'bi bi-hand-thumbs-up';
      },
      // 10.22 추가 : 좋아요 상태를 토글하고, 좋아요 수를 업데이트하는 메소드
      toggleLike(reviewId) {
        // 1. 현재 좋아요 상태를 확인
        const isLiked = this.likedStatus[reviewId];

        // 2. 상태 반전
        // Vue는 배열의 인덱스나 객체의 속성을 직접 수정하면 반응성을 잃을 수 있어 $set을 사용.
        this.likedStatus[reviewId] = !isLiked;

        // 3. 리뷰 데이터의 좋아요 수 업데이트
        const review = this.reviews.find(r => r.id === reviewId);
        if (review) {
          if(isLiked) {
            // 좋아요를 취소하는 경우
            review.likes -= 1;
          } else {
            // 좋아요를 누르는 경우
            review.likes += 1;
          }
        }

        console.log(`리뷰 ID: ${reviewId}, 좋아요 상태: ${!isLiked ? '좋아요' : '취소'}`);
      }
    },
    // 10.22 추가 : 컴포넌트가 생성될 때 모든 리뷰에 대한 초기 좋아요 상태를 설정
    created() {
      // reviews 배열을 순회하며, 모든 리뷰의 초기 좋아요 상태를 false로 설정
      this.reviews.forEach(review => {
        // Vue의 반응성을 위해 $set을 사용하여 객체 속성을 추가
        this.likedStatus[review.id] = false;
      });
    }
  };
</script>

<style scoped>
/* === 1-5. 방문자 후기 섹션 스타일 === */
.review-section {
  padding: 15px;
  background-color: white;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
}

.review-summary {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.review-stars {
  color: #FFC107;
  margin-right: 8px;
  font-size: 24px; /* 총 평점 별점 크기 */
}

.total-score {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-right: 8px;
}

.review-count {
  font-size: 14px;
  line-height: 1.2;
  color: #999;
}

.subsection-title {
  font-size: 16px;
  font-weight: bold;
  margin: 16px 0 16px;
}

.photo-reviews {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
}

.photo-btn {
  background-color: #f0f0f0;
  color: #666;
  border: none;
  padding: 0;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  flex-basis: 25%;
  width: 0;
  aspect-ratio: 1 / 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.more-btn {
  background-color: #ddd;
  font-weight: bold;
}

.write-review-btn {
  width: 100%;
  background-color: #4A89F3; /* 버튼 색상 통일 */
  color: white;
  border: none;
  padding: 12px 0;
  border-radius: 48px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-bottom: 32px;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 16px 0;
  position: relative;
}

.reviewer-profile {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 8px;
  background-color: #ddd;
}

.name {
  font-weight: bold;
  font-size: 14px;
  margin-right: 8px;
}

.review-item .stars {
  color: #FFC107;
  font-size: 14px;
}

.review-content {
  font-size: 14px;
  font-weight: 400;
  color: #333;
  line-height: 1.4;
  margin: 5px 0 10px 50px;
}

.review-meta {
  display: flex;
  font-size: 12px;
  line-height: 1.2;
  color: #999;
  margin-left: 50px;
}

.review-meta .date {
  margin-right: 8px;
}

.review-meta .likes {
  cursor: pointer;
}

.more-options {
  position: absolute;
  top: 16px;
  right: 0;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

/* 10.22 추가 : 신고하기 메뉴를 위한 스타일 */
.report-menu {
  position: absolute;
  /* '더보기' 버튼의 오른쪽에 위치하도록 합니다. */
  top: 40px; 
  right: 0;
  /* 메뉴가 리뷰 콘텐츠 위에 떠 보이도록 그림자를 추가합니다. */
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  z-index: 10; /* 다른 요소 위에 표시되도록 z-index를 높입니다. */
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.report-btn {
  display: block;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #EB3223;
  cursor: pointer;
  white-space: nowrap; /* 내용이 줄 바꿈되지 않도록 합니다. */
}

.report-btn:hover {
  background-color: #f0f0f0;
}

</style>