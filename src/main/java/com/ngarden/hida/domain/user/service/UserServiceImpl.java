package com.ngarden.hida.domain.user.service;

import com.ngarden.hida.domain.diary.entity.EmotionEnum;
import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.global.error.NoExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .diaryCount(0L)
                .joyCount(0L)
                .angerCount(0L)
                .sadnessCount(0L)
                .fearCount(0L)
                .refreshToken(userCreateRequest.getRefreshToken())
                .build();

        return userRepository.save(userEntity);
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

    /**
     * DB의 count(diaryCount, joyCount, angerCount, fearCount, sadnessCount)값을 amount만큼 증감한다. diaryCount와 감정들의 Count는 amount로 같이 증감된다.
     * @param userId
     * @param emotions 증감할 서로다른 감정 Enum을 리스트로 준다. length 보통 2개, 아니면 1개가 들어와야한다.
     * @param amount 증감할 양을 준다. 보통 1 or -1일 것이다.
     */
    @Override
    public void updateCounts(Long userId, List<EmotionEnum> emotions, int amount) {
        UserEntity user = findById(userId);
        user.setDiaryCount(user.getDiaryCount() + amount);

        for (EmotionEnum emotion : emotions) {
            switch (emotion) {
                case JOY -> user.setJoyCount(user.getJoyCount() + amount);
                case ANGER -> user.setAngerCount(user.getAngerCount() + amount);
                case FEAR -> user.setFearCount(user.getFearCount() + amount);
                case SADNESS -> user.setSadnessCount(user.getSadnessCount() + amount);
                default -> throw new IllegalArgumentException("Unknown emotion: " + emotion);
            }
        }
        userRepository.save(user);
    }
}
