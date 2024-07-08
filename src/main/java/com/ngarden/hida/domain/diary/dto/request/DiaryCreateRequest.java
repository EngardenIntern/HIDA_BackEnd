package com.ngarden.hida.domain.diary.dto.request;

import com.ngarden.hida.domain.user.entity.UserEntity;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryCreateRequest {
    private Long userId;
    private String title;
    private String detail;
    private String comment;
    private String summary;
    private Boolean aiStatus;
    private LocalDate DiaryDate;
}
