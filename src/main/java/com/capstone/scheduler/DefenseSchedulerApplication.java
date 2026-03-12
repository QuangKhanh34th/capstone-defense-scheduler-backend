package com.capstone.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DefenseSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DefenseSchedulerApplication.class, args);

		System.out.println("HELLO BRO NHA");
	}

}
