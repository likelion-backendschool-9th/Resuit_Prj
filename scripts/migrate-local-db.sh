#!/usr/bin/env bash
set -euo pipefail
case "${1:-}" in
  --help)
    echo '사용법: bash scripts/migrate-local-db.sh [--list]'
    echo '개인 Compose DB에 database/migrations와 docs/migrations의 모든 SQL을 적용합니다.'
    echo '--list: DB에 연결하지 않고 실행 순서만 표시합니다.'
    echo '기존 테이블이 있는 DB용입니다. 빈 DB 초기화나 데모 데이터 입력은 하지 않습니다.'
    exit 0
    ;;
  ''|--list) ;;
  *) echo "알 수 없는 옵션: $1" >&2; exit 1 ;;
esac
project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"
# 파일명 정렬로 새 SQL을 자동 포함한다. 같은 번호라도 파일이 다르면 모두 실행한다.
# 실행 이력을 저장하지 않으므로 추가 SQL도 재실행 가능하게 작성해야 한다.
export LC_ALL=C
shopt -s nullglob
migrations=(database/migrations/*.sql)
doc_migrations=(docs/migrations/*.sql)
# 프로필 인덱스는 배지 마이그레이션이 추가하는 user_badge.status를 참조한다.
badge_migration='docs/migrations/2026-11-04-reviewer-badge-renewal.sql'
if [[ -f "$badge_migration" ]]; then
  migrations+=("$badge_migration")
fi
for migration in "${doc_migrations[@]}"; do
  [[ "$migration" == "$badge_migration" ]] || migrations+=("$migration")
done
if [[ "${1:-}" == '--list' ]]; then
  printf '%s\n' "${migrations[@]}"
  exit 0
fi
source "$project_root/scripts/local-env.sh"
if [[ "${POSTGRES_HOST:-}" != localhost && "${POSTGRES_HOST:-}" != 127.0.0.1 ]]; then
  echo '로컬 DB 설정에서만 실행할 수 있습니다. 공용 DB는 담당자와 SQL을 검토하세요.' >&2
  exit 1
fi
for migration in "${migrations[@]}"; do
  echo "적용: $migration"
  docker compose exec -T db sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -X -v ON_ERROR_STOP=1' < "$migration"
done
