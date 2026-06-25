package com.tgbsolutions.minitransfer.common;

/**
 * Ressource demandée introuvable (→ HTTP 404).
 * Ex. : destinataire d'un transfert inexistant.
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
