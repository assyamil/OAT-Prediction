# Repository Guidelines

## Project Structure & Module Organization

OATPrediction is a single-module Android application using Kotlin, Jetpack Compose, and Material 3. The Android app contains Home, introduction, prediction, result, analysis, guide, and local history flows, with ViewModels, Retrofit, and Room. A separate `backend/` contains a Flask REST API integrated with the trained MLR/CART artifacts and deployed on PythonAnywhere. Android follows the five-predictor contract, and the live Android-to-API flow has been verified on a connected device.

- `app/src/main/java/com/example/oatprediction/`: application source; `MainActivity.kt` hosts the Compose UI, and `ui/theme/` defines colors, typography, and theming.
- `app/src/main/res/`: Android strings, themes, icons, and XML configuration. `app/src/main/AndroidManifest.xml` declares application components.
- `app/src/test/`: local JVM tests. `app/src/androidTest/`: device and emulator tests.
- `backend/`: Flask API, local deployment bundle, model inference service, and backend tests. Private `.joblib` artifacts remain ignored by Git.
- `docs/`: product, architecture, API, ML, and testing references.
- `SKRIPSI/`: local thesis and research data; keep private research files out of commits unless explicitly intended.
- `app/build.gradle.kts`: Android module configuration. `gradle/libs.versions.toml`: dependency and plugin versions. Root Gradle files configure repositories and the wrapper.

## Build, Test, and Development Commands

Run these commands from the repository root in PowerShell; use `./gradlew` on macOS/Linux:

- `.\gradlew.bat :app:assembleDebug` — build the debug APK in `app/build/outputs/apk/debug/`.
- `.\gradlew.bat :app:testDebugUnitTest` — run local JUnit tests.
- `.\gradlew.bat :app:connectedDebugAndroidTest` — run instrumented tests on a connected device or emulator.
- `.\gradlew.bat :app:lintDebug` — run Android Lint.
- `.\gradlew.bat :app:installDebug` — install the debug app; launch it from the device or use Android Studio's Run action.

Install Android SDK 37; supported devices start at API 24. The Gradle daemon configuration requests JDK 25, while Java source compatibility is 11.

## Coding Style & Naming Conventions

Follow the configured official Kotlin style with four-space indentation and Android Studio formatting. Use PascalCase for classes and UI composables, camelCase for ordinary functions and properties, and lowercase underscore names for Android resources. Keep packages under `com.example.oatprediction`. Reuse `OATPredictionTheme` and place user-facing strings in resources. No standalone formatter or additional lint plugin is configured.

## Testing Guidelines

Use JUnit 4 for local logic tests and AndroidJUnit4 for instrumented tests; Espresso and Compose UI test dependencies are available. Name classes `*Test` and methods descriptively, such as `prediction_validInput_returnsExpectedValue`. Add behavior-focused tests for new logic and UI interactions. No coverage threshold is configured; existing tests are starter examples.

## Commit & Pull Request Guidelines

This checkout has Git metadata, but historical commit conventions have not been reviewed. Use concise, imperative commit subjects, such as `Add prediction input validation`. Keep changes focused. Pull requests should describe behavior changes, link relevant issues, report validation performed, and include screenshots for UI changes.

## Security & Configuration

Keep SDK paths in ignored `local.properties`. Never commit credentials, signing keys, or generated build/cache directories. Manage dependency versions through the version catalog.

## OAT Prediction Project Rules

### Project Context

Project ini adalah aplikasi Android untuk penelitian skripsi S1 Teknik Informatika dengan judul:

“PREDIKSI DAN KLASIFIKASI KEBUTUHAN OBAT TUBERKULOSIS MENGGUNAKAN METODE MULTIPLE LINEAR REGRESSION DAN DECISION TREE BERBASIS ANDROID”

Aplikasi bernama OAT Prediction.

Tujuan aplikasi adalah membantu melakukan prediksi kebutuhan/permintaan obat Tuberkulosis (OAT) dan klasifikasi tingkat kebutuhan obat.

