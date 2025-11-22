# FlagShip 🚩

FlagShip is a robust, lightweight Android SDK for remote feature
flagging. It enables developers to toggle features, configure values,
and manage rollouts dynamically from a remote dashboard without
requiring app updates.

This repository contains the complete source code for the SDK and a demo
application.

------------------------------------------------------------------------

## 📂 Project Structure

-   **:flagship** -- Core feature flagging SDK (Android Library)
-   **:app** -- Demo application showcasing integration and usage

------------------------------------------------------------------------

## ✨ Key Features

-   **Remote Configuration** -- Fetch and update feature flags via API
-   **Offline Resilience** -- Local caching via Jetpack DataStore
-   **Type Safety** -- Boolean, String, Number, and JSON support
-   **Safe Fallbacks** -- Strict default handling ensures no crashes
-   **Coroutines First** -- Built entirely with Kotlin Coroutines & Flow

------------------------------------------------------------------------

## 🛠 Tech Stack

-   **Language:** Kotlin\
-   **Networking:** Retrofit 2 + Gson
-   **Async:** Kotlin Coroutines & Flow
-   **Local Storage:** AndroidX DataStore

------------------------------------------------------------------------

## 🚀 Integration Guide

### 1. Add Dependency

Add the module to your Gradle config:

``` gradle
dependencies {
    implementation("com.github.vedraj360:flagship-android:1.0.2")
}
```

### 2. Initialization

Initialize the SDK in your `Application` class:

``` kotlin
Flagship.initialize(
    context = this,
    apiKey = "YOUR_API_KEY",
    baseUrl = "https://api.your-dashboard.com/",
    loadData = true,
    defaults = mapOf(
        "new_ui_enabled" to false,
        "retry_limit" to 3
    )
)
```

### 3. Usage

``` kotlin
// 1. Boolean toggle
if (Flagship.isEnabled("new_ui_enabled", defaultValue = false)) {
    showNewDashboard()
} else {
    showOldDashboard()
}

// 2. Get a String configuration
val title = Flagship.getString("home_title", "Welcome Back")

// 3. Get a Number
val maxRetries = Flagship.getNumber("max_retries", 3)

// 4. Fetch flags manually (suspend)
lifecycleScope.launch {
    Flagship.fetchFlags()
}
```

------------------------------------------------------------------------

## 🔐 Security & Best Practices

### 1. Prevent Data Extraction (Critical)

Disable Android backup to protect cached flag data:

**app/src/main/AndroidManifest.xml**

``` xml
<application
    ...
    android:allowBackup="false"
    android:fullBackupContent="false">
```

------------------------------------------------------------------------

### 2. Code Obfuscation

Enable R8/ProGuard for release builds:

**app/build.gradle**

``` gradle
buildTypes {
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

------------------------------------------------------------------------

### 3. Network Security

Always use **HTTPS**.
Disable cleartext traffic in production:

``` xml
<application
    ...
    android:usesCleartextTraffic="false">
```

------------------------------------------------------------------------

## 📱 Running the Demo

1.  Clone the repository
2.  Open in Android Studio
3.  Run the `app` module
4.  Enter your API Key + Dashboard URL on the login screen

------------------------------------------------------------------------

## 📄 License

MIT License
