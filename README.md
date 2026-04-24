# ⚖️ CivicIQ — Indian Constitution Learning App

> **A production-quality Android app** built with Jetpack Compose (Material 3), MVVM + Clean Architecture, offline-first content, and premium UI/UX.

---

## 📱 Screenshots & Screens

| Screen | Description |
|---|---|
| 🌟 Splash | Animated logo with bounce + fade |
| 🏠 Home | Drawer + TabRow + HorizontalPager + Feature Cards |
| 📚 Flashcards | Swipe gestures + 3D flip animation |
| 🧠 Quiz | MCQ + countdown timer + score tracking |
| 🎡 Spin Wheel | Custom Canvas wheel with deceleration |
| 📖 Article Detail | Expandable sections + clean typography |
| 📊 Progress | DataStore-backed quiz history + flashcard stats |
| ⚙️ Settings | Toggles + dropdowns + WorkManager integration |
| ℹ️ About | App info + feature cards |

---

## 🏗 Project Structure

```
CivicIQ/
├── app/
│   ├── build.gradle.kts              # Dependencies (Compose BOM, DataStore, WorkManager…)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   └── civic_content.json    ← All app content (flashcards + quiz + spin topics)
│       ├── java/com/civiciq/app/
│       │   ├── MainActivity.kt        ← Single Activity host
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   └── Models.kt      ← All data classes
│       │   │   └── local/
│       │   │       ├── ContentRepository.kt    ← JSON loader (assets)
│       │   │       ├── DataStoreManager.kt     ← Preferences persistence
│       │   │       └── DailyReminderWorker.kt  ← WorkManager notification
│       │   ├── navigation/
│       │   │   ├── Screen.kt          ← Sealed class route definitions
│       │   │   └── NavGraph.kt        ← NavHost with slide transitions
│       │   └── ui/
│       │       ├── theme/
│       │       │   ├── Color.kt       ← Full color palette (gradients, accents…)
│       │       │   ├── Type.kt        ← Typography hierarchy
│       │       │   └── Theme.kt       ← MaterialTheme + AppSpacing
│       │       ├── components/
│       │       │   └── CommonComponents.kt   ← GlassCard, FeatureCard, StatCard…
│       │       └── screens/
│       │           ├── SplashScreen.kt
│       │           ├── HomeScreen.kt         ← Drawer + Tabs + HorizontalPager
│       │           ├── FlashcardScreen.kt    ← Swipe + flip animation
│       │           ├── QuizScreen.kt         ← Timer + MCQ + result screen
│       │           ├── SpinWheelScreen.kt    ← Canvas wheel + bottom sheet
│       │           ├── ProgressScreen.kt
│       │           ├── SettingsScreen.kt
│       │           ├── ArticleDetailScreen.kt
│       │           └── AboutScreen.kt
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml        ← SplashScreen + status bar config
│           └── xml/
│               ├── backup_rules.xml
│               └── data_extraction_rules.xml
└── gradle/
    └── libs.versions.toml
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK with API 35 installed

### Steps
```bash
# 1. Clone or copy this project
# 2. Open in Android Studio → "Open Existing Project"
# 3. Let Gradle sync (it downloads all deps automatically)
# 4. Run on emulator (API 24+) or physical device
```

### Gradle Sync Notes
All dependencies use **Compose BOM 2024.12.01** — no version conflicts.
```
androidx.compose:compose-bom:2024.12.01
```

---

## 📦 Key Dependencies

```kotlin
// Compose BOM (manages all Compose versions)
platform("androidx.compose:compose-bom:2024.12.01")

// Navigation Compose
"androidx.navigation:navigation-compose:2.8.5"

// DataStore Preferences
"androidx.datastore:datastore-preferences:1.1.1"

// WorkManager (daily reminders)
"androidx.work:work-runtime-ktx:2.10.0"

// Gson (JSON parsing)
"com.google.code.gson:gson:2.11.0"

// Splash Screen API
"androidx.core:core-splashscreen:1.0.1"

