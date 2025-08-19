# 🚀 Social Media Dashboard

A comprehensive Java application for analyzing Instagram 📸 data via Web 🌐, Desktop 🖥️, and REST API 🔌 interfaces.


---

## 📑 Table of Contents

* [👀 Overview](#overview)
* [✨ Features](#features)
* [📂 Project Structure](#project-structure)
* [⚙️ Setup Instructions](#setup-instructions)
* [🔑 API Keys & Environment Variables](#api-keys--environment-variables)
* [▶️ Running Applications](#running-applications)
* [📡 REST API Documentation](#rest-api-documentation)
* [💻 Web & Desktop Usage](#web--desktop-usage)
* [❗ Error Handling](#error-handling)
* [📦 Dependencies](#dependencies)
* [⚡ Quick Start](#quick-start)
* [🤝 Contributing](#contributing)
* [📜 License](#license)

---

## 👀 Overview

**Social Media Dashboard** provides:

* 🌐 Web Application (responsive UI)
* 🖥️ Desktop Application (JavaFX)
* 🔌 REST API (integration-ready)

---

## ✨ Features

### 🔧 Core

* 📸 Instagram
* 🔍 Search posts
* 📊 Real-time stats

### 🌐 Web

* 📱 Responsive, modern UI
* 📈 Interactive charts
* 🔄 Live updates

### 🖥️ Desktop

* 🖼️ Native JavaFX UI
* 🗂️ Tabbed interface
* 📥 Offline/cached data

### 🔌 REST API

* 🌍 RESTFUL endpoints
* 📦 JSON responses
* 🔒 CORS & error handling
* ❤️ Health checks

---

## 📂 Project Structure

```text
social-media-dashboard/
├── pom.xml                        # Maven configuration
├── config.properties.example      # Example config file for API keys
├── src/
│   ├── main/
│   │   ├── java/com/example/dashboard/
│   │   │   ├── App.java
│   │   │   ├── api/
│   │   │   │   ├── SocialMediaApiService.java
│   │   │   │   ├── InstagramApiService.java
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java
│   │   │   │   └── WebController.java
│   │   │   ├── desktop/
│   │   │   │   └── DesktopApplication.java
│   │   │   ├── config/
│   │   │   │   └── ApiConfig.java
│   │   │   ├── model/
│   │   │   │   ├── UserProfile.java
│   │   │   │   ├── SocialMediaPost.java
│   │   │   │   └── SocialMediaStats.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── index.html
│   │       │   ├── dashboard.html
│   │       │   └── ...
│   │       ├── application.properties
│   │       └── static/            # CSS, JS, images
│   └── test/java/com/example/dashboard/
│       └── AppTest.java           # Unit tests
└── README.md
```

---

## ⚙️ Setup Instructions

### ✅ Prerequisites

* ☕ Java 8+
* 📦 Maven 3.6+
* 🌀 Git

### 📥 Clone & Build

```bash
git clone <repository-url>
cd social-media-dashboard
mvn clean install
```

---

## 🔑 API Keys & Environment Variables

### 🔧 Configure API Keys

**Option 1: config.properties**

```bash
cp config.properties.example config.properties
# Edit config.properties and add your API keys 🔑
```

---

## ▶️ Running Applications

### 🌐 Web Application

```bash
mvn spring-boot:run
# or
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar web
```

👉 Access: [http://localhost:8080](http://localhost:8080)

---

### 🖥️ Desktop Application

```bash
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar desktop
# or
mvn exec:java -Dexec.mainClass="com.example.dashboard.desktop.DesktopApplication"
```

---

### 📡 REST API Server

```bash
java -jar target/social-media-dashboard-1.0-SNAPSHOT.jar api
# or
mvn spring-boot:run -Dspring.profiles.active=api
```

🌍 API Base URL: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)

---

## 📡 REST API Documentation

### 📍 Endpoints

* **📊 User Statistics:** `GET /api/v1/stats/{username}`
* **🗂️ Platform Data:** `GET /api/v1/platforms/{username}` or `GET /api/v1/platforms/{platform}/{username}`
* **🔍 Search:** `GET /api/v1/search?query={search_term}&maxResults={number}`
* **📝 Recent Posts:** `GET /api/v1/platforms/{platform}/{username}/posts?limit={number}`
* **❤️ Health & Config:** `GET /api/v1/health`, `GET /api/v1/config`

---

## 💻 Web & Desktop Usage

### 🌐 Web

1. 🔗 Open [http://localhost:8080](http://localhost:8080)
2. 👤 Enter username
3. 📊 View dashboard: stats, analytics, posts, charts

### 🖥️ Desktop

1. ▶️ Launch app
2. 👤 Enter username
3. 🔎 Click "Analyze"
4. 🗂️ Use tabs for overview, platform details, posts, charts

---

## ❗ Error Handling

* ✅ Graceful fallback to demo data
* 📝 Detailed logging
* 💡 User-friendly error messages
* 🔄 Retry for transient failures

---

## 📦 Dependencies

* ⚡ Spring Boot
* 🌐 Apache HttpClient
* 🔄 Jackson
* 🎨 Thymeleaf, Bootstrap, Font Awesome (Web)
* 🖼️ JavaFX (Desktop)
* 🗄️ H2 Database, Spring Data JPA (Database)

---

## ⚡ Quick Start

```bash
git clone <repository-url>
cd social-media-dashboard
mvn clean install
mvn spring-boot:run
# 🚀 Open http://localhost:8080 and enter any username for demo data
```

---

## 🤝 Contributing

Contributions welcome! Submit a PR 📬 or open an issue 🐞.
Feel Free 😊👨‍💻

![Thanks](https://tenor.com/en-IN/view/the-office-michael-scott-thank-you-bow-steve-carell-gif-5009516783019270794)
---

## 📜 License

📄 Apache License. See LICENSE.
