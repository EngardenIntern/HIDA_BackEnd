package com.ngarden.hida.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = NoExistException.class)
    public ResponseEntity<ErrorMessage> handleException(NoExistException e){
        ErrorMessage message = ErrorMessage.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(value = AlreadyExistException.class)
    public ResponseEntity<ErrorMessage> handleException(AlreadyExistException e){
        ErrorMessage message = ErrorMessage.builder()
                .status(HttpStatus.NOT_ACCEPTABLE.value())
                .message(e.getMessage())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
}
