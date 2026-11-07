# Arsitektur Teknis — OAT Prediction

## Status dan Referensi

Arsitektur mengikuti requirement produk di [PRD.md](PRD.md), kontrak [API_CONTRACT.md](API_CONTRACT.md), serta metodologi penelitian di [`SKRIPSI/SKRIPSI.docx`](../SKRIPSI/SKRIPSI.docx) dan [ML_SPECIFICATION.md](ML_SPECIFICATION.md). Bagian kondisi aktual membedakan implementasi saat ini dari rancangan target.

## Kondisi Aktual

Satu module Android app dengan package com.example.oatprediction. Minimum SDK 24, compile/target SDK 37. Client Retrofit, Repository, Room, layar utama, form, hasil, analisis, panduan, dan riwayat lokal tersedia. Flask lokal dan deployment PythonAnywhere memakai artefak MLR/CART utama dengan kontrak lima prediktor. Tahap 3 menyelaraskan form, model domain, validasi, DTO, dan migrasi riwayat; Tahap 4 mengatur URL HTTPS dan memverifikasi alur Android langsung.

Fondasi fase arsitektur sudah dipisahkan sebagai berikut:

- MainActivity mengatur Activity dan tema; navigation/OatNavigation memakai Navigation Compose NavHost dengan rute Home, Tentang, Form Prediksi, Result, Riwayat lokal, dan Detail. Masing-masing layar memiliki Scaffold agar inset tidak diterapkan dua kali.
- UI dipisahkan ke ui/home, ui/prediction, ui/result, dan ui/history. Form dapat digulir dan menangani inset keyboard; Riwayat dan Detail membaca catatan Room yang disimpan manual.
- PredictionViewModel dibuat dengan lifecycle owner Activity sebelum NavHost, sehingga isian tetap tersedia ketika rute Form dikeluarkan dari back stack. SavedStateHandle menyimpan teks mentah setiap field untuk pemulihan isian Activity/proses oleh Android; penyimpanan riwayat terpisah memakai Room.
- PredictionFormState menyimpan isian, error per field, dan status validasi melalui Compose State. Form meminta Puskesmas, pemakaian, sisa stok, penerimaan, dan kasus baru TBC; bulan/tahun hanya metadata horizon. Validator memeriksa Puskesmas, field wajib, dan empat angka bulat nonnegatif yang muat dalam Long.
- PredictionUiState bersifat observable (Compose State): Idle → Loading → Success/Error. submit memvalidasi, memetakan Long, lalu memanggil Repository dalam viewModelScope. Error menggunakan enum alasan dan kode HTTP opsional; UI memilih pesan dari string resources. InvalidPredictionResponseException membedakan kegagalan kontrak dari koneksi.
- data/model/PredictionInput dan PredictionResult adalah model aplikasi, bukan DTO JSON atau model ML. Input model memakai lima prediktor yang disetujui; Y tidak dimasukkan ke request. Bulan/tahun formulir tetap metadata Android. Kategori produk hanya metadata lokal untuk riwayat dan tidak menjadi input model.
- data/repository/PredictionRepository mendefinisikan akses prediksi; RemotePredictionRepository meneruskannya ke PredictionRemoteDataSource melalui constructor injection.
- data/network/PredictionRemoteDataSource diimplementasikan oleh RetrofitPredictionRemoteDataSource. PredictionApi mendefinisikan suspend POST predict dengan DTO yang sudah tersedia. Adapter memetakan kelas secara eksplisit dan meneruskan kegagalan HTTP/decoding/jaringan/pembatalan tanpa membuat hasil.
- PredictionRepositoryProvider merupakan composition root lazy untuk Repository, adapter, dan API. Instance diinjeksi melalui PredictionViewModel.factory dengan createSavedStateHandle. UI test dapat memasok ViewModel dengan Repository terkontrol tanpa mengubah provider produksi.

NetworkClient memusatkan Retrofit/OkHttp dan converter Kotlin Serialization; base URL dari BuildConfig.API_BASE_URL melalui properti Gradle OAT_API_BASE_URL. Default sekarang HTTPS PythonAnywhere; alamat development lokal dapat diberikan melalui properti Gradle. Konfigurasi timeout, HTTP development, dan error dijelaskan dalam API_CONTRACT.md. resultNavigationPending dikonsumsi sekali setelah navigasi saat Form RESUMED. Hasil bertahan pada rotasi melalui ViewModel, tetapi tidak disimpan ke SavedStateHandle/database; Result tanpa hasil setelah process death kembali ke Form tanpa request ulang.

## Pemisahan Tanggung Jawab

