package com.absensi.controller;

import com.absensi.model.MataKuliah;
import com.absensi.model.User;
import com.absensi.repository.MataKuliahRepository;
import com.absensi.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MataKuliahRepository mataKuliahRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Mengembalikan user jika yang login adalah ADMIN, selain itu null
    private User getAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == User.Role.ADMIN) {
            return user;
        }
        return null;
    }

    // ---------- Dashboard ----------
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User admin = getAdmin(session);
        if (admin == null) {
            return "redirect:/login";
        }
        model.addAttribute("admin", admin);
        model.addAttribute("daftarUser", userRepository.findAll());
        model.addAttribute("daftarMk", mataKuliahRepository.findAll());
        return "admin/dashboard";
    }

    // ---------- Pengguna ----------
    @PostMapping("/user/tambah")
    public String tambahUser(@RequestParam String nomorInduk,
                             @RequestParam String namaLengkap,
                             @RequestParam String password,
                             @RequestParam String role,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (getAdmin(session) == null) {
            return "redirect:/login";
        }

        String nomor = nomorInduk.trim();
        if (nomor.isEmpty() || namaLengkap.trim().isEmpty() || password.isEmpty()) {
            ra.addFlashAttribute("pesanError", "Semua kolom pengguna wajib diisi!");
            return "redirect:/admin/dashboard";
        }
        if (userRepository.findByNomorInduk(nomor) != null) {
            ra.addFlashAttribute("pesanError", "Nomor Induk " + nomor + " sudah terdaftar!");
            return "redirect:/admin/dashboard";
        }

        User.Role roleBaru;
        try {
            roleBaru = User.Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("pesanError", "Peran tidak valid!");
            return "redirect:/admin/dashboard";
        }

        User user = new User();
        user.setNomorInduk(nomor);
        user.setNamaLengkap(namaLengkap.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(roleBaru);
        userRepository.save(user);

        ra.addFlashAttribute("pesanSukses", "Pengguna " + user.getNamaLengkap() + " berhasil ditambahkan.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/user/{id}/hapus")
    public String hapusUser(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User admin = getAdmin(session);
        if (admin == null) {
            return "redirect:/login";
        }
        if (admin.getId().equals(id)) {
            ra.addFlashAttribute("pesanError", "Anda tidak bisa menghapus akun yang sedang dipakai!");
            return "redirect:/admin/dashboard";
        }

        try {
            userRepository.deleteById(id);
            ra.addFlashAttribute("pesanSukses", "Pengguna berhasil dihapus.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("pesanError",
                    "Pengguna tidak bisa dihapus karena sudah memiliki data sesi atau presensi.");
        } catch (Exception e) {
            ra.addFlashAttribute("pesanError", "Pengguna tidak ditemukan atau gagal dihapus.");
        }
        return "redirect:/admin/dashboard";
    }

    // ---------- Mata Kuliah ----------
    @PostMapping("/mk/tambah")
    public String tambahMk(@RequestParam String kodeMk,
                           @RequestParam String namaMk,
                           @RequestParam int sks,
                           HttpSession session,
                           RedirectAttributes ra) {
        if (getAdmin(session) == null) {
            return "redirect:/login";
        }

        String kode = kodeMk.trim().toUpperCase();
        if (kode.isEmpty() || namaMk.trim().isEmpty() || sks < 1) {
            ra.addFlashAttribute("pesanError", "Data mata kuliah tidak lengkap atau SKS tidak valid!");
            return "redirect:/admin/dashboard";
        }
        if (mataKuliahRepository.existsByKodeMk(kode)) {
            ra.addFlashAttribute("pesanError", "Kode mata kuliah " + kode + " sudah ada!");
            return "redirect:/admin/dashboard";
        }

        MataKuliah mk = new MataKuliah();
        mk.setKodeMk(kode);
        mk.setNamaMk(namaMk.trim());
        mk.setSks(sks);
        mataKuliahRepository.save(mk);

        ra.addFlashAttribute("pesanSukses", "Mata kuliah " + mk.getNamaMk() + " berhasil ditambahkan.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/mk/{id}/hapus")
    public String hapusMk(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (getAdmin(session) == null) {
            return "redirect:/login";
        }

        try {
            mataKuliahRepository.deleteById(id);
            ra.addFlashAttribute("pesanSukses", "Mata kuliah berhasil dihapus.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("pesanError",
                    "Mata kuliah tidak bisa dihapus karena sudah dipakai pada sesi kelas.");
        } catch (Exception e) {
            ra.addFlashAttribute("pesanError", "Mata kuliah tidak ditemukan atau gagal dihapus.");
        }
        return "redirect:/admin/dashboard";
    }
}