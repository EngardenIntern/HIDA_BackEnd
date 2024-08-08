package com.ngarden.hida.domain.diary.dto.request;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiarySaveDTO {
    private Long userId;
    private String title;
    private String detail;
    private String mom;
    private String summary;
    private String emotions;
    private Boolean aiStatus;
    private LocalDate DiaryDate;
}
