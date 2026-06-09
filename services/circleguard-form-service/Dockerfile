FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY services/circleguard-auth-service/build.gradle.kts services/circleguard-auth-service/build.gradle.kts
COPY services/circleguard-identity-service/build.gradle.kts services/circleguard-identity-service/build.gradle.kts
COPY services/circleguard-promotion-service/build.gradle.kts services/circleguard-promotion-service/build.gradle.kts
COPY services/circleguard-notification-service/build.gradle.kts services/circleguard-notification-service/build.gradle.kts
COPY services/circleguard-form-service/build.gradle.kts services/circleguard-form-service/build.gradle.kts
COPY services/circleguard-file-service/build.gradle.kts services/circleguard-file-service/build.gradle.kts
COPY services/circleguard-gateway-service/build.gradle.kts services/circleguard-gateway-service/build.gradle.kts
COPY services/circleguard-dashboard-service/build.gradle.kts services/circleguard-dashboard-service/build.gradle.kts

RUN chmod +x ./gradlew && ./gradlew :services:circleguard-form-service:dependencies --no-daemon

COPY services/circleguard-form-service/src services/circleguard-form-service/src
RUN ./gradlew :services:circleguard-form-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre AS runtime

LABEL org.opencontainers.image.title="CircleGuard Form Service" \
      org.opencontainers.image.description="Health survey and questionnaire microservice for CircleGuard" \
      org.opencontainers.image.version="1.0.0-SNAPSHOT" \
      org.opencontainers.image.source="https://github.com/circleguard/circleguard" \
      org.opencontainers.image.vendor="CircleGuard" \
      org.opencontainers.image.licenses="Academic"

WORKDIR /app
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --shell /usr/sbin/nologin --create-home app
COPY --from=builder /workspace/services/circleguard-form-service/build/libs/*.jar app.jar
RUN chown -R 10001:10001 /app

USER 10001
EXPOSE 8086
ENTRYPOINT ["java","-jar","app.jar"]
