# Product Requirements Document — OAT Prediction

## Status dan Sumber

**Versi:** 1.1 — diselaraskan dengan metodologi terbaru pada [`SKRIPSI/SKRIPSI.docx`](../SKRIPSI/SKRIPSI.docx) dan keputusan pengguna. Dokumen ini menjadi sumber requirement produk; status requirement tidak berarti seluruh fitur final sudah diimplementasikan.

Project kini memiliki pengenalan, Home, Form Prediksi, Result Screen, Riwayat lokal, Detail Riwayat, Analisis, dan panduan. Submit valid mengirim POST /predict melalui ViewModel, Repository, dan Retrofit ke Flask dengan pipeline MLR dan CART. Sumber Dataset pada Analisis menggunakan 224 baris lengkap dari dataset penelitian; data Riwayat tetap berasal dari catatan lokal.

### Acuan metodologi yang berlaku

Model memakai lima prediktor: Puskesmas, pemakaian, sisa stok, penerimaan, dan kasus baru TBC. `Y` adalah permintaan yang tercatat pada formulir LPLPO bulan `t` untuk kebutuhan `t+1`; Y historis menjadi target pelatihan/evaluasi dan tidak diminta oleh aplikasi. Kategori data dibatasi pada OAT Dewasa Kategori 1 dan bukan input model. Bulan/tahun formulir adalah metadata untuk menunjukkan horizon bulan berikutnya. Spesifikasi, pembagian waktu, penanganan nilai kosong, dan metrik model mengikuti [ML_SPECIFICATION.md](ML_SPECIFICATION.md).

Catatan implementasi bertanggal setelah bagian requirement utama merekam keadaan/keputusan pada tahap terdahulu. Jika catatan tersebut masih menyebut kategori sebagai input atau X7, itu merupakan jejak implementasi lama dan bukan requirement metodologi yang berlaku.

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
| FR-01 | Beranda menampilkan identitas aplikasi, kartu “Prediksi Kebutuhan OAT”/“Mulai Prediksi”, navigasi bawah, serta angka evaluasi model yang diberi label terpisah untuk MLR dan CART. |
| FR-02 | Form menyediakan input pada tabel di bawah. |
| FR-03 | Puskesmas memakai dropdown dari data penelitian tervalidasi, bukan teks bebas atau nama produksi fiktif. |
| FR-04 | Dataset dibatasi pada OAT Dewasa Kategori 1; kategori bersifat tetap dan tidak menjadi field form atau prediktor. |
| FR-05 | Empat input numerik wajib berupa bilangan bulat nonnegatif: pemakaian, sisa stok, penerimaan, dan kasus baru TBC. Nilai kosong tidak boleh dianggap nol. |
| FR-06 | Android memvalidasi sebelum mengirim dan menampilkan error yang mudah dipahami, misalnya “Sisa Stok wajib diisi”. Backend wajib memvalidasi ulang. |
| FR-07 | Android mengirim POST /predict sesuai API_CONTRACT.md; nama field konsisten sampai preprocessing backend. |
| FR-08 | Respons sukses memuat success, prediksi_mlr, dan klasifikasi_dt. Respons final berasal dari model terlatih; dummy hanya untuk pengembangan/pengujian. |
| FR-09 | Hasil menampilkan prediksi permintaan beserta satuan yang ditetapkan dan kelas kebutuhan dengan jelas. Istilah teknis seperti koefisien, Gini, atau entropy tidak ditampilkan kepada pengguna biasa tanpa kebutuhan khusus. |
| FR-10 | Aplikasi menangani loading, success, dan error. Kegagalan API biasa tidak menyebabkan crash; tampilkan pesan manusiawi dan arahan mencoba kembali. |
| FR-11 | Prediksi sukses dapat disimpan sebagai riwayat berisi tanggal/waktu, bulan formulir, Puskesmas, lima prediktor, nilai MLR mentah, dan kelas CART. |
| FR-12 | Detail riwayat menampilkan input dan kedua hasil yang tersimpan. Y aktual dan permintaan periode sebelumnya bukan input aplikasi. |

### Form Prediksi

| Field | Kontrol | Wajib |
| --- | --- | --- |
| Puskesmas | Dropdown | Ya |
| Pemakaian | Angka | Ya |
| Sisa Stok | Angka | Ya |
| Penerimaan Obat | Angka | Ya |
| Kasus Baru TBC | Angka | Ya |
| Bulan/Tahun formulir `t` | Pemilih periode | Ya; metadata, bukan prediktor |

