# Pool Training Analyzer v0.3

Update dari versi v0.2 yang sebelumnya berhasil dibuild lewat GitHub Actions.

## Ditambahkan
- Game Mode: 8 Ball / 9 Ball / Practice / No Guide.
- Group 8 Ball: Auto / Solids / Stripes / Black 8.
- Shot Mode: Auto / Direct / Bank / Kick / Combo / Trick.
- NEXT / TARGET untuk pindah kandidat bola.
- POCKET untuk pindah pocket.
- CAL untuk kalibrasi 4 sudut meja.
- ANALYZE untuk satu frame.
- LIVE untuk tracking berkala (maks sekitar 7 frame analisis/detik).
- Deteksi arah cue dan indikator LEFT / RIGHT / LOCKED.
- Estimasi POWER.
- Solver geometri direct, 1-cushion bank, dan 1-cushion kick.
- TRICK menggabungkan kandidat bank + kick.

## Cara pakai
1. Izinkan Floating Overlay.
2. Aktifkan Screen Capture.
3. Tampilkan Analyzer Overlay.
4. Buka 8 Ball Pool.
5. Tekan CAL dan tap: top-left, top-right, bottom-right, bottom-left area meja.
6. Pilih MODE.
7. Kalau 8 Ball, pilih GROUP sesuai milikmu.
8. Pilih SHOT atau AUTO.
9. Tekan ANALYZE.
10. Aktifkan LIVE supaya indikator mengikuti perubahan arah cue.
11. Geser cue secara manual sampai indikator LOCKED.
12. NEXT/TARGET/POCKET untuk pilihan lain.

## Catatan MVP
- Klasifikasi solids/stripes masih heuristic dari piksel dan dapat salah pada skin/tema tertentu.
- 9 Ball belum OCR nomor bola; TARGET dipakai untuk memilih kandidat legal secara manual.
- COMBO masih mode MVP dan belum full multi-ball chain solver.
- LIVE membaca layar saja; tidak menggerakkan cue atau menembak otomatis.
