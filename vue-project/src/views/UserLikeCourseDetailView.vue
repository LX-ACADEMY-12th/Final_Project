<template>
  <div class="course-recommend-container" style="font-family: 'SUIT', sans-serif">
    <!-- 헤더 -->
    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left" style="flex: 1;">
        <i class="bi bi-arrow-left fs-5" style="cursor: pointer;" @click="goBack"></i>
      </div>
      <div class="header-center fw-bold fs-6" style="flex: 1; text-align: center;">
        관심 코스
      </div>
      <div class="header-right" style="flex: 1;">
      </div>
    </div>

    <CourseMap :items="courseItems" class="map-area" />

    <div class="course-root-name">
      <span>
        {{ exhibitionName || '코스 로딩 중...' }}
      </span>
    </div>

    <div class="course-add-btn" v-if="pageType === 'place'">
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-plus"></i> 경로추가
      </button>
    </div>

    <div class="scrollable-content">
      <div class="course-list-container">
        <div v-if="pageType === 'exhibition'">
          <CourseExhibitionCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
            couseType="전시" @edit="handleEdit" @delete="handleDelete" />
        </div>

        <div v-else-if="pageType === 'place'">
          <CoursePlaceEditCard v-for="course in courseItems" :key="course.id" :item="course" :showControls="true"
            couseType="답사" @edit="handleEdit" @delete="handleDelete" />
        </div>

        <div v-else>
          <p>코스 상세 정보를 불러오는 중입니다....</p>
        </div>

      </div>
    </div>

    <ConfirmDeleteModal :show="showDeleteModal" @confirm="confirmDeleteItem" @close="closeDeleteModal" />

    <AddPlaceModal :show="showAddModal" @add-item="addNewItem" @close="closeAddModal" />

  </div>
</template>

<script>
import ConfirmDeleteModal from '@/components/modal/ConfirmDeleteModal.vue';
import AddPlaceModal from '@/components/modal/AddPlaceModal.vue';
import CourseMap from '@/components/map/CourseMap.vue';
import CourseExhibitionCard from '@/components/card/CourseExhibitionPlaceCard.vue';
import CoursePlaceEditCard from '@/components/card/CoursePlaceEditCard.vue';

