package com.brickerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrickErpBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BrickErpBackendApplication.class, args);
	}
}