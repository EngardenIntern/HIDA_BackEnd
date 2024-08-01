package com.ngarden.hida.domain.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;

import java.time.LocalDate;

public interface DiaryService {
    DiaryEntity saveDiary(DiaryCreateRequest diaryCreateRequest);
    DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date);
    DiaryListResponse getDiaryList(Long userId);
    MessageResponse createDiaryByEmotionGpt(String diaryDetail, JsonNode diarySummary, String inputAssistantId);
    MessageResponse createDiaryByGpt(String prompt, String inputAssistantId);
    String createJsonByDiaryRequest(DiaryCreateRequest diaryCreateRequest);
}
