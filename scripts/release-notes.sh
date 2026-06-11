#!/bin/bash
set -euo pipefail

VERSION=""

if [[ "${1:-}" == "--next-version" ]]; then
  VERSION="${2:?Usage: ./scripts/release-notes.sh --next-version <version>}"
else
  VERSION="${1:?Usage: ./scripts/release-notes.sh <version>}"
fi

PREVIOUS_TAG="$(git describe --abbrev=0 --tags 2>/dev/null || true)"

{
  echo "# Release Notes v${VERSION}"
  echo
  echo "## Changes"
  if [[ -n "${PREVIOUS_TAG}" ]]; then
    git log --oneline --pretty=format:"- %s" "${PREVIOUS_TAG}..HEAD"
  else
    git log --oneline --pretty=format:"- %s"
  fi
  echo
} > CHANGELOG.md