Platform utama:

- Android
- Kotlin
- Jetpack Compose
- Material 3

Arsitektur aplikasi yang direncanakan:

```text
Compose UI
→ ViewModel
→ Repository
→ Retrofit
→ Flask REST API
→ Machine Learning models
```

Backend:

- Python
- Flask
- REST API

Machine Learning:

- Multiple Linear Regression untuk prediksi numerik
- Decision Tree menggunakan algoritma CART untuk klasifikasi

### Approved Prediction Inputs

The latest `SKRIPSI/SKRIPSI.docx` is the authority for research variables. The model uses exactly five predictors:

1. Puskesmas (categorical; one-hot encoded for both MLR and CART)
2. Pemakaian (boxes used during form month `t`)
3. Sisa Stok (boxes remaining at the end of form month `t`)
4. Penerimaan (boxes received during form month `t`)
5. Kasus Baru TBC (new patients beginning OAT treatment during form month `t`)

The dependent target `Y` is the OAT demand recorded on the LPLPO form for month `t`, intended for month `t+1`. Historical `Y` is used only as the training/evaluation target and to derive CART labels; the app must not request `Y` as an input. Do not use prior-period demand as predictor X7. The study is limited to OAT Dewasa Kategori 1, so category is not a model feature or prediction-form input. Month/year identifies form month `t` and is metadata for the next-month horizon, not a predictor.

### Machine Learning Rules

1. MLR digunakan untuk menghasilkan prediksi numerik permintaan obat.
2. Decision Tree CART digunakan untuk klasifikasi tingkat kebutuhan.
3. Kelas klasifikasi adalah:
   - Rendah
   - Sedang
   - Tinggi
4. MLR dan Decision Tree merupakan model yang terpisah dan tidak boleh diasumsikan bahwa Decision Tree hanya memberikan label terhadap output MLR.
5. Jangan mengubah MLR menjadi algoritma lain.
6. Jangan mengubah Decision Tree CART menjadi algoritma klasifikasi lain tanpa persetujuan.
7. Jangan membuat model Machine Learning sebelum diminta secara eksplisit.
8. Jangan membuat hasil penelitian, metrik evaluasi, atau angka performa secara fiktif.
9. Jangan membuat dataset penelitian fiktif.
10. Gunakan lima prediktor yang disetujui dalam skripsi; jangan menambah X7, kategori OAT, atau Y aktual sebagai input model/aplikasi.

### Dummy Data Rules

Pada tahap awal aplikasi, MLR dan Decision Tree belum tersedia.

Dummy/mock response hanya boleh digunakan untuk:

- UI development
- API development
- Retrofit testing
- Android-to-Flask integration testing
- loading/error/success state testing

Dummy tidak boleh dianggap sebagai:

- hasil penelitian
- hasil prediksi aktual
- hasil evaluasi model
- data penelitian

### Data Integrity

Data penelitian harus berasal dari sumber penelitian yang sebenarnya.

Jangan:

- mengarang nilai yang hilang;
- mengubah data agar model terlihat lebih baik;
- membuat angka evaluasi;
- menghapus data tanpa alasan yang terdokumentasi;
- membuat data sintetis lalu menganggapnya sebagai data penelitian.

Empty/missing value tidak otomatis berarti zero.

Nilai 0 hanya digunakan jika sumber data memang menunjukkan nilai/transaksi tersebut nol.

### Data Leakage

Hindari target leakage.

Y aktual tidak boleh menjadi predictor. Untuk setiap formulir `t`, prediktor berasal dari lima variabel X pada formulir `t`, sedangkan Y pada formulir yang sama adalah permintaan untuk `t+1`. Permintaan periode sebelumnya bukan predictor pada metodologi terbaru.

### Development Rules

