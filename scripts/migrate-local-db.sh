#!/usr/bin/env bash
set -euo pipefail
if [[ "${1:-}" == '--help' ]]; then
  echo '사용법: bash scripts/migrate-local-db.sh'
  echo '개인 Compose DB의 database/migrations SQL과 기업 이메일 도메인 데이터를 적용합니다.'
  echo '테이블 초기 생성이나 공용 DB 변경에는 사용하지 않습니다.'
  exit 0
fi
source "$(dirname -- "${BASH_SOURCE[0]}")/local-env.sh"
if [[ "${POSTGRES_HOST:-}" != localhost && "${POSTGRES_HOST:-}" != 127.0.0.1 ]]; then
  echo '로컬 DB 설정에서만 실행할 수 있습니다. 공용 DB는 담당자와 SQL을 검토하세요.' >&2
  exit 1
fi
cd "$RESUIT_PROJECT_ROOT"
for migration in database/migrations/*.sql docs/migrations/2026-11-06-company-email-domains.sql; do
  echo "적용: $migration"
  docker compose exec -T db sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -X -v ON_ERROR_STOP=1' < "$migration"
done