Puskesmas dikirim sebagai string kategorikal dan di-one-hot encode di pipeline model. Empat input numerik memakai satuan kotak untuk variabel obat dan jumlah orang untuk kasus baru TBC. Bulan/Tahun menunjukkan bulan formulir `t`; aplikasi menampilkan hasil untuk `t+1` dan tidak mengirim periode tersebut sebagai prediktor. Y aktual tidak diminta saat prediksi.

## Requirement Nonfungsional

### Keputusan UI tahap Home dan Form

Puskesmas dipilih dari tujuh nama pada dataset yang telah diverifikasi. Ruang lingkup data adalah OAT Dewasa Kategori 1; tidak ada dropdown kategori karena penelitian tidak memodelkan kategori OAT sebagai variabel.

Semua lima prediktor dan periode formulir wajib diisi. Input jumlah obat berupa bilangan bulat nonnegatif dalam kotak; kasus baru TBC berupa hitungan orang. Nilai kosong tidak berubah menjadi nol. Nol hanya sah bila sumber memang mencatat nol.

Dropdown awalnya belum dipilih dan input angka kosong. Tombol Prediksi memvalidasi sebelum request. Saat Loading, tampilkan “Mengirim permintaan…” dan nonaktifkan seluruh input/submit. Kegagalan koneksi, timeout, HTTP, atau respons tidak valid ditampilkan pada form; isian tetap tersedia dan pengguna dapat mencoba kembali. Back dari form membatalkan request; rotasi tidak membatalkannya.

Respons sukses membuka Result sekali per submit, dengan prediksi MLR mentah dari API dan kelas CART. Pada tahap dummy, label DUMMY / TEST ONLY wajib tampil. Pada sistem final, API dan riwayat mempertahankan nilai MLR mentah; layar aplikasi menampilkan jumlah kotak yang dibulatkan ke bilangan bulat terdekat dan membatasi nilai tampilan minimum nol. Pembulatan/batas tampilan tidak mengubah prediksi yang dipakai menghitung metrik penelitian. Kembali dari Result menuju Home tanpa menyimpan; isian form tetap dipertahankan.

Tombol Riwayat membuka catatan yang disimpan manual melalui Room. Isian dipertahankan saat kembali dari Home dan saat Activity dibuat ulang.

### Perilaku produk final

- UI sederhana, sesuai penggunaan administratif, hierarki informasi jelas, tipografi konsisten, dan label tombol mudah dimengerti.
- Request asynchronous; UI tidak membeku saat menunggu server.
- Prediksi memerlukan koneksi ke backend. Jika backend tidak tersedia, prediksi gagal dengan pesan yang jelas.
- Riwayat lokal dapat dibaca, difilter, dan dihapus offline menggunakan Room.
- Model dapat diperbarui di backend tanpa membangun ulang UI selama kontrak tetap kompatibel.
- Secret, password, credential, dan private key tidak disimpan dalam source. URL backend dikelola melalui konfigurasi environment/build.
- Dummy harus berstatus DUMMY / TEST ONLY dan tidak menjadi hasil aktual, dataset penelitian, metrik, isi BAB IV, atau kesimpulan penelitian.

## Di Luar Lingkup

Tidak mencakup login kompleks, multi-role, integrasi Dinas Kesehatan/rumah sakit/WhatsApp, pengadaan atau distribusi obat, pembayaran, notifikasi prediksi otomatis atau terjadwal, chatbot, diagnosis TBC, prediksi jumlah pasien, rekam medis lengkap, penyimpanan NIK/nama/alamat/foto pasien, infrastruktur cloud kompleks, atau administrasi Puskesmas di luar prediksi.

Analisis lokal, grafik, panduan, dan ekspor yang sudah ada merupakan fitur pendukung aplikasi; tidak menjadi hasil model atau bukti penelitian. Login dan pengelolaan multi-Puskesmas berada di luar lingkup. Deployment Flask ke PythonAnywhere termasuk target penyelesaian; infrastruktur cloud lain tetap di luar lingkup.

## Kriteria Penerimaan Utama

