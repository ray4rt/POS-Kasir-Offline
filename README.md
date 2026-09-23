# POS Kasir Offline

Aplikasi Point of Sale (POS) berbasis Android yang dirancang khusus untuk operasional offline 100%. Dibuat menggunakan teknologi modern Android (Jetpack Compose, Room Database, Kotlin Coroutines) untuk memberikan performa cepat, antarmuka intuitif, dan keamanan data lokal yang handal.

## 🚀 Fitur Utama

### 1. Sistem Autentikasi Ganda (Admin & Kasir)
*   **Login PIN:** Keamanan akses menggunakan PIN unik untuk setiap akun.
*   **Hak Akses Terpisah:** 
    *   **Admin:** Akses penuh ke laporan keuangan, manajemen harga, manajemen pengguna, dan pengaturan sistem.
    *   **Kasir:** Fokus pada operasional penjualan dan manajemen stok dasar.
*   **Logout Aman:** Fitur keluar cepat untuk mengunci aplikasi kembali.

### 2. Manajemen Penjualan (Kasir)
*   **Antarmuka Kasir Cepat:** Pilih produk berdasarkan kategori atau cari manual.
*   **Multi-Unit Support:** Mendukung berbagai satuan (pcs, kg, liter, dus, dll) dengan konversi otomatis.
*   **Sistem Parkir Transaksi (Hold):** Simpan keranjang belanja sementara jika pelanggan ingin menambah belanjaan nanti.
*   **Diskon & Pajak:** Fleksibilitas dalam memberikan diskon per item atau per transaksi, serta pengaturan pajak toko.
*   **Pembayaran Tunai & Non-Tunai:** Pencatatan metode pembayaran yang fleksibel.
*   **Cetak Struk:** Desain struk yang bersih dengan informasi detail toko (Offline Ready).

### 3. Inventaris & Produk
*   **Katalog Produk Lengkap:** Kelola SKU, Barcode, Kategori, Harga Modal, dan Harga Jual.
*   **Manajemen Stok Real-time:** Stok berkurang otomatis setiap kali terjadi penjualan.
*   **Notifikasi Stok Rendah:** Peringatan visual untuk produk yang mendekati batas minimal stok.
*   **Riwayat Mutasi Stok:** Lacak setiap perpindahan stok (Masuk, Keluar, Penyesuaian, Retur).

### 4. Analisis & Laporan (Khusus Admin)
*   **Dashboard Statistik:** Ringkasan total penjualan, perkiraan profit, dan jumlah transaksi harian/bulanan.
*   **Riwayat Transaksi:** Detail setiap invoice dengan opsi pembatalan transaksi (Restorasi Stok otomatis).
*   **Laporan Profit:** Hitung keuntungan bersih berdasarkan selisih harga modal dan harga jual.
*   **Riwayat Perubahan Harga:** Pantau kapan dan siapa yang mengubah harga modal/jual produk.

### 5. Keamanan & Backup Data
*   **100% Offline:** Data disimpan di perangkat Anda menggunakan Room Database (SQLite). Tidak perlu koneksi internet.
*   **Backup & Restore JSON:** Ekspor seluruh database ke file JSON untuk cadangan atau pindah perangkat.
*   **Reset PIN:** Fitur pemulihan PIN ke pengaturan awal untuk Admin.

### 6. Kustomisasi & UI
*   **Mode Gelap (Dark Mode):** Antarmuka modern yang nyaman di mata untuk penggunaan jangka panjang.
*   **Pengaturan Toko:** Ubah nama toko, alamat, nomor telepon, dan footer struk sesuai kebutuhan bisnis Anda.

## 🛠 Teknologi yang Digunakan
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **Local Database:** Room Database
*   **Concurrency:** Kotlin Coroutines & Flow
*   **Architecture:** MVVM (Model-View-ViewModel)

---

## 📥 Cara Instalasi File APK (Debug/Unsigned)

Karena aplikasi ini di-build untuk keperluan pengembangan (tanpa keystore resmi), ikuti langkah-langkah berikut untuk menginstalnya di perangkat Android Anda:

1.  **[Unduh File APK]([https://github.com/ray4rt/POS-Kasir-Offline/tree/main/kasir-offline/app](https://drive.google.com/file/d/17cfDkmTo-3_nus_IZhBpkFw5WIxv5Yb8/view?usp=drivesdk):** Ambil file APK yang telah di-generate dari folder output build.
2.  **Aktifkan Izin Sumber Tidak Dikenal:**
    *   Buka **Pengaturan** di HP Anda.
    *   Cari menu **Keamanan** atau **Privasi**.
    *   Aktifkan opsi **"Instal Aplikasi dari Sumber Tidak Dikenal"** (Install Unknown Apps). Jika di Android versi baru, berikan izin ini pada aplikasi Browser atau File Manager yang Anda gunakan untuk membuka APK.
3.  **Instal APK:**
    *   Buka file APK yang telah diunduh.
    *   Klik **Instal**.
4.  **Lewati Peringatan Play Protect (Jika Muncul):**
    *   Karena aplikasi ini tidak terdaftar di Play Store, Google Play Protect mungkin akan menampilkan peringatan "Blocked by Play Protect".
    *   Klik **"Install Anyway"** (Tetap Instal) untuk melanjutkan.
5.  **Buka Aplikasi:** Aplikasi siap digunakan!

---

## 🔑 Informasi Login Default (Setelah Reset)
*   **Admin:** PIN `1234`
*   **Kasir:** PIN `4321`

---
*Dikembangkan oleh **Rayden** • Versi 1.0.0*
