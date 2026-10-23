# MatrixTracker

**MatrixTracker** is a high-performance, offline-first Android habit and goal tracking application built with Jetpack Compose, Room Database, and Kotlin. Designed with the familiar 2D spreadsheet layout of Excel/Google Sheets, MatrixTracker lets you visualize and mark habits across daily, weekly, and monthly time horizons with instant responsiveness.

---

## 🌟 Key Features

- **2D Synchronized Matrix Grid**: High-performance spreadsheet grid with frozen top-left corner, locked task titles, and synchronized bi-directional horizontal and vertical scrolling.
- **Multi-Cadence Boards**: Create custom boards for **Daily**, **Weekly**, or **Monthly** tracking routines.
- **Multi-Cell Drag Gestures**: Mark multiple days in a single gesture by dragging horizontally across cell rows with real-time selection highlights and haptic tick feedback.
- **Header Quick-Actions**:
  - Long-press column date headers to *"Mark All Done"* or *"Clear All"*.
  - Long-press habit headers to *"Mark Past 7 Days Done"*.
- **Consistency & Momentum Dashboard**:
  - Real-time monthly score ring and pacing metrics based on elapsed days.
  - Active habit streak tracking (neutral on skipped days so streaks don't break unfairly).
  - Best-performing weekday analytics (e.g., *"🏆 Best Day: Thursday (85%)"*).
- **Data Export & Backup**:
  - Export boards directly to CSV files for Excel/Sheets analysis.
  - Full structured JSON backup and restore.
- **Printable PDF Generator**: Renders standard A4 Landscape printable PDFs with print-friendly monochrome symbols and system Print Spooler / Wi-Fi printer integration.
- **Jetpack Glance Home Screen Widget**: Interactive widget displaying today's habits with single-tap background quick-checks.
- **Dark, Light & Modern AMOLED Themes**: Pitch-black `#000000` AMOLED theme with vibrant neon emerald, cyan, and crimson cell states.
- **Dynamic In-App Arabic Localization**: Full RTL layout and Arabic translation support with instant in-app locale switching.

---

## 🏗️ Architecture & Tech Stack

- **Language**: 100% Modern Kotlin 2.1
- **UI Framework**: Jetpack Compose & Material 3
- **Database**: Room 2.8 with KSP (Kotlin Symbol Processing) and schema export enabled
- **State Management**: Reactive `StateFlow` and Clean Architecture Repository pattern
- **Home Widget**: Jetpack Glance 1.2 (`glance-appwidget` & `glance-material3`)
- **PDF Rendering**: Native Android `PdfDocument` & `PrintManager` integration
- **Testing**: JUnit 4, Robolectric 4.17, and Coroutines Test (`31 unit tests passing`)

---

## 🚀 Building & Testing

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34 / 37)

### Build Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
./gradlew test
```

### Build Release APK
```bash
./gradlew assembleRelease
```
The optimized release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔐 CI/CD Keystore Persistence & Release Guide

To prevent Android's **"App not installed"** error during in-place APK updates, two requirements are strictly enforced:

### 1. Consistent Release Signing Keystore
Android rejects updates if the new APK's SHA-256 certificate signature differs from the installed APK. Follow these 3 steps ONCE to configure a persistent signing key in GitHub Actions:

#### Step 1: Generate a Keystore Locally
Run the following command on your local terminal:
```bash
keytool -genkeypair -v -keystore release.keystore -alias matrixreleasekey \
  -keyalg RSA -keysize 2048 -validity 10000 -storepass YourPassword123 -keypass YourPassword123
```

#### Step 2: Convert Keystore to Base64
- **Linux / macOS**:
  ```bash
  base64 -w 0 release.keystore > keystore_base64.txt
  ```
- **Windows PowerShell**:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) > keystore_base64.txt
  ```

#### Step 3: Add Repository Secrets in GitHub
Go to **GitHub Repository Settings → Secrets and variables → Actions → New repository secret**:
- `RELEASE_KEYSTORE_BASE64` : Paste content of `keystore_base64.txt`
- `RELEASE_KEYSTORE_PASSWORD` : `YourPassword123`
- `RELEASE_KEY_ALIAS` : `matrixreleasekey`
- `RELEASE_KEY_PASSWORD` : `YourPassword123`

---

### 2. Automated Version Code Auto-Increment
Android rejects in-place updates if the new APK's `versionCode` is less than or equal to the currently installed version.

When running `release.py`, the script automatically:
1. Reads the current `versionCode` from `app/build.gradle.kts`.
2. Increments `versionCode` by 1 (`versionCode = currentCode + 1`).
3. Bumps `versionName` to match the calculated release tag (`v1.0.X`).
4. Commits `app/build.gradle.kts`, tags the commit, and pushes to remote.

To publish a new automated release:
```bash
python release.py "feat: your release feature description"
```

---

## 📄 License
Licensed under the Apache License, Version 2.0.
