# Kontrak API — OAT Prediction

## Status dan Otoritas

Kontrak inferensi mengikuti [`SKRIPSI/SKRIPSI.docx`](../SKRIPSI/SKRIPSI.docx), [PRD.md](PRD.md), dan [ARCHITECTURE.md](ARCHITECTURE.md). Flask memuat artefak MLR dan CART utama dengan kontrak lima prediktor. Per 31 Oktober 2026, API aktif di `https://syamildeveloper.pythonanywhere.com`; Android telah diselaraskan dan alur langsung melalui HTTPS lulus uji pada perangkat.

Endpoint inferensi tetap `POST /predict`. Request model final berisi lima prediktor saja. Bulan/tahun formulir adalah metadata lokal Android, bukan predictor dan tidak dikirim pada request model. `Y` aktual, kategori OAT, dan permintaan periode sebelumnya tidak dikirim.

## POST /predict

Request dan response menggunakan JSON. Android mengirim input melalui Retrofit; Flask memvalidasi ulang dan memprosesnya. Header JSON menggunakan Content-Type: application/json.

| Field request | Tipe JSON | Kewajiban dan batas |
| --- | --- | --- |
| puskesmas | string | Wajib; salah satu dari tujuh Puskesmas dalam dataset yang tervalidasi |
| pemakaian | integer | Wajib; jumlah kotak yang digunakan pada formulir bulan `t`, tidak negatif |
| sisa_stok | integer | Wajib; jumlah kotak yang tersisa pada akhir bulan `t`, tidak negatif |
| penerimaan | integer | Wajib; jumlah kotak yang diterima pada formulir bulan `t`, tidak negatif |
| kasus_baru_tbc | integer | Wajib; jumlah kasus baru pada bulan `t`, tidak negatif |

### Contoh Request — fixture teknis

Contoh format untuk pengujian teknis saja; nilainya bukan hasil prediksi atau data penelitian.

~~~json
{
  "puskesmas": "Muara Satu",
  "pemakaian": 70,
  "sisa_stok": 100,
  "penerimaan": 50,
  "kasus_baru_tbc": 30
}
~~~

### Respons Sukses

| Field | Tipe | Makna |
| --- | --- | --- |
| success | boolean | true |
| prediksi_mlr | number | Prediksi numerik mentah Permintaan Obat dalam kotak; API tidak membulatkan atau membatasi nilai |
| klasifikasi_dt | string | Rendah, Sedang, atau Tinggi dari CART |

Contoh berikut hanya menjelaskan bentuk response. Nilai bukan hasil prediksi tetap dan bukan hasil evaluasi penelitian:

~~~json
{
  "success": true,
  "prediksi_mlr": 125,
  "klasifikasi_dt": "Tinggi"
}
~~~

Pada sistem final, kedua hasil berasal dari model terlatih yang terpisah. Angka tidak boleh di-hardcode sebagai hasil aktual. API dan riwayat menyimpan nilai MLR mentah. Android menampilkan jumlah kotak yang dibulatkan ke bilangan bulat terdekat dan membatasi nilai tampilan minimum nol; transformasi tampilan tidak mengubah metrik penelitian.

### Respons Error

Bentuk error validasi dari lampiran:

~~~json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input tidak valid"
  }
}
~~~

Flask menggunakan 415 untuk Content-Type bukan JSON, 400 untuk JSON rusak, dan 422 untuk field hilang, field tambahan, atau nilai tidak valid, dengan kode `VALIDATION_ERROR`. Jika artefak tidak tersedia, `/health` dan `/predict` memberi HTTP 503; kegagalan inferensi memberi HTTP 500. Detail internal hanya dicatat pada error log, bukan dikirim ke klien. Path/metode tidak tersedia menggunakan 404/405 dengan kode `HTTP_ERROR`.

## Validasi dan Kompatibilitas

### DTO target Android

DTO `PredictionRequest` dan model domain `PredictionInput` masing-masing berisi lima prediktor pada tabel berikut, tetap terpisah dari satu sama lain. DTO response menggunakan Kotlin Serialization dan Retrofit melalui adapter remote/Repository; UI memanggil Repository melalui PredictionViewModel setelah validasi.

| Properti request Kotlin | Nama JSON | Tipe Kotlin |
| --- | --- | --- |
| puskesmas | puskesmas | String |
| pemakaian | pemakaian | Long |
| sisaStok | sisa_stok | Long |
| penerimaan | penerimaan | Long |
| kasusBaruTbc | kasus_baru_tbc | Long |

Semua properti request final wajib dan non-null. Puskesmas dikirim sebagai string; empat prediktor numerik dikirim sebagai integer nonnegatif. Kategori OAT, Y aktual, permintaan periode sebelumnya, dan bulan/tahun tidak menjadi field model request. Bulan/tahun tetap berada pada metadata Android untuk menampilkan periode target `t+1`.

