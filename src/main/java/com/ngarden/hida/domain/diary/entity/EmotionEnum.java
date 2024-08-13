package com.ngarden.hida.domain.diary.entity;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum EmotionEnum {
    JOY("기쁜"),
    SADNESS("슬픈"),
    ANGER("화나는"),
    FEAR("두려운"),
    NEUTRAL("평범한");

    private final String emotionKorean;

    EmotionEnum(String emotionKorean) {
        this.emotionKorean = emotionKorean;
    }

    private String getEmotionKorean() {
        return emotionKorean;
    }

    public static EmotionEnum getEmotionEnumByKorean(String korean) {
        for (EmotionEnum emotion : EmotionEnum.values()) {
            if (Objects.equals(emotion.getEmotionKorean(), korean)) {
                return emotion;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + korean);
    }
}
