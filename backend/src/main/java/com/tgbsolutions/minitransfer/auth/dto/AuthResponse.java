package com.tgbsolutions.minitransfer.auth.dto;

import com.tgbsolutions.minitransfer.user.dto.UserSummary;

/**
 * Réponse d'authentification : token JWT et informations de l'utilisateur connecté.
 */
public record AuthResponse(String token, String tokenType, UserSummary user) {
}
