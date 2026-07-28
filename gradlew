#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$APP_HOME/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/Xevaryth/Mysticism/2d425ac722ab9e2963d53d9048b4df7792501c53/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading the Gradle wrapper bootstrap..."
    mkdir -p "$WRAPPER_DIR"

    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error \
            "$WRAPPER_URL" --output "$WRAPPER_JAR" || exit 1
    elif command -v wget >/dev/null 2>&1; then
        wget --quiet "$WRAPPER_URL" --output-document="$WRAPPER_JAR" || exit 1
    else
        echo "curl or wget is required for the first Gradle launch." >&2
        exit 1
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" -Xmx64m -Xms64m \
    -Dorg.gradle.appname=gradlew \
    -classpath "" \
    -jar "$WRAPPER_JAR" "$@"