// 목록 화면과 동일한 데이터 사용 (실제로는 API에서 가져올 데이터)
const getUserLikeCourseData = () => {
  return [
    {
      id: 1,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '전시명1',
      address: '국립과천과학관',
      coursePlaces: ['전시명1', '전시명2', '전시명3'],
      type: '전시',
      courseItems: [
        {
          id: 1,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '습지생물코너',
          type: '상설',
          place: '국립중앙과학관 자연사관',
          hashtags: ['항상성과 몸의 조절', '생명과학과 인간의 생활'],
          lat: 36.3758,
          lng: 127.3845
        },
        {
          id: 2,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '4학년',
          title: '빛의 원리',
          type: '기획',
          place: '국립과천과학관',
          hashtags: ['파동', '빛', '물리1', '체험'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 3,
          number: 3,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '5학년',
          title: '미래 에너지',
          type: '상설',
          place: '서울시립과학관',
          hashtags: ['에너지', '화학 반응', '미래 기술'],
          lat: 37.6094,
          lng: 127.0706
        }
      ]
    },
    {
      id: 2,
      imageUrl: 'https://placehold.co/600x400',
      subject: '화학',
      grade: '3학년',
      ExhibitionName: '전시명2',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 4,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '3학년',
          title: '화학 실험실',
          type: '상설',
          place: '국립과천과학관 화학관',
          hashtags: ['화학 반응', '실험'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 5,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '화학',
          grade: '3학년',
          title: '분자 모형 전시',
          type: '상설',
          place: '국립과천과학관 분자관',
          hashtags: ['분자', '화학 구조'],
          lat: 37.4360,
          lng: 126.9750
        }
      ]
    },
    {
      id: 3,
      imageUrl: 'https://placehold.co/600x400',
      subject: '물리',
      grade: '3학년',
      ExhibitionName: '전시명3',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 6,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '3학년',
          title: '물리 체험관',
          type: '상설',
          place: '국립과천과학관 물리관',
          hashtags: ['역학', '물리 체험'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 7,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '3학년',
          title: '전기 실험실',
          type: '상설',
          place: '국립과천과학관 전기관',
          hashtags: ['전기', '전자기학'],
          lat: 37.4365,
          lng: 126.9748
        },
        {
          id: 8,
          number: 3,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '물리',
          grade: '3학년',
          title: '자기장 체험',
          type: '상설',
          place: '국립과천과학관 자기관',
          hashtags: ['자기장', '전자기학'],
          lat: 37.4368,
          lng: 126.9752
        }
      ]
    },
    {
      id: 4,
      imageUrl: 'https://placehold.co/600x400',
      subject: '생명',
      grade: '3학년',
      ExhibitionName: '생명과학 탐험',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 9,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '생명',
          grade: '3학년',
          title: '생명의 기원',
          type: '상설',
          place: '국립과천과학관 생명관',
          hashtags: ['생명의 기원', '진화'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 10,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '생명',
          grade: '3학년',
          title: 'DNA 모형',
          type: '상설',
          place: '국립과천과학관 유전자관',
          hashtags: ['DNA', '유전자'],
          lat: 37.4366,
          lng: 126.9749
        }
      ]
    },
    {
      id: 5,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '지구과학 여행',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 11,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '지구의 구조',
          type: '상설',
          place: '국립과천과학관 지구관',
          hashtags: ['지구 구조', '지질학'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 12,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '화석 전시관',
          type: '상설',
          place: '국립과천과학관 화석관',
          hashtags: ['화석', '고생물학'],
          lat: 37.4370,
          lng: 126.9755
        }
      ]
    },
    {
      id: 6,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '천체 관측',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 13,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '천체 투영관',
          type: '상설',
          place: '국립과천과학관 천체관',
          hashtags: ['천체', '천문학'],
          lat: 37.4363,
          lng: 126.9746
        }
      ]
    },
    {
      id: 7,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '우주 탐험',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 14,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '우주선 모형',
          type: '상설',
          place: '국립과천과학관 우주관',
          hashtags: ['우주', '우주선'],
          lat: 37.4363,
          lng: 126.9746
        },
        {
          id: 15,
          number: 2,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '달 탐사',
          type: '상설',
          place: '국립과천과학관 달관',
          hashtags: ['달', '우주 탐사'],
          lat: 37.4372,
          lng: 126.9758
        }
      ]
    },
    {
      id: 8,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '환경 보호',
      address: '국립과천과학관',
      type: '전시',
      courseItems: [
        {
          id: 16,
          number: 1,
          color: '#e53e3e',
          imageUrl: 'https://placehold.co/600x400',
          subject: '지구',
          grade: '3학년',
          title: '환경 오염',
          type: '상설',
          place: '국립과천과학관 환경관',
          hashtags: ['환경', '오염'],
          lat: 37.4363,
          lng: 126.9746
        }
      ]
    },
    {
      id: 9,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '장소명1',
      address: '장소명1 주소',
      type: '답사',
      courseItems: [
        {
          id: 17,
          number: 1,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '해운대 해변',
          type: '답사',
          place: '부산시 해운대구 해운대해변로',
          hashtags: ['고체지구', '유체지구', '천체'],
          lat: 35.1587,
          lng: 129.1604
        },
        {
          id: 18,
          number: 2,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '동백섬',
          type: '답사',
          place: '부산시 해운대구 동백로',
          hashtags: ['지질학', '해안 지형'],
          lat: 35.1532,
          lng: 129.1635
        },
        {
          id: 19,
          number: 3,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '해운대 온천',
          type: '답사',
          place: '부산시 해운대구 중동',
          hashtags: ['지하수', '온천'],
          lat: 35.1598,
          lng: 129.1588
        }
      ]
    },
    {
      id: 10,
      imageUrl: 'https://placehold.co/600x400',
      subject: '지구',
      grade: '3학년',
      ExhibitionName: '장소명2',
      address: '장소명2 주소',
      type: '답사',
      courseItems: [
        {
          id: 20,
          number: 1,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '경복궁 정문',
          type: '답사',
          place: '서울특별시 종로구 사직로 161',
          hashtags: ['역사', '문화재'],
          lat: 37.5796,
          lng: 126.9770
        },
        {
          id: 21,
          number: 2,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '근정전',
          type: '답사',
          place: '경복궁 근정전',
          hashtags: ['궁궐 건축', '조선 시대'],
          lat: 37.5794,
          lng: 126.9769
        },
        {
          id: 22,
          number: 3,
          color: '#3B82F6',
          imageUrl: 'https://placehold.co/600x400/AACCFF/000000',
          subject: '지구',
          grade: '3학년',
          title: '경회루',
          type: '답사',
          place: '경복궁 경회루',
          hashtags: ['전통 건축', '연못'],
          lat: 37.5802,
          lng: 126.9765
        }
      ]
    }
  ];
};

