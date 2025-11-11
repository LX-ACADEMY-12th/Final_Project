// 데이터를 정의하고 'export' 할거임

export const curriculumData = {
  '초등 3학년': {
    '1학기': {
      물리: [
        {
          title: '힘과 에너지',
          description: '밀기와 당기기, 무게, 수평잡기, 도구의 이용을 배웁니다.',
          simulationComponent: null,
        },
      ],
      화학: [],
      생명: [
        { title: '생물의 구조와 에너지', description: '동물의 생김새, 식물의 생김새' },
        {
          title: '생물의 연속성',
          description:
            '동물의 한살이, 식물의 한살이, 식물이 자라는 조건, 다양한 환경에 사는 동물과 식물, 특징에 따른 동물과 식물 분류',
        },
        { title: '생명과학과 인간의 생활', description: '생활 속에서 동물과 식물의 이용' },
      ],
      지구: [],
    },
    '2학기': {
      물리: [
        { title: '빛과 파동', description: '소리의 발생, 소리의 세기, 소리의 높낮이, 소리의 전달' },
      ],
      화학: [
        { title: '물체와 물질', description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화' },
      ],
      생명: [
        {
          title: '생명과학과 인간의 생활',
          description: '생명과학과 우리 생활: 감염병과 우리의 생활',
        },
      ],
      지구: [
        {
          title: '유체지구',
          description: '바다의 특징, 밀물과 썰물 ,파도 ,바닷가 주변 지형 ,갯벌 보전, 지구의 대기',
        },
      ],
    },
  },

  '초등 4학년': {
    '1학기': {
      물리: [
        {
          title: '전기와 자기',
          description: '자석과 물체 사이의 힘 ,자석과 자석 사이의 힘 ,자석의 극 ,자석의 이용',
          simulationComponent: 'MagnetField',
        },
      ],
      화학: [
        {
          title: '물질의 성질',
          description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화',
          simulationComponent: 'StatesOfMatter',
        },
      ],
      생명: [
        { title: '생물의 구조와 에너지', description: '균류, 원생생물, 세균의 특징' },
        { title: '생명과학과 인간의 생활', description: '균류, 원생생물, 세균의 이용' },
      ],
      지구: [
        {
          title: '고체지구',
          description: '강 주변 지형, 화산 활동, 화성암, 지진 대처 방법',
          simulationComponent: 'ColumnarJoint',
        },
      ],
    },
    '2학기': {
      물리: [],
      화학: [
        { title: '물질의 성질', description: '물체와 물질, 물질의 성질, 물질의 기능, 물질의 변화' },
      ],
      생명: [
        {
          title: '환경과 생태계',
          description:
            '생물의 요소와 비생물 요소, 환경오염이 생물에 미치는 영향, 먹이사슬과 먹이그물',
          simulationComponent: 'Ecosystem',
        },
      ],
      지구: [
        { title: '천체', description: '달의 모양과 표면, 달의 위상변화 ,태양계 행성, 별과 별자리' },
        { title: '기후변화와 우리 생활', description: null },
      ],
    },
  },
  '초등 5학년': {
    '1학기': {
      물리: [
        {
          title: '빛과 파동',
          description: '빛의 직진, 평면거울에서의 빛의 반사, 빛의 굴절, 렌즈의 이용',
        },
      ],
      화학: [
        {
          title: '물질의 성질',
          description: '용해, 용액, 용질의 종류, 용질의 녹는 양, 용액의 진하기',
        },
      ],
      생명: [
        {
          title: '생명의 구조와 에너지',
          description:
            '세포의 구조, 뼈와 근육의 구조와 기능, 소화, 순환, 호흡, 배설 기관의 구조와 기능',
        },
      ],
      지구: [{ title: '고체 지구', description: '지층, 퇴적암, 화석의 생성, 과거 생물과 환경' }],
    },
    '2학기': {
      물리: [{ title: '열', description: '온도, 열의 이동, 단열' }],
      화학: [{ title: '물체와 물질', description: '혼합물의 분리' }],
      생명: [{ title: '자원과 에너지', description: '재생에너지 종류, 에너지 사용' }],
      지구: [
        { title: '유체지구', description: '날씨와 기상요소, 이슬, 안개, 구름, 고기압과 저기압' },
      ],
    },
  },
  '초등 6학년': {
    '1학기': {
      물리: [{ title: '힘과 에너지', description: '위치의 변화, 속력, 속력과 안전' }],
      화학: [{ title: '물질의 성질', description: '용액의 분류, 지시약, 산성 용액, 염기성 용액' }],
      생명: [{ title: '생물의 구조와 에너지', description: '뿌리, 줄기, 잎, 꽃의 구조와 기능' }],
      지구: [
        {
          title: '천체',
          description: '태양과 별의 위치 변화, 지구의 자전과 공전, 계절별 별자리 변화',
        },
      ],
    },
    '2학기': {
      물리: [
        { title: '전기와 자기', description: '전기 회로, 전지의 직렬연결 ,전자석, 전기 안전' },
      ],
      화학: [{ title: '물질의 변화', description: '연소조건, 연소 생성물' }],
      생명: [
        {
          title: '환경과 생태계',
          description:
            '생물의 요소와 비생물 요소, 환경오염이 생물에 미치는 영향, 먹이사슬과 먹이그물',
        },
      ],
      지구: [{ title: '천체', description: '태양 고도의 일변화, 계절별 낮의 길이' }],
    },
  },
}
