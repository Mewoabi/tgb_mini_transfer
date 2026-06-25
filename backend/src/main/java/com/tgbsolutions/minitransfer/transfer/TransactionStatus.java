package com.tgbsolutions.minitransfer.transfer;

/**
 * Statut d'une transaction.
 *
 * <p>Seul {@code COMPLETED} est utilisé pour l'instant : seuls les transferts réussis sont
 * enregistrés. L'énumération reste ouverte à des statuts futurs (ex. PENDING, FAILED).</p>
 */
public enum TransactionStatus {
	COMPLETED
}
