# Rencana Pengujian — OAT Prediction

## Status dan Referensi

Rencana pengujian berdasarkan [PRD.md](PRD.md), [API_CONTRACT.md](API_CONTRACT.md), dan [ML_SPECIFICATION.md](ML_SPECIFICATION.md). Skenario lintas fase tetap merupakan rencana; hasil yang sudah dieksekusi dicatat terpisah di bawah.

Project memiliki unit test JUnit 4, test instrumentasi AndroidJUnit4, serta test UI Compose/Espresso. Artefak MLR/CART telah dievaluasi dan backend model nyata sudah dideploy. Tidak ada threshold coverage.

Bagian hasil bertanggal di bawah adalah catatan historis untuk perubahan saat itu. Verifikasi kontrak lima prediktor dan deployment terbaru dicatat tersendiri setelah bagian historis.

## Pengujian tahap Home dan Form (baseline sebelum integrasi)

- PredictionFormValidatorTest: Puskesmas wajib; empat input numerik menerima nol atau nilai positif yang muat dalam Long; angka negatif/desimal/teks dan overflow ditolak. Form tidak meminta kategori OAT, X7, atau Y aktual.
- PredictionViewModelTest: error diperbarui setelah input diperbaiki, nilai mentah dipulihkan dari SavedStateHandle, dan form valid tidak menghasilkan PredictionUiState.Success.
- HomePredictionNavigationTest: Home menuju Form/Riwayat, tombol kembali dan Back Android, pemilihan dropdown, error wajib, submit valid hanya menampilkan pesan layanan belum tersedia, tombol dapat digulir saat keyboard aktif, serta pemulihan isian setelah Activity recreation dan kembali dari Home.

Test menggunakan nilai teknis pengujian saja, bukan data atau hasil penelitian. Activity recreation menguji perubahan lifecycle; simulasi pemulihan SavedStateHandle dilakukan terpisah pada unit test. Pengujian penghentian proses oleh sistem secara penuh belum diklaim.

### Hasil verifikasi 18 September 2026

Perintah akhir: `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest`.

