package com.ngarden.hida.domain.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryListResponse {
    private List<DiaryDailyResponse> diaryDailyResponseList;
    private String userName;
}
