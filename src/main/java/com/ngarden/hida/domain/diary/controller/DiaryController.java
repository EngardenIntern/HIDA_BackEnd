package com.ngarden.hida.domain.diary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.EmotionTypeEnum;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.global.error.NoExistException;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "일기 저장", description = "일기를 저장하고, 일기 본문과 EMOTIONS, MOM을 보내준다.")
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ){
        //유저 있는지 확인
        Optional<UserEntity> userEntity = Optional.ofNullable(userService.findById(diaryCreateRequest.getUserId()));
        //TODO:파일 있는지 없는지 여기서 확인 하면 좋을듯

        MessageResponse momResponse =  diaryService.createDiaryByGpt(diaryCreateRequest.getDetail(), momAssistantId);
        MessageResponse summaryResponse = diaryService.createDiaryByGpt(diaryCreateRequest.getDetail(), summaryAssistantId);
        MessageResponse emotionResponse = null;
        String emotionsComment = null;

        JSONObject momObject = new JSONObject(JsonParsing(momResponse.getMessage()));

        summaryResponse.setMessage(JsonParsing(summaryResponse.getMessage()));
        momResponse.setMessage(momObject.get("comment").toString());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(summaryResponse.getMessage());
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
            else{
                emotionsComment = "[{ " +
                        "\"emotion\": \"Empty\"," +
                        "\"comment\":\"Empty\"}]";
            }
            JSONObject jsonObjectSummary = new JSONObject();
            jsonObjectSummary.put("date", diaryCreateRequest.getDiaryDate());
            jsonObjectSummary.put("summary", summaryNode);
            summaryResponse.setMessage(jsonObjectSummary.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        DiarySaveDTO diarySaveDTO = DiarySaveDTO.builder()
                .userId(diaryCreateRequest.getUserId())
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .mom(momResponse.getMessage())
                .summary(summaryResponse.getMessage() + ",")
                .emotions(emotionsComment)
                .aiStatus(Boolean.TRUE)
                .DiaryDate(diaryCreateRequest.getDiaryDate())
                .build();

        diaryService.saveDiary(diarySaveDTO);

        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(diarySaveDTO.getDiaryDate())
                .title(diarySaveDTO.getTitle())
                .detail(diarySaveDTO.getDetail())
                .emotions(diarySaveDTO.getEmotions())
                .mom(diarySaveDTO.getMom())
                .aiStatus(diarySaveDTO.getAiStatus())
                .userName(userEntity.get().getUserName())
                .build();

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{userId}/{date}")
    @Operation(summary = "특정 날짜 일기 조회", description = "해당 날짜의 일기 본문, EMOTION, MOM 내용을 반환한다. 일기 목록의 일기를 클릭했을 때 사용한다.")
    public ResponseEntity<DiaryDailyResponse> getDiaryDaily(
            @PathVariable("userId") Long userId,
            @PathVariable("date") LocalDate date
    ){
        DiaryDailyResponse diaryDailyResponse = diaryService.getDiaryDaily(userId, date);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "전체 일기 리스트 조회", description = "일기 리스트(날짜, 제목) 반환한다. 일기 목록을 볼 때 사용한다.")
    public ResponseEntity<DiaryListResponse> getDiaryList(
            @PathVariable("userId") Long userId
    ){
        DiaryListResponse diaryListResponse = diaryService.getDiaryList(userId);

        return ResponseEntity.ok().body(diaryListResponse);
    }

    /**
     * String의 앞과 끝을 {~~}로 마감해서 반환해줌
     * @param message 파싱할 String
     * @return 파싱된 String
     */
    private String JsonParsing(String message){

        int firstIndex = message.indexOf('{');
        int lastIndex = message.lastIndexOf('}');

        if (firstIndex != -1 && lastIndex != -1 && firstIndex < lastIndex) {
            // '{' 문자부터 '}' 문자까지의 부분 문자열을 얻습니다.
            return message.substring(firstIndex, lastIndex + 1);
        } else {
            throw new NoExistException("Json 형식이 아닙니다.");
        }
    }

}
