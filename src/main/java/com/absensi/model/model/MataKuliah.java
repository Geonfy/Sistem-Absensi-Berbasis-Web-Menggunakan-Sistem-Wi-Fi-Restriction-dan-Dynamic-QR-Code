package com.absensi.model.model;
import jakarta.persistence.*;

@Entity 
@Table(name = "mata_kuliah")
public class MataKuliah {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String kodeMk;
    private String namaMk;
    private int sks;
}
