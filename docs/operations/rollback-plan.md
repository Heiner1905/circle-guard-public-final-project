# Rollback Plan by Service

## Objetivo

Restaurar rapidamente una revision estable de un microservicio CircleGuard sin revertir todo el sistema.

## Precondiciones

- Acceso kubectl al namespace `circleguard`.
- Revision objetivo identificada con `kubectl rollout history`.
- Comunicacion previa con Heiner para cambios de infraestructura, ingress, TLS o secretos.

## Comandos base

```bash
kubectl rollout history deployment/circleguard-auth-service -n circleguard
kubectl rollout undo deployment/circleguard-auth-service -n circleguard --to-revision=X
```

```bash
kubectl rollout history deployment/circleguard-promotion-service -n circleguard
kubectl rollout undo deployment/circleguard-promotion-service -n circleguard --to-revision=X
```

```bash
kubectl rollout history deployment/circleguard-notification-service -n circleguard
kubectl rollout undo deployment/circleguard-notification-service -n circleguard --to-revision=X
```

## Script

```bash
./scripts/rollback.sh auth-service 1
./scripts/rollback.sh promotion-service 1
./scripts/rollback.sh notification-service 1
```

## Validacion posterior

Comandos documentados, no ejecutados en esta rama:

```bash
kubectl rollout status deployment/circleguard-auth-service -n circleguard
kubectl get pods -n circleguard
kubectl logs deployment/circleguard-auth-service -n circleguard --tail=100
```

## Servicios

| Servicio | Deployment |
|----------|------------|
| auth-service | `circleguard-auth-service` |
| identity-service | `circleguard-identity-service` |
| promotion-service | `circleguard-promotion-service` |
| notification-service | `circleguard-notification-service` |
| form-service | `circleguard-form-service` |
| file-service | `circleguard-file-service` |
| gateway-service | `circleguard-gateway-service` |
| dashboard-service | `circleguard-dashboard-service` |
