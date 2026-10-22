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
- **Dark & Light Jewel Tone Palette**: Theme-aware contrast colors for crisp visibility under both light and dark themes.

---

## 🏗️ Architecture & Tech Stack

- **Language**: 100% Modern Kotlin 2.1
- **UI Framework**: Jetpack Compose & Material 3
- **Database**: Room 2.8 with KSP (Kotlin Symbol Processing) and schema export enabled
- **State Management**: Reactive `StateFlow` and Clean Architecture Repository pattern
- **Home Widget**: Jetpack Glance 1.2 (`glance-appwidget` & `glance-material3`)
- **PDF Rendering**: Native Android `PdfDocument` & `PrintManager` integration
- **Testing**: JUnit 4, Robolectric 4.17, and Coroutines Test (`28 unit tests passing`)

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

## 📄 License
Licensed under the Apache License, Version 2.0.
