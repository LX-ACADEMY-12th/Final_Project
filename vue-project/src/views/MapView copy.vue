<template>
  <div class="vh-100 position-relative overflow-hidden bg-light" style="font-family: 'SUIT', sans-serif">

    <div ref="mapContainer" class="map-placeholder"></div>
    <div class="position-absolute d-flex align-items-center p-3 bg-white rounded-4 shadow" style="
        z-index: 20;
        gap: 12px;
        width: calc(100% - 36px); /* 좌우 18px 여백 */
        height: 72px;
        left: 18px;
        top: 16px;
      ">
      <img src="https://placehold.co/600x400" class="rounded-circle" style="width: 48px; height: 48px; flex-shrink: 0;">

      <div class="flex-grow-1" style="font-family: 'SUIT', sans-serif">
        <div class="text-secondary" style="font-size: 0.9rem;">안녕하세요</div>
        <div class="fw-bold" style="font-size: 1.1rem;">김민수 학부모님</div>
      </div>
      <button class="btn btn-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0"
        style="width: 48px; height: 48px;" @click.prevent="isModalOpen = true">
        <i class="bi bi-search fs-5"></i>
      </button>
    </div>
    <div class="position-absolute d-flex flex-row" style="z-index: 10; top: 104px; left: 18px; gap: 8px;">
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '전시' }"
        @click="changeTab('전시')">전시</button>
      <button type="button" class="spec-button shadow-sm" :class="{ 'active': selectedTab === '답사' }"
        @click="changeTab('답사')">답사</button>
    </div>

    <div class="position-absolute d-flex flex-column" style="
        z-index: 10;
        gap: 10px;
        right: 18px;
        /* 하단 UI 요소들(캐러셀, 네비바) 위에 위치 */
        bottom: calc(170px + 63px + 16px);
      ">
      <button
        class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center">
        <i class="bi bi-geo-alt" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">방문장소</span>
      </button>
      <button class="btn btn-dark btn-circle shadow-sm d-flex flex-column p-0 justify-content-center align-items-center"
        @click="goToCurrentLocation">
        <i class="bi bi-bullseye" style="font-size: 1rem; line-height: 1;"></i>
        <span style="font-size: 0.6rem; margin-top: 2px;">현위치</span>
      </button>
    </div>

    <div class="position-absolute" style="
        bottom: 63px; /* 하단 네비게이션바 높이 */
        left: 0;
        right: 0;
        height: 170px; /* 카드(137px) + 상하 여백 */
        z-index: 20;
      ">
      <div class="card-carousel"
        style="width: 100%; max-width: 100%; overflow-x: auto; overflow-y: hidden; height: 100%;">
        <div class="d-flex flex-row align-items-center" style="gap: 16px; height: 100%; padding: 0 18px;">
          <div v-if="isSearching" class="text-center p-5 text-muted w-100">
            검색 중...
          </div>
          <div v-else-if="displayedItems.length === 0" class="text-center p-5 text-muted w-100">
            <div>표시할 장소가 없습니다.</div>
            <div v-if="locationType === 'radius'" class="text-sm mt-2">
              현재위치: {{ currentUserLocation ? `${currentUserLocation.lat.toFixed(4)},
              ${currentUserLocation.lng.toFixed(4)}` : '없음' }}<br>
              반경: {{ searchRadius }}km / 과목: {{ selectedSubject }} / 학년: {{ selectedGrade }}
            </div>
          </div>
          <PlaceCard v-else v-for="item in displayedItems" :key="item.id" :item="item" @add="goToDetail(item)"
            @item-click="handleItemClick(item)" />
        </div>
      </div>
    </div>

    <FilterModal v-if="isModalOpen" :initialLocationType="locationType" :initialRadius="searchRadius"
      :initialRegion="selectedRegion" :initialSubject="selectedSubject" :initialGrade="selectedGrade"
      @close="isModalOpen = false" @complete="handleFilterComplete" />

    <BottomNavbar :selectedNavItem="selectedNavItem" @navigate="handleNavigation" />
  </div>
</template>

<script>
// <KeepAlive>가 인식할 수 있도록 name을 지정
export default {
  name: 'MapComponent'
}
</script>

