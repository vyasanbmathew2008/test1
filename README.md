# Rocky Port Scanner — Android

A native Android TCP connect port scanner built in Java. This repository now contains the Android Studio application (not the earlier terminal-only Maven program).

## Features

- **Common ports:** one-tap scan of frequently used service ports (FTP, SSH, DNS, HTTP/S, SMB, RDP, databases, VNC, and more).
- **Custom ports:** single ports, comma-separated lists, and inclusive ranges (for example: `22,80,443,8000-8100`).
- **Scan history:** the latest 50 scan summaries and CSV reports are stored locally on the device.
- **Export:** save a current or historical scan report as CSV using Android's document picker.
- Background TCP checks, progress reporting, stop control, and service-name hints.

## Open and build

1. Open this repository in Android Studio (Hedgehog or newer).
2. Allow Gradle sync to complete. Android Gradle Plugin 8.9.2 and compile SDK 35 are configured.
3. Select the `app` run configuration and run it on an Android device or emulator.

Requirements: Android Studio with Android SDK 35 installed; JDK 17 for the Android Gradle Plugin.

## How to use

1. Enter a hostname or IP address.
2. Tap **Common ports** or enter custom ports/ranges and tap **Scan custom**.
3. Open ports appear as they are detected. Use **Stop scan** to cancel.
4. Open **History** to review previous scans; tap an entry to export it.
5. Use **Export latest** to save the most recent report.

## Technical notes

- Package/application ID: `com.rocky.portscanner`
- Language: Java 8 source compatibility
- Minimum Android version: API 23 (Android 6.0)
- Target SDK: API 35
- Uses ordinary TCP socket connections; no raw sockets, packet crafting, root access, or external dependencies.
- A failed connection or timeout is shown as not open; it does not prove a port is filtered.
- Scan cap: 4,096 ports per run; worker pool: 32; connect timeout: 900 ms.
- Scan history and report contents are stored locally in app preferences.

## Responsible use

Only scan devices and networks that you own or are explicitly authorized to assess. Keep scan rates reasonable and follow the target owner's rules.
