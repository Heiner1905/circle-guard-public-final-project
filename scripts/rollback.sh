#!/bin/bash
set -euo pipefail

SERVICE=${1:?Usage: ./scripts/rollback.sh <service|service-name> <revision>}
REVISION=${2:?Usage: ./scripts/rollback.sh <service|service-name> <revision>}
NAMESPACE=${NAMESPACE:-circleguard}

if [[ "${SERVICE}" == circleguard-* ]]; then
  DEPLOYMENT="${SERVICE}"
elif [[ "${SERVICE}" == *-service ]]; then
  DEPLOYMENT="circleguard-${SERVICE}"
else
  DEPLOYMENT="circleguard-${SERVICE}-service"
fi

kubectl rollout undo "deployment/${DEPLOYMENT}" \
  -n "${NAMESPACE}" \
  --to-revision="${REVISION}"
