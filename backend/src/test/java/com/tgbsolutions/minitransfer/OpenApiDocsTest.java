package com.tgbsolutions.minitransfer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Vérifie que la documentation OpenAPI est publiquement accessible et décrit bien les endpoints (étape B8).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsTest {

	@Autowired
	MockMvc mockMvc;

	// Le document OpenAPI est accessible sans authentification et liste les endpoints clés.
	@Test
	void apiDocsArePubliclyAccessibleAndListEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/auth/register']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/login']").exists())
				.andExpect(jsonPath("$.paths['/api/wallet/balance']").exists())
				.andExpect(jsonPath("$.paths['/api/transfers']").exists())
				.andExpect(jsonPath("$.paths['/api/transfers/history']").exists());
	}
}
