package com.ngarden.hida.domain.diary.dto.request;

import com.ngarden.hida.domain.user.entity.UserEntity;
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
