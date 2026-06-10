# US-13 CI Coordination Notes

Comentarios para Heiner antes de ajustar pipelines en `.github/workflows`.

```yaml
# COORDINAR - Sprint 2 US-13
# Agregar los siguientes steps al pipeline:
#
# - name: Run tests with coverage
#   run: ./gradlew test jacocoTestReport
#
# - name: Verify coverage threshold
#   run: ./gradlew jacocoTestCoverageVerification
#
# - name: SonarQube Scan
#   run: ./gradlew sonar -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}
#   env:
#     SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```
