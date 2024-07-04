package com.ngarden.hida.domain.diary.repository;

import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {
    DiaryEntity findByUserAndDiaryDate(UserEntity userId, LocalDate date);

    List<DiaryEntity> findByUser(UserEntity userId);
}
