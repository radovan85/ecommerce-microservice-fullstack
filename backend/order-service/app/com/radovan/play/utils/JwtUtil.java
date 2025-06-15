package com.radovan.play.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Singleton
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private Provider<PublicKeyCache> publicKeyCacheProvider;

    @Inject
    private void initialize(Provider<PublicKeyCache> publicKeyCacheProvider) {
        this.publicKeyCacheProvider = publicKeyCacheProvider;
    }

    public CompletableFuture<Boolean> validateToken(String token) {
        return publicKeyCacheProvider.get().getPublicKey()
                .thenApply(publicKey -> {
                    try {
                        Jwts.parser()
                                .verifyWith(publicKey)
                                .build()
                                .parseSignedClaims(token);
                        return true;
                    } catch (Exception e) {
                        logger.warn("Token validation failed: {}", e.getMessage());
                        return false;
                    }
                });
    }

    public CompletableFuture<Optional<String>> extractUsername(String token) {
        return extractClaim(token, Claims::getSubject)
                .exceptionally(ex -> {
                    logger.error("Username extraction failed", ex);
                    return Optional.empty();
                });
    }

    public CompletableFuture<Optional<List<String>>> extractRoles(String token) {
        return extractClaim(token, claims -> {
            Object rawRoles = claims.get("roles");

            if (rawRoles instanceof List<?>) {
                List<String> roles = new ArrayList<>();
                for (Object role : (List<?>) rawRoles) {
                    if (role instanceof String) {
                        roles.add((String) role);
                    }
                }
                return roles;
            }
            return Collections.<String>emptyList();
        });
    }

    public CompletableFuture<Optional<Date>> extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> CompletableFuture<Optional<T>> extractClaim(String token, Function<Claims, T> claimsResolver) {
        return publicKeyCacheProvider.get().getPublicKey()
                .thenApply(publicKey -> {
                    try {
                        Claims claims = Jwts.parser()
                                .verifyWith(publicKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                        T result = claimsResolver.apply(claims);
                        return Optional.ofNullable(result);
                    } catch (Exception e) {
                        logger.warn("Token parsing failed", e);
                        return Optional.<T>empty();
                    }
                });
    }

    // Helper za clean token
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
