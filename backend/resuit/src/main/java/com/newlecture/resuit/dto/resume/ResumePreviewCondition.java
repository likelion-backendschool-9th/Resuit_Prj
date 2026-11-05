package com.newlecture.resuit.dto.resume;

// 이력서 미리보기(4.5). 공통·특화 이력서는 테이블이 달라 id가 겹칠 수 있어 type으로 구분한다.
// type: COMMON, TARGET
public record ResumePreviewCondition(
    Long id,
    String type
) {}