Compose UI → ViewModel → Repository → Retrofit → Flask REST API → pipeline model.

| Komponen | Tanggung jawab |
| --- | --- |
| Compose UI | Form, hasil, riwayat, interaksi, serta tampilan state dan error manusiawi |
| ViewModel | Mengelola input, validasi awal, tindakan pengguna, dan state Idle/Loading/Success/Error |
| Repository | Menyediakan akses data dan memisahkan ViewModel dari detail sumber data |
| Retrofit | Mengirim request dan membaca respons sesuai kontrak API |
| Flask | Validasi ulang, preprocessing, inferensi, respons JSON, dan penanganan error |
| Pipeline MLR | Prediksi numerik Permintaan Obat |
| Pipeline CART | Klasifikasi Rendah/Sedang/Tinggi secara terpisah dari MLR |

Composable tidak boleh memanggil Retrofit atau HTTP langsung. Request asynchronous menjaga UI responsif. Loading menonaktifkan isian dan submit; Back dari Form membatalkan job dan mereset state. Rotasi tidak membatalkan request. CancellationException diteruskan tanpa pesan error. Retry hanya atas submit pengguna, bukan otomatis.

## Usulan Struktur Package

Di bawah com.example.oatprediction:

~~~text
data/
  model/
  network/
  repository/
ui/
  home/
  prediction/
  result/
  history/
  theme/          (sudah ada)
navigation/
viewmodel/
util/
~~~

Struktur ini usulan dari lampiran dan boleh disesuaikan dengan kebutuhan sambil mempertahankan pemisahan tanggung jawab. Tidak membuat package kosong atau memilih library tambahan dalam tahap dokumentasi.

## Backend dan Model

Backend terdiri dari app.py (factory Flask, `/health`, `POST /predict`, validasi dan error HTTP) serta prediction_service.py (pemuatan dan inferensi MLR/CART). Dua artefak utama dan ZIP deployment disiapkan lokal oleh package_deployment.py; artefak tidak diikutkan ke Git. requirements.txt mematok pustaka yang tercatat saat pelatihan. Python tetap berada di `backend/`; project Android mengikuti kontrak final lima prediktor di [API_CONTRACT.md](API_CONTRACT.md).

Saat startup worker, Flask memuat masing-masing pipeline sekali dari `backend/models/`. Service memetakan `kasus_baru_tbc` ke fitur training `kasus_baru`, lalu mengirim lima kolom ke kedua model secara terpisah. API mengembalikan nilai MLR mentah dan label CART, tidak memuat dataset, dan tidak menyimpan request. `/health` melaporkan kesiapan model; error publik tidak mengungkap detail internal. Petunjuk lokal tersedia di [README backend](../backend/README.md), sementara panduan deploy ada di [PYTHONANYWHERE_DEPLOYMENT.md](../backend/PYTHONANYWHERE_DEPLOYMENT.md).

MLR dan CART merupakan model terpisah, bukan MLR → CART. Puskesmas di-one-hot encode oleh pipeline dan fitur mengikuti nama kolom saat training. PythonAnywhere menggunakan WSGI, bukan server `app.run()`; deployment Free dengan HTTPS sudah aktif dan `/health` telah mengonfirmasi kedua model.

Puskesmas di-one-hot encode oleh pipeline MLR dan CART; Android mengirim nama Puskesmas sebagai string, bukan mapping angka. Empat prediktor numerik dikirim sebagai bilangan bulat. Detail pemetaan fitur dan preprocessing ada di [ML_SPECIFICATION.md](ML_SPECIFICATION.md).

## Konfigurasi, Riwayat, dan Kegagalan

URL backend berada dalam konfigurasi environment/build. Credential dan private key tidak ditulis ke source.

Prediksi membutuhkan backend; tidak tersedia fallback prediksi lokal dalam baseline. Kegagalan koneksi menghasilkan Error tanpa crash. Riwayat menyimpan input, waktu, dan kedua hasil sesuai PRD; Room menyimpan secara manual dan mendukung baca/filter/hapus offline.

Format API model nyata mengikuti [API_CONTRACT.md](API_CONTRACT.md). API dan Room mempertahankan nilai MLR mentah; Android membulatkan nilai untuk jumlah kotak dan membatasi nilai tampilan minimum nol. Uji langsung `FlaskIntegrationTest` pada perangkat berhasil melalui HTTPS PythonAnywhere.

## Keputusan Teknis yang Belum Ditetapkan

