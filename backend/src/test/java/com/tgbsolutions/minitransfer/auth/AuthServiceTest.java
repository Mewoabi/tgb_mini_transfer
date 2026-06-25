package com.tgbsolutions.minitransfer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tgbsolutions.minitransfer.auth.dto.AuthResponse;
import com.tgbsolutions.minitransfer.auth.dto.LoginRequest;
import com.tgbsolutions.minitransfer.auth.dto.RegisterRequest;
import com.tgbsolutions.minitransfer.common.DuplicateResourceException;
import com.tgbsolutions.minitransfer.security.JwtService;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests unitaires du service d'authentification (étape B4), dépendances simulées (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	JwtService jwtService;

	@InjectMocks
	AuthService authService;

	// L'inscription crée un utilisateur avec le solde initial, chiffre le mot de passe et renvoie un token.
	@Test
	void registerCreatesUserWithInitialBalanceAndReturnsToken() {
		when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
		when(userRepository.existsByPhone("+237600000001")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User saved = invocation.getArgument(0);
			saved.setId("user-1");
			return saved;
		});
		when(jwtService.generateToken("user-1", "alice@mail.com", "Alice")).thenReturn("token-xyz");

		AuthResponse response =
				authService.register(new RegisterRequest("Alice", "alice@mail.com", "+237600000001", "password123"));

		assertThat(response.token()).isEqualTo("token-xyz");
		assertThat(response.user().balance()).isEqualTo(AuthService.INITIAL_BALANCE);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getBalance()).isEqualTo(10_000L);
		assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
	}

	// L'inscription est rejetée si l'email est déjà utilisé.
	@Test
	void registerRejectsDuplicateEmail() {
		when(userRepository.existsByEmail("dup@mail.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(
				new RegisterRequest("Alice", "dup@mail.com", "+237600000001", "password123")))
				.isInstanceOf(DuplicateResourceException.class);

		verify(userRepository, never()).save(any());
	}

	// L'inscription est rejetée si le téléphone est déjà utilisé.
	@Test
	void registerRejectsDuplicatePhone() {
		when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
		when(userRepository.existsByPhone("+237600000001")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(
				new RegisterRequest("Alice", "alice@mail.com", "+237600000001", "password123")))
				.isInstanceOf(DuplicateResourceException.class);

		verify(userRepository, never()).save(any());
	}

	// La connexion renvoie un token quand les identifiants sont valides.
	@Test
	void loginReturnsTokenWhenCredentialsValid() {
		User user = new User("Alice", "alice@mail.com", "+237600000001", "hashed", 10_000L, Instant.now());
		user.setId("user-1");
		when(userRepository.findByEmail("alice@mail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
		when(jwtService.generateToken("user-1", "alice@mail.com", "Alice")).thenReturn("token-xyz");

		AuthResponse response = authService.login(new LoginRequest("alice@mail.com", "password123"));

		assertThat(response.token()).isEqualTo("token-xyz");
	}

	// La connexion est rejetée si le mot de passe est erroné.
	@Test
	void loginRejectsWrongPassword() {
		User user = new User("Alice", "alice@mail.com", "+237600000001", "hashed", 10_000L, Instant.now());
		user.setId("user-1");
		when(userRepository.findByEmail("alice@mail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("alice@mail.com", "wrong")))
				.isInstanceOf(BadCredentialsException.class);
	}

	// La connexion est rejetée si l'email est inconnu.
	@Test
	void loginRejectsUnknownEmail() {
		when(userRepository.findByEmail("inconnu@mail.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("inconnu@mail.com", "password123")))
				.isInstanceOf(BadCredentialsException.class);
	}
}
