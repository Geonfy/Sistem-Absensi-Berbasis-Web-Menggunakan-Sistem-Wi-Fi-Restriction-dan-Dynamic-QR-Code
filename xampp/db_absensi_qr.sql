-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 13, 2026 at 01:58 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_absensi_qr`
--

-- --------------------------------------------------------

--
-- Table structure for table `mata_kuliah`
--

CREATE TABLE `mata_kuliah` (
  `id` bigint(20) NOT NULL,
  `kode_mk` varchar(255) DEFAULT NULL,
  `nama_mk` varchar(255) DEFAULT NULL,
  `sks` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `mata_kuliah`
--

INSERT INTO `mata_kuliah` (`id`, `kode_mk`, `nama_mk`, `sks`) VALUES
(1, 'STDA4101', 'PENGANTAR SAINS DATA', 3),
(2, 'STSI4409', 'DATA WAREHOUSE', 3),
(3, 'STS14403', 'MANAJEMEN RESIKO DAN AUDIT SISTEM INFORMASI', 3),
(4, 'STSI4402', 'TATA KELOLA TEKNOLOGI', 2),
(5, 'STSI4440', 'CAPSTONE PROJECT', 6);

-- --------------------------------------------------------

--
-- Table structure for table `presensi`
--

CREATE TABLE `presensi` (
  `id` bigint(20) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `status` enum('ALPA','HADIR','IZIN','SAKIT') DEFAULT NULL,
  `waktu_absen` datetime(6) DEFAULT NULL,
  `mahasiswa_id` bigint(20) DEFAULT NULL,
  `sesi_kelas_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sesi_kelas`
--

CREATE TABLE `sesi_kelas` (
  `id` bigint(20) NOT NULL,
  `pertemuan_ke` int(11) NOT NULL,
  `ruangan` varchar(255) DEFAULT NULL,
  `waktu_mulai` datetime(6) DEFAULT NULL,
  `waktu_selesai` datetime(6) DEFAULT NULL,
  `dosen_id` bigint(20) DEFAULT NULL,
  `mata_kuliah_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `nama_lengkap` varchar(255) DEFAULT NULL,
  `nomor_induk` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','DOSEN','MAHASISWA') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nama_lengkap`, `nomor_induk`, `password`, `role`) VALUES
(1, 'Bagus Setiyawan (Admin)', 'ADMIN-01', 'admin123', 'ADMIN'),
(2, 'Pak Candra Surya', 'DSN-001', 'dosen123', 'DOSEN'),
(3, 'Bagus Setiyawan (Mahasiswa)', '12345678', 'mahasiswa123', 'MAHASISWA'),
(4, 'Muhammad Saputra Rhamadan', '051554356', 'putra123', 'MAHASISWA'),
(5, 'Mohammad ATH. Thor Zulqarnain', '054546396', 'thor123', 'MAHASISWA'),
(6, 'Prihartini', '051400359', 'prihartini123', 'MAHASISWA'),
(7, 'Windi Rohmatul Jaliah', '049899407', 'windi123', 'MAHASISWA');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `mata_kuliah`
--
ALTER TABLE `mata_kuliah`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK5h8bsj9l85lpem1sqi3oaq7gl` (`kode_mk`);

--
-- Indexes for table `presensi`
--
ALTER TABLE `presensi`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK1uw1fg0ysncmln0bsqp53mbae` (`mahasiswa_id`),
  ADD KEY `FKs9wmuda81mm0uh94symonv1yt` (`sesi_kelas_id`);

--
-- Indexes for table `sesi_kelas`
--
ALTER TABLE `sesi_kelas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgg66n1pkjt1vb05dvocir67ua` (`dosen_id`),
  ADD KEY `FKmrr3x1qit2nt4x7jq1x6858pt` (`mata_kuliah_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKaqlyo6md40d9dcg8e4dd4t60g` (`nomor_induk`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `mata_kuliah`
--
ALTER TABLE `mata_kuliah`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `presensi`
--
ALTER TABLE `presensi`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `sesi_kelas`
--
ALTER TABLE `sesi_kelas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `presensi`
--
ALTER TABLE `presensi`
  ADD CONSTRAINT `FK1uw1fg0ysncmln0bsqp53mbae` FOREIGN KEY (`mahasiswa_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKs9wmuda81mm0uh94symonv1yt` FOREIGN KEY (`sesi_kelas_id`) REFERENCES `sesi_kelas` (`id`);

--
-- Constraints for table `sesi_kelas`
--
ALTER TABLE `sesi_kelas`
  ADD CONSTRAINT `FKgg66n1pkjt1vb05dvocir67ua` FOREIGN KEY (`dosen_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKmrr3x1qit2nt4x7jq1x6858pt` FOREIGN KEY (`mata_kuliah_id`) REFERENCES `mata_kuliah` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
