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

    /**
     *  directoryName : 1_최정식
     *                  2_이동재
     *  defaultFilePath\\directoryName
     */
    @Column(name = "directory_name")
    private String directoryName;

    private String refreshToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<DiaryEntity> diaryEntityList;
}
