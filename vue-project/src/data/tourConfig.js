// ===================================================================
// [시연용 코스] 창의나래(4) -> 과학기술(4) -> 야외(3)
// ===================================================================
export const courseDemo = {
  default: {
    firstScene: 'hall_1_entrance',
    autoLoad: true,
    sceneFadeDuration: 1000,
  },
  scenes: {
    // =================================================================================
    // 1. 창의나래관 (Start)
    // =================================================================================
    hall_1_entrance: {
      title: '창의나래관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_Hall_02.png',
      hotSpots: [
        // (1)  전시물 순서대로 보러가기
        { pitch: -5, yaw: 0, type: 'scene', text: '전시물 관람 시작 (4개)', sceneId: 'hall_1_seq_1' },
        
        // (2) 다음 관으로 건너뛰기 (간격 벌림)
        { pitch: 0, yaw: 60, type: 'scene', text: '바로 다음 관 이동 (과학기술관)', sceneId: 'hall_3_entrance' },

        // (3) ❌ 종료 버튼 (모든 화면 공통)
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 창의나래관 시퀀스 ---
    hall_1_seq_1: {
      title: '디지털 물리쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_PhygitalShow.jpg', 
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 전시물 (2/4)', sceneId: 'hall_1_seq_2' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로 (입구)', sceneId: 'hall_1_entrance' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' } // 추가됨
      ]
    },
    hall_1_seq_2: {
      title: '가상현실라이더',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_VRRider.jpg', 
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 전시물 (3/4)', sceneId: 'hall_1_seq_3' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_1' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' } // 추가됨
      ]
    },
    hall_1_seq_3: {
      title: '팝 드론',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_POPDRONE.jpg', 
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 전시물 (4/4)', sceneId: 'hall_1_seq_4' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_2' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' } // 추가됨
      ]
    },
    hall_1_seq_4: {
      title: '로봇쇼',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_RobotShow.jpg', 
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '관람 끝! 과학기술관으로 이동', sceneId: 'hall_3_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_3' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' } // 추가됨
      ]
    },

    // =================================================================================
    // 2. 과학기술관 (중간)
    // =================================================================================
    hall_3_entrance: {
      title: '과학기술관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Hall_02.png',
      hotSpots: [
        // (1)  전시물 순서대로
        { pitch: -5, yaw: 0, type: 'scene', text: '전시물 관람 시작 (4개)', sceneId: 'hall_3_seq_1' },
        
        // (2) 다음 관 이동 (★ 각도를 60도로 넓혀서 잘 보이게 수정)
        { pitch: 0, yaw: 60, type: 'scene', text: '바로 다음 관 이동 (야외전시)', sceneId: 'hall_14_entrance' },
        
        // (3) 이전 관 돌아가기 (★ 각도를 -60도로 넓힘)
        { pitch: 0, yaw: -60, type: 'scene', text: '이전 관 (창의나래관)', sceneId: 'hall_1_entrance' },

        // (4) ❌ 종료 버튼
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 과학기술관 시퀀스 ---
    hall_3_seq_1: {
      title: '야외·공원',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_OutdoorNPark.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 전시물 (2/4)', sceneId: 'hall_3_seq_2' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_entrance' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' } // 추가됨
      ]
    },
    hall_3_seq_2: {
      title: '이웃 동네', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Neighbor.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 (3/4)', sceneId: 'hall_3_seq_3' }, 
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_1' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },
    hall_3_seq_3: { 
      title: '물리코너', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Physics.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 (4/4)', sceneId: 'hall_3_seq_4' }, 
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_2' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },
    hall_3_seq_4: {
      title: '한국과학기술사', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_HistoryOfScienceNTechnologyInKorea.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '관람 끝! 야외전시로 이동', sceneId: 'hall_14_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_3' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },

    // =================================================================================
    // 3. 야외전시 (마지막)
    // =================================================================================
    hall_14_entrance: {
      title: '야외전시 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/OutdoorExhibition.jpg', 
      hotSpots: [
        { pitch: -5, yaw: 0, type: 'scene', text: '전시물 관람 시작 (3개)', sceneId: 'hall_14_seq_1' },
        { pitch: 0, yaw: 60, type: 'scene', text: '처음으로 (창의나래관)', sceneId: 'hall_1_entrance' },
        { pitch: 0, yaw: -60, type: 'scene', text: '이전 관 (과학기술관)', sceneId: 'hall_3_entrance' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 야외전시 시퀀스 ---
    hall_14_seq_1: {
      title: '역사의 광장', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Square_of_History.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 (2/3)', sceneId: 'hall_14_seq_2' }, 
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_entrance' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },
    hall_14_seq_2: {
      title: '우주과학공원', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Space_Science_Park.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '다음 (3/3)', sceneId: 'hall_14_seq_3' }, 
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_seq_1' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },
    hall_14_seq_3: {
      title: '자기부상열차 역사관', type: 'equirectangular', 
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_Playground.jpg',
      hotSpots: [
        { pitch: 0, yaw: 0, type: 'scene', text: '모든 관람 끝! 처음으로', sceneId: 'hall_1_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_seq_2' },
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ]
    },
  },
};

// ===================================================================
// [코스 2] 야외(3) -> 창의(4) -> 과학(4)
// ===================================================================
export const Course_2_Out_Chang_Sci = {
  default: {
    firstScene: 'hall_14_entrance', // ★ 시작점이 야외전시
    autoLoad: true,
    sceneFadeDuration: 1000,
  },
  scenes: {
    // =================================================================================
    // 1. 야외전시 (Start) - 3개
    // =================================================================================
    hall_14_entrance: {
      title: '야외전시 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/OutdoorExhibition.jpg',
      hotSpots: [
        // (1) 전시물 관람 시작
        { pitch: -5, yaw: 0, type: 'scene', text: '전시물 관람 시작 (3개)', sceneId: 'hall_14_seq_1' },
        
        // (2) ★ 다음 관: 창의나래관
        { pitch: 0, yaw: 60, type: 'scene', text: '바로 다음 관 이동 (창의나래관)', sceneId: 'hall_1_entrance' },
        
        // (3) 종료
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 야외전시 시퀀스 ---
    hall_14_seq_1: {
      title: '역사의 광장', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Square_of_History.jpg',
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (2/3)', sceneId: 'hall_14_seq_2' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_entrance' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }]
    },
    hall_14_seq_2: {
      title: '우주과학공원', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Space_Science_Park.jpg',
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (3/3)', sceneId: 'hall_14_seq_3' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_seq_1' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }]
    },
    hall_14_seq_3: {
      title: '자기부상열차 역사관', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_Playground.jpg',
      hotSpots: [
        // ★ 야외전시 끝 -> 창의나래관 입구로 연결
        { pitch: 0, yaw: 0, type: 'scene', text: '관람 끝! 창의나래관으로 이동', sceneId: 'hall_1_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_14_seq_2' },
        { pitch: -30, yaw: 90, type: 'info', text: '종료' }
      ]
    },

    // =================================================================================
    // 2. 창의나래관 (Middle) - 4개
    // =================================================================================
    hall_1_entrance: {
      title: '창의나래관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_Hall_02.png',
      hotSpots: [
        { pitch: -5, yaw: 0, type: 'scene', text: '전시물 관람 시작 (4개)', sceneId: 'hall_1_seq_1' },
        
        // (2) ★ 다음 관: 과학기술관
        { pitch: 0, yaw: 60, type: 'scene', text: '바로 다음 관 이동 (과학기술관)', sceneId: 'hall_3_entrance' },
        
        // (3) ★ 이전 관: 야외전시
        { pitch: 0, yaw: -60, type: 'scene', text: '이전 관 (야외전시)', sceneId: 'hall_14_entrance' },
        
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 창의나래관 시퀀스 ---
    hall_1_seq_1: { title: '디지털 물리쇼', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_PhygitalShow.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (2/4)', sceneId: 'hall_1_seq_2' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_entrance' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_1_seq_2: { title: '가상현실라이더', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_VRRider.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (3/4)', sceneId: 'hall_1_seq_3' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_1' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_1_seq_3: { title: '팝 드론', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_POPDRONE.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (4/4)', sceneId: 'hall_1_seq_4' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_2' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_1_seq_4: {
      title: '로봇쇼', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Changui_Narae_RobotShow.jpg',
      hotSpots: [
        // ★ 창의나래 끝 -> 과학기술관 입구로 연결
        { pitch: 0, yaw: 0, type: 'scene', text: '관람 끝! 과학기술관으로 이동', sceneId: 'hall_3_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_1_seq_3' },
        { pitch: -30, yaw: 90, type: 'info', text: '종료' }
      ]
    },

    // =================================================================================
    // 3. 과학기술관 (End) - 4개
    // =================================================================================
    hall_3_entrance: {
      title: '과학기술관 (입구)',
      type: 'equirectangular',
      panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Home.jpg',
      hotSpots: [
        { pitch: -5, yaw: 0, type: 'scene', text: ' 전시물 관람 시작 (4개)', sceneId: 'hall_3_seq_1' },
        
        // (2) ★ 이전 관: 창의나래관
        { pitch: 0, yaw: -60, type: 'scene', text: '이전 관 (창의나래관)', sceneId: 'hall_1_entrance' },
        
        { pitch: -30, yaw: 90, type: 'info', text: '가상 답사를 종료합니다.' }
      ],
    },
    // --- 과학기술관 시퀀스 ---
    hall_3_seq_1: { title: '야외·공원', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_OutdoorNPark.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (2/4)', sceneId: 'hall_3_seq_2' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_entrance' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_3_seq_2: { title: '이웃 동네', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Neighbor.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (3/4)', sceneId: 'hall_3_seq_3' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_1' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_3_seq_3: { title: '물리코너', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_Physics.jpg', 
      hotSpots: [{ pitch: 0, yaw: 0, type: 'scene', text: '다음 (4/4)', sceneId: 'hall_3_seq_4' }, { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_2' }, { pitch: -30, yaw: 90, type: 'info', text: '종료' }] },
    hall_3_seq_4: {
      title: '한국과학기술사', type: 'equirectangular', panorama: 'https://storage.googleapis.com/virtual_tour_team4/Science_and_Technology_HistoryOfScienceNTechnologyInKorea.jpg',
      hotSpots: [
        // ★ 모든 관람 끝 -> 처음으로(야외전시) 돌아가기
        { pitch: 0, yaw: 0, type: 'scene', text: '모든 관람 끝! 처음으로(야외전시)', sceneId: 'hall_14_entrance' },
        { pitch: 0, yaw: 180, type: 'scene', text: '뒤로', sceneId: 'hall_3_seq_3' },
        { pitch: -30, yaw: 90, type: 'info', text: '종료' }
      ]
    },
  },
};