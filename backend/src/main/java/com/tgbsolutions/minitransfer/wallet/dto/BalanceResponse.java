package com.tgbsolutions.minitransfer.wallet.dto;

/**
 * Solde du portefeuille de l'utilisateur connecté.
 *
 * @param balance  solde courant, en entier (FCFA)
 * @param currency devise (toujours « FCFA »)
 */
public record BalanceResponse(long balance, String currency) {
}
