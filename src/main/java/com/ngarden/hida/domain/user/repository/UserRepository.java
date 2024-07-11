package com.ngarden.hida.domain.user.repository;

import com.ngarden.hida.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByOuthId(Long aLong);
}
