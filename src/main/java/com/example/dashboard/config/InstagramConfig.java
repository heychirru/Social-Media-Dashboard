package com.example.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "instagram.api")
@Component
public class InstagramConfig {

    private String url = "https://graph.instagram.com/v21.0";
    private String appId;
    private String appSecret;
    private String accessToken;
    private String clientId;
    private String clientSecret;
    private String redirectUri = "http://localhost:8080/auth/instagram/callback";

    // Rate limiting
    private int requestsPerHour = 200;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 1000;

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public int getRequestsPerHour() { return requestsPerHour; }
    public void setRequestsPerHour(int requestsPerHour) { this.requestsPerHour = requestsPerHour; }

    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }

    // Validation methods
    public boolean hasValidAccessToken() {
        return accessToken != null &&
                !accessToken.trim().isEmpty() &&
                !accessToken.equals("demo-token") &&
                !accessToken.startsWith("$") &&
                !accessToken.contains("your_access_token_here") &&
                accessToken.length() > 20; // Basic length validation
    }

    public String getConfigStatus() {
        if (hasValidAccessToken()) {
            return "CONFIGURED";
        } else {
            return "NOT_CONFIGURED";
        }
    }

    public void validateConfiguration() throws RuntimeException {
        if (!hasValidAccessToken()) {
            throw new RuntimeException("Instagram API configuration is invalid. Please set a valid INSTAGRAM_ACCESS_TOKEN environment variable or in application properties.");
        }

        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException("Instagram API URL is not configured.");
        }

        if (!url.startsWith("https://")) {
            throw new RuntimeException("Instagram API URL must use HTTPS protocol.");
        }
    }

    // Configuration info for debugging
    public String getConfigurationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Instagram API Configuration:\n");
        info.append("- URL: ").append(url != null ? url : "NOT SET").append("\n");
        info.append("- Access Token: ").append(hasValidAccessToken() ? "CONFIGURED" : "NOT SET OR INVALID").append("\n");
        info.append("- App ID: ").append(appId != null && !appId.trim().isEmpty() ? "SET" : "NOT SET").append("\n");
        info.append("- Client ID: ").append(clientId != null && !clientId.trim().isEmpty() ? "SET" : "NOT SET").append("\n");
        info.append("- Redirect URI: ").append(redirectUri).append("\n");
        info.append("- Rate Limit: ").append(requestsPerHour).append(" requests/hour\n");
        info.append("- Status: ").append(getConfigStatus()).append("\n");

        return info.toString();
    }
}