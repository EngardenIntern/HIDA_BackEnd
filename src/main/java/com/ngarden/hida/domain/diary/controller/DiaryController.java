package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ){
        DiaryEntity diaryEntity = diaryService.createDiary(diaryCreateRequest);

        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(LocalDate.now())
                .title(diaryEntity.getTitle())
                .detail(diaryEntity.getDetail())
                .aiStatus(Boolean.FALSE)
                .summary(null)
                .comment(null)
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
