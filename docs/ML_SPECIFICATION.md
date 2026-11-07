# Spesifikasi Machine Learning — OAT Prediction

## Status dan sumber keputusan

Spesifikasi ini mengikuti metodologi pada [`SKRIPSI/SKRIPSI.docx`](../SKRIPSI/SKRIPSI.docx). Workbook sumber berada di [`SKRIPSI/data_lplpo_oat_kategori1.xlsx`](../SKRIPSI/data_lplpo_oat_kategori1.xlsx). Workbook telah diaudit tanpa mengubah sumber; proses dan hasil audit tersedia pada [`notebook persiapan data`](../SKRIPSI/OATPrediction_DataPreparation.ipynb), [`skrip persiapan data`](../SKRIPSI/prepare_dataset.py), dan folder [`derived`](../SKRIPSI/derived/). Notebook Colab MLR ([`MLR_Training_Colab.ipynb`](../SKRIPSI/MLR_Training_Colab.ipynb)) telah dijalankan dan paket hasilnya tersimpan di [`mlr_stage5_results`](../SKRIPSI/mlr_stage5_results/). Hasil yang sudah diverifikasi: CV RMSE rata-rata 4,1008 (SD populasi 0,6599); pada 56 baris uji, MAE 4,5050, RMSE 6,1588, dan R² −0,1371. Notebook Colab CART Tahap 6 ([`CART_Training_Colab.ipynb`](../SKRIPSI/CART_Training_Colab.ipynb)) juga telah dijalankan; paket hasilnya ada di [`cart_stage6_results`](../SKRIPSI/cart_stage6_results/). CV Macro-F1 rata-rata 0,5000 (SD populasi 0,1671); pada 56 baris uji, Accuracy 0,6786, Macro-Precision 0,4024, Macro-Recall 0,4196, Macro-F1 0,3846, dan Recall kelas Tinggi 0,1429.

Penelitian dibatasi pada OAT Dewasa Kategori 1 dengan satuan kotak. Nilai kosong tidak diimputasi atau dianggap nol. Nilai nol dipertahankan hanya jika tercatat sebagai nol pada sumber.

## Target, prediktor, dan periode

Satu baris formulir LPLPO bulan `t` memasangkan prediktor bulan `t` dengan nilai permintaan pada formulir yang sama. Permintaan `Y` tersebut ditujukan untuk kebutuhan bulan `t+1`; aplikasi menerima lima prediktor untuk formulir `t` dan menampilkan horizon `t+1`.

| Variabel | Definisi pada formulir bulan `t` | Pemakaian |
| --- | --- | --- |
| X₁ — Puskesmas | Identitas salah satu dari tujuh Puskesmas | Prediktor kategorikal; one-hot encoding untuk MLR dan CART |
| X₂ — Pemakaian | Kotak OAT yang digunakan/dikeluarkan selama bulan `t` | Prediktor numerik |
| X₃ — Sisa Stok | Kotak yang tersisa pada akhir bulan `t` | Prediktor numerik |
| X₄ — Penerimaan | Kotak OAT yang diterima selama bulan `t` | Prediktor numerik |
| X₅ — Kasus Baru TBC | Pasien kasus baru yang memulai pengobatan OAT Dewasa Kategori 1 selama bulan `t` | Prediktor numerik |
| Y — Permintaan Obat | Kotak yang diminta pada formulir bulan `t` untuk kebutuhan bulan `t+1` | Target MLR dan sumber label CART; bukan input inferensi |

Kategori OAT tidak menjadi prediktor karena workbook penelitian hanya mencakup OAT Dewasa Kategori 1. Bulan/tahun formulir adalah metadata untuk menunjukkan bulan pencatatan dan horizon berikutnya, bukan prediktor model. Permintaan periode sebelumnya/X7 tidak digunakan. Model CART dan MLR dilatih terpisah; CART memprediksi kelas langsung dari lima X, bukan dari prediksi MLR.

## Profil dan preprocessing dataset

Workbook mencakup 294 baris untuk tujuh Puskesmas dan 42 bulan, Januari 2023–Juni 2026. Audit menemukan 224 baris lengkap untuk lima X dan Y serta 70 baris tidak lengkap. Tidak ditemukan duplikasi kunci Puskesmas-bulan atau baris persis yang berulang. Pada seluruh 70 baris yang tidak lengkap, empat kolom model `pemakaian`, `sisa_stok`, `penerimaan`, dan `permintaan` kosong; `puskesmas` dan `kasus_baru` tetap terisi. Kolom sumber `stok_awal` dan `persediaan` juga kosong pada 70 baris tersebut.

