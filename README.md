# MT5 Android Emulator

A standalone Android project for exploring a local Windows compatibility environment for the **MetaTrader 5 desktop terminal** on Android devices.

> **Current status:** early scaffold. The app can download the official MT5 Windows installer and offers an optional foreground keep-awake service. **Wine/Box64, a Windows root filesystem, graphics integration, and running Windows executables are not integrated yet.** Downloading the installer does not install or launch MT5.

## Target device
- Samsung Galaxy M53 5G (ARM64)
- Android 13+ target for initial development; test on the actual phone before relying on it.

## Included in this first scaffold
- Native Kotlin Android app and Material-style dark UI
- Download the official MT5 Windows installer to Android Downloads
- Open the official MT5 desktop download page
- Optional foreground notification + partial wake lock while the service is enabled
- GitHub Actions debug APK build

## Planned runtime work
1. Select and audit compatible open-source Wine/Box64 runtime sources and licenses.
2. Package or download ARM64 runtime assets safely; verify hashes and storage needs.
3. Create and manage a per-user Wine prefix (virtual `C:` drive).
4. Add a graphics/input bridge and launch a Windows executable in a container.
5. Install MT5 and verify terminal startup, network connectivity, and restart behavior.
6. Test battery/background behavior on the target Samsung phone.

Runtime archives can be large; they should not be committed directly to Git. Prefer version-pinned downloads with SHA-256 verification and clear license notices.

## Build
The GitHub Actions workflow builds a debug APK on pushes and via manual dispatch. Open **Actions → Build Android APK → Artifacts** to retrieve it after a successful run.

Local build requirements: JDK 17 and Gradle 8.9.

```sh
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Important limitations
- This is not yet a working Windows emulator and must not be described as MT5-ready until Wine/Box64 and the execution path are implemented and tested.
- Android may stop background work because of battery, memory, or manufacturer policy. A foreground service and wake lock reduce interruptions but cannot guarantee uninterrupted operation.
- Test unattended trading carefully. Do not rely on an untested phone setup for time-critical financial activity.
- The MetaTrader 5 terminal is proprietary software. This repository does not include MetaQuotes binaries.

## Official sources
- MT5 desktop download: https://www.metatrader.com/en/download/desktop
- MetaTrader 5 installation help: https://www.metatrader5.com/en/terminal/help/start_advanced/installation
- Android foreground services: https://developer.android.com/develop/background-work/services/foreground-services
- Winlator upstream reference: https://github.com/brunodev85/winlator
