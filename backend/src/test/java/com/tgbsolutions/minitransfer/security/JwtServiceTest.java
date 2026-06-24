package com.tgbsolutions.minitransfer.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires du service JWT (étape B3) — sans contexte Spring.
 */
class JwtServiceTest {

	private static final String SECRET = "secret-de-test-suffisamment-longue-pour-hs256-0123456789-abcdef";

	private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

	// Un token généré peut être relu : le sujet et les claims sont préservés (aller-retour).
	@Test
	void generatesAndParsesTokenWithClaims() {
		String token = jwtService.generateToken("user-1", "alice@mail.com", "Alice");

		Claims claims = jwtService.parseClaims(token);

		assertThat(claims.getSubject()).isEqualTo("user-1");
		assertThat(claims.get("email", String.class)).isEqualTo("alice@mail.com");
		assertThat(claims.get("name", String.class)).isEqualTo("Alice");
	}

	// Un token expiré est rejeté.
	@Test
	void rejectsExpiredToken() {
		// Expiration dans le passé (durée négative) : le token est déjà expiré à la génération.
		JwtService expiredService = new JwtService(SECRET, -1_000L);
		String token = expiredService.generateToken("user-1", "a@b.com", "A");

		assertThatThrownBy(() -> expiredService.parseClaims(token)).isInstanceOf(JwtException.class);
	}

	// Un token falsifié (signature altérée) est rejeté.
	@Test
	void rejectsTamperedToken() {
		String token = jwtService.generateToken("user-1", "a@b.com", "A");
		char last = token.charAt(token.length() - 1);
		String tampered = token.substring(0, token.length() - 1) + (last == 'a' ? 'b' : 'a');

		assertThatThrownBy(() -> jwtService.parseClaims(tampered)).isInstanceOf(JwtException.class);
	}

	// Un token signé avec une autre clé est rejeté.
	@Test
	void rejectsTokenSignedWithDifferentSecret() {
		JwtService otherService =
				new JwtService("une-autre-secret-tres-longue-pour-hs256-9876543210-zyxwvut", 3_600_000L);
		String token = otherService.generateToken("user-1", "a@b.com", "A");

		assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(JwtException.class);
	}
}
