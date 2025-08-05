package com.example.dashboard.model;

public class UserProfile {
    private final String username;
    private final int followers;
    private final int posts;
    private final int likes;

    public UserProfile(String username, int followers, int posts, int likes) {
        this.username = username;
        this.followers = followers;
        this.posts = posts;
        this.likes = likes;
    }

    public String getUsername() { return username; }
    public int getFollowers() { return followers; }
    public int getPosts() { return posts; }
    public int getLikes() { return likes; }
}