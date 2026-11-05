package com.absensi.controller;

import com.absensi.model.Presensi;
import com.absensi.model.SesiKelas;
import com.absensi.model.User;
import com.absensi.qr.QrTokenService;
import com.absensi.repository.PresensiRepository;
import com.absensi.repository.SesiKelasRepository;
import com.absensi.wifi.WifiValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;

@Controller
@RequestMapping("/mahasiswa")
public class MahasiswaController {

    @Autowired
    private PresensiRepository presensiRepository;

    @Autowired
    private SesiKelasRepository sesiKelasRepository;

    @Autowired
    private QrTokenService qrTokenService;

    @Autowired
    private WifiValidator wifiValidator;

    // Tolak jika satu IP dipakai dua akun berbeda pada sesi yang sama.
    // Matikan (false) saat uji di localhost, karena semua akun memakai 127.0.0.1.
    @Value("${absensi.cek-ip-ganda:true}")
    private boolean cekIpGanda;

    // 1. Dashboard Mahasiswa
    @GetMapping("/dashboard")
    public String dashboardMahasiswa(HttpSession session, HttpServletRequest request, Model model) {
        User mahasiswa = (User) session.getAttribute("loggedInUser");
        if (mahasiswa == null || mahasiswa.getRole() != User.Role.MAHASISWA) {
            return "redirect:/login";
        }

        String clientIp = wifiValidator.getClientIp(request);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("clientIp", clientIp);

        try {
            model.addAttribute("riwayatPresensi", presensiRepository.findByMahasiswa(mahasiswa));
        } catch (Exception e) {
            model.addAttribute("riwayatPresensi", Collections.emptyList());
        }

        return "mahasiswa/dashboard";
    }

    // Jika halaman /mahasiswa/absen di-refresh lewat GET
    @GetMapping("/absen")
    public String redirectAbsenGet() {
        return "redirect:/mahasiswa/dashboard";
    }

    // 2. Memproses token QR
    @PostMapping("/absen")
    public String prosesAbsen(@RequestParam(value = "qrToken", required = false) String qrToken,
                              HttpSession session,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        try {
            User mahasiswa = (User) session.getAttribute("loggedInUser");
            if (mahasiswa == null || mahasiswa.getRole() != User.Role.MAHASISWA) {
                return "redirect:/login";
            }

            if (qrToken == null || qrToken.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("pesanError", "Token QR tidak boleh kosong!");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 1: Wi-Fi Restriction
            String clientIp = wifiValidator.getClientIp(request);
            if (!wifiValidator.isAllowedNetwork(clientIp)) {
                redirectAttributes.addFlashAttribute("pesanError",
                        "Presensi Ditolak! Anda tidak terhubung ke Wi-Fi Kampus. (IP: " + clientIp + ")");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 2: Format token (SESI-{id}-{kodeAcak})
            String tokenBersih = qrToken.trim();
            String[] bagian = tokenBersih.split("-");
            if (bagian.length != 3 || !bagian[0].equals("SESI")) {
                redirectAttributes.addFlashAttribute("pesanError", "Format Token QR tidak dikenali!");
                return "redirect:/mahasiswa/dashboard";
            }

            Long sesiId;
            try {
                sesiId = Long.parseLong(bagian[1]);
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("pesanError", "Format Token QR tidak dikenali!");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 3: Token masih berlaku
            if (!qrTokenService.validateToken(sesiId, tokenBersih)) {
                redirectAttributes.addFlashAttribute("pesanError",
                        "Presensi Gagal! QR Code sudah kedaluwarsa atau tidak valid. Silakan scan ulang.");
                return "redirect:/mahasiswa/dashboard";
            }

            SesiKelas sesi = sesiKelasRepository.findById(sesiId).orElse(null);
            if (sesi == null) {
                redirectAttributes.addFlashAttribute("pesanError", "Sesi kelas tidak ditemukan!");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 4: Sesi masih dalam rentang waktu
            LocalDateTime sekarang = LocalDateTime.now();
            if ((sesi.getWaktuMulai() != null && sekarang.isBefore(sesi.getWaktuMulai()))
                    || (sesi.getWaktuSelesai() != null && sekarang.isAfter(sesi.getWaktuSelesai()))) {
                redirectAttributes.addFlashAttribute("pesanError", "Sesi kelas ini sudah ditutup atau belum dimulai!");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 5: Belum pernah absen di sesi ini
            if (presensiRepository.existsBySesiKelasAndMahasiswa(sesi, mahasiswa)) {
                redirectAttributes.addFlashAttribute("pesanError",
                        "Anda sudah tercatat HADIR pada sesi mata kuliah ini sebelumnya!");
                return "redirect:/mahasiswa/dashboard";
            }

            // VALIDASI 6: Satu IP tidak boleh dipakai akun lain pada sesi yang sama
            if (cekIpGanda && presensiRepository.existsBySesiKelasAndIpAddress(sesi, clientIp)) {
                redirectAttributes.addFlashAttribute("pesanError",
                        "Perangkat ini sudah dipakai untuk presensi akun lain pada sesi ini!");
                return "redirect:/mahasiswa/dashboard";
            }

            Presensi presensi = new Presensi();
            presensi.setSesiKelas(sesi);
            presensi.setMahasiswa(mahasiswa);
            presensi.setWaktuAbsen(sekarang);
            presensi.setStatus(Presensi.StatusPresensi.HADIR);
            presensi.setIpAddress(clientIp);
            presensiRepository.save(presensi);

            redirectAttributes.addFlashAttribute("pesanSukses",
                    "Presensi Berhasil! Kehadiran Anda telah tercatat.");

        } catch (Exception e) {
            e.printStackTrace(); // detail hanya di log server, tidak ditampilkan ke pengguna
            redirectAttributes.addFlashAttribute("pesanError",
                    "Terjadi kesalahan teknis. Silakan coba lagi atau hubungi dosen.");
        }

        return "redirect:/mahasiswa/dashboard";
    }
}