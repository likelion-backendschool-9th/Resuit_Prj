#!/usr/bin/env bash
set -euo pipefail
if [[ "${1:-}" == '--help' ]]; then
  echo '사용법: bash scripts/migrate-local-db.sh'
  echo '개인 Compose DB의 database/migrations SQL과 아래 목록의 docs/migrations SQL을 순서대로 적용합니다.'
  echo '기업 이메일 → 인재 필터 → 기업 요금제 통합 → 현직자 인증 → 이력서 사진'
  echo '테이블 초기 생성이나 공용 DB 변경에는 사용하지 않습니다.'
  exit 0
fi
source "$(dirname -- "${BASH_SOURCE[0]}")/local-env.sh"
if [[ "${POSTGRES_HOST:-}" != localhost && "${POSTGRES_HOST:-}" != 127.0.0.1 ]]; then
  echo '로컬 DB 설정에서만 실행할 수 있습니다. 공용 DB는 담당자와 SQL을 검토하세요.' >&2
  exit 1
fi
cd "$RESUIT_PROJECT_ROOT"
# 번호 기반 마이그레이션은 새 파일도 자동으로 포함한다.
# docs/migrations의 2026-11-04 수동 SQL은 재실행할 수 없으므로 glob으로 포함하지 않는다.
# 인재 필터가 만드는 구 요금제 테이블을 요금제 통합이 이전하므로 이 순서를 유지한다.
migrations=(
  database/migrations/*.sql
  docs/migrations/2026-11-06-company-email-domains.sql
  docs/migrations/2026-11-06-talent-filters.sql
  docs/migrations/2026-11-09-company-plan-periods.sql
  docs/migrations/2026-11-10-mentor-verification.sql
  docs/migrations/2026-11-10-resume-photo.sql
  docs/migrations/2026-11-10-drop-mentor-career-years.sql
)
for migration in "${migrations[@]}"; do
  echo "적용: $migration"
  docker compose exec -T db sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -X -v ON_ERROR_STOP=1' < "$migration"
done
