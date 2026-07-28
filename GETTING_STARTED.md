# PurrCare - Cat Health Tracking App

A native Android app built with Kotlin, Jetpack Compose, and Room Database.

## Prerequisites (What You Need to Install)

### Step 1: Install Java JDK 17

**Windows:**
1. Go to https://adoptium.net/download/
2. Choose **OpenJDK 17 (LTS)**, your OS (Windows), and architecture (x64)
3. Download the `.msi` installer and run it
4. Check "Set JAVA_HOME variable" during installation
5. Verify by opening Command Prompt: `java -version` (should show 17.x.x)

**macOS:**
1. Open Terminal and install Homebrew if you do not have it:
   `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`
2. Install Java 17: `brew install openjdk@17`
3. Verify: `java -version` (should show 17.x.x)

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

### Step 2: Install Android Studio

1. Go to https://developer.android.com/studio
2. Click **Download Android Studio** (the large green button)
3. Run the installer:
   - **Windows**: Run the `.exe`, accept defaults, click Next through all screens
   - **macOS**: Drag Android Studio to the Applications folder
   - **Linux**: Extract the `.tar.gz` and run `./android-studio/bin/studio.sh`

4. When Android Studio first opens, it will launch the **Setup Wizard**:
   - Choose **Standard** installation type
   - Select your preferred theme (Dark/Light)
   - Click **Next** until it starts downloading SDK components
   - This takes 5-15 minutes depending on your internet speed
   - Click **Finish** when done

### Step 3: Open the PurrCare Project

1. Copy the `PurrCare` folder (this entire directory) to a location on your computer
2. Open Android Studio
3. Click **Open** on the welcome screen (or File > Open)
4. Navigate to the PurrCare folder and select it
5. Click **OK**
6. Android Studio will detect it is a Gradle project. A notification appears at the bottom-right:
   **"Gradle project sync in progress..."**
7. Wait for the sync to complete (progress bar in the bottom bar). This downloads all dependencies the first time and may take 3-10 minutes.

### Step 4: Create a Virtual Device (Emulator)

1. In Android Studio, click the **Device Manager** icon on the right sidebar (it looks like a phone with a small Android icon)
2. Click **Create device**
3. Choose a phone model (e.g., **Pixel 6**) and click **Next**
4. Select a system image:
   - Click the **Download** link next to **Tiramisu (API 33)** or **UpsideDownCake (API 34)**
   - Wait for it to download (this is ~1GB)
   - After download, select it and click **Next**
5. Name the AVD (e.g., "Pixel 6 API 34") and click **Finish**

### Step 5: Run the App

1. Look at the top toolbar of Android Studio. Next to the green play button (triangle icon), there is a dropdown
2. Make sure your virtual device (e.g., "Pixel 6 API 34") is selected
3. Click the **green Play button** (or press Shift+F10)
4. The emulator will launch (this may take 1-2 minutes the first time)
5. The PurrCare app will install and open automatically

### Using the App

When the app launches:
- **Home tab**: Shows your cat's profile info and quick stats. Empty until you create a profile.
- **Profile tab**: Tap to create/edit your cat's name, birth year, target weight, and notes
- **Log tab**: Tap to add a daily health log (weight, appetite, water intake, litter habits, notes)

### Troubleshooting Common Issues

| Problem | Solution |
|---------|----------|
| "SDK location not found" | Go to File > Project Structure > SDK Location and set the Android SDK path (usually `~/Android/Sdk` or `C:\Users\You\AppData\Local\Android\Sdk`) |
| "Gradle sync failed" | Go to File > Settings > Build Tools > Gradle > set Gradle JDK to **JDK 17** |
| Emulator stuck on Android logo | In Device Manager, click the down arrow next to the device > **Cold Boot Now** |
| "Installing APK" stuck | Click the red stop button, then run again |
| Out of disk space on C: drive (Windows) | Move `.android` folder: create a folder on D: drive, then in Environment Variables add `ANDROID_SDK_HOME=D:\AndroidSDK` |

## Build Without Android Studio (Terminal Only)

If you only want to build the APK without Android Studio:

```bash
# Windows (Command Prompt)
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

Copy it to your phone and install it (you may need to enable "Install from unknown sources" in Settings).

## Project Structure

```
PurrCare/
├── app/
│   ├── build.gradle.kts          # App-level dependencies
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml   # App declaration
│       ├── res/                  # Resources (icons, themes, strings)
│       └── java/com/purrcare/
│           ├── MainActivity.kt             # Entry point
│           ├── PurrCareApplication.kt      # App class (DB singleton)
│           ├── data/
│           │   ├── entity/       # Room database tables
│           │   ├── dao/          # Database queries
│           │   └── database/     # Database class + converters
│           └── ui/
│               ├── navigation/   # Routes + bottom navigation
│               ├── screen/       # Composable screens
│               └── viewmodel/    # State management
├── build.gradle.kts              # Project-level build config
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
├── gradlew / gradlew.bat         # Gradle wrapper scripts
└── gradle/
    ├── libs.versions.toml        # Dependency version catalog
    └── wrapper/
        └── gradle-wrapper.properties
```
