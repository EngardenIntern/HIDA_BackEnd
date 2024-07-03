package com.ngarden.hida.global.error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorMessage> handleException(CustomException e){
        ErrorMessage message = ErrorMessage.builder()
                .status(e.getErrorMessage().getStatus())
                .message(e.getErrorMessage().getMessage())
                .error(e.getErrorMessage().getError())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(e.getErrorMessage().getStatus()).body(message);
    }

}
