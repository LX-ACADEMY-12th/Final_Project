// 📍 파일 위치: src/utils/tourMapper.js

/**
 * 장소 제목(title)을 기반으로 tourConfig.js의 sceneId를 매핑합니다.
 * @param {string | null | undefined} title - 장소 제목
 * @returns {string | null} - 매핑된 sceneId (예: 'hall_1_entrance') 또는 null
 */
export function getSceneIdFromTitle(title) {
  if (!title || typeof title !== 'string') {
    // 1. 방어 코드: title이 null이거나 문자열이 아니면 null 반환
    return null;
  }

  // 2. 강력하게 수정: 
  //    - trim(): " 창의나래관 " 같은 공백 제거
  //    - toLowerCase(): "Chang-Ui" 같은 영문/대소문자 무시
  const cleanTitle = title.trim().toLowerCase();

  // 3. 콘솔 로그로 확인 (가장 중요!)
  console.log(`[sceneId 매핑] 원본: "${title}" -> 변환: "${cleanTitle}"`);

  if (cleanTitle.includes('창의나래관')) {
    return 'hall_1_entrance';
  }
  if (cleanTitle.includes('어린이과학관')) {
    return 'hall_2_entrance';
  }
  if (cleanTitle.includes('과학기술관')) {
    return 'hall_3_entrance';
  }
  if (cleanTitle.includes('자연사관')) {
    return 'hall_4_entrance';
  }
  if (cleanTitle.includes('인류관')) {
    return 'hall_5_entrance';
  }
  if (cleanTitle.includes('어린이 과학놀이터')) {
    return 'hall_6_entrance';
  }

  if (cleanTitle.includes('역사의 광장')) {
    return 'hall_7_entrance';
  }
  if (cleanTitle.includes('꿈아띠체험관')) {
    return 'hall_8_entrance';
  }
  if (cleanTitle.includes('천체관')) {
    return 'hall_9_entrance';
  }
  if (cleanTitle.includes('생물탐구관')) {
    return 'hall_10_entrance';
  }
  if (cleanTitle.includes('미래기술관')) {
    return 'hall_11_entrance';
  }
  if (cleanTitle.includes('천체관측소')) {
    return 'hall_12_entrance';
  }

  if (cleanTitle.includes('우주과학공원')) {
    return 'hall_13_entrance';
  }
  if (cleanTitle.includes('자기부상열차역사관')) {
    return 'hall_14_entrance';
  }

  // 4. 키워드를 못찾으면 null 반환
  console.warn(`[sceneId 매핑] "${cleanTitle}"에서 키워드를 찾지 못해 null 반환`);
  return null;
}

// (나중에 다른 공통 매핑 함수가 필요하면 여기에 또 추가하면 됩니다)