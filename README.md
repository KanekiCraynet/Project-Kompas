# Compass Pro

Aplikasi kompas profesional yang terintegrasi dengan fitur lokasi GPS dan informasi cuaca real-time. Dibangun dengan teknologi Android modern menggunakan Jetpack Compose dan arsitektur MVVM.

## Fitur Utama

### 🧭 Kompas Digital
- **Akurasi Tinggi**: Menggunakan sensor magnetometer dan accelerometer untuk pembacaan yang presisi
- **Kalibrasi Otomatis**: Sistem kalibrasi cerdas untuk memastikan akurasi maksimal
- **Animasi Smooth**: Transisi halus dan responsif untuk pengalaman pengguna yang optimal
- **Tampilan Modern**: Desain elegan dengan gradien dan animasi yang menarik

### 📍 Lokasi GPS
- **Akurasi Tinggi**: Menggunakan GPS dan Network Provider untuk lokasi yang presisi
- **Informasi Detail**: Menampilkan koordinat, altitude, kecepatan, dan akurasi
- **Update Real-time**: Pembaruan lokasi secara kontinyu dengan optimasi baterai
- **Magnetic Declination**: Perhitungan otomatis untuk konversi ke arah sebenarnya

### 🌤️ Informasi Cuaca
- **Data Real-time**: Integrasi dengan OpenWeatherMap API untuk data cuaca terkini
- **Arah Angin**: Informasi kecepatan dan arah angin yang akurat
- **Parameter Lengkap**: Suhu, kelembaban, tekanan, dan visibilitas
- **Kategori Wind**: Klasifikasi kecepatan angin berdasarkan skala Beaufort

### 🎨 UI/UX Modern
- **Material Design 3**: Mengikuti panduan desain terbaru dari Google
- **Dark/Light Theme**: Dukungan tema otomatis berdasarkan sistem
- **Responsive Design**: Optimal untuk berbagai ukuran layar
- **Animasi Fluid**: Transisi dan animasi yang smooth dan natural

## Teknologi yang Digunakan

### Framework & Library
- **Jetpack Compose**: UI modern dengan declarative programming
- **Material 3**: Komponen UI yang konsisten dan modern
- **Kotlin Coroutines**: Asynchronous programming yang efisien
- **Flow**: Reactive programming untuk data streams
- **Koin**: Dependency injection yang ringan dan mudah digunakan

### Architecture
- **MVVM Pattern**: Model-View-ViewModel untuk separation of concerns
- **Repository Pattern**: Abstraksi layer data untuk maintainability
- **Use Cases**: Business logic yang terorganisir dan testable
- **State Management**: Reactive state management dengan StateFlow

### Services & APIs
- **Location Services**: Google Play Services untuk GPS
- **Weather API**: OpenWeatherMap untuk data cuaca
- **Sensor Framework**: Android Sensor API untuk kompas
- **Firebase**: Analytics dan Crashlytics untuk monitoring

## Struktur Proyek

```
app/
├── src/main/java/com/compasspro/
│   ├── data/
│   │   ├── api/           # API interfaces
│   │   ├── model/         # Data models
│   │   └── repository/    # Data repositories
│   ├── domain/
│   │   ├── model/         # Domain models
│   │   └── usecase/       # Business logic
│   ├── service/           # Background services
│   ├── ui/
│   │   ├── component/     # Reusable UI components
│   │   ├── screen/        # Screen composables
│   │   └── theme/         # UI theming
│   ├── utils/             # Utility functions
│   └── di/                # Dependency injection
├── src/main/res/
│   ├── drawable/          # Vector drawables
│   ├── values/            # Colors, strings, themes
│   └── xml/               # Configuration files
└── build.gradle           # App-level build configuration
```

## Instalasi & Setup

### Prerequisites
- Android Studio Arctic Fox atau lebih baru
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9.10+
- Gradle 8.1.2+

