package com.tgbsolutions.minitransfer.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import com.tgbsolutions.minitransfer.security.JwtService;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration de l'endpoint de transfert (étape B6), avec la chaîne de sécurité
 * réelle, des transactions MongoDB (Testcontainers) et vérification de la conservation de l'argent.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TransferControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JwtService jwtService;

	@Autowired
	TransactionRepository transactionRepository;

	private User sender;
	private User recipient;
	private String senderToken;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		transactionRepository.deleteAll();
		sender = userRepository.save(new User("Alice", "alice@mail.com", "+237600000001", "hash", 10_000L, Instant.now()));
		recipient = userRepository.save(new User("Bob", "bob@mail.com", "+237600000002", "hash", 10_000L, Instant.now()));
		senderToken = jwtService.generateToken(sender.getId(), sender.getEmail(), sender.getName());
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}

	private org.springframework.test.web.servlet.ResultActions postTransfer(String token, TransferRequest request)
			throws Exception {
		return mockMvc.perform(post("/api/transfers")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(request)));
	}

	private long balanceOf(String id) {
		return userRepository.findById(id).orElseThrow().getBalance();
	}

	// Un transfert valide débite/crédite les deux portefeuilles sans perdre ni créer d'argent.
	@Test
	void successfulTransferConservesMoney() throws Exception {
		postTransfer(senderToken, new TransferRequest("bob@mail.com", 3_000L))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.amount").value(3000))
				.andExpect(jsonPath("$.newBalance").value(7000))
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		assertThat(balanceOf(sender.getId())).isEqualTo(7_000L);
		assertThat(balanceOf(recipient.getId())).isEqualTo(13_000L);
		// Conservation : la somme totale d'argent est inchangée.
		assertThat(balanceOf(sender.getId()) + balanceOf(recipient.getId())).isEqualTo(20_000L);
	}

	// Un solde insuffisant renvoie 409 et laisse les soldes intacts.
	@Test
	void insufficientBalanceReturns409AndKeepsBalances() throws Exception {
		postTransfer(senderToken, new TransferRequest("bob@mail.com", 50_000L))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));

		assertThat(balanceOf(sender.getId())).isEqualTo(10_000L);
		assertThat(balanceOf(recipient.getId())).isEqualTo(10_000L);
	}

	// Un transfert vers soi-même renvoie 400 et laisse le solde intact.
	@Test
	void selfTransferReturns400() throws Exception {
		postTransfer(senderToken, new TransferRequest("alice@mail.com", 1_000L))
				.andExpect(status().isBadRequest());

		assertThat(balanceOf(sender.getId())).isEqualTo(10_000L);
	}

	// Un destinataire inexistant renvoie 404.
	@Test
	void unknownRecipientReturns404() throws Exception {
		postTransfer(senderToken, new TransferRequest("ghost@mail.com", 1_000L))
				.andExpect(status().isNotFound());
	}

	// Un montant non strictement positif renvoie 400 (validation).
	@Test
	void nonPositiveAmountReturns400() throws Exception {
		postTransfer(senderToken, new TransferRequest("bob@mail.com", 0L))
				.andExpect(status().isBadRequest());
		postTransfer(senderToken, new TransferRequest("bob@mail.com", -100L))
				.andExpect(status().isBadRequest());

		assertThat(balanceOf(sender.getId())).isEqualTo(10_000L);
	}

	// Un transfert par numéro de téléphone réussit (201).
	@Test
	void transferByPhoneReturns201() throws Exception {
		postTransfer(senderToken, new TransferRequest("+237600000002", 2_000L))
				.andExpect(status().isCreated());

		assertThat(balanceOf(recipient.getId())).isEqualTo(12_000L);
	}

	// Sans token, le transfert est refusé (401).
	@Test
	void transferRequiresAuthentication() throws Exception {
		mockMvc.perform(post("/api/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(new TransferRequest("bob@mail.com", 1_000L))))
				.andExpect(status().isUnauthorized());
	}

	// Sans token, l'historique est refusé (401).
	@Test
	void historyRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/transfers/history"))
				.andExpect(status().isUnauthorized());
	}

	// L'historique regroupe transactions émises et reçues, triées du plus récent au plus ancien.
	@Test
	void historyReturnsSentAndReceivedNewestFirst() throws Exception {
		Instant now = Instant.now();
		// Plus ancienne : Alice a envoyé à Bob (émise du point de vue d'Alice).
		transactionRepository.save(new Transaction(sender.getId(), recipient.getId(), "Alice", "Bob",
				1_000L, now.minusSeconds(60), TransactionStatus.COMPLETED));
		// Plus récente : Bob a envoyé à Alice (reçue du point de vue d'Alice).
		transactionRepository.save(new Transaction(recipient.getId(), sender.getId(), "Bob", "Alice",
				2_000L, now, TransactionStatus.COMPLETED));

		mockMvc.perform(get("/api/transfers/history").header(HttpHeaders.AUTHORIZATION, "Bearer " + senderToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].direction").value("RECEIVED"))
				.andExpect(jsonPath("$[0].counterpartyName").value("Bob"))
				.andExpect(jsonPath("$[0].amount").value(2000))
				.andExpect(jsonPath("$[1].direction").value("SENT"))
				.andExpect(jsonPath("$[1].counterpartyName").value("Bob"))
				.andExpect(jsonPath("$[1].amount").value(1000));
	}
}
