#!/bin/bash

echo "🧭 Building Kompas GPS App..."

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
npm install

# Install React Native CLI locally if not installed
if ! command -v react-native &> /dev/null; then
    echo "📱 Installing React Native CLI locally..."
    npm install --save-dev react-native-cli
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
cd android
./gradlew clean
cd ..

# Build debug APK
echo "🔨 Building debug APK..."
cd android
./gradlew assembleDebug
cd ..

# Check if APK was created
if [ -f "android/app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Debug APK built successfully!"
    echo "📱 APK location: android/app/build/outputs/apk/debug/app-debug.apk"
    
    # Copy APK to root directory for easy access
    cp android/app/build/outputs/apk/debug/app-debug.apk ./kompas-gps-debug.apk
    echo "📋 APK copied to: ./kompas-gps-debug.apk"
else
    echo "❌ Failed to build APK"
    exit 1
fi

echo "🎉 Build completed successfully!"
echo ""
echo "📱 To install on device:"
echo "   adb install kompas-gps-debug.apk"
echo ""
echo "🔧 To build release APK:"
echo "   cd android && ./gradlew assembleRelease"
