package com.example.dashboard.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();

        // Try to load from file first
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Config file not found, using environment variables or defaults");
        }

        // Override with environment variables if they exist
        String instagramToken = System.getenv("INSTAGRAM_ACCESS_TOKEN");
        if (instagramToken != null) {
            properties.setProperty("instagram.access.token", instagramToken);
        }
    }

    public static String getInstagramAccessToken() {
        return properties.getProperty("instagram.access.token", "demo_token");
    }

    public static boolean isDemoMode() {
        String instagramToken = getInstagramAccessToken();
        return "demo_token".equals(instagramToken);
    }

    public static void reloadProperties() {
        loadProperties();
    }
}