PythonAnywhere Free adalah target hosting; subdomain HTTPS digunakan tanpa autentikasi aplikasi pada prototipe ini. Runtime model dipatok ke Python 3.13 dan pustaka yang dicatat pada metadata Colab. `/health` menunjukkan kesiapan kedua model; `/predict` memakai 503 saat model tak tersedia dan 500 saat inferensi gagal. Navigasi memakai Navigation Compose, state form memakai Compose State, serializer memakai Kotlin Serialization, dan Repository menggunakan constructor injection. Timeout/retry client awal tercatat dalam kontrak API.

Perubahan arsitektur harus beralasan teknis dan terdokumentasi. Implementasi berikutnya mengikuti fase AGENTS.md serta rencana file, pengujian, risiko, dan asumsi.

## Riwayat Room

Riwayat lama tetap tersimpan sebagai data legacy; migrasi Room 2→3 mempertahankan nilai lamanya dan tidak menebak sisa stok atau kasus baru. Catatan baru menyimpan lima prediktor final. Kategori hanya metadata lokal, bukan fitur model.

Room 2.8.4 + KSP 2.3.4 menggunakan version catalog, schema versi 3 diekspor ke app/schemas. Migrasi 1→2 menambah metadata periode; migrasi 2→3 mempertahankan kolom legacy sebagai nullable dan menambah sisa stok/kasus baru. Tidak ada fallback migrasi destruktif. Database privat oat_history.db dibuat singleton memakai applicationContext; backup cloud dan transfer database dikecualikan pada kedua file aturan backup.

HistoryRecord adalah snapshot immutable berisi UUID, timestamp respons diterima, lima input model, PredictionResult, dan sumber hasil. Data legacy ditampilkan terpisah dan tidak dipetakan menjadi prediktor baru. PredictionViewModel membuat snapshot saat sukses, bukan saat Simpan, sehingga perubahan form tidak mencampur input/hasil. HistorySaveState terpisah dari state jaringan. Primary key dan INSERT IGNORE memastikan retry idempotent tanpa mengganti catatan lama.

HistoryDao menyediakan Flow daftar terbaru/detail dan operasi suspend insert/delete. RoomHistoryRepository memetakan entity ke domain dan memfilter daftar di dispatcher Default; satu emisi berisi hasil terfilter, total, dan nama Puskesmas unik agar empty state/filter konsisten. Strategi sederhana ini ditujukan untuk riwayat lokal tahap awal; pagination/filter SQL dapat ditinjau jika volume nyata membutuhkannya.

HistoryViewModel menyimpan filter pada SavedStateHandle dan memulai ulang pengamatan dengan batas waktu kalender saat RESUMED. HistoryDetailViewModel menangani baca/hapus/retry. Navigasi membawa ID, bukan payload. Hasil simpan menuju Home; hasil hapus menuju Riwayat; navigasi menunggu lifecycle RESUMED. Operasi simpan/hapus memblokir aksi ganda dan Back sementara. UI menggunakan strings Bahasa Indonesia.

Timestamp disimpan dalam epoch milliseconds, ditampilkan mengikuti zona perangkat. Perhitungan menggunakan Calendar untuk mendukung API 24 dan perubahan DST tanpa mengubah toolchain. Kolom numerik input Long, output Double, kelas dan sumber disimpan eksplisit; missing bukan nol.

## Startup dan pengenalan

MainActivity membuat StartupViewModel dengan lifecycle Activity. StartupContent menampilkan Loading, ReadError, Introduction(saving/saveError), atau konten OatNavigation saat Ready. Pengenalan berada di luar NavHost sehingga setelah selesai tidak dapat dibuka dengan Back dari Home. OatNavigation baru dikomposisikan setelah flag dibaca atau penyimpanan berhasil; tidak ada timer ataupun perubahan splash bawaan Android.

DataStoreIntroductionPreferences mengimplementasikan IntroductionPreferences. Delegate preferencesDataStore pada applicationContext menyediakan satu DataStore untuk files/datastore/introduction.preferences_pb; key Boolean intro_completed. Nilai missing menjadi false, tetapi exception baca tetap diteruskan. Pembacaan satu kali dan edit asynchronous dijalankan dari viewModelScope; retry terkontrol, CancellationException diteruskan. ViewModel bertahan pada rotasi. Tidak ada perubahan schema Room, kontrak Flask, maupun model ML.

Dependency datastore-preferences 1.1.7 dikelola version catalog. File pengenalan dikecualikan melalui backup_rules.xml dan data_extraction_rules.xml untuk cloud backup dan device transfer. Referensi library: https://developer.android.com/jetpack/androidx/releases/datastore#1.1.7


## Implementasi visual tahap 1

