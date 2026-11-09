package com.newlecture.resuit.dto.resume;

import java.util.Map;

// 특화 이력서 수정(PATCH). null인 값은 바꾸지 않는다.
// content.answers: [{ "questionId": 문항 ID, "answer": "답변" }]
// photoUrl: null이면 그대로, ""이면 삭제, 업로드한 임시 경로(tmpImage/...)면 저장
public record TargetResumeUpdateRequest(String title, Map<String, Object> content, String photoUrl) {}
