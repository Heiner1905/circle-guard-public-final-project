#!/bin/bash
set -euo pipefail

echo "=== Rollback Demo ==="

helm upgrade --install circleguard ./helm/circleguard -n circleguard \
  --set global.imageTag=v1.0.0

helm upgrade --install circleguard ./helm/circleguard -n circleguard \
  --set global.imageTag=v1.1.0

kubectl rollout undo deployment/circleguard-gateway-service -n circleguard

echo "Rollback successful"