- BUILD SUCCESSFUL; APK debug di `app/build/outputs/apk/debug/app-debug.apk`.
- 10 unit test lulus (9 test baru dan 1 test starter), tanpa kegagalan.
- 5 test instrumentasi lulus pada Infinix X6833B Android 14 (4 test UI baru dan 1 test starter).
- Lint berhasil tanpa error; masih ada peringatan pembaruan dependency, label manifest redundan, dan resource warna starter tidak terpakai.
- Laporan: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/androidTests/connected/debug/index.html`, dan `app/build/reports/lint-results-debug.html`.

Pengujian dilakukan pada satu perangkat. Penghentian proses oleh sistem, perangkat lain, serta fitur backend/ML/riwayat final belum diuji. Toolchain project tidak diubah.

## Keterlacakan Requirement dan Penerimaan

| Requirement / AC | Skenario dan hasil yang diharapkan |
| --- | --- |
| FR-01 / AC-01 | Home terbuka dengan nama, deskripsi, serta tombol Mulai Prediksi dan Riwayat. |
| FR-02 / AC-02, AC-03 | Form terbuka dan seluruh input wajib yang berlaku dapat diisi. |
| FR-03, FR-04 | Dropdown Puskesmas memakai nama penelitian tervalidasi; OAT Dewasa Kategori 1 tetap menjadi ruang lingkup dataset dan bukan field input. |
| FR-05, FR-06 / AC-04 | Kosong wajib, teks numerik, dan format salah ditolak dengan pesan jelas; request tidak dikirim. Uji batas/negatif setelah aturan domain dirinci. |
| FR-07 / AC-05, AC-06 | Android mengirim POST /predict dengan nama field sesuai kontrak dan Flask menerimanya. |
| FR-06 / AC-04 | Backend menolak input tidak valid meskipun validasi Android dilewati. |
| FR-08 / AC-07 | JSON sukses/error sesuai kontrak; enum kelas dikenali. Kasus status HTTP menunggu pemetaan final. |
| FR-09 / AC-08 | Kedua hasil ditampilkan dengan jelas; satuan mengikuti keputusan domain. |
| FR-10 / AC-12 | Loading terlihat, UI tetap responsif; kegagalan koneksi/API menjadi Error tanpa crash. |
| FR-11 / AC-09 | Prediksi sukses dapat disimpan beserta waktu, input, dan kedua hasil. |
| FR-12 | Detail riwayat sesuai data tersimpan; Y aktual tidak diminta sebagai input. |
| ML / AC-10 | Backend menggunakan MLR nyata beserta preprocessing yang benar. |
| ML / AC-11 | Backend menggunakan CART nyata secara terpisah dari output MLR. |

Untuk setiap kasus, catat ID requirement, lokasi implementasi setelah tersedia, test, lingkungan, hasil aktual, dan bukti. Jangan mengisi bukti sebelum eksekusi.

## Lapisan Pengujian

- Unit Android: validasi input dan transisi Idle/Loading/Success/Error pada komponen yang telah dibuat.
- UI/instrumentasi: navigasi, form, hasil, riwayat, dan pesan error.
- Kontrak/integrasi: serialisasi JSON, validasi ulang Flask, request/response, kegagalan jaringan, dan kompatibilitas dummy dengan model nyata.
- Data/ML: asal data, missing value bukan nol otomatis, ketersediaan fitur sebelum target, pemisahan training/test, konsistensi preprocessing, serta larangan target leakage.
- Penyelarasan LPLPO: untuk formulir bulan `t`, uji bahwa lima X (Puskesmas, pemakaian, sisa stok, penerimaan, kasus baru TBC) berasal dari baris `t`, sedangkan Y pada baris tersebut adalah permintaan untuk `t+1`. Pastikan Y dan permintaan periode sebelumnya tidak masuk sebagai fitur; periode tetap metadata.
- Pembagian data: pastikan 34 bulan pengembangan (Januari 2023–Oktober 2025) dan 8 bulan uji (November 2025–Juni 2026), tanpa memisahkan Puskesmas dari bulan yang sama. Dari 294 baris, cocokkan 224 lengkap, 168 pengembangan, dan 56 uji; dokumentasikan alasan pengeluaran 70 baris tidak lengkap.
- Validasi silang: lima lipatan berurutan dengan jendela validasi tiga bulan; jumlah bulan latih 19, 22, 25, 28, dan 31. Periksa bahwa tidak ada bulan masa depan masuk ke bagian latih pada lipatan mana pun.
- Evaluasi MLR: skor CV RMSE; evaluasi akhir MAE, RMSE, dan R² pada data uji mentah. R² negatif diperbolehkan dan tidak dibatasi menjadi nol.
- Evaluasi CART: skor CV Macro-F1; evaluasi akhir Accuracy, Macro-Precision, Macro-Recall, Macro-F1, Recall kelas Tinggi, dan confusion matrix. Ambang kelas dihitung dari Y positif pada bagian latih terkait, dengan Y nol tetap ikut dilabeli; hitung ulang pada tiap lipatan.
- End-to-end: Home → form → API → kedua model → hasil → simpan dan buka riwayat.

Uji akses riwayat offline hanya bila rancangan penyimpanan mendukungnya; kebutuhan ini belum dijamin. Perangkat, versi Android uji, framework backend, dan target performa numerik belum ditentukan.

## Gerbang Fase

### Verifikasi backend Flask dummy (kontrak legacy)

Hasil berikut merekam keadaan kode sebelum metodologi skripsi menetapkan kontrak lima prediktor.

- `backend/tests/test_predict.py`: 7 test unittest lulus, termasuk subtest missing/null, tipe/batas numerik, teks kosong, JSON rusak, Content-Type, metode/path, dan respons tetap untuk input berbeda.
- Server Flask sungguhan dijalankan sementara pada 127.0.0.1:5000: request valid menghasilkan HTTP 200 dengan JSON dummy tepat dan header X-OAT-Mode; request tanpa X7 menghasilkan 422 sesuai kontrak dummy legacy saat itu; JSON rusak menghasilkan 400. Ketiga pemeriksaan HTTP lulus dan proses server uji dihentikan setelahnya.
- Environment pengujian: Python 3.12.14 dan Flask 3.1.2 dalam backend/.venv. Seluruh payload adalah fixture teknis, bukan dataset/hasil penelitian.
- Source Android tidak diubah pada tahap backend, sehingga build Android tidak diulang. Pengujian ini belum mencakup klik tombol Android sampai respons Flask.

### Verifikasi client Retrofit (kontrak legacy)

`PredictionNetworkTest` pada saat itu menggunakan MockWebServer lokal dan fixture TEST ONLY, bukan backend Flask atau dataset penelitian. Sembilan test memeriksa POST /predict dengan DTO legacy, JSON request dan presisi Long, pemetaan ketiga kelas, HTTP error, respons success=false/kelas tidak dikenal, JSON rusak/missing/null, penolakan input sebelum jaringan, koneksi terputus tanpa retry, pembatalan coroutine, dan validasi base URL. Request final lima prediktor akan diuji setelah DTO diselaraskan.

Hasil eksekusi pada tahap ini: `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` mencapai BUILD SUCCESSFUL. Total 25 unit test lulus tanpa kegagalan (9 test jaringan dan 16 test sebelumnya). Lint berhasil; peringatan yang tidak menggagalkan build tetap tercatat di laporan. Default BuildConfig dan manifest debug diverifikasi memuat URL emulator, izin INTERNET, dan cleartext development.

Pada saat verifikasi Retrofit tersebut, koneksi ke Flask dan alur UI → jaringan belum diuji. Test instrumentasi UI tidak diulang karena tahap itu tidak mengubah UI. Hasil test client bukan bukti prediksi atau evaluasi ML.

Penomoran mengikuti 16 fase AGENTS.md, bukan penomoran berbeda dalam lampiran:

| Fase | Bukti penerimaan yang direncanakan |
| --- | --- |
| 1: Foundation | Build sukses dan aplikasi dapat berjalan |
| 2: Architecture | Pemisahan tanggung jawab dan rencana struktur terdokumentasi |
| 3–5: Home, form, ViewModel/Repository | Navigasi/input/validasi/state bekerja sesuai requirement lima prediktor |
| 6–8: Retrofit, Flask dummy, integrasi | POST /predict tersambung; dummy dikenali sebagai DUMMY / TEST ONLY dan tidak dianggap prediksi model |
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

## Integrasi Android ke Flask dummy

Implementasi saat ini menghubungkan submit valid melalui ViewModel → Repository → Retrofit → Flask dummy → Result; Room menyimpan riwayat lokal secara manual. Belum ada model ML. Bukti baseline di atas menggambarkan fase sebelumnya, bukan seluruh perilaku submit saat ini.

- PredictionSubmissionTest: validasi sebelum jaringan, pemetaan field, Loading/Success/Error, larangan submit ganda dan edit selama Loading, retry manual, pembatalan tanpa error, konsumsi navigasi sekali, dan pemulihan form tanpa hasil/request otomatis. Fixture pemetaan tujuh field yang ada masih menguji kontrak dummy legacy.
- ResultFormattingTest: angka bulat tanpa .0 serta pecahan tanpa pembulatan.
- PredictionFlowTest: Repository terkontrol untuk memeriksa seluruh kontrol nonaktif saat loading, error tanpa kehilangan input, retry, label dummy, hasil, dan tombol kembali tanpa navigasi berulang.
- HomePredictionNavigationTest: navigasi dasar, error wajib, akses tombol dengan keyboard, Activity recreation, dan isian saat kembali dari Home. Submit valid tidak lagi mengharapkan snackbar layanan belum tersedia.
- FlaskIntegrationTest: test perangkat opt-in melalui argumen backendMode=online/offline. Online memakai server Flask sebenarnya, memeriksa 125/Tinggi, label dummy, Activity recreation di Result, dan input setelah kembali. Offline memeriksa error koneksi dan isian tetap tersedia. Tanpa argumen, test ini dilewati.

Koneksi uji: HP USB menggunakan adb reverse tcp:5000 tcp:5000 dan base URL build http://127.0.0.1:5000/. Default emulator tidak diubah. Perintah lengkap terdapat di README backend. Semua angka pengujian adalah fixture teknis, bukan data atau hasil penelitian.

### Hasil verifikasi integrasi 18 September 2026

- assembleDebug, testDebugUnitTest, dan lintDebug berhasil; 32 unit test lulus. Lint: 0 error dan 22 warning.
- Delapan test instrumentasi lulus tanpa skipped/failure pada Infinix X6833B Android 14, termasuk FlaskIntegrationTest dengan backendMode=online. Alur Form → Flask → Result menghasilkan 125/Tinggi; pemulihan Activity di Result dan kembali ke form juga lulus.
- Tujuh test Flask lulus. Request HTTP langsung menghasilkan 200, JSON dummy sesuai kontrak, dan header X-OAT-Mode: dummy-integration-test.
- Perintah instrumentasi awal gagal karena argumen properti bertitik dipecah PowerShell. Pengulangan dengan argumen -P dikutip berhasil; README menggunakan bentuk yang telah diperbaiki.
- Skenario error/retry dengan Repository terkontrol lulus. Pengujian perangkat dengan backend sungguhan dimatikan (backendMode=offline) belum dijalankan; jangan menganggapnya lulus. Penghentian proses Android sepenuhnya juga belum diuji pada perangkat.

## Pengujian Riwayat Lokal

- HistoryFilterTest: irisan filter, batas inklusif/eksklusif, tujuh hari kalender lintas tahun, bulan kabisat, dan hari dengan DST.
- HistoryViewModelTest: snapshot input yang dikirim, retry/duplikasi simpan/hapus, pemulihan filter, reset Puskesmas yang hilang, serta ID detail tidak ditemukan.
- HistoryDatabaseTest: database Room nyata pada file test terisolasi, close/reopen tanpa backend, keutuhan Long/Double/DUMMY, ID duplikat, urutan, filter, nama unik, dan penghapusan terarah.
- HistoryFlowTest: Room terisolasi dan respons teknis terkontrol; Simpan → Home → Riwayat → Detail, semua input/output, label dummy, tiga dropdown, konfirmasi/batal hapus, empty state, serta Back tanpa menyimpan.
- Test navigasi integrasi lama disesuaikan: Result kini kembali ke Home. Test HTTP Flask tetap opt-in; tidak diperlukan untuk operasi riwayat lokal.

Semua fixture adalah data teknis pengujian, bukan dataset atau hasil penelitian. Hasil eksekusi dicatat setelah pengujian selesai.

### Perintah verifikasi riwayat tanpa server Flask

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest '-POAT_API_BASE_URL=http://127.0.0.1:5000/' '-Pandroid.testInstrumentationRunnerArguments.notClass=com.example.oatprediction.FlaskIntegrationTest'
```

