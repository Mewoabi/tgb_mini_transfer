package com.tgbsolutions.minitransfer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Corps de réponse normalisé pour toutes les erreurs de l'API.
 *
 * <p>{@code fieldErrors} n'est présent que pour les erreurs de validation (champ → message) ;
 * il est omis du JSON sinon.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors) {

	/** Fabrique une erreur simple (sans détail de validation). */
	public static ApiError of(HttpStatus status, String message, String path) {
		return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, null);
	}
}
