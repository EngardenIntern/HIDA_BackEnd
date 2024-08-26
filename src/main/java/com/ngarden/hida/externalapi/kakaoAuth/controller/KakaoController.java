package com.ngarden.hida.externalapi.kakaoAuth.controller;

import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.externalapi.kakaoAuth.dto.response.AuthLoginResponse;
import com.ngarden.hida.externalapi.kakaoAuth.service.KakaoService;
import com.ngarden.hida.global.error.NoExistException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.0.161:3000"}, allowCredentials = "true")
@RequestMapping("/api/v1/kakao")
public class KakaoController {
    private final KakaoService kakaoService;

    @GetMapping("/login")
    @Operation(summary = "로그인..")
    public ResponseEntity<AuthLoginResponse> login(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        AuthLoginResponse loginResponse = kakaoService.login(code);
        return kakaoService.makeCookieResponse(loginResponse, response);
    }

    //쿠키에 RefreshToken
    @PatchMapping("/login")
    @Operation(summary = "Authorization 헤더에 RefreshToken")
    public ResponseEntity<AuthLoginResponse> login(HttpServletRequest request) {
        Cookie refreshToken = kakaoService.getCookie(request.getCookies(), "refreshToken");
        return kakaoService.refresh(refreshToken.getValue());
    }

    //서버에 남은 RefreshToken 삭제
    @Operation(summary = "서버에 남은 RefreshToken 삭제")
    @DeleteMapping("/login")
    public ResponseEntity<HttpStatus> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        return kakaoService.logout(request, response, authentication);
    }

    //현재 로그인한 유저정보 조회
    @Operation(summary = "현재 로그인한 유저정보 조회")
    @GetMapping("/userInfo")
    public ResponseEntity<UserCreateResponse> getUserInfo(Authentication authentication) throws IOException {
        return kakaoService.getUserCreateResponse(authentication);
    }
}
