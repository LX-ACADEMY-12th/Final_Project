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

    <div class="voice-interaction-body flex-grow-1 d-flex flex-column align-items-center text-center p-4">

      <div class="ai-character-bubble flex-shrink-0" :class="uiState">
        <div class="ai-character" :style="listeningAnimationStyle">
          <i class="bi" :class="getCharacterIcon()"></i>
        </div>
      </div>

      <div class="chat-history-container" ref="chatHistoryRef">

        <div v-for="(msg, index) in messages" :key="index" class="chat-message" :class="msg.sender">
          <div class="chat-bubble" :class="msg.sender">
            {{ msg.text }}
          </div>
        </div>

        <div class="chat-message ai" v-if="statusText">
          <div class="chat-bubble status">
            {{ statusText }}
          </div>
        </div>
      </div>

    </div>

    <div class="voice-control-area d-flex align-items-center justify-content-center p-4 bg-white flex-shrink-0">
      <button class="btn btn-lg rounded-circle d-flex justify-content-center align-items-center shadow"
        :class="getButtonClass()" @click="toggleRecording" :disabled="uiState === 'processing'">
        <i class="bi fs-1" :class="uiState === 'listening' ? 'bi-stop-fill' : 'bi-mic-fill'"></i>
      </button>
    </div>

  </div>
</template>
<script setup>
// [!!] (수정됨) watch, nextTick 추가
import { ref, computed, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);
const router = useRouter();

const goToHome = () => router.push('/home');

// --- 상태 관리 ---
const uiState = ref('idle');

// [!!] (수정됨) messages와 statusText 역할 분리
const messages = ref([
  { sender: 'ai', text: '안녕! 만나서 반가워. \n 궁금한 걸 물어봐!' }
]);
const statusText = ref(''); // 일시적인 상태 메시지 (듣는 중, 생각 중...)
const currentAudio = ref(null);

// [!!] (추가됨) 자동 스크롤을 위한 ref
const chatHistoryRef = ref(null);

// --- 녹음 로직 (동일) ---
const mediaRecorder = ref(null);
const audioChunks = ref([]);
const audioContext = ref(null);
const analyser = ref(null);
const source = ref(null);
const animationFrameId = ref(null);
const currentVolume = ref(0);

const toggleRecording = () => {
  if (uiState.value === 'listening') {
    stopAndSendAudio();

    // [!!] 'idle' 또는 'speaking' 상태일 때
  } else if (uiState.value === 'idle' || uiState.value === 'speaking') {
    if (currentAudio.value) {
      currentAudio.value.pause(); // (중요!) AI 음성을 즉시 중지
      currentAudio.value.currentTime = 0;
    }
    statusText.value = '';
    startRecording(); // 즉시 새로운 녹음 시작
  }
};

// [!!] (수정됨) statusText 설정
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder.value = new MediaRecorder(stream, { mimeType: 'audio/webm' });
    mediaRecorder.value.ondataavailable = (event) => audioChunks.value.push(event.data);
    mediaRecorder.value.start();

    audioContext.value = new (window.AudioContext || window.webkitAudioContext)();
    analyser.value = audioContext.value.createAnalyser();
    source.value = audioContext.value.createMediaStreamSource(stream);
    source.value.connect(analyser.value);
    analyser.value.fftSize = 256;

    visualizeAudio();

    console.log("Real recording started...");
    uiState.value = 'listening';
    statusText.value = '듣고 있어! 말해봐~'; // (수정됨)
  } catch (error) {
    console.error("Error starting recording:", error);
    statusText.value = '앗! 마이크를 켤 수 없어.'; // (수정됨)
  }
};

