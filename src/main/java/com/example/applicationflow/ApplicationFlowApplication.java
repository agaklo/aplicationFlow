package com.example.applicationflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.applicationflow"})
public class ApplicationFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationFlowApplication.class, args);
	}

}
