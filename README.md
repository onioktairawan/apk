# Pool Training Analyzer Android

MVP Android untuk latihan pool berbasis screen capture + floating overlay.

## Build via GitHub Actions
1. Upload semua isi folder ini ke repository GitHub.
2. Buka tab Actions.
3. Pilih `Build Android APK`.
4. Klik `Run workflow`.
5. Setelah sukses, download artifact `PoolTrainingAnalyzer-debug`.
6. Extract artifact untuk mendapatkan APK.

Workflow juga otomatis berjalan pada push ke `main` / `master`.

## Penggunaan
1. Izinkan Floating Overlay.
2. Aktifkan Screen Capture.
3. Tampilkan Analyzer Overlay.
4. Buka 8 Ball Pool.
5. Tekan ANALYZE pada panel floating.
6. CLEAR untuk menghapus garis.

## Batasan
Detector ini masih MVP heuristik berbasis piksel layar. Tema meja, resolusi,
animasi dan layout UI dapat memengaruhi akurasi. Aplikasi tidak mengubah APK game,
tidak meng-hook proses game, tidak menekan tombol game otomatis, dan tidak mencoba
melewati protected/secure display.
