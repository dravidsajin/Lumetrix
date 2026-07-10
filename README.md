# Lumetrix

Lumetrix is an advanced Android digital wellbeing and productivity analytics app — a native Kotlin upgrade to the built-in Digital Wellbeing experience. It helps users understand screen time, build focus habits, and get actionable insights about how they use their device.

## Features (planned)

- Screen time and app usage analytics
- Focus mode and app blocking
- Notification analytics
- Productivity and focus scoring
- Weekly and monthly reports
- Backup and restore (Google Drive, encrypted)
- Home screen widgets
- AI-driven insights (future)

For the full product roadmap, see [docs/implementation_plan.md](docs/implementation_plan.md).

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Clean Architecture |
| DI | Hilt |
| Database | Room |
| Async | Coroutines, Flow |
| Background work | WorkManager, Foreground Services |

## Prerequisites

Before running the project locally, install:

1. **JDK 17** — required by the Android Gradle Plugin  
   Verify: `java -version`

2. **Android Studio** (latest stable recommended) with:
   - Android SDK Platform 36
   - Android SDK Build-Tools
   - Android Emulator (optional, for virtual device testing)

3. **Android SDK** — Android Studio installs this automatically.  
   The project expects `local.properties` to point at your SDK:

   ```properties
   sdk.dir=/path/to/Android/sdk
   ```

   Android Studio creates this file on first open. Do not commit it.

4. **Physical device or emulator** running **Android 8.0 (API 26)** or higher

## Current folder structure

The project is in **Phase 1** (foundation). It uses a single `app` module with package-based layers. Gradle modules listed below will be added in later phases.

```text
Lumetrix/
├── app/                                    # Main Android application module
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/lumetrix/statsmanager/
│       │   ├── StatsManagerApp.kt          # Hilt application class
│       │   ├── MainActivity.kt             # Compose entry activity
│       │   ├── data/                       # Local data sources, Room (upcoming)
│       │   ├── domain/                     # Use cases and domain models (upcoming)
│       │   ├── di/                         # Hilt modules (upcoming)
│       │   └── ui/
│       │       ├── home/                   # Home / dashboard UI
│       │       └── theme/                  # Material 3 theme
│       └── res/                            # Strings, icons, themes
├── docs/
│   └── implementation_plan.md              # Full architecture and roadmap
├── gradle/
│   ├── libs.versions.toml                  # Centralized dependency versions
│   └── wrapper/                            # Gradle wrapper
├── build.gradle.kts                        # Root Gradle config
├── settings.gradle.kts                     # Module includes
├── gradle.properties
├── gradlew                                 # Unix/macOS/Linux build script
├── gradlew.bat                             # Windows build script
└── README.md
```

## Planned Gradle modules

These modules are defined in the implementation plan and will be introduced as the app grows beyond the MVP foundation.

```text
Lumetrix/
├── app/                                    # App shell, navigation, DI wiring
│
├── core/
│   ├── database/                           # Room entities, DAOs, migrations
│   ├── network/                            # API clients (backup, sync)
│   ├── utils/                              # Shared helpers
│   ├── permissions/                        # Usage stats, accessibility flows
│   ├── encryption/                         # AES-256 for backups/exports
│   └── analytics/                          # Event processing and scoring
│
├── feature-dashboard/                      # Screen time summary, charts, goals
├── feature-tracking/                       # Real-time usage and sessions
├── feature-focus-mode/                     # Focus sessions, Pomodoro, deep work
├── feature-app-blocker/                  # App blocking enforcement
├── feature-notifications/                  # Notification frequency analytics
├── feature-reports/                        # Weekly/monthly reports, PDF export
├── feature-backup/                         # Google Drive backup and restore
├── feature-settings/                       # App preferences and permissions
├── feature-ai-insights/                    # On-device ML recommendations (future)
│
├── services/
│   ├── accessibility/                      # Active app detection, blocking
│   ├── usage-stats/                        # UsageStatsManager integration
│   ├── notification-listener/              # NotificationListenerService
│   └── foreground-monitor/                 # Persistent monitoring service
│
└── widgets/                                # Home screen widgets
```

### Development phases

| Phase | Focus |
|-------|--------|
| 1 | Project setup, Compose, Hilt, Room *(current)* |
| 2 | Usage tracking engine (UsageStats, screen time, unlocks) |
| 3 | Dashboard, charts, daily summaries |
| 4 | Focus mode, app blocking, productivity scoring |
| 5 | Backup system (Google Drive, encryption) |
| 6 | AI insights and recommendations |

## Run locally

### Option A — Android Studio (recommended)

1. **Clone or open the project**
   ```bash
   cd /path/to/Kotlin/Lumetrix
   ```

2. **Open in Android Studio**  
   `File → Open` and select the `Lumetrix` folder (the directory that contains `settings.gradle.kts`).

3. **Sync Gradle**  
   Wait for the Gradle sync to finish. Android Studio will download dependencies on first open.

4. **Set up a device**
   - **Emulator:** `Device Manager → Create Device` (API 26+), then start it  
   - **Physical device:** Enable **Developer options** and **USB debugging**, connect via USB

5. **Run the app**  
   Select your device and click **Run** (▶), or press `Ctrl+R` (macOS) / `Shift+F10` (Windows/Linux).

### Option B — Command line

1. **Navigate to the project root**
   ```bash
   cd /path/to/Kotlin/Lumetrix
   ```

2. **Ensure `local.properties` exists** with your SDK path:
   ```properties
   sdk.dir=/Users/YOUR_USER/Library/Android/sdk
   ```
   On Linux, typically: `sdk.dir=/home/YOUR_USER/Android/Sdk`  
   On Windows: `sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk`

3. **Build a debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on a connected device or running emulator**
   ```bash
   ./gradlew installDebug
   ```

5. **Launch from the device**  
   Open the **Lumetrix** app from the app drawer.

### Useful Gradle commands

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew installDebug       # Install debug build on device
./gradlew clean              # Remove build outputs
./gradlew :app:dependencies  # List app module dependencies
```

## App configuration

| Setting | Value |
|---------|--------|
| Application ID | `com.lumetrix.statsmanager` |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| Version | `0.1.0` |

## Permissions (upcoming)

Later phases will require special Android permissions and services:

- `PACKAGE_USAGE_STATS` — app usage and screen time
- Accessibility Service — focus mode and app blocking
- Notification Listener — notification analytics
- Foreground Service — real-time monitoring

These are not wired in yet; they will be added with the tracking and focus modules.

## Contributing

Work is organized around the phases in [docs/implementation_plan.md](docs/implementation_plan.md). Prefer small, focused changes that match the current phase before adding new modules.

## License

License not yet specified.
