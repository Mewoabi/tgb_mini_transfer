package com.tgbsolutions.minitransfer.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Données d'inscription d'un utilisateur (corps de POST /api/auth/register).
 */
public record RegisterRequest(

		@Schema(example = "Alice Demo", description = "Nom complet de l'utilisateur")
		@NotBlank(message = "Le nom est obligatoire.")
		String name,

		@Schema(example = "alice@example.com", description = "Email unique, sert d'identifiant de connexion")
		@NotBlank(message = "L'email est obligatoire.")
		@Email(message = "L'email doit être valide.")
		String email,

		@Schema(example = "+237600000001", description = "Numéro de téléphone unique (8 à 15 chiffres, préfixe + optionnel)")
		@NotBlank(message = "Le numéro de téléphone est obligatoire.")
		@Pattern(regexp = "^\\+?[0-9]{8,15}$",
				message = "Le numéro de téléphone doit contenir 8 à 15 chiffres (préfixe + optionnel).")
		String phone,

		@Schema(example = "password123", description = "Mot de passe (au moins 8 caractères)")
		@NotBlank(message = "Le mot de passe est obligatoire.")
		@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
		String password) {
}
