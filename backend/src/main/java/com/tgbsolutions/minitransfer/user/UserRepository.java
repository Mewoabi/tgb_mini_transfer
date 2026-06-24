package com.tgbsolutions.minitransfer.user;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Dépôt MongoDB des utilisateurs.
 */
public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);
}
