package com.tgbsolutions.minitransfer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service de gestion des tokens JWT (signés en HS256).
 *
 * <p>Le sujet (subject) du token est l'identifiant de l'utilisateur ; l'email et le nom
 * sont ajoutés comme claims pour reconstruire le {@link CurrentUser} sans accès à la base.</p>
 */
@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		// La clé doit faire au moins 256 bits (32 octets) pour HS256.
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	/** Génère un token signé pour l'utilisateur donné. */
	public String generateToken(String userId, String email, String name) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId)
				.claim("email", email)
				.claim("name", name)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}

	/**
	 * Vérifie la signature et l'expiration du token, puis renvoie ses claims.
	 *
	 * @throws io.jsonwebtoken.JwtException si le token est invalide, expiré ou falsifié
	 */
	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
