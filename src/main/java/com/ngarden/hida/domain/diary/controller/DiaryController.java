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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        summaryResponse.setMessage(JsonParsing(summaryResponse.getMessage()));
        momResponse.setMessage(JsonParsing(momResponse.getMessage()));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(summaryResponse.getMessage());
            //TODO: summary가 채팅때 쓸 요약본임. 추후에 db에 넣어야함
            JsonNode summaryNode = rootNode.get("summary");
            JsonNode majorEventNode = rootNode.get("majorEvent");
//            StringBuilder summaryStringBuilder = new StringBuilder();
//            StringBuilder emotionStringBuilder = new StringBuilder();

            if( !majorEventNode.toString().equals("[]")) {
                JSONArray jsonArrayEmotions = new JSONArray();

                for(JsonNode majorEventElement : majorEventNode){
                    String mainEmotion = majorEventElement.get("mainEmotion").asText();
                    JSONObject jsonObjectEmotion = new JSONObject();
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
//                    emotionStringBuilder.append("{\"emotion\" : \"").append(EmotionTypeEnum.getByEmotionKorean(mainEmotion))
//                            .append("\", \"comment\" : \"").append(emotionResponse.getMessage()).append("\"},");
                    jsonObjectEmotion.put("emotion", EmotionTypeEnum.getByEmotionKorean(mainEmotion));
                    jsonObjectEmotion.put("comment", emotionResponse.getMessage());
                    jsonArrayEmotions.put(jsonObjectEmotion);
                }
                emotionsComment = jsonArrayEmotions.toString();
            }
            JSONObject jsonObjectSummary = new JSONObject();
            jsonObjectSummary.put("date", diaryCreateRequest.getDiaryDate());
            jsonObjectSummary.put("summary", summaryNode);
            summaryResponse.setMessage(jsonObjectSummary.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        diaryCreateRequest.setAiStatus(Boolean.TRUE);
        diaryCreateRequest.setMom(momResponse.getMessage());
        diaryCreateRequest.setSummary(summaryResponse.getMessage());
        diaryCreateRequest.setEmotions(emotionsComment);

        /**
         * DATE / SUMMARY => summaryResponse
         * 파일이름: 최정식_summary.txt
         * {
         *      "date" : "2012-02-12",
         *      "summary" :
         *      [
         *          {
         *              "event" : "~~~",
         *              "mainEmotion" : "~~~",
         *              "subEmotion" : "~~~"
         *          },
         *          {
         *              "event" : "~~~",
         *              "mainEmotion" : "~~~",
         *              "subEmotion" : "~~~"
         *          }
         *      ]
         * },
         *
         * DATE / TITLE / DETAIL / MOM / EMOTIONS
         * => diaryCreateRequest(LocalDate date, String title, String detail)
         * => momResponse(String message) / emotionsComment(String "emotion"-"comment")
         * 파일이름: 최정식_20120212.txt
         * {
         *      "date" : "2012-02-12",
         *      "title" : "~~~",
         *      "detail" : "~~~",
         *      "mom" : "~~~~",
         *      "emotions" : [{
         *          "emotion" : "~~~",
         *          "comment" : "~~~",
         *      },
         *      {
         *          "emotion" : "~~~",
         *          "comment" : "~~~",
         *       }]
         * }
         *
         */

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

    private String JsonParsing(String message){

        int firstIndex =message.indexOf('{');
        int lastIndex = message.lastIndexOf('}');

        if (firstIndex != -1 && lastIndex != -1 && firstIndex < lastIndex) {
            // '{' 문자부터 '}' 문자까지의 부분 문자열을 얻습니다.
            return message.substring(firstIndex, lastIndex + 1);
        } else {
            throw new NoExistException("Json 형식이 아닙니다.");
        }
    }

}
