package com.tgbsolutions.minitransfer.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Vérifie la cohérence transactionnelle (étape B6) : si l'enregistrement de la transaction
 * échoue après le débit/crédit, toute l'opération est annulée (rollback) et les soldes
 * reviennent à leur valeur initiale. C'est la garantie « ne jamais perdre ni créer d'argent ».
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransferRollbackTest {

	@Autowired
	TransferService transferService;

	@Autowired
	UserRepository userRepository;

	// On simule une panne lors de l'enregistrement de la transaction.
	@MockitoBean
	TransactionRepository transactionRepository;

	private User sender;
	private User recipient;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		sender = userRepository.save(new User("Alice", "alice@mail.com", "+237600000001", "hash", 10_000L, Instant.now()));
		recipient = userRepository.save(new User("Bob", "bob@mail.com", "+237600000002", "hash", 10_000L, Instant.now()));
	}

	// Une panne pendant l'enregistrement annule le débit et le crédit (soldes inchangés).
	@Test
	void rollsBackBalancesWhenRecordingFails() {
		when(transactionRepository.save(any(Transaction.class)))
				.thenThrow(new RuntimeException("panne simulée lors de l'enregistrement"));

		assertThatThrownBy(() -> transferService.transfer(sender.getId(), new TransferRequest("bob@mail.com", 3_000L)))
				.isInstanceOf(RuntimeException.class);

		// La transaction MongoDB a été annulée : aucun argent n'a bougé.
		assertThat(userRepository.findById(sender.getId()).orElseThrow().getBalance()).isEqualTo(10_000L);
		assertThat(userRepository.findById(recipient.getId()).orElseThrow().getBalance()).isEqualTo(10_000L);
	}
}
