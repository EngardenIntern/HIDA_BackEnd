package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;

public interface DiaryService {
    DiaryEntity createDiary(DiaryCreateRequest diaryCreateRequest);
}