| ID | Kriteria |
| --- | --- |
| AC-01 | Pengguna dapat membuka Home. |
| AC-02 | Pengguna dapat membuka form prediksi. |
| AC-03 | Pengguna dapat mengisi lima prediktor dan periode formulir; Y aktual tidak diminta. |
| AC-04 | Input tidak valid ditolak. |
| AC-05 | Android dapat mengirim POST /predict. |
| AC-06 | Flask dapat menerima request. |
| AC-07 | Backend menghasilkan respons sesuai kontrak. |
| AC-08 | Android menampilkan hasil. |
| AC-09 | Riwayat dapat menyimpan prediksi sukses. |
| AC-10 | Backend menggunakan MLR sebenarnya pada fase integrasi model final. |
| AC-11 | Backend menggunakan CART sebenarnya pada fase integrasi model final. |
| AC-12 | Kegagalan API ditangani tanpa crash. |
| AC-13 | Data uji mengikuti delapan bulan terakhir secara kronologis; metrik hanya berasal dari evaluasi nyata. |
| AC-14 | Request model hanya memuat lima X; periode formulir adalah metadata dan tidak menjadi prediktor. |

Lulus integrasi dummy tidak memenuhi AC-10/AC-11. Keterlacakan requirement → implementasi → test → bukti dicatat dalam [TESTING_PLAN.md](TESTING_PLAN.md).

## Keputusan Terbuka dan Penyesuaian Sumber

- Dataset final menggunakan OAT Dewasa Kategori 1. Lima prediktor, definisi Y, split waktu, CV, penanganan missing, dan metrik mengikuti [ML_SPECIFICATION.md](ML_SPECIFICATION.md).
- API dan riwayat menyimpan nilai MLR mentah. Aplikasi membulatkan nilai tampilan ke bilangan bulat dan membatasi nilai tampilan minimum nol; evaluasi tetap memakai prediksi mentah.
- Riwayat disimpan manual melalui Room dan tersedia offline; tidak ada penghapusan otomatis. Penghapusan per catatan memerlukan konfirmasi pengguna.
- PythonAnywhere merupakan target deployment Flask; detail akun/URL dan autentikasi API perlu ditetapkan saat konfigurasi.
- Lampiran menomori fase berbeda; urutan 16 fase AGENTS.md tetap menjadi acuan.
- Diagram berantai MLR → DT pada bagian fase lampiran tidak dijadikan alur model karena aturan eksplisit menetapkan dua model terpisah.
- Pernyataan Android tidak mengetahui dummy ditafsirkan sebagai kompatibilitas kontrak dan tidak bergantung pada implementasi model. Status DUMMY / TEST ONLY tetap wajib; penandanya berupa label tetap di Result Screen serta header X-OAT-Mode pada backend dummy.

## Referensi Teknis

[ARCHITECTURE.md](ARCHITECTURE.md) mengatur arsitektur, [API_CONTRACT.md](API_CONTRACT.md) mengatur komunikasi, [ML_SPECIFICATION.md](ML_SPECIFICATION.md) mengatur ML, dan [TESTING_PLAN.md](TESTING_PLAN.md) mengatur pengujian. Konflik requirement harus dilaporkan dan diklarifikasi bila perlu, bukan diubah diam-diam.

## Riwayat Lokal — keputusan tahap implementasi

Catatan pada bagian ini dan subbagian bertanggal berikut merekam perubahan UI/fitur yang telah diuji pada tahap sebelumnya. Beberapa catatan memakai kontrak tujuh field lama; catatan tersebut bukan definisi prediktor final. Implementasi kontrak akan diselaraskan pada tahap API berikutnya.