// Permissions (Accompanist)
"com.google.accompanist:accompanist-permissions:0.36.0"
```

---

## 🎨 Design System

### Color Palette
```kotlin
NavyDeep       = #0A0E27  // Background
ElectricBlue   = #4F7FFF  // Primary accent
PurpleAccent   = #7C5CFC  // Secondary
CyanGlow       = #00D4FF  // Tertiary
EmeraldGreen   = #00D68F  // Success
GoldAccent     = #FFBB33  // Warning/Tips
ErrorColor     = #FF5C5C  // Error
```

### Category Colors
```kotlin
Legislature → ElectricBlue  (#4F7FFF)
Executive   → PurpleAccent  (#7C5CFC)
Judiciary   → EmeraldGreen  (#00D68F)
```

### Glassmorphism Cards
```kotlin
// Semi-transparent white overlay with blur border
background: linearGradient(0x26FFFFFF → 0x0DFFFFFF)
border: linearGradient(0x40FFFFFF → 0x10FFFFFF)
cornerRadius: 20dp
```

---

## 🧠 Architecture

```
┌─────────────────────────────────────────┐
│              UI Layer                    │
│  Screens (Compose) + Components         │
│  ← collectAsStateWithLifecycle()        │
├─────────────────────────────────────────┤
│           ViewModel Layer               │
│  (State hoisted in Screen composables)  │
│  ← StateFlow / MutableState            │
├─────────────────────────────────────────┤
│            Data Layer                   │
│  ContentRepository  ← assets/*.json     │
│  DataStoreManager   ← Preferences       │
│  DailyReminderWorker ← WorkManager      │
└─────────────────────────────────────────┘
```

> **Note:** Given the app is offline-only with simple data flow, ViewModels are kept lean — state is hoisted directly in Composables using `remember {}` and persisted via DataStoreManager. This avoids over-engineering for an educational app.

---

## ✨ Animation Inventory

| Feature | Animation |
|---|---|
| Splash logo | `scaleIn` + `spring(DampingRatioMediumBouncy)` |
| Loading dots | `infiniteRepeatable` + `RepeatMode.Reverse` |
| Card press | `animateFloatAsState` scale to 0.96f |
| Tab switch | `animateColorAsState` + `animateFloatAsState` |
| Flashcard flip | `animateFloatAsState` rotationY 0→180 |
| Progress bars | `animateFloatAsState` + tween(600) |
| Quiz reveal | `AnimatedVisibility` slideInVertically |
| Spin wheel | `animateFloatAsState` + CubicBezierEasing deceleration |
| Screen transitions | `slideInHorizontally` + `fadeIn` |
| Result screen | `scaleIn` + spring bounce |
| Settings expand | `expandVertically` + `shrinkVertically` |
| Shimmer loading | `infiniteRepeatable` LinearGradient sweep |

---

## 📊 Content Structure (civic_content.json)

```json
{
  "legislature": {
    "flashcards": [ { id, frontTitle, frontSummary, backExplanation, backExample, category } ],
    "quiz":       [ { id, question, options[], correctAnswer, explanation } ],
    "spinTopics": [ "Article 79", "Lok Sabha", ... ]
  },
  "executive": { ... },
  "judiciary":  { ... }
}
```

**To add content:** Simply edit `app/src/main/assets/civic_content.json`.  
No code changes needed — `ContentRepository` handles the rest.

---

## 🔔 Daily Reminders

The `DailyReminderWorker` uses **WorkManager** with `PeriodicWorkRequest` (24h interval).

- Enable/disable via Settings → Daily Reminder toggle
- Pick reminder time from 6 AM – 10 PM
- Notification channel: `civiciq_daily_reminder`
- Requires `POST_NOTIFICATIONS` permission (Android 13+)

---

## 💾 DataStore Keys

| Key | Type | Purpose |
|---|---|---|
| `daily_reminder_enabled` | Boolean | Reminder toggle |
| `preferred_category` | String | Default tab |
| `quiz_history_json` | String (JSON) | Quiz records list |
| `flashcard_progress_json` | String (JSON) | Per-category seen count |
| `total_quizzes` | Int | Counter |
| `current_streak` | Int | Daily streak |
| `last_session_date` | Long | For streak calculation |
| `notification_hour` | Int | Reminder hour (0–23) |

---

## 🔧 Customization Tips

1. **Add more questions:** Edit `civic_content.json` — follow the existing schema
2. **Change colors:** Edit `ui/theme/Color.kt` — all screens inherit from theme
3. **Add a new tab/category:** Add entry to `tabs` list in `HomeScreen.kt` + add JSON section
4. **Add new screen:** Create composable → add `Screen` sealed class entry → add `composable {}` in `NavGraph.kt`

---

## 📋 Min SDK & Compatibility

| | Value |
|---|---|
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Language | Kotlin 2.1.0 |
| Compose | BOM 2024.12.01 |
| Architecture | MVVM + Clean |

---

*Built with ❤️ in India — CivicIQ v1.0*
