package com.example.demo.service;

import com.example.demo.dto.VoiceResponseDTO;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // 👈 [추가]
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID; // 👈 [추가]

@Service
public class VoiceQueryService {

    // (기존 ChatService)
    private final ChatService chatService;
    private final SpeechClient speechClient;
    private final TextToSpeechClient ttsClient;
    private final Storage storage; // 👈 [주입 1] GCS 클라이언트
    // Logger 객체
    private static final Logger log = LoggerFactory.getLogger(VoiceQueryService.class);

    @Value("${gcs.voice-bucket-name}")
    private String bucketName; // 👈 [주입 2] application.properties에서 버킷 이름 가져오기

    public VoiceQueryService(ChatService chatService,
                             SpeechClient speechClient,
                             TextToSpeechClient ttsClient,
                             Storage storage) { // 👈 생성자에 Storage 추가
        this.chatService = chatService;
        this.speechClient = speechClient;
        this.ttsClient = ttsClient;
        this.storage = storage;
    }

    public VoiceResponseDTO processVoiceQuery(MultipartFile audioFile) throws IOException {

        // 1. STT (Speech-to-Text)
        byte[] audioBytes = audioFile.getBytes();
        String userTranscript = convertSpeechToText(audioBytes);

        // STT 변환 결과를 바로 로그로 출력합니다.
        log.info("[STT_RESULT] 변환된 텍스트: {}", userTranscript);

        // STT 결과가 비어있으면 AI 호출 없이 반환 (오류 방지)
        if (userTranscript.isEmpty()) {
            return new VoiceResponseDTO(
                    "",
                    "앗! 목소리를 잘 못 들었어. \n 마이크에 대고 다시 말해줄래?",
                    // (이런 안내용 음성도 미리 GCS에 올려두고 URL을 쓸 수 있습니다)
                    "httpsC://actions.google.com/sounds/v1/cartoon/magic_chime.ogg"
            );
        }

        // 2. AI 응답 (기존 ChatService 재사용)
        String aiResponseText = chatService.generateResponse(userTranscript);

        // 3. TTS (Text-to-Speech) 및 GCS 업로드
        String aiResponseAudioUrl = convertTextToSpeechAndUpload(aiResponseText);

        // 4. DTO로 응답 반환
        // (참고: Vue에서 \n을 줄바꿈으로 인식하도록 replace 로직을 넣었습니다)
        return new VoiceResponseDTO(userTranscript, aiResponseText.replaceAll("\n", " \n"), aiResponseAudioUrl);
    }

    /**
     * STT - 오디오 바이트를 텍스트로 변환
     */
    private String convertSpeechToText(byte[] audioBytes) throws IOException {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                .setSampleRateHertz(48000)
                .setLanguageCode("ko-KR")
                .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioBytes))
                .build();

        RecognizeResponse response = speechClient.recognize(config, audio);

        // STT 결과가 없을 경우 예외 처리
        if (response.getResultsCount() > 0) {
            SpeechRecognitionResult result = response.getResultsList().get(0);
            return result.getAlternativesList().get(0).getTranscript();
        } else {
            return ""; // 빈 텍스트 반환
        }
    }

    /**
     * TTS - 텍스트를 음성으로 변환하고 GCS에 업로드 후 Public URL 반환
     */
    private String convertTextToSpeechAndUpload(String text) throws IOException {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setSsmlGender(SsmlVoiceGender.FEMALE)
                .setName("ko-KR-Standard-A")
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3) // (MP3가 OGG보다 범용성이 높습니다)
                .build();

        SynthesizeSpeechResponse response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
        ByteString audioContents = response.getAudioContent();

        // --- 여기부터 GCS 업로드 로직 (완성) ---

        // 1. 고유한 파일 이름 생성 (캐싱 방지 및 파일 식별)
        String fileName = "ai-voice/" + UUID.randomUUID().toString() + ".mp3";

        // 2. GCS에 업로드할 파일 정보(BlobInfo) 생성
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/mpeg") // (MP3의 MIME 타입)
                .build();

        // 3. GCS에 파일 업로드 실행
        storage.create(blobInfo, audioContents.toByteArray());

        // 4. 업로드된 파일의 Public URL 반환
        // (형식: https://storage.googleapis.com/[버킷이름]/[파일경로])
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }
}