Kekosongan terkonsentrasi pada periode Januari 2023–Desember 2024 dan mengikuti cakupan pelaporan Puskesmas, bukan pola acak: Muara Satu mulai memiliki baris lengkap pada Desember 2023, Mon Geudong pada Januari 2024, Muara Dua pada Desember 2024, dan Blang Mangat pada Januari 2025; tiga Puskesmas lainnya lengkap sejak Januari 2023. Penghapusan baris tidak lengkap dapat membuat complete-case kurang mewakili periode awal dan Puskesmas dengan cakupan pelaporan yang lebih pendek. Workbook sumber tidak diimputasi atau ditimpa; daftar baris yang dikeluarkan dan kolom kosongnya tersimpan di `SKRIPSI/derived/excluded_rows.csv`.

Pada 224 baris lengkap, nilai permintaan nol berjumlah 168 (75%). Nilai nol dipertahankan sebagaimana tercatat dan tidak otomatis dianggap missing; makna operasionalnya perlu dikonfirmasi kepada pemilik data bila belum pasti. Semua nilai numerik terisi berupa bilangan bulat nonnegatif. Pemeriksaan konsistensi menemukan 0 ketidaksesuaian pada `persediaan = stok_awal + penerimaan` (224 baris), `sisa_stok = persediaan - pemakaian` (224 baris), dan `stok_awal` terhadap `sisa_stok` bulan sebelumnya (217 pasangan berurutan). Kolom hitungan tersebut tersimpan sebagai nilai statis, bukan formula spreadsheet.

Skrip dan notebook menghitung ulang profil, periode, duplikasi, nilai kosong, nilai nol, pemeriksaan aritmetika, dan pembagian waktu. Setiap baris yang dikeluarkan dicatat beserta nomor baris Excel sumber dan alasannya. Semua Puskesmas pada bulan formulir yang sama tetap berada pada subset waktu yang sama. `periode` dan `subset` hanya metadata; keduanya tidak menjadi predictor. One-hot encoder dan seluruh preprocessing harus di-fit hanya pada bagian latih setiap lipatan. Nilai nol yang tercatat dipertahankan, sedangkan baris yang tidak lengkap pada salah satu X atau Y tidak masuk pemodelan.

## Hold-out kronologis dan validasi silang

Pembagian hold-out berdasarkan bulan formulir, bukan pengacakan baris: 34 bulan pertama, Januari 2023–Oktober 2025, menjadi data pengembangan; delapan bulan terakhir, November 2025–Juni 2026, menjadi data uji. Dari 224 baris lengkap, 168 baris berada di data pengembangan dan 56 baris di data uji. Seluruh Puskesmas pada bulan yang sama tetap berada dalam subset yang sama.

Validasi silang pada data pengembangan memakai lima lipatan expanding-window dengan jendela validasi tiga bulan. Jika `N = 34` bulan data pengembangan dan `k = 5`, jumlah bulan latih lipatan ke-`i` adalah `mᵢ = N − 3(k − i + 1)`, sehingga berturut-turut 19, 22, 25, 28, dan 31 bulan. Periode validasi berturut-turut Agustus–Oktober 2024, November 2024–Januari 2025, Februari–April 2025, Mei–Juli 2025, dan Agustus–Oktober 2025. Pada lipatan kedua, data latih berisi lima Puskesmas, sementara validasi mencakup Blang Mangat dan Muara Dua yang belum muncul di data latih. Notebook MLR memakai `OneHotEncoder(drop="first", handle_unknown="ignore")` dengan Banda Sakti sebagai kategori acuan. Tiga baris validasi dari Puskesmas yang belum dikenal pada lipatan kedua menjadi indikator nol dan mengikuti efek kategori acuan; kondisi ini dicatat sebagai batas generalisasi kategori. Urutan waktu tetap dipertahankan. Delapan bulan uji tidak digunakan untuk pemilihan model.

