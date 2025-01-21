package com.one.social_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaAuditing
@EnableMongoRepositories(basePackages = "com.one.social_project.domain.chat.repository.mongo")
@SpringBootApplication
public class SocialProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialProjectApplication.class, args);
	}

}
