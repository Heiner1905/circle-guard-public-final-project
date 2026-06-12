package com.circleguard.promotion.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class PromotionInfrastructureIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("circleguard_promotion")
            .withUsername("circleguard")
            .withPassword("circleguard");

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.12")
            .withAdminPassword("password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Test
    void shouldProvisionPromotionDependencies() {
        assertTrue(postgres.isRunning());
        assertTrue(neo4j.isRunning());
        assertTrue(redis.isRunning());
        assertFalse(kafka.getBootstrapServers().isBlank());
        assertTrue(redis.getMappedPort(6379) > 0);
    }
}
