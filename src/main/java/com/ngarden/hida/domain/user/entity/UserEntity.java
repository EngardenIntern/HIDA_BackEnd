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

    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
    private List<DiaryEntity> diaryEntityList;
}
