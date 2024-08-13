package com.ngarden.hida.externalapi.kakaoAuth.service;

import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.externalapi.kakaoAuth.dto.response.AuthLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface KakaoService {
    ResponseEntity<AuthLoginResponse> login(String code) throws IOException;
    ResponseEntity<AuthLoginResponse> login(Authentication authentication);
    ResponseEntity<HttpStatus> logout(Authentication authentication);
    ResponseEntity<UserCreateResponse> getUserCreateResponse(Authentication authentication);
}
