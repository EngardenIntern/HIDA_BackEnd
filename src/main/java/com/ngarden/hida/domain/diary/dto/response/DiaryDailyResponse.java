package com.ngarden.hida.domain.diary.dto.response;

import com.ngarden.hida.domain.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryDailyResponse {
    private LocalDate date;
    private Boolean aiStatus;
    private String userName;
    private String title;
    private String detail;
    private String emotions;
    private String mom;
}
