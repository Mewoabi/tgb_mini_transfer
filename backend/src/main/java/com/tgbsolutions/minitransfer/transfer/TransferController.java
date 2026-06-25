package com.tgbsolutions.minitransfer.transfer;

import com.tgbsolutions.minitransfer.security.CurrentUser;
import com.tgbsolutions.minitransfer.transfer.dto.TransactionHistoryItem;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.transfer.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint d'exécution des transferts d'argent.
 */
@Tag(name = "Transferts", description = "Transferts d'argent et historique des transactions")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	/** Effectue un transfert depuis l'utilisateur connecté (authentification requise). */
	@Operation(summary = "Effectuer un transfert",
			description = "Transfère un montant vers un autre utilisateur (identifié par email ou téléphone).")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TransferResponse transfer(@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody TransferRequest request) {
		return transferService.transfer(currentUser.id(), request);
	}

	/** Historique des transactions de l'utilisateur connecté, triées par date décroissante. */
	@Operation(summary = "Consulter l'historique",
			description = "Liste les transactions émises et reçues, de la plus récente à la plus ancienne.")
	@GetMapping("/history")
	public List<TransactionHistoryItem> history(@AuthenticationPrincipal CurrentUser currentUser) {
		return transferService.getHistory(currentUser.id());
	}
}
