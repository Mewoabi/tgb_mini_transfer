package com.tgbsolutions.minitransfer.auth;

import com.tgbsolutions.minitransfer.auth.dto.AuthResponse;
import com.tgbsolutions.minitransfer.auth.dto.LoginRequest;
import com.tgbsolutions.minitransfer.auth.dto.RegisterRequest;
import com.tgbsolutions.minitransfer.common.DuplicateResourceException;
import com.tgbsolutions.minitransfer.security.JwtService;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import com.tgbsolutions.minitransfer.user.dto.UserSummary;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification : inscription et connexion des utilisateurs.
 */
@Service
public class AuthService {

	/** Solde de démonstration crédité à l'inscription (en FCFA). */
	public static final long INITIAL_BALANCE = 10_000L;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	/**
	 * Inscrit un nouvel utilisateur : vérifie l'unicité (email, téléphone), chiffre le mot de passe,
	 * crédite le solde initial, puis renvoie un token JWT.
	 */
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateResourceException("Un compte existe déjà avec cet email.");
		}
		if (userRepository.existsByPhone(request.phone())) {
			throw new DuplicateResourceException("Un compte existe déjà avec ce numéro de téléphone.");
		}

		User user = new User(
				request.name(),
				request.email(),
				request.phone(),
				passwordEncoder.encode(request.password()),
				INITIAL_BALANCE,
				Instant.now());

		try {
			user = userRepository.save(user);
		} catch (DuplicateKeyException ex) {
			// Filet de sécurité contre une inscription concurrente (course sur l'index unique).
			throw new DuplicateResourceException("Un compte existe déjà avec cet email ou ce numéro de téléphone.");
		}

		return buildAuthResponse(user);
	}

	/**
	 * Authentifie un utilisateur par email + mot de passe et renvoie un token JWT.
	 * Le même message d'erreur est renvoyé que l'email soit inconnu ou le mot de passe erroné,
	 * afin de ne pas révéler quels comptes existent.
	 */
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BadCredentialsException("Identifiants invalides."));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Identifiants invalides.");
		}

		return buildAuthResponse(user);
	}

	private AuthResponse buildAuthResponse(User user) {
		String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
		return new AuthResponse(token, "Bearer", UserSummary.from(user));
	}
}