FlaskIntegrationTest dikecualikan eksplisit karena memerlukan server dan backendMode. Runner saat ini melaporkan asumsi backendMode yang tidak terpenuhi sebagai failure, sehingga jangan mengandalkan skip implisit.

HistoryRecreationTest menambahkan bukti pemulihan filter dan Detail saat Activity dibuat ulang, serta membaca/menghapus catatan setelah Activity ditutup dan diluncurkan lagi. Test hanya membuat dan membersihkan ID fixture miliknya sendiri; tidak menghapus riwayat pengguna. Room close/reopen diuji terpisah oleh HistoryDatabaseTest. Ini bukan simulasi OS mematikan proses secara penuh.

### Berkas implementasi riwayat

| Berkas (relatif root) | Fungsi |
| --- | --- |
| app/src/main/java/com/example/oatprediction/data/model/HistoryRecord.kt | Snapshot hasil, sumber dummy, filter dan batas kalender. |
| app/src/main/java/com/example/oatprediction/data/local/HistoryEntity.kt | Representasi tabel dan pemetaan domain. |
| app/src/main/java/com/example/oatprediction/data/local/HistoryDao.kt | Query Flow, insert idempotent, hapus berdasarkan ID. |
| app/src/main/java/com/example/oatprediction/data/local/HistoryDatabase.kt | Database Room dan provider Repository singleton. |
| app/src/main/java/com/example/oatprediction/data/repository/HistoryRepository.kt | Akses riwayat dan penyaringan offline. |
| app/src/main/java/com/example/oatprediction/viewmodel/PredictionViewModel.kt | Membuat snapshot dan menangani Simpan. |
| app/src/main/java/com/example/oatprediction/viewmodel/HistoryViewModel.kt | Daftar, filter tersimpan, refresh dan error. |
| app/src/main/java/com/example/oatprediction/viewmodel/HistoryDetailViewModel.kt | Detail, penghapusan dan error. |
| app/src/main/java/com/example/oatprediction/ui/history/HistoryScreen.kt | Daftar, dropdown filter dan keadaan kosong. |
| app/src/main/java/com/example/oatprediction/ui/history/HistoryDetailScreen.kt | Semua input/output dan dialog hapus. |
| app/src/main/java/com/example/oatprediction/ui/result/ResultScreen.kt | Simpan dan kembali ke Home. |
| app/src/main/java/com/example/oatprediction/ui/home/HomeScreen.kt | Konfirmasi simpan melalui snackbar. |
| app/src/main/java/com/example/oatprediction/navigation/OatNavigation.kt | Rute riwayat/detail dan navigasi setelah simpan/hapus. |
| app/src/main/res/values/strings.xml | Teks UI Bahasa Indonesia. |
| app/src/main/res/xml/backup_rules.xml; app/src/main/res/xml/data_extraction_rules.xml | Pengecualian database dari backup/transfer. |
| gradle/libs.versions.toml; build.gradle.kts; app/build.gradle.kts | Dependency/plugin Room dan KSP serta ekspor schema. |
| app/schemas/com.example.oatprediction.data.local.HistoryDatabase/1.json | Schema database versi pertama. |
| app/src/test/java/com/example/oatprediction/viewmodel/HistoryFilterTest.kt; HistoryViewModelTest.kt; HistoryTestRepository.kt | Unit test filter/state dan fixture teknis. |
| app/src/androidTest/java/com/example/oatprediction/HistoryDatabaseTest.kt; HistoryFlowTest.kt; HistoryRecreationTest.kt | Test Room, UI, rotasi dan pembukaan ulang offline. |
| app/src/androidTest/java/com/example/oatprediction/HomePredictionNavigationTest.kt; PredictionFlowTest.kt; FlaskIntegrationTest.kt | Penyesuaian ekspektasi navigasi dan penggantian placeholder. |
| docs/PRD.md; docs/ARCHITECTURE.md; docs/API_CONTRACT.md; docs/TESTING_PLAN.md | Requirement, rancangan, dan bukti pengujian riwayat. |