Laporkan rata-rata dan standar deviasi skor antar-lipatan. Skor CV MLR adalah RMSE; skor CV CART adalah Macro-F1. Jangan mengacak lipatan atau mengintip hasil hold-out ketika memilih model.

## Multiple Linear Regression

MLR menggunakan Ordinary Least Squares dengan satu skenario tetap yang memuat seluruh lima prediktor X₁–X₅. Puskesmas di-one-hot encode dengan satu kategori acuan, Banda Sakti; empat prediktor numerik X₂–X₅ dipakai pada skala aslinya. Bab I–III tidak menetapkan skenario subset lain, sehingga tidak dilakukan pencarian kombinasi fitur. Preprocessing di-fit hanya pada bagian latih yang sedang digunakan. Prediksi mentah pada data uji dievaluasi menggunakan MAE, RMSE, dan R². R² pada data uji dapat bernilai negatif tanpa batas bawah dan dilaporkan apa adanya; tidak dibatasi menjadi nol. Skor aktual dicatat sebagai hasil eksperimen, bukan sebagai jaminan kinerja pada penggunaan mendatang.

Validasi silang MLR melaporkan RMSE pada tiap lipatan, rata-rata, dan standar deviasi populasi antar-lipatan. Karena hanya satu skenario lima prediktor yang digunakan dan MLR OLS ini tidak memiliki hiperparameter yang disetel, skor CV menjadi evaluasi berurutan pada data pengembangan; skor tersebut tidak dipakai untuk mencari subset fitur. Data uji tetap hanya untuk evaluasi akhir.

## Decision Tree CART

CART adalah model klasifikasi terpisah dengan kriteria Gini. Label dibentuk dari Y aktual, bukan prediksi MLR. Untuk setiap bagian latih, hitung Q1 dan Q3 dengan interpolasi linear dari Y positif. Permintaan nol tetap disertakan saat memberi label: Rendah jika `Y ≤ Q1`, Sedang jika `Q1 < Y ≤ Q3`, dan Tinggi jika `Y > Q3`. Hitung ulang ambang hanya dari bagian latih pada setiap lipatan, lalu terapkan pada data validasi. Untuk evaluasi akhir, ambang seluruh data pengembangan diterapkan pada data uji; pada dataset saat ini Q1 = 5 dan Q3 = 10.

Puskesmas di-one-hot encode untuk CART juga, tanpa menghapus kategori acuan; `OneHotEncoder(handle_unknown="ignore")` menangani kategori yang tidak muncul pada data latih. Notebook Tahap 6 membentuk kandidat `ccp_alpha` dari `cost_complexity_pruning_path` pada bagian latih lipatan pertama saja, lalu memakai kandidat yang sama pada lima lipatan berurutan. Parameter terpilih memiliki rata-rata Macro-F1 tertinggi; jika seri, dipilih `ccp_alpha` yang lebih besar agar pohon lebih sederhana. Rata-rata dan standar deviasi populasi skor dicatat. Macro-F1 dihitung dengan tiga kelas tetap (`Rendah`, `Sedang`, `Tinggi`) dan `zero_division=0`.

Pada lipatan kedua, tiga baris validasi berasal dari Blang Mangat dan Muara Dua yang tidak muncul pada data latih; fitur one-hot kategori tak dikenal menjadi nol. Validasi lipatan kelima hanya memiliki 21 label Rendah dan tidak memiliki label Sedang atau Tinggi. Karena itu, Macro-F1 lipatan tersebut tetap mencakup ketiga kelas tetapi perlu ditafsirkan dengan mempertimbangkan ketiadaan dua kelas pada label aktual. Data uji tidak digunakan untuk membentuk grid atau memilih `ccp_alpha`. Evaluasi akhir satu kali pada data uji melaporkan Accuracy, Macro-Precision, Macro-Recall, Macro-F1, Recall kelas Tinggi, dan confusion matrix berurutan Rendah–Sedang–Tinggi. Y aktual tidak diminta saat inferensi aplikasi.

### Eksperimen CART pra-pemangkasan (sudah ditinjau; bukan metode utama)

Notebook [`CART_PrePruning_Training_Colab.ipynb`](../SKRIPSI/CART_PrePruning_Training_Colab.ipynb) menguji CART tanpa bobot dengan `ccp_alpha=0` pada grid 24 pasangan `max_depth` dan `min_samples_leaf`, menggunakan lima prediktor awal dan lima lipatan kronologis. Baseline Tahap 6 dijalankan ulang; seluruh 50 skor alpha-lipatan cocok dengan hasil awal. Dataset dan ambang juga cocok: 224 baris, pembagian pengembangan/uji 168/56, serta Q1=5 dan Q3=10.

