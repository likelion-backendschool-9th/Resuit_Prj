# 개인 개발 환경 실행

현재 프로젝트의 Compose DB를 사용하는 로컬 실행 안내다. 공용·운영 DB 초기화용이 아니다.

## 환경 준비

```bash
cp .env.example .env
```

이미 .env가 있으면 덮어쓰지 않는다. 파일에서 개인 DB 사용자·비밀번호·포트를 설정한다. 비밀번호에 #, !, 공백 등이 있으면 작은따옴표로 감싼다. .env는 Git에서 제외되며 팀에 공유하지 않는다. 실행 스크립트가 shell로 읽으므로 신뢰하는 로컬 설정만 넣는다.

기본 예제는 DB 5433, backend 8081, frontend 5174다. 다른 프로그램이 포트를 쓰고 있으면 .env에서 변경한다. TZ는 Asia/Seoul을 사용한다. Docker 내부 DB 포트는 항상 5432이며 Compose가 이를 외부 포트와 연결한다.

```bash
docker compose up -d --build db
```

기존 볼륨이 있으면 .env의 비밀번호를 바꿔도 DB 계정 비밀번호가 자동 변경되지 않는다. 인증 실패 시 기존 계정을 확인하고 볼륨을 임의로 삭제하지 않는다.

## 처음 만든 빈 DB에서만 테이블 생성

기존 테이블·데이터가 있는 DB는 이 단계를 건너뛴다. 제외하기로 한 docs/erdcloud/resuit_erd_import.sql은 실행하지 않는다.

```bash
# 프로젝트 루트
set -a
source .env
set +a
export TZ=Asia/Seoul
cd backend/resuit
SPRING_JPA_HIBERNATE_DDL_AUTO=update RESUIT_REVIEWS_SELECTION_ENABLED=false ./gradlew bootRun --args='--server.port=8081'
```

서버가 정상 시작되면 Ctrl+C로 종료한다. 엔티티 기준 테이블만 생성된 상태이며, 추가 DB 제약과 선정 완료 테이블은 다음 migration으로 적용한다. 자동 선정은 초기화 동안 비활성화해 아직 없는 테이블을 조회하지 않도록 한다.

## 추가 DB 변경·실행

프로젝트 루트로 돌아와 실행한다.

```bash
bash scripts/migrate-local-db.sh
bash scripts/dev-backend.sh
```

다른 터미널에서:

```bash
bash scripts/dev-frontend.sh
```

migration 스크립트는 현재 Compose db 컨테이너에 `database/migrations/*.sql`을 파일명 순서대로 모두 적용하고, 기업 이메일 도메인 초기 데이터 파일도 적용한다. 이미 적용된 변경은 다시 추가하지 않는다. 파일마다 트랜잭션이며 한 파일이 실패하면 이후 파일은 실행하지 않는다. 기존 중복 채택·선정 때문에 실패한 경우 데이터를 삭제하지 말고 원인을 확인한다.

평소 backend 스크립트는 항상 ddl-auto=validate를 사용한다. 테이블이나 컬럼이 없으면 기동 실패로 알려 주며 자동으로 변경하지 않는다. 스케줄러는 기본 활성화다. 개인 실험에서 비활성화하려면 .env에 RESUIT_REVIEWS_SELECTION_ENABLED=false를 넣는다.

주소:

- http://localhost:5174/reviews/best
- http://localhost:5174/reviews/hall-of-fame
- http://localhost:5174/profile/verification

RESUIT_BACKEND_PORT·RESUIT_FRONTEND_PORT를 변경했다면 해당 포트를 사용한다. BACKEND_URL을 따로 지정하지 않으면 frontend 스크립트가 백엔드 포트에 맞춰 자동 설정한다. 프록시 설정을 바꾼 뒤에는 프론트를 재시작한다.

## 가상 데이터가 필요한 경우

개인 DB에서만 실행한다. 이미 만든 가상 리뷰의 채택 상태와 기간 날짜가 다시 설정될 수 있다.

```bash
docker compose exec -T db sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -X -v ON_ERROR_STOP=1' < database/local/review-demo.sql
```

프론트 mock.js를 사용하는 것이 아니라 DB에 가상 기록을 저장하고 API로 조회한다.

## 팀 연동 대기

소셜 로그인 앱 키와 SMTP 설정이 준비되지 않으면 인증이 필요한 좋아요·채택·메일 발송은 401을 반환한다. 조회 화면은 이용할 수 있다. [기업 이메일 인증](company-verification.md)과 [담당 기능 연동 현황](implementation-status.md)을 참고한다.

소셜 로그인 앱 등록·콜백·키 설정은 [소셜 로그인](social-login.md)을 따른다.
