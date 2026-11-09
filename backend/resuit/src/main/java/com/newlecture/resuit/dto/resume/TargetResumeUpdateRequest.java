package com.newlecture.resuit.dto.resume;

import java.util.Map;

// 특화 이력서 수정(PATCH). null인 값은 바꾸지 않는다.
// content.answers: [{ "questionId": 문항 ID, "answer": "답변" }]
public record TargetResumeUpdateRequest(String title, Map<String, Object> content, String photoUrl) {}
