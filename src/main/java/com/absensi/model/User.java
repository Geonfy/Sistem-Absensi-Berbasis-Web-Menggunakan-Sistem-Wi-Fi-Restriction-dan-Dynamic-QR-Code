package com.absensi.model;
import jakarta.persistence.*;
import lombok.Data;

@Data 
@Entity 
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nomorInduk; 

    private String namaLengkap;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; 
    
    public enum Role { ADMIN, DOSEN, MAHASISWA }
}
