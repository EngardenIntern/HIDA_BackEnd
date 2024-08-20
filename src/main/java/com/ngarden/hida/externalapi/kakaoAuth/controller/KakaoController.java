package com.ngarden.hida.externalapi.kakaoAuth.controller;

import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.externalapi.kakaoAuth.dto.response.AuthLoginResponse;
import com.ngarden.hida.externalapi.kakaoAuth.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/kakao")
@CrossOrigin(origins = "*")
public class KakaoController {
    private final KakaoService kakaoService;

    @GetMapping("/login")
    @Operation(summary = "로그인..")
    public ResponseEntity<AuthLoginResponse> login(@RequestParam("code") String code) throws IOException {
        return kakaoService.login(code);
    }

    //Authorization 헤더에 RefreshToken
    @PatchMapping("/login")
    @Operation(summary = "Authorization 헤더에 RefreshToken")
    public ResponseEntity<AuthLoginResponse> login(Authentication authentication){
        return kakaoService.login(authentication);
    }

    //서버에 남은 RefreshToken 삭제
    @Operation(summary = "서버에 남은 RefreshToken 삭제")
    @DeleteMapping("/login")
    public ResponseEntity<HttpStatus> logout(Authentication authentication){
        return kakaoService.logout(authentication);
    }

    //현재 로그인한 유저정보 조회
    @Operation(summary = "현재 로그인한 유저정보 조회")
    @GetMapping("/userInfo")
    public ResponseEntity<UserCreateResponse> getUserInfo(Authentication authentication) throws IOException {
        return kakaoService.getUserCreateResponse(authentication);
    }
}
