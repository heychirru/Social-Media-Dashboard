package com.example.dashboard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiController {

    private final SocialMediaApiService apiService;

    @Autowired
    public ApiController(SocialMediaApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Get aggregated statistics for a user across all platforms
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String username) {
        Map<String, Object> stats = apiService.getAggregatedStats(username);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get data from all platforms for a specific user
     */
    @GetMapping("/platforms/{username}")
    public ResponseEntity<Map<String, SocialMediaStats>> getAllPlatformData(@PathVariable String username) {
        Map<String, SocialMediaStats> data = apiService.fetchAllPlatformData(username);
        return ResponseEntity.ok(data);
    }

    /**
     * Get data from a specific platform
     */
    @GetMapping("/platforms/{platform}/{username}")
    public ResponseEntity<SocialMediaStats> getPlatformData(
            @PathVariable String platform,
            @PathVariable String username) {
        SocialMediaStats stats = apiService.fetchPlatformData(platform, username);
        return ResponseEntity.ok(stats);
    }

    /**
     * Search for posts across platforms
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, List<SocialMediaPost>>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int maxResults) {
        Map<String, List<SocialMediaPost>> results = apiService.searchAcrossPlatforms(query, maxResults);
        return ResponseEntity.ok(results);
    }

    /**
     * Get recent posts from a specific platform
     */
    @GetMapping("/platforms/{platform}/{username}/posts")
    public ResponseEntity<List<SocialMediaPost>> getRecentPosts(
            @PathVariable String platform,
            @PathVariable String username,
            @RequestParam(defaultValue = "10") int limit) {
        SocialMediaStats stats = apiService.fetchPlatformData(platform, username);
        List<SocialMediaPost> posts = stats.getRecentPosts();
        if (posts.size() > limit) {
            posts = posts.subList(0, limit);
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Get API configuration status
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = Map.of(
                "instagramConfigured", true // Set based on your config
        );
        return ResponseEntity.ok(config);
    }
}