Pasangan terbaik CV adalah `max_depth=None` dan `min_samples_leaf=2`, dengan Macro-F1 rata-rata 0,5010 dan standar deviasi populasi 0,1811. Baseline `ccp_alpha` memperoleh 0,5000 dan 0,1671; selisih rata-ratanya hanya +0,0011 dan variasi skor eksperimen pra-pemangkasan lebih besar. Skor per lipatan tidak meningkat konsisten. Pohon terpilih memiliki kedalaman 11 dan 31 daun. Karena 24 pasangan dipilih menggunakan lipatan yang sama untuk menghitung skor, angka CV ini tetap eksploratif dan dapat terlalu optimistis.

Perbandingan pada 56 baris uji lama juga hanya eksploratif: Accuracy 0,7143 dibanding 0,6786 pada baseline, tetapi Macro-F1 0,3402 dibanding 0,3846. Recall kelas Tinggi sama-sama 0,1429. Data uji tidak dipakai memilih parameter dan hasil ini tidak menjadi evaluasi independen. Metadata paket mencatat prediksi tetap sama setelah model dimuat ulang; pemeriksaan pemuatan ulang tidak dapat diulangi secara lokal karena scikit-learn tidak tersedia. Artefak dan tabel ada di [`cart_prepruning_results`](../SKRIPSI/cart_prepruning_results/) dan [`cart_prepruning_results.zip`](../SKRIPSI/cart_prepruning_results.zip).

Eksperimen ini tidak menunjukkan peningkatan yang cukup kuat untuk mengganti baseline. `ccp_alpha` tetap metode utama; Bab II–III tidak diubah.

### Eksperimen CART berbobot (belum diputuskan sebagai metode utama)

Notebook [`CART_Weighted_Training_Colab.ipynb`](../SKRIPSI/CART_Weighted_Training_Colab.ipynb) disiapkan sebagai eksperimen terpisah dengan `class_weight="balanced"`. Notebook, model, dan paket CART tanpa bobot tetap menjadi baseline; hasil berbobot disimpan dalam paket terpisah. Bobot kelas dihitung dari label bagian latih pada masing-masing lipatan dan dari seluruh label data pengembangan untuk model final. Tidak ada resampling, perubahan ambang kuartil, atau tuning bobot kelas. Kandidat `ccp_alpha` dibentuk ulang dari pruning path model berbobot pada data latih lipatan pertama, lalu dipilih berdasarkan rata-rata Macro-F1 pada lima lipatan kronologis; seri diputuskan dengan alpha lebih besar.

Paket Colab varian berbobot sudah diperiksa terhadap dataset dan baseline. CV Macro-F1 varian berbobot adalah 0,5050 (SD populasi 0,1194), dibanding 0,5000 (SD 0,1671) pada baseline tanpa bobot; selisih rata-rata hanya +0,0051. Skor berbobot lebih tinggi pada lipatan pertama, tetapi lebih rendah pada empat lipatan berikutnya. Hasil ini belum menunjukkan peningkatan yang konsisten.

Data uji 56 baris yang sama telah pernah ditinjau pada eksperimen baseline. Karena itu, metrik uji varian berbobot hanya merupakan perbandingan eksploratif: Macro-F1 0,3529 dan Recall kelas Tinggi 0,0000, dibanding 0,3846 dan 0,1429 pada baseline. Confusion matrix berbobot menunjukkan tidak satu pun dari tujuh label aktual Tinggi terdeteksi sebagai Tinggi. Metrik ini tidak digunakan untuk memilih varian dan bukan evaluasi konfirmatori independen. Metadata Colab melaporkan prediksi model setelah dimuat ulang cocok; pemeriksaan pemuatan ulang tidak dapat diulang lokal karena scikit-learn tidak tersedia di lingkungan ini.

Varian berbobot tetap berstatus eksperimen dan belum menggantikan CART tanpa bobot sebagai metode utama. Evaluasi konfirmatori memerlukan periode data baru yang belum dilihat. Bab II–III tidak diubah; keputusan untuk mengadopsi varian berbobot perlu dibuat sebelum metode skripsi diselaraskan.

