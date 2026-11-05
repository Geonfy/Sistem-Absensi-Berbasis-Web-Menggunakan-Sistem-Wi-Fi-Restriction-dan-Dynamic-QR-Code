package com.absensi.auth;

import com.absensi.model.User;
import com.absensi.repository.UserRepository;
import com.absensi.wifi.WifiValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WifiValidator wifiValidator;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. Halaman login
    @GetMapping({"/", "/login"})
    public String tampilkanHalamanLogin() {
        return "auth/login";
    }

    // 2. Proses login
    @PostMapping("/login")
    public String prosesLogin(@RequestParam String nomorInduk,
                              @RequestParam String password,
                              HttpServletRequest request,
                              HttpSession session,
                              Model model) {

        User user = userRepository.findByNomorInduk(nomorInduk.trim());

        if (user == null || !passwordCocok(user, password)) {
            model.addAttribute("error", "Nomor Induk atau Password salah!");
            return "auth/login";
        }

        // Wi-Fi Restriction: selain ADMIN wajib berada di jaringan yang diizinkan
        String clientIp = wifiValidator.getClientIp(request);
        if (user.getRole() != User.Role.ADMIN) {
            if (!wifiValidator.isAllowedNetwork(clientIp)) {
                model.addAttribute("error",
                        "Akses Ditolak! Anda harus menggunakan Wi-Fi Kampus/Kantor. (IP Anda: " + clientIp + ")");
                return "auth/login";
            }
        }

        // Ganti ID sesi setelah login untuk mencegah session fixation
        request.changeSessionId();
        session.setAttribute("loggedInUser", user);

        if (user.getRole() == User.Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (user.getRole() == User.Role.DOSEN) {
            return "redirect:/dosen/dashboard";
        } else {
            return "redirect:/mahasiswa/dashboard";
        }
    }

    // 3. Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Mendukung hash BCrypt dan password lama (polos).
    // Password polos yang cocok otomatis diubah menjadi hash saat login berhasil.
    private boolean passwordCocok(User user, String passwordInput) {
        String tersimpan = user.getPassword();
        if (tersimpan == null) {
            return false;
        }

        if (tersimpan.startsWith("$2")) {
            return passwordEncoder.matches(passwordInput, tersimpan);
        }

        if (tersimpan.equals(passwordInput)) {
            user.setPassword(passwordEncoder.encode(passwordInput));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}