package com.example.dashboard.analytics;

import com.example.dashboard.model.UserProfile;

public class AnalyticsService {
    public void analyzeUserEngagement(UserProfile userProfile) {
        System.out.println("Analyzing engagement for: " + userProfile.getUsername());
    }

    public void generateReport(UserProfile userProfile) {
        System.out.println("Report for " + userProfile.getUsername() + ":");
        System.out.println("Followers: " + userProfile.getFollowers());
        System.out.println("Posts: " + userProfile.getPosts());
        System.out.println("Likes: " + userProfile.getLikes());
    }
}