### Hasil verifikasi riwayat — 19 September 2026

- Perintah verifikasi riwayat di atas mencapai BUILD SUCCESSFUL. APK debug berhasil dibangun untuk URL USB; tidak ada compile error.
- 39 unit test lulus tanpa kegagalan.
- 11 test instrumentasi lulus tanpa failure/error/skipped pada Infinix X6833B Android 14, termasuk Room persistence, alur riwayat, dan Activity recreation/relaunch. FlaskIntegrationTest dikecualikan eksplisit dan tidak dihitung sebagai lulus.
- Lint berhasil: 0 error, 27 warning. Peringatan tidak menggagalkan build; rinciannya terdapat di app/build/reports/lint-results-debug.html.
- Percobaan instrumentasi pertama gagal pada selector teks Puskesmas ambigu dan asumsi backendMode test Flask. Selector diperbaiki agar hanya memilih menu popup, lalu seluruh suite lokal diulang dengan pengecualian Flask eksplisit dan berhasil.
- Laporan: app/build/reports/tests/testDebugUnitTest/index.html dan app/build/reports/androidTests/connected/debug/index.html. git diff --check tidak menemukan error whitespace.
- Pengujian riwayat tidak mengakses jaringan: UI memakai respons teknis terkontrol, Room memakai database lokal. Tidak menghentikan server pengguna dan tidak mengulang HTTP Flask pada tahap ini. Pengujian process death penuh, perangkat lain, dan model ML nyata tidak diklaim.

## Pengujian Pengenalan

- StartupViewModelTest: pembacaan tertunda tanpa Home, false/belum selesai, true, baca gagal dan retry, simpan gagal dan retry, klik ganda, serta keluar sebelum Mulai tidak menulis flag.
- IntroductionPreferencesTest: file DataStore test terisolasi; key tidak ada/false belum selesai, true tetap tersedia setelah store ditutup dan dibuka ulang.
- IntroductionFlowTest: loading/error tidak menampilkan Home, retry, tombol nonaktif saat simpan, konten dapat digulir pada font 200% dan tema gelap, serta status selesai melewati pengenalan.
- StartupActivityTest: Mulai jika diperlukan, rotasi pada Form tanpa kembali ke intro, Back dari Home, kemudian peluncuran Activity baru langsung Home tanpa menekan Mulai lagi.
- IntroductionTestSupport secara eksplisit menyelesaikan pengenalan jika muncul pada test lama berbasis MainActivity. Test yang memasang OatNavigation langsung tetap menguji fitur secara terisolasi. Tidak menghapus data aplikasi/riwayat untuk memaksa kondisi fresh install.

Berkas baru: data/preferences/IntroductionPreferences.kt, viewmodel/StartupViewModel.kt, ui/introduction/IntroductionScreen.kt; unit test dan instrumentasi di atas. Berkas diperbarui: MainActivity.kt, strings.xml, kedua aturan backup, version catalog, app/build.gradle.kts, test HomePredictionNavigation/HistoryRecreation/FlaskIntegration, serta PRD/ARCHITECTURE/TESTING_PLAN. Semua path Kotlin produksi di bawah app/src/main/java/com/example/oatprediction/.

