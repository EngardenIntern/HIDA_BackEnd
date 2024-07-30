package com.ngarden.hida.domain.diary.entity;

import com.ngarden.hida.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DIARY")
public class DiaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    @Column(name = "diary_date")
    private LocalDate diaryDate;

    @Column(name = "title")
    private String title;

    @Column(name = "detail")
    private String detail;

    @Column(name = "AI_status")
    private Boolean aiStatus = Boolean.FALSE;

    @Column(name = "summary")
    private String summary;

    @Column(name = "mom")
    private String mom;

    @Column(name = "emotions")
    private String emotions;

    @Column(name = "diary_path")
    private  String diaryPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserEntity user;
}
