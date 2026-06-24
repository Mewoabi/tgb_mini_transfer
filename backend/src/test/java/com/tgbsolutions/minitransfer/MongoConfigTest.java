package com.tgbsolutions.minitransfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Vérifie la connexion à MongoDB et le bon câblage des transactions (étape B1).
 *
 * <p>Le conteneur MongoDB (Testcontainers) démarre en replica set, ce qui permet de
 * valider dès maintenant le mécanisme transactionnel sur lequel reposeront les transferts.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MongoConfigTest {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	MongoTransactionManager transactionManager;

	// Vérifie que la connexion à MongoDB est établie (la base répond au ping).
	@Test
	void pingSucceeds() {
		Document result = mongoTemplate.getDb().runCommand(new Document("ping", 1));
		assertThat(((Number) result.get("ok")).intValue()).isEqualTo(1);
	}

	// Vérifie qu'une transaction validée (commit) persiste bien tous ses documents.
	@Test
	void committedTransactionPersistsDocuments() {
		TransactionTemplate tx = new TransactionTemplate(transactionManager);
		String collection = "b1_commit";
		mongoTemplate.dropCollection(collection);
		mongoTemplate.createCollection(collection);

		// Deux insertions dans une même transaction, puis validation.
		tx.executeWithoutResult(status -> {
			mongoTemplate.insert(new Document("k", 1), collection);
			mongoTemplate.insert(new Document("k", 2), collection);
		});

		assertThat(mongoTemplate.getCollection(collection).countDocuments()).isEqualTo(2L);
		mongoTemplate.dropCollection(collection);
	}

	// Vérifie qu'une transaction annulée (rollback) ne persiste aucun document.
	@Test
	void rolledBackTransactionPersistsNothing() {
		TransactionTemplate tx = new TransactionTemplate(transactionManager);
		String collection = "b1_rollback";
		mongoTemplate.dropCollection(collection);
		mongoTemplate.createCollection(collection);

		// Une exception au milieu de la transaction doit tout annuler (rollback).
		try {
			tx.executeWithoutResult(status -> {
				mongoTemplate.insert(new Document("k", 1), collection);
				throw new RuntimeException("échec simulé pour déclencher le rollback");
			});
		} catch (RuntimeException ignored) {
			// Exception attendue.
		}

		assertThat(mongoTemplate.getCollection(collection).countDocuments()).isZero();
		mongoTemplate.dropCollection(collection);
	}
}
