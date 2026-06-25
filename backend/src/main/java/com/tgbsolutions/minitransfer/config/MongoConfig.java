package com.tgbsolutions.minitransfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Configuration MongoDB.
 *
 * <p>Déclare le gestionnaire de transactions multi-documents. La simple présence de ce
 * bean active la prise en charge de {@code @Transactional} sur les opérations MongoDB,
 * ce qui garantit la cohérence des transferts (débit et crédit atomiques : on ne perd
 * ni ne crée jamais d'argent).</p>
 *
 * <p>Prérequis : MongoDB doit fonctionner en <i>replica set</i> — configuré côté
 * docker-compose, et fourni automatiquement par Testcontainers lors des tests.</p>
 */
@Configuration
public class MongoConfig {

	/**
	 * Gestionnaire de transactions MongoDB, adossé à la fabrique de base de données.
	 */
	@Bean
	MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory databaseFactory) {
		return new MongoTransactionManager(databaseFactory);
	}
}
