package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import com.ngarden.hida.externalapi.chatGPT.service.GPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final GPTService gptService;

    @Value("${OPENAI.ASSISTANT-ID}")
    private String assistantId;

    @PostMapping
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ){
        DiaryEntity diaryEntity = diaryService.createDiary(diaryCreateRequest);


        CreateThreadAndRunRequest AIRequest =  gptService.generateThreadAndRun(assistantId);
        Optional<CreateThreadAndRunResponse> AIResponse = Optional.ofNullable(gptService.createThreadAndRun(AIRequest));
        if(AIResponse.isEmpty()){
            throw new RuntimeException();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<MessageResponse> messageResponseList = gptService.getListMessage(AIResponse.get().getThreadId());

        System.out.println("messageList: " + messageResponseList.toString());
        String message = String.valueOf(messageResponseList.get(0));

        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(LocalDate.now())
                .title(diaryEntity.getTitle())
                .detail(diaryEntity.getDetail())
                .aiStatus(Boolean.TRUE)
                .summary(null)
                .comment(messageResponseList.get(0).getMessage())
                .userName(diaryEntity.getUser().getUserName()).build();

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
