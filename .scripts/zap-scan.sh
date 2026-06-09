#!/bin/bash
set -euo pipefail

TARGET=${1:?Usage: ./.scripts/zap-scan.sh <target-url>}
REPORT_DIR="reports/zap"

mkdir -p "${REPORT_DIR}"

docker run --rm -v "$(pwd):/zap/wrk" \
  owasp/zap2docker-stable zap-baseline.py \
  -t "${TARGET}" \
  -r "${REPORT_DIR}/zap-report.html" \
  -J "${REPORT_DIR}/zap-report.json" \
  -w "${REPORT_DIR}/zap-alerts.md"
