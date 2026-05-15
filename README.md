<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# Gandha-Siri 🌳

**Gandha-Siri** is a professional-grade agricultural application designed specifically for Sandalwood farmers. It acts as a digital "Guard" for high-value assets, providing biological growth tracking, security auditing, and financial valuation insights through a premium, data-driven dashboard.

---

## ✨ Key Features

### 📊 Professional Dashboard
- **Natural Wealth Summary:** High-impact gradient card displaying estimated farm valuation based on real-time growth metrics and market rates.
- **Wealth Analytics:** Interactive line charts powered by `MPAndroidChart` to visualize asset growth trends over time.
- **Next Harvest Estimate:** Intelligent countdown based on tree maturity and planting date algorithms.

### 🗺️ Farm Estate Preview
- **Satellite Mode:** High-definition satellite view of your plantation estate, providing a realistic view of your assets.
- **Custom Markers:** Custom-drawn green circular markers with white halos for professional estate visualization, exactly as required for modern agricultural audits.
- **Proximity Audit:** Integrated tracking to monitor registered assets relative to the user's current GPS coordinates.

### 🛡️ Operation Center
- **Tag New Tree:** GPS-enabled tree registration with girth measurement and photo logging.
- **Security Check:** Infrastructure audit checklist (Fencing, CCTV, Panic System, Patrol logs).
- **Legal Guide:** Quick access to Karnataka Sandalwood Policy 2022 guidelines for farmers.
- **Panic Alert:** High-urgency emergency security trigger for immediate situational awareness.

---

## 🛠️ Tech Stack
- **Platform:** Android (Java)
- **Database:** Room Persistence Library (Offline-first architecture)
- **Mapping:** Google Maps SDK for Android (High-Definition Satellite Mode)
- **Location:** Fused Location Provider Client
- **Charts:** MPAndroidChart
- **Architecture:** MVVM / ViewBinding

---

## 🚀 How to Run the App

### 1. Prerequisites
- **Android Studio** (Hedgehog | 2023.1.1 or newer recommended).
- **Google Maps API Key**: Ensure the **Maps SDK for Android** is enabled in your [Google Cloud Console](https://console.cloud.google.com/).

### 2. Configuration
1. Open the project in Android Studio.
2. Open `app/src/main/AndroidManifest.xml`.
3. Locate the `com.google.android.geo.API_KEY` meta-data tag.
4. Replace the value with your valid API Key:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```

### 3. Build & Deploy
1. Click **Sync Project with Gradle Files**.
2. Connect your Android device or start an emulator (API 21+).
3. Click **Run 'app'**.

---

## 🎨 Design Language
The app follows a strict **"Sandal/Wood"** palette for a premium agricultural feel:
- **Sandalwood Cream:** `#FDF5E6` (Backgrounds)
- **Deep Bark Brown:** `#5D4037` (Headings & Primary Cards)
- **Forest Green:** `#2E7D32` (Success States & Lush Markers)
- **Premium Gold:** `#FFD700` (Accents & Highlights)

---

## 📄 Project Status
Finalized for submission. All UI components, Map Estate previews, and Wealth Tracking features are fully implemented and optimized for a smooth, production-ready user experience.
