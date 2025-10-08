# CompassPro - Aplikasi Kompas Digital dengan Akurasi Tinggi

## Deskripsi

CompassPro adalah aplikasi kompas digital yang dirancang khusus untuk perangkat Android dengan fitur-fitur canggih yang memberikan akurasi tinggi dalam menampilkan arah, lokasi pengguna, dan informasi arah angin real-time. Aplikasi ini menggunakan arsitektur modern dengan Jetpack Compose dan mengintegrasikan berbagai sensor perangkat untuk memberikan pengalaman kompas yang optimal.

## Fitur Utama

### 🧭 Kompas Digital
- **Akurasi Tinggi**: Menggunakan algoritma kompleks untuk perhitungan arah yang presisi
- **Kalibrasi Otomatis**: Sistem kalibrasi sensor yang cerdas untuk akurasi optimal
- **Visualisasi Modern**: UI yang elegan dengan animasi smooth dan responsif
- **Arah Kardinal**: Menampilkan arah dalam bahasa Indonesia (Utara, Timur, Selatan, Barat, dll.)

### 📍 Lokasi Real-time
- **GPS Integration**: Menggunakan GPS dan Network Provider untuk akurasi maksimal
- **Geocoding**: Konversi koordinat ke alamat yang dapat dibaca
- **Background Location**: Monitoring lokasi berkelanjutan dengan foreground service
- **Address Display**: Menampilkan alamat lengkap pengguna

### 🌬️ Informasi Angin
- **Arah Angin**: Menampilkan arah angin real-time
- **Kecepatan Angin**: Informasi kecepatan dan hembusan angin
- **Data Cuaca**: Suhu, kelembaban, tekanan udara, dan jarak pandang
- **API Integration**: Terintegrasi dengan OpenWeatherMap API

### 🎨 UI/UX Modern
- **Material Design 3**: Menggunakan desain terbaru dari Google
- **Dark/Light Theme**: Dukungan tema gelap dan terang
- **Responsive Design**: Optimal untuk berbagai ukuran layar
- **Smooth Animations**: Animasi yang halus dan natural

## Arsitektur Teknis

### 🏗️ Clean Architecture
Aplikasi menggunakan Clean Architecture dengan pemisahan yang jelas antara:
- **Presentation Layer**: UI dengan Jetpack Compose
- **Domain Layer**: Business logic dan use cases
- **Data Layer**: Repository pattern dengan API dan local storage

### 🔧 Teknologi yang Digunakan
- **Kotlin**: Bahasa pemrograman utama
- **Jetpack Compose**: UI framework modern
- **Hilt**: Dependency injection
- **Coroutines & Flow**: Asynchronous programming
- **Room**: Local database
- **Retrofit**: Network communication
- **WorkManager**: Background tasks
- **Material Design 3**: Design system

### 📱 Sensor Integration
- **Accelerometer**: Deteksi orientasi perangkat
- **Magnetometer**: Deteksi medan magnetik bumi
- **Gyroscope**: Deteksi rotasi perangkat
- **GPS**: Lokasi presisi tinggi
- **Network Location**: Backup location provider

## Instalasi dan Setup

### Prasyarat
- Android Studio Arctic Fox atau lebih baru
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9.10+
- Gradle 8.1.4+

