#!/usr/bin/env sh
# Gradle Wrapper bootstrap note:
# This project ships gradle-wrapper.properties. Open the project in Android Studio
# to let it download the wrapper JAR and SDK, or run:
#   gradle wrapper --gradle-version 8.9
# then re-run ./gradlew
DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "gradle-wrapper.jar missing. Open in Android Studio (recommended) or run: gradle wrapper"
  exit 1
fi
exec java -jar "$JAR" "$@"
