# US-14 E2E, Performance and Security Test Plan

## Alcance

- Flujo E2E principal de promocion de estado de salud.
- Prueba de carga HTTP con Locust contra el gateway.
- Escaneo dinamico baseline con OWASP ZAP.
- Reportes versionables en `reports/` cuando se autorice ejecutar las herramientas.

## E2E

Archivo base: `tests/e2e/rest-assured/PromotionE2ETest.java`

El test documenta el flujo:

1. Usuario A registra contacto con Usuario B.
2. Usuario B registra contacto con Usuario C.
3. Usuario A pasa a `CONFIRMED`.
4. Usuario B debe quedar `SUSPECT`.
5. Usuario C debe quedar `PROBABLE`.

El test queda deshabilitado hasta conectar el source set E2E y confirmar endpoints finales con los servicios levantados.

## Rendimiento

Archivo base: `locustfile.py`

Metricas a capturar:

| Metrica | Objetivo |
|---------|----------|
| RPS | Throughput estable durante la ventana de prueba |
| P50 | Latencia mediana por endpoint |
| P95 | Latencia percentil 95 por endpoint |
| P99 | Latencia percentil 99 por endpoint |
| Failures | Errores HTTP y timeouts |

Comando documentado, no ejecutado:

```bash
locust -f locustfile.py --host=http://localhost:8087 --headless -u 50 -t 2m
```

## Seguridad

Archivo base: `.scripts/zap-scan.sh`

Comando documentado, no ejecutado:

```bash
./.scripts/zap-scan.sh http://localhost:8087
```

Los hallazgos se consolidan en `docs/security/zap-findings.md`.
