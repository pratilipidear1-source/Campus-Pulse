# Campus Pulse 📍

> **See the campus pulse before you make the walk.**

Campus Pulse is a Kotlin + Jetpack Compose Android prototype rebuilt around the supplied **Campus Pulse Figma**.

## 🎨 Five Figma-aligned screens

1. **Onboarding — Editorial Pulse** — radial petal artwork, editorial headline, telemetry preview cards and step navigation.
2. **Home — Studio Live Radar** — greeting header, featured venue bento, Check In / Live Map tiles, live venue cards and community snapshot.
3. **Live Map — Aura Heatmap** — campus heatmap, crowd legend, live pins and nearby pulse list.
4. **Place Detail — Studio Green Library** — venue hero, optimal-window insight, crowd curve, floor breakdown and community buzz.
5. **Check-in Flow — Tactile Telemetry** — contextual backdrop, geofence status, three-state crowd selector, ambient tags, streak card and submit action.

## 🌈 Visual system

The implementation follows the Figma language: paper/cream background, dark ink typography, lavender hero surfaces, coral crowd alerts, sage low-crowd states, butter secondary cards, rounded editorial surfaces and the radial pulse/petal motif.

### App icon

A dedicated **Campus Pulse launcher icon** is included at `app/src/main/res/drawable/ic_campus_pulse_logo.xml` and uses the same lavender + coral + sage + ink palette and radial pulse geometry. The Android manifest points both `icon` and `roundIcon` to this mark.

## 🛠️ Tech stack

- Kotlin 2.0.21
- Jetpack Compose + Material 3
- Navigation Compose
- Android Gradle Plugin 8.7.3
- Compose BOM 2024.12.01
- Compile/Target SDK 35
- Minimum SDK 24
- GitHub Actions + Java 17 + Gradle 8.9

## 🤖 APK build

Every push to `main` runs `.github/workflows/android.yml`, builds `app-debug.apk`, and uploads **`campus-pulse-debug-apk`**.

## 📱 Prototype scope

The current release uses representative data. Authentication, real-time backend synchronization, occupancy sensors, GPS/geofencing, push notifications, moderation and production analytics remain future phases.

## 🔮 Roadmap

- Connect timestamped crowd reports to a backend.
- Add campus/student authentication.
- Add report freshness, moderation and confidence indicators.
- Add real-time updates.
- Add history/distance-aware “go now / wait” suggestions.
- Add tests, accessibility, privacy controls and release signing.
