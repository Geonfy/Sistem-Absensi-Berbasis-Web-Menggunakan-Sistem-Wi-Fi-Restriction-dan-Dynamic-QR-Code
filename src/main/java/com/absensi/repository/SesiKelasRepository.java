package com.absensi.repository;

import com.absensi.model.SesiKelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesiKelasRepository extends JpaRepository<SesiKelas, Long> {
    
}