const visualizeAudio = () => {
  // (시각화 로직은 동일)
  if (!analyser.value) return;
  const bufferLength = analyser.value.frequencyBinCount;
  const dataArray = new Uint8Array(bufferLength);
  analyser.value.getByteFrequencyData(dataArray);
  let sum = 0;
  for (let i = 0; i < bufferLength; i++) {
    sum += dataArray[i];
  }
  let avgVolume = sum / bufferLength;
  currentVolume.value = Math.min(avgVolume / 60, 1) * 100;
  animationFrameId.value = requestAnimationFrame(visualizeAudio);
};

// [!!] (수정됨) statusText 및 messages 처리 로직 변경
const stopAndSendAudio = () => {
  if (!mediaRecorder.value) return;

  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrameId.value);
    animationFrameId.value = null;
  }

  console.log("Real recording stopped. Sending to server...");
  uiState.value = 'processing';
  statusText.value = '음... 잠깐만 생각해볼게!'; // (수정됨)

  mediaRecorder.value.onstop = async () => {
    const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' });
    audioChunks.value = [];
    const formData = new FormData();
    formData.append('audio', audioBlob, 'recording.webm');

    if (mediaRecorder.value && mediaRecorder.value.stream) {
      mediaRecorder.value.stream.getTracks().forEach(track => track.stop());
    }
    if (audioContext.value && audioContext.value.state !== 'closed') {
      audioContext.value.close();
    }
    currentVolume.value = 0;

    try {
      const response = await axios.post('http://localhost:8080/api/voice-query', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      const { userTranscript: sttText, aiResponseText, aiResponseAudio } = response.data;

      // [!!] (수정됨) "processing" 상태 메시지를 지우고 대화 내역(messages)에 추가
      statusText.value = '';

      // [!!] (수정됨)
      // "못 들었어" 케이스와 정상 케이스를 분리하여 messages에 push
      // 이것이 '사용자 입력 텍스트 표시' 문제를 해결합니다.
      if (!sttText && aiResponseText.includes("목소리를 잘 못 들었어")) {
        messages.value.push({ sender: 'ai', text: aiResponseText });
      } else {
        messages.value.push({ sender: 'user', text: sttText });
        messages.value.push({ sender: 'ai', text: aiResponseText });
      }

      uiState.value = 'speaking';
      // statusText.value = aiResponseText; // (제거됨)

      currentAudio.value = new Audio(aiResponseAudio);
      currentAudio.value.play();

      currentAudio.value.onended = () => {
        uiState.value = 'idle';
        statusText.value = '또 궁금한 거 있어?'; // (수정됨)
      };

    } catch (error) {
      console.error("Error sending audio:", error);
      uiState.value = 'idle';
      statusText.value = '앗! 대답을 못 찾았어. \n 다시 물어봐줄래?'; // (수정됨)
    }
  }; // onstop 핸들러 정의 끝

  mediaRecorder.value.stop();
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
    case 'idle':
    case 'speaking': // [!!] (추가됨) 말하는 중에도 'go' 버튼을 표시
      return 'btn-speak-go';
    case 'processing':
    default:
      return 'btn-speak-disabled';
  }
}

const listeningAnimationStyle = computed(() => {
  // (computed 로직은 동일)
  if (uiState.value !== 'listening') {
    return { transform: 'scale(1)' };
  }
  const scale = 1.1 + (currentVolume.value / 100) * 0.3;
  return {
    transform: `scale(${scale})`
  };
});

// [!!] (추가됨) 새 메시지 또는 상태 변경 시 맨 아래로 스크롤
const scrollToBottom = async () => {
  await nextTick(); // DOM 업데이트를 기다림
  const el = chatHistoryRef.value;
  if (el) {
    el.scrollTop = el.scrollHeight;
  }
};
watch(messages, scrollToBottom, { deep: true });
watch(statusText, scrollToBottom);

</script>
<style scoped>
/* [폰트] - (동일) */
@import url('https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/variable/woff2/SUIT-Variable.css');

.chat-page-container {
  /* (동일) */
  font-family: 'SUIT', sans-serif;
  overflow: hidden;
  margin: 0 auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  background-color: #E6F7FF;
}

