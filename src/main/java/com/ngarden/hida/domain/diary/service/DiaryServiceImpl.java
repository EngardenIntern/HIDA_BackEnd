package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@Service
@Transactional
@RequiredArgsConstructor
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
                .comment(diaryCreateRequest.getComment())
                .summary(diaryCreateRequest.getSummary())
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
                .comment(diaryEntity.getComment())
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
                    .comment(diaryEntity.getComment())
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
    public MessageResponse createDiaryByGpt(DiaryCreateRequest diaryCreateRequest, String InputAssistantId) {

        List<MessageResponse> messageResponseList = List.of();
        Semaphore semaphore = semaConfig.semaphore();
        CreateThreadAndRunResponse AIResponse = null;
        try {
            semaphore.acquire();
            CreateThreadAndRunRequest AIRequest = gptService.generateThreadAndRun(InputAssistantId, diaryCreateRequest.getDetail());
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
            while(Boolean.TRUE) {
                Thread.sleep(500);
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
