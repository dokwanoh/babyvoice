# Baby Voice Bridge / 아기 브리핑

Android companion app for Korean-speaking parents and caregivers. The first complete path is the offline demo flavor, with mock data, briefing voice playback, one-shot speech recognition, deep links, and tablet-first UI.

## Current build choices

- `minSdk`: 29
- `compileSdk`: 36
- `targetSdk`: 36
- Android Gradle Plugin: 9.1.1
- Gradle: 9.6.1
- Kotlin: 2.3.20
- Compose BOM: 2026.06.01

Reasoning:
- API 36 matches the highest stable platform installed in the local SDK setup and keeps the selected AndroidX stack compatible with the build.
- Kotlin 2.3.20 and the selected AndroidX stack are current stable releases from the official Maven metadata at implementation time.
- JDK 17 is the safest supported runtime for the chosen Android Gradle plugin line.

## Build flavors

- `demo`: offline mock data only
- `lab`: export-file scaffolding, notification diagnostics, and a user-enabled accessibility widget reader for the visible BabyTime widget
- `production`: partner API scaffold only, no unofficial BabyTime integration

## Main features

- Korean briefing voice playback with Android `TextToSpeech`
- One-shot speech recognition with Korean intent parsing
- Voice-assistant friendly launcher behavior: opening the app through the normal launcher intent immediately starts the full briefing
- Launcher aliases for voice recognition: `아기 상태`, `베이비 브리핑`, `Baby Voice`, `아기 기록`, and `육아 기록` all open the same briefing flow
- Demo-only personal-test launcher alias: `베이비 타임`
- Demo-only personal-test shortcut/query candidate: `베이비 타임`
- Lab-only BabyTime notification bridge: reads only user-authorized notifications from `yducky.application.babytime`
- Lab-only BabyTime widget reader: reads the visible BabyTime widget tree after the accessibility service is enabled on the owned tablet
- Deep links:
  - `babyvoice://briefing`
  - `babyvoice://last-feeding`
  - `babyvoice://next-feeding`
  - `babyvoice://last-wake`
  - `babyvoice://next-sleep`
  - `babyvoice://last-diaper`
- Demo scenario screen for normal, stale, empty, syncing, permission, and error states
- Static launcher shortcut for briefing
- Quick Settings Tile that opens the briefing deep link
- Local-first caching and explicit provenance per field

## What this repository does not do

- No BabyTime APK reverse engineering
- No unofficial BabyTime API guessing
- No unauthorized accessibility scraping, OCR, or MediaProjection collection
- No traffic interception or certificate bypass
- No hidden account collection
- No medical recommendation generation

## Run

If you have the Android SDK installed and the wrapper present:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDemoDebug
```

## ADB checks

After installing the demo build:

```bash
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.MainActivity
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyStatusLauncherAlias
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyBriefingLauncherAlias
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.BabyVoiceLauncherAlias
adb shell am start -a android.intent.action.VIEW -d "babyvoice://briefing" com.babyvoice.bridge.demo.debug
adb shell am start -a android.intent.action.VIEW -d "babyvoice://last-feeding" com.babyvoice.bridge.demo.debug
adb shell am start -a android.intent.action.VIEW -d "babyvoice://next-feeding" com.babyvoice.bridge.demo.debug
adb shell am start -a android.intent.action.VIEW -d "babyvoice://last-wake" com.babyvoice.bridge.demo.debug
adb shell am start -a android.intent.action.VIEW -d "babyvoice://next-sleep" com.babyvoice.bridge.demo.debug
adb shell am start -a android.intent.action.VIEW -d "babyvoice://last-diaper" com.babyvoice.bridge.demo.debug
```

Live voice check on the Galaxy Tab A11+:

```text
오케이 구글, 아기 상태 열어줘
```

This phrase was verified on `SM-X230` with Gemini/Google Assistant. It opened the app and the app immediately read the briefing.

Lab widget-reader bench check:

1. Install the `labDebug` APK.
1. Enable the accessibility service `com.babyvoice.bridge.lab.debug/com.babyvoice.bridge.integration.assistant.BabyTimeWidgetAccessibilityService`.
1. Put the tablet on the home screen where the BabyTime widget is visible.
1. Launch `com.babyvoice.bridge.lab.debug/com.babyvoice.bridge.MainActivity`.
1. Confirm the UI shows `출처: BabyTime 위젯` and a briefing line sourced from the widget.

Quick Settings Tile bench check:

```bash
adb shell cmd statusbar add-tile com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.integration.assistant.BriefingTileService
adb shell cmd statusbar click-tile com.babyvoice.bridge.demo.debug/com.babyvoice.bridge.integration.assistant.BriefingTileService
```

## Architecture at a glance

The implementation is intentionally consolidated into one Android application module for the first pass, while preserving the requested boundaries in package structure:

- `core/model`
- `core/common`
- `core/voice`
- `core/ui`
- `data/mock`
- `data/export`
- `data/notification`
- `data/partner-api`
- `feature/briefing`
- `feature/settings`
- `integration/assistant`

This keeps the first build small enough to finish, while still separating providers, voice, UI, and integration code.

## Documentation

- [`docs/product-scope.md`](docs/product-scope.md)
- [`docs/assumptions.md`](docs/assumptions.md)
- [`docs/architecture.md`](docs/architecture.md)
- [`docs/data-provenance.md`](docs/data-provenance.md)
- [`docs/privacy-threat-model.md`](docs/privacy-threat-model.md)
- [`docs/test-plan.md`](docs/test-plan.md)
- [`docs/bixby-setup-ko.md`](docs/bixby-setup-ko.md)
- [`docs/app-actions.md`](docs/app-actions.md)
- [`docs/experimental-default-assistant.md`](docs/experimental-default-assistant.md)
- [`docs/appfunctions-future.md`](docs/appfunctions-future.md)
- [`docs/simfler-integration-request-ko.md`](docs/simfler-integration-request-ko.md)
- [`docs/proposed-partner-api.yaml`](docs/proposed-partner-api.yaml)
