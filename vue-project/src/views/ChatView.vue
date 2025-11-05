<template>
  <div class="chat-page-container d-flex flex-column vh-100" style="font-family: 'SUIT', sans-serif;">

    <div class="chat-header d-flex justify-content-between align-items-center p-3 flex-shrink-0">
      <div class="header-left"></div>
      <div class="header-center fw-bold fs-6">
        AI 선생님
      </div>
      <div class="header-right" @click="goToHome">
        <i class="bi bi-x-lg fs-5" style="cursor: pointer; color: #555;"></i>
      </div>
    </div>

    <div
      class="voice-interaction-body flex-grow-1 d-flex flex-column justify-content-center align-items-center text-center p-4">

      <div class="ai-character-bubble" :class="uiState">
        <div class="ai-character">
          <i class="bi" :class="getCharacterIcon()"></i>
        </div>
      </div>

      <div class="speech-bubble" v-if="statusText">
        {{ statusText }}
      </div>
    </div>

    <div class="voice-control-area d-flex align-items-center justify-content-center p-4 bg-white flex-shrink-0">

      <button class="btn btn-lg rounded-circle d-flex justify-content-center align-items-center shadow"
        :class="getButtonClass()" @click="toggleRecording"
        :disabled="uiState === 'processing' || uiState === 'speaking'">
        <i class="bi fs-1" :class="uiState === 'listening' ? 'bi-stop-fill' : 'bi-mic-fill'"></i>
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
// import axios from '@/api/axiosSetup'; // 👈 [!!] 실제 API 호출을 안 하므로 주석 처리 (나중에 다시 활성화)
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);
const router = useRouter();

const goToHome = () => router.push('/home');

// --- 상태 관리 (동일) ---
const uiState = ref('idle');
const statusText = ref(`안녕! 만나서 반가워. \n 궁금한 걸 물어봐!`);
const messages = ref([]);
const currentAudio = ref(null);

// --- 녹음 로직 (동일) ---
const mediaRecorder = ref(null);
const audioChunks = ref([]);

const toggleRecording = () => {
  if (uiState.value === 'listening') {
    stopAndSendAudio();
  } else if (uiState.value === 'idle') {
    if (currentAudio.value) {
      currentAudio.value.pause();
      currentAudio.value.currentTime = 0;
    }
    startRecording();
  }
};

const startRecording = async () => {
  try {
    // [!!] 목업 테스트 중에는 실제 마이크를 켤 필요가 없습니다.
    // [!!] '듣는 중' 상태로 바로 전환합니다.
    // const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    // mediaRecorder.value = new MediaRecorder(stream, { mimeType: 'audio/webm' });
    // mediaRecorder.value.ondataavailable = (event) => audioChunks.value.push(event.data);
    // mediaRecorder.value.start();

    console.log("Mock recording started...");
    uiState.value = 'listening';
    statusText.value = '듣고 있어! 말해봐~';

    // [!!] 테스트를 위해 3초 후에 자동으로 녹음 중지 및 전송
    setTimeout(() => {
      if (uiState.value === 'listening') {
        console.log("Mock recording auto-stopping after 3s...");
        stopAndSendAudio();
      }
    }, 3000);

  } catch (error) {
    statusText.value = '앗! 마이크를 켤 수 없어.';
  }
};


// [!!] 1. 가짜 API 호출 함수 (목업 데이터)
// ----------------------------------------------------
/**
 * 2초간의 딜레이 후, 가짜 AI 응답(목업 데이터)을 반환하는 Promise
 */
const mockApiCall = () => {
  console.log("Mock API 호출 시작... (2초간 '생각 중' 상태)");
  return new Promise((resolve, reject) => {
    setTimeout(() => {

      // 80% 확률로 성공, 20% 확률로 실패 (오류 케이스 테스트용)
      if (Math.random() < 0.8) {
        console.log("Mock API -> 성공 응답 반환");
        resolve({
          data: {
            userTranscript: "선생님, 공룡은 왜 멸종했어요?", // (가짜 STT 결과)
            aiResponseText: "아주아주 큰 운석이 지구랑 \n 꽝! 부딪혔기 때문이야.", // (가짜 AI 답변)
            // (가짜 AI 음성 - 구글의 효과음 URL로 대체)
            aiResponseAudio: "https://actions.google.com/sounds/v1/cartoon/magic_chime.ogg"
          }
        });
      } else {
        console.log("Mock API -> 실패 응답 반환");
        reject(new Error("Mock API Error: 앗! 대답을 못 찾았어요."));
      }

    }, 2000); // 2초 딜레이
  });
};
// ----------------------------------------------------


/**
 * 녹음 중지 및 서버 전송 ( [!!] 이 부분이 수정되었습니다 )
 */
