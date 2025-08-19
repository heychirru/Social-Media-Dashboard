package com.example.dashboard.api;

import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialMediaApiService {

    private final InstagramApiService instagramApiService;

    @Value("${instagram.api.access-token:}")
    private String instagramAccessToken;

    @Autowired
    public SocialMediaApiService(InstagramApiService instagramApiService) {
        this.instagramApiService = instagramApiService;
    }

    public Map<String, Object> getAggregatedStats(String username) throws RuntimeException {
        Map<String, Object> stats = new HashMap<>();

        try {
            SocialMediaStats instagramStats = fetchPlatformData("instagram", username);
            stats.put("instagram", instagramStats);
            stats.put("totalFollowers", instagramStats.getFollowers());
            stats.put("totalPosts", instagramStats.getTotalPosts());
            stats.put("engagementRate", String.format("%.1f%%", instagramStats.getEngagementRate()));
            stats.put("totalLikes", instagramStats.getTotalLikes());
            stats.put("totalComments", instagramStats.getTotalComments());

            return stats;

        } catch (RuntimeException e) {
            System.err.println("Error getting aggregated stats for " + username + ": " + e.getMessage());
            throw new RuntimeException("Failed to get aggregated social media stats for user '" + username + "': " + e.getMessage(), e);
        }
    }

    public Map<String, SocialMediaStats> fetchAllPlatformData(String username) throws RuntimeException {
        Map<String, SocialMediaStats> data = new HashMap<>();

        try {
            data.put("instagram", fetchPlatformData("instagram", username));
            return data;

        } catch (RuntimeException e) {
            System.err.println("Error fetching all platform data for " + username + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch platform data for user '" + username + "': " + e.getMessage(), e);
        }
    }

    public SocialMediaStats fetchPlatformData(String platform, String username) throws RuntimeException {
        if (!"instagram".equalsIgnoreCase(platform)) {
            throw new UnsupportedOperationException("Platform '" + platform + "' is not supported. Currently supported platforms: instagram");
        }

        try {
            return instagramApiService.fetchProfileStats(username, instagramAccessToken);
        } catch (RuntimeException e) {
            System.err.println("Error fetching " + platform + " platform data for " + username + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch " + platform + " data for user '" + username + "': " + e.getMessage(), e);
        }
    }

    public Map<String, List<SocialMediaPost>> searchAcrossPlatforms(String query, int maxResults) throws RuntimeException {
        Map<String, List<SocialMediaPost>> results = new HashMap<>();

        try {
            List<SocialMediaPost> instagramResults = instagramApiService.searchPosts(query, instagramAccessToken, maxResults);
            results.put("instagram", instagramResults);
            return results;

        } catch (RuntimeException e) {
            System.err.println("Error searching across platforms for query '" + query + "': " + e.getMessage());
            throw new RuntimeException("Failed to search for posts with query '" + query + "': " + e.getMessage(), e);
        }
    }

    public SocialMediaStats fetchInstagramProfile(String username) throws RuntimeException {
        try {
            return instagramApiService.fetchProfileStats(username, instagramAccessToken);
        } catch (RuntimeException e) {
            System.err.println("Error fetching Instagram profile for username: " + username + " - " + e.getMessage());
            throw new RuntimeException("Failed to fetch Instagram profile for user '" + username + "': " + e.getMessage(), e);
        }
    }

    public List<SocialMediaPost> fetchInstagramPosts(String username) throws RuntimeException {
        try {
            return instagramApiService.fetchRecentPosts(username, instagramAccessToken);
        } catch (RuntimeException e) {
            System.err.println("Error fetching Instagram posts for username: " + username + " - " + e.getMessage());
            throw new RuntimeException("Failed to fetch Instagram posts for user '" + username + "': " + e.getMessage(), e);
        }
    }

    // Service status methods
    public boolean isInstagramConfigured() {
        return instagramApiService.isConfigured();
    }

    public String getInstagramStatus() {
        return instagramApiService.getServiceStatus();
    }
}