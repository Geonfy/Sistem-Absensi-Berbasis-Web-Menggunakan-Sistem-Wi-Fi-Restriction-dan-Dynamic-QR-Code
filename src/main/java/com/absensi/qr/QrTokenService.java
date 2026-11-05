package com.absensi.qr;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QrTokenService {

    private static final long TTL_MS = 15_000; // 15 detik

    private final Map<Long, List<TokenData>> activeTokens = new ConcurrentHashMap<>();

    public static class TokenData {
        private final String token;
        private final long expiryTime;

        public TokenData(String token, long expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }
        public String getToken() { return token; }
        public long getExpiryTime() { return expiryTime; }
    }

    public String generateNewToken(Long sesiId) {
        String randomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String token = "SESI-" + sesiId + "-" + randomCode;
        long now = System.currentTimeMillis();

        List<TokenData> list = activeTokens.computeIfAbsent(sesiId, k -> new CopyOnWriteArrayList<>());
        list.removeIf(t -> t.getExpiryTime() < now);          // bersihkan yang sudah expired
        list.add(new TokenData(token, now + TTL_MS));         // tambahkan, JANGAN menimpa
        return token;
    }

    public boolean validateToken(Long sesiId, String inputToken) {
        List<TokenData> list = activeTokens.get(sesiId);
        if (list == null) return false;
        long now = System.currentTimeMillis();
        return list.stream().anyMatch(t -> t.getToken().equals(inputToken) && now <= t.getExpiryTime());
    }
}