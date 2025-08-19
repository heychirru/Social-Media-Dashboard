package com.example.dashboard.controller;

import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class WebController {

    @Autowired
    private SocialMediaApiService apiService;

    /**
     * Root path - HTML landing page
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Social Media Dashboard");
        model.addAttribute("message", "Welcome to Social Media Dashboard");

        // Add configuration status to help with setup
        boolean isConfigured = apiService.isInstagramConfigured();
        String configStatus = apiService.getInstagramStatus();

        model.addAttribute("isConfigured", isConfigured);
        model.addAttribute("configStatus", configStatus);

        if (!isConfigured) {
            model.addAttribute("setupMessage", "Please configure your Instagram API credentials to get started.");
            model.addAttribute("setupInstructions", Map.of(
                    "step1", "Get Instagram API credentials from Facebook Developers Console",
                    "step2", "Set INSTAGRAM_ACCESS_TOKEN environment variable",
                    "step3", "Restart the application",
                    "step4", "Test with /dashboard?username=YOUR_INSTAGRAM_USERNAME"
            ));
        }

        return "index";
    }

    /**
     * API info as JSON
     */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "Social Media Dashboard API");
        info.put("status", "running");
        info.put("version", "1.0.0");
        info.put("configured", apiService.isInstagramConfigured());
        info.put("config_status", apiService.getInstagramStatus());

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/api/v1/health");
        endpoints.put("config", "/api/v1/config");
        endpoints.put("dashboard", "/dashboard?username=YOUR_USERNAME");
        endpoints.put("stats", "/api/v1/stats/YOUR_USERNAME");
        endpoints.put("platforms", "/api/v1/platforms/YOUR_USERNAME");

        info.put("endpoints", endpoints);

        if (!apiService.isInstagramConfigured()) {
            info.put("warning", "Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
        }

        return info;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam String username, Model model) {
        System.out.println(">>> Received request for dashboard");
        System.out.println(">>> Username parameter: [" + username + "]");

        try {
            // Check if service is configured
            if (!apiService.isInstagramConfigured()) {
                model.addAttribute("error", "Instagram API is not configured. Please set up your Instagram API credentials.");
                model.addAttribute("username", username);
                model.addAttribute("hasError", true);
                model.addAttribute("errorType", "CONFIGURATION_ERROR");
                model.addAttribute("configInstructions", Map.of(
                        "step1", "Visit https://developers.facebook.com/",
                        "step2", "Create an app and add Instagram Basic Display product",
                        "step3", "Generate an access token",
                        "step4", "Set INSTAGRAM_ACCESS_TOKEN environment variable",
                        "step5", "Restart the application"
                ));
                return "error";
            }

            // Fetch Instagram profile data
            SocialMediaStats profile = apiService.fetchInstagramProfile(username);
            System.out.println(">>> API returned profile: " + profile);

            // Fetch Instagram posts
            List<SocialMediaPost> posts = apiService.fetchInstagramPosts(username);
            System.out.println(">>> API returned posts count: " + (posts != null ? posts.size() : 0));

            // Create platformData map that the template expects
            Map<String, SocialMediaStats> platformData = new HashMap<>();
            platformData.put("Instagram", profile);

            // Add all the required attributes to the model
            model.addAttribute("aggregatedStats", profile);
            model.addAttribute("platformData", platformData);
            model.addAttribute("posts", posts);
            model.addAttribute("username", username);
            model.addAttribute("totalPlatforms", 1);
            model.addAttribute("hasError", false);

            return "dashboard";

        } catch (UnsupportedOperationException e) {
            System.err.println(">>> Platform not supported: " + e.getMessage());
            model.addAttribute("error", "Platform not supported: " + e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("hasError", true);
            model.addAttribute("errorType", "UNSUPPORTED_PLATFORM");
            return "error";

        } catch (RuntimeException e) {
            System.err.println(">>> Error loading dashboard for username: " + username);
            System.err.println(">>> Error details: " + e.getMessage());

            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("hasError", true);
            model.addAttribute("errorType", "API_ERROR");

            // Provide specific error guidance based on error message
            if (e.getMessage().contains("access denied") || e.getMessage().contains("OAuthException")) {
                model.addAttribute("errorCategory", "AUTHENTICATION");
                model.addAttribute("suggestions", List.of(
                        "Check if your Instagram access token is valid and not expired",
                        "Verify that your app has the required permissions (user_profile, user_media)",
                        "Regenerate your access token from Facebook Developers Console",
                        "Ensure your Instagram account is connected to the Facebook app"
                ));
            } else if (e.getMessage().contains("rate limit")) {
                model.addAttribute("errorCategory", "RATE_LIMIT");
                model.addAttribute("suggestions", List.of(
                        "You have exceeded Instagram's API rate limits",
                        "Wait for the rate limit to reset (typically 1 hour)",
                        "Reduce the frequency of API calls",
                        "Consider implementing caching to reduce API requests"
                ));
            } else if (e.getMessage().contains("not found")) {
                model.addAttribute("errorCategory", "USER_NOT_FOUND");
                model.addAttribute("suggestions", List.of(
                        "Verify the Instagram username is correct",
                        "Check if the Instagram account exists and is public",
                        "Try using the Instagram user ID instead of username",
                        "Ensure the account is not private or restricted"
                ));
            } else if (e.getMessage().contains("network") || e.getMessage().contains("connectivity")) {
                model.addAttribute("errorCategory", "NETWORK");
                model.addAttribute("suggestions", List.of(
                        "Check your internet connection",
                        "Verify that Instagram API endpoints are accessible",
                        "Check for any firewall or proxy issues",
                        "Try again in a few minutes"
                ));
            } else {
                model.addAttribute("errorCategory", "GENERAL");
                model.addAttribute("suggestions", List.of(
                        "Check the application logs for detailed error information",
                        "Verify your Instagram API configuration",
                        "Try with a different Instagram username",
                        "Contact support if the issue persists"
                ));
            }

            return "error";
        }
    }

    /**
     * Health check endpoint for dashboard
     */
    @GetMapping("/dashboard/health")
    @ResponseBody
    public Map<String, Object> dashboardHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "dashboard");

        try {
            boolean isConfigured = apiService.isInstagramConfigured();
            String configStatus = apiService.getInstagramStatus();

            health.put("status", isConfigured ? "UP" : "CONFIGURATION_REQUIRED");
            health.put("instagram_configured", isConfigured);
            health.put("config_status", configStatus);

            if (isConfigured) {
                // Test with a basic API call to verify connectivity
                // Note: This would require a valid test user, so we'll just check configuration
                health.put("instagram_service", "READY");
                health.put("message", "Instagram API is configured and ready");
            } else {
                health.put("instagram_service", "NOT_CONFIGURED");
                health.put("message", "Instagram API configuration required");
                health.put("required_action", "Set INSTAGRAM_ACCESS_TOKEN environment variable");
            }

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("instagram_service", "ERROR");
            health.put("error", e.getMessage());
            health.put("message", "Service health check failed");
        }

        return health;
    }
}