### Langkah Instalasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/yourusername/Project-Kompas.git
   cd Project-Kompas
   ```

2. **Setup API Keys**
   - Daftar di [OpenWeatherMap](https://openweathermap.org/api) untuk API key
   - Update `WEATHER_API_KEY` di `app/build.gradle`
   - Setup Firebase project dan download `google-services.json`

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Konfigurasi API

1. **OpenWeatherMap API**
   ```kotlin
   // app/build.gradle
   buildConfigField "String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\""
   ```

2. **Firebase Setup**
   - Buat project di [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` dan letakkan di folder `app/`
   - Enable Analytics dan Crashlytics

## Penggunaan

### Kompas
1. Buka aplikasi dan berikan permission lokasi
2. Kalibrasi kompas dengan menggerakkan perangkat dalam pola figure-8
3. Arahkan perangkat ke arah yang diinginkan
4. Baca heading dan arah dari tampilan kompas

### Lokasi
1. Pastikan GPS aktif dan permission lokasi diberikan
2. Aplikasi akan otomatis mendeteksi lokasi saat ini
3. Lihat informasi detail di kartu lokasi
4. Gunakan untuk navigasi dan orientasi

### Cuaca
1. Lokasi otomatis digunakan untuk data cuaca
2. Refresh manual tersedia di tombol action
3. Lihat informasi lengkap di kartu cuaca
4. Monitor arah dan kecepatan angin

## Kompatibilitas Perangkat

### Minimum Requirements
- **Android Version**: 7.0 (API 24)
- **RAM**: 2GB
- **Storage**: 50MB
- **Sensors**: Magnetometer, Accelerometer, GPS

### Recommended
- **Android Version**: 10.0+ (API 29)
- **RAM**: 4GB+
- **Storage**: 100MB+
- **Sensors**: Gyroscope, Barometer (optional)

### Tested Devices
- Samsung Galaxy S21/S22 series
- Google Pixel 6/7 series
- OnePlus 9/10 series
- Xiaomi Mi 11/12 series
- Huawei P40/P50 series

## Performance & Optimization

### Battery Optimization
- **Smart Location Updates**: Adaptive interval berdasarkan akurasi
- **Sensor Optimization**: Efficient sensor usage dengan proper lifecycle
- **Background Processing**: Minimal background activity
- **Doze Mode Compatible**: Mengikuti Android battery optimization

### Memory Management
- **Leak Prevention**: Proper lifecycle management
- **Efficient Rendering**: Optimized Compose recomposition
- **Resource Cleanup**: Automatic cleanup of resources
- **Memory Monitoring**: Built-in memory usage tracking

## Troubleshooting

### Common Issues

1. **Compass Tidak Akurat**
   - Pastikan perangkat dikalibrasi dengan benar
   - Hindari area dengan interferensi magnetik
   - Restart aplikasi jika diperlukan

2. **Lokasi Tidak Terdeteksi**
   - Periksa permission lokasi
   - Pastikan GPS aktif
   - Coba di area terbuka

3. **Data Cuaca Tidak Update**
   - Periksa koneksi internet
   - Verifikasi API key
   - Coba refresh manual

### Debug Mode
```bash
# Enable debug logging
adb shell setprop log.tag.CompassPro DEBUG

# View logs
adb logcat | grep CompassPro
```

## Contributing

Kontribusi sangat diterima! Silakan ikuti langkah berikut:

1. Fork repository
2. Buat feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

### Coding Standards
- Ikuti [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Gunakan [Material Design Guidelines](https://material.io/design)
- Tulis unit tests untuk business logic
- Dokumentasi kode yang jelas

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

- **Developer**: Your Name
- **Email**: your.email@example.com
- **Project Link**: [https://github.com/yourusername/Project-Kompas](https://github.com/yourusername/Project-Kompas)

## Acknowledgments

- [OpenWeatherMap](https://openweathermap.org/) untuk API cuaca
- [Google Play Services](https://developers.google.com/android) untuk location services
- [Material Design](https://material.io/) untuk design guidelines
- [Jetpack Compose](https://developer.android.com/jetpack/compose) untuk modern UI framework

---

**Compass Pro** - Navigasi profesional dengan teknologi terdepan 🧭✨
