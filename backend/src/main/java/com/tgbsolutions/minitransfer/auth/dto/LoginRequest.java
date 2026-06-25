package com.tgbsolutions.minitransfer.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Identifiants de connexion (corps de POST /api/auth/login). La connexion se fait par email.
 *
 * <p>Les exemples correspondent à l'utilisateur créé par l'exemple d'inscription : on peut donc
 * s'inscrire puis se connecter en cliquant simplement « Execute » dans Swagger.</p>
 */
public record LoginRequest(

		@Schema(example = "alice@example.com", description = "Email du compte")
		@NotBlank(message = "L'email est obligatoire.")
		@Email(message = "L'email doit être valide.")
		String email,

		@Schema(example = "password123", description = "Mot de passe du compte")
		@NotBlank(message = "Le mot de passe est obligatoire.")
		String password) {
}
