package com.radovan.spring.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@PropertySource("classpath:application.properties")
public class JwtUtil {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private long jwtExpiration;

	@Autowired
	private Environment environment;

	public JwtUtil() {
		// Privremeno postavljamo vrednosti dok Spring ne injektuje Environment
		this.privateKey = null;
		this.publicKey = null;
		this.jwtExpiration = 0;
	}

	@Autowired
	public void init() {
		String privateKeyString = environment.getProperty("jwt.private-key");
		String publicKeyString = environment.getProperty("jwt.public-key");
		String expiration = environment.getProperty("jwt.expiration");

		if (privateKeyString == null || publicKeyString == null || expiration == null) {
			throw new IllegalStateException("JWT configuration missing in application.properties");
		}

		this.privateKey = loadPrivateKey(cleanKeyString(privateKeyString));
		this.publicKey = loadPublicKey(cleanKeyString(publicKeyString));
		this.jwtExpiration = Long.parseLong(expiration);
	}

	private String cleanKeyString(String keyString) {
		if (keyString == null) {
			throw new IllegalArgumentException("Key string cannot be null");
		}

		// Uklanjanje svih whitespace karaktera osim unutar Base64 dela
		return keyString.replaceAll("-----(BEGIN|END) (PRIVATE|PUBLIC) KEY-----", "").replaceAll("\\s", "");
	}

	private PrivateKey loadPrivateKey(String keyString) {
		try {

			byte[] keyBytes = Base64.getDecoder().decode(keyString.trim());
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load private key. Key: " + keyString, e);
		}
	}

	private PublicKey loadPublicKey(String keyString) {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(keyString);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(spec);
		} catch (Exception e) {
			logger.error("Failed to load public key", e);
			throw new RuntimeException("Failed to load public key", e);
		}
	}

	public String generateToken(String username, List<String> roles) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", username);
		claims.put("roles", roles);

		String token = Jwts.builder().claims(claims).subject(username).issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusSeconds(jwtExpiration))).signWith(privateKey, Jwts.SIG.RS256)
				.compact();

		return token;
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			logger.warn("Invalid JWT token: {}", e.getMessage());
			return false;
		}
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public List<String> extractRoles(String token) {
		return extractClaim(token, claims -> claims.get("roles", List.class));
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
	}

	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public String getPublicKeyAsPEM() {
		try {
			String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			return "-----BEGIN PUBLIC KEY-----\n" + splitKeyIntoLines(encoded) + "\n-----END PUBLIC KEY-----";
		} catch (Exception e) {
			logger.error("Failed to export public key", e);
			throw new RuntimeException("Failed to export public key", e);
		}
	}

	public String getPublicKeyAsSingleLine() {
		try {
			return Base64.getEncoder().encodeToString(publicKey.getEncoded());
		} catch (Exception e) {
			logger.error("Failed to export public key", e);
			throw new RuntimeException("Failed to export public key", e);
		}
	}

	private String splitKeyIntoLines(String key) {
		// Dodaje novi red svakih 64 karaktera
		return key.replaceAll("(.{64})", "$1\n").trim();
	}
}