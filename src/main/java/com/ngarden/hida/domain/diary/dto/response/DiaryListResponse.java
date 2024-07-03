package com.ngarden.hida.domain.diary.dto.response;

import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryListResponse {
    private List<DiaryEntity> diaryEntityList;
    private UserEntity userEntity;
}
