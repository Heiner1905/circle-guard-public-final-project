# Change Management Process

## Roles

- Release Manager: Karold
- QA Lead: Por definir
- Dev Lead: Heiner

## Approval Flow

1. Developer abre PR hacia `dev`.
2. QA revisa pruebas unitarias, integracion, E2E, rendimiento y seguridad segun aplique.
3. Release Manager aprueba despliegue a staging desde `main`.
4. Dev Lead aprueba despliegue a produccion desde `master`.

## Checklist de cambio

- Historia de usuario vinculada.
- Commits con Conventional Commits.
- Evidencia de validacion local o razon documentada si no se ejecuto.
- Plan de rollback identificado por servicio.
- Riesgos de privacidad FERPA revisados.
- Coordinacion con Heiner para infraestructura, secretos, TLS, RBAC o observabilidad.

## Ventanas y comunicacion

- Cambios normales: ventana acordada con equipo de Sprint.
- Cambios urgentes: aprobacion verbal o escrita del Release Manager y Dev Lead.
- Rollback: se ejecuta cuando hay degradacion critica, errores de seguridad o impacto funcional mayor.

## Evidencia esperada

- Link al PR.
- Version o tag desplegado.
- Resultado de health checks.
- Resultado de pruebas relevantes.
- Notas de release generadas.