export default {
  name: 'UserLikeCourseDetail',
  components: {
    CourseMap,
    ConfirmDeleteModal,
    AddPlaceModal,
    CourseExhibitionCard,
    CoursePlaceEditCard
  },
  data() {
    return {
      course: null, // history.state에서 받은 원본 데이터
      error: null,

      // 템플릿에서 직접 사용하는 데이터
      exhibitionName: '', // 코스 이름 (e.g., '전시명1')
      pageType: '',       // 'exhibition' or 'place'
      courseItems: [],    // 지도와 목록에 표시될 실제 장소 배열

      // 모달 상태
      showDeleteModal: false,
      showAddModal: false,
      itemToDeleteId: null,
    };
  },

  created() {
    const courseData = history.state?.courseData;

    if (courseData) {
      // 1. history.state에서 직접 로드 (목록에서 진입 시)
      console.log('전달받은 코스 데이터 (history.state):', courseData);
      this.course = courseData; // 원본 저장

      // 템플릿이 사용할 수 있도록 데이터 매핑
      this.exhibitionName = courseData.ExhibitionName;
      this.courseItems = courseData.courseItems || []; // 실제 장소 목록

      // '전시'/'답사' 문자열을 'exhibition'/'place'로 변환
      this.pageType = courseData.type === '전시' ? 'exhibition' : 'place';

    } else {
      // 2. 새로고침 또는 URL 직접 접근 시 (Fallback)
      console.log('history.state 없음. URL 파라미터로 데이터 조회 시도.');

      // URL 파라미터에서 정보 가져오기
      const nameFromParams = this.$route.params.ExhibitionName;
      const idFromParams = this.$route.params.courseId; // courseId도 확인
      const typeFromQuery = this.$route.query.type; // '전시' or '답사'

      console.log('URL 파라미터:', { nameFromParams, idFromParams, typeFromQuery });

      if (nameFromParams || idFromParams) {
        // 이름 또는 ID로 데이터 찾기
        const searchKey = nameFromParams || idFromParams;
        this.exhibitionName = searchKey;

        // 쿼리 파라미터 기준으로 pageType 설정
        this.pageType = typeFromQuery === '전시' ? 'exhibition' : 'place';

        // 목록화면과 동일한 데이터에서 코스 아이템 목록 조회
        this.fetchCourseData(searchKey);
      } else {
        this.error = '코스 정보를 불러올 수 없습니다. 목록으로 돌아가서 다시 시도해주세요.';
      }
    }
  },

  methods: {
    // 목록화면과 동일한 데이터에서 이름(key) 또는 ID로 찾아오는 로직
    fetchCourseData(searchKey) {
      const allCourseData = getUserLikeCourseData();
      console.log('전체 코스 데이터:', allCourseData);
      console.log('찾으려는 검색키:', searchKey);
      console.log('사용 가능한 코스명들:', allCourseData.map(course => course.ExhibitionName));
      console.log('사용 가능한 코스ID들:', allCourseData.map(course => course.id));

      // ID 또는 ExhibitionName으로 코스 찾기
      const targetCourse = allCourseData.find(course => {
        const matchById = course.id == searchKey; // == 사용으로 타입 변환 허용
        const matchByName = course.ExhibitionName === searchKey;
        console.log(`비교 ID: ${course.id} == ${searchKey} → ${matchById}`);
        console.log(`비교 이름: "${course.ExhibitionName}" === "${searchKey}" → ${matchByName}`);
        return matchById || matchByName;
      });

      if (targetCourse) {
        // 원본 수정을 방지하기 위해 깊은 복사
        this.courseItems = JSON.parse(JSON.stringify(targetCourse.courseItems || []));
        console.log(`'${searchKey}' 코스 데이터를 찾았습니다:`, this.courseItems);
        this.exhibitionName = targetCourse.ExhibitionName; // 실제 이름으로 업데이트
      } else {
        console.error(`'${searchKey}'에 해당하는 코스 데이터를 찾을 수 없습니다.`);
        console.error('사용 가능한 코스명:', allCourseData.map(c => c.ExhibitionName));
        console.error('사용 가능한 코스ID:', allCourseData.map(c => c.id));

        // 유사한 이름 찾기 (대소문자 무시, 공백 제거)
        const normalizedSearchKey = searchKey.toString().replace(/\s+/g, '').toLowerCase();
        const similarCourse = allCourseData.find(course => {
          const normalizedCourseName = course.ExhibitionName.replace(/\s+/g, '').toLowerCase();
          return normalizedCourseName === normalizedSearchKey;
        });

        if (similarCourse) {
          console.log('유사한 코스를 찾았습니다:', similarCourse.ExhibitionName);
          this.courseItems = JSON.parse(JSON.stringify(similarCourse.courseItems || []));
          this.exhibitionName = similarCourse.ExhibitionName;
        } else {
          this.error = '해당 코스 데이터를 찾을 수 없습니다.';
          this.courseItems = []; // 데이터를 못찾으면 빈 배열로 설정
        }
      }
    },

    goBack() {
      this.$router.back();
    },

    // --- 수정/삭제 이벤트 핸들러 ---
    handleEdit(id) {
      console.log('수정할 ID:', id);
      // (TODO: 수정 로직 구현)
    },

    handleDelete(id) {
      console.log('삭제 모달 열기, ID:', id);
      this.itemToDeleteId = id;
      this.showDeleteModal = true;
    },

    // --- 삭제 모달용 함수 ---
    confirmDeleteItem() {
      console.log('삭제 확정, ID:', this.itemToDeleteId);
      this.courseItems = this.courseItems.filter(item => item.id !== this.itemToDeleteId);
      // 삭제 후 순서(number) 재정렬
      this.reorderCourseItems();
      this.closeDeleteModal();
    },

    closeDeleteModal() {
      this.itemToDeleteId = null;
      this.showDeleteModal = false;
    },

    // --- 장소 추가 모달용 함수 ---
    openAddModal() {
      this.showAddModal = true;
    },

    closeAddModal() {
      this.showAddModal = false;
    },

    addNewItem(place) {
      console.log('추가할 장소:', place); // AddPlaceModal에서 전달된 place 객체

      // '답사' (place) 형식에 맞게 새 아이템 생성
      // (place 객체에 name, address, lat, lng가 있다고 가정)
      const newItem = {
        id: new Date().getTime(), // 임시 고유 ID
        number: this.courseItems.length + 1,
        color: '#3B82F6', // 답사 기본 색상
        imageUrl: place.imageUrl || 'https://placehold.co/600x400/AACCFF/000000',
        subject: place.subject || '미지정', // (모달에서 받거나 임시값)
        grade: place.grade || '공통',   // (모달에서 받거나 임시값)
        title: place.name,  // 모달에서 받은 장소 이름
        type: '답사',
        place: place.address, // 모달에서 받은 주소
        hashtags: ['새로 추가됨'],
        lat: place.lat, // 모달에서 받은 위도
        lng: place.lng  // 모달에서 받은 경도
      };

      this.courseItems.push(newItem); // 데이터 배열에 추가
      this.closeAddModal(); // 모달 닫기
    },

    // 아이템 삭제/순서 변경 시 number를 다시 정렬하는 함수
    reorderCourseItems() {
      this.courseItems.forEach((item, index) => {
        item.number = index + 1;
      });
    }
  }
}
</script>

<style scoped>
.course-recommend-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 60px);
}

.map-area {
  height: 200px;
  width: 100%;
  flex-shrink: 0;
}

.course-root-name {
  display: flex;
  font-size: large;
  margin: 16px;
  flex-shrink: 0;
}

.course-add-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

/* [헤더]
   채팅방 헤더와 동일한 구조
*/
.chat-header {
  position: relative;
}

.chat-header .header-left,
.chat-header .header-right {
  flex: 1;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-weight: 600;
}

.btn {
  width: 327px;
  height: 48px;
  border-radius: 30px;
  background-color: #007bff;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

.btn .bi-plus {
  margin-right: 8px;
}

.scrollable-content {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
}

.course-list-container {
  padding: 24px;
  background-color: #f8f9fa;
}
</style>
