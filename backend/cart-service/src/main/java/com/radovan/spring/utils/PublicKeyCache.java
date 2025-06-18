package com.radovan.spring.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class PublicKeyCache {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyCache.class);
    private static final String CACHE_KEY = "jwt-public-key";

    private final RestTemplate restTemplate;
    private final ServiceUrlProvider urlProvider;
    private final Cache<String, PublicKey> cache;

    public PublicKeyCache(ServiceUrlProvider urlProvider, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
    }

    public CompletableFuture<PublicKey> getPublicKey() {
        return CompletableFuture.supplyAsync(() -> {
            PublicKey cachedKey = cache.getIfPresent(CACHE_KEY);
            if (cachedKey != null) {
                return cachedKey;
            }
            return refreshPublicKey();
        });
    }

    public PublicKey refreshPublicKey() {
        try {
            PublicKey newKey = fetchAndParsePublicKey();
            cache.put(CACHE_KEY, newKey);
            return newKey;
        } catch (Exception e) {
            logger.error("*** [ERROR] Failed to refresh public key", e);
            throw new RuntimeException("Public key refresh failed", e);
        }
    }

    private PublicKey fetchAndParsePublicKey() throws Exception {
        String publicKeyPem = fetchPublicKeyFromAuthService();
        return parsePublicKey(cleanKey(publicKeyPem));
    }

    private String fetchPublicKeyFromAuthService() throws Exception {
        String url = urlProvider.getAuthServiceUrl() + "/api/auth/public-key";
        logger.debug("*** [DEBUG] Fetching public key from: {}", url);

        String publicKeyPem = restTemplate.getForObject(url, String.class);

        if (publicKeyPem == null || publicKeyPem.trim().isEmpty()) {
            throw new RuntimeException("*** [ERROR] Received empty response from auth-service!");
        }

        return publicKeyPem;
    }

    private String cleanKey(String publicKeyPem) {
        return publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private PublicKey parsePublicKey(String keyBase64) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public boolean isKeyAvailable() {
        return cache.getIfPresent(CACHE_KEY) != null;
    }
}
