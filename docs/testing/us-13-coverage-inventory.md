# US-13 Coverage Inventory

Este inventario se basa en inspeccion estatica. La cobertura real debe confirmarse con JaCoCo cuando se autorice ejecutar Gradle.

## Servicios con cobertura reforzada

| Servicio | Controller | Service | Repository / Integration |
|----------|------------|---------|--------------------------|
| auth-service | `LoginControllerTest` | `CustomUserDetailsServiceTest`, `JwtTokenServiceTest`, `QrTokenServiceTest` | `AuthPostgreSqlIntegrationTest` |
| dashboard-service | `AnalyticsControllerTest` | `AnalyticsServiceTest` | Circuit breaker client test |
| file-service | `FileUploadControllerTest` | `FileStorageServiceTest` | N/A |
| form-service | `HealthSurveyControllerTest`, `QuestionnaireControllerTest`, `AttachmentControllerTest` | `HealthSurveyServiceTest`, `QuestionnaireServiceTest`, `SymptomMapperTest` | Repository behavior covered through service mocks |
| gateway-service | `GateControllerTest` | `QrValidationServiceTest` | `GatewayRedisIntegrationTest` |
| identity-service | `IdentityVaultControllerTest` | `IdentityVaultServiceTest` | `IdentityMappingRepositoryTest` |
| notification-service | `NotificationControllerTest` | Dispatcher/listener/template/channel tests | `NotificationKafkaIntegrationTest` |
| promotion-service | `HealthStatusControllerTest` | Health lifecycle, reevaluation, floor, admin correction tests | `PromotionInfrastructureIntegrationTest` |

## Riesgos de cobertura a validar

- `promotion-service` tiene mas superficie de controllers que el resto; si JaCoCo queda bajo 80%, los siguientes candidatos son `BuildingController`, `FloorController`, `AccessPointController` y `CircleController`.
- `notification-service` ahora tiene borde HTTP minimo, pero la cobertura principal sigue en listeners y dispatcher porque el servicio es orientado a eventos Kafka.
- `file-service` es pequeno; `FileStorageServiceTest` cubre almacenamiento local basico, pero no hay backend S3-compatible implementado en codigo fuente.
- `form-service` cubre servicios centrales, aunque repositorios JPA adicionales se validan indirectamente hasta que se agreguen pruebas `@DataJpaTest`.

## Validacion pendiente

No ejecutado por restriccion de Sprint 2:

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```