`ui/theme` mendefinisikan palet terang/gelap statis, bentuk dan font lokal. `ui/components/BrandComponents.kt` menyediakan logo, header, kartu, tombol, badge dan navigasi bawah. `ui/home`, `ui/introduction`, `ui/about` menggunakan komponen tersebut. Resource font merupakan instans statis bobot 400/600/700 dari font resmi Google Fonts, sehingga mendukung minSdk 24; lisensi disertakan pada `assets/licenses`.

`ThemeViewModel` membaca/menyimpan `DataStoreThemePreferences` pada file `appearance.preferences_pb`, key `dark_theme`; file ini terpisah dari `introduction.preferences_pb` dan Room. MainActivity menunggu pembacaan awal sebelum menampilkan konten, menerapkan tema serta kontras ikon system bars. Nilai tema baru diterapkan setelah persistensi berhasil. Read failure memakai terang; write failure mempertahankan nilai sebelumnya.

Navigation Compose memakai `launchSingleTop`, `popUpTo(home) { saveState = true }`, dan `restoreState` untuk tab utama. History ViewModel dan state scroll tersimpan saat pergantian tab. Back pada tab selain Home diarahkan ke Home. Pengenalan tetap gate di luar NavHost; Form/Hasil/Detail tidak memakai navigasi bawah. Skema Room dan kontrak jaringan tidak berubah.


## Tahap 2: periode lokal dan snapshot hasil

PredictionFormState memuat recordingMonth/recordingYear dan periodError. ViewModel menginisialisasi sekali dan menyimpan pilihan di SavedStateHandle. Saat submit valid, nilai periode disalin sebelum coroutine request. PredictionInput/DTO tetap tujuh field; HistoryRecord memuat dua atribut periode nullable. ResultScreen menerima HistoryRecord agar identitas, waktu dan periode selalu sesuai hasil.

PeriodPicker mengelola draft dialog bulan/tahun, validasi dan penerapan; recordingPeriodLabel dipakai Hasil/Detail. Prediksi Ulang kembali ke rute Form yang ada, lalu membersihkan hasil sementara tanpa mengubah input atau memanggil Repository.

HistoryEntity dan schema Room v2 menambah recordingMonth INTEGER NULL dan recordingYear INTEGER NULL. MIGRATION_1_2 memakai ALTER TABLE, terdaftar pada provider database; tidak ada migrasi destruktif. Data lama tidak diisi periode buatan. Filter waktu tetap memakai recordedAt.


## Tahap 3: filter kategori dan hapus dari daftar

HistoryFilter.kategoriOat nullable memperluas matches() dengan kesamaan kategori snapshot; Repository tetap memakai satu emisi daftar/total/nama Puskesmas dari Room. HistoryViewModel menyimpan kategoriOat melalui SavedStateHandle. HistoryDeletionState terpisah dari HistoryListState agar refresh/emisi Room tidak menghapus status operasi. delete(id) berjalan di viewModelScope, memblokir operasi ganda dan menyampaikan error/konfirmasi ke UI. Rotasi tidak memulai operasi baru.

HistoryScreen dan HistoryDetailScreen memakai HistoryComponents untuk badge klasifikasi, ringkasan hasil serta konfirmasi hapus. Navigasi bawah dapat dinonaktifkan selama penghapusan daftar; Back dan pembukaan Detail juga diblokir. HistoryDetailViewModel tetap mengelola penghapusan Detail. Label/satuan hanya tampilan; schema Room tetap versi 2, tanpa perubahan kontrak jaringan atau dependency.


## Ekspor panduan offline

AndroidGuideExporter mengimplementasikan GuideExporter, membaca asset guides/panduan_penggunaan.pdf dan menulis ke folder Download pada Dispatchers.IO. MediaStore Downloads pada API 29+ memakai IS_PENDING hingga selesai; API 24–28 mereservasi file baru secara atomik di folder Download publik, memindainya, dan mengembalikan URI aman melalui FileProvider. Nama tambahan memakai waktu/ID unik. Destinasi parsial yang dibuat operasi dihapus bila gagal/cancel; file lain tidak disentuh.

GuideViewModel menyimpan metadata/loading/status izin/hasil/error dan URI file, menjalankan ekspor lewat viewModelScope dan memblokir permintaan ganda. GuideDownloadSection menghubungkan launcher izin Android, notifikasi unduhan yang dapat diketuk untuk membuka pemilih aplikasi, dan tombol pengaturan; GuideDownloadCard hanya merender state. Android 13+ meminta POST_NOTIFICATIONS sebelum ekspor; jika ditolak, file tetap disimpan tanpa notifikasi. ViewModel mengikuti lifecycle destination Tentang sehingga rotasi tidak memulai operasi baru. File PDF sumber dan salinan asset dipertahankan identik.