### Hasil verifikasi pengenalan — 19 September 2026

- assembleDebug, testDebugUnitTest, dan lintDebug: BUILD SUCCESSFUL; 45 unit test lulus, 0 kegagalan. Lint: 0 error, 28 warning.
- connectedDebugAndroidTest dengan pengecualian eksplisit FlaskIntegrationTest: BUILD SUCCESSFUL; 15 test lulus, 0 failure/error/skipped pada Infinix X6833B Android 14.
- Bukti mencakup DataStore file baru/missing/false, persistensi true setelah reopen, retry baca/simpan, tombol saat menyimpan, font 200%/tema gelap, rotasi Activity pada Form, dan peluncuran ulang langsung Home. Test regresi riwayat dan navigasi turut lulus.
- Kondisi preferensi baru diuji pada file terisolasi dan UI terkontrol; tidak melakukan uninstall/hapus data aplikasi pengguna. Backup/transfer diperiksa melalui konfigurasi XML, bukan percobaan pemulihan cloud nyata. Process death penuh belum diuji.
- Test otomatis berbasis MainActivity menyelesaikan pengenalan jika diperlukan; flag pada perangkat uji kini tersimpan sehingga pembukaan berikutnya langsung Home. Riwayat pengguna tidak dihapus.
- Laporan aktual: app/build/reports/tests/testDebugUnitTest/index.html, app/build/reports/androidTests/connected/debug/index.html, dan app/build/reports/lint-results-debug.html. git diff --check berhasil tanpa error whitespace. Backend/ML tidak diubah atau diuji ulang pada tahap ini.


## Redesign tahap 1

- Unit: preferensi tema default terang dan persisten setelah DataStore dibuka ulang; gagal baca fallback terang; gagal simpan tetap tema lama dan dapat retry; klik ganda tidak menghasilkan penulisan paralel.
- Compose/perangkat: Pengenalan → Mulai Aplikasi → Beranda; pembukaan berikutnya melewati Pengenalan; rotasi dan Back; tab Tentang/Riwayat kembali Beranda tanpa duplikasi; filter dan posisi daftar dipertahankan; Form–Hasil–Simpan–Detail tetap berjalan.
- Visual: screenshot aktual Pengenalan, Beranda, Tentang dibandingkan Stitch. Periksa mode gelap, font 2×, scroll, target sentuh, system bars, badge dummy, versi build, dan tidak adanya metrik performa rekaan.
- Perbedaan sengaja: identitas penelitian menggantikan instansi resmi; status dummy menggantikan online; status pengembangan menggantikan akurasi; rumus MLR tidak menetapkan jumlah predictor final; tidak ada kontak/PDF contoh.
- Jalankan assembleDebug, testDebugUnitTest, lintDebug, serta connectedDebugAndroidTest jika perangkat/emulator tersedia. Test Flask nyata dijalankan terpisah dengan server aktif; tidak diganti dengan klaim keberhasilan dummy UI.


Hasil redesign tahap 1: assembleDebug/test APK berhasil, 47 JVM test lulus, lint 0 error/31 warning. Setelah percobaan Pixel 6 API 34 terganggu ANR sistem emulator, suite instrumentasi diulang melalui adb pada Pixel 2 API 30: 19 tes lulus. Tes persistensi file DataStore berada di androidTest karena kendala rename file pada Windows JVM. Rincian file dan bukti visual ada pada [REDESIGN_STAGE1_REPORT.md](REDESIGN_STAGE1_REPORT.md).


## Revisi teks 21 September 2026

- Pengenalan menampilkan UPTD PUSKESMAS KOTA LHOKSEUMAWE, tanpa kartu dummy; perilaku startup/loading/retry tetap diuji.
- Beranda menampilkan status online dengan label Contoh tampilan, kartu 94,8%/Valid dengan catatan belum berdasarkan evaluasi; deskripsi sapaan dihapus.
- Tentang tidak memuat badge/paragraf dummy; kredit © 2026 Teknik Informatika Universitas Malikussaleh berada di dasar konten dan dapat dijangkau dengan scroll.
- RedesignVisualTest memeriksa teks dan screenshot terbaru, navigasi/filter, mode gelap dan font 2×. IntroductionFlowTest disesuaikan; label dummy Hasil/Detail dan sumber riwayat tidak diubah.
- Screenshot laporan tahap 1 adalah kondisi sebelum revisi. Bukti revisi disimpan terpisah dalam `screenshots/text-revision/`.

Hasil aktual revisi: assembleDebug dan assembleDebugAndroidTest berhasil; 47 unit test lulus; lint 0 error/36 warning. IntroductionFlowTest dan RedesignVisualTest dijalankan pada HP terhubung: 4 tes lulus. Lima screenshot aktual diperiksa, termasuk tema gelap/font 2×. Suite instrumentasi lain dan endpoint Flask tidak dijalankan ulang untuk revisi teks ini.


## Kartu metode Stitch

MethodsCardTest memeriksa tujuh variabel, kedua persamaan, catatan ilustrasi dan metodologi, serta keterjangkauan isi melalui scroll pada tema terang/gelap dan font 2×. Screenshot MLR/CART disimpan terpisah pada `screenshots/methods/`. Build debug, unit test, lint dan instrumentasi terkait harus diperiksa; ini tidak mengevaluasi model ML.


