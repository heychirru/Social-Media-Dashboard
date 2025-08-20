package com.example.dashboard.api;

import com.example.dashboard.config.InstagramConfig;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstagramApiService {

    private final InstagramConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache for authenticated user info to avoid repeated API calls
    private String authenticatedUserId = null;
    private String authenticatedUsername = null;

    @Autowired
    public InstagramApiService(InstagramConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public SocialMediaStats fetchProfileStats(String username, String accessToken) throws RuntimeException {
        validateConfiguration();

        String token = getValidToken(accessToken);

        try {
            // Get authenticated user info
            AuthenticatedUserInfo userInfo = getAuthenticatedUserInfo(token);
            
            // Check if requested username matches authenticated user
            if (!userInfo.username.equalsIgnoreCase(username)) {
                System.out.println("⚠️ Warning: Instagram Basic Display API limitation - requested username '" + 
                                 username + "' but can only return data for authenticated user '" + 
                                 userInfo.username + "'");
                
                // Throw an exception explaining the limitation
                throw new RuntimeException("Instagram Basic Display API can only access data for the authenticated user '" + 
                                         userInfo.username + "'. Requested user '" + username + "' is not accessible. " +
                                         "This is a limitation of Instagram Basic Display API - it only provides access to the token owner's data.");
            }

            String userId = userInfo.userId;
            String actualUsername = userInfo.username;

            // Fetch profile data using the authenticated user's ID
            String profileUrl = String.format("%s/%s?fields=id,username,account_type,media_count,followers_count,follows_count&access_token=%s",
                    config.getUrl(), userId, token);

            ResponseEntity<String> response = restTemplate.getForEntity(profileUrl, String.class);
            JsonNode profileData = objectMapper.readTree(response.getBody());

            // Fetch recent posts for engagement metrics
            List<SocialMediaPost> recentPosts = fetchRecentPosts(actualUsername, token);

            // Calculate engagement metrics
            EngagementMetrics metrics = calculateEngagementMetrics(recentPosts);

            int followersCount = profileData.has("followers_count") ?
                    profileData.get("followers_count").asInt() : 0;
            int followingCount = profileData.has("follows_count") ?
                    profileData.get("follows_count").asInt() : 0;
            int postsCount = profileData.has("media_count") ?
                    profileData.get("media_count").asInt() : 0;

            double engagementRate = calculateEngagementRate(metrics, followersCount, recentPosts.size());

            System.out.println("✅ Successfully fetched Instagram data for: " + actualUsername);

            return new SocialMediaStats(
                    profileData.get("id").asText(),
                    profileData.get("username").asText(), // Use actual username from API
                    followersCount,
                    followingCount,
                    postsCount,
                    metrics.totalLikes,
                    metrics.totalComments,
                    metrics.totalShares,
                    engagementRate,
                    LocalDateTime.now(),
                    recentPosts
            );

        } catch (HttpClientErrorException e) {
            throw handleHttpError(e, username, "profile stats");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Network connectivity issue while fetching profile for username: " + username + ". Please check your internet connection.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Instagram profile stats for username: " + username + ". Error: " + e.getMessage(), e);
        }
    }

    public List<SocialMediaPost> fetchRecentPosts(String username, String accessToken) throws RuntimeException {
        validateConfiguration();

        String token = getValidToken(accessToken);

        try {
            // Get authenticated user info
            AuthenticatedUserInfo userInfo = getAuthenticatedUserInfo(token);
            
            // Check if requested username matches authenticated user
            if (!userInfo.username.equalsIgnoreCase(username)) {
                System.out.println("⚠️ Warning: Instagram Basic Display API limitation - requested username '" + 
                                 username + "' but can only return posts for authenticated user '" + 
                                 userInfo.username + "'");
                
                // Throw an exception explaining the limitation
                throw new RuntimeException("Instagram Basic Display API can only access posts for the authenticated user '" + 
                                         userInfo.username + "'. Requested user '" + username + "' is not accessible. " +
                                         "This is a limitation of Instagram Basic Display API.");
            }

            String userId = userInfo.userId;
            String actualUsername = userInfo.username;

            // Fetch recent media using authenticated user's ID
            String mediaUrl = String.format("%s/%s/media?fields=id,caption,media_type,media_url,permalink,timestamp,like_count,comments_count&limit=25&access_token=%s",
                    config.getUrl(), userId, token);

            ResponseEntity<String> response = restTemplate.getForEntity(mediaUrl, String.class);
            JsonNode mediaData = objectMapper.readTree(response.getBody());

            List<SocialMediaPost> posts = new ArrayList<>();

            if (mediaData.has("data") && mediaData.get("data").isArray()) {
                for (JsonNode post : mediaData.get("data")) {
                    posts.add(parsePostData(post, actualUsername)); // Use actual username
                }
            }

            System.out.println("✅ Successfully fetched " + posts.size() + " Instagram posts for: " + actualUsername);
            return posts;

        } catch (HttpClientErrorException e) {
            throw handleHttpError(e, username, "recent posts");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Network connectivity issue while fetching posts for username: " + username + ". Please check your internet connection.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Instagram posts for username: " + username + ". Error: " + e.getMessage(), e);
        }
    }

    public List<SocialMediaPost> searchPosts(String query, String accessToken, int maxResults) throws RuntimeException {
        validateConfiguration();

        String token = getValidToken(accessToken);

        try {
            // Instagram's hashtag search
            String searchUrl = String.format("%s/ig_hashtag_search?q=%s&access_token=%s",
                    config.getUrl(), query.replace("#", ""), token);

            ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
            JsonNode searchData = objectMapper.readTree(searchResponse.getBody());

            List<SocialMediaPost> posts = new ArrayList<>();

            if (searchData.has("data") && searchData.get("data").isArray() && searchData.get("data").size() > 0) {
                JsonNode firstResult = searchData.get("data").get(0);
                if (firstResult.has("id")) {
                    String hashtagId = firstResult.get("id").asText();
                    posts = fetchHashtagPosts(hashtagId, token, maxResults);
                }
            }

            System.out.println("✅ Successfully searched Instagram posts for query: " + query + ", found: " + posts.size());
            return posts;

        } catch (HttpClientErrorException e) {
            throw handleHttpError(e, query, "search posts");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Network connectivity issue while searching for query: " + query + ". Please check your internet connection.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search Instagram posts for query: " + query + ". Error: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private void validateConfiguration() throws RuntimeException {
        if (!config.hasValidAccessToken()) {
            throw new RuntimeException("Instagram API is not properly configured. Please provide a valid access token via INSTAGRAM_ACCESS_TOKEN environment variable or application properties.");
        }
    }

    private String getValidToken(String providedToken) throws RuntimeException {
        // Use provided token if valid, otherwise fall back to config
        String token = (providedToken != null && isValidToken(providedToken)) ?
                providedToken : config.getAccessToken();

        if (!isValidToken(token)) {
            throw new RuntimeException("Invalid Instagram access token provided. Please check your token configuration.");
        }

        return token;
    }

    private boolean isValidToken(String token) {
        return token != null &&
                !token.trim().isEmpty() &&
                !token.equals("demo-token") &&
                !token.startsWith("$") &&
                !token.contains("your_access_token_here") &&
                token.length() > 20; // Basic length check
    }

    /**
     * Get authenticated user information and cache it
     */
    private AuthenticatedUserInfo getAuthenticatedUserInfo(String token) throws Exception {
        // Return cached info if available
        if (authenticatedUserId != null && authenticatedUsername != null) {
            return new AuthenticatedUserInfo(authenticatedUserId, authenticatedUsername);
        }

        // Fetch authenticated user info using /me endpoint
        String meUrl = String.format("%s/me?fields=id,username&access_token=%s", config.getUrl(), token);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(meUrl, String.class);
            JsonNode userData = objectMapper.readTree(response.getBody());
            
            // Cache the results
            authenticatedUserId = userData.get("id").asText();
            authenticatedUsername = userData.get("username").asText();
            
            System.out.println("📍 Authenticated Instagram user: " + authenticatedUsername + " (ID: " + authenticatedUserId + ")");
            
            return new AuthenticatedUserInfo(authenticatedUserId, authenticatedUsername);
            
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("❌ Error fetching authenticated user info: " + errorBody);
            throw new RuntimeException("Unable to fetch authenticated user information. HTTP " + e.getStatusCode() + ": " + errorBody, e);
        }
    }

    private SocialMediaPost parsePostData(JsonNode postNode, String username) {
        String postId = postNode.has("id") ? postNode.get("id").asText() : "";
        String caption = postNode.has("caption") ? postNode.get("caption").asText() : "";
        String mediaUrl = postNode.has("media_url") ? postNode.get("media_url").asText() : "";
        String mediaType = postNode.has("media_type") ? postNode.get("media_type").asText().toLowerCase() : "image";
        int likes = postNode.has("like_count") ? postNode.get("like_count").asInt() : 0;
        int comments = postNode.has("comments_count") ? postNode.get("comments_count").asInt() : 0;

        LocalDateTime timestamp = LocalDateTime.now();
        if (postNode.has("timestamp")) {
            try {
                String timestampStr = postNode.get("timestamp").asText();
                
                // Handle Instagram's timestamp format: 2025-08-13T07:00:18+0000
                // Convert +0000 to Z for proper ISO parsing
                if (timestampStr.endsWith("+0000")) {
                    timestampStr = timestampStr.replace("+0000", "Z");
                }
                
                // Parse as ZonedDateTime first, then convert to LocalDateTime
                if (timestampStr.endsWith("Z")) {
                    timestamp = ZonedDateTime.parse(timestampStr).toLocalDateTime();
                } else {
                    // Fallback to original parsing
                    timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
                }
                
            } catch (Exception e) {
                System.err.println("⚠️ Failed to parse timestamp: " + postNode.get("timestamp").asText());
                // Use current time as fallback
                timestamp = LocalDateTime.now();
            }
        }

        return new SocialMediaPost(
                postId,
                "instagram",
                caption,
                mediaUrl,
                likes,
                comments,
                0, // Instagram doesn't have direct shares metric
                timestamp,
                username,
                "" // profile pic placeholder
        );
    }

    private List<SocialMediaPost> fetchHashtagPosts(String hashtagId, String token, int maxResults) throws Exception {
        String mediaUrl = String.format("%s/%s/recent_media?fields=id,caption,media_type,media_url,permalink,timestamp,like_count,comments_count&limit=%d&access_token=%s",
                config.getUrl(), hashtagId, maxResults, token);

        ResponseEntity<String> mediaResponse = restTemplate.getForEntity(mediaUrl, String.class);
        JsonNode mediaData = objectMapper.readTree(mediaResponse.getBody());

        List<SocialMediaPost> posts = new ArrayList<>();
        if (mediaData.has("data")) {
            for (JsonNode post : mediaData.get("data")) {
                posts.add(parsePostData(post, "hashtag_search"));
            }
        }

        return posts;
    }

    private RuntimeException handleHttpError(HttpClientErrorException e, String context, String operation) {
        String errorBody = e.getResponseBodyAsString();
        HttpStatus status = e.getStatusCode();

        System.err.println("📡 Instagram API HTTP Error for " + operation + " (context: " + context + "): " + status + " - " + errorBody);

        if (errorBody.contains("API access blocked") ||
                errorBody.contains("OAuthException") ||
                status == HttpStatus.UNAUTHORIZED) {

            return new RuntimeException("Instagram API access denied for " + operation + ". Your access token may be invalid, expired, or lack required permissions. HTTP " + status + ": " + errorBody);

        } else if (status == HttpStatus.FORBIDDEN) {
            return new RuntimeException("Instagram API access forbidden for " + operation + ". This may require app review or additional permissions. HTTP " + status + ": " + errorBody);

        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return new RuntimeException("Instagram API rate limit exceeded for " + operation + ". Please wait before making more requests. HTTP " + status + ": " + errorBody);

        } else if (status == HttpStatus.NOT_FOUND) {
            return new RuntimeException("Instagram resource not found for " + operation + " (context: " + context + "). Please verify the username/query is correct. HTTP " + status + ": " + errorBody);

        } else if (status == HttpStatus.BAD_REQUEST) {
            return new RuntimeException("Invalid request for Instagram " + operation + " (context: " + context + "). Please check your parameters. HTTP " + status + ": " + errorBody);

        } else {
            return new RuntimeException("Instagram API error for " + operation + " (context: " + context + "). HTTP " + status + ": " + errorBody);
        }
    }

    // Helper class for authenticated user info
    private static class AuthenticatedUserInfo {
        final String userId;
        final String username;
        
        AuthenticatedUserInfo(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    private static class EngagementMetrics {
        int totalLikes = 0;
        int totalComments = 0;
        int totalShares = 0;
    }

    private EngagementMetrics calculateEngagementMetrics(List<SocialMediaPost> posts) {
        EngagementMetrics metrics = new EngagementMetrics();
        for (SocialMediaPost post : posts) {
            metrics.totalLikes += post.getLikes();
            metrics.totalComments += post.getComments();
            metrics.totalShares += post.getShares();
        }
        return metrics;
    }

    private double calculateEngagementRate(EngagementMetrics metrics, int followers, int postsCount) {
        if (followers <= 0 || postsCount <= 0) return 0.0;
        return ((double)(metrics.totalLikes + metrics.totalComments) / (followers * postsCount)) * 100;
    }

    // Public service status methods
    public String getServiceStatus() {
        return config.getConfigStatus();
    }

    public boolean isConfigured() {
        return config.hasValidAccessToken();
    }

    /**
     * Get the authenticated user's username (who owns the access token)
     */
    public String getAuthenticatedUsername(String accessToken) {
        try {
            String token = getValidToken(accessToken);
            AuthenticatedUserInfo userInfo = getAuthenticatedUserInfo(token);
            return userInfo.username;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clear cached authenticated user info (call this if token changes)
     */
    public void clearAuthenticatedUserCache() {
        authenticatedUserId = null;
        authenticatedUsername = null;
    }
}