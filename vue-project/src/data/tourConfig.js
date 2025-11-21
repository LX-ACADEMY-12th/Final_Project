// 📍 파일 위치: src/data/tourConfig.js

// ===================================================================
// [코스 A] 기존 코스 (창의나래관 4개 -> 어린이과학관 -> 과학기술관)
// ===================================================================
export const courseA_default = {
  default: {
    firstScene: 'hall_1_entrance', // 시작은 '창의나래관 입구'
    autoLoad: true,
    sceneFadeDuration: 1000,
  },
  scenes: {
    // 1. 창의나래관 (총 4개 장면)
    hall_1_entrance: {
      title: '창의나래관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg',
      hotSpots: [
        {
          pitch: -2.1,
          yaw: 132.9,
          type: 'scene',
          text: '디지털 물리쇼 (앞으로)',
          sceneId: 'hall_1_ex1',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },
    hall_1_ex1: {
      title: '창의나래관: 디지털 물리쇼', // (코스 A의 ex1)
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex1.jpg',
      hotSpots: [
        { pitch: -0.6, yaw: 37.7, type: 'scene', text: '전기쇼 (앞으로)', sceneId: 'hall_1_ex2' },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '입구 (뒤로)',
          sceneId: 'hall_1_entrance',
        },
      ],
    },
    hall_1_ex2: {
      title: '창의나래관: 전기쇼', // (코스 A의 ex2)
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex2.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '팝드론 (앞으로)', sceneId: 'hall_1_ex3' },
        { pitch: 0, yaw: 160, type: 'scene', text: '디지털 물리쇼 (뒤로)', sceneId: 'hall_1_ex1' },
      ],
    },
    hall_1_ex3: {
      title: '창의나래관: 팝드론', // (코스 A의 ex3)
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex3.jpg',
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관으로 (어린이과학관)',
          sceneId: 'hall_2_entrance', // 2. 어린이과학관으로 연결
        },
        { pitch: 0, yaw: 160, type: 'scene', text: '전기쇼 (뒤로)', sceneId: 'hall_1_ex2' },
      ],
    },

    // 2. 어린이과학관 (총 5개 장면)
    hall_2_entrance: {
      title: '어린이과학관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '공존 (앞으로)', sceneId: 'hall_2_ex1' },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (창의나래관)',
          sceneId: 'hall_1_ex3',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },
    hall_2_ex1: {
      /* ... (기존 hall_2_ex1 정의) ... */
    },
    hall_2_ex2: {
      /* ... (기존 hall_2_ex2 정의) ... */
    },
    hall_2_ex3: {
      /* ... (기존 hall_2_ex3 정의) ... */
    },
    hall_2_ex4: {
      title: '어린이과학관: 위협',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg',
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관으로 (과학기술관)',
          sceneId: 'hall_3_entrance',
        },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '기계와 인간 (뒤로)',
          sceneId: 'hall_2_ex3',
        },
      ],
    },

    // 3. 과학기술관 (총 4개 장면)
    hall_3_entrance: {
      title: '과학기술관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg',
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '마트, 테마파크 (앞으로)',
          sceneId: 'hall_3_ex1',
        },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (어린이과학관)',
          sceneId: 'hall_2_ex4',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },
    hall_3_ex1: {
      /* ... (기존 hall_3_ex1 정의) ... */
    },
    hall_3_ex2: {
      /* ... (기존 hall_3_ex2 정의) ... */
    },
    hall_3_ex3: {
      /* ... (기존 hall_3_ex3 정의) ... */
    },

    // (이하 4~14관 Placeholder 정의)
    hall_4_entrance: {
      /* ... */
    },
    hall_5_entrance: {
      /* ... */
    },
    hall_6_entrance: {
      /* ... */
    },
    hall_7_entrance: {
      /* ... */
    },
    hall_8_entrance: {
      /* ... */
    },
    hall_9_entrance: {
      /* ... */
    },
    hall_10_entrance: {
      /* ... */
    },
    hall_11_entrance: {
      /* ... */
    },
    hall_12_entrance: {
      /* ... */
    },
    hall_13_entrance: {
      /* ... */
    },
    hall_14_entrance: {
      /* ... (코스 A의 자기부상열차) ... */
    },
  },
}

