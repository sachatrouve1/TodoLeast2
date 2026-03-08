#!/bin/sh

set -e

APK_DIR="/tmp/todoleast-apk"
APK_NAME="app-debug.apk"

if ! command -v gh &> /dev/null; then
    echo "Erreur: GitHub CLI (gh) n'est pas installé"
    echo "Installe-le avec: sudo pacman -S github-cli"
    exit 1
fi

if ! command -v adb &> /dev/null; then
    echo "Erreur: ADB n'est pas installé ou pas dans le PATH"
    exit 1
fi

DEVICE_COUNT=$(adb devices | grep -c -E "device$" || true)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "Erreur: Aucun appareil Android connecté"
    echo "Connecte ton téléphone en USB et active le débogage USB"
    exit 1
fi

rm -rf "$APK_DIR"
mkdir -p "$APK_DIR"

echo "Téléchargement de l'APK depuis GitHub Actions..."
gh run download --name todoleast-debug --dir "$APK_DIR"

APK_PATH=$(find "$APK_DIR" -name "*.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    echo "Erreur: Aucun APK trouvé dans l'artifact"
    exit 1
fi

echo "APK trouvé: $APK_PATH"
echo "Installation sur l'appareil..."
adb uninstall com.app.todoleast
adb install -r "$APK_PATH"

echo "Installation terminée!"
echo "Lancement de l'application..."
adb shell am start -n com.app.todoleast/.MainActivity

rm -rf "$APK_DIR"
echo "Done!"
