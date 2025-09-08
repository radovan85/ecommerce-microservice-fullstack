package com.radovan.play.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class PublicKeyCache {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyCache.class);
    private static final String CACHE_KEY = "jwt-public-key";

    private final WSClient wsClient;
    private final ServiceUrlProvider urlProvider;
    private final Cache<String, PublicKey> cache;

    @Inject
    public PublicKeyCache(WSClient wsClient, ServiceUrlProvider urlProvider) {
        this.wsClient = wsClient;
        this.urlProvider = urlProvider;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                // Uklonjen refreshAfterWrite jer zahteva LoadingCache
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
            logger.error("Failed to refresh public key", e);
            throw new RuntimeException("Public key refresh failed", e);
        }
    }

    private PublicKey fetchAndParsePublicKey() throws Exception {
        String publicKeyPem = fetchPublicKeyFromAuthService();
        return parsePublicKey(cleanKey(publicKeyPem));
    }

    private String fetchPublicKeyFromAuthService() throws Exception {
        String url = urlProvider.getAuthServiceUrl() + "/api/auth/public-key";

        WSResponse response = wsClient.url(url)
                .setRequestTimeout(5000)
                .get()
                .toCompletableFuture()
                .join();

        if (response.getStatus() != 200) {
            throw new RuntimeException("HTTP " + response.getStatus());
        }

        return response.getBody();
    }

    // Ostale metode (cleanKey, parsePublicKey, isKeyAvailable) ostaju iste
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