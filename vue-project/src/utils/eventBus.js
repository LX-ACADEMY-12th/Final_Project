// 1. 'mitt'라는 방송국 건설 도구를 가져옵니다.
// (이걸 쓰려면 터미널에서 npm install mitt 를 먼저 해야 합니다)
import mitt from 'mitt';

// 2. 'mitt' 도구로 실제 방송국을 짓고 'eventBus'라고 부릅니다.
const eventBus = mitt();

// 3. 다른 파일(main.js, App.vue)에서 이 방송국을 쓸 수 있게 내보냅니다.
export default eventBus;