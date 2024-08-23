package com.ngarden.hida.domain.user.service;

import com.ngarden.hida.domain.diary.entity.EmotionEnum;
import com.ngarden.hida.domain.user.dto.UserInfo;
import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.dto.response.UserResponse;
import com.ngarden.hida.domain.user.entity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {
    UserEntity createUser(UserCreateRequest userCreateRequest);

    List<UserEntity> selectAllUser();

    UserEntity findById(Long userId);

    void updateCounts(Long userId, List<EmotionEnum> emotionEnumList, int amount);

    Long getLoggedInUserId(Authentication authentication);

    ResponseEntity<UserInfo> getUserInfo(Authentication authentication);
}