<script setup>
import { ref, onMounted, watch, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import BottomNavbar from '@/components/BottomNavbar.vue';
import FilterModal from '@/components/modal/FilterModal.vue';
import PlaceCard from '@/components/card/PlaceCard.vue';
// Haversine 거리 계산 함수 import
import { calculateDistance } from '@/utils/distance'; // (경로는 실제 파일 위치에 맞게 수정!)

const router = useRouter();
const selectedTab = ref('전시');
const selectedNavItem = ref('지도');
const isModalOpen = ref(false);
const mapContainer = ref(null);
const map = ref(null);
const markers = ref([]);
const currentLocationMarker = ref(null);

// --- 필터 및 검색 상태 ---
const locationType = ref('all'); // 'all', 'radius', 'region' (이름 및 기본값 변경)
const searchRadius = ref(5); // km (모달 기본값과 일치)
const selectedRegion = ref(''); // 예: "서울시 강남구"
const selectedSubject = ref('물리');
const selectedGrade = ref('초등 3학년'); // 모달 기본값과 일치

const currentUserLocation = ref(null); // { lat: number, lng: number }
const displayedItems = ref([]);      // 최종적으로 화면/지도에 보여줄 목록
const isSearching = ref(false);      // (선택) 로딩 상태

// --- 목 데이터 ---
const exhibitionItems = ref([
  // --- 기존 데이터 (10개) ---
  { id: 1, imageUrl: 'https://placehold.co/600x400', city: '대전', district: '유성구', grade: '3학년', subject: '지구', title: '습지생물코너', type: '상설', place: '국립중앙과학관 자연사관', hashtags: ['생명과학'], lat: 36.3758, lng: 127.3845 },
  { id: 2, imageUrl: 'https://placehold.co/600x400', city: '과천', district: '', grade: '4학년', subject: '물리', title: '빛의 원리', type: '기획', place: '국립과천과학관', hashtags: ['파동', '빛'], lat: 37.4363, lng: 126.9746 },
  { id: 3, imageUrl: 'https://placehold.co/600x400', city: '서울', district: '노원구', grade: '5학년', subject: '화학', title: '미래 에너지', type: '상설', place: '서울시립과학관', hashtags: ['에너지', '화학 반응'], lat: 37.6094, lng: 127.0706 },
  { id: 101, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '대전', district: '유성구', grade: '3학년', subject: '물리', title: '떠오르는 힘, 부력', type: '상설', place: '국립중앙과학관', hashtags: ['유체', '물리1'], lat: 36.3758, lng: 127.3845 },
  { id: 102, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '서울', district: '노원구', grade: '5학년', subject: '지구', title: '태양계 행성 탐험', type: '기획', place: '서울시립과학관', hashtags: ['우주', '천체'], lat: 37.6094, lng: 127.0706 },
  { id: 103, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '과천', district: '', grade: '6학년', subject: '생명', title: '인체의 신비', type: '상설', place: '국립과천과학관', hashtags: ['소화', '순환'], lat: 37.4363, lng: 126.9746 },
  { id: 104, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '서울', district: '노원구', grade: '4학년', subject: '화학', title: '반짝반짝 결정', type: '기획', place: '서울시립과학관', hashtags: ['물질', '용액'], lat: 37.6094, lng: 127.0706 },
  { id: 105, imageUrl: 'https://placehold.co/600x400/CC99FF/000', city: '대전', district: '유성구', grade: '6학년', subject: '지구', title: '움직이는 대륙', type: '상설', place: '국립중앙과학관', hashtags: ['판 구조론', '화산'], lat: 36.3758, lng: 127.3845 },
  { id: 106, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '과천', district: '', grade: '5학년', subject: '물리', title: '전기와 자기', type: '상설', place: '국립과천과학관', hashtags: ['전자기', '회로'], lat: 37.4363, lng: 126.9746 },
  { id: 107, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서울', district: '노원구', grade: '3학년', subject: '생명', title: '동물의 한살이', type: '기획', place: '서울시립과학관', hashtags: ['곤충', '동물'], lat: 37.6094, lng: 127.0706 },

  // --- 추가 데이터 (20개) ---
  // 부산: 35.3151, 129.2105
  // 대구: 35.8288, 128.4238

  // 물리
  { id: 108, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '부산', district: '기장군', grade: '3학년', subject: '물리', title: '소리의 파동', type: '상설', place: '부산국립과학관', hashtags: ['소리', '진동'], lat: 35.3151, lng: 129.2105 },
  { id: 109, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '대구', district: '달성군', grade: '4학년', subject: '물리', title: '빛과 렌즈', type: '기획', place: '대구국립과학관', hashtags: ['굴절', '반사'], lat: 35.8288, lng: 128.4238 },
  { id: 110, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '대전', district: '유성구', grade: '5학년', subject: '물리', title: '전자기 유도', type: '상설', place: '국립중앙과학관', hashtags: ['전기', '자기장'], lat: 36.3758, lng: 127.3845 },
  { id: 111, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '서울', district: '노원구', grade: '6학년', subject: '물리', title: '운동과 에너지', type: '기획', place: '서울시립과학관', hashtags: ['역학', '에너지 보존'], lat: 37.6094, lng: 127.0706 },
  { id: 112, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '과천', district: '', grade: '6학년', subject: '물리', title: '도구와 일', type: '상설', place: '국립과천과학관', hashtags: ['지렛대', '도르래'], lat: 37.4363, lng: 126.9746 },

  // 화학
  { id: 113, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '부산', district: '기장군', grade: '3학년', subject: '화학', title: '물질의 상태', type: '상설', place: '부산국립과학관', hashtags: ['고체', '액체', '기체'], lat: 35.3151, lng: 129.2105 },
  { id: 114, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '대구', district: '달성군', grade: '3학년', subject: '화학', title: '용해와 용액', type: '기획', place: '대구국립과학관', hashtags: ['설탕물', '소금물'], lat: 35.8288, lng: 128.4238 },
  { id: 115, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '대전', district: '유성구', grade: '4학년', subject: '화학', title: '산과 염기', type: '상설', place: '국립중앙과학관', hashtags: ['지시약', '중화 반응'], lat: 36.3758, lng: 127.3845 },
  { id: 116, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '과천', district: '', grade: '5학년', subject: '화학', title: '연소와 소화', type: '상설', place: '국립과천과학관', hashtags: ['연소 조건', '소화기'], lat: 37.4363, lng: 126.9746 },
  { id: 117, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '서울', district: '노원구', grade: '6학년', subject: '화학', title: '우리 몸의 화학', type: '기획', place: '서울시립과학관', hashtags: ['소화효소', '화학물질'], lat: 37.6094, lng: 127.0706 },

  // 생명
  { id: 118, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '부산', district: '기장군', grade: '3학년', subject: '생명', title: '식물의 한살이', type: '기획', place: '부산국립과학관', hashtags: ['씨앗', '발아'], lat: 35.3151, lng: 129.2105 },
  { id: 119, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '대구', district: '달성군', grade: '4학년', subject: '생명', title: '동물의 분류', type: '상설', place: '대구국립과학관', hashtags: ['척추동물', '무척추동물'], lat: 35.8288, lng: 128.4238 },
  { id: 120, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '대전', district: '유성구', grade: '4학년', subject: '생명', title: '작은 생물의 세계', type: '상설', place: '국립중앙과학관', hashtags: ['짚신벌레', '현미경'], lat: 36.3758, lng: 127.3845 },
  { id: 121, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '과천', district: '', grade: '5학년', subject: '생명', title: '환경과 생태계', type: '상설', place: '국립과천과학관', hashtags: ['먹이그물', '환경오염'], lat: 37.4363, lng: 126.9746 },
  { id: 122, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서울', district: '노원구', grade: '5학년', subject: '생명', title: '유전과 진화', type: '기획', place: '서울시립과학관', hashtags: ['멘델', 'DNA'], lat: 37.6094, lng: 127.0706 },

  // 지구
  { id: 123, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '부산', district: '기장군', grade: '3학년', subject: '지구', title: '지구의 모습', type: '상설', place: '부산국립과학관', hashtags: ['육지', '바다'], lat: 35.3151, lng: 129.2105 },
  { id: 124, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '대구', district: '달성군', grade: '4학년', subject: '지구', title: '화산과 지진', type: '상설', place: '대구국립과학관', hashtags: ['마그마', '지진파'], lat: 35.8288, lng: 128.4238 },
  { id: 125, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '과천', district: '', grade: '4학년', subject: '지구', title: '암석과 광물', type: '상설', place: '국립과천과학관', hashtags: ['화성암', '퇴적암'], lat: 37.4363, lng: 126.9746 },
  { id: 126, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '대전', district: '유성구', grade: '5학년', subject: '지구', title: '날씨와 기후', type: '기획', place: '국립중앙과학관', hashtags: ['기단', '전선'], lat: 36.3758, lng: 127.3845 },
  { id: 127, imageUrl: 'https://placehold.co/600x400/00CCFF/000', city: '과천', district: '', grade: '6학년', subject: '지구', title: '계절의 변화', type: '상설', place: '국립과천과학관', hashtags: ['자전축', '태양'], lat: 37.4363, lng: 126.9746 },
]);

const fieldTripItems = ref([
  // --- 기존 데이터 (10개) ---
  { id: 4, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '부산', district: '해운대구', grade: '5학년', subject: '지구', title: '해운대 (지질 탐사)', place: '부산시 해운대구', hashtags: ['고체지구', '퇴적암'], lat: 35.1587, lng: 129.1604 },
  { id: 5, imageUrl: 'https://placehold.co/600x400/CCBBAA/000000', city: '서울', district: '성동구', grade: '4학년', subject: '물리', title: '서울숲 (공원 산책)', place: '서울시 성동구', hashtags: ['운동', '자연'], lat: 37.5445, lng: 127.0374 },
  { id: 201, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '인천', district: '남동구', grade: '3학년', subject: '화학', title: '갯벌 체험 (염전)', place: '인천 소래습지', hashtags: ['소금', '물질의 특성'], lat: 37.4021, lng: 126.7301 },
  { id: 202, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '영월', district: '', grade: '5학년', subject: '지구', title: '영월 한반도 지형', place: '강원도 영월군', hashtags: ['침식', '퇴적'], lat: 37.2045, lng: 128.4557 },
  { id: 203, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서천', district: '', grade: '6학년', subject: '생명', title: '양서류 관찰', place: '국립생태원', hashtags: ['생태계', '동물'], lat: 36.6631, lng: 126.6913 },
  { id: 204, imageUrl: 'https://placehold.co/600x400/FF99CC/000', city: '서울', district: '강서구', grade: '6학년', subject: '화학', title: '암모니아 분수 실험장', place: 'LG사이언스파크', hashtags: ['산염기', '기체'], lat: 37.5649, lng: 126.8300 },
  { id: 205, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '서울', district: '광진구', grade: '3학년', subject: '물리', title: '그림자 놀이 체험', place: '어린이대공원', hashtags: ['빛', '그림자'], lat: 37.5492, lng: 127.0747 },
  { id: 206, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '서울', district: '강서구', grade: '4학년', subject: '생명', title: '서울 식물원', place: '서울 식물원', hashtags: ['식물', '광합성'], lat: 37.5704, lng: 126.8359 },
  { id: 207, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '포천', district: '', grade: '4학년', subject: '지구', title: '화성암 관찰 (한탄강)', place: '포천 한탄강', hashtags: ['화성암', '지층'], lat: 38.0069, lng: 127.2088 },
  { id: 208, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '과천', district: '', grade: '3학년', subject: '물리', title: '놀이기구 속 과학', place: '서울랜드', hashtags: ['원심력', '관성'], lat: 37.4334, lng: 126.9922 },

  // --- 추가 데이터 (23개) ---

  // 물리
  { id: 209, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '춘천', district: '', grade: '4학년', subject: '물리', title: '소양강댐 수력발전', place: '소양강댐', hashtags: ['위치에너지', '전기에너지'], lat: 37.9546, lng: 127.7963 },
  { id: 210, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '인천', district: '중구', grade: '5학년', subject: '물리', title: '비행기의 양력', place: '인천국제공항 전망대', hashtags: ['공기저항', '양력'], lat: 37.4692, lng: 126.4350 },
  { id: 211, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '제주', district: '제주시', grade: '5학년', subject: '물리', title: '행원 풍력발전단지', place: '행원 풍력발전', hashtags: ['신재생에너지', '터빈'], lat: 33.5283, lng: 126.8522 },
  { id: 212, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '부산', district: '수영구', grade: '6학년', subject: '물리', title: '다리의 과학 (광안대교)', place: '광안대교', hashtags: ['트러스 구조', '현수교'], lat: 35.1472, lng: 129.1301 },
  { id: 213, imageUrl: 'https://placehold.co/600x400/FFCC00/000', city: '서울', district: '용산구', grade: '6학년', subject: '물리', title: 'N서울타워 (도르래)', place: 'N서울타워', hashtags: ['도르래', '엘리베이터'], lat: 37.5512, lng: 126.9882 },

  // 화학
  { id: 214, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '포항', district: '남구', grade: '3학년', subject: '화학', title: '포스코 (철의 제련)', place: '포스코 역사박물관', hashtags: ['용광로', '철'], lat: 35.9932, lng: 129.3734 },
  { id: 215, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '서울', district: '강북구', grade: '4학년', subject: '화학', title: '아리수 정수센터', place: '아리수 정수센터', hashtags: ['물의 정화', '여과'], lat: 37.6409, lng: 127.0135 },
  { id: 216, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '하남', district: '', grade: '4학년', subject: '화학', title: '유니온타워 (소각장)', place: '하남 유니온타워', hashtags: ['연소', '재활용'], lat: 37.5678, lng: 127.2064 },
  { id: 217, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '울산', district: '남구', grade: '5학년', subject: '화학', title: '석유화학단지 견학', place: 'SK이노베이션 울산CLX', hashtags: ['원유', '플라스틱'], lat: 35.5311, lng: 129.3519 },
  { id: 218, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '용인', district: '', grade: '5학년', subject: '화학', title: '천연 비누 만들기', place: '용인 농촌테마파크', hashtags: ['비누화 반응', '산과 염기'], lat: 37.2301, lng: 127.2510 },
  { id: 219, imageUrl: 'https://placehold.co/600x400/FF99AA/000', city: '안산', district: '단원구', grade: '6학년', subject: '화학', title: '시화호 수질 관찰', place: '시화호 조력발전소', hashtags: ['수질 오염', '산성비'], lat: 37.3060, lng: 126.7214 },

  // 생명
  { id: 220, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '양평', district: '', grade: '3학년', subject: '생명', title: '양평 곤충 박물관', place: '양평 곤충 박물관', hashtags: ['곤충', '한살이'], lat: 37.4913, lng: 127.5028 },
  { id: 221, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '이천', district: '', grade: '3학년', subject: '생명', title: '농장 체험 (작물 재배)', place: '이천 돼지박물관', hashtags: ['식물', '농업'], lat: 37.1959, lng: 127.4200 },
  { id: 222, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '파주', district: '', grade: '4학년', subject: '생명', title: 'DMZ 생태 공원', place: '임진각 평화누리', hashtags: ['DMZ', '야생동물'], lat: 37.8906, lng: 126.7443 },
  { id: 223, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '순천', district: '', grade: '5학년', subject: '생명', title: '순천만 국가정원', place: '순천만 국가정원', hashtags: ['습지', '생태계'], lat: 34.9304, lng: 127.5147 },
  { id: 224, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '포천', district: '', grade: '5학년', subject: '생명', title: '국립수목원 (광릉숲)', place: '국립수목원', hashtags: ['산림 생태계', '피톤치드'], lat: 37.7562, lng: 127.1681 },
  { id: 225, imageUrl: 'https://placehold.co/600x400/99FF99/000', city: '부산', district: '중구', grade: '6학년', subject: '생명', title: '자갈치 시장 (해양생물)', place: '자갈치 시장', hashtags: ['어류', '해양 생태계'], lat: 35.0970, lng: 129.0323 },

  // 지구
  { id: 226, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '서울', district: '동작구', grade: '3학년', subject: '지구', title: '기상청 견학', place: '기상청', hashtags: ['날씨', '기상관측'], lat: 37.5042, lng: 126.9197 },
  { id: 227, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '영월', district: '', grade: '3학년', subject: '지구', title: '별마로 천문대', place: '별마로 천문대', hashtags: ['별자리', '천체관측'], lat: 37.1882, lng: 128.4682 },
  { id: 228, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '고성', district: '', grade: '4학년', subject: '지구', title: '공룡 발자국 화석지', place: '경남고성공룡세계엑스포', hashtags: ['공룡', '화석'], lat: 34.9605, lng: 128.3227 },
  { id: 229, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '태백', district: '', grade: '5학년', subject: '지구', title: '태백 고생대자연사박물관', place: '태백고생대자연사박물관', hashtags: ['고생대', '삼엽충'], lat: 37.1633, lng: 128.9881 },
  { id: 230, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '제주', district: '서귀포시', grade: '6학년', subject: '지구', title: '서귀포 화석 산지', place: '서귀포층패류화석', hashtags: ['신생대', '화석'], lat: 33.2435, lng: 126.4255 },
  { id: 231, imageUrl: 'https://placehold.co/600x400/AACCFF/000000', city: '태안', district: '', grade: '6학년', subject: '지구', title: '해안 침식 지형 (신두리)', place: '신두리 해안사구', hashtags: ['침식', '퇴적', '사구'], lat: 36.8123, lng: 126.2163 },
]);

// URL 쿼리 복원
const tabFromQuery = router.currentRoute.value.query.tab;
if (tabFromQuery === '답사') {
  selectedTab.value = '답사';
}

// KeepAlive 활성화 시 네비바 복원
onActivated(() => {
  selectedNavItem.value = '지도';
});

// 탭 변경 함수
const changeTab = (tabName) => {
  selectedTab.value = tabName;
  router.replace({ query: { tab: tabName } });
  performSearch();
};

// 상세 페이지 이동
const goToDetail = (item) => {
  if (selectedTab.value === '전시') {
    router.push(`/exhibition/${item.id}`);
  } else {
    router.push(`/place/${item.id}`);
  }
};

// 카드 클릭 시 지도 이동
const handleItemClick = (item) => {
  moveMapToItem(item.lat, item.lng);
};

// 현재 위치 가져오기
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      return reject(new Error("Geolocation 미지원"));
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentUserLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        console.log('현재 위치 획득:', currentUserLocation.value);
        resolve(currentUserLocation.value);
      },
      (error) => {
        console.error('위치 정보 획득 실패:', error);
        currentUserLocation.value = null;
        reject(error);
      }
    );
  });
};

