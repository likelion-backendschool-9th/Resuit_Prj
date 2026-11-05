# 공고 임시저장

기존 Job과 JobQuestion을 사용한다. 기업 권한은 DB의 `COMPANY`로 검사하며 작성자는 로그인 사용자로 결정한다.

| 요청 | 동작 |
| --- | --- |
| POST /api/v1/jobs/me/drafts | 새 DRAFT 생성, 201과 JobDetailResponse 반환 |
| GET /api/v1/jobs/me/drafts?page=0&size=20 | 본인의 삭제되지 않은 DRAFT 목록 |
| GET /api/v1/jobs/me/drafts/{jobId} | 본인 초안과 문항 조회 |
| PUT /api/v1/jobs/me/drafts/{jobId} | 같은 초안의 전체 편집 결과 재저장 |
| PATCH /api/v1/jobs/{jobId} | 기존 CreateJobRequest로 검증 후 같은 ID를 OPEN으로 게시 |
| DELETE /api/v1/jobs/{jobId} | 기존 본인 공고 삭제 API로 초안도 소프트 삭제 |

초안 전용 API는 로그인 필수이며 타인·삭제된·이미 게시된 초안은 초안 상세/재저장에서 404를 반환한다. 일반 사용자는 403이다. 공개 공고 조회는 OPEN/CLOSED만 반환한다. 일반·특화 지원 서비스 모두 DRAFT 지원을 거부한다.

## 임시저장 요청

```json
{
  "type": "SPECIAL",
  "title": "백엔드 개발자 채용",
  "description": "작성 중",
  "questions": [
    { "content": "", "charLimit": null }
  ]
}
```

- 제목과 유형은 필수이며 유형은 GENERAL/SPECIAL이다(대소문자 허용).
- 다른 공고 항목은 생략 가능하고 초안의 과거 마감일도 허용한다.
- 문항 목록은 생략 또는 null이면 빈 목록으로 저장한다. 문항 자체와 내용은 null일 수 없다.
- 빈 내용·공백, null/0/음수 charLimit 등 미완성 입력을 보존한다. 문항 수·유형별 완성 규칙은 게시 때 검사한다.
- DB 컬럼 제약을 유지한다: 제목 100자, 지역 50자, 고용 형태·경력 30자, 문항 내용 300자.
- PUT은 부분 수정이 아니다. 유지할 공고 필드와 문항 전체를 보내야 한다. 문항은 기존 방식대로 전체 교체되어 ID가 바뀐다.
- 게시 버튼은 전체 최종 값을 CreateJobRequest 형식으로 PATCH한다. 게시 검증에 실패하면 트랜잭션이 롤백되어 기존 초안을 보존한다.

초안은 공개 리소스와 접근 권한 및 검증 규칙이 달라 별도 편집 API로 분리했다. 프론트 버튼 연동은 이 변경에 포함하지 않는다.
