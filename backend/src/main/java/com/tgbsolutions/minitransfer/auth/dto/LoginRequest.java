package com.tgbsolutions.minitransfer.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Identifiants de connexion (corps de POST /api/auth/login). La connexion se fait par email.
 */
public record LoginRequest(

		@NotBlank(message = "L'email est obligatoire.")
		@Email(message = "L'email doit être valide.")
		String email,

		@NotBlank(message = "Le mot de passe est obligatoire.")
		String password) {
}
