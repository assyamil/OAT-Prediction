# Product Requirements Document — OAT Prediction

## Status dan Sumber

**Versi:** 1.0 — draft baseline requirement. Disusun dari dokumen requirement yang diberikan pemilik proyek pada 18 September 2026 dan [AGENTS.md](../AGENTS.md). Dokumen ini menjadi sumber utama requirement produk; status requirement tidak berarti fiturnya sudah diimplementasikan.

Project saat ini masih starter Android. Pembaruan ini hanya dokumentasi; implementasi mengikuti fase AGENTS.md dan instruksi berikutnya.

## Konteks, Tujuan, dan Pengguna

Penelitian skripsi S1 Teknik Informatika berjudul:

“PREDIKSI DAN KLASIFIKASI KEBUTUHAN OBAT TUBERKULOSIS MENGGUNAKAN METODE MULTIPLE LINEAR REGRESSION DAN DECISION TREE BERBASIS ANDROID”

OAT Prediction membantu petugas Puskesmas yang mencatat atau merencanakan kebutuhan obat TBC untuk memasukkan data, memperoleh prediksi numerik permintaan OAT dan klasifikasi kebutuhan, serta melihat riwayat. Pengguna tidak perlu memahami machine learning.

Platform: Android, Kotlin, Jetpack Compose, Material 3. Backend: Python Flask REST API. MLR memprediksi Permintaan Obat; Decision Tree CART menghasilkan kelas Rendah/Sedang/Tinggi melalui model terpisah.

## Lingkup MVP dan Alur Utama

Home → Form Prediksi → Validasi → Loading → POST /predict → Flask → MLR dan CART → Hasil → Simpan Riwayat → Detail Riwayat.

MVP mencakup Home, Prediksi, Hasil Prediksi, Riwayat, dan Detail Riwayat. Pengaturan dasar hanya jika diperlukan dan rinciannya ditetapkan. Backend mencakup validasi ulang, preprocessing, inferensi dua model, respons JSON, dan penanganan error. Pengembangan ML mencakup preprocessing, training, evaluasi, penyimpanan model, dan inferensi pada fasenya.

## Requirement Fungsional

| ID | Requirement |
| --- | --- |
| FR-01 | Home menampilkan nama aplikasi, deskripsi singkat, tombol “Mulai Prediksi” dan “Riwayat”; tidak menghitung ML. |
| FR-02 | Form menyediakan input pada tabel di bawah. |
| FR-03 | Puskesmas memakai dropdown dari data penelitian tervalidasi, bukan teks bebas atau nama produksi fiktif. |
| FR-04 | Kategori OAT memakai dropdown sesuai dataset final. “OAT Dewasa” dan “OAT Anak” hanya contoh, bukan kategori final. |
| FR-05 | Input numerik menolak teks, format tidak valid, dan nilai kosong untuk field wajib. Angka negatif tidak diterima tanpa alasan domain terdokumentasi; rincian validasi domain harus ditetapkan. |
| FR-06 | Android memvalidasi sebelum mengirim dan menampilkan error yang mudah dipahami, misalnya “Stok Awal wajib diisi”. Backend wajib memvalidasi ulang. |
| FR-07 | Android mengirim POST /predict sesuai API_CONTRACT.md; nama field konsisten sampai preprocessing backend. |
| FR-08 | Respons sukses memuat success, prediksi_mlr, dan klasifikasi_dt. Respons final berasal dari model terlatih; dummy hanya untuk pengembangan/pengujian. |
| FR-09 | Hasil menampilkan prediksi permintaan beserta satuan yang ditetapkan dan kelas kebutuhan dengan jelas. Istilah teknis seperti koefisien, Gini, atau entropy tidak ditampilkan kepada pengguna biasa tanpa kebutuhan khusus. |
| FR-10 | Aplikasi menangani loading, success, dan error. Kegagalan API biasa tidak menyebabkan crash; tampilkan pesan manusiawi dan arahan mencoba kembali. |
| FR-11 | Prediksi sukses dapat disimpan sebagai riwayat berisi tanggal/waktu, Puskesmas, kategori OAT, input utama, prediksi MLR, dan kelas CART. |
| FR-12 | Pengguna dapat membuka detail riwayat untuk melihat tanggal, identitas input, seluruh input yang berlaku, dan kedua hasil. X7 hanya berlaku jika dipertahankan. |

### Form Prediksi

| Field | Kontrol | Wajib |
| --- | --- | --- |
| Puskesmas | Dropdown | Ya |
| Kategori OAT | Dropdown | Ya |
| Stok Awal | Angka | Ya |
| Penerimaan Obat | Angka | Ya |
| Pemakaian Obat | Angka | Ya |
| Jumlah Pasien TBC | Angka | Ya |
| Permintaan Sebelumnya (X7) | Angka | Bersyarat; menunggu validasi dataset dan metodologi |