### Eksperimen fitur musim (sudah ditinjau; bukan metode utama)

Notebook [`Seasonality_Experiment_Colab.ipynb`](../SKRIPSI/Seasonality_Experiment_Colab.ipynb) membandingkan baseline lima prediktor dengan varian yang menambahkan dua fitur siklik dari bulan target `t+1`, dihitung dari `periode` formulir `t` menggunakan sinus dan kosinus. Eksperimen mencakup MLR dan CART awal tanpa bobot; notebook, model, dan hasil baseline maupun eksperimen CART berbobot tetap terpisah. Skor baseline yang dijalankan ulang cocok dengan paket Tahap 5 dan Tahap 6.

Pada MLR, rata-rata RMSE CV varian musim adalah 4,1911 (SD 0,3950), lebih tinggi daripada baseline 4,1008 (SD 0,6599). Pada CART, rata-rata Macro-F1 CV varian musim adalah 0,4565 (SD 0,1323), lebih rendah daripada baseline 0,5000 (SD 0,1671). Jadi, eksperimen ini tidak mendukung penambahan fitur musim pada kedua model berdasarkan CV yang tersedia.

Metrik hold-out lama hanya eksploratif karena 56 baris yang sama sudah pernah dilihat. Pada MLR, MAE/RMSE/R² varian musim adalah 4,7400/6,3851/−0,2222, dibanding 4,5050/6,1588/−0,1371 pada baseline. Pada CART, Macro-F1 dan Recall kelas Tinggi varian musim adalah 0,3266 dan 0,0000, dibanding 0,3846 dan 0,1429 pada baseline; Accuracy keduanya 0,6786. Hasil uji ini tidak digunakan untuk memilih model atau mengklaim evaluasi independen. Paket cocok dengan hash dataset dan model pada metadata; metadata Colab melaporkan pemuatan ulang kedua model musim menghasilkan prediksi yang sama.

Fitur musim tidak diadopsi sebagai metode utama. Metode lima prediktor tetap berlaku; Bab II–III dan aplikasi tidak diubah. Validasi konfirmatori tetap memerlukan periode data baru yang belum dilihat.

## Kontrak inferensi dan tampilan

Request model berisi hanya Puskesmas, pemakaian, sisa stok, penerimaan, dan kasus baru TBC. Periode pencatatan dapat disimpan sebagai metadata Android untuk menampilkan bulan target berikutnya; periode tersebut tidak masuk sebagai prediktor. Respons API dan nilai yang disimpan mempertahankan angka prediksi MLR mentah, termasuk desimal atau nilai negatif. Aplikasi menampilkan prediksi sebagai jumlah kotak bulat dengan batas tampilan minimum nol; aturan tampilan ini tidak mengubah prediksi yang dihitung untuk metrik.

Pada data uji, label aktual terdiri atas 46 Rendah, 3 Sedang, dan 7 Tinggi; model mengklasifikasikan benar 36, 1, dan 1 baris pada masing-masing kelas. Recall Tinggi 0,1429 berarti model hanya mengenali 1 dari 7 baris aktual Tinggi. Kinerja kelas minoritas dan ukuran sampelnya perlu disebutkan saat hasil dibahas; akurasi saja tidak cukup untuk menyatakan model memadai.

Artefak model, urutan kategori, ambang kelas, versi pustaka, dan metadata split harus disimpan agar hasil dapat direproduksi. CSV hasil CART telah dicocokkan kembali dengan dataset dan metriknya dihitung ulang; hash artefak model juga cocok dengan metadata. Notebook Colab memeriksa prediksi setelah model dimuat ulang sebelum membuat paket. Pada 31 Oktober 2026, dua pipeline utama telah diintegrasikan ke Flask lokal dan dideploy ke [PythonAnywhere](../backend/PYTHONANYWHERE_DEPLOYMENT.md). Empat belas unittest backend lulus; endpoint publik sehat, request teknis valid berhasil, dan input negatif mendapat HTTP 422. Kontrak Android lima prediktor dan alur HTTPS Android-ke-API juga lulus uji pada perangkat. Inferensi API bukan metrik penelitian baru. Notebook audit dan fixture request bukan hasil model atau hasil penelitian.
