package com.circleguard.auth.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class AuthPostgreSqlIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("circleguard_auth")
            .withUsername("circleguard")
            .withPassword("circleguard");

    @Test
    void shouldOpenPostgreSqlConnectionForAuthPersistence() throws Exception {
        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             var statement = connection.createStatement();
             var result = statement.executeQuery("select 1")) {

            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }
    }
}
