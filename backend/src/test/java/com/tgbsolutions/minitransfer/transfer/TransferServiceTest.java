package com.tgbsolutions.minitransfer.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.result.UpdateResult;
import com.tgbsolutions.minitransfer.common.InsufficientBalanceException;
import com.tgbsolutions.minitransfer.common.InvalidTransferException;
import com.tgbsolutions.minitransfer.common.ResourceNotFoundException;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.transfer.dto.TransferResponse;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

/**
 * Tests unitaires du service de transfert (étape B6), dépendances simulées (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	TransactionRepository transactionRepository;

	@Mock
	MongoTemplate mongoTemplate;

	@InjectMocks
	TransferService transferService;

	private User user(String id, String name, String email, String phone, long balance) {
		User u = new User(name, email, phone, "hash", balance, Instant.now());
		u.setId(id);
		return u;
	}

	// Un transfert valide débite l'émetteur, enregistre la transaction et renvoie le nouveau solde.
	@Test
	void successfulTransferRecordsTransactionAndReturnsNewBalance() {
		User sender = user("sender-1", "Alice", "alice@mail.com", "+237600000001", 10_000L);
		User recipient = user("recipient-1", "Bob", "bob@mail.com", "+237600000002", 5_000L);
		when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
		when(userRepository.findByEmail("bob@mail.com")).thenReturn(Optional.of(recipient));
		when(mongoTemplate.updateFirst(any(Query.class), any(UpdateDefinition.class), eq(User.class)))
				.thenReturn(UpdateResult.acknowledged(1L, 1L, null));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
			Transaction saved = invocation.getArgument(0);
			saved.setId("tx-1");
			return saved;
		});

		TransferResponse response =
				transferService.transfer("sender-1", new TransferRequest("bob@mail.com", 3_000L));

		assertThat(response.amount()).isEqualTo(3_000L);
		assertThat(response.newBalance()).isEqualTo(7_000L);
		assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
		assertThat(response.recipientName()).isEqualTo("Bob");
		verify(transactionRepository).save(any(Transaction.class));
	}

	// Le destinataire peut être identifié par son numéro de téléphone.
	@Test
	void resolvesRecipientByPhoneWhenEmailNotFound() {
		User sender = user("sender-1", "Alice", "alice@mail.com", "+237600000001", 10_000L);
		User recipient = user("recipient-1", "Bob", "bob@mail.com", "+237600000002", 5_000L);
		when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
		when(userRepository.findByEmail("+237600000002")).thenReturn(Optional.empty());
		when(userRepository.findByPhone("+237600000002")).thenReturn(Optional.of(recipient));
		when(mongoTemplate.updateFirst(any(Query.class), any(UpdateDefinition.class), eq(User.class)))
				.thenReturn(UpdateResult.acknowledged(1L, 1L, null));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

		TransferResponse response =
				transferService.transfer("sender-1", new TransferRequest("+237600000002", 1_000L));

		assertThat(response.recipientName()).isEqualTo("Bob");
	}

	// Solde insuffisant : le débit conditionnel ne modifie rien → erreur, aucune transaction enregistrée.
	@Test
	void rejectsTransferWhenBalanceInsufficient() {
		User sender = user("sender-1", "Alice", "alice@mail.com", "+237600000001", 1_000L);
		User recipient = user("recipient-1", "Bob", "bob@mail.com", "+237600000002", 5_000L);
		when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
		when(userRepository.findByEmail("bob@mail.com")).thenReturn(Optional.of(recipient));
		when(mongoTemplate.updateFirst(any(Query.class), any(UpdateDefinition.class), eq(User.class)))
				.thenReturn(UpdateResult.acknowledged(0L, 0L, null));

		assertThatThrownBy(() -> transferService.transfer("sender-1", new TransferRequest("bob@mail.com", 3_000L)))
				.isInstanceOf(InsufficientBalanceException.class);

		verify(transactionRepository, never()).save(any());
	}

	// Un transfert vers soi-même est rejeté.
	@Test
	void rejectsSelfTransfer() {
		User sender = user("sender-1", "Alice", "alice@mail.com", "+237600000001", 10_000L);
		when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
		when(userRepository.findByEmail("alice@mail.com")).thenReturn(Optional.of(sender));

		assertThatThrownBy(() -> transferService.transfer("sender-1", new TransferRequest("alice@mail.com", 1_000L)))
				.isInstanceOf(InvalidTransferException.class);

		verify(transactionRepository, never()).save(any());
	}

	// Un destinataire inexistant est rejeté.
	@Test
	void rejectsUnknownRecipient() {
		User sender = user("sender-1", "Alice", "alice@mail.com", "+237600000001", 10_000L);
		when(userRepository.findById("sender-1")).thenReturn(Optional.of(sender));
		when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());
		when(userRepository.findByPhone("ghost@mail.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> transferService.transfer("sender-1", new TransferRequest("ghost@mail.com", 1_000L)))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	// Un montant non strictement positif est rejeté.
	@Test
	void rejectsNonPositiveAmount() {
		assertThatThrownBy(() -> transferService.transfer("sender-1", new TransferRequest("bob@mail.com", 0L)))
				.isInstanceOf(InvalidTransferException.class);

		verify(userRepository, never()).findById(any());
	}

	// Un émetteur inexistant (token valide mais compte supprimé) est rejeté.
	@Test
	void rejectsUnknownSender() {
		when(userRepository.findById("sender-1")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> transferService.transfer("sender-1", new TransferRequest("bob@mail.com", 1_000L)))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
