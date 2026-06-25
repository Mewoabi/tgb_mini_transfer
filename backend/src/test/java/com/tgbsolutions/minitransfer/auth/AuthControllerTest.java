package com.tgbsolutions.minitransfer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import com.tgbsolutions.minitransfer.auth.dto.LoginRequest;
import com.tgbsolutions.minitransfer.auth.dto.RegisterRequest;
import com.tgbsolutions.minitransfer.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration des endpoints d'authentification (étape B4), avec la chaîne
 * de sécurité réelle et un MongoDB Testcontainers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserRepository userRepository;

	@BeforeEach
	void cleanUp() {
		userRepository.deleteAll();
	}

	private String json(Object value) throws Exception {
		return objectMapper.writeValueAsString(value);
	}

	private void register(RegisterRequest request) throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(request)))
				.andExpect(status().isCreated());
	}

	// L'inscription renvoie 201, un token et le solde initial de 10 000 FCFA.
	@Test
	void registerReturns201WithTokenAndInitialBalance() throws Exception {
		RegisterRequest request =
				new RegisterRequest("Alice", "alice@mail.com", "+237600000001", "password123");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("alice@mail.com"))
				.andExpect(jsonPath("$.user.balance").value(10000));

		assertThat(userRepository.findByEmail("alice@mail.com")).isPresent();
	}

	// Un corps invalide renvoie 400 avec le détail des erreurs par champ.
	@Test
	void registerWithInvalidBodyReturns400() throws Exception {
		RegisterRequest invalid = new RegisterRequest("", "pas-un-email", "abc", "court");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(invalid)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors").exists());
	}

	// Une inscription avec un email déjà utilisé renvoie 409.
	@Test
	void registerWithDuplicateEmailReturns409() throws Exception {
		register(new RegisterRequest("Alice", "dup@mail.com", "+237600000001", "password123"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(new RegisterRequest("Bob", "dup@mail.com", "+237600000002", "password123"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	// La connexion avec de bons identifiants renvoie 200 et un token.
	@Test
	void loginReturns200WithToken() throws Exception {
		register(new RegisterRequest("Alice", "alice@mail.com", "+237600000001", "password123"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(new LoginRequest("alice@mail.com", "password123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value("alice@mail.com"));
	}

	// La connexion avec un mauvais mot de passe renvoie 401.
	@Test
	void loginWithWrongPasswordReturns401() throws Exception {
		register(new RegisterRequest("Alice", "alice@mail.com", "+237600000001", "password123"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(new LoginRequest("alice@mail.com", "mauvais-mot-de-passe"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}
}
