#!/usr/bin/env bash
set -euo pipefail
if [[ "${1:-}" == '--help' ]]; then
  echo '사용법: bash scripts/dev-frontend.sh'
  echo '루트 .env를 로드하고 기본 5174 포트에서 실행합니다. 백엔드 기본값은 localhost:8081입니다.'
  exit 0
fi
source "$(dirname -- "${BASH_SOURCE[0]}")/local-env.sh"
export BACKEND_URL="${BACKEND_URL:-http://localhost:${RESUIT_BACKEND_PORT:-8081}}"
cd "$RESUIT_PROJECT_ROOT/frontend"
exec npm run dev -- -p "${RESUIT_FRONTEND_PORT:-5174}"
