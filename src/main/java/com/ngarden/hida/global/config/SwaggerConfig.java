package com.ngarden.hida.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api(){
        Info info = new Info()
                .title("엔가든 HIDA 프로젝트 웹 BE Server")
                .version("v0.0.1")
                .description("엔가든 HIDA 프로젝트 웹 BE Server 입니다.");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}