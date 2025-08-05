![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/heychirru/Social-Media-Dashboard?utm_source=oss&utm_medium=github&utm_campaign=heychirru%2FSocial-Media-Dashboard&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)

# Social Media Dashboard

## Overview
The Social Media Dashboard is a comprehensive Java application that provides **three different interfaces** for analyzing Instagram and Twitter/X social media data:

1. **🌐 Web Application** - Modern web interface with responsive design
2. **🖥️ Desktop Application** - Native JavaFX desktop application
3. **🔌 REST API** - Full-featured REST API for integration

## 🚀 Features

### Core Features
- **Instagram Integration**: Fetch user profiles, posts, and engagement metrics using Instagram Graph API
- **Cross-Platform Analytics**: Aggregate data from multiple platforms for comprehensive insights
- **Search Functionality**: Search for posts and tweets across platforms
- **Real-time Data**: Get up-to-date statistics and recent posts
- **Demo Mode**: Test all applications with dummy data without API keys

### Web Application Features
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Modern UI**: Built with Bootstrap 5 and Font Awesome icons
- **Interactive Charts**: Visual data representation
- **Real-time Updates**: Live data fetching and display
- **Search Interface**: User-friendly search across platforms

### Desktop Application Features
- **Native Interface**: JavaFX-based desktop application
- **Tabbed Interface**: Organized data presentation
- **Charts and Graphs**: Built-in data visualization
- **Offline Capability**: Works without internet connection (with cached data)
- **Keyboard Shortcuts**: Quick access to features

### REST API Features
- **RESTful Design**: Standard HTTP methods and status codes
- **JSON Responses**: Structured data format
- **CORS Support**: Cross-origin resource sharing enabled
- **Error Handling**: Comprehensive error responses
- **Health Checks**: API status monitoring endpoints

## 📁 Project Structure
```
social-media-dashboard/
├── src/main/java/com/example/dashboard/
│   ├── App.java                          # Main application launcher
│   ├── api/
│   │   ├── SocialMediaApiService.java    # Unified API service
│   │   ├── InstagramApiService.java      # Instagram API integration
│   │   └── TwitterApiService.java        # Twitter/X API integration
│   ├── controller/
│   │   ├── ApiController.java            # REST API endpoints
│   │   └── WebController.java            # Web application controllers
│   ├── desktop/
│   │   └── DesktopApplication.java       # JavaFX desktop application
│   ├── config/
│   │   └── ApiConfig.java                # Configuration management
│   ├── model/
│   │   ├── UserProfile.java              # User profile model
│   │   ├── SocialMediaPost.java          # Post/tweet model
│   │   └── SocialMediaStats.java         # Statistics model
│   └── demo/
│       └── SocialMediaDemo.java          # Demo application
├── src/main/resources/
│   ├── templates/                        # Thymeleaf HTML templates
│   │   ├── index.html                    # Home page
│   │   ├── dashboard.html                # Dashboard page
│   │   └── ...
│   └── application.properties            # Spring Boot configuration
├── config.properties.example             # API configuration template
└── pom.xml                              # Maven dependencies
```

## 🛠️ Setup Instructions

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher
- Git

### 1. Clone and Build
```bash
git clone <repository-url>
cd social-media-dashboard
mvn clean install
```

### 2. Configure API Keys

**Option A: Configuration File**
```bash
cp config.properties.example config.properties
# Edit config.properties and add your API keys
```

**Option B: Environment Variables**
```bash
export INSTAGRAM_ACCESS_TOKEN="your_instagram_token_here"
```

### 3. Run Applications

#### Web Application
```bash
# Start web application (default)
mvn spring-boot:run

# Or with specific mode
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar web
```
**Access at:** http://localhost:8080

#### Desktop Application
```bash
# Start desktop application
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar desktop

# Or run directly
mvn exec:java -Dexec.mainClass="com.example.dashboard.desktop.DesktopApplication"
```

#### REST API Server
```bash
# Start API server only
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar api

# Or run with Spring Boot
mvn spring-boot:run -Dspring.profiles.active=api
```
**API Base URL:** http://localhost:8080/api/v1

#### Demo Mode
```bash
# Run demo with console output
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar demo

# Or run demo class directly
mvn exec:java -Dexec.mainClass="com.example.dashboard.demo.SocialMediaDemo"
```