Bulan/Tahun merupakan atribut pencatatan/temporal, bukan otomatis predictor. Daftar input bukan penetapan final fitur masing-masing model. Target periode yang sama tidak boleh menjadi predictor.

## Requirement Nonfungsional

- UI sederhana, sesuai penggunaan administratif, hierarki informasi jelas, tipografi konsisten, dan label tombol mudah dimengerti.
- Request asynchronous; UI tidak membeku saat menunggu server.
- Prediksi memerlukan koneksi ke backend. Jika backend tidak tersedia, prediksi gagal dengan pesan yang jelas.
- Riwayat lokal dapat dibaca offline apabila rancangan penyimpanan mendukungnya; mekanismenya belum dipilih.
- Model dapat diperbarui di backend tanpa membangun ulang UI selama kontrak tetap kompatibel.
- Secret, password, credential, dan private key tidak disimpan dalam source. URL backend dikelola melalui konfigurasi environment/build.
- Dummy harus berstatus DUMMY / TEST ONLY dan tidak menjadi hasil aktual, dataset penelitian, metrik, isi BAB IV, atau kesimpulan penelitian.

## Di Luar Lingkup

Tidak mencakup login kompleks, multi-role, integrasi Dinas Kesehatan/rumah sakit/WhatsApp, pengadaan atau distribusi obat, pembayaran, notifikasi otomatis, chatbot, diagnosis TBC, prediksi jumlah pasien, rekam medis lengkap, penyimpanan NIK/nama/alamat/foto pasien, infrastruktur cloud kompleks, atau administrasi Puskesmas di luar prediksi.

Dashboard, grafik historis, ekspor laporan, filter riwayat, login, pengelolaan multi-Puskesmas, dan cloud deployment adalah kemungkinan pengembangan setelah MVP stabil; bukan izin implementasi. Dropdown Puskesmas pada form tetap bagian MVP.

## Kriteria Penerimaan Utama

| ID | Kriteria |
| --- | --- |
| AC-01 | Pengguna dapat membuka Home. |
| AC-02 | Pengguna dapat membuka form prediksi. |
| AC-03 | Pengguna dapat mengisi seluruh input wajib yang berlaku. |
| AC-04 | Input tidak valid ditolak. |
| AC-05 | Android dapat mengirim POST /predict. |
| AC-06 | Flask dapat menerima request. |
| AC-07 | Backend menghasilkan respons sesuai kontrak. |
| AC-08 | Android menampilkan hasil. |
| AC-09 | Riwayat dapat menyimpan prediksi sukses. |
| AC-10 | Backend menggunakan MLR sebenarnya pada fase integrasi model final. |
| AC-11 | Backend menggunakan CART sebenarnya pada fase integrasi model final. |
| AC-12 | Kegagalan API ditangani tanpa crash. |

Lulus integrasi dummy tidak memenuhi AC-10/AC-11. Keterlacakan requirement → implementasi → test → bukti dicatat dalam [TESTING_PLAN.md](TESTING_PLAN.md).

## Keputusan Terbuka dan Penyesuaian Sumber

- Dataset, daftar Puskesmas/kategori, satuan obat, horizon prediksi, predictor final, X7, dan pembentukan label belum disahkan.
- Batas numerik, integer/desimal, pembulatan hasil, dan pengecualian nilai negatif belum dirinci.
- Penyimpanan riwayat otomatis atau manual, teknologi penyimpanan, retensi, dan akses offline belum ditetapkan.
- Pengaturan dasar, autentikasi API, lingkungan deployment, timeout, dan target performa numerik belum ditetapkan.
- Lampiran menomori fase berbeda; urutan 16 fase AGENTS.md tetap menjadi acuan. Tidak ada perubahan AGENTS.md.
- Diagram berantai MLR → DT pada bagian fase lampiran tidak dijadikan alur model karena aturan eksplisit menetapkan dua model terpisah.
- Pernyataan Android tidak mengetahui dummy ditafsirkan sebagai kompatibilitas kontrak dan tidak bergantung pada implementasi model. Status DUMMY / TEST ONLY tetap wajib; mekanisme penandanya belum ditetapkan.

## Referensi Teknis

[ARCHITECTURE.md](ARCHITECTURE.md) mengatur arsitektur, [API_CONTRACT.md](API_CONTRACT.md) mengatur komunikasi, [ML_SPECIFICATION.md](ML_SPECIFICATION.md) mengatur ML, dan [TESTING_PLAN.md](TESTING_PLAN.md) mengatur pengujian. Konflik requirement harus dilaporkan dan diklarifikasi bila perlu, bukan diubah diam-diam.
