package com.ngarden.hida.externalapi.chatGPT.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatGPTConfig {
    @Value("${OPENAI.API-KEY}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

    @Bean
    public HttpHeaders httpHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.set("OpenAI-Beta", "assistants=v2");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