// 현위치 버튼 클릭 핸들러
const goToCurrentLocation = async () => {
  try {
    await getCurrentLocation(); // 현재 위치 업데이트
    if (currentUserLocation.value && map.value) {
      const currentLatLng = new window.kakao.maps.LatLng(
        currentUserLocation.value.lat,
        currentUserLocation.value.lng
      );
      map.value.setCenter(currentLatLng);
      map.value.setLevel(3); // 현위치 근처로 확대

      // --- [수정] 원 대신 커스텀 오버레이(작은 원) 생성 및 표시 ---

      // 1. 기존 현위치 표시(마커 또는 오버레이) 제거
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null);
      }

      // 2. 새 커스텀 오버레이 생성 (HTML 컨텐츠 사용)
      const content = '<div class="current-location-dot"></div>'; // CSS로 스타일링될 div
      const newOverlay = new window.kakao.maps.CustomOverlay({
        position: currentLatLng,
        content: content,
        // yAnchor: 0.5, // 필요시 오버레이 위치 미세 조정
        // xAnchor: 0.5
      });

      // 3. 오버레이를 맵에 표시
      newOverlay.setMap(map.value);

      // 4. ref에 새 오버레이 저장
      currentLocationMarker.value = newOverlay;
      // --- 오버레이 생성 끝 ---

    }
  } catch (error) {
    console.error("현위치 이동 실패:", error);
    alert("현위치를 가져올 수 없습니다. GPS가 켜져 있는지 확인해주세요.");
  }
};

