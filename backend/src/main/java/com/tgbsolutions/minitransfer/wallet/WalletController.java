package com.tgbsolutions.minitransfer.wallet;

import com.tgbsolutions.minitransfer.security.CurrentUser;
import com.tgbsolutions.minitransfer.wallet.dto.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de consultation du solde du portefeuille de l'utilisateur connecté.
 */
@Tag(name = "Portefeuille", description = "Consultation du solde du portefeuille")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	/** Solde du portefeuille courant (authentification requise). */
	@Operation(summary = "Consulter le solde", description = "Renvoie le solde du portefeuille de l'utilisateur connecté.")
	@GetMapping("/balance")
	public BalanceResponse balance(@AuthenticationPrincipal CurrentUser currentUser) {
		return walletService.getBalance(currentUser.id());
	}
}
