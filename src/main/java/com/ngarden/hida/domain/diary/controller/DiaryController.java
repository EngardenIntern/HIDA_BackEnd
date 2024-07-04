package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<DiaryEntity> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
            ){
        DiaryEntity diaryEntity = diaryService.createDiary(diaryCreateRequest);

        UserEntity userEntity = userService.find
        DiaryDailyResponse diaryDailyResponse = DiaryDailyResponse.builder()
                .date(LocalDate.now())
                .title(diaryEntity.getTitle())
                .detail(diaryEntity.getDetail())
                .aiStatus(Boolean.FALSE)
                .summary(null)
                .comment(null)
                .userEntity(UserEntity.builder().userName().build()).build();

    }

}
