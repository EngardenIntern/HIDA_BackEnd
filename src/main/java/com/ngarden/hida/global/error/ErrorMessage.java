package com.ngarden.hida.global.error;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorMessage {
    private int status;
    private String message;
    private final LocalDateTime timeStamp;
}