1. Baca AGENTS.md sebelum mengerjakan task.
2. Baca dokumentasi dalam docs/ jika tersedia dan relevan.
3. Untuk task besar, buat implementation plan terlebih dahulu.
4. Untuk task kecil, lakukan perubahan seminimal mungkin.
5. Jangan mengubah arsitektur tanpa alasan teknis yang jelas.
6. Jangan menambahkan fitur besar yang tidak terdapat dalam requirement.
7. Setelah perubahan penting, jalankan build/test yang relevan.
8. Laporkan file yang diubah.
9. Laporkan hasil build/test.
10. Jangan menyembunyikan error.
11. Jika requirement belum jelas, jangan menebak keputusan penelitian.
12. Pertahankan kode sederhana dan mudah dipahami mahasiswa.

### Development Phases

Tahapan utama project:

1. **Phase 1:** Android project foundation.
2. **Phase 2:** Application architecture.
3. **Phase 3:** Home UI.
4. **Phase 4:** Prediction input form.
5. **Phase 5:** ViewModel and Repository.
6. **Phase 6:** Retrofit integration.
7. **Phase 7:** Flask dummy backend.
8. **Phase 8:** Android ↔ Flask integration.
9. **Phase 9:** Result screen.
10. **Phase 10:** Prediction history.
11. **Phase 11:** Dataset preprocessing.
12. **Phase 12:** MLR model development and evaluation.
13. **Phase 13:** Decision Tree CART development and evaluation.
14. **Phase 14:** Integration of real ML models into Flask.
15. **Phase 15:** End-to-end testing.
16. **Phase 16:** Final Android build and documentation.

Important:
Do not skip major development phases without explicit instruction.

A later phase may be explored or prepared when technically necessary, but do not implement its production functionality before the required previous phase is sufficiently stable.

Do not implement Machine Learning before the project explicitly reaches the Machine Learning development phase.

### Documentation

Project documentation will be stored in:

`docs/`

Expected documents:

- `PRD.md`
- `ARCHITECTURE.md`
- `API_CONTRACT.md`
- `ML_SPECIFICATION.md`
- `TESTING_PLAN.md`

Use these documents as project references when they exist.

Do not duplicate large documentation unnecessarily inside AGENTS.md.

### Documentation Authority

`SKRIPSI/SKRIPSI.docx` is the primary source for approved research scope and methodology. `docs/PRD.md` is the primary source of truth for product requirements.

`docs/ARCHITECTURE.md` is the primary reference for technical architecture.

`docs/API_CONTRACT.md` is the primary reference for Android-backend communication.

`docs/ML_SPECIFICATION.md` is the primary reference for Machine Learning integration.

`docs/TESTING_PLAN.md` is the primary reference for testing requirements.

If implementation conflicts with these documents, do not silently change the requirements. Report the conflict and ask for clarification when necessary.

### Current Development State

The Android application has its main screens, Retrofit client, local Room history, and extensive unit/instrumentation test sources. The main MLR and CART pipelines are trained and verified from Colab artifacts. Flask loads them through the approved five-predictor API contract and is live at `https://syamildeveloper.pythonanywhere.com`; `/health`, a valid prediction request, and HTTP 422 validation have been confirmed.

Phases 3 and 4 aligned the Android form, domain model, DTO, validation, local history, and Room migration to the five approved predictors; Retrofit defaults to the PythonAnywhere HTTPS endpoint. The connected Android-to-API test and Room history migration test pass. Legacy history values remain preserved separately and are never converted into missing predictors. The trained `.joblib` files and generated deployment bundle remain local and are ignored by Git.

Do not treat old dummy responses, historical UI fixtures, or example request values as model results or research data.

### Planning Workflow

For tasks that affect multiple files, architecture, networking, backend integration, database structure, or Machine Learning integration, create or follow an implementation plan before making substantial changes.

For small and isolated changes, a separate planning step is not required.

Before implementing a major plan:
1. Read the relevant project documentation.
2. Inspect the existing code.
3. Identify files that will be created or modified.
4. Identify relevant tests.
5. Explain important risks or assumptions.

Do not make substantial code changes while only asked to plan.
