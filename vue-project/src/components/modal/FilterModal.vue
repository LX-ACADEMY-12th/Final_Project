<template>
  <div>
    <div class="modal-backdrop fade show" @click="$emit('close')"></div>

    <div class="modal-sheet d-flex flex-column" style="font-family: 'SUIT', sans-serif">

      <div class="p-3 d-flex align-items-center flex-shrink-0 rounded-top" style="background-color: #4A7CEC;">
        <i class=" bi bi-person-circle fs-2 me-3" style="color: white;"></i>
        <div class="flex-grow-1">
          <h5 class="fw-bold m-0 fs-6 text-white">{{ showSearchOptions ? '검색 옵션 선택' : '필터 선택' }}</h5>
          <small class="text-white">{{ showSearchOptions ? '원하는 방식으로 장소를 찾아보세요.' : '탐구영역과 학년을 선택하세요.' }}</small>
        </div>
        <button class="btn-close btn-close-white fs-5" @click="$emit('close')"></button>
      </div>

      <div class="modal-body flex-grow-1 overflow-auto p-4">

        <template v-if="showSearchOptions">
          <h6 class="fw-bold mb-3">검색 방식</h6>
          <div class="btn-group w-100 mb-4" role="group">
            <input type="radio" class="btn-check" name="searchType" id="searchTypeFilter" autocomplete="off"
              value="filter" v-model="localSearchType" checked>
            <label class="btn btn-outline-primary" for="searchTypeFilter">필터</label>

            <input type="radio" class="btn-check" name="searchType" id="searchTypeRadius" autocomplete="off"
              value="radius" v-model="localSearchType">
            <label class="btn btn-outline-primary" for="searchTypeRadius">내 주변</label>

            <input type="radio" class="btn-check" name="searchType" id="searchTypeRegion" autocomplete="off"
              value="region" v-model="localSearchType">
            <label class="btn btn-outline-primary" for="searchTypeRegion">지역</label>
          </div>
          <hr v-if="localSearchType !== 'filter'">
        </template>

        <div v-if="!showSearchOptions || localSearchType === 'filter'">
          <h6 class="fw-bold mb-3">탐구영역</h6>
          <div class="row g-3">
            <div class="col-6" v-for="subject in subjects" :key="subject">
              <button type="button" class="filter-btn" :class="{ 'active': localSubject === subject }"
                @click="localSubject = subject">
                {{ subject }}
              </button>
            </div>
          </div>

          <h6 class="fw-bold mb-3 mt-4 pt-2">초등학교</h6>
          <div class="row g-3">
            <div class="col-6" v-for="grade in grades" :key="grade">
              <button type="button" class="filter-btn" :class="{ 'active': localGrade === grade }"
                @click="localGrade = grade">
                {{ grade }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="showSearchOptions && localSearchType === 'radius'">
          <h6 class="fw-bold mb-3">검색 반경 (km)</h6>
          <div class="d-flex align-items-center mb-4">
            <input type="range" class="form-range flex-grow-1 me-3" min="1" max="300" step="1"
              v-model.number="localRadius">
            <span class="fw-bold">{{ localRadius }}km</span>
          </div>
          <hr>
          <p class="text-muted mt-4 mb-2"><small>선택한 과목/학년 기준으로 주변 장소를 찾습니다.</small></p>
          <h6 class="fw-bold mb-3">탐구영역: {{ localSubject }}</h6>
          <h6 class="fw-bold mb-3">학년: {{ localGrade }}</h6>
        </div>

        <div v-if="showSearchOptions && localSearchType === 'region'">
          <h6 class="fw-bold mb-3">지역 선택</h6>
          <input type="text" class="form-control mb-3" placeholder="예: 서울시 강남구, 부산 해운대" v-model="localRegion">
          <hr>
          <p class="text-muted mt-4 mb-2"><small>선택한 과목/학년 기준으로 지역 내 장소를 찾습니다.</small></p>
          <h6 class="fw-bold mb-3">탐구영역: {{ localSubject }}</h6>
          <h6 class="fw-bold mb-3">학년: {{ localGrade }}</h6>
        </div>

      </div>

      <div class="modal-footer p-3 border-top flex-shrink-0">
        <button class="btn btn-primary btn-lg w-100 rounded-3 py-3 fw-bold" @click="completeSelection">
          선택완료
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Define Props received from parent
const props = defineProps({
  // 검색 방식 UI 표시 여부 (기본값: true)
  showSearchOptions: { type: Boolean, default: true },

  initialSearchType: { type: String, default: 'filter' },
  initialRadius: { type: Number, default: 5 },
  initialRegion: { type: String, default: '' },
  initialSubject: String,
  initialGrade: String
})

// Define Emits sent to parent
const emit = defineEmits(['close', 'complete']);

// Local state for the modal, initialized with props
const localSearchType = ref(props.initialSearchType);
const localRadius = ref(props.initialRadius);
const localRegion = ref(props.initialRegion);
const localSubject = ref(props.initialSubject);
const localGrade = ref(props.initialGrade);

// Data for v-for loops
const subjects = ref(['물리', '화학', '생명', '지구']);
const grades = ref(['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년']);

// Function to emit all selected values on completion
const completeSelection = () => {
  // region 값 처리: localRegion.value가 존재하고 문자열이면 trim(), 아니면 빈 문자열('') 할당
  const regionValue = (typeof localRegion.value === 'string' ? localRegion.value.trim() : '');
  emit('complete', {
    searchType: localSearchType.value,
    radius: localRadius.value,
    region: regionValue,
    subject: localSubject.value,
    grade: localGrade.value
  });
};
</script>

<style scoped>
/* Modal Backdrop */
.modal-backdrop {
  position: absolute;
  /* Use fixed to cover the whole screen */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1050;
  /* Bootstrap modal z-index */
}

/* Modal Sheet (Centered) */
.modal-sheet {
  position: absolute;
  /* Use fixed for centering */
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  /* Adjust width as needed */
  max-width: 500px;
  /* Optional: Set a max-width */
  height: auto;
  max-height: 90vh;
  /* Use vh for viewport height */
  background: white;
  border-radius: 20px;
  /* Apply to all corners */
  z-index: 1051;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  /* Prevent content overflow */
}

/* Search Type Button Group */
.btn-group .btn {
  border-radius: 0;
  padding-top: 0.75rem;
  /* Adjust padding for better look */
  padding-bottom: 0.75rem;
}

.btn-group .btn:first-child {
  border-top-left-radius: .375rem;
  border-bottom-left-radius: .375rem;
}

.btn-group .btn:last-child {
  border-top-right-radius: .375rem;
  border-bottom-right-radius: .375rem;
}

.btn-check:checked+.btn-outline-primary {
  /* Ensure active state looks correct */
  color: #fff;
  background-color: #0d6efd;
  border-color: #0d6efd;
}

/* Filter Buttons */
.filter-btn {
  display: block;
  width: 100%;
  padding: 1rem 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  text-align: center;
  background: #FFFFFF;
  color: #333;
  border: 1px solid #dee2e6;
  border-radius: 16px;
  transition: background-color 0.2s, color 0.2s;
}

/* Active Filter Button */
.filter-btn.active {
  background: #4A7CEC;
  color: white;
  border-color: #4A7CEC;
}

/* Ensure modal body scrolls if content is too tall */
.modal-body {
  overflow-y: auto;
}

/* Style range input */
.form-range {
  cursor: pointer;
}
</style>
