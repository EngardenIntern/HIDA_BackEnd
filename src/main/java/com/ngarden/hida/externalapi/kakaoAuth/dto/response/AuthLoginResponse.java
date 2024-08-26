package com.ngarden.hida.externalapi.kakaoAuth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginResponse {
    private String accessToken;

    //Patch 메서드에서는 입력한 RefreshToken 그대로 반환
    private String refreshToken;
}
