package com.absensi.wifi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WifiValidator {

    // Awalan IP jaringan Wi-Fi kampus, bisa diubah di application.properties
    @Value("${absensi.wifi.prefix:192.168.1.}")
    private String allowedPrefix;

    // true HANYA saat pengembangan (uji di laptop sendiri). Matikan saat demo/produksi.
    @Value("${absensi.wifi.izinkan-localhost:false}")
    private boolean izinkanLocalhost;

    public boolean isAllowedNetwork(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }
        if (ipAddress.equals("127.0.0.1")
                || ipAddress.equals("0:0:0:0:0:0:0:1")
                || ipAddress.equals("::1")) {
            return izinkanLocalhost;
        }
        return ipAddress.startsWith(allowedPrefix);
    }

    // Hanya memakai IP koneksi asli. Header X-Forwarded-For SENGAJA tidak dipakai
    // karena bisa dipalsukan oleh klien.
    public String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}