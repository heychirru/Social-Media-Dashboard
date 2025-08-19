package com.example.dashboard.model;

import java.time.LocalDateTime;

public class SocialMediaPost {
    private final String id;
    private final String platform;
    private final String content;
    private final String mediaUrl;
    private final int likes;
    private final int comments;
    private final int shares;
    private final LocalDateTime createdAt;
    private final String authorUsername;
    private final String authorProfilePic;

    public SocialMediaPost(String id, String platform, String content, String mediaUrl,
            int likes, int comments, int shares, LocalDateTime createdAt,
            String authorUsername, String authorProfilePic) {
        this.id = id;
        this.platform = platform;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.createdAt = createdAt;
        this.authorUsername = authorUsername;
        this.authorProfilePic = authorProfilePic;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getContent() {
        return content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public int getShares() {
        return shares;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getAuthorProfilePic() {
        return authorProfilePic;
    }

    @Override
    public String toString() {
        return "SocialMediaPost{" +
                "id='" + id + '\'' +
                ", platform='" + platform + '\'' +
                ", content='" + content + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                ", shares=" + shares +
                ", author='" + authorUsername + '\'' +
                '}';
    }

    public Object getCaption() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}