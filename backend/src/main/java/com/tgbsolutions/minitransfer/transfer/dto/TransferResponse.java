package com.tgbsolutions.minitransfer.transfer.dto;

import com.tgbsolutions.minitransfer.transfer.TransactionStatus;
import java.time.Instant;

/**
 * Résultat d'un transfert effectué.
 *
 * @param transactionId identifiant de la transaction enregistrée
 * @param recipientName nom du destinataire
 * @param amount        montant transféré (FCFA)
 * @param timestamp     date et heure du transfert
 * @param status        statut de la transaction
 * @param newBalance    nouveau solde de l'émetteur après le transfert
 */
public record TransferResponse(
		String transactionId,
		String recipientName,
		long amount,
		Instant timestamp,
		TransactionStatus status,
		long newBalance) {
}
