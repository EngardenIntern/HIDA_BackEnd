package com.ngarden.hida.domain.user.service;

import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity createUser(UserCreateRequest userCreateRequest);

    List<UserEntity> selectAllUser();
}
