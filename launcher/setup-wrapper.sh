#!/bin/bash
# Generate gradle-wrapper.jar for local builds.
# Run this once from the launcher/ directory: bash setup-wrapper.sh
set -e

GRADLE_VERSION="8.4"
GRADLE_DIR="/tmp/gradle-${GRADLE_VERSION}"

if [ ! -d "$GRADLE_DIR" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d /tmp/
    rm /tmp/gradle.zip
fi

echo "Generating gradle-wrapper.jar..."
"$GRADLE_DIR/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION"

echo "Done. gradle-wrapper.jar is ready."
ls -lh gradle/wrapper/gradle-wrapper.jar
