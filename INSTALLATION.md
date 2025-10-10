# 📱 Panduan Instalasi Kompas GPS

## 🚀 Instalasi Cepat

### 1. Prasyarat
Pastikan sistem Anda memiliki:
- **Node.js** (v16 atau lebih baru)
- **npm** atau **yarn**
- **Android SDK** (untuk build APK)
- **Java Development Kit** (JDK 8+)

### 2. Setup Environment
```bash
# Clone repository
git clone <repository-url>
cd Project-Kompas

# Install dependencies
npm install

# Setup Android environment variables
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 3. Build APK
```bash
# Build debug APK (cara mudah)
./build.sh

# Atau build manual
cd android
./gradlew assembleDebug
cd ..
```

### 4. Install di Device
```bash
# Via ADB
adb install kompas-gps-debug.apk

# Atau copy file APK ke device dan install manual
```

## 🔧 Setup Detail

### Android SDK Setup
1. Download Android Studio atau Android SDK Command Line Tools
2. Set environment variables:
   ```bash
   export ANDROID_HOME=/path/to/android-sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```
3. Accept Android SDK licenses:
   ```bash
   $ANDROID_HOME/tools/bin/sdkmanager --licenses
   ```

### Node.js Setup
```bash
# Install Node.js (Ubuntu/Debian)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install Node.js (macOS)
brew install node

# Install Node.js (Windows)
# Download dari https://nodejs.org/
```

## 🐛 Troubleshooting

### Error: "SDK location not found"
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/android-sdk
echo 'export ANDROID_HOME=/path/to/android-sdk' >> ~/.bashrc
source ~/.bashrc
```

### Error: "Java not found"
```bash
# Install OpenJDK
sudo apt-get install openjdk-8-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

### Error: "Permission denied" saat build
```bash
# Berikan permission ke script
chmod +x build.sh
chmod +x test-compass.js
```

### APK tidak bisa diinstall
1. Aktifkan "Unknown sources" di device Android
2. Pastikan device kompatibel (Android 5.0+)
3. Cek signature APK

## 📊 Testing

### Test Functions
```bash
# Jalankan test script
node test-compass.js
```

### Test di Device
1. Install APK di device
2. Buka aplikasi
3. Berikan permission lokasi
4. Test di area terbuka
5. Bandingkan dengan kompas fisik

## 🎯 Optimasi Akurasi

### Kalibrasi Sensor
1. Gerakkan device dalam bentuk angka 8
2. Hindari area dengan medan magnet kuat
3. Pastikan GPS aktif
4. Test di area terbuka

### Tips Penggunaan
- Gunakan di area terbuka untuk GPS akurat
- Hindari dekat peralatan elektronik
- Kalibrasi ulang jika arah tidak akurat
- Periksa akurasi GPS di bagian bawah

## 📱 Build Release APK

### Setup Keystore
```bash
# Generate keystore
keytool -genkey -v -keystore kompas-release-key.keystore -alias kompas-key-alias -keyalg RSA -keysize 2048 -validity 10000

# Update gradle.properties
echo "MYAPP_UPLOAD_STORE_FILE=kompas-release-key.keystore" >> android/gradle.properties
echo "MYAPP_UPLOAD_KEY_ALIAS=kompas-key-alias" >> android/gradle.properties
echo "MYAPP_UPLOAD_STORE_PASSWORD=your_password" >> android/gradle.properties
echo "MYAPP_UPLOAD_KEY_PASSWORD=your_password" >> android/gradle.properties
```

### Build Release
```bash
cd android
./gradlew assembleRelease
cd ..

# APK akan tersedia di: android/app/build/outputs/apk/release/app-release.apk
```

## 🔍 Debugging

### Log Android
```bash
# Lihat log aplikasi
adb logcat | grep KompasApp

# Lihat log React Native
npx react-native log-android
```

### Debug Mode
```bash
# Jalankan dalam debug mode
npx react-native run-android

# Atau
npm run android
```

## 📞 Support

Jika mengalami masalah:
1. Cek log error di terminal
2. Pastikan semua dependencies terinstall
3. Cek environment variables
4. Buat issue di repository

---

**Selamat menggunakan Kompas GPS! 🧭**
