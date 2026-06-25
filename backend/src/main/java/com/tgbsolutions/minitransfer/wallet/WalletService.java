package com.tgbsolutions.minitransfer.wallet;

import com.tgbsolutions.minitransfer.common.ResourceNotFoundException;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import com.tgbsolutions.minitransfer.wallet.dto.BalanceResponse;
import org.springframework.stereotype.Service;

/**
 * Service du portefeuille : consultation du solde de l'utilisateur connecté.
 */
@Service
public class WalletService {

	/** Devise utilisée par la plateforme. */
	public static final String CURRENCY = "FCFA";

	private final UserRepository userRepository;

	public WalletService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/** Renvoie le solde de l'utilisateur identifié par son id (issu du token JWT). */
	public BalanceResponse getBalance(String userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
		return new BalanceResponse(user.getBalance(), CURRENCY);
	}
}
