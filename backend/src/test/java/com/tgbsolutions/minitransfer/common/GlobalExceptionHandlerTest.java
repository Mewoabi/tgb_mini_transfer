package com.tgbsolutions.minitransfer.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests du gestionnaire d'erreurs (étape B3), via un MockMvc autonome
 * (contrôleur jetable + advice), sans contexte Spring ni base de données.
 */
class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	// Une ressource introuvable est traduite en 404.
	@Test
	void notFoundReturns404() throws Exception {
		mockMvc.perform(get("/throw/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Ressource introuvable"))
				.andExpect(jsonPath("$.path").value("/throw/not-found"));
	}

	// Un doublon est traduit en 409.
	@Test
	void duplicateReturns409() throws Exception {
		mockMvc.perform(get("/throw/duplicate"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	// Un solde insuffisant est traduit en 409.
	@Test
	void insufficientBalanceReturns409() throws Exception {
		mockMvc.perform(get("/throw/insufficient"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	// Un transfert invalide est traduit en 400.
	@Test
	void invalidTransferReturns400() throws Exception {
		mockMvc.perform(get("/throw/invalid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	// De mauvais identifiants sont traduits en 401.
	@Test
	void badCredentialsReturns401() throws Exception {
		mockMvc.perform(get("/throw/bad-credentials"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	// Un corps invalide (@Valid) renvoie 400 avec le détail par champ.
	@Test
	void validationReturns400WithFieldErrors() throws Exception {
		mockMvc.perform(post("/throw/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.name").exists());
	}

	/** Contrôleur jetable qui déclenche chaque type d'erreur. */
	@RestController
	static class TestController {

		@GetMapping("/throw/not-found")
		void notFound() {
			throw new ResourceNotFoundException("Ressource introuvable");
		}

		@GetMapping("/throw/duplicate")
		void duplicate() {
			throw new DuplicateResourceException("Doublon");
		}

		@GetMapping("/throw/insufficient")
		void insufficient() {
			throw new InsufficientBalanceException("Solde insuffisant");
		}

		@GetMapping("/throw/invalid")
		void invalid() {
			throw new InvalidTransferException("Transfert invalide");
		}

		@GetMapping("/throw/bad-credentials")
		void badCredentials() {
			throw new BadCredentialsException("Mauvais identifiants");
		}

		@PostMapping("/throw/validate")
		void validate(@Valid @RequestBody SampleRequest body) {
			// Le corps invalide déclenche MethodArgumentNotValidException avant d'arriver ici.
		}
	}

	/** DTO jetable pour tester la validation. */
	record SampleRequest(@NotBlank String name) {
	}
}
