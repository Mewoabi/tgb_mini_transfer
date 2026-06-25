package com.tgbsolutions.minitransfer.transfer;

import com.tgbsolutions.minitransfer.security.CurrentUser;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.transfer.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint d'exécution des transferts d'argent.
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	/** Effectue un transfert depuis l'utilisateur connecté (authentification requise). */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TransferResponse transfer(@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody TransferRequest request) {
		return transferService.transfer(currentUser.id(), request);
	}
}
