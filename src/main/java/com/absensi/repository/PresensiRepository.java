package com.absensi.repository;

import com.absensi.model.Presensi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {
    
}
