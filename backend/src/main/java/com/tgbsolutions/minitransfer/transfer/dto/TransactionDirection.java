package com.tgbsolutions.minitransfer.transfer.dto;

/**
 * Sens d'une transaction du point de vue de l'utilisateur courant.
 *
 * <p>{@code SENT} : l'utilisateur est l'émetteur ; {@code RECEIVED} : il est le destinataire.</p>
 */
public enum TransactionDirection {
	SENT,
	RECEIVED
}