Hasil verifikasi akhir: assembleDebug dan assembleDebugAndroidTest berhasil; 47 unit test lulus; lint 0 error/46 warning (termasuk resource metode lama yang kini tidak terpakai). MethodsCardTest dan RedesignVisualTest dijalankan ulang pada HP setelah penyesuaian kolom/formula: 2 tes lulus. Screenshot aktual diperiksa pada tema terang/gelap dan font 2×. Log perangkat: `app/build/methods-instrumentation.txt`. Suite instrumentasi lain dan endpoint Flask tidak dijalankan ulang untuk perubahan kartu ini.


## Redesign tahap 2

RecordingPeriodTest memeriksa default kalender, pemulihan pilihan, tahun 1–9999, input invalid yang tidak mengirim request, snapshot periode sebelum request dan Prediksi Ulang tanpa request/reset. HistoryMigrationTest menggunakan schema v1 pada database uji terpisah, memvalidasi migrasi ke v2, periode lama null, keutuhan data, persistensi periode baru dan filter timestamp. StageTwoFlowTest memeriksa dialog periode, loading, hasil, Prediksi Ulang, simpan/detail, screenshot terang dan font 2× pada tema gelap. Jalankan regresi form, riwayat, rotasi, save/error dan navigation dengan suite instrumentasi; Flask nyata diuji terpisah bila server tersedia.

Hasil tahap 2: build debug dan test APK berhasil, 50 unit test lulus, lint 0 error/54 warning. Suite perangkat luas: 20 dari 22 lulus; dua kegagalan berada pada label/catatan Beranda yang tidak tampil. Verifikasi final Form/Hasil/navigasi/migrasi: 11 tes lulus. Lihat [REDESIGN_STAGE2_REPORT.md](REDESIGN_STAGE2_REPORT.md) untuk rincian, screenshot, dan keterbatasan.


## Redesign tahap 3

Uji filter kategori tunggal/gabungan empat filter, hasil kosong, pemulihan kategori serta reset hanya Puskesmas setelah penghapusan. Unit test memeriksa status hapus tetap tersedia saat refresh, kegagalan/retry, operasi ganda dan keutuhan catatan lain. HistoryDatabaseTest memeriksa kategori dan metadata setelah database dibuka ulang, tanpa backend.

StageThreeFlowTest memakai database in-memory dan fixture teknis untuk kategori, periode lama kosong, batal/hapus dari daftar dan Detail, error/retry, proteksi Back/tab saat hapus, tema terang/gelap, font 2× dan screenshot. HistoryRecreationTest memeriksa kategori saat Activity dibuat ulang dan baca/hapus setelah pembukaan ulang. Tes alur lama disesuaikan untuk teks Lihat Detail dan scroll pada susunan baru. Dua tes Beranda yang sebelumnya gagal tidak dilemahkan atau diubah pada tahap ini.

Hasil final tahap 3: assembleDebug dan test APK berhasil; 52 unit test lulus; lint 0 error/56 warning. Suite HP tanpa FlaskIntegrationTest menjalankan 24 tes: 22 lulus, dua kegagalan Beranda tetap seperti tahap sebelumnya. Seluruh tes riwayat dan alur tahap 3 lulus. Rincian file, screenshot dan batas pemeriksaan: [REDESIGN_STAGE3_REPORT.md](REDESIGN_STAGE3_REPORT.md).


## Koreksi visual Riwayat

HistoryStitchVisualTest menggunakan tiga fixture teknis dalam Room in-memory untuk screenshot, semua dropdown lewat scroll horizontal, indikator aktif, Semua, kombinasi empat filter, kembali dari Detail dengan posisi horizontal tetap, tema gelap dan font 2×. HistoryRecreationTest memeriksa posisi chip sesudah Activity recreation. HistoryViewModelTest memeriksa reset empat nilai SavedStateHandle dengan satu pengamatan ulang. Tes alur lama diarahkan ke chip lewat dua sumbu scroll dan menggunakan Back Android karena tombol Kembali Riwayat dihilangkan. Dua tes Beranda yang diketahui gagal tetap tidak diubah.


Hasil koreksi visual Riwayat: build debug/test APK berhasil; 53 unit test lulus; lint 0 error/57 warning. Lima tes terarah lulus. Regresi HP tanpa FlaskIntegrationTest: 23 dari 25 lulus, dengan dua kegagalan Beranda yang sama. Screenshot dan daftar perubahan ada pada [HISTORY_STITCH_CORRECTION_REPORT.md](HISTORY_STITCH_CORRECTION_REPORT.md).


## Panduan PDF dan warna klasifikasi Hasil

GuideViewModelTest memeriksa metadata/retry, permintaan izin berulang, penolakan biasa/permanen, gagal penyimpanan, klik ganda, sukses dan nama file. GuideExportTest pada API 29+ memeriksa ekspor nyata ke Download, byte identik, PdfRenderer dapat membuka PDF, IS_PENDING selesai, nama unik dan cleanup saat asset tidak dapat dibaca. Tes hanya menghapus file yang dibuat sendiri.

GuideActivityTest memeriksa tombol Tentang mengekspor asset tanpa backend dan Activity recreation tidak membuat unduhan tambahan. GuideAndClassificationUiTest memeriksa status kartu, screenshot terang/gelap/font 2×, serta ketiga teks/warna badge Hasil pada dua tema. Hash sumber/asset/APK dibandingkan. Jalur izin Android 7–9 perlu perangkat/emulator API 24–28; jika tidak tersedia laporkan batas pengujian, jangan menyebut jalur tersebut sudah diuji di perangkat.

