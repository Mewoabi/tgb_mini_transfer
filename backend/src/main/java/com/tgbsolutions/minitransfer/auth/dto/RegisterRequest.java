package com.tgbsolutions.minitransfer.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Données d'inscription d'un utilisateur (corps de POST /api/auth/register).
 */
public record RegisterRequest(

		@NotBlank(message = "Le nom est obligatoire.")
		String name,

		@NotBlank(message = "L'email est obligatoire.")
		@Email(message = "L'email doit être valide.")
		String email,

		@NotBlank(message = "Le numéro de téléphone est obligatoire.")
		@Pattern(regexp = "^\\+?[0-9]{8,15}$",
				message = "Le numéro de téléphone doit contenir 8 à 15 chiffres (préfixe + optionnel).")
		String phone,

		@NotBlank(message = "Le mot de passe est obligatoire.")
		@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
		String password) {
}
