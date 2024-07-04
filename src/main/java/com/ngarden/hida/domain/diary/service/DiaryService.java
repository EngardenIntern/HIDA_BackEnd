package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.user.entity.UserEntity;

import java.time.LocalDate;

public interface DiaryService {
    DiaryEntity createDiary(DiaryCreateRequest diaryCreateRequest);
    DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date);
    DiaryListResponse getDiaryList(Long userId);
}
