/**
 * 전시관 key(ID)와 층별 안내도 정보를 수동으로 매핑합니다.
 * 이 key는 /api/halls에서 오는 key와 반드시 일치해야 합니다.
 * imageUrl은 /public 폴더 기준의 절대 경로입니다.
 */
export const floorGuideMap = {
  // key: 1 (자연사관)
  1: [{ floorName: '1F', imageUrl: '/img/floors/nature_1f.png' }],
  // key: 2 (인류관)
  2: [{ floorName: '1F', imageUrl: '/img/floors/nature_1f.png' }],
  // key: 3 (창의나래관)
  3: [
    { floorName: '1F', imageUrl: '/img/floors/changui_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/changui_2f.png' },
    { floorName: '3F', imageUrl: '/img/floors/changui_3f.png' },
  ],
  // key: 4 (어린이 과학놀이터)
  4: [{ floorName: '지상', imageUrl: '/img/floors/children_science_playground.png' }],
  // key: 5 (역사의 광장)
  5: [{ floorName: '지상', imageUrl: '/img/floors/history_ground.png' }],
  // key: 6 (과학기술관 중지하층)
  6: [{ floorName: 'B1', imageUrl: '/img/floors/sciencetech_b1_middle.png' }],
  // key: 7 (과학기술관 1층)
  7: [{ floorName: '1F', imageUrl: '/img/floors/sciencetech_1f.png' }],
  // key: 8 (꿈아띠 체험관)
  8: [{ floorName: '1F', imageUrl: '/img/floors/dream_atthi_1f.png' }],
  // key: 9 (천체관)
  9: [{ floorName: '1F', imageUrl: '/img/floors/cheonchae_1f.png' }],
  // key: 10 (생물탐구관)
  10: [{ floorName: '1F', imageUrl: '/img/floors/sangmul_tamgu_1f.png' }],
  // key: 11 (미래기술관 1F)
  11: [{ floorName: '1F', imageUrl: '/img/floors/future_science_1f.png' }],
  // key: 12 (미래기술관 2F)
  12: [{ floorName: '1F', imageUrl: '/img/floors/future_science_1f.png' }],
  // key: 13 (천체관측소 2F)
  13: [{ floorName: '2F', imageUrl: '/img/floors/cheonchae_look_2f.png' }],
  // key: 14 (어린이 과학관 1F)
  14: [{ floorName: '1F', imageUrl: '/img/floors/children_science_1f.png' }],
  // key: 15 (어린이 과학관 2F)
  15: [{ floorName: '2F', imageUrl: '/img/floors/children_science_2f.png' }],
  // key: 16 (우주 과학공원)
  16: [{ floorName: '지상', imageUrl: '/img/floors/ugu_science_playground.png' }],
  // key: 17 (자기 부상 열차 역사관)
  17: [{ floorName: '지상', imageUrl: '/img/floors/chagi_busang_train.png' }],
}

// 매핑 정보가 없는 경우 사용할 기본값
export const defaultGuide = [{ floorName: '안내도', imageUrl: '/img/floors/default.png' }]
