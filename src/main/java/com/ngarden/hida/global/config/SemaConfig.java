package com.ngarden.hida.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class SemaConfig {
    @Bean
    public Semaphore semaphore(){
        return new Semaphore(1);
    }
}
