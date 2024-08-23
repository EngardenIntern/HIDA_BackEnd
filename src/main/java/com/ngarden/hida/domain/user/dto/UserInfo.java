package com.ngarden.hida.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserInfo {
    private Long userId;

    private String userName;

    private String email;

    private Long outhId;
}