Response memiliki `success: Boolean`, `prediksiMlr: Double` (JSON `prediksi_mlr`), dan `klasifikasiDt: String` (JSON `klasifikasi_dt`). Double menerima angka prediksi bulat maupun pecahan tanpa pembulatan eksplisit. DTO ini hanya mewakili bentuk response sukses, bukan payload error di atas. Adapter memetakan Rendah/Sedang/Tinggi ke LOW/MEDIUM/HIGH secara eksplisit; nilai prediksi numerik tidak menentukan kelas.

Decoding JSON standar menolak field wajib yang hilang atau null, bukan menggantinya dengan nol. Contoh angka hanya digunakan sebagai fixture pengujian, bukan hasil penelitian. Converter Kotlin Serialization sudah dipasang pada Retrofit dan model domain, form, validator, serta DTO Android sudah memakai kontrak lima prediktor.

## Implementasi Flask model nyata

Project Python berada dalam `backend/`, terpisah dari `app/` Android. Cara menjalankan, menguji, dan menyiapkan deployment tercatat di [README backend](../backend/README.md) dan [panduan PythonAnywhere](../backend/PYTHONANYWHERE_DEPLOYMENT.md).

`backend/app.py` memvalidasi lima field final, memeriksa tujuh nama Puskesmas yang tervalidasi, lalu meneruskan input ke dua pipeline yang telah dimuat satu kali per worker. Pipeline MLR dan CART tetap independen. Field API `kasus_baru_tbc` dipetakan ke kolom training `kasus_baru`; preprocessing Puskesmas ditangani pipeline. Field tambahan seperti `permintaan_sebelumnya`, kategori OAT, dan Y aktual ditolak.

Respons sukses menggunakan bentuk `success`, `prediksi_mlr`, dan `klasifikasi_dt`. Nilai MLR berasal langsung dari pipeline, tidak dibulatkan atau dibatasi; kelas berasal langsung dari CART. Semua respons memakai `Cache-Control: no-store`. Backend tidak menyimpan request atau memuat dataset.

`GET /health` mengembalikan status `ok` ketika kedua model termuat, atau HTTP 503 jika model belum tersedia. Kegagalan load dicatat pada log agar deployment dapat diperiksa tanpa membuka detail path atau exception ke klien. Android tidak perlu memanggil health check untuk setiap prediksi; endpoint tersedia bagi pemeriksaan operasional.

## Konfigurasi Retrofit development

- `NetworkClient` merupakan lokasi tunggal konfigurasi Retrofit 3.0.0, converter Kotlin Serialization, dan OkHttp 4.12.0.
- Properti Gradle `OAT_API_BASE_URL` menghasilkan `BuildConfig.API_BASE_URL`. Default project adalah `https://syamildeveloper.pythonanywhere.com/`; development lokal dapat menggantinya dengan `http://10.0.2.2:5000/` pada Android Emulator. Backend lokal tidak otomatis dijalankan.
- URL harus HTTP(S), berakhir `/`, dan tanpa credential/query/fragment. API memakai path relatif `predict`; base URL root menghasilkan `POST /predict`.
- Untuk perangkat fisik, gunakan alamat LAN komputer backend yang dapat dijangkau perangkat; `10.0.2.2` bukan alamat host komputer untuk perangkat fisik. IP contoh di bawah harus diganti.

~~~powershell
.\gradlew.bat :app:assembleDebug -POAT_API_BASE_URL=http://192.168.1.10:5000/
~~~

Alternatif persisten untuk development lokal: tetapkan `OAT_API_BASE_URL=http://alamat-komputer:5000/` dalam Gradle user properties (`~/.gradle/gradle.properties`). Argumen `-P` dapat menggantinya untuk satu build. Perubahan URL memerlukan build ulang; jangan menaruh credential di URL.

Manifest memiliki izin INTERNET. Cleartext HTTP hanya diaktifkan pada debug agar alamat LAN development dapat berubah; release menonaktifkannya dan client mensyaratkan base URL HTTPS. Default release sudah HTTPS.

Timeout client: koneksi 10 detik, baca/tulis 30 detik, seluruh call 60 detik. Retry koneksi otomatis dan redirect dimatikan; tidak ada retry/fallback prediksi atau logging body. Nilai ini konfigurasi teknis awal, bukan SLA penelitian.

`PredictionRepositoryProvider.repository` menyediakan instance lazy `RemotePredictionRepository → RetrofitPredictionRemoteDataSource → PredictionApi`. `predict` bersifat suspend. Pembuatan instance tidak mengirim request. Factory ViewModel menginjeksi Repository; submit valid memanggil predict dan meneruskan hasil ke Result Screen.