// 지도 특정 위치로 이동
const moveMapToItem = (lat, lng) => {
  if (map.value) {
    const itemLatLng = new window.kakao.maps.LatLng(lat, lng);
    map.value.setCenter(itemLatLng);
    map.value.setLevel(7); // 줌인
  }
};

// 마커 모두 제거
const clearMarkers = () => {
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];
};

// 아이템들을 지도에 마커로 표시
const drawMarkers = (items) => {
  if (!map.value || !items.length) return;

  items.forEach(item => {
    const markerPosition = new window.kakao.maps.LatLng(item.lat, item.lng);
    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      title: item.title
    });
    marker.setMap(map.value);
    markers.value.push(marker);

    // 마커 클릭 이벤트
    window.kakao.maps.event.addListener(marker, 'click', () => {
      handleItemClick(item);
    });
  });
};

// 검색 실행 함수
const performSearch = async () => {
  console.log('==== 검색 실행 시작 ====');
  console.log('검색 타입:', locationType.value);
  console.log('탭:', selectedTab.value);
  console.log('과목:', selectedSubject.value);
  console.log('학년:', selectedGrade.value);
  console.log('반경:', searchRadius.value, 'km');

  isSearching.value = true;

  let baseList = (selectedTab.value === '전시') ? exhibitionItems.value : fieldTripItems.value;
  let filteredList = [];

  console.log('기본 목록 개수:', baseList.length);

  try {

    // --- [수정] 디버깅을 위해 거리 계산 및 콘솔 로깅을 맨 위로 이동 ---
    if (currentUserLocation.value) {
      console.log('--- [디버그] 현재 위치 기준 거리 계산 ---');
      console.log(`현재 위치: lat ${currentUserLocation.value.lat.toFixed(4)}, lng ${currentUserLocation.value.lng.toFixed(4)}`);

      baseList.forEach(item => {
        const distance = calculateDistance(
          currentUserLocation.value.lat, currentUserLocation.value.lng,
          item.lat, item.lng
        );
        console.log(
          `[${item.title}] (lat: ${item.lat}, lng: ${item.lng}) -> 거리: ${distance.toFixed(1)}km`
        );
      });
      console.log('--- [디버그] 거리 계산 완료 ---');
    } else {
      // 'all'이나 'region' 검색은 현위치 없이도 가능하므로 에러를 발생시키지 않음
      console.log('--- [디버그] 현재 위치 정보가 없어 거리 계산을 건너뜁니다 ---');
    }

    if (locationType.value === 'radius') {
      console.log('반경 검색 모드');

      // 'radius' 모드일 때만 현위치가 필수임
      if (!currentUserLocation.value) {
        console.log('현재 위치 없음, 위치 정보 요청 (반경 검색용)');
        await getCurrentLocation();
      }

      if (currentUserLocation.value) {
        // 필터링을 위해 거리 정보가 포함된 배열을 새로 생성
        const itemsWithDistance = baseList.map(item => {
          const distance = calculateDistance(
            currentUserLocation.value.lat, currentUserLocation.value.lng,
            item.lat, item.lng
          );
          return { ...item, distance };
        });

        // 실제 필터링
        filteredList = itemsWithDistance.filter(item => {
          const withinRadius = item.distance <= searchRadius.value;
          const subjectMatch = item.subject === selectedSubject.value;
          const gradeMatch = item.grade === selectedGrade.value.replace('초등 ', '');

          // 'radius' 모드일 때의 상세 필터링 로그 (선택적)
          // console.log(`${item.title}: 반경내(${withinRadius}) 과목일치(${subjectMatch}) 학년일치(${gradeMatch})`);

          return withinRadius && subjectMatch && gradeMatch;
        });

        console.log('반경 내 필터링 결과:', filteredList.length, '개');

      } else {
        // 반경 검색을 시도했으나 끝내 위치 정보를 얻지 못한 경우
        throw new Error("현재 위치 정보 없음 (반경 검색 실패)");
      }
    } else if (locationType.value === 'region') {
      console.log('지역 검색 모드');
      filteredList = baseList.filter(item => {
        const regionString = `${item.city} ${item.district}`.trim();
        return (selectedRegion.value && regionString.includes(selectedRegion.value)) &&
          item.subject === selectedSubject.value &&
          item.grade === selectedGrade.value.replace('초등 ', '');
      });
    } else { // 'all'
      console.log('전체 지역 검색 모드');
      filteredList = baseList.filter(item => {
        return item.subject === selectedSubject.value &&
          item.grade === selectedGrade.value.replace('초등 ', '');
      });
    }

    displayedItems.value = filteredList;
    console.log('최종 결과:', displayedItems.value.length, '개');

  } catch (error) {
    console.error("검색 중 오류 발생:", error);
    alert("검색 중 오류가 발생했습니다. " + (error.message.includes("위치") ? "위치 정보를 확인해주세요." : ""));
    displayedItems.value = [];
  } finally {
    isSearching.value = false;
    console.log('==== 검색 완료 ====');
  }
};