// ===================================================================
// [코스 B] 신규 코스 (창의나래관 10개 -> 야외전시 -> 과학기술관)
// ===================================================================
export const courseB_new = {
  default: {
    firstScene: 'hall_1_entrance', // 시작은 코스 A와 동일한 ID
    autoLoad: true,
    sceneFadeDuration: 1000,
  },
  scenes: {
    // --- 1. 창의나래관 (10개 전시물 코스) ---
    hall_1_entrance: {
      title: '창의나래관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg',
      hotSpots: [
        {
          pitch: -2.1,
          yaw: 132.9,
          type: 'scene',
          text: '1. 가상현실라이더 (코스 시작)',
          sceneId: 'hall_1_ex1',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },
    hall_1_ex1: {
      // 1. 가상현실라이더
      title: '창의나래관: 가상현실라이더',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // (임시 1)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '2. 개방형 수장고 (앞으로)',
          sceneId: 'hall_1_ex2',
        },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '입구 (뒤로)',
          sceneId: 'hall_1_entrance',
        },
      ],
    },
    hall_1_ex2: {
      // 2. 개방형 수장고
      title: '창의나래관: 개방형 수장고',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // (임시 2)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '3. 괴짜과학자의 바이러스 (앞으로)',
          sceneId: 'hall_1_ex3',
        },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '1. 가상현실라이더 (뒤로)',
          sceneId: 'hall_1_ex1',
        },
      ],
    },
    hall_1_ex3: {
      // 3. 괴짜과학자
      title: '창의나래관: 괴짜과학자의 바이러스',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // (임시 1)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '4. 디지털 물리쇼 (앞으로)',
          sceneId: 'hall_1_ex4',
        },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '2. 개방형 수장고 (뒤로)',
          sceneId: 'hall_1_ex2',
        },
      ],
    },
    hall_1_ex4: {
      // 4. 디지털 물리쇼
      title: '창의나래관: 디지털 물리쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex1.jpg', // (임시)
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '5. 로봇쇼 (앞으로)', sceneId: 'hall_1_ex5' },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '3. 괴짜과학자의 바이러스 (뒤로)',
          sceneId: 'hall_1_ex3',
        },
      ],
    },
    hall_1_ex5: {
      // 5. 로봇쇼
      title: '창의나래관: 로봇쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // (임시 2)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '6. 맵핑영상체험 (앞으로)',
          sceneId: 'hall_1_ex6',
        },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '4. 디지털 물리쇼 (뒤로)',
          sceneId: 'hall_1_ex4',
        },
      ],
    },
    hall_1_ex6: {
      // 6. 맵핑영상
      title: '창의나래관: 맵핑영상체험',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // (임시 1)
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '7. 전기쇼 (앞으로)', sceneId: 'hall_1_ex7' },
        { pitch: 0, yaw: 160, type: 'scene', text: '5. 로봇쇼 (뒤로)', sceneId: 'hall_1_ex5' },
      ],
    },
    hall_1_ex7: {
      // 7. 전기쇼
      title: '창의나래관: 전기쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex2.jpg', // (임시)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '8. 증강현실(AR) (앞으로)',
          sceneId: 'hall_1_ex8',
        },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '6. 맵핑영상체험 (뒤로)',
          sceneId: 'hall_1_ex6',
        },
      ],
    },
    hall_1_ex8: {
      // 8. AR
      title: '창의나래관: 증강현실(AR)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // (임시 2)
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '9. 팝드론 (앞으로)', sceneId: 'hall_1_ex9' },
        { pitch: 0, yaw: 160, type: 'scene', text: '7. 전기쇼 (뒤로)', sceneId: 'hall_1_ex7' },
      ],
    },
    hall_1_ex9: {
      // 9. 팝드론
      title: '창의나래관: 팝드론',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex3.jpg', // (임시)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '10. 화성테라포밍 (앞으로)',
          sceneId: 'hall_1_ex10',
        },
        {
          pitch: 0,
          yaw: 160,
          type: 'scene',
          text: '8. 증강현실(AR) (뒤로)',
          sceneId: 'hall_1_ex8',
        },
      ],
    },
    hall_1_ex10: {
      // 10. 화성테라포밍
      title: '창의나래관: 화성테라포밍',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // (임시 1)
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '다음 (야외전시)', sceneId: 'hall_14_entrance' }, // 2. 야외전시로 연결
        { pitch: 0, yaw: 160, type: 'scene', text: '9. 팝드론 (뒤로)', sceneId: 'hall_1_ex9' },
      ],
    },

    // 2. 야외전시 (자기부상열차 역사관)
    hall_14_entrance: {
      title: '야외전시: 자기부상열차 역사관',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // (임시 2)
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 (과학기술관)',
          sceneId: 'hall_3_entrance',
        }, // 3. 과학기술관으로 연결
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 (창의나래관)',
          sceneId: 'hall_1_ex10',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },

    // 3. 과학기술관 (생활체험과학관)
    hall_3_entrance: {
      title: '과학기술관: 생활체험과학관',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg',
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (야외전시)',
          sceneId: 'hall_14_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '처음으로 (창의나래관)',
          sceneId: 'hall_1_entrance',
        },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다. ' },
      ],
    },
  },
}