## 🔌 REST API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### User Statistics
```http
GET /api/v1/stats/{username}
```
Returns aggregated statistics across all platforms.

#### Platform Data
```http
GET /api/v1/platforms/{username}
GET /api/v1/platforms/{platform}/{username}
```
Returns data from all platforms or a specific platform.

#### Search
```http
GET /api/v1/search?query={search_term}&maxResults={number}
```
Search for posts/tweets across platforms.

#### Recent Posts
```http
GET /api/v1/platforms/{platform}/{username}/posts?limit={number}
```
Get recent posts from a specific platform.

#### Health & Configuration
```http
GET /api/v1/health
GET /api/v1/config
```
Check API status and configuration.

### Example API Usage
```bash
# Get user stats
curl http://localhost:8080/api/v1/stats/example_user

# Search for posts
curl "http://localhost:8080/api/v1/search?query=tech&maxResults=10"

# Get Instagram data
curl http://localhost:8080/api/v1/platforms/Instagram/example_user
```

## 🖥️ Desktop Application Usage

1. **Launch the application**
2. **Enter username** in the input field
3. **Click "Analyze"** to fetch user data
4. **Use tabs** to view different data sections:
   - **Overview**: Aggregated statistics
   - **Platform Details**: Platform-specific data
   - **Recent Posts**: Latest posts and tweets
   - **Charts**: Visual data representation

## 🌐 Web Application Usage

1. **Open browser** and navigate to http://localhost:8080
2. **Enter username** in the search form
3. **View comprehensive dashboard** with:
   - Aggregated statistics
   - Platform-specific analytics
   - Recent posts and engagement metrics
   - Interactive charts and graphs

## 🔧 API Setup

### Instagram API Setup
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app or use an existing one
3. Add Instagram Basic Display product to your app
4. Generate an Instagram Graph API access token
5. Add the token to your configuration

## 📊 Demo Mode

All applications work in **Demo Mode** without API keys:
- Uses realistic sample data
- Perfect for testing and demonstration
- No API setup required
- Shows all features and functionality

   ```

## Environment Variables
Set these environment variables:
```bash
# Windows (PowerShell)
$env:INSTAGRAM_ACCESS_TOKEN="your_instagram_token"
$env:TWITTER_BEARER_TOKEN="your_twitter_token"

# Windows (Command Prompt)
set INSTAGRAM_ACCESS_TOKEN=your_instagram_token
set TWITTER_BEARER_TOKEN=your_twitter_token

# Linux/Mac
export INSTAGRAM_ACCESS_TOKEN="your_instagram_token"
export TWITTER_BEARER_TOKEN="your_twitter_token"
```

#### Method 3: Use Helper Scripts
Run the provided helper scripts:
```bash
# Windows
turn_off_demo.bat
# or
powershell -ExecutionPolicy Bypass -File turn_off_demo.ps1
```

#### Getting API Tokens

**Instagram Graph API Access Token:**
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app or use an existing one
3. Add Instagram Basic Display product to your app
4. Generate an Instagram Graph API access token
5. Add the token to your configuration


#### Verification
After setting up real tokens:
- Restart your application
- The demo mode indicator should disappear
- You'll see real data from social media platforms
- Check the configuration page at `/config` to verify token status

## 🛡️ Error Handling

- **Graceful Fallbacks**: Automatic fallback to demo data
- **Comprehensive Logging**: Detailed error tracking
- **User-Friendly Messages**: Clear error explanations
- **Retry Mechanisms**: Automatic retry for transient failures

## 📦 Dependencies

### Core Dependencies
- **Spring Boot 2.5.4**: Application framework
- **Apache HttpClient**: HTTP client for API requests
- **JSON**: JSON parsing and manipulation
- **Jackson**: JSON serialization/deserialization

### Web Application
- **Thymeleaf**: Template engine
- **Bootstrap 5**: CSS framework
- **Font Awesome**: Icons

### Desktop Application
- **JavaFX 16**: Desktop UI framework
- **JavaFX Charts**: Data visualization

### Database
- **H2 Database**: In-memory database
- **Spring Data JPA**: Data access layer

## 🚀 Quick Start

```bash
# 1. Clone and build
git clone <repository-url>
cd social-media-dashboard
mvn clean install

# 2. Run web application (demo mode)
mvn spring-boot:run

# 3. Open browser
# Navigate to: http://localhost:8080
# Enter any username to see demo data
```

## 🤝 Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for more details.
