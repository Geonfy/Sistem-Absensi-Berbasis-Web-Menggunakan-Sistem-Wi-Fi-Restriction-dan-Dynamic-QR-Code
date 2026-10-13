package com.absensi.model;
import jakarta.persistence.*;
import lombok.Data;

@Data 
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
