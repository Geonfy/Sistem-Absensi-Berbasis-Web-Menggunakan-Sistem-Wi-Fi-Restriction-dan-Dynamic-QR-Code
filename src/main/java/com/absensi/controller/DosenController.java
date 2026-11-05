package com.absensi.controller;

import com.absensi.model.Presensi;
import com.absensi.model.SesiKelas;
import com.absensi.model.User;
import com.absensi.qr.QrTokenService;
import com.absensi.repository.MataKuliahRepository;
import com.absensi.repository.PresensiRepository;
import com.absensi.repository.SesiKelasRepository;
import com.absensi.model.MataKuliah;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dosen")
public class DosenController {

    @Autowired
    private MataKuliahRepository mataKuliahRepository;

    @Autowired
    private SesiKelasRepository sesiKelasRepository;

    @Autowired
    private PresensiRepository presensiRepository;

    @Autowired
    private QrTokenService qrTokenService;

    // ---------- Fungsi bantu ----------

    // Mengembalikan user jika yang login adalah DOSEN, selain itu null
    private User getDosen(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == User.Role.DOSEN) {
            return user;
        }
        return null;
    }

    // Hanya dosen pemilik sesi yang boleh mengakses sesi tersebut
    private boolean pemilikSesi(SesiKelas sesi, User dosen) {
        return sesi.getDosen() != null && sesi.getDosen().getId().equals(dosen.getId());
    }

    // ---------- 1. Dashboard Dosen ----------
    @GetMapping("/dashboard")
    public String dashboardDosen(HttpSession session, Model model) {
        User dosen = getDosen(session);
        if (dosen == null) {
            return "redirect:/login";
        }

        model.addAttribute("dosen", dosen);
        model.addAttribute("daftarMk", mataKuliahRepository.findAll());
        model.addAttribute("daftarSesi", sesiKelasRepository.findByDosen(dosen));
        return "dosen/dashboard";
    }

    // ---------- 2. Membuat sesi kelas ----------
    @PostMapping("/sesi/buat")
    public String buatSesiKelas(@RequestParam Long mataKuliahId,
                                @RequestParam Integer pertemuanKe,
                                @RequestParam String ruangan,
                                HttpSession session) {
        User dosen = getDosen(session);
        if (dosen == null) {
            return "redirect:/login";
        }

        MataKuliah mk = mataKuliahRepository.findById(mataKuliahId).orElse(null);
        if (mk == null) {
            return "redirect:/dosen/dashboard";
        }

        LocalDateTime sekarang = LocalDateTime.now();
        SesiKelas sesi = new SesiKelas();
        sesi.setMataKuliah(mk);
        sesi.setDosen(dosen);
        sesi.setPertemuanKe(pertemuanKe);
        sesi.setRuangan(ruangan);
        sesi.setWaktuMulai(sekarang);
        sesi.setWaktuSelesai(sekarang.plusHours(2));
        sesiKelasRepository.save(sesi);

        return "redirect:/dosen/sesi/" + sesi.getId() + "/qr";
    }

    // ---------- 3. Layar proyektor QR ----------
    @GetMapping("/sesi/{id}/qr")
    public String tampilkanLayarQr(@PathVariable Long id, HttpSession session, Model model) {
        User dosen = getDosen(session);
        if (dosen == null) {
            return "redirect:/login";
        }

        SesiKelas sesi = sesiKelasRepository.findById(id).orElse(null);
        if (sesi == null || !pemilikSesi(sesi, dosen)) {
            return "redirect:/dosen/dashboard";
        }

        List<Presensi> daftarHadir = presensiRepository.findBySesiKelas(sesi);

        model.addAttribute("dosen", dosen);
        model.addAttribute("sesi", sesi);
        model.addAttribute("daftarHadir", daftarHadir);
        return "dosen/layarqr";
    }

    // ---------- 4. API token QR (hanya dosen pemilik sesi) ----------
    @GetMapping("/api/qr-token/{sesiId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getNewQrToken(@PathVariable Long sesiId, HttpSession session) {
        User dosen = getDosen(session);
        if (dosen == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SesiKelas sesi = sesiKelasRepository.findById(sesiId).orElse(null);
        if (sesi == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!pemilikSesi(sesi, dosen)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", qrTokenService.generateNewToken(sesiId));
        return ResponseEntity.ok(response);
    }

    // ---------- 5. Ekspor rekap kehadiran ke CSV ----------
    @GetMapping("/sesi/{id}/export")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id, HttpSession session) {
        User dosen = getDosen(session);
        if (dosen == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SesiKelas sesi = sesiKelasRepository.findById(id).orElse(null);
        if (sesi == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!pemilikSesi(sesi, dosen)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM agar Excel membaca UTF-8 dengan benar
        sb.append("NIM,Nama Mahasiswa,Status,Waktu Absen,IP Address\r\n");

        for (Presensi p : presensiRepository.findBySesiKelas(sesi)) {
            String nim = p.getMahasiswa() != null ? p.getMahasiswa().getNomorInduk() : "";
            String nama = p.getMahasiswa() != null ? p.getMahasiswa().getNamaLengkap() : "";
            String status = p.getStatus() != null ? p.getStatus().name() : "";
            String waktu = p.getWaktuAbsen() != null ? p.getWaktuAbsen().format(fmt) : "";

            sb.append(csv(nim)).append(',')
              .append(csv(nama)).append(',')
              .append(csv(status)).append(',')
              .append(csv(waktu)).append(',')
              .append(csv(p.getIpAddress())).append("\r\n");
        }

        String namaFile = "presensi_sesi_" + sesi.getId() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + namaFile + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // Membungkus nilai CSV dan mencegah CSV injection (sel diawali = + - @)
    private String csv(String nilai) {
        if (nilai == null) {
            return "\"\"";
        }
        String s = nilai;
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}