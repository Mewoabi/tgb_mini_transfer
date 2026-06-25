package com.tgbsolutions.minitransfer.auth;

import com.tgbsolutions.minitransfer.auth.dto.AuthResponse;
import com.tgbsolutions.minitransfer.auth.dto.LoginRequest;
import com.tgbsolutions.minitransfer.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d'authentification (inscription et connexion).
 */
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** Inscription d'un utilisateur. */
	@Operation(summary = "Inscrire un utilisateur",
			description = "Crée un compte (solde initial de 10 000 FCFA) et renvoie un token JWT.")
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	/** Connexion : renvoie un token JWT. */
	@Operation(summary = "Se connecter", description = "Authentifie l'utilisateur par email et renvoie un token JWT.")
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
