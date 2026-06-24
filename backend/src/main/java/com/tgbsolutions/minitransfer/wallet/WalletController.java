package com.tgbsolutions.minitransfer.wallet;

import com.tgbsolutions.minitransfer.security.CurrentUser;
import com.tgbsolutions.minitransfer.wallet.dto.BalanceResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de consultation du solde du portefeuille de l'utilisateur connecté.
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	/** Solde du portefeuille courant (authentification requise). */
	@GetMapping("/balance")
	public BalanceResponse balance(@AuthenticationPrincipal CurrentUser currentUser) {
		return walletService.getBalance(currentUser.id());
	}
}
