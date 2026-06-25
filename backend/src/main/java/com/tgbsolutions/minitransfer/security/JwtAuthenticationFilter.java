package com.tgbsolutions.minitransfer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre d'authentification JWT exécuté une fois par requête.
 *
 * <p>Si l'en-tête {@code Authorization: Bearer <token>} contient un token valide, l'utilisateur
 * est authentifié dans le contexte de sécurité (principal = {@link CurrentUser}). Sinon, la
 * requête poursuit sans authentification : le point d'entrée renverra un 401 si l'accès est protégé.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			String token = header.substring(BEARER_PREFIX.length());
			try {
				Claims claims = jwtService.parseClaims(token);
				CurrentUser principal = new CurrentUser(
						claims.getSubject(),
						claims.get("email", String.class),
						claims.get("name", String.class));

				var authentication = new UsernamePasswordAuthenticationToken(
						principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException ex) {
				// Token absent, malformé, expiré ou falsifié : on n'authentifie pas.
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