- Hasil menyediakan Simpan dan Kembali ke Home. Simpan sukses menuju Home dengan konfirmasi; Back Android/Kembali ke Home tidak menyimpan. Form/Hasil dikeluarkan dari back stack. Selama penyimpanan, kontrol dan navigasi keluar dinonaktifkan; kegagalan tetap di Hasil dengan opsi mencoba kembali.
- Riwayat berurutan terbaru, menampilkan tanggal/jam, Puskesmas, kategori OAT, Dummy, dan Detail. Bedakan database kosong dari hasil filter kosong; sediakan loading/error/retry.
- Detail memuat tanggal/jam lengkap, tujuh input yang dikirim, kedua hasil, dan penanda dummy. Waktu merupakan penerimaan respons, bukan periode target penelitian. Kembali menuju daftar.
- Hapus Riwayat hanya per catatan di Detail, melalui konfirmasi Batal/Hapus. Sukses kembali ke daftar dengan pesan; gagal tetap di Detail. Tidak ada Hapus Semua, edit, ekspor, atau sinkronisasi.
- Filter waktu: Semua Waktu, Hari Ini, 7 Hari Terakhir (hari ini + enam hari kalender sebelumnya), Bulan Ini. Zona waktu perangkat menentukan batas kalender. Filter dihitung ulang ketika halaman aktif.
- Filter Puskesmas: Semua Puskesmas dan nama unik dari data tersimpan. Filter kelas: Semua Klasifikasi, Rendah, Sedang, Tinggi. Ketiga filter menggunakan irisan; pilihan dipertahankan pada rotasi/kembali dari Detail. Jika seluruh catatan Puskesmas terpilih dihapus, pilihan kembali ke Semua Puskesmas.
- Input/hasil disimpan sebagai snapshot dengan ID unik; retry tidak menduplikasi ID. Request baru memiliki ID baru. Semua hasil fase ini menyimpan sumber DUMMY, bukan menyimpulkannya dari nilai hasil. Label dummy tampil di daftar dan Detail; penanda tidak boleh diubah menjadi hasil nyata kemudian.
- Data lokal tetap tersedia setelah aplikasi ditutup. Database dikecualikan dari backup cloud/transfer perangkat; uninstall atau hapus data aplikasi menghapus riwayat. Hasil yang belum disimpan hanya berada di memori.

## Pengenalan saat instalasi pertama

Sebelum Home, aplikasi menampilkan satu halaman pengenalan setelah splash bawaan Android. Tidak ada timer. Identitas “OAT Prediction” disertai tujuan aplikasi, langkah singkat penggunaan, tanpa kartu pemberitahuan dummy pada halaman pengenalan. Tombol utama Mulai Aplikasi menyimpan penyelesaian pengenalan sebelum membuka Home.

Status intro_completed disimpan di Preferences DataStore terpisah dari Room. Missing/false menampilkan pengenalan; true langsung membuka aplikasi. Loading ditampilkan saat pembacaan agar Home tidak berkedip. Kegagalan baca/simpan menampilkan pesan dan Coba lagi; kegagalan tidak dianggap selesai. Tombol dinonaktifkan selama menyimpan. Rotasi mempertahankan layar aktif. Back dari pengenalan keluar secara normal; pengenalan tidak berada di back stack Home.

Jika pengguna keluar sebelum Mulai, halaman muncul lagi. Pengguna versi sebelumnya melihat pengenalan sekali karena flag belum ada. File preferensi dikecualikan dari backup cloud/transfer; uninstall atau hapus data aplikasi meresetnya. Konten menggunakan Material 3, tema terang/gelap, dapat digulir untuk layar kecil/font besar, dan memakai logo yang disediakan pada tahap redesign.


## Redesign tahap 1 — identitas visual dan navigasi

