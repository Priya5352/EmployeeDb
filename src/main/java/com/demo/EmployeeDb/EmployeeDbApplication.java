package com.demo.EmployeeDb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Defines it as a spring boot Application
@SpringBootApplication
public class EmployeeDbApplication {

	// Main method
	public static void main(String[] args) {
		
		// Bcrypt password generate
		System.out.println(new BCryptPasswordEncoder().encode("5678"));
		SpringApplication.run(EmployeeDbApplication.class, args);
	}

}
