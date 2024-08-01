package com.ngarden.hida.domain.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.repository.DiaryRepository;
import com.ngarden.hida.domain.file.FileService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.externalapi.chatGPT.service.GPTService;
import com.ngarden.hida.global.config.SemaConfig;
import com.ngarden.hida.global.error.AlreadyExistException;
import com.ngarden.hida.global.error.NoExistException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

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
public class DiaryServiceImpl implements DiaryService{
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final SemaConfig semaConfig;
    private final GPTService gptService;
    private final FileService fileService;


    @Override
    public DiaryEntity saveDiary(DiaryCreateRequest diaryCreateRequest) {

        Optional<UserEntity> userEntity = userRepository.findById(diaryCreateRequest.getUserId());
        if(userEntity.isEmpty()){
            throw new NoExistException("유저 정보가 없습니다.");
        }

        //diaryFilePath: "1\\diary", summaryFilePath: "1\\summary"
        String diaryFilePath = userEntity.get().getUserId().toString() + "\\diary";
        String summaryFilePath = userEntity.get().getUserId().toString() + "\\summary";
        //diaryFileName: "2012-02-12.json", summaryFileName: "2012-02.json"
        String diaryFileName = diaryCreateRequest.getDiaryDate().toString() + ".json";
        String summaryFileName = diaryCreateRequest.getDiaryDate().getYear() + "-" + diaryCreateRequest.getDiaryDate().getMonthValue() + ".json";
        String diaryFileContent = createJsonByDiaryRequest(diaryCreateRequest);

        //파일 생성, 본문 쓰기
        File diaryFile = fileService.createOrOpenFileInPath(diaryFilePath, diaryFileName);
        File summaryFile = fileService.createOrOpenFileInPath(summaryFilePath, summaryFileName);

        if (diaryFile.length() != 0) {
            throw new AlreadyExistException("해당 날짜의 다이어리가 이미 존재합니다 :" + diaryFileName);
        }

        fileService.writeStringInFile(diaryFile, diaryFileContent, FALSE);
        fileService.writeStringInFile(summaryFile, diaryCreateRequest.getSummary(), TRUE);
        /**
         *  Title / Detail / [Summary] / MoM / Emotions
         *
         * 1. 파일 열기 (적절한 파일 위치)
         *
         * 2. 파일에 글 옮기기 - 정확히는 fwrite()
         * 3. 파일 경로를 diaryEntity에 저장
         * 4. diaryEntity Save
         */

        DiaryEntity diaryEntity = DiaryEntity.builder()
                .user(userEntity.get())
                .aiStatus(diaryCreateRequest.getAiStatus())
                .diaryDate(diaryCreateRequest.getDiaryDate())
                .title(diaryCreateRequest.getTitle())
                .build();

        return diaryRepository.save(diaryEntity);
    }

    @Override
    public DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new NoExistException("유저 정보가 없습니다.");
        }

        String path = userId.toString() + "\\diary";
        String fileName = date.toString() + ".json";
        File file = fileService.createOrOpenFileInPath(path, fileName);
        String content = fileService.readStringInFile(file);
        JSONObject jsonObject = new JSONObject(content);

        DiaryEntity diaryEntity = diaryRepository.findByUserAndDiaryDate(userEntity.get(), date);
        return DiaryDailyResponse.builder()
                .date(diaryEntity.getDiaryDate())
                .title(diaryEntity.getTitle())
                .aiStatus(diaryEntity.getAiStatus())
                .userName(userEntity.get().getUserName())
                .detail(jsonObject.get("detail").toString())
                .emotions(jsonObject.get("emotions").toString())
                .mom(jsonObject.get("mom").toString())
                .build();
    }

    @Override
    public DiaryListResponse getDiaryList(Long userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if(userEntity.isEmpty()){
            throw new NoExistException("유저정보가 없습니다.");
        }
        List<DiaryEntity> diaryEntityList = diaryRepository.findByUser(userEntity.get());
        List<DiaryDailyResponse> diaryDailyResponseList = new ArrayList<>();
        for (DiaryEntity diaryEntity : diaryEntityList){
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
                .userName(userEntity.get().getUserName())
                .build();
    }

    /**
     * gpt한테 보낼 데이터 가공
     */
    @Override
    public MessageResponse createDiaryByEmotionGpt(String diaryDetail, JsonNode diarySummary, String inputAssistantId) {
        String diaryDetailAndSummary = "{\"diary\" : " + diaryDetail +
                ", \"event\" : " + diarySummary.get("event") +
                ", \"emotion\" : " + diarySummary.get("subEmotion") + "}";

        return createDiaryByGpt(diaryDetailAndSummary, inputAssistantId);
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

    /**
     *{
     * 	"date" : "2012-02-12",
     * 	"title" : "~~~",
     * 	"detail" : "~~~",
     * 	"mom" : "~~~~",
     * 	"emotions" : [{
     * 		"emotion" : "~~~",
     * 		"comment" : "~~~",
     * 	    },
     *    {
     * 		"emotion" : "~~~",
     * 		"comment" : "~~~",
     *    }]
     * }
     * @param diaryCreateRequest
     * @return diaryCreateRequest에 있는 내용으로 파일에 넣을 Json형식을 생성해서 반환
     */
    @Override
    public String createJsonByDiaryRequest(DiaryCreateRequest diaryCreateRequest) {
        JSONObject object = new JSONObject();
        object.put("date", diaryCreateRequest.getDiaryDate());
        object.put("title", diaryCreateRequest.getTitle());
        object.put("detail", diaryCreateRequest.getDetail());
        object.put("mom", diaryCreateRequest.getMom());
        JSONArray emotions = new JSONArray(diaryCreateRequest.getEmotions());
        object.put("emotions", emotions);

        return object.toString();
    }
}
