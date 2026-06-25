package com.tgbsolutions.minitransfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tgbsolutions.minitransfer.TestcontainersConfiguration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Tests du dépôt des utilisateurs (étape B2), sur un MongoDB Testcontainers.
 */
@DataMongoTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	MongoTemplate mongoTemplate;

	@BeforeEach
	void cleanUp() {
		userRepository.deleteAll();
	}

	private User newUser(String name, String email, String phone) {
		return new User(name, email, phone, "hash", 10_000L, Instant.now());
	}

	// Sauvegarde un utilisateur puis le retrouve par son email.
	@Test
	void savesAndFindsUserByEmail() {
		userRepository.save(newUser("Alice", "alice@mail.com", "+237600000001"));
		assertThat(userRepository.findByEmail("alice@mail.com"))
				.isPresent()
				.get()
				.satisfies(u -> assertThat(u.getName()).isEqualTo("Alice"));
	}

	// Sauvegarde un utilisateur puis le retrouve par son numéro de téléphone.
	@Test
	void savesAndFindsUserByPhone() {
		userRepository.save(newUser("Bob", "bob@mail.com", "+237600000002"));
		assertThat(userRepository.findByPhone("+237600000002"))
				.isPresent()
				.get()
				.satisfies(u -> assertThat(u.getEmail()).isEqualTo("bob@mail.com"));
	}

	// existsByEmail / existsByPhone reflètent les utilisateurs persistés.
	@Test
	void existsByEmailAndPhoneReflectPersistedUsers() {
		userRepository.save(newUser("Alice", "alice@mail.com", "+237600000001"));
		assertThat(userRepository.existsByEmail("alice@mail.com")).isTrue();
		assertThat(userRepository.existsByPhone("+237600000001")).isTrue();
		assertThat(userRepository.existsByEmail("inconnu@mail.com")).isFalse();
		assertThat(userRepository.existsByPhone("+000000000000")).isFalse();
	}

	// L'index unique sur l'email rejette un doublon.
	@Test
	void rejectsDuplicateEmail() {
		userRepository.save(newUser("Alice", "dup@mail.com", "+237600000001"));
		assertThatThrownBy(() -> userRepository.save(newUser("Alice2", "dup@mail.com", "+237600000099")))
				.isInstanceOf(DuplicateKeyException.class);
	}

	// L'index unique sur le téléphone rejette un doublon.
	@Test
	void rejectsDuplicatePhone() {
		userRepository.save(newUser("Alice", "a1@mail.com", "+237600000001"));
		assertThatThrownBy(() -> userRepository.save(newUser("Alice2", "a2@mail.com", "+237600000001")))
				.isInstanceOf(DuplicateKeyException.class);
	}

	// Les index uniques sur l'email et le téléphone sont bien créés en base.
	@Test
	void createsUniqueIndexesOnEmailAndPhone() {
		userRepository.save(newUser("Alice", "idx@mail.com", "+237600000001"));
		var indexes = mongoTemplate.indexOps(User.class).getIndexInfo();
		assertThat(indexes).anySatisfy(i -> {
			assertThat(i.isUnique()).isTrue();
			assertThat(i.getIndexFields()).anySatisfy(f -> assertThat(f.getKey()).isEqualTo("email"));
		});
		assertThat(indexes).anySatisfy(i -> {
			assertThat(i.isUnique()).isTrue();
			assertThat(i.getIndexFields()).anySatisfy(f -> assertThat(f.getKey()).isEqualTo("phone"));
		});
	}
}
