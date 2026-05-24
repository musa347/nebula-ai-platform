package com.aiagent.orchestrator.escalation;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiResponseCache {

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private static final long TTL_MS = 3600_000; // 1 hour

    public Optional<String> get(String task, String context, String type) {
        String key = generateKey(task, context, type);
        CachedResponse cached = cache.get(key);

        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.response);
        }

        cache.remove(key);
        return Optional.empty();
    }

    public void put(String task, String context, String type, String response) {
        String key = generateKey(task, context, type);
        cache.put(key, new CachedResponse(response, System.currentTimeMillis() + TTL_MS));
    }

    public void clear() {
        cache.clear();
    }

    private String generateKey(String task, String context, String type) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = task + "|" + context + "|" + type;
            byte[] hash = md.digest(combined.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf((task + context + type).hashCode());
        }
    }

    private static class CachedResponse {
        final String response;
        final long expiresAt;

        CachedResponse(String response, long expiresAt) {
            this.response = response;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
