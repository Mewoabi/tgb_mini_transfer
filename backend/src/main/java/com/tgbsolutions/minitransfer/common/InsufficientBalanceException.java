package com.tgbsolutions.minitransfer.common;

/**
 * Solde insuffisant pour effectuer un transfert (→ HTTP 409).
 */
public class InsufficientBalanceException extends RuntimeException {

	public InsufficientBalanceException(String message) {
		super(message);
	}
}
