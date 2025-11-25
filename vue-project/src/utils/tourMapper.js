// 📍 파일 위치: src/utils/tourMapper.js

// [수정] tourConfig.js에서 만든 두 개의 코스를 가져옵니다.
import { courseDemo, Course_2_Out_Chang_Sci } from '@/data/tourConfig.js'

/**
 * 장소 제목(title)을 기반으로 tourConfig.js의 sceneId를 매핑합니다.
 * (이 함수는 기존과 동일합니다. 제목을 ID로 바꾸는 역할)
 */
export function getSceneIdFromTitle(title) {
  if (!title || typeof title !== 'string') {
    return null
  }
  const cleanTitle = title.trim().toLowerCase()

  // 1. 코스 B (신규 10개) 씬 ID 매핑
  if (cleanTitle.includes('가상현실라이더')) return 'hall_1_ex1'
  if (cleanTitle.includes('개방형 수장고')) return 'hall_1_ex2'
  if (cleanTitle.includes('괴짜과학자의 바이러스')) return 'hall_1_ex3'
  if (cleanTitle.includes('디지털 물리쇼')) return 'hall_1_ex4'
  if (cleanTitle.includes('로봇쇼')) return 'hall_1_ex5'
  if (cleanTitle.includes('맵핑영상체험')) return 'hall_1_ex6'
  if (cleanTitle.includes('전기쇼')) return 'hall_1_ex7'
  if (cleanTitle.includes('증강현실') || cleanTitle.includes('ar')) return 'hall_1_ex8'
  if (cleanTitle.includes('팝드론')) return 'hall_1_ex9'
  if (cleanTitle.includes('화성테라포밍')) return 'hall_1_ex10'

  // 2. 야외 전시
  if (cleanTitle.includes('자기부상열차역사관') || cleanTitle.includes('야외전시')) {
    return 'hall_14_entrance'
  }

  // 3. 공통 및 코스 A 매핑
  if (cleanTitle.includes('창의나래관')) return 'hall_1_entrance'
  if (cleanTitle.includes('어린이과학관')) return 'hall_2_entrance'
  if (cleanTitle.includes('과학기술관') || cleanTitle.includes('생활체험과학관')) return 'hall_3_entrance'
  
  // (나머지 관 매핑 유지...)
  if (cleanTitle.includes('자연사관')) return 'hall_4_entrance'
  if (cleanTitle.includes('인류관')) return 'hall_5_entrance'
  if (cleanTitle.includes('어린이 과학놀이터')) return 'hall_6_entrance'
  if (cleanTitle.includes('역사의 광장')) return 'hall_7_entrance'
  if (cleanTitle.includes('꿈아띠체험관')) return 'hall_8_entrance'
  if (cleanTitle.includes('천체관')) return 'hall_9_entrance'
  if (cleanTitle.includes('생물탐구관')) return 'hall_10_entrance'
  if (cleanTitle.includes('미래기술관')) return 'hall_11_entrance'
  if (cleanTitle.includes('천체관측소')) return 'hall_12_entrance'
  if (cleanTitle.includes('우주과학공원')) return 'hall_13_entrance'

  console.warn(`[sceneId 매핑] "${cleanTitle}"에서 키워드를 찾지 못해 null 반환`)
  return null
}

/**
 * [수정됨] 백엔드에서 받은 코스 목록의 '첫 번째 장소'를 보고
 * 1번 코스(창의 시작)를 쓸지, 2번 코스(야외 시작)를 쓸지 결정합니다.
 */
export function getMasterConfig(tourItems) {
  
  // 1. 데이터가 없으면 안전하게 기본값(1번 코스) 반환
  if (!tourItems || tourItems.length === 0) {
    console.log("⚠️ [Mapper] 코스 데이터 없음 -> 기본(1번 코스) 반환");
    return courseDemo;
  }

  // 2. 첫 번째 아이템의 sceneId를 확인 (시작 지점 파악)
  const startSceneId = tourItems[0].sceneId || '';
  console.log(`🕵️ [Mapper] 감지된 시작 지점: ${startSceneId}`);

  // -------------------------------------------------------
  // 🎯 코스 선택 로직
  // -------------------------------------------------------

  // [Case 2] 야외전시(hall_14)로 시작하는 경우 -> 2번 코스 반환
  if (startSceneId.startsWith('hall_14_')) {
    console.log("👉 [Mapper] 야외전시 시작 감지 -> '2번 코스' 로드");
    return Course_2_Out_Chang_Sci;
  }

  // [Case 1] 창의나래관(hall_1)으로 시작하는 경우 -> 1번 코스 반환
  if (startSceneId.startsWith('hall_1_')) {
    console.log("👉 [Mapper] 창의나래관 시작 감지 -> '1번 코스(Demo)' 로드");
    return courseDemo;
  }

  // [Case 3] 과학기술관(hall_3)으로 시작하는 경우 (나중에 3번 코스 만들면 여기에 추가)
  // if (startSceneId.startsWith('hall_3_')) { return Course_3_... }

  // [Default] 매칭되는 게 없으면 무조건 1번 코스
  console.log("👉 [Mapper] 매칭되는 시작점 없음 -> '1번 코스' 기본 반환");
  return courseDemo;
}