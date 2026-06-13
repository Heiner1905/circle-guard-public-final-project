# CircleGuard Kubernetes Deployment

US-05 adds an umbrella Helm chart at `helm/circleguard` for the eight Spring Boot microservices.

## Install

Render or install the development environment with:

```bash
helm install circleguard ./helm/circleguard -n circleguard --create-namespace -f helm/circleguard/values-dev.yaml
```

For stage and prod:

```bash
helm install circleguard ./helm/circleguard -n circleguard --create-namespace -f helm/circleguard/values-stage.yaml
helm install circleguard ./helm/circleguard -n circleguard --create-namespace -f helm/circleguard/values-prod.yaml
```

## Images

By default each service pulls from:

```text
<acr-login-server>/<service-name>:latest
```

Override `global.imageRegistry`, per-service `image.repository`, or per-service `image.tag` in the environment values file once ACR is available.

## Runtime Topology

The chart creates:

- Namespace `circleguard`.
- One ServiceAccount per microservice.
- One Deployment per microservice.
- One ClusterIP Service per microservice.
- One ConfigMap and one Secret per microservice.
- Ingress only for `circleguard-gateway-service` and `circleguard-dashboard-service`.

Default replicas are `1` in dev, `2` in stage, and `3` in prod. Default resources are `100m/256Mi` requests and `500m/512Mi` limits.

## Middleware DNS

Service configuration points to middleware in namespace `circleguard-middleware`:

- PostgreSQL: `postgresql.circleguard-middleware.svc.cluster.local`
- Neo4j: `neo4j.circleguard-middleware.svc.cluster.local`
- Kafka: `kafka.circleguard-middleware.svc.cluster.local:9092`
- Redis: `redis.circleguard-middleware.svc.cluster.local`
- LDAP: `openldap.circleguard-middleware.svc.cluster.local`

## Validate Without A Cluster

```bash
helm lint ./helm/circleguard
helm template circleguard ./helm/circleguard -n circleguard -f helm/circleguard/values-dev.yaml
```
