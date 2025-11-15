// 📍 파일 위치: src/data/tourConfig.js

export const tourConfig = {
  default: {
    firstScene: 'hall_1_entrance', // 시작은 '창의나래관 입구'
    autoLoad: true,
    sceneFadeDuration: 1000,
  },
  scenes: {
    // ======================================================
    // 1. 창의나래관 (총 4개 장면)
    // ======================================================
    hall_1_entrance: {
      title: '창의나래관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg',
      hotSpots: [
        {
          pitch: -2.1,
          yaw: 132.9,
          type: 'scene',
          text: '디지털 물리쇼 (앞으로)',
          sceneId: 'hall_1_ex1',
        },
        // [추가] 입구에서도 바로 종료 가능
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    hall_1_ex1: {
      title: '창의나래관: 디지털 물리쇼',
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
      title: '창의나래관: 전기쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex2.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '팝드론 (앞으로)', sceneId: 'hall_1_ex3' },
        { pitch: 0, yaw: 160, type: 'scene', text: '디지털 물리쇼 (뒤로)', sceneId: 'hall_1_ex1' },
      ],
    },
    hall_1_ex3: {
      title: '창의나래관: 팝드론',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/chang_ex3.jpg',
      hotSpots: [
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관으로 (어린이과학관)',
          sceneId: 'hall_2_entrance',
        },
        { pitch: 0, yaw: 160, type: 'scene', text: '전기쇼 (뒤로)', sceneId: 'hall_1_ex2' },
      ],
    },

    // ======================================================
    // 2. 어린이과학관 (총 5개 장면)
    // ======================================================
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
          text: '이전 관으로 (창의나래관)',
          sceneId: 'hall_1_ex3',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    hall_2_ex1: {
      title: '어린이과학관: 공존',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/child_ex1.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '극복 (앞으로)', sceneId: 'hall_2_ex2' },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '입구 (뒤로)',
          sceneId: 'hall_2_entrance',
        },
      ],
    },
    hall_2_ex2: {
      title: '어린이과학관: 극복',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '기계와 인간 (앞으로)', sceneId: 'hall_2_ex3' },
        { pitch: -2.5, yaw: -150.0, type: 'scene', text: '공존 (뒤로)', sceneId: 'hall_2_ex1' },
      ],
    },
    hall_2_ex3: {
      title: '어린이과학관: 기계와 인간',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/child_ex3.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '위협 (앞으로)', sceneId: 'hall_2_ex4' },
        { pitch: -2.5, yaw: -150.0, type: 'scene', text: '극복 (뒤로)', sceneId: 'hall_2_ex2' },
      ],
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

    // ======================================================
    // 3. 과학기술관 (총 4개 장면)
    // ======================================================
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
          text: '이전 관으로 (어린이과학관)',
          sceneId: 'hall_2_ex4',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    hall_3_ex1: {
      title: '과학기술관: 마트, 테마파크',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '이웃, 동네 (앞으로)', sceneId: 'hall_3_ex2' },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '입구 (뒤로)',
          sceneId: 'hall_3_entrance',
        },
      ],
    },
    hall_3_ex2: {
      title: '과학기술관: 이웃, 동네',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg',
      hotSpots: [
        { pitch: -10, yaw: 0, type: 'scene', text: '집, 가정 (앞으로)', sceneId: 'hall_3_ex3' },
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '마트, 테마파크 (뒤로)',
          sceneId: 'hall_3_ex1',
        },
      ],
    },
    hall_3_ex3: {
      title: '과학기술관: 집, 가정 (답사 종료)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg',
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이웃, 동네 (뒤로)',
          sceneId: 'hall_3_ex2',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '처음으로 (창의나래관)',
          sceneId: 'hall_1_entrance',
        },
        {
          pitch: -10,
          yaw: 50,
          type: 'scene',
          text: '다음 관 (자연사관)',
          sceneId: 'hall_4_entrance',
        },
        // (이미 존재)
        {
          pitch: -10,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },

    // ======================================================
    // 4. 자연사관 (Placeholder)
    // ======================================================
    hall_4_entrance: {
      title: '자연사관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (과학기술관)',
          sceneId: 'hall_3_ex3',
        },
        { pitch: -10, yaw: 0, type: 'scene', text: '다음 관 (인류관)', sceneId: 'hall_5_entrance' },
        // (이미 존재)
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 5. 인류관 (Placeholder)
    // ======================================================
    hall_5_entrance: {
      title: '인류관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (자연사관)',
          sceneId: 'hall_4_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (어린이 과학놀이터)',
          sceneId: 'hall_6_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 6. 어린이 과학놀이터 (Placeholder)
    // ======================================================
    hall_6_entrance: {
      title: '어린이 과학놀이터 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (인류관)',
          sceneId: 'hall_5_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (역사의 광장)',
          sceneId: 'hall_7_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 7. 역사의 광장 (Placeholder)
    // ======================================================
    hall_7_entrance: {
      title: '역사의 광장 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (과학놀이터)',
          sceneId: 'hall_6_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (꿈아띠체험관)',
          sceneId: 'hall_8_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 8. 꿈아띠체험관 (Placeholder)
    // ======================================================
    hall_8_entrance: {
      title: '꿈아띠체험관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/child_ex1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (역사의 광장)',
          sceneId: 'hall_7_entrance',
        },
        { pitch: -10, yaw: 0, type: 'scene', text: '다음 관 (천체관)', sceneId: 'hall_9_entrance' },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 9. 천체관 (Placeholder)
    // ======================================================
    hall_9_entrance: {
      title: '천체관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (꿈아띠)',
          sceneId: 'hall_8_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (생물탐구관)',
          sceneId: 'hall_10_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 10. 생물탐구관 (Placeholder)
    // ======================================================
    hall_10_entrance: {
      title: '생물탐구관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (천체관)',
          sceneId: 'hall_9_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (미래기술관)',
          sceneId: 'hall_11_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 11. 미래기술관 (Placeholder)
    // ======================================================
    hall_11_entrance: {
      title: '미래기술관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (생물탐구관)',
          sceneId: 'hall_10_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (천체관측소)',
          sceneId: 'hall_12_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 12. 천체관측소 (Placeholder)
    // ======================================================
    hall_12_entrance: {
      title: '천체관측소 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/child_ex1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (미래기술관)',
          sceneId: 'hall_11_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (우주과학공원)',
          sceneId: 'hall_13_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 13. 우주과학공원 (Placeholder)
    // ======================================================
    hall_13_entrance: {
      title: '우주과학공원 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img1.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (천체관측소)',
          sceneId: 'hall_12_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '다음 관 (자기부상열차)',
          sceneId: 'hall_14_entrance',
        },
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
    // ======================================================
    // 14. 자기부상열차역사관 (Placeholder)
    // ======================================================
    hall_14_entrance: {
      title: '자기부상열차역사관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/img2.jpg', // 임시 이미지
      hotSpots: [
        {
          pitch: -2.5,
          yaw: -150.0,
          type: 'scene',
          text: '이전 관 (우주과학공원)',
          sceneId: 'hall_13_entrance',
        },
        {
          pitch: -10,
          yaw: 0,
          type: 'scene',
          text: '처음으로 (창의나래관)',
          sceneId: 'hall_1_entrance',
        }, // 다시 처음으로 순환
        // [추가]
        {
          pitch: -30,
          yaw: 90,
          type: 'info',
          text: '가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)',
        },
      ],
    },
  },
}