// 맵 초기화
onMounted(async () => {
  if (window.kakao && window.kakao.maps) {
    const options = {
      center: new window.kakao.maps.LatLng(37.566826, 126.9786567), // 서울시청
      level: 3,
    };
    map.value = new window.kakao.maps.Map(mapContainer.value, options);

    try {
      await getCurrentLocation();
      if (currentUserLocation.value && map.value) {
        const currentLatLng = new window.kakao.maps.LatLng(
          currentUserLocation.value.lat,
          currentUserLocation.value.lng
        );
        map.value.setCenter(currentLatLng);

        // --- onMounted 시 현위치 커스텀 오버레이 그리기 ---

        // 1. 기존 표시 제거 (만약을 대비)
        if (currentLocationMarker.value) {
          currentLocationMarker.value.setMap(null);
        }
        // 2. 새 커스텀 오버레이 생성 및 표시
        const content = '<div class="current-location-dot"></div>';
        currentLocationMarker.value = new window.kakao.maps.CustomOverlay({
          position: currentLatLng,
          content: content,
          // yAnchor: 0.5,
          // xAnchor: 0.5
        });
        currentLocationMarker.value.setMap(map.value);
        // --- 오버레이 그리기 끝 ---
      }
    } catch (error) {
      console.warn("초기 현위치 로드 실패:", error.message);
      // 실패해도 기본 위치(서울시청)로 진행
    }

    // 첫 검색 실행
    await performSearch();

  } else {
    console.error("Kakao Maps API 스크립트가 로드되지 않았습니다.");
  }
});

