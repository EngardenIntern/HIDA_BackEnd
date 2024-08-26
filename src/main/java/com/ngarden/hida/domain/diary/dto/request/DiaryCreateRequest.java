package com.ngarden.hida.domain.diary.dto.request;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryCreateRequest {
    private LocalDate DiaryDate;
    private String title;
    private String detail;
}
