# Arsitektur Teknis — OAT Prediction

## Status dan Referensi

Draft baseline dari requirement pemilik proyek, [PRD.md](PRD.md), dan [AGENTS.md](../AGENTS.md). Komponen di bawah merupakan rencana, kecuali yang disebut sebagai kondisi aktual.

## Kondisi Aktual

Satu module Android app dengan package com.example.oatprediction. Minimum SDK 24, compile/target SDK 37. Test starter tersedia di app/src/test/ dan app/src/androidTest/. Retrofit, Flask, MLR, CART, dan database belum diimplementasikan.

Fondasi fase arsitektur sudah dipisahkan sebagai berikut:

- MainActivity mengatur Activity, tema, dan Scaffold; navigation/OatNavigation menjadi titik masuk satu layar.
- ui/StarterScreen mempertahankan greeting starter dengan teks dari resource. Home produk dan navigasi antarlayar belum dibuat.
- viewmodel/PredictionViewModel menyediakan state awal Idle; PredictionUiState mendefinisikan Idle, Loading, Success, dan Error. ViewModel belum dihubungkan ke UI atau Repository dan belum mengirim prediksi.
- data/model/PredictionInput dan PredictionResult adalah model aplikasi, bukan DTO JSON atau model ML. Double merupakan representasi awal angka, bukan keputusan satuan atau validasi domain. X7 nullable menyatakan keputusan belum tersedia; bukan aturan mengirim null/omission ke API.
- data/repository/PredictionRepository mendefinisikan akses prediksi; RemotePredictionRepository meneruskannya ke PredictionRemoteDataSource melalui constructor injection.
- data/network/PredictionRemoteDataSource merupakan kontrak adapter jaringan yang belum diimplementasikan. Adapter Retrofit kelak harus memetakan JSON, error, dan kelas Rendah/Sedang/Tinggi ke NeedLevel LOW/MEDIUM/HIGH.

Belum ada client HTTP, hasil dummy, atau permintaan jaringan. ViewModel memakai dependency Lifecycle ViewModel eksplisit melalui version catalog. NavHost, serializer, dan wiring fitur ditambahkan pada fase terkait; belum diperlukan untuk satu layar starter.

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

Composable tidak boleh memanggil Retrofit atau HTTP langsung. Operasi jaringan asynchronous agar UI tetap responsif. Input yang tidak valid tidak dikirim. Submit valid memasuki Loading, kemudian Success atau Error.

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

Flask mula-mula dapat menyediakan dummy pada fase yang sesuai, lalu menggunakan pipeline model terlatih dengan kontrak yang tetap kompatibel. Backend menjalankan MLR dan CART sebagai model terpisah, bukan MLR → CART. Input masing-masing mengikuti pipeline training.

Encoding kategorikal dan mapping fitur dikelola backend/pipeline model; Android tidak membuat mapping angka secara arbitrer. Detail ada di [ML_SPECIFICATION.md](ML_SPECIFICATION.md).

## Konfigurasi, Riwayat, dan Kegagalan

URL backend berada dalam konfigurasi environment/build. Credential dan private key tidak ditulis ke source.

Prediksi membutuhkan backend; tidak tersedia fallback prediksi lokal dalam baseline. Kegagalan koneksi menghasilkan Error tanpa crash. Riwayat menyimpan input, waktu, dan kedua hasil sesuai PRD; teknologi penyimpanan, mekanisme penyimpanan, serta jaminan offline belum diputuskan.

Format respons dummy dan model nyata tetap kompatibel sesuai [API_CONTRACT.md](API_CONTRACT.md). Status DUMMY / TEST ONLY tetap harus jelas dalam penggunaan/pengujian. Mekanisme penandaan belum dipilih dan tidak otomatis menambahkan field API.

## Keputusan Teknis yang Belum Ditetapkan

Library navigasi, dependency injection, penyimpanan riwayat, representasi state dalam kode, serializer, timeout/retry, autentikasi, hosting, format artefak model, dan pemuatan/versi model.

Perubahan arsitektur harus beralasan teknis dan terdokumentasi. Implementasi mengikuti fase AGENTS.md serta rencana file, pengujian, risiko, dan asumsi; dokumen ini tidak memulai implementasi.
