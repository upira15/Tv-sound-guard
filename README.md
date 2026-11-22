# TV SOUND GUARD — Android TV test project

Target: Xiaomi / Mi TV running Android TV or Google TV.

## What this first version does
- Attempts to create Android `Equalizer` and `BassBoost` audio effects on audio session `0` (the global output mix).
- Provides Normal, Speech, Movie and Night equalizer presets.
- Provides manual equalizer-band and bass-boost controls.
- Reports when global effects cannot be opened.

## Important limitations
This is a compatibility test and manual tone-control app, **not** a guaranteed system-wide sound repair tool. Many Xiaomi retail TV firmware builds do not let ordinary third-party apps attach effects to the global output mix. If the TV rejects the effects, the app cannot change sound from YouTube, HDMI/set-top boxes, or other apps.

This version does not implement real-time automatic loudness compression, adaptive noise reduction, speaker repair, or audio interception. Those require supported system audio-processing APIs, privileged firmware access, or external audio hardware. If sound fluctuates or crackles even in the TV's own menus, check the TV speaker/amp hardware and service settings too.

## Build the APK
1. On a Windows, Mac or Linux computer, install Android Studio (current stable) and Android SDK Platform 35.
2. Open this `TV_SOUND_GUARD` folder in Android Studio.
3. Use JDK 17 when prompted.
4. Allow Gradle sync to finish.
5. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Find the APK at `app/build/outputs/apk/debug/app-debug.apk`.

## Install on Xiaomi TV
1. Copy `app-debug.apk` to a USB drive, or transfer it to the TV.
2. On the TV, enable installation from unknown sources for the file manager you use (menu names vary by Android TV version).
3. Open the APK and install.
4. Launch **TV SOUND GUARD** from the TV app launcher.
5. Check the connection status at the top. Test a low-risk setting first and keep the TV volume low.

## If you only have an Android phone
This ZIP contains the complete Android Studio source project, not a prebuilt APK. Building a reliable APK from a phone alone is not supported by the standard Android Studio workflow. You can open the project on a computer with Android Studio, or use a trusted Android build environment that supports Gradle projects. Do not install APKs from unknown websites.

## Troubleshooting
- “Global audio control is blocked or unsupported”: this is a TV firmware restriction, not a setting you can fix inside this app.
- If controls connect but do not affect YouTube/HDMI, the TV may be applying effects only to a limited audio path.
- If crackling/noise remains at low volume across all sources, disconnect HDMI devices and test the TV's built-in apps. Persistent distortion may indicate speaker or amplifier damage.

## Build the APK using GitHub from your phone (no laptop required)
The project includes `.github/workflows/build-apk.yml`.
1. Create a new GitHub repository and upload the contents of this project ZIP (keep the folder structure).
2. Open the repository in your phone browser and select **Actions**.
3. Open **Build TV SOUND GUARD APK**. If it has not run automatically, tap **Run workflow**.
4. Wait for the workflow to finish successfully.
5. Open the completed workflow run, scroll to **Artifacts**, and download `TV-SOUND-GUARD-debug-APK`.
6. Extract the downloaded artifact ZIP to get `app-debug.apk`, then transfer/install it on the TV.

GitHub's mobile website layout may vary. The workflow builds the APK in GitHub's hosted runner; it does not mean the app has been tested on your particular TV.
