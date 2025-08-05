package com.example.dashboard.model;

import java.time.LocalDateTime;
import java.util.List;

public class SocialMediaStats {
    private String platform;
    private String username;
    private int followers;
    private int following;
    private int totalPosts;
    private int totalLikes;
    private int totalComments;
    private int totalShares;
    private double engagementRate;
    private LocalDateTime lastUpdated;
    private List<SocialMediaPost> recentPosts;

    public SocialMediaStats(String platform, String username, int followers, int following,
                           int totalPosts, int totalLikes, int totalComments, int totalShares,
                           double engagementRate, LocalDateTime lastUpdated, List<SocialMediaPost> recentPosts) {
        this.platform = platform;
        this.username = username;
        this.followers = followers;
        this.following = following;
        this.totalPosts = totalPosts;
        this.totalLikes = totalLikes;
        this.totalComments = totalComments;
        this.totalShares = totalShares;
        this.engagementRate = engagementRate;
        this.lastUpdated = lastUpdated;
        this.recentPosts = recentPosts;
    }

    // Getters
    public String getPlatform() { return platform; }
    public String getUsername() { return username; }
    public int getFollowers() { return followers; }
    public int getFollowing() { return following; }
    public int getTotalPosts() { return totalPosts; }
    public int getTotalLikes() { return totalLikes; }
    public int getTotalComments() { return totalComments; }
    public int getTotalShares() { return totalShares; }
    public double getEngagementRate() { return engagementRate; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public List<SocialMediaPost> getRecentPosts() { return recentPosts; }

    @Override
    public String toString() {
        return "SocialMediaStats{" +
                "platform='" + platform + '\'' +
                ", username='" + username + '\'' +
                ", followers=" + followers +
                ", following=" + following +
                ", totalPosts=" + totalPosts +
                ", engagementRate=" + engagementRate +
                '}';
    }
} 