Adapter final memerlukan nama Puskesmas dan empat angka nonnegatif sesuai DTO lima-prediktor. One-hot encoding dilakukan oleh pipeline backend, bukan Android. Model domain request memakai Long untuk nilai input bilangan bulat. Android telah diselaraskan; base URL default Gradle memakai endpoint HTTPS PythonAnywhere dan tetap dapat ditimpa untuk development lokal.

HTTP non-2xx diteruskan sebagai `HttpException` dengan status/body yang dapat diperiksa pemanggil; UI menampilkan kode HTTP tanpa meneruskan pesan mentah server; tidak menambahkan parsing DTO error baru. JSON tidak valid/incomplete menghasilkan kegagalan decoding. Respons `success=false` yang lengkap, kelas tidak dikenal, atau nilai prediksi tidak finite ditolak. IOException jaringan dan pembatalan coroutine diteruskan, tanpa hasil rekaan. DTO sukses belum mencakup payload error dengan HTTP 200; payload semacam itu akan ditolak, bukan dianggap sukses.

### Aturan integrasi berikutnya

- Android memvalidasi sebelum request; backend tetap memvalidasi ulang.
- Tolak field wajib kosong, teks pada input numerik, dan format tidak valid.
- Nilai negatif tidak diterima tanpa alasan domain terdokumentasi; batas final dan tipe integer/desimal perlu diperinci.
- Missing value tidak otomatis menjadi nol. DTO Android menolak field wajib hilang/null; backend merespons 422 untuk input tersebut.
- Nama field konsisten antara Android, Retrofit, Flask, dan preprocessing.
- Y aktual, permintaan periode sebelumnya, dan kategori OAT tidak dikirim sebagai predictor. Bulan/tahun formulir tetap menjadi metadata lokal; model memakai lima X.
- One-hot encoding Puskesmas dikelola pipeline backend, bukan Android.
- Backend saat ini menjalankan model nyata; request body tidak dicatat dan tidak memuat Y aktual. Jangan memakai contoh fixture API sebagai data atau hasil penelitian.

## Health, Deployment, dan Keputusan API

`GET /health` tersedia untuk memeriksa pemuatan kedua pipeline. Respons normal adalah `{"status":"ok","models":{"mlr":"loaded","cart":"loaded"}}`; status HTTP 503 dan `status:"unavailable"` menandakan model belum siap.

API berjalan pada paket PythonAnywhere Free melalui subdomain HTTPS `https://syamildeveloper.pythonanywhere.com`. Tidak ada autentikasi API pada prototipe ini karena login berada di luar lingkup produk; validasi tetap dilakukan di Android dan Flask. Response error model menggunakan 503 untuk model tidak tersedia dan 500 untuk kegagalan inferensi. Tidak ada endpoint riwayat.

Verifikasi PythonAnywhere pada 31 Oktober 2026: `/health` mengembalikan kedua model berstatus `loaded`; `POST /predict` menerima lima input dan mengembalikan response nyata; nilai negatif pada salah satu input ditolak dengan HTTP 422 dan JSON `VALIDATION_ERROR`. Contoh request adalah fixture teknis, bukan data penelitian. `FlaskIntegrationTest` berhasil mengirim request dari Android melalui HTTPS dan menampilkan kelas CART serta hasil MLR; `HistoryMigrationTest` juga berhasil pada perangkat.

## Integrasi UI dan koneksi USB

Untuk HP USB dengan Flask lokal, jalankan Flask pada 127.0.0.1:5000, lalu `adb reverse tcp:5000 tcp:5000`. Build/install debug dengan `-POAT_API_BASE_URL=http://127.0.0.1:5000/`. Default project tetap URL HTTPS PythonAnywhere; `10.0.2.2` hanya digunakan oleh Android Emulator. Ulangi reverse setelah perangkat tersambung kembali. Langkah lengkap tersedia di README backend.

UI menggunakan state Loading/Error/Success dan tidak mengubah kontrak JSON. Form dan DTO Android mengirim lima prediktor ke base URL HTTPS PythonAnywhere. Tidak ada fallback angka lokal atau request ulang otomatis. Riwayat disimpan manual di Room pada Android; tidak ada endpoint riwayat.


## Metadata lokal Android — tahap 2

recordingMonth dan recordingYear hanya berada pada state form/snapshot/Room, tidak dikirim ke POST /predict atau ditambahkan ke DTO request/response. Keduanya bukan predictor; dipakai Android untuk menampilkan bulan target berikutnya. Nilai model menggunakan satuan kotak untuk OAT dan hitungan orang untuk kasus baru TBC.


## Urutan dan periode prediksi — 22 September 2026

Periode prediksi tampilan ditetapkan satu bulan setelah recordingMonth/recordingYear. Keduanya tetap metadata lokal, tidak masuk JSON POST /predict dan bukan predictor. Model nyata memprediksi Y yang tercatat pada formulir bulan `t`, yaitu permintaan untuk `t+1`; backend tidak menggunakan bulan/tahun sebagai fitur.