ClassificationBadge pada ui/components menjadi sumber pemetaan warna dan label bersama bagi Hasil, Riwayat, dan Detail. Bentuk/ukuran komponen Riwayat yang ringkas tetap dipertahankan. Tidak ada perubahan API, Room atau dependency.


## Bahasa tampilan netral

Revisi 22 September 2026 memisahkan teks presentasi dari sumber data. Compose tidak lagi menampilkan pemberitahuan dummy pada Hasil, kartu Riwayat dan Detail. Teks Form/Hasil berasal dari string resources dengan bahasa penggunaan. Pembaruan 30 Oktober mengganti service Flask lokal dengan pipeline model nyata; sinkronisasi Tahap 3–4 kemudian menambahkan sumber `MODEL` untuk respons API dan mempertahankan catatan lama berstatus `DUMMY` tanpa mengubah isi legacy-nya.


## Validasi, sesi form, dan snackbar — 22 September 2026

PredictionViewModel memisahkan clearPrediction() dan resetForm(); leaveForm()/repeatPrediction() membersihkan request/hasil sementara serta SavedStateHandle isian dan periode. formSessionId menjadi key untuk mereset state gulir Compose. Snapshot tersimpan tetap immutable. PredictionScreen menggunakan BringIntoViewRequester per field dan periode, dipicu penghitung submit setelah layout error. UiNotice membawa ID unik; NoticeEffect menjalankan snackbar dalam scope komposisi, mengonsumsi event saat mulai, serta membatalkan saat lifecycle STOP/dispose. Penghapusan dari daftar memakai ID catatan sebagai identitas event selesai; simpan dan hapus Detail memakai UUID. API/Room tidak berubah.


## Urutan dan periode prediksi — 22 September 2026

HistoryFilter.sort memakai HistorySort NEWEST/OLDEST; applyTo menyaring lalu mengurutkan timestamp/ID secara konsisten pada Repository. HistoryViewModel menyimpan pilihan di SavedStateHandle. LazyListState kembali ke atas hanya ketika urutan berubah. nextPredictionPeriod menghitung metadata turunan tanpa tanggal perangkat dan tanpa kolom Room baru. UI memakai formatter bersama. Snapshot dan schema Room versi 2 tetap.
## Analisis dan ekspor OOXML — 30 September 2026

`AnalysisScreen → AnalysisViewModel → AnalysisRepository` memisahkan presentasi grafik, filter/operasi unduhan, dan sumber data. `CsvAnalysisDataset` membaca salinan asset `modeling_dataset.csv`; parser memetakan lima prediktor dan permintaan aktual Y untuk visualisasi, tanpa menjalankan prediksi atau pelatihan. Sumber Riwayat mengamati Flow `HistoryRepository`, mengecualikan kategori selain OAT Dewasa Kategori 1 dan memilih snapshot valid terakhir per Puskesmas/periode, tanpa mutasi catatan. `AnalysisModels` menyimpan input Long dan nilai permintaan aktual dalam BigDecimal agar digit tidak hilang; keluaran Double dipertahankan sebagai nilai desimal. Flow diproses pada dispatcher Default.

ViewModel menyimpan filter di SavedStateHandle. LazyColumn dan scroll grafik memakai state Compose yang dapat dipulihkan. Canvas hanya menghubungkan dua bulan berurutan yang sama-sama tersedia; nilai/label juga tersedia sebagai teks dan semantics. ViewModel menahan snapshot ekspor sebelum dialog izin dan sepanjang operasi IO; pergantian konfigurasi tidak memulai ekspor baru. Navigasi tab/Back diblokir selama operasi tersebut.

`AnalysisWorkbookWriter` menulis ZIP/XML SpreadsheetML, tiga sheet dan drawing/chart dengan referensi sel internal, tanpa macro/tautan eksternal. Data kolom H menyimpan angka lengkap sebagai teks, I menyimpan angka grafik dengan presisi 15 digit; sel kosong tidak ditulis sebagai nol. `AndroidAnalysisExporter` menulis pada dispatcher IO ke MediaStore Downloads (API 29+) atau Download publik (API 24–28), memakai nama UUID, mengembalikan URI aman melalui FileProvider pada API 24–28, dan membersihkan hanya entri/file parsial operasinya jika gagal. Setiap ekspor sukses memicu notifikasi yang membuka pemilih aplikasi Android untuk workbook; Android 13+ meminta POST_NOTIFICATIONS sebelum ekspor dan tetap menyimpan file jika izin ditolak. Tidak ada dependency spreadsheet desktop dalam APK. Room tetap versi 2; backend, prediktor ML, PDF dan Word tidak berubah.
