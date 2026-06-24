package com.tgbsolutions.minitransfer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		// Image alignée sur celle du docker-compose, pour la cohérence des tests.
		// MongoDBContainer démarre automatiquement un replica set mono-nœud,
		// ce qui permet de tester les transactions multi-documents.
		return new MongoDBContainer(DockerImageName.parse("mongo:7"));
	}

}
