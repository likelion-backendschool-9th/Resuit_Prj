#!/usr/bin/env bash
# 다른 실행 스크립트에서 source한다. .env는 신뢰하는 로컬 파일만 사용한다.
RESUIT_PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ ! -f "$RESUIT_PROJECT_ROOT/.env" ]]; then
  echo '프로젝트 루트에 .env가 없습니다. .env.example을 복사하고 로컬 값을 입력하세요.' >&2
  exit 1
fi
set -a
source "$RESUIT_PROJECT_ROOT/.env"
set +a
export TZ="${TZ:-Asia/Seoul}"
