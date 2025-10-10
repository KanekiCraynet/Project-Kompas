#!/bin/bash

echo "🧭 Building Kompas GPS App (Simple Version)..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

# Install dependencies
echo "📦 Installing dependencies..."
npm install --legacy-peer-deps

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Trying to find Android SDK..."
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
        echo "✅ Found Android SDK at: $ANDROID_HOME"
    elif [ -d "/opt/android-sdk" ]; then
        export ANDROID_HOME="/opt/android-sdk"
        echo "✅ Found Android SDK at: $ANDROID_HOME"
    else
        echo "❌ Android SDK not found. Please install Android SDK and set ANDROID_HOME"
        echo "   You can install it via:"
        echo "   sudo snap install android-studio --classic"
        echo "   Or download from: https://developer.android.com/studio"
        exit 1
    fi
fi

# Add Android tools to PATH
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Clean previous builds
echo "🧹 Cleaning previous builds..."
cd android
if [ -f "./gradlew" ]; then
    ./gradlew clean
else
    echo "⚠️  gradlew not found, skipping clean"
fi
cd ..

# Build debug APK
echo "🔨 Building debug APK..."
cd android
if [ -f "./gradlew" ]; then
    ./gradlew assembleDebug
    BUILD_SUCCESS=$?
else
    echo "❌ gradlew not found. Cannot build APK."
    exit 1
fi
cd ..

# Check if APK was created
if [ $BUILD_SUCCESS -eq 0 ] && [ -f "android/app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Debug APK built successfully!"
    echo "📱 APK location: android/app/build/outputs/apk/debug/app-debug.apk"
    
    # Copy APK to root directory for easy access
    cp android/app/build/outputs/apk/debug/app-debug.apk ./kompas-gps-debug.apk
    echo "📋 APK copied to: ./kompas-gps-debug.apk"
    
    echo ""
    echo "🎉 Build completed successfully!"
    echo ""
    echo "📱 To install on device:"
    echo "   adb install kompas-gps-debug.apk"
    echo ""
    echo "🔧 To build release APK:"
    echo "   cd android && ./gradlew assembleRelease"
else
    echo "❌ Failed to build APK"
    echo ""
    echo "🔍 Troubleshooting:"
    echo "1. Make sure Android SDK is properly installed"
    echo "2. Check that ANDROID_HOME is set correctly"
    echo "3. Ensure Java JDK is installed"
    echo "4. Try running: npm install --legacy-peer-deps"
    exit 1
fi
