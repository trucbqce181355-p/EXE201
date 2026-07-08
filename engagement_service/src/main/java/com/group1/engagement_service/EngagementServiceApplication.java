package com.group1.engagement_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
@EnableFeignClients
public class EngagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EngagementServiceApplication.class, args);
	}

}
