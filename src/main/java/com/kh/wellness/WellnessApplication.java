package com.kh.wellness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WellnessApplication {

	public static void main(String[] args) {
		SpringApplication.run(WellnessApplication.class, args);
	}

}
