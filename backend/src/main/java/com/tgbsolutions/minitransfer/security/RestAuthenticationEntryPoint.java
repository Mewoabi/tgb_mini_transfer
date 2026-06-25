package com.tgbsolutions.minitransfer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbsolutions.minitransfer.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Point d'entrée d'authentification : renvoie un 401 au format {@link ApiError} (JSON)
 * lorsqu'une requête non authentifiée vise une ressource protégée, plutôt que la page
 * d'erreur par défaut de Spring Security.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		ApiError body = ApiError.of(HttpStatus.UNAUTHORIZED,
				"Authentification requise : token manquant ou invalide.", request.getRequestURI());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}
}
