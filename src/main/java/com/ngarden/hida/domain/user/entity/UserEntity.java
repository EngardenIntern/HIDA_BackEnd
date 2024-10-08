package com.ngarden.hida.domain.user.entity;

import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.global.basic.BasicEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USERS")
public class UserEntity extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(name = "outh_id")
    private Long outhId;

    @Column(name = "user_status")
    private Boolean userStatus = Boolean.FALSE;

    @Column(name = "diary_count")
    private Long diaryCount;

    @Column(name = "joy_count")
    private Long joyCount;

    @Column(name = "sadness_count")
    private Long sadnessCount;

    @Column(name = "anger_count")
    private Long angerCount;

    @Column(name = "fear_count")
    private Long fearCount;

    @Column(name = "refresh_token")
    private String refreshToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<DiaryEntity> diaryEntityList;
}