const stopAndSendAudio = () => {
  // [!!] 실제 녹음 로직은 필요 없으므로 주석 처리
  // if (!mediaRecorder.value) return;
  // mediaRecorder.value.stop();

  console.log("Mock recording stopped. Calling Mock API...");
  uiState.value = 'processing';
  statusText.value = '음... 잠깐만 생각해볼게!'; // 생각 중 멘트

  // [!!] onstop 대신 바로 비동기 로직 실행
  (async () => {
    // const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' });
    // audioChunks.value = [];
    // const formData = new FormData();
    // formData.append('audio', audioBlob, 'recording.webm');

    // ... (실제 마이크 스트림 중지 로직도 주석 처리) ...

    try {
      // [!!] 2. 실제 axios.post 대신 mockApiCall()을 호출합니다.
      // const response = await axios.post('http://localhost:8080/api/voice-query', formData, {
      //   headers: { 'Content-Type': 'multipart/form-data' }
      // });
      const response = await mockApiCall(); // 👈 [!!] 이 부분 변경


      const { userTranscript: sttText, aiResponseText, aiResponseAudio } = response.data;

      messages.value.push({ sender: 'user', text: sttText });
      messages.value.push({ sender: 'ai', text: aiResponseText });

      uiState.value = 'speaking';
      statusText.value = aiResponseText;

      currentAudio.value = new Audio(aiResponseAudio);
      currentAudio.value.play();

      currentAudio.value.onended = () => {
        uiState.value = 'idle';
        statusText.value = '또 궁금한 거 있어?';
      };

    } catch (error) {
      // [!!] 목업 에러가 발생하면 이 부분이 실행됩니다.
      console.error(error.message);
      uiState.value = 'idle';
      statusText.value = '앗! 대답을 못 찾았어. \n 다시 물어봐줄래?';
    }
  })(); // 비동기 즉시 실행 함수
};

// --- UI 상태 함수 (동일) ---

const getCharacterIcon = () => {
  switch (uiState.value) {
    case 'listening': return 'bi-ear-fill';
    case 'processing': return 'bi-hourglass-top';
    case 'speaking': return 'bi-emoji-sunglasses-fill';
    case 'idle':
    default:
      return 'bi-emoji-smile-fill';
  }
}

const getButtonClass = () => {
  switch (uiState.value) {
    case 'listening': return 'btn-speak-stop';
    case 'idle': return 'btn-speak-go';
    case 'processing':
    case 'speaking':
    default:
      return 'btn-speak-disabled';
  }
}
</script>

<style scoped>
/* [폰트] - SUIT 폰트가 설치되어 있어야 합니다. */
@import url('https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/variable/woff2/SUIT-Variable.css');

.chat-page-container {
  font-family: 'SUIT', sans-serif;
  overflow: hidden;
  margin: 0 auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  /* 배경색: 부드러운 하늘색 */
  background-color: #E6F7FF;
}

/* [헤더] */
.chat-header {
  background-color: #FFF;
  border-bottom: 1px solid #E0E0E0;
}

.chat-header .header-left,
.chat-header .header-right {
  flex: 1;
}

.chat-header .header-center {
  flex: 1;
  text-align: center;
  font-weight: 600;
}

.chat-header .header-right {
  text-align: right;
}

/* [메인 화면] */
.voice-interaction-body {
  width: 100%;
  padding-bottom: 100px;
  /* 하단 버튼 영역만큼 공간 확보 */
}

/* [말풍선] */
.speech-bubble {
  position: relative;
  background: #ffffff;
  border-radius: 20px;
  padding: 16px 24px;
  margin-top: 30px;
  max-width: 90%;
  font-size: 1.2rem;
  font-weight: 500;
  line-height: 1.6;
  color: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  white-space: pre-line;
}

/* 말풍선 꼬리 */
.speech-bubble::after {
  content: '';
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 15px;
  border-style: solid;
  border-color: transparent transparent #ffffff transparent;
}

/* [AI 캐릭터] */
.ai-character-bubble {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  background-color: #FFD600;
  /* 노란색 배경 */
  border: 8px solid #FFF;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.ai-character {
  font-size: 80px;
  color: #FFF;
  /* 아이콘 흰색 */
  transition: transform 0.2s ease;
}

/* -- 캐릭터 애니메이션 -- */

/* 듣는 중 (귀 쫑긋) */
.ai-character-bubble.listening {
  background-color: #00C4FF;
  /* 하늘색 */
}

.ai-character-bubble.listening .ai-character {
  transform: scale(1.1);
  /* 살짝 커짐 */
}

/* 생각 중 (뱅글뱅글) */
.ai-character-bubble.processing {
  background-color: #7B68EE;
  /* 보라색 */
  animation: thinking 1s infinite linear;
}

.ai-character-bubble.processing .ai-character {
  color: #FFF;
}

/* 말하는 중 (신남!) */
.ai-character-bubble.speaking {
  background-color: #FF6B6B;
  /* 붉은색 */
  animation: speaking 0.5s infinite alternate ease-in-out;
}

/* [하단 컨트롤] */
.voice-control-area {
  min-height: 120px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
}

.voice-control-area .btn {
  width: 80px;
  height: 80px;
  border: none;
  color: white;
  transition: all 0.2s ease;
}

.voice-control-area .btn:active {
  transform: scale(0.95);
}

/* 버튼 색상 */
.btn-speak-go {
  background-color: #28a745;
  /* 초록색 */
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4);
}

.btn-speak-go:hover {
  background-color: #218838;
}

.btn-speak-stop {
  background-color: #dc3545;
  /* 빨간색 */
  box-shadow: 0 4px 15px rgba(220, 53, 69, 0.4);
}

.btn-speak-stop:hover {
  background-color: #c82333;
}

.btn-speak-disabled {
  background-color: #E0E0E0;
  /* 회색 */
  opacity: 0.7;
}

/* [애니메이션 키프레임] */
@keyframes thinking {
  from {
    transform: rotate(0deg) scale(1.05);
  }

  to {
    transform: rotate(360deg) scale(1.05);
  }
}

@keyframes speaking {
  from {
    transform: scale(1.0);
  }

  to {
    transform: scale(1.15);
  }
}
</style>
