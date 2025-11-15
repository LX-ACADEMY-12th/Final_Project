<template>
  <div>
    <div class="modal-backdrop fade show" @click="$emit('close')"></div>

    <div class="modal-sheet d-flex flex-column" style="font-family: 'SUIT', sans-serif">

      <div class="p-3 d-flex align-items-center flex-shrink-0 rounded-top" style="background-color: #4A7CEC;">
        <i class=" bi bi-person-circle fs-2 me-3" style="color: white;"></i>
        <div class="flex-grow-1">
          <h5 class="fw-bold m-0 fs-6 text-white">필터 선택</h5>
          <small class="text-white">탐구영역과 학년을 선택하세요.</small>
        </div>
        <button class="btn-close btn-close-white fs-5" @click="$emit('close')"></button>
      </div>

      <div class="modal-body flex-grow-1 overflow-auto p-4">

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

        <!-- 위치 기준 섹션: showLocationOptions가 true일 때만 표시 -->
        <template v-if="showLocationOptions">
          <h6 class="fw-bold mb-3 mt-4 pt-2">위치 기준</h6>
          <div class="btn-group w-100 mb-3" role="group">
            <input type="radio" class="btn-check" name="locationType" id="locTypeAll" autocomplete="off" value="all"
              v-model="localLocationType" checked>
            <label class="btn btn-outline-primary" for="locTypeAll">전체 지역</label>
            <input type="radio" class="btn-check" name="locationType" id="locTypeRadius" autocomplete="off"
              value="radius" v-model="localLocationType">
            <label class="btn btn-outline-primary" for="locTypeRadius">내 주변</label>
            <input type="radio" class="btn-check" name="locationType" id="locTypeRegion" autocomplete="off"
              value="region" v-model="localLocationType">
            <!-- <label class="btn btn-outline-primary" for="locTypeRegion">지역 선택</label> -->
          </div>

          <div v-if="localLocationType === 'radius'">
            <h6 class="fw-bold mb-3">검색 반경</h6>
            <div class="row g-3">
              <div class="col-6" v-for="radius in radiusOptions" :key="radius">
                <button type="button" class="filter-btn" :class="{ 'active': localRadius === radius }"
                  @click="localRadius = radius">
                  {{ radius }}km 이내
                </button>
              </div>
            </div>
          </div>

          <div v-if="localLocationType === 'region'">
            <h6 class="fw-bold mb-3">지역 선택</h6>
            <input type="text" class="form-control" placeholder="예: 서울시 강남구, 부산 해운대" v-model="localRegion">
          </div>
        </template>

      </div>

      <div class="modal-footer p-3 border-top flex-shrink-0">
        <button class="btn btn-lg w-100 rounded-3 py-3 fw-bold" @click="completeSelection"
          style="background-color: #4A7CEC; border-color: #4A7CEC; color: white;">
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
  // 위치 옵션 표시 여부 (기본값: true, 장소 목록에서는 false로 설정)
  showLocationOptions: { type: Boolean, default: true },
  // 위치 관련 초기값들 (showLocationOptions가 true일 때만 사용)
  initialLocationType: { type: String, default: 'all' },
  initialRadius: { type: Number, default: 5 },
  initialRegion: { type: String, default: '' },
  // 필터 관련 초기값들
  initialSubject: String,
  initialGrade: String
})

// Define Emits sent to parent
const emit = defineEmits(['close', 'complete']);

// Local state for the modal, initialized with props
const localLocationType = ref(props.initialLocationType);
const localRadius = ref(props.initialRadius);
const localRegion = ref(props.initialRegion);
const localSubject = ref(props.initialSubject);
const localGrade = ref(props.initialGrade);

// Data for v-for loops
const subjects = ref(['물리', '화학', '생명', '지구']);
const grades = ref(['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년']);
// 반경 검색 프리셋 (슬라이더 대신 버튼 제공)
const radiusOptions = ref([3, 5, 10, 30]);

// Function to emit all selected values on completion
const completeSelection = () => {
  const result = {
    subject: localSubject.value,
    grade: localGrade.value
  };

  // 위치 옵션이 활성화된 경우에만 위치 관련 데이터 추가
  if (props.showLocationOptions) {
    let radiusValue = null;
    let regionValue = '';

    // 선택된 위치 기준에 따라 radius와 region 값을 정리
    if (localLocationType.value === 'radius') {
      radiusValue = localRadius.value;
    } else if (localLocationType.value === 'region') {
      regionValue = (typeof localRegion.value === 'string' ? localRegion.value.trim() : '');
    }

    result.locationType = localLocationType.value; // 'all', 'radius', 'region'
    result.radius = radiusValue; // 'radius'일 때만 값, 아니면 null
    result.region = regionValue; // 'region'일 때만 값, 아니면 ''
  }

  emit('complete', result);
};
</script>

<style scoped>
/* Modal Backdrop */
.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1050;
}

/* Modal Sheet (Centered) */
.modal-sheet {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 500px;
  height: auto;
  max-height: 90vh;
  background: white;
  border-radius: 20px;
  z-index: 1051;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* Search Type Button Group */
.btn-group .btn {
  border-radius: 0;
  padding-top: 0.75rem;
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
  color: #fff;
  background-color: #4A7CEC;
  border-color: #4A7CEC;
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
