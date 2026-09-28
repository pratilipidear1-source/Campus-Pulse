# Campus Pulse 📍

> **See the campus pulse before you make the walk.**

Campus Pulse is a student-focused Android prototype for checking how crowded key campus spaces are right now and deciding whether to go, wait, or choose another place.

## ✨ Features
- Live crowd states for mess, library, canteen, gym, printer shop and labs
- One-tap check-in with crowd and ambient tags
- Campus live-map / heatmap presentation
- Place detail with crowd trends and floor breakdown
- Pulse Points, streaks and leaderboard presentation
- Figma-inspired editorial visual system

> **Prototype:** the current app uses representative/demo data and is not connected to a production campus backend, sensors, authentication or real-time database.

## 🛠️ Tech Stack
| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 + custom Compose styling |
| Navigation | Navigation Compose |
| Build | Gradle + Android Gradle Plugin 8.7.3 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.12.01 |
| Compile/Target SDK | 35 |
| Minimum SDK | 24 |
| CI | GitHub Actions |
| Java in CI | Temurin 17 |

## 🧭 App Flow
`Onboarding → Home → Live Map → Place Detail → Check-in → Home`

## 🎨 Figma → Android
Major flows implemented from the supplied Campus Pulse design:
- Onboarding — Editorial Pulse
- Home — Studio Live Radar
- Live Map — Aura Heatmap
- Place Detail — Studio Green Library
- Check-in Flow — Tactile Telemetry

The UI uses cream/paper backgrounds, dark ink typography, lavender/coral/sage/butter accents, rounded editorial cards and custom pulse graphics. Native Compose shapes/drawing are used where original Figma assets were unavailable.

## 📁 Project Structure
```
Campus-Pulse/
├── .github/workflows/android.yml
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/campuspulse/app/MainActivity.kt
│       └── res/values/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

## 🚀 Run Locally
Open the project in Android Studio, let Gradle sync, then run the `app` configuration on an emulator or Android device.

```bash
gradle assembleDebug
```

APK output:
```
app/build/outputs/apk/debug/app-debug.apk
```

## 🤖 GitHub Actions / APK Build
Every push to `main` runs `.github/workflows/android.yml`. The workflow installs Java 17, Gradle 8.9 and the Android SDK required for API 35, builds the debug APK, and uploads it as the artifact **`campus-pulse-debug-apk`**. It can also be started manually from the Actions tab.

## 🔐 Prototype Scope
Included: onboarding, live venue cards, crowd states, map presentation, place detail, trend visualization, floor breakdown, community UI, check-in flow, ambient tags, streak/Pulse Points presentation and navigation.

Not yet connected: campus authentication, Firebase/Supabase/custom backend, persistent accounts, real-time synchronization, occupancy sensors, GPS/geofencing, push notifications and production analytics.

## 🔮 Roadmap
1. Connect real timestamped crowd reports to a backend.
2. Add campus/student authentication.
3. Add report freshness, moderation, confidence and anti-spam controls.
4. Add real-time updates.
5. Add distance/history-aware crowd suggestions.
6. Add tests, accessibility, privacy controls, release signing and Play Store configuration.

## 🧪 Testing Checklist
- [ ] Onboarding opens correctly
- [ ] Home cards scroll and remain readable
- [ ] Bottom navigation works
- [ ] Map and place interactions work
- [ ] Check-in selector and tags work
- [ ] Check-in submission returns to the expected screen
- [ ] API 24+ behavior verified
- [ ] Accessibility/content descriptions added before production
- [ ] Release build signed before distribution

## 📦 APK Artifact
After a successful GitHub Actions run, download the debug APK from the workflow artifact named **`campus-pulse-debug-apk`**. Debug APKs are for testing, not Play Store production release.

## 🔒 Privacy Direction
A production version should minimize collected data, avoid exposing exact individual locations, aggregate crowd information where possible, define retention rules, protect authenticated endpoints, validate/rate-limit reports and give users control over account/data settings.

## 📄 License
No open-source license has been selected yet. Until one is added, treat the code as all rights reserved by its owner.

## 👩‍💻 Project
**Campus Pulse** — Android prototype built with Kotlin + Jetpack Compose from the supplied Campus Pulse product/design concept.
