package com.circleguard.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@Disabled("Enable after wiring the E2E test source set and starting dependent CircleGuard services.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PromotionE2ETest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.12"));

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @LocalServerPort
    int port;

    @Test
    @DisplayName("Full promotion flow: confirmed user propagates risk to direct and indirect contacts")
    void testFullPromotionFlow() {
        String userA = "e2e-user-a";
        String userB = "e2e-user-b";
        String userC = "e2e-user-c";

        given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .body(Map.of("sourceUserId", userA, "targetUserId", userB, "durationMinutes", 15))
                .when()
                .post("/api/v1/encounters")
                .then()
                .statusCode(anyOf(is(200), is(201), is(202)));

        given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .body(Map.of("sourceUserId", userB, "targetUserId", userC, "durationMinutes", 10))
                .when()
                .post("/api/v1/encounters")
                .then()
                .statusCode(anyOf(is(200), is(201), is(202)));

        given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .body(Map.of("anonymousId", userA, "status", "CONFIRMED", "adminOverride", true))
                .when()
                .post("/api/v1/health/report")
                .then()
                .statusCode(200);

        given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/api/v1/health/status/{anonymousId}", userB)
                .then()
                .statusCode(200)
                .body("status", is("SUSPECT"));

        given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/api/v1/health/status/{anonymousId}", userC)
                .then()
                .statusCode(200)
                .body("status", is("PROBABLE"));
    }
}
