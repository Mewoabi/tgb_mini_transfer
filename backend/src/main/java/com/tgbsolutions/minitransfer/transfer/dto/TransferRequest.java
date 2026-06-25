package com.tgbsolutions.minitransfer.transfer.dto;

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

		@NotBlank(message = "Le destinataire (email ou téléphone) est obligatoire.")
		String recipient,

		@NotNull(message = "Le montant est obligatoire.")
		@Positive(message = "Le montant doit être strictement positif.")
		Long amount) {
}
