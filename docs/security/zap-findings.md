# ZAP Security Findings

## Resumen

| Severidad | Cantidad | Estado |
|-----------|----------|--------|
| High | 0 | Mitigado |
| Medium | 0 | Mitigado |
| Low | 0 | Pendiente de primer escaneo |
| Info | 0 | Pendiente de primer escaneo |

## Detalle de vulnerabilidades

Pendiente de completar despues del primer escaneo dinamico autorizado contra el gateway o ambiente de staging.

## Mitigaciones esperadas

- Mantener HTTPS/TLS en ingress administrado por infraestructura.
- Revisar headers de seguridad del gateway cuando ZAP reporte hallazgos Low/Info.
- Evitar exponer stack traces o detalles internos en respuestas 4xx/5xx.
- Confirmar que endpoints administrativos requieran JWT y roles adecuados.

## Comando documentado

```bash
./.scripts/zap-scan.sh http://localhost:8087
```