- Acuan: screenshot dan HTML dalam `Design Stitch/`, dengan `DESIGN.md` sebagai referensi komponen. Aplikasi tetap native Compose. Logo halaman memakai `LOGO APLIKASI.png`; ikon launcher tidak diubah.
- Palet terang mint `#E7FFF5`, emerald `#004C3A`, aksen `#00664F`, kartu putih membulat. Plus Jakarta Sans regular/semibold/bold dibundel offline beserta lisensi OFL.
- Tema default terang, tanpa dynamic color. Toggle pada Beranda menyimpan tema terang/gelap di Preferences DataStore. Kegagalan simpan mempertahankan tema sebelumnya dan menampilkan pesan; gagal baca menggunakan terang.
- Pengenalan mempertahankan gate sekali setelah instalasi, retry dan loading. Identitas Pengenalan: “UPTD PUSKESMAS KOTA LHOKSEUMAWE”, menggunakan resource terpisah dari konteks penelitian di Tentang. Tombol Mulai Aplikasi membuka Home setelah flag tersimpan. Versi berasal dari build.
- Beranda menampilkan sapaan Petugas Farmasi, kartu Prediksi Kebutuhan OAT, dan kartu evaluasi yang memuat CART Accuracy 67,86% serta MLR MAE 4,5050 kotak, RMSE 6,1588 kotak, dan R² −0,1371. Keterangan menyebut bahwa metrik berasal dari data uji kronologis sebanyak 56 baris; metrik regresi tidak disebut akurasi. Angka merupakan hasil evaluasi hold-out yang sudah tercatat, bukan jaminan kinerja prediksi mendatang.
- Tentang berisi logo, versi aktual, tujuan dan konteks skripsi. Badge/paragraf dummy dihapus dari halaman ini; footer rata tengah menampilkan “© 2026 Teknik Informatika Universitas Malikussaleh”. Tidak ada kontak contoh atau dokumen unduhan rekaan.
- Navigasi bawah Beranda–Riwayat–Tentang hanya tampil pada tiga halaman tersebut. Tab tidak menggandakan halaman; filter dan posisi Riwayat dipertahankan. Back dari Riwayat/Tentang menuju Beranda; Back Beranda keluar normal.
- Pada tahap 1, Form, Hasil dan Detail hanya menerima tema bersama. Tahap 2 di bawah menggantikan batas tersebut untuk Form/Hasil, satuan tampilan, metadata periode pencatatan, dan Prediksi Ulang. Redesign Riwayat/Detail, filter kategori, dan Undo belum termasuk tahap ini; periode target penelitian belum ditetapkan.


Revisi teks 21 September 2026 tidak mengubah penanda DUMMY pada Hasil/Riwayat/Detail atau penyimpanan, API, backend, maupun model ML.


## Kartu Metode Algoritma Prediksi — ilustrasi Stitch

Kartu Beranda memuat header metode, subjudul MLR/Decision Tree, dua bagian bernomor dengan badge Prediksi/Klasifikasi, panel mint dan kotak rumus. Rumus MLR ditampilkan dalam bentuk umum `Y = β₀ + β₁X₁ + … + βₚXₚ + e`. Daftar LPLPO menjelaskan Puskesmas, kategori, X3–X6, dan X7 yang berasal dari kolom Permintaan formulir bulan sebelumnya. Karena dataset final hanya menggunakan OAT Dewasa Kategori 1, kategori tetap tercatat tetapi tidak menjadi fitur model. Daftar dua kolom menjadi satu pada lebar panel kurang dari 240 dp atau fontScale ≥ 1,4. Ilustrasi CART memuat Gini(S) = 1 − Σᵢ pᵢ² serta keterangan simbol.

Catatan wajib menjelaskan bahwa rumus kartu adalah ilustrasi umum, bukan daftar koefisien/fitur akhir; untuk formulir periode `t`, X3–X6 berasal dari formulir `t`, X7 berasal dari kolom Permintaan formulir `t−1`, dan Y adalah kolom Permintaan formulir `t` untuk bulan `t+1`. Target pada formulir `t` tidak boleh dipakai kembali sebagai X7. MLR/CART terpisah. Teks riwayat klinis/ambang persediaan pada kartu hanya bagian ilustrasi desain, bukan penetapan aturan klasifikasi penelitian. Tidak mengubah bentuk request API, schema database, atau proses prediksi pada tahap dokumentasi UI ini.


## Redesign tahap 2 — Form dan Hasil

Form mengikuti kartu Stitch dengan identitas Puskesmas, logistik, dan periode pencatatan. Tujuh input API tetap sama; angka obat ditampilkan dalam Kotak, jumlah pasien dalam Pasien. Seluruh field API tetap wajib, bilangan bulat nonnegatif dalam Long. Isian numerik responsif dua kolom bila ruang cukup dan satu kolom untuk font besar. Tidak ada klaim training/akurasi pada catatan form.

Periode pencatatan bulan–tahun wajib untuk hasil baru, default bulan/tahun perangkat dan dapat diubah melalui dialog. Tahun adalah bilangan bulat 1–9999 (batas teknis). Pilihan tersimpan melalui SavedStateHandle, tidak direset ketika membuka form kembali. Periode adalah metadata lokal, bukan horizon prediksi atau predictor; tidak dikirim ke Flask.

