package com.tgbsolutions.minitransfer.security;

/**
 * Utilisateur authentifié, reconstruit à partir des informations du token JWT.
 *
 * <p>Sert de « principal » dans le contexte de sécurité : les contrôleurs y accèdent via
 * {@code @AuthenticationPrincipal CurrentUser} pour connaître l'utilisateur courant sans
 * relire la base à chaque requête (authentification sans état).</p>
 */
public record CurrentUser(String id, String email, String name) {
}