### Langkah Instalasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/yourusername/Project-Kompas.git
   cd Project-Kompas
   ```

2. **Buka di Android Studio**
   - Buka Android Studio
   - Pilih "Open an existing project"
   - Navigasi ke folder Project-Kompas

3. **Konfigurasi API Key**
   - Buka file `WeatherRepository.kt`
   - Ganti `"YOUR_API_KEY_HERE"` dengan API key OpenWeatherMap
   - Daftar di [OpenWeatherMap](https://openweathermap.org/api)

4. **Build dan Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Permission yang Diperlukan
- `ACCESS_FINE_LOCATION`: Untuk GPS presisi tinggi
- `ACCESS_COARSE_LOCATION`: Untuk network location
- `ACCESS_BACKGROUND_LOCATION`: Untuk monitoring lokasi background
- `INTERNET`: Untuk API cuaca
- `ACCESS_NETWORK_STATE`: Untuk cek koneksi internet

## Penggunaan

### Kalibrasi Sensor
1. Buka aplikasi CompassPro
2. Jika sensor belum dikalibrasi, akan muncul tombol "Kalibrasi Sensor"
3. Tekan tombol kalibrasi
4. Gerakkan perangkat dalam bentuk angka 8 selama 10 detik
5. Kalibrasi akan selesai otomatis

### Membaca Kompas
- **Jarum Merah**: Menunjuk ke arah Utara
- **Jarum Putih**: Menunjuk ke arah Selatan
- **Derajat**: Ditampilkan di bagian bawah kompas
- **Arah Kardinal**: Ditampilkan dalam bahasa Indonesia

### Informasi Lokasi
- **Koordinat**: Latitude dan longitude ditampilkan
- **Alamat**: Alamat lengkap berdasarkan koordinat
- **Akurasi**: Tingkat akurasi GPS dalam meter

### Data Angin
- **Arah Angin**: Ditampilkan dalam derajat
- **Kecepatan**: Dalam meter per detik
- **Kondisi Cuaca**: Suhu, kelembaban, tekanan udara

## Optimasi dan Kompatibilitas

### Perangkat yang Didukung
- **Minimum**: Android 7.0 (API 24)
- **Target**: Android 14 (API 34)
- **Sensor**: Accelerometer, Magnetometer, GPS
- **RAM**: Minimum 2GB (Recommended 4GB+)

### Optimasi Performa
- **Background Processing**: Menggunakan WorkManager untuk efisiensi
- **Sensor Filtering**: Algoritma filtering untuk mengurangi noise
- **Memory Management**: Efficient memory usage dengan proper lifecycle
- **Battery Optimization**: Minimal battery drain dengan smart polling

### Kompatibilitas Perangkat
- **Samsung**: Galaxy S series, Note series, A series
- **Google**: Pixel series
- **OnePlus**: OnePlus series
- **Xiaomi**: Mi series, Redmi series
- **Huawei**: P series, Mate series (dengan GMS)

## Troubleshooting

### Masalah Umum

**Kompas tidak akurat**
- Pastikan perangkat tidak dekat dengan benda magnetik
- Lakukan kalibrasi sensor
- Hindari area dengan interferensi magnetik tinggi

**Lokasi tidak ditemukan**
- Pastikan GPS aktif
- Cek permission lokasi
- Pastikan berada di area terbuka

**Data angin tidak muncul**
- Cek koneksi internet
- Pastikan API key valid
- Cek quota API OpenWeatherMap

**Aplikasi crash**
- Restart aplikasi
- Clear cache aplikasi
- Update ke versi terbaru

### Log dan Debug
Aplikasi menyediakan logging yang komprehensif untuk debugging:
```bash
adb logcat | grep CompassPro
```

## Kontribusi

Kami menyambut kontribusi dari komunitas! Untuk berkontribusi:

1. Fork repository
2. Buat feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

### Guidelines
- Ikuti coding style yang sudah ada
- Tambahkan unit test untuk fitur baru
- Update dokumentasi jika diperlukan
- Pastikan semua test pass

## Lisensi

Distributed under the MIT License. See `LICENSE` for more information.

## Kontak

- **Developer**: [Your Name]
- **Email**: [your.email@example.com]
- **GitHub**: [@yourusername](https://github.com/yourusername)
- **LinkedIn**: [Your LinkedIn](https://linkedin.com/in/yourprofile)

## Changelog

### Version 1.0.0 (Current)
- ✅ Kompas digital dengan akurasi tinggi
- ✅ Integrasi GPS dan lokasi real-time
- ✅ Informasi arah angin dan cuaca
- ✅ UI modern dengan Jetpack Compose
- ✅ Kalibrasi sensor otomatis
- ✅ Dark/Light theme support
- ✅ Background location monitoring
- ✅ Material Design 3

### Roadmap
- 🔄 Kompas 3D dengan AR
- 🔄 Navigasi ke waypoint
- 🔄 Export data kompas
- 🔄 Widget untuk home screen
- 🔄 Offline mode
- 🔄 Multiple language support

---

**CompassPro** - Navigasi yang Akurat, Informasi yang Lengkap 🌍🧭
