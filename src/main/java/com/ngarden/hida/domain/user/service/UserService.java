package com.ngarden.hida.domain.user.service;

import com.ngarden.hida.domain.diary.entity.EmotionTypeEnum;
import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity createUser(UserCreateRequest userCreateRequest);

    List<UserEntity> selectAllUser();

    UserEntity findById(Long userId);

    void updateCounts(Long userId, List<EmotionTypeEnum> emotionTypeEnumList, int amount);
}
