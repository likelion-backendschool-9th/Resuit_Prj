#!/usr/bin/env bash
set -euo pipefail
if [[ "${1:-}" == '--help' ]]; then
  echo '사용법: bash scripts/dev-backend.sh [Gradle 옵션]'
  echo '루트 .env를 로드하고 기본 8081 포트에서 실행합니다. 스키마는 validate만 수행합니다.'
  exit 0
fi
source "$(dirname -- "${BASH_SOURCE[0]}")/local-env.sh"
: "${POSTGRES_HOST:?POSTGRES_HOST가 필요합니다}"
: "${POSTGRES_PORT:?POSTGRES_PORT가 필요합니다}"
: "${POSTGRES_DB:?POSTGRES_DB가 필요합니다}"
: "${POSTGRES_USER:?POSTGRES_USER가 필요합니다}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD가 필요합니다}"
export SPRING_JPA_HIBERNATE_DDL_AUTO=validate
cd "$RESUIT_PROJECT_ROOT/backend/resuit"
exec bash ./gradlew bootRun "--args=--server.port=${RESUIT_BACKEND_PORT:-8081}" "$@"
