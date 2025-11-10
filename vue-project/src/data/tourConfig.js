export const tourConfig = {
  "default": {
    "firstScene": "hall_1_entrance", // 시작은 '창의나래관 입구'
    "autoLoad": true,
    "sceneFadeDuration": 1000
  },
  "scenes": {
    // ======================================================
    // 1. 창의나래관 (총 4개 장면)
    // ======================================================
    "hall_1_entrance": {
      "title": "창의나래관 (입구)",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/changhall1.jpg", // 창의 나래관 입구
      "hotSpots": [
        { "pitch": -2.1, "yaw": 132.9, "type": "scene",
          "text": "디지털 물리쇼 (앞으로)",
          "sceneId": "hall_1_ex1" // -> 1-1. 디지털 물리쇼
        }
      ]
    },
    "hall_1_ex1": {
      "title": "창의나래관: 디지털 물리쇼",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/chang_ex1.jpg", // ⚠️ '디지털 물리쇼' 사진 URL로 변경하세요.
      "hotSpots": [
        { "pitch": -0.6, "yaw": 37.7, "type": "scene",
          "text": "전기쇼 (앞으로)",
          "sceneId": "hall_1_ex2" // -> 1-2. 전기쇼
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "입구 (뒤로)",
          "sceneId": "hall_1_entrance" // -> 1. 입구
        }
      ]
    },
    "hall_1_ex2": {
      "title": "창의나래관: 전기쇼",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/chang_ex2.jpg", // ⚠️ '전기쇼' 사진 URL로 변경하세요.
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "팝드론 (앞으로)", 
          "sceneId": "hall_1_ex3" // -> 1-3. 팝드론
        },
        { "pitch": 0, "yaw": 160, "type": "scene",
          "text": "디지털 물리쇼 (뒤로)",
          "sceneId": "hall_1_ex1" // -> 1-1. 디지털 물리쇼
        }
      ]
    },
    "hall_1_ex3": {
      "title": "창의나래관: 팝드론",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/chang_ex3.jpg", // ⚠️ '팝드론' 사진 URL로 변경하세요.
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "다음 관으로 (어린이과학관)", 
          "sceneId": "hall_2_entrance" // -> 2. 어린이과학관 입구
        },
        { "pitch": 0, "yaw": 160, "type": "scene",
          "text": "전기쇼 (뒤로)",
          "sceneId": "hall_1_ex2" // -> 1-2. 전기쇼
        }
      ]
    },

    // ======================================================
    // 2. 어린이과학관 (총 5개 장면)
    // ======================================================
    "hall_2_entrance": {
      "title": "어린이과학관 (입구)",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg", // ⚠️ '어린이과학관 입구' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "공존 (앞으로)",
          "sceneId": "hall_2_ex1" // -> 2-1. 공존
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "이전 관으로 (창의나래관)",
          "sceneId": "hall_1_ex3" // -> 1-3. 팝드론
        }
      ]
    },
    "hall_2_ex1": {
      "title": "어린이과학관: 공존",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/child_ex1.jpg", // ⚠️ '공존' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "극복 (앞으로)",
          "sceneId": "hall_2_ex2" // -> 2-2. 극복
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "입구 (뒤로)",
          "sceneId": "hall_2_entrance" // -> 2. 입구
        }
      ]
    },
    "hall_2_ex2": {
      "title": "어린이과학관: 극복",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/img2.jpg", // ⚠️ '극복' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "기계와 인간 (앞으로)",
          "sceneId": "hall_2_ex3" // -> 2-3. 기계와 인간
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "공존 (뒤로)",
          "sceneId": "hall_2_ex1" // -> 2-1. 공존
        }
      ]
    },
    "hall_2_ex3": {
      "title": "어린이과학관: 기계와 인간",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/child_ex3.jpg", // ⚠️ '기계와 인간' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "위협 (앞으로)",
          "sceneId": "hall_2_ex4" // -> 2-4. 위협
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "극복 (뒤로)",
          "sceneId": "hall_2_ex2" // -> 2-2. 극복
        }
      ]
    },
    "hall_2_ex4": {
      "title": "어린이과학관: 위협",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/img2.jpg", // ⚠️ '위협' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "다음 관으로 (과학기술관)",
          "sceneId": "hall_3_entrance" // -> 3. 과학기술관 입구
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "기계와 인간 (뒤로)",
          "sceneId": "hall_2_ex3" // -> 2-3. 기계와 인간
        }
      ]
    },

    // ======================================================
    // 3. 과학기술관 (총 4개 장면)
    // ======================================================
    "hall_3_entrance": {
      "title": "과학기술관 (입구)",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg", // ⚠️ '과학기술관 입구' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "마트, 테마파크 (앞으로)",
          "sceneId": "hall_3_ex1" // -> 3-1. 마트
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "이전 관으로 (어린이과학관)",
          "sceneId": "hall_2_ex4" // -> 2-4. 위협
        }
      ]
    },
    "hall_3_ex1": {
      "title": "과학기술관: 마트, 테마파크",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/img1.jpg", // ⚠️ '마트, 테마파크' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "이웃, 동네 (앞으로)",
          "sceneId": "hall_3_ex2" // -> 3-2. 이웃
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "입구 (뒤로)",
          "sceneId": "hall_3_entrance" // -> 3. 입구
        }
      ]
    },
    "hall_3_ex2": {
      "title": "과학기술관: 이웃, 동네",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/img2.jpg", // ⚠️ '이웃, 동네' 사진 URL
      "hotSpots": [
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "집, 가정 (앞으로)",
          "sceneId": "hall_3_ex3" // -> 3-3. 집
        },
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "마트, 테마파크 (뒤로)",
          "sceneId": "hall_3_ex1" // -> 3-1. 마트
        }
      ]
    },
    "hall_3_ex3": {
      "title": "과학기술관: 집, 가정 (답사 종료)",
      "type": "equirectangular",
      "panorama": "https://storage.googleapis.com/virtual_tour_team4/flat_panorama.jpg", // ⚠️ '집, 가정' 사진 URL
      "hotSpots": [
        { "pitch": -2.5, "yaw": -150.0, "type": "scene",
          "text": "이웃, 동네 (뒤로)",
          "sceneId": "hall_3_ex2" // -> 3-2. 이웃
        },
        { "pitch": -10, "yaw": 0, "type": "scene",
          "text": "처음으로 (창의나래관)",
          "sceneId": "hall_1_entrance" // -> 1. 처음으로
        },
        { "pitch": -10, "yaw": 50, "type": "info",
          "text": "가상 답사를 종료합니다. (클릭하여 닫기 - Vue 연동 필요)"
          // 이 핫스팟은 Vue 컴포넌트에서 클릭 이벤트를 연동해야 합니다.
        }
      ]
    }
  }
};