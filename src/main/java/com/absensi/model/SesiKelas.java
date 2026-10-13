package com.absensi.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data 
@Entity
@Table(name = "sesi_kelas")
public class SesiKelas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mata_kuliah_id")
    private MataKuliah mataKuliah;

    @ManyToOne
    @JoinColumn(name = "dosen_id")
    private User dosen;

    private int pertemuanKe;
    private LocalDateTime waktuMulai;
    private LocalDateTime waktuSelesai;
    private String ruangan;
}
