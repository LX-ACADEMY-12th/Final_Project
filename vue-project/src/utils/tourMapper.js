// 📍 파일 위치: src/utils/tourMapper.js

// [수정] 2개의 코스 설정을 모두 import
import { courseA_default, courseB_new } from '@/data/tourConfig.js'

/**
 * 장소 제목(title)을 기반으로 tourConfig.js의 sceneId를 매핑합니다.
 * @param {string | null | undefined} title - 장소 제목
 * @returns {string | null} - 매핑된 sceneId (예: 'hall_1_entrance') 또는 null
 */
export function getSceneIdFromTitle(title) {
  if (!title || typeof title !== 'string') {
    return null
  }
  const cleanTitle = title.trim().toLowerCase()

  // 1. 코스 B (신규 10개) 씬 ID 매핑
  if (cleanTitle.includes('가상현실라이더')) {
    return 'hall_1_ex1'
  }
  if (cleanTitle.includes('개방형 수장고')) {
    return 'hall_1_ex2'
  }
  if (cleanTitle.includes('괴짜과학자의 바이러스')) {
    return 'hall_1_ex3'
  }
  if (cleanTitle.includes('디지털 물리쇼')) {
    // [참고] 코스 A와 B에 둘 다 '디지털 물리쇼'가 있다면,
    // tourMapper.js만으로는 구분이 불가능할 수 있습니다.
    // 지금은 코스 B의 ID(hall_1_ex4)로 우선 매핑합니다.
    return 'hall_1_ex4'
  }
  if (cleanTitle.includes('로봇쇼')) {
    return 'hall_1_ex5'
  }
  if (cleanTitle.includes('맵핑영상체험')) {
    return 'hall_1_ex6'
  }
  if (cleanTitle.includes('전기쇼')) {
    return 'hall_1_ex7'
  }
  if (cleanTitle.includes('증강현실') || cleanTitle.includes('ar')) {
    return 'hall_1_ex8'
  }
  if (cleanTitle.includes('팝드론')) {
    return 'hall_1_ex9'
  }
  if (cleanTitle.includes('화성테라포밍')) {
    return 'hall_1_ex10'
  }

  // 2. 코스 B (야외 전시)
  if (cleanTitle.includes('자기부상열차역사관') || cleanTitle.includes('야외전시')) {
    return 'hall_14_entrance' // 코스 B의 야외전시 씬 ID
  }

  // 3. 공통 및 코스 A 매핑
  if (cleanTitle.includes('창의나래관')) {
    return 'hall_1_entrance'
  }
  if (cleanTitle.includes('어린이과학관')) {
    return 'hall_2_entrance'
  }
  if (cleanTitle.includes('과학기술관') || cleanTitle.includes('생활체험과학관')) {
    return 'hall_3_entrance'
  }
  if (cleanTitle.includes('자연사관')) {
    return 'hall_4_entrance'
  }
  if (cleanTitle.includes('인류관')) {
    return 'hall_5_entrance'
  }
  if (cleanTitle.includes('어린이 과학놀이터')) {
    return 'hall_6_entrance'
  }
  if (cleanTitle.includes('역사의 광장')) {
    return 'hall_7_entrance'
  }
  if (cleanTitle.includes('꿈아띠체험관')) {
    return 'hall_8_entrance'
  }
  if (cleanTitle.includes('천체관')) {
    return 'hall_9_entrance'
  }
  if (cleanTitle.includes('생물탐구관')) {
    return 'hall_10_entrance'
  }
  if (cleanTitle.includes('미래기술관')) {
    return 'hall_11_entrance'
  }
  if (cleanTitle.includes('천체관측소')) {
    return 'hall_12_entrance'
  }
  if (cleanTitle.includes('우주과학공원')) {
    return 'hall_13_entrance'
  }

  // 4. 키워드를 못찾으면 null 반환
  console.warn(`[sceneId 매핑] "${cleanTitle}"에서 키워드를 찾지 못해 null 반환`)
  return null
}

/**
 * [신규 추가]
 * 백엔드에서 받은 코스 아이템 목록(tourItems)을 분석하여
 * '코스 A' 설정을 쓸지, '코스 B' 설정을 쓸지 결정합니다.
 *
 * @param {Array} tourItems - sceneId가 포함된 코스 아이템 배열
 * @returns {Object} - courseA_default 또는 courseB_new 설정 객체
 */
export function getMasterConfig(tourItems) {
  if (!tourItems || tourItems.length === 0) {
    console.log("[getMasterConfig] tourItems가 비어있어 '코스 A' (기본)를 반환합니다.")
    return courseA_default // 기본값
  }

  const sceneIdSet = new Set(tourItems.map((item) => item.sceneId))

  // [코스 B] 식별 키: '야외전시/자기부상'(hall_14_entrance) 또는
  // 코스 B의 10개 전시물 중 하나(예: 'hall_1_ex10')가 포함되어 있다면 '코스 B' 사용
  if (
    sceneIdSet.has('hall_14_entrance') ||
    sceneIdSet.has('hall_1_ex10') ||
    sceneIdSet.has('hall_1_ex1')
  ) {
    console.log("[getMasterConfig] '코스 B' (신규 코스) 키를 발견. '코스 B' 설정을 반환합니다.")
    return courseB_new
  }

  // [코스 A] 식별 키: '어린이과학관'(hall_2_entrance)이 포함되어 있다면 '코스 A' 사용
  if (sceneIdSet.has('hall_2_entrance')) {
    console.log("[getMasterConfig] '코스 A' (기존 코스) 키를 발견. '코스 A' 설정을 반환합니다.")
    return courseA_default
  }

  // 둘 다 아니면 '코스 A' (기본값) 반환
  console.log("[getMasterConfig] 특정 코스 키를 찾지 못함. '코스 A' (기본)를 반환합니다.")
  return courseA_default
}
