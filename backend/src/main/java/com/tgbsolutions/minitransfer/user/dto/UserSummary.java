package com.tgbsolutions.minitransfer.user.dto;

import com.tgbsolutions.minitransfer.user.User;

/**
 * Vue publique d'un utilisateur (jamais le mot de passe), renvoyée après inscription/connexion.
 */
public record UserSummary(String id, String name, String email, String phone, long balance) {

	public static UserSummary from(User user) {
		return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getBalance());
	}
}