Pemeriksaan notifikasi unduhan: pada Android 13+, izinkan POST_NOTIFICATIONS lalu unduh panduan dan ekspor Excel dari masing-masing grafik; pastikan setiap file berhasil disimpan dan notifikasi heads-up menampilkan nama file. Ketuk notifikasi dan pastikan pemilih aplikasi menerima URI dengan izin baca serta tipe MIME yang sesuai (PDF atau XLSX). Ulangi dengan izin notifikasi ditolak dan pastikan penyimpanan tetap berhasil tanpa notifikasi. Pada Android 7–9, verifikasi URI FileProvider dapat membuka PDF/XLSX tanpa FileUriExposedException. Skenario ini merupakan pemeriksaan manual yang direncanakan; jangan laporkan lulus sebelum dijalankan pada perangkat/emulator.

Hasil 22 September 2026: build debug/test APK berhasil, 56 unit test lulus, lint 0 error/57 warning. Tujuh tes terarah di HP Android 14 lulus. Regresi tanpa FlaskIntegrationTest: 26 dari 28 lulus; dua kegagalan Beranda sebelumnya tetap ada. Ekspor nyata/PdfRenderer/keutuhan file/nama unik/cleanup dan recreation setelah unduh lulus. Android 7–9 serta rotasi tepat saat penyalinan belum diuji di perangkat. Rincian file dan screenshot: [GUIDE_DOWNLOAD_REPORT.md](GUIDE_DOWNLOAD_REPORT.md).


## Revisi teks netral

PredictionFlowTest memeriksa judul Hasil Prediksi dan tidak adanya pemberitahuan dummy setelah sukses. HistoryFlowTest memastikan label dummy tidak tampil pada daftar/Detail sambil tetap menguji hasil, seluruh input, navigasi dan penghapusan. FlaskIntegrationTest disesuaikan untuk ketiadaan label; respons uji tetap dummy. Unit/database test yang memverifikasi ResultSource.DUMMY dipertahankan. Tes Beranda yang gagal sebelumnya tetap terpisah karena tampilan Beranda tidak diubah pada revisi ini.

Hasil revisi: build debug/test APK berhasil, 56 unit test lulus, lint 0 error/54 warning. Tes perangkat belum berjalan karena HP tidak terdeteksi dan instalasi APK emulator macet. Rincian dan file berubah: [NEUTRAL_UI_COPY_REPORT.md](NEUTRAL_UI_COPY_REPORT.md).


## Validasi, sesi form, dan snackbar — 22 September 2026

Uji reset nilai/SavedStateHandle/error/periode, pembatalan dan snapshot, serta restorasi saat rotasi. FormFeedbackTest memeriksa gulir otomatis pada submit kosong berulang/angka desimal tanpa request dan snackbar sekali tampil meskipun keluar/masuk; event baru boleh menampilkan teks sama. Tes alur lama disesuaikan untuk form kosong setelah kembali dari Beranda dan Prediksi Ulang. Jalankan assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest dan instrumentasi pada perangkat tersedia.

Hasil final: build debug/test APK berhasil, 57 unit test lulus, lint 0 error/54 warning/1 hint. Dua belas tes terarah di HP Android 14 lulus. Rincian file, hasil, dan batas pemeriksaan: [FORM_FEEDBACK_REPORT.md](FORM_FEEDBACK_REPORT.md).


## Urutan dan periode prediksi — 22 September 2026

PredictionPeriodTest memeriksa bulan/tahun/batas/null serta sort/filter/tie ID. HistoryViewModelTest memeriksa restorasi sort, reset filter tanpa reset urutan dan blokir saat hapus. HistorySortPeriodTest memeriksa toggle/urutan/tab serta periode lama kosong. StageTwoFlowTest memeriksa Form baca-saja, snapshot Hasil dan Detail pada Desember menjadi Januari, termasuk reset form. Jalankan build/unit/lint/instrumentasi dan laporkan hasil aktual.

Hasil final urutan/periode: build berhasil, 60 unit test lulus, lint 0 error/55 warning/1 hint, dan 9 tes terarah di HP Android 14 lulus. Rincian dan screenshot: [HISTORY_SORT_PERIOD_REPORT.md](HISTORY_SORT_PERIOD_REPORT.md).


## Flask MLR/CART dan deployment PythonAnywhere — 30 Oktober 2026

- Backend harus menguji pemuatan model MLR/CART, kesiapan `/health`, request valid, mapping `kasus_baru_tbc` → `kasus_baru`, serta prediksi API yang cocok dengan pemanggilan langsung kedua pipeline.
- Validasi API menguji Puskesmas di luar daftar, setiap field hilang/null/salah tipe, nilai negatif, desimal, boolean, integer di luar Long, field tambahan, JSON rusak, dan Content-Type salah. Kegagalan load model harus menghasilkan 503 tanpa detail internal; kegagalan inferensi menghasilkan 500 JSON tanpa stack trace publik.
- Sebelum unggah, paket hanya berisi Flask, dependensi runtime, manifest hash/versi, dan dua artefak utama. Dataset, metrik, prediksi hold-out, notebook, dan gambar tidak boleh masuk paket.
- Setelah reload PythonAnywhere, uji `/health`, request valid dan invalid lewat HTTPS, serta error log. Catat URL publik, waktu verifikasi, status HTTP, dan versi runtime tanpa menyimpan kredensial.
- Android tetap belum tersinkron pada tahap deployment ini. Uji Android → API dilakukan setelah Tahap 3 (kontrak/form/DTO) dan Tahap 4 (Retrofit, HTTPS, serta alur end-to-end).

