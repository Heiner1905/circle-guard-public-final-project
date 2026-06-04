# Circuit Breaker Pattern

US-06 applies Resilience4j to HTTP calls between CircleGuard services.

## Why Resilience4j

Resilience4j is lightweight, works natively with Spring Boot 3, exposes Micrometer metrics, and provides actuator integration without requiring a service mesh. That fits CircleGuard's current architecture: small Spring Boot services with direct HTTP calls and Kubernetes readiness/liveness probes.

## State Diagram

```text
Closed
  | failures >= threshold in sliding window
  v
Open
  | waitDurationInOpenState elapsed
  v
Half-Open
  | permitted test calls succeed
  v
Closed

Half-Open
  | test calls fail
  v
Open
```

## Applied Services

| Caller | Downstream | Circuit name | Fallback |
| --- | --- | --- | --- |
| auth-service | identity-service | `identityService` | Throws `IdentityServiceUnavailableException` because login cannot safely mint a token without an anonymous identity |
| dashboard-service | promotion-service | `promotionService` | Returns an analytics map with `error=Service unavailable` and timestamp/department context |
| notification-service | auth-service | `authService` | Returns an empty admin list, so alert processing degrades without retry storms |

## Configuration

Each circuit uses:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      <name>:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        registerHealthIndicator: true
```

Actuator exposure is enabled for:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,circuitbreakers
  health:
    circuitbreakers:
      enabled: true
```

## Validation

Run focused tests:

```bash
GRADLE_USER_HOME=/tmp/gradle-cache ./gradlew \
  :services:circleguard-auth-service:test \
  :services:circleguard-dashboard-service:test \
  :services:circleguard-notification-service:test \
  --tests '*CircuitBreakerTest'
```

At runtime, inspect:

```bash
curl http://localhost:<port>/actuator/circuitbreakers
curl http://localhost:<port>/actuator/health
curl http://localhost:<port>/actuator/prometheus
```
