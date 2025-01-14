package com.one.social_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SocialProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialProjectApplication.class, args);
	}

}
