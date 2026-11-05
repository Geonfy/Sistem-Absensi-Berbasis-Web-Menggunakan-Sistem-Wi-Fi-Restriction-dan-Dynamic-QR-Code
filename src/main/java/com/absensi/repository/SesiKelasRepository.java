package com.absensi.repository;

import com.absensi.model.SesiKelas;
import com.absensi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SesiKelasRepository extends JpaRepository<SesiKelas, Long> {
    List<SesiKelas> findByDosen(User dosen);
}