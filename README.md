# 🧭 Kompas GPS - Aplikasi Kompas dengan Lokasi

Aplikasi kompas lengkap yang menggunakan sensor magnetometer dan GPS untuk memberikan navigasi yang akurat tanpa memerlukan Android Studio.

## ✨ Fitur Utama

- **Kompas Digital Akurat**: Menggunakan sensor magnetometer untuk deteksi arah
- **GPS Integration**: Menampilkan koordinat lokasi dan akurasi GPS
- **UI Modern**: Interface yang menarik dengan animasi smooth
- **Real-time Updates**: Update data lokasi dan arah secara real-time
- **Magnetic Declination**: Koreksi otomatis untuk akurasi yang lebih baik
- **Multi-Platform**: Dibangun dengan React Native

## 🛠️ Teknologi yang Digunakan

- **React Native**: Framework utama untuk cross-platform development
- **React Native Sensors**: Akses ke sensor magnetometer
- **React Native Geolocation**: Integrasi GPS dan lokasi
- **React Native SVG**: Visualisasi kompas yang menarik
- **Gradle**: Build system untuk Android

## 📋 Persyaratan Sistem

- Node.js (v16 atau lebih baru)
- npm atau yarn
- Android SDK (untuk build APK)
- Java Development Kit (JDK 8 atau lebih baru)

## 🚀 Instalasi dan Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd Project-Kompas
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Setup Android Environment
Pastikan Android SDK sudah terinstall dan environment variables sudah dikonfigurasi:
```bash
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## 🔨 Build APK

### Build Debug APK (Cara Mudah)
```bash
./build.sh
```

### Build Manual
```bash
# Install dependencies
npm install

# Build debug APK
cd android
./gradlew assembleDebug
cd ..

# APK akan tersedia di: android/app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
```bash
cd android
./gradlew assembleRelease
cd ..

# APK akan tersedia di: android/app/build/outputs/apk/release/app-release.apk
```

## 📱 Instalasi di Device

### Via ADB
```bash
adb install kompas-gps-debug.apk
```

### Via File Manager
1. Copy file APK ke device Android
2. Buka file manager di device
3. Tap file APK dan ikuti instruksi instalasi
4. Pastikan "Install from unknown sources" diaktifkan

## 🎯 Cara Penggunaan

1. **Buka Aplikasi**: Launch aplikasi Kompas GPS
2. **Izinkan Permission**: Berikan akses lokasi saat diminta
3. **Kalibrasi**: Gerakkan device dalam bentuk angka 8 untuk kalibrasi
4. **Navigasi**: Arahkan device ke arah yang diinginkan
5. **Lihat Info**: Perhatikan koordinat GPS dan akurasi di bagian bawah

## 🔧 Konfigurasi

### Permission yang Diperlukan
- `ACCESS_FINE_LOCATION`: Untuk GPS akurat
- `ACCESS_COARSE_LOCATION`: Untuk lokasi umum
- `INTERNET`: Untuk layanan lokasi

### Kalibrasi Sensor
Aplikasi akan otomatis menggunakan data magnetometer untuk menghitung arah. Untuk hasil terbaik:
- Hindari area dengan medan magnet yang kuat
- Kalibrasi dengan gerakan angka 8
- Pastikan GPS aktif untuk akurasi maksimal

## 📊 Spesifikasi Teknis

### Sensor yang Digunakan
- **Magnetometer**: Deteksi medan magnet untuk arah
- **GPS**: Koordinat lokasi dan akurasi
- **Accelerometer**: (Opsional) Untuk stabilisasi

### Akurasi
- **Kompas**: ±3° dalam kondisi normal
- **GPS**: Tergantung kualitas sinyal (biasanya 3-5 meter)
- **Update Rate**: 100ms untuk sensor, 1 detik untuk GPS

## 🐛 Troubleshooting

### APK Tidak Bisa Diinstall
- Pastikan "Unknown sources" diaktifkan
- Cek signature APK
- Pastikan device kompatibel (Android 5.0+)

### Kompas Tidak Akurat
- Kalibrasi sensor dengan gerakan angka 8
- Hindari area dengan medan magnet kuat
- Pastikan GPS aktif

### Build Error
- Pastikan Android SDK terinstall
- Cek environment variables
- Update dependencies: `npm update`

## 📝 Logika Pemrograman

### Algoritma Kompas
```javascript
// Hitung arah dari data magnetometer
let angle = Math.atan2(y, x) * (180 / Math.PI);
angle = angle < 0 ? angle + 360 : angle;

// Koreksi magnetic declination
const magneticDeclination = getMagneticDeclination(location);
angle = angle + magneticDeclination;
```

### Update Rate Optimization
- Sensor: 100ms untuk responsivitas
- GPS: 1 detik untuk efisiensi baterai
- UI: Smooth animation dengan Animated API

## 🤝 Kontribusi

1. Fork repository
2. Buat feature branch
3. Commit perubahan
4. Push ke branch
5. Buat Pull Request

## 📄 Lisensi

Project ini menggunakan lisensi MIT. Lihat file LICENSE untuk detail.

## 📞 Support

Jika mengalami masalah atau memiliki pertanyaan:
- Buat issue di repository
- Email: [your-email@domain.com]
- Dokumentasi: [link-to-docs]

---

**Dibuat dengan ❤️ menggunakan React Native**
