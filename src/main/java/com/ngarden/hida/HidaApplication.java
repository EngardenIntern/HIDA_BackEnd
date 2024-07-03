package com.ngarden.hida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HidaApplication {

	public static void main(String[] args) {
		SpringApplication.run(HidaApplication.class, args);
	}

}
