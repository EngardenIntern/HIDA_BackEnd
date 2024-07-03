package com.ngarden.hida.domain.diary.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryCreateRequest {
    private Long userId;
    private String title;
    private String detail;
}