/* [헤더] - (동일) */
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
  overflow: hidden;
}

/* [AI 캐릭터] */
.ai-character-bubble {
  /* (동일) */
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  background-color: #FFD600;
  border: 8px solid #FFF;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  /* [!!] (추가됨) 상단 고정을 위해 */
  margin-bottom: 20px;
  flex-shrink: 0;
}

.ai-character {
  /* (동일) */
  font-size: 80px;
  color: #FFF;
  transition: transform 0.2s ease;
}


/* [!!] (수정됨)
   여기부터가 새롭게 추가/수정된 채팅 UI 스타일입니다. */

/* 1. 채팅 내역 스크롤 컨테이너 */
.chat-history-container {
  width: 100%;
  flex-grow: 1;
  /* 캐릭터를 제외한 나머지 공간을 모두 차지 */
  overflow-y: auto;
  /* [!!] '긴 텍스트 깨짐' 문제를 해결하는 핵심 */
  padding: 0 1rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  /* 말풍선 사이의 간격 */
  gap: 15px;

  /* 1. Firefox 브라우저용 */
  scrollbar-width: none;
  /* 2. IE/Edge (구형) 브라우저용 */
  -ms-overflow-style: none;
}

/* 2. 말풍선 행 (정렬용) */
.chat-message {
  display: flex;
  width: 100%;
}

.chat-message.user {
  justify-content: flex-end;
  /* [!!] 사용자 말풍선은 오른쪽 */
}

.chat-message.ai {
  justify-content: flex-start;
  /* [!!] AI 말풍선은 왼쪽 */
}

/* 3. 새 말풍선 스타일 (꼬리 제거, 범용성) */
.chat-bubble {
  position: relative;
  background: #ffffff;
  border-radius: 20px;
  padding: 16px 24px;
  max-width: 85%;
  /* 너무 길어지지 않게 */
  font-size: 1.1rem;
  /* 글자 크기 살짝 줄임 */
  font-weight: 500;
  line-height: 1.6;
  color: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  white-space: pre-line;
  /* \n을 줄바꿈으로 인식 */
  text-align: left;
  /* 항상 왼쪽 정렬 */
}

/* 4. 사용자 말풍선 스타일 */
.chat-bubble.user {
  background-color: #FFECB3;
  /* 연한 노란색 */
  color: #333;
}

/* 5. 상태 메시지 말풍선 스타일 (듣는 중, 생각 중...) */
.chat-bubble.status {
  background-color: #f0f0f0;
  /* 연한 회색 */
  color: #555;
  font-style: italic;
}

/* [!!] 기존 .speech-bubble 및 .speech-bubble::after 스타일은 삭제했습니다. */


/* -- 캐릭터 애니메이션 (동일) -- */
.ai-character-bubble.listening {
  background-color: #00C4FF;
}

.ai-character-bubble.listening .ai-character {
  transform: scale(1.1);
  /* (참고) listeningAnimationStyle에 의해 오버라이드 됨 */
}

.ai-character-bubble.processing {
  background-color: #7B68EE;
  animation: thinking 1s infinite linear;
}

.ai-character-bubble.processing .ai-character {
  color: #FFF;
}

.ai-character-bubble.speaking {
  background-color: #FF6B6B;
  animation: speaking 0.5s infinite alternate ease-in-out;
}

/* [하단 컨트롤] - (동일) */
.voice-control-area {
  min-height: 120px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  flex-shrink: 0;
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

.btn-speak-go {
  background-color: #28a745;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4);
}

.btn-speak-go:hover {
  background-color: #218838;
}

.btn-speak-stop {
  background-color: #dc3545;
  box-shadow: 0 4px 15px rgba(220, 53, 69, 0.4);
}

.btn-speak-stop:hover {
  background-color: #c82333;
}

.btn-speak-disabled {
  background-color: #E0E0E0;
  opacity: 0.7;
}

/* [애니메이션 키프레임] - (동일) */
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
