package com.ngarden.hida.domain.diary.service;

import com.ngarden.hida.domain.diary.dto.request.DiaryCreateRequest;
import com.ngarden.hida.domain.diary.entity.DiaryEntity;
import com.ngarden.hida.domain.diary.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService{
    private final DiaryRepository diaryRepository;

    @Override
    public DiaryEntity createDiary(DiaryCreateRequest diaryCreateRequest) {

        DiaryEntity diaryEntity = DiaryEntity.builder()
                .title(diaryCreateRequest.getTitle())
                .detail(diaryCreateRequest.getDetail())
                .build();
        return diaryRepository.save(diaryEntity);
    }
}
