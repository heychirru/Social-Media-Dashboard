package com.example.dashboard.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.api.InstagramApiService;
import com.example.dashboard.config.InstagramConfig;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiController {

    private final SocialMediaApiService apiService;
    private final InstagramApiService instagramService;
    private final InstagramConfig instagramConfig;

    @Autowired
    public ApiController(SocialMediaApiService apiService,
                         InstagramApiService instagramService,
                         InstagramConfig instagramConfig) {
        this.apiService = apiService;
        this.instagramService = instagramService;
        this.instagramConfig = instagramConfig;
    }

    /**
     * Get aggregated statistics for a user across all platforms
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String username) {
        try {
            // Validate configuration first
            if (!instagramConfig.hasValidAccessToken()) {
                return createConfigurationErrorResponse("Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
            }

            Map<String, Object> stats = apiService.getAggregatedStats(username);

            // Add metadata about the response
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("username", username);
            stats.put("status", "success");
            stats.put("config_status", instagramConfig.getConfigStatus());

            return ResponseEntity.ok(stats);

        } catch (UnsupportedOperationException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PLATFORM", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error getting user stats for " + username + ": " + e.getMessage());
            return createApiErrorResponse(username, e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error getting user stats for " + username + ": " + e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Get data from all platforms for a specific user
     */
    @GetMapping("/platforms/{username}")
    public ResponseEntity<Map<String, Object>> getAllPlatformData(@PathVariable String username) {
        try {
            if (!instagramConfig.hasValidAccessToken()) {
                return createConfigurationErrorResponse("Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
            }

            Map<String, SocialMediaStats> data = apiService.fetchAllPlatformData(username);

            // Wrap response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("platforms", data.keySet());
            response.put("username", username);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            response.put("config_status", instagramConfig.getConfigStatus());

            return ResponseEntity.ok(response);

        } catch (UnsupportedOperationException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PLATFORM", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error getting all platform data for " + username + ": " + e.getMessage());
            return createApiErrorResponse(username, e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error getting all platform data for " + username + ": " + e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Get data from a specific platform
     */
    @GetMapping("/platforms/{platform}/{username}")
    public ResponseEntity<Map<String, Object>> getPlatformData(
            @PathVariable String platform,
            @PathVariable String username) {
        try {
            if (!instagramConfig.hasValidAccessToken()) {
                return createConfigurationErrorResponse("Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
            }

            SocialMediaStats stats = apiService.fetchPlatformData(platform, username);

            // Wrap response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("data", stats);
            response.put("platform", platform);
            response.put("username", username);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            response.put("config_status", instagramConfig.getConfigStatus());

            return ResponseEntity.ok(response);

        } catch (UnsupportedOperationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Platform not supported: " + platform);
            errorResponse.put("supported_platforms", List.of("instagram"));
            errorResponse.put("status", "unsupported_platform");
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            System.err.println("Error getting platform data for " + platform + "/" + username + ": " + e.getMessage());
            return createApiErrorResponse(username, e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error getting platform data for " + platform + "/" + username + ": " + e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Search for posts across platforms
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int maxResults) {
        try {
            if (!instagramConfig.hasValidAccessToken()) {
                return createConfigurationErrorResponse("Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
            }

            if (query == null || query.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Search query cannot be empty.");
            }

            if (maxResults < 1 || maxResults > 50) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "maxResults must be between 1 and 50.");
            }

            Map<String, List<SocialMediaPost>> results = apiService.searchAcrossPlatforms(query, maxResults);

            // Calculate total results
            int totalResults = results.values().stream().mapToInt(List::size).sum();

            // Wrap response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("query", query);
            response.put("maxResults", maxResults);
            response.put("totalResults", totalResults);
            response.put("platforms", results.keySet());
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            response.put("config_status", instagramConfig.getConfigStatus());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("Error searching posts for query '" + query + "': " + e.getMessage());
            return createSearchErrorResponse(query, e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error searching posts for query '" + query + "': " + e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Get recent posts from a specific platform
     */
    @GetMapping("/platforms/{platform}/{username}/posts")
    public ResponseEntity<Map<String, Object>> getRecentPosts(
            @PathVariable String platform,
            @PathVariable String username,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (!instagramConfig.hasValidAccessToken()) {
                return createConfigurationErrorResponse("Instagram API is not configured. Please set INSTAGRAM_ACCESS_TOKEN environment variable.");
            }

            if (limit < 1 || limit > 25) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Limit must be between 1 and 25.");
            }

            SocialMediaStats stats = apiService.fetchPlatformData(platform, username);
            List<SocialMediaPost> posts = stats.getRecentPosts();

            // Apply limit
            if (posts.size() > limit) {
                posts = posts.subList(0, limit);
            }

            // Wrap response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("platform", platform);
            response.put("username", username);
            response.put("limit", limit);
            response.put("totalPosts", posts.size());
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");
            response.put("config_status", instagramConfig.getConfigStatus());

            return ResponseEntity.ok(response);

        } catch (UnsupportedOperationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Platform not supported: " + platform);
            errorResponse.put("supported_platforms", List.of("instagram"));
            errorResponse.put("status", "unsupported_platform");
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            System.err.println("Error getting recent posts for " + platform + "/" + username + ": " + e.getMessage());
            return createApiErrorResponse(username, e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error getting recent posts for " + platform + "/" + username + ": " + e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Enhanced health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Social Media Dashboard API");
        health.put("version", "1.0.0");

        boolean isConfigured = instagramConfig.hasValidAccessToken();
        String configStatus = instagramConfig.getConfigStatus();

        health.put("status", isConfigured ? "UP" : "CONFIGURATION_REQUIRED");
        health.put("config_status", configStatus);
        health.put("instagram_configured", isConfigured);

        if (isConfigured) {
            health.put("instagram_service", "READY");
            health.put("message", "All services are operational");
        } else {
            health.put("instagram_service", "NOT_CONFIGURED");
            health.put("message", "Instagram API configuration required");
            health.put("required_action", "Set INSTAGRAM_ACCESS_TOKEN environment variable");
        }

        HttpStatus status = isConfigured ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    /**
     * Enhanced API configuration status
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Basic config info
        config.put("apiVersion", "v1");
        config.put("service", "Social Media Dashboard");
        config.put("supportedPlatforms", List.of("instagram"));

        // Instagram configuration status
        boolean isConfigured = instagramConfig.hasValidAccessToken();
        config.put("instagram", Map.of(
                "configured", isConfigured,
                "status", instagramConfig.getConfigStatus(),
                "apiUrl", instagramConfig.getUrl(),
                "hasAccessToken", instagramConfig.getAccessToken() != null && !instagramConfig.getAccessToken().isEmpty()
        ));

        // Available features
        config.put("features", Map.of(
                "profileStats", isConfigured,
                "recentPosts", isConfigured,
                "searchPosts", isConfigured,
                "errorHandling", true,
                "rateLimiting", true
        ));

        // API endpoints
        config.put("endpoints", Map.of(
                "health", "/api/v1/health",
                "config", "/api/v1/config",
                "userStats", "/api/v1/stats/{username}",
                "platformData", "/api/v1/platforms/{platform}/{username}",
                "allPlatforms", "/api/v1/platforms/{username}",
                "recentPosts", "/api/v1/platforms/{platform}/{username}/posts",
                "searchPosts", "/api/v1/search?query={query}&maxResults={limit}",
                "dashboard", "/dashboard?username={username}"
        ));

        config.put("timestamp", System.currentTimeMillis());
        config.put("status", "active");

        return ResponseEntity.ok(config);
    }

    /**
     * Configuration validation endpoint
     */
    @GetMapping("/config/validate")
    public ResponseEntity<Map<String, Object>> validateConfig() {
        Map<String, Object> validation = new HashMap<>();

        try {
            instagramConfig.validateConfiguration();

            // If validation passes
            validation.put("valid", true);
            validation.put("status", "CONFIGURED");
            validation.put("message", "Instagram API is properly configured and ready to use");
            validation.put("timestamp", System.currentTimeMillis());
            validation.put("config_details", instagramConfig.getConfigurationInfo());

            return ResponseEntity.ok(validation);

        } catch (RuntimeException e) {
            // If validation fails
            validation.put("valid", false);
            validation.put("status", "CONFIGURATION_ERROR");
            validation.put("message", e.getMessage());
            validation.put("timestamp", System.currentTimeMillis());
            validation.put("required_actions", List.of(
                    "Set up Instagram Basic Display API at https://developers.facebook.com/",
                    "Generate a valid access token with required permissions",
                    "Set INSTAGRAM_ACCESS_TOKEN environment variable",
                    "Restart the application"
            ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }
    }

    // Helper methods for error responses
    private ResponseEntity<Map<String, Object>> createConfigurationErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", "configuration_error");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("required_actions", List.of(
                "Set INSTAGRAM_ACCESS_TOKEN environment variable",
                "Ensure the token has required permissions",
                "Restart the application"
        ));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> createApiErrorResponse(String username, String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        errorResponse.put("username", username);
        errorResponse.put("status", "api_error");
        errorResponse.put("timestamp", System.currentTimeMillis());

        // Categorize error for better client handling
        if (errorMessage.contains("access denied") || errorMessage.contains("OAuthException")) {
            errorResponse.put("error_category", "AUTHENTICATION");
            errorResponse.put("suggestions", List.of(
                    "Check if your access token is valid and not expired",
                    "Verify app permissions",
                    "Regenerate access token if needed"
            ));
        } else if (errorMessage.contains("rate limit")) {
            errorResponse.put("error_category", "RATE_LIMIT");
            errorResponse.put("suggestions", List.of(
                    "Wait for rate limit to reset",
                    "Reduce API call frequency"
            ));
        } else if (errorMessage.contains("not found")) {
            errorResponse.put("error_category", "NOT_FOUND");
            errorResponse.put("suggestions", List.of(
                    "Verify the username is correct",
                    "Check if the account exists and is public"
            ));
        } else {
            errorResponse.put("error_category", "GENERAL");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> createSearchErrorResponse(String query, String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        errorResponse.put("query", query);
        errorResponse.put("status", "search_error");
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String errorType, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", errorType.toLowerCase());
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("config_status", instagramConfig.getConfigStatus());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Global exception handler for API endpoints
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception e) {
        System.err.println("Unhandled API exception: " + e.getMessage());
        e.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal server error: " + e.getMessage());
        errorResponse.put("status", "internal_server_error");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("type", "unexpected_error");
        errorResponse.put("config_status", instagramConfig.getConfigStatus());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}