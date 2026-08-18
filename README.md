# EzFit 🏋️

EzFit is an Android fitness tracking application designed to help users organize workouts, log exercises, and track their progress over time.

## Features

* **Workout Logging** — Record exercises, sets, reps, and weights.
* **Exercise Progress Tracking** — View your performance and progress for individual exercises.
* **Pre-built Workout Splits** — Includes:

  * Push Pull Legs (PPL)
  * Upper / Lower
  * Bro Split
* **Custom Workout Splits** — Create, edit, and delete personalized workout splits.
* **Exercise Library** — Choose from a collection of pre-configured exercises.
* **Progress Analytics** — Visualize exercise performance using charts.
* **Real-time Input Validation** — Validates workout data while logging exercises.
* **Offline Storage** — Workout data is stored locally using SQLite.
* **Material Design UI** — Clean Android interface built with Material Design components.

## Tech Stack

| Technology      | Usage                        |
| --------------- | ---------------------------- |
| Java            | Application development      |
| XML             | UI layouts                   |
| SQLite          | Local database               |
| Android SDK     | Mobile application framework |
| Material Design | User interface components    |
| MPAndroidChart  | Progress visualization       |
| Gradle          | Build system                 |

## Project Structure

```text
EzFit/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/vishnu/ezfit/
│           ├── res/
│           │   ├── drawable/
│           │   ├── layout/
│           │   ├── menu/
│           │   └── values/
│           └── AndroidManifest.xml
│
├── gradle/
│   └── wrapper/
│
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
└── gradlew.bat
```

## Getting Started

### Prerequisites

* Android Studio
* JDK 17
* Android SDK
* Android device or Android Emulator

### Installation

1. Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/EzFit.git
```

2. Open the project in Android Studio.

3. Allow Gradle to sync and download the required dependencies.

4. Connect an Android device or start an emulator.

5. Build and run the application.

## Database

EzFit uses **SQLite** for offline data storage.

The database stores information related to:

* Exercises
* Workout splits
* Workout sessions
* Sets
* Repetitions
* Weights
* Exercise progress

No external database server is required.

## Progress Analytics

EzFit uses **MPAndroidChart** to visualize workout performance and exercise progress, allowing users to see changes in their training performance over time.

## Custom Workout Splits

Users can create their own workout routines by selecting exercises from the exercise library. Custom splits can be edited or deleted as needed.

## Future Improvements

* Cloud synchronization
* User accounts
* Cross-device workout synchronization
* Backup and restore
* Additional analytics
* Personal records and achievement tracking
* Web version and cross-platform support

## License

This project is intended for educational and portfolio purposes.
