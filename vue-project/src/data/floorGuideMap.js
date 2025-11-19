/**
 * 전시관 key(ID)와 층별 안내도 정보를 수동으로 매핑합니다.
 * 이 key는 /api/halls에서 오는 key와 반드시 일치해야 합니다.
 * imageUrl은 /public 폴더 기준의 절대 경로입니다.
 */
export const floorGuideMap = {
  // key: 1 (자연사관)
  1: [{ floorName: '1F', imageUrl: '/img/floors/nature_1f.png' }],
  // key: 2 (인류관)
  2: [{ floorName: '2F', imageUrl: '/img/floors/human_2f.png' }],
  // key: 3 (창의나래관)
  3: [
    { floorName: '1F', imageUrl: '/img/floors/changui_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/changui_2f.png' },
    { floorName: '3F', imageUrl: '/img/floors/changui_3f.png' },
  ],
  // key: 6 (야외전시관)
  6: [{ floorName: '1F', imageUrl: '/img/floors/outdoor.png' }],
  // key: 7 (과학기술관)
  7: [
    { floorName: 'B1', imageUrl: '/img/floors/sciencetech_b1.png' },
    { floorName: 'B1_M', imageUrl: '/img/floors/sciencetech_b1_middle.png' },
    { floorName: '1F', imageUrl: '/img/floors/sciencetech_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/sciencetech_2f.png' },
  ],
  // key: 12 (천체관)
  12: [{ floorName: '1F', imageUrl: '/img/floors/cheonchae_1f.png' }],
  // key: 13 (생물탐구관)
  13: [{ floorName: '1F', imageUrl: '/img/floors/sangmul_tamgu_1f.png' }],
  // key: 16 (미래기술관)
  16: [
    { floorName: '1F', imageUrl: '/img/floors/future_science_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/future_science_2f.png' },
  ],
  // kye: 18 (천체관측소)
  18: [
    { floorName: '1F', imageUrl: '/img/floors/cheonchae_look_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/cheonchae_look_2f.png' },
  ],
  // key: 43 (어린이 과학관 1F)
  43: [
    { floorName: '1F', imageUrl: '/img/floors/children_science_1f.png' },
    { floorName: '2F', imageUrl: '/img/floors/children_science_2f.png' },
  ],
}

// 매핑 정보가 없는 경우 사용할 기본값
export const defaultGuide = [{ floorName: '안내도', imageUrl: '/img/floors/default.png' }]
