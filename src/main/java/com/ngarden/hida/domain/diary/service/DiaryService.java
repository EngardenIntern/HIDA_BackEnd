package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.domain.diary.dto.response.DiaryListResponse;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import org.json.JSONObject;

import java.time.LocalDate;

public interface DiaryService {
    DiaryEntity saveDiary(DiarySaveDTO diarySaveDTO);
    DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date);
    DiaryListResponse getDiaryList(Long userId);
    MessageResponse createDiaryByEmotionGpt(String diaryDetail, JSONObject diarySummary, String inputAssistantId);
    MessageResponse createDiaryByGpt(String prompt, String inputAssistantId);
    String createJsonByDiaryRequest(DiarySaveDTO diarySaveDTO);
    void deleteDiary(Long userId, LocalDate date);
}
