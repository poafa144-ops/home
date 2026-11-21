#!/usr/bin/env bash
# Debloat an Android TV / Google TV over ADB, no root needed.
# Uses "pm uninstall --user 0", which removes the app for your user only.
# It is reversible: `adb shell cmd package install-existing <package>` brings an app back,
# and a factory reset restores everything.
#
# Usage:
#   ./debloat.sh <TV_IP>          # e.g. ./debloat.sh 192.168.1.50
#   ./debloat.sh <TV_IP> --dry    # only show what would be removed
#
# On the TV: Settings > Device Preferences > About > tap "Build" 7 times,
# then Developer options > enable "USB debugging" / "Network debugging".
# Accept the prompt on the TV the first time you connect.

set -u
IP="${1:-}"
DRY="${2:-}"
[ -z "$IP" ] && { echo "Usage: $0 <TV_IP> [--dry]"; exit 1; }

adb connect "$IP:5555" >/dev/null
adb -s "$IP:5555" get-state >/dev/null 2>&1 || { echo "Cannot reach TV. Check IP and debugging switch."; exit 1; }

# Safe-to-remove candidates. Packages that do not exist on your TV are skipped.
# Review this list before running; delete any line you want to keep.
PACKAGES=(
  # Google extras
  com.google.android.videos            # Google TV (movies) store
  com.google.android.music
  com.google.android.play.games
  com.google.android.apps.mediashell.leanback
  com.google.android.feedback
  com.google.android.partnersetup
  com.google.android.tvrecommendations
  com.google.android.leanbacklauncher.recommendations
  # Stock launcher (only after Clean Home is installed and set as default)
  # com.google.android.tvlauncher
  # com.google.android.apps.tv.launcherx
  # Common pre-installed junk on many brands
  # com.netflix.mediaclient
  # com.amazon.amazonvideo.livingroom
  # com.disney.disneyplus
  # com.hotstar.android
  # in.startv.hotstar
  # com.spotify.tv.android
  com.facebook.katana
  com.facebook.appmanager
  com.facebook.services
  com.facebook.system
  com.android.browser
  com.android.gallery3d
  com.android.music
  com.android.email
)

for pkg in "${PACKAGES[@]}"; do
  pkg="${pkg%%#*}"; pkg="$(echo "$pkg" | xargs)"
  [ -z "$pkg" ] && continue
  if adb -s "$IP:5555" shell pm list packages "$pkg" | grep -q "package:$pkg$"; then
    if [ "$DRY" = "--dry" ]; then
      echo "[dry] would remove $pkg"
    else
      printf 'Removing %s ... ' "$pkg"
      adb -s "$IP:5555" shell pm uninstall --user 0 "$pkg" 2>&1 | tr -d '\r'
    fi
  fi
done

echo "Done."
echo "Never remove: com.android.vending (Play Store), com.google.android.gms, com.google.android.gsf, com.android.settings"
