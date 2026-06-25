package com.tgbsolutions.minitransfer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité HTTP.
 *
 * <p>API sans état (stateless) : pas de session, authentification uniquement par token JWT.
 * Les endpoints d'authentification et de santé sont publics ; tout le reste exige un token valide.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtService jwtService;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public SecurityConfig(JwtService jwtService, RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtService = jwtService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// API REST sans formulaire ni cookie de session : CSRF inutile.
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						// Documentation publique (OpenAPI JSON + Swagger UI).
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.anyRequest().authenticated())
				// Renvoie un 401 JSON normalisé en cas de requête non authentifiée.
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
				// Le filtre JWT s'exécute avant le filtre d'authentification par identifiant/mot de passe.
				.addFilterBefore(new JwtAuthenticationFilter(jwtService),
						UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/** Encodeur de mot de passe : BCrypt (jamais de mot de passe stocké en clair). */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
