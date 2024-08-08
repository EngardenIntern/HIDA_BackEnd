package com.ngarden.hida.global.time;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1")
public class TimeController {
    @GetMapping("time")
    @Operation(summary = "서버 시간 조회")
    public ResponseEntity<LocalDateTime> getServerTime(){return ResponseEntity.ok().body(LocalDateTime.now());}
}
