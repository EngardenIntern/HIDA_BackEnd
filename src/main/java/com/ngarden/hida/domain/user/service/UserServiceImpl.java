package com.ngarden.hida.domain.user.service;

import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.global.error.NoExistException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserEntity createUser(UserCreateRequest userCreateRequest) {
        UserEntity userEntity = UserEntity.builder()
                .userName(userCreateRequest.getUserName())
                .email(userCreateRequest.getEmail())
                .build();

        UserEntity user = userRepository.save(userEntity);

        return user;
    }

    @Override
    public List<UserEntity> selectAllUser() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity findById(Long userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if(userEntity.isEmpty()){
            throw new NoExistException("유저가 없습니다");
        }

        return userEntity.get();
    }
}
