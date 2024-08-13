package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 저장", description = "일기를 저장하고, 일기 본문과 EMOTIONS, MOM을 보내준다.")
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest
    ) {
        DiarySaveDTO diarySaveDTO = DiarySaveDTO.builder()
                .userId(diaryCreateRequest.getUserId())
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .DiaryDate(diaryCreateRequest.getDiaryDate())
                .aiStatus(Boolean.FALSE)
                .build();

        DiaryDailyResponse diaryDailyResponse = diaryService.saveDiary(diarySaveDTO);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{userId}/{date}")
    @Operation(summary = "특정 날짜 일기 조회", description = "해당 날짜의 일기 본문, EMOTION, MOM 내용을 반환한다. 일기 목록의 일기를 클릭했을 때 사용한다.")
    public ResponseEntity<DiaryDailyResponse> getDiaryDaily(
            @PathVariable("userId") Long userId,
            @PathVariable("date") LocalDate date
    ) {
        DiaryDailyResponse diaryDailyResponse = diaryService.getDiaryDaily(userId, date);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @DeleteMapping("/{userId}/{date}")
    @Operation(summary = "특정 날짜 일기 삭제", description = "유저의 특정 날짜 일기의 SUMMARY 내용과 일기 본문을 삭제한다.")
    public ResponseEntity deleteDiary(
            @PathVariable("userId") Long userId,
            @PathVariable("date") LocalDate date
    ) {
        diaryService.deleteDiary(userId, date);

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "전체 일기 리스트 조회", description = "일기 리스트(날짜, 제목) 반환한다. 일기 목록을 볼 때 사용한다.")
    public ResponseEntity<DiaryListResponse> getDiaryList(
            @PathVariable("userId") Long userId
    ) {
        DiaryListResponse diaryListResponse = diaryService.getDiaryList(userId);

        return ResponseEntity.ok().body(diaryListResponse);
    }
}
