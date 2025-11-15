import { ref } from 'vue';
import { defineStore } from 'pinia';

// 'curriculum'이라는 ID로 새 스토어를 정의합니다.
export const useCurriculumStore = defineStore('curriculum', () => {
  // 1. state: localStorage에서 값을 꺼내거나 기본값('초등 3학년')을 사용합니다.
  const selectedGrade = ref(localStorage.getItem('selectedGrade') || '초등 3학년');
  const selectedSubject = ref(localStorage.getItem('selectedSubject') || '물리');

  // 2. actions: 상태를 변경하는 함수
  function setFilter(grade, subject) {
    // ref 값을 업데이트합니다.
    selectedGrade.value = grade;
    selectedSubject.value = subject;
    
    // localStorage에도 저장하여 새로고침해도 유지되도록 합니다.
    localStorage.setItem('selectedGrade', grade);
    localStorage.setItem('selectedSubject', subject);
  }

  // 3. 스토어가 노출할 state와 actions를 반환합니다.
  return {
    selectedGrade,
    selectedSubject,
    setFilter,
  };
});