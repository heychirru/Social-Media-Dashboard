package com.example.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.dashboard.desktop.DesktopApplication;

public class App {

    public static void main(String[] args) {
        if (args.length > 0) {
            String mode = args[0].toLowerCase();

            switch (mode) {
                case "web":
                    startWebApplication();
                    break;
                case "desktop":
                    startDesktopApplication();
                    break;
                case "api":
                    startApiServer();
                    break;
                default:
                    System.out.println("Usage: java -jar app.jar [web|desktop|api]");
                    System.out.println("  web     - Start web application");
                    System.out.println("  desktop - Start desktop application");
                    System.out.println("  api     - Start REST API server");
                    break;
            }
        } else {
            // Default to web application
            startWebApplication();
        }
    }

    private static void startWebApplication() {
        System.out.println("Starting Social Media Dashboard Web Application...");
        SpringApplication.run(WebApplication.class, new String[0]); // Pass empty args
    }

    private static void startDesktopApplication() {
        System.out.println("Starting Social Media Dashboard Desktop Application...");
        DesktopApplication.main(new String[0]);
    }

    private static void startApiServer() {
        System.out.println("Starting Social Media Dashboard API Server...");
        SpringApplication.run(ApiServerApplication.class, new String[0]); // Pass empty args
    }

    @SpringBootApplication
    public static class WebApplication {
        public static void main(String[] args) {
            SpringApplication.run(WebApplication.class, args);
        }
    }

    @SpringBootApplication
    public static class ApiServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(ApiServerApplication.class, args);
        }
    }
}