package com.example.dashboard.desktop;

import java.util.List;
import java.util.Map;

import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.config.ApiConfig;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DesktopApplication extends Application {

    private SocialMediaApiService apiService;
    private TextField usernameField;
    private TabPane tabPane;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize API service with only Instagram
        apiService = new SocialMediaApiService(
                ApiConfig.getInstagramAccessToken()
                // Remove Twitter/X token
                , null);

        // Create main window
        primaryStage.setTitle("Social Media Dashboard - Desktop Application");

        // Create main layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Create header
        Label titleLabel = new Label("Social Media Analytics Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Demo mode indicator
        if (ApiConfig.isDemoMode()) {
            Label demoLabel = new Label("DEMO MODE - Using sample data");
            demoLabel.setStyle(
                    "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");
            root.getChildren().add(demoLabel);
        }

        // Create input section
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        usernameField = new TextField();
        usernameField.setPromptText("Enter username (e.g., example_user)");
        usernameField.setPrefWidth(300);

        Button analyzeButton = new Button("Analyze");
        analyzeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeButton.setOnAction(e -> analyzeUser());

        Button searchButton = new Button("Search Posts");
        searchButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton.setOnAction(e -> searchPosts());

        inputBox.getChildren().addAll(usernameLabel, usernameField, analyzeButton, searchButton);

        // Create tab pane for different views
        tabPane = new TabPane();
        tabPane.setPrefHeight(600);

        // Overview tab
        Tab overviewTab = new Tab("Overview", createOverviewTab());
        overviewTab.setClosable(false);

        // Platform details tab
        Tab platformsTab = new Tab("Platform Details", createPlatformsTab());
        platformsTab.setClosable(false);

        // Posts tab
        Tab postsTab = new Tab("Recent Posts", createPostsTab());
        postsTab.setClosable(false);

        // Charts tab
        Tab chartsTab = new Tab("Charts", createChartsTab());
        chartsTab.setClosable(false);

        tabPane.getTabs().addAll(overviewTab, platformsTab, postsTab, chartsTab);

        // Status label
        statusLabel = new Label("Ready to analyze");
        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // Add all components to root
        root.getChildren().addAll(titleLabel, inputBox, tabPane, statusLabel);

        // Create scene
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set up keyboard shortcuts
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    analyzeUser();
                    break;
                default:
                    break;
            }
        });
    }

    private VBox createOverviewTab() {
        VBox overviewBox = new VBox(10);
        overviewBox.setPadding(new Insets(10));

        // Create statistics cards
        HBox statsBox = new HBox(10);
        statsBox.setAlignment(Pos.CENTER);

        // These will be populated when data is loaded
        Label followersLabel = new Label("Followers: 0");
        Label postsLabel = new Label("Posts: 0");
        Label likesLabel = new Label("Likes: 0");
        Label engagementLabel = new Label("Engagement: 0%");

        followersLabel.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");
        postsLabel.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");
        likesLabel.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");
        engagementLabel.setStyle(
                "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");

        statsBox.getChildren().addAll(followersLabel, postsLabel, likesLabel, engagementLabel);

        // Summary text area
        TextArea summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(10);
        summaryArea.setPromptText("Summary will appear here after analysis...");

        overviewBox.getChildren().addAll(statsBox, summaryArea);
        return overviewBox;
    }

    private VBox createPlatformsTab() {
        VBox platformsBox = new VBox(10);
        platformsBox.setPadding(new Insets(10));

        // Instagram section only
        TitledPane instagramPane = new TitledPane("Instagram", createPlatformContent("Instagram"));
        instagramPane.setExpanded(true);

        platformsBox.getChildren().addAll(instagramPane);
        return platformsBox;
    }

    private VBox createPlatformContent(@SuppressWarnings("unused") String platform) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        Label followersLabel = new Label("Followers: 0");
        Label followingLabel = new Label("Following: 0");
        Label postsLabel = new Label("Posts: 0");
        Label engagementLabel = new Label("Engagement Rate: 0%");

        content.getChildren().addAll(followersLabel, followingLabel, postsLabel, engagementLabel);
        return content;
    }

    private VBox createPostsTab() {
        VBox postsBox = new VBox(10);
        postsBox.setPadding(new Insets(10));

        // Platform filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Platform:");
        ComboBox<String> platformFilter = new ComboBox<>();
        platformFilter.getItems().addAll("All", "Instagram");
        platformFilter.setValue("All");

        filterBox.getChildren().addAll(filterLabel, platformFilter);

        // Posts list
        ListView<String> postsList = new ListView<>();
        postsList.setPrefHeight(400);

        postsBox.getChildren().addAll(filterBox, postsList);
        return postsBox;
    }

    private VBox createChartsTab() {
        VBox chartsBox = new VBox(10);
        chartsBox.setPadding(new Insets(10));

        // Engagement pie chart
        PieChart engagementChart = new PieChart();
        engagementChart.setTitle("Engagement Distribution");

        // Followers bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> followersChart = new BarChart<>(xAxis, yAxis);
        followersChart.setTitle("Followers by Platform");

        chartsBox.getChildren().addAll(engagementChart, followersChart);
        return chartsBox;
    }

    private void analyzeUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Error", "Please enter a username");
            return;
        }

        statusLabel.setText("Analyzing user: " + username);

        // Run analysis in background thread
        new Thread(() -> {
            try {
                Map<String, SocialMediaStats> allData = apiService.fetchAllPlatformData(username);
                Map<String, Object> aggregated = apiService.getAggregatedStats(username);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    updateUIWithData(allData, aggregated);
                    statusLabel.setText("Analysis complete for: " + username);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to analyze user: " + e.getMessage());
                    statusLabel.setText("Analysis failed");
                });
            }
        }).start();
    }

    private void searchPosts() {
        String query = usernameField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Error", "Please enter a search query");
            return;
        }

        statusLabel.setText("Searching for posts: " + query);

        new Thread(() -> {
            try {
                // FIX: Use correct type for results
                Map<String, List<SocialMediaPost>> results = apiService.searchAcrossPlatforms(query, 20);

                Platform.runLater(() -> {
                    updateUIWithSearchResults(results);
                    statusLabel.setText("Search complete for: " + query);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to search posts: " + e.getMessage());
                    statusLabel.setText("Search failed");
                });
            }
        }).start();
    }

    private void updateUIWithData(Map<String, SocialMediaStats> allData, Map<String, Object> aggregated) {
        // Update overview tab
        Tab overviewTab = tabPane.getTabs().get(0);
        VBox overviewContent = (VBox) overviewTab.getContent();

        HBox statsBox = (HBox) overviewContent.getChildren().get(0);
        Label followersLabel = (Label) statsBox.getChildren().get(0);
        Label postsLabel = (Label) statsBox.getChildren().get(1);
        Label likesLabel = (Label) statsBox.getChildren().get(2);
        Label engagementLabel = (Label) statsBox.getChildren().get(3);

        followersLabel.setText("Followers: " + aggregated.get("totalFollowers"));
        postsLabel.setText("Posts: " + aggregated.get("totalPosts"));
        likesLabel.setText("Likes: " + aggregated.get("totalLikes"));
        engagementLabel
                .setText("Engagement: " + String.format("%.2f%%", (Double) aggregated.get("averageEngagementRate")));

        // Update summary
        TextArea summaryArea = (TextArea) overviewContent.getChildren().get(1);
        StringBuilder summary = new StringBuilder();
        summary.append("Analysis Summary for ").append(usernameField.getText()).append("\n\n");
        summary.append("Total Followers: ").append(aggregated.get("totalFollowers")).append("\n");
        summary.append("Total Posts: ").append(aggregated.get("totalPosts")).append("\n");
        summary.append("Total Likes: ").append(aggregated.get("totalLikes")).append("\n");
        summary.append("Total Comments: ").append(aggregated.get("totalComments")).append("\n");
        summary.append("Total Shares: ").append(aggregated.get("totalShares")).append("\n");
        summary.append("Average Engagement Rate: ")
                .append(String.format("%.2f%%", (Double) aggregated.get("averageEngagementRate"))).append("\n\n");

        for (Map.Entry<String, SocialMediaStats> entry : allData.entrySet()) {
            SocialMediaStats stats = entry.getValue();
            summary.append(entry.getKey()).append(":\n");
            summary.append("  Followers: ").append(stats.getFollowers()).append("\n");
            summary.append("  Posts: ").append(stats.getTotalPosts()).append("\n");
            summary.append("  Engagement Rate: ").append(String.format("%.2f%%", stats.getEngagementRate()))
                    .append("\n\n");
        }

        summaryArea.setText(summary.toString());
    }

    private void updateUIWithSearchResults(Map<String, List<SocialMediaPost>> results) {
        // Update posts tab
        Tab postsTab = tabPane.getTabs().get(2);
        VBox postsContent = (VBox) postsTab.getContent();

        ListView<String> postsList = (ListView<String>) postsContent.getChildren().get(1);
        postsList.getItems().clear();

        for (Map.Entry<String, List<SocialMediaPost>> entry : results.entrySet()) {
            postsList.getItems().add("=== " + entry.getKey() + " ===");
            for (SocialMediaPost post : entry.getValue()) {
                postsList.getItems()
                        .add(post.getContent().substring(0, Math.min(post.getContent().length(), 100)) + "...");
                postsList.getItems().add("  Likes: " + post.getLikes() + " | Comments: " + post.getComments()
                        + " | Shares: " + post.getShares());
                postsList.getItems().add("");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (apiService != null) {
            apiService.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}