// displayedItems 변경 감지하여 마커 업데이트
watch(displayedItems, (newItems) => {
  if (!map.value) return;

  // 마커는 항상 새로 그림
  clearMarkers();
  drawMarkers(newItems);

  // === [수정된 줌 레벨 로직] ===
  if (newItems.length === 1) {
    // 검색 결과가 1개일 때:
    // 해당 위치로 이동하고 고정된 레벨(예: 3)을 사용합니다.
    const item = newItems[0];
    const itemLatLng = new window.kakao.maps.LatLng(item.lat, item.lng);
    map.value.setCenter(itemLatLng);

    // 여기 레벨을 수정하세요 (카드 클릭 시 레벨과 맞추는 것을 권장)
    map.value.setLevel(7);

  } else if (newItems.length > 1) {
    // 검색 결과가 여러 개일 때:
    // 모든 마커가 보이도록 지도의 경계(bounds)를 설정합니다.
    const bounds = new window.kakao.maps.LatLngBounds();
    newItems.forEach(item => {
      bounds.extend(new window.kakao.maps.LatLng(item.lat, item.lng));
    });
    map.value.setBounds(bounds);

    // (선택 사항) bounds가 너무 타이트(꽉 차게) 보인다면,
    // 아래 주석을 풀어 한 레벨 줌 아웃 할 수 있습니다.
    // map.value.setLevel(map.value.getLevel() + 1);

  }
  // 4. 검색 결과가 0개일 때는 지도 위치를 변경하지 않습니다.

});

