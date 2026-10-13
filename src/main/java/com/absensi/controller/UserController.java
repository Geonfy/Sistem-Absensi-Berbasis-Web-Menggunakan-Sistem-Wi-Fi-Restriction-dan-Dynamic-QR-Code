package com.absensi.controller;

import com.absensi.model.User;
import com.absensi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository; //manggil repository untuk mengambil data dari database

    @GetMapping("/mahasiswa")
    public String tampilkanDaftarMahasiswa(Model model) {
        
        List<User> daftarPengguna = userRepository.findAll(); //nyari semua data mahasiswa dari database

        model.addAttribute("daftarMahasiswa", daftarPengguna);

        return "mahasiswa/daftar"; //buat nampilin file template daftar.html
    }
}