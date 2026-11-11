# 리뷰 시연 데이터 수동 이관

`review-demo-transfer.sql`은 2026-11-10 로컬 테스트 데이터의 스냅샷이다.
실제 이용자의 활동이나 실적이 아닌 가상 데이터이며 팀 시연 용도로만 사용한다.
앱 변경이나 재시작은 필요하지 않다. 자동 배포/마이그레이션에서는 실행하지 않는다.

## 포함 범위

- 가상 회원 19명(리뷰어 18명 + 연결에 필요한 `local-review-demo` 1명)
- 이력서 12개, 피드백 요청 12개, 리뷰 54개, 선정 기록 54개
- 가상 리뷰어가 남긴 좋아요만 포함. 실제 회원의 좋아요와 비밀번호·이메일은 제외
- 순위 구성과 날짜는 검증한 로컬 스냅샷을 보존한다. 주간 최신 시작일 2026-11-02, 월간 2026-11-01

## 실행

사용자 요청에 따라 백업 단계는 생략한다. 파일 경로는 저장소 루트 기준이다.
이 컴퓨터의 저장소 터미널에서 외부 서버로 파일을 복사한다.

```bash
scp database/manual/review-demo-transfer.sql team3-resuit@cloud.newlecture.com:~/review-demo-transfer.sql
```

이후 접속해 둔 **외부 서버 터미널**에서 시험 실행한다. 정상 종료해도 ROLLBACK이므로 데이터가 남지 않는다.

```bash
docker exec -i resuit-db-1 sh -c 'exec psql -X -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < ~/review-demo-transfer.sql
```

`DRY RUN: all demo inserts rolled back` 출력과 종료 코드 0을 확인한 다음 실제 적용한다.
오류가 있으면 실제 적용하지 않는다.

```bash
docker exec -i resuit-db-1 sh -c 'exec psql -X -v apply=true -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < ~/review-demo-transfer.sql
```

`APPLIED: demo data committed`가 출력되어야 적용 완료다.
`ON_ERROR_STOP`과 단일 트랜잭션을 사용하므로 오류 시 연결이 종료되며 전체 롤백된다.
기존 행을 UPDATE/DELETE하거나 충돌을 무시하지 않는다. 재실행 시 중복 오류로 중단된다.
짧은 테이블 쓰기 잠금 중 다른 쓰기 요청은 대기할 수 있으며, 잠금 획득이 5초를 넘으면 중단한다.
COMMIT 후에는 ROLLBACK으로 취소할 수 없다. 이후 삭제는 추가 활동과 연결 관계를 확인해 별도 작업한다.

## 확인

- `/reviews/best`: 주간 3개, 월간 3개
- `/reviews/hall-of-fame`: 시연 리뷰어 18명(다른 선정 데이터가 없는 경우)
- DB 초기화 시 시연 데이터도 사라진다.

```bash
curl -fsS 'http://resuit.newlecture.com:3003/api/v1/reviews/best?period=WEEKLY'
curl -fsS 'http://resuit.newlecture.com:3003/api/v1/reviews/hall-of-fame?page=0&size=20'
```

## 검증 범위

전달받은 서버 컬럼 55개·인덱스 11개 및 기본값/IDENTITY 설정을 로컬과 비교했다.
로컬 스키마 복제본에 기존 리뷰 3개를 둔 임시 DB에서 시험/적용/롤백/ID 충돌/중복 실행과
실제 Repository의 베스트·명예의 전당 조회 SQL을 검사했다.
서버 전체 덤프 복제 검사는 아니며, 공용 서버의 시험 실행이 최종 호환성 검사다.

테스트 ID 900001~900054는 명시 삽입하며 기존 ID 시퀀스는 변경하지 않는다.
따라서 향후 자동 ID가 이 범위에 도달하기 전 시연 데이터를 정리하거나 별도 시퀀스 조정이 필요하다.
