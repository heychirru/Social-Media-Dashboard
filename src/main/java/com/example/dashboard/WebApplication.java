package com.example.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApplication {
    public static void main(String[] args) {
        System.out.println("Starting Social Media Dashboard Web Application Run On http://localhost:8080 ...");
        SpringApplication.run(WebApplication.class, args);
    }
}
