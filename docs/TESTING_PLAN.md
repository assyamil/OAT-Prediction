# Rencana Pengujian — OAT Prediction

## Status dan Referensi

Draft baseline pengujian berdasarkan [PRD.md](PRD.md), [API_CONTRACT.md](API_CONTRACT.md), dan [ML_SPECIFICATION.md](ML_SPECIFICATION.md). Seluruh skenario berikut adalah rencana; tidak menyatakan fitur sudah tersedia atau pengujian sudah lulus.

Project saat ini memiliki test starter JUnit 4 dan AndroidJUnit4 serta dependensi Espresso/Compose UI testing. Belum ada threshold coverage.

## Keterlacakan Requirement dan Penerimaan

| Requirement / AC | Skenario dan hasil yang diharapkan |
| --- | --- |
| FR-01 / AC-01 | Home terbuka dengan nama, deskripsi, serta tombol Mulai Prediksi dan Riwayat. |
| FR-02 / AC-02, AC-03 | Form terbuka dan seluruh input wajib yang berlaku dapat diisi. |
| FR-03, FR-04 | Dropdown memakai daftar tervalidasi; nama/kategori dummy bukan data produksi. |
| FR-05, FR-06 / AC-04 | Kosong wajib, teks numerik, dan format salah ditolak dengan pesan jelas; request tidak dikirim. Uji batas/negatif setelah aturan domain dirinci. |
| FR-07 / AC-05, AC-06 | Android mengirim POST /predict dengan nama field sesuai kontrak dan Flask menerimanya. |
| FR-06 / AC-04 | Backend menolak input tidak valid meskipun validasi Android dilewati. |
| FR-08 / AC-07 | JSON sukses/error sesuai kontrak; enum kelas dikenali. Kasus status HTTP menunggu pemetaan final. |
| FR-09 / AC-08 | Kedua hasil ditampilkan dengan jelas; satuan mengikuti keputusan domain. |
| FR-10 / AC-12 | Loading terlihat, UI tetap responsif; kegagalan koneksi/API menjadi Error tanpa crash. |
| FR-11 / AC-09 | Prediksi sukses dapat disimpan beserta waktu, input, dan kedua hasil. |
| FR-12 | Detail riwayat sesuai data tersimpan; X7 hanya ditampilkan bila berlaku. |
| ML / AC-10 | Backend menggunakan MLR nyata beserta preprocessing yang benar. |
| ML / AC-11 | Backend menggunakan CART nyata secara terpisah dari output MLR. |

Untuk setiap kasus, catat ID requirement, lokasi implementasi setelah tersedia, test, lingkungan, hasil aktual, dan bukti. Jangan mengisi bukti sebelum eksekusi.

## Lapisan Pengujian

- Unit Android: validasi input dan transisi Idle/Loading/Success/Error pada komponen yang telah dibuat.
- UI/instrumentasi: navigasi, form, hasil, riwayat, dan pesan error.
- Kontrak/integrasi: serialisasi JSON, validasi ulang Flask, request/response, kegagalan jaringan, dan kompatibilitas dummy dengan model nyata.
- Data/ML: asal data, missing value bukan nol otomatis, ketersediaan fitur sebelum target, pemisahan training/test, konsistensi preprocessing, serta larangan target leakage.
- Evaluasi MLR: MAE, MSE, RMSE, R². Evaluasi CART: Accuracy, Precision, Recall, F1-score, Confusion Matrix. Hanya eksperimen aktual; averaging dan batas skor belum ditetapkan.
- End-to-end: Home → form → API → kedua model → hasil → simpan dan buka riwayat.

Uji akses riwayat offline hanya bila rancangan penyimpanan mendukungnya; kebutuhan ini belum dijamin. Perangkat, versi Android uji, framework backend, dan target performa numerik belum ditentukan.

## Gerbang Fase

Penomoran mengikuti 16 fase AGENTS.md, bukan penomoran berbeda dalam lampiran:

| Fase | Bukti penerimaan yang direncanakan |
| --- | --- |
| 1: Foundation | Build sukses dan aplikasi dapat berjalan |
| 2: Architecture | Pemisahan tanggung jawab dan rencana struktur terdokumentasi |
| 3–5: Home, form, ViewModel/Repository | Navigasi/input/validasi/state bekerja sesuai requirement pada fase terkait |
| 6–8: Retrofit, Flask dummy, integrasi | POST /predict tersambung; respons dummy dikenali sebagai DUMMY / TEST ONLY |
| 9–10: Hasil dan riwayat | Hasil terbaca, riwayat dapat disimpan dan dibuka |
| 11: Preprocessing | Dataset penelitian tervalidasi dan keputusan preprocessing terdokumentasi |
| 12–13: MLR dan CART | Model masing-masing terlatih dan evaluasi aktual tersedia |
| 14: Integrasi model nyata | Backend memakai kedua model terpisah dengan kontrak kompatibel |
| 15: End-to-end | Alur utama dan kegagalan API teruji |
| 16: Build final dan dokumentasi | Build, hasil test relevan, dan dokumentasi akhir tersedia |

Integrasi dummy tidak memenuhi AC-10/AC-11 atau membuktikan performa penelitian. Jangan melompati fase besar tanpa instruksi eksplisit.

## Perintah Android

Dari root project melalui PowerShell, pada tahap implementasi yang relevan:

~~~powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug
~~~

Test instrumentasi memerlukan device/emulator kompatibel. Test lokal di app/src/test/ dan instrumentasi di app/src/androidTest/. Gunakan kelas *Test dan nama metode deskriptif.

## Dummy, Bukti, dan Definisi Selesai

Dummy hanya untuk pengembangan/pengujian teknis yang diizinkan, berstatus DUMMY / TEST ONLY, dan tidak masuk hasil penelitian, BAB IV, evaluasi, atau kesimpulan. Tidak membuat dataset penelitian dari fixture pengujian.

Task implementasi selesai bila requirement terpenuhi, build berhasil, test relevan dijalankan dan lulus, tidak ada error diketahui yang belum diselesaikan, serta perubahan terdokumentasi. Laporkan perintah, hasil, file berubah, error, dan keterbatasan; bedakan belum dijalankan, terhalang, gagal, dan lulus.

Task dokumentasi diperiksa melalui struktur file, tautan, konsistensi kontrak/requirement, serta keterlacakan FR/AC. Build Android tidak diperlukan untuk perubahan dokumentasi saja.
