package com.tgbsolutions.minitransfer.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

/**
 * Tests du dépôt des transactions (étape B2), sur un MongoDB Testcontainers.
 */
@DataMongoTest
@Import(TestcontainersConfiguration.class)
class TransactionRepositoryTest {

	@Autowired
	TransactionRepository transactionRepository;

	@BeforeEach
	void cleanUp() {
		transactionRepository.deleteAll();
	}

	// L'historique d'un utilisateur regroupe transactions émises et reçues, triées par date décroissante.
	@Test
	void findsSentAndReceivedTransactionsOrderedByTimestampDesc() {
		Instant now = Instant.now();
		// A -> B : le plus ancien (émis par A)
		transactionRepository.save(new Transaction("A", "B", "Alice", "Bob", 1000L,
				now.minus(2, ChronoUnit.HOURS), TransactionStatus.COMPLETED));
		// C -> A : intermédiaire (reçu par A)
		transactionRepository.save(new Transaction("C", "A", "Carol", "Alice", 2000L,
				now.minus(1, ChronoUnit.HOURS), TransactionStatus.COMPLETED));
		// A -> D : le plus récent (émis par A)
		transactionRepository.save(new Transaction("A", "D", "Alice", "Dan", 3000L,
				now, TransactionStatus.COMPLETED));
		// Bruit : B -> C ne concerne pas A, doit être exclu.
		transactionRepository.save(new Transaction("B", "C", "Bob", "Carol", 500L,
				now, TransactionStatus.COMPLETED));

		List<Transaction> history =
				transactionRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc("A", "A");

		assertThat(history).hasSize(3);
		// Ordre décroissant par date : le plus récent (3000) en premier, le plus ancien (1000) en dernier.
		assertThat(history).extracting(Transaction::getAmount).containsExactly(3000L, 2000L, 1000L);
	}

	// Un utilisateur sans transaction obtient un historique vide.
	@Test
	void returnsEmptyHistoryForUserWithoutTransactions() {
		List<Transaction> history =
				transactionRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc("ZZZ", "ZZZ");
		assertThat(history).isEmpty();
	}
}
