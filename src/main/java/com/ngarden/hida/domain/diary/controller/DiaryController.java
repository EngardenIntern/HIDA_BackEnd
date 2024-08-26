package com.ngarden.hida.domain.diary.controller;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.service.DiaryService;
import com.ngarden.hida.domain.file.FileService;
import com.ngarden.hida.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/diary")
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.0.161:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final UserService userService;
    private final FileService fileService;

    @PostMapping
    @Operation(summary = "일기 저장", description = "일기를 저장하고, 일기 본문과 EMOTIONS, MOM을 보내준다.")
    public ResponseEntity<DiaryDailyResponse> createDiary(
            @RequestBody DiaryCreateRequest diaryCreateRequest,
            Authentication authentication
    ) {
        final Long userId = userService.getLoggedInUserId(authentication);

        DiarySaveDTO diarySaveDTO = DiarySaveDTO.builder()
                .userId(userId)
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .DiaryDate(diaryCreateRequest.getDiaryDate())
                .aiStatus(Boolean.FALSE)
                .build();

        DiaryDailyResponse diaryDailyResponse = diaryService.saveDiary(diarySaveDTO);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @GetMapping("/{date}")
    @Operation(summary = "특정 날짜 일기 조회", description = "해당 날짜의 일기 본문, EMOTION, MOM 내용을 반환한다. 일기 목록의 일기를 클릭했을 때 사용한다.")
    public ResponseEntity<DiaryDailyResponse> getDiaryDaily(
            @PathVariable("date") LocalDate date,
            Authentication authentication
    ) {
        final Long userId = userService.getLoggedInUserId(authentication);

        DiaryDailyResponse diaryDailyResponse = diaryService.getDiaryDaily(userId, date);

        return ResponseEntity.ok().body(diaryDailyResponse);
    }

    @DeleteMapping("/{date}")
    @Operation(summary = "특정 날짜 일기 삭제", description = "유저의 특정 날짜 일기의 SUMMARY 내용과 일기 본문을 삭제한다.")
    public ResponseEntity deleteDiary(
            @PathVariable("date") LocalDate date,
            Authentication authentication
    ) {
        final Long userId = userService.getLoggedInUserId(authentication);

        diaryService.deleteDiary(userId, date);

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping()
    @Operation(summary = "전체 일기 리스트 조회", description = "일기 리스트(날짜, 제목) 반환한다. 일기 목록을 볼 때 사용한다.")
    public ResponseEntity<List<DiaryDailyResponse>> getDiaryList(
            Authentication authentication
    ) {
        final Long userId = userService.getLoggedInUserId(authentication);

        List<DiaryDailyResponse> diaryListResponse = diaryService.getDiaryList(userId);

        return ResponseEntity.ok().body(diaryListResponse);
    }

    @GetMapping("/summary/{month}")
    @Operation(summary = "해당 달 요약파일 조회", description = "ex) month = \"2024-06.json\"")
    public ResponseEntity<String> getDiaryList(
            @PathVariable("month") String month,
            Authentication authentication
    ) {
        final Long userId = userService.getLoggedInUserId(authentication);

        File file = fileService.createOrOpenFileInPath(userId + File.separator + "summary", month);
        String response = fileService.readStringInFile(file);

        return ResponseEntity.ok().body(response);
    }
}
