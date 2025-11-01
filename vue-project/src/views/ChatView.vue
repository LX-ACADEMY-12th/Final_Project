<template>
  <div class="chat-page-container d-flex flex-column vh-100 bg-light" style="font-family: 'SUIT', sans-serif;">

    <div class="chat-header d-flex justify-content-between align-items-center p-3 bg-white border-bottom flex-shrink-0">
      <div class="header-left d-flex align-items-center gap-2">
        <i class="bi bi-book fs-5"></i>
        <span style="font-size: 0.9rem; font-weight: 500;">교과서</span>
      </div>
      <div class="header-center fw-bold fs-6">
        AI 선생님
      </div>
      <div class="header-right" @click="goToHome">
        <i class="bi bi-x-lg fs-5" style="cursor: pointer;"></i>
      </div>
    </div>

    <div ref="chatBody" class="chat-body flex-grow-1 p-3 overflow-auto d-flex flex-column gap-3">

      <div v-for="message in messages" :key="message.id"
        :class="['message-group', message.sender === 'ai' ? 'incoming' : 'outgoing']">

        <div class="d-flex align-items-start" :class="{ 'flex-row-reverse': message.sender === 'user' }">

          <div v-if="message.sender === 'ai'" class="profile-icon">
            <i class="bi bi-chat-fill"></i>
          </div>

          <div class="message-bubble">
            {{ message.text }}
          </div>
        </div>

        <div class="timestamp mt-1">
          {{ message.time }}
        </div>
      </div>

    </div>

    <div class="chat-input-area d-flex align-items-center gap-2 p-3 bg-white border-top flex-shrink-0">
      <input type="text" class="form-control message-input" placeholder="메시지를 입력하세요..." v-model="newMessage"
        @keyup.enter="sendMessage">
      <i class="bi bi-send-fill fs-4 send-icon" :class="{ 'active': newMessage.length > 0 }" @click="sendMessage"></i>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from '@/api/axiosSetup';
import { useAuthStore } from '@/stores/authStore'; // 👈 [추가] 1. 스토어 import
import { storeToRefs } from 'pinia'; // 👈 [추가] 2. storeToRefs import

const authStore = useAuthStore(); // 👈 [추가] 3. 스토어 인스턴스 생성
const { user } = storeToRefs(authStore); // 👈 [추가] 4. 'user' 변수 선언 (반응성 유지

const userName = computed(() => {
 if(user.value?.name) {
  return user.value.name;
 }
 return '로그인 필요';
});

const router = useRouter();
const chatBody = ref(null); // 채팅 본문 DOM을 참조할 ref

// 채팅 메시지 목록 (ref로 관리)
const messages = ref([
 {
  id: 1,
  sender: 'ai',
    // 👈 [수정] text 속성 자체를 computed로 감싸서
    //      userName.value의 변경을 실시간으로 반영합니다.
  text: computed(() => `${userName.value}님, 안녕하세요. 무엇을 도와드릴까요?`),
  time: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true })
 },
]);

// 하단 입력창에 바인딩될 ref
const newMessage = ref('');

// 홈으로 이동하는 함수
const goToHome = () => {
  router.push('/home')
}

// [!!] 2. 스크롤을 맨 아래로 내리는 함수 생성
const scrollToBottom = async () => {
  // nextTick을 사용해 DOM 업데이트가 완료될 때까지 기다립니다.
  await nextTick();
  if (chatBody.value) {
    // chatBody 요소의 scrollTop을 scrollHeight로 설정하여 맨 아래로 내립니다.
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
};

// [!!] 3. 컴포넌트가 마운트(로드)될 때도 스크롤을 한 번 내립니다.
onMounted(() => {
  scrollToBottom();
});

// 메시지 전송 함수
const sendMessage = async () => {
  // 입력된 내용이 없으면 전송하지 않음
  if (newMessage.value.trim() === '') {
    return;
  }

  // 현재 시간을 "HH:MM AM/PM" 형식으로 포맷팅
  const now = new Date();
  const time = now.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  });

  // 사용자 메시지 추가
  const userMessage = {
    id: messages.value.length + 1,
    sender: 'user',
    text: newMessage.value,
    time: time
  };
  // messages 배열에 새 메시지 추가
  messages.value.push(userMessage);

  // '... 생각 중' 메시지 먼저 표시
  const thinkingMessageId = messages.value.length + 1;
  const thinkingMessage = {
    id: thinkingMessageId,
    sender: 'ai',
    text: 'AI 튜터가 답변을 생각 중입니다.',
    time: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true })
  };
  messages.value.push(thinkingMessage);

  // 스크롤 및 입력창 비우기
  const userMessageText = newMessage.value // API 호출 전에 값 저장
  newMessage.value = '';
  await scrollToBottom(); // 생각 중 이 보이도록 스크롤

  try {
    // 백엔드로 API 요청
    const response = await axios.post('http://localhost:8080/api/chat', {
      prompt: userMessageText
    });
    // 실제 ai 응답으로 생각 중 메시지 업데이트
    const aiAnswer = response.data.response;

    // 생각 중 메시지를 찾아서 내용 업데이트
    const thinkingMsgIndex = messages.value.findIndex(m => m.id === thinkingMessageId);
    if (thinkingMsgIndex !== -1) {
      messages.value[thinkingMsgIndex].text = aiAnswer;
      // 시간도 현재 시간으로 업데이트
      messages.value[thinkingMsgIndex].time = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
    }
  } catch (error) {
    console.error("AI 응답 오류:", error);
    // 오류 발생 시 '생각 중' 메시지를 오류 메시지로 변경
    const thinkingMsgIndex = messages.value.findIndex(m => m.id === thinkingMessageId);
    if (thinkingMsgIndex !== -1) {
      messages.value[thinkingMsgIndex].text = "오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    }
  } finally {
    // 최종 응답 후 다시 스크롤
    await scrollToBottom();
  }
};
</script>

