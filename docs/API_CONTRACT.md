# Kontrak API — OAT Prediction

## Status dan Otoritas

Draft baseline dari lampiran requirement pemilik proyek. Referensi utama komunikasi Android–Flask; terkait [PRD.md](PRD.md) FR-07/FR-08 dan [ARCHITECTURE.md](ARCHITECTURE.md).

Endpoint dan field berikut ditetapkan sebagai baseline. Bagian terbuka harus diselesaikan sebelum implementasi terkait; jangan mengubah kontrak tanpa instruksi.

## POST /predict

Request dan response menggunakan JSON. Android mengirim input melalui Retrofit; Flask memvalidasi ulang dan memprosesnya. Header JSON menggunakan Content-Type: application/json.

| Field request | Tipe JSON | Kewajiban dan batas |
| --- | --- | --- |
| puskesmas | string | Wajib; nilai dropdown dari data penelitian tervalidasi |
| kategori_oat | string | Wajib; kategori dataset final |
| stok_awal | number | Wajib; satuan/batas rinci belum ditetapkan |
| penerimaan | number | Wajib; satuan/batas rinci belum ditetapkan |
| pemakaian | number | Wajib; satuan/batas rinci belum ditetapkan |
| jumlah_pasien | number | Wajib; rincian validasi domain belum ditetapkan |
| permintaan_sebelumnya | number | Bersyarat pada keputusan mempertahankan X7 |

Status X7 belum final. Tabel ini tidak menetapkan bahwa null, omission, atau nol adalah fallback yang sah. Jika X7 tidak dipertahankan, sesuaikan kontrak secara eksplisit bersama Android, backend, dan pipeline model.

### Contoh Request — DUMMY / TEST ONLY

Contoh dari lampiran untuk menjelaskan format; nama, kategori, dan angka bukan data produksi atau dataset penelitian.

~~~json
{
  "puskesmas": "Puskesmas A",
  "kategori_oat": "OAT Dewasa",
  "stok_awal": 100,
  "penerimaan": 50,
  "pemakaian": 70,
  "jumlah_pasien": 30,
  "permintaan_sebelumnya": 80
}
~~~

### Respons Sukses

| Field | Tipe | Makna |
| --- | --- | --- |
| success | boolean | true |
| prediksi_mlr | number | Prediksi numerik Permintaan Obat |
| klasifikasi_dt | string | Rendah, Sedang, atau Tinggi dari CART |

Contoh berikut **DUMMY / TEST ONLY**, bukan hasil inferensi atau evaluasi penelitian:

~~~json
{
  "success": true,
  "prediksi_mlr": 125,
  "klasifikasi_dt": "Tinggi"
}
~~~

Pada sistem final, kedua hasil berasal dari model terlatih yang terpisah. Angka tidak boleh di-hardcode sebagai hasil aktual. Satuan, pembulatan, dan kebijakan prediksi negatif belum ditetapkan.

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

Pemetaan status HTTP, kode error selain VALIDATION_ERROR, dan respons jika salah satu model gagal belum ditetapkan. Jangan menganggap contoh ini mendefinisikan seluruh kasus error.

## Validasi dan Kompatibilitas

- Android memvalidasi sebelum request; backend tetap memvalidasi ulang.
- Tolak field wajib kosong, teks pada input numerik, dan format tidak valid.
- Nilai negatif tidak diterima tanpa alasan domain terdokumentasi; batas final dan tipe integer/desimal perlu diperinci.
- Missing value tidak otomatis menjadi nol. Penanganan field hilang/null belum disepakati.
- Nama field konsisten antara Android, Retrofit, Flask, dan preprocessing.
- Target periode yang sama tidak dikirim sebagai predictor. Bulan/Tahun belum menjadi field kontrak baseline.
- Mapping kategori dikelola backend/pipeline, bukan encoding angka sembarang dari Android.
- Pergantian dummy ke model nyata mempertahankan format kompatibel; penggunaan dummy wajib berstatus DUMMY / TEST ONLY. Mekanisme penanda belum ditetapkan, tanpa menambah field secara sepihak.

## Endpoint Opsional dan Keputusan Terbuka

GET /health adalah kandidat untuk memeriksa backend aktif, belum endpoint wajib; responsnya belum ditetapkan.

Base URL, versi API, autentikasi, timeout/retry, status HTTP, daftar kategori sah, aturan field tambahan, representasi X7 bila ditiadakan, penanda dummy, serta satuan/batas numerik menunggu keputusan. Tidak ada endpoint riwayat yang ditetapkan.

Pengujian mengikuti [TESTING_PLAN.md](TESTING_PLAN.md). Dokumen ini tidak membuat client Retrofit atau backend Flask.
