<template>
  <div>
    <div class="modal-backdrop fade show" @click="$emit('close')"></div>

    <div class="modal-sheet d-flex flex-column" style="font-family: 'SUIT', sans-serif">

      <div class="modal-header p-3 d-flex align-items-center flex-shrink-0" style="background-color: #4A7CEC">
        <i class="bi bi-person-circle fs-2 me-3" style="color: white;"></i>
        <div class="flex-grow-1">
          <h5 class="fw-bold m-0 fs-6 text-white">필터 선택</h5>
          <small class="text-black">탐구영역과 학년을 선택하세요.</small>
        </div>
        <button class="btn-close fs-5" @click="$emit('close')"></button>
      </div>

      <div class="modal-body flex-grow-1 overflow-auto p-4">

        <h6 class="fw-bold mb-3">탐구영역</h6>
        <div class="row g-3">
          <div class="col-6" v-for="subject in subjects" :key="subject">
            <button type="button" class="filter-btn" :class="{ 'active': selectedSubject === subject }"
              @click="selectedSubject = subject">
              {{ subject }}
            </button>
          </div>
          <!-- <div class="col-12 mt-2">
            <button type="button" class="filter-btn" :class="{ 'active': selectedSubject === '전체' }"
              @click="selectedSubject = '전체'">
              전체
            </button>
          </div> -->
        </div>

        <h6 class="fw-bold mb-3 mt-4 pt-2">초등학교</h6>
        <div class="row g-3">
          <div class="col-6" v-for="grade in grades" :key="grade">
            <button type="button" class="filter-btn" :class="{ 'active': selectedGrade === grade }"
              @click="selectedGrade = grade">
              {{ grade }}
            </button>
          </div>
          <!-- <div class="col-12 mt-2">
            <button type="button" class="filter-btn" :class="{ 'active': selectedGrade === '전체' }"
              @click="selectedGrade = '전체'">
              전체
            </button>
          </div> -->
        </div>
      </div>

      <div class="modal-footer p-3 border-top flex-shrink-0">
        <button class="btn btn-primary btn-lg w-100 rounded-3 py-3 fw-bold"
          @click="$emit('complete', { subject: selectedSubject, grade: selectedGrade })">
          선택완료
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 부모로부터 초기값을 props로 받기
const props = defineProps({
  initialSubject: String,
  initialGrade: String
})

// 부모 컴포넌트로 이벤트 보내기
const emit = defineEmits(['close', 'complete']);

// 모달 내부 상태를 props로 받은 값으로 초기화
const selectedSubject = ref(props.initialSubject);
const selectedGrade = ref(props.initialGrade);

// v-for로 렌더링할 버튼 배열
const subjects = ref(['물리', '화학', '생명', '지구']);
const grades = ref(['초등 3학년', '초등 4학년', '초등 5학년', '초등 6학년']);

</script>

<style scoped>
/* 모달 뒷배경 */
.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  /* 부트스트랩 모달 z-index */
  z-index: 1050;
}

/*
  모달 본체 (Bottom Sheet -> Center Modal)
*/
.modal-sheet {
  position: absolute;

  /* 1. 화면 정중앙 배치 */
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  /* 2. 크기 조절 */
  width: 90%;
  /* 화면의 90% 너비 (조절 가능) */
  height: auto;
  /* 내용물에 맞게 높이 자동 조절 */
  max-height: 90%;
  /* 최대 높이는 90%로 제한 */

  background: white;
  border-radius: 20px;
  /* 3. 모든 모서리를 둥글게 */
  z-index: 1051;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  /* 그림자 수정 */
}

/* 필터 버튼 (수정 없음) */
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

/* 활성 상태 (Active) */
.filter-btn.active {
  background: #4A7CEC;
  color: white;
  border-color: #4A7CEC;
}
</style>
