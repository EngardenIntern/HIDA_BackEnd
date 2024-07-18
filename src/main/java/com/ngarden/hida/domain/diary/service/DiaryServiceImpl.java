package com.ngarden.hida.domain.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.entity.EmotionTypeEnum;
import com.ngarden.hida.domain.diary.repository.DiaryRepository;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.externalapi.chatGPT.service.GPTService;
import com.ngarden.hida.global.config.SemaConfig;
import com.ngarden.hida.global.error.NoExistException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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



    @Override
    public DiaryEntity saveDiary(DiaryCreateRequest diaryCreateRequest) {

        Optional<UserEntity> userEntity = userRepository.findById(diaryCreateRequest.getUserId());
        if(userEntity.isEmpty()){
            throw new NoExistException("유저 정보가 없습니다.");
        }
        DiaryEntity diaryEntity = DiaryEntity.builder()
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .user(userEntity.get())
                .mom(diaryCreateRequest.getMom())
                .summary(diaryCreateRequest.getSummary())
                .emotions(diaryCreateRequest.getEmotions())
                .aiStatus(diaryCreateRequest.getAiStatus())
                .diaryDate(diaryCreateRequest.getDiaryDate())
                .build();


        return diaryRepository.save(diaryEntity);
    }

    @Override
    public DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new NoExistException("유저 정보가 없습니다.");
        }

        DiaryEntity diaryEntity = diaryRepository.findByUserAndDiaryDate(userEntity.get(), date);

        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(diaryEntity.getDiaryDate())
                .title(diaryEntity.getTitle())
                .detail(diaryEntity.getDetail())
                .aiStatus(diaryEntity.getAiStatus())
                .summary(diaryEntity.getSummary())
                .mom(diaryEntity.getMom())
                .emotions(diaryEntity.getEmotions())
                .userName(userEntity.get().getUserName())
                .diaryDate(diaryEntity.getDiaryDate())
                .build();

        return diaryDailyResponse;
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
                    .detail(diaryEntity.getDetail())
                    .aiStatus(diaryEntity.getAiStatus())
                    .summary(diaryEntity.getSummary())
                    .mom(diaryEntity.getMom())
                    .emotions(diaryEntity.getEmotions())
                    .userName(diaryEntity.getUser().getUserName())
                    .diaryDate(diaryEntity.getDiaryDate())
                    .build();

            diaryDailyResponseList.add(diaryDailyResponse);
        }
        DiaryListResponse diaryListResponse = DiaryListResponse.builder()
                .diaryDailyResponseList(diaryDailyResponseList)
                .userName(userEntity.get().getUserName())
                .build();

        return diaryListResponse;
    }

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
}
