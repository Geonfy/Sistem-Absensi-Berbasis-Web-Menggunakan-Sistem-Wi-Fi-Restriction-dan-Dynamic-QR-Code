package com.absensi.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data 
@Entity
@Table(name = "presensi")
public class Presensi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sesi_kelas_id")
    private SesiKelas sesiKelas;

    @ManyToOne
    @JoinColumn(name = "mahasiswa_id")
    private User mahasiswa;

    private LocalDateTime waktuAbsen;
    
    @Enumerated(EnumType.STRING)
    private StatusPresensi status; 
    public enum StatusPresensi { HADIR, ALPA, IZIN, SAKIT }

    private String ipAddress;
}
