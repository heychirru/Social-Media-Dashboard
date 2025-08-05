package com.example.dashboard.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dashboard.api.SocialMediaApiService;
import com.example.dashboard.config.ApiConfig;
import com.example.dashboard.model.SocialMediaPost;
import com.example.dashboard.model.SocialMediaStats;

@Controller
public class WebController {

    private final SocialMediaApiService apiService;

    public WebController() {
        this.apiService = new SocialMediaApiService(
                ApiConfig.getInstagramAccessToken(), null);
    }

    /**
     * Home page
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("demoMode", ApiConfig.isDemoMode());
        model.addAttribute("instagramConfigured", !"demo_token".equals(ApiConfig.getInstagramAccessToken()));
        return "index";
    }

    /**
     * Dashboard page for a specific user
     */
    @GetMapping("/dashboard/{username}")
    public String dashboard(@PathVariable String username, Model model) {
        try {
            Map<String, SocialMediaStats> allData = apiService.fetchAllPlatformData(username);
            Map<String, Object> aggregated = apiService.getAggregatedStats(username);

            model.addAttribute("username", username);
            model.addAttribute("platformData", allData);
            model.addAttribute("aggregatedStats", aggregated);
            model.addAttribute("demoMode", ApiConfig.isDemoMode());

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Dashboard page for a specific user (query parameter version)
     */
    @GetMapping("/dashboard")
    public String dashboardQuery(@RequestParam String username, Model model) {
        try {
            Map<String, SocialMediaStats> allData = apiService.fetchAllPlatformData(username);
            Map<String, Object> aggregated = apiService.getAggregatedStats(username);

            model.addAttribute("username", username);
            model.addAttribute("platformData", allData);
            model.addAttribute("aggregatedStats", aggregated);
            model.addAttribute("demoMode", ApiConfig.isDemoMode());

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Platform-specific dashboard
     */
    @GetMapping("/platform/{platform}/{username}")
    public String platformDashboard(
            @PathVariable String platform,
            @PathVariable String username,
            Model model) {
        try {
            SocialMediaStats stats = apiService.fetchPlatformData(platform, username);

            model.addAttribute("platform", platform);
            model.addAttribute("username", username);
            model.addAttribute("stats", stats);
            model.addAttribute("demoMode", ApiConfig.isDemoMode());

            return "platform-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Search page
     */
    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            try {
                Map<String, List<SocialMediaPost>> results = apiService.searchAcrossPlatforms(query, 20);
                model.addAttribute("searchResults", results);
                model.addAttribute("query", query);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        model.addAttribute("demoMode", ApiConfig.isDemoMode());
        return "search";
    }

    /**
     * Configuration page
     */
    @GetMapping("/config")
    public String configPage(Model model) {
        model.addAttribute("demoMode", ApiConfig.isDemoMode());
        model.addAttribute("instagramConfigured", !"demo_token".equals(ApiConfig.getInstagramAccessToken()));
        return "config";
    }

    /**
     * About page
     */
    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("demoMode", ApiConfig.isDemoMode());
        return "about";
    }
}