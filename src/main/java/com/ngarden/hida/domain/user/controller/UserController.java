package com.ngarden.hida.domain.user.controller;

import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<UserEntity>> selectAllUser()
    {
        List<UserEntity> userEntityList = userService.selectAllUser();

        return ResponseEntity.ok().body(userEntityList);
    }
}
