package com.tgbsolutions.minitransfer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI (Swagger UI).
 *
 * <p>Déclare le schéma de sécurité « bearerAuth » (token JWT) afin que Swagger UI affiche un
 * bouton « Authorize » : on y colle le token obtenu via /api/auth/login pour tester les
 * endpoints protégés directement depuis le navigateur.</p>
 */
@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME = "bearerAuth";

	@Bean
	OpenAPI miniTransferOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("MiniTransfer API")
						.description("API REST de la mini-plateforme de transfert d'argent MiniTransfer.")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
				.components(new Components().addSecuritySchemes(SECURITY_SCHEME,
						new SecurityScheme()
								.name(SECURITY_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
