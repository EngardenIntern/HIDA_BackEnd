package com.ngarden.hida.externalapi.kakaoAuth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoAuthCodeRequest {
    private String clientId;
    private String redirectUri;
}