Hasil memakai snapshot input, output, timestamp penerimaan, serta periode yang ditangkap sebelum request. Tampilkan identitas, kategori, kelas, angka dengan Kotak, periode, dan catatan dummy. Simpan Hasil ke Riwayat menuju Beranda; Prediksi Ulang membuang hasil sementara dan kembali ke Form tanpa request otomatis, dengan isian/periode tetap. Kembali ke Beranda/Back tidak menyimpan. Semua tindakan keluar dinonaktifkan saat penyimpanan.

Room v2 menyimpan bulan/tahun nullable. Migrasi v1 mempertahankan seluruh catatan lama dengan periode null. Detail menampilkan Tidak tersedia untuk periode lama; filter waktu tetap berdasarkan timestamp penerimaan hasil. Tidak mengubah schema JSON atau membuat model ML.


## Redesign tahap 3 — Riwayat dan Detail

Riwayat mengikuti kartu Stitch dengan header berlogo, warna dan teks klasifikasi, waktu penerimaan hasil, kategori, angka prediksi dalam Kotak, penanda Dummy, Lihat Detail dan tombol hapus. Detail menampilkan ringkasan hasil, seluruh input snapshot, periode pencatatan, satuan Kotak/Pasien dan catatan dummy; data lama tanpa periode tetap Tidak tersedia. Tidak menghitung ulang atau membulatkan hasil. Kedua halaman mendukung tema gelap, font besar dan scroll.

Filter Riwayat terdiri atas waktu, Puskesmas, dan tingkat kebutuhan; tidak ada filter Jenis Obat. Catatan lama tetap dipertahankan dan dapat terlihat beserta kategori historisnya. Waktu tetap timestamp penerimaan; daftar Puskesmas berasal dari seluruh riwayat.

Hapus tersedia dari daftar maupun Detail, melalui konfirmasi berisi Puskesmas, tanggal dan peringatan tidak dapat dibatalkan. Batal tidak mengubah data. Selama penghapusan, aksi ganda dan navigasi keluar diblokir. Hapus dari daftar tetap di daftar; hapus dari Detail kembali ke daftar. Kegagalan ditampilkan dan dapat dicoba kembali. Filter tetap, kecuali Puskesmas yang tak lagi memiliki catatan direset ke Semua. Tidak ada filter kategori. Tidak ada Undo atau Hapus Semua. Ketentuan tahap ini menggantikan batas sebelumnya bahwa penghapusan hanya dari Detail.


## Koreksi visual Riwayat mengikuti Stitch

Ketentuan ini menggantikan filter vertikal pada tahap 3. Header Riwayat ringkas memakai logo, judul dan Puskesmas Kota Lhokseumawe, tanpa tombol Kembali. Back Android tetap ke Beranda. Chip satu baris dapat digeser horizontal: Terbaru (penanda urutan), Semua (reset filter), Puskesmas, Tingkat Kebutuhan, Waktu. Chip aktif memakai emerald; nilai default menampilkan nama filter. Posisi horizontal dipertahankan saat rotasi/kembali; baris ikut scroll vertikal daftar.

Kartu memakai margin/padding sekitar 16 dp, radius 12 dp, garis klasifikasi 5 dp hanya di kiri, nama/ikon fasilitas kecil di kiri dan badge kanan atas. Waktu ringkas dan Dummy berada di bawah nama. Panel mint memuat kategori di kiri, angka/Kotak di kanan. Lihat Detail berikon mata dan hapus kotak membulat. Font lokal 18/24/14/12 sp mengikuti referensi tanpa mengganti tipografi global. Font besar dapat membungkus susunan; target sentuh minimal 48 dp. Seluruh operasi dan penanda dummy tetap. Detail tidak didesain ulang pada koreksi ini.


## Panduan PDF offline dan indikator klasifikasi Hasil

Tentang menyediakan kartu Panduan Penggunaan Aplikasi sebelum kredit, mengikuti Stitch: ikon PDF, subjudul Dokumen Panduan (PDF), deskripsi, tombol Unduh Panduan Lengkap dan ukuran aktual asset. PDF berasal dari PANDUAN PENGGUNAAN APLIKASI.pdf, dibundel tanpa perubahan dan dapat disimpan offline langsung ke Download. Setiap unduhan yang berhasil menampilkan notifikasi; mengetuknya membuka pemilih aplikasi Android untuk membuka PDF.

Android 10+ memakai MediaStore tanpa izin penyimpanan; Android 7–9 meminta WRITE_EXTERNAL_STORAGE khusus versi tersebut. Izin ditolak menampilkan pesan/retry; penolakan permanen menyediakan pengaturan aplikasi. Pada Android 13+, aplikasi meminta izin notifikasi saat pengguna memulai unduhan. Unduhan tetap berjalan jika izin notifikasi ditolak atau notifikasi dimatikan di pengaturan; notifikasi hanya tampil jika diizinkan. Mengetuk notifikasi membuka pemilih aplikasi Android dengan tipe PDF. Selama meminta izin atau menyalin, tombol unduh tidak aktif. Sukses menampilkan nama file/lokasi Download; gagal menampilkan pesan dan retry. Unduhan berulang tidak menimpa file lama. Rotasi tidak memulai unduhan baru.

Badge klasifikasi Hasil mengikuti warna Riwayat: Tinggi merah, Sedang biru, Rendah hijau, dengan teks dan variasi terang/gelap. Badge kategori OAT, snapshot, penanda dummy dan proses simpan hasil tetap. Ketentuan ini menggantikan batas tahap awal bahwa Tentang belum menyediakan PDF.


## Revisi bahasa antarmuka — 22 September 2026

Atas instruksi pemilik untuk pemakaian pribadi sebelum rilis, tampilan memakai bahasa netral yang dapat dipertahankan saat model asli tersedia. Ketentuan ini menggantikan kewajiban label/peringatan dummy pada UI di bagian sebelumnya: Hasil memakai judul Hasil Prediksi; Hasil, daftar Riwayat dan Detail tidak menampilkan label Dummy atau penjelasan pengujian. Form memakai petunjuk pengisian; catatan hasil mengarahkan pemeriksaan input/periode dan penyimpanan riwayat. Pesan koneksi/respons ditujukan kepada pengguna, tanpa instruksi konfigurasi backend/USB.

Perubahan hanya presentasi. Backend masih dummy, model belum tersedia, seluruh snapshot tetap menyimpan ResultSource.DUMMY. Riwayat lama tidak dikonversi menjadi hasil nyata. Status tersebut tetap dicatat dalam dokumentasi teknis dan pengujian, bukan dihapus dari data. Rilis untuk petugas tetap menunggu model asli, evaluasi dan penanganan riwayat pengujian secara terpisah. Revisi ini tidak menetapkan metodologi, metrik evaluasi atau kesiapan produksi; kartu Beranda dan angka preview dari tahap sebelumnya tidak diubah.


## Validasi, sesi form, dan snackbar — 22 September 2026

Submit tidak valid menggulir ke field salah pertama sesuai urutan tampilan, termasuk periode, setelah error ditampilkan. Keyboard ditutup tanpa membuka dropdown otomatis; setiap submit mengulang penentuan target. Mengedit input tidak menggulir otomatis. Keluar Form/hasil menuju Beranda mengakhiri sesi: dropdown dan angka dikosongkan, error dibersihkan, periode kembali ke bulan/tahun perangkat. Prediksi Ulang juga memulai form kosong dari atas tanpa request otomatis. Rotasi/latar belakang/dialog dan kegagalan request mempertahankan isian selama sesi form. Ini menggantikan aturan lama mempertahankan isian setelah keluar. Snackbar simpan/hapus tampil sekali dengan durasi Short; pindah halaman menutupnya dan kembali/rotasi tidak mengulang pesan. Operasi berikutnya dapat menampilkan pesan yang sama.


## Urutan dan periode prediksi — 22 September 2026

Terbaru menjadi tombol toggle Terbaru/Terlama berdasarkan timestamp penerimaan, dengan ID sebagai pembeda konsisten. Default terbaru; pilihan bertahan saat rotasi/tab/Detail. Semua hanya mereset filter; pergantian urutan menggulir daftar ke atas. Tombol nonaktif saat menghapus. Periode Prediksi Model pada Form bersifat baca-saja, otomatis satu bulan setelah periode pencatatan yang diterapkan. Hasil, kartu Riwayat dan Detail menampilkan Untuk bulan <bulan tahun> dari snapshot, termasuk riwayat lama yang memiliki periode. Catatan tanpa periode atau di luar batas menampilkan Periode prediksi tidak tersedia. Pencatatan baru maksimal November 9999. Ini menggantikan penanda urutan statis serta keputusan horizon yang sebelumnya belum ditetapkan pada tampilan.
## Analisis dan unduhan Excel — diperbarui 3 November 2026

