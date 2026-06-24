package com.tgbsolutions.minitransfer.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tgbsolutions.minitransfer.common.ResourceNotFoundException;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import com.tgbsolutions.minitransfer.wallet.dto.BalanceResponse;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service du portefeuille (étape B5), dépendances simulées (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

	@Mock
	UserRepository userRepository;

	@InjectMocks
	WalletService walletService;

	// Renvoie le solde et la devise pour un utilisateur existant.
	@Test
	void returnsBalanceForExistingUser() {
		User user = new User("Alice", "alice@mail.com", "+237600000001", "hash", 7_500L, Instant.now());
		user.setId("user-1");
		when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

		BalanceResponse response = walletService.getBalance("user-1");

		assertThat(response.balance()).isEqualTo(7_500L);
		assertThat(response.currency()).isEqualTo("FCFA");
	}

	// Lève une erreur si l'utilisateur n'existe pas.
	@Test
	void throwsWhenUserNotFound() {
		when(userRepository.findById("inconnu")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> walletService.getBalance("inconnu"))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
