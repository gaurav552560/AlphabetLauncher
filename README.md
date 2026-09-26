# Alphabet Launcher

A minimal Android home screen built with **Kotlin** and **Jetpack Compose**. The launcher displays the current time/date, favourite apps, and an interactive curved A–Z alphabet bar for quickly browsing installed apps.

## Features

### Core

* Live clock and date.
* 5–7 apps on the home screen.
* Vertical A–Z alphabet bar with a star at the top and dot at the bottom.
* Real installed launchable apps loaded through `PackageManager`.
* Android 11+ package visibility support.
* Curved alphabet animation following the finger.
* Enlarged selected-letter bubble beside the finger.
* Apps filtered by selected starting letter and sorted alphabetically.
* Clear `No Apps` state for empty letters.
* Release animation returning the alphabet bar to its resting position.
* Tapping an app launches it.
* App list is cached outside the touch path.

## Bonus Features

* Default launcher support.
* Haptic `CLOCK_TICK` feedback.
* Spring-based release animation.
* Swipe-up search with keyboard and live filtering.
* Long-press favourites with persistent storage.
* Automatic refresh when apps are installed, removed, or changed.
* Empty alphabet letters are dimmed.
* Unit-test support.
* System light/dark theme support.

## Tech Stack

* Kotlin
* Jetpack Compose
* Material 3
* Android PackageManager
* AndroidX DataStore Preferences
* Gradle Kotlin DSL

## Requirements

* Android Studio.
* Android SDK supporting API 36.
* JDK 21 recommended for the current Gradle setup.
* Android device or emulator running API 24 or newer.

## Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync.
4. Connect an Android device or start an emulator.
5. Run the `app` configuration.

### Build

Windows:

```bash
gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

### Run tests

Windows:

```bash
gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

## Curve Animation

The alphabet is rendered as a vertical Compose `Column`.

During a vertical drag, the finger's Y position is converted into a normalized value between `0` and `1`. Every alphabet item also has a normalized vertical position.

The distance between the finger and each letter is used with a cosine falloff to calculate the horizontal offset. Letters closest to the finger move furthest toward the centre, while letters farther away move less.

This creates the curved/bulged alphabet effect without using a third-party curve-animation library.

The selected letter is calculated directly from the touch position. The installed application list is grouped by its first character, so changing the selected letter updates the displayed list without querying `PackageManager` during every touch event.

When the finger is released, the alphabet returns to its resting position using spring-based animation.

## Favourites and Persistence

Long-pressing an app adds it to or removes it from the home-screen favourites.

Favourite package names are stored using AndroidX DataStore Preferences so the selection survives application restarts.

The application list is also refreshed when packages are installed, removed, or changed.

Because favourites are stored by package name, an uninstalled favourite disappears while the application is absent and can automatically reappear when the same package is installed again.

## Libraries

| Library                        | Version        | Purpose                                     |
| ------------------------------ | -------------- | ------------------------------------------- |
| AndroidX Core KTX              | 1.10.1         | Kotlin-friendly Android core APIs           |
| AndroidX Activity Compose      | 1.8.0          | Compose integration with Android Activity   |
| AndroidX Lifecycle Runtime KTX | 2.6.1          | Android lifecycle runtime support           |
| Jetpack Compose                | BOM 2026.02.01 | Compose UI toolkit and dependency alignment |
| Material 3                     | BOM-managed    | UI components and system theming            |
| AndroidX DataStore Preferences | 1.1.7          | Persistent favourite-app storage            |
| JUnit                          | 4.13.2         | Local unit testing                          |
| AndroidX Test JUnit            | 1.1.5          | Android testing support                     |
| Espresso                       | 3.5.1          | Android UI/instrumentation testing support  |

## Testing

The project contains unit-test support for launcher logic.

The test suite was verified with:

```bash
gradlew.bat test
```

Result:

```text
BUILD SUCCESSFUL
```

## AI / Tutorial Disclosure

ChatGPT was used as a development assistant during this assignment for implementation guidance, debugging, code review, Android/Jetpack Compose explanations, and documentation assistance.

It was also used to help troubleshoot Gradle configuration, Compose touch handling, launcher behavior, search behavior, and persistence issues.

The final implementation was integrated, reviewed, tested, and run by the developer.

No third-party tutorial code was copied for the core curve-animation implementation. The curve calculation and touch-position-to-letter logic were implemented directly in this project.

## Project Structure

```text
app/
├── src/main/java/com/pmgaurav/alphabetlauncher/
│   ├── AlphabetLauncher.kt
│   ├── AppInfo.kt
│   ├── CurvedAlphabet.kt
│   ├── FavouriteStore.kt
│   ├── LetterUtils.kt
│   ├── MainActivity.kt
│   └── ui/theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── src/test/java/com/pmgaurav/alphabetlauncher/
    └── LetterUtilsTest.kt
```

`LetterUtils.kt` contains the reusable alphabet/letter-related logic used by the launcher, while `LetterUtilsTest.kt` contains the corresponding unit tests.

## Notes

The project intentionally keeps the core launcher interaction lightweight. The alphabet curve and touch logic are implemented directly with Jetpack Compose rather than relying on a third-party launcher or curve-animation library.
