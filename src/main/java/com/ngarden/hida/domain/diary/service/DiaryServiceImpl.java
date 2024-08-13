package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.entity.EmotionEnum;
import com.ngarden.hida.domain.diary.repository.DiaryRepository;
import com.ngarden.hida.domain.file.FileService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.externalapi.chatGPT.service.GPTService;
import com.ngarden.hida.global.config.SemaConfig;
import com.ngarden.hida.global.error.NoExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import static java.lang.Boolean.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final SemaConfig semaConfig;
    private final GPTService gptService;
    private final FileService fileService;
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

    @Override
    public DiaryDailyResponse saveDiary(DiarySaveDTO diarySaveDTO) {
        //유저 존재 여부 확인
        UserEntity userEntity = userService.findById(diarySaveDTO.getUserId());

        // 디비에 다이어리가 있는지 확인. 다이어리가 있는데 save가 들어오면 "수정"으로 간주
        Optional<DiaryEntity> diaryEntity = Optional.ofNullable(diaryRepository.findByUserAndDiaryDate(userEntity, diarySaveDTO.getDiaryDate()));
        if (diaryEntity.isPresent()) { // 다이어리가 있다면
            log.info("기존의 다이어리를 삭제하고 현재 다이어리를 저장합니다. : " + diarySaveDTO.getDiaryDate());
            deleteDiary(diarySaveDTO.getUserId(), diarySaveDTO.getDiaryDate());
        }

        //momGpt 생성
        MessageResponse momResponse = createDiaryByGpt(diarySaveDTO.getDetail(), momAssistantId);
        JSONObject momObject = new JSONObject(parseJson(momResponse.getMessage()));
        momResponse.setMessage(momObject.getString("comment"));
        diarySaveDTO.setMom(momResponse.getMessage());

        //summaryGpt 생성
        MessageResponse summaryResponse = createDiaryByGpt(diarySaveDTO.getDetail(), summaryAssistantId);
        summaryResponse.setMessage(parseJson(summaryResponse.getMessage()));
        JSONObject rootJSONObject = new JSONObject(summaryResponse.getMessage());
        JSONArray summaryJsonArray = rootJSONObject.getJSONArray("summary");
        JSONArray majorEventJsonArray = rootJSONObject.getJSONArray("majorEvent"); //emotion생성할 때 쓸 majorEvent
        for (int i = 0; i < summaryJsonArray.length(); i++) {                           //summayList에서 "평범한" 삭제
            if(summaryJsonArray.getJSONObject(i).getString("mainEmotion").equals("평범한")){
                summaryJsonArray.remove(i);
                i--;
            }
        }

        JSONObject jsonObjectSummary = new JSONObject()
                .put("date", diarySaveDTO.getDiaryDate())
                .put("summary", summaryJsonArray);
        diarySaveDTO.setSummary(jsonObjectSummary.toString());

        //emotionsGpt 생성
        String emotionsComment = null;
        List<EmotionEnum> emotionEnumList = new ArrayList<>();

        //gpt가 잘못 해서 []가 들어오면
        if(majorEventJsonArray.toString().equals("[]")){
            emotionsComment = "[{ " +
                    "\"emotion\": \"Empty\"," +
                    "\"comment\":\"Empty\"}]";
        } else {
            JSONArray jsonArrayEmotions = new JSONArray();

            for (int i = 0; i < majorEventJsonArray.length(); i++) {
                JSONObject majorEventJSONObject = majorEventJsonArray.getJSONObject(i);
                String mainEmotion = majorEventJSONObject.getString("mainEmotion");

                //summary Assistant가 2개의 majorEvent를 고르는데 그 둘의 감정이 같다면 건너뜀
                if(i > 0 && emotionEnumList.get(i - 1) == EmotionEnum.getEmotionEnumByKorean(mainEmotion)){
                    continue;
                }

                // 각각의 Emotion Assistant한테 보내기
                String diaryDetailAndSummary = "{\"diary\" : " + diarySaveDTO.getDetail() +
                        ", \"event\" : " + majorEventJSONObject.getString("event") +
                        ", \"emotion\" : " + majorEventJSONObject.getString("subEmotion") + "}";

                MessageResponse emotionResponse = switch (EmotionEnum.getEmotionEnumByKorean(mainEmotion)) {
                    case JOY -> {
                        emotionEnumList.add(EmotionEnum.JOY);
                        yield createDiaryByGpt(diaryDetailAndSummary, joyAssistantId);
                    }
                    case SADNESS -> {
                        emotionEnumList.add(EmotionEnum.SADNESS);
                        yield createDiaryByGpt(diaryDetailAndSummary, sadnessAssistantId);
                    }
                    case ANGER -> {
                        emotionEnumList.add(EmotionEnum.ANGER);
                        yield createDiaryByGpt(diaryDetailAndSummary, angerAssistantId);
                    }
                    case FEAR -> {
                        emotionEnumList.add(EmotionEnum.FEAR);
                        yield createDiaryByGpt(diaryDetailAndSummary, fearAssistantId);
                    }
                    default -> null;
                };
                //TODO:응답 받아서 COMMNET STRING에 추가
                assert emotionResponse != null;

                JSONObject jsonObjectEmotion = new JSONObject();
                jsonObjectEmotion.put("emotion", EmotionEnum.getEmotionEnumByKorean(mainEmotion));
                jsonObjectEmotion.put("comment", emotionResponse.getMessage());
                jsonArrayEmotions.put(jsonObjectEmotion);
            }

            emotionsComment = jsonArrayEmotions.toString();
        }

        //UserEntity에 Diary, Emotion Count 반영
        userService.updateCounts(diarySaveDTO.getUserId(), emotionEnumList, 1);

        diarySaveDTO.setEmotions(emotionsComment);
        diarySaveDTO.setAiStatus(TRUE);

        //diarySaveDTO를 보내서 파일로 전부 저장한다.
        saveDiaryToFile(diarySaveDTO);

        //diaryEntity 저장
        DiaryEntity newDiaryEntity = DiaryEntity.builder()
                .user(userEntity)
                .aiStatus(diarySaveDTO.getAiStatus())
                .diaryDate(diarySaveDTO.getDiaryDate())
                .title(diarySaveDTO.getTitle())
                .aiStatus(diarySaveDTO.getAiStatus())
                .build();
        diaryRepository.save(newDiaryEntity);


        return DiaryDailyResponse.builder()
                .date(diarySaveDTO.getDiaryDate())
                .title(diarySaveDTO.getTitle())
                .detail(diarySaveDTO.getDetail())
                .emotions(diarySaveDTO.getEmotions())
                .mom(diarySaveDTO.getMom())
                .aiStatus(diarySaveDTO.getAiStatus())
                .userName(userEntity.getUserName())
                .build();
    }

    @Override
    public DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date) {
        UserEntity userEntity = userService.findById(userId);
        Optional<DiaryEntity> diaryEntity = Optional.ofNullable(diaryRepository.findByUserAndDiaryDate(userEntity, date));
        if (diaryEntity.isEmpty()) {
            throw new NoExistException("해당 날짜의 다이어리가 없습니다. :" + date);
        }

        String path = userId.toString() + File.separator + "diary";
        String fileName = date.toString() + ".json";
        File file = fileService.createOrOpenFileInPath(path, fileName);
        String content = fileService.readStringInFile(file);
        JSONObject jsonObject = new JSONObject(content);

        return DiaryDailyResponse.builder()
                .date(diaryEntity.get().getDiaryDate())
                .title(diaryEntity.get().getTitle())
                .aiStatus(diaryEntity.get().getAiStatus())
                .userName(userEntity.getUserName())
                .detail(jsonObject.get("detail").toString())
                .emotions(jsonObject.get("emotions").toString())
                .mom(jsonObject.get("mom").toString())
                .build();
    }

    @Override
    public DiaryListResponse getDiaryList(Long userId) {
        UserEntity userEntity = userService.findById(userId);

        List<DiaryEntity> diaryEntityList = diaryRepository.findByUser(userEntity);
        List<DiaryDailyResponse> diaryDailyResponseList = new ArrayList<>();
        for (DiaryEntity diaryEntity : diaryEntityList) {
            DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                    .date(diaryEntity.getDiaryDate())
                    .title(diaryEntity.getTitle())
                    .aiStatus(diaryEntity.getAiStatus())
                    .userName(diaryEntity.getUser().getUserName())
                    .build();

            diaryDailyResponseList.add(diaryDailyResponse);
        }

        return DiaryListResponse.builder()
                .diaryDailyResponseList(diaryDailyResponseList)
                .userName(userEntity.getUserName())
                .build();
    }

    @Override
    public MessageResponse createDiaryByGpt(String prompt, String inputAssistantId) {

        List<MessageResponse> messageResponseList = List.of();
        Semaphore semaphore = semaConfig.semaphore();
        CreateThreadAndRunResponse AIResponse = null;
        try {
            semaphore.acquire();
            CreateThreadAndRunRequest AIRequest = gptService.generateThreadAndRun(prompt, inputAssistantId);

            Optional<CreateThreadAndRunResponse> GPTAIResponse = Optional.ofNullable(gptService.createThreadAndRun(AIRequest));
            if (GPTAIResponse.isEmpty()) {
                throw new NoExistException("GPTAIResponse가 없습니다.");
            }
            AIResponse = GPTAIResponse.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
        try {
            semaphore.acquire();
            while(TRUE) {
                Thread.sleep(500);
                assert AIResponse != null;
                messageResponseList = gptService.getListMessage(AIResponse.getThreadId());
                if (!(messageResponseList.isEmpty() || messageResponseList.get(0).getMessage() == null)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }

        log.info("GPT message response:" + messageResponseList.get(0).getMessage());

        String message = String.valueOf(messageResponseList.get(0).getMessage());
        String messageId = messageResponseList.get(0).getId();
        String assistantId = messageResponseList.get(0).getAssistantId();
        String threadId = messageResponseList.get(0).getThreadId();
        String role = messageResponseList.get(0).getRole();

        return MessageResponse.builder()
                .id(messageId)
                .assistantId(assistantId)
                .threadId(threadId)
                .role(role)
                .message(message)
                .build();
    }

    @Override
    public void deleteDiary(Long userId, LocalDate date) {
        //유저와 다이어리 있는지 확인
        UserEntity userEntity = userService.findById(userId);
        Optional<DiaryEntity> diaryEntity = Optional.ofNullable(diaryRepository.findByUserAndDiaryDate(userEntity, date));
        if (diaryEntity.isEmpty()) {
            throw new NoExistException("해당 날짜의 다이어리가 없습니다. :" + date);
        }

        //diaryFilePath: "1\\diary", summaryFilePath: "1\\summary"
        String diaryFilePath = userId + File.separator + "diary";
        String summaryFilePath = userId + File.separator + "summary";
        //diaryFileName: "2012-02-12.json", summaryFileName: "2012-02.json"
        String diaryFileName = date + ".json";
        String summaryFileName = date.getYear() + "-" + date.getMonthValue() + ".json";

        File diaryFile = fileService.createOrOpenFileInPath(diaryFilePath, diaryFileName);
        File summaryFile = fileService.createOrOpenFileInPath(summaryFilePath, summaryFileName);

        if (diaryFile.length() == 0) {
            throw new NoExistException("DB에는 다이어리가 있으나 일기 파일이 없습니다. :" + diaryFileName);
        }

        List<EmotionEnum> emotionEnumList = new ArrayList<>();

        JSONObject diaryJsonObject = new JSONObject(fileService.readStringInFile(diaryFile));
        JSONArray emotionsJsonArray = diaryJsonObject.getJSONArray("emotions");
        for (int i = 0; i < emotionsJsonArray.length(); i++) {
            JSONObject majorEventJSONObject = emotionsJsonArray.getJSONObject(i);
            switch (majorEventJSONObject.getString("emotion")) {
                case "JOY" -> emotionEnumList.add(EmotionEnum.JOY);
                case "SADNESS" -> emotionEnumList.add(EmotionEnum.SADNESS);
                case "ANGER" -> emotionEnumList.add(EmotionEnum.ANGER);
                case "FEAR" -> emotionEnumList.add(EmotionEnum.FEAR);
                case "Empty" -> {
                    break;
                }
                default ->
                        throw new IllegalArgumentException("Unknown emotion: " + majorEventJSONObject.getString("emotion"));
            }
        }

        JSONObject summaryJsonObject = new JSONObject(fileService.readStringInFile(summaryFile));
        JSONArray diarySummeriesJsonArray = summaryJsonObject.getJSONArray("diarySummaries");
        for (int i = 0; i < diarySummeriesJsonArray.length(); i++) {
            if(diarySummeriesJsonArray.getJSONObject(i).getString("date").equals(date.toString())){
                diarySummeriesJsonArray.remove(i);
            }
        }

        fileService.writeStringInFile(summaryFile, summaryJsonObject.toString(), false);
        fileService.deleteFile(diaryFile);
        diaryRepository.delete(diaryEntity.get());

        userService.updateCounts(userId, emotionEnumList, -1);
    }

    /**
     * diarySaveDTO의 정보로 diary와 summary의 파일을 저장함.
     * 해당 날짜의 일기가 이미 있는 경우 내용을 덮어씀(일기 수정)
     * @param diarySaveDTO
     */
    @Override
    public void saveDiaryToFile(DiarySaveDTO diarySaveDTO) {
        //diaryFilePath: "1\\diary", summaryFilePath: "1\\summary"
        String diaryFilePath = diarySaveDTO.getUserId() + File.separator + "diary";
        String summaryFilePath = diarySaveDTO.getUserId() + File.separator + "summary";
        //diaryFileName: "2012-02-12.json", summaryFileName: "2012-02.json"
        String diaryFileName = diarySaveDTO.getDiaryDate() + ".json";
        String summaryFileName = diarySaveDTO.getDiaryDate().getYear() + "-" + diarySaveDTO.getDiaryDate().getMonthValue() + ".json";
        String diaryFileContent = createJsonByDiaryDTO(diarySaveDTO);
        //파일 생성, 본문 쓰기
        File diaryFile = fileService.createOrOpenFileInPath(diaryFilePath, diaryFileName);
        File summaryFile = fileService.createOrOpenFileInPath(summaryFilePath, summaryFileName);

        //TODO: 일기 수정도 가능하도록 수정해야함
        //일기를 썼는데 이미 해당 날짜의 일기 파일이 있는 경우
        if (diaryFile.length() != 0) {
            log.info("해당 날짜의 다이어리가 있었고 다이어리를 \"수정\" 했습니다. {}", diaryFileName);
        } else {
            log.info("해당 날짜의 다이어리를 새롭게 \"생성\" 했습니다. {}", diaryFileName);
        }

        /*
        그 달의 summary파일을 처음 생성했을 경우
        {
            "diarySummaries" : []
        }
         */
        JSONObject summaryJsonObject = null;
        if (summaryFile.length() == 0) {
            summaryJsonObject = new JSONObject("""
                    {
                      "diarySummaries" : []
                    }""");
        } else {
            String summaryFileText = fileService.readStringInFile(summaryFile);
            summaryJsonObject = new JSONObject(summaryFileText);
        }
        JSONArray diarySummariesJsonArray = summaryJsonObject.getJSONArray("diarySummaries");
        for (int i = 0; i < diarySummariesJsonArray.length(); i++) {
            JSONObject majorEventJsonObject = diarySummariesJsonArray.getJSONObject(i);
            if (majorEventJsonObject.getString("date").equals(diarySaveDTO.getDiaryDate().toString())) {
                diarySummariesJsonArray.remove(i);
                i--;
            }
        }

        JSONObject diaryDTOSummaryJsonObjcet = new JSONObject(diarySaveDTO.getSummary());
        summaryJsonObject.getJSONArray("diarySummaries").put(diaryDTOSummaryJsonObjcet);

        fileService.writeStringInFile(diaryFile, diaryFileContent, FALSE);
        fileService.writeStringInFile(summaryFile, summaryJsonObject.toString(), FALSE);

    }

    /**
     * String의 앞과 끝을 {~~}로 마감해서 반환해줌
     *
     * @param message 파싱할 String
     * @return 파싱된 String
     */
    @Override
    public String parseJson(String message) {
        int firstIndex = message.indexOf('{');
        int lastIndex = message.lastIndexOf('}');

        if (firstIndex != -1 && lastIndex != -1 && firstIndex < lastIndex) {
            // '{' 문자부터 '}' 문자까지의 부분 문자열을 얻습니다.
            return message.substring(firstIndex, lastIndex + 1);
        } else {
            throw new NoExistException("Json 형식이 아닙니다.");
        }
    }

    /**
     * {
     * "date" : "2012-02-12",
     * "title" : "~~~",
     * "detail" : "~~~",
     * "mom" : "~~~~",
     * "emotions" : [{
     * "emotion" : "~~~",
     * "comment" : "~~~",
     * },
     * {
     * "emotion" : "~~~",
     * "comment" : "~~~",
     * }]
     * }
     *
     * @param diarySaveDTO
     * @return DiarySaveDTO에 있는 내용으로 diary 파일에 넣을 Json형식을 생성해서 반환
     */
    @Override
    public String createJsonByDiaryDTO(DiarySaveDTO diarySaveDTO) {
        JSONObject object = new JSONObject();
        object.put("date", diarySaveDTO.getDiaryDate());
        object.put("title", diarySaveDTO.getTitle());
        object.put("detail", diarySaveDTO.getDetail());
        object.put("mom", diarySaveDTO.getMom());
        JSONArray emotions = new JSONArray(diarySaveDTO.getEmotions());
        object.put("emotions", emotions);

        return object.toString();
    }
}
