package com.example.dashboard.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

/**
 * Central service to fetch data from all social platforms.
 * Currently supports Instagram Business accounts via Facebook Page ID.
 */
@Service
public class SocialMediaApiService {

    private static final Logger logger = Logger.getLogger(SocialMediaApiService.class.getName());

    private final InstagramApiService instagramApiService;
    // Removed unused apiUrl field

    /**
     * Constructor with injected configuration values.
     *
     * @param instagramAccessToken Injected from application properties
     * @param apiUrl               Injected from application properties
     */
    public SocialMediaApiService(
            @Value("${social.api.token}") String instagramAccessToken,
            @Value("${social.api.url}") String apiUrl) {

        this.instagramApiService = new InstagramApiService(instagramAccessToken);
    }

    // Fetch stats for all platforms (currently Instagram only)
    public Map<String, SocialMediaStats> fetchAllPlatformData(String username) {
        Map<String, SocialMediaStats> allStats = new HashMap<>();
        try {
            SocialMediaStats instagramStats = instagramApiService.fetchStatsForUser(username);
            allStats.put("Instagram", instagramStats);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to fetch Instagram stats", e);
        }
        return allStats;
    }

    // Aggregated stats for overview tab
    public Map<String, Object> getAggregatedStats(String username) {
        Map<String, SocialMediaStats> statsMap = fetchAllPlatformData(username);
        Map<String, Object> aggregated = new HashMap<>();
        int totalFollowers = 0, totalPosts = 0, totalLikes = 0, totalComments = 0, totalShares = 0;
        double totalEngagement = 0.0;
        int count = 0;
        for (SocialMediaStats stats : statsMap.values()) {
            totalFollowers += stats.getFollowers();
            totalPosts += stats.getTotalPosts();
            totalLikes += stats.getTotalLikes();
            totalComments += stats.getTotalComments();
            totalShares += stats.getTotalShares();
            totalEngagement += stats.getEngagementRate();
            count++;
        }
        aggregated.put("totalFollowers", totalFollowers);
        aggregated.put("totalPosts", totalPosts);
        aggregated.put("totalLikes", totalLikes);
        aggregated.put("totalComments", totalComments);
        aggregated.put("totalShares", totalShares);
        aggregated.put("averageEngagementRate", count > 0 ? totalEngagement / count : 0.0);
        return aggregated;
    }

    // Search posts across platforms
    public Map<String, List<SocialMediaPost>> searchAcrossPlatforms(String query, int limit) {
        Map<String, List<SocialMediaPost>> results = new HashMap<>();
        try {
            List<SocialMediaPost> instagramPosts = instagramApiService.searchPosts(query, limit);
            results.put("Instagram", instagramPosts);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to search Instagram posts", e);
        }
        return results;
    }

    // Fetch data from a specific platform
    public SocialMediaStats fetchPlatformData(String platform, String username) {
        switch (platform.toLowerCase()) {
            case "instagram":
                return instagramApiService.fetchStatsForUser(username);
            default:
                throw new UnsupportedOperationException("Platform not supported: " + platform);
        }
    }

    // Cleanup all API clients
    public void close() {
        instagramApiService.close();
    }
}