<style scoped>
/* [폰트]
*/
.chat-page-container {
  font-family: 'SUIT', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  /* 모바일 화면처럼 보이도록 최대 너비 및 그림자 설정 (선택 사항) */
  margin: 0 auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;
}

/* [헤더]
  헤더의 좌/우/중앙 정렬을 위한 flex 설정
*/
.chat-header {
  position: relative;
}

.chat-header .header-left {
  flex: 1;
  justify-content: flex-start;
}

.chat-header .header-center {
  /* position: absolute; left: 50%; transform: translateX(-50%); */
  flex: 1;
  text-align: center;
  font-weight: 600;
}

.chat-header .header-right {
  flex: 1;
  text-align: right;
}

/*
  [본문]
  채팅방 배경색 (이미지와 동일하게)
*/
.chat-body {
  /* 이미지의 연한 회색 배경 */
  background-color: #f0f2f5;
  /* 이 줄을 추가하여 flex 아이템이 넘치지 않도록 수정 */
  overflow: auto;
  min-height: 0;
}

/* 스크롤바 숨기기 (표준 CSS) */
.chat-body::-webkit-scrollbar {
  display: none;
}

.chat-body {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.chat-page-container {
  font-family: 'SUIT', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  /* 모바일 화면처럼 보이도록 최대 너비 및 그림자 설정 (선택 사항) */
  margin: 0 auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;

  /* 자식 요소(chat-body)가 부모(vh-100)의 높이를
    넘어서 팽창하려 할 때, 부모가 그 팽창을 무시하고
    자신의 높이를 강제로 지키게 만듭니다.
  */
  overflow: hidden;
}

/* [메시지 그룹]
  프로필 + 버블 + 타임스탬프를 묶는 단위
*/
.message-group.incoming {
  align-self: flex-start;
  /* 수신(AI) 메시지는 왼쪽 정렬 */
}

.message-group.outgoing {
  align-self: flex-end;
  /* 발신(사용자) 메시지는 오른쪽 정렬 */
}

/* [AI 프로필 아이콘]
  이미지의 파란색 원 + 말풍선 아이콘
*/
.profile-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #0d6efd;
  /* 부트스트랩 primary blue */
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  margin-right: 10px;
}

.profile-icon .bi {
  font-size: 1.1rem;
}

/* [메시지 버블 (공통)]
  말풍선 기본 스타일
*/
.message-bubble {
  padding: 12px 16px;
  border-radius: 20px;
  /* 둥근 모서리 */
  max-width: 280px;
  /* 말풍선 최대 너비 */
  word-wrap: break-word;
  /* 긴 텍스트 자동 줄바꿈 */
  line-height: 1.5;
  font-size: 0.95rem;
}

/* [수신(AI) 버블]
  흰색 배경, 검은색 텍스트
  왼쪽 위 모서리를 뾰족하게 (꼬리표 효과)
*/
.message-group.incoming .message-bubble {
  background-color: #ffffff;
  color: #212529;
  border-top-left-radius: 5px;
}

/* [발신(사용자) 버블]
  파란색 배경, 흰색 텍스트
  오른쪽 위 모서리를 뾰족하게 (꼬리표 효과)
*/
.message-group.outgoing .message-bubble {
  background-color: #0d6efd;
  /* 부트스트랩 primary blue */
  color: white;
  border-top-right-radius: 5px;
}

/* [타임스탬프]
  작고 연한 회색 글씨
*/
.timestamp {
  font-size: 0.75rem;
  color: #6c757d;
}

/* 수신(AI) 메시지의 타임스탬프는 아이콘 너비만큼 들여쓰기 */
.message-group.incoming .timestamp {
  margin-left: 46px;
  /* profile-icon(36px) + margin(10px) */
}

/* 발신(사용자) 메시지의 타임스탬프는 오른쪽 정렬 */
.message-group.outgoing .timestamp {
  text-align: right;
}

/* [하단 입력창]
*/
.message-input {
  border-radius: 22px;
  /* 둥근 입력창 */
  background-color: #f0f2f5;
  /* 연한 회색 배경 */
  border: 1px solid #e0e0e0;
  padding: 10px 20px;
}

.message-input:focus {
  background-color: #fff;
  border-color: #0d6efd;
  box-shadow: none;
}

/*
  [전송 아이콘]
  기본: 회색, 텍스트 입력 시: 파란색
*/
.send-icon {
  color: #adb5bd;
  /* 비활성 회색 */
  cursor: pointer;
  transition: color 0.2s;
}

.send-icon.active {
  color: #0d6efd;
  /* 활성 파란색 */
}
</style>
