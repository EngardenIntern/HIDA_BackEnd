package com.ngarden.hida.domain.user.controller;

import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.domain.user.dto.response.UserResponse;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping()
    @Operation(summary = "[테스트용]유저 생성", description = "테스트용 유저를 생성해준다. 서비스에서는 카카오 소셜로그인을 사용한다.")
    public ResponseEntity<UserCreateResponse> createUser(
            @RequestBody UserCreateRequest userCreateRequest
            ){
        UserEntity userEntity = userService.createUser(userCreateRequest);

        UserCreateResponse userCreateResponse = UserCreateResponse.builder()
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .userName(userEntity.getUserName())
                .build();
        return ResponseEntity.ok().body(userCreateResponse);
    }

    @GetMapping()
    @Operation(summary = "전체 유저 리스트 반환", description = "전체 유저 리스트(유저 ID, 유저 이름, 유저 Email)를 반환해준다.")
    public ResponseEntity<List<UserResponse>> selectAllUser()
    {
        List<UserEntity> userEntityList = userService.selectAllUser();
        List<UserResponse> userResponseList = new ArrayList<>();

        for(UserEntity userEntity : userEntityList){
            UserResponse userResponse = UserResponse.builder()
                    .userId(userEntity.getUserId())
                    .userName(userEntity.getUserName())
                    .userStatus(userEntity.getUserStatus())
                    .build();
            userResponseList.add(userResponse);
        }

        return ResponseEntity.ok().body(userResponseList);
    }
}
