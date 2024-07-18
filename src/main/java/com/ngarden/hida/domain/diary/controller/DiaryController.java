package com.ngarden.hida.domain.diary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.EmotionTypeEnum;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.global.error.NoExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final UserService userService;

    @Value("${OPENAI.ASSISTANT-ID.SUMMARY}")
    private String summaryAssistantId;
    @Value("${OPENAI.ASSISTANT-ID.MOM}")
    private String momAssistantId;
    @Value("${OPENAI.ASSISTANT-ID.JOY}")
    private String joyAssistantId;
    @Value("${OPENAI.ASSISTANT-ID.ANGER}")
    private String angerAssistantId;
    @Value("${OPENAI.ASSISTANT-ID.FEAR}")
    private String fearAssistantId;
    @Value("${OPENAI.ASSISTANT-ID.SADNESS}")
    private String sadnessAssistantId;

    @PostMapping
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ){
        MessageResponse momResponse =  diaryService.createDiaryByGpt(diaryCreateRequest.getDetail(), momAssistantId);
        MessageResponse summaryResponse = diaryService.createDiaryByGpt(diaryCreateRequest.getDetail(), summaryAssistantId);
        MessageResponse emotionResponse = null;
        String emotionsComment = null;


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(summaryResponse.getMessage());
            //TODO: summary가 채팅때 쓸 요약본임. 추후에 db에 넣어야함
            JsonNode summaryNode = rootNode.get("summary");
            JsonNode majorEventNode = rootNode.get("majorEvent");
            StringBuilder summaryStringBuilder = new StringBuilder();
            StringBuilder emotionStringBuilder = new StringBuilder();
            emotionStringBuilder.append("[");

            if( !majorEventNode.toString().equals("[]")) {

                for(JsonNode majorEventElement : majorEventNode){
                    String mainEmotion = majorEventElement.get("mainEmotion").asText();
                    // Assistant한테 보내기
                    emotionResponse = switch (EmotionTypeEnum.getByEmotionKorean(mainEmotion)) {
                        case JOY ->
                                diaryService.createDiaryByEmotionGpt(diaryCreateRequest.getDetail(), majorEventElement, joyAssistantId);
                        case SADNESS ->
                                diaryService.createDiaryByEmotionGpt(diaryCreateRequest.getDetail(), majorEventElement, sadnessAssistantId);
                        case ANGER ->
                                diaryService.createDiaryByEmotionGpt(diaryCreateRequest.getDetail(), majorEventElement, angerAssistantId);
                        case FEAR ->
                                diaryService.createDiaryByEmotionGpt(diaryCreateRequest.getDetail(), majorEventElement, fearAssistantId);
                        default -> emotionResponse;
                    };
                    //TODO:응답 받아서 COMMNET STRING에 추가
                    assert emotionResponse != null;
                    emotionStringBuilder.append("{\"emotion\" : \"").append(EmotionTypeEnum.getByEmotionKorean(mainEmotion))
                            .append("\", \"comment\" : \"").append(emotionResponse.getMessage()).append("\"},");
                }
                emotionStringBuilder.deleteCharAt(emotionStringBuilder.lastIndexOf(","));
                emotionStringBuilder.append("]");
                emotionsComment = emotionStringBuilder.toString();

            }
            summaryStringBuilder.append("{\"date\" : \"").append(diaryCreateRequest.getDiaryDate())
                    .append("\",").append("\"summary\" : ").append(summaryNode.toString()).append("}");
            summaryResponse.setMessage(summaryStringBuilder.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        diaryCreateRequest.setAiStatus(Boolean.TRUE);
        diaryCreateRequest.setMom(momResponse.getMessage());
        diaryCreateRequest.setSummary(summaryResponse.getMessage());
        diaryCreateRequest.setEmotions(emotionsComment);

        diaryService.saveDiary(diaryCreateRequest);

        Optional<UserEntity> userEntity = Optional.ofNullable(userService.findById(diaryCreateRequest.getUserId()));
        if(userEntity.isEmpty()){
            throw new NoExistException("유저가 없습니다.");
        }
        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(diaryCreateRequest.getDiaryDate())
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .aiStatus(diaryCreateRequest.getAiStatus())
                .summary(diaryCreateRequest.getSummary())
                .mom(diaryCreateRequest.getMom())
                .emotions(diaryCreateRequest.getEmotions())
                .userName(userEntity.get().getUserName())
                .diaryDate(diaryCreateRequest.getDiaryDate())
                .build();

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{userId}/{date}")
    public ResponseEntity<DiaryDailyResponse> getDiaryDaily(
            @PathVariable("userId") Long userId,
            @PathVariable("date") LocalDate date
    ){
        DiaryDailyResponse diaryDailyResponse = diaryService.getDiaryDaily(userId, date);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DiaryListResponse> getDiaryList(
            @PathVariable("userId") Long userId
    ){
        DiaryListResponse diaryListResponse = diaryService.getDiaryList(userId);

        return ResponseEntity.ok().body(diaryListResponse);
    }

}
