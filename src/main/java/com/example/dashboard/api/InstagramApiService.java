package com.example.dashboard.api;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

public class InstagramApiService {
    private static final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v23.0";
    private static final int DEFAULT_POST_LIMIT = 10;
    private final String accessToken;
    private final CloseableHttpClient httpClient;
    private static final Logger logger = Logger.getLogger(InstagramApiService.class.getName());

    public InstagramApiService(String accessToken) {
        this.accessToken = accessToken;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Main use case: Get Instagram stats from a list of Facebook Page IDs.
     * Each page must be connected to an Instagram Business or Creator account.
     *
     * @param pageIds List of Facebook Page IDs
     * @return List of SocialMediaStats for each connected IG account
     */
    public List<SocialMediaStats> fetchStatsForPages(List<String> pageIds) {
        List<SocialMediaStats> allStats = new ArrayList<>();
        for (String pageId : pageIds) {
            try {
                SocialMediaStats stats = fetchUserStatsFromPageId(pageId);
                if (stats != null) {
                    allStats.add(stats);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to fetch stats for page ID: " + pageId, e);
            }
        }
        return allStats;
    }

    /**
     * Fetch Instagram user profile and recent media using a Facebook Page ID
     *
     * @param pageId Facebook Page ID (must be connected to IG Business account)
     * @return SocialMediaStats or dummy fallback
     */
    @SuppressWarnings({ "LoggerStringConcat", "UseSpecificCatch" })
    public SocialMediaStats fetchUserStatsFromPageId(String pageId) {
        try {
            // 1. Get Instagram Business Account ID from Facebook Page
            String instagramUserId = getInstagramUserIdFromPage(pageId);
            if (instagramUserId == null) {
                logger.warning("Instagram business account not linked to page ID: " + pageId);
                return createDummyInstagramStats("unknown-" + pageId);
            }

            // 2. Get IG profile (follower count, username, etc.)
            String profileUrl = GRAPH_API_BASE_URL + "/" + instagramUserId +
                    "?fields=id,username,account_type,media_count,followers_count,follows_count&access_token=" +
                    accessToken;
            String profileResponse = makeApiRequest(profileUrl);
            JSONObject profileData = new JSONObject(profileResponse);
            String username = profileData.optString("username", "unknown");

            // 3. Get recent media
            String mediaUrl = GRAPH_API_BASE_URL + "/" + instagramUserId +
                    "/media?fields=id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,like_count,comments_count&access_token="
                    +
                    accessToken;
            String mediaResponse = makeApiRequest(mediaUrl);
            JSONObject mediaData = new JSONObject(mediaResponse);
            List<SocialMediaPost> recentPosts = parseRecentPosts(mediaData.getJSONArray("data"), DEFAULT_POST_LIMIT);

            // 4. Calculate engagement rate
            int totalLikes = recentPosts.stream().mapToInt(SocialMediaPost::getLikes).sum();
            int totalComments = recentPosts.stream().mapToInt(SocialMediaPost::getComments).sum();
            int followers = profileData.optInt("followers_count", 0);
            double engagementRate = followers > 0
                    ? ((double) (totalLikes + totalComments) / followers) * 100
                    : 0;

            return new SocialMediaStats(
                    "Instagram",
                    username,
                    followers,
                    profileData.optInt("follows_count", 0),
                    profileData.optInt("media_count", 0),
                    totalLikes,
                    totalComments,
                    0,
                    engagementRate,
                    OffsetDateTime.now().toLocalDateTime(),
                    recentPosts);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching Instagram stats for page ID: " + pageId, e);
            return createDummyInstagramStats("unknown-" + pageId);
        }
    }

    /**
     * Use case: Resolve Instagram Business Account ID linked to a Facebook Page.
     */
    @SuppressWarnings("UseSpecificCatch")
    private String getInstagramUserIdFromPage(String pageId) {
        try {
            String url = GRAPH_API_BASE_URL + "/" + pageId +
                    "?fields=instagram_business_account&access_token=" + accessToken;
            String response = makeApiRequest(url);
            JSONObject json = new JSONObject(response);

            if (json.has("instagram_business_account")) {
                return json.getJSONObject("instagram_business_account").optString("id", null);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get IG account from Page ID: " + pageId, e);
            return null;
        }
    }

    /**
     * Use case: Parse media feed response into app-friendly post objects
     */
    private List<SocialMediaPost> parseRecentPosts(JSONArray postsArray, int postLimit) {
        List<SocialMediaPost> posts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        for (int i = 0; i < postsArray.length() && i < postLimit; i++) {
            JSONObject post = postsArray.getJSONObject(i);

            String mediaUrl = post.optString("media_url", post.optString("thumbnail_url", ""));
            OffsetDateTime createdAt = OffsetDateTime.parse(
                    post.optString("timestamp", OffsetDateTime.now().toString()),
                    formatter);

            posts.add(new SocialMediaPost(
                    post.optString("id", ""),
                    "Instagram",
                    post.optString("caption", ""),
                    mediaUrl,
                    post.optInt("like_count", 0),
                    post.optInt("comments_count", 0),
                    0,
                    createdAt.toLocalDateTime(),
                    "", "" // Optional fields
            ));
        }

        return posts;
    }

    /**
     * Core API request handler with HTTP error logging
     */
    @SuppressWarnings("LoggerStringConcat")
    private String makeApiRequest(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity);

            // Handle common errors
            if (statusCode >= 400) {
                logger.warning("API Error [" + statusCode + "]: " + body);
                throw new IOException("API request failed: " + statusCode + " - " + body);
            }

            return body;
        }
    }

    /**
     * Use case: Return mock stats when API fails or user not found
     */
    private SocialMediaStats createDummyInstagramStats(String username) {
        List<SocialMediaPost> dummyPosts = new ArrayList<>();
        dummyPosts.add(new SocialMediaPost(
                "1", "Instagram", "Example post 🌅",
                "https://example.com/sunset.jpg", 150, 10, 0,
                OffsetDateTime.now().minusHours(2).toLocalDateTime(), username, ""));
        dummyPosts.add(new SocialMediaPost(
                "2", "Instagram", "Sample coffee post ☕",
                "https://example.com/coffee.jpg", 95, 7, 0,
                OffsetDateTime.now().minusHours(5).toLocalDateTime(), username, ""));

        return new SocialMediaStats(
                "Instagram", username, 1234, 567, 42, 245, 17, 0, 2.3,
                OffsetDateTime.now().toLocalDateTime(), dummyPosts);
    }

    /**
     * Cleanup HTTP resources
     */
    @SuppressWarnings("LoggerStringConcat")
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.warning("Error closing HTTP client: " + e.getMessage());
        }
    }

    /**
     * FIXED: Implements fetchStatsForUser using dummy data
     */
    public SocialMediaStats fetchStatsForUser(String username) {
        String pageId = lookupPageIdForUsername(username); // You must implement this
        if (pageId == null) {
            return createDummyInstagramStats(username); // fallback
        }
        return fetchUserStatsFromPageId(pageId); // fetch real stats
    }

    private String lookupPageIdForUsername(String username) {
        // TODO: Replace with real lookup logic (e.g., database or API call)
        logger.log(Level.INFO, "Looking up page ID for Instagram username: {0}", username);
        // For now, always return a dummy page ID for testing
        return "DUMMY_PAGE_ID";
    }

    /**
     * FIXED: Implements searchPosts using dummy posts
     */
    public List<SocialMediaPost> searchPosts(String query, int limit) {
        logger.log(Level.INFO, "Searching Instagram posts for query: {0}", query);
        List<SocialMediaPost> allPosts = createDummyInstagramStats("search").getRecentPosts();
        List<SocialMediaPost> filtered = new ArrayList<>();
        for (SocialMediaPost post : allPosts) {
            if (post.getContent().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(post);
            }
            if (filtered.size() >= limit)
                break;
        }
        return filtered;
    }
}
