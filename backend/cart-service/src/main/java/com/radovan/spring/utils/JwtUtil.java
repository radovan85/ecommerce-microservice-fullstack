package com.radovan.spring.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

	private PublicKeyCache publicKeyCache;

	@Autowired
	private void initialize(PublicKeyCache publicKeyCache) {
		this.publicKeyCache = publicKeyCache;
	}

	public boolean validateToken(String token) {
		try {
			PublicKey publicKey = publicKeyCache.getPublicKey().join(); // ✅ Očekuje stvarni `PublicKey`

			Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Optional<String> extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Optional<List<String>> extractRoles(String token) {
		return extractClaim(token, claims -> {
			Object rawRoles = claims.get("roles");
			if (rawRoles instanceof List<?>) {
				List<String> roles = ((List<?>) rawRoles).stream().filter(role -> role instanceof String)
						.map(role -> (String) role).toList();

				return roles;
			}
			return Collections.emptyList();
		});
	}

	private <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
		try {
			PublicKey publicKey = publicKeyCache.getPublicKey().join(); // ✅ Sada dobijamo pravi PublicKey

			Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
			return Optional.ofNullable(claimsResolver.apply(claims));
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

}