Verifikasi lokal 31 Oktober 2026: 14 unittest backend lulus, termasuk pemuatan default kedua artefak, kesiapan `/health`, validasi/error, serta kesamaan prediksi API dengan inferensi langsung MLR dan CART. Paket deployment hanya berisi tujuh file yang diizinkan; hash model cocok dengan metadata. Endpoint publik PythonAnywhere sudah diverifikasi oleh pemilik akun.

## Android lima prediktor dan API PythonAnywhere — 31 Oktober 2026

- `PredictionInput`, form, validator, dan DTO Android memakai Puskesmas, pemakaian, sisa stok, penerimaan, dan kasus baru TBC. Bulan/tahun hanya metadata lokal; kategori OAT dan permintaan periode sebelumnya tidak dikirim.
- Room schema versi 3 menyimpan lima prediktor pada catatan baru. Migrasi 2→3 mempertahankan field catatan lama sebagai nilai legacy nullable; nilai lama tidak ditafsirkan sebagai sisa stok/kasus baru.
- Endpoint `https://syamildeveloper.pythonanywhere.com/health` mengembalikan status `ok` dengan kedua model berstatus `loaded`. Request lima prediktor tervalidasi berhasil; input negatif menghasilkan HTTP 422 dan JSON `VALIDATION_ERROR`. Contoh request hanya fixture teknis.
- Hasil pemeriksaan: `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, dan `:app:compileDebugAndroidTestKotlin` berhasil. `FlaskIntegrationTest` berhasil mengirim input lima prediktor ke API HTTPS dan menampilkan keluaran MLR/CART pada Infinix X6833B Android 14. `HistoryDatabaseTest` memverifikasi penyimpanan lima predictor baru, sedangkan `HistoryMigrationTest` memastikan riwayat lama tetap tersimpan dan tidak diubah menjadi predictor baru.
- Base URL default Gradle diarahkan ke HTTPS PythonAnywhere. Uji integrasi memakai fixture teknis yang sama dengan request yang sebelumnya diverifikasi; keluaran tampilan 0 dan kelas Rendah adalah hasil aplikasi untuk input uji tersebut, bukan klaim performa penelitian. Suite instrumentasi penuh belum dijalankan.
## Analisis dan unduhan Excel — diperbarui 3 November 2026

- `AnalysisTest`: parser CSV memetakan lima prediktor dan Y aktual, mempertahankan nilai nol, serta menangani data lengkap. Uji deduplikasi timestamp/ID, periode invalid, nilai nol/pecahan/Long besar, pilihan indikator, pembaruan Room, loading/error/retry, izin, ekspor ganda dan snapshot. Periksa struktur XML/ZIP, sel hilang, angka lengkap, dua tipe grafik dan referensi internal.
- `AnalysisUiTest`: sumber Dataset membaca 224 baris asset (Januari 2023–Juni 2026), menunjukkan Permintaan Aktual dan tidak menunjukkan filter jenis obat. Sumber Riwayat hanya menganalisis OAT Dewasa Kategori 1 serta melaporkan kategori lama yang dikecualikan; gunakan database in-memory agar riwayat pengguna tidak berubah. Periksa kedua grafik, ekspor, tab/Back, restorasi state, tema terang/gelap dan font 2×.
- `AnalysisExportTest`: MediaStore API 29+, nama unik, status pending selesai, isi workbook, kegagalan tulis dan pembersihan file parsial. Hapus hanya file yang dibuat pengujian. Jalur izin API 24–28 membutuhkan perangkat versi tersebut; jangan mengklaim lulus hanya dari mock ViewModel.
- Buka workbook dengan pembaca independen dan Microsoft Excel bila tersedia; pastikan sheet, nilai lengkap, celah, seri grafik mengacu ke Data, dan chart dikenali sebagai grafik native. Ambil screenshot halaman/dua grafik dan font besar. Jalankan assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest dan instrumentasi terarah. Catat keterbatasan serta kegagalan regresi lama secara terpisah.
## Tooltip klasifikasi — 30 September 2026

`ClassificationTooltipTest` memeriksa pesan tepat pada tekan lama ketiga kelas dalam tema terang/gelap, penutupan melalui Back, serta tekan lama filter yang tidak memilih nilai dan ketuk biasa yang tetap memilih/reset filter. Periksa juga warna badge tetap sama melalui `GuideAndClassificationUiTest`. Build dan lint wajib diperiksa; instrumentasi memakai komponen/fixture terpisah tanpa mutasi riwayat pengguna.

Hasil 30 September 2026: assembleDebug/assembleDebugAndroidTest berhasil dengan konfigurasi USB `-POAT_API_BASE_URL=http://127.0.0.1:5000/`; 66 unit test lulus; lint 0 error, 57 warning, 3 hint. Pada HP Android 14, ClassificationTooltipTest (2) dan GuideAndClassificationUiTest (1) lulus setelah memperbaiki fokus popup agar Back hanya menutup tooltip. Log: `app/build/tooltip-device-tests.txt`. Suite perangkat penuh tidak diulang; dua kegagalan Beranda sebelumnya tidak dinyatakan terselesaikan.

File perubahan: `ui/components/ClassificationTooltip.kt` (komponen bersama baru), `ui/components/ClassificationBadge.kt` (Hasil/Detail), `ui/history/HistoryRecordCard.kt` (badge daftar), `ui/history/HistoryFilterBar.kt` (chip dan pilihan dropdown), `res/values/strings.xml` (tiga pesan), `ClassificationTooltipTest.kt` (tes instrumentasi baru), `docs/PRD.md`, dan dokumen ini. Path package Kotlin berada di `app/src/main/java/com/example/oatprediction/`; tes berada di `app/src/androidTest/java/com/example/oatprediction/`.