// 모달 완료 핸들러
const handleFilterComplete = (filterData) => {
  console.log(`필터 선택 완료:`, filterData);
  locationType.value = filterData.locationType; // 'searchType' -> 'locationType'
  searchRadius.value = filterData.radius;     // 'radius' 모드가 아니면 null이 됨 (정상)
  selectedRegion.value = filterData.region;
  selectedSubject.value = filterData.subject;
  selectedGrade.value = filterData.grade;
  isModalOpen.value = false;

  performSearch();
};

// 하단 네비게이션 핸들러
const handleNavigation = (navItemName) => {
  selectedNavItem.value = navItemName;
  if (navItemName === '홈') router.push('/home');
  else if (navItemName === '목록') router.push('/list');
  else if (navItemName === '지도') router.push('/map'); // 현재 페이지
  else if (navItemName === '코스관리') router.push('/usercourselist');
  else if (navItemName === '마이페이지') router.push('/mypage');
};

</script>

<style>
.current-location-dot {
  width: 16px;
  /* 원 크기 */
  height: 16px;
  /* 원 크기 */
  border-radius: 50%;
  /* 동그랗게 */
  background-color: #007AFF;
  /* 파란색 */
  border: 3px solid white;
  /* 흰색 테두리 */
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  /* 그림자 */
  /* 오버레이 위치 보정 (핀 중심 맞추기) */
  transform: translate(-50%, -50%);
}
</style>

<style scoped>
/* 지도 플레이스홀더 */
.map-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #e9e9e9;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  color: #aaa;
  z-index: 0;
}

/* 하단 카드 캐러셀 스크롤바 숨기기 (표준 CSS) */
.card-carousel::-webkit-scrollbar {
  display: none;
}

.card-carousel {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 상단 필터 버튼 (전시, 탐험) */
.spec-button {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 5px 16px;
  gap: 8px;
  position: relative;
  height: 38px;
  border-radius: 20px;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #ddd;
  transition: background-color 0.2s, color 0.2s;
  font-family: 'SUIT', sans-serif;
  font-weight: 500;
}

.spec-button.active {
  background: #4A7CEC;
  color: white;
  border: none;
  font-weight: 700;
}

/* 우측 플로팅 원형 버튼 */
.btn-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  padding: 0;
  font-weight: 500;
}
</style>
