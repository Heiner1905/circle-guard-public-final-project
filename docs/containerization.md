# CircleGuard microservice containerization

US-04 adds one Dockerfile per Spring Boot microservice under `services/circleguard-*-service/`.

## Base Image Choice

All images use official Eclipse Temurin 21 images:

- `eclipse-temurin:21-jdk` for the Gradle builder stage.
- `eclipse-temurin:21-jre` for the runtime stage.

Temurin is the Eclipse Adoptium production build of OpenJDK and matches the project Java 21 toolchain. The runtime stage keeps only the executable Spring Boot jar and runs it as UID `10001`, avoiding root in the final image.

## Build Pattern

Each Dockerfile is intended to be built from the repository root:

```bash
docker build -f services/circleguard-auth-service/Dockerfile -t circleguard-auth-service:local .
docker build -f services/circleguard-identity-service/Dockerfile -t circleguard-identity-service:local .
docker build -f services/circleguard-promotion-service/Dockerfile -t circleguard-promotion-service:local .
docker build -f services/circleguard-notification-service/Dockerfile -t circleguard-notification-service:local .
docker build -f services/circleguard-form-service/Dockerfile -t circleguard-form-service:local .
docker build -f services/circleguard-file-service/Dockerfile -t circleguard-file-service:local .
docker build -f services/circleguard-gateway-service/Dockerfile -t circleguard-gateway-service:local .
docker build -f services/circleguard-dashboard-service/Dockerfile -t circleguard-dashboard-service:local .
```

The Dockerfiles copy Gradle wrapper files, root Gradle metadata, and service `build.gradle.kts` files before service source code. That allows Docker to reuse the Gradle dependency layer when only application code changes.

## Published Ports

| Service | Port |
| --- | ---: |
| circleguard-auth-service | 8180 |
| circleguard-identity-service | 8083 |
| circleguard-promotion-service | 8088 |
| circleguard-notification-service | 8082 |
| circleguard-form-service | 8086 |
| circleguard-file-service | 8085 |
| circleguard-gateway-service | 8087 |
| circleguard-dashboard-service | 8084 |

## Estimated Image Sizes

Expected runtime image size is approximately `260-340 MB` per service with `eclipse-temurin:21-jre`, depending on jar dependencies:

| Service | Estimated runtime image |
| --- | ---: |
| auth-service | 300 MB |
| identity-service | 300 MB |
| promotion-service | 340 MB |
| notification-service | 330 MB |
| form-service | 310 MB |
| file-service | 270 MB |
| gateway-service | 290 MB |
| dashboard-service | 300 MB |

Exact sizes should be captured by CI after `docker images`.

## Runtime

Each final image:

- Runs with non-root UID/GID `10001`.
- Exposes the service port declared in `application.yml`.
- Starts with exec-form `ENTRYPOINT ["java","-jar","app.jar"]`.
- Includes OCI labels for title, description, version, source, vendor, and license.

Health checks are left to Kubernetes readiness/liveness probes instead of image-level `HEALTHCHECK`, because the Temurin JRE runtime image does not guarantee `curl` or `wget` availability.
