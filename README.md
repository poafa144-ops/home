# Clean Home: minimal Android TV launcher

A clean, dark home screen for Android TV / Google TV. It works with the stock remote:
D-pad moves between tiles, OK opens an app, long-press OK or MENU opens app info
(where you can uninstall). Play Store is always the first tile.

## What's included
- `app/` : Android Studio project (Kotlin, RecyclerView grid, leanback HOME intent filter)
- `scripts/debloat.sh` : removes unneeded preinstalled apps over ADB (reversible)

## 1. Build the APK
1. Install Android Studio and open the `CleanHome` folder.
2. Let Gradle sync, then Build > Build APK(s).
3. Output: `app/build/outputs/apk/debug/app-debug.apk`

(Command line: `gradle assembleDebug` if you have Gradle 8.7+ and the Android SDK.)

## 2. Install on the TV (no keyboard needed, factory remote only)
Easiest: put the APK on a USB stick, install a file manager from Play Store
(e.g. "File Commander" or "X-plore"), open the APK and install it.
Or over Wi-Fi with ADB: `adb connect <TV_IP>:5555 && adb install app-debug.apk`

## 3. Make it the default home
Press the Home button. Android TV asks which launcher to use. Pick Clean Home and
choose "Always". If it doesn't ask, go to Settings > Apps > Default apps > Home screen app.
Some TVs (e.g. certain Sony/TCL models) don't allow changing the launcher;
in that case open Clean Home from the app row.

## 4. Remove unnecessary apps
Option A (remote only): on any tile, hold OK or press MENU > Uninstall.
System apps usually show Disable instead; that is fine.

Option B (script): enable Developer options on the TV, turn on network debugging, then
`./scripts/debloat.sh <TV_IP> --dry` to preview and `./scripts/debloat.sh <TV_IP>` to run.
Edit the package list first. To undo one app:
`adb shell cmd package install-existing <package>`.

## Notes
- Play Store comes with Android TV / Google TV already. It is pinned first, and the pinned
  order is the `pinned` list in `MainActivity.kt`.
- Don't remove the stock launcher until Clean Home works as default.
- Fire TV, Samsung (Tizen) and LG (webOS) are not supported by this project.

## Build without Android Studio (GitHub Actions)
1. Create a free GitHub account and a new repository.
2. Upload the contents of this folder (including the hidden `.github` folder) to it, on the `main` branch.
3. Open the Actions tab > "Build APK" > Run workflow (it also runs on upload).
4. When it finishes (about 3-5 min), open the run and download the `CleanHome-apk` artifact. Unzip it to get `app-debug.apk`.
