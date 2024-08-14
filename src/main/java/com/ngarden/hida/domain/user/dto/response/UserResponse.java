package com.ngarden.hida.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String userName;
    private Boolean userStatus;
    private Long diaryCount;
    private Long joyCount;
    private Long sadnessCount;
    private Long angerCount;
    private Long fearCount;
}
