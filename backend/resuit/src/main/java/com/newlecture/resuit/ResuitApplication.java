package com.newlecture.resuit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResuitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResuitApplication.class, args);
	}

}
