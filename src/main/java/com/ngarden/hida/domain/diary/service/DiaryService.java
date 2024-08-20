package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiarySaveDTO;
import com.ngarden.hida.domain.diary.dto.response.DiaryDailyResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;

import java.time.LocalDate;
import java.util.List;

public interface DiaryService {
    DiaryDailyResponse saveDiary(DiarySaveDTO diarySaveDTO);
    DiaryDailyResponse getDiaryDaily(Long userId, LocalDate date);
    List<DiaryDailyResponse> getDiaryList(Long userId);
    MessageResponse createDiaryByGpt(String prompt, String inputAssistantId);
    String createJsonByDiaryDTO(DiarySaveDTO diarySaveDTO);
    void saveDiaryToFile(DiarySaveDTO diarySaveDTO);
    void deleteDiary(Long userId, LocalDate date);
    String parseJson(String message);
}