package com.absensi.repository;

import com.absensi.model.Presensi;
import com.absensi.model.SesiKelas;
import com.absensi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {
    List<Presensi> findBySesiKelas(SesiKelas sesiKelas);
    List<Presensi> findByMahasiswa(User mahasiswa);
    boolean existsBySesiKelasAndMahasiswa(SesiKelas sesiKelas, User mahasiswa);
    boolean existsBySesiKelasAndIpAddress(SesiKelas sesiKelas, String ipAddress);
}