package com.ngarden.hida.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String userName;
    private Boolean userStatus;
}