Navigasi utama menjadi Beranda–Riwayat–Analisis–Tentang. Analisis menampilkan tren Januari–Desember untuk satu Puskesmas serta perbandingan seluruh Puskesmas pada bulan pilihan. Filter mencakup sumber, tahun, dan indikator; tidak ada pilihan jenis obat karena cakupan tetap OAT Dewasa Kategori 1. Sumber Dataset memuat Sisa Stok, Penerimaan Obat, Pemakaian Obat, Kasus Baru TBC, dan Permintaan Aktual (Y); Y aktual bukan prediksi model. Sumber Riwayat menyediakan Prediksi Permintaan dari catatan tersimpan. Satuan Kotak/Pasien tidak dicampur. Nilai lengkap tersedia melalui pemilihan titik/batang dan rincian angka.

Dataset diambil dari salinan `modeling_dataset.csv` yang dibundel sebagai asset aplikasi: 224 baris lengkap pada periode Januari 2023–Juni 2026. Nilai nol dipertahankan; 70 baris sumber yang tidak lengkap tidak diimputasi dan tidak masuk dataset analisis. Periode tanpa observasi tetap kosong pada grafik. Default sumber Dataset, tahun terbaru, Sisa Stok, Muara Satu, dan bulan terakhir yang tersedia pada tahun pilihan.

Sumber Riwayat hanya memasukkan catatan OAT Dewasa Kategori 1 dan memilih catatan terakhir per Puskesmas/periode pencatatan berdasarkan timestamp lalu ID. Catatan kategori lama selain Kategori 1 tetap tersimpan dan terlihat di Riwayat, tetapi dikecualikan dari grafik Analisis dengan jumlah pemberitahuan. Catatan tanpa input atau periode valid juga dikecualikan; bulan kosong bukan nol dan garis tidak melewati celah. Prediksi Permintaan memakai sumbu periode pencatatan dengan keterangan target bulan berikutnya. Simpan/hapus memperbarui analisis tanpa mengubah catatan asli. Filter dan scroll dipertahankan saat rotasi/pergantian tab; Back menuju Beranda.

Setiap kartu menyediakan Unduh Excel jika ada nilai. Workbook offline memiliki Informasi, Data, dan Grafik native yang dapat diedit; isinya snapshot saat ditekan, termasuk sumber, cakupan, indikator, satuan, waktu ekspor, dasar periode, dan aturan pemilihan. Nilai lengkap tersedia sebagai teks; angka grafik di atas presisi 15 digit diberi catatan pendekatan. Nilai hilang tetap kosong. Nama unik disimpan ke Download tanpa menimpa. Setiap ekspor yang berhasil menampilkan notifikasi; mengetuknya membuka pemilih aplikasi Android untuk membuka workbook. Android 13+ meminta izin notifikasi saat ekspor dimulai; jika ditolak, ekspor tetap berjalan tanpa notifikasi. Android 10+ menggunakan MediaStore pending; Android 7–9 meminta izin penyimpanan. Ekspor/permintaan izin mencegah klik ganda dan keluar halaman, dengan status, pesan gagal/retry, serta pengaturan jika izin ditolak permanen. Analisis membaca dataset untuk visualisasi; tidak melatih model atau mengubah kontrak API/schema Room.
## Tooltip tingkat kebutuhan — 30 September 2026

Tekan lama label Rendah, Sedang, atau Tinggi pada Hasil, kartu Riwayat, Detail, serta chip/pilihan filter klasifikasi menampilkan penjelasan. Tooltip tetap terbuka agar dapat dibaca dan ditutup dengan mengetuk di luar atau Back. Ketuk biasa pada filter tetap memilih nilai. Pesan sesuai permintaan pemilik:

- Rendah: “Kebutuhan rendah sehingga permintaan dapat disesuaikan dengan kebutuhan dan stok yang tersedia”.
- Sedang: “Kebutuhan menengah sehingga petugas perlu melakukan perencanaan permintaan secara normal”.
- Tinggi: “Kebutuhan besar sehingga petugas perlu lebih cepat mengajukan permintaan dan memastikan ketersediaan stok”.

Tooltip hanya menjelaskan label; tidak menetapkan ambang kelas atau mengubah prediksi, data, dan model.
