package com.tgbsolutions.minitransfer.common;

/**
 * Transfert invalide au regard des règles métier (→ HTTP 400).
 * Ex. : transfert vers soi-même, montant non strictement positif.
 */
public class InvalidTransferException extends RuntimeException {

	public InvalidTransferException(String message) {
		super(message);
	}
}
