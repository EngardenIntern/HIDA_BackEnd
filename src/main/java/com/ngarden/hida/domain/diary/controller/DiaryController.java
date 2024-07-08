package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.repository.DiaryRepository;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.externalapi.chatGPT.service.GPTService;
import com.ngarden.hida.global.config.SemaConfig;
import com.ngarden.hida.global.error.NoExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final UserService userService;

    @Value("${OPENAI.ASSISTANT-ID.COMMENT}")
    private String commentAssistantId;

    @Value("${OPENAI.ASSISTANT-ID.SUMMARY}")
    private String summaryAssistantId;

    @PostMapping
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ){
        MessageResponse commentResponse =  diaryService.createDiaryByGpt(diaryCreateRequest, commentAssistantId);
        MessageResponse summaryResponse = diaryService.createDiaryByGpt(diaryCreateRequest, summaryAssistantId);

        diaryCreateRequest.setAiStatus(Boolean.TRUE);
        diaryCreateRequest.setComment(commentResponse.getMessage());
        diaryCreateRequest.setSummary(summaryResponse.getMessage());

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
                .comment(diaryCreateRequest.getComment())
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
