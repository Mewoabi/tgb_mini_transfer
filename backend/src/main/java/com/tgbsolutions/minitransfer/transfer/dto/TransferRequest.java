package com.tgbsolutions.minitransfer.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Demande de transfert (corps de POST /api/transfers).
 *
 * @param recipient email ou numéro de téléphone du destinataire
 * @param amount    montant à transférer, en FCFA (entier strictement positif)
 */
public record TransferRequest(

		@Schema(example = "bob@example.com", description = "Email ou numéro de téléphone du destinataire")
		@NotBlank(message = "Le destinataire (email ou téléphone) est obligatoire.")
		String recipient,

		@Schema(example = "3000", description = "Montant à transférer, en FCFA (entier strictement positif)")
		@NotNull(message = "Le montant est obligatoire.")
		@Positive(message = "Le montant doit être strictement positif.")
		Long amount) {
}
