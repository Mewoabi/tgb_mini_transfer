package com.tgbsolutions.minitransfer.wallet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import com.tgbsolutions.minitransfer.security.JwtService;
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
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration de l'endpoint de solde (étape B5), avec la chaîne de sécurité
 * réelle et un MongoDB Testcontainers. Couvre aussi le comportement de sécurité
 * (401 sans token, 200 avec un token valide).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class WalletControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JwtService jwtService;

	@BeforeEach
	void cleanUp() {
		userRepository.deleteAll();
	}

	// Sans token, l'accès au solde est refusé (401).
	@Test
	void balanceRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/wallet/balance"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	// Un token invalide est refusé (401).
	@Test
	void rejectsInvalidToken() throws Exception {
		mockMvc.perform(get("/api/wallet/balance").header(HttpHeaders.AUTHORIZATION, "Bearer token-bidon"))
				.andExpect(status().isUnauthorized());
	}

	// Avec un token valide, renvoie le solde et la devise (200).
	@Test
	void returnsBalanceWithValidToken() throws Exception {
		User user = userRepository.save(
				new User("Alice", "alice@mail.com", "+237600000001", "hash", 10_000L, Instant.now()));
		String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());

		mockMvc.perform(get("/api/wallet/balance").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(10000))
				.andExpect(jsonPath("$.currency").value("FCFA"));
	}
}
