package com.tgbsolutions.minitransfer.transfer.dto;

import com.tgbsolutions.minitransfer.transfer.TransactionStatus;
import java.time.Instant;

/**
 * Élément d'historique présenté à l'utilisateur courant.
 *
 * @param transactionId    identifiant de la transaction
 * @param direction        sens (émise ou reçue) du point de vue de l'utilisateur
 * @param counterpartyName nom de l'autre partie (destinataire si émise, émetteur si reçue)
 * @param amount           montant (FCFA)
 * @param timestamp        date et heure
 * @param status           statut de la transaction
 */
public record TransactionHistoryItem(
		String transactionId,
		TransactionDirection direction,
		String counterpartyName,
		long amount,
		Instant timestamp,
		TransactionStatus status) {
}
