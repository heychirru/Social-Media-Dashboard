package com.example.dashboard.desktop;

import com.example.dashboard.api.InstagramApiService;
import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.config.InstagramConfig;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;
import java.util.List;

public class DesktopApplication {

    public static void main(String[] args) {
        System.out.println("Starting Social Media Dashboard Desktop Application...");
        
        try {
            // Create and configure InstagramConfig
            InstagramConfig instagramConfig = new InstagramConfig();
            
            // Set access token from environment variable or system property
            String accessToken = System.getenv("INSTAGRAM_ACCESS_TOKEN");
            if (accessToken == null || accessToken.trim().isEmpty()) {
                accessToken = System.getProperty("instagram.access.token");
            }
            
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                instagramConfig.setAccessToken(accessToken);
                System.out.println("✅ Instagram access token configured from environment");
            } else {
                System.out.println("⚠️ Warning: No Instagram access token found in environment variables");
                System.out.println("Please set INSTAGRAM_ACCESS_TOKEN environment variable for full functionality");
                // Set a placeholder token for testing (will cause API errors but won't crash)
                instagramConfig.setAccessToken("demo-token-for-testing");
            }
            
            // Create the required dependencies with proper configuration
            InstagramApiService instagramApiService = new InstagramApiService(instagramConfig);
            SocialMediaApiService apiService = new SocialMediaApiService(instagramApiService);
            
            // Check configuration status
            System.out.println("Instagram API Status: " + instagramApiService.getServiceStatus());
            
            // Test the functionality
            System.out.println("Testing API service...");
            
            String testUsername = "testuser";
            System.out.println("Fetching profile for: " + testUsername);
            
            if (instagramConfig.hasValidAccessToken()) {
                // Only test actual API calls if properly configured
                SocialMediaStats profile = apiService.fetchInstagramProfile(testUsername);
                System.out.println("Profile: " + profile.getUsername() + 
                                 " - Followers: " + profile.getFollowers());
                
                List<SocialMediaPost> posts = apiService.fetchInstagramPosts(testUsername);
                System.out.println("Posts count: " + posts.size());
                
                System.out.println("✅ Desktop application test completed successfully!");
            } else {
                System.out.println("⚠️ Desktop application initialized but Instagram API not configured");
                System.out.println("To enable Instagram functionality:");
                System.out.println("1. Get an Instagram access token from Facebook Developers");
                System.out.println("2. Set INSTAGRAM_ACCESS_TOKEN environment variable");
                System.out.println("3. Restart the application");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in desktop application: " + e.getMessage());
            
            // Provide helpful error context
            if (e.getMessage().contains("configuration")) {
                System.err.println("💡 This appears to be a configuration issue.");
                System.err.println("Please ensure INSTAGRAM_ACCESS_TOKEN environment variable is set.");
            } else if (e.getMessage().contains("access denied") || e.getMessage().contains("OAuthException")) {
                System.err.println("💡 This appears to be an authentication issue.");
                System.err.println("Please check if your Instagram access token is valid and has required permissions.");
            }
            
            e.printStackTrace();
        }
    }
}