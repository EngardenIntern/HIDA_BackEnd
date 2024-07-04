package com.ngarden.hida.domain.user.controller;

import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.domain.user.dto.response.UserResponse;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
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
    public ResponseEntity<List<UserResponse>> selectAllUser()
    {
        List<UserEntity> userEntityList = userService.selectAllUser();
        List<UserResponse> userResponseList = new ArrayList<>();

        for(UserEntity userEntity : userEntityList){
            UserResponse userResponse = UserResponse.builder()
                    .userName(userEntity.getUserName())
                    .userStatus(userEntity.getUserStatus())
                    .build();
            userResponseList.add(userResponse);
        }

        return ResponseEntity.ok().body(userResponseList);
    }
}
