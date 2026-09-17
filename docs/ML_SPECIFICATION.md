# Spesifikasi Machine Learning — OAT Prediction

## Status dan Referensi

Draft baseline berdasarkan lampiran requirement, [PRD.md](PRD.md), dan [AGENTS.md](../AGENTS.md). Model belum dibuat. Pengembangan hanya dimulai pada fase ML dan setelah diminta eksplisit.

## Model dan Target

| Model | Tujuan | Keluaran |
| --- | --- | --- |
| Multiple Linear Regression | Prediksi numerik Y = Permintaan Obat | Nilai numerik; satuan belum ditetapkan |
| Decision Tree CART | Klasifikasi tingkat kebutuhan | Rendah, Sedang, Tinggi |

MLR secara konseptual: Ŷ = β0 + β1X1 + … + βnXn. Koefisien hanya diperoleh melalui training aktual, bukan ditetapkan dalam dokumentasi.

Input → pipeline MLR → prediksi numerik.
Input → pipeline CART → kelas kebutuhan.

Kedua model terpisah dan menggunakan fitur sesuai training masing-masing. CART bukan formatter hasil MLR. Jangan mengganti MLR; jangan mengganti CART tanpa persetujuan.

## Calon Fitur dan Waktu Ketersediaan

Calon variabel: Puskesmas, Kategori OAT, Stok Awal, Penerimaan Obat, Pemakaian Obat, Jumlah Pasien TBC, dan Permintaan Sebelumnya (X7, bersyarat). Daftar predictor final menunggu validasi dataset dan metodologi.

Puskesmas dan Kategori OAT adalah variabel kategorikal; pilihan dan encoding berasal dari pipeline yang disahkan. Android mengirim nilai sesuai kontrak dan tidak membuat kode kategori arbitrer.

Bulan/Tahun merupakan atribut pencatatan/temporal, bukan otomatis predictor. Target periode yang sama dilarang menjadi predictor. X7 hanya boleh digunakan bila secara metodologis dipertahankan dan tersedia sebelum periode target. Periksa waktu ketersediaan seluruh fitur untuk mencegah leakage.

## Pembentukan Label CART

Label Rendah/Sedang/Tinggi mengikuti aturan penelitian yang disahkan. Jangan menciptakan threshold.

Quartile hanya opsi bersyarat dari lampiran, bukan metode yang telah dipilih. Jika digunakan, batas dihitung menggunakan data training sesuai prosedur penelitian, lalu diterapkan pada validasi/test/data baru; bukan dihitung dari seluruh dataset.

## Data dan Preprocessing

Data berasal dari sumber penelitian sebenarnya. Jangan membuat dataset fiktif, mengisi nilai hilang secara arbitrer, mengubah angka demi performa, menghapus outlier tanpa dasar, atau menyajikan data sintetis sebagai data asli. Setiap perubahan preprocessing memiliki alasan terdokumentasi.

Kosong tidak sama dengan nol. Nol hanya untuk kejadian/transaksi yang memang nol menurut sumber. Nilai tidak diketahui ditangani melalui prosedur missing-value penelitian yang belum ditetapkan.

Pipeline mencakup pembersihan, penanganan missing value, encoding kategori, persiapan fitur, pemisahan data, training, dan evaluasi. Diagram konseptual dalam lampiran bukan izin mempelajari parameter preprocessing dari seluruh dataset: transformasi yang belajar dari data harus di-fit pada training dan digunakan konsisten pada validasi/test/inferensi. Metode pembagian data belum ditetapkan.

Preprocessing inferensi harus sesuai dengan training, termasuk mapping kategori, urutan fitur, dan transformasi tiap model. Kebijakan kategori tidak dikenal belum ditetapkan.

## Evaluasi yang Direncanakan

| Model | Metrik dari requirement |
| --- | --- |
| MLR | MAE, MSE, RMSE, R² |
| CART | Accuracy, Precision, Recall, F1-score, Confusion Matrix |

Nilai metrik hanya dilaporkan dari eksperimen aktual menggunakan data penelitian yang sah. Belum ada hasil atau target skor. Metode averaging multiclass, split, validasi, parameter model, dan kriteria penerimaan masih harus ditetapkan.

Dummy untuk UI/API/koneksi/state tidak boleh dipakai untuk menghitung atau mengklaim evaluasi penelitian.

## Persistensi dan Integrasi

Model hasil training disimpan dan dimuat backend Flask. Backend memvalidasi input, menerapkan preprocessing yang sesuai, menjalankan kedua model, lalu mengembalikan hasil sesuai [API_CONTRACT.md](API_CONTRACT.md).

Format artefak, versi, mekanisme pemuatan, lingkungan runtime, perilaku jika model gagal, pembulatan, dan penanganan prediksi negatif belum ditetapkan. Pembaruan model tidak memerlukan perombakan UI selama kontrak kompatibel.

## Keputusan Penelitian yang Masih Terbuka

Sumber dan cakupan dataset, periode/satuan observasi, satuan obat, horizon target, fitur final, X7, label/threshold, encoding, missing value/outlier, split, hyperparameter, dan batas penerimaan. Detail ini harus ditentukan dari penelitian, bukan ditebak saat pengembangan aplikasi.
