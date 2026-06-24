package com.tgbsolutions.minitransfer.common;

/**
 * Conflit : une ressource unique existe déjà (→ HTTP 409).
 * Ex. : inscription avec un email ou un téléphone déjà utilisé.
 */
public class DuplicateResourceException extends RuntimeException {

	public DuplicateResourceException(String message) {
		super(